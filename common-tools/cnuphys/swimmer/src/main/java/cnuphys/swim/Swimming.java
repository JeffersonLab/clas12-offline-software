package cnuphys.swim;

import java.util.ArrayList;

import javax.swing.event.EventListenerList;

import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;

public class Swimming {

	// List of SwimTrajectorylisteners
	private static EventListenerList _listenerList;

	// the mc trajectories
	private static ArrayList<SwimTrajectory> _mcTrajectories = new ArrayList<SwimTrajectory>();

	// the recon trajectories
	private static ArrayList<SwimTrajectory> _reconTrajectories = new ArrayList<SwimTrajectory>();

	/**
	 * Clear all the mc trajectories.
	 */
	public static void clearMCTrajectories() {
//		System.err.println("CLEAR MC TRAJ");
//		(new Throwable()).printStackTrace();
		_mcTrajectories.clear();
		notifyListeners();
	}

	/**
	 * Clear all the recon trajectories.
	 */
	public static void clearReconTrajectories() {
		_reconTrajectories.clear();
		notifyListeners();
	}

	/**
	 * Get all the cached mc trajectories
	 * 
	 * @return all the cached mc trajectories
	 */
	public static ArrayList<SwimTrajectory> getMCTrajectories() {
		return _mcTrajectories;
	}

	/**
	 * Get all the cached recon trajectories
	 * 
	 * @return all the cached recon trajectories
	 */
	public static ArrayList<SwimTrajectory> getReconTrajectories() {
		return _reconTrajectories;
	}

	/**
	 * Add a trajectory to the mc collection
	 * 
	 * @param traj
	 *            the trajectory to add.
	 */
	public static void addMCTrajectory(SwimTrajectory traj) {
		_mcTrajectories.remove(traj);
		_mcTrajectories.add(traj);
		notifyListeners();
	}

	/**
	 * Add a trajectory to the recon collection
	 * 
	 * @param traj
	 *            the trajectory to add.
	 */
	public static void addReconTrajectory(SwimTrajectory traj) {
		_reconTrajectories.remove(traj);
		_reconTrajectories.add(traj);
		notifyListeners();
	}

	/**
	 * Remove a trajectory from the mc collection
	 * 
	 * @param traj
	 *            the trajectory to remove.
	 */
	public static void removeMCTrajectory(SwimTrajectory traj) {
		_mcTrajectories.remove(traj);
		notifyListeners();
	}

	/**
	 * Remove a trajectory from the recon collection
	 * 
	 * @param traj
	 *            the trajectory to remove.
	 */
	public static void removeReconTrajectory(SwimTrajectory traj) {
		_reconTrajectories.remove(traj);
		notifyListeners();
	}


