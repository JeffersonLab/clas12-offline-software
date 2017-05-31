package cnuphys.magfieldC;

public class CylindricalVector {

	public double rho;
	public double phi;  //stored in degrees
	public double z;
	
	
	/**
	 * null constructor
	 */
	public CylindricalVector() {
	}

	/**
	 * A Cylindrical 3-vector
	 * @param rho
	 * @param phi the azimuth in degrees
	 * @param z
	 */
	public CylindricalVector(double rho, double phi, double z) {
		super();
		this.rho = rho;
		this.phi = phi;
		this.z = z;
	}
	
	/**
	 * Create a Cylindrical Vector from a Cartesian vector
	 * @param v the Cartesian vector
	 */
	public CylindricalVector(CartesianVector v) {
		super();
		rho = MagMath.hypot(v.x, v.y);
		phi = MagMath.atan2Deg(v.y, v.x);
		z = v.z;
	}

	/**
	 * get the length of the vector
	 * @return the length of the vector
	 */
	public double length() {
		return MagMath.hypot(rho, z);
	}
	
	/**
	 * Multiply the vector by a scaler
	 * @param scaleFactor
	 */
	public void scale(double scaleFactor) {
		rho *= scaleFactor;
		z *= scaleFactor;
	}
	
	/**
	 * Convert to cartesian coordinates
	 * @param v the vector yo hold the cartesian coordinates
	 */
	public void toCartesian(CartesianVector v) {
		v.x = rho*MagMath.cosDeg(phi);
		v.y = rho*MagMath.sinDeg(phi);
		v.z = z;
	}
	
	/**
	 * A string representation of the vector
	 *
	 * @param v
	 *            the vector (float array) to represent.
	 * @return a string representation of the vector (array).
	 */
	@Override
	public String toString() {
		String s = String.format("(%8.5f, %8.5f deg, %8.5f) magnitude: %8.5f", rho, phi, z, length());
		return s;
	}
}
