package cnuphys.tinyMS.server;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Vector;

import cnuphys.tinyMS.message.Message;
import cnuphys.tinyMS.message.MessageProcessor;
import cnuphys.tinyMS.message.MessageQueue;
import cnuphys.tinyMS.message.MessageType;

public class TinyMessageServer {

	//clients will ping the server to monitor health
	protected static final long PINGINTERVAL = 5000;

	// the default ports
	public static final int DEFAULT_PORTS[] = { 5377, 2953, 3310, 3976, 4720,
			9173, 3916, 7571 };

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
	
	//will process the inbound messages
	private MessageProcessor _messageProcessor;
	
	//to avoid multiple shutdowns resulting possible resulting from
	//the shutdown hook thread
	private boolean _alreadyShutDown = false;
	
	/**
	 * Create a message server on a specific port.
	 * @param name descriptive name of the server
	 * @param port the port to try
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
	 * @param name descriptive name of the server
	 * @throws IOException
	 */
	public TinyMessageServer(String name) throws IOException {
		_name = name;

		//try to start on any of the default ports
		for (int i = 0; i < DEFAULT_PORTS.length; i++) {
			try {
				_serverSocket = startServer(DEFAULT_PORTS[i]);

				if (_serverSocket != null) {
					break;
				}
			}
			catch (BindException e) {
				System.out.println("Port " + DEFAULT_PORTS[i]
						+ " appears to be busy.");
			}
		}

		initialize();
	}
	
	//intialize-- create the message processor, the shared
	//inbound queue and the connection accept thread.
	private void initialize() {
		
		if (_serverSocket == null) {
			System.err.println("No Message Server started");
			return;
		}
		
		System.err.println("Message Server started on " + getHostName()
				+ " " + getHostAddress() + " port: " + getLocalPort());


		//create a message processor
		_messageProcessor = createMessageProcessor();
		
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
						ProxyClient newClient = new ProxyClient(
								_serverSocket.accept());
						System.err.println("Adding a new proxy client.");
						addClient(newClient);
					}
					catch (IOException e) {
						// socket closed (is OK)
					}
				}
			}
		};
		new Thread(acceptThread).start();
		
		//create a shutdown thread so we can die gracefully even if
		//killed by a ctrl-c
		
		Runnable shutDown = new Runnable() {

			@Override
			public void run() {
				System.err.println("Server is shutting down");
				try {
					shutdown();
				} catch (IOException e) {
				}
			}
			
		};
		
		Thread shutDownThread = new Thread(shutDown);
		Runtime.getRuntime().addShutdownHook(shutDownThread);
	}
	
	/**
	 * Try to start a server on a given port.
	 * @param port the port to try
	 * @return the serverSocket if successful, otherwise <code>null</code>
	 */
	protected ServerSocket startServer(int port) throws IOException, BindException {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port, 50,
					InetAddress.getLocalHost());
		}
		catch (UnknownHostException e) {
		}
		
		return  serverSocket;
	}

	//create the message processor
	private MessageProcessor createMessageProcessor() {
		MessageProcessor processor = new  MessageProcessor() {

			//this message is a client voluntarily logging out.
			//That is, the message originated from the client
			@Override
			public void processLogoutMessage(Message message) {
				ProxyClient proxyClient = getSender(message);
				if (proxyClient == null) {
					return;
				}

				try {
					System.err.println("closing a connection for client: " + proxyClient.name());
					//call close, not closeAndNotify. The latter is
					//for a server forced logout of a client (e.g., 
					//at shutdown)
					proxyClient.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			//this message is a client being shutdown.
			//That is, the message originated from the server
			@Override
			public void processShutdownMessage(Message message) {
				System.err.println("It is rarely a good sign that a shutdown message has arrived at the server.");
			}


			/**
			 * Process a handshake message. This is used to verify the client.
			 * @message the handshake message
			 */
			@Override
			public void processHandshakeMessage(Message message) {
				System.err.println("Server Received Handshake!");
				
				ProxyClient sender = getSender(message);
				if (sender == null) {
					System.err.println("Could not get sender of handshake");
				}
				else {
					System.err.println("remote client verified");
					sender.setVerified(true);
					
					//get username
					String env[] = message.getStringArray();
					System.err.println("*********");
					System.err.println("Client id: " + sender.getId());
					System.err.println("USER NAME: " + env[0]);
					System.err.println("LOGIN NAME: " + env[1]);
					System.err.println("OS NAME: " + env[2]);
					System.err.println("HOST NAME: " + env[3]);
					
					sender.setEnvStrings(env);
					sender.startPingTimer();
				}
			}

			@Override
			//ping has made a round trip
			public void processPingMessage(Message message) {
				ProxyClient sender = getSender(message);
				sender.pingArrived(message);
			}
			
			@Override
			public void processDataMessage(Message message) {
				dataMessage(message);
			}

			@Override
			public boolean accept(Message message) {
				
				//handshakes are accepted and used to verify
				//a client. Otherwise the client must be verified.
				
				if (message.getMessageType() == MessageType.HANDSHAKE) {
					return true;
				}
				
				ProxyClient sender = getSender(message);
				if (sender == null) {
					System.err.println("Could not match a remote client to message sender.");
					return false;
				}
				
				if (sender.isVerified()) {
					
					//if someone else if the destination, try to forward
					if (message.getDestinationId() != Message.SERVER) {
						forwardMessage(message);
						return false;
					}
					
					return true;
				}
				else {
					System.err.println("Message from unverified client");
					return false;
				}

			}

			
		};
		return processor;
	}
	
	/**
	 * This is the only non-admininistrative message. Just forward it to the destination(s).
	 * @param message the data message
	 */
	protected void dataMessage(Message message) {
		//TODO implement the fowarding. Check if the destination
		
		//if the destination is Message.BROADCAST, forward to every client except the source (sender)
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
	protected void addClient(ProxyClient proxyClient) {
		ProxyClient rc = getProxyClient(proxyClient.getId());

		// bad error if duplicate
		if (rc != null) {
			System.err.println("SEVERE ERROR tried to add remote client with"
					+ " Id already being used: " + proxyClient.getId());
			return;
		}

		proxyClient.setServer(this);
		_proxyClients.add(proxyClient);
		proxyClient.startReader();
		proxyClient.startWriter();
		
		//send client a handshake
		Message message = Message.createHandshakeMessage(proxyClient.getId());
		proxyClient.getOutboundQueue().queue(message);
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

		return getProxyClient(message.getSourceId());
	}

	// a message arrived whose source was not the server
	protected void forwardMessage(Message message) {
		//try to find a remote client for the destination
		ProxyClient client = getProxyClient(message.getDestinationId());
		if (client != null) {
			System.err.println("forwarding");
			try {
				client.writeMessage(message);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Shutdown the server. As a courtesy, notify all the
	 * clients.
	 */
	public void shutdown() throws IOException {
		
		if (_alreadyShutDown) {
			System.err.println("Server already shut down");
			return;
		}

		_alreadyShutDown = true;
		
		acceptConnections = false;

		// close and notify the proxy clients
		for (ProxyClient proxyClient : _proxyClients) {
			try {
				proxyClient.shutdown();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
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

}
