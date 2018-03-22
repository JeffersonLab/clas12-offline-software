package cnuphys.magfield;

public class UniformProbe extends FieldProbe {

	public UniformProbe(Uniform field) {
		super(field);
	}

	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		_field.fieldCylindrical(phi, rho, z, result);
	}

}
