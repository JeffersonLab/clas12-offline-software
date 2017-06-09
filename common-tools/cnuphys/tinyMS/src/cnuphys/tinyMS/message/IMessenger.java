package cnuphys.tinyMS.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

public interface IMessenger {

	/**
	 * Get an identifying name
	 * 
	 * @return a name of the messenger
	 */
	public String getClientName();

	/**
	 * Get the data input stream. Messages will be read from this stream and
	 * placed on an inbound queue.
	 * 
	 * @return the data input stream
	 */
	public DataInputStream getDataInputStream();

	/**
	 * Get the data output stream. Messages will be pulled from the outbound
	 * queue and sent on this stream.
	 * 
	 * @return the data output stream
	 */
	public DataOutputStream getDataOutputStream();

	/**
	 * Get the queue where we grab inbound messages. One or more ReaderThreads
	 * will be putting messages in this queue. They need to be dequeued and
	 * processed,
	 * 
	 * @return the queue where we place inbound messages
	 */
	public MessageQueue getInboundQueue();

	/**
	 * Get the queue where we place outbound messages. A WriterThread will be
	 * grabbing the messages and sending them to their destination.
	 * 
	 * @return the queue where we place outbound messages ready for
	 *         transmission.
	 */
	public MessageQueue getOutboundQueue();

	/**
	 * Send a message to its destination using the outbound stream.
	 * 
	 * @param message
	 *            the message to send
	 */
	public void writeMessage(Message message) throws IOException;

	/**
	 * Read a message from the inbound stream.
	 * 
	 * @return the message that was read.
	 */
	public Message readMessage() throws SocketException, EOFException, IOException;

	/**
	 * Close down the messenger
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;
}
