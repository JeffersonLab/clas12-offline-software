package cnuphys.adaptiveSwim;

import cnuphys.swim.SwimTrajectory;

public class AdaptiveDefaultStopper extends AAdaptiveStopper {

	private static final double TOL = 1.0e-5; //meters
	
	private double _sCutoff;
			
	/**
	 * Rho  stopper  (does check max path length)
	 * @param u0          initial state vector
	 * @param sf          final path length meters
	 * @param trajectory  optional trajectory
	 */
	public AdaptiveDefaultStopper(final double[] u0, final double sf, SwimTrajectory trajectory) {
		super(u0, sf, Double.NaN, trajectory);
		_sCutoff = sf - TOL;
	}

	
	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		
		accept(snew, unew);
		
		// within tolerance?
		return (snew > _sCutoff);
	}
	
}