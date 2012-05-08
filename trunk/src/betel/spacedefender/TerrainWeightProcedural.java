package betel.spacedefender;

import android.util.FloatMath;
import betel.alw3d.math.Vector3f;
import betel.alw3d.procedurals.Procedural;
import betel.alw3d.procedurals.fBm;

public class TerrainWeightProcedural implements Procedural {
	
	private float a0;
	private float a1;
	private float a2;
	
	public TerrainWeightProcedural(float a0, float a1, float a2) {
		this.a0 = a0;
		this.a1 = a1;
		this.a2 = a2;
	}

	@Override
	public double getValue(double x, double y, double z, double resolution) {
		return getValueNormal(x, y, z, resolution, null);
	}

	@Override
	public double getValueNormal(double x, double y, double z,
			double resolution, Vector3f gradient) {
				
		gradient.set(0f, a1 + a2/2*(float)y, 0f);
		
		return a0 + a1 * y;
	}

}
