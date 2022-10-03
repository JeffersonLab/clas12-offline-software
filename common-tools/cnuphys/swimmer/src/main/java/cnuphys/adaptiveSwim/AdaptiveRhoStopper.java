package cnuphys.adaptiveSwim;

/**
 * Stopper for swimming to a fixed cylindrical cs radius (rho) value
 * @author heddle
 *
 */
public class AdaptiveRhoStopper extends AAdaptiveStopper {

	//the rho you want to stop at in meters
	private double _targetRho;


	//is the starting rho bigger or smaller than the target
	private int _startSign;

	//new value
	private double _newRho;
	
	private double _prevRho;
	/**
	 * Rho  stopper  (does check max path length)
	 * @param sMax           the maximum value of the path length in meters
	 * @param targetRho    stopping rho in meters
	 * @param accuracy     the accuracy in meters
	 * @param result holds the results, its u statevector should have been initialized
	 * to the starting vector
	 */
	public AdaptiveRhoStopper(final double sMax, final double targetRho, double accuracy, AdaptiveSwimResult result) {
		super(sMax, accuracy, result);
		_targetRho = targetRho;
	}

	
	/**
	 * For doing things like setting the initial sign and distance
	 */
	@Override
	public void initialize() {
		_prevRho = getRho(_result.getU());
		_newRho = _prevRho;
		_startSign = sign();
	}

	//just get the rho coordinate
	private double getRho(double u[]) {
		return Math.hypot(u[0], u[1]);
	}

	@Override
	public boolean stopIntegration(double snew, double[] unew) {

		_newRho = getRho(unew);
		
		// within accuracy?
		if (Math.abs(_newRho - _targetRho) < _accuracy) {
			accept(snew, unew);
			_result.setStatus(AdaptiveSwimmer.SWIM_SUCCESS);
  			return true;
		}

		//top but don't accept new data. We crossed the target  boundary
		if (sign() != _startSign) {
			_result.setStatus(AdaptiveSwimmer.SWIM_CROSSED_BOUNDARY);
			
			//use the previous rho to calculate new stepsize
			_del = Math.abs(_prevRho - _targetRho);
			return true;
		}

		//stop and accept new data. We exceeded smax and didn't hit the target
		if (snew > _sMax) {
			accept(snew, unew);
			_result.setStatus(AdaptiveSwimmer.SWIM_TARGET_MISSED);
			return true;
		}

		//not there yet-- accept new data and continue
		accept(snew, unew);
		_prevRho = _newRho;
		return false;
	}

	//get the sign based on the current rho
	private int sign() {
		return ((_newRho < _targetRho) ? -1 : 1);
	}

}