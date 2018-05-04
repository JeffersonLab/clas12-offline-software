package cnuphys.magfield;

public class Cell3D {

	public double q1Min = Float.POSITIVE_INFINITY;
	public double q1Max = Float.NEGATIVE_INFINITY;
	public double q2Min = Float.POSITIVE_INFINITY;
	public double q2Max = Float.NEGATIVE_INFINITY;
	public double q3Min = Float.POSITIVE_INFINITY;
	public double q3Max = Float.NEGATIVE_INFINITY;

	private MagneticField field;

	private double q1Norm;
	private double q2Norm;
	private double q3Norm;

	private double b1_000 = 0.0;
	private double b1_001 = 0.0;
	private double b1_010 = 0.0;
	private double b1_100 = 0.0;
	private double b1_011 = 0.0;
	private double b1_110 = 0.0;
	private double b1_101 = 0.0;
	private double b1_111 = 0.0;

	private double b2_000 = 0.0;
	private double b2_001 = 0.0;
	private double b2_010 = 0.0;
	private double b2_100 = 0.0;
	private double b2_011 = 0.0;
	private double b2_110 = 0.0;
	private double b2_101 = 0.0;
	private double b2_111 = 0.0;

	private double b3_000 = 0.0;
	private double b3_001 = 0.0;
	private double b3_010 = 0.0;
	private double b3_100 = 0.0;
	private double b3_011 = 0.0;
	private double b3_110 = 0.0;
	private double b3_101 = 0.0;
	private double b3_111 = 0.0;

	private double f[] = new double[3];
	private double g[] = new double[3];
	private double aa[] = new double[8];

	/**
	 * Create a 3D cell (for Torus)
	 * 
	 * @param field
	 *            the magnetic field
	 */
	public Cell3D(MagneticField field) {
		this.field = field;
	}

	// reset the cached values
	private void reset(double phi, double rho, double z) {
		GridCoordinate q1Coord = field.q1Coordinate;
		GridCoordinate q2Coord = field.q2Coordinate;
		GridCoordinate q3Coord = field.q3Coordinate;

		int n1 = q1Coord.getIndex(phi);
		if (n1 < 0) {
			System.err.println("WARNING Bad n1 in Cell3D.reset: " + n1);
			return;
		}
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

		q1Min = q1Coord.getMin(n1);
		q1Max = q1Coord.getMax(n1);
		q1Norm = 1. / (q1Max - q1Min);

		q2Min = q2Coord.getMin(n2);
		q2Max = q2Coord.getMax(n2);
		q2Norm = 1. / (q2Max - q2Min);

		q3Min = q3Coord.getMin(n3);
		q3Max = q3Coord.getMax(n3);
		q3Norm = 1. / (q3Max - q3Min);

		int i000 = field.getCompositeIndex(n1, n2, n3);
		int i001 = i000 + 1;

		int i010 = field.getCompositeIndex(n1, n2 + 1, n3);
		int i011 = i010 + 1;

		int i100 = field.getCompositeIndex(n1 + 1, n2, n3);
		int i101 = i100 + 1;

		int i110 = field.getCompositeIndex(n1 + 1, n2 + 1, n3);
		int i111 = i110 + 1;

		b1_000 = field.getB1(i000);
		b1_001 = field.getB1(i001);
		b1_010 = field.getB1(i010);
		b1_011 = field.getB1(i011);
		b1_100 = field.getB1(i100);
		b1_101 = field.getB1(i101);
		b1_110 = field.getB1(i110);
		b1_111 = field.getB1(i111);

		b2_000 = field.getB2(i000);
		b2_001 = field.getB2(i001);
		b2_010 = field.getB2(i010);
		b2_011 = field.getB2(i011);
		b2_100 = field.getB2(i100);
		b2_101 = field.getB2(i101);
		b2_110 = field.getB2(i110);
		b2_111 = field.getB2(i111);

		b3_000 = field.getB3(i000);
		b3_001 = field.getB3(i001);
		b3_010 = field.getB3(i010);
		b3_011 = field.getB3(i011);
		b3_100 = field.getB3(i100);
		b3_101 = field.getB3(i101);
		b3_110 = field.getB3(i110);
		b3_111 = field.getB3(i111);

	}

	/**
	 * Check whether the cell boundaries (not the map boundaries) include the
	 * point
	 * 
	 * @param phi
	 *            azimuthal angle in degrees.
	 * @param rho
	 *            the cylindrical rho coordinate in cm.
	 * @param z
	 *            coordinate in cm
	 * @return <code>true</code> if the point is included in the boundary of the
	 *         field
	 * 
	 */
	public boolean containedCylindrical(double phi, double rho, double z) {
		return (phi >= q1Min && phi < q1Max) && (rho >= q2Min && rho < q2Max) && (z >= q3Min && z < q3Max);
	}

	/**
	 * Calculate the field in kG
	 * 
	 * @param phi
	 * @param rho
	 * @param z
	 * @param result
	 */
	public void calculate(double phi, double rho, double z, float[] result) {
		if (field.containedCylindrical((float)phi, (float)rho, (float)z)) {
			// do we need to reset?
			if (!containedCylindrical(phi, rho, z)) {
				reset(phi, rho, z);
			}
//			else {
//				System.err.println("Using cached 3d");
//			}
			f[0] = (phi - q1Min) * q1Norm;/// (q1_max - q1Min);
			f[1] = (rho - q2Min) * q2Norm;// / (q2_max - q2Min);
			f[2] = (z - q3Min) * q3Norm;// / (q3_max - q3Min);

			f[0] = f[0] - Math.floor(f[0]);
			f[1] = f[1] - Math.floor(f[1]);
			f[2] = f[2] - Math.floor(f[2]);

			g[0] = 1 - f[0];
			g[1] = 1 - f[1];
			g[2] = 1 - f[2];

			aa[0] = g[0] * g[1] * g[2];
			aa[1] = g[0] * g[1] * f[2];
			aa[2] = g[0] * f[1] * g[2];
			aa[3] = g[0] * f[1] * f[2];
			aa[4] = f[0] * g[1] * g[2];
			aa[5] = f[0] * g[1] * f[2];
			aa[6] = f[0] * f[1] * g[2];
			aa[7] = f[0] * f[1] * f[2];

			double bx = b1_000 * aa[0] + b1_001 * aa[1] + b1_010 * aa[2] + b1_011 * aa[3] + b1_100 * aa[4]
					+ b1_101 * aa[5] + b1_110 * aa[6] + b1_111 * aa[7];
			double by = b2_000 * aa[0] + b2_001 * aa[1] + b2_010 * aa[2] + b2_011 * aa[3] + b2_100 * aa[4]
					+ b2_101 * aa[5] + b2_110 * aa[6] + b2_111 * aa[7];
			double bz = b3_000 * aa[0] + b3_001 * aa[1] + b3_010 * aa[2] + b3_011 * aa[3] + b3_100 * aa[4]
					+ b3_101 * aa[5] + b3_110 * aa[6] + b3_111 * aa[7];

			result[0] = (float) bx;
			result[1] = (float) by;
			result[2] = (float) bz;
		} else {
			for (int i = 0; i < 3; i++) {
				result[i] = 0f;
			}
		}
	}

}
