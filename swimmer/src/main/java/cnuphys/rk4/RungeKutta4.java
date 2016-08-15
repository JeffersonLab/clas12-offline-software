package cnuphys.rk4;

import java.util.List;

/**
 * Static methods for Runge-Kutta 4 integration, including a constant stepsize
 * method and an adaptive stepsize method.
 * 
 * @author heddle
 * 
 */
public class RungeKutta4 {

	// for adaptive stepsize, this is how much h will grow
	private static final double HGROWTH = 1.5;

	private static double MINSTEPSIZE = 1.0e-8;
	private static double MAXSTEPSIZE = 0.2;

	// the max dimension we'll use is probably 6, for state vectors
	// [x,y,z,vx,vy,vz].
	private static int MAXDIM = 10; // we'll know if this fails!

	private double[] getWorkArray() {
		return new double[MAXDIM];
	}

	/**
	 * Create a RungeKutta4 object that can be used for integration
	 */
	public RungeKutta4() {
	}

	/**
	 * Driver that uses the RungeKutta advance with a uniform step size. (i.e.,
	 * this does NOT use an adaptive step size.)
	 * 
	 * This version stores each step into the arrays t[] and y[][]. An
	 * alternative does not store the results but instead uses an IRk4Listener
	 * to notify the listener that the next step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param y
	 *            will be filled with results. The first index is small-- the
	 *            dimensionality of the problem-- i.e., often it is 6 for (xo,
	 *            yo, zo, vxo, vyo, vzo). The second dimension is for storing
	 *            results and determining stepsize. If it is 1000, we will have
	 *            a thousand steps and the stepsize will be (tf-to)/1000
	 *            (actually 999).
	 * @param t
	 *            will filled with the locations of t--should have the exact
	 *            same large dimension as the second index of y--i.e., something
	 *            like 1000.
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @return the number of steps used--may be less than the space provided if
	 *         the integration ended early as a result of an exit condition.
	 */
	public int uniformStep(double yo[], double to, double tf,
			final double y[][], final double t[], IDerivative deriv,
			IStopper stopper) {
		int nstep = t.length; // the number of steps to store

		// the dimensionality of the problem, e.g. six if (x, y, z, vx, vy, vz)
		final int nDim = yo.length;

		// uniform step size
		double h = (tf - to) / (nstep - 1);

		// put starting step in
		t[0] = to;
		for (int i = 0; i < nDim; i++) {
			y[i][0] = yo[i];
		}

		IRk4Listener listener = new IRk4Listener() {

			int step = 1;

			@Override
			public void nextStep(double tNext, double yNext[], double h) {
				t[step] = tNext;
				for (int i = 0; i < nDim; i++) {
					// store results for this step
					y[i][step] = yNext[i];
				}
				step++;
			}

		};

		return uniformStep(yo, to, tf, h, deriv, stopper, listener);

	}

	/**
	 * Integrator that uses the standard RK4 advance with a uniform step size.
	 * (i.e., this does NOT use an adaptive step size.)
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next
	 * step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @param listener
	 *            listens for each step
	 * @return the number of steps used.
	 */
	public int uniformStep(double yo[], double to, double tf, double h,
			IDerivative deriv, IStopper stopper, IRk4Listener listener) {

		UniformAdvance advancer = new UniformAdvance();
		return driver(yo, to, tf, h, deriv, stopper, listener, advancer);
	}

	/**
	 * Integrator that uses the RungeKutta advance with a Butcher Tableau and
	 * constant stepsize. (i.e., this does NOT use an adaptive step size.)
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next
	 * step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @param listener
	 *            listens for each step
	 * @param tableau
	 *            the Butcher Tableau
	 * @return the number of steps used.
	 */
	public int uniformStep(double yo[], double to, double tf, double h,
			IDerivative deriv, IStopper stopper, IRk4Listener listener,
			ButcherTableau tableau) {

		ButcherTableauAdvance advancer = new ButcherTableauAdvance(tableau);
		return driver(yo, to, tf, h, deriv, stopper, listener, advancer);
	}

