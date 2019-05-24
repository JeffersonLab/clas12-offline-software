package cnuphys.rk4;

/**
 * Integrators used by the Z swimmer
 * @author heddle
 *
 */

import java.util.ArrayDeque;
import java.util.List;

/**
 * Static methods for Runge-Kutta 4 integration, including a constant stepsize
 * method and an adaptive stepsize method.
 * 
 * @author heddle
 * 
 */
public class RungeKuttaZ {

	// for adaptive stepsize, this is how much h will grow
	private static final double HGROWTH = 1.5;

	// think in cm
	public static double DEFMINSTEPSIZE = 1.0e-3;
	public static double DEFMAXSTEPSIZE = 40;

	private double _minStepSize = DEFMINSTEPSIZE;
	private double _maxStepSize = DEFMAXSTEPSIZE;

	// the max dimension we'll use is probably 6, for state vectors
	// [x,y,z,vx,vy,vz].
	private static int MAXDIM = 6; // we'll know if this fails!

	// object cache
	private ArrayDeque<double[]> _workArrayCache = new ArrayDeque<>();

	/**
	 * Create a RungeKutta object that can be used for integration
	 */
	public RungeKuttaZ() {
	}

	/**
	 * Integrator that uses the RungeKutta advance with a Butcher Tableau and
	 * adaptive stepsize and a tolerance vector.
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next step
	 * has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable is
	 * time.
	 * 
	 * @param yo           initial values. Probably something like (xo, yo, zo, vxo,
	 *                     vyo, vzo).
	 * @param to           the initial value of the independent variable, e.g.,
	 *                     time.
	 * @param tf           the maximum value of the independent variable.
	 * @param h            the starting steps size
	 * @param t            a list of the values of t at each step
	 * @param y            a list of the values of the state vector at each step
	 * @param deriv        the derivative computer (interface). This is where the
	 *                     problem specificity resides.
	 * @param stopper      if not <code>null</code> will be used to exit the
	 *                     integration early because some condition has been
	 *                     reached.
	 * @param relTolerance the error tolerance as fractional diffs. Note it is a
	 *                     vector, the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz].
	 * @param hdata        if not null, should be double[3]. Upon return, hdata[0]
	 *                     is the min stepsize used, hdata[1] is the average
	 *                     stepsize used, and hdata[2] is the max stepsize used
	 * @return the number of steps used.
	 * @throws RungeKuttaException
	 */
	public int adaptiveStepToTf(double yo[], double to, double tf, double h, final List<Double> t,
			final List<double[]> y, IDerivative deriv, IStopper stopper, double relTolerance[], double hdata[])
			throws RungeKuttaException {

		// put starting step in
		t.add(to);
		y.add(copy(yo));

		IRkListener listener = new IRkListener() {

			@Override
			public void nextStep(double tNext, double yNext[], double h) {
				t.add(tNext);
				y.add(copy(yNext));
			}

		};
		return adaptiveStepToTf(yo, to, tf, h, deriv, stopper, listener, relTolerance, hdata);
	}

	/**
	 * Integrator that uses the RungeKutta advance with a Butcher Tableau and
	 * adaptive stepsize
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next step
	 * has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable is
	 * time.
	 * 
	 * @param yo           initial values. Probably something like (xo, yo, zo, vxo,
	 *                     vyo, vzo).
	 * @param to           the initial value of the independent variable, e.g.,
	 *                     time.
	 * @param tf           the maximum value of the independent variable.
	 * @param h            the starting steps size
	 * @param deriv        the derivative computer (interface). This is where the
	 *                     problem specificity resides.
	 * @param stopper      if not <code>null</code> will be used to exit the
	 *                     integration early because some condition has been
	 *                     reached.
	 * @param listener     listens for each step * @param tableau the Butcher
	 *                     Tableau
	 * @param relTolerance the error tolerance as fractional diffs. Note it is a
	 *                     vector, the same
	 * @param hdata        if not null, should be double[3]. Upon return, hdata[0]
	 *                     is the min stepsize used, hdata[1] is the average
	 *                     stepsize used, and hdata[2] is the max stepsize used
	 * @return the number of steps used.
	 * @throws RungeKuttaException
	 */
	public int adaptiveStepToTf(double yo[], double to, double tf, double h, IDerivative deriv, IStopper stopper,
			IRkListener listener, double relTolerance[], double hdata[]) throws RungeKuttaException {

		// use a simple half-step advance
		IAdvance advancer = new HalfStepAdvance();

		int nStep = 0;
		try {
			nStep = driverToTf(yo, to, tf, h, deriv, stopper, listener, advancer, relTolerance, hdata);
		} catch (RungeKuttaException e) {
//			System.err.println("Trying to integrate from " + to + " to " + tf);
			throw e;
		}
		return nStep;
	}

