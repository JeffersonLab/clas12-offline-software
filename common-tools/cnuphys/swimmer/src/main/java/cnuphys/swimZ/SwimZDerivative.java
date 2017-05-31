package cnuphys.swimZ;

import cnuphys.magfield.IField;
import cnuphys.rk4.IDerivative;

public class SwimZDerivative implements IDerivative {

	// obtains the field in kG, coordinates should be in cm
	private IField _field;

	// magnitude of the momentum in GeV/c
	private double _p;

	// the integer charge
	private int _Q;

	// the constant member of the state vector
	private double _q;

	private float B[] = new float[3];

	/**
	 * The derivative for swimming through a magnetic field
	 * 
	 * @param Q
	 *            -1 for electron, +1 for proton, etc.
	 * @param p
	 *            the magnitude of the momentum in GeV/c.
	 * @param field
	 *            the magnetic field getter
	 */
	public SwimZDerivative(int Q, double p, IField field) {
		_Q = Q;
		_p = p;
		_q = Q / p;
		_field = field;
	}

	/**
	 * Compute the derivatives given the value of the independent variable and
	 * the values of the function. Think of the Differential Equation as being
	 * dydt = f[y,t].
	 * 
	 * @param z
	 *            the value of the independent variable (the z coordinate)
	 *            (input).
	 * @param x
	 *            the values of the state vector (x, y, tx, ty, q) at z (input).
	 * @param dxdz
	 *            will be filled with the values of the derivatives at z
	 *            (output).
	 */
	@Override
	public void derivative(double z, double[] x, double[] dxdz) {

		double qv = _q * SwimZ.V;

		// get the field
		_field.field((float) x[0], (float) x[1], (float) z, B);

		// some needed factors
		double tx = x[2];
		double ty = x[3];
		double txsq = tx * tx;
		double tysq = ty * ty;
		double fact = Math.sqrt(1 + txsq + tysq);
		double Ax = fact * (ty * (tx * B[0] + B[2]) - (1 + txsq) * B[1]);
		double Ay = fact * (-tx * (ty * B[1] + B[2]) + (1 + tysq) * B[0]);

		dxdz[0] = tx;
		dxdz[1] = ty;
		dxdz[2] = qv * Ax;
		dxdz[3] = qv * Ay;
	}

}
