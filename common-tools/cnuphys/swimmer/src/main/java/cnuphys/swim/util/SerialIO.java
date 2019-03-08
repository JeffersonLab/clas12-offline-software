package cnuphys.swim.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerialIO {

	/**
	 * Reads a serializable object from a file.
	 * 
	 * @param fullfn the full path.
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
		} catch (Exception e) {
			System.err.println("Exception in serialRead: " + e.getMessage());
		}

		finally {
			if (f != null) {

				try {
					f.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (s != null) {
				try {
					s.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return obj;
	}

	/**
	 * serialRead reads a serializable object from a byte array
	 * 
	 * @param bytes the byte array
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
		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			if (f != null) {

				try {
					f.close();
				} catch (Exception e) {
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
	 * serialWrite writes out a serializable object to a file.
	 * 
	 * @param obj    the serializable object.
	 * 
	 * @param fullfn the full path.
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
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (s != null) {

				try {
					s.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * serialWrite writes out a serializable object to a byte array.
	 * 
	 * @param obj the serializable object.
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
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (s != null) {
				try {
					s.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return bytes;
	}
}