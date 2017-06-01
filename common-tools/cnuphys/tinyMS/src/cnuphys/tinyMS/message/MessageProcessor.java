package cnuphys.tinyMS.message;

import cnuphys.tinyMS.server.ServerMessageProcessor;

/**
 * This object is used by clients and servers to direct messages to their proper
 * handler based on message type. The allowed types are enumerated in
 * {@link MessageType}.
 * 
 * @author heddle
 *
 */
public abstract class MessageProcessor {

	/**
	 * Process a message based on its type.
	 * 
	 * @param message
	 *            the message to process
	 * @see Message
	 */
	public void processMessage(Message message) {
		if ((message == null) || !accept(message)) {
			return;
		}
		
		if (this instanceof ServerMessageProcessor) {
			System.err.println("*** SERVER received message of type: " + message.getMessageType() + "  frm: " + message.getSourceId());
		}
		// CLOSE, DATA, HANDSHAKE, PING, USER;

		switch (message.getMessageType()) {
		case LOGOUT:
			processLogoutMessage(message);
			break;

		case SHUTDOWN:
			processShutdownMessage(message);
			break;

		case DATA:
			processDataMessage(message);
			break;

		case HANDSHAKE:
			processHandshakeMessage(message);
			break;

		case PING:
			processPingMessage(message);
			break;

		case SERVERLOG:
			processServerLogMessage(message);
			break;

		}
	}

	/**
	 * Process a LOGOUT message. This is a message that a client sends signaling
	 * a "logout."
	 * 
	 * @param message
	 *            the LOGOUT message to process
	 */
	public abstract void processLogoutMessage(Message message);

	/**
	 * Process a SHUTDOWN message. This is a message that server sends signaling
	 * a "shutdown."
	 * 
	 * @param message
	 *            the SHUTDOWN message to process
	 */
	public abstract void processShutdownMessage(Message message);

	/**
	 * Process a HANDSHAKE message
	 * 
	 * @param message
	 *            the message to process
	 */
	public abstract void processHandshakeMessage(Message message);

	/**
	 * Process a PING message
	 * 
	 * @param message
	 *            the message to process
	 */
	public abstract void processPingMessage(Message message);
	
	/**
	 * Process a SERVERLOG message
	 * 
	 * @param message
	 *            the message to process
	 */
	public abstract void processServerLogMessage(Message message);


	/**
	 * Process a DATA message
	 * 
	 * @param message
	 *            the message to process
	 */
	public abstract void processDataMessage(Message message);

	/**
	 * Can be used to toss away messages for example they have the wrong
	 * destination or a client is not verified.
	 * 
	 * @param message
	 *            the message to process.
	 * @return <code>true</code> if the message is acceptable for processing.
	 */
	public abstract boolean accept(Message message);
}
