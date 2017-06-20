package cnuphys.tinyMS.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketException;

import cnuphys.tinyMS.common.DataType;
import cnuphys.tinyMS.common.SerialIO;
import cnuphys.tinyMS.log.Log;

public class Message {

	// the message header
	private Header _header;

	// the payload is always stored as an object, but it is written and
	// restored by type, e.g. and array of atomics or a serialized object
	// or a StreamedOutputPayload
	private Object _payload;

	// for data types that use serialization
	private byte[] _serializedBytes;

	/**
	 * Create a message with just a header. This is private. The clients should
	 * use the static createMessage methods.
	 * 
	 * @param header
	 *            the header of the message
	 */
	private Message(Header header) {
		_header = header;
	}

	/**
	 * Set a payload made of a single byte.
	 * 
	 * @param v
	 *            the byte to use as the payload.
	 */
	public void setPayload(byte v) {
		byte vals[] = { v };
		setPayload(vals);
	}

	/**
	 * Set a payload made of a single short.
	 * 
	 * @param v
	 *            the short to use as the payload.
	 */
	public void setPayload(short v) {
		short vals[] = { v };
		setPayload(vals);
	}

	/**
	 * Set a payload made of a single int.
	 * 
	 * @param v
	 *            the int to use as the payload.
	 */
	public void setPayload(int v) {
		int vals[] = { v };
		setPayload(vals);
	}

	/**
	 * Set a payload made of a single long.
	 * 
	 * @param v
	 *            the long to use as the payload.
	 */
	public void setPayload(long v) {
		long vals[] = { v };
		setPayload(vals);
	}

	/**
	 * Set a payload made of a single float.
	 * 
	 * @param v
	 *            the float to use as the payload.
	 */
	public void setPayload(float v) {
		float vals[] = { v };
		setPayload(vals);
	}

	/**
	 * Set a payload made of a single double.
	 * 
	 * @param v
	 *            the double to use as the payload.
	 */
	public void setPayload(double v) {
		double vals[] = { v };
		setPayload(vals);
	}

	/**
	 * Add (or replace) a payload to the message. If the payload doesn't match
	 * any of the recognized data types, a null payload is used.
	 * 
	 * @param payload
	 *            the payload to add to the message. Should be one of the known
	 *            data types. @see cnuphys.tinyMS.common.DataType
	 */
	public void setPayload(Object payload) {

		int len = 0;
		DataType dtype = DataType.NO_DATA;

		_payload = payload;
		
		/*
		 * len is ALWAYS the length in bytes, so must multiply by the size of the atoming type
		 */

		if (payload == null) {
		} else if (payload instanceof byte[]) {
			dtype = DataType.BYTE_ARRAY;
			// for a byte array, len is the length in bytes
			len = ((byte[]) payload).length;
		} else if (payload instanceof short[]) {
			dtype = DataType.SHORT_ARRAY;
			// for a short array, len is the number of elements
			len = 2*((short[]) payload).length;
		} else if (payload instanceof int[]) {
			dtype = DataType.INT_ARRAY;
			// for an int array, len is the number of elements
			len = 4*((int[]) payload).length;
		} else if (payload instanceof long[]) {
			dtype = DataType.LONG_ARRAY;
			// for a long array, len is the number of elements
			len = 8*((long[]) payload).length;
		} else if (payload instanceof float[]) {
			dtype = DataType.FLOAT_ARRAY;
			// for a float array, len is the number of elements
			len = 4*((float[]) payload).length;
		} else if (payload instanceof double[]) {
			dtype = DataType.DOUBLE_ARRAY;
			// for a double array, len is the number of elements
			len = 8*((double[]) payload).length;
		} else if (payload instanceof String) {
			dtype = DataType.STRING;
			// for a single string, len is the char length of the string
			len = ((String) payload).length();
		} else if (payload instanceof String[]) {
			dtype = DataType.STRING_ARRAY;
			_serializedBytes = SerialIO.serialWrite((Serializable) _payload);
			len = _serializedBytes.length;
		} else if (payload instanceof Serializable) {
			dtype = DataType.SERIALIZED_OBJECT;
			_serializedBytes = SerialIO.serialWrite((Serializable) _payload);
			len = _serializedBytes.length;
			System.out.println("SENDING SERIALIZED OBJECT bytes len: " + len);
		} else if (payload instanceof StreamedOutputPayload) {
			dtype = DataType.STREAMED;
			_serializedBytes = ((StreamedOutputPayload) payload).getBytes();
			len = _serializedBytes.length;
			System.out.println("SENDING STREAMED OBJECT bytes len: " + len);
		} else {
			System.out.println("UNKNOWN payload type in Message.setPayload");
			_payload = null;
		}

		_header.setDataType(dtype);
		_header.setDataLength(len);
	}

