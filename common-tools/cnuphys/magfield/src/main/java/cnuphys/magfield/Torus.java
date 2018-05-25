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
 */

public class Torus extends MagneticField {
	
	/**
	 * Instantiates a new torus.
	 * Note q1 = phi, q2 = rho, q3 = z
	 */
	public Torus() {
		setCoordinateNames("phi", "rho", "z");
		_scaleFactor = -1; // default
	}
	
	/**
	 * Tests whether this is a full field or a phi symmetric field
	 * @return <code>true</code> if this is a full field
	 */
	public static boolean isFieldmapFullField(String torusPath) throws FileNotFoundException {
		return FullTorus.isFieldmapFullField(torusPath);
	}

	/**
	 * Obtain a torus object from a binary file, probably
	 * "clas12-fieldmap-torus.dat"
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
	 * A quick test to throw out points definitely outside the boundaries
	 * @param x the x coordinate in the units of the map
	 * @param y the y coordinate in the units of the map
	 * @param z the z coordinate in the units of the map
	 * @return <code>true</code> if the point is in range (approximate)
	 */
	protected boolean crudeInRange(float x, float y, float z) {
		if ((z < getZMin()) || (z > getZMax())) {
			return false;
		}
		if ((Math.abs(x) < getRhoMin()) || (Math.abs(x) > getRhoMax())) {
			return false;
		}
		if ((Math.abs(y) < getRhoMin()) || (Math.abs(y) > getRhoMax())) {
			return false;
		}
		return true;
	}
	
	/** 
	 * A quick test to throw out points definitely outside the boundaries
	 * @param phi the phi coordinate in the units of the map
	 * @param rho the rho coordinate in the units of the map
	 * @param z the z coordinate in the units of the map
	 * @return <code>true</code> if the point is in range (approximate)
	 */
	protected boolean crudeInRangeCylindrical(float phi, float rho, float z) {
		if ((z < getZMin()) || (z > getZMax())) {
			return false;
		}
		if ((rho < getRhoMin()) || (rho > getRhoMax())) {
			return false;
		}
		return true;
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

		while (phi >= 360.0) {
			phi -= 360.0;
		}
		while (phi < 0.0) {
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
		
		int sector = getSector(phi);

		if (sector > 1) {
	//	double diff = (phi - relativePhi);
	//	if (diff > 0.001) {
	//		System.err.println("Diff: " + diff + "  sector: " + sector);
	//		double rdiff = Math.toRadians(diff);
	//		double cos = Math.cos(rdiff);
	//		double sin = Math.sin(rdiff);
			double cos = cosSect[sector];
			double sin = sinSect[sector];
			double bx = result[X];
			double by = result[Y];
			result[X] = (float) (bx * cos - by * sin);
			result[Y] = (float) (bx * sin + by * cos);
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
				
				dos.close();
				lnr.close();
			} catch (IOException e1) {
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
			path = "../../../data/clas12-fieldmap-torus.dat";
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
	
	public double getZMax() {
		return q3Coordinate.getMax();
	}

	public double getZMin() {
		return q3Coordinate.getMin();
	}

	public double getRhoMax() {
		return q2Coordinate.getMax();
	}

	public double getRhoMin() {
		return q2Coordinate.getMin();
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
