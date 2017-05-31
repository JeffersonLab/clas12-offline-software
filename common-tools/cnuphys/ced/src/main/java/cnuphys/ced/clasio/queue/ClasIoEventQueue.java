package cnuphys.ced.clasio.queue;

import java.util.Vector;

import org.jlab.io.base.DataEvent;

/**
 * A queue for holding events to be used in a wait-notify manner.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class ClasIoEventQueue extends Vector<DataEvent> {

	// specifies whether new events are accepted
	private boolean _accept = true;

	/**
	 * Create an EventQueue
	 */
	public ClasIoEventQueue() {
		super(1000, 100);
	}

	/**
	 * Queue an event. The add will notify threads that are waiting.
	 * 
	 * @param event
	 *            the event to queue
	 */
	public synchronized void queue(DataEvent event) {
		add(event);
	}

	/**
	 * The add, which puts the event in the queue and notifies and consumers
	 * that are waiting.
	 * 
	 * @param event
	 *            the event to queue
	 * @returns <code>true</true> as specified by JAVA.
	 */
	@Override
	public synchronized boolean add(DataEvent event) {
		if ((event != null) && _accept) {
			super.add(event);
			// notify any threads waiting for events
			notifyAll();
		}
		return true;
	}

	/**
	 * Determines whether queue will accept any new events.
	 * 
	 * @param accept
	 *            if <code>false</code>, will no longer queue events
	 */
	public void setAccept(boolean accept) {
		_accept = accept;
	}

	/**
	 * Dequeue an event. If queue is empty, wait.
	 * 
	 * @return a DataEvent event for processing.
	 */
	public synchronized DataEvent dequeue() {
		if (isEmpty()) {
			try {
				// wait until notified of arriving event
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}

		}
		// return first element (FIFO)
		return remove(0);
	}

}