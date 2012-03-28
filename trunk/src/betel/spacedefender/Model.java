package betel.spacedefender;

import java.util.Collection;
import java.util.HashSet;

import betel.alw3d.Alw3dModel;
import betel.alw3d.FrustumVolume;
import betel.alw3d.renderer.CameraNode;
import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.GeometryNode;
import betel.alw3d.renderer.Material;
import betel.alw3d.renderer.Node;

public class Model extends Alw3dModel {
	public float spherePos = 0;
	public float sphereRadius = 0.5f;
		
	public Geometry bulletGeometry;
	public Material bulletMaterial;
	public Geometry ufoGeometry;
	public Material ufoMaterial;
	
	public long timeForNextUfoSpawn = 0;
	
	public Collection<Bullet> bullets = new HashSet<Bullet>();
	Gun gun;
	
	public Collection<Ufo> ufos = new HashSet<Ufo>();
	
	Node rootNode;
	
	public FrustumVolume killFrustum;
	public CameraNode currentCameraNode;
	
	public GeometryNode testSphere;
}
