package cnuphys.magfield;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

/**
 * The Class Torus.
 *
 * @author David Heddle
 * @author Nicole Schumacher
 * @version 1.0
 */
public class Torus extends MagneticField {
	
	/**
	 * Instantiates a new torus.
	 */
	public Torus() {
		setCoordinateNames("phi", "rho", "z");
		_scaleFactor = -1; // default
	}
	

	/**
	 * Obtain a torus object from a binary file, probably
	 * "clas12_torus_fieldmap_binary.dat"
	 *
	 * @param file the file to read
	 * @return the torus object
	 * @throws FileNotFoundException the file not found exception
	 */
	public static Torus fromBinaryFile(File file) throws FileNotFoundException {
		Torus torus = new Torus();
		torus.readBinaryMagneticField(file);
		return torus;
	}

	/**
	 * Must deal with the fact that we only have the field between 0 and 30
	 * degrees.
	 *
	 * @param absolutePhi the absolute phi
	 * @return the relative phi (-30, 30) from the nearest middle of a sector in
	 *         degrees.
	 */
	private double relativePhi(double absolutePhi) {
		if (absolutePhi < 0.0) {
			absolutePhi += 360.0;
		}

		// make relative phi between 0 -30 and 30
		double relativePhi = absolutePhi;
		while (Math.abs(relativePhi) > 30.0) {
			relativePhi -= 60.0;
		}
		return relativePhi;
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
	public void fieldCylindrical(TorusProbe probe, double phi, double rho, double z,
			float result[]) {
		if (isZeroField()) {
			result[X] = 0f;
			result[Y] = 0f;
			result[Z] = 0f;
			return;
		}

		if (phi < 0.0) {
			phi += 360.0;
		}
		
		// relativePhi (-30, 30) phi relative to middle of sector
		double relativePhi = relativePhi(phi);

		boolean flip = (relativePhi < 0.0);

		if ((probe == null) || !FieldProbe.CACHE) {
			interpolateField(Math.abs(relativePhi), rho, z, result);
		}
		else {
			calculate(Math.abs(relativePhi), rho, z, probe, result);
		}

		// negate change x and z components
		if (flip) {
			result[X] = -result[X];
			result[Z] = -result[Z];
		}

		// rotate onto to proper sector
		
		double bx = result[X];
		double by = result[Y];

		int sector = getSector(phi);
		switch (sector) {
		case 2:
			result[X] = (float) (bx * 0.5 - by * ROOT3OVER2);
			result[Y] = (float) (bx * ROOT3OVER2 + by * 0.5);
			break;
		case 3:
			result[X] = (float) (-bx * 0.5 - by * ROOT3OVER2);
			result[Y] = (float) (bx * ROOT3OVER2 - by * 0.5);
			break;
		case 4:
			result[X] = (float) (-bx);
			result[Y] = (float) (-by);
			break;
		case 5:
			result[X] = (float) (-bx * 0.5 + by * ROOT3OVER2);
			result[Y] = (float) (-bx * ROOT3OVER2 - by * 0.5);
			break;
		case 6:
			result[X] = (float) (bx * 0.5 + by * ROOT3OVER2);
			result[Y] = (float) (-bx * ROOT3OVER2 + by * 0.5);
			break;
		default:
			break;
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
//	@Override
//	protected String vectorToString(float v[]) {
//		float vx = v[X] / 10;
//		float vy = v[Y] / 10;
//		float vz = v[Z] / 10;
//		float vLen = vectorLength(v) / 10;
//		String s = String.format("(%8.5f, %8.5f, %8.5f) magnitude: %8.5f T", vx,
//				vy, vz, vLen);
//		return s;
//	}

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

	public static void asciiToBinary() {

//		String cwd = System.getProperty("user.dir");
//		System.out.println("CWD: " + cwd);

		String asciiFileName = "../../../data/largeAsciiMap.txt"; 

		File file = new File(asciiFileName);
		long numMB = file.length() / 1000000;
		System.out.println("Size of ascii file in MB: " + numMB);

		String binaryFileName = "../../../data/clas12TorusOriginalMap.binary.v2.dat";
		
		int nPhi = 121;
		int nRho = 251;
		int nZ = 251;

		// Opening of the file

		FileInputStream fis;
		long startTime = System.nanoTime();
		try {
			fis = new FileInputStream(asciiFileName);
			DataOutputStream dos = new DataOutputStream(
					new FileOutputStream(binaryFileName));
			InputStreamReader isr = new InputStreamReader(fis);
			LineNumberReader lnr = new LineNumberReader(isr);

			float phimin = 0.0f;
			float phimax = 30.0f;
			float rhomin = 0.0f;
			float rhomax = 500.0f;
			float zmin = 100.0f;
			float zmax = 600.0f;

			try {
				// write the header
				dos.writeInt(0xced);
				dos.writeInt(0);
				dos.writeInt(1);
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

				int nLine = 0;
				boolean readheader = false;
				while (true) {
					String line = null;

					try {
						line = lnr.readLine();

						if (!readheader) {
							readheader = line.contains("</mfield>");
							if (readheader) {
								line = lnr.readLine();
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

					if (readheader) {

						// detection of EOF
						if (line == null) {
							break;
						}

						String tokens[] = tokens(line, " ");
						try {
							dos.writeFloat(Float.parseFloat(tokens[3]));
							dos.writeFloat(Float.parseFloat(tokens[4]));
							dos.writeFloat(Float.parseFloat(tokens[5]));
						} catch (ArrayIndexOutOfBoundsException aeobe) {
							System.err.println("line: " + line);
							aeobe.printStackTrace();
							System.exit(1);
						}

						nLine++;
					}
				}

				long elapsedTime = System.nanoTime() - startTime;
				double seconds = elapsedTime / 1.0e9;
				System.out.println(
						"read " + nLine + " lines in " + seconds + " seconds");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	//simple tokenizer
	protected static String[] tokens(String str, String delimiter) {

		StringTokenizer t = new StringTokenizer(str, delimiter);
		int num = t.countTokens();
		String lines[] = new String[num];

		for (int i = 0; i < num; i++) {
			lines[i] = t.nextToken();
		}

		return lines;
	}

	/**
	 * main method used for testing.
	 *
	 * @param arg command line arguments
	 */
	public static void main(String arg[]) {

		boolean convert = false;
		if (convert) {
			asciiToBinary();
			System.exit(0);
		}

		String path = null;

		if ((arg != null) && (arg.length > 0)) {
			path = arg[0];
		}

		if (path == null) {
			path = "../../../data/clas12_torus_fieldmap_binary.dat";
		}

		File file = new File(path);

		Torus torus = null;
		try {
			torus = fromBinaryFile(file);
			System.out.println("Created field object.");
			setInterpolate(false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}

		float x = 32.60f;
		float y = 106.62f;
		float z = 410.0f;
		float result[] = new float[3];
		torus.field(x, y, z, result);

		System.out.println("(x,y,z) = " + x + ", " + y + ", " + z);

		String fieldStr = torus.vectorToString(result);
		System.out.println("Field: " + fieldStr);
		
		
		x = (float) (44f*Math.cos(Math.toRadians(30)));
		y = (float) (44f*Math.sin(Math.toRadians(30)));
		z = 314f;
		torus.field(-x, -y, z, result);

		System.out.println("(x,y,z) = " + x + ", " + y + ", " + z);

		fieldStr = torus.vectorToString(result);
		System.out.println("Field: " + fieldStr);
		
		
		x = 0f;
		y = 39.93f;
		z = 388.10f;
		torus.field(-x, -y, z, result);

		System.out.println("(x,y,z) = " + x + ", " + y + ", " + z);

		fieldStr = torus.vectorToString(result);
		System.out.println("Field: " + fieldStr);

	}

	/**
	 * Get the name of the field
	 * 
	 * @return the name
	 */
	@Override
	public String getName() {
		return "Torus";
	}


}
