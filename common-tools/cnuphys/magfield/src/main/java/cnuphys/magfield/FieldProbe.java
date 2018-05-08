package cnuphys.magfield;

import java.io.File;
import java.io.FileNotFoundException;

public abstract class FieldProbe implements IField {

//	protected static boolean CACHE = true;
	
	protected static final double TINY = 1.0e-8;
	
	//the field
	protected IField _field;
	
	/**
	 * 
	 * @param field
	 */
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
//		CACHE = cacheOn;
	}
	
	/**
	 * Check whether cache is on
	 * @return <code>true</code> if cache is on
	 */
	@Deprecated
	public static boolean isCache() {
//		return CACHE;
		return true;
	}
	
	/**
	 * Get the name of the field
	 */
	@Override
	public String getName() {
		return _field.getName();
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
		
		//use three point derivative
		float del = 1f; //cm
		float del2 = 2*del;
		
		float baseVal = fieldMagnitude(x, y, z);
		float bv3 = -3*baseVal;
		
		float bx0 = fieldMagnitude(x+del, y, z);
		float bx1 = fieldMagnitude(x+del2, y, z);
		
//		System.err.println(" " + baseVal + "  " + bx0 + "  " + bx1);
		float by0 = fieldMagnitude(x, y+del, z);
		float by1 = fieldMagnitude(x, y+del2, z);
		float bz0 = fieldMagnitude(x, y, z+del);
		float bz1 = fieldMagnitude(x, y, z+del2);
		
		result[0] = (bv3 + 4*bx0 - bx1)/del2;
		result[1] = (bv3 + 4*by0 - by1)/del2;
		result[2] = (bv3 + 4*bz0 - bz1)/del2;
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
		phi = Math.toRadians(phi);
		double x = rho*FastMath.cos(phi);
    	double y = rho*FastMath.sin(phi);
    	gradient((float)x, (float)y, (float)z, result);
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
