package cnuphys.swim;

import cnuphys.magfield.FieldProbe;
import cnuphys.rk4.IDerivative;

/**
 * This is for integrating using time as the independent variable. 
 * The state vector is (tx = px/p, ty = py/p, tz = pz/p)
 * is the unit vector in the direction of momentum (or velocity)
 * @author heddle
 *
 */
public class TimeDerivative implements IDerivative {
	
	//for getting the magnetic field
	private FieldProbe _probe;

	private double _momentum;  //momentum GeV/c
	
	private double _u; //velocity m/s

	// alpha is qe/p where q is the integer charge,
	// e is the electron charge = (10^-9) in GeV/(T*m)
	// p is in GeV/c
	private double _alpha;
	
	//for mag field result
	float b[] = new float[3];

	/**
	 * Create a time derivative
	 * @param charge the integer charge
	 * @param momentum the momentun in GeV/c
	 * @param u the velocity magnitude in m/c
	 * @param field the fieldprobe
	 */
	public TimeDerivative(int charge, double momentum, double u, FieldProbe field) {
		_probe = field;
		_momentum = momentum;
		_u = u;
		_alpha = _u * 1.0e-9 * charge * Swimmer.C / _momentum;
		
	}

	@Override
	public void derivative(double t, double[] u, double[] du) {
		double Bx = 0.0;
		double By = 0.0;
		double Bz = 0.0;

		if (_probe != null) {

			float b[] = new float[3];

			// convert to cm
			double xx = u[0] * 100;
			double yy = u[1] * 100;
			double zz = u[2] * 100;

			_probe.field((float) xx, (float) yy, (float) zz, b);
			// convert to tesla
			Bx = b[0] / 10.0;
			By = b[1] / 10.0;
			Bz = b[2] / 10.0;
		}

		du[3] = _alpha * (u[4] * Bz - u[5] * By); // vyBz-vzBy
		du[4] = _alpha * (u[5] * Bx - u[3] * Bz); // vzBx-vxBz
		du[5] = _alpha * (u[3] * By - u[4] * Bx); // vxBy-vyBx
		du[0] = _u * u[3];
		du[1] = _u * u[4];
		du[2] = _u * u[5];	}

}
