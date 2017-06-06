package cnuphys.tinyMS.message;

/**
 * Enumerates the allowable message types. They are
 * <ul>
 * <li><code>CLIENT</code> The only non-administrative message. Used to send
 * arrays of any primitive type, and serialized java objects.
 * <li><code>HANDSHAKE</code> This is the first message sent from the server to
 * a new client. It will contain the client's unique Id. The client sends a
 * response to the handshake. That response will cause the client to be verified
 * (trusted) by the server.
 * <li><code>LOGOUT</code> This is sent <b>by</b> a client when he logs out. It
 * is sent to the client if the server is telling him that he is about to be
 * logged out (probably because the server is shutting down.) The connection
 * will be shut down and the corresponding ProxyClient on the server side
 * removed. Other clients will be notified that the client has left.
 * <li><code>PING</code> Pings originate from the server side and are sent to
 * every client at an interval of TinyMessageServer.PINGINTERVAL in milliseconds
 * (now set at 5000, or 5 seconds).
 * * <li><code>SERVERLOG</code> Used by a client to send a message to the server for logging
 <li><code>SHUTDOWN</code> This is the server telling a client to shut down
 * </ul>
 * 
 * @author heddle
 *
 */
public enum MessageType {

	CLIENT, HANDSHAKE, LOGOUT, PING, SERVERLOG, SUBSCRIBE, SHUTDOWN, UNSUBSCRIBE;

	/**
	 * Obtain the name from the value.
	 * 
	 * @param value
	 *            the value to match.
	 * @return the name, or "UNKNOWN".
	 */
	public static String getName(int value) {
		MessageType mt = getMessageType(value);

		return (mt == null) ? "UNKNOWN" : mt.name();
	}

	/**
	 * Obtain the enum from the value.
	 * 
	 * @param value
	 *            the ordinal value to match.
	 * @return the matching enum, or <code>null</code>.
	 */
	public static MessageType getMessageType(int value) {
		if (value < 0) {
			return null;
		}
		MessageType msgtypes[] = MessageType.values();

		if (value >= msgtypes.length) {
			return null;
		}

		return msgtypes[value];
	}

	/**
	 * Is this a valid message type?
	 * 
	 * @param value
	 *            the value to match.
	 * @return <code>true</code> if this is a valid type.
	 */
	public static boolean isValidMessageType(int value) {
		return (getMessageType(value) != null);
	}

}
