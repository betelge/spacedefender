package betel.spacedefender;

import betel.alw3d.math.Quaternion;
import betel.alw3d.math.Vector3f;
import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.Material;
import betel.alw3d.renderer.MovableGeometryNode;

public class GameObject extends MovableGeometryNode {

	public GameObject(Geometry geometry, Material material) {
		super(geometry, material);
	}

	public boolean isInUse = false;
	
	public float mass = 1f;
	//public boolean isExplosive = false;
	//public float explosionPower = 10f;
	
	public void reset() {
		getTransform().getRotation().set(Quaternion.UNIT);
		getTransform().getPosition().multThis(0f);
		getMovement().getRotation().set(Quaternion.UNIT);
		getMovement().getPosition().multThis(0f);
		setLastTime(0);
		setNextTime(0);
		detachFromParent();
		isInUse = false;
	}
}
