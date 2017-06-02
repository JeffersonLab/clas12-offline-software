package cnuphys.tinyMS.server;

import java.io.IOException;

import cnuphys.tinyMS.log.Log;
import cnuphys.tinyMS.message.Message;
import cnuphys.tinyMS.message.MessageProcessor;
import cnuphys.tinyMS.message.MessageType;
import cnuphys.tinyMS.server.gui.ServerFrame;

public class ServerMessageProcessor extends MessageProcessor {

	// the log
	private Log _log = Log.getInstance();

	// the server
	private TinyMessageServer _server;

	/**
	 * Create a message processor for the server
	 * @param server the server owner
	 */
	public ServerMessageProcessor(TinyMessageServer server) {
		_server = server;
	}

	/**
	 * A message is about to be farmed out to the appropriate handler.
	 * This allows you to take a peek at it first.
	 * @param message the message
	 */
	public void peekAtMessage(Message message) {
		ProxyClient proxyClient = _server.getSender(message);
		if (proxyClient != null) {
			proxyClient.incrementMessageCount();
		}
	}

	// this message is a client voluntarily logging out.
	// That is, the message originated from the client
	@Override
	public void processLogoutMessage(Message message) {
		ProxyClient proxyClient = _server.getSender(message);
		if (proxyClient == null) {
			return;
		}

		try {
			_log.info("closing a connection for client: " + proxyClient.getClientName());
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
	 * @message the handshake message
	 */
	@Override
	public void processHandshakeMessage(Message message) {
		System.err.println("Server Received Handshake!");

		ProxyClient sender = _server.getSender(message);
		if (sender == null) {
			_log.warning("Could not get sender of handshake");
		}
		else {
			_log.config("remote client verified: " + sender.getClientName());
			sender.setVerified(true);

			// get username
			String env[] = message.getStringArray();
			sender.startPingTimer();
			sender.setClientName(env[0]);
			sender.setUserName(env[1]);
			sender.setOSName(env[2]);
			sender.setHostName(env[3]);
			
			_log.info("*********");
			_log.info("CLIENT ID: " + sender.getId());
			_log.info("CLIENT NAME: " + sender.getClientName());
			_log.info("USER NAME: " + sender.getUserName());
			_log.info("OS NAME: " + sender.getOSName());
			_log.info("HOST NAME: " + sender.getHostName());
			fireTableDataChanged();			
		}
	}

	@Override
	// ping has made a round trip
	public void processPingMessage(Message message) {
		ProxyClient sender = _server.getSender(message);
		sender.pingArrived(message);
	}

	@Override
	public void processDataMessage(Message message) {
		_log.warning("It is rarely a good sign that a data message has arrived for the server.");
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
		String nstr = _server.getProxyClient(message.getSourceId()).getClientName();
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


	@Override
	public boolean accept(Message message) {

		// handshakes are accepted and used to verify
		// a client. Otherwise the client must be verified.

		if (message.getMessageType() == MessageType.HANDSHAKE) {
			return true;
		}

		ProxyClient sender = _server.getSender(message);
		if (sender == null) {
			_log.warning("Could not match a remote client to message sender.");
			return false;
		}

		if (sender.isVerified()) {

			// if someone else if the destination, try to forward
			if (message.getDestinationId() != Message.SERVER) {
				forwardMessage(message);
				return false;
			}

			return true;
		}
		else {
			_log.warning("Message from unverified client");
			return false;
		}

	}

	/**
	 * Broadcast a message to all clients (except the server)
	 * 
	 * @param message
	 *            the message to broadcast
	 */
	protected void broadcastMessage(Message message) {
		ProxyClient[] array = _server.getProxyClientArray();

		if (array != null) {
			for (ProxyClient client : array) {
				try {

					// don't send to sender!
					if (client.getId() != message.getSourceId()) {
						client.writeMessage(message);
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// a message arrived whose source was not the server
	protected void forwardMessage(Message message) {

		if (message.getDestinationId() == Message.BROADCAST) {
			broadcastMessage(message);
			return;
		}

		// point to point! try to find a remote client for the destination
		ProxyClient client = _server.getProxyClient(message.getDestinationId());
		if (client != null) {
			try {
				client.writeMessage(message);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Convenience routine to fire a data changed event
	 * so that the table (if present)  updates
	 */
	public void fireTableDataChanged() {
		ServerFrame gui = _server.getGui();
		if (gui != null) {
			gui.fireTableDataChanged();
		}
	}


}
