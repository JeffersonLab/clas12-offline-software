package cnuphys.adaptiveSwim;

public interface IAdaptiveStopper {

	/**
	 * Given the current state of the integration, should we stop? This allows the
	 * integration to stop, for example, if some distance from the origin has been
	 * exceeded or if the independent variable passes some threshold. It won't be
	 * precise, because the check may not happen on every step, but it should be
	 * close.
	 * 
	 * @param sNew the new value of the independent variable (typically pathlength)
	 * @param uNew the new state vector (typically [x, y, z, vx, vy, vz])
	 * @return <code>true</code> if we should stop now.
	 */
	public boolean stopIntegration(double snew, double unew[]);


	/**
	 * Get the current independent variable
	 * @return the current independent variable
	 */
	public double getS();
	
	/**
	 * Get the current value of the state vector
	 * @return the current value of the state vector
	 */
	public double[] getU();
	
	/**
	 * Get the max or final value of the independent variable
	 * @return the max or final value of the independent variable
	 */
	public double getSmax();
	
	/**
	 * Convenience method to get the remaining range,
	 * i.e. sMax - s
	 * @return the remaining range in meters
	 */
	public double getRemainingRange();
	
	/**
	 * Get the max step size. This can vary with conditions, primarily
	 * with the proximity to a target 
	 * @return the current max step in meters
	 */
	public double getMaxStepSize();
}