	/**
	 * Integrator that uses the RungeKutta advance with a Butcher Tableau and
	 * adaptive stepsize and a tolerance vector.
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next
	 * step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param h
	 *            the starting steps size
	 * @param t
	 *            a list of the values of t at each step
	 * @param y
	 *            a list of the values of the state vector at each step
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *            [x,y,z,vx,vy,vz].
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used, hdata[1] is the average stepsize used, and
	 *            hdata[2] is the max stepsize used
	 * @return the number of steps used.
	 * @throws RungeKuttaException
	 */
	public int adaptiveStepToTf(double yo[], double to, double tf, double h,
			final List<Double> t, final List<double[]> y, IDerivative deriv,
			IStopper stopper, double relTolerance[], double hdata[])
			throws RungeKuttaException {

		// put starting step in
		t.add(to);
		y.add(copy(yo));

		IRk4Listener listener = new IRk4Listener() {

			@Override
			public void nextStep(double tNext, double yNext[], double h) {
				// System.err.println("plen: " + tNext + "[" + yNext[0] + ","+
				// yNext[1] + ", " + yNext[2] + "] h = " + h);
				t.add(tNext);
				y.add(copy(yNext));

				// System.err.println("next Step:   " + tNext + "  [" + yNext[0]
				// + ", " + yNext[1] + "]");
			}

		};
		return adaptiveStepToTf(yo, to, tf, h, deriv, stopper, listener,
				relTolerance, hdata);
	}

	/**
	 * Integrator that uses the RungeKutta advance with a Butcher Tableau and
	 * adaptive stepsize and a tolerance vector.
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next
	 * step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param h
	 *            the starting steps size
	 * @param t
	 *            a list of the values of t at each step
	 * @param y
	 *            a list of the values of the state vector at each step
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @param tableau
	 *            the Butcher Tableau
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *            [x,y,z,vx,vy,vz].
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used, hdata[1] is the average stepsize used, and
	 *            hdata[2] is the max stepsize used
	 * @return the number of steps used.
	 * @throws RungeKuttaException
	 */
	public int adaptiveStep(double yo[], double to, double tf, double h,
			final List<Double> t, final List<double[]> y, IDerivative deriv,
			IStopper stopper, ButcherTableau tableau, double relTolerance[],
			double hdata[]) throws RungeKuttaException {

		// put starting step in
		t.add(to);
		y.add(copy(yo));

		IRk4Listener listener = new IRk4Listener() {

			@Override
			public void nextStep(double tNext, double yNext[], double h) {
				// System.err.println("plen: " + tNext + "[" + yNext[0] + ","+
				// yNext[1] + ", " + yNext[2] + "] h = " + h);
				t.add(tNext);
				y.add(copy(yNext));
			}

		};

		return adaptiveStep(yo, to, tf, h, deriv, stopper, listener, tableau,
				relTolerance, hdata);
	}

	/**
	 * Integrator that uses the RungeKutta advance with a Butcher Tableau and
	 * adaptive stepsize
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next
	 * step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param h
	 *            the starting steps size
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @param listener
	 *            listens for each step * @param tableau the Butcher Tableau
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used, hdata[1] is the average stepsize used, and
	 *            hdata[2] is the max stepsize used
	 * @return the number of steps used.
	 * @throws RungeKuttaException
	 */
	public int adaptiveStepToTf(double yo[], double to, double tf, double h,
			IDerivative deriv, IStopper stopper, IRk4Listener listener,
			double relTolerance[], double hdata[]) throws RungeKuttaException {

		// use a simple half-step advance
		IAdvance advancer = new HalfStepAdvance();

		return driverToTf(yo, to, tf, h, deriv, stopper, listener, advancer,
				relTolerance, hdata);
	}

	/**
	 * Integrator that uses the RungeKutta advance with a Butcher Tableau and
	 * adaptive stepsize
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next
	 * step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param h
	 *            the starting steps size
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @param listener
	 *            listens for each step
	 * @param tableau
	 *            the Butcher Tableau
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used, hdata[1] is the average stepsize used, and
	 *            hdata[2] is the max stepsize used
	 * @return the number of steps used.
	 * @throws RungeKuttaException
	 */
	public int adaptiveStep(double yo[], double to, double tf, double h,
			IDerivative deriv, IStopper stopper, IRk4Listener listener,
			ButcherTableau tableau, double relTolerance[], double hdata[])
			throws RungeKuttaException {

		// ButcherTableauAdvance advancer = new ButcherTableauAdvance(tableau);
		// use a simple half-step advance
		IAdvance advancer = new HalfStepAdvance();
		return driver(yo, to, tf, h, deriv, stopper, listener, advancer,
				relTolerance, hdata);
	}

