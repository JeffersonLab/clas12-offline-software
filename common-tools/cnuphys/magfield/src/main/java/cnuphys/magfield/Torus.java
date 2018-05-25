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
	
	//has part of the solenoid been added in to remove the overlap?
	protected boolean _addedSolenoid;
	
	/**
	 * Instantiates a new torus.
	 * Note q1 = phi, q2 = rho, q3 = z
	 */
	public Torus() {
		setCoordinateNames("phi", "rho", "z");
		_scaleFactor = -1; // default
		_cell = new Cell3D(this);
		_addedSolenoid = false;
	}
	
	/**
	 * Has part of the solenoid been added in to remove the overlap?
	 * @return<code>true</code> if the solenoid was added in.
	 */
	public boolean isSolenoidAdded() {
		return _addedSolenoid;
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
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates. The field is returned as a Cartesian vector in kiloGauss.
	 * The coordinates are in the canonical CLAS system with the origin at the
	 * nominal target, x through the middle of sector 1 and z along the beam.
	 * 
	 * @param x
	 *            the x coordinate in cm
	 * @param y
	 *            the y coordinate in cm
	 * @param z
	 *            the z coordinate in cm
	 * @param result
	 *            a array holding the retrieved (interpolated) field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */
	@Override
	public final void field(float x, float y, float z, float result[]) {

		if (isRectangularGrid()) {
			if (!contains(x, y, z)) {
				result[X] = 0f;
				result[Y] = 0f;
				result[Z] = 0f;
			} else {
				_cell.calculate(x, y, z, result);
			}
			return;
		}

		double rho = FastMath.sqrt(x * x + y * y);
		double phi = FastMath.atan2Deg(y, x);
		fieldCylindrical(phi, rho, z, result);
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
	public void fieldCylindrical(Cell3D cell, double phi, double rho, double z,
			float result[]) {
		
		if (isRectangularGrid()) {
			System.err.println("Calling fieldCylindrical in Torus for Rectangular Grid");
			System.exit(1);
		}
		
		if (!containsCylindrical((float)phi, (float)rho, (float)z)) {
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
		
		// relativePhi (-30, 30) phi relative to middle of sector
		double relativePhi = relativePhi(phi);

		boolean flip = (relativePhi < 0.0);

		cell.calculate(Math.abs(relativePhi), rho, z, result);

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
		if (isRectangularGrid()) {
			System.err.println("Asking for Rho Max for Rectangular Grid");
			System.exit(1);
		}
		return q2Coordinate.getMax();
	}

	/**
	 * Get the minimum rho coordinate of the field boundary
	 * @return the minimum rho coordinate of the field boundary
	 */
	public double getRhoMin() {
		if (isRectangularGrid()) {
			System.err.println("Asking for Rho Min for Rectangular Grid");
			System.exit(1);
		}
		return q2Coordinate.getMin();
	}
	
	/**
	 * Get the maximum x coordinate of the field boundary
	 * @return the maximum x coordinate of the field boundary
	 */
	public double getXMax() {
		if (isCylindricalGrid()) {
			System.err.println("Asking for X Max for Cylandrical Grid");
			System.exit(1);
		}
		return q1Coordinate.getMax();
	}
	
	/**
	 * Get the maximum x coordinate of the field boundary
	 * @return the maximum x coordinate of the field boundary
	 */
	public double getXMin() {
		if (isCylindricalGrid()) {
			System.err.println("Asking for X Min for Cylandrical Grid");
			System.exit(1);
		}
		return q1Coordinate.getMin();
	}

	
	/**
	 * Get the maximum y coordinate of the field boundary
	 * @return the maximum y coordinate of the field boundary
	 */
	public double getYMax() {
		if (isCylindricalGrid()) {
			System.err.println("Asking for Y Max for Cylandrical Grid");
			System.exit(1);
		}
		return q2Coordinate.getMax();
	}
	
	/**
	 * Get the maximum y coordinate of the field boundary
	 * @return the maximum y coordinate of the field boundary
	 */
	public double getYMin() {
		if (isCylindricalGrid()) {
			System.err.println("Asking for Y Min for Cylandrical Grid");
			System.exit(1);
		}
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
     * @param x the x coordinate in the map units
     * @param y the y coordinate in the map units
     * @param z the z coordinate in the map units
     * @return <code>true</code> if the point is included in the boundary of the field
     */
	@Override
    public boolean contains(float x, float y, float z) {
		if (isRectangularGrid()) {
			if ((x < getXMin()) || (x > getXMax())) {
				return false;
			}
			if ((y < getYMin()) || (y > getYMax())) {
				return false;
			}
			if ((z < getZMin()) || (z > getZMax())) {
				return false;
			}
			return true;
		}
		
		double rho = FastMath.sqrt(x * x + y * y);
		double phi = FastMath.atan2Deg(y, x);
        return containsCylindrical((float)phi, (float)rho, z);
    }
	
	/**
	 * Used to add the solenoid into the torus. Experimental!!
	 * @param compositeIndex the composite index
	 * @param result the solenoid field added in
	 */
	public void addToField(int compositeIndex, float[] result) {
		int index = 3*compositeIndex;
		for (int i = 0; i < 3; i++) {
			int j = index + i;
			field.put(j, field.get(j) + result[i]);
		}
		_addedSolenoid = true;
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
		
		if (isRectangularGrid()) {
			System.err.println("Calling containsCylindrical for a rectangular grid.");
			(new Throwable()).printStackTrace();
			System.exit(1);
		}
		
		if ((z < getZMin()) || (z > getZMax())) {
			return false;
		}
		if ((rho < getRhoMin()) || (rho > getRhoMax())) {
			return false;
		}
		return true;
	}
}
