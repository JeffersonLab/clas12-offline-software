package cnuphys.magfield;

import java.io.File;
import java.io.FileNotFoundException;

public class FieldProbe implements IField {

	protected static boolean CACHE = true;
	
	protected static final double TINY = 1.0e-8;
	
	//the field
	protected IField _field;
	
	/**
	 * 
	 * @param field
	 * @deprecated this is nothing more than a wrapper for the underlying IField object. Just 
	 * use the IField object directly. The probe technologie is now built into the IField object.
	 */
	@Deprecated
	public FieldProbe(IField field) {
		if (field instanceof FieldProbe) {
			System.err.println("WARNING: Making a Magnetic Field Probe from a Probe.");
		}
		_field = field;
	}
	
	/**
	 * Get the underlying field
	 * @return the field that backs this probe
	 */
	@Deprecated
	public IField getField() {
		return _field;
	}

	/**
	 * Turn the caching on or off globally
	 * 
	 * @param cacheOn
	 *            the value of the flag
	 */
	@Deprecated
	public static void cache(boolean cacheOn) {
		CACHE = cacheOn;
	}
	
	/**
	 * Check whether cache is on
	 * @return <code>true</code> if cache is on
	 */
	public static boolean isCache() {
		return CACHE;
	}
	
	@Override
	public String getName() {
		return _field.getName();
	}

	@Override
	public void field(float x, float y, float z, float result[]) {
		_field.field(x, y, z, result);
	}


	@Override
	public float fieldMagnitudeCylindrical(double phi, double r, double z) {
		return _field.fieldMagnitudeCylindrical(phi, r, z);
	}

	@Override
	public float fieldMagnitude(float x, float y, float z) {
		return _field.fieldMagnitudeCylindrical(x, y, z);
	}

	@Override
	public float getMaxFieldMagnitude() {
		return _field.getMaxFieldMagnitude();
	}

	@Override
	public void readBinaryMagneticField(File binaryFile) throws FileNotFoundException {
		_field.readBinaryMagneticField(binaryFile);
	}

	@Override
	public boolean isZeroField() {
		return _field.isZeroField();
	}
	
	/**
	 * Get the appropriate probe for the active field
	 * @return the probe for the active field
	 */
	public static FieldProbe factory() {
		return factory(MagneticFields.getInstance().getActiveField());
	}
	

    /**
     * Obtain an approximation for the magnetic field gradient at a given location expressed in cylindrical
     * coordinates. The field is returned as a Cartesian vector in kiloGauss/cm.
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
    public void gradientCylindrical(double phi, double rho, double z,
    	    float result[]) {
		_field.gradientCylindrical(phi, rho, z, result);
    }


    /**
     * Obtain an approximation for the magnetic field gradient at a given location expressed in Cartesian
     * coordinates. The field is returned as a Cartesian vector in kiloGauss/cm.
     *
     * @param x
     *            the x coordinate in cm
     * @param y
     *            the y coordinate in cm
     * @param z
     *            the z coordinate in cm
     * @param result
     *            a float array holding the retrieved field in kiloGauss. The
     *            0,1 and 2 indices correspond to x, y, and z components.
     */
     @Override
	public void gradient(float x, float y, float z, float result[]) {
 		_field.gradientCylindrical(x, y, z, result);
     }

	
	/**
	 * Get the appropriate probe for the given field
	 * @return the probe for the givev field
	 */
	public static FieldProbe factory(IField field) {
		

		if (field != null) {

			if (field instanceof Torus) {
				return new TorusProbe((Torus) field);
			} else if (field instanceof Solenoid) {
				return new SolenoidProbe((Solenoid) field);
			} else if (field instanceof RotatedCompositeField) {
				return new RotatedCompositeProbe((RotatedCompositeField) field);
			} else if (field instanceof CompositeField) {
				return new CompositeProbe((CompositeField) field);
			} else {
				System.err.println("WARNING: cannot create probe for " + field.getName());
			}
		}

//		System.err.println("WARNING: null probe");
		return null;
	}
	

    /**
     * Is the physical magnet represented by the map misaligned?
     * @return <code>true</code> if magnet is misaligned
     */
    @Override
	public boolean isMisaligned() {
    	return _field.isMisaligned();
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
	public boolean contains(float x, float y, float z) {
		return _field.contains(x, y, z);
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
	public boolean containsCylindrical(float phi, float rho, float z) {
		return _field.containsCylindrical(phi, rho, z);
	}

	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		_field.fieldCylindrical(phi, rho, z, result);
	}

}
