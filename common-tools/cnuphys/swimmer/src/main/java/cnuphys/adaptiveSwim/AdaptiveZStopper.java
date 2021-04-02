package cnuphys.adaptiveSwim;

import cnuphys.swim.SwimTrajectory;

public class AdaptiveZStopper extends AAdaptiveStopper {

	private double _targetZ;


	//is the starting rho bigger or smaller than the target
	private int _startSign;
			
	/**
	 * Z  stopper  (does check max path length)
	 * @param u0           initial state vector
	 * @param sf           the maximum value of the path length in meters
	 * @param targetZ      stopping z in meters
	 * @param accuracy     the accuracy in meters
	 * @param trajectory   optional swim trajectory (can be null)
	 */
	public AdaptiveZStopper(final double[] u0, final double sf, final double targetZ, double accuracy, SwimTrajectory trajectory) {
		super(u0, sf, accuracy, trajectory);
		_targetZ = targetZ;
		_startSign = sign(u0[2]);
	}

	
	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		

		// within accuracy?
		//note this could also result with s > smax
		if (Math.abs(unew[2] - _targetZ) < _accuracy) {
			accept(snew, unew);
  			return true;
		}
		
		//stop but don't accept new data. We crossed the target  boundary
		if (sign(unew[2]) != _startSign) {
			return true;
		}


		//stop and accept new data. We exceeded smax
		if (snew > _sf) {
			accept(snew, unew);
			return true;
		}
				
		//accept new data and continue
		accept(snew, unew);
		
		return false;
	}
	
	
	//get the sign based on the current rho
	private int sign(double z) {
		return ((z < _targetZ) ? -1 : 1);
	}
	


}