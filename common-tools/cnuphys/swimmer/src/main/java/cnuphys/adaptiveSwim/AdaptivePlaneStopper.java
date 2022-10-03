package cnuphys.adaptiveSwim;

import cnuphys.adaptiveSwim.geometry.Plane;

/**
 * Stopper for swimming to a plane
 * @author heddle
 *
 */
public class AdaptivePlaneStopper extends AAdaptiveStopper {

	//the plane you want to swim to
	protected Plane _targetPlane;

	//previous distance to the plane
	protected double _prevDist;
	
	//the newest distance to the plane
	protected double _newDist;


	//is the starting rho bigger or smaller than the target
	protected int _startSign;

	/**
	 * Plane  stopper  (does check max path length)
	 * @param sMax           the maximum value of the path length in meters
	 * @param targetPlane  the target plane
	 * @param accuracy     the accuracy in meters
	 * @param result holds the results, its u statevector should have been initialized
	 * to the starting vector
	 */
	public AdaptivePlaneStopper(double sMax, Plane targetPlane, double accuracy, AdaptiveSwimResult result) {
		super(sMax, accuracy, result);
		_targetPlane = targetPlane;
	}
	
	/**
	 * For doing things like setting the initial sign and distance
	 */
	@Override
	public void initialize() {
		_prevDist = signedDistance(_result.getU());
		_newDist = _prevDist;
		_startSign = sign();
	}


	/**
	 * Get the signed distance from a state vector to a point on the target plane
	 * @param u the state vector
	 * @return the signed distance from a state vector to a point on the target plane in meters
	 */
	protected double signedDistance(double u[]) {
		return _targetPlane.signedDistance(u[0], u[1], u[2]);
	}

	@Override
	public boolean stopIntegration(double snew, double[] unew) {

		_newDist = signedDistance(unew);
		
		//stop but don't accept new data. We crossed the target  boundary
		if (sign() != _startSign) {
			_result.setStatus(AdaptiveSwimmer.SWIM_CROSSED_BOUNDARY);
			_del = Math.abs(_prevDist)/2;
			return true;
		}


		// within accuracy? Accept and stop
		if (Math.abs(_newDist) < _accuracy) {
			accept(snew, unew);
			_result.setStatus(AdaptiveSwimmer.SWIM_SUCCESS);
  			return true;
		}

		//stop and accept new data. We exceeded smax and didn't hit the target
		if (snew > _sMax) {
			accept(snew, unew);
			_result.setStatus(AdaptiveSwimmer.SWIM_TARGET_MISSED);
			return true;
		}

		//accept new data and continue
		accept(snew, unew);
		_prevDist = _newDist;
		return false;
	}


	//get the sign based on the current signed distance
	protected int sign() {
		return (_newDist < 0) ? -1 : 1;
	}


}