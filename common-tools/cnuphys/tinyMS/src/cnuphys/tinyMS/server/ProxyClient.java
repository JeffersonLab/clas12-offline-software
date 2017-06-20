package cnuphys.tinyMS.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
/**
 * A ProxyClient is actually server side object. There is one of these objects
 * maintained by the server for each actual client. It holds the inbound and 
 * outbound steams for the client on the server side.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import cnuphys.tinyMS.Environment.DateString;
import cnuphys.tinyMS.common.ReaderThread;
import cnuphys.tinyMS.common.WriterThread;
import cnuphys.tinyMS.log.Log;
import cnuphys.tinyMS.message.Message;
import cnuphys.tinyMS.message.MessageQueue;
import cnuphys.tinyMS.message.Messenger;
import cnuphys.tinyMS.table.ConnectionTable;

public class ProxyClient extends Messenger {
	
	//log
	private Log _log = Log.getInstance();
	
	/** Remote client's descriptive name, e.g. "ced" */
	private String _clientName = "???";
	
	/** Remote client's user name, e.g. "heddle" */
	private String _userName = "???";

	/** Remote client's operating system */
	private String _osName = "???";
	
	/** Remote client's host name" */
	private String _hostName = "???";

    /** number of messages arriving at server */
	private long _messageCount = 0;
	
	/** the remote client's local port on the client machine */
	private int _localPort;
	
	/** list of topics the client is subscribed to */
	private Vector<String> _subscriptions = new Vector<String>();

		
	// time in seconds that a client has to get itself verified
	private static final int VERIFY_SECONDS = 30;

	// the next available remote client Id. These are assigned
	// sequentially starting at 1. It is assumed that no single
	// invocation of a server will survive 2.1 billion client connections!
	private static int NEXTID = 1;

	// the unique Id of this remote client
	private int _id;

	// the underlying socket
	private Socket _socket;

	// the stream used to write messages to the real client
	private DataOutputStream _outputStream;

	// the stream used to read messages from the real client
	private DataInputStream _inputStream;

	// if true, the client was verified by sending
	// the required handshake.
	private boolean _verified;

	// the reader thread for reading messages from the real client
	// the messages are placed in the server's shared inbound message
	// queue
	private ReaderThread _reader;

	// the writer thread for writing messages to the real client
	private WriterThread _writer;

	// outbound queue where server will place messages
	// ready for transmission. The writer thread will dequeue
	// them and send them to the real client
	private MessageQueue _outboundQueue;

	// the server
	private TinyMessageServer _server;

	// the system time of the last ping from this client
	private long _lastPing = -1;
	
	//the round trip time of the last ping
	private long _duration = -1;

	// to avoid multiple closings
	private boolean _alreadyClosed = false;

	/**
	 * Create an object that is the proxy for a remote client.
	 * 
	 * @param socket
	 * @throws IOException
	 */
	public ProxyClient(Socket socket) throws IOException {
		_socket = socket;
		_id = (NEXTID++);

		// start unverified. If the client is not verified
		// his messages (except the required handshake) will
		// be ignored and eventually he'll be killed.
		_verified = false;

		// to read inbound messages from the remote real client
		_reader = new ReaderThread(_socket, this);

		// to write (transmit) outbound messages to the
		// remote real client "self"
		_writer = new WriterThread(_socket, this);

		// where server will place message for transmission
		_outboundQueue = new MessageQueue(100, 20);

		_inputStream = new DataInputStream(new BufferedInputStream(_socket.getInputStream()));
		_outputStream = new DataOutputStream(new BufferedOutputStream(_socket.getOutputStream()));

		// start a timer which will kill the client if
		// not verified

		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				if (!_verified) {
					_log.warning("Closing unverified client [" + _id + "]");
					try {
						close();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				} // end not verified
			}

		};

		Timer timer = new Timer();
		// one time schedule
		timer.schedule(task, VERIFY_SECONDS * 1000L);

	}

	/**
	 * Start a ping timer to monitor the connection
	 */
	protected void startPingTimer() {
		TimerTask pingTask = new TimerTask() {

			@Override
			public void run() {
				if (_verified && !_alreadyClosed) {
					// queue the message for my remote (real client) self
					Message message = Message.createPingMessageMessage(_id);
					_outboundQueue.queue(message);
				} // end if verified
			}

		};

		Timer timer = new Timer();
		// repeat schedule after initial delay
		timer.schedule(pingTask, 500, TinyMessageServer.PINGINTERVAL);
	}

	/**
	 * A ping has arrived, which is the end of a successful round trip.
	 */
	protected void pingArrived(Message message) {
		_lastPing = System.nanoTime();

		// let's check the round trip time
		long longArray[] = message.getLongArray();
		long sentTime = longArray[0];
		_duration = _lastPing - sentTime;
		_log.info(getLastPingDuration());

		ConnectionTable table = _server.getClientTable();
		if (table != null) {
			table.fireTableDataChanged();
		}
	}
	
	/**
	 * Get a string representation of the time since the last ping 
	 * @return a string representation of the the last ping 
	 */
	public String getTimeSinceLastPing() {
		if (_lastPing < 0) {
			return "";
		}
		double lapse = (System.nanoTime() - _lastPing) / 1.0e9;
		return String.format("%5.1f s", lapse);
	}
	
	/**
	 * Get a string representation of the the last ping 
	 * @return a string representation of the the last ping 
	 */
	public String getLastPing() {
		return DateString.dateStringSS(_lastPing);
	}
	
	/**
	 * Get a minimal string representation of the duration of the last ping.
	 * @return a small string representation of the duration of the last ping.
	 */
	public String getLastPingDurationSmall() {
		return String.format("%6.2f ms", _duration/1.0e6);
	}
	
	/**
	 * Get a string representation of the duration of the last ping.
	 * @return a string representation of the duration of the last ping.
	 */
	public String getLastPingDuration() {
		return String.format("[id: " + getId() + "]  [cnt: " + getMessageCount() + "] " + getClientName() + " server round trip ping: %7.3f ms", _duration / 1.0e6);
	}

	/**
	 * Get the unique Id of this remote client. These are assigned sequentially
	 * starting at 1.
	 * 
	 * @return the unique Id of this remote client
	 */
	@Override
	public int getId() {
		return _id;
	}

	/**
	 * Get the port used by the remote client. This will of course be different
	 * from the server's port.
	 * 
	 * @return the port used by the remote client.
	 */
	public int getPort() {
		return _socket.getPort();
	}

	/**
	 * Get the InetAddress of the remote client
	 * 
	 * @return the inetAddress of the remote client
	 */
	public InetAddress getInetAddress() {
		return _socket.getInetAddress();
	}


	/**
	 * Get the "closed" state of the remote client.
	 * 
	 * @return the "closed" state of the remote client.
	 */
	public boolean isClosed() {
		return _socket.isClosed();
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
		_outboundQueue.setAccept(false);

		// try to wait for outbound queue to flush
		for (int i = 0; i < 5; i++) {
			if (_outboundQueue.isEmpty()) {
				System.out.println("Outbound queue for proxy is empty");
				break;
			}
			else {
				System.out.println("Outbound queue for proxy not empty");
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
				}
			}
		}

		_server.removeProxyClient(this);
		_reader.stopReader();
		_writer.stopWriter();
		
		System.out.println("** CLOSING PROXYCLIENT STREAMS");
		_inputStream.close();
		_outputStream.close();
		_socket.close();
	}

	/**
	 * Shuts down the proxy client and notifies the remote (real) client. This
	 * is done when the server is shutting down, or if for some reason the
	 * server wants to manually remove a client.
	 */
	public void shutdown() {
		
		_log.config("Server sending a shudown to: " + getClientName());

		// send a shutdown
		Message message = Message.createShutdownMessage(getId());
		_outboundQueue.queue(message);

		try {
			close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the underlying socket.
	 * 
	 * @return the underlying socket.
	 */
	public Socket getSocket() {
		return _socket;
	}

	/**
	 * Start the reader thread
	 */
	public void startReader() {
		_reader.start();
	}

	/**
	 * Start the reader thread
	 */
	public void startWriter() {
		_writer.start();
	}

	/**
	 * Set the message server
	 * 
	 * @param server
	 *            the new message server
	 */
	public void setServer(TinyMessageServer server) {
		_server = server;
	}

	/**
	 * Get the message server
	 * 
	 * @return the message server
	 */
	public TinyMessageServer getServer() {
		return _server;
	}

	/**
	 * Get the data input stream. Messages will be read from this stream and
	 * placed on the server's inbound queue, which is shared by all clients.
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
	 * A ReaderThread will be putting messages in this queue. It is owned by the
	 * server, and shared by all remote clients.
	 * 
	 * @return the queue where we place inbound messages for the server to grab
	 *         and process.
	 */
	@Override
	public MessageQueue getInboundQueue() {
		return _server.getInboundQueue();
	}

	/**
	 * Get the queue where we place outbound messages. A WriterThread will be
	 * grabbing the messages and sending them to their destination (to the
	 * client on the other end). The server will be the object that puts message
	 * on this queue when it has one that needs to be sent to the connected
	 * client.
	 * 
	 * @return the queue where we place outbound messages ready for
	 *         transmission.
	 */
	@Override
	public MessageQueue getOutboundQueue() {
		return _outboundQueue;
	}

	/**
	 * Check whether the client wwas verified.
	 * 
	 * @return the verified flag.
	 */
	public boolean isVerified() {
		return _verified;
	}

	/**
	 * Set the verified flag.
	 * 
	 * @param verified
	 *            the verified to set
	 */
	public void setVerified(boolean verified) {
		_verified = verified;
	}


	/**
	 * Get a descriptive name (e.g., "ced") of the remote client.
	 * It might be the same as the juser name.
	 * 
	 * @return a name of the messenger
	 */
	@Override
	public String getClientName() {
		return _clientName;
	}
	
	/**
	 * Get the username of the remote client.
	 * 
	 * @return the username name of the remote client
	 */
	public String getUserName() {
		return _userName;
	}

	/**
	 * Get the OS name of the remote client.
	 * 
	 * @return the operating system name of the remote client
	 */
	public String getOSName() {
		return _osName;
	}
	
	/**
	 * Get the host name of the remote client.
	 * 
	 * @return the operating system name of the remote client
	 */
	public String getHostName() {
		return _hostName;
	}
	
	/**
	 * Set the client name of the remote client
	 * @param name client name of the remote client
	 */
	protected void setClientName(String name) {
		_clientName = (name != null) ? name : "???";
	}
	
	/**
	 * Set the user name of the remote client
	 * @param name user name of the remote client
	 */
	protected void setUserName(String name) {
		_userName = (name != null) ? name : "???";
	}

	/**
	 * Set the name of the remote client
	 * @param operating system name of the remote client
	 */
	protected void setOSName(String name) {
		_osName = (name != null) ? name : "???";
	}

	/**
	 * Set the host name of the remote client
	 * @param name host name of the remote client
	 */
	protected void setHostName(String name) {
		_hostName = (name != null) ? name : "???";
	}

	/**
	 * Set the local port of the remote client
	 * @param localPort the local port
	 */
	public void setLocalPort(int localPort) {
		_localPort = localPort;
	}
	
	/**
	 * Gt the local port of the remote client
	 * @return localPort the local port
	 */
	public int getLocalPort() {
		return _localPort;
	}

	/**
	 * Convert a list of ProxyClients into an array
	 * 
	 * @param pclist
	 *            the list
	 * @return the array
	 */
	public synchronized static ProxyClient[] toArray(List<ProxyClient> pclist) {

		ProxyClient[] array = null;

		if ((pclist != null) && (pclist.size() > 0)) {
			array = new ProxyClient[pclist.size()];
			for (int i = 0; i < pclist.size(); i++) {
				array[i] = pclist.get(i);
			}
		}

		return array;
	}
	
	/**
	 * Increment the message count. The Server does this.
	 */
	protected void incrementMessageCount() {
		_messageCount++;
	}
	
	/**
	 * Get the number of messages this remote client has sent to the server
	 * @return the number of messages this remote client has sent to the server
	 */
	public long getMessageCount() {
		return _messageCount;
	}

	
	/**
	 * Subscribe to a topic
	 * @param topic the topic to subscribe to. This will be trimmed
	 * of white space and converted to lower case, i.e., topics are 
	 * NOT case sensitive.
	 */
	protected void subscribe(String topic) {
		if (topic != null) {
			topic = topic.trim().toLowerCase();
			if (topic.length() > 0) {
				_subscriptions.remove(topic);
				_subscriptions.add(topic);
			}
		}
	}
	
	/**
	 * Unsubscribe to a topic
	 * @param topic the topic to unsubscribe to. This will be trimmed
	 * of white space and converted to lower case, i.e., topics are 
	 * NOT case sensitive.
	 */
	protected void unsubscribe(String topic) {
		if (topic != null) {
			topic = topic.trim().toLowerCase();
			if (topic.length() > 0) {
				_subscriptions.remove(topic);
			}
		}
	}
	
	/**
	 * Check whether this client subscribes to a topic
	 * @param topic the topic to subscribe to. This will be trimmed
	 * of white space and converted to lower case, i.e., topics are 
	 * NOT case sensitive.
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
	 * Send a message 
	 * @param message the message to send
	 */
	protected void send(Message message) {
		if (message != null) {
			if (_outboundQueue != null) {
				_outboundQueue.queue(message);
			}
		}
	}

}