	/**
	 * Integrator that uses the RungeKutta advance with a Butcher Tableau and
	 * adaptive stepsize. This uses an desired absolute error relative to some
	 * scale (of max values of the dependent variables)
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next
	 * step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param h
	 *            the starting steps size
	 * @param t
	 *            a list of the values of t at each step
	 * @param y
	 *            a list of the values of the state vector at each step
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @param tableau
	 *            the Butcher Tableau
	 * @param eps
	 *            the required accuracy
	 * @param yscale
	 *            scale the error against this array. It can be the approximate
	 *            max value of each component of y, which gives you constant
	 *            absolute errors, or it can be null in which case y will be
	 *            used and you have constant relative error.
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used, hdata[1] is the average stepsize used, and
	 *            hdata[2] is the max stepsize used
	 * @return the number of steps used.
	 * @throws RungeKuttaException
	 */
	public int adaptiveStep(double yo[], double to, double tf, double h,
			final List<Double> t, final List<double[]> y, IDerivative deriv,
			IStopper stopper, ButcherTableau tableau, double eps,
			double yscale[], double hdata[]) throws RungeKuttaException {

		// put starting step in
		t.add(to);
		y.add(copy(yo));

		IRk4Listener listener = new IRk4Listener() {

			@Override
			public void nextStep(double tNext, double yNext[], double h) {
				t.add(tNext);
				y.add(copy(yNext));
			}

		};

		return adaptiveStep(yo, to, tf, h, deriv, stopper, listener, tableau,
				eps, yscale, hdata);
	}

