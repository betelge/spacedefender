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
import betel.alw3d.math.Vector3f;
import betel.alw3d.renderer.CameraNode;
import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.GeometryNode;
import betel.alw3d.renderer.Material;
import betel.alw3d.renderer.Node;
import betel.alw3d.renderer.ShaderProgram;
import betel.alw3d.renderer.passes.ClearPass;
import betel.alw3d.renderer.passes.SceneRenderPass;
import android.app.Activity;
import android.os.Bundle;
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
        cameraNode.getTransform().setPosition(new Vector3f(0f,2f,10f));
        
        // Sphere
        Geometry sphereMesh = GeometryLoader.loadObj(R.raw.sphere);
        GeometryNode sphere = new GeometryNode(sphereMesh, null);
        sphere.getTransform().getPosition().set(model.spherePos, 0f, 0f);
        sphere.getTransform().getScale().multThis(2*model.sphereRadius);
        model.rootNode.attach(sphere);
        
        // Plane
        Geometry groundMesh = GeometryLoader.loadObj(R.raw.ground);
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
	    synchronized(model.ufos) {
			Iterator<Ufo> it = model.ufos.iterator();
			while(it.hasNext()) {
				Ufo ufo = it.next();
				if(ufo.getTransform().getPosition().getLength() > 10)
					ufo.reset();
			}
	    }
	    
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
			model.ufos.add(ufo);
		}
		
		ufo.getTransform().getPosition().set(10f*(rand.nextFloat()-0.5f),10f,0f);
		Vector3f speed = ufo.getMovement().getPosition();
		ufo.getTransform().getScale().set(0.4f, 0.4f, 0.4f);
		speed.set(0.1f*(rand.nextFloat()-0.5f), -0.1f*(rand.nextFloat()+0.1f), 0f);
		
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
				model.bullets.add(bullet);
			}
			
			bullet.getTransform().getPosition().set(0f,0.5f,0f);
			Vector3f speed = bullet.getMovement().getPosition();
			bullet.getTransform().getScale().set(0.15f, 0.1f, 0.1f);
			speed.set(0f, 0.2f, 0f);
			model.gun.getAim().mult(speed, speed);
			
			Log.d(LOG_TAG,"Bullet speed: " + speed + " Abs: " + speed.getLength());
			
			bullet.isInUse = true;
			model.rootNode.attach(bullet);
		}
    }
    
    @Override
    public void onSimulationTick(long time) {
    	if(time > model.timeForNextUfoSpawn) {
    		model.timeForNextUfoSpawn = time + (long)(4000000000l * rand.nextFloat()); // nanoseconds
    		spawn();
    	}
    }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(v == view) {
			int action = event.getAction();
			if ( action == MotionEvent.ACTION_DOWN ) {
				// TODO: Will only work with 800x480 resolutions!!!
				float xPos = (event.getX() - 240) / 480;
				float yPos = -(event.getY() - 500) / 800;
				
				model.gun.getAim().fromAngleNormalAxis(-(float)Math.atan2(xPos, yPos), Vector3f.UNIT_Z);

				Log.d(LOG_TAG, "Action down: (" + xPos + "," + yPos + ")");
				fire(event.getEventTime());
				return true;
			}
			else if ( action == MotionEvent.ACTION_MOVE ) {
				// TODO: Will only work with 800x480 resolutions!!!
				float xPos = (event.getX() - 240) / 480;
				float yPos = -(event.getY() - 400) / 800;
				
				model.gun.getAim().fromAngleNormalAxis(-(float)Math.atan2(xPos, yPos), Vector3f.UNIT_Z);

				Log.d(LOG_TAG, "Action move: (" + xPos + "," + yPos + ")");
				fire(event.getEventTime());
				return true;
			}
		}
		return true;
	}
}