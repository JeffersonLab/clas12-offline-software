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
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates in the sector (not lab or global) system. 
	 * The field is returned as a Cartesian vector in kiloGauss.
	 * @param sector the sector [1..6]
	 * @param x
	 *            the x sector coordinate in cm
	 * @param y
	 *            the y sector coordinate in cm
	 * @param z
	 *            the z sector coordinate in cm
	 * @param result
	 *            the result is a float array holding the retrieved field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */
	@Override
	public void field(int sector, float x, float y, float z, float[] result) {
				
		//rotate to the correct sector to get the lab coordinates. We can use the result array!
		MagneticFields.sectorToLab(sector, result, x, y, z);
		x = result[0];
		y = result[1];
		z = result[2];
		
		//get the field using the global (lab) coordinates
		field(x, y, z, result);
		
		//rotate the field back to the sector coordinates
		MagneticFields.labToSector(sector, result, result[0],  result[1],  result[2]);

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
		_field.gradient(x, y, z, result);
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
	 * Get the appropriate probe for the active field
	 * @return the probe for the active field
	 */
	public static IField factory() {
		return factory(MagneticFields.getInstance().getActiveField());
	}
	
	
	/**
	 * Get the appropriate probe for the given field
	 * @return the probe for the givev field
	 */
	public static IField factory(IField field) {
		
		

		if (field != null) {
			
			if (MagneticFields.getInstance().isProbeOrCompositeProbe(field)) {
				return field;
			}

			if (field instanceof Torus) {
				return new TorusProbe((Torus) field);
			} else if (field instanceof Solenoid) {
				return new SolenoidProbe((Solenoid) field);
			} else if (field instanceof RotatedCompositeField) {
				return new RotatedCompositeProbe((RotatedCompositeField) field);
			} else if (field instanceof CompositeField) {
				return new CompositeProbe((CompositeField) field);
			} else {
				(new Throwable()).printStackTrace();
				System.err.println("WARNING: cannot create probe for " + field.getName() + "  class: " + field.getClass().getName());
			}
		}

		return new ZeroProbe();
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
	public boolean contains(double x, double y, double z) {
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
	public boolean containsCylindrical(double phi, double rho, double z) {
		return _field.containsCylindrical(phi, rho, z);
	}

	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		_field.fieldCylindrical(phi, rho, z, result);
	}

}
