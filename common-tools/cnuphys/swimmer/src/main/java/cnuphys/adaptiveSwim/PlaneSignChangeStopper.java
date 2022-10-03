package cnuphys.adaptiveSwim;

import cnuphys.adaptiveSwim.geometry.Plane;


public class PlaneSignChangeStopper extends AAdaptiveStopper {

	private Plane _targetPlane;
	protected int _startSign;


	/**
	 * Line  stopper  (does check max path length)
	 * @param sfMax             the maximum value of the path length in meters
	 * @param targetPlane       the target plane
	 * @param accuracy          the accuracy in meters
	 * @param result holds the results, its u statevector should have been initialized
	 * to the starting vector
	 */
	public PlaneSignChangeStopper(final double sMax, Plane targetPlane, AdaptiveSwimResult result) {
		super(sMax, 0, result);
		_targetPlane = targetPlane;

	}
	
	/**
	 * For doing things like setting the initial sign and distance
	 */
	@Override
	public void initialize() {
		_startSign = sign(_result.getS(), _result.getU());
	}


	@Override
	public boolean stopIntegration(double snew, double[] unew) {

		//stop and accept new data. We exceeded smax and didn't hit the target
		if (snew > _sMax) {
			accept(snew, unew);
			_result.setStatus(AdaptiveSwimmer.SWIM_TARGET_MISSED);
			return true;
		}


 		accept(snew, unew);
		int newSign = sign(snew, unew);

		if ((newSign != _startSign) || (snew > _sMax)) {
			return true;
		}
		
		return false;
	}


	public int sign(double snew, double[] unew) {
		double signedDistance = _targetPlane.signedDistance(unew[0], unew[1], unew[2]);
		return (signedDistance < 0) ? -1 : 1;
	}


}
