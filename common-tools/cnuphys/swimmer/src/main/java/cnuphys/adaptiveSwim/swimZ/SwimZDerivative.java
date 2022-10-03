package cnuphys.adaptiveSwim.swimZ;

import cnuphys.magfield.FieldProbe;
import cnuphys.rk4.IDerivative;

/**
 * This class calculates the 4D swimZ derivatives. The swimZ integration follows
 * the method described for the HERA-B magnet here:
 * http://arxiv.org/pdf/physics/0511177v1.pdf<br>
 * <p>
 * In this swim, z is the independent variable. It is only good for swims where
 * z is monotonic. It can handle z increasing or z decreasing (backwars swim)
 * but not where z changes direction.
 * <p>
 * The state vector has five elements: <br>
 * (x, y, tx, ty, q) <br>
 * Where x and y are the transverse coordinates, tx = px/pz, ty = py/pz, and q =
 * Q/|p| where Q is the integer charge (e.g. -1 for an electron)
 * <p>
 * Note q is constant, so this is only a 4D problem, unlike the regular swimmer
 * which is 6D. That's why we go through all the trouble.
 * <p>
 * UNITS (NOT THE SAME AS THE REGULAR SWIMMER!!!!)
 * <ul>
 * <li>x, y, and z are in cm
 * <li>p is in GeV/c
 * <li>B (mag field) is in kGauss
 * </ul>
 * <p>
 * 
 * @author heddle
 *
 */
public final class SwimZDerivative implements IDerivative {

	// obtains the field in kG, coordinates should be in cm
	protected FieldProbe _probe;

	// the constant member of the state vector
	protected double _q;

	// q times v
	protected double _qv;

	// hold the mag field
	protected float B[] = new float[3];


	/**
	 * The derivative for swimming through a magnetic field
	 * 
	 * @param Q     -1 for electron, +1 for proton, etc.
	 * @param p     the magnitude of the momentum in GeV/c.
	 * @param probe the magnetic field getter
	 */
	public SwimZDerivative(int Q, double p, FieldProbe probe) {
		_q = Q / p;
		_probe = probe;
		_qv = _q * SwimZ.C;
	}

	/**
	 * Compute the derivatives given the value of the independent variable and the
	 * values of the function.
	 * 
	 * @param z    the value of the independent variable (the z coordinate) (input).
	 * @param u    the values of the state vector (x, y, tx, ty, q) at z (input).
	 *             FOR THIS SWIMMER LENGTH UNITS ARE CM
	 * @param dudz will be filled with the values of the derivatives at z (output).
	 */
	@Override
	public void derivative(double z, double[] u, double[] dudz) {

		// get the field
		_probe.field((float) u[0], (float) u[1], (float) z, B);

		// some needed factors
		double tx = u[2];
		double ty = u[3];
		double txsq = tx * tx;
		double tysq = ty * ty;
		double fact = Math.sqrt(1 + txsq + tysq);
		double Ax = fact * (ty * (tx * B[0] + B[2]) - (1 + txsq) * B[1]);
		double Ay = fact * (-tx * (ty * B[1] + B[2]) + (1 + tysq) * B[0]);

		dudz[0] = tx;
		dudz[1] = ty;
		dudz[2] = _qv * Ax;
		dudz[3] = _qv * Ay;
	}

}