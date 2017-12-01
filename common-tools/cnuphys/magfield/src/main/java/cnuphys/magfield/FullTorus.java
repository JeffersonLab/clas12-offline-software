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
	 * Get the field by trilinear interpolation.
	 *
	 * @param phi azimuthal angle in degrees.
	 * @param rho the cylindrical rho coordinate in cm.
	 * @param z coordinate in cm
	 * @param result the result
	 * @result a Cartesian vector holding the calculated field in kiloGauss.
	 */
	@Override
	public void fieldCylindrical(TorusProbe probe, double phi, double rho, double z,
			float result[]) {
		if (isZeroField()) {
			result[X] = 0f;
			result[Y] = 0f;
			result[Z] = 0f;
			return;
		}

		while (phi < 0.0) {
			phi += 360.0;
		}
		while (phi > 360.0) {
			phi -= 360.0;
		}


		if ((probe == null) || !FieldProbe.CACHE) {
			interpolateField(phi, rho, z, result);
		}
		else {
			calculate(phi, rho, z, probe, result);
		}


		result[X] *= _scaleFactor;
		result[Y] *= _scaleFactor;
		result[Z] *= _scaleFactor;
	}
	

	
}
