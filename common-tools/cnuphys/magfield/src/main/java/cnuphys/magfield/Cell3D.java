package cnuphys.magfield;

import java.io.PrintStream;

/**
 * Cells are used by the probes. 3D cells for the torus, 2D cells for the
 * solenoid.
 * 
 * @author heddle
 *
 */
public class Cell3D {

	// used for debug printing
	private boolean printedOnce = false;

	// the limits of the current cell
	public double q1Min = Double.POSITIVE_INFINITY;
	public double q1Max = Double.NEGATIVE_INFINITY;
	public double q2Min = Double.POSITIVE_INFINITY;
	public double q2Max = Double.NEGATIVE_INFINITY;
	public double q3Min = Double.POSITIVE_INFINITY;
	public double q3Max = Double.NEGATIVE_INFINITY;

	// the probe using this cell
	private FieldProbe _probe;
	
	// hold field at 8 corners of cell
	double c[][][][] = new double[2][2][2][3];


	// field indices of the current cell
	private int _n1 = -1;
	private int _n2 = -1;
	private int _n3 = -1;

	/**
	 * Create a 3D cell (for Torus and Transverse solenoid)
	 * 
	 * @param probe the magnetic probe
	 */
	public Cell3D(FieldProbe probe) {
		_probe = probe;
	}

	// reset because we have crossed into another cell
	private boolean reset(double q1, double q2, double q3) {
		GridCoordinate q1Coord = _probe.q1Coordinate;
		GridCoordinate q2Coord = _probe.q2Coordinate;
		GridCoordinate q3Coord = _probe.q3Coordinate;

		_n1 = q1Coord.getIndex(q1);
		_n2 = q2Coord.getIndex(q2);
		_n3 = q3Coord.getIndex(q3);
		
		if ((_n1 < 0) || (_n2 < 0) || (_n3 < 0)) {
			return false;
		}

		q1Min = q1Coord.getValue(_n1);
		q1Max = q1Coord.getValue(_n1 + 1);
		q2Min = q2Coord.getValue(_n2);
		q2Max = q2Coord.getValue(_n2 + 1);
		q3Min = q3Coord.getValue(_n3);
		q3Max = q3Coord.getValue(_n3 + 1);

		for (int i = 0; i < 2; i++) {
			int nn1 = _n1 + i;
			for (int j = 0; j < 2; j++) {
				int nn2 = _n2 + j;
				for (int k = 0; k < 2; k++) {
					int nn3 = _n3 + k;
					int index = _probe.getCompositeIndex(nn1, nn2, nn3);
					
					if (index < 0) {
						System.out.println();
					}
					c[i][j][k][0] = _probe.getB1(index);
					c[i][j][k][1] = _probe.getB2(index);
					c[i][j][k][2] = _probe.getB3(index);

				}
			}
		}

		return true;
	}

	/**
	 * Check whether the cell boundaries (not the map boundaries) include the point
	 * 
	 * @param q1 phi in deg for cylindrical, x (cm) for rectangular
	 * @param q2 rho (cm) for cylindrical, y (cm) for rectangular
	 * @param q3 z (cm) for cylindrical or rectangular
	 * @return <code>true</code> if the point is inside the boundary of the cell
	 * 
	 */
	public boolean contained(double q1, double q2, double q3) {
		return ((q1 > q1Min) && (q1 < q1Max) && (q2 > q2Min) && 
				(q2 < q2Max) && (q3 > q3Min) && (q3 < q3Max));
	}

		
	/**
	 * Calculate the field in kG by trilinear interpolation
	 * 
	 * @param phi    the phi coordinate in degrees
	 * @param rho    the rho coordinate in cm
	 * @param z      the z component in cm
	 * @param result the field in kG
	 */
	public void trilinear(double q1, double q2, double q3, float[] result) {
		
		GridCoordinate q1Coord = _probe.q1Coordinate;
		GridCoordinate q2Coord = _probe.q2Coordinate;
		GridCoordinate q3Coord = _probe.q3Coordinate;
				
		double xd = (q1 - q1Min)/q1Coord.getDelta();
		double yd = (q2 - q2Min)/q2Coord.getDelta();
		double zd = (q3 - q3Min)/q3Coord.getDelta();
		
		double omxd = 1-xd;
		double omyd = 1-yd;

		for (int i = 0; i < 3; i++) {
			double c00 = c[0][0][0][i]*omxd + c[1][0][0][i]*xd;
			double c01 = c[0][0][1][i]*omxd + c[1][0][1][i]*xd;
			double c10 = c[0][1][0][i]*omxd + c[1][1][0][i]*xd;
			double c11 = c[0][1][1][i]*omxd + c[1][1][1][i]*xd;
			
			double c0 = c00*omyd + c10*yd;
			double c1 = c01*omyd + c11*yd;
		
			result[i] = (float)(c0*(1-zd) + c1*zd);
	}

	}

	
	/**
	 * Calculate the field in kG
	 * 
	 * @param phi    the phi coordinate in degrees
	 * @param rho    the rho coordinate in cm
	 * @param z      the z coordinate int cm
	 * @param result the field in kG
	 */
	public void calculate(double q1, double q2, double q3, float[] result) {
		
		if (!MagneticField.isInterpolate()) {
			nearestNeighbor(q1, q2, q3, result);
			return;
		}


		// do we need to reset?
		if (!contained(q1, q2, q3)) {
			if (!reset(q1, q2, q3)) {
				result[0] = 0;
				result[1] = 0;
				result[2] = 0;
				return;
			}
		}
		
		trilinear(q1, q2, q3, result);
	}

	// nearest neighbor algorithm
	private void nearestNeighbor(double phi, double rho, double z, float[] result) {
		
		GridCoordinate q1Coord = _probe.q1Coordinate;
		GridCoordinate q2Coord = _probe.q2Coordinate;
		GridCoordinate q3Coord = _probe.q3Coordinate;

		
		int n1 = q1Coord.getRoundedIndex(phi);
		int n2 = q2Coord.getRoundedIndex(rho);
		int n3 = q3Coord.getRoundedIndex(z);
		
		int index = _probe.getCompositeIndex(n1, n2, n3);
		result[0] = _probe.getB1(index);
		result[1] = _probe.getB2(index);
		result[2] = _probe.getB3(index);
		
	}

}
