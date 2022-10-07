package cnuphys.adaptiveSwim;

import cnuphys.adaptiveSwim.geometry.AGeometric;

public abstract class ASignChangeStopper extends AAdaptiveStopper {
	
	protected AGeometric _target;
	protected int _startSign;  //call start side "left" arbitrarily
	
	protected AdaptiveSwimIntersection _intersection;


	/**
	 * Sign change  stopper  (does check max path length)
	 * @param sfMax             the maximum value of the path length in meters
	 * @param target            the target geometric
	 * @param result holds the results, its u statevector should have been initialized
	 * to the starting vector
	 */
	public ASignChangeStopper(final double sMax, AGeometric target, AdaptiveSwimResult result) {
		super(sMax, 0, result);
		_target = target;

		_intersection = result.getIntersection();
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


 		//if sign changed we have success
		int newSign = sign(snew, unew);
		if (newSign != _startSign) {
			//point is on "right" and will be last point accepted
			
			if (_intersection != null) {
				_intersection.setRight(unew, snew);
			}

			accept(snew, unew);
			_result.setStatus(AdaptiveSwimmer.SWIM_SUCCESS);
			return true;
		}

		//stop and accept new data. We exceeded smax and didn't hit the target
		if (snew > _sMax) {
			_result.setStatus(AdaptiveSwimmer.SWIM_TARGET_MISSED);
			return true;
		}
		
		//point is on "left"
		
		if (_intersection != null) {
			_intersection.setLeft(unew, snew);
		}
		accept(snew, unew);
		return false;
	}


	/** 
	 * Compute the sign based on the actual geometric object
	 * @param snew the new value of the pathlength
	 * @param unew the new state vector
	 * @return the "which side am I on" sign
	 */
	public abstract int sign(double snew, double[] unew);

}
