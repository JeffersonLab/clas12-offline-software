package cnuphys.adaptiveSwim;

import cnuphys.adaptiveSwim.geometry.Cylinder;

public class AdaptiveCylinderStopper extends AAdaptiveStopper {

	//the target cylinder
	private Cylinder _targetCylinder;
	
	//the newest distance to the cylinder
	private double _prevDist;

	//the newest distance to the cylinder
	private double _newDist;

	//the start sign of the distance
	private double _startSign;


	/**
	 * Cylinder  stopper  (does check max path length)
	 * @param sMax                the maximum value of the path length in meters
	 * @param targetCylinder    the target cylinder
	 * @param accuracy          the accuracy in meters
	 * @param result holds the results, its u statevector should have been initialized
	 * to the starting vector
	 */
	public AdaptiveCylinderStopper(double sMax, Cylinder targetCylinder, double accuracy, AdaptiveSwimResult result) {
		super(sMax, accuracy, result);
		_targetCylinder = targetCylinder;
	}


	/**
	 * For doing things like setting the initial sign and distance
	 */
	@Override
	public void initialize() {
		double u[] = _result.getU();
		_prevDist = _targetCylinder.signedDistance(u[0], u[1], u[2]);
		_newDist = _prevDist;

		_startSign = sign();		
	}


	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		//a negative distance means we are inside the cylinder
		_newDist = _targetCylinder.signedDistance(unew[0], unew[1], unew[2]);

		// within accuracy? Accept and stop
		if (Math.abs(_newDist) < _accuracy) {
			accept(snew, unew);
			_result.setStatus(AdaptiveSwimmer.SWIM_SUCCESS);
  			return true;
		}

		//stop but don't accept new data. We crossed the target  boundary
		if (sign() != _startSign) {
			_result.setStatus(AdaptiveSwimmer.SWIM_CROSSED_BOUNDARY);
			
			//use prev distance to calculate next step
			_del = Math.abs(_prevDist);
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
	private int sign() {
		return (_newDist < 0) ? -1 : 1;
	}


}
