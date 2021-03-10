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
	private double _prevS; //previous step path length
	private double _sMax;
	private double _accuracy;
	private double[] _uf; //final state vector
	private double _s0; //starting path length
//	private int _prevRdot = 0;
//	public boolean rdotChanged;
	
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
		
		
		_s0 = s0;
		_dim = uo.length;
		
		_uf = new double[_dim];
		
		_targetRho = targetRho;
		_totS = 0;
		_prevS = 0;
		_sMax = sMax;
		_accuracy = accuracy;
		_startSign = sign(rho0);
	}
	
	//get the sign based on the current rho
	private int sign(double currentRho) {
		return ((currentRho < _targetRho) ? -1 : 1);
	}


	//array copy for state vectors
	private void copy(double src[], double[] dest) {
		System.arraycopy(src, 0, dest, 0, _dim);
	}
	
	@Override
	public boolean stopIntegration(double s, double[] u) {
		
		double currentRho = Math.hypot(u[0], u[1]);
		_totS = s;

		// within accuracy?
		//note this could also result with s > smax
		if (Math.abs(currentRho - _targetRho) < _accuracy) {
            copy(u, _uf);
			return true;
		}

		//stop (and backup/reset to prev) if we crossed the boundary or exceeded smax
		if ((getFinalT() > _sMax) || (sign(currentRho) != _startSign)) {
			_totS = _prevS;
			return true;
		}
		
//		int rdSign = rhoDotSign(u);
//		if ((_prevRdot != 0) && (rdSign != 0) && (_prevRdot != rdSign)) {
//			rdotChanged = true;;
//		}
//		_prevRdot = rdSign;
		
		//copy current to previous
		_prevS = _totS;
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
	
//	public int rhoDotSign(double u[]) {
//		double val = u[0]*u[3] + u[1]*u[4];
//		if (val == 0) {
//			System.err.println("ZERO!");
//		}
//		
//		if (val < 0) {
//			return -1;
//		}
//		else if (val > 0) {
//			return 1;
//		}
//		else {
//			return 0;
//		}
//	}

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