package cnuphys.swim;

import cnuphys.rk4.IRkListener;

public class DefaultListener implements IRkListener {

	// value of independent variable (e.g., time or path length)
	private double independentVar;

	// value of the state vector
	private double[] lastStateVector;

	private int count;

	/**
	 * Create a default listener that simply tracks the total number of steps taken
	 * and stores the last step.
	 */
	public DefaultListener() {
		reset();
	}

	/**
	 * The integration has advanced one step
	 * 
	 * @param newS the new value of the independent variable (e.g., path length)
	 * @param newY the new value of the dependent variable, e.g. often a vector with
	 *             six elements (e.g., x, y, z, px/p, py/p, pz/p)
	 * @param h    the stepsize used for this advance
	 */
	@Override
	public void nextStep(double newS, double[] newY, double h) {
		independentVar = newS;

		int nDim = newY.length;

		if (lastStateVector == null) {
			lastStateVector = new double[nDim];
		}

		System.arraycopy(newY, 0, lastStateVector, 0, nDim);

		// for (int i = 0; i < nDim; i++) {
		// lastStateVector[i] = newY[i];
		// }

		count++;
	}

	/**
	 * Call reset if you are going to use the same object again.
	 */
	public void reset() {
		independentVar = Double.NaN;
		lastStateVector = null;
		count = 0;
	}

	/**
	 * Get the independent variable (e.g., path length) for the last step.
	 * 
	 * @return the independent variable (e.g., path length) at the last step
	 */
	public double getIndependentVariable() {
		return independentVar;
	}

	/**
	 * Get the last state vector entries (e.g., [x,y,z, px.p, py/p, pz/p])
	 * 
	 * @return the last state vector
	 */
	public double[] getLastStateVector() {
		return lastStateVector;
	}

	/**
	 * Get the number of steps taken
	 * 
	 * @return the count, i.e., the number of steps taken
	 */
	public int getCount() {
		return count;
	}

}
