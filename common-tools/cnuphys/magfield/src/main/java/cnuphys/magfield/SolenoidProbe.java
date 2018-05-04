package cnuphys.magfield;

public class SolenoidProbe extends FieldProbe {
	
	public SolenoidProbe(Solenoid field) {
		super(field);
	}


	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		((Solenoid)_field).fieldCylindrical(phi, rho, z, result);
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
		return ((Solenoid)_field).containedCylindrical(phi, rho, z);
	}

}
