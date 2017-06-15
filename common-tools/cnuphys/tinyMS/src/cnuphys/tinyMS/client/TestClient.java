package cnuphys.tinyMS.client;

import cnuphys.tinyMS.common.BadSocketException;
import cnuphys.tinyMS.message.Message;

public class TestClient extends Client {
	
	private static int count = 1;

	public TestClient() throws BadSocketException {
		super("TestClient " + (count++), null, -1);
	}
	
	/**
	 * This is a good place for clients to do custom initializations
	 * such as subscriptions. It is not necessary, but convenient. It
	 * is called at the end of the constructor.
	 */
	public void initialize() {
		System.err.println("TestClient initialization");
		subscribe("Scalars");
		subscribe("Triggers");
		subscribe("Alarms");
		
		String name = getClientName();
		if ((name.contains("TestClient")) && (name.contains("2"))) {
			testIntArray();
		}
		if ((name.contains("TestClient")) && (name.contains("1"))) {
			testDoubleArray();
			testString();
		}
	}
	
	//test send an array of ints
	private void testString() {
		Message message = Message.createMessage(getId(), "Triggers");
		message.addPayload("Gimme a break, man!");
		send(message);
	}

	
	//test send an array of ints
	private void testIntArray() {
		Message message = Message.createMessage(getId(), "Triggers");
		
		int array[] = {1,2,3,4,5,6,7,8,9};
		message.addPayload(array);
		send(message);
	}
	
	//test send an array of doubles
	private void testDoubleArray() {
		Message message = Message.createMessage(getId(), "Triggers");
		
		double array[] = {1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8, 9.9};
		message.addPayload(array);
		
		System.err.println("Client: [" + getClientName() + "]  sending test int array");
		send(message);
	}


}
