package cnuphys.swim;

import cnuphys.adaptiveSwim.geometry.Plane;
import cnuphys.rk4.IStopper;

/**
 * This stopper is to stop at the boundary of an arbitrary cylinder
 * @author heddle
 *
 */

public class NewPlaneStopper implements IStopper {
	
	private double _totS; //total path length
	private double _sMax;
	private double _accuracy;
	private double[] _uf; //final state vector
	private double _s0; //starting path length
	
	//the newest distance to the cylinder
	private double _distance;

	
	//cache whether we have crossed the boundary
	private boolean _crossedBoundary;
	
	//cache whether we have passed smax
	private boolean _passedSmax;
	
	private int _dim;   //dimension of our system
	
	//the start sign of the distance
	private double _startSign;

	//the target Plane
	private Plane _targetPlane;

	/**
	 * Cylinder stopper that looks at boundary (does check max path length)
	 * 
	 * @param u0              starting state vector
	 * @param s0              starting path length in meters
	 * @param sMax            maximal path length in meters
	 * @param targetPlane     target Plane
	 * @param accuracy        the accuracy in meters
	 * @param normalDirection <code></code> if going smaller to larger rho
	 */
	public NewPlaneStopper(double[] u0, double s0, double sMax, Plane targetPlane, double accuracy) {

		
		_s0 = s0;
		_dim = u0.length;
		
		_uf = new double[_dim];
		copy(u0, _uf);
		
		_targetPlane = targetPlane;
		_distance = _targetPlane.signedDistance(u0[0], u0[1], u0[2]);
		_totS = 0;
		_sMax = sMax;
		_accuracy = accuracy;
		_startSign = Math.signum(_distance);
	}
	
	@Override
	public boolean stopIntegration(double s, double[] unew) {
		
		//a negative distance means we are inside the cylinder
		double newDist = _targetPlane.signedDistance(unew[0], unew[1], unew[2]);
		double newSign = Math.signum(newDist);
		newDist = Math.abs(newDist);

		// within accuracy?
		if (newDist < _accuracy) {
			_distance = newDist;
			_totS = s;
            copy(unew, _uf);
			return true;
		}
		
		//if we crossed the boundary (don't accept, reset)
		_crossedBoundary = newSign != _startSign;
		if (_crossedBoundary) {
			return true;
		}

		
		_passedSmax = (s > _sMax);
		//if exceeded max path length accept and stop
		if (_passedSmax) {
			_distance = newDist;
			_totS = s;
            copy(unew, _uf);
			return true;
		}		
		
		
		//accept and continue
		_distance = newDist;
		_totS = s;
        copy(unew, _uf);
		return false;
	}

	//array copy for state vectors
	private void copy(double src[], double[] dest) {
		System.arraycopy(src, 0, dest, 0, _dim);
	}

	/**
	 * Did we pas the max path length?
	 * @return true if we crossed the boundary
	 */
	public boolean passedSmax() {
		return _passedSmax;
	}


	/**
	 * Did we cross the boundary
	 * @return true if we crossed the boundary
	 */
	public boolean crossedBoundary() {
		return _crossedBoundary;
	}

	/**
	 * Get the final path length in meters
	 * 
	 * @return the final path length in meters
	 */
	@Override
	public double getFinalT() {
		return _s0 + _totS;
	}

	@Override
	public void setFinalT(double finalT) {
		// Do nothing
	}

	/**
	 * Get the final value of the state vector
	 * @return
	 */
	public double[] getFinalU() {
		if (_uf == null) {
			System.err.println("Returning null final u");
		}
		return _uf;
	}
	
	/**
	 * Generally this is the same as stop integration. So most will just return
	 * stopIntegration(). But sometimes stop just means we reset and integrate more.
	 * For example, with a fixed rho integrator we "stop" when we cross the rho boundary
	 * however we are not done unless we are within tolerance. If we are within
	 * tolerance (on either side) we are really done!
	 * 
	 * @param t the current value of the independent variable (typically pathlength)
	 * @param y the current state vector (typically [x, y, z, vx, vy, vz])
	 * @return <code>true</code> if we should stop now.
	 */
	@Override
	public boolean terminateIntegration(double t, double y[]) {
		System.err.println("Should not be called. Ever.");
		System.exit(1);
		return false;
	}

}