	/**
	 * Integrator that uses the RungeKutta advance with a Butcher Tableau and
	 * adaptive stepsize. This uses an desired absolute error relative to some
	 * scale (of max values of the dependent variables)
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next
	 * step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param h
	 *            the starting steps size
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @param listener
	 *            listens for each step
	 * @param tableau
	 *            the Butcher Tableau
	 * @param eps
	 *            the required accuracy
	 * @param yscale
	 *            scale the error against this array. It can be the approximate
	 *            max value of each component of y, which gives you constant
	 *            absolute errors, or it can be null in which case y will be
	 *            used and you have constant relative error.
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used, hdata[1] is the average stepsize used, and
	 *            hdata[2] is the max stepsize used
	 * @return the number of steps used.
	 * @throws RungeKuttaException
	 */
	public int adaptiveStep(double yo[], double to, double tf, double h,
			IDerivative deriv, IStopper stopper, IRk4Listener listener,
			ButcherTableau tableau, double eps, double yscale[], double hdata[])
			throws RungeKuttaException {

		// ButcherTableauAdvance advancer = new ButcherTableauAdvance(tableau);
		// use a simple half-step advance
		IAdvance advancer = new HalfStepAdvance();
		return driver(yo, to, tf, h, deriv, stopper, listener, advancer, eps,
				yscale, hdata);
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
	 * Driver that uses the RungeKutta advance with a uniform step size. (I.e.,
	 * this does NOT use an adaptive step size.)
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next
	 * step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @return the number of steps used.
	 */
	private int driver(double yo[], double to, double tf, double h,
			IDerivative deriv, IStopper stopper, IRk4Listener listener,
			IAdvance advancer) {
		int nstep = (int) (1 + (tf - to) / h); // the number of steps to store

		// the dimensionality of the problem. E.., 6 if (x, y, z, vx, vy, vz)
		int nDim = yo.length;

		// yt is the current value of the state vector,
		// typically [x, y, z, vx, vy, vz] and derivative
		double yt[] = new double[nDim];
		double dydt[] = new double[nDim];

		double t = to;
		for (int i = 0; i < nDim; i++) {
			yt[i] = yo[i];
		}

		for (int k = 1; k < nstep; k++) {
			// use derivs at previous t
			deriv.derivative(t, yt, dydt);

			advancer.advance(t, yt, dydt, h, deriv, yt, null);
			t += h;

			// someone listening?
			if (listener != null) {
				listener.nextStep(t, yt, h);
			}

			// premature termination? Skip if stopper is null.
			if (stopper != null) {
				stopper.setFinalT(t);
				if (stopper.stopIntegration(t, yt)) {
					return k + 1; // actual number of steps taken
				}
			}
		}

		return nstep;
	}

	/**
	 * Driver that uses the RungeKutta advance with an adaptive step size
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next
	 * step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param h
	 *            the step size
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @param listener
	 *            listens for each step
	 * @param advancer
	 *            takes the next step
	 * @param absError
	 *            the absolute tolerance for eact of the state variables. Note
	 *            it is a vector, the same
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used, hdata[1] is the average stepsize used, and
	 *            hdata[2] is the max stepsize used
	 * @return the number of steps used.
	 * @throw(new 
	 *            RungeKuttaException("Step size too small in Runge Kutta driver"
	 *            ));
	 */
	private int driverToTf(double yo[], double to, double tf, double h,
			IDerivative deriv, IStopper stopper, IRk4Listener listener,
			IAdvance advancer, double absError[], double hdata[])
			throws RungeKuttaException {

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

		// the dimensionality of the problem. E.., 6 if (x, y, z, vx, vy, vz)
		int nDim = yo.length;

		// yt is the current value of the state vector,
		// typically [x, y, z, vx, vy, vz] and derivative
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

			double newt = t + h;

			int oldSign = ((tf - t) < 0) ? -1 : 1;
			int newSign = ((tf - newt) < 0) ? -1 : 1;

			if (oldSign != newSign) { // crossed tf
				// System.out.println("CROSSED BOUNDARY AT NSTEP = " + nstep);
				h = tf - t;
				// advancer = new UniformAdvance();
				// System.out.println("UNIF ADV FINAL STEP");
				keepGoing = false;
			}

			advancer.advance(t, yt, dydt, h, deriv, yt2, error);

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
				if (h < MINSTEPSIZE) {
					throw (new RungeKuttaException(
							"Step size too small in Runge Kutta driver (A)"));
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

				t += h;
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
				h = Math.min(h, MAXSTEPSIZE);

			} // accepted this step max error < tolerance
		} // while (keepgoing)

		// System.err.println("EXCEEDED MAX PATH: pl = " + t + " MAX: " + tf);

		if ((hdata != null) && (nstep > 0)) {
			hdata[1] = hdata[1] / nstep;
		}
		return nstep;
	}

