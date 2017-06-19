package cnuphys.tinyMS.server;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cnuphys.tinyMS.client.DefaultClient;
import cnuphys.tinyMS.client.ClientSupport;
import cnuphys.tinyMS.client.TestClient;
import cnuphys.tinyMS.common.BadSocketException;

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
			try {
				final DefaultClient client1 = ClientSupport.findLocalServer("client 1");
				final DefaultClient client2 = ClientSupport.findLocalServer("client 2");
				final DefaultClient client3 = ClientSupport.findLocalServer("client 3");
				final TestClient client4 = new TestClient(false);
				final TestClient client5 = new TestClient(true);
//				testShutdown(server, client2);
//				testLogout(server, client3);
			}
			catch (BadSocketException e) {
				e.printStackTrace();
			}
			


		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private static void testShutdown(TinyMessageServer server, DefaultClient client) {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				if (client != null) {
					ProxyClient pc = server.getProxyClient(client.getId());
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

	
	private static void testLogout(TinyMessageServer server, DefaultClient client) {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				if (client != null) {
					client.logout();
				}
			}

		};

		Timer timer = new Timer();
		// one time schedule
		timer.schedule(task, 15000L);
	}

}
