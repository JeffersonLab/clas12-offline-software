package cnuphys.magfield;

public class ConstantField extends MagneticField {

    private float _fieldVal = 10f; // kg

    public ConstantField(float fval) {
	_fieldVal = fval;
    }

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
    @Override
    public void fieldCylindrical(double phi, double rho, double z,
	    float[] result) {

	result[0] = 0;
	result[1] = 0;
	result[2] = _fieldVal; // 1 tesla;

    }

    /**
     * Obtain the maximum field magnitude of any point in the map.
     * 
     * @return the maximum field magnitude in the units of the map.
     */
    @Override
    public float getMaxFieldMagnitude() {
	return _fieldVal;
    }

    @Override
    protected void computeMaxField() {
	System.err
		.println("[UNIFORMFIELD] computeMaxField sould not be called.");
    }

    /**
     * Get the name of the field
     * 
     * @return the name
     */
    @Override
    public String getName() {
	return "Uniform Field";
    }

}
