package cnuphys.magfield;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * The Class Torus.
 *
 * @author David Heddle
 * @author Nicole Schumacher
 */

public class Torus extends MagneticField {
	
	//the cell implements the probe trick
	protected Cell3D _cell;
	
	/**
	 * Instantiates a new torus.
	 * Note q1 = phi, q2 = rho, q3 = z
	 */
	public Torus() {
		setCoordinateNames("phi", "rho", "z");
		_scaleFactor = -1; // default
		_cell = new Cell3D(this);
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

		_cell.calculate(Math.abs(relativePhi), rho, z, result);

		// negate change x and z components
		if (flip) {
			result[X] = -result[X];
			result[Z] = -result[Z];
		}

		// rotate onto to proper sector
		
		int sector = getSector(phi);

		if (sector > 1) {
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

	/**
	 * Get the maximum z coordinate of the field boundary
	 * @return the maximum z coordinate of the field boundary
	 */
	public double getZMax() {
		return q3Coordinate.getMax();
	}

	/**
	 * Get the minimum z coordinate of the field boundary
	 * @return the minimum z coordinate of the field boundary
	 */
	public double getZMin() {
		return q3Coordinate.getMin();
	}

	/**
	 * Get the maximum rho coordinate of the field boundary
	 * @return the maximum rho coordinate of the field boundary
	 */
	public double getRhoMax() {
		return q2Coordinate.getMax();
	}

	/**
	 * Get the minimum rho coordinate of the field boundary
	 * @return the minimum rho coordinate of the field boundary
	 */
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

	/**
	 * Check whether the field boundaries include the point
	 * 
	 * @param phi
	 *            azimuthal angle in degrees.
	 * @param rho
	 *            the cylindrical rho coordinate in cm.
	 * @param z
	 *            coordinate in cm
	 * @return <code>true</code> if the point is included in the boundary of the
	 *         field
	 * 
	 */
	@Override
	public boolean containsCylindrical(float phi, float rho, float z) {		
		if ((z < getZMin()) || (z > getZMax())) {
			return false;
		}
		if ((rho < getRhoMin()) || (rho > getRhoMax())) {
			return false;
		}
		return true;
	}
}
