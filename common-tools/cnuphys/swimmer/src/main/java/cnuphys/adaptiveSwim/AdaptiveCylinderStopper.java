package cnuphys.adaptiveSwim;

import cnuphys.adaptiveSwim.geometry.Cylinder;
import cnuphys.swim.SwimTrajectory;

public class AdaptiveCylinderStopper extends AAdaptiveStopper {
	
	//the target cylinder
	private Cylinder _targetCylinder;
	
	//the newest distance to the cylinder
	private double _distance;
	
	//cache whether we have passed smax
	private boolean _passedSmax;
	
	//cache whether we have crossed the boundary
	private boolean _crossedBoundary;

	//the start sign of the distance
	private double _startSign;

	
	/**
	 * Cylinder  stopper  (does check max path length)
	 * @param u0                initial state vector
	 * @param sf                the maximum value of the path length in meters
	 * @param targetCylinder    the target cylinder
	 * @param accuracy          the accuracy in meters
	 * @param trajectory        optional swim trajectory (can be null)
	 */
	public AdaptiveCylinderStopper(final double[] u0, final double sf, Cylinder targetCylinder, double accuracy, SwimTrajectory trajectory) {
		super(u0, sf, accuracy, trajectory);
		_targetCylinder = targetCylinder;
		_distance = _targetCylinder.distance(u0[0], u0[1], u0[2]);
		_startSign = Math.signum(_distance);
	}
	

	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		
		//a negative distance means we are inside the cylinder
		double newDist = _targetCylinder.distance(unew[0], unew[1], unew[2]);
		double newSign = Math.signum(newDist);
		newDist = Math.abs(newDist);

		// within accuracy?
		if (newDist < _accuracy) {
			_distance = newDist;
			accept(snew, unew);
  			return true;
		}
		
		//if we crossed the boundary (don't accept, reset)
		_crossedBoundary = newSign != _startSign;
		if (_crossedBoundary) {
			return true;
		}

		_passedSmax = (snew > _sf);
		//if exceeded max path length accept and stop
		if (_passedSmax) {
			_distance = newDist;
			accept(snew, unew);
			return true;
		}

		//accept new data and continue
		_distance = newDist;
		accept(snew, unew);
		return false;
	}

	/**
	 * Get the current value of the distance (positive definite)
	 * @return the current value of distance
	 */
	public double getDistance() {
		return _distance;
	}

	/**
	 * Did we cross the boundary?
	 * @return true if we crossed the boundary
	 */
	public boolean crossedBoundary() {
		return _crossedBoundary;
	}

	/**
	 * Did we pas the max path length?
	 * @return true if we crossed the boundary
	 */
	public boolean passedSmax() {
		return _passedSmax;
	}


}
