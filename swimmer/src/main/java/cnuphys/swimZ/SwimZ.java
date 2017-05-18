package cnuphys.swimZ;

import java.util.ArrayList;

import cnuphys.magfield.IField;
import cnuphys.rk4.DefaultStopper;
import cnuphys.rk4.IStopper;
import cnuphys.rk4.RungeKutta;
import cnuphys.rk4.RungeKuttaException;

/**
 * This static class holds the parameters and static methods for the swimZ
 * integration. The swimZ integration follows the method described for the
 * HERA-B magnet here: http://arxiv.org/pdf/physics/0511177v1.pdf<br>
 * <p>
 * The state vector has five elements: <br>
 * (x, y, tx, ty, q) <br>
 * Where x and y are the transverse coordinates (cm), tx = px/pz, ty = py/pz,
 * and q = Q/|p| where Q is the integer charge (e.g. -1 for an electron)
 * <p>
 * UNITS
 * <ul>
 * <li>x, y, and z are in cm
 * <li>p is in GeV/c
 * <li>B (mag field) is in kGauss
 * </ul>
 * <p>
 * The global parameters are:
 * <ul>
 * 
 * </ul>
 * 
 * @author heddle
 *
 */
public class SwimZ {

	/** The speed of light in these units: (GeV/c)(1/kG)(1/cm) */
	public static final double V = 0.000299792458;

	// Min momentum to swim in GeV/c
	public static final double MINMOMENTUM = 5e-05;

	/** The current magnetic field */
	private IField _field;

	// create a do nothing stopper for now
	private IStopper _stopper = new DefaultStopper();

	/**
	 * SwimZ constructor. Here we create a Swimmer that will use the given
	 * magnetic field.
	 * 
	 * @param field
	 *            interface into a magnetic field
	 */
	public SwimZ(IField field) {
		_field = field;
	}

	/**
	 * Swim to a fixed z over short distances using RK adaptive stepsize
	 * 
	 * @param Q
	 *            the integer charge of the particle (-1 for electron)
	 * @param p
	 *            the momentum in Gev/c
	 * @param start
	 *            the starting state vector
	 * @param zf
	 *            the final z value
	 * @param stepSize
	 *            the initial step size
	 * @param relTolerance
	 *            the absolute tolerances on each state variable [x, y, tx, ty]
	 *            (q = const). So it is an array with four entries, like [1.0e-4
	 *            cm, 1.0e-4 cm, 1.0e-5, 1.0e05]
	 * @param hdata
	 *            An array with three elements. Upon return it will have the
	 *            min, average, and max step size (in that order).
	 * @return the swim result
	 * @throws SwimZException
	 */
	public SwimZResult adaptiveRK(int Q, double p, SwimZStateVector start, final double zf, double stepSize,
			double absError[], double hdata[]) throws SwimZException {
		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		// straight line?
		if (Q == 0) {
			System.out.println("Z adaptive swimmer detected straight line.");
			return straightLineResult(Q, p, start, zf);
		}

		// need a new derivative
		SwimZDerivative deriv = new SwimZDerivative(Q, p, _field);

		// need a RK4 object
		RungeKutta rk4 = new RungeKutta();

		double yo[] = { start.x, start.y, start.tx, start.ty };

		// create the lists to hold the trajectory
		ArrayList<Double> z = new ArrayList<Double>(100);
		ArrayList<double[]> y = new ArrayList<double[]>(100);

		int nStep = 0;
		try {
			nStep = rk4.adaptiveStepToTf(yo, start.z, zf, stepSize, z, y, deriv, _stopper, absError, hdata);
		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}

		if (nStep == 0) {
			return null;
		}

		SwimZResult result = new SwimZResult(Q, p, start.z, zf, nStep);
		result.add(start);
		for (int i = 0; i < z.size(); i++) {
			double v[] = y.get(i);
			SwimZStateVector sv = new SwimZStateVector(z.get(i), v);
			result.add(sv);
		}

		return result;

	}

	// /**
	// * Integrator that uses the RungeKutta advance with a Butcher Tableau and
	// * adaptive stepsize and a tolerance vector.
	// *
	// * This version uses an IRk4Listener to notify
	// * the listener that the next step has been advanced.
	// *
	// * A very typical case is a 2nd order ODE converted to a 1st order where
	// the
	// * dependent variables are x, y, z, vx, vy, vz and the independent
	// variable is
	// * time.
	// *
	// * @param yo initial values. Probably something like (xo, yo, zo, vxo,
	// vyo,
	// * vzo).
	// * @param to the initial value of the independent variable, e.g., time.
	// * @param tf the maximum value of the independent variable.
	// * @param h the starting steps size
	// * @param t a list of the values of t at each step
	// * @param y a list of the values of the state vector at each step
	// * @param deriv the derivative computer (interface). This is where the
	// problem specificity resides.
	// * @param stopper if not <code>null</code> will be used to exit the
	// * integration early because some condition has been reached.
	// * @param tableau the Butcher Tableau
	// * @param relTolerance the error tolerance as fractional diffs. Note it is
	// a vector, the same
	// * dimension of the problem, e.g., 6 for [x,y,z,vx,vy,vz].
	// * @param hdata if not null, should be double[3]. Upon return, hdata[0] is
	// the min stepsize
	// * used, hdata[1] is the average stepsize used, and hdata[2] is the max
	// stepsize used
	// * @return the number of steps used.
	// * @throws RungeKuttaException
	// */
	// public int adaptiveStep(double yo[], double to, double tf, double h,
	// final List<Double> t, final List<double[]> y, IDerivative deriv,
	// IStopper stopper, ButcherTableau tableau, double relTolerance[],
	// double hdata[]) throws RungeKuttaException {

