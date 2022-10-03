package cnuphys.adaptiveSwim;

public class AdaptiveZStopper extends AAdaptiveStopper {

	private double _targetZ;


	//is the starting rho bigger or smaller than the target
	private int _startSign;
	
	//new value
	private double _newZ;
	
	private double _prevZ;


	/**
	 * Z  stopper  (does check max path length)
	 * @param sMax           the maximum value of the path length in meters
	 * @param targetZ      stopping z in meters
	 * @param accuracy     the accuracy in meters
	 * @param result holds the results, its u statevector should have been initialized
	 * to the starting vector
	 */
	public AdaptiveZStopper(final double sMax, final double targetZ, double accuracy, AdaptiveSwimResult result) {
		super(sMax, accuracy, result);
		_targetZ = targetZ;
	}

	
	/**
	 * For doing things like setting the initial sign and distance
	 */
	@Override
	public void initialize() {
		_prevZ = getZ(_result.getU());
		_newZ = _prevZ;
		_startSign = sign();
	}


	//just get the z coordinate
	private double getZ(double u[]) {
		return u[2];
	}


	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		
		_newZ = getZ(unew);
				
		// within accuracy? Accept and stop
		if (Math.abs(unew[2] - _targetZ) < _accuracy) {
			accept(snew, unew);
			_result.setStatus(AdaptiveSwimmer.SWIM_SUCCESS);
  			return true;
		}

		//stop but don't accept new data. We crossed the target  boundary
		if (sign() != _startSign) {
			_result.setStatus(AdaptiveSwimmer.SWIM_CROSSED_BOUNDARY);
			
			//use the previous z to calculate new stepsize
			_del = Math.abs(_prevZ - _targetZ);
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
		_prevZ = _newZ;
		return false;
	}


	//get the sign based on the current z
	private int sign() {
		return ((_newZ < _targetZ) ? -1 : 1);
	}



}