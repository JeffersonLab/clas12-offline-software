package cnuphys.magfield;

public class SolenoidProbe extends FieldProbe {
	
	private Cell2D _cell;

	public SolenoidProbe(Solenoid field) {
		super(field);
		_cell = new Cell2D(field);
	}
	
	@Override
	public void field(float x, float y, float z, float result[]) {
		
		if (!contains(x, y, z)) {
			result[0] = 0f;
			result[1] = 0f;
			result[2] = 0f;
			return;
		}

		double rho = FastMath.sqrt(x * x + y * y);
		double phi = FastMath.atan2Deg(y, x);
		fieldCylindrical(phi, rho, z, result);
	}

	
	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		((Solenoid)_field).fieldCylindrical(_cell, phi, rho, z, result);
	}


}
