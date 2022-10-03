package cnuphys.adaptiveSwim;


public class AdaptiveDefaultStopper extends AAdaptiveStopper {

	/**
	 * Default stopper simply checks pathlength. If the max pathlength is exceeded
	 * we stop.
	 * @param sMax          max path length meters
	 * @param result holds the results, its u statevector should have been initialized
	 * to the starting vector
	 */
	public AdaptiveDefaultStopper(final double sMax, AdaptiveSwimResult result) {
		super(sMax, Double.NaN, result);
	}

	/**
	 * For doing things like setting the initial sign and distance
	 */
	@Override
	public void initialize() {
		//do nothing
	}


	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		//the point that exceeds _sMax will also be accepted
		accept(snew, unew);

		if (snew > _sMax) {
			_result.setStatus(AdaptiveSwimmer.SWIM_SUCCESS);
			return true;
		}

		return false;
	}


	@Override
	public double getNewStepSize(double h) {
		// should not be called
		System.err.println("getNewStepSize should not have been called for the defaultStopper.");
		return 0;
	}

}