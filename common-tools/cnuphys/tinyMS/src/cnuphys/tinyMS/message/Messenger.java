package cnuphys.tinyMS.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

public abstract class Messenger implements IMessenger {

	/**
	 * Get the Id of the messenger
	 * 
	 * @return the Id of the messenger
	 */
	public abstract int getId();

	/**
	 * Get the data input stream. Messages will be read from this stream and
	 * placed on an inbound queue.
	 * 
	 * @return the data input stream
	 */
	@Override
	public abstract DataInputStream getDataInputStream();

	/**
	 * Get the data output stream. Messages will be pulled from the outbound
	 * queue and sent on this stream.
	 * 
	 * @return the data output stream
	 */
	@Override
	public abstract DataOutputStream getDataOutputStream();

	/**
	 * Get the queue where we grab inbound messages. One or more ReaderThreads
	 * will be putting messages in this queue. They need to be dequeued and
	 * processed,
	 * 
	 * @return the queue where we place inbound messages
	 */
	@Override
	public abstract MessageQueue getInboundQueue();

	/**
	 * Get the queue where we place outbound messages. A WriterThread will be
	 * grabbing the messages and sending them to their destination.
	 * 
	 * @return the queue where we place outbound messages ready for
	 *         transmission.
	 */
	@Override
	public abstract MessageQueue getOutboundQueue();

	/**
	 * Send a message to its destination using the outbound stream.
	 * 
	 * @param message
	 *            the message to send
	 */
	@Override
	public void writeMessage(Message message) throws IOException {
		try {
			message.writeMessage(getDataOutputStream());
		} catch (Exception e) {
			System.err.println("Exception in write Message Messenger class " + getClass().getName());
			e.printStackTrace();
		}

	}

	/**
	 * Read a message from the inbound stream.
	 * 
	 * @return the message that was read.
	 */
	@Override
	public Message readMessage() throws SocketException, EOFException, IOException {
		return Message.readMessage(getDataInputStream());
	}
	
}