	// copy a vector
	private double[] copy(double v[]) {
		double w[] = new double[v.length];
		System.arraycopy(v, 0, w, 0, v.length);

		// for (int i = 0; i < v.length; i++) {
		// w[i] = v[i];
		// }
		return w;
	}

	/**
	 * Driver that uses the RungeKutta advance with an adaptive step size
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next step
	 * has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable is
	 * time.
	 * 
	 * @param yo       initial values. Probably something like (xo, yo, zo, vxo,
	 *                 vyo, vzo).
	 * @param to       the initial value of the independent variable, e.g., time.
	 * @param tf       the maximum value of the independent variable.
	 * @param h        the step size
	 * @param deriv    the derivative computer (interface). This is where the
	 *                 problem specificity resides.
	 * @param stopper  if not <code>null</code> will be used to exit the integration
	 *                 early because some condition has been reached.
	 * @param listener listens for each step
	 * @param advancer takes the next step
	 * @param absError the absolute tolerance for eact of the state variables. Note
	 *                 it is a vector, the same
	 * @param hdata    if not null, should be double[3]. Upon return, hdata[0] is
	 *                 the min stepsize used, hdata[1] is the average stepsize used,
	 *                 and hdata[2] is the max stepsize used
	 * @return the number of steps used.
	 * @throw(new RungeKuttaException("Step size too small in Runge Kutta driver"
	 *            ));
	 */
	private int driverToTf(double yo[], double to, double tf, double h, IDerivative deriv, IStopper stopper,
			IRkListener listener, IAdvance advancer, double absError[], double hdata[]) throws RungeKuttaException {

		// going in the normal direction?
		boolean normalDir = (tf > to);

		// if our advancer does not compute error we can't use adaptive stepsize
		if (!advancer.computesError()) {
			return 0;
		}

		// capture stepsize data?
		if (hdata != null) {
			hdata[0] = h;
			hdata[1] = h;
			hdata[2] = h;
		}

		// the dimensionality of the problem
		int nDim = yo.length;

		// yt is the current value of the state vector,
		double yt[] = new double[nDim];
		double yt2[] = new double[nDim];
		double dydt[] = new double[nDim];

		// do we compute error?
		double error[] = new double[nDim];

		double t = to;
		for (int i = 0; i < nDim; i++) {
			yt[i] = yo[i];
		}

		int nstep = 0;
		boolean keepGoing = true;

		while (keepGoing) {
			// use derivs at previous t
			// System.out.println("h = " + h);
			deriv.derivative(t, yt, dydt);
			// System.out.println("curr y: [" + yt[0] + ", " + yt[1] + "]");

			// we might be going backwards
			double newt = (normalDir ? t + h : t - h);

			int oldSign = ((tf - t) < 0) ? -1 : 1;
			int newSign = ((tf - newt) < 0) ? -1 : 1;

			if (oldSign != newSign) { // crossed tf
				h = Math.abs(tf - t); // h always positive
				keepGoing = false;
			}

			if (normalDir) {
				advancer.advance(t, yt, dydt, h, deriv, yt2, error);
			} else {
				advancer.advance(t, yt, dydt, -h, deriv, yt2, error);
			}

			boolean decreaseStep = false;
			if (keepGoing) {
				for (int i = 0; i < nDim; i++) {
					decreaseStep = error[i] > absError[i];
					if (decreaseStep) {
						break;
					}
				}
			}

			if (decreaseStep) {
				h = h / 2;
				if (h < _minStepSize) {
					throw (new RungeKuttaException("Step size too small in Runge Kutta driver (A)" + "\nzo = " + to
							+ "  zf = " + tf + "  h = " + h));
				}
			} else { // accepted this step

				if (hdata != null) {
					hdata[0] = Math.min(hdata[0], h);
					hdata[1] += h;
					hdata[2] = Math.max(hdata[2], h);
				}

				for (int i = 0; i < nDim; i++) {
					yt[i] = yt2[i];
				}

				if (normalDir) {
					t += h;
				} else {
					t -= h;
				}

				// System.out.println("z = " + t);
				nstep++;

				// someone listening?
				if (listener != null) {
					listener.nextStep(t, yt, h);
				}

				// premature termination? Skip if stopper is null.
				if (stopper != null) {
					stopper.setFinalT(t);
					if (stopper.stopIntegration(t, yt)) {
						if ((hdata != null) && (nstep > 0)) {
							hdata[1] = hdata[1] / nstep;
						}
						return nstep; // actual number of steps taken
					}
				}
				// System.err.println("HEY MAN (B) h = " + h);
				h *= HGROWTH;
				h = Math.min(h, _maxStepSize);

			} // accepted this step max error < tolerance
		} // while (keepgoing)

		// System.err.println("EXCEEDED MAX PATH: pl = " + t + " MAX: " + tf);

		if ((hdata != null) && (nstep > 0)) {
			hdata[1] = hdata[1] / nstep;
		}
		return nstep;
	}

