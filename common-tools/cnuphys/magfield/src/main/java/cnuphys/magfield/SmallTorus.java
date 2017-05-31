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
public final class SmallTorus extends Torus {

	// private constructor
	/**
	 * Instantiates a new torus.
	 */
	private SmallTorus() {
		setCoordinateNames("phi", "rho", "z");
		_scaleFactor = -1; // default
	}

	/**
	 * Obtain a torus object from a binary file, probably
	 * "clas12_small_torus.dat"
	 *
	 * @param file the file to read
	 * @return the torus object
	 * @throws FileNotFoundException the file not found exception
	 */
	public static SmallTorus fromBinaryFile(File file) throws FileNotFoundException {
		SmallTorus torus = new SmallTorus();
		torus.readBinaryMagneticField(file);
		return torus;
	}


	//convert the ascii to a binary file
	public static void asciiToBinary() {
		
//		Hi Mac, Veronique, Dave,
//
//		Following Mac suggestions, I produced a torus map that is:
//
//		a. coarser in phi (steps of 1.75 degrees, or 1/8 of the original)
//		b. in gauss units instead of kgauss: these are INT so anything < 1 will be 0, which saves a lot of space too.
//
//		You can find this map here:
//
//		/lustre/expphy/work/hallb/clas12/ungaro/clas12TorusSmallMap.dat
//
//		Same structure as before: phi, r, z, bx, by, bz
//
//		Let me know how you want to test it.
//
//		Regards,
//
//		Mauri

//		String cwd = System.getProperty("user.dir");
//		System.out.println("CWD: " + cwd);

		String asciiFileName = "../../../data/smallAsciiMap.txt"; 

		File file = new File(asciiFileName);
		
		System.err.println("ascii file path: " + file.getAbsolutePath());
		System.err.println("ascii file exists: " + file.exists());
		
		if (!file.exists()) return;
		
		
		long numMB = file.length() / 1000000;
		System.out.println("Size of ascii file in MB: " + numMB);

		String binaryFileName = "../../../data/clas12_small_torus.dat";
		
		int nPhi = 18;
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
			float phimax = 29.75f;
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

					String tokens[] = tokens(line);
					try {
						//convert to KG from G
						double bx = Double.parseDouble(tokens[3])/1000.;
						double by = Double.parseDouble(tokens[4])/1000.;
						double bz = Double.parseDouble(tokens[5])/1000.;
						
						dos.writeFloat((float)bx);
						dos.writeFloat((float)by);
						dos.writeFloat((float)bz);
					} catch (ArrayIndexOutOfBoundsException aeobe) {
						System.err.println("line: " + line);
						aeobe.printStackTrace();
						System.exit(1);
					}

					nLine++;
				}
				
				lnr.close();
				dos.close();

				long elapsedTime = System.nanoTime() - startTime;
				double seconds = elapsedTime / 1.0e9;
				System.out.println(
						"read " + nLine + " lines in " + seconds + " seconds");
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	//tokenizer
	private static String[] tokens(String str) {
		return tokens(str, null);
	}
	
	/**
	 * Get the name of the field
	 * 
	 * @return the name
	 */
	@Override
	public String getName() {
		return "Small Torus";
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
			path = "../../../data/clas12_small_torus.dat";
		}

		File file = new File(path);

		SmallTorus torus = null;
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
		String fieldStr;
		
//		torus.field(x, y, z, result);
//
//		System.out.println("(x,y,z) = " + x + ", " + y + ", " + z);
//
//		fieldStr = torus.vectorToString(result);
//		System.out.println("Field: " + fieldStr);
//		
//		
//		x = (float) (44f*Math.cos(Math.toRadians(29.75)));
//		y = (float) (44f*Math.sin(Math.toRadians(29.75)));
//		z = 280f;
//		torus.field(-x, -y, z, result);>
//
//		System.out.println("(x,y,z) = " + x + ", " + y + ", " + z);
//
//		fieldStr = torus.vectorToString(result);
//		System.out.println("Field: " + fieldStr);
//		
		x = (float) (462f*Math.cos(Math.toRadians(15.75)));
		y = (float) (462f*Math.sin(Math.toRadians(15.75)));
		z = 436f;
		torus.field(x, y, z, result);
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


}