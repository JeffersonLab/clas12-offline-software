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
		x -= _solenoid.getShiftX();
		y -= _solenoid.getShiftY();
		z -= _solenoid.getShiftZ();

		double rho = FastMath.sqrt(x * x + y * y);
		double phi = FastMath.atan2Deg(y, x);
		fieldCylindrical(_cell, phi, rho, z, result);
	}

	/**
	 * Get the field by bilinear interpolation.
	 * 
	 * @param cell   holds cached nearest neighbors
	 * @param phi    azimuthal angle in degrees.
	 * @param rho    the cylindrical rho coordinate in cm.
	 * @param z      coordinate in cm
	 * @param result the result
	 * @result a Cartesian vector holding the calculated field in kiloGauss.
	 */
	private void fieldCylindrical(Cell2D cell, double phi, double rho, double z, float result[]) {

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
		
		//The solenoid map has cylindrical field components
		double bphi = result[0]; //0 if symmetric
		double brho = result[1];
		result[X] = (float) (brho * cos);
		result[Y] = (float) (brho * sin);
	

		double sf = _solenoid.getScaleFactor();
		result[X] *= sf;
		result[Y] *= sf;
		result[Z] *= sf;
	}

}