	private double[] getWorkArrayFromCache() {
		double array[];
		if (_workArrayCache.isEmpty()) {
			array = new double[MAXDIM];
		} else {
			array = _workArrayCache.pop();
		}
		return array;
	}

	// A uniform step size advancer
	class UniformAdvance implements IAdvance {

		@Override
		public void advance(double t, double[] y, double[] dydt, double h, IDerivative deriv, double[] yout,
				double[] error) {
			int nDim = y.length;

			// note that dydt (input) is k1
			double k1[] = dydt; // the current dreivatives
			// we need some arrays from the pool

			double k2[] = getWorkArrayFromCache();
			double k3[] = getWorkArrayFromCache();
			double k4[] = getWorkArrayFromCache();
			double ytemp[] = getWorkArrayFromCache();

			double hh = h * 0.5; // half step
			double h6 = h / 6.0;

			// advance t to mid point
			double tmid = t + hh;

			// first step: initial derivs to midpoint
			// after this,
			for (int i = 0; i < nDim; i++) {
				ytemp[i] = y[i] + hh * k1[i];
			}
			deriv.derivative(tmid, ytemp, k2);

			// second step (like 1st, but use midpoint just computed derivatives
			// dyt)
			for (int i = 0; i < nDim; i++) {
				ytemp[i] = y[i] + hh * k2[i];
			}
			deriv.derivative(tmid, ytemp, k3);

			// third (full) step
			for (int i = 0; i < nDim; i++) {
				ytemp[i] = y[i] + h * k3[i];
			}
			deriv.derivative(t + h, ytemp, k4);

			for (int i = 0; i < nDim; i++) {
				yout[i] = y[i] + h6 * (k1[i] + +2.0 * k2[i] + 2 * k3[i] + k4[i]);
			}

			// return the work arrays to the cache
			// note k1 is NOT a work array
			_workArrayCache.push(k2);
			_workArrayCache.push(k3);
			_workArrayCache.push(k4);
			_workArrayCache.push(ytemp);
		}

		@Override
		public boolean computesError() {
			return false;
		}
	}

	// simple half stepper for adaptive
	class HalfStepAdvance implements IAdvance {

		private UniformAdvance uniAdvance;

		public HalfStepAdvance() {
			// get a uniform advancer
			uniAdvance = new UniformAdvance();
		}

		@Override
		public void advance(double t, double[] y, double[] dydt, double h, IDerivative deriv, double[] yout,
				double[] error) {

			// System.err.println("HALF STEP ADVANCE");
			// advance the full step
			int ndim = y.length;
			double yfull[] = new double[ndim];
			uniAdvance.advance(t, y, dydt, h, deriv, yfull, null);

			// advance two half steps
			// double y1[] = new double[ndim];
			double h2 = h / 2;
			double tmid = t + h2;
			// double newdydt[] = new double[ndim];
			uniAdvance.advance(t, y, dydt, h2, deriv, yout, null);
			deriv.derivative(tmid, yout, dydt);
			uniAdvance.advance(tmid, yout, dydt, h2, deriv, yout, null);

			// compute absolute errors
			for (int i = 0; i < ndim; i++) {
				error[i] = Math.abs(yfull[i] - yout[i]);

//				if (error[i] > 1.0e-10) {
//				error[i] /= Math.max(Math.abs(yfull[i]),  Math.abs(yout[i]));
//				}
			}
		}

