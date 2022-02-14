package cnuphys.adaptiveSwim;

import cnuphys.adaptiveSwim.geometry.Plane;
import cnuphys.swim.SwimTrajectory;

/**
 * Stopper for swimming to a plane
 * @author heddle
 *
 */
public class AdaptivePlaneStopper extends AAdaptiveStopper {

	//the plane you want to swim to
	private Plane _targetPlane;


	//is the starting rho bigger or smaller than the target
	private int _startSign;
			
	/**
	 * Rho  stopper  (does check max path length)
	 * @param u0           initial state vector
	 * @param sf           the maximum value of the path length in meters
	 * @param targetPlane  the target plane
	 * @param accuracy     the accuracy in meters
	 * @param trajectory   optional swim trajectory (can be null)
	 */
	public AdaptivePlaneStopper(final double[] u0, final double sf, Plane targetPlane, double accuracy, SwimTrajectory trajectory) {
		super(u0, sf, accuracy, trajectory);
		_targetPlane = targetPlane;
		_startSign = sign(signedDistance(u0));
	}

	/**
	 * Get the signed distance from a state vector to a point on the target plane
	 * @param u the state vector
	 * @return the signed distance from a state vector to a point on the target plane in meters
	 */
	private double signedDistance(double u[]) {
		return _targetPlane.signedDistance(u[0], u[1], u[2]);
	}
	
	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		
		double newSignedDist = signedDistance(unew);

		// within accuracy?
		//note this could also result with s > smax
		if (Math.abs(newSignedDist) < _accuracy) {
			accept(snew, unew);
  			return true;
		}

		//stop and don't accept new data. We crossed the boundary or exceeded smax
		if ((snew > _sf) || (sign(newSignedDist) != _startSign)) {
			return true;
		}
				
		//accept new data and continue
		accept(snew, unew);
		return false;
	}
	
	
	//get the sign based on the signed distance
	private int sign(double dist) {
		return ((dist < 0) ? -1 : 1);
	}
	

}