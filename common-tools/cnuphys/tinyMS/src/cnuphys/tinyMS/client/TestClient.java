package cnuphys.tinyMS.client;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cnuphys.tinyMS.common.BadSocketException;
import cnuphys.tinyMS.message.Message;
import cnuphys.tinyMS.message.StreamedInputPayload;
import cnuphys.tinyMS.message.StreamedOutputPayload;
import cnuphys.tinyMS.message.TestObject;

public class TestClient extends DefaultClient {

	private static int count = 1;
	
	//am I a sender?
	private boolean _sender;

	/**
	 * Create a TestClient
	 * @param sender if true, this will send out test messages
	 * @throws BadSocketException
	 */
	public TestClient(boolean sender) throws BadSocketException {
		super("TestClient " + (count++), null, -1);
		_sender = sender;
	}

	/**
	 * This is a good place for clients to do custom initializations such as
	 * subscriptions. It is not necessary, but convenient. It is called at the
	 * end of the constructor.
	 */
	@Override
	public void initialize() {
		System.out.println("TestClient initialization");
		subscribe("Scalars");
		subscribe("Triggers");
		subscribe("Alarms");

		//If I'm a sender, run some tests
		if (_sender) {
//			testIntArray();
//			testSerializedObject();
//			testStringArray();
//			testDoubleArray();
//			testString();
//			testByteArray();
//			testStreamedMessage();
			stressTest();
		}
	}
	
	//run a stress test
	private void stressTest() {
		
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				int count = 1;
				int len = 100;
				int maxCount = 1000000;
				
				System.out.println("Start stress test...");
				while (count <= maxCount) {
					
					if ((count % 1000) == 0) {
						System.out.println("Count: " + count + " of " + maxCount);
						System.gc();
					}
					
					long larray[] = new long[len];
					
					larray[0] = count;
					larray[len-1] = count;
					
					Message message = Message.createMessage(getId(), "Triggers");
					message.setPayload(larray);
					send(message);

					
//					try {
//						Thread.sleep(25);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}

					count++;
				}
				System.out.println("Done.");

			}

		};

		Timer timer = new Timer();
		// one time schedule
		timer.schedule(task, 2000L);
	}

	// test send an array of ints
	private void testStringArray() {
		Message message = Message.createMessage(getId(), "Triggers");
		String array[] = { "Gimme", "a", "break", "dude" };
		message.setPayload(array);
		send(message);
	}

	// test send an array of ints
	private void testString() {
		Message message = Message.createMessage(getId(), "Triggers");
		message.setPayload("Gimme a break, man!");
		send(message);
	}

	// test send an array of ints
	private void testIntArray() {
		Message message = Message.createMessage(getId(), "Triggers");

		int array[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		message.setPayload(array);
		send(message);
	}

	// test send an array of doubles
	private void testDoubleArray() {
		Message message = Message.createMessage(getId(), "Triggers");

		double array[] = { 1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8, 9.9 };
		message.setPayload(array);

		System.out.println("Client: [" + getClientName() + "]  sending test double array");
		send(message);
	}

	// test byte array
	private void testByteArray() {
		Message message = Message.createMessage(getId(), "Alarms");

		byte array[] = { 2, 4, 6, 8, 10, 12 };
		message.setPayload(array);

		System.out.println("Client: [" + getClientName() + "]  sending test byte array");
		send(message);
	}

	// test a serialized object
	private void testSerializedObject() {
		Message message = Message.createMessage(getId(), "Alarms");
		TestObject obj = new TestObject();

		message.setPayload(obj);

		System.out.println("Client: [" + getClientName() + "]  sending test serialized object");
		send(message);
	}

	// test streamed message
	private void testStreamedMessage() {
		Message message = Message.createMessage(getId(), "Scalars");
		StreamedOutputPayload so = StreamedOutputPayload.createStreamedOutputPayload();

		try {
			so.writeByte(3);
			so.writeDouble(1234.56789);
			so.writeLong(1234567);
			System.out.println("Client: [" + getClientName() + "]  sending test streamed object");

			message.setPayload(so);
			send(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processClientMessage(Message message) {
		if (message != null) {
			switch (message.getDataType()) {

			case NO_DATA:
				break;

			case BYTE_ARRAY:
				byte[] barray = message.getByteArray();
				System.out.print("\nClient: " + getClientName() + " got byte array [");
				for (byte b : barray) {
					System.out.print(b + " ");
				}
				System.out.println("]");
				break;

			case SHORT_ARRAY:
				break;

			case INT_ARRAY:
				int[] iarray = message.getIntArray();
				System.out.print("\nClient: " + getClientName() + " got int array [");
				for (int i : iarray) {
					System.out.print(i + " ");
				}
				System.out.println("]");
				break;

			case LONG_ARRAY:
				long[] larray = message.getLongArray();
				int len = larray.length;
				
				if (larray[0] != larray[len-1]) {
					System.out.println("Bad array, 1st = " + larray[0] + "  last = " + larray[len-1]);
					System.exit(1);
				}
//				System.out.print("\nClient: " + getClientName() + " got long array of len = " + len + 
//						" with 1st elem = " + larray[0] + " last elem = " + larray[len-1]);
				break;

			case FLOAT_ARRAY:
				break;

			case DOUBLE_ARRAY:
				double[] darray = message.getDoubleArray();
				System.out.print("\nClient: " + getClientName() + " got double array [");
				for (double i : darray) {
					System.out.print(i + " ");
				}
				System.out.println("]");
				break;

			case STRING_ARRAY:
				String[] sarray = message.getStringArray();
				System.out.print("\nClient: " + getClientName() + " got string array [");
				for (String s : sarray) {
					System.out.print("\"" + s + "\" ");
				}
				System.out.println("]");
				break;

			case STRING:
				String s = message.getString();
				System.out.print("\nClient: " + getClientName() + " got string \"" + s + "\"");
				break;

			case SERIALIZED_OBJECT:
				System.out.print("\nClient: " + getClientName() + " got serialized object\n");
				Object obj = message.getSerializedObject();
				System.out.println(obj.toString());
				break;

			case STREAMED:
				System.out.print("\nClient: " + getClientName() + " got streamed object\n");
				StreamedInputPayload si = message.getStreamedInputPayload();
				try {
					System.out.println("Got byte: " + si.readByte());
					System.out.println("Got double: " + si.readDouble());
					System.out.println("Got long: " + si.readLong());
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

			}
		}
	}
}
