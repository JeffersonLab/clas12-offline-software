package cnuphys.swim;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Random;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFields;
import cnuphys.rk4.RungeKuttaException;

public class Swimming {

	// List of SwimTrajectorylisteners
	private static EventListenerList _listenerList;

	// the mc trajectories
	private static Vector<SwimTrajectory> _mcTrajectories = new Vector<SwimTrajectory>();

	// the recon trajectories
	private static Vector<SwimTrajectory> _reconTrajectories = new Vector<SwimTrajectory>();

	/**
	 * Clear all the mc trajectories.
	 */
	public static void clearMCTrajectories() {
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
	public static Vector<SwimTrajectory> getMCTrajectories() {
		return _mcTrajectories;
	}

	/**
	 * Get all the cached recon trajectories
	 * 
	 * @return all the cached recon trajectories
	 */
	public static Vector<SwimTrajectory> getReconTrajectories() {
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
						.format("R = [%8.5f, %8.5f, %8.5f] |R| = %7.4f m\nP = [%7.4e, %7.4e, %7.4e] |P| =  %9.6e GeV/c",
								Q[0], Q[1], Q[2], R, P * Q[3], P * Q[4], P
										* Q[5], P));
		System.out.println("norm (should be 1): " + norm);
		System.out.println("--------------------------------------\n");
	}

}
