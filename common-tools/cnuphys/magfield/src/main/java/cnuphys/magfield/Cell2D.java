/**
 * Cells are used by the probes. 3D cells for the torus,
 * 2D cells for the solenoid.
 * @author heddle
 *
 */
package cnuphys.magfield;

public class Cell2D {

	// used for debug printing
	private boolean printedOnce = false;

	// the limits of the current cell
	public double q2Min = Double.POSITIVE_INFINITY;
	public double q2Max = Double.NEGATIVE_INFINITY;
	public double q3Min = Double.POSITIVE_INFINITY;
	public double q3Max = Double.NEGATIVE_INFINITY;

	// the probe using this cell
	private FieldProbe _probe;

	private double q2Norm;
	private double q3Norm;

	private int _n2 = -1;
	private int _n3 = -1;

	// hold field at 4 corners of cell
	FloatVect b[][] = new FloatVect[2][2];

	/**
	 * Create a 2D cell (for solenoid)
	 * 
	 * @param probe the magnetic probe
	 */
	public Cell2D(FieldProbe probe) {
		this._probe = probe;

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				b[i][j] = new FloatVect();
			}
		}

	}

	// reset the cached values
	private void reset(double rho, double z) {

		GridCoordinate q2Coord = _probe.q2Coordinate;
		GridCoordinate q3Coord = _probe.q3Coordinate;

		_n2 = q2Coord.getIndex(rho);
		if (_n2 < 0) {
			if (!printedOnce) {
				printedOnce = true;
				// System.err.println("WARNING Bad n2 in Cell2D.reset: " + _n2 + " rho: " +
				// rho);
			}
			return;
		}
		_n3 = q3Coord.getIndex(z);
		if (_n3 < 0) {
			if (!printedOnce) {
				printedOnce = true;
				// System.err.println("WARNING Bad n3 in Cell2D.reset: " + _n3 + " z: " + z);
			}
			return;
		}

		// precompute the boundaries and some factors
		q2Min = q2Coord.getMin(_n2);
		q2Max = q2Coord.getMax(_n2);
		q2Norm = 1. / (q2Max - q2Min);

		q3Min = q3Coord.getMin(_n3);
		q3Max = q3Coord.getMax(_n3);
		q3Norm = 1. / (q3Max - q3Min);

		int i000 = _probe.getCompositeIndex(0, _n2, _n3);
		int i001 = i000 + 1;

		int i010 = _probe.getCompositeIndex(0, _n2 + 1, _n3);
		int i011 = i010 + 1;

		b[0][0].x = _probe.getB2(i000); // x means rho component
		b[0][1].x = _probe.getB2(i001);
		b[1][0].x = _probe.getB2(i010);
		b[1][1].x = _probe.getB2(i011);
		b[0][0].z = _probe.getB3(i000);
		b[0][1].z = _probe.getB3(i001);
		b[1][0].z = _probe.getB3(i010);
		b[1][1].z = _probe.getB3(i011);

	}

	/**
	 * Check whether the cell boundaries (not the map boundaries) include the point
	 * 
	 * @param rho the cylindrical rho coordinate in cm.
	 * @param z   coordinate in cm
	 * @return <code>true</code> if the point is included in the boundary of the
	 *         field
	 * 
	 */
	public boolean containedCylindrical(double rho, double z) {
		return ((rho > q2Min) && (rho < q2Max) && (z > q3Min) && (z < q3Max));
	}

	/**
	 * Calculate the field in kG in cylindrical components
	 * 
	 * @param rho rho coordinate in cm
	 * @param z z coordinate in cm
	 * @param result field in kG in cylindrical components
	 */
	public void calculate(double rho, double z, float[] result) {
		// if (_probe.containsCylindrical(0, rho, z)) {
		// do we need to reset?
		if (!containedCylindrical(rho, z)) {
			reset(rho, z);
			if ((_n2 < 0) || (_n3 < 0)) {
				result[0] = 0;
				result[1] = 0;
				result[2] = 0;
				return;
			}
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
		double bz = b[0][0].z * g1g2 + b[0][1].z * g1f2 + b[1][0].z * f1g2 + b[1][1].z * f1f2;
		result[0] = 0f; // bphi is 0
		result[1] = (float) brho;
		result[2] = (float) bz;

//		} 
//	else {
//			for (int i = 0; i < 3; i++) {
//				result[i] = 0f;
//			}
//		}
	}

	// nearest neighbor algorithm
	private void nearestNeighbor(double rho, double z, float[] result) {
		double f1 = (rho - q2Min) * q2Norm;
		double f2 = (z - q3Min) * q3Norm;

		int N2 = (f1 < 0.5) ? 0 : 1;
		int N3 = (f2 < 0.5) ? 0 : 1;

		result[0] = 0f; // bphi is 0
		result[1] = b[N2][N3].x; // Brho
		result[2] = b[N2][N3].z;
	}

	public void reset() {
		q2Min = Double.POSITIVE_INFINITY;
		q2Max = Double.NEGATIVE_INFINITY;
		q3Min = Double.POSITIVE_INFINITY;
		q3Max = Double.NEGATIVE_INFINITY;
	}
}
