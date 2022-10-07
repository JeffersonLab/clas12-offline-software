package cnuphys.adaptiveSwim;

import cnuphys.adaptiveSwim.geometry.Plane;


public class PlaneSignChangeStopper extends ASignChangeStopper {


	/**
	 * Sign change  stopper  (does check max path length)
	 * @param sfMax             the maximum value of the path length in meters
	 * @param targetPlane       the target plane
	 * @param result holds the results, its u statevector should have been initialized
	 * to the starting vector
	 */
	public PlaneSignChangeStopper(final double sMax, Plane targetPlane, AdaptiveSwimResult result) {
		super(sMax, targetPlane, result);
	}


	@Override
	public int sign(double snew, double[] unew) {
		Plane _targetPlane = (Plane)_target;
		double signedDistance = _targetPlane.signedDistance(unew[0], unew[1], unew[2]);
		return (signedDistance < 0) ? -1 : 1;
	}


}
