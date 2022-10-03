package cnuphys.adaptiveSwim;



import cnuphys.adaptiveSwim.geometry.Sphere;

public class AdaptiveSphereStopper extends AAdaptiveStopper {

	private Sphere _targetSphere;
	
	//the newest distance to the sphere
	private double _prevDist;

	//the newest distance to the sphere
	private double _newDist;

	//the start sign of the distance
	private double _startSign;

	/**
	 * Sphere  stopper  (does check max path length)
	 * @param sMax                the maximum value of the path length in meters
	 * @param targetSphere    the target cylinder
	 * @param accuracy          the accuracy in meters
	 * @param result holds the results, its u statevector should have been initialized
	 * to the starting vector
	 */
	public AdaptiveSphereStopper(double sMax, Sphere targetSphere, double accuracy, AdaptiveSwimResult result) {
		super(sMax, accuracy, result);
		_targetSphere = targetSphere;
	}
	
	/**
	 * Sphere  stopper  (does check max path length)
	 * This assumes a sphere centered on the origin
	 * @param sMax                the maximum value of the path length in meters
	 * @param targetSphere    the target cylinder
	 * @param accuracy          the accuracy in meters
	 * @param result holds the results, its u statevector should have been initialized
	 * to the starting vector
	 */
	public AdaptiveSphereStopper(double sMax, double radius, double accuracy, AdaptiveSwimResult result) {
		this(sMax, new Sphere(radius), accuracy, result);
	}
	
	/**
	 * For doing things like setting the initial sign and distance
	 */
	@Override
	public void initialize() {
		double u[] = _result.getU();
		_prevDist = _targetSphere.signedDistance(u[0], u[1], u[2]);
		_newDist = _prevDist;

		_startSign = sign();		
	}


	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		//a negative distance means we are inside the cylinder
		_newDist = _targetSphere.signedDistance(unew[0], unew[1], unew[2]);

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

		//see if we jumped over the sphere but intersect it
		//only relevant if we started outside
		//if so, treat as sign change
		if (_startSign > 0) {
			if (_targetSphere.segmentIntersects(unew[0], unew[1], unew[2], _result.getU()[0],
					_result.getU()[1], _result.getU()[2])) {
				
				System.out.println("JUMPED OVER SPHERE");
				_result.setStatus(AdaptiveSwimmer.SWIM_CROSSED_BOUNDARY);
				
				//use prev distance to calculate next step
				_del = Math.abs(_prevDist);
				return true;
			}
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