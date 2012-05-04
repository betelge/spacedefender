package betel.spacedefender;

import android.util.FloatMath;
import betel.alw3d.math.Procedural;
import betel.alw3d.math.Vector3f;
import betel.alw3d.procedurals.fBm;

public class TerrainProcedural implements Procedural {
		
	private final long seed;
	fBm hills;
	
	private final float a0 = 0.1f;
	private final float a1 = 0.3f;
	//private final float a2 = 0;
	
	public TerrainProcedural(long seed) {
		this.seed = seed;
		hills = new fBm(seed);
	}
	
	private double height(double x, double y) {
		return a0 + a1 * y;
	}
	
	private void modifyNormal(Vector3f normal) {
		/*float normalization = 1f / (1f + a1*a1);
		normal.y += 1f * normalization; //TODO: wtf?
		normal.x += a1 * normalization;
		normal.normalizeThis();*/
		
		float v = (float) Math.atan(a0);
		
		float x = normal.x * FloatMath.cos(v) - normal.y * FloatMath.sin(v);
		normal.y = normal.x * FloatMath.sin(v) + normal.y * FloatMath.cos(v);
		normal.x = x;
	}

	@Override
	public double getValue(double x, double y, double z, double resolution) {
		return getValueNormal(x, y, z, resolution, null);
	}

	@Override
	public double getValueNormal(double x, double y, double z,
			double resolution, Vector3f normal) {
		
		double w = height(x,y);
		
		double value = w * hills.getValueNormal(x, y, z, resolution, normal);
		modifyNormal(normal);
		normal.x *= w;
		normal.normalizeThis();
		
		return value;
	}

}
