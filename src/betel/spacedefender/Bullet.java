package betel.spacedefender;

import betel.alw3d.math.Vector3f;
import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.Material;
import betel.alw3d.renderer.MovableGeometryNode;

public class Bullet extends MovableGeometryNode {

	public Bullet(Geometry geometry, Material material) {
		super(geometry, material);
	}

	public boolean isInUse = false;
	
	//public float mass = 1f;
	//public boolean isExplosive = false;
	//public float explosionPower = 10f;
	
	public void reset() {
		getTransform().getRotation().fromAngleNormalAxis(0f, Vector3f.UNIT_X);
		getTransform().getPosition().multThis(0f);
		getMovement().getRotation().fromAngleNormalAxis(0f, Vector3f.UNIT_X);
		getMovement().getPosition().multThis(0f);
		detachFromParent();
		isInUse = false;
	}
}
