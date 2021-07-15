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
import java.io.PrintStream;
import java.util.StringTokenizer;

/**
 * The Class Solenoid.
 *
 * @author Sebouh Paul
 * @version 1.0
 */
public final class StandardSolenoid extends Solenoid {

	// private constructor
	/**
	 * Instantiates a new solenoid.
	 */
	private StandardSolenoid() {
		setCoordinateNames("phi", "rho", "z");
	}


	/**
	 * Checks this field active.
	 * 
	 * @return <code>true</code> if this field is active;
	 */
	@Override
	public boolean isActive() {
		return MagneticFields.getInstance().hasActiveSolenoid();
	}

	/**
	 * Obtain a solenoid object from a binary file, probably
	 * "clas12_solenoid_fieldmap_binary.dat"
	 *
	 * @param file the file to read
	 * @return the solenoid object
	 * @throws FileNotFoundException the file not found exception
	 */
	public static StandardSolenoid fromBinaryFile(File file) throws FileNotFoundException {
		StandardSolenoid solenoid = new StandardSolenoid();
		solenoid.readBinaryMagneticField(file);
		// is the field ready to use?
		System.out.println(solenoid.toString());
		return solenoid;
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

	/**
	 * Get some data as a string.
	 * 
	 * @return a string representation.
	 */
	@Override
	public final String toString() {
		String s = "Solenoid\n";
		s += super.toString();
		return s;
	}

	/**
	 * Print the current configuration
	 * 
	 * @param ps the print stream
	 */
	@Override
	public void printConfiguration(PrintStream ps) {
		ps.println(String.format("SOLENOID scale: %6.3f file: %s", _scaleFactor,
				MagneticFields.getInstance().getSolenoidBaseName()));
	}
	
	/**
	 * main method used for testing.
	 *
	 * @param arg command line arguments
	 */
	public static void main(String arg[]) {
		
		// covert the new ascii to binary
		File asciiFile = new File("../../../data/clas12SolenoidFieldMap.dat.txt");
		if (!asciiFile.exists()) {
			System.out.println("File not found: " + asciiFile.getPath());
		} else {
			System.out.println("File found: " + asciiFile.getPath());

			FileReader fileReader;
			try {
				fileReader = new FileReader(asciiFile);
				final BufferedReader bufferedReader = new BufferedReader(fileReader);

				// prepare the binary file
				String binaryFileName = "../../../data/clas12-fieldmap-solenoid.dat";
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

				DataOutputStream dos = new DataOutputStream(new FileOutputStream(binaryFileName));
				MagneticFields.writeHeader(dos, 0, 0, 0, 0, 0, phimin, phimax, nPhi, rhomin, rhomax, nRho, zmin, zmax, nZ);

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
	
	//tokenize a string
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
	 * @param bufferedReader a buffered reader which should be linked to an ascii
	 *                       file
	 * @return the next non comment line (or <code>null</code>)
	 */
	
	private static String nextNonComment(BufferedReader bufferedReader) {
		String s = null;
		try {
			s = bufferedReader.readLine();
			if (s != null) {
				s = s.trim();
			}
			
			
			while ((s != null) && (s.startsWith("<") || (s.length() < 1) || s.startsWith("r(mm"))) {
				
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


}
