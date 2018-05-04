package cnuphys.magfield;

public class Cell2D {

	private MagneticField field;

	public double q2Min = Float.POSITIVE_INFINITY;
	public double q2Max = Float.NEGATIVE_INFINITY;
	public double q3Min = Float.POSITIVE_INFINITY;
	public double q3Max = Float.NEGATIVE_INFINITY;

	private double q2Norm;
	private double q3Norm;

	// private double b1_b000 = 0.0;
	// private double b1_b001 = 0.0;
	// private double b1_b010 = 0.0;
	// private double b1_b011 = 0.0;
	private double b2_b000 = 0.0;
	private double b2_b001 = 0.0;
	private double b2_b010 = 0.0;
	private double b2_b011 = 0.0;
	private double b3_b000 = 0.0;
	private double b3_b001 = 0.0;
	private double b3_b010 = 0.0;
	private double b3_b011 = 0.0;

	/**
	 * Create a 2D cell (for solenoid)
	 * 
	 * @param field
	 *            the magnetic field
	 */
	public Cell2D(MagneticField field) {
		this.field = field;
	}

	// reset the cached values
	private void reset(double rho, double z) {

		GridCoordinate q2Coord = field.q2Coordinate;
		GridCoordinate q3Coord = field.q3Coordinate;

		int n2 = q2Coord.getIndex(rho);
		if (n2 < 0) {
			System.err.println("WARNING Bad n2 in Cell3D.reset: " + n2);
			return;
		}
		int n3 = q3Coord.getIndex(z);
		if (n3 < 0) {
			System.err.println("WARNING Bad n3 in Cell3D.reset: " + n3);
			return;
		}

		q2Min = q2Coord.getMin(n2);
		q2Max = q2Coord.getMax(n2);
		q2Norm = 1. / (q2Max - q2Min);

		q3Min = q3Coord.getMin(n3);
		q3Max = q3Coord.getMax(n3);
		q3Norm = 1. / (q3Max - q3Min);

		int i000 = field.getCompositeIndex(0, n2, n3);
		int i001 = i000 + 1;

		int i010 = field.getCompositeIndex(0, n2 + 1, n3);
		int i011 = i010 + 1;

		// b1_b000 = 0;
		// b1_b001 = 0;
		// b1_b010 = 0;
		// b1_b011 = 0;
		// probe.b1_b000 = getB1(i000);
		// probe.b1_b001 = getB1(i001);
		// probe.b1_b010 = getB1(i010);
		// probe.b1_b011 = getB1(i011);
		b2_b000 = field.getB2(i000);
		b2_b001 = field.getB2(i001);
		b2_b010 = field.getB2(i010);
		b2_b011 = field.getB2(i011);
		b3_b000 = field.getB3(i000);
		b3_b001 = field.getB3(i001);
		b3_b010 = field.getB3(i010);
		b3_b011 = field.getB3(i011);

	}

	/**
	 * Check whether the cell boundaries (not the map boundaries) include the
	 * point
	 * 
	 * @param rho
	 *            the cylindrical rho coordinate in cm.
	 * @param z
	 *            coordinate in cm
	 * @return <code>true</code> if the point is included in the boundary of the
	 *         field
	 * 
	 */
	public boolean containedCylindrical(double rho, double z) {
		return (rho >= q2Min && rho < q2Max) && (z >= q3Min && z < q3Max);
	}

	/**
	 * Calculate the field in kG
	 * 
	 * @param rho
	 * @param z
	 * @param result
	 */
	public void calculate(double rho, double z, float[] result) {
		if (field.containedCylindrical(0f, (float) rho, (float) z)) {
			// do we need to reset?
			if (!containedCylindrical(rho, z)) {
				reset(rho, z);
			}
			// else {
			// System.err.println("Using cached 2d");
			// }
			double f1 = (rho - q2Min) * q2Norm;
			double f2 = (z - q3Min) * q3Norm;

			f1 = f1 - Math.floor(f1);
			f2 = f2 - Math.floor(f2);

			double g1 = 1 - f1;
			double g2 = 1 - f2;

			double g1g2 = g1 * g2;
			double f1g2 = f1 * g2;
			double g1f2 = g1 * f2;
			double f1f2 = f1 * f2;

			double bphi = 0;

			double brho = b2_b000 * g1g2 + b2_b001 * g1f2 + b2_b010 * f1g2 + b2_b011 * f1f2;

			double bz = b3_b000 * g1g2 + b3_b001 * g1f2 + b3_b010 * f1g2 + b3_b011 * f1f2;
			result[0] = (float) bphi;
			result[1] = (float) brho;
			result[2] = (float) bz;

		} else {
			for (int i = 0; i < 3; i++) {
				result[i] = 0f;
			}
		}
	}
}
