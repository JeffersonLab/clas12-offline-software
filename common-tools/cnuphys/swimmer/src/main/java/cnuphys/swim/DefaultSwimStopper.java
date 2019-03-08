package cnuphys.swim;

import cnuphys.rk4.IStopper;

public class DefaultSwimStopper implements IStopper {

	private double _rMaxSq;

	protected double _finalPathLength = Double.NaN;

	/**
	 * A default swim stopper that will stop if either a max pathlength is exceeded
	 * or if a radial coordinate is exceeded
	 * 
	 * @param maxR the max radial coordinate in meters. Give a negative
	 */
	public DefaultSwimStopper(final double maxR) {
		_rMaxSq = maxR * maxR;
	}

	@Override
	public boolean stopIntegration(double t, double[] y) {
		double xx = y[0];
		double yy = y[1];
		double zz = y[2];

		// stop if radial coordinate too big
		double rsq = xx * xx + yy * yy + zz * zz;
		return (rsq > _rMaxSq);
	}

	/**
	 * Get the final path length in meters
	 * 
	 * @return the final path length in meters
	 */
	@Override
	public double getFinalT() {
		return _finalPathLength;
	}

	/**
	 * Set the final path length in meters
	 * 
	 * @param finalPathLength the final path length in meters
	 */
	@Override
	public void setFinalT(double finalPathLength) {
		_finalPathLength = finalPathLength;
	}

}
