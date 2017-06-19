package cnuphys.tinyMS.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

import cnuphys.tinyMS.common.ByteSwap;
import cnuphys.tinyMS.common.DataType;
import cnuphys.tinyMS.log.Log;

/**
 * The header for a message. The header consists of
 * 
 * 1: (int) the magic word TinyMessageServer.MAGIC_WORD=0xCEBAF 2: (int) id of
 * the source of the message (0 if server) 3: (int) id of the destination of the
 * message (0 if server) 4: (short) value of a MessageType enum 5: (short) a tag
 * for further differentiation 6: (short) value of the DataType enum 7: (int)
 * the length of the data array in the body (might be 0)
 * 
 * @author heddle
 * 
 */
public class Header {
	
	// used to validate a message and to see if byte swapping is necessary
	public static final int MAGIC_WORD = 0xCEBAF; // 846767
	
	/** A reserved topic used by all administrative messages */
	public static final String SERVER_TOPIC = "Server";

	// Id of the source, 0 for the server.
	private int _clientId;

	// The message type. Never used by clients.
	private MessageType _messageType;

	// for further differentiation
	private short _tag;

	// the type of data in the payload. Will be set when
	// the payload is added.
	private DataType _dataType;

	// the length of the data array in the payload. Will
	// be set when payload is added.
	private int _length;

	// If true, had to swap bytes when read to get
	// magic number right
	private boolean _swap = false;
	
	//the message topic. For administrative messages it is
	//always the constant (reserved) SERVER_TOPIC
	private String _topic = SERVER_TOPIC;

	/**
	 * Create a dataless Header with no user data
	 * 
	 * @param clientId
	 *            the id of the source. The server ID is 0. Client IDs start at
	 *            +1 and are handed out sequentially.
	 * @param mtype
	 *            the message type
	 * @see cnuphys.tinyMS.message.MessageType
	 */
	public Header(int clientId,  MessageType mtype) {
		this(clientId, mtype, (short) 0, SERVER_TOPIC);
	}

	/**
	 * Create a message header
	 * 
	 * @param clientId
	 *            the id of the source. The server ID is 0. Client IDs start at
	 *            +1 and are handed out sequentially.
	 * @param mtype
	 *            the message type
	 * @param tag
	 *            for any purpose
	 * @param topic the topic
	 * @see cnuphys.tinyMS.message.MessageType
	 */
	public Header(int clientId,  MessageType mtype, short tag, String topic) {
		_clientId = clientId;
		_messageType = mtype;
		_tag = tag;
		_topic = (topic == null) ? SERVER_TOPIC : new String(topic);
		_dataType = DataType.NO_DATA; // updated when payload added
		_length = 0; // will be updated when payload added
	}

	/**
	 * Get the Id of the client. The server itself has an Id of zero.
	 * 
	 * @return the clientId
	 */
	public int getClientId() {
		return _clientId;
	}


	/**
	 * Get the type of the message as a MessageType enum.
	 * 
	 * @return the messageType
	 * @see cnuphys.tinyMS.message.MessageType
	 */
	public MessageType getMessageType() {
		return _messageType;
	}

	/**
	 * Get the name of the message type
	 * @return the name of the message type
	 */
	public String getMessageTypeName() {
		return MessageType.getName(_messageType.ordinal());
	}

	/**
	 * Get the message tag.
	 * 
	 * @return the message tag.
	 */
	public short getTag() {
		return _tag;
	}
	
	/**
	 * Get the message topic
	 * @return the message topic
	 */
	public String getTopic() {
		return _topic;
	}

	/**
	 * Get the type of data we are delivering. Might be NODDATA for dataless
	 * messages)
	 * 
	 * @return the dataType. Specifies what type of array will follow in the
	 *         payload.
	 * @see cnuphys.tinyMS.common.DataType
	 */
	public DataType getDataType() {
		return _dataType;
	}

