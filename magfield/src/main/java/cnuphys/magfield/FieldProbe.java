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
	public final void field(float x, float y, float z, float result[]) {
		float rho = (float) Math.sqrt(x * x + y * y);

		float phi = (float) MagneticField.atan2Deg(y, x);
		fieldCylindrical(phi, rho, z, result);
	}


	@Override
	public float fieldMagnitudeCylindrical(double phi, double r, double z) {
		return _field.fieldMagnitudeCylindrical(phi, r, z);
	}

	@Override
	public float fieldMagnitude(float x, float y, float z) {
		return _field.fieldMagnitude(x, y, z);
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
	
	public static FieldProbe factory(IField field) {
		
		if (field == null) {
			System.err.println("null field in probe factory");
		}
		
		if (field != null) {

//			System.err.println("Will create probe for " + field.getName());
			
			if (field instanceof Torus) {
				return new TorusProbe((Torus)field);
			}
			else if (field instanceof Solenoid) {
				return new SolenoidProbe((Solenoid)field);
			}
			else if (field instanceof CompositeField) {
				return new CompositeProbe((CompositeField)field);
			}
			else if (field instanceof RotatedCompositeField) {
				return new RotatedCompositeProbe((RotatedCompositeField)field);
			}
			else {
				System.err.println("WARNING: cannot create probe for " + field.getName());
			}
		}

		System.err.println("WARNING: null probe");
		(new Throwable()).printStackTrace();
		return null;
	}

}
