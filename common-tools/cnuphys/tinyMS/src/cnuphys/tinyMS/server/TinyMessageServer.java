package cnuphys.tinyMS.server;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;

import cnuphys.tinyMS.log.Log;
import cnuphys.tinyMS.message.Message;
import cnuphys.tinyMS.message.IMessageProcessor;
import cnuphys.tinyMS.message.MessageQueue;
import cnuphys.tinyMS.message.MessageType;
import cnuphys.tinyMS.server.gui.ServerFrame;
import cnuphys.tinyMS.table.ConnectionTable;

public class TinyMessageServer implements IMessageProcessor, Runnable {
	
	private final String _version = "0.51";
	
	// the log
	private Log _log = Log.getInstance();
	
	// bytes transferred
	private long _bytesTransferred;
	
	//the port used
	private int _port = -1;

	// clients will ping the server to monitor health
	protected static final long PINGINTERVAL = 5000;

	// the default ports
	public static final int DEFAULT_PORTS[] = { 5377, 2953, 3310, 22724};

	// accepting connections?
	private boolean acceptConnections = true;

	// actual server socket
	private ServerSocket _serverSocket;

	// a queue for holding data messages arriving from clients
	// every remote client has a reader thread that will dump
	// inbound messages to this queue
	private MessageQueue _sharedInboundQueue;

	// create a vector of active connections. They will be given
	// unique sequential Ids starting at 1 and never reused, so they
	// will always be in order of Id
	private Vector<ProxyClient> _proxyClients = new Vector<ProxyClient>();
	
	// All known topics
	private Vector<String> _allTopics = new Vector<String>();

	// name of the server
	private String _name;
	
	//the GUI
	private ServerFrame _serverFrame;

	// to avoid multiple shutdowns resulting possible resulting from
	// the shutdown hook thread
	private boolean _shutDown = false;
	
	/**
	 * Create a message server on a specific port.
	 * 
	 * @param name
	 *            descriptive name of the server
	 * @param port
	 *            the port to try
	 * @throws IOException
	 */
	public TinyMessageServer(String name, int port) throws IOException {
		_name = name;

		// try to start on given port
		try {
			_serverSocket = startServer(port);
		}
		catch (BindException e) {
			System.out.println("Port " + port + " appears to be busy.");
		}

		initialize();
	}

	/**
	 * Create a message server, trying the default ports.
	 * 
	 * @param name
	 *            descriptive name of the server
	 * @throws IOException
	 */
	public TinyMessageServer(String name) throws IOException {
		_name = name;

		// try to start on any of the default ports
		for (int i = 0; i < DEFAULT_PORTS.length; i++) {
			try {
				_serverSocket = startServer(DEFAULT_PORTS[i]);

				if (_serverSocket != null) {
					break;
				}
			}
			catch (BindException e) {
				System.out.println("Port " + DEFAULT_PORTS[i] + " appears to be busy.");
			}
		}

		initialize();
	}

	// intialize-- create the message processor, the shared
	// inbound queue and the connection accept thread.
	private void initialize() {
		// create a gui
		_serverFrame = ServerFrame.createServerFrame(this);

		if (_serverSocket == null) {
			_log.warning("No Tiny Message Server started");
			return;
		}

		_log.config("Tiny Message Server started on " + getHostName() + " " + getHostAddress() + " port: "
				+ getLocalPort());


		// create an input queue and a thread to dequeue
		// inbound data from remote clients
		createInboundQueue();

		// wait for connections in another thread, since
		// the accept method blocks.
		Runnable acceptThread = new Runnable() {
			@Override
			public void run() {
				while (acceptConnections) {
					try {
						// the accept() blocks
						ProxyClient newClient = new ProxyClient(_serverSocket.accept());
						_log.info("Adding a new proxy client.");
						addClient(newClient);
					}
					catch (IOException e) {
						// socket closed (is OK)
					}
				}
			}
		};
		new Thread(acceptThread).start();
		
		// create a shutdown thread so we can die gracefully even if
		// killed by a ctrl-c

		Runnable shutDown = new Runnable() {

			@Override
			public void run() {
				_log.warning("Server is shutting down");
				System.out.println("\n===========================================");
				System.out.println("***** Server is shutting down from kill thread. *****");
				System.out.println("\n===========================================");
				try {
					shutdown();
				}
				catch (IOException e) {
				}
			}

		};

		Thread shutDownThread = new Thread(shutDown);
		Runtime.getRuntime().addShutdownHook(shutDownThread);
	}
	

