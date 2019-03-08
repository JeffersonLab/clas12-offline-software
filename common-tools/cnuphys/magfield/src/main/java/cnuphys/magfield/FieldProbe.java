package cnuphys.magfield;

public abstract class FieldProbe implements IField {

	// indices of components
	protected static final int X = 0;
	protected static final int Y = 1;
	protected static final int Z = 2;

	// for rotating field
	protected static final double ROOT3OVER2 = Math.sqrt(3.) / 2.;
	protected static final double cosSect[] = { Double.NaN, 1, 0.5, -0.5, -1, -0.5, 0.5 };
	protected static final double sinSect[] = { Double.NaN, 0, ROOT3OVER2, ROOT3OVER2, 0, -ROOT3OVER2, -ROOT3OVER2 };

	// the field
	protected final IMagField _field;

	// cache the name of the field
	protected String _name;

	/**
	 * Holds the grid info for the slowest changing coordinate. This is cloned from
	 * the field.
	 */
	protected GridCoordinate q1Coordinate;

	/**
	 * Holds the grid info for the medium changing coordinate This is cloned from
	 * the field.
	 */
	protected GridCoordinate q2Coordinate;

	/**
	 * Holds the grid info for the fastest changing coordinate This is cloned from
	 * the field.
	 */
	protected GridCoordinate q3Coordinate;

	/**
	 * Create a probe, which is a thread safe way to use the field
	 * 
	 * @param field the underlying field
	 */
	public FieldProbe(IMagField field) {
		_field = field;
		if (_field != null) {
			_name = new String(field.getName());
		} else { // zero field
			_name = "No Field";
		}

	}

	/**
	 * Get the underlying field
	 * 
	 * @return the field that backs this probe
	 */
	public IMagField getField() {
		return _field;
	}

	/**
	 * Get the name of the field
	 * 
	 * @return the name of the underlying field
	 */
	@Override
	public String getName() {
		return _name;
	}

	/**
	 * Get the field magnitude in kiloGauss at a given location expressed in
	 * Cartesian coordinates.
	 * 
	 * @param x the x coordinate in cm
	 * @param y the y coordinate in cm
	 * @param z the z coordinate in cm
	 * @return the magnitude of the field in kiloGauss.
	 */
	@Override
	public float fieldMagnitude(float x, float y, float z) {
		float result[] = new float[3];
		field(x, y, z, result);
		return FastMath.vectorLength(result);
	}

	/**
	 * Obtain the maximum field magnitude of any point in the map.
	 * 
	 * @return the maximum field magnitude in the units of the map.
	 */
	@Override
	public float getMaxFieldMagnitude() {
		return _field.getMaxFieldMagnitude();
	}

	/**
	 * Checks whether the field has been set to always return zero.
	 * 
	 * @return <code>true</code> if the field is set to return zero.
	 */
	@Override
	public boolean isZeroField() {
		return _field.isZeroField();
	}

	/**
	 * Get the composite index to take me to the correct place in the buffer.
	 * 
	 * @param n1 the index in the q1 direction
	 * @param n2 the index in the q2 direction
	 * @param n3 the index in the q3 direction
	 * @return the composite index (buffer offset)
	 */
	public final int getCompositeIndex(int n1, int n2, int n3) {
		int n23 = q2Coordinate.getNumPoints() * q3Coordinate.getNumPoints();
		return n1 * n23 + n2 * q3Coordinate.getNumPoints() + n3;
	}

	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates in the sector (not lab or global) system. The field is returned
	 * as a Cartesian vector in kiloGauss.
	 * 
	 * @param sector the sector [1..6]
	 * @param x      the x sector coordinate in cm
	 * @param y      the y sector coordinate in cm
	 * @param z      the z sector coordinate in cm
	 * @param result the result is a float array holding the retrieved field in
	 *               kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *               components.
	 */
	@Override
	public void field(int sector, float x, float y, float z, float[] result) {

		// rotate to the correct sector to get the lab coordinates. We can use the
		// result array!
		MagneticFields.sectorToLab(sector, result, x, y, z);
		x = result[0];
		y = result[1];
		z = result[2];

		// get the field using the global (lab) coordinates
		field(x, y, z, result);

		// rotate the field back to the sector coordinates
		MagneticFields.labToSector(sector, result, result[0], result[1], result[2]);

	}

	/**
	 * Obtain an approximation for the magnetic field gradient at a given location
	 * expressed in Cartesian coordinates. The field is returned as a Cartesian
	 * vector in kiloGauss/cm.
	 *
	 * @param x      the x coordinate in cm
	 * @param y      the y coordinate in cm
	 * @param z      the z coordinate in cm
	 * @param result a float array holding the retrieved field in kiloGauss. The 0,1
	 *               and 2 indices correspond to x, y, and z components.
	 */
	@Override
	public void gradient(float x, float y, float z, float result[]) {

		// use three point derivative
		float del = 1f; // cm
		float del2 = 2 * del;

		float baseVal = fieldMagnitude(x, y, z);
		float bv3 = -3 * baseVal;

		float bx0 = fieldMagnitude(x + del, y, z);
		float bx1 = fieldMagnitude(x + del2, y, z);

//		System.err.println(" " + baseVal + "  " + bx0 + "  " + bx1);
		float by0 = fieldMagnitude(x, y + del, z);
		float by1 = fieldMagnitude(x, y + del2, z);
		float bz0 = fieldMagnitude(x, y, z + del);
		float bz1 = fieldMagnitude(x, y, z + del2);

		result[0] = (bv3 + 4 * bx0 - bx1) / del2;
		result[1] = (bv3 + 4 * by0 - by1) / del2;
		result[2] = (bv3 + 4 * bz0 - bz1) / del2;
	}

