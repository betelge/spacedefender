package betel.spacedefender;

import android.util.FloatMath;
import betel.alw3d.math.Vector3f;
import betel.alw3d.procedurals.MultProcedural;
import betel.alw3d.procedurals.Procedural;
import betel.alw3d.procedurals.fBm;

public class TerrainProcedural implements Procedural {
		
	private final long seed;
	MultProcedural terrain;
	
	private final float a0 = 0.2f;
	private final float a1 = 1f;
	private final float a2 = 1.5f;
	
	public TerrainProcedural(long seed) {
		this.seed = seed;
		fBm hills = new fBm(seed);
		TerrainWeightProcedural wProcedural = new TerrainWeightProcedural(a0, a1, a2);
		terrain = new MultProcedural(hills, wProcedural);
	}

	@Override
	public double getValue(double x, double y, double z, double resolution) {
		return getValueNormal(x, y, z, resolution, null);
	}

	@Override
	public double getValueNormal(double x, double y, double z,
			double resolution, Vector3f gradient) {
		return terrain.getValueNormal(x, y, z, resolution, gradient);
	}

}
