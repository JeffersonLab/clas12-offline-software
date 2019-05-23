package cnuphys.swim;

import cnuphys.rk4.IStopper;

/**
 * This stopper is to stop at a fixed value of the cylindrical coordinate rho. It can
 * also be considered a cylindrical stopper if the cylinder is centered on the z axis.
 * @author heddle
 *
 */
public class DefaultRhoStopper implements IStopper {

	private double _targetRho;
	private boolean _normalDirection;
	private double _totalPathLength;
	private double _maxS;
	private double _accuracy;
	private double _currentRho = Double.NaN;
	private double[] _finalY; //final state vector

	public DefaultRhoStopper() {
	}

	/**
	 * Rho  stopper that doesn't check max R (does check max path length)
	 * 
	 * @param s0              starting path length in meters
	 * @param sMax            maximal path length in meters
	 * @param targetRho       stopping rho in meters
	 * @param accuracy        the accuracy in meters
	 * @param normalDirection <code></code> if going smaller to larger rho
	 */
	public DefaultRhoStopper(double s0, double sMax, double targetRho, double accuracy, boolean normalDirection) {
		_targetRho = targetRho;
		_totalPathLength = s0;
		_maxS = sMax;
		_normalDirection = normalDirection;
		_accuracy = accuracy;
	}

	/**
	 * Set the starting pathlength. This is only necessary if we are continuing a track.
	 * @param s0 the starting path length in meters
	 */
	public void setS0(double s0) {
		_totalPathLength = s0;
	}

	/**
	 * Set the maximum pathlength in meters
	 * @param sMax the maximum path length in meters
	 */
	public void setSMax(double sMax) {
		_maxS = sMax;
	}

	/**
	 * Set the target value for rho in meters
	 * @param targetRho the target value for rho in meters
	 */
	public void setTargetRho(double targetRho) {
		_targetRho = targetRho;
	}

	/**
	 * Set the accuracy with which you want to hit the rho target value
	 * @param accuracy in meters
	 */
	public void setAccuracy(double accuracy) {
		_accuracy = accuracy;
	}

	/**
	 * Set the normal direction to +1 if rho is increasing
	 * and -1 if it is decreasing
	 * @param normalDirection the direction indicator
	 */
	public void setNormalDirection(boolean normalDirection) {
		_normalDirection = normalDirection;
	}

	//copy onto the final y
	private void copyToFinalY(double[] y) {
		int len = y.length;
		_finalY = new double[len];
		System.arraycopy(y, 0, _finalY, 0, len);
	}
	
	
	@Override
	public boolean stopIntegration(double s, double[] y) {

		_currentRho = Math.hypot(y[0], y[1]);
		_totalPathLength = s;

		// within accuracy?
		if (Math.abs(_currentRho - _targetRho) < _accuracy) {
            copyToFinalY(y);
			return true;
		}

		// independent variable s is the path length
		if (s > _maxS) {
            copyToFinalY(y);
			return true;
		}

		boolean stop;
		if (_normalDirection) {
			stop = (_currentRho > _targetRho);
		} else {
			stop = (_currentRho < _targetRho);
		}
		
		if (stop) {
			copyToFinalY(y);	
		}
		
		return stop;
	}

	/**
	 * Get the final path length in meters
	 * 
	 * @return the final path length in meters
	 */
	@Override
	public double getFinalT() {
		return _totalPathLength;
	}

	/**
	 * Is the current rho within accuracy
	 * 
	 * @param rho        current rho
	 * @param accuracy accuracy
	 * @return <code>true</code> if current z with accuracy
	 */
	public boolean withinAccuracy(double rho, double accuracy) {
		return Math.abs(rho - _targetRho) < accuracy;
	}

	@Override
	public void setFinalT(double finalT) {
		// Do nothing
	}

	/**
	 * Get the final value of the state vector
	 * @return
	 */
	public double[] getFinalY() {
		return _finalY;
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
		boolean stop = Math.abs(_currentRho - _targetRho) < _accuracy;
		
		if (stop) {
			copyToFinalY(y);	
		}

		return stop;
	}

}