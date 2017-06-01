package cnuphys.tinyMS.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Contains static methods for serializing and deserializing from byte arrays
 * and files.
 * 
 * @author heddle
 *
 */
public class SerialIO {

	/**
	 * Deserializes a serializable object from a file.
	 * 
	 * @param fullfn
	 *            the full path of the file containing the serialized object.
	 * @return the deserialized object.
	 */
	public static Object serialRead(String fullfn) {

		FileInputStream f = null;
		ObjectInput s = null;
		Object obj = null;

		try {
			f = new FileInputStream(fullfn);
			s = new ObjectInputStream(f);
			obj = s.readObject();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			if (f != null) {

				try {
					f.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (s != null) {
				try {
					s.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return obj;
	}

	/**
	 * Deserializes a serializable object from a byte array
	 * 
	 * @param bytes
	 *            the byte array containing the serialized object
	 * @return the deserialized object
	 */
	public static Object serialRead(byte[] bytes) {

		ByteArrayInputStream f = null;

		ObjectInput s = null;
		Object obj = null;

		try {
			f = new ByteArrayInputStream(bytes);
			s = new ObjectInputStream(f);
			obj = s.readObject();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			if (f != null) {

				try {
					f.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (s != null) {
				try {
					s.close();
				}

				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return obj;
	}

	/**
	 * Serializes a serializable object to a file.
	 * 
	 * @param obj
	 *            the serializable object.
	 * 
	 * @param fullfn
	 *            the full path.
	 */
	public static void serialWrite(Serializable obj, String fullfn) {

		FileOutputStream f = null;

		ObjectOutput s = null;

		try {
			f = new FileOutputStream(fullfn);
			s = new ObjectOutputStream(f);
			s.writeObject(obj);
			s.flush();
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		finally {

			if (f != null) {
				try {
					f.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (s != null) {

				try {
					s.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Serializes a serializable object to a byte array.
	 * 
	 * @param obj
	 *            the serializable object.
	 * @return the array of bytes.
	 */
	public static byte[] serialWrite(Serializable obj) {

		ByteArrayOutputStream f = null;
		ObjectOutput s = null;

		byte[] bytes = null;

		try {
			f = new ByteArrayOutputStream();
			s = new ObjectOutputStream(f);
			s.writeObject(obj);
			s.flush();

			bytes = f.toByteArray();
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			if (f != null) {
				try {
					f.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (s != null) {
				try {
					s.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return bytes;
	}
}