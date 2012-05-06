package betel.spacedefender;

import android.util.FloatMath;
import betel.alw3d.math.Vector3f;
import betel.alw3d.procedurals.Procedural;
import betel.alw3d.procedurals.fBm;

public class TerrainWeightProcedural implements Procedural {
	
	private float a0;
	private float a1;
	//private final float a2 = 0;
	
	public TerrainWeightProcedural(float a0, float a1) {
		this.a0 = a0;
		this.a1 = a1;
	}

	@Override
	public double getValue(double x, double y, double z, double resolution) {
		return getValueNormal(x, y, z, resolution, null);
	}

	@Override
	public double getValueNormal(double x, double y, double z,
			double resolution, Vector3f normal) {
		
		float norm = 1f/(1f+a1*a1);
		
		normal.x = 0;
		normal.y = 1*norm;
		normal.z = a1*norm;
		
		return a0 + a1 * y;
	}

}
