package cnuphys.magfield;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * A simple program that demonstrates the use of the magfield package.
 * 
 * @author heddle
 *
 */
public class Test {

    public static void main(String arg[]) {
	// will read mag field assuming we are in a
	// location relative to clasJlib. This will
	// have to be modified as appropriate.

	// look in the got location

	String clasJlib = System.getProperty("user.home")
		+ "/git/cnuphys/clasJlib";

	// see if it is a good location
	File file = new File(clasJlib);
	if (!file.exists()) {
	    System.err.println("dir: " + clasJlib + " does not exist.");
	    System.exit(1);
	}

	// OK, see if we can create a Torus
	String torusFileName = clasJlib
		+ "/data/torus/v1.0/clas12_torus_fieldmap_binary.dat";
	File torusFile = new File(torusFileName);
	Torus torus = null;
	try {
	    torus = Torus.fromBinaryFile(torusFile);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}

	// OK, see if we can create a Solenoid
	String solenoidFileName = clasJlib
		+ "/data/solenoid/v1.0/solenoid-srr.dat";
	File solenoidFile = new File(solenoidFileName);
	Solenoid solenoid = null;
	try {
	    solenoid = Solenoid.fromBinaryFile(solenoidFile);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}

	// print some features
	if (torus != null) {
	    System.out.println("************ Torus: \n" + torus);
	}
	if (solenoid != null) {
	    System.out.println("************ Solenoid: \n" + solenoid);
	}

    }
}
