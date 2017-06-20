package cnuphys.tinyMS.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

import cnuphys.tinyMS.Environment.Environment;
import cnuphys.tinyMS.common.BadSocketException;
import cnuphys.tinyMS.common.ReaderThread;
import cnuphys.tinyMS.common.WriterThread;
import cnuphys.tinyMS.message.Header;
import cnuphys.tinyMS.message.Message;
import cnuphys.tinyMS.message.IMessageProcessor;
import cnuphys.tinyMS.message.MessageQueue;
import cnuphys.tinyMS.message.MessageType;
import cnuphys.tinyMS.message.Messenger;

/**
 * This is the base class for the actual client. This is different from the
 * ProxyClient, which is the server-side proxy for this client.
 * 
 * @author heddle
 * 
 */
public class DefaultClient extends Messenger implements IMessageProcessor, Runnable {

	// the underlying socket
	private Socket _socket;

	// an id returned by the server at first connection
	private int _id = -1;

	// the data input stream. Inbound message will be read from this stream.
	private DataInputStream _inputStream;

	// the data output stream
	private DataOutputStream _outputStream;

	// the inbound queue. Messages from the server will
	// end up here
	private MessageQueue _inboundQueue;

	// the outbound queue. Messages being sent to the server
	// will be placed here.
	private MessageQueue _outboundQueue;

	// the reader thread
	private ReaderThread _reader;

	// the writer thread
	private WriterThread _writer;

	// last ping from server
	private long _lastPing = -1L;

	// Descriptive user name. If null, actual login username will be used
	private String _clientName;

	// to avoid multiple calls to close
	private boolean _alreadyClosed = false;

	/** list of topics the client is subscribed to */
	private Vector<String> _subscriptions = new Vector<String>();

	/**
	 * Create a client that will connect to a {@link TinyMessageServer}.
	 * 
	 * @param clientName the name of the client
	 * @param hostName the name of the server machine
	 * @param port the port the server listens on
	 * @throws BadSocketException
	 */
	public DefaultClient(String clientName, String hostName, int port) throws BadSocketException {
		this(clientName, ClientSupport.getSocket(hostName, port));
	}
	
	/**
	 * Create a client that will connect to a {@link TinyMessageServer}.
	 * 
	 * @param clientName the name of the client. Since the host name and port are not given, this looks for 
	 * a server on the same local machine using one of the default ports.
	 * @throws BadSocketException
	 */
	public DefaultClient(String clientName) throws BadSocketException {
		this(clientName, null, -1);
	}

