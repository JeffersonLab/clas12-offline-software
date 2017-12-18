package cnuphys.magfield;

import java.io.File;
import java.io.FileNotFoundException;

public abstract class FieldProbe implements IField {

	protected static boolean CACHE = true;
	
	protected static final double TINY = 1.0e-8;
	
	//the field
	protected IField _field;
	
	public FieldProbe(IField field) {
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
		float rho = (float) Math.sqrt(x * x + y * y);
		float phi = (float) MagneticField.atan2Deg(y, x);
		fieldCylindrical(phi, rho, z, result);
	}


	@Override
	public float fieldMagnitudeCylindrical(double phi, double r, double z) {
		float result[] = new float[3];
		fieldCylindrical(phi, r, z, result);
		return vectorLength(result);
	}

	@Override
	public float fieldMagnitude(float x, float y, float z) {
		float result[] = new float[3];
		field(x, y, z, result);
		return vectorLength(result);
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
		phi = Math.toRadians(phi);
    	double x = rho*Math.cos(phi);
    	double y = rho*Math.sin(phi);
    	gradient((float)x, (float)y, (float)z, result);
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
     public void gradient(float x, float y, float z, float result[]) {
 		
  		//TODO improve
  		float[] fr1 = new float[3];
 		float[] fr2 = new float[3];
 		float del = 10f; //cm 
  		
 		field(x-del, y, z, fr1);
 		field(x+del, y, z, fr2);
 		result[0] = (fr2[0]-fr1[0])/(2*del);
// 		System.err.println("---------");
// 		System.err.println("x = " + x + " y = " + y + " z = " + z);
// 		System.err.println(" f1z = " + fr1[0]);
//		System.err.println(" f2z = " + fr2[0]);

 		field(x, y-del, z, fr1);
 		field(x, y+del, z, fr2);
 		result[1] = (fr2[1]-fr1[1])/(2*del);
 		
 		field(x, y, z-del, fr1);
 		field(x, y, z+del, fr2);
 		
// 		System.err.println("---------");
// 		System.err.println(" f1z = " + fr1[2]);
//		System.err.println(" f2z = " + fr2[2]);
		result[2] = (fr2[2]-fr1[2])/(2*del);	
     }

	
	/**
	 * Get the appropriate probe for the given field
	 * @return the probe for the givev field
	 */
	public static FieldProbe factory(IField field) {
		
//		System.err.println("new probe");

		
//		if (field == null) {
//			System.err.println("null field in probe factory");
//		}
		
		if (field != null) {
			
			if (field instanceof Torus) {
				return new TorusProbe((Torus)field);
			}
			else if (field instanceof Solenoid) {
				return new SolenoidProbe((Solenoid)field);
			}
			else if (field instanceof RotatedCompositeField) {
				return new RotatedCompositeProbe((RotatedCompositeField)field);
			}
			else if (field instanceof CompositeField) {
				return new CompositeProbe((CompositeField)field);
			}
			else {
				System.err.println("WARNING: cannot create probe for " + field.getName());
			}
		}

//		System.err.println("WARNING: null probe");
		return null;
	}
	
	/**
	 * Vector length.
	 *
	 * @param v
	 *            the v
	 * @return the float
	 */
	protected final float vectorLength(float v[]) {
		float vx = v[0];
		float vy = v[1];
		float vz = v[2];
		return (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
	}


    /**
     * Is the physical magnet represented by the map misaligned?
     * @return <code>true</code> if magnet is misaligned
     */
    public boolean isMisaligned() {
    	return _field.isMisaligned();
    }
}
