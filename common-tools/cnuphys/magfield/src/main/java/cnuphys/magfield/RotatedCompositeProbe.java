package cnuphys.magfield;

public class RotatedCompositeProbe extends CompositeProbe {

	// the angle in degrees for rotating between tilted ans sector CS
	private final double _angle = -25.0;
	private final double _sin25 = Math.sin(Math.toRadians(_angle));
	private final double _cos25 = Math.cos(Math.toRadians(_angle));
	
	public static boolean TestMode;

	public RotatedCompositeProbe(RotatedCompositeField field) {
		super(field);
	}

	
	private static final float m11 = 0.90630779f;
	private static final float m12 = 0.42261826f;
	private static final float m21 = 0.45315389f;
	private static final float m22 = 0.86602540f;
	private static final float m23 = 0.21130913f;
	private static final float m31 = 0.78488557f;
	private static final float m32 = 0.5f;
	private static final float m33 = 0.36599815f;
	
	
	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates of the tilted sector system. The field is returned as a Cartesian
	 * vector in kiloGauss.
	 * 
	 * @param sector the sector [1..6]
	 * @param xtilt     the tilted x coordinate in cm
	 * @param ytilt     the tilted y coordinate in cm
	 * @param ztilt     the tilted z coordinate in cm
	 * @param result the result is a float array holding the retrieved field in
	 *               kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *               components.
	 */
	@Override
	public void field(int sector, float xtilt, float ytilt, float ztilt, float[] result) {

		float x, y, z;
		float bxl = 0, byl = 0, bzl = 0;
		
		
		if (TestMode) {
			z = -m12 * xtilt + m11 * ztilt;
			
			switch(sector) {
			case 1:
				x = m11 * xtilt + m12 *ztilt;
				y = ytilt;
				break;
				
			case 2:
				x = m21 * xtilt - m22 * ytilt + m23 * ztilt;
				y = m31 * xtilt + m32 * ytilt + m33 * ztilt;
				break;
				
			case 3:
				x = -m21 * xtilt - m22 * ytilt - m23 * ztilt;
				y = m31 * xtilt - m32 * ytilt + m33 * ztilt;
				break;
				
			case 4:
				x = -m11 * xtilt - m12 *ztilt;
				y = -ytilt;
				break;
				
			case 5:
				x = -m21 * xtilt + m22 * ytilt - m23 * ztilt;
				y = -m31 * xtilt - m32 * ytilt - m33 * ztilt;
				break;
				
			case 6:
				x = m21 * xtilt + m22 * ytilt + m23 * ztilt;
				y = -m31 * xtilt + m32 * ytilt- m33 * ztilt;
				break;
				
			default:
				x = Float.NaN;
				y = Float.NaN;
				System.err.println("Bad sector in RotatedCompositeProbe, sector = " + sector);
				break;
			}

		} else {
			// first rotate location to get to the sector coordinate system
			x = (float) (xtilt * _cos25 - ztilt * _sin25);
			y = (ytilt);
			z = (float) (ztilt * _cos25 + xtilt * _sin25);

			// now rotate to the correct sector to get the lab coordinates. We can use the
			// result array!
			MagneticFields.sectorToLab(sector, result, x, y, z);
			x = result[0];
			y = result[1];
			z = result[2];
		}

		
		for (IField probe : probes) {
			probe.field(x, y, z, result);
			bxl += result[0];
			byl += result[1];
			bzl += result[2];
		}
		
		if (TestMode) {
			
			switch(sector) {
			case 1:
				result[0] = m11 * bxl - m12 * bzl;
				result[1] = byl;
				result[2] = m12 * bxl + m11 * bzl;
				break;
				
			case 2:
				result[0] = m21 * bxl + m31 * byl - m12 * bzl;
				result[1] = -m22 * bxl + m32 * byl;
				result[2] = m23 * bxl + m33 * byl + m11 * bzl;			
				break;
				
			case 3:
				result[0] = -m21 * bxl + m31 * byl - m12 * bzl;
				result[1] = -m22 * bxl - m32 * byl;
				result[2] = -m23 * bxl + m33 * byl + m11 * bzl;			
				break;
				
			case 4:
				result[0] = -m11 * bxl - m12 * bzl;
				result[1] = -byl;
				result[2] = -m12 * bxl + m11 * bzl;
				break;
				
			case 5:
				result[0] = -m21 * bxl - m31 * byl - m12 * bzl;
				result[1] = m22 * bxl - m32 * byl;
				result[2] = -m23 * bxl - m33 * byl + m11 * bzl;			
				break;
				
			case 6:
				result[0] = m21 * bxl - m31 * byl - m12 * bzl;
				result[1] = m22 * bxl + m32 * byl;
				result[2] = m23 * bxl - m33 * byl + m11 * bzl;			
				break;
				
			default:
				result[0] = Float.NaN;
				result[1] = Float.NaN;
				result[2] = Float.NaN;
				System.err.println("Bad sector in RotatedCompositeProbe, sector = " + sector);
				break;
			}
		}
		else {

			MagneticFields.labToSector(sector, result, bxl, byl, bzl);
			float bx = result[0];
			float bz = result[2];

			// now rotate the field in the opposite sense
			result[0] = (float) (bx * _cos25 + bz * _sin25);
			result[2] = (float) (bz * _cos25 - bx * _sin25);
		}
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
