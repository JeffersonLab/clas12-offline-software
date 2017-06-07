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
import cnuphys.tinyMS.message.MessageProcessor;
import cnuphys.tinyMS.message.MessageQueue;
import cnuphys.tinyMS.server.gui.ServerFrame;
import cnuphys.tinyMS.table.ConnectionTable;

public class TinyMessageServer {

	// the log
	private Log _log = Log.getInstance();

	// clients will ping the server to monitor health
	protected static final long PINGINTERVAL = 5000;

	// the default ports
	public static final int DEFAULT_PORTS[] = { 5377, 2953, 3310 };

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

	// name of the server
	private String _name;
	
	//the GUI
	private ServerFrame _serverFrame;

	// will process the inbound messages
	private MessageProcessor _messageProcessor;

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
			System.err.println("Port " + port + " appears to be busy.");
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
				System.err.println("Port " + DEFAULT_PORTS[i] + " appears to be busy.");
			}
		}

		initialize();
	}

	// intialize-- create the message processor, the shared
	// inbound queue and the connection accept thread.
	private void initialize() {
		System.err.print("Creating gui...");
		// create a gui
		_serverFrame = ServerFrame.createServerFrame(this);
		System.err.println("done.");

		if (_serverSocket == null) {
			_log.warning("No Tiny Message Server started");
			return;
		}

		_log.config("Tiny Message Server started on " + getHostName() + " " + getHostAddress() + " port: "
				+ getLocalPort());

		// create a message processor
		_messageProcessor = new ServerMessageProcessor(this);

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
			System.err.print("Ready to create server socket...");
			serverSocket = new ServerSocket(port, 50, InetAddress.getLocalHost());
			System.err.println("done.");
		}
		catch (UnknownHostException e) {
		}

		return serverSocket;
	}

	// a queue for holding data messages arriving from clients
	// every remote client has a reader thread that will dump
	// inbound messages to this queue
	private void createInboundQueue() {
		_sharedInboundQueue = new MessageQueue(200, 50);
		Runnable dequerer = new Runnable() {

			@Override
			public void run() {
				while (true) {
					Message message = _sharedInboundQueue.dequeue();
					_messageProcessor.processMessage(message);
				}
			}
		};
		new Thread(dequerer).start();
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
	 */
	public void shutdown() throws IOException {

		if (_shutDown) {
			System.err.println("Server already shut down");
			return;
		}

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

		System.err.println("Server shutdown");
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

}
