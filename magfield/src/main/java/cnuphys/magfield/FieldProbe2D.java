package cnuphys.magfield;

public class FieldProbe2D extends FieldProbe {

	double q2_min = 0.0;
	double q2_max = 0.0;
	double q3_min = 0.0;
	double q3_max = 0.0;

	double b1_b000 = 0.0;
	double b1_b001 = 0.0;
	double b1_b010 = 0.0;
	double b1_b011 = 0.0;
	double b2_b000 = 0.0;
	double b2_b001 = 0.0;
	double b2_b010 = 0.0;
	double b2_b011 = 0.0;
	double b3_b000 = 0.0;
	double b3_b001 = 0.0;
	double b3_b010 = 0.0;
	double b3_b011 = 0.0;

	/**
	 * Check whether the point is contained in this
	 * probe. A probe corresponds to a grid cell.
	 * Variable 1 (q1) is fixed 
	 * @param q2 the value of the first coordinate
	 * @param q3 the value of the second coordinate
	 * @return <code>true</code> of caching is on and the coordinate is in
	 * the cell corresponding to this probe.
	 */
	public boolean contains(double q2, double q3) {
		if (!CACHE) {
			return false; //will prevent caching
		}
		return (q2 >= q2_min && q2 <= q2_max) && (q3 >= q3_min && q3 <= q3_max);
	}

	/**
	 * Interpolate the field
	 * @param q2
	 * @param q3
	 * @param scale scale factor
	 * @return the field
	 */
	public void evaluate(double q2, double q3, float[] result) {
				
		double f1 = (q2 - q2_min) / (q2_max - q2_min);
		double f2 = (q3 - q3_min) / (q3_max - q3_min);
		double g1 = 1 - f1;
		double g2 = 1 - f2;
//		double bphi = b1_b000 * g1 * g2 + b1_b001 * g1 * f2 + b1_b010 * f1 * g2
//				+ b1_b011 * f1 * f2;
		
		double bphi = 0;
		
		double brho = b2_b000 * g1 * g2 + b2_b001 * g1 * f2 + b2_b010 * f1 * g2
				+ b2_b011 * f1 * f2;
		
		double bz = b3_b000 * g1 * g2 + b3_b001 * g1 * f2 + b3_b010 * f1 * g2
				+ b3_b011 * f1 * f2;
		result[0] = (float) bphi;
		result[1] = (float) brho;
		result[2] = (float) bz;
	}

}
