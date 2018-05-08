package cnuphys.magfield;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FullTorus extends Torus {
		
	
	/**
	 * Obtain a torus object from a binary file, probably
	 * "clas12-fieldmap-torus.dat"
	 *
	 * @param file the file to read
	 * @return the torus object
	 * @throws FileNotFoundException the file not found exception
	 */
	public static FullTorus fromBinaryFile(File file) throws FileNotFoundException {
		FullTorus fullTorus = new FullTorus();
		fullTorus.readBinaryMagneticField(file);
		return fullTorus;
	}
	
	
	/**
	 * Tests whether this is a full field or a phi symmetric field
	 * @return <code>true</code> if this is a full field
	 */
	public static boolean isFieldmapFullField(String torusPath) throws FileNotFoundException {
		File file = new File(torusPath);

		if (!file.exists()) {
			throw new FileNotFoundException("TORUS Map not found at [" + torusPath + "]");
		}

		try {
			DataInputStream dos = new DataInputStream(new FileInputStream(file));

			boolean swap = false;
			int magicnum = dos.readInt(); // magic number

			// TODO handle swapping if necessary
			swap = (magicnum != MAGICNUMBER);
			if (swap) {
				System.err.println("byte swapping required but not yet implemented.");
				dos.close();
				return false;
			}
			
			//read five ints related to cs
			dos.readInt();
			dos.readInt();
			dos.readInt();
			dos.readInt();
			dos.readInt();
			
			//now read phi min and phi max in degrees


			float phiMin = dos.readFloat();
			float phiMax = dos.readFloat();
			
			return (phiMax > 300);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
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
	public void fieldCylindrical(Cell3D cell, double phi, double rho, double z,
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

		_cell.calculate(phi, rho, z, result);

		result[X] *= _scaleFactor;
		result[Y] *= _scaleFactor;
		result[Z] *= _scaleFactor;
	}
	
	/**
	 * Get the field by trilinear interpolation. Uses the
	 * common cell which should not be done in a multithreaded environment.
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

		fieldCylindrical(_cell, phi, rho, z, result);
	}

	
	//for testing
	public static void main(String arg[]) {
		
//		String fn[] = {"/Users/heddle/magfield/clas12-fieldmap-torus.dat", "/Users/heddle/magfield/Jan_clas12TorusFull_2.00.dat"};
//		for (String name : fn) {
//			try {
//				boolean isFull = isFieldmapFullField(name);
//				System.out.println("Path [" + name + "] full: " + isFull);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//		
		
		try {
			MagneticFields.getInstance().initializeMagneticFieldsFromEnv();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MagneticFieldInitializationException e) {
			e.printStackTrace();
		}
	}

	
}
