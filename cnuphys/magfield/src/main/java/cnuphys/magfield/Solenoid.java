/*
 * 
 */
package cnuphys.magfield;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * The Class Solenoid.
 *
 * @author Sebouh Paul
 * @version 1.0
 */
public final class Solenoid extends MagneticField {
	
	// private constructor
	/**
	 * Instantiates a new solenoid.
	 */
	private Solenoid() {
		setCoordinateNames("phi", "rho", "z");
	}
	

	/**
	 * Obtain a solenoid object from a binary file, probably
	 * "clas12_solenoid_fieldmap_binary.dat"
	 *
	 * @param file the file to read
	 * @return the solenoid object
	 * @throws FileNotFoundException the file not found exception
	 */
	public static Solenoid fromBinaryFile(File file)
			throws FileNotFoundException {
		Solenoid solenoid = new Solenoid();
		solenoid.readBinaryMagneticField(file);
		// is the field ready to use?
		return solenoid;
	}

	
	/**
	 * Get the field by trilinear interpolation.
	 * 
	 * @param probe
	 *            for faster results
	 * @param phi
	 *            azimuthal angle in degrees.
	 * @param rho
	 *            the cylindrical rho coordinate in cm.
	 * @param z
	 *            coordinate in cm
	 * @param result
	 *            the result
	 * @result a Cartesian vector holding the calculated field in kiloGauss.
	 */
	public void fieldCylindrical(SolenoidProbe probe, double phi, double rho, double z, float result[]) {
		
		if (isZeroField()) {
			result[X] = 0f;
			result[Y] = 0f;
			result[Z] = 0f;
			return;
		}

		if (phi < 0.0) {
			phi += 360.0;
		}

		if ((probe == null) || !FieldProbe.CACHE) {
			interpolateField(rho, z, result);
		}
		else {
			calculate(rho, z, probe, result);
		}

		// rotate onto to proper sector
		
		if (phi > 0.001) {
			double rphi = Math.toRadians(phi);
			double cos = MagneticField.cos(rphi);
			double sin = MagneticField.sin(rphi);
			double bphi = result[0];
			double brho = result[1];
			result[X] = (float) (brho * cos - bphi * sin);
			result[Y] = (float) (brho * sin + bphi * cos);
		}

		result[X] *= _scaleFactor;
		result[Y] *= _scaleFactor;
		result[Z] *= _scaleFactor;
	}

	/**
	 * Get the field by trilinear interpolation.
	 *
	 * @param phi azimuthal angle in degrees.
	 * @param rho the cylindrical rho coordinate in cm.
	 * @param z coordinate in cm
	 * @param result the result
	 * @result a Cartesian vector holding the calculated field in kiloGauss.
	 */
	@Override
	public void fieldCylindrical(double phi, double rho, double z,
			float result[]) {
		
		fieldCylindrical(null, phi, rho, z, result);
	}

	/**
	 * Convert a array used as a vector to a readable string.
	 *
	 * @param v the vector (float array) to represent.
	 * @return a string representation of the vector (array).
	 */
	// @Override
	// protected String vectorToString(float v[]) {
	// float vx = v[X] / 10;
	// float vy = v[Y] / 10;
	// float vz = v[Z] / 10;
	// float vLen = vectorLength(v) / 10;
	// String s = String.format("(%8.5f, %8.5f, %8.5f) magnitude: %8.5f T",
	// vx, vy, vz, vLen);
	// return s;
	// }

