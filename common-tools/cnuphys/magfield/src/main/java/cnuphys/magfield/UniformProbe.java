package cnuphys.magfield;

public class UniformProbe extends FieldProbe {

	public UniformProbe(Uniform field) {
		super(field);
	}

	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		_field.fieldCylindrical(phi, rho, z, result);
	}

    @Override
    public void field(int s, float x, float y, float z, float[] result) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