	/**
	 * Obtain an approximation for the magnetic field gradient at a given location
	 * expressed in cylindrical coordinates. The field is returned as a Cartesian
	 * vector in kiloGauss/cm.
	 *
	 * @param phi    azimuthal angle in degrees.
	 * @param rho    the cylindrical rho coordinate in cm.
	 * @param z      coordinate in cm
	 * @param result the result
	 * @result a Cartesian vector holding the calculated field in kiloGauss.
	 */
//	@Override
//    public void gradientCylindrical(double phi, double rho, double z,
//    	    float result[]) {
//		phi = Math.toRadians(phi);
//		double x = rho*FastMath.cos(phi);
//    	double y = rho*FastMath.sin(phi);
//    	gradient((float)x, (float)y, (float)z, result);
//    }

	/**
	 * Get the appropriate probe for the active field
	 * 
	 * @return the probe for the active field
	 */
	public static FieldProbe factory() {
		return factory(MagneticFields.getInstance().getActiveField());
	}

	private static int ZEROPROBEWARNINGCOUNT = 0;

	/**
	 * Get the appropriate probe for the given field
	 * 
	 * @return the probe for the given field
	 */
	public static FieldProbe factory(IMagField field) {

		if (field != null) {

			if (field instanceof Torus) {
				return new TorusProbe((Torus) field);
			} else if (field instanceof Solenoid) {
				return new SolenoidProbe((Solenoid) field);
			} else if (field instanceof RotatedCompositeField) {
				return new RotatedCompositeProbe((RotatedCompositeField) field);
			} else if (field instanceof CompositeField) {
				return new CompositeProbe((CompositeField) field);
			} else {
				(new Throwable()).printStackTrace();
				System.err.println("WARNING: cannot create probe for " + field.getName() + "  class: "
						+ field.getClass().getName());
			}
		}

		// give limited number of warnings
		if (ZEROPROBEWARNINGCOUNT < 3) {
			System.err.println("WARNING: creating a Zero probe");
			ZEROPROBEWARNINGCOUNT++;
		}

		return new ZeroProbe();
	}

	/**
	 * Check whether the field boundaries include the point
	 * 
	 * @param x the x coordinate in the map units
	 * @param y the y coordinate in the map units
	 * @param z the z coordinate in the map units
	 * @return <code>true</code> if the point is included in the boundary of the
	 *         field
	 */
	@Override
	public boolean contains(double x, double y, double z) {
		return _field.contains(x, y, z);
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

	/**
	 * Get the sector [1..6] from the phi value
	 * 
	 * @param phi the value of phi in degrees
	 * @return the sector [1..6]
	 */
	protected int getSector(double phi) {
		// convert phi to [0..360]

		while (phi < 0) {
			phi += 360.0;
		}
		while (phi > 360.0) {
			phi -= 360.0;
		}

		if ((phi > 30.0) && (phi <= 90.0)) {
			return 2;
		}
		if ((phi > 90.0) && (phi <= 150.0)) {
			return 3;
		}
		if ((phi > 150.0) && (phi <= 210.0)) {
			return 4;
		}
		if ((phi > 210.0) && (phi <= 270.0)) {
			return 5;
		}
		if ((phi > 270.0) && (phi <= 330.0)) {
			return 6;
		}
		return 1;
	}

	/**
	 * @return the phiCoordinate
	 */
	public GridCoordinate getPhiCoordinate() {
		return q1Coordinate;
	}

	/**
	 * @return the rCoordinate
	 */
	public GridCoordinate getRCoordinate() {
		return q2Coordinate;
	}

	/**
	 * @return the zCoordinate
	 */
	public GridCoordinate getZCoordinate() {
		return q3Coordinate;
	}

	/**
	 * Get the maximum z coordinate of the field boundary
	 * 
	 * @return the maximum z coordinate of the field boundary
	 */
	public double getZMax() {
		return q3Coordinate.getMax();
	}

	/**
	 * Get the minimum z coordinate of the field boundary
	 * 
	 * @return the minimum z coordinate of the field boundary
	 */
	public double getZMin() {
		return q3Coordinate.getMin();
	}

	/**
	 * Get the maximum rho coordinate of the field boundary
	 * 
	 * @return the maximum rho coordinate of the field boundary
	 */
	public double getRhoMax() {
		return q2Coordinate.getMax();
	}

	/**
	 * Get the minimum rho coordinate of the field boundary
	 * 
	 * @return the minimum rho coordinate of the field boundary
	 */
	public double getRhoMin() {
		return q2Coordinate.getMin();
	}

	/**
	 * Get the maximum phi coordinate of the field boundary (deg)
	 * 
	 * @return the maximum phi coordinate of the field boundary
	 */
	public double getPhiMax() {
		double phimax = q1Coordinate.getMax();
		while (phimax < 0) {
			phimax += 360.;
		}
		return phimax;
	}

	/**
	 * Get the minimum phi coordinate of the field boundary (deg)
	 * 
	 * @return the minimum phi coordinate of the field boundary
	 */
	public double getPhiMin() {
		return q1Coordinate.getMin();
	}

	/**
	 * Get B1 at a given index.
	 * 
	 * @param index the index.
	 * @return the B1 at the given index.
	 */
	public final float getB1(int index) {
		return _field.getB1(index);
	}

	/**
	 * Get B2 at a given index.
	 * 
	 * @param index the index.
	 * @return the B2 at the given index.
	 */
	public final float getB2(int index) {
		return _field.getB2(index);
	}

	/**
	 * Get B3 at a given index.
	 * 
	 * @param index the index.
	 * @return the B3 at the given index.
	 */
	public final float getB3(int index) {
		return _field.getB3(index);
	}

}
