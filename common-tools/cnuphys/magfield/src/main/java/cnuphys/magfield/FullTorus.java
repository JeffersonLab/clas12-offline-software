package cnuphys.magfield;

import java.io.File;
import java.io.FileNotFoundException;

public class FullTorus extends Torus {
		
	
	/**
	 * Obtain a torus object from a binary file, probably
	 * "clas12_torus_fieldmap_binary.dat"
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
	 * Get the name of the field
	 * 
	 * @return the name
	 */
	@Override
	public String getName() {
		return "Torus (full)";
	}


	
}
