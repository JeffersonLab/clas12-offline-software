package cnuphys.tinyMS.server;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cnuphys.tinyMS.client.Client;
import cnuphys.tinyMS.client.ClientSupport;

/**
 * A Test program for the TinyMessageServer
 * 
 * @author heddle
 *
 */
public class TestServer {

	public static void main(String arg[]) {

		// create a server
		try {
			final TinyMessageServer server = new TinyMessageServer("Test_Server");

			// try to find a local server
			final Client client1 = ClientSupport.findLocalServer("client 1");
			final Client client2 = ClientSupport.findLocalServer("client 2");

			TimerTask task = new TimerTask() {

				@Override
				public void run() {
					if (client2 != null) {
						ProxyClient pc = server.getProxyClient(client2.getId());
						if (pc != null) {
							pc.shutdown();
						}
					}
				}

			};

			Timer timer = new Timer();
			// one time schedule
			timer.schedule(task, 5000L);

		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

}
