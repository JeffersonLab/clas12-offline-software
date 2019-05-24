package cnuphys.rk4;

public interface IStopper {

	/**
	 * Given the current state of the integration, should we stop? This allows the
	 * integration to stop, for example, if some distance from the origin has been
	 * exceeded or if the independent variable passes some threshold. It won't be
	 * precise, because the check may not happen on every step, but it should be
	 * close.
	 * 
	 * @param t the current value of the independent variable (typically pathlength)
	 * @param y the current state vector (typically [x, y, z, vx, vy, vz])
	 * @return <code>true</code> if we should stop now.
	 */
	public boolean stopIntegration(double t, double y[]);

	/**
	 * Get the final independent variable (typically path length in meters)
	 * 
	 * @return the final independent variable (typically path length in meters)
	 */
	public double getFinalT();

	/**
	 * Set the final independent variable (typically path length in meters)
	 * 
	 * @param finalT the final independent variable (typically path length in
	 *               meters)
	 */
	public void setFinalT(double finalT);

	/**
	 * Generally this is the same as stop integration. So most will just return
	 * stopIntegration(). But sometimes stop just means we reset and integrate more.
	 * For example, with a fixed Z integrator we "stop" when we cross the z boundary
	 * however we are not done unless we are within tolerance. If we are within
	 * tolerance (on either side) we are really done!
	 * 
	 * @param t the current value of the independent variable (typically pathlength)
	 * @param y the current state vector (typically [x, y, z, vx, vy, vz])
	 * @return <code>true</code> if we should stop now.
	 */
	default boolean terminateIntegration(double t, double y[]) {
		return stopIntegration(t, y);
	}
}