	/**
	 * Send a message to the server to be logged
	 * 
	 * @param clientId
	 *            the client id
	 * @param level
	 *            the level of the log message
	 * @param logStr
	 *            the actual string to log at the server
	 * @return the message
	 */
	public static Message createServerLogMessage(int clientId, Log.Level level, String logStr) {
		Header header = new Header(clientId, MessageType.SERVERLOG, (short) level.ordinal(), Header.SERVER_TOPIC);
		Message message = new Message(header);
		message.setPayload(logStr);
		return message;
	}

	/**
	 * Convenience routine for the server to create a ping message. The ping
	 * message originates on the server. The client sends it back.
	 * 
	 * @param clientId
	 *            the id of the destination client
	 * @return a Message with type MessageType.PING. These are initiated by the
	 *         server.
	 */
	public static Message createPingMessageMessage(int clientId) {
		Message message = new Message(new Header(clientId, MessageType.PING));

		// send one piece of data, the current time in nansec
		message.setPayload(System.nanoTime());
		return message;
	}

	/**
	 * Convenience routine to create a handshake message. These originate on the
	 * server and are sent to the client when he first connects. The response
	 * from the client is used to verify the client as trusted. This message,
	 * when it arrives at the client, will also serve to notify the client of
	 * his own Id.
	 * 
	 * @param clientId
	 *            the id of the destination client
	 * @return a message of type Message.HANDSHAKE
	 */
	public static Message createHandshakeMessage(int clientId) {
		Header header = new Header(clientId, MessageType.HANDSHAKE);
		return new Message(header);
	}

	/**
	 * Convenience routine to create a logout message. It should only originate
	 * from a client, the client is telling the server he is logging out.
	 * 
	 * @param clientId
	 *            the id of the client logging out
	 * @return a message of type Message.LOGOUT
	 */
	public static Message createLogoutMessage(int clientId) {
		Header header = new Header(clientId, MessageType.LOGOUT);
		return new Message(header);
	}

	/**
	 * Convenience routine to create a subscribe message.
	 * 
	 * @param clientId
	 *            the id of the client subscribing
	 * @param topic
	 *            the topic
	 * @return a message of type Message.SUBSCRIBE
	 */
	public static Message createSubscribeMessage(int clientId, String topic) {
		Header header = new Header(clientId, MessageType.SUBSCRIBE);
		Message message = new Message(header);
		message.setPayload(topic);
		return message;
	}

	/**
	 * Convenience routine to create an unsubscribe message.
	 * 
	 * @param clientId
	 *            the id of the client unsubscribing
	 * @param topic
	 *            the topic
	 * @return a message of type Message.UNSUBSCRIBE
	 */
	public static Message createUnsubscribeMessage(int clientId, String topic) {
		Header header = new Header(clientId, MessageType.UNSUBSCRIBE);
		Message message = new Message(header);
		message.setPayload(topic);
		return message;
	}

	/**
	 * Convenience routine to create a shutdown message. It should only
	 * originate from the server, telling the client to shutdown.
	 * 
	 * @param clientId
	 *            the id of the client being shut down
	 * @return a message of type Message.SHUTDOWN
	 */
	public static Message createShutdownMessage(int clientId) {
		Header header = new Header(clientId, MessageType.SHUTDOWN);
		return new Message(header);
	}

	/**
	 * Convenience routine to create a client message with no payload. You can
	 * then Set a payload with {@link setPayload}. This uses the default tag of
	 * 0.
	 * 
	 * @param clientId
	 *            the id of the client sending the message
	 * @param topic
	 *            the topic of the message
	 * @return a message of type Message.DATA
	 */
	public static Message createMessage(int clientId, String topic) {
		return createMessage(clientId, (short) 0, topic);
	}

