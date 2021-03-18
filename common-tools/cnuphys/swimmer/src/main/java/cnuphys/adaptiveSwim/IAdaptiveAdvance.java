package cnuphys.adaptiveSwim;

import cnuphys.rk4.IDerivative;

public interface IAdaptiveAdvance {

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
	 * param  result container for the new value of the independent variable and the new step size
	 */
	public void advance(double s, double u[], double du[], double h, IDerivative deriv, double uf[],
			double eps, AdaptiveStepResult result);
}
