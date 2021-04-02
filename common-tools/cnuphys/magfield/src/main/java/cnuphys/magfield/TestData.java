package cnuphys.magfield;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TestData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2968299793724503616L;
	public String torusFile;
	public String solenoidFile;
	public String javaVersion;
	
	public float x[];
	public float y[];
	public float z[];
	
	public float result[][];
	
	public transient float testResult[][];
	
	public TestData(int n) {
		javaVersion = System.getProperty("java.version");
		x = new float[n];
		y = new float[n];
		z = new float[n];
		result = new float[n][3];
	}
	
	public int count() {
		return (x == null) ? 0 : x.length;
	}
	/**
	 * Reads a serializable object from a file.
	 * 
	 * @param fullfn the full path.
	 * @return the deserialized object.
	 */
	public static TestData serialRead(String fullfn) {

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
		return (TestData)obj;
	}
	
	/**
	 * serialWrite writes out a TestData object to a file.
	 * 
	 * @param obj    the serializable object.
	 * 
	 * @param fullfn the full path.
	 */
	public static void serialWrite(TestData obj, String fullfn) {

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
	

}