	/**
	 * Convenience routine to create a client message with no payload. You can
	 * then Set a payload with {@link setPayload}.
	 * 
	 * @param clientId
	 *            the id of the client sending the message
	 * @param tag
	 *            an optional tag
	 * @param topic
	 *            the topic of the message
	 * @return a message of type Message.DATA
	 */
	public static Message createMessage(int clientId, short tag, String topic) {
		Header header = new Header(clientId, MessageType.CLIENT, tag, topic);
		return new Message(header);
	}

	/**
	 * Get the byte array if the payload is a byte array.
	 * 
	 * @return the byte array if the payload is a byte array.
	 */
	public byte[] getByteArray() {
		if ((getDataType() != DataType.BYTE_ARRAY) || (getDataLength() < 1) || (_payload == null)) {
			return null;
		}
		return (byte[]) _payload;
	}

	/**
	 * Get the short array if the payload is a short array.
	 * 
	 * @return the short array if the payload is a short array.
	 */
	public short[] getShortArray() {
		if ((getDataType() != DataType.SHORT_ARRAY) || (getDataLength() < 1) || (_payload == null)) {
			return null;
		}
		return (short[]) _payload;
	}

	/**
	 * Get the int array if the payload is a int array.
	 * 
	 * @return int byte array if the payload is a int array.
	 */
	public int[] getIntArray() {
		if ((getDataType() != DataType.INT_ARRAY) || (getDataLength() < 1) || (_payload == null)) {
			return null;
		}
		return (int[]) _payload;
	}

	/**
	 * Get the long array if the payload is a long array.
	 * 
	 * @return the long array if the payload is a long array.
	 */
	public long[] getLongArray() {
		if ((getDataType() != DataType.LONG_ARRAY) || (getDataLength() < 1) || (_payload == null)) {
			return null;
		}
		return (long[]) _payload;
	}

	/**
	 * Get the float array if the payload is a float array.
	 * 
	 * @return the float array if the payload is a float array.
	 */
	public float[] getFloatArray() {
		if ((getDataType() != DataType.FLOAT_ARRAY) || (getDataLength() < 1) || (_payload == null)) {
			return null;
		}
		return (float[]) _payload;
	}

	/**
	 * Get the double array if the payload is a double array.
	 * 
	 * @return the double array if the payload is a double array.
	 */
	public double[] getDoubleArray() {
		if ((getDataType() != DataType.DOUBLE_ARRAY) || (getDataLength() < 1) || (_payload == null)) {
			return null;
		}
		return (double[]) _payload;
	}

	/**
	 * Get the payload as a String, if it is a String
	 * 
	 * @return the payload as a single String object.
	 */
	public String getString() {
		if ((getDataType() != DataType.STRING) || (getDataLength() < 1) || (_payload == null)) {
			return null;
		}
		return (String) _payload;
	}

	/**
	 * Get the payload as a String array, if it is a String array
	 * 
	 * @return the payload as a String array object.
	 */
	public String[] getStringArray() {
		if ((getDataType() != DataType.STRING_ARRAY) || (getDataLength() < 1) || (_payload == null)) {
			return null;
		}
		return (String[]) _payload;
	}

	/**
	 * Get the payload as a serializable object.
	 * 
	 * @return the payload as a serialized object.
	 */
	public Object getSerializedObject() {
		if ((getDataType() != DataType.SERIALIZED_OBJECT) || (getDataLength() < 1) || (_payload == null)) {
			return null;
		}

		return _payload;
		// byte[] bytes = (byte[]) _payload;
		// if ((bytes == null) || (bytes.length < 1)) {
		// return null;
		// }
		//
		// try {
		// Object obj = SerialIO.serialRead(bytes);
		// return obj;
		// } catch (Exception e) {
		// Log.getInstance().exception(e);
		// }
		// return null;
	}

	/**
	 * Get the payload as a StreamedInputPayload.
	 * 
	 * @return the payload as a StreamedInputPayload
	 */

	public StreamedInputPayload getStreamedInputPayload() {
		return (StreamedInputPayload) _payload;
		// if ((getDataType() != DataType.STREAMED) || (getDataLength() < 1) ||
		// (_payload == null)) {
		// return null;
		// }
		//
		// byte[] bytes = (byte[]) _payload;
		// if ((bytes == null) || (bytes.length < 1)) {
		// return null;
		// }
		//
		// try {
		// return StreamedInputPayload.fromBytes(bytes);
		// } catch (Exception e) {
		// Log.getInstance().exception(e);
		// }
		// return null;

	}