	/**
	 * Swim to a fixed z over short distances using uniform stepsize RK4
	 * 
	 * @param Q
	 *            the integer charge of the particle (-1 for electron)
	 * @param p
	 *            the momentum in Gev/c
	 * @param start
	 *            the starting state vector
	 * @param zf
	 *            the final z value
	 * @param stepSize
	 *            the step size
	 * @return the swim result
	 * @throws SwimZException
	 */
	public SwimZResult uniformRK4(int Q, double p, SwimZStateVector start, double zf, double stepSize)
			throws SwimZException {
		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		// straight line?
		if (Q == 0) {
			System.out.println("Z uniform swimmer detected straight line.");
			return straightLineResult(Q, p, start, zf);
		}

		// need a new derivative
		SwimZDerivative deriv = new SwimZDerivative(Q, p, _field);

		// need a RK4 object
		RungeKutta rk4 = new RungeKutta();

		// obtain a range
		SwimZRange swimZrange = new SwimZRange(start.z, zf, stepSize);

		// create a do nothing stopper for now
		IStopper stopper = new DefaultStopper();

		// RK4 storage
		int nDim = 4; // should be able to make 4 since q = const
		int nStep = swimZrange.getNumStep();

		double yo[] = { start.x, start.y, start.tx, start.ty };
		double z[] = new double[nStep];
		double y[][] = new double[nDim][nStep];
		nStep = rk4.uniformStep(yo, start.z, zf, y, z, deriv, stopper);

		SwimZResult result = new SwimZResult(Q, p, start.z, zf, nStep + 1);
		result.add(start);
		for (int i = 0; i < nStep; i++) {

			double v[] = { y[0][i], y[1][i], y[2][i], y[3][i] };
			SwimZStateVector sv = new SwimZStateVector(z[i], v);
			result.add(sv);
		}

		return result;
	}

	/**
	 * Swim to a fixed z over short distances using a parabolic estimate
	 * 
	 * @param Q
	 *            the integer charge of the particle (-1 for electron)
	 * @param p
	 *            the momentum in Gev/c
	 * @param start
	 *            the starting state vector
	 * @param zf
	 *            the final z value
	 * @param stepSize
	 *            the step size
	 * @return the swim result
	 * @throws SwimZException
	 */
	public SwimZResult parabolicEstimate(int Q, double p, SwimZStateVector start, double zf, double stepSize)
			throws SwimZException {

		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		// straight line?
		if (Q == 0) {
			return straightLineResult(Q, p, start, zf);
		}

		double q = Q / p;

		// obtain a range
		SwimZRange swimZrange = new SwimZRange(start.z, zf, stepSize);

		// storage for results
		SwimZResult result = new SwimZResult(Q, p, start.z, zf, swimZrange.getNumStep() + 1);
		result.add(start);
		SwimZStateVector v0 = start;

		for (int i = 0; i < swimZrange.getNumStep(); i++) {
			// get the field
			float B[] = new float[3];
			double x0 = v0.x;
			double y0 = v0.y;
			double z0 = v0.z;
			double tx0 = v0.tx;
			double ty0 = v0.ty;

			_field.field((float) x0, (float) y0, (float) z0, B);

			// some needed factors
			double txsq = tx0 * tx0;
			double tysq = ty0 * ty0;
			double fact = Math.sqrt(1 + txsq + tysq);
			double Ax = fact * (ty0 * (tx0 * B[0] + B[2]) - (1 + txsq) * B[1]);
			double Ay = fact * (-tx0 * (ty0 * B[1] + B[2]) + (1 + tysq) * B[0]);

			double s = stepSize;
			double qvs = q * V * s;
			double qvsq = 0.5 * qvs * s;

			double x1 = x0 + tx0 * s + qvsq * Ax;
			double y1 = y0 + ty0 * s + qvsq * Ay;
			double tx1 = tx0 + qvs * Ax;
			double ty1 = ty0 + qvs * Ay;
			// public SwimZStateVector(double x, double y, double z, double tx,
			// double ty,
			// double q) {

			SwimZStateVector v1 = new SwimZStateVector(x1, y1, swimZrange.z(i + 1), tx1, ty1);

			// add to the resuts
			result.add(v1);
			v0 = v1;
		}

		return result;
	}

	private SwimZResult straightLineResult(int Q, double p, SwimZStateVector start, double zf) {
		SwimZResult result = new SwimZResult(Q, p, start.z, zf, 2);
		result.add(start);
		double s = zf - start.z;
		double x1 = start.x + start.tx * s;
		double y1 = start.y + start.ty * s;
		SwimZStateVector v = new SwimZStateVector(x1, y1, zf, start.tx, start.ty);
		result.add(v);
		return result;
	}

}
