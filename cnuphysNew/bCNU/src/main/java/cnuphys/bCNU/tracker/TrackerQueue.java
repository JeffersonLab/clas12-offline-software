/**
 * 
 */
package cnuphys.bCNU.tracker;

import java.util.Vector;

/**
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class TrackerQueue extends Vector<Runnable> {

	public TrackerQueue() {
		super(25);
	}

	/**
	 * Place a new Runnable job in the work queue.
	 * 
	 * @param r
	 *            the <code>Runnable</code> job.
	 */
	public synchronized void put(Runnable r) {
		if (r != null) {
			add(r);

			// wake up anyone waiting for a queue entry
			notifyAll();
		}
	}

	/**
	 * Place a new Runnable job in the work queue, removing anyone else already
	 * there.
	 * 
	 * @param r
	 *            the <code>Runnable</code> job.
	 */
	public synchronized void supercede(Runnable r) {

		removeAllElements();
		if (r != null) {
			add(r);

			// wake up anyone waiting for a queue entry
			notifyAll();
		}
	}

	/**
	 * Dequeue a <code>Runnable</code> job from the FIFO.
	 * 
	 * @return the <code>Runnable</code> job.
	 */
	public synchronized Runnable get() {
		if (isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return remove(0);
	}
}
