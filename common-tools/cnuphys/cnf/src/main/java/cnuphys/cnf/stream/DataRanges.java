package cnuphys.cnf.stream;

/**
 * Maintains the limitsof the data that are in memory
 * @author davidheddle
 *
 */
public class DataRanges {
	public float rmin = Float.POSITIVE_INFINITY;
	public float rmax = Float.NEGATIVE_INFINITY;

	public float xmin = Float.POSITIVE_INFINITY;
	public float xmax = Float.NEGATIVE_INFINITY;
	public float ymin = Float.POSITIVE_INFINITY;
	public float ymax = Float.NEGATIVE_INFINITY;
	public float zmin = Float.POSITIVE_INFINITY;
	public float zmax = Float.NEGATIVE_INFINITY;
	public float bmin = Float.POSITIVE_INFINITY;
	public float bmax = Float.NEGATIVE_INFINITY;
	public float dmin = Float.POSITIVE_INFINITY;
	public float dmax = Float.NEGATIVE_INFINITY;


	public void newValue(float[] array) {

		float x = array[StreamManager.X];
		float y = array[StreamManager.Y];
		float z = array[StreamManager.Z];
		float bx = array[StreamManager.BX];
		float by = array[StreamManager.BY];
		float bz = array[StreamManager.BZ];

		xmin = Math.min(xmin, x);
		xmax = Math.max(xmax, x);
		ymin = Math.min(ymin, y);
		ymax = Math.max(ymax, y);
		zmin = Math.min(zmin, z);
		zmax = Math.max(zmax, z);
		
		dmin = Math.min(dmin, array[StreamManager.DEL]);
		dmax = Math.max(dmax, array[StreamManager.DEL]);


		float rsq = x * x + y * y + z * z;
		float r = (float) Math.sqrt(rsq);

		rmin = Math.min(rmin, r);
		rmax = Math.max(rmax, r);

		double magSq = bx * bx + by * by + bz * bz;
		float mag = (float) Math.sqrt(magSq);

		bmin = Math.min(bmin, mag);
		bmax = Math.max(bmax, mag);

	}

	@Override
	public String toString() {
		return String.format(
				"Ranges:\n x:[%6.3f, %6.3f]\n y:[%6.3f, %6.3f]\n z:[%6.3f, %6.3f]\n r:[%6.3f, %6.3f]\n b:[%6.3f, %6.3f]\n d:[%6.3f, %6.3f]",
				xmin, xmax, ymin, ymax, zmin, zmax, rmin, rmax, bmin, bmax, dmin, dmax);

	}
}
