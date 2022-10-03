package cnuphys.adaptiveSwim.swimZ;

import cnuphys.adaptiveSwim.AAdaptiveStopper;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.swim.SwimTrajectory;

/** 
 * For when we want to swim a precise path length
 * @author heddle
 *
 */
public class SwimZStopper {
	
	//previous Z
	private double _prevZ;
	
	//new Z
	private double _newZ;
	
	//the desired accuracy
	private double _accuracy;
	
	//starting sign
	private int _startSign;
	
	//the target z
	private double _zf;
	
	private SwimZResult _result;

	/**
	 * For the z swimmer, z is the independent variable. This
	 * attempts to swim to a final z value
	 * @param zf           the desired final value of z in cm
	 * @param accuracy     the accuracy in cm
	 * @param result holds the results, its u statevector should have been initialized
	 * to the starting vector
	 */
	public SwimZStopper(double zf, double accuracy, SwimZResult result) {
		_zf = zf;
		_accuracy = accuracy;
		_result = result;
	}
	
	public SwimZResult getResult() {
		return _result;
	}

	public void initialize() {
		_prevZ = getResult().getZ();
		_newZ = _prevZ;
		_startSign = sign(_newZ);
	}

	public boolean stopIntegration(double znew, double[] unew) {
		
		
		// within accuracy? Accept and stop
		if (Math.abs(znew - _zf) < _accuracy) {
			accept(znew, unew);
  			return true;
		}

		//stop and don't accept new data. We crossed the boundary
		if (sign(znew) != _startSign) {
			return true;
		}
				
		//accept new data and continue
		accept(znew, unew);
		return false;
	}

	//look for a sign change
	private int sign(double z) {
		double dz = z - _zf;
		return (dz < 0) ? 1 : -1;
	}

	/**
	 * Accept a new integration step
	 * @param snew the new value of s in meters
	 * @param unew the new state vector
	 */
	protected void accept(double znew, double[] unew) {
		_result.setU(unew);
        _result.setZ(znew);

        //add to trajectory?
        if (_result.shouldUpdateTrajectory()) {
        	
        	_result.getTrajectory().add(unew, znew);
        }
	}


}
