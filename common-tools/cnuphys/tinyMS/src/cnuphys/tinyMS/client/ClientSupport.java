package cnuphys.tinyMS.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import cnuphys.tinyMS.common.BadSocketException;
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
	 * @throws BadSocketException 
	 */
	public static DefaultClient findLocalServer(String userName) throws BadSocketException {
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
	 * @throws BadSocketException 
	 */
	public static DefaultClient findOrStartLocalServer(String userName) throws BadSocketException {
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
	 * @throws BadSocketException 
	 */
	public static DefaultClient findServer(String userName, String hostName) throws BadSocketException {
		return findServer(userName, hostName, false);
	}

	/**
	 * Try to find any TinyMessageServer running on the provided IP address. If
	 * successful, return a Client connected to the found server.
	 * 
	 * @param clientName
	 *            the name for the client. If <code>null</code>, the actual
	 *            login-username will be used.
	 * @param hostName
	 *            The server host name can either be a machine name, such as
	 *            "daisy.jlab.org", or a textual representation of its IP
	 *            address. IPv6 address format is accepted. If the hostname is
	 *            null, a server on the local machine will be searched for.
	 * @param startServer
	 *            if this is <code>true</code>, and if this is a request for a
	 *            local server, and if a local server is not found, this will
	 *            try to start a local server with name "Server started by " +
	 *            userName.
	 * @return a client connecting to a TinyMessageServer or <code>null</code>
	 * @throws BadSocketException 
	 */
	private static DefaultClient findServer(String clientName, String hostName, boolean startServer) throws BadSocketException {
		// tries to find a TinyMessageServer on the local machine

		boolean local = (hostName == null);

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

		// try all the default ports
		// return the first one we find
		Socket socket = getSocket(hostName, -1);
		if (socket != null) {
			return new DefaultClient(clientName, socket);
		}

		// if here no server found. Might try to start one.
		if (local && startServer) {
			String sname = "Server started by " + clientName;
			System.out.println("Attempting to start a local server named: " + sname);
			try {
				TinyMessageServer server = new TinyMessageServer(sname);
				return findServer(clientName, null, false);
			}
			catch (IOException e) {
				System.out.println("Attempt appears to have failed.");
			}
		}

		return null;
	}
	
	/**
	 * Try to create a socket for communication
	 * @param hostName the server hostname. If null, assume server is on the local host.
	 * @param port the port. If < 0, try the default ports 
	 * @return a  Socket, or null
	 */
	public static Socket getSocket(String hostName, int port) {

		boolean local = (hostName == null);

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
		
		int ports[];
		if (port < 0) {
			ports = TinyMessageServer.DEFAULT_PORTS;
		}
		else {
			ports = new int[1];
			ports[0] = port;
		}
		for (int tport : ports) {
			Socket socket;
			try {
				socket = new Socket(host, tport);
				if (socket != null) {
					System.out.println("Found server at " + socket.getInetAddress().getHostAddress() + " on port: "
							+ socket.getPort());
					return socket;
				}
			}
			catch (IOException e) {
				e.printStackTrace();
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
	public static void serverLog(DefaultClient client, Log.Level level, String logStr) {
	    if (client.isClosed()) {
	    	System.out.println("client is closed.");
	    	return;
	    }
		Message message = Message.createServerLogMessage(client.getId(), level, logStr);
		client.getOutboundQueue().queue(message);
	}
	

}
