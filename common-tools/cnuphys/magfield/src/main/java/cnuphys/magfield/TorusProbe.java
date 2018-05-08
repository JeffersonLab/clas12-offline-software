package cnuphys.magfield;

/**
 *
 * @author gavalian
 */
public class TorusProbe extends FieldProbe {

	private Cell3D _cell;

	public TorusProbe(Torus field) {
		super(field);
		_cell = new Cell3D(field);
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
		((Torus)_field).fieldCylindrical(_cell, phi, rho, z, result);
	}
	


}
