package cnuphys.magfield;

/**
 *
 * @author gavalian
 */
public class TorusProbe extends FieldProbe {

	// cell used to cache corner information
	private Cell3D _cell;

	// the torus field
	private Torus _torus;

	// 12 -fold symmetry or full map?
	private boolean _fullMap;

	/**
	 * Create a probe for use with the torus
	 * 
	 * @param field the torus field
	 */
	public TorusProbe(Torus field) {
		super(field);
		if (MagneticFields.getInstance().getTorus() != field) {
			MagneticFields.getInstance().setTorus(field);
		}

		_torus = MagneticFields.getInstance().getTorus();

		_cell = new Cell3D(this);
		_fullMap = _torus.isFullMap();

		q1Coordinate = _torus.q1Coordinate.clone();
		q2Coordinate = _torus.q2Coordinate.clone();
		q3Coordinate = _torus.q3Coordinate.clone();

	}

	/**
	 * Create a probe for use with the torus Used only for the weird DoubleTorus
	 * 
	 * @param field the torus field
	 */
	public TorusProbe(Torus field, boolean set) {
		super(field);

		if (set) {
			if (MagneticFields.getInstance().getTorus() != field) {
				MagneticFields.getInstance().setTorus(field);
			}
		}

		_torus = field;

		_cell = new Cell3D(this);
		_fullMap = _torus.isFullMap();

		q1Coordinate = _torus.q1Coordinate.clone();
		q2Coordinate = _torus.q2Coordinate.clone();
		q3Coordinate = _torus.q3Coordinate.clone();

	}

	/**
	 * Get the field in kG
	 * 
	 * @param x      the x coordinate in cm
	 * @param y      the y coordinate in cm
	 * @param z      the z coordinate in cm
	 * @param result holds the resuts, the Cartesian coordinates of B in kG
	 */
	@Override
	public void field(float x, float y, float z, float result[]) {

		if (isZeroField()) {
			result[X] = 0f;
			result[Y] = 0f;
			result[Z] = 0f;
			return;
		}

		// note that the contains functions handles the shifts
		if (!contains(x, y, z)) {
			result[0] = 0f;
			result[1] = 0f;
			result[2] = 0f;
			return;
		}

		// apply the shifts
		x -= _torus.getShiftX();
		y -= _torus.getShiftY();
		z -= _torus.getShiftZ();

		double rho = FastMath.sqrt(x * x + y * y);
		double phi = FastMath.atan2Deg(y, x);
		fieldCylindrical(_cell, phi, rho, z, result);
	}

	/**
	 * Get the field by trilinear interpolation. Assumes all shifting from
	 * misalignment is done.
	 * 
	 * @param phi    azimuthal angle in degrees.
	 * @param rho    the cylindrical rho coordinate in cm.
	 * @param z      coordinate in cm
	 * @param result the result
	 * @result a Cartesian vector holding the calculated field in kiloGauss.
	 */
	private void fieldCylindrical(Cell3D cell, double phi, double rho, double z, float result[]) {

		// must deal with 12-fold symmetry possibility
		if (_fullMap) {
			if (phi < 0) {
				phi += 360;
			}

			cell.calculate(phi, rho, z, result);
		} else { // 12-fold symmmetric
			// relativePhi (-30, 30) phi relative to middle of sector
			double relativePhi = relativePhi(phi);

			boolean flip = (relativePhi < 0.0);

			cell.calculate(Math.abs(relativePhi), rho, z, result);

			// negate change x and z components
			if (flip) {
				result[X] = -result[X];
				result[Z] = -result[Z];
			}

			// rotate onto to proper sector

			int sector = getSector(phi);

			if (sector > 1) {
				double cos = cosSect[sector];
				double sin = sinSect[sector];
				double bx = result[X];
				double by = result[Y];
				result[X] = (float) (bx * cos - by * sin);
				result[Y] = (float) (bx * sin + by * cos);
			}

		}

		double sf = _torus._scaleFactor;
		result[X] *= sf;
		result[Y] *= sf;
		result[Z] *= sf;
	}

	/**
	 * Must deal with the fact that we only have the field between 0 and 30 degrees.
	 *
	 * @param absolutePhi the absolute phi
	 * @return the relative phi (-30, 30) from the nearest middle of a sector in
	 *         degrees.
	 */
	private double relativePhi(double absolutePhi) {
		if (absolutePhi < 0.0) {
			absolutePhi += 360.0;
		}

		// make relative phi between 0 -30 and 30
		double relativePhi = absolutePhi;
		while (Math.abs(relativePhi) > 30.0) {
			relativePhi -= 60.0;
		}
		return relativePhi;
	}

	/**
	 * Check whether the field boundaries include the point
	 * 
	 * @param phi azimuthal angle in degrees.
	 * @param rho the cylindrical rho coordinate in cm.
	 * @param z   coordinate in cm
	 * @return <code>true</code> if the point is included in the boundary of the
	 *         field
	 * 
	 */
//	@Override
//	public boolean containsCylindrical(double phi, double rho, double z) {	
//		return _torus.contains(rho, z);
//	}

}
