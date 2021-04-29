package cnuphys.adaptiveSwim;


import cnuphys.adaptiveSwim.geometry.Sphere;
import cnuphys.swim.SwimTrajectory;

public class AdaptiveSphereStopper extends AAdaptiveStopper {
	
	//the target cylinder
	private Sphere _targetSphere;
	
	//the newest distance to the cylinder
	private double _newDist;
	
	
	/**
	 * Cylinder  stopper  (does check max path length)
	 * @param u0                initial state vector
	 * @param sf                the maximum value of the path length in meters
	 * @param targetSphere      the target sphere
	 * @param accuracy          the accuracy in meters
	 * @param trajectory        optional swim trajectory (can be null)
	 */
	public AdaptiveSphereStopper(final double[] u0, final double sf, Sphere targetSphere, double accuracy, SwimTrajectory trajectory) {
		super(u0, sf, accuracy, trajectory);
		_targetSphere = targetSphere;
		
	}
	

	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		
		//a negative distance means we are inside the cylinder
		//we don't care so take abs val
		_newDist = Math.abs(_targetSphere.distance(unew[0], unew[1], unew[2]));

		// within accuracy?
		//note this could also result with s > smax
		if (_newDist < _accuracy) {
			accept(snew, unew);
  			return true;
		}

		//stop and don't accept new data. We exceeded smax
		if (snew > _sf) {
			return true;
		}
				
		//accept new data and continue
		accept(snew, unew);
		return false;
	}


	/**
	 * Accept a new integration step
	 * @param snew the new value of s in meters
	 * @param unew the new state vector
	 */
	@Override
	protected void accept(double snew, double[] unew) {
		super.accept(snew, unew);
		
		//do not take a step that might leap us over the sphere
		//this is necessary because there is no crossover "side" for a cylinder
		//like for a plane or fixed z,rho
		
		double newMaxStep = Math.min(_THEMAXSTEP, Math.max(AdaptiveSwimUtilities.MIN_STEPSIZE, _newDist/5));
	
		setMaxStep(newMaxStep);
	}

}
