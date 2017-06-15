package cnuphys.tinyMS.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StreamedOutputPayload extends DataOutputStream {

	private ByteArrayOutputStream _baStream;

	private StreamedOutputPayload(ByteArrayOutputStream baStream) {
		super(baStream);
		_baStream = baStream;
	}

	/**
	 * Created a StreamedOutputObject
	 * @return a StreamedOutputObject
	 */
	public static StreamedOutputPayload createStreamedOutputPayload() {
		ByteArrayOutputStream baStream = new ByteArrayOutputStream();
		return new StreamedOutputPayload(baStream);
	}

	/**
	 * Get the object's bytes
	 * 
	 * @return the bytes of the objects
	 */
	public byte[] getBytes() {
		try {
			flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return _baStream.toByteArray();
	}
	
	
	/**
	 * Get the payload data length in bytes
	 * @return the payload data length in bytes
	 */
	public int length() {
		byte bytes[] = getBytes();
		return (bytes == null) ? 0 : bytes.length;
	}
	
	public static void main(String arg[]) {
		StreamedOutputPayload so = createStreamedOutputPayload();
		
		try {
			so.writeInt(7);
			so.writeBoolean(false);
			so.writeBoolean(true);
			
			so.close();
			byte[] bytes = so.getBytes();
			System.out.println("INPUT byte count = " + bytes.length);
			System.out.println();
			
			//now read back
			StreamedInputPayload si = StreamedInputPayload.fromBytes(bytes);
			System.out.println("OUTPUT byte count = " + si.length());

			int vi = si.readInt();
			System.out.println("read int: " + vi);
			
			boolean vb = si.readBoolean();
			System.out.println("read boolean: " + vb);
			vb = si.readBoolean();
			System.out.println("read boolean: " + vb);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
