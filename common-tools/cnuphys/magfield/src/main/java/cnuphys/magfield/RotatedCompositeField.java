package cnuphys.magfield;

public final class RotatedCompositeField extends CompositeField {

	// the angle in degrees
	private double _angle = -25.0;
	private double _sin = Math.sin(Math.toRadians(_angle));
	private double _cos = Math.cos(Math.toRadians(_angle));
	
	/**
	 * Set the rotation angle
	 * 
	 * @param angle
	 *            the rotation angle in degrees
	 */
	public void setRotationAngle(double angle) {
		_angle = angle;
		_sin = Math.sin(Math.toRadians(_angle));
		_cos = Math.cos(Math.toRadians(_angle));
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
		
		//first rotate the point
		double x = xs * _cos - zs * _sin;
		double y = ys;
		double z = zs * _cos + xs * _sin;

		//sum the fields
		float bx = 0, by = 0, bz = 0;
		for (IField field : this) {
			field.field((float)x, (float)y, (float)z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		
		//now rotate the field
		result[0] = (float)(bx * _cos + bz * _sin);
		result[1] = (by);
		result[2] = (float)(bz * _cos - bx * _sin);
	}

	
	// the rotation only works for the Cartesian call
	// TODO fix
	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		System.err.println("FATAL: Cannot use direct cylindrical call for rotated field.");
		System.exit(1);
//		phi = Math.toRadians(phi);
//		double x = rho*Math.cos(phi);
//		double y = rho*Math.sin(phi);
//		field((float)x, (float)y, (float)z, result);
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
	

	 /**
	 * Check whether the field boundaries include the point
	 * 
	 * @param x
	 *            the x coordinate in the map units
	 * @param y
	 *            the y coordinate in the map units
	 * @param z
	 *            the z coordinate in the map units
	 * @return <code>true</code> if the point is included in the boundary of the
	 *         field
	 */
	@Override
	public boolean contained(float xs, float ys, float zs) {
		
		//first rotate the point
		double x = xs * _cos - zs * _sin;
		double y = ys;
		double z = zs * _cos + xs * _sin;

		
		double rho = Math.sqrt(x * x + y * y);
		double phi = MagneticField.atan2Deg(y, x);
		return containedCylindrical((float) phi, (float) rho, (float)z);
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
	public boolean containedCylindrical(float phi, float rho, float z) {
		for (IField field : this) {
			if (field.containedCylindrical(phi, rho, z)) {
				return true;
			}
		}
		return false;
	}

}
