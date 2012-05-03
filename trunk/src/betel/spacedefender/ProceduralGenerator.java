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
	
	static private final long seed = 1336l;
	static private final int size = 64;

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
				
				float gain = 0f;
				Vector3f normal = new Vector3f();
				float x = 0.2f*(-1f + 2f*i/(float)size) * (0.2f + 10f*(float)(size-j)/(float)size);
				float y = (-1f + 2f*j/(float)size) * (0.1f + 3f*(float)(size-j)/(float)size);
				float height = (1f + gain) * (float) fractal.getValueNormal(
						0.5f*x, 0.5f*y, 0, 1f/size, normal);
				
				normal.multThis(1f + gain);
				
				// Only use the 2D gradient
				normal.z = 1;
				normal.normalizeThis();
				
				positions.put(x);
				positions.put(height);
				positions.put(y);
				
				normals.put(-normal.x);
				normals.put(normal.z);
				normals.put(-normal.y);
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
