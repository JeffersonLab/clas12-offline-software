package cnuphys.rk4;

public interface IAdvance {

	/**
	 * Advance the solution by one step of the independent variable.
	 * 
	 * @param t     the value of the independent variable (e.g., t) (input).
	 * @param y     the values of the state vector (usually [x,y,z,dx/dt, dy/dt,
	 *              dz/dt]) at t (input).
	 * @param dydt  values of the derivative at the current t (before advance)
	 *              (input).
	 * @param h     the current step size (input)
	 * @param deriv the function (interface) that can compute the derivatives. That
	 *              is where the problem specificity resides (input).
	 * @param yout  where the state vector at the next step will be stored (output)
	 * @param error the error vector which is filled in if the advancer knows how to
	 *              compute errors
	 */
	public void advance(double t, double y[], double dydt[], double h, IDerivative deriv, double yout[],
			double error[]);

	/**
	 * Reports whether this advancer knows how to compute an error array
	 * 
	 * @return <code>true</code> if this advancer knows how to compute errors
	 */
	public boolean computesError();
}
