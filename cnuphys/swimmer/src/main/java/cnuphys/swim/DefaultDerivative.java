package cnuphys.swim;

import cnuphys.magfield.IField;
import cnuphys.rk4.IDerivative;

public class DefaultDerivative implements IDerivative {

    private IField _field;

    private double _momentum;

    private double _alpha;

    /**
     * The derivative for swimming through a magnetic field
     * 
     * @param charge
     *            -1 for electron, +1 for proton, etc.
     * @param momentum
     *            the magnitude of the momentum.
     * @param field
     *            the magnetic field
     */
    public DefaultDerivative(int charge, double momentum, IField field) {
	_field = field;
	_momentum = momentum;

	_alpha = 1.0e-9 * charge * Swimmer.C / _momentum;
    }

    /**
     * Compute the derivatives given the value of s (path length) and the values
     * of the state vector.
     * 
     * @param s
     *            the value of the independent variable path length (input).
     * @param Q
     *            the values of the state vector ([x,y,z, px/p, py/p, pz/p]) at
     *            s (input).
     * @param dydt
     *            will be filled with the values of the derivatives at t
     *            (output).
     */
    @Override
    public void derivative(double t, double[] Q, double[] dQds) {
	double Bx = 0.0;
	double By = 0.0;
	double Bz = 0.0;

	if (_field != null) {

	    float b[] = new float[3];

	    // convert to cm
	    double xx = Q[0] * 100;
	    double yy = Q[1] * 100;
	    double zz = Q[2] * 100;

	    _field.field((float) xx, (float) yy, (float) zz, b);
	    // convert to tesla
	    Bx = b[0] / 10.0;
	    By = b[1] / 10.0;
	    Bz = b[2] / 10.0;
	}

	dQds[3] = _alpha * (Q[4] * Bz - Q[5] * By); // vyBz-vzBy
	dQds[4] = _alpha * (Q[5] * Bx - Q[3] * Bz); // vzBx-vxBz
	dQds[5] = _alpha * (Q[3] * By - Q[4] * Bx); // vxBy-vyBx
	dQds[0] = Q[3];
	dQds[1] = Q[4];
	dQds[2] = Q[5];
    }

}
