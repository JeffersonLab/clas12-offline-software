package cnuphys.magfield;

public class Cell3D implements MagneticFieldChangeListener {

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


	private double f[] = new double[3];
	private double g[] = new double[3];
	private double a[] = new double[8];

	private int n1 = -1;
	private int n2 = -1;
	private int n3 = -1;
	
	//hold field at 8 corners of cell
	FloatVect b[][][] = new FloatVect[2][2][2];

	/**
	 * Create a 3D cell (for Torus)
	 * 
	 * @param field
	 *            the magnetic field
	 */
	public Cell3D(MagneticField field) {
		this.field = field;
		MagneticFields.getInstance().addMagneticFieldChangeListener(this);
		
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 2; k++) {
					b[i][j][k] = new FloatVect();
				}			
			}		
		}
		
	}

	// reset the cached values
	private void reset(double q1, double q2, double q3) {
		GridCoordinate q1Coord = field.q1Coordinate;
		GridCoordinate q2Coord = field.q2Coordinate;
		GridCoordinate q3Coord = field.q3Coordinate;

		n1 = q1Coord.getIndex(q1);
		if (n1 < 0) {
			System.err.println("WARNING Bad n1 in Cell3D.reset: " + n1);
			return;
		}
		n2 = q2Coord.getIndex(q2);
		if (n2 < 0) {
			System.err.println("WARNING Bad n2 in Cell3D.reset: " + n2);
			return;
		}
		n3 = q3Coord.getIndex(q3);
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

		int i000 = field.getCompositeIndex(n1, n2, n3); //n1 n2 n3
		int i001 = i000 + 1; //n1 n2 n3+1

		int i010 = field.getCompositeIndex(n1, n2 + 1, n3); //n1 n2+1 n3
		int i011 = i010 + 1;  //n1 n2+1 n3+1

		int i100 = field.getCompositeIndex(n1 + 1, n2, n3);  //n1+1 n2 n3
		int i101 = i100 + 1;  //n1+1 n2 n3+1

		int i110 = field.getCompositeIndex(n1 + 1, n2 + 1, n3); //n1+1 n2+1 n3
		int i111 = i110 + 1; //n1+1 n2+1 n3+1
		
		//field at 8 corners
		
		b[0][0][0].x = field.getB1(i000);
		b[0][0][1].x = field.getB1(i001);
		b[0][1][0].x = field.getB1(i010);
		b[0][1][1].x = field.getB1(i011);
		b[1][0][0].x = field.getB1(i100);
		b[1][0][1].x = field.getB1(i101);
		b[1][1][0].x = field.getB1(i110);
		b[1][1][1].x = field.getB1(i111);
		
		b[0][0][0].y = field.getB2(i000);
		b[0][0][1].y = field.getB2(i001);
		b[0][1][0].y = field.getB2(i010);
		b[0][1][1].y = field.getB2(i011);
		b[1][0][0].y = field.getB2(i100);
		b[1][0][1].y = field.getB2(i101);
		b[1][1][0].y = field.getB2(i110);
		b[1][1][1].y = field.getB2(i111);
		
		b[0][0][0].z = field.getB3(i000);
		b[0][0][1].z = field.getB3(i001);
		b[0][1][0].z = field.getB3(i010);
		b[0][1][1].z = field.getB3(i011);
		b[1][0][0].z = field.getB3(i100);
		b[1][0][1].z = field.getB3(i101);
		b[1][1][0].z = field.getB3(i110);
		b[1][1][1].z = field.getB3(i111);

	}

	/**
	 * Check whether the cell boundaries (not the map boundaries) include the
	 * point
	 * 
	 * @param q1  phi in deg for cylindrical, x (cm) for rectangular
	 * @param q2  rho (cm) for cylindrical, y (cm) for rectangular
	 * @param q3  z (cm) for cylindrical or rectangular
	 * @return <code>true</code> if the point is included in the boundary of the
	 *         field
	 * 
	 */
	public boolean contained(double q1, double q2, double q3) {
		return ((q1 > q1Min) && (q1 < q1Max) && (q2 > q2Min) && (q2 < q2Max) && (q3 > q3Min) && (q3 < q3Max));
	}

	/**
	 * Calculate the field in kG
	 * 
	 * @param phi
	 *            the phi coordinate in degrees
	 * @param rho
	 *            the rho coordinate in cm
	 * @param z
	 * @param result
	 */
	public void calculate(double q1, double q2, double q3, float[] result) {
		
		
		boolean contains;
		if (field.isRectangularGrid()) {
			contains = field.contains((float) q1, (float) q2, (float)q3);
		}
		else {
			contains = field.containsCylindrical((float) q1, (float) q2, (float)q3);
		}
		
		if (contains) {
			// do we need to reset?
			if (!contained(q1, q2, q3)) {
				reset(q1, q2, q3);
			}

			if (!MagneticField.isInterpolate()) {
				nearestNeighbor(q1, q2, q3, result);
				return;
			}

			f[0] = (q1 - q1Min) * q1Norm;/// (q1_max - q1Min);
			f[1] = (q2 - q2Min) * q2Norm;// / (q2_max - q2Min);
			f[2] = (q3 - q3Min) * q3Norm;// / (q3_max - q3Min);

			f[0] = f[0] - Math.floor(f[0]);
			f[1] = f[1] - Math.floor(f[1]);
			f[2] = f[2] - Math.floor(f[2]);

			g[0] = 1 - f[0];
			g[1] = 1 - f[1];
			g[2] = 1 - f[2];

			a[0] = g[0] * g[1] * g[2];
			a[1] = g[0] * g[1] * f[2];
			a[2] = g[0] * f[1] * g[2];
			a[3] = g[0] * f[1] * f[2];
			a[4] = f[0] * g[1] * g[2];
			a[5] = f[0] * g[1] * f[2];
			a[6] = f[0] * f[1] * g[2];
			a[7] = f[0] * f[1] * f[2];

//			double bx = b1_000 * aa[0] + b1_001 * aa[1] + b1_010 * aa[2] + b1_011 * aa[3] + b1_100 * aa[4]
//					+ b1_101 * aa[5] + b1_110 * aa[6] + b1_111 * aa[7];
//			double by = b2_000 * aa[0] + b2_001 * aa[1] + b2_010 * aa[2] + b2_011 * aa[3] + b2_100 * aa[4]
//					+ b2_101 * aa[5] + b2_110 * aa[6] + b2_111 * aa[7];
//			double bz = b3_000 * aa[0] + b3_001 * aa[1] + b3_010 * aa[2] + b3_011 * aa[3] + b3_100 * aa[4]
//					+ b3_101 * aa[5] + b3_110 * aa[6] + b3_111 * aa[7];
			
			double bx = b[0][0][0].x * a[0] + b[0][0][1].x * a[1] + b[0][1][0].x * a[2] + b[0][1][1].x * a[3] + b[1][0][0].x * a[4]
					+ b[1][0][1].x * a[5] + b[1][1][0].x * a[6] + b[1][1][1].x * a[7];
			double by = b[0][0][0].y * a[0] + b[0][0][1].y * a[1] + b[0][1][0].y * a[2] + b[0][1][1].y * a[3] + b[1][0][0].y * a[4]
					+ b[1][0][1].y * a[5] + b[1][1][0].y * a[6] + b[1][1][1].y * a[7];
			double bz = b[0][0][0].z * a[0] + b[0][0][1].z * a[1] + b[0][1][0].z * a[2] + b[0][1][1].z * a[3] + b[1][0][0].z * a[4]
					+ b[1][0][1].z * a[5] + b[1][1][0].z * a[6] + b[1][1][1].z * a[7];


			result[0] = (float) bx;
			result[1] = (float) by;
			result[2] = (float) bz;
		} else {
			for (int i = 0; i < 3; i++) {
				result[i] = 0f;
			}
		}
	}

	// nearest neighbor algorithm
	private void nearestNeighbor(double phi, double rho, double z, float[] result) {
		f[0] = (phi - q1Min) * q1Norm;
		f[1] = (rho - q2Min) * q2Norm;
		f[2] = (z - q3Min) * q3Norm;

		int N1 = (f[0] < 0.5) ? 0 : 1;
		int N2 = (f[1] < 0.5) ? 0 : 1;
		int N3 = (f[2] < 0.5) ? 0 : 1;

		result[0] = b[N1][N2][N3].x;
		result[1] = b[N1][N2][N3].y;
		result[2] = b[N1][N2][N3].z;
	}

	@Override
	public void magneticFieldChanged() {
		q1Min = Float.POSITIVE_INFINITY;
		q1Max = Float.NEGATIVE_INFINITY;
		q2Min = Float.POSITIVE_INFINITY;
		q2Max = Float.NEGATIVE_INFINITY;
		q3Min = Float.POSITIVE_INFINITY;
		q3Max = Float.NEGATIVE_INFINITY;
	}

}
