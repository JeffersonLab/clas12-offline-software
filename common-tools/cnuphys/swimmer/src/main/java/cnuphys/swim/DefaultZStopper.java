package cnuphys.swim;

import cnuphys.rk4.IStopper;

public class DefaultZStopper implements IStopper {

	private double _targetZ;
	private boolean _normalDirection;
	private double _totalPathLength;
	private double _maxS;
	private double _accuracy;
	private double _currentZ = Double.NaN;
	private double _maxRsSq = Double.POSITIVE_INFINITY;

	public DefaultZStopper() {
	}

	/**
	 * Z stopper that doesn't check max R (does check max path length)
	 * 
	 * @param s0              starting path length in meters
	 * @param sMax            maximal path length in meters
	 * @param targetZ         stopping Z in meters
	 * @param accuracy        the accuracy in meters
	 * @param normalDirection <code></code> if going smaller to larger z
	 */
	public DefaultZStopper(double s0, double sMax, double targetZ, double accuracy, boolean normalDirection) {
		_targetZ = targetZ;
		_totalPathLength = s0;
		_maxS = sMax;
		_normalDirection = normalDirection;
		_accuracy = accuracy;
	}

	/**
	 * Z stopper that checks Rmax (and sMax)
	 * 
	 * @param s0              starting path length in meters
	 * @param rMax            maximal radius in meters
	 * @param sMax            maximal path length in meters
	 * @param targetZ         stopping Z in meters
	 * @param accuracy        the accuracy in meters
	 * @param normalDirection <code></code> if going smaller to larger z
	 */
	public DefaultZStopper(double s0, double rMax, double sMax, double targetZ, double accuracy,
			boolean normalDirection) {
		this(s0, sMax, targetZ, accuracy, normalDirection);
		_maxRsSq = rMax * rMax;
	}

	public void setS0(double s0) {
		_totalPathLength = s0;
	}

	public void setSMax(double sMax) {
		_maxS = sMax;
	}

	public void setTargetZ(double targetZ) {
		_targetZ = targetZ;
	}

	public void setAccuracy(double accuracy) {
		_accuracy = accuracy;
	}

	public void setNormalDirection(boolean normalDirection) {
		_normalDirection = normalDirection;
	}

	@Override
	public boolean stopIntegration(double s, double[] y) {

		_currentZ = y[2];
		_totalPathLength = s;

		// within accuracy?
		if (Math.abs(_currentZ - _targetZ) < _accuracy) {
			return true;
		}

		// check limit of radial coordinate if finite max
		if (Double.isFinite(_maxRsSq)) {
			double rsq = y[0] * y[0] + y[1] * y[1] + y[2] * y[2];
			if (rsq > _maxRsSq) {
				return true;
			}
		}

		// independent variable s is the path length
		if (s > _maxS) {
			return true;
		}

		if (_normalDirection) {
			return (_currentZ > _targetZ);
		} else {
			return (_currentZ < _targetZ);
		}
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
	 * Is the current z within accuracy
	 * 
	 * @param z        current z
	 * @param accuracy accuracy
	 * @return <code>true</code> if current z with accuracy
	 */
	public boolean withinAccuracy(double z, double accuracy) {
		return Math.abs(z - _targetZ) < accuracy;
	}

	@Override
	public void setFinalT(double finalT) {
		// Do nothing
	}

	/**
	 * Generally this is the same as stop integration. So most will just return
	 * stopIntegration(). But sometimes stop just means we reset and integrate more.
	 * For example, with a fixed Z integrator we "stop" when we cross the z boundary
	 * however we are not done unless we are within tolerance. If we are within
	 * tolerance (on either side) we are really done!
	 * 
	 * @param t the current value of the independent variable (typically pathlength)
	 * @param y the current state vector (typically [x, y, z, vx, vy, vz])
	 * @return <code>true</code> if we should stop now.
	 */
	@Override
	public boolean terminateIntegration(double t, double y[]) {
		return Math.abs(_currentZ - _targetZ) < _accuracy;
	}

}
