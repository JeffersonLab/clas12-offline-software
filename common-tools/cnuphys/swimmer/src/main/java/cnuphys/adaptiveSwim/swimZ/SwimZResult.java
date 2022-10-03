package cnuphys.adaptiveSwim.swimZ;

import cnuphys.adaptiveSwim.AdaptiveSwimmer;

/**
 * This class holds the results the swimZ
 * integration. The swimZ integration follows the method described for the
 * HERA-B magnet here: http://arxiv.org/pdf/physics/0511177v1.pdf<br>
 * <p>
 * In this swim, z is the independent variable. It is only good for swims
 * where z is monotonic. It can handle z increasing or z decreasing (backwars
 * swim) but not where z changes direction.
 * <p>
 * The state vector has five elements: <br>
 * (x, y, tx, ty, q) <br>
 * Where x and y are the transverse coordinates, tx = px/pz, ty = py/pz,
 * and q = Q/|p| where Q is the integer charge (e.g. -1 for an electron)
 * * <p>
 * Note q is constant, so this is only a 4D problem, unlike the regular swimmer which is 6D.
 * That's why we go through all the trouble.
 * <p>
 * UNITS  (NOT THE SAME AS THE REGULAR SWIMMER!!!!)
 * <ul>
 * <li>x, y, and z are in cm
 * <li>p is in GeV/c
 * <li>B (mag field) is in kGauss
 * </ul>
 * <p>
 * **/
public class SwimZResult {
	
	//the current 4D state vector
	private final double[] _u;

	//the number of integration steps
	private int _nStep;

	//the current z
	private double _z;

	//a status, one of the AdaptiveSwimmer class constants
	private int _status;

	//optionally holds a trajectory of [x, y, z, tx, ty, tz] (coords in meters)
	private SwimZTrajectory _trajectory;

	//the initial values in the usual variables
	private SwimZInitialValues _initialValues;

	//whether a trajectory is created
	private boolean _saveTrajectory;

	//this can be used to pause updating the trajectory
	private boolean _updateTrajectory;

	//the direction, 1 if zf > zo
	private int _sign;
	
	// the integer charge
	private int _charge;
	
	//the momentum (constant) in GeV/c
	private double _p;

	
	/**
	 * Create a container for the swim results for default of 4D state vector
	 * @param charge the integer charge
	 * @param p the momentum in GeV/c
	 * @param saveTrajectory if true, we will save the trajectory
	 */
	public SwimZResult(int charge, double p, boolean saveTrajectory) {
		_u = new double[4];
		_saveTrajectory = saveTrajectory;
		reset(charge, p);
	}
	
	/**
	 * Reset so that the object can be reused.
	 * @param charge the integer charge
	 * @param p the momentum in GeV/c
	 */
	public void reset(int charge, double p) {

		_charge = charge;
		_p = p;
		
		for (int i = 0; i < 4; i++) {
			_u[i] = Double.NaN;
		}

		if (_saveTrajectory) {
			_trajectory = new SwimZTrajectory(charge, p);
			_updateTrajectory = true;
		}
		else {
			_trajectory = null;
			_updateTrajectory = false;
		}

		_nStep = 0;
		_status = AdaptiveSwimmer.SWIM_SWIMMING;

		if (_initialValues != null) {
			_initialValues.charge = 0;
			_initialValues.xo = Double.NaN;
			_initialValues.yo = Double.NaN;
			_initialValues.zo = Double.NaN;
			_initialValues.p = Double.NaN;
			_initialValues.theta = Double.NaN;
			_initialValues.phi = Double.NaN;
		}
	}

	/**
	 * Set the swimZ sign, +1 if zf > zo otherwise -1
	 * @param sign the swimZ sign
	 */
	public void setSign(int sign) {
		_sign = sign;
	}
	
	/**
	 * Get the swimZ sign, +1 if zf > zo otherwise -1
	 * @return the swimZ sign
	 */
	public int getSign() {
		return _sign;
	}
	
	/**
	 * Get the current value of the independent variable z
	 * @return the current value of z in cm
	 */
	public double getZ() {
		return _z;
	}
	
	/**
	 * Set the current value of the independent variable z
	 * @param z the current value of z in cm
	 */
	public void setZ(double z) {
		_z = z;
	}
	
	/**
	 * Set the current state vector, usually [x, y, tx, ty]
	 * where x, y are in cm
	 * @param u the new current state vector
	 */
	public void setU(double[] u) {
		for (int i = 0; i < 4; i++) {
			_u[i] = u[i];
		}
	}
	
	/**
	 * Gt the current state vector, usually [x, y, tx, ty]
	 * where x, y are in cm
	 * @return the current state vector
	 */
	public double[] getU() {
		return _u;
	}


	/**
	 * Get the integer charge
	 * @return the integer charge
	 */
	public int getCharge() {
		return _charge;
	}
	
	/**
	 * Get the momentum in GeV/c
	 * @return the momentum in GeV/c
	 */
	public double getMomentum() {
		return _p;
	}

	/**
	 * Checks whether a trajectory should be updated
	 * (by a stopper). Two things must be true, the _updateTrajectory
	 * should be true (updating not pause) and we actually have a trajectory
	 * object.
	 * @return true if the trajectory should be updated by a stopper
	 */
	public boolean shouldUpdateTrajectory() {
		return _updateTrajectory && (_trajectory != null);
	}

	/**
	 * Get the trajectory
	 * @return the trajectory (might be <code>null</code>
	 */
	public SwimZTrajectory getTrajectory() {
		return _trajectory;
	}

	
	/**
	 * Set the status of the swim
	 * @param status the status of the swim
	 */
	public void setStatus(int status) {
		_status = status;
	}

	/**
	 * Get the status of the swim
	 * @return the status
	 */
	public int getStatus() {
		return _status;
	}
}
