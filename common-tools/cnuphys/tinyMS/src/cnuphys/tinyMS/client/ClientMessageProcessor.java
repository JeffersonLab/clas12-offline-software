package cnuphys.tinyMS.client;

import java.io.IOException;
import cnuphys.tinyMS.Environment.Environment;
import cnuphys.tinyMS.message.Message;
import cnuphys.tinyMS.message.MessageProcessor;
import cnuphys.tinyMS.message.MessageType;

public class ClientMessageProcessor extends MessageProcessor {
	
	//the client
	private Client _client;
	
	/**
	 * Create a processor for client messages
	 * @param client the client owner
	 */
	public ClientMessageProcessor(Client client) {
		_client = client;
	}
	
	/**
	 * A message is about to be farmed out to the appropriate handler.
	 * This allows you to take a peek at it first.
	 * @param message the message
	 */
	public void peekAtMessage(Message message) {
		//default: do nothing
	}

	// A logout message should be from client to server
	@Override
	public void processLogoutMessage(Message message) {
		System.err.println("It is rarely a good sign that a logout message arrives at a client.");
	}

	// A shutdown message arriving at the client means the server is
	// telling the client he will be logged out. Probably something of a
	// courtesy,
	// as part of the server shutting down gracefully.
	@Override
	public void processShutdownMessage(Message message) {
		System.err.println("!!! [" + _client.getClientName() + "] " + " received a SHUTDOWN!");
		try {
			_client.getOutboundQueue().setAccept(false);
			
			//give it time to send last message
			try {
				Thread.currentThread();
				Thread.sleep(2000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			_client.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	// A handshake is the first message sent by the server.
	// From it I can get my Id.
	@Override
	public void processHandshakeMessage(Message message) {
		System.err.println("Client Received Handshake! [Client " + _client.getId() + "]");

		if (_client.getId() > 0) {
			System.err.println("Already have a good Id. Should not have gotten a handshake.");
			return;
		}

		// now I can learn my id
		int id = message.getDestinationId();
		System.err.println("Acquired client ID: " + id);
		_client.setId(id);

		message.invert(); // prepare to send it back

		// add some environmental strings (and my user name)

		message.addPayload(envStringArray());
		_client.getOutboundQueue().queue(message);

	}

	/**
	 * A ping has arrived from the server. Log the last ping time and
	 * send the message back.
	 * 
	 * @param message
	 *            the ping message
	 */
	@Override
	public void processPingMessage(Message message) {
		long ct = System.nanoTime();
		if (_client.getLastPing() > 0) {
			long duration = ct - _client.getLastPing();
			String pdstr = String.format("[" + _client.getClientName() + "] " + "Time since last ping: %7.3f ms", duration / 1.0e6);
			System.err.println(pdstr);
		}

		_client.setLastPing(ct);

		message.invert(); // prepare to send it back
		_client.getOutboundQueue().queue(message);
	}

	@Override
	public void processDataMessage(Message message) {
	}
	
	/**
	 * Process a SERVERLOG message
	 * 
	 * @param message
	 *            the message to process
	 */
	@Override
	public void processServerLogMessage(Message message) {
		System.err.println("It is rarely a good sign that a logout message arrives at a client.");
	}

	// can be used as a filter
	@Override
	public boolean accept(Message message) {
		// handshakes are accepted and used to get my Id

		if (message.getMessageType() == MessageType.HANDSHAKE) {
			return true;
		}

		if (message.getDestinationId() != _client.getId()) {
			System.err.println("Client recieved a message with wrong Id");
			return false;
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
		String array[] = new String[4];
		
		Environment env = Environment.getInstance();

		array[0] = _client.getClientName();
		array[1] = env.getUserName();
		array[2] = env.getOsName();
		array[3] = env.getHostName();
		return array;
	}

}
