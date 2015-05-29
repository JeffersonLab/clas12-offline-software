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

    // private constructor
    /**
     * Instantiates a new torus.
     */
    private Torus() {
	setCoordinateNames("phi", "rho", "z");
	_scaleFactor = -1; //default
    }

    /**
     * Obtain a torus object from a binary file, probably
     * "clas12_torus_fieldmap_binary.dat"
     *
     * @param file
     *            the file to read
     * @return the torus object
     * @throws FileNotFoundException
     *             the file not found exception
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
     * @param absolutePhi
     *            the absolute phi
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
    @Override
    public void fieldCylindrical(double phi, double rho, double z,
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

	interpolateField(Math.abs(relativePhi), rho, z, result);

	// negate change x and z components
	if (flip) {
	    result[X] = -result[X];
	    result[Z] = -result[Z];
	}

	// rotate onto to proper sector?
	// System.err.println("relative: " + relativePhi);
	// System.err.println("absolute: " + phi);
	// System.err.println("diff: " + (phi - relativePhi));

	double diff = (phi - relativePhi);
	if (diff > 0.001) {
	    double rdiff = Math.toRadians(diff);
	    double cos = Math.cos(rdiff);
	    double sin = Math.sin(rdiff);
	    double bx = result[0];
	    double by = result[1];
	    result[X] = (float) (bx * cos - by * sin);
	    result[Y] = (float) (bx * sin + by * cos);
	}

	result[X] *= _scaleFactor;
	result[Y] *= _scaleFactor;
	result[Z] *= _scaleFactor;
   }

    /**
     * Convert a array used as a vector to a readable string.
     *
     * @param v
     *            the vector (float array) to represent.
     * @return a string representation of the vector (array).
     */
    @Override
    protected String vectorToString(float v[]) {
	float vx = v[X] / 10;
	float vy = v[Y] / 10;
	float vz = v[Z] / 10;
	float vLen = vectorLength(v) / 10;
	String s = String.format("(%8.5f, %8.5f, %8.5f) magnitude: %8.5f T",
		vx, vy, vz, vLen);
	return s;
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

	String asciiFileName = "/home/heddle/fieldmaps/clas12_torus_fieldmap.dat"; // small
	// String asciiFileName = "/home/heddle/fieldmaps/sptorus_map.dat";
	// //big

	File file = new File(asciiFileName);
	long numMB = file.length() / 1000000;
	System.out.println("Size of ascii file in MB: " + numMB);

	String binaryFileName = "/home/heddle/fieldmaps/clas12_torus_fieldmap_binary.dat";
	int nPhi = 61;
	int nRho = 126;
	int nZ = 126;
	if (numMB > 100) {
	    binaryFileName = "/home/heddle/fieldmaps/clas12_torus_fieldmap_big_binary.dat";
	    nPhi = 121;
	    nRho = 251;
	    nZ = 251;
	}

	// Opening of the file

	FileInputStream fis;
	long startTime = System.nanoTime();
	try {
	    fis = new FileInputStream(asciiFileName);
	    DataOutputStream dos = new DataOutputStream(new FileOutputStream(
		    binaryFileName));
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
		while (true) {
		    String line = null;
		    try {
			line = lnr.readLine();

		    } catch (IOException e) {
			e.printStackTrace();
		    }

		    // detection of EOF
		    if (line == null) {
			break;
		    }

		    String tokens[] = tokens(line, " ");
		    dos.writeFloat(Float.parseFloat(tokens[3]));
		    dos.writeFloat(Float.parseFloat(tokens[4]));
		    dos.writeFloat(Float.parseFloat(tokens[5]));

		    nLine++;
		}

		long elapsedTime = System.nanoTime() - startTime;
		double seconds = elapsedTime / 1.0e9;
		System.out.println("read " + nLine + " lines in " + seconds
			+ " seconds");
	    } catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }

	} catch (FileNotFoundException e) {
	    e.printStackTrace();
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
     * main method used for testing.
     *
     * @param arg
     *            command line arguments
     */
    public static void main(String arg[]) {

	boolean convert = true;
	if (convert) {
	    asciiToBinary();
	    System.exit(0);
	}

	String path = null;

	if ((arg != null) && (arg.length > 0)) {
	    path = arg[0];
	}

	if (path == null) {
	    path = "data/clas12_torus_fieldmap_binary.dat";
	}

	File file = new File(path);

	Torus torus = null;
	try {
	    torus = fromBinaryFile(file);
	    System.out.println("Created field object.");
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
