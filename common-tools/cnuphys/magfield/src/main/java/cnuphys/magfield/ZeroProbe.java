package cnuphys.magfield;

public class ZeroProbe extends FieldProbe {
	
	public ZeroProbe() {
		super(null);
	}
	
	/**
	 * Get the name of the field
	 */
	@Override
	public String getName() {
		return "No Field";
	}

	@Override
	public float fieldMagnitudeCylindrical(double phi, double r, double z) {
		return 0;
	}

	@Override
	public float fieldMagnitude(float x, float y, float z) {
		return 0;
	}

	@Override
	public float getMaxFieldMagnitude() {
		return 0;
	}

	@Override
	public boolean isZeroField() {
		return true;
	}

	@Override
    public void gradient(float x, float y, float z, float result[]) {
		result[0] = 0;
		result[1] = 0;
		result[2] = 0;
    }

	@Override
    public void gradientCylindrical(double phi, double rho, double z,
    	    float result[]) {
		result[0] = 0;
		result[1] = 0;
		result[2] = 0;
    }


	@Override
	public void field(float x, float y, float z, float[] result) {
		result[0] = 0;
		result[1] = 0;
		result[2] = 0;
	}
	
	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		result[0] = 0;
		result[1] = 0;
		result[2] = 0;
	}

	
	@Override
	public boolean contains(double x, double y, double z) {
		return false;
	}


}
