package cnuphys.magfieldC;

public class CartesianVector {

	public double x;
	public double y;
	public double z;
	
	/**
	 * null constructor
	 */
	public CartesianVector() {
	}
	
	/**
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
	public CartesianVector(double x, double y, double z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Create a CartesianVector from a CylindricalVector
	 * @param cv the cylindical vector
	 */
	public CartesianVector(CylindricalVector cv) {
		super();
		x = cv.rho*MagMath.cosDeg(cv.phi);
		y = cv.rho*MagMath.sinDeg(cv.phi);
		z = cv.z;
	}
	
	/**
	 * Zero the components
	 */
	public void zero() {
		x = 0;
		y = 0;
		z = 0;
	}
	
	/**
	 * Add a vector to this vector
	 * @param b the vector to add
	 * @param sum the sum, which can be the vector itself
	 */
	public void add(CartesianVector b, CartesianVector sum) {
		sum.x = x + b.x;
		sum.y = y + b.y;
		sum.z = z + b.z;
	}
	
	/**
	 * get the length of the vector
	 * @return the length of the vector
	 */
	public double length() {
		return MagMath.sqrt(x*x + y*y + z*z);
	}
	
	/**
	 * Multiply the vector by a scaler
	 * @param scaleFactor
	 */
	public void scale(double scaleFactor) {
		x *= scaleFactor;
		y *= scaleFactor;
		z *= scaleFactor;
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
		String s = String.format("(%8.5f, %8.5f, %8.5f) magnitude: %8.5f", x, y, z, length());
		return s;
	}

}