	/**
	 * main method used for testing.
	 *
	 * @param arg command line arguments
	 */
	public static void main(String arg[]) {
		// String path = null;
		//
		// if ((arg != null) && (arg.length > 0)) {
		// path = arg[0];
		// }
		//
		// if (path == null) {
		// path = "data/solenoid-srr.dat";
		// }
		//
		// File file = new File(path);
		//
		// Solenoid solenoid = null;
		// try {
		// solenoid = fromBinaryFile(file);
		// System.out.println("Created field object.");
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// System.exit(1);
		// }
		//
		// float x = 0.60f;
		// float y = 0.62f;
		// float z = 10.0f;
		// float result[] = new float[3];
		// solenoid.field(x, y, z, result);
		//
		// System.out.println("(x,y,z) = " + x + ", " + y + ", " + z);
		//
		// String fieldStr = solenoid.vectorToString(result);
		// System.out.println("Field: " + fieldStr);

		// covert the new ascii to binary
		File asciiFile = new File("../../../data/clas12SolenoidFieldMap.dat.txt");
		if (!asciiFile.exists()) {
			System.out.println("File not found: " + asciiFile.getPath());
		}
		else {
			System.out.println("File found: " + asciiFile.getPath());

			FileReader fileReader;
			try {
				fileReader = new FileReader(asciiFile);
				final BufferedReader bufferedReader = new BufferedReader(
						fileReader);

				// prepare the binary file
				String binaryFileName = "../../../data/solenoid-srr.dat";
				// String binaryFileName = "data/solenoid-srr_V3.dat";
				int nPhi = 1;
				int nRho = 601;
				int nZ = 1201;
				float phimin = 0.0f;
				float phimax = 360.0f;
				float rhomin = 0.0f;
				float rhomax = 300.0f;
				float zmin = -300.0f;
				float zmax = 300.0f;

				DataOutputStream dos = new DataOutputStream(
						new FileOutputStream(binaryFileName));
				try {
					// write the header
					dos.writeInt(0xced);
					dos.writeInt(0);// cylindrical
					dos.writeInt(0);// cylindrical
					dos.writeInt(0);
					dos.writeInt(0);
					dos.writeInt(0);
					dos.writeFloat(phimin);
					dos.writeFloat(phimax);
					dos.writeInt(nPhi);
					dos.writeFloat(rhomin);
					dos.writeFloat(rhomax);
					dos.writeInt(nRho);
					dos.writeFloat(zmin);
					dos.writeFloat(zmax);
					dos.writeInt(nZ);
					dos.writeInt(0);
					dos.writeInt(0);
					dos.writeInt(0);
					dos.writeInt(0);
					dos.writeInt(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				boolean reading = true;
				while (reading) {
					String s = nextNonComment(bufferedReader);
					// System.out.println("s: [" + s + "]");

					if (s != null) {
						String tokens[] = tokens(s, " ");
						dos.writeFloat(0f);
						dos.writeFloat(10 * Float.parseFloat(tokens[2]));
						dos.writeFloat(10 * Float.parseFloat(tokens[3]));
						// System.out.println(s);
					} else {
						reading = false;
					}
				}

				dos.close();
				System.out.println("done");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private static String[] tokens(String str, String delimiter) {

		StringTokenizer t = new StringTokenizer(str, delimiter);
		int num = t.countTokens();
		String lines[] = new String[num];

		for (int i = 0; i < num; i++) {
			lines[i] = t.nextToken();
		}

		return lines;
	}

	/**
	 * Get the next non comment line
	 * 
	 * @param bufferedReader a buffered reader which should be linked to an
	 *            ascii file
	 * @return the next non comment line (or <code>null</code>)
	 */
	private static String nextNonComment(BufferedReader bufferedReader) {
		String s = null;
		try {
			s = bufferedReader.readLine();
			if (s != null) {
				s = s.trim();
			}
			while ((s != null) && (s.startsWith("<") || s.length() < 1)) {
				s = bufferedReader.readLine();
				if (s != null) {
					s = s.trim();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return s;
	}

	/**
	 * Interpolates a vector by bilinear interpolation (instead of trilinear)
	 * 
	 * @param q1 the q1 coordinate
	 * @param q2 the q2 coordinate
	 * @param q3 the q3 coordinate
	 * @param result will hold the result
	 */
	protected void interpolateField(double q2, double q3, float result[]) {
		

		result[0] = 0f;
		result[1] = 0f;
		result[2] = 0f;

		int n1 = q2Coordinate.getIndex(q2);
		if (n1 < 0) {
			return;
		}
		int n2 = q3Coordinate.getIndex(q3);
		if (n2 < 0) {
			return;
		}

		double f1 = q2Coordinate.getFraction(q2, n1);
		double f2 = q3Coordinate.getFraction(q3, n2);

		if (!_interpolate) {
			f1 = (f1 < 0.5) ? 0 : 1;
			f2 = (f2 < 0.5) ? 0 : 1;
		}

		double g1 = 1 - f1;
		double g2 = 1 - f2;

		// get the neighbor indices
		int i000 = getCompositeIndex(0, n1, n2);
		int i001 = i000 + 1;

		int i010 = getCompositeIndex(0, n1 + 1, n2);
		int i011 = i010 + 1;

		// Bphi (zero for simple maps)
		double b000 = getB1(i000);
		double b001 = getB1(i001);
		double b010 = getB1(i010);
		double b011 = getB1(i011);

		double bphi = b000 * g1 * g2 + b001 * g1 * f2 + b010 * f1 * g2
				+ b011 * f1 * f2;

		// now Brho
		b000 = getB2(i000);
		b001 = getB2(i001);
		b010 = getB2(i010);
		b011 = getB2(i011);

		double brho = b000 * g1 * g2 + b001 * g1 * f2 + b010 * f1 * g2
				+ b011 * f1 * f2;

		// now Bz
		b000 = getB3(i000);
		b001 = getB3(i001);
		b010 = getB3(i010);
		b011 = getB3(i011);

		double bz = b000 * g1 * g2 + b001 * g1 * f2 + b010 * f1 * g2
				+ b011 * f1 * f2;

		result[0] = (float) bphi;
		result[1] = (float) brho;
		result[2] = (float) bz;

	}

	/**
	 * Interpolates the field magnitude by bilinear interpolation. (instead of
	 * trilinear)
	 *
	 * @param q1 the q1 coordinate
	 * @param q2 the q2 coordinate
	 * @param q3 the q3 coordinate return the interpolated value of the field
	 *            magnitude
	 * @return the float
	 */
	protected float interpolateFieldMagnitude(double q2, double q3) {

		float result[] = new float[3];
		interpolateField(q2, q3, result);
		return (float) Math.sqrt(result[0]*result[0] + result[1]*result[1] + result[2]*result[2]);

//		int n1 = q2Coordinate.getIndex(q2);
//		if (n1 < 0) {
//			return 0f;
//		}
//		int n2 = q3Coordinate.getIndex(q3);
//		if (n2 < 0) {
//			return 0f;
//		}
//
//		double f1 = q2Coordinate.getFraction(q2, n1);
//		double f2 = q3Coordinate.getFraction(q3, n2);
//
//		double g1 = 1 - f1;
//		double g2 = 1 - f2;
//
//		// get the neighbor indices
//		int i000 = getCompositeIndex(0, n1, n2);
//		int i001 = i000 + 1;
//
//		int i010 = getCompositeIndex(0, n1 + 1, n2);
//		int i011 = i010 + 1;
//
//		double b000 = fieldMagnitude(i000);
//		double b001 = fieldMagnitude(i001);
//		double b010 = fieldMagnitude(i010);
//		double b011 = fieldMagnitude(i011);
//
//		return (float) (b000 * g1 * g2 + b001 * g1 * f2 + b010 * f1 * g2
//				+ b011 * f1 * f2);

	}

	public double getZMax() {
		return q3Coordinate.getMax();
	}

	public double getZMin() {
		return q3Coordinate.getMin();
	}

	public double getRhoMax() {
		return q2Coordinate.getMax();
	}

	/**
	 * @return the phiCoordinate
	 */
	public GridCoordinate getPhiCoordinate() {
		return q1Coordinate;
	}

	/**
	 * @return the rCoordinate
	 */
	public GridCoordinate getRCoordinate() {
		return q2Coordinate;
	}

	/**
	 * @return the zCoordinate
	 */
	public GridCoordinate getZCoordinate() {
		return q3Coordinate;
	}

	/**
	 * Get the name of the field
	 * 
	 * @return the name
	 */
	@Override
	public String getName() {
		return "Solenoid";
	}

}