	/**
	 * Try to start a server on a given port.
	 * 
	 * @param port
	 *            the port to try
	 * @return the serverSocket if successful, otherwise <code>null</code>
	 */
	protected ServerSocket startServer(int port) throws IOException, BindException {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port, 50, InetAddress.getLocalHost());
			if (serverSocket != null) {
				System.out.println("============================================");
				System.out.println("== TinyMessageServer started ");
				System.out.println("== Server name: " + _name);
				System.out.println("== Server port: " + port);
				System.out.println("============================================");
				_port = port;
			}
		}
		catch (UnknownHostException e) {
		}

		return serverSocket;
	}
	
	/**
	 * Get the port
	 * @return the port used by the server.
	 */
	public int getPort() {
		return _port;
	}

	// a queue for holding data messages arriving from clients
	// every remote client has a reader thread that will dump
	// inbound messages to this queue
	private void createInboundQueue() {
		_sharedInboundQueue = new MessageQueue(200, 50);
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		while (true) {
			Message message = _sharedInboundQueue.dequeue();
			message.process(this);
		}
	}

	/**
	 * Get the server version
	 * @return the server version
	 */
	public String getVersion() {
		return _version;
	}
	
	/**
	 * Add a new client. It is put in the sorted collection. It should have a
	 * unique Id, and should go in at the end--but we'll check for a duplicate
	 * Id which would indicate a serious error.
	 * 
	 * @param proxyClient
	 *            the new proxy client, which corresponds to an actual client.
	 */
	protected synchronized void addClient(ProxyClient proxyClient) {
		ProxyClient rc = getProxyClient(proxyClient.getId());

		// bad error if duplicate
		if (rc != null) {
			_log.error(
					"SEVERE ERROR tried to add remote client with" + " Id already being used: " + proxyClient.getId());
			return;
		}

		proxyClient.setServer(this);
		_proxyClients.add(proxyClient);
		proxyClient.startReader();
		proxyClient.startWriter();

		// send client a handshake
		Message message = Message.createHandshakeMessage(proxyClient.getId());
		proxyClient.getOutboundQueue().queue(message);
		_log.config("There are now " + _proxyClients.size() + " proxy clients");
		_serverFrame.fireTableDataChanged();
	}

	/**
	 * All this does is remove the proxy client from the collection.
	 * 
	 * @param proxyClient
	 */
	protected synchronized void removeProxyClient(ProxyClient proxyClient) {
		if (proxyClient != null) {
			_proxyClients.remove(proxyClient);
			_log.config("There are now " + _proxyClients.size() + " proxy clients");
			_serverFrame.fireTableDataChanged();
		}
	}

	/**
	 * Get the proxy client from the Id. Note it relies on the fact that client
	 * Ids are handed out sequentially (and never reused) and always added at
	 * the end.
	 * 
	 * @param id
	 *            the id of the proxy client to search for
	 * @return the remote client, or null
	 */
	protected ProxyClient getProxyClient(int id) {
		for (ProxyClient rc : _proxyClients) {
			if (rc.getId() == id) {
				return rc;
			}
			if (rc.getId() > id) {
				return null;
			}
		}

		return null;
	}

	/**
	 * Convenience routine the gets the remote client who sent the message.
	 * 
	 * @param message
	 *            the message received
	 * @return the remote client, or <code>null</code>
	 */
	protected ProxyClient getSender(Message message) {
		if (message == null) {
			return null;
		}

		return getProxyClient(message.getClientId());
	}

	/**
	 * Get an array of proxy clients
	 * 
	 * @return an array of proxy clients
	 */
	public ProxyClient[] getProxyClientArray() {
		return ProxyClient.toArray(_proxyClients);
	}
	
	/**
	 * Get the list of proxy clients
	 * @return the list of proxy clients
	 */
	public List<ProxyClient> getProxyClients() {
		return _proxyClients;
	}
	
	/**
	 * See if the server has been shut down
	 * @return <code>true</code> if the server has been shut down
	 */
	public boolean isShutDown() {
		return _shutDown;
	}

	/**
	 * Shutdown the server. As a courtesy, notify all the clients.
	 * @throws IOException 
	 */
	public void shutdown() throws IOException {

		if (_shutDown) {
			System.out.println("Server already shut down");
			return;
		}
		
		System.out.println("\n===========================================");
		System.out.println("***** Server is shutting down from shutdown method. *****");
		System.out.println("\n===========================================");

		_shutDown = true;

		acceptConnections = false;

		// close and notify the proxy clients
		// put in array to avoid concurrent violations
		ProxyClient[] array = ProxyClient.toArray(_proxyClients);
		if (array != null) {
			for (ProxyClient proxyClient : array) {
				try {
					proxyClient.shutdown();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// close the socket
		if (_serverSocket != null) {
			_serverSocket.close();
		}

		System.out.println("Server shutdown");
	}

	/**
	 * Get the server's data inputQueue
	 * 
	 * @return the server's data inputQueue
	 */
	public MessageQueue getInputQueue() {
		return _sharedInboundQueue;
	}

	/**
	 * Get the port used by the remote client. This will of course be different
	 * from the server's port.
	 * 
	 * @return the port used by the remote client.
	 */
	public int getLocalPort() {
		return _serverSocket.getLocalPort();
	}

	/**
	 * Get the InetAddress of the remote client
	 * 
	 * @return the inetAddress of the remote client
	 */
	public InetAddress getInetAddress() {
		return _serverSocket.getInetAddress();
	}

	/**
	 * Get the underlying host name
	 * 
	 * @return the underlying host name
	 */
	public String getHostName() {
		return _serverSocket.getInetAddress().getHostName();
	}

	/**
	 * The the IP address of the server (this) as a string.
	 * 
	 * @return the IP address of the server.
	 */
	public String getHostAddress() {
		if (_serverSocket == null) {
			return null;
		}
		return _serverSocket.getInetAddress().getHostAddress();
	}

	/**
	 * Get the common inbound queue shared by all remote clients.
	 * 
	 * @return the common inbound queue.
	 */
	public MessageQueue getInboundQueue() {
		return _sharedInboundQueue;
	}

	/**
	 * get the name of the server
	 * 
	 * @return the name of the server
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Get the Gui
	 * @return the server frame gui
	 */
	public ServerFrame getGui() {
		return _serverFrame;
	}
	
	/**
	 * Get the client table
	 * @return the client table
	 */
	public ConnectionTable getClientTable() {
		return (_serverFrame == null) ? null : _serverFrame.getClientTable();
	}

	
	/**
	 * Add a topic to the list of known topics. Do knowthing if allready known.
	 * @param topic the topic to add.
	 */
	protected void addTopic(String topic)  {
		if (topic != null) {
			topic = topic.trim().toLowerCase();
			if (topic.length() > 0) {
				if (!_allTopics.contains(topic)) {
					_allTopics.add(topic);
					_serverFrame.getTopicList().addTopic(topic);
				}
			}
		}
	}
	

	/**
	 * A message is about to be farmed out to the appropriate handler.
	 * This allows you to take a peek at it first.
	 * @param message the message
	 */
	public void peekAtMessage(Message message) {
		
		_bytesTransferred += message.getDataLength();

		ProxyClient proxyClient = getSender(message);
		if (proxyClient != null) {
			proxyClient.incrementMessageCount();
		}
	}

	// this message is a client voluntarily logging out.
	// That is, the message originated from the client
	@Override
	public void processLogoutMessage(Message message) {
		ProxyClient proxyClient = getSender(message);
		if (proxyClient == null) {
			return;
		}

		try {
			_log.info("Logging out client: " + proxyClient.getClientName());
			// call close, not shutdown. The latter is
			// for a server forced logout of a client.
			proxyClient.close();
			fireTableDataChanged();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	// this message is a client being shutdown.
	// That is, the message originated from the server
	@Override
	public void processShutdownMessage(Message message) {
		_log.warning("It is rarely a good sign that a shutdown message has arrived at the server.");
	}

	/**
	 * Process a handshake message. This is used to verify the client.
	 * 
	 * @param message the handshake message
	 */
	@Override
	public void processHandshakeMessage(Message message) {
		_log.info("Server Received Handshake");

		ProxyClient sender = getSender(message);
		if (sender == null) {
			_log.warning("Could not get sender of handshake");
		} else {
			try {
				_log.config("remote client verified: " + sender.getClientName());
				sender.setVerified(true);

				// get username
				String env[] = message.getStringArray();
				sender.startPingTimer();
				sender.setClientName(env[0]);
				sender.setUserName(env[1]);
				sender.setOSName(env[2]);
				sender.setHostName(env[3]);
				
				int localPort = Integer.parseInt(env[4]);
				sender.setLocalPort(localPort);

				_log.info("*********");
				_log.info("CLIENT ID: " + sender.getId());
				_log.info("CLIENT NAME: " + sender.getClientName());
				_log.info("USER NAME: " + sender.getUserName());
				_log.info("OS NAME: " + sender.getOSName());
				_log.info("HOST NAME: " + sender.getHostName());
				_log.info("LOCAL PORT: " + sender.getLocalPort());
				fireTableDataChanged();
			} catch (Exception e) {
				_log.exception(e);
			}
		}
	}

	@Override
	// ping has made a round trip
	public void processPingMessage(Message message) {
		ProxyClient sender = getSender(message);
		sender.pingArrived(message);
	}

	/**
	 * A non-administrative message has arrived. This should
	 * be broadcast to other clients subscribed to the topic
	 */
	@Override
	public void processClientMessage(Message message) {
		broadcastMessage(message);
	}
	
	/**
	 * Process a SERVERLOG message
	 * 
	 * @param message
	 *            the message to process
	 */
	@Override
	public void processServerLogMessage(Message message) {
		_log.config("Server got a SERVERLOG message");
		Log.Level level = Log.Level.values()[message.getTag()];
		String lstr = message.getString();
		String nstr = getProxyClient(message.getClientId()).getClientName();
		String s = "[Client: " + nstr + "] " + lstr;
		
		//ERROR, CONFIG, WARNING, INFO, EXCEPTION
		switch (level) {
		case INFO:
			_log.info(s);
			break;
			
		case CONFIG:
			_log.config(s);
			break;
			
		case ERROR:
		case EXCEPTION:
			_log.error(s);
			break;
			
		case WARNING:
			_log.warning(s);
			break;
			
		}
	}

	/**
	 * Process a SUBSCRIBE message
	 * 
	 * @param message
	 *            the message to process
	 */
	@Override
	public void processSubscribeMessage(Message message) {
		ProxyClient sender = getSender(message);
		if (sender == null) {
			_log.warning("Could not get sender of subscribe message");
		} else {
			try {
				String topic = message.getString();
				topic = topic.trim().toLowerCase();
				if (topic.length() > 0) {
					addTopic(topic);
					_log.config("client subscription: " + sender.getClientName() + "  topic: " + topic);
					sender.subscribe(topic);

					// send back as confirmation
					sender.send(message);
				}
			} catch (Exception e) {
				_log.exception(e);
			}

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
		ProxyClient sender = getSender(message);
		if (sender == null) {
			_log.warning("Could not get sender of unsubscribe message");
		} else {
			try {
				String topic = message.getString();
				topic = topic.trim().toLowerCase();
				_log.config("client unsubscription: " + sender.getClientName() + "  topic: " + topic);
				sender.unsubscribe(topic);
				
				//send back as confirmation
				sender.send(message);
			} catch (Exception e) {
				_log.exception(e);
			}

		}
	}


	@Override
	public boolean accept(Message message) {

		// handshakes are accepted and used to verify
		// a client. Otherwise the client must be verified.

		if (message.getMessageType() == MessageType.HANDSHAKE) {
			return true;
		}

		ProxyClient sender = getSender(message);
		if (sender == null) {
			_log.warning("Could not match a remote client to message sender.");
			return false;
		}
		else if (sender.isVerified()) {
			return true;
		}
		else {
			_log.warning("Message from unverified client");
			return false;
		}

	}

	/**
	 * Broadcast a message to all clients (except the sender)
	 * 
	 * @param message
	 *            the message to broadcast
	 */
	protected void broadcastMessage(Message message) {
		ProxyClient[] array = getProxyClientArray();

		if (array != null) {
			for (ProxyClient client : array) {
				try {

					// don't send back to sender!
					if (client.getId() != message.getClientId()) {

						// subscribed?
						if (client.isSubscribed(message.getTopic())) {
							client.writeMessage(message);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	
	/**
	 * Convenience routine to fire a data changed event
	 * so that the table (if present)  updates
	 */
	public void fireTableDataChanged() {
		ServerFrame gui = getGui();
		if (gui != null) {
			gui.fireTableDataChanged();
		}
	}


	/***
	 * Get the bytes read by the server
	 * @return the bytes read since start
	 */
	public long getBytesTransferred() {
		return _bytesTransferred;
	}
	
	/**
	 * Starts a server (well, attempts to) on one of the default ports.
	 * @param arg the variable arguments
	 */
	public static void main(String arg[]) {
		try {
			new TinyMessageServer("Tiny Message Server");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