	/**
	 * Create a client that will connect to a {@link TinyMessageServer}. Most
	 * applications will not use this constructor directly (since you won't have
	 * a socket) but rather the static convenience methods in
	 * {@link ClientSupport}.
	 * 
	 * @param clientName
	 *            descriptive user name. If null, the actual login username will
	 *            be used.
	 * @param socket
	 *            the underlying socket
	 * @see ClientSupport
	 */
	public DefaultClient(String clientName, Socket socket) throws BadSocketException {

		if (socket == null) {
			throw new BadSocketException("null socket in Client constructor");
		}
		_clientName = (clientName != null) ? clientName : Environment.getInstance().getUserName();
		_socket = socket;
		
		// where to place message for transmission
		_outboundQueue = new MessageQueue(100, 20);

		// where inbound messages are dequeued for processing
		_inboundQueue = new MessageQueue(100, 20);

		// grab the streams, create the reader and writer and a thread to
		// dequeue messages.
		try {
			_inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			_outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			// to read inbound messages from server
			_reader = new ReaderThread(_socket, this);

			// to write (transmit) outbound messages to server
			_writer = new WriterThread(_socket, this);

			_reader.start();
			_writer.start();

			new Thread(this).start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		while (true) {
			// the dequeue method "waits" so no thread yielding is
			// necessary
			Message message = _inboundQueue.dequeue();
			message.process(this);
		}
	}

	/**
	 * This is a good place for clients to do custom initializations such as
	 * subscriptions. It is not necessary, but convenient. It is called at the
	 * end of the constructor.
	 */
	public void initialize() {
		// base implementation does nothing
	}

	/**
	 * Subscribe to a topic
	 * 
	 * @param topic
	 *            the topic to subscribe to
	 */
	public void subscribe(String topic) {
		if (Header.acceptableTopic(topic)) {

			topic = topic.trim().toLowerCase();
			if (!isSubscribed(topic)) {
				Message message = Message.createSubscribeMessage(getId(), topic);
				send(message);
			}
		}
	}

	/**
	 * Unsubscribe to a topic
	 * 
	 * @param topic
	 *            the topic to subscribe to
	 */
	public void unsubscribe(String topic) {
		if (Header.acceptableTopic(topic)) {

			topic = topic.trim().toLowerCase();
			if (isSubscribed(topic)) {
				Message message = Message.createUnsubscribeMessage(getId(), topic);
				send(message);
			}
		}
	}

	/**
	 * Return the id which should be > 0 as provided by the server.
	 * 
	 * @return the id provided by the server
	 */
	@Override
	public int getId() {
		return _id;
	}

	/**
	 * Set the client id
	 * 
	 * @param id
	 *            the client id
	 */
	protected void setId(int id) {
		_id = id;
	}

	/**
	 * Get the underlying socket
	 * 
	 * @return the underlying socket
	 */
	public Socket getSocket() {
		return _socket;
	}

	/**
	 * Get the data input stream. Messages will be read from this stream and
	 * placed on the inbound queue.
	 * 
	 * @return the data input stream
	 */
	@Override
	public DataInputStream getDataInputStream() {
		return _inputStream;
	}

	/**
	 * Get the data output stream. Messages will be removed from the outbound
	 * queue and sent on this stream.
	 * 
	 * @return the data output stream
	 */
	@Override
	public DataOutputStream getDataOutputStream() {
		return _outputStream;
	}

	/**
	 * A ReaderThread will be putting messages in this queue. They should be
	 * dequeued and processed,
	 * 
	 * @return the queue where we place inbound messages coming from the server.
	 */
	@Override
	public MessageQueue getInboundQueue() {
		return _inboundQueue;
	}

	/**
	 * Get the queue where we place outbound messages. A WriterThread will be
	 * grabbing the messages and sending them to their destination (to the
	 * server on the other end).
	 * 
	 * @return the queue where we place outbound messages ready for
	 *         transmission.
	 */
	@Override
	public MessageQueue getOutboundQueue() {
		return _outboundQueue;
	}

	/**
	 * Get an identifying name for the client
	 * 
	 * @return a name of the client
	 */
	@Override
	public String getClientName() {
		return _clientName;
	}

	/**
	 * This logs the client out after sending a message to the server.
	 */
	public void logout() {

		Message message = Message.createLogoutMessage(getId());
		send(message);
		_outboundQueue.setAccept(false);

		// try to wait for the message to be sent
		for (int i = 0; i < 5; i++) {
			if (_outboundQueue.isEmpty()) {
				System.out.println("Outbound queue is empty");
				break;
			} else {
				System.out.println("Outbound queue not empty");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}

		try {
			System.out.println("\n------\nlogout method called for " + getClientName() + "\n-----");
			close();
		} catch (IOException e) {
		}
	}

	/**
	 * Close the underlying socket.
	 * 
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		if (_alreadyClosed) {
			return;
		}
		_alreadyClosed = true;

		(new Throwable()).printStackTrace();
		
		System.out.println("\n------\nClosing " + getClientName() + "\n-----");
		_reader.stopReader();
		_writer.stopWriter();
		_socket.close();
		System.out.println("Socket closed for [Client " + getId() + "]");
	}

	/**
	 * Get the last ping time
	 * 
	 * @return the last ping time (-1 if never pinged)
	 */
	protected long getLastPing() {
		return _lastPing;
	}

	/**
	 * Set the last ping time
	 * 
	 * @param ping
	 *            the last ping time
	 */
	protected void setLastPing(long ping) {
		_lastPing = ping;
	}

	/**
	 * Check if the client's socket is closed
	 * 
	 * @return <code>true</code> if the clients socket id closed
	 */
	public boolean isClosed() {
		return (_socket == null) || _socket.isClosed();
	}

	/**
	 * Check whether this client subscribes to a topic
	 * 
	 * @param topic
	 *            the topic to subscribe to. This will be trimmed of white space
	 *            and converted to lower case, i.e., topics are NOT case
	 *            sensitive.
	 * @return <code>true</code> if this client is subscribed to the topic
	 */
	protected boolean isSubscribed(String topic) {
		if (topic != null) {
			topic = topic.trim().toLowerCase();
			if (topic.length() > 0) {
				return _subscriptions.contains(topic);
			}
		}
		return false;
	}

	/**
	 * Get the list of subscriptions that client subscribes to
	 * 
	 * @return the list of subscriptions
	 */
	protected Vector<String> getSubscriptions() {
		return _subscriptions;
	}

	/**
	 * Send a message
	 * 
	 * @param message
	 *            the message to send
	 */
	protected void send(Message message) {
		if (message != null) {
			if (_outboundQueue != null) {
				_outboundQueue.queue(message);
				try {
					Thread.sleep(1);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * A message is about to be farmed out to the appropriate handler. This
	 * allows you to take a peek at it first.
	 * 
	 * @param message
	 *            the message
	 */
	public void peekAtMessage(Message message) {
		// default: do nothing
	}

	// A logout message should be from client to server
	@Override
	public void processLogoutMessage(Message message) {
		System.out.println("It is rarely a good sign that a logout message arrives at a client.");
	}

	// A shutdown message arriving at the client means the server is
	// telling the client he will be logged out. Probably something of a
	// courtesy,
	// as part of the server shutting down gracefully.
	@Override
	public void processShutdownMessage(Message message) {
		System.out.println("!!! [" + getClientName() + "] " + " received a SHUTDOWN!");
		try {
			getOutboundQueue().setAccept(false);

			// give it time to send last message
			try {
				Thread.currentThread();
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// A handshake is the first message sent by the server.
	// From it I can get my Id.
	@Override
	public void processHandshakeMessage(Message message) {

		if (getId() > 0) {
			System.out.println("Client " + getId() + " already had a good Id. Should not have gotten a handshake.");
			return;
		}

		// now I can learn my id
		int id = message.getClientId();
		System.out.println("Client Received Handshake. Acquired client ID: " + id);
		setId(id);

		// add some environmental strings (and my user name)
		// then send it back
		message.setPayload(envStringArray());
		getOutboundQueue().queue(message);

		// call the client initialization
		initialize();

	}

	/**
	 * A ping has arrived from the server. Log the last ping time and send the
	 * message back.
	 * 
	 * @param message
	 *            the ping message
	 */
	@Override
	public void processPingMessage(Message message) {
		long ct = System.nanoTime();
		if (getLastPing() > 0) {
			long duration = ct - getLastPing();
//			String pdstr = String.format("[" + getClientName() + "] " + "Time since last ping: %7.3f ms",
//					duration / 1.0e6);
//			System.out.println(pdstr);
		}

		setLastPing(ct);

		// send it back
		getOutboundQueue().queue(message);
	}

	// NO_DATA, BYTE_ARRAY, SHORT_ARRAY, INT_ARRAY, LONG_ARRAY, FLOAT_ARRAY,
	// DOUBLE_ARRAY, STRING_ARRAY, STRING, SERIALIZED_OBJECT;

	/**
	 * This is a non-administrative message that has arrived from some other
	 * client. This is what every client will override to deal with messages.
	 * Only messages for which the client is subscribed will end up here.
	 */
	@Override
	public void processClientMessage(Message message) {
		if (message != null) {
			switch (message.getDataType()) {

			case NO_DATA:
				break;

			case BYTE_ARRAY:
				break;

			case SHORT_ARRAY:
				break;

			case INT_ARRAY:
				break;

			case LONG_ARRAY:
				break;

			case FLOAT_ARRAY:
				break;

			case DOUBLE_ARRAY:
				break;

			case STRING_ARRAY:
				break;

			case STRING:
				break;

			case SERIALIZED_OBJECT:
				break;

			case STREAMED:
				break;

			}
		}
	}

	/**
	 * Process a SERVERLOG message
	 * 
	 * @param message
	 *            the message to process
	 */
	@Override
	public void processServerLogMessage(Message message) {
		System.out.println("It is rarely a good sign that a logout message arrives at a client.");
	}

	/**
	 * Process a SUBSCRIBE message
	 * 
	 * @param message
	 *            the message to process
	 */
	@Override
	public void processSubscribeMessage(Message message) {
		String topic = message.getString();
		topic = topic.trim().toLowerCase();
		System.out.println(getClientName() + " subscribed to topic: " + topic);

		if (!isSubscribed(topic)) {
			getSubscriptions().add(topic);
		}
	}

	/**
	 * Process a UNSUBSCRIBE message
	 * 
	 * @param message
	 *            the message to process
	 */
	@Override
	public void processUnsubscribeMessage(Message message) {
		String topic = message.getString();
		topic = topic.trim().toLowerCase();
		System.out.println(getClientName() + " unsubscribed to topic: " + topic);

		if (isSubscribed(topic)) {
			getSubscriptions().remove(topic);
		}
	}

	// can be used as a filter
	@Override
	public boolean accept(Message message) {
		// handshakes are accepted and used to get my Id

		if (message.getMessageType() == MessageType.HANDSHAKE) {
			return true;
		}

		return true;
	}

	/**
	 * Get the environment string array, getting strings at these indices:<br>
	 * [0] the descriptive user name<br>
	 * [1] the login user name<br>
	 * [2] the os name<br>
	 * [3] the host name
	 * 
	 * @return the environment string array
	 */
	private String[] envStringArray() {
		String array[] = new String[5];

		Environment env = Environment.getInstance();

		array[0] = getClientName();
		array[1] = env.getUserName();
		array[2] = env.getOsName();
		array[3] = env.getHostName();
		array[4] = (_socket != null) ? "" + _socket.getLocalPort() : "0";
		return array;
	}

}
