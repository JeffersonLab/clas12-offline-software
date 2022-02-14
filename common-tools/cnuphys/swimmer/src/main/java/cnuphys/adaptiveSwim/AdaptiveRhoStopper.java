package cnuphys.adaptiveSwim;

import cnuphys.magfield.FastMath;
import cnuphys.swim.SwimTrajectory;

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
	
	//the current rho
	private double _rho;
			
	//cache whether we have crossed the boundary
	private boolean _crossedBoundary;
	
	//cache whether we have passed smax
	private boolean _passedSmax;


	/**
	 * Rho  stopper  (does check max path length)
	 * @param u0           initial state vector
	 * @param sf           the maximum value of the path length in meters
	 * @param targetRho    stopping rho in meters
	 * @param accuracy     the accuracy in meters
	 * @param trajectory   optional swim trajectory (can be null)
	 */
	public AdaptiveRhoStopper(final double[] u0, final double sf, final double targetRho, double accuracy, SwimTrajectory trajectory) {
		super(u0, sf, accuracy, trajectory);
		_targetRho = targetRho;
		_rho = FastMath.hypot(u0[0], u0[1]);
		_startSign = sign(_rho);
	}

	
	@Override
	public boolean stopIntegration(double snew, double[] unew) {
		
		double newRho = Math.hypot(unew[0], unew[1]);

		// within accuracy?
		if (Math.abs(newRho - _targetRho) < _accuracy) {
			_rho = newRho;
			accept(snew, unew);
  			return true;
		}
		
		//if we crossed the boundary (don't accept, reset)
		_crossedBoundary = sign(newRho) != _startSign;
		if (_crossedBoundary) {
			return true;
		}
		
		_passedSmax = (snew > _sf);
		//if exceeded max path length accept and stop
		if (_passedSmax) {
			_rho = newRho;
			accept(snew, unew);
			return true;
		}
				
		//accept new data and continue
		_rho = newRho;
		accept(snew, unew);
		return false;
	}
	
	/**
	 * Get the current value of rho
	 * @return the current value of rho
	 */
	public double getRho() {
		return _rho;
	}
	
	
	//get the sign based on the current rho
	private int sign(double currentRho) {
		return ((currentRho < _targetRho) ? -1 : 1);
	}
	
	/**
	 * Did we cross the boundary?
	 * @return true if we crossed the boundary
	 */
	public boolean crossedBoundary() {
		return _crossedBoundary;
	}
	
	/**
	 * Did we pas the max path length?
	 * @return true if we crossed the boundary
	 */
	public boolean passedSmax() {
		return _passedSmax;
	}

	
}