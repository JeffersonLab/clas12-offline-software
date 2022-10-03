package cnuphys.adaptiveSwim;

import java.io.PrintStream;

import cnuphys.adaptiveSwim.geometry.AGeometric;
import cnuphys.adaptiveSwim.geometry.Point;
import cnuphys.magfield.FastMath;
import cnuphys.swim.SwimTrajectory;

public class AdaptiveSwimResult {

	//the current state vector
	private final double[] _u;

	//the number of integration steps
	private int _nStep;

	//the current path length
	private double _s;

	//a status, one of the AdaptiveSwimmer class constants
	private int _status;

	//optionally holds a trajectory of [x, y, z, tx, ty, tz] (coords in meters)
	private SwimTrajectory _trajectory;

	//the initial values in the usual variables
	//x, y, z, p, theta, phi
	private InitialValues _initialValues;

	//some stoppers (e.g. plane) will use this when they interpolate to an intersection
	private AdaptiveSwimIntersection _intersection;

	//whether a trajectory is created
	private boolean _saveTrajectory;

	//this can be used to pause updating the trajectory
	private boolean _updateTrajectory;


	/**
	 * Create a container for the swim results for default of 6D state vector
	 * @param saveTrajectory if true, we will save the trajectory
	 */
	public AdaptiveSwimResult(boolean saveTrajectory) {
		_u = new double[AdaptiveSwimmer.DIM];
		_saveTrajectory = saveTrajectory;
		reset();
	}

	/**
	 * Reset so that the object can be reused.
	 */
	public void reset() {

		for (int i = 0; i < AdaptiveSwimmer.DIM; i++) {
			_u[i] = Double.NaN;
		}

		if (_saveTrajectory) {
			_trajectory = new SwimTrajectory();
			_updateTrajectory = true;
		}
		else {
			_trajectory = null;
			_updateTrajectory = false;
		}

		_nStep = 0;
		_s = 0;
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

		if (_intersection != null) {
			_intersection.reset();
		}

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
	 * Get the flag which tells us that updating is "on".
	 * @return true if updating is active
	 */
	public boolean getUpdateTrajectory() {
       return _updateTrajectory;
	}

	/**
	 * Set whether updating is active. This can be used
	 * to pause updating.
	 * @param update the new value of the flag.
	 */
	public void setUpdateTrajectory(boolean update) {
		_updateTrajectory = update;
	}

	/**
	 * Does this result hold a trajectory?
	 * @return <code>true</code> if there is a trajectory
	 */
	public boolean hasTrajectory() {
		return _trajectory != null;
	}

	/**
	 * Get the trajectory
	 * @return the trajectory (might be <code>null</code>
	 */
	public SwimTrajectory getTrajectory() {
		return _trajectory;
	}

	/**
	 * Get the current state vector,[x, y, x, px/p, py/p, pz/p]
	 * where x, y, z are in meters.
	 * To get the augmented, assume the trajectory was created, use
	 * getLastTrajectoryPoint.
	 * @return the current state vector, always with six elements.
	 */
	public double[] getU() {
		return _u;
	}
	
	/**
	 * Get the current state (i.e., last) vector,[x, y, x, px/p, py/p, pz/p]
	 * where x, y, z are in meters.
	 * To get the augmented, assume the trajectory was created, use
	 * getLastTrajectoryPoint.
	 * @deprecated Use {@link AdaptiveSwimResult#getU} instead.
	 * @return the current state vector, always with six elements.
	 */
	public double[] getUf() {
		return _u;
	}

	/**
	 * Gets the last trajectory point if the trajectory was saved.
	 * This should be augmented with pathlength and bdl in indices 6 and 7.
	 * Otherwise return the usual six element final state vector.
	 * @return last trajectory point
	 */
	public double[] getLastTrajectoryPoint() {
		if (_trajectory == null) {
			return getU();
		}
		else {
			//augmented
			return _trajectory.lastElement();
		}
	}

	/**
	 * Set the current state vector, usually [x, y, x, px/p, py/p, pz/p]
	 * where x, y, z are in meters
	 * @param u the new current state vector
	 */
	public void setU(double[] u) {
		for (int i = 0; i < AdaptiveSwimmer.DIM; i++) {
			_u[i] = u[i];
		}
	}

	/**
	 * Get the number of steps of the swim
	 * @return the number of steps
	 */
	public int getNStep() {
		return _nStep;
	}


	/**
	 * Set the number of steps of the swim
	 * @param nStep the number of steps
	 */
	public void setNStep(int nStep) {
		_nStep = nStep;
	}


	/**
	 * Get the path length of the swim
	 * @return the path length in meters
	 */
	public double getS() {
		return _s;
	}

	/**
	 * Get the path length of the swim
	 * @deprecated Use {@link AdaptiveSwimResult#getS} instead.
	 * @return the path length in meters
	 */
	public double getFinalS() {
		return _s;
	}

	/**
	 * Set the path length of the swim
	 * @param s the path length in meters
	 */
	public void setS(double s) {
		_s = s;
	}
	
    /**
	 * Set the path length of the swim
     * @deprecated Use {@link AdaptiveSwimResult#setS} instead.
     * @param s the path length in meters
     */
	public void setFinalS(double s) {
		_s = s;		
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
	
	/**
	 * Get the final value of rho
	 * @return the final value of rho
	 */
	public double getFinalRho() {
		return Math.hypot(_u[0], _u[1]);
	}

	/**
	 * Set the initial values
	 * @param q The integer charge
	 * @param xo The x coordinate of the vertex in meters
	 * @param yo The y coordinate of the vertex in meters
	 * @param zo The z coordinate of the vertex in meters
	 * @param p The momentum in GeV/c
	 * @param theta The polar angle in degrees
	 * @param phi The azimuthal angle in degrees
	 */
	public void setInitialValues(int q, double xo, double yo, double zo, double p, double theta, double phi) {
		if (_initialValues == null) {
			_initialValues = new InitialValues();
		}
		_initialValues.charge = q;
		_initialValues.xo = xo;
		_initialValues.yo = yo;
		_initialValues.zo = zo;
		_initialValues.p = p;
		_initialValues.theta = theta;
		_initialValues.phi = phi;
	}



	/**
	 * A string containing the initial valies
	 * @return
	 */
	public String initialValuesString() {
		InitialValues v = _initialValues;

		if (v == null) {
			return "";
		}

		String s = String.format("charge = %d\nvertex = [%-10.7f, %-10.7f, %-10.7f] m\np = %-10.7f GeV/c\ntheta = %-10.7f deg\nphi = %-10.7f deg\n-------\n", v.charge, v.xo, v.yo, v.zo, v.p, v.theta, v.phi);

		return "\n-----------\nInitial values:\n"  + s;
	}


	/**
	 * Get the initial values
	 * @return the initial values
	 */
	public InitialValues getInitialValues() {
		return _initialValues;
	}


	/**
	 * Print the result to a print stream, such as System.out.
	 * Do not print the trajectory.
	 * @param ps the print stream
	 * @param message a header message
	 */
	public void printOut(PrintStream ps, String message) {
		printOut(ps, message, false);
	}


	/**
	 * Print the result to a print stream, such as System.out
	 * @param ps the print stream
	 * @param message a header message
	 * @param printTrajectory if true, will print the trajectory (if there is one)
	 */
	public void printOut(PrintStream ps, String message, boolean printTrajectory) {
		ps.println("\n" + message);
		ps.println(toString());


		if (printTrajectory && hasTrajectory()) {
			_trajectory.print(ps);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append(locationString());
		sb.append(momentumString());
		sb.append(infoString());

		if (hasTrajectory()) {
			sb.append("\nBDL: "+ _trajectory.getComputedBDL());
		}
		else {
			sb.append("\nNo trajectory.");
		}

		return initialValuesString() + sb.toString();
	}


	//the location
	private String locationString() {
		double x = _u[0];
		double y = _u[1];
		double z = _u[2];
		double r = Math.sqrt(x*x + y*y + z*z);

		double rho = Math.hypot(x, y);
		double phi = Math.toDegrees(Math.atan2(y, x));

		return String.format("R = [%10.7f, %10.7f, %10.7f] |R| = %10.7f m\n", x, y, z, r) +
				String.format("[phi, rho, z] = [%10.7f, %10.7f, %10.7f]\n", phi, rho, z);
	}

	//the momentum string
	private String momentumString() {

		double tx = _u[3];
		double ty = _u[4];
		double tz = _u[5];
		double norm = Math.sqrt(tx * tx + ty * ty + tz * tz);

		if (_initialValues != null) {
			double px = _initialValues.p * tx;
			double py = _initialValues.p * ty;
			double pz = _initialValues.p * tz;

			return String.format("Initial sector: %d    Final sector: %d\n", getInitialSector(), getSector())
					+ String.format("P = [%10.7e, %10.7e, %10.7e] |P| = %10.7e\n", px, py, pz, _initialValues.p)
					+ String.format("norm (should be 1): %9.7f\n", norm);
		} else {
			return String.format("t = [%10.7e, %10.7e, %10.7e]\n", tx, ty, tz)
					+ String.format("norm (should be 1): %9.7f\n", norm);
		}
	}

	private String infoString() {

		return "#steps = " + _nStep + " (has traj: " + hasTrajectory() + ") status: " + _status +
				String.format("   pathlength = %10.7f m\n", _s);
	}

	/**
	 * Get the final sector of the swim
	 * @return the final CLAS sector [1..6]
	 */
	public int getSector() {
		double x = _u[0];
		double y = _u[1];

		double phi = Math.toDegrees(Math.atan2(y, x));
		return AdaptiveSwimUtilities.getSector(phi);
	}

	/**
	 * Get the initial sector of the swim
	 * @return the final CLAS sector [1..6], or
	 * -1 if the initial values were not cached
	 */
	public int getInitialSector() {
		if (_initialValues == null) {
			return -1;
		}
		double x = _initialValues.xo;
		double y = _initialValues.yo;

		double phi = Math.toDegrees(Math.atan2(y, x));
		return AdaptiveSwimUtilities.getSector(phi);
	}


	/**
	 * get the final directional theta in degrees
	 * @return the final directional theta in degrees
	 */
	public double getTheta() {
		double theta = FastMath.acos2Deg(_u[5]);
		return theta;

	}

	/**
	 * get the final rho in meters
	 * @return the final rho in meters
	 */
	public double getRho() {
		double x = _u[0];
		double y = _u[1];
		return Math.hypot(x, y);
	}

	/**
	 * get the final directional phi in degrees
	 * @return the final directional phi in degrees
	 */
	public double getPhi() {
		double phi = FastMath.atan2Deg(_u[4], _u[3]);
		return phi;

	}


	/**
	 * Get the "initial values" that allows a retrace. This is used mostly
	 * for testing. Assumes the initial values have been set,
	 * @return the "initial values" that allows a retrace.
	 */
	public InitialValues retrace() {
		InitialValues iv =  getInitialValues();
		InitialValues revIv= new InitialValues();
		double uf[] = getU();

		double txf = uf[3];
		double tyf = uf[4];
		double tzf = uf[5];

		txf *= -1;
		tyf *= -1;
		tzf *= -1;

		revIv.charge = -iv.charge;
		revIv.p = iv.p;
		revIv.xo = uf[0];
		revIv.yo = uf[1];
		revIv.zo = uf[2];
		revIv.theta = FastMath.acos2Deg(tzf);
		revIv.phi = FastMath.atan2Deg(tyf, txf);

		return revIv;
	}

	/**
	 * Get the Euclidean distance between the last point of two results.
	 * Used for comparisons.
	 * @param res the other result
	 * @return the Euclidean distance between the last points.
	 */
	public double delDifference(AdaptiveSwimResult res) {

		double u[] = this.getLastTrajectoryPoint();
		double v[] = res.getLastTrajectoryPoint();

		double sum = 0;
		for (int i = 0 ; i < 3; i++) {
			double del = v[i] - u[i];
			sum += del*del;
		}


		return Math.sqrt(sum);

	}

	/**
	 * Compute the intersection of the left and right
	 * points with a geometric object using a linear interpolation.
	 * @param geom the object, such as a plane
	 */
	public void computeIntersection(AGeometric geom) {
		getIntersection().computeIntersection(geom);
	}

	/**
	 * Get the distance to the plane (should be 0)
	 * @return the distance to the plane in m
	 */
	public double getIntersectDistance() {
		return getIntersection().getIntersectDistance();
	}

	/**
	 * Get the intersection (interpolated) point on the plane.
	 * @return the intersection on the plane.
	 */
	public Point getIntersectionPoint() {
		return getIntersection().getIntersectionPoint();
	}

	/**
	 * Not all swim methods use this. It is fr those (e.g. planes) that
	 * want an estimate of the intersection. This will create the object
	 * if necessary.
	 * @return the intersection object
	 */
	public AdaptiveSwimIntersection getIntersection() {
		if (_intersection == null) {
			_intersection = new AdaptiveSwimIntersection();
		}
		return _intersection;
	}
	
	/**
	 * Get the status of the swim as a string
	 * @return the status of the swim as a string
	 */
	public String statusString() {
		String s = AdaptiveSwimmer.resultNames.get(_status);
		if (s == null) {
			s = "Unknown (" + _status + ")";
		}
		return s;
	}
	

}
