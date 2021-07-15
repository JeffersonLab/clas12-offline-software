package cnuphys.swim;

import cnuphys.rk4.IStopper;

/**
 * This stopper is to stop at a fixed value of the cylindrical coordinate rho. It can
 * also be considered a cylindrical stopper if the cylinder is centered on the z axis.
 * @author heddle
 *
 */
public class DefaultRhoStopper implements IStopper {

	private double _targetRho;
	private double _totS; //total path length
	private double _sMax;
	private double _accuracy;
	private double[] _uf; //final state vector
	private double _s0; //starting path length
	
	//cache whether we have crossed the boundary
	private boolean _crossedBoundary;
	
	//cache whether we have passed smax
	private boolean _passedSmax;
	
	//the current rho
	private double _rho;


	
	private int _dim;   //dimension of our system
	
	private int _startSign; //is starting val bigger or smaller than targ value

	/**
	 * Rho  stopper that doesn't check max R (does check max path length)
	 * 
	 * @param uo              starting state vector
	 * @param s0              starting path length in meters
	 * @param sMax            maximal path length in meters
	 * @param targetRho       stopping rho in meters
	 * @param accuracy        the accuracy in meters
	 * @param normalDirection <code></code> if going smaller to larger rho
	 */
	public DefaultRhoStopper(double[] uo, double s0, double sMax, double rho0, double targetRho, double accuracy) {
		
		_rho = rho0;
		
		_s0 = s0;
		_dim = uo.length;
		
		_uf = new double[_dim];
		copy(uo, _uf);
		
		_targetRho = targetRho;
		_totS = 0;
		_sMax = sMax;
		_accuracy = accuracy;
		_startSign = sign(_rho);
		
		
	}
	
	//get the sign based on the current rho
	private int sign(double currentRho) {
		return ((currentRho < _targetRho) ? -1 : 1);
	}
	
	/**
	 * Did we pas the max path length?
	 * @return true if we crossed the boundary
	 */
	public boolean passedSmax() {
		return _passedSmax;
	}


	/**
	 * Did we cross the boundary
	 * @return true if we crossed the boundary
	 */
	public boolean crossedBoundary() {
		return _crossedBoundary;
	}
	
	/**
	 * Get the current value of rho
	 * @return the current value of rho
	 */
	public double getRho() {
		return _rho;
	}


	//array copy for state vectors
	private void copy(double src[], double[] dest) {
		System.arraycopy(src, 0, dest, 0, _dim);
	}
	
	@Override
	public boolean stopIntegration(double s, double[] u) {
		
		double newRho = Math.hypot(u[0], u[1]);

		// within accuracy?
		if (Math.abs(newRho - _targetRho) < _accuracy) {
			_rho = newRho;
			_totS = s;
            copy(u, _uf);
			return true;
		}
		
		//if we crossed the boundary (don't accept, reset)
		_crossedBoundary = sign(newRho) != _startSign;
		if (_crossedBoundary) {
			return true;
		}

		
		_passedSmax = (s > _sMax);
		//if exceeded max path length accept and stop
		if (_passedSmax) {
			_rho = newRho;
			_totS = s;
            copy(u, _uf);
			return true;
		}		
		
		
		//accept and continue
		_rho = newRho;
		_totS = s;
        copy(u, _uf);
		return false;
	}

	/**
	 * Get the final path length in meters
	 * 
	 * @return the final path length in meters
	 */
	@Override
	public double getFinalT() {
		return _s0 + _totS;
	}

	@Override
	public void setFinalT(double finalT) {
		// Do nothing
	}

	/**
	 * Get the final value of the state vector
	 * @return
	 */
	public double[] getFinalU() {
		if (_uf == null) {
			System.err.println("Returning null final u");
		}
		return _uf;
	}
	
	/**
	 * Generally this is the same as stop integration. So most will just return
	 * stopIntegration(). But sometimes stop just means we reset and integrate more.
	 * For example, with a fixed rho integrator we "stop" when we cross the rho boundary
	 * however we are not done unless we are within tolerance. If we are within
	 * tolerance (on either side) we are really done!
	 * 
	 * @param t the current value of the independent variable (typically pathlength)
	 * @param y the current state vector (typically [x, y, z, vx, vy, vz])
	 * @return <code>true</code> if we should stop now.
	 */
	@Override
	public boolean terminateIntegration(double t, double y[]) {
		System.err.println("Should not be called. Ever.");
		System.exit(1);
		return false;
	}

}