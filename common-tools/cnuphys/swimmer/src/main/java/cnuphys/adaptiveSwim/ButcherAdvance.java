package cnuphys.adaptiveSwim;

import cnuphys.rk4.ButcherTableau;
import cnuphys.rk4.IDerivative;

public class ButcherAdvance implements IAdaptiveAdvance {

	//a safety fudge factor
	private static final double _safety = 0.9;

	//power used when the step should grow
	private static final double _pgrow = -0.20;

	//power used when the step should shrink
	private static final double _pshrink = -0.25;

	//for error control
	private static final double _errControl = 1.89e-4;

	//to make a correction that gives us 5th order accuracy
	private static final double _correctFifth = 1. / 15;

	//a tiny number
	private static final double _tiny = 1.0e-14;


	private ButcherTableau _tableau;

	private double _uscale[];

	private double _error[];

	/**
	 * Create a butcher advancer
	 * @param tableau the Butcher tableau
	 */
	public ButcherAdvance(ButcherTableau tableau) {
		_tableau = tableau;
		_uscale = new double[AdaptiveSwimmer.DIM];
		_error = new double[AdaptiveSwimmer.DIM];
	}

	/**
	 * Advance the solution by one step of the independent variable.
	 *
	 * @param s     the current value of the independent variable
	 * @param u     the current values of the state vector
	 * @param du    the current values of the derivative (before advance)
	 * @param h     the current step size
	 * @param deriv the function (interface) that can compute the derivatives. That
	 *              is where the problem specificity resides .
	 * @param uf    where the state vector at the next step will be stored (output)
	 * @param eps   the overall tolerance (e.g., 1.0e-5)
	 * @param  stepResult container for the new value of the independent variable and the new step size
	 */
	@Override
	public void advance(double s, double[] u, double[] du, double h, IDerivative deriv, double[] uf, double eps,
			AdaptiveStepResult stepResult) {

		boolean done = false;

		// almost relative error, but with safety when values of u are small
		for (int i = 0; i < AdaptiveSwimmer.DIM; i++) {
			_uscale[i] = Math.abs(u[i]) + Math.abs(h * du[i]) + _tiny;
		}

		while (!done) {

			AdaptiveSwimUtilities.singleButcherStep(eps, u, du, h, deriv, uf, _error, _tableau);

			double errMax = 0;
			for (int i = 0; i < AdaptiveSwimmer.DIM; i++) {
				errMax = Math.max(errMax, Math.abs(_error[i] / _uscale[i]));
			}

			// scale based on tolerance in eps
			errMax = errMax / eps;

			double hnew;
			if (errMax > 1) {
				// get smaller h, then try again since done = false

				double shrinkFact = _safety * Math.pow(errMax, _pshrink);

				// no more than a factor of 4
				shrinkFact = Math.max(shrinkFact, 0.25);
				h = h * shrinkFact;
			} else { // can grow
				if (errMax > _errControl) {
					double growFact = _safety * Math.pow(errMax, _pgrow);
					hnew = h * growFact;
				} else {
					hnew = 2 * h;
				}

				stepResult.setHNew(hnew);


//				System.out.println(String.format("    ADV S old = %15.12f", stepResult.getSNew()));
				stepResult.setSNew(s + h);
//				System.out.println(String.format("    ADV S new = %15.12f", stepResult.getSNew()));
				done = true;
			}

		} // !done

		// mop up 5th order truncation error
		//so result is actually 5th order
		for (int i = 0; i < AdaptiveSwimmer.DIM; i++) {
			uf[i] = uf[i] + _error[i] * _correctFifth;
		}
	} // end advance

}
