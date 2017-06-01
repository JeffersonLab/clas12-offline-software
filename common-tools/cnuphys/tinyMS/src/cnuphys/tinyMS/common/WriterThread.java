package cnuphys.tinyMS.common;

import java.io.IOException;
import java.net.Socket;

import cnuphys.tinyMS.message.IMessenger;
import cnuphys.tinyMS.message.Message;

/**
 * This thread uses a IMessenger object to write (transmit) messages that it
 * dequeues from the Imessenger's outbound queue.
 * 
 * @author heddle
 * 
 */

public class WriterThread extends Thread {

	// the messenger
	private IMessenger _messenger;

	// used to kill the thread
	private volatile boolean _running = false;

	// the socket
	private Socket _socket;

	/**
	 * Create a writer thread. This thread will be used to transmit messages off
	 * a message queue.
	 * 
	 * @param socket
	 *            the underlying socket
	 * @param messenger
	 *            the messenger with the queue
	 * @throws IOException
	 */
	public WriterThread(Socket socket, IMessenger messenger) throws IOException {
		super("tinyMS writer thread");
		_socket = socket;
		_messenger = messenger;
	}

	/**
	 * Start the thread
	 */
	@Override
	public void start() {
		_running = true;
		super.start();
	}

	/**
	 * The threads run method
	 */
	@Override
	public void run() {
		while (_running) {
			try {
				// dequeue will wait until there is a message
				Message message = _messenger.getOutboundQueue().dequeue();

				// did I get a good read? If so write it
				if (safeToUse(message)) {
					_messenger.writeMessage(message);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean safeToUse(Message message) {
		return (message != null) && (_socket != null) && (!_socket.isClosed());
	}

	/**
	 * Sets a flag to cause the thread to stop.
	 */
	// TODO improve, since this won't stop while reading
	public void stopWriter() {
		_running = false;
	}

}
