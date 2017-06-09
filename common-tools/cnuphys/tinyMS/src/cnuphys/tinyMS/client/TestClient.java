package cnuphys.tinyMS.client;

import cnuphys.tinyMS.common.BadSocketException;

public class TestClient extends Client {

	public TestClient() throws BadSocketException {
		super("TestClient", null, -1);
	}
	
	/**
	 * This is a good place for clients to do custom initializations
	 * such as subscriptions. It is not necessary, but convenient. It
	 * is called at the end of the constructor.
	 */
	public void initialize() {
		System.err.println("TestClient initialization");
	}

}
