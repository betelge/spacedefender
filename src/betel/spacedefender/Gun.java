package betel.spacedefender;

import android.text.AndroidCharacter;
import betel.alw3d.math.Quaternion;
import betel.alw3d.math.Vector3f;
import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.Material;
import betel.alw3d.renderer.MovableGeometryNode;
import betel.alw3d.renderer.Node;

public class Gun extends Node {
	Gun(Geometry bulletGeometry, Material bulletMaterial, long fireDelay) {
		this.bulletGeometry = bulletGeometry;
		this.bulletMaterial = bulletMaterial;
		this.fireDelay = fireDelay;
	}
	
	Geometry bulletGeometry;
	Material bulletMaterial;
	
	long lastFired = 0;
	long fireDelay;
	
	/*private Runnable shootRunnable = new Runnable() {
		@Override
		public void run() {
			fire(System.currentTimeMillis());
			Thread.sleep(fireDelay);
		}
		
	};
	
	Thread shootThread = new Thread(shootRunnable);*/
	
	Quaternion aim = new Quaternion();
	
	Quaternion getAim() {
		return aim;
	}
	
	long getLastFired() {
		return lastFired;
	}
	
	void setLastFired(long time) {
		this.lastFired = time;
	}
	
	long getFireDelay() {
		return fireDelay;
	}
	
	void setFireDelay(long delay) {
		this.fireDelay = delay;
	}
	
	boolean isReady(long currentTime) {
		return (currentTime - lastFired >= fireDelay);
	}
	
	/*boolean fire() {
		MovableGeometryNode bullet = new MovableGeometryNode(bulletGeometry, bulletMaterial);
		bullet.getMovement().setPosition(aim.mult(Vector3f.UNIT_Y));
		return bullet;
	}*/
}