	// notify listeners that the collection of trajectories has changed
	protected static void notifyListeners() {

		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// This weird loop is the bullet proof way of notifying all listeners.
		// for (int i = listeners.length - 2; i >= 0; i -= 2) {
		// order is flipped so it goes in order as added
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == SwimTrajectoryListener.class) {
				SwimTrajectoryListener listener = (SwimTrajectoryListener) listeners[i + 1];
				listener.trajectoriesChanged();
			}

		}
	}

	/**
	 * Add a magnetic field change listener
	 * 
	 * @param SwimTrajectoryListener
	 *            the listener to add
	 */
	public static void addSwimTrajectoryListener(
			SwimTrajectoryListener SwimTrajectoryListener) {

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		// avoid adding duplicates
		_listenerList.remove(SwimTrajectoryListener.class,
				SwimTrajectoryListener);
		_listenerList.add(SwimTrajectoryListener.class, SwimTrajectoryListener);
	}

	/**
	 * Remove a SwimTrajectoryListener.
	 * 
	 * @param SwimTrajectoryListener
	 *            the SwimTrajectoryListener to remove.
	 */

	public static void removeSwimTrajectoryListener(
			SwimTrajectoryListener SwimTrajectoryListener) {

		if ((SwimTrajectoryListener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(SwimTrajectoryListener.class,
				SwimTrajectoryListener);
	}


	public static void printSummary(String message, int nstep, double momentum,
			double Q[], double hdata[]) {
		System.out.println(message);
		double R = Math.sqrt(Q[0] * Q[0] + Q[1] * Q[1] + Q[2] * Q[2]);
		double norm = Math.sqrt(Q[3] * Q[3] + Q[4] * Q[4] + Q[5] * Q[5]);
		double P = momentum * norm;

		System.out.println("Number of steps: " + nstep);

		if (hdata != null) {
			System.out.println("min stepsize: " + hdata[0]);
			System.out.println("avg stepsize: " + hdata[1]);
			System.out.println("max stepsize: " + hdata[2]);
		}
		System.out
				.println(String
						.format("R = [%9.6f, %9.6f, %9.6f] |R| = %9.6f m\nP = [%9.6e, %9.6e, %9.6e] |P| =  %9.6e GeV/c",
								Q[0], Q[1], Q[2], R, P * Q[3], P * Q[4], P
										* Q[5], P));
		System.out.println("norm (should be 1): " + norm);
		System.out.println("--------------------------------------\n");
	}
	
	
	
	public static SwimTrajectory testSwim(int opt) {
		int charge = -1;
		
		//positions in METERS
		double xo = 0;
		double yo = 0;
		double zo = 0;
		double momentum = 1.0;
		double theta = 30;
		double phi = 0;
		double accuracy = 1.0e-5;  //ten microns
		double rMax = 7.0;
		double stepSize = 5e-3; // m
		double maxPathLen = 8.0; // m
		double hdata[] = new double[3];
		double ztarget = 5.0; //meters

		
		if (opt == 1) {
			System.out.println("\nSWIMMER 1");
		}
		Swimmer swimmer = new Swimmer(MagneticFields.getInstance().getActiveField());

		SwimTrajectory traj = null;
		try {
			traj = swimmer.swim(charge, xo, yo, zo, momentum, theta, phi, ztarget, accuracy, rMax, maxPathLen, stepSize,
					Swimmer.CLAS_Tolerance, hdata);

			if (opt > 0) {
				double lastY[] = traj.lastElement();
				printSummary("\nresult from adaptive stepsize method with storage and Z cutoff at " + ztarget,
						traj.size(), momentum, lastY, hdata);
			}

		}
		catch (RungeKuttaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return traj;
	}
	
	public static SwimTrajectory testSwim2(int opt) {
		int charge = -1;
		
		//positions in METERS
		double xo = 0;
		double yo = 0;
		double zo = 0;
		double momentum = 1.0;
		double theta = 30;
		double phi = 0;
		double accuracy = 1.0e-5;  //ten microns
		double stepSize = 5e-3; // m
		double maxPathLen = 8.0; // m
		double hdata[] = new double[3];
		double ztarget = 5.0; //meters

		
		if (opt == 1) {
			System.out.println("\nSWIMMER 2");
		}
		Swimmer2 swimmer = new Swimmer2(MagneticFields.getInstance().getActiveField());

		SwimTrajectory traj = null;
		try {
			traj = swimmer.swim(charge, xo, yo, zo, momentum, theta, phi, ztarget, accuracy, maxPathLen, stepSize,
					Swimmer.CLAS_Tolerance, hdata);

			if (opt > 0) {
				double lastY[] = traj.lastElement();
				printSummary("\nresult from adaptive stepsize method with storage and Z cutoff at " + ztarget,
						traj.size(), momentum, lastY, hdata);
			}

		}
		catch (RungeKuttaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return traj;
	}

	public static int testSwim3(int opt) {
		int charge = -1;
		
		//positions in METERS
		double xo = 0;
		double yo = 0;
		double zo = 0;
		double momentum = 1.0;
		double theta = 30;
		double phi = 0;
		double accuracy = 1.0e-5;  //ten microns
		double stepSize = 5e-3; // m
		double maxPathLen = 8.0; // m
		double hdata[] = new double[3];
		double ztarget = 5.0; //meters
		
		
		double maxStepSize = 0.4; //meters

		double finalState[] = new double[6];
		
		if (opt == 1) {
			System.out.println("\nSWIMMER 3");
		}
		Swimmer2 swimmer = new Swimmer2(MagneticFields.getInstance().getActiveField());

		int nStep= 0;
		
		try {
			nStep = swimmer.swim(charge, xo, yo, zo, momentum, theta, phi, ztarget, accuracy, maxPathLen, stepSize,
					maxStepSize, Swimmer.CLAS_Tolerance, hdata, finalState);

			if (opt > 0) {
				printSummary("\nresult from adaptive stepsize method with storage and Z cutoff at " + ztarget,
						nStep, momentum, finalState, hdata);
			}

		}
		catch (RungeKuttaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nStep;
	}
	public static void main(String arg[]) {
		
		MagneticFields.getInstance().initializeMagneticFields();
		MagneticFields.getInstance().setActiveField(FieldType.TORUS);
		// swimmer = new Swimmer(MagneticFields.getInstance().getActiveField());

		System.out.println("Active Field Description: " + MagneticFields.getInstance().getActiveFieldDescription());

		FastMath.setMathLib(FastMath.MathLib.SUPERFAST);

		testSwim(1);
		testSwim2(1);
		testSwim3(1);

		for (int i = 0; i < 100; i++) {
			testSwim(0);
		}

		long time = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			testSwim(0);
		}
		time = System.currentTimeMillis() - time;
		testSwim(1);

		System.out.println("\n*** TIME SWIMMER 1: " + time);
		
		for (int i = 0; i < 100; i++) {
			testSwim2(0);
		}

		time = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			testSwim2(0);
		}
		time = System.currentTimeMillis() - time;
		testSwim2(1);

		System.out.println("\n*** TIME SWIMMER 2: " + time);
		
		for (int i = 0; i < 100; i++) {
			testSwim3(0);
		}

		time = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			testSwim3(0);
		}
		time = System.currentTimeMillis() - time;
		testSwim3(1);

		System.out.println("\n*** TIME SWIMMER 3: " + time);

	}

}
