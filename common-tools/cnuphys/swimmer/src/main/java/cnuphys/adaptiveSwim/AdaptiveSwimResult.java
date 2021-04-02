package cnuphys.adaptiveSwim;

import java.io.PrintStream;

import cnuphys.adaptiveSwim.test.InitialValues;
import cnuphys.magfield.FastMath;
import cnuphys.swim.SwimTrajectory;

public class AdaptiveSwimResult {
	
	//the final state vector
	private double[] _uf;
	
	//the number of integration steps
	private int _nStep;
	
	//the final path length
	private double _finalS;
	
	//a status, one of the class constants
	private int _status;
	
	//optionally holds a trajectory of [x, y, z, tx, ty, tz] (coords in meters)
	private SwimTrajectory _trajectory;
	
	//the initial values
	private InitialValues _initialValues;
	
	/**
	 * Create a container for the swim results for default of 6D state vector
	 * @param saveTrajectory if true, we will save the trajectory
	 */
	public AdaptiveSwimResult(boolean saveTrajectory) {
		this(6, saveTrajectory);
	}

	/**
	 * Create a container for the swim results
	 * @param dim the dimension of the system (probably 6)
	 * @param saveTrajectory if true, we will save the trajectory
	 */
	public AdaptiveSwimResult(int dim, boolean saveTrajectory) {
		_uf = new double[dim];
		
		if (saveTrajectory) {
			_trajectory = new SwimTrajectory();
		}
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
	 * Get the final state vector, usually [x, y, x, px/p, py/p, pz/p]
	 * where x, y, z are in meters. This final vector is never augmented.
	 * To get the augmented, assume the trajectory was created, use
	 * getLastTrajectoryPoint.
	 * @return the final state vector, always with six elements.
	 */
	public double[] getUf() {
		return _uf;
	}
	
	/**
	 * Gets the last trajectory point if the trajectory was saved.
	 * This should be augmented with pathlength and bdl in indices 6 and 7.
	 * Otherwise return the usual six element final state vector.
	 * @return last trajectory point
	 */
	public double[] getLastTrajectoryPoint() {
		if (_trajectory == null) {
			return getUf();
		}
		else {
			//augmented
			return _trajectory.lastElement();
		}
	}

	/**
	 * Set the final state vector, usually [x, y, x, px/p, py/p, pz/p]
	 * where x, y, z are in meters
	 * @param uf the final state vector
	 */
	public void setUf(double[] uf) {
		_uf = uf;
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
	 * Get the final path length of the swim
	 * @return the final path length in meters
	 */
	public double getFinalS() {
		return _finalS;
	}


	/**
	 * Set the final path length of the swim
	 * @param finalS the final path length in meters
	 */
	public void setFinalS(double finalS) {
		_finalS = finalS;
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
		_initialValues.xo = xo;
		_initialValues.yo = yo;
		_initialValues.zo = zo;
		_initialValues.p = p;
		_initialValues.theta = theta;
		_initialValues.phi = phi;
	}
	
	/**
	 * Set the initial values
	 * @param iv the source
	 */
	public void setInitialValies(InitialValues iv) {
		_initialValues = new InitialValues(iv);
	}
	
	/**
	 * Used to compare to old swimmer
	 * @param traj trajectory (probably from old swimmer)
	 */
	public void setTrajectory(SwimTrajectory traj) {
		_trajectory = traj;
		
		_nStep = traj.size();
		
		double last[] = traj.lastElement();
		for (int i = 0; i < 6; i++) {
			_uf[i] = last[i];
		}
		
		if (last.length > 6) {
			_finalS = last[6];
		}
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

		return sb.toString();
	}
	
	//the location
	private String locationString() {
		double x = _uf[0];
		double y = _uf[1];
		double z = _uf[2];
		double r = Math.sqrt(x*x + y*y + z*z);
		
		double rho = Math.hypot(x, y);
		double phi = Math.toDegrees(Math.atan2(y, x));
		
		return String.format("R = [%10.7f, %10.7f, %10.7f] |R| = %10.7f m\n", x, y, z, r) +
				String.format("[phi, rho, z] = [%10.7f, %10.7f, %10.7f]\n", phi, rho, z);
	}
	
	//the momentum string
	private String momentumString() {

		double tx = _uf[3];
		double ty = _uf[4];
		double tz = _uf[5];
		double norm = Math.sqrt(tx * tx + ty * ty + tz * tz);

		if (_initialValues != null) {
			double px = _initialValues.p * tx;
			double py = _initialValues.p * ty;
			double pz = _initialValues.p * tz;

			return String.format("Initial sector: %d    Final sector: %d\n", getInitialSector(), getFinalSector())
					+ String.format("P = [%10.7e, %10.7e, %10.7e] |P| = %10.7e\n", px, py, pz, _initialValues.p)
					+ String.format("norm (should be 1): %9.7f\n", norm);
		} else {
			return String.format("t = [%10.7e, %10.7e, %10.7e]\n", tx, ty, tz)
					+ String.format("norm (should be 1): %9.7f\n", norm);
		}
	}
	
	private String infoString() {
		
		return "#steps = " + _nStep + " (has traj: " + hasTrajectory() + ") status: " + _status + 
				String.format("   pathlength = %10.7f m\n", _finalS);
	}
	
	/**
	 * Get the final sector of the swim
	 * @return the final CLAS sector [1..6]
	 */
	public int getFinalSector() {
		double x = _uf[0];
		double y = _uf[1];
		
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
	 * get the final theta in degrees
	 * @return the final theta in degrees
	 */
	public double getFinalTheta() {
		double x = _uf[0];
		double y = _uf[1];
		double z = _uf[2];
		double r = Math.sqrt(x*x + y*y + z*z);
		
		if (r < 1.0e-10) {
			return 0;
		}
		
		return Math.toDegrees(Math.acos(z/r));

	}
	
	/**
	 * get the final phi in degrees
	 * @return the final phi in degrees
	 */
	public double getFinalPhi() {
		double x = _uf[0];
		double y = _uf[1];
		return Math.toDegrees(Math.atan2(y, x));

	}
	
	/**
	 * Used for testing z swim.
	 * @param zTarg the target z (m)
	 * @return the signed difference zFinal -  zTarg
	 */
	public double finalDeltaZ(double zTarg) {
		return _uf[2] - zTarg;
	}
	
	/**
	 * Get the "initial values" that allows a retrace. This is used mostly
	 * for testing. Assumes the initial values have been set,
	 * @return the "initial values" that allows a retrace.
	 */
	public InitialValues retrace() {
		InitialValues iv =  getInitialValues();
		InitialValues revIv= new InitialValues();
		double uf[] = getUf();
		
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
}