	/**
	 * Get the tag of the message. The tag is not used internally. It is an
	 * extra (and optional) id hook that can be used by clients.
	 * 
	 * @return the message tag
	 */
	public short getTag() {
		return _header.getTag();
	}

	/**
	 * Get the topic of the message
	 * 
	 * @return the message topic
	 */
	public String getTopic() {
		return _header.getTopic();
	}

	/**
	 * Get the Id of the client. The server itself has an Id of zero.
	 * 
	 * @return the client Id, or 0 for the server
	 */
	public int getClientId() {
		return _header.getClientId();
	}

	/**
	 * Get the type of the message
	 * 
	 * @return the messageType
	 */
	public MessageType getMessageType() {
		return _header.getMessageType();
	}

	/**
	 * Get the type of data we are delivering. Might be NODDATA for dataless
	 * messages)
	 * 
	 * @return the dataType. Specifies what type of array will follow in the
	 *         payload.
	 */
	public DataType getDataType() {
		return _header.getDataType();
	}

	/**
	 * If true, the bytes were swapped when creating this header from a read of
	 * a DataInputStream.
	 * 
	 * @return <code>true</code> if the header required a swap to match the
	 *         magic word. If so, the payload will require swapping too.
	 */
	public boolean isSwap() {
		return _header.isSwap();
	}

	/**
	 * Get the length of the payload data array. This is not a byte count but an
	 * entry count. E.g., if the payload is a int array with two entries, this
	 * should return 2.
	 * 
	 * @return the length of the data array.
	 */
	public int getDataLength() {
		return _header.getDataLength();
	}

	/**
	 * Read a message from an input stream
	 * 
	 * @param inputStream
	 *            the stream to read from
	 * @return the message, if successful
	 * @throws IOException
	 * @throws EOFException
	 * @throws SocketException
	 */
	protected static Message readMessage(DataInputStream inputStream)
			throws IOException, EOFException, SocketException {
		Header header = Header.readHeader(inputStream);
		Message message = new Message(header);

		DataType dtype = header.getDataType();
		int len = header.getDataLength();

		if ((dtype == DataType.NO_DATA) || (len < 1)) {
			return message;
		}

		readPayload(message, inputStream, dtype, len);
		return message;
	}

	// read the payload
	private static void readPayload(Message message, DataInputStream inputStream, DataType dtype, int len)
			throws IOException {
		
		/*
		 * len is ALWAYS the length in bytes, so must divide by the size of the atomic type
		 * to get the array len
		 */

		switch (dtype) {
		case BYTE_ARRAY:
			byte bytes[] = new byte[len];
			inputStream.readFully(bytes);
			message._payload = bytes;
			break;

		case SHORT_ARRAY:
			len /= 2;
			short shorts[] = new short[len];
			for (int i = 0; i < len; i++) {
				shorts[i] = inputStream.readShort();
			}
			message._payload = shorts;
			break;

		case INT_ARRAY:
			len /= 4;
			int ints[] = new int[len];
			for (int i = 0; i < len; i++) {
				ints[i] = inputStream.readInt();
			}
			message._payload = ints;
			break;

		case LONG_ARRAY:
			len /= 8;
			long longs[] = new long[len];
			for (int i = 0; i < len; i++) {
				longs[i] = inputStream.readLong();
			}
			message._payload = longs;
			break;

		case FLOAT_ARRAY:
			len /= 4;
			float floats[] = new float[len];
			for (int i = 0; i < len; i++) {
				floats[i] = inputStream.readFloat();
			}
			message._payload = floats;
			break;

		case DOUBLE_ARRAY:
			len /= 8;
			double doubles[] = new double[len];
			for (int i = 0; i < len; i++) {
				doubles[i] = inputStream.readDouble();
			}
			message._payload = doubles;
			break;

		case STRING:
			char chars[] = new char[len];
			for (int i = 0; i < len; i++) {
				chars[i] = inputStream.readChar();
			}
			message._payload = new String(chars);
			break;

		// treat a string array as a serialized object
		case STRING_ARRAY:
			message._serializedBytes = new byte[len];
			inputStream.readFully(message._serializedBytes);
			Object object = SerialIO.serialRead(message._serializedBytes);
			message._payload = object;
			break;

		case SERIALIZED_OBJECT:
			message._serializedBytes = new byte[len];
			inputStream.readFully(message._serializedBytes);
			object = SerialIO.serialRead(message._serializedBytes);
			message._payload = object;
			break;

		case STREAMED:
			message._serializedBytes = new byte[len];
			inputStream.readFully(message._serializedBytes);
			message._payload = StreamedInputPayload.fromBytes(message._serializedBytes);
			break;

		case NO_DATA:
			message._payload = null;
			break;
		}
	}

