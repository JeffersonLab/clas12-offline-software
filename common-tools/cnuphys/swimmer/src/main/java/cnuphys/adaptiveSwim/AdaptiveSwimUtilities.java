package cnuphys.adaptiveSwim;

import cnuphys.rk4.ButcherTableau;
import cnuphys.rk4.IDerivative;

public class AdaptiveSwimUtilities {

	//step size limits in meters
	public static final double MIN_STEPSIZE = 1.0e-8; // meters

	//maximum number of integration steps
	public static int MAX_NUMSTEP = 2000;

	//limit warning prints
	private static int _warningCount = 0;

	//workspace
	// ut is the running value of the state vector,
	// [x, y, z, tx, ty, tz]
	private static double _ut[] = new double[AdaptiveSwimmer.DIM];

	//du is for derivatives
	private static double _du[] = new double[AdaptiveSwimmer.DIM];


	private static double _unew[] = new double[AdaptiveSwimmer.DIM];

	//workspace for single step
	private static int _numStage = -99;

	private static double[] _utemp;
	private static double _k[][];



	/**
	 * Basic adaptive step size driver that tries to swim until a stop condition
	 * is met.
	 *
	 * @param h            the step size at the start
	 * @param deriv        the derivative computer (interface). This is where the
	 *                     problem specificity resides.
	 * @param stopper      will be used to exit the swimming
	 * @param advancer     takes the next single step
	 * @param eps          tolerance (e.g., 1.0e-6)
	 * @return the number of steps used.
	 * @throw AdaptiveSwimException
	 */
	public static int driver(double h, IDerivative deriv, IAdaptiveStopper stopper,
			IAdaptiveAdvance advancer, double eps) throws AdaptiveSwimException {


		//init the new value to be the same as the curent value
		System.arraycopy(stopper.getU(), 0, _unew, 0, AdaptiveSwimmer.DIM);

		//track the number of steps
		int nstep = 0;

		double snew = stopper.getS();

		AdaptiveStepResult result = new AdaptiveStepResult();

		//keep taking single steps until we reach the upper limit
		//or the stopper stops us
		while (nstep < MAX_NUMSTEP) {
			System.arraycopy(_unew, 0, _ut, 0, AdaptiveSwimmer.DIM);

			//compute derivs at current step
			deriv.derivative(stopper.getS(), _ut, _du);
			advancer.advance(stopper.getS(), _ut, _du, h, deriv, _unew, eps, result);

			double hnew = result.getHNew();

			h = Math.max(MIN_STEPSIZE, Math.min(stopper.getMaxStepSize(), hnew));
			snew = result.getSNew();

			nstep++;

			//will the stopper terminate?
			if (stopper.stopIntegration(snew, _unew)) {
				return nstep;
			}

		}

		if (_warningCount < 4) {
			System.err.println("In the adaptive swimmer, the step count reached the max limit of " + MAX_NUMSTEP +
					" which usually indicates bad stopper logic.");
			System.err.println(" snew = " +  snew);
			System.err.println(String.format("ut = (%11.7f, %11.7f, %11.7f)", _ut[0], _ut[1], _ut[2]));
			_warningCount++;
		}

		return nstep;
	}


	/**
	 * Take a single step using a Butcher tableau
	 *
	 * @param s     the independent variable
	 * @param u0    the current state vector
	 * @param du    the current derivatives
	 * @param h     the step size
	 * @param deriv can compute the rhs of the diffy q
	 * @param uf    the state vector after the step (output)
	 * uf cannot be the same as u0
	 * @param error the error estimate (output)
	 * @param tableau the Butcher tableau
	 */
	public static void singleButcherStep(double s, double[] u0, double[] du, double h, IDerivative deriv, double[] uf,
			double[] error, ButcherTableau tableau) {

		//one time initialization
		if (tableau.getNumStage() != _numStage) {
			_numStage = tableau.getNumStage();
			_utemp = new double[AdaptiveSwimmer.DIM];
			_k = new double[_numStage + 1][];
			_k[0] = null; // not used
			_k[1] = new double[AdaptiveSwimmer.DIM];


			for (int stage = 2; stage <= _numStage; stage++) {
				_k[stage] =new double[AdaptiveSwimmer.DIM];
			}

		}

		// k1 is just h*du
		for (int i = 0; i < AdaptiveSwimmer.DIM; i++) {
			_k[1][i] = h * du[i];
		}

		// fill the numStage k vectors
		for (int stage = 2; stage <= _numStage; stage++) {

			double ts = s + tableau.c(stage);
			for (int i = 0; i < AdaptiveSwimmer.DIM; i++) {
				_utemp[i] = u0[i];
				for (int ss = 1; ss < stage; ss++) {
					_utemp[i] += tableau.a(stage, ss) * _k[ss][i];
				}
			}
			deriv.derivative(ts, _utemp, _k[stage]);
			for (int i = 0; i < AdaptiveSwimmer.DIM; i++) {
				_k[stage][i] *= h;
			}
		}

		for (int i = 0; i < AdaptiveSwimmer.DIM; i++) {
			double sum = 0.0;
			for (int stage = 1; stage <= _numStage; stage++) {
				sum += tableau.b(stage) * _k[stage][i];
			}
			uf[i] = u0[i] + sum;
		}

		// compute error?
		if (tableau.isAugmented() && (error != null)) {

			// absolute error
			for (int i = 0; i < AdaptiveSwimmer.DIM; i++) {
				error[i] = 0.0;
				// error diff 4th and 5th order
				for (int stage = 1; stage <= _numStage; stage++) {
					error[i] += tableau.bdiff(stage) * _k[stage][i]; // abs error
				}
			}

		}


	}

	/**
	 * Set the maximum number of steps beyond which an error occurs
	 * @param maxSteps the maximum number of steps. Default is 2000.
	 */
	public static void setMaxNumberSteps(int maxSteps) {
		MAX_NUMSTEP = maxSteps;
	}


	/**
	 * Get the sector [1..6] from the phi value
	 *
	 * @param phi the value of phi in degrees
	 * @return the sector [1..6]
	 */
	public static int getSector(double phi) {
		// convert phi to [0..360]

		while (phi < 0) {
			phi += 360.0;
		}
		while (phi > 360.0) {
			phi -= 360.0;
		}

		if ((phi > 30.0) && (phi <= 90.0)) {
			return 2;
		}
		if ((phi > 90.0) && (phi <= 150.0)) {
			return 3;
		}
		if ((phi > 150.0) && (phi <= 210.0)) {
			return 4;
		}
		if ((phi > 210.0) && (phi <= 270.0)) {
			return 5;
		}
		if ((phi > 270.0) && (phi <= 330.0)) {
			return 6;
		}
		return 1;
	}
	
	/**
	 * Get just the x y z location of the state vector in cm
	 * @param u the state vector
	 * @return the x y z location of the state vector in cm
	 */
	public static String uStringXYZ(double u[]) {
		return String.format("(%11.8f, %11.8f, %11.8f) cm", 100*u[0], 100*u[1], 100*u[2]);
	}
	
	/**
	 * Get just the x y z location of the state vector in cm
	 * with a prepended message
	 * @param u the state vector
	 * @return the x y z location of the state vector in cm
	 * with a prepended message
	 */
	public static String uStringXYZ(String message, double u[]) {
		return message + " " + uStringXYZ(u);
	}



}
