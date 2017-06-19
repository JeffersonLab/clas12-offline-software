package cnuphys.tinyMS.common;

import java.io.IOException;
import java.net.Socket;

import cnuphys.tinyMS.message.IMessenger;
import cnuphys.tinyMS.message.Message;

/**
 * This thread uses a IMessenger object to read messages, and then places the
 * messages on the IMessenger's inbound queue.
 * 
 * @author heddle
 * 
 */

public class ReaderThread extends Thread {

	// the messenger
	private IMessenger _messenger;

	// used to kill the thread
	private volatile boolean _running = false;

	// the socket
	private Socket _socket;

	/**
	 * Create a reader thread. This thread will be used to read messages off a
	 * message queue.
	 * 
	 * @param socket
	 *            the underlying socket
	 * @param messenger
	 *            the messenger with the queue
	 * @throws IOException
	 */
	public ReaderThread(Socket socket, IMessenger messenger) throws IOException {
		super("tinyMS reader thread");
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
				Message message = _messenger.readMessage();

				// did I get a good read? If so give it to the inbound queue
				if (safeToUse(message)) {
					_messenger.getInboundQueue().queue(message);
				}
			}
			catch (Exception e) {
//				e.printStackTrace();
//				try {
//				_messenger.close();
//				}
//				catch (IOException e1) {
//					e1.printStackTrace();
//				}
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
	public void stopReader() {
		_running = false;
	}

}
