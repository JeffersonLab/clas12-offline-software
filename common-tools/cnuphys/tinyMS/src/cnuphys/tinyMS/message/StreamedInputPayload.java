package cnuphys.tinyMS.message;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class StreamedInputPayload extends DataInputStream {
	
	private byte[] _bytes;
	
	private StreamedInputPayload(byte[] bytes) {
		super(new ByteArrayInputStream(bytes));
		_bytes = bytes;
	}
	
	/**
	 * Get the input payload from a byte array
	 * @param bytes the byte array
	 * @return the SreamedInputPayload
	 */
	public static StreamedInputPayload fromBytes(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		return new StreamedInputPayload(bytes);
	}
	
	/**
	 * Get the input payload from a message
	 * @param message the message
	 * @return the SreamedInputPayload
	 */
	public static StreamedInputPayload fromBytes(Message message) {
		if (message != null) {
			return null;
		}
		
		
		return new StreamedInputPayload(message.getByteArray());
	}

	
	/**
	 * Get the payload data length in bytes
	 * @return the payload data length in bytes
	 */
	public int length() {
		return (_bytes == null) ? 0 : _bytes.length;
	}

}
