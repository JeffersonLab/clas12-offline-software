package cnuphys.adaptiveSwim;

import cnuphys.rk4.ButcherTableau;
import cnuphys.rk4.IDerivative;

public class AdaptiveSwimUtilities {
	
	public enum DebugLevel  {OFF, ON, VERBOSE};
	
	//the debul level
	private static DebugLevel _debugLevel = DebugLevel.OFF;

	//tolerance when swimmimg to a max path length
	public static final double SMAX_TOLERANCE = 1.0e-4;  //meters

	//step size limits in meters
	public static final double MIN_STEPSIZE = 1.0e-6; // meters
	
	//maximum number of integration steps
	public static int MAX_NUMSTEP = 2000; 
	
	
	/**
	 * Basic adaptive step size driver that tries to integrate from s = 0 to s = sf,
	 * where sf is in the stopper object. Often this will terminate because the stopper
	 * stops the integration before sf is reached.
	 * 
	 * @param h            the step size at the start
	 * @param deriv        the derivative computer (interface). This is where the
	 *                     problem specificity resides.
	 * @param stopper      will be used to exit the
	 *                     integration early because some condition has been
	 *                     reached.
	 * @param advancer     takes the next single step
	 * @param eps          tolerance (e.g., 1.0e-6)
	 * @param uf           will hold final state vector
	 *                     
	 * @return the number of steps used.
	 * @throw AdaptiveSwimException
	 */
	public static int driver(double h, IDerivative deriv, IAdaptiveStopper stopper,
			IAdaptiveAdvance advancer, double eps, double uf[]) throws AdaptiveSwimException {
		
		// get the dimensionality of the problem. e.g, 6 if u = (x, y, z, tx, ty, tz)
		double[] u0 = stopper.getU();
		int nDim = u0.length;
		
				
		// ut is the running value of the state vector,
		// typically [x, y, z, tx, ty, tz]
		double ut[] = new double[nDim];
		
		//du is for derivatives
		double du[] = new double[nDim];

		//init the final value to be the same as the start value
		System.arraycopy(u0, 0, uf, 0, nDim);
		
		//track the number of steps
		int nstep = 0;
				
		AdaptiveStepResult result = new AdaptiveStepResult();
		
		//keep taking single steps until we reach the upper limit 
		//or the stopper stops us
		while (Math.abs(stopper.getRemainingRange())> SMAX_TOLERANCE) {
			
			if (h > stopper.getRemainingRange()) {
				h = stopper.getRemainingRange();
			}
			
			System.arraycopy(uf, 0, ut, 0, nDim);

			//compute derivs at current step
			deriv.derivative(stopper.getS(), ut, du);
			advancer.advance(stopper.getS(), ut, du, h, deriv, uf, eps, result);
			
			double hnew = result.getHNew();
			
			if (hnew < MIN_STEPSIZE) {
				if (_debugLevel == DebugLevel.VERBOSE) {
					throw new AdaptiveSwimException("");
				}
			}
			h = Math.max(MIN_STEPSIZE, Math.min(stopper.getMaxStepSize(), hnew));
			double snew = result.getSNew();
			
			nstep++;
			
			//will the stopper terminate?
			if (stopper.stopIntegration(snew, uf)) {
				return nstep;
			} 

		}
		
		return nstep;
	}

	/**
	 * Take a single step using basic fourth order RK
	 * 
	 * @param s     the independent variable
	 * @param u     the current state vector
	 * @param du    the current derivatives
	 * @param h     the step size
	 * @param deriv can compute the rhs of the diffy q
	 * @param uf    the state vector after the step
	 */
	public static void singleRK4Step(final double s, double[] u, double[] du, final double h, IDerivative deriv, double[] uf) {

		int nDim = u.length;

		// note that du (input) is k1
		double k1[] = du; // the current derivatives

		double k2[] = new double[nDim];
		double k3[] = new double[nDim];
		double k4[] = new double[nDim];
		double utemp[] = new double[nDim];

		double hh = h * 0.5; // half step
		double h6 = h / 6.0;

		// advance t to mid point
		double sMid = s + hh;

		// first step: initial derivs to midpoint
		for (int i = 0; i < nDim; i++) {
			utemp[i] = u[i] + hh * k1[i];
		}
		deriv.derivative(sMid, utemp, k2);

		// 2nd step (like 1st, but use midpoint just computed derivs dyt)
		for (int i = 0; i < nDim; i++) {
			utemp[i] = u[i] + hh * k2[i];
		}
		deriv.derivative(sMid, utemp, k3);

		// third (full) step
		for (int i = 0; i < nDim; i++) {
			utemp[i] = u[i] + h * k3[i];
		}
		deriv.derivative(s + h, utemp, k4);

		for (int i = 0; i < nDim; i++) {
			uf[i] = u[i] + h6 * (k1[i] + 2 * k2[i] + 2 * k3[i] + k4[i]);
		}

	}
	
	
	/**
	 * Take a single step using a Butcher tableau
	 * 
	 * @param s     the independent variable
	 * @param u     the current state vector
	 * @param du    the current derivatives
	 * @param h     the step size
	 * @param deriv can compute the rhs of the diffy q
	 * @param uf    the state vector after the step (output)
	 * @param error the error estimate (output)
	 * @param tableau the Butcher tableau
	 */
	public static void singleButcherStep(double s, double[] u, double[] du, double h, IDerivative deriv, double[] uf,
			double[] error, ButcherTableau tableau) {
		
		int nDim = u.length;
		int numStage = tableau.getNumStage();

		double utemp[] = new double[nDim];
		double k[][] = new double[numStage + 1][];
		k[0] = null; // not used

		// k1 is just h*du
		k[1] = new double[nDim];
		for (int i = 0; i < nDim; i++) {
			k[1][i] = h * du[i];
		}

		// fill the numStage k vectors
		for (int stage = 2; stage <= numStage; stage++) {
			k[stage] =new double[nDim];

			double ts = s + tableau.c(stage);
			for (int i = 0; i < nDim; i++) {
				utemp[i] = u[i];
				for (int ss = 1; ss < stage; ss++) {
					utemp[i] += tableau.a(stage, ss) * k[ss][i];
				}
			}
			deriv.derivative(ts, utemp, k[stage]);
			for (int i = 0; i < nDim; i++) {
				k[stage][i] *= h;
			}
		}
		
		for (int i = 0; i < nDim; i++) {
			double sum = 0.0;
			for (int stage = 1; stage <= numStage; stage++) {
				sum += tableau.b(stage) * k[stage][i];
			}
			uf[i] = u[i] + sum;
		}

		// compute error?
		if (tableau.isAugmented() && (error != null)) {

			// absolute error
			for (int i = 0; i < nDim; i++) {
				error[i] = 0.0;
				// error diff 4th and 5th order
				for (int stage = 1; stage <= numStage; stage++) {
					error[i] += tableau.bdiff(stage) * k[stage][i]; // abs error
				}
			}

			// relative error
			// for (int i = 0; i < nDim; i++) {
			// double sum = 0.0;
			// for (int s = 1; s <= numStage; s++) {
			// sum += tableau.bstar(s)*k[s][i];
			// }
			// double ystar = y[i] + sum;
			// error[i] = relativeDiff(yout[i], ystar);
			// }

			// for (int i = 0; i < nDim; i++) {
			// System.out.print(String.format("[%-12.5e] ", error[i]));
			// }
			// System.out.println();

		}


	}
	
	/**
	 * Set the debug level
	 * @param level the new debug level
	 */
	public static void setDebugLevel(DebugLevel level) {
		_debugLevel = level;
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


}
