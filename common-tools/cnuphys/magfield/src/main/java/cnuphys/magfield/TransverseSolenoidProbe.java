package cnuphys.magfield;

public class TransverseSolenoidProbe extends SolenoidProbe {
	
	// cell used to cache corner information
	private Cell3D _cell;

	// the transverse solenoid field
	private TransverseSolenoid _transverseSolenoid;

	
	/**
	 * Create a probe for use with the transverse solenoid field
	 * 
	 * @param field the transverse solenoid field
	 */
	public TransverseSolenoidProbe(TransverseSolenoid field) {
		super(field);
		if (MagneticFields.getInstance().getSolenoid() != field) {
			MagneticFields.getInstance().setSolenoid(field);
		}

		_transverseSolenoid = (TransverseSolenoid)MagneticFields.getInstance().getSolenoid();

		_cell = new Cell3D(this);

		q1Coordinate = _transverseSolenoid.q1Coordinate.clone();
		q2Coordinate = _transverseSolenoid.q2Coordinate.clone();
		q3Coordinate = _transverseSolenoid.q3Coordinate.clone();

	}

	@Override
	public void field(float x, float y, float z, float[] result) {

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
		x -= _transverseSolenoid.getShiftX();
		y -= _transverseSolenoid.getShiftY();
		z -= _transverseSolenoid.getShiftZ();

		_cell.calculate(x, y, z, result);
		double sf = _transverseSolenoid._scaleFactor;
		result[X] *= sf;
		result[Y] *= sf;
		result[Z] *= sf;

	}

}