	/**
	 * If true, the bytes were swapped when creating this header from a read of
	 * a DataInputStream.
	 * 
	 * @return <code>true</code> if the header required a swap to match the
	 *         magic word. If so, the payload will require swapping too.
	 */
	public boolean isSwap() {
		return _swap;
	}

	/**
	 * Get the length of the payload data array. This is not a byte count but an
	 * entry count. E.g., if the payload is a int array with two entries, this
	 * should return 2.
	 * 
	 * @return the length of the data array.
	 */
	public int getDataLength() {
		return _length;
	}

	/**
	 * Set the data type of the payload data.
	 * 
	 * @param dtype
	 *            the data type of the payload data array.
	 */
	public void setDataType(DataType dtype) {
		_dataType = dtype;
	}

	/**
	 * Set the length of the payload array.
	 * 
	 * @param len
	 *            the length of the payload array.
	 */
	public void setDataLength(int len) {
		_length = len;
	}

	/**
	 * Read a header from an input stream. This will be called as part of
	 * reading in a message.
	 * 
	 * @param inputStream
	 * @return the Header if it is successful.
	 * @throws IOException
	 */
	protected static Header readHeader(DataInputStream inputStream) throws IOException, EOFException, SocketException {
		int word = inputStream.readInt();

		boolean swap = false;
		boolean foundMagic = word == MAGIC_WORD;

		// swap?
		if (!foundMagic) {
			word = ByteSwap.swapInt(word);
			foundMagic = (word == MAGIC_WORD);

			if (foundMagic) {
				swap = true;
			}
			else {
				Log.getInstance().error("Did not get magic word 0xCEBAF");
	//			throw new IOException("Expected to find MAGIC WORD 0xCEBAF");
			}
		}

		int clientId = inputStream.readInt();
		short mtype = inputStream.readShort();
		short tag = inputStream.readShort();
		String topic = inputStream.readUTF();
		short dtype = inputStream.readShort();
		int len = inputStream.readInt();

		if (swap) {
			clientId = ByteSwap.swapInt(clientId);
			mtype = ByteSwap.swapShort(mtype);
			dtype = ByteSwap.swapShort(dtype);
			len = ByteSwap.swapInt(len);
		}

		Header header = new Header(clientId, MessageType.getMessageType(mtype), tag, topic);

		// set data type for payload reader
		header.setDataType(DataType.getDataType(dtype));

		// set length for payload reader
		header.setDataLength(len);

		// payload reader will have to know if it must swap
		header._swap = swap;

		return header;
	}

	/**
	 * Write the header to an output stream. This will be called as part of
	 * writing a message.
	 * 
	 * @param outputStream
	 *            the output stream to write to.
	 * @throws IOException
	 */
	protected void writeHeader(DataOutputStream outputStream) throws IOException {

		try {
			outputStream.writeInt(MAGIC_WORD);
			outputStream.writeInt(_clientId);
			outputStream.writeShort((short) _messageType.ordinal());
			outputStream.writeShort(_tag);
			outputStream.writeUTF((_topic == null) ? SERVER_TOPIC : _topic);
			outputStream.writeShort((short) _dataType.ordinal());
			outputStream.writeInt(_length);
		}
		catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get a nice String representation
	 * 
	 * @return a nice String representation
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("     client: " + _clientId + "\n");
		sb.append("   msg type: " + _messageType + "\n");
		sb.append("        tag: " + _tag + "\n");
		sb.append("      topic: " + _topic + "\n");
		sb.append("  data type: " + _dataType + "\n");
		sb.append("     length: " + _length + "\n");
		return sb.toString();
	}
	
	/**
	 * Check whether a topic is acceptable
	 * @param topic the topic to check
	 * @return <code>true</code> if the topic is acceptable
	 */
	public static boolean acceptableTopic(String topic) {
		if (topic == null) {
			return false;
		}
		
		topic = topic.trim();
		
		if (topic.length() < 1) {
			return false;
		}
		
		//check any reserved
		if (SERVER_TOPIC.equalsIgnoreCase(topic)) {
			return false;
		}
		
		return true;
	}
}
