package cnuphys.swim;

import cnuphys.rk4.IStopper;
import cnuphys.swim.util.Plane;

public class DefaultPlaneStopper implements IStopper {

	private double _totalPathLength;
	private double _maxS;
	private double _accuracy;
	private double _maxRsSq = Double.POSITIVE_INFINITY;

	private Plane _plane;

	// which side of the plane
	private int _side;

	/**
	 * Plane stopper that doesn't check max R (does check max path length)
	 * 
	 * @param s0       starting path length in meters
	 * @param sMax     maximal path length in meters
	 * @param plane    the plane
	 * @param accuracy the accuracy in meters
	 * @param side     the starting side. Set to any number > 100 if not known.
	 */
	public DefaultPlaneStopper(double s0, double sMax, Plane plane, double accuracy, int side) {
		_totalPathLength = s0;
		_maxS = sMax;
		_accuracy = accuracy;
		_plane = plane;
		_side = side;
	}

	/**
	 * Plane stopper that checks Rmax (and sMax)
	 * 
	 * @param s0       starting path length in meters
	 * @param rMax     maximal radius in meters
	 * @param sMax     maximal path length in meters
	 * @param plane    the plane
	 * @param accuracy the accuracy in meters
	 * @param side     the starting side. Set to any number > 100 if not known.
	 */
	public DefaultPlaneStopper(double s0, double rMax, double sMax, Plane plane, double accuracy, int side) {
		this(s0, sMax, plane, accuracy, side);
		_maxRsSq = rMax * rMax;
	}

	/**
	 * Get the current side
	 * 
	 * @return the current side
	 */
	public int getSide() {
		return _side;
	}

	@Override
	public boolean stopIntegration(double s, double[] y) {
		// within accuracy?
		if (_plane.distanceToPlane(y[0], y[1], y[2]) < _accuracy) {
			return true;
		}

		// check limit of radial coordinate if finite max
		if (Double.isFinite(_maxRsSq)) {
			double rsq = y[0] * y[0] + y[1] * y[1] + y[2] * y[2];
			if (rsq > _maxRsSq) {
				return true;
			}
		}
		// TODO Auto-generated method stub
		if (s > _maxS) {
			return true;
		}

		int newSide = _plane.directionSign(y[0], y[1], y[2]);

		boolean shouldStop;
		if (_side > 99) {
			shouldStop = false;
		} else {
			shouldStop = (_side != newSide);
		}
		_side = newSide;
		return shouldStop;
	}

	@Override
	public double getFinalT() {
		return _totalPathLength;
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
		return _plane.contained(y[0], y[1], y[2], _accuracy);
	}
}