		@Override
		public boolean computesError() {
			return true;
		}

	}

//	// a Butcher Tableau advancer
//	class ButcherTableauAdvance implements IAdvance {
//
//		private ButcherTableau tableau;
//
//		public ButcherTableauAdvance(ButcherTableau tableau) {
//			this.tableau = tableau;
//		}
//
//		@Override
//		public void advance(double t,
//				double[] y,
//				double[] dydt,
//				double h,
//				IDerivative deriv,
//				double[] yout,
//				double[] error) {
//
//			// System.err.println("TABLEAU ADVANCE");
//			int nDim = y.length;
//			int numStage = tableau.getS();
//
//			double ytemp[] = getWorkArrayFromCache();
//			double k[][] = new double[numStage + 1][];
//			k[0] = null; // not used
//
//			// k1 is just h*dydt
//			k[1] = getWorkArrayFromCache();
//			for (int i = 0; i < nDim; i++) {
//				k[1][i] = h * dydt[i];
//			}
//
//			// fill the numStage k vectors
//			for (int s = 2; s <= numStage; s++) {
//				k[s] = getWorkArrayFromCache();
//
//				double ts = t + tableau.c(s);
//				for (int i = 0; i < nDim; i++) {
//					ytemp[i] = y[i];
//					for (int ss = 1; ss < s; ss++) {
//						ytemp[i] += tableau.a(s, ss) * k[ss][i];
//					}
//				}
//				deriv.derivative(ts, ytemp, k[s]);
//				for (int i = 0; i < nDim; i++) {
//					k[s][i] *= h;
//				}
//			}
//
//			for (int i = 0; i < nDim; i++) {
//				double sum = 0.0;
//				for (int s = 1; s <= numStage; s++) {
//					sum += tableau.b(s) * k[s][i];
//				}
//				yout[i] = y[i] + sum;
//			}
//
//			// compute error?
//
//			if (tableau.isAugmented() && (error != null)) {
//
//				// absolute error
//				for (int i = 0; i < nDim; i++) {
//					error[i] = 0.0;
//					// error diff 4th and 5th order
//					for (int s = 1; s <= numStage; s++) {
//						error[i] += tableau.bdiff(s) * k[s][i]; // abs error
//					}
//				}
//
//				// relative error
//				// for (int i = 0; i < nDim; i++) {
//				// double sum = 0.0;
//				// for (int s = 1; s <= numStage; s++) {
//				// sum += tableau.bstar(s)*k[s][i];
//				// }
//				// double ystar = y[i] + sum;
//				// error[i] = relativeDiff(yout[i], ystar);
//				// }
//
//				// for (int i = 0; i < nDim; i++) {
//				// System.out.print(String.format("[%-12.5e] ", error[i]));
//				// }
//				// System.out.println();
//
//			}
//
//			// //return the work arrays
//			_workArrayCache.push(ytemp);
//			for (int s = 1; s <= numStage; s++) {
//				_workArrayCache.push((k[s]));
//			}
//		}
//
//		@Override
//		public boolean computesError() {
//			return tableau.isAugmented();
//		}
//
//	}

	/**
	 * Set the maximum step size
	 * 
	 * @param maxSS the maximum stepsize is whatever units you are using
	 */
	public void setMaxStepSize(double maxSS) {
		_maxStepSize = maxSS;
	}

	/**
	 * Set the minimum step size
	 * 
	 * @param maxSS the minimum stepsize is whatever units you are using
	 */
	public void setMinStepSize(double minSS) {
		_minStepSize = minSS;
	}

	/**
	 * Get the maximum step size
	 * 
	 * @return the maximum stepsize is whatever units you are using
	 */
	public double getMaxStepSize() {
		return _maxStepSize;
	}

	/**
	 * Get the minimum step size
	 * 
	 * @return the minimum stepsize is whatever units you are using
	 */
	public double getMinStepSize() {
		return _minStepSize;
	}

}