	/**
	 * Driver that uses the RungeKutta advance with an adaptive step size
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next
	 * step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param h
	 *            the step size
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @param listener
	 *            listens for each step
	 * @param advancer
	 *            takes the next step
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used, hdata[1] is the average stepsize used, and
	 *            hdata[2] is the max stepsize used
	 * @return the number of steps used.
	 * @throw(new 
	 *            RungeKuttaException("Step size too small in Runge Kutta driver"
	 *            ));
	 */
	private int driver(double yo[], double to, double tf, double h,
			IDerivative deriv, IStopper stopper, IRk4Listener listener,
			IAdvance advancer, double relTolerance[], double hdata[])
			throws RungeKuttaException {

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

		// the dimensionality of the problem. E.., 6 if (x, y, z, vx, vy, vz)
		int nDim = yo.length;

		// yt is the current value of the state vector,
		// typically [x, y, z, vx, vy, vz] and derivative
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
		while (t < tf) {
			// use derivs at previous t
			deriv.derivative(t, yt, dydt);

			advancer.advance(t, yt, dydt, h, deriv, yt2, error);

			boolean decreaseStep = false;
			for (int i = 0; i < nDim; i++) {
				decreaseStep = error[i] > relTolerance[i];
				// System.err.println("error " + error[i] + "  reltol: " +
				// relTolerance[i] + "   dec: " + decreaseStep);
				if (decreaseStep) {
					break;
				}
			}

			if (decreaseStep) {
				h = h / 2;
				if (h < MINSTEPSIZE) {
					throw (new RungeKuttaException(
							"Step size too small in Runge Kutta driver (A)"));
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

				t += h;

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

				// System.err.println("HEY MAN (A) h = " + h);
				h *= HGROWTH;
				h = Math.min(h, MAXSTEPSIZE);

			} // max error < tolerance
		}

		// System.err.println("EXCEEDED MAX PATH: pl = " + t + " MAX: " + tf);

		if ((hdata != null) && (nstep > 0)) {
			hdata[1] = hdata[1] / nstep;
		}
		return nstep;
	}

	/**
	 * Driver that uses the RungeKutta advance with an adaptive step size. This
	 * uses an desired absolute error relative to some scale (of max values of
	 * the dependent variables)
	 * 
	 * This version uses an IRk4Listener to notify the listener that the next
	 * step has been advanced.
	 * 
	 * A very typical case is a 2nd order ODE converted to a 1st order where the
	 * dependent variables are x, y, z, vx, vy, vz and the independent variable
	 * is time.
	 * 
	 * @param yo
	 *            initial values. Probably something like (xo, yo, zo, vxo, vyo,
	 *            vzo).
	 * @param to
	 *            the initial value of the independent variable, e.g., time.
	 * @param tf
	 *            the maximum value of the independent variable.
	 * @param deriv
	 *            the derivative computer (interface). This is where the problem
	 *            specificity resides.
	 * @param stopper
	 *            if not <code>null</code> will be used to exit the integration
	 *            early because some condition has been reached.
	 * @param listener
	 *            listens for each step
	 * @param advancer
	 *            takes the next step
	 * @param eps
	 *            the required accuracy
	 * @param yscale
	 *            scale the error against this array. It can be the approximate
	 *            max value of each component of y, which gives you constant
	 *            absolute errors, or it can be null in which case y will be
	 *            used and you have constant relative error.
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used, hdata[1] is the average stepsize used, and
	 *            hdata[2] is the max stepsize used
	 * @return the number of steps used.
	 * @throws RungeKuttaException
	 */
	private int driver(double yo[], double to, double tf, double h,
			IDerivative deriv, IStopper stopper, IRk4Listener listener,
			IAdvance advancer, double eps, double yscale[], double hdata[])
			throws RungeKuttaException {

		// if our advancer does not compute error we can't use adaptive stepsize
		if (!advancer.computesError()) {
			return 0;
		}

		// if yscale is null scale off of the current y.
		// this changes from absolute error to relative error
		if (yscale == null) {
			yscale = yo;
		}

		// capture stepsize data?
		if (hdata != null) {
			hdata[0] = h;
			hdata[1] = 0.;
			hdata[2] = h;
		}

		// the dimensionality of the problem. E.., 6 if (x, y, z, vx, vy, vz)
		int nDim = yo.length;

		// yt is the current value of the state vector,
		// typically [x, y, z, vx, vy, vz] and derivative
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
		while (t < tf) {
			// use derivs at previous t
			deriv.derivative(t, yt, dydt);

			advancer.advance(t, yt, dydt, h, deriv, yt2, error);

			double errMax = 0.0;
			for (int i = 0; i < nDim; i++) {
				errMax = Math.max(errMax,
						Math.abs(error[i] / Math.max(1.0e-20, yscale[i])));
			}
			errMax /= eps;

			if (errMax > 1.0) {
				double hnew = 0.9 * h * Math.pow(errMax, -0.25);
				h = Math.max(hnew, 0.1 * h); // limit reduction
				if (h < MINSTEPSIZE) {
					throw (new RungeKuttaException(
							"Step size too small in Runge Kutta driver (B)"));
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

				t += h;
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

				if (errMax > 1.89e-4) {
					h = 0.9 * h * Math.pow(errMax, 0.2);
				} else {
					h = 5.0 * h; // limit growth
				}

			} // max error < tolerance
		}

		if ((hdata != null) && (nstep > 0)) {
			hdata[1] = hdata[1] / nstep;
		}
		return nstep;
	}

	// relative difference with no danger of dividing by zero
	private double relativeDiff(double a, double b) {

		double diff = Math.abs(a - b);
		if (diff < 1.0e-20) {
			return 0.;
		}

		double aabs = Math.abs(a);
		double babs = Math.abs(b);

		double denom = aabs;
		if (denom < 1.0e-20) {
			denom = Math.max(aabs, babs);
		}

		double err = diff / denom;

		return err;
	}

	// A uniform step size advancer
	class UniformAdvance implements IAdvance {

		@Override
		public void advance(double t, double[] y, double[] dydt, double h,
				IDerivative deriv, double[] yout, double[] error) {
			int nDim = y.length;

			// note that dydt (input) is k1
			double k1[] = dydt; // the current dreivatives
			// we need some arrays from the pool
			double k2[] = getWorkArray();
			double k3[] = getWorkArray();
			double k4[] = getWorkArray();
			double ytemp[] = getWorkArray();

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
				yout[i] = y[i] + h6
						* (k1[i] + +2.0 * k2[i] + 2 * k3[i] + k4[i]);
			}

			// return the work arrays to the pool
			// note k1 is NOT a work array
			// arrayPool.add(k2);
			// arrayPool.add(k3);
			// arrayPool.add(k4);
			// arrayPool.add(ytemp);
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
		public void advance(double t, double[] y, double[] dydt, double h,
				IDerivative deriv, double[] yout, double[] error) {

			// System.err.println("HALF STEP ADVANCE");
			// advance the full step
			int ndim = yout.length;
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
			}
		}

		@Override
		public boolean computesError() {
			return true;
		}

	}

	// a Butcher Tableau advancer
	class ButcherTableauAdvance implements IAdvance {

		private ButcherTableau tableau;

		public ButcherTableauAdvance(ButcherTableau tableau) {
			this.tableau = tableau;
		}

		@Override
		public void advance(double t, double[] y, double[] dydt, double h,
				IDerivative deriv, double[] yout, double[] error) {

			// System.err.println("TABLEAU ADVANCE");
			int nDim = y.length;
			int numStage = tableau.getS();

			double ytemp[] = getWorkArray();
			double k[][] = new double[numStage + 1][];
			k[0] = null; // not used

			// k1 is just h*dydt
			k[1] = getWorkArray();
			for (int i = 0; i < nDim; i++) {
				k[1][i] = h * dydt[i];
			}

			// fill the numStage k vectors
			for (int s = 2; s <= numStage; s++) {
				k[s] = getWorkArray();

				double ts = t + tableau.c(s);
				for (int i = 0; i < nDim; i++) {
					ytemp[i] = y[i];
					for (int ss = 1; ss < s; ss++) {
						ytemp[i] += tableau.a(s, ss) * k[ss][i];
					}
				}
				deriv.derivative(ts, ytemp, k[s]);
				for (int i = 0; i < nDim; i++) {
					k[s][i] *= h;
				}
			}

			for (int i = 0; i < nDim; i++) {
				double sum = 0.0;
				for (int s = 1; s <= numStage; s++) {
					sum += tableau.b(s) * k[s][i];
				}
				yout[i] = y[i] + sum;
			}

			// compute error?

			if (tableau.isAugmented() && (error != null)) {

				// absolute error
				for (int i = 0; i < nDim; i++) {
					error[i] = 0.0;
					// error diff 4th and 5th order
					for (int s = 1; s <= numStage; s++) {
						error[i] += tableau.bdiff(s) * k[s][i]; // abs error
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
				// System.out.print(String.format("[%-12.5e]   ", error[i]));
				// }
				// System.out.println();

			}

			// //return the work arrays
			// arrayPool.add(ytemp);
			// for (int s = 1; s <= numStage; s++) {
			// arrayPool.add(k[s]);
			// }
		}

		@Override
		public boolean computesError() {
			return tableau.isAugmented();
		}

	}

	/**
	 * Set the maximum step size
	 * 
	 * @param maxSS
	 *            the maximum stepsize is whatever units you are using
	 */
	public static void setMaxStepSize(double maxSS) {
		MAXSTEPSIZE = maxSS;
	}

	/**
	 * Set the minimum step size
	 * 
	 * @param maxSS
	 *            the minimum stepsize is whatever units you are using
	 */
	public static void setMinStepSize(double minSS) {
		MINSTEPSIZE = minSS;
	}

	/**
	 * Get the maximum step size
	 * 
	 * @return the maximum stepsize is whatever units you are using
	 */
	public static double getMaxStepSize() {
		return MAXSTEPSIZE;
	}

}
