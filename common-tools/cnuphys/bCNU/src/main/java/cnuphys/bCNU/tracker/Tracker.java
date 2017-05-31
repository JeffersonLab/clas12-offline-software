/**
 * 
 */
package cnuphys.bCNU.tracker;

import javax.swing.event.EventListenerList;

/**
 * Trackers are work threads. A queue is maintained. They are especially useful
 * for jobs that "supercede." For example, repainting. If there is already a
 * repaint in the queue, we presumably want to remove it when adding a new one.
 * 
 * @author heddle
 */
public class Tracker extends Thread {

	/**
	 * Listener for tracker events
	 */
	private EventListenerList listenerList = null;

	/**
	 * A flag to indicate whether this tracker is running.
	 */
	private boolean running = false;

	/**
	 * The job queue
	 */
	private TrackerQueue jobQueue;

	/**
	 * Create a tracker.
	 * 
	 * @param jobQueue
	 */
	public Tracker(TrackerQueue jobQueue) {
		this.jobQueue = jobQueue;
	}

	/**
	 * The run method. It will keep trying to deque a new job. If none are
	 * available, the thread will stop (because of the wait in the jobQueue's
	 * get method. When a job is added, this thread will wake up because of the
	 * notify call in the jobQueue's put method.
	 */
	@Override
	public void run() {
		while (running) {
			Runnable runnable = jobQueue.get();
			if (runnable != null) {
				runnable.run();
			}

			// notify listeners that a run completed

			notifyTrackerListeners(runnable,
					TrackerEvent.TrackerEventType.RUNCOMPLETED);

			// if empty, notify listeners that all runs completed

			if (jobQueue.isEmpty()) {
				notifyTrackerListeners(null,
						TrackerEvent.TrackerEventType.ALLRUNSCOMPLETED);
			}
		}
	}

	/**
	 * Notify interested parties that a tracker event occurred.
	 * 
	 * @param runnable
	 *            the runnable that finished, or null if all finished.
	 * @param type
	 *            the type of event.
	 */
	public void notifyTrackerListeners(Runnable runnable,
			TrackerEvent.TrackerEventType type) {

		TrackerEvent te = new TrackerEvent(type, runnable);

		if (listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array

		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ITrackerListener.class) {
				((ITrackerListener) listeners[i + 1]).trackerEvent(te);
			}
		}
	}

	/**
	 * Add a TrackerListener.
	 * 
	 * @param tl
	 *            the TrackerListener to add.
	 */
	public void addTrackerListener(ITrackerListener tl) {

		if (tl == null) {
			return;
		}

		if (listenerList == null) {
			listenerList = new EventListenerList();
		}

		listenerList.add(ITrackerListener.class, tl);
	}

	/**
	 * Remove a TrackerListener.
	 * 
	 * @param tl
	 *            the TrackerListener to remove.
	 */

	public void removeTrackerListener(ITrackerListener tl) {

		if ((tl == null) || (listenerList == null)) {
			return;
		}

		listenerList.remove(ITrackerListener.class, tl);
	}

	/**
	 * Starts the thread. Does nothing if already running, as indicated by the
	 * run flag.
	 */
	@Override
	public void start() {
		if (running) {
			return;
		}
		running = true;
		super.start();
	}

	/**
	 * Stop this tracker, after its next round.
	 * 
	 */
	public void stopTracker() {
		notifyAll();
		running = false;
	}

}
