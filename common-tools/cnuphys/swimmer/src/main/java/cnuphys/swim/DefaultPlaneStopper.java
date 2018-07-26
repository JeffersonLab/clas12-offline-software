package cnuphys.swim;

import cnuphys.rk4.IStopper;

public class DefaultPlaneStopper implements IStopper {
	
	private double _totalPathLength;
	private double _maxS;
	private double _accuracy;
	private double _maxRsSq = Double.POSITIVE_INFINITY;
	
	private Plane _plane;

	
	/**
	 * Plane stopper that doesn't check max R (does check max path length)
	 * @param s0 starting path length in meters
	 * @param sMax maximal path length in meters
	 * @param norm the normal vector where the components (0, 1, 2) map to (x, y, z)
	 * @param p a point in the plane where the components (0, 1, 2) map to (x, y, z)
	 * @param accuracy the accuracy in meters
	 */
	public DefaultPlaneStopper(double s0, double sMax, double norm[], double p[], double accuracy) {
		_totalPathLength = s0;
		_maxS = sMax;
		_accuracy = accuracy;
		_plane = Plane.createPlane(norm, p);
	}
	
	/**
	 * Plane stopper that checks Rmax (and sMax)
	 * @param s0 starting path length in meters
	 * @param rMax maximal radius in meters
	 * @param sMax maximal path length in meters
	 * @param norm the normal vector where the components (0, 1, 2) map to (x, y, z)
	 * @param p a point in the plane where the components (0, 1, 2) map to (x, y, z)
	 * @param accuracy the accuracy in meters
	 */
	public DefaultPlaneStopper(double s0, double rMax, double sMax, double norm[], double p[], double accuracy) {
		this(s0, sMax, norm, p, accuracy);
		_maxRsSq = rMax*rMax;
	}


	@Override
	public boolean stopIntegration(double t, double[] y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getFinalT() {
		return _totalPathLength;
	}

	@Override
	public void setFinalT(double finalT) {
		// Do nothing
	}

}
