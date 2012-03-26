package betel.spacedefender;

import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.Material;

public class Bullet extends GameObject {

	public Bullet(Geometry geometry, Material material) {
		super(geometry, material);
		
		mass = 1f;
	}
	
}
