package cnuphys.magfield;

import java.io.File;
import java.io.FileNotFoundException;

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
		
		if (isRectangularGrid()) {
			System.err.println("Calling fieldCylindrical in FullTorus (with cell) for Rectangular Grid");
			(new Throwable()).printStackTrace();
			System.exit(1);
		}

		
		if (!containsCylindrical(phi, rho, z)) {
			result[X] = 0f;
			result[Y] = 0f;
			result[Z] = 0f;
			return;
		}

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
		
		if (isRectangularGrid()) {
			System.err.println("Calling fieldCylindrical in FullTorus for Rectangular Grid");
			System.exit(1);
		}


		fieldCylindrical(_cell, phi, rho, z, result);
	}


	
}
