package cnuphys.tinyMS.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import cnuphys.tinyMS.common.ReaderThread;
import cnuphys.tinyMS.common.WriterThread;
import cnuphys.tinyMS.message.Message;
import cnuphys.tinyMS.message.MessageProcessor;
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
public class Client extends Messenger {
	
	// the underlying socket
	private Socket _socket;

	// an id returned by the server at first connection
	private int _id = -1;

	// the data input stream. Inbound message will be read from this stream.
	private DataInputStream _inputStream;

	// the data output stream
	private DataOutputStream _outputStream;
	
	//the inbound queue. Messages from the server will
	//end up here
	private MessageQueue _inboundQueue;
	
	//the outbound queue. Messages being sent to the server
	//will be placed here.
	private MessageQueue _outboundQueue;

	// the reader thread
	private ReaderThread _reader;
	
	// the writer thread
	private WriterThread _writer;

	//will process the inbound messages
	private MessageProcessor _messageProcessor;
	
	//last ping from server
	private long _lastPing = -1L;
	
	//Descriptive user name. If null, actual login username will be used
	private String _userName;

	//to avoid multiple calls to close
	private boolean _alreadyClosed = false;
	
	/**
	 * Create a client that will connect to a {@link TinyMessageServer}. Most applications
	 * will not use this constructor directly (since you won't have a socket) but rather
	 * the static convenience methods in {@link ClientSupport}.
	 * @param userName descriptive user name. If null, the actual login username will be used.
	 * @param socket the underlying socket
	 * @see ClientSupport
	 */
	public Client(String userName, Socket socket) {
		_userName = userName;
		_socket = socket;
		
		//create a message processor
		_messageProcessor = createMessageProcessor();

		//where to place message for transmission
		_outboundQueue = new MessageQueue(100, 20);
		
		//where inbound messages are dequeued for processing
		_inboundQueue = new MessageQueue(100, 20);
		
		//grab the streams, create the reader and writer and a thread to
		//dequeue messages.
		try {
			_inputStream = new DataInputStream(socket.getInputStream());
			_outputStream = new DataOutputStream(socket.getOutputStream());
			//to read inbound messages from server
			_reader = new ReaderThread(_socket, this);
			
			//to write (transmit) outbound messages to server
			_writer = new WriterThread(_socket, this);
			
			_reader.start();
			_writer.start();
			
			Runnable dequerer = new Runnable() {

				@Override
				public void run() {
					while (true) {
						//the dequeue method "waits" so no thread yielding is necessary
						Message message = _inboundQueue.dequeue();
						_messageProcessor.processMessage(message);
					}
				}
			};
			new Thread(dequerer).start();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	//create the message processor
	private MessageProcessor createMessageProcessor() {
		MessageProcessor processor = new  MessageProcessor() {
			
			//A logout message should be from client to server
			@Override
			public void processLogoutMessage(Message message) {
				System.err.println("It is rarely a good sign that a logout message arrives at a client.");
			}

			//A shutdown message arriving at the client means the server is
			//telling the client he will be logged out. Probably something of a courtesy, 
			//as part of the server shutting down gracefully.
			@Override
			public void processShutdownMessage(Message message) {
				try {
					System.err.println("Client Received Shutdown! [Client: " + _id + "]");
					_outboundQueue.setAccept(false);
					close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			//A handshake is the first message sent by the server. 
			//From it I can get my Id. 
			@Override
			public void processHandshakeMessage(Message message) {
				System.err.println("Client Received Handshake! [Client " + getId() + "]");
				
				if (_id > 0) {
					System.err.println("Already have a good Id. Should not have gotten a handshake.");
					return;
				}
				
				//now I can learn my id
				_id = message.getDestinationId();
				System.err.println("Acquired client ID: " + _id);
				
				message.invert(); //prepare to send it back
				
				//add some environmental strings (and my user name)
				
				message.addPayload(envStringArray());
				_outboundQueue.queue(message);
				
			}

			/** 
			 * A ping has arrived from the server. Log the last ping time
			 * and send the message back.
			 * @param message the ping message
			 */
			@Override
			public void processPingMessage(Message message) {
				long ct = System.nanoTime();
				if (_lastPing > 0) {
					long duration = ct - _lastPing;
					String pdstr = String.format("Time since last ping: %7.3f ms", duration/1.0e6);
					System.err.println(pdstr);
				}
				
				_lastPing = ct;
				
				message.invert(); //prepare to send it back
				_outboundQueue.queue(message);
			}
			
			@Override
			public void processDataMessage(Message message) {
			}

			//can be used as a filter
			@Override
			public boolean accept(Message message) {
				//handshakes are accepted and used to get my Id
				
				if (message.getMessageType() == MessageType.HANDSHAKE) {
					return true;
				}
				
				if (message.getDestinationId() != getId()) {
					System.err.println("Client recieved a message with wrong Id");
					return false;
				}

				return true;
			}

			
		};
		return processor;
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
	 * Get the underlying socket
	 * 
	 * @return the underlying socket
	 */
	public Socket getSocket() {
		return _socket;
	}
	
	/**
	 * Get the data input stream. Messages will be
	 * read from this stream and placed on the inbound queue.
	 * @return the data input stream
	 */
	@Override
	public DataInputStream getDataInputStream() {
		return _inputStream;
	}

	/**
	 * Get the data output stream. Messages will be removed from
	 * the outbound queue and sent on this stream.
	 * @return the data output stream
	 */
	@Override
	public DataOutputStream getDataOutputStream() {
		return _outputStream;
	}

	/**
	 * A ReaderThread will be putting messages in this queue.
	 * They should be dequeued and processed,
	 * @return  the queue where we place inbound messages
	 * coming from the server.
	 */
	@Override
	public MessageQueue getInboundQueue() {
		return _inboundQueue;
	}

	/**
	 * Get the queue where we place outbound messages. A WriterThread
	 * will be grabbing the messages and sending them to their
	 * destination (to the server on the other end).
	 * @return the queue where we place outbound messages ready for
	 * transmission.
	 */
	@Override
	public MessageQueue getOutboundQueue() {
		return _outboundQueue;
	}
	
	/**
	 * Get an identifying name for the client
	 * @return a name of the client
	 */
	@Override
	public String name() {
		if (_userName == null) {
			_userName = getProperty("user.name");
		}
		return _userName;
	}
	
	/**
	 * This logs the client out after sending a message to the 
	 * server.
	 */
	public void logout() {
		
		Message message = Message.createLogoutMessage(getId());
		_outboundQueue.queue(message);
		_outboundQueue.setAccept(false);
		
		//try to wait for the message to be sent
		for (int i = 0; i < 5; i++) {
			if (_outboundQueue.isEmpty()) {
				System.err.println("Outbound queue is empty");
				break;
			}
			else {
				System.err.println("Outbound queue not empty");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
		
		try {
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
		
		System.err.println("Closing " + name());
		_reader.stopReader();
		_writer.stopWriter();
		_socket.close();
		System.err.println("Socket closed for [Client " + getId() + "]");
	}
	
	/**
	 * Convenience routine for getting a system property.
	 * 
	 * @param keyName
	 *            the key name of the property
	 * @return the property, or <code>null</null>.
	 */
	protected String getProperty(String keyName) {
		try {
			return System.getProperty(keyName);
		}
		catch (Exception e) {
			return null;
		}
	}


	/**
	 * Get the environment string array, getting strings
	 * at these indices:<br>
	 * [0] the descriptive user name<br>
	 * [1] the login user name<br>
	 * [2] the os name<br>
	 * [3] the host name
	 * @return the environment string array
	 */
	protected String[] envStringArray() {
		String array[] = new String[4];
		
		array[0] = name();
		array[1] = getProperty("user.name");
		array[2] = getProperty("os.name");


		try {
			InetAddress addr = InetAddress.getLocalHost();

			// Get hostname
			array[3] = addr.getHostName();
		}
		catch (UnknownHostException e) {
			array[3] = "???";
		}

		return array;
	}

}
