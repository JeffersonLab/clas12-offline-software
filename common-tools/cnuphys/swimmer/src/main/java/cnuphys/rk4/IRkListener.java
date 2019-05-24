package cnuphys.rk4;

public interface IRkListener {

	/**
	 * The integration has advanced one step
	 * 
	 * @param newT the new value of the independent variable (e.g., time)
	 * @param newY the new value of the dependent variable, e.g. often a vector with
	 *             six elements, e.g.: x, y, z, dx/dt, dy/dt, dz/dt
	 * @param h    the stepsize used for this advance
	 */
	public void nextStep(double newT, double newY[], double h);
}
