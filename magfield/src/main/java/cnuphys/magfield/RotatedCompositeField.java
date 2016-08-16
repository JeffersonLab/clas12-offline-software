package cnuphys.magfield;

public final class RotatedCompositeField extends CompositeField {

	// the angle in degrees
	private float _angle = -25.0f;
	private float _sin = (float) Math.sin(Math.toRadians(_angle));
	private float _cos = (float) Math.cos(Math.toRadians(_angle));

	/**
	 * Set the rotation angle
	 * 
	 * @param angle
	 *            the rotation angle in degrees
	 */
	public void setRotationAngle(float angle) {
		_angle = angle;
		_sin = (float) Math.sin(Math.toRadians(_angle));
		_cos = (float) Math.cos(Math.toRadians(_angle));
	}
	
	@Override
	public String getName() {
		String s = "Rotated Composite contains: ";

		int count = 1;
		for (IField field : this) {
			if (count == 1) {
				s += field.getName();
			}
			else {
				s += " + " + field.getName();
			}
			count++;
		}

		return s;
	}


	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates. The field is returned as a Cartesian vector in kiloGauss.
	 *
	 * @param xs
	 *            the x coordinate in cm
	 * @param ys
	 *            the y coordinate in cm
	 * @param zs
	 *            the z coordinate in cm
	 * @param result
	 *            the result is a float array holding the retrieved field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */
	@Override
	public void field(float xs, float ys, float zs, float[] result) {

		float x = xs * _cos - zs * _sin;
		float y = ys;
		float z = zs * _cos + xs * _sin;

		float bx = 0, by = 0, bz = 0;
		for (IField field : this) {
			field.field(x, y, z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		result[0] = bx * _cos + bz * _sin;
		result[1] = by;
		result[2] = bz * _cos - bx * _sin;
	}


	// the rotation only works for the Cartesian call
	// TODO fix
	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		phi = Math.toRadians(phi);
		double x = rho*Math.cos(phi);
		double y = rho*Math.sin(phi);
//		System.err.println("Cannot use cylindrical call for rotated field");
		field((float)x, (float)y, (float)z, result);
	}

	/**
	 * Get the field magnitude in kiloGauss at a given location expressed in
	 * cylindrical coordinates.
	 * 
	 * @param phi
	 *            azimuthal angle in degrees.
	 * @param r
	 *            in cm.
	 * @param z
	 *            in cm
	 * @return the magnitude of the field in kiloGauss.
	 */
	@Override
	public float fieldMagnitudeCylindrical(double phi, double rho, double z) {
		phi = Math.toRadians(phi);
		double x = rho*Math.cos(phi);
		double y = rho*Math.sin(phi);
		return fieldMagnitude((float)x, (float)y, (float)z);
	}


}
