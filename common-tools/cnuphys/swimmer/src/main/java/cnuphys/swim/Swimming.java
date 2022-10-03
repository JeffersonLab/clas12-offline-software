package cnuphys.swim;

import java.util.ArrayList;

import javax.swing.event.EventListenerList;

import cnuphys.lund.SwimTrajectoryListener;

public class Swimming {

	// List of SwimTrajectorylisteners
	private static EventListenerList _listenerList;

	// the mc trajectories
	private static ArrayList<SwimTrajectory> _mcTrajectories = new ArrayList<>();

	// the recon trajectories
	private static ArrayList<SwimTrajectory> _reconTrajectories = new ArrayList<>();

	// auxilliary (like CNF) trajectories
	private static ArrayList<SwimTrajectory> _auxTrajectories = new ArrayList<>();


	private static boolean _notifyOn = true;

	/**
	 * Set whether we notify listeners. Might turn off temporarily to avoid multiple
	 * notifications.
	 *
	 * @param notifyOn the flag.
	 */
	public static void setNotifyOn(boolean notifyOn) {
		_notifyOn = notifyOn;
	}

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
	 * Clear all the aux trajectories.
	 */
	public static void clearAuxTrajectories() {
		_auxTrajectories.clear();
		notifyListeners();
	}


	/**
	 * Clear all trajectories
	 */
	public static void clearAllTrajectories() {
		_notifyOn = false;
		clearMCTrajectories();
		clearReconTrajectories();
		clearAuxTrajectories();
		_notifyOn = true;
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
	 * Get all the cached aux trajectories
	 *
	 * @return all the cached aux trajectories
	 */
	public static ArrayList<SwimTrajectory> getAuxTrajectories() {
		return _auxTrajectories;
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

	/**
	 * Remove a trajectory from the aux collection
	 *
	 * @param traj the trajectory to remove.
	 */
	public static void removeAuxTrajectory(SwimTrajectory traj) {
		_auxTrajectories.remove(traj);
		notifyListeners();
	}


	// notify listeners that the collection of trajectories has changed
	public static void notifyListeners() {

		if (!_notifyOn || (_listenerList == null)) {
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



}
