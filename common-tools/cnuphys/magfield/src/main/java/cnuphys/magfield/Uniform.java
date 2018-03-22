package cnuphys.magfield;

public class Uniform extends MagneticField {
	
	private static float _uniformField[] = new float[3];
	
	/**
	 * A uniform field in the z direction
	 * @param Bx the x component in Tesla
	 * @param Bx the x component in Tesla
	 * @param Bx the x component in Tesla
	 */
	public Uniform(float Bx, float By, float Bz) {
		_uniformField[0] = Bx;
		_uniformField[1] = By;
		_uniformField[2] = Bz;
	}

	@Override
    /**
     * Get the field by trilinear interpolation.
     *
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
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		result[0] = 10*_uniformField[0];  //convert to kG
		result[1] = 10*_uniformField[1];  //convert to kG
		result[2] = 10*_uniformField[2];  //convert to kG
	}

	@Override
	public String getName() {
		return "Uniform";
	}
	
	/**
	 * Set the strength of the uniform field
	 * @param strength the strength in Tesla
	 */
	public void setStrength(float Bx, float By, float Bz) {
		_uniformField[0] = Bx;
		_uniformField[1] = By;
		_uniformField[2] = Bz;
	}

}
