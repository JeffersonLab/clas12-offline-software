package cnuphys.magfield;

public class RotatedCompositeProbe extends CompositeProbe {

	// the angle in degrees for rotating between tilted ans sector CS
	private final double _angle = -25.0;
	private final double _sin25 = Math.sin(Math.toRadians(_angle));
	private final double _cos25 = Math.cos(Math.toRadians(_angle));

	public RotatedCompositeProbe(RotatedCompositeField field) {
		super(field);
	}

	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates of the tilted sector system. The field is returned as a Cartesian
	 * vector in kiloGauss.
	 * 
	 * @param sector the sector [1..6]
	 * @param xs     the tilted x coordinate in cm
	 * @param ys     the tilted y coordinate in cm
	 * @param zs     the tilted z coordinate in cm
	 * @param result the result is a float array holding the retrieved field in
	 *               kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *               components.
	 */
	@Override
	public void field(int sector, float xs, float ys, float zs, float[] result) {

		// first rotate location to get to the sector coordinate system
		float x = (float) (xs * _cos25 - zs * _sin25);
		float y = (ys);
		float z = (float) (zs * _cos25 + xs * _sin25);

		// now rotate to the correct sector to get the lab coordinates. We can use the
		// result array!
		MagneticFields.sectorToLab(sector, result, x, y, z);
		x = result[0];
		y = result[1];
		z = result[2];

		float bx = 0, by = 0, bz = 0;
		for (IField probe : probes) {
			probe.field(x, y, z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}

		MagneticFields.labToSector(sector, result, bx, by, bz);
		bx = result[0];
		by = result[1];
		bz = result[2];

		// now rotate the field in the opposite sense
		result[0] = (float) (bx * _cos25 + bz * _sin25);
		result[1] = (by);
		result[2] = (float) (bz * _cos25 - bx * _sin25);
	}

	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates. The field is returned as a Cartesian vector in kiloGauss. THIS
	 * ASSUMES COORDINATES ARE IN A SECTOR 1 SECTOR SYSTEM
	 *
	 * @param xs     the x coordinate in cm in sector 1 sector system
	 * @param ys     the y coordinate in cm in sector 1 sector system
	 * @param zs     the z coordinate in cm in sector 1 sector system
	 * @param result the result is a float array holding the retrieved field in
	 *               kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *               components.
	 */
	@Override
	public void field(float xs, float ys, float zs, float[] result) {
		System.err.println(
				"SHOULD NOT HAPPEN. In rotated composite field probe, should not call field without the sector argument.");

		(new Throwable()).printStackTrace();
		System.exit(1);

		field(1, xs, ys, zs, result); // assume sector 1
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
	public void gradient(float xs, float ys, float zs, float result[]) {
		double x = xs * _cos25 - zs * _sin25;
		double y = ys;
		double z = zs * _cos25 + xs * _sin25;
		float bx = 0, by = 0, bz = 0;
		for (IField probe : probes) {
			probe.gradient((float) x, (float) y, (float) z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		result[0] = (float) (bx * _cos25 + bz * _sin25);
		result[1] = (by);
		result[2] = (float) (bz * _cos25 - bx * _sin25);
	}

}
