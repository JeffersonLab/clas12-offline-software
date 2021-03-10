package cnuphys.adaptiveSwim;

import cnuphys.swim.SwimTrajectory;

/** 
 * For when we want to swim a precise path length
 * @author heddle
 *
 */
public class AdaptiveSStopper extends AAdaptiveStopper {
	
	//the desired accuract
	private double _accuracy;
	
	/**
	 * Pathlength  stopper 
	 * @param u0          initial state vector
	 * @param sf          final path length meters
	 * @param accuracy    the accuracy
	 * @param trajectory  optional trajectory
	 */
	public AdaptiveSStopper(final double[] u0, final double sf, double accuracy, SwimTrajectory trajectory) {
		super(u0, sf, Double.NaN, trajectory);
		_accuracy = accuracy;
	}


	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		
		// within accuracy?
		if (Math.abs(snew - _s) < _accuracy) {
			accept(snew, unew);
  			return true;
		}

		//stop and don't accept new data. We crossed the boundary or exceeded smax
		if (snew > _sf) {
			return true;
		}
				
		//accept new data and continue
		accept(snew, unew);
		return false;
	}

}
