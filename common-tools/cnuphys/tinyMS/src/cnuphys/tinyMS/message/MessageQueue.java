package cnuphys.tinyMS.message;

import java.util.Vector;

/**
 * A queue for holding message data
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class MessageQueue extends Vector<Message> {

	// specifies whether new messages are accepted
	private boolean _accept = true;

	/**
	 * Create a MessageQueue
	 * 
	 * @param capacity
	 *            the original capacity
	 * @param increment
	 *            the capacity increment
	 */
	public MessageQueue(int capacity, int increment) {
		super(capacity, increment);
	}

	/**
	 * Queue a MessageData object. Notify threads that are waiting.
	 * 
	 * @param message
	 *            the data to queue
	 */
	public synchronized void queue(Message message) {
		add(message);
	}

	@Override
	public synchronized boolean add(Message message) {
		if (message == null) {
			return true;
		}

		if (!_accept) {
			return true;
		}

		boolean b = super.add(message);

		// notify any threads waiting for data
		notifyAll();
		return b;
	}

	/**
	 * Determines whether queue will accept any new messages.
	 * 
	 * @param accept
	 *            if false, will no longer queue messages
	 */
	public void setAccept(boolean accept) {
		_accept = accept;
	}

	/**
	 * Dequeue a MessageData object. If queue is empty, wait.
	 * 
	 * @return a MessageData object for processing.
	 */
	public synchronized Message dequeue() {
		if (isEmpty()) {
			try {
				// wait until notified of arriving message
				wait();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}

		}
		// return first element
		return remove(0);
	}

}
