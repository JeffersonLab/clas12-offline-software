package cnuphys.swimZ;

import cnuphys.magfield.FastMath;
import cnuphys.magfield.FieldProbe;
import cnuphys.rk4.IDerivative;

public class SwimZDerivative implements IDerivative {

	// obtains the field in kG, coordinates should be in cm
	protected FieldProbe _probe;

	// the constant member of the state vector
	protected double _q;

	// q times v
	protected double _qv;

	// hold the mag field
	protected float B[] = new float[3];

	public SwimZDerivative() {
		set(0, Double.NaN, null);
	}

	/**
	 * The derivative for swimming through a magnetic field
	 * 
	 * @param Q     -1 for electron, +1 for proton, etc.
	 * @param p     the magnitude of the momentum in GeV/c.
	 * @param probe the magnetic field getter
	 */
	public SwimZDerivative(int Q, double p, FieldProbe probe) {
		set(Q, p, probe);
	}

	/**
	 * Set the parameters
	 * 
	 * @param Q     -1 for electron, +1 for proton, etc.
	 * @param p     the magnitude of the momentum in GeV/c.
	 * @param probe the magnetic field getter
	 */
	public void set(int Q, double p, FieldProbe probe) {
		_q = Q / p;
		_probe = probe;
		_qv = _q * SwimZ.C;
	}

	/**
	 * Compute the derivatives given the value of the independent variable and the
	 * values of the function. Think of the Differential Equation as being dydt =
	 * f[y,t].
	 * 
	 * @param z    the value of the independent variable (the z coordinate) (input).
	 * @param x    the values of the state vector (x, y, tx, ty, q) at z (input).
	 * @param dxdz will be filled with the values of the derivatives at z (output).
	 */
	@Override
	public void derivative(double z, double[] x, double[] dxdz) {

		// get the field
		_probe.field((float) x[0], (float) x[1], (float) z, B);

		// some needed factors
		double tx = x[2];
		double ty = x[3];
		double txsq = tx * tx;
		double tysq = ty * ty;
		double fact = FastMath.sqrt(1 + txsq + tysq);
//		double txty = tx*ty;
		double Ax = fact * (ty * (tx * B[0] + B[2]) - (1 + txsq) * B[1]);
		double Ay = fact * (-tx * (ty * B[1] + B[2]) + (1 + tysq) * B[0]);
//		double Ax = fact * (txty * B[0] + ty*B[2] - (1 + txsq) * B[1]);
//		double Ay = fact * (-txty * B[1] - tx*B[2] + (1 + tysq) * B[0]);

		dxdz[0] = tx;
		dxdz[1] = ty;
		dxdz[2] = _qv * Ax;
		dxdz[3] = _qv * Ay;
	}

}
