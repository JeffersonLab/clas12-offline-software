package cnuphys.tinyMS.message;

/**
 * This object is used by clients and servers to direct messages to their proper
 * handler based on message type. The allowed types are enumerated in
 * {@link MessageType}.
 * 
 * @author heddle
 *
 */
public interface IMessageProcessor {
	
	/**
	 * A message is about to be farmed out to the appropriate handler.
	 * This allows you to take a peek at it first.
	 * @param message the message
	 */
	public abstract void peekAtMessage(Message message);

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
	 * Process a SUBSCRIBE message
	 * 
	 * @param message
	 *            the message to process
	 */
	public abstract void processSubscribeMessage(Message message);


	/**
	 * Process a UNSUBSCRIBE message
	 * 
	 * @param message
	 *            the message to process
	 */
	public abstract void processUnsubscribeMessage(Message message);
	
	/**
	 * Process a DATA message
	 * 
	 * @param message
	 *            the message to process
	 */
	public abstract void processClientMessage(Message message);

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
