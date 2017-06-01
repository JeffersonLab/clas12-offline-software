package cnuphys.tinyMS.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import cnuphys.tinyMS.log.Log;
import cnuphys.tinyMS.message.Message;
import cnuphys.tinyMS.server.TinyMessageServer;

public class ClientSupport {

	/**
	 * Try to find any TinyMessageServer running on the local machine. If
	 * successful, return a Client connected to the found server.
	 * 
	 * @param userName
	 *            the name for the client. If <code>null</code>, the actual
	 *            login-username will be used.
	 * @return a client connecting to a TinyMessageServer or <code>null</code>
	 */
	public static Client findLocalServer(String userName) {
		// tries to find a TinyMessageServer on the local machine
		return findServer(userName, null, false);
	}

	/**
	 * Try to find any TinyMessageServer running on the local machine. If none
	 * is found, try to start one. If successful, return a Client connected to
	 * the found (or started) server. If the server was started, its name will
	 * be "Server started by " + userName.
	 * 
	 * @param userName
	 *            the name for the client. If <code>null</code>, the actual
	 *            login-username will be used.
	 * @return a client connecting to a TinyMessageServer or <code>null</code>
	 */
	public static Client findOrStartLocalServer(String userName) {
		// tries to find a TinyMessageServer on the local machine
		// if can't fine one, it will start one
		return findServer(userName, null, true);
	}

	/**
	 * Try to find any TinyMessageServer running on the provided IP address. If
	 * successful, return a Client connected to the found server.
	 * 
	 * @param userName
	 *            the name for the client. If <code>null</code>, the actual
	 *            login-username will be used.
	 * @param hostName
	 *            The host name can either be a machine name, such as
	 *            "daisy.jlab.org", or a textual representation of its IP
	 *            address. IPv6 address format is accepted. If the hostname is
	 *            null, a server on the local machine will be searched for.
	 * @return a client connecting to a TinyMessageServer or <code>null</code>
	 */
	public static Client findServer(String userName, String hostName) {
		return findServer(userName, hostName, false);
	}

	/**
	 * Try to find any TinyMessageServer running on the provided IP address. If
	 * successful, return a Client connected to the found server.
	 * 
	 * @param userName
	 *            the name for the client. If <code>null</code>, the actual
	 *            login-username will be used.
	 * @param hostName
	 *            The host name can either be a machine name, such as
	 *            "daisy.jlab.org", or a textual representation of its IP
	 *            address. IPv6 address format is accepted. If the hostname is
	 *            null, a server on the local machine will be searched for.
	 * @param startServer
	 *            if this is <code>true</code>, and if this is a request for a
	 *            local server, and if a local server is not found, this will
	 *            try to start a local server with name "Server started by " +
	 *            userName.
	 * @return a client connecting to a TinyMessageServer or <code>null</code>
	 */
	private static Client findServer(String userName, String hostName, boolean startServer) {
		// tries to find a TinyMessageServer on the local machine

		boolean local = hostName == null;

		InetAddress host = null;
		try {
			if (local) {
				host = InetAddress.getLocalHost();
			}
			else {
				host = InetAddress.getByName(hostName);
			}
		}
		catch (UnknownHostException e1) {
			e1.printStackTrace();
			return null;
		}

		// get all the default ports
		int ports[] = TinyMessageServer.DEFAULT_PORTS;

		// return the first one we find
		for (int i = 0; i < ports.length; i++) {
			try {
				Socket socket = new Socket(host, ports[i]);
				if (socket != null) {
					System.err.println("Found server at " + socket.getInetAddress().getHostAddress() + " on port: "
							+ socket.getPort());
					return new Client(userName, socket);
				}

			}
			catch (UnknownHostException e) {
			}
			catch (ConnectException e) {
			}
			catch (IOException e) {
			}
		}

		// if here no server found. Might try to start one.
		if (local && startServer) {
			String sname = "Server started by " + userName;
			System.err.println("Attempting to start a local server named: " + sname);
			try {
				TinyMessageServer server = new TinyMessageServer(sname);
				return findServer(userName, null, false);
			}
			catch (IOException e) {
				System.err.println("Attempt appears to have failed.");
			}
		}

		return null;
	}
	
	/**
	 * Send a message to be logged on the server log
	 * @param client the client source
	 * @param level the log level of the message
	 * @param logStr the string to be logged
	 */
	public static void serverLog(Client client, Log.Level level, String logStr) {
	    if (client.isClosed()) {
	    	System.err.println("client is closed.");
	    	return;
	    }
		Message message = Message.createServerLogMessage(client.getId(), level, logStr);
		client.getOutboundQueue().queue(message);
	}
	

}
