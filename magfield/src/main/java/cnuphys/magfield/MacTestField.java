package cnuphys.magfield;

public class MacTestField extends MagneticField {

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

	// convert to meters
	rho = rho / 100.;
	z = z / 100.0;

	if ((z < 2) || (z > 4)) {
	    result[0] = 0;
	    result[1] = 0;
	    result[2] = 0;
	} else {
	    double bmag = 0.808 / rho; // Tesla
	    bmag *= 10; // kGauss
	    double phiRad = Math.toRadians(phi);
	    // B is in phi direction
	    result[0] = (float) -(bmag * Math.sin(phiRad));
	    result[1] = (float) (bmag * Math.cos(phiRad));
	    result[2] = 0;
	}

    }

    /**
     * Obtain the maximum field magnitude of any point in the map.
     * 
     * @return the maximum field magnitude in the units of the map.
     */
    @Override
    public float getMaxFieldMagnitude() {
	return 4.0f;
    }

    @Override
    protected void computeMaxField() {
	System.err
		.println("[MACTESTFIELD] computeMaxField sould not be called.");
    }

    /**
     * Get the name of the field
     * 
     * @return the name
     */
    @Override
    public String getName() {
	return "Mac's 1/r Test Field";
    }

}
