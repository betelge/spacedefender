package betel.spacedefender;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import utils.GeometryLoader;
import utils.ShaderLoader;
import utils.StringLoader;
import betel.alw3d.Alw3dOnSimulationListener;
import betel.alw3d.Alw3dSimulation;
import betel.alw3d.Alw3dSimulator;
import betel.alw3d.Alw3dView;
import betel.alw3d.SphereVolume;
import betel.alw3d.Volume;
import betel.alw3d.math.Transform;
import betel.alw3d.math.Vector3f;
import betel.alw3d.renderer.CameraNode;
import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.GeometryNode;
import betel.alw3d.renderer.Light;
import betel.alw3d.renderer.Material;
import betel.alw3d.renderer.Node;
import betel.alw3d.renderer.ShaderProgram;
import betel.alw3d.renderer.passes.ClearPass;
import betel.alw3d.renderer.passes.SceneRenderPass;
import android.app.Activity;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class SpacedefenderActivity extends Activity implements OnTouchListener, Alw3dOnSimulationListener {
	
	String LOG_TAG = "SDEFENDER";

	private Model model;
	private Alw3dView view;
	
	Random rand = new Random();
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        model = new Model();
        view = new Alw3dView(this, model);
        view.setOnTouchListener(this);

        
        setContentView(view/*R.layout.main*/);
        
        GeometryLoader.setContext(this);
        StringLoader.setContext(this);
        ShaderProgram.DEFAULT = ShaderLoader.loadShaderProgram(R.raw.default_v, R.raw.default_f);
        
        // Root Node
        model.rootNode = new Node();
        
        // Camera
        CameraNode cameraNode = new CameraNode(60f, 0, 0.1f, 1000f);
        model.rootNode.attach(cameraNode);
        cameraNode.getTransform().setPosition(new Vector3f(0f,4f,10f));
        cameraNode.setVolume(cameraNode.generateFrustumVolume());
        model.currentCameraNode = cameraNode;
        
        // Make a kill frustum that's bigger then the camera frustum
        model.killFrustum = cameraNode.generateFrustumVolume();
        for(int i = 0; i < model.killFrustum.distances.length; i++) {
        	model.killFrustum.distances[i] -= 5;
        }
        cameraNode.attach(model.killFrustum);
        
        // Light
        Light light = new Light();
        light.getTransform().getPosition().set(100, 100, 100);
        model.rootNode.attach(light);
        
        // Sphere
        Geometry sphereMesh = GeometryLoader.loadObj(R.raw.sphere);
        GeometryNode sphere = new GeometryNode(sphereMesh, null);
        sphere.getTransform().getPosition().set(model.spherePos, 0f, 0f);
        sphere.getTransform().getScale().multThis(2*model.sphereRadius);
        model.rootNode.attach(sphere);
        
        // Terrain
        Geometry groundMesh = ProceduralGenerator.generateTerrain();
        GeometryNode ground = new GeometryNode(groundMesh, null);
        model.rootNode.attach(ground);
        ground.getTransform().getScale().multThis(3.5f);
        
        SceneRenderPass sceneRenderPass = new SceneRenderPass(model.rootNode, cameraNode);
    	model.addRenderPass(new ClearPass(ClearPass.COLOR_BUFFER_BIT | ClearPass.DEPTH_BUFFER_BIT, null));
        model.addRenderPass(sceneRenderPass);
        
        // Game
        model.gun = new Gun(sphereMesh, Material.DEFAULT, 100);
        model.bulletGeometry = sphereMesh;
        model.bulletMaterial = Material.DEFAULT;
        sphere.attach(model.gun);
        
        model.ufoGeometry = sphereMesh;
        model.ufoMaterial = Material.DEFAULT;
        
        Set<Node> simNodes = new HashSet<Node>();
        simNodes.add(model.rootNode);
        Alw3dSimulator simulator = new Alw3dSimulator(simNodes);
    	simulator.setSimulation(new Alw3dSimulation(50));
    	simulator.setOnSimulationListener(this);
    	model.setSimulator(simulator);
    	Thread.yield(); // TODO: Race condition?
    	simulator.start();
    }
    
    private void spawn() {
	    	    
	    Ufo ufo = null;
		
		synchronized(model.ufos) {
			Iterator<Ufo> it2 = model.ufos.iterator();
			while(it2.hasNext()) {
				ufo = it2.next();
				if(ufo.isInUse)
					ufo = null;
				else
					break;
			}
		}
		
		if(ufo == null) {
			ufo = new Ufo(model.ufoGeometry, model.ufoMaterial);
			ufo.setVolume(new SphereVolume(0.4f));
			model.ufos.add(ufo);
		}
		
		float size = rand.nextFloat();
		size *= size*size*size * 2;
		size += 0.25;
		
		ufo.getTransform().getPosition().set(10f*(rand.nextFloat()-0.5f),10f+size,0f);
		Vector3f speed = ufo.getMovement().getPosition();
		ufo.getTransform().getScale().set(size, size, size);
		
		Volume v = ufo.getVolume();
		if(v instanceof SphereVolume) {
			((SphereVolume)v).setRadius(size);
		}
		
		ufo.mass = 100*size*size;
		speed.set(0.05f*(rand.nextFloat()-0.5f), -0.1f*(rand.nextFloat()+0.1f), 0f);
		
		ufo.isInUse = true;
		model.rootNode.attach(ufo);
	}
    
    private void fire(long time) {    	
    	synchronized(model.bullets) {
			Iterator<Bullet> it = model.bullets.iterator();
			while(it.hasNext()) {
				Bullet bullet = it.next();
				if(bullet.getTransform().getPosition().getLength() > 10)
					bullet.reset();
			}
		}
				
		if (model.gun.isReady(time)) {
		
			model.gun.setLastFired(time);
			
			Bullet bullet = null;
			
			synchronized(model.bullets) {
				Iterator<Bullet> it2 = model.bullets.iterator();
				while(it2.hasNext()) {
					bullet = it2.next();
					if(bullet.isInUse)
						bullet = null;
					else
						break;
				}
			}
			
			if(bullet == null) {
				bullet = new Bullet(model.bulletGeometry, model.bulletMaterial);
				bullet.setVolume(new SphereVolume(0.1f));
				synchronized(model.bullets) {
					model.bullets.add(bullet);
				}
			}
			
			// TODO: separate gun from gun sphere
			//bullet.getTransform().getPosition().set(0f,0.5f,0f);
			Vector3f speed = bullet.getMovement().getPosition();
			bullet.getTransform().getScale().set(0.1f, 0.1f, 0.1f);
			speed.set(0f, model.bulletspeed, 0f);
			bullet.mass = 0.4f;
			//model.gun.getAim().mult(speed, speed);
			model.gun.getTransform().getRotation().mult(speed, speed);
			
			bullet.isInUse = true;
			model.rootNode.attach(bullet);
		}
    }
    
    private Vector3f collisionPoint = new Vector3f();
    @Override
    public void onSimulationTick(long time) {
    	
    	// Fire gun
    	if(model.gun.isTriggerPressed) {
    		fire(time / 1000000); // Try firing gun, converting nanoseconds into milliseconds
    	}
    	
    	// Spawn new ufos
    	if(time > model.timeForNextUfoSpawn) {
    		model.timeForNextUfoSpawn = time + (long)(4000000000l * rand.nextFloat()); // nanoseconds
    		spawn();
    	}
    	
    	// Reset bullets that are far away 
    	synchronized(model.bullets) {
    		for(Bullet bullet : model.bullets) {
    			if(!bullet.isInUse)
	    			continue;
    			
    			if(!model.killFrustum.isCollidedWith(bullet.getVolume())) {
	    			bullet.reset();
	    			continue;
	    		}
    		}
    	}
    	
    	synchronized(model.ufos) {
	    	for(Ufo ufo : model.ufos) {
	    		if(!ufo.isInUse)
	    			continue;
	    		
	    		if(!model.killFrustum.isCollidedWith(ufo.getVolume())) {
	    			Log.d(LOG_TAG, "Resetting ufo at: " + ufo.getTransform().getPosition() + " with speed: " +
	    					ufo.getMovement().getPosition());
	    			ufo.reset();
	    			continue;
	    		}
	    		
	        	// Check for bullet/ufo collisions.
	    		synchronized(model.bullets) {
		    		for(Bullet bullet : model.bullets) {
		    			if(!bullet.isInUse)
			    			continue;
		    			
		    			Volume v1 = bullet.getVolume();
		    			Volume v2 = ufo.getVolume();
		    			if(v1 != null && v2 != null && bullet.isInUse && ufo.isInUse) {
			    			if(v1.isCollidedWith(v2, collisionPoint)) {
			    				/*bullet.reset();
			    				ufo.reset();*/
			    				
			    				bounceBalls(bullet, ufo, collisionPoint, 0.6f);
			    			}
		    			}
		    		}
	    		}
	    		
	    		// check for ufo/ufo collisions.
	    		for(Ufo ufo2 : model.ufos) {
	    			if(!ufo2.isInUse || ufo == ufo2)
	    				continue;
	    			
	    			Volume v1 = ufo.getVolume();
	    			Volume v2 = ufo2.getVolume();
	    			if(v1 != null & v2 != null) {
	    				if(v1.isCollidedWith(v2, collisionPoint)) {
	    					bounceBalls(ufo, ufo2, collisionPoint, 0.4f);
	    				}
	    			}
	    		}
	    	}
    	}
    }
    
    // TODO: not thread safe
    private Vector3f normal = new Vector3f();
    private final float SMALL_NUMBER = 0.001f;
    private void bounceBalls(GameObject ball1, GameObject ball2, Vector3f collisionPoint, float C) {
    	// collisionPoint is relative to the ball1 center.
    	// It's also orthogonal to the collision plane.
    	//Vector3f normal;// = collisionPoint;
    	// TODO: Why was collisionPoint wrong?
    	normal.set(ball2.getAbsoluteTransform().getPosition());
    	normal.subThis(ball1.getAbsoluteTransform().getPosition());
    	normal.normalizeThis();
    	
    	// Move spheres out of each other if needed
    	if(ball1.getVolume() instanceof SphereVolume && ball2.getVolume() instanceof SphereVolume) {
	    	float distance = ball1.getAbsoluteTransform().getPosition().getDistance(
	    						ball2.getAbsoluteTransform().getPosition());
	    	
	    	float overlap = -distance + ((SphereVolume)ball1.getVolume()).getRadius() +
	    			((SphereVolume)ball2.getVolume()).getRadius() + SMALL_NUMBER;
		    if(overlap >= 0) {
			   	
			   	// TODO: Only works if ball has root as parent
			   	ball1.getTransform().getPosition().addMultThis(normal, -overlap*ball2.mass/(ball1.mass+ball2.mass));
			   	ball2.getTransform().getPosition().addMultThis(normal, overlap*ball1.mass/(ball1.mass+ball2.mass));
		    }
    	}
    	
    	Vector3f b1Speed = ball1.getMovement().getPosition();
    	Vector3f b2Speed = ball2.getMovement().getPosition();
    	
    	
    	
    	// Here we get the 1-dimensional case
    	float u1 = b1Speed.dot(normal);
    	float u2 = b2Speed.dot(normal);
    	float m1 = ball1.mass;
    	float m2 = ball2.mass;
    	
    	float v1 = (m1*u1 + m2*u2 + m2*C*(u2-u1))/(m1+m2);
    	float v2 = (m1*u1 + m2*u2 + m1*C*(u1-u2))/(m1+m2);
    	
    	// Add the changes back to the velocities
    	b1Speed.addMultThis(normal, v1-u1);
    	b2Speed.addMultThis(normal, v2-u2);
    }

    // TODO: Should this be thread safe?
    private Vector3f rayDir = new Vector3f();
    private Vector3f rayStart = new Vector3f();
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(v == view) {
			int action = event.getAction();
			
			float w = model.getWidth();
			float h = model.getHeight();
			
			rayDir.set( -(event.getX() - w/2) / h,
						(event.getY() - h/2) / h,
						0.5f*FloatMath.sin((float)Math.PI/2 - (float)Math.PI/180*model.currentCameraNode.getFov()/2) /
							FloatMath.sin((float)Math.PI/180*model.currentCameraNode.getFov()/2) );
			
			Transform cameraTransform = model.currentCameraNode.getAbsoluteTransform();
			rayStart.set(cameraTransform.getPosition());
			cameraTransform.getRotation().mult(rayDir, rayDir);
			
			rayStart.subThis(model.gun.getAbsoluteTransform().getPosition());
			
			float projection = rayDir.dot(Vector3f.UNIT_Z);
			if(projection == 0) {
				// Parallel to the plane
				projection = 0.0000001f;
			}
			
			float t = -rayStart.dot(Vector3f.UNIT_Z) / projection;
			
			// This is the 3D point intersection between the click and the 2D game plane.
			rayDir.multThis(t);
			rayDir.addThis(rayStart);
			rayDir.subThis(model.gun.getTransform().getPosition());
							
			if ( action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE ) {
				float angle = -(float)Math.atan2(rayDir.x, rayDir.y);
				// This would reverse the aim
				//angle += (float)Math.PI;
				model.gun.getTransform().getRotation().fromAngleNormalAxis(angle, Vector3f.UNIT_Z);

				model.gun.isTriggerPressed = true;
				fire(event.getEventTime());
			}
			/*else if ( action == MotionEvent.ACTION_MOVE ) {
			}*/
			else if ( action == MotionEvent.ACTION_UP ) {
				model.gun.isTriggerPressed = false;
			}
		}
		return true;
	}
}
