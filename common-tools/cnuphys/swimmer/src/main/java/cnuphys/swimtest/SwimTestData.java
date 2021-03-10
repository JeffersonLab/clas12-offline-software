package cnuphys.swimtest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cnuphys.magfield.MagneticFields;
import cnuphys.swim.SwimTrajectory;

public class SwimTestData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3683250826826946912L;
	public String torusFile;
	public String solenoidFile;
	public String javaVersion;
	public double rMax;
	public double sMax;
	public double stepSize;
	public double distanceBetweenSaves;
	
	public int[] charge;
	public double xo[];
	public double yo[];
	public double zo[];
	public double p[];
	public double theta[];
	public double phi[];
	
	public SwimTrajectory[] results;
	public transient SwimTrajectory[] testResults;
	
	public SwimTestData(double rMax, double sMax, double stepSize, double distanceBetweenSaves, int n) {
		javaVersion = System.getProperty("java.version");
		torusFile = new String(MagneticFields.getInstance().getTorusBaseName());
		solenoidFile = new String(MagneticFields.getInstance().getSolenoidBaseName());

		this.rMax = rMax;
		this.sMax = sMax;
		this.stepSize = stepSize;
		this.distanceBetweenSaves = distanceBetweenSaves;
		
		charge = new int[n];
		xo = new double[n];
		yo = new double[n];
		zo = new double[n];
		p = new double[n];
		theta = new double[n];
		phi = new double[n];
		results = new SwimTrajectory[n];
	}
	
	public int count() {
		return (xo == null) ? 0 : xo.length;
	}
	
	/**
	 * Reads a serializable object from a file.
	 * 
	 * @param fullfn the full path.
	 * @return the deserialized object.
	 */
	public static SwimTestData serialRead(String fullfn) {

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
		return (SwimTestData)obj;
	}
	
	/**
	 * serialWrite writes out a SwimTestData object to a file.
	 * 
	 * @param obj    the serializable object.
	 * 
	 * @param fullfn the full path.
	 */
	public static void serialWrite(SwimTestData obj, String fullfn) {

		FileOutputStream f = null;

		ObjectOutput s = null;

		try {
			f = new FileOutputStream(fullfn);
			s = new ObjectOutputStream(f);
			System.err.println("Serializing test data...");
			s.writeObject(obj);
			System.err.println("done.");
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
