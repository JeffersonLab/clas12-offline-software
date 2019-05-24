package cnuphys.swimZ;

import java.util.ArrayList;

import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.IMagField;
import cnuphys.magfield.MagneticField;
import cnuphys.magfield.RotatedCompositeProbe;
import cnuphys.rk4.DefaultStopper;
import cnuphys.rk4.IRkListener;
import cnuphys.rk4.IStopper;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.rk4.RungeKuttaZ;

/**
 * This class holds the parameters and static methods for the swimZ integration.
 * The swimZ integration follows the method described for the HERA-B magnet
 * here: http://arxiv.org/pdf/physics/0511177v1.pdf<br>
 * <p>
 * The state vector has five elements: <br>
 * (x, y, tx, ty, q) <br>
 * Where x and y are the transverse coordinates (meters), tx = px/pz, ty =
 * py/pz, and q = Q/|p| where Q is the integer charge (e.g. -1 for an electron)
 * <p>
 * UNITS
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
public class SwimZ {

	/** The speed of light in these units: (GeV/c)(1/kG)(1/cm) */
	public static final double C = 2.99792458e-04;

	/** Argon radiation length in cm */
	public static final double ARGONRADLEN = 14.;

	// Min momentum to swim in GeV/c
//	public static final double MINMOMENTUM = 5e-05;

	/** The current magnetic field probe */
	private FieldProbe _probe;

	// create a do nothing stopper for now
	private IStopper _stopper = new DefaultStopper();

	// need an integrator
	private RungeKuttaZ _rk4 = new RungeKuttaZ();

	// storage for values of independent variable z
	private ArrayList<Double> zArray = new ArrayList<Double>(100);

	// storage for values of the dependent variables (state vector)
	private ArrayList<double[]> yArray = new ArrayList<double[]>(100);

	// the derivatives (i.e., the ODEs)
	private SwimZDerivative deriv;

	/**
	 * In swimming routines that require a tolerance vector, this is a reasonable
	 * one to use for CLAS. These represent absolute errors in the adaptive stepsize
	 * algorithms
	 */
	private double _eps = 1.0e-3;

	// absolute tolerances
	private double _absoluteTolerance[] = new double[4];

	/**
	 * SwimZ constructor. Here we create a Swimmer that will use the given magnetic
	 * field.
	 * 
	 * @param field interface into a magnetic field
	 */
	public SwimZ() {
		_probe = FieldProbe.factory();
		initialize();
	}

	/**
	 * Create a swimmer specific to a magnetic field
	 * 
	 * @param magneticField the magnetic field
	 */
	public SwimZ(MagneticField magneticField) {
		_probe = FieldProbe.factory(magneticField);
		initialize();
	}

	/**
	 * Create a swimmer specific to a magnetic field
	 * 
	 * @param magneticField the magnetic field
	 */
	public SwimZ(IMagField magneticField) {
		_probe = FieldProbe.factory(magneticField);
		initialize();
	}

	/**
	 * Get the underlying field probe
	 * 
	 * @return the probe
	 */
	public FieldProbe getProbe() {
		return _probe;
	}

	// some initialization
	private void initialize() {

		if (_probe instanceof RotatedCompositeProbe) {
			deriv = new SectorSwimZDerivative();
		} else {
			deriv = new SwimZDerivative();
		}

		setAbsoluteTolerance(1.0e-3);
	}

	/**
	 * Set the tolerance used by the CLAS_Tolerance array
	 * 
	 * @param eps the baseline absolute tolerance.
	 */
	public void setAbsoluteTolerance(double eps) {
		_eps = eps;
		double xscale = 1.0; // position scale order of cm
		double pscale = 1.0; // track slope scale order of 1
		double xTol = eps * xscale;
		double pTol = eps * pscale;
		for (int i = 0; i < 2; i++) {
			_absoluteTolerance[i] = xTol;
			_absoluteTolerance[i + 2] = pTol;
		}
	}

	/**
	 * Get the tolerance used by the CLAS_Toleance array
	 * 
	 * @return the tolerance used by the CLAS_Toleance array
	 */
	public double getEps() {
		return _eps;
	}

	/**
	 * Swim to a fixed z over short distances using RK adaptive stepsize
	 * 
	 * @param Q            the integer charge of the particle (-1 for electron)
	 * @param p            the momentum in GeV/c
	 * @param start        the starting state vector
	 * @param zf           the final z value (cm)
	 * @param stepSize     the initial step size
	 * @param relTolerance the absolute tolerances on each state variable [x, y, tx,
	 *                     ty] (q = const). So it is an array with four entries,
	 *                     like [1.0e-4 cm, 1.0e-4 cm, 1.0e-5, 1.0e05]
	 * @param hdata        An array with three elements. Upon return it will have
	 *                     the min, average, and max step size (in that order).
	 * @return the swim result
	 * @throws SwimZException
	 */
	public SwimZResult adaptiveRK(int Q, double p, SwimZStateVector start, final double zf, double stepSize,
			double hdata[]) throws SwimZException {

		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		// straight line?
		if ((Q == 0) || (_probe == null) || _probe.isZeroField()) {
			return straightLineResult(Q, p, start, zf);
		}

		// ARGGH
		if (start.z > zf) {
			Q = -Q;
		}

		// need to set the derivative
		deriv.set(Q, p, _probe);

		double yo[] = { start.x, start.y, start.tx, start.ty };

		// create the lists to hold the trajectory
		zArray.clear();
		yArray.clear();

		int nStep = 0;
		try {
			nStep = _rk4.adaptiveStepToTf(yo, start.z, zf, stepSize, zArray, yArray, deriv, _stopper,
					_absoluteTolerance, hdata);
		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}

		if (nStep == 0) {
			return null;
		}

		SwimZResult result = new SwimZResult(Q, p, start.z, zf, nStep);
		result.add(start);
		for (int i = 0; i < zArray.size(); i++) {
			double v[] = yArray.get(i);
			SwimZStateVector sv = new SwimZStateVector(zArray.get(i), v);
			result.add(sv);
		}

		return result;

	}

	/**
	 * Swim to a fixed z over short distances using RK adaptive stepsize
	 * 
	 * @param sector       the sector [1..6]
	 * @param Q            the integer charge of the particle (-1 for electron)
	 * @param p            the momentum in GeV/c
	 * @param start        the starting state vector
	 * @param zf           the final z value (cm)
	 * @param stepSize     the initial step size
	 * @param relTolerance the absolute tolerances on each state variable [x, y, tx,
	 *                     ty] (q = const). So it is an array with four entries,
	 *                     like [1.0e-4 cm, 1.0e-4 cm, 1.0e-5, 1.0e05]
	 * @param hdata        An array with three elements. Upon return it will have
	 *                     the min, average, and max step size (in that order).
	 * @return the swim result
	 * @throws SwimZException
	 */
	public SwimZResult sectorAdaptiveRK(int sector, int Q, double p, SwimZStateVector start, final double zf,
			double stepSize, double hdata[]) throws SwimZException {

		if (!(_probe instanceof RotatedCompositeProbe)) {
			System.err.println("Can only call sectorAdaptiveRK with a RotatedComposite Probe");
			System.exit(1);
			return null;
		}

		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		// straight line?
		if ((Q == 0) || (_probe == null) || _probe.isZeroField()) {
			return straightLineResult(Q, p, start, zf);
		}

		// ARGGH
		if (start.z > zf) {
			Q = -Q;
		}

		// need to set the derivative
		((SectorSwimZDerivative) deriv).set(sector, Q, p, _probe);

		double yo[] = { start.x, start.y, start.tx, start.ty };

		// create the lists to hold the trajectory
		zArray.clear();
		yArray.clear();

		int nStep = 0;
		try {
			nStep = _rk4.adaptiveStepToTf(yo, start.z, zf, stepSize, zArray, yArray, deriv, _stopper,
					_absoluteTolerance, hdata);
		} catch (RungeKuttaException e) {
//			System.err.println("Integration Failure");
//			System.err.println("Q = " + Q + "  p = " + p + " zf = " + zf);
//			int pzSign = (zf < start.z) ? -1 : 1;
//			System.err.println("Start SV: " + start.normalPrint(p, pzSign));
			// e.printStackTrace();
			throw new SwimZException("Runge Kutta Failure in SwimZ sectorAdaptiveRK");
		}

		if (nStep == 0) {
			return null;
		}

		SwimZResult result = new SwimZResult(Q, p, start.z, zf, nStep);
		result.add(start);
		for (int i = 0; i < zArray.size(); i++) {
			double v[] = yArray.get(i);
			SwimZStateVector sv = new SwimZStateVector(zArray.get(i), v);
			result.add(sv);
		}

		return result;

	}

	/**
	 * Swim to a fixed z using RK adaptive stepsize
	 * 
	 * @param Q        the integer charge of the particle (-1 for electron)
	 * @param p        the momentum in Gev/c
	 * @param start    the starting state vector
	 * @param stop     will hold the final state vector
	 * @param zf       the final z value
	 * @param stepSize the initial step size
	 * @param hdata    An array with three elements. Upon return it will have the
	 *                 min, average, and max step size (in that order).
	 * @return the number of steps
	 * @throws SwimZException
	 */
	public int adaptiveRK(int Q, double p, SwimZStateVector start, SwimZStateVector stop, final double zf,
			double stepSize, double hdata[]) throws SwimZException {
		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		// straight line?
		if ((Q == 0) || (_probe == null) || _probe.isZeroField()) {
			System.out.println("Z adaptive swimmer detected straight line.");
			straightLineResult(Q, p, start, stop, zf);
			return 2;
		}

		// need to set the derivative
		deriv.set(Q, p, _probe);

		double yo[] = { start.x, start.y, start.tx, start.ty };

		IRkListener listener = new IRkListener() {

			@Override
			public void nextStep(double newZ, double[] newStateVec, double h) {
				stop.x = newStateVec[0];
				stop.y = newStateVec[1];
				stop.tx = newStateVec[2];
				stop.ty = newStateVec[3];
				stop.z = newZ;
			}

		};

		int nStep = 0;
		try {
			nStep = _rk4.adaptiveStepToTf(yo, start.z, zf, stepSize, deriv, _stopper, listener, _absoluteTolerance,
					hdata);
		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}

		return nStep;
	}

	/**
	 * Swim to a fixed z over short distances using a parabolic estimate, without
	 * intermediate points
	 * 
	 * @param Q     the integer charge of the particle (-1 for electron)
	 * @param p     the momentum in GeV/c
	 * @param start the starting state vector
	 * @param stop  at end, holds final state vector
	 * @param zf    the final z value (cm)
	 * @return the swim result
	 * @throws SwimZException
	 */

	public void parabolicEstimate(int Q, double p, SwimZStateVector start, SwimZStateVector stop, double zf)
			throws SwimZException {

		if (start == null) {
			throw new SwimZException("Null starting state vector.");
		}

		// straight line?
		if ((Q == 0) || (_probe == null) || _probe.isZeroField()) {
			System.out.println("Z parabolicEstimate swimmer detected straight line.");
			straightLineResult(Q, p, start, stop, zf);
			return;
		}

		double q = Q / p;

		// get the field
		float B[] = new float[3];
		double x0 = start.x;
		double y0 = start.y;
		double z0 = start.z;
		double tx0 = start.tx;
		double ty0 = start.ty;

		_probe.field((float) x0, (float) y0, (float) z0, B);

		// some needed factors
		double txsq = tx0 * tx0;
		double tysq = ty0 * ty0;
		double fact = Math.sqrt(1 + txsq + tysq);
		double Ax = fact * (ty0 * (tx0 * B[0] + B[2]) - (1 + txsq) * B[1]);
		double Ay = fact * (-tx0 * (ty0 * B[1] + B[2]) + (1 + tysq) * B[0]);

		double s = (stop.z - start.z);
		double qvs = q * C * s;
		double qvsq = 0.5 * qvs * s;

		stop.z = zf;
		stop.x = start.x + tx0 * s + qvsq * Ax;
		stop.y = start.y + ty0 * s + qvsq * Ay;
		stop.tx = tx0 + qvs * Ax;
		stop.ty = ty0 + qvs * Ay;

	}

	/**
	 * Swim to a fixed z over short distances using a parabolic estimate
	 * 
	 * @param Q        the integer charge of the particle (-1 for electron)
	 * @param p        the momentum in GeV/c
	 * @param start    the starting state vector
	 * @param zf       the final z value (cm)
	 * @param stepSize the step size
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

			_probe.field((float) x0, (float) y0, (float) z0, B);

			// some needed factors
			double txsq = tx0 * tx0;
			double tysq = ty0 * ty0;
			double fact = Math.sqrt(1 + txsq + tysq);
			double Ax = fact * (ty0 * (tx0 * B[0] + B[2]) - (1 + txsq) * B[1]);
			double Ay = fact * (-tx0 * (ty0 * B[1] + B[2]) + (1 + tysq) * B[0]);

			double s = stepSize;
			double qvs = q * C * s;
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

	// straight line
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

	// straight line
	private void straightLineResult(int Q, double p, SwimZStateVector start, SwimZStateVector stop, double zf) {
		double s = zf - start.z;
		stop.x = start.x + start.tx * s;
		stop.y = start.y + start.ty * s;
		stop.z = zf;
		stop.tx = start.tx;
		stop.ty = start.ty;
	}

	private double[] A(double tx, double ty, double Bx, double By, double Bz) {

		double C = Math.sqrt(1 + tx * tx + ty * ty);
		double Ax = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
		double Ay = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);

		return new double[] { Ax, Ay };
	}

	private double[] delA_delt(double tx, double ty, double Bx, double By, double Bz) {

		double C2 = 1 + tx * tx + ty * ty;
		double C = Math.sqrt(C2);
		double Ax = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
		double Ay = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);

		double delAx_deltx = tx * Ax / C2 + C * (ty * Bx - 2 * tx * By);
		double delAx_delty = ty * Ax / C2 + C * (tx * Bx + Bz);
		double delAy_deltx = tx * Ay / C2 + C * (-ty * By - Bz);
		double delAy_delty = ty * Ay / C2 + C * (-tx * By + 2 * ty * Bx);

		return new double[] { delAx_deltx, delAx_delty, delAy_deltx, delAy_delty };
	}
}
