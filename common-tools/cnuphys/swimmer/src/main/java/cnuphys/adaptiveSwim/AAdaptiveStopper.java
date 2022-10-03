package cnuphys.adaptiveSwim;

public abstract class AAdaptiveStopper implements IAdaptiveStopper {

	//the max step size
	protected static final double _THEMAXSTEP = 0.5; // meters
	// the current max step size, which varies with proximity
	//to a target
	private double _maxStep = _THEMAXSTEP; // meters

	protected final double _accuracy;

	//last step size used
	protected double _hLast = Double.NaN;

	//pathlength cutoff
	protected double _sMax;

	//swimming result
	protected AdaptiveSwimResult _result;

	//for specifying distance to a boundary
	protected double _del = Double.NaN;


	/**
	 * Create an stopper
	 * @param sMax the maximum value of the pathlength in meters
	 * @param accuracy the required accuracy in meters
	 * @param result holds the results, its u statevector should have been initialized
	 * to the starting vector
	 */
	public AAdaptiveStopper(final double sMax, final double accuracy, AdaptiveSwimResult result) {
		_sMax = sMax;
		_accuracy = accuracy;
		_result = result;
        if (_result.shouldUpdateTrajectory()) {
        	_result.getTrajectory().add(result.getU(), 0);
        }
	}
	
	/**
	 * Get the current path length
	 * @return the current path length in meters
	 */
	@Override
	public double getS() {
		return _result.getS();
	}

	/**
	 * Get the current state vector from the result object
	 * @return the current state vector
	 */
	@Override
	public double[] getU() {
		return _result.getU();
	}

	/**
	 * Accept a new integration step
	 * @param snew the new value of s in meters
	 * @param unew the new state vector
	 */
	protected void accept(double snew, double[] unew) {
		_result.setU(unew);
        _result.setS(snew);

        //add to trajectory?
        if (_result.shouldUpdateTrajectory()) {
        	_result.getTrajectory().add(unew, snew);
        }
	}

	/**
	 * Get the max value of the path length in meters
	 * @return the max or final value of the path length
	 */
	@Override
	public double getSmax() {
		return _sMax;
	}

	/**
	 * Copy a state vector
	 * @param uSrc the source
	 * @param uDest the destination
	 */
	protected void copy(double uSrc[], double[] uDest) {
		System.arraycopy(uSrc, 0, uDest, 0, AdaptiveSwimmer.DIM);
	}

	/**
	 * Get the max step size. This can vary with conditions, primarily
	 * with the proximity to a target
	 * @return the current max step in meters
	 */
	@Override
	public double getMaxStepSize() {
		return _maxStep;
	}

	/**
	 * Set the current max step
	 * @param maxStep the current max step in meters
	 */
	protected void setMaxStep(double maxStep) {
		_maxStep = Math.min(_THEMAXSTEP, Math.max(AdaptiveSwimUtilities.MIN_STEPSIZE, Math.abs(maxStep)));
	}

	@Override
	public double getNewStepSize(double h) {
		//_del should have been set by the stopper

		if (Double.isNaN(_del)) {
			System.err.println("in getNewStepSize, _del is NaN. That's rarely a good sign.");
			return Math.max(AdaptiveSwimUtilities.MIN_STEPSIZE, h/2);
		}
		
		double newH = Math.min(h/2, _del / 5);
		return Math.max(AdaptiveSwimUtilities.MIN_STEPSIZE, newH);
	}
	
	@Override
	public AdaptiveSwimResult getResult() {
		return _result;
	}


}
