package cnuphys.magfield;

public class SolenoidProbe extends FieldProbe {

	private Cell2D _cell;

	private Solenoid _solenoid;

	// cache the z shift
	// private double _shiftZ;

	// private double _fakeZMax;

	public SolenoidProbe(Solenoid field) {
		super(field);

		if (MagneticFields.getInstance().getSolenoid() != field) {
			MagneticFields.getInstance().setSolenoid(field);
		}

		_solenoid = MagneticFields.getInstance().getSolenoid();
		_cell = new Cell2D(this);
		// _scaleFactor =
		// MagneticFields.getInstance().getSolenoid().getScaleFactor();
		// _shiftZ = MagneticFields.getInstance().getSolenoid().getShiftZ();
		// _fakeZMax = MagneticFields.getInstance().getSolenoid().getFakeZMax();

		q1Coordinate = _solenoid.q1Coordinate.clone();
		q2Coordinate = _solenoid.q2Coordinate.clone();
		q3Coordinate = _solenoid.q3Coordinate.clone();

	}

	/**
	 * Get the field magnitude in kiloGauss at a given location expressed in
	 * Cartesian coordinates.
	 * 
	 * @param x
	 *            the x coordinate in cm
	 * @param y
	 *            the y coordinate in cm
	 * @param z
	 *            the z coordinate in cm
	 * @return the magnitude of the field in kiloGauss.
	 */
	@Override
	public void field(float x, float y, float z, float result[]) {

		if (!contains(x, y, z)) {
			result[0] = 0f;
			result[1] = 0f;
			result[2] = 0f;
			return;
		}

		double rho = FastMath.sqrt(x * x + y * y);
		double phi = FastMath.atan2Deg(y, x);
		fieldCylindrical(phi, rho, z, result);
	}

	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		fieldCylindrical(_cell, phi, rho, z, result);
	}

	/**
	 * Get the field by trilinear interpolation.
	 * 
	 * @param probe
	 *            for faster results
	 * @param phi
	 *            azimuthal angle in degrees.
	 * @param rho
	 *            the cylindrical rho coordinate in cm.
	 * @param z
	 *            coordinate in cm
	 * @param result
	 *            the result
	 * @result a Cartesian vector holding the calculated field in kiloGauss.
	 */
	private void fieldCylindrical(Cell2D cell, double phi, double rho, double z, float result[]) {


		if (isZeroField()) {
			result[X] = 0f;
			result[Y] = 0f;
			result[Z] = 0f;
			return;
		}

		// misalignment??
		if (_solenoid.isMisalignedZ()) {
			z = z - _solenoid.getShiftZ();
		}
		
		//x and y uglier
		if (_solenoid.isMisalignedX() || _solenoid.isMisalignedY()) {
			double phiRad  = Math.toRadians(phi);
			double x = rho*FastMath.cos(phiRad);
			double y = rho*FastMath.sin(phiRad);
			x = x - _solenoid.getShiftY();
			y = y - _solenoid.getShiftY();
			rho = FastMath.hypot(x, y);
			phi = FastMath.atan2Deg(y, x);
		}
		
		if (!containsCylindrical(phi, rho, z)) {
			result[X] = 0f;
			result[Y] = 0f;
			result[Z] = 0f;
			return;
		}


		if (phi < 0.0) {
			phi += 360.0;
		}

		// this will return
		// result[0] = bphi = 0;
		// result[1] = brho
		// result[2] = bphi

		cell.calculate(rho, z, result);
		// rotate onto to proper phi

		// if (phi > 0.001) {
		double rphi = Math.toRadians(phi);
		double cos = Math.cos(rphi);
		double sin = Math.sin(rphi);
		double bphi = result[0];
		double brho = result[1];
		result[X] = (float) (brho * cos - bphi * sin);
		result[Y] = (float) (brho * sin + bphi * cos);
		// }

		double sf = _solenoid.getScaleFactor();
		result[X] *= sf;
		result[Y] *= sf;
		result[Z] *= sf;
	}

	/**
	 * Check whether the field boundaries include the point
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
	@Override
	public boolean containsCylindrical(double phi, double rho, double z) {	
		return _solenoid.contains(rho, z);
	}

}
