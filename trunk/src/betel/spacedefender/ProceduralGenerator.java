package betel.spacedefender;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import betel.alw3d.Alw3d;
import betel.alw3d.math.Vector3f;
import betel.alw3d.procedurals.fBm;
import betel.alw3d.renderer.Geometry;
import betel.alw3d.renderer.Geometry.Attribute;

public class ProceduralGenerator {
	
	static private final long seed = 1337l;
	static private final int size = 16;

	static public Geometry generateTerrain() {
		fBm fractal = new fBm(seed);
		
		IntBuffer indices = IntBuffer.allocate(size*size*6);
		
		Attribute atPosition = new Attribute();
		atPosition.name = "position";
		atPosition.size = 3;
		atPosition.type = Geometry.Type.FLOAT;
		FloatBuffer positions = FloatBuffer.allocate(3 * (size+1)*(size+1));
		atPosition.buffer = positions;
		
		Attribute atNormal = new Attribute();
		atNormal.name = "normal";
		atNormal.size = 3;
		atNormal.type = Geometry.Type.FLOAT;
		FloatBuffer normals = FloatBuffer.allocate(3 * (size+1)*(size+1));
		atNormal.buffer = normals;
		

		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				indices.put(i*(size+1) + j);
				indices.put(i*(size+1) + j + 1);
				indices.put(i*(size+1) + j + size + 1);
				
				indices.put(i*(size+1) + j + size + 1);
				indices.put(i*(size+1) + j + 1);
				indices.put(i*(size+1) + j + size + 2);
			}
		}
		
		for(int i = 0; i < size + 1; i++) {
			for(int j = 0; j < size + 1; j++) {		
				
				Vector3f normal = new Vector3f();
				float height = 0.5f * (float) fractal.getValueNormal((float)i/size, (float)j/size, 0,
						1f/size, normal);
				
				positions.put(-1f + 2f*i/size);
				positions.put(height);
				positions.put(-1f + 2f*j/size);
				
				normals.put(normal.x);
				normals.put(normal.y);
				normals.put(normal.z);
			}
		}
		indices.flip();
		positions.flip();
		normals.flip();
		
		List<Attribute> attributes = new LinkedList<Attribute>();
		attributes.add(atPosition);
		attributes.add(atNormal);
		
		return new Geometry(indices, attributes);
	}
}