	/**
	 * Write the message to an output stream.
	 * 
	 * @param outputStream
	 *            the output stream to write to.
	 * @throws IOException
	 */
	protected void writeMessage(DataOutputStream outputStream) throws IOException {

		// Log.getInstance().info("Writing message type: " +
		// _header.getMessageTypeName() + " source: "
		// + idString(_header.getSourceId()) + " destination: " +
		// idString(_header.getDestinationId()));
		_header.writeHeader(outputStream);
		if ((getDataType() != DataType.NO_DATA) && (getDataLength() > 0) && (_payload != null)) {
			writePayload(outputStream);
		}

		outputStream.flush();
	}

	// write the payload to an output stream

	private void writePayload(DataOutputStream outputStream) throws IOException {
		
		/*
		 * getDataLength() is ALWAYS the length in bytes, so must divide by the size of the atomic type
		 */

		try {
			switch (getDataType()) {
			case BYTE_ARRAY:
				outputStream.write((byte[]) _payload);
				break;

			case SHORT_ARRAY:
				short shorts[] = (short[]) _payload;
				for (int i = 0; i < getDataLength()/2; i++) {
					outputStream.writeShort(shorts[i]);
				}
				break;

			case INT_ARRAY:
				int ints[] = (int[]) _payload;
				for (int i = 0; i < getDataLength()/4; i++) {
					outputStream.writeInt(ints[i]);
				}
				break;

			case LONG_ARRAY:
				long longs[] = (long[]) _payload;
				for (int i = 0; i < getDataLength()/8; i++) {
					outputStream.writeLong(longs[i]);
				}
				break;

			case FLOAT_ARRAY:
				float floats[] = (float[]) _payload;
				for (int i = 0; i < getDataLength()/4; i++) {
					outputStream.writeFloat(floats[i]);
				}
				break;

			case DOUBLE_ARRAY:
				double doubles[] = (double[]) _payload;
				for (int i = 0; i < getDataLength()/8; i++) {
					outputStream.writeDouble(doubles[i]);
				}
				break;

			case STRING:
				String str = (String) _payload;
				outputStream.writeChars(str);
				break;

			case STRING_ARRAY:
				outputStream.write(_serializedBytes);
				break;

			case SERIALIZED_OBJECT:
				outputStream.write(_serializedBytes);
				break;

			case STREAMED:
				outputStream.write(_serializedBytes);
				break;

			case NO_DATA:
				break;
			} // end switch
		} catch (SocketException e) {

			String ms = "Socket exception when writing a mesage payload." + 
			"\nThe message data type is " + getDataType();
			System.out.println(ms);
			e.printStackTrace();
		}
	}

	/**
	 * Farm out a message for a processor
	 * 
	 * @param processor
	 *            the processor
	 */
	public void process(IMessageProcessor processor) {

		if (processor == null) {
			return;
		}

		if (!processor.accept(this)) {
			return;
		}

		// first peek at message
		processor.peekAtMessage(this);

		switch (getMessageType()) {
		case LOGOUT:
			processor.processLogoutMessage(this);
			break;

		case SHUTDOWN:
			processor.processShutdownMessage(this);
			break;

		case CLIENT:
			processor.processClientMessage(this);
			break;

		case HANDSHAKE:
			processor.processHandshakeMessage(this);
			break;

		case PING:
			processor.processPingMessage(this);
			break;

		case SERVERLOG:
			processor.processServerLogMessage(this);
			break;

		case SUBSCRIBE:
			processor.processSubscribeMessage(this);
			break;

		case UNSUBSCRIBE:
			processor.processUnsubscribeMessage(this);
			break;

		}
	}

}
