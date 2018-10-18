package cnuphys.apacheSwim;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import org.jlab.clas.clas.math.FastMath;
import cnuphys.magfield.FieldProbe;

/**
 * This class holds the derivatives (ODE) for swimmin with z as the
 * independent variable. The integration follows the method described for the
 * HERA-B magnet here: http://arxiv.org/pdf/physics/0511177v1.pdf<br>
 * <p>
 * The "official" state vector has five elements: <br>
 * (x, y, tx, ty, q) <br>
 * Where x and y are the transverse coordinates (meters), tx = px/pz, ty = py/pz,
 * and q = Q/|p| where Q is the integer charge (e.g. -1 for an electron)
 * <p>
 * However, since q is a constant, we only have four first order differential eqns.
 * <p>
 * UNITS
 * <ul>
 * <li>x, y, and z are in meters
 * <li>p is in GeV/c
 * <li>B (mag field) is in kGauss
 * </ul>
 * <p>
 * 
 * @author heddle
 *
 */
final class SwimZODE implements FirstOrderDifferentialEquations {
	
	
	// obtains the field in kG, coordinates should be in cm
	private FieldProbe _probe;

	// the constant member of the state vector
	private double _q;
	
	//q times v
	private double _qv;

	//hold the mag field
	private float B[] = new float[3];

	public SwimZODE() {
		set(0, Double.NaN, null);
	}

	/**
	 * Create the derivative (i.e. the differential equations)
	 * 
	 * @param Q
	 *            -1 for electron, +1 for proton, etc.
	 * @param p
	 *            the magnitude of the momentum in GeV/c.
	 * @param probe
	 *            the magnetic field getter
	 */
	public SwimZODE(int Q, double p, FieldProbe probe) {
		set(Q, p, probe);
	}
	
	/**
	 * Set the parameters
	 * 
	 * @param Q
	 *            -1 for electron, +1 for proton, etc.
	 * @param p
	 *            the magnitude of the momentum in GeV/c.
	 * @param probe
	 *            the magnetic field getter
	 */
	public void set(int Q, double p, FieldProbe probe) {
		_q = Q / p;
		_probe = probe;
		_qv = _q * apacheSwimZ.C;
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
	public void computeDerivatives(double z, double[] x, double[] dxdz)
			throws MaxCountExceededException, DimensionMismatchException {
		

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

	@Override
	public int getDimension() {
		return 4;
	}
	
}