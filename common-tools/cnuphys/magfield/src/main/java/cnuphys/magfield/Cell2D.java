package cnuphys.magfield;

public class Cell2D implements MagneticFieldChangeListener {

	private MagneticField field;

	public double q2Min = Float.POSITIVE_INFINITY;
	public double q2Max = Float.NEGATIVE_INFINITY;
	public double q3Min = Float.POSITIVE_INFINITY;
	public double q3Max = Float.NEGATIVE_INFINITY;

	private double q2Norm;
	private double q3Norm;
	
	private int n2 = -1;
	private int n3 = -1;

	//hold field at 4 corners of cell
	FloatVect b[][] = new FloatVect[2][2];

	/**
	 * Create a 2D cell (for solenoid)
	 * 
	 * @param field
	 *            the magnetic field
	 */
	public Cell2D(MagneticField field) {
		this.field = field;
		MagneticFields.getInstance().addMagneticFieldChangeListener(this);

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				b[i][j] = new FloatVect();
			}
		}

	}

	boolean printedOnce = false;
	// reset the cached values
	private void reset(double rho, double z) {

		GridCoordinate q2Coord = field.q2Coordinate;
		GridCoordinate q3Coord = field.q3Coordinate;

		n2 = q2Coord.getIndex(rho);
		if (n2 < 0) {
			if (!printedOnce) {
				printedOnce = true;
				System.err.println("WARNING Bad n2 in Cell2D.reset: " + n2 + "  rho: " + rho);
			}
			return;
		}
		n3 = q3Coord.getIndex(z);
		if (n3 < 0) {
			if (!printedOnce) {
				printedOnce = true;
				System.err.println("WARNING Bad n3 in Cell2D.reset: " + n3 + "  z: " + z);
			}
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

			
			b[0][0].x = field.getB2(i000); //x means rho component
			b[0][1].x = field.getB2(i001);
			b[1][0].x = field.getB2(i010);
			b[1][1].x = field.getB2(i011);
			b[0][0].z = field.getB3(i000); 
			b[0][1].z = field.getB3(i001);
			b[1][0].z = field.getB3(i010);
			b[1][1].z = field.getB3(i011);

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
		return ((rho > q2Min) && (rho < q2Max) && (z > q3Min) && (z < q3Max));
	}

	/**
	 * Calculate the field in kG
	 * 
	 * @param rho
	 * @param z
	 * @param result
	 */
	public void calculate(double rho, double z, float[] result) {
		if (field.containsCylindrical(0, rho, z)) {
			// do we need to reset?
			if (!containedCylindrical(rho, z)) {
				reset(rho, z);
			}
			
			if (!MagneticField.isInterpolate()) {
				nearestNeighbor(rho, z, result);
				return;
			}

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

			double brho = b[0][0].x * g1g2 + b[0][1].x * g1f2 + b[1][0].x * f1g2 + b[1][1].x * f1f2;
			double bz = b[0][0].z * g1g2 + b[0][1].z * g1f2 + b[1][0].z * f1g2 + b[1][1].z* f1f2;
			result[0] = 0f;  //bphi is 0
			result[1] = (float) brho;
			result[2] = (float) bz;

		} else {
			for (int i = 0; i < 3; i++) {
				result[i] = 0f;
			}
		}
	}
	
	
	//nearest neighbor algorithm
	private void nearestNeighbor(double rho, double z, float[] result) {
		double f1 = (rho - q2Min) * q2Norm;
		double f2 = (z - q3Min) * q3Norm;

		int N2 = (f1 < 0.5) ? 0 : 1;
		int N3 = (f2 < 0.5) ? 0 : 1;
				
		result[0] = 0f;  //bphi is 0
		result[1] = b[N2][N3].x;  //Brho
		result[2] = b[N2][N3].z;
	}

	@Override
	public void magneticFieldChanged() {
		q2Min = Float.POSITIVE_INFINITY;
		q2Max = Float.NEGATIVE_INFINITY;
		q3Min = Float.POSITIVE_INFINITY;
		q3Max = Float.NEGATIVE_INFINITY;
	}
}
