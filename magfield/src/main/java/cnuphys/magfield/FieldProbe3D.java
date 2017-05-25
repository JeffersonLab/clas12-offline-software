package cnuphys.magfield;

/**
 *
 * @author gavalian
 */
public class FieldProbe3D extends FieldProbe {

	double q1_min = 0.0;
	double q1_max = 0.0;
	double q2_min = 0.0;
	double q2_max = 0.0;
	double q3_min = 0.0;
	double q3_max = 0.0;

	double b1_000 = 0.0;
	double b1_001 = 0.0;
	double b1_010 = 0.0;
	double b1_100 = 0.0;
	double b1_011 = 0.0;
	double b1_110 = 0.0;
	double b1_101 = 0.0;
	double b1_111 = 0.0;

	double b2_000 = 0.0;
	double b2_001 = 0.0;
	double b2_010 = 0.0;
	double b2_100 = 0.0;
	double b2_011 = 0.0;
	double b2_110 = 0.0;
	double b2_101 = 0.0;
	double b2_111 = 0.0;

	double b3_000 = 0.0;
	double b3_001 = 0.0;
	double b3_010 = 0.0;
	double b3_100 = 0.0;
	double b3_011 = 0.0;
	double b3_110 = 0.0;
	double b3_101 = 0.0;
	double b3_111 = 0.0;

	/**
	 * Check whether the point is contained in this
	 * probe. A probe corresponds to a grid cell.
	 * @param q1 the value of the first coordinate
	 * @param q2 the value of the second coordinate
	 * @param q3 the value of the third coordinate
	 * @return <code>true</code> of caching is on and the coordinate is in
	 * the cell corresponding to this probe.
	 */
	public boolean contains(double q1, double q2, double q3) {
		if (!CACHE) {
			return false; //will prevent caching
		}
		return (q1 >= q1_min && q1 <= q1_max) && (q2 >= q2_min && q2 <= q2_max) && (q3 >= q3_min && q3 <= q3_max);
	}

	/**
	 * Interpolate the field
	 * @param q1
	 * @param q2
	 * @param q3
	 * @param scale scale factor
	 * @return the field
	 */
	public void evaluate(double q1, double q2, double q3, float[] result) {
				
		double f0 = (q1 - q1_min) / (q1_max - q1_min);
		double f1 = (q2 - q2_min) / (q2_max - q2_min);
		double f2 = (q3 - q3_min) / (q3_max - q3_min);
		double g0 = 1 - f0;
		double g1 = 1 - f1;
		double g2 = 1 - f2;
		double x = b1_000 * g0 * g1 * g2 + b1_001 * g0 * g1 * f2 + b1_010 * g0 * f1 * g2 + b1_011 * g0 * f1 * f2
				+ b1_100 * f0 * g1 * g2 + b1_101 * f0 * g1 * f2 + b1_110 * f0 * f1 * g2 + b1_111 * f0 * f1 * f2;
		double y = b2_000 * g0 * g1 * g2 + b2_001 * g0 * g1 * f2 + b2_010 * g0 * f1 * g2 + b2_011 * g0 * f1 * f2
				+ b2_100 * f0 * g1 * g2 + b2_101 * f0 * g1 * f2 + b2_110 * f0 * f1 * g2 + b2_111 * f0 * f1 * f2;
		double z = b3_000 * g0 * g1 * g2 + b3_001 * g0 * g1 * f2 + b3_010 * g0 * f1 * g2 + b3_011 * g0 * f1 * f2
				+ b3_100 * f0 * g1 * g2 + b3_101 * f0 * g1 * f2 + b3_110 * f0 * f1 * g2 + b3_111 * f0 * f1 * f2;
		result[0] = (float) x;
		result[1] = (float) y;
		result[2] = (float) z;
	}


}
