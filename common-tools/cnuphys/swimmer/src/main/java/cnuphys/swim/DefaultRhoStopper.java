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
	private double _maxS;
	private double _accuracy;
	private double _currentRho = Double.NaN;
	private double[] _uf; //final state vector
	private double[] _uprev; //last u with the same sign
	private double _s0; //starting path length
	
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
		_uprev = new double[_dim];
		
		_targetRho = targetRho;
		_totS = 0;
		_prevS = 0;
		_maxS = sMax;
		_accuracy = accuracy;
		_startSign = sign(rho0);
	}
	
	//get the sign based on the current rho
	private int sign(double currentRho) {
		return ((currentRho < _targetRho) ? -1 : 1);
	}


	private void copy(double src[], double[] dest) {
		System.arraycopy(src, 0, dest, 0, _dim);
	}
	
	@Override
	public boolean stopIntegration(double s, double[] u) {
		
		_currentRho = Math.hypot(u[0], u[1]);
		_totS = s;

		// within accuracy?
		if (Math.abs(_currentRho - _targetRho) < _accuracy) {
            copy(u, _uf);
			return true;
		}

		// independent variable s is the path length
		if (s > _maxS) {
			copy(u, _uf);
			return true;
		}

		//stop (and backup/reset to prev) if we crossed the boundary
		if (sign(_currentRho) != _startSign) {
			_totS = _prevS;
			copy(_uprev, _uf);	
			return true;
		}
		
		//copy current to previous
		_prevS = _totS;
		copy(u, _uprev);	
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

	/**
	 * Is the current rho within accuracy
	 * 
	 * @param rho        current rho
	 * @param accuracy accuracy
	 * @return <code>true</code> if current z with accuracy
	 */
	public boolean withinAccuracy(double rho, double accuracy) {
		return Math.abs(rho - _targetRho) < accuracy;
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
		boolean stop = Math.abs(_currentRho - _targetRho) < _accuracy;
		
		if (stop) {
			copy(_uf, y);	
		}

		return stop;
	}

}