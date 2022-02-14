package cnuphys.adaptiveSwim.geometry;

/**
 * Ordinary 3D vector
 * 
 * @author heddle
 *
 */
public class Vector extends Point {

	/**
	 * Create a new vetor with a zero components
	 */
	public Vector() {
	}

	/**
	 * Create a Vector from a point
	 * 
	 * @param p the point
	 */
	public Vector(Point p) {
		this(p.x, p.y, p.z);
	}

	/**
	 * Create a vector
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
	public Vector(double x, double y, double z) {
		super(x, y, z);
	}

	/**
	 * The square of the length of the vector
	 * 
	 * @return the square of the length of the vector
	 */
	public double lengthSquared() {
		return x * x + y * y + z * z;
	}

	/**
	 * The length of the vector
	 * 
	 * @return the length of the vector
	 */
	public double length() {
		return Math.sqrt(lengthSquared());
	}

	/**
	 * Set the components of the vector
	 * 
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component
	 */
	@Override
	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * The cross product of two vectors
	 * 
	 * @param a one vector
	 * @param b other vector
	 * @return c = a x b
	 */
	public static Vector cross(Vector a, Vector b) {
		Vector c = new Vector();
		cross(a, b, c);
		return c;
	}

	/**
	 * The in-place cross product of two vectors
	 * 
	 * @param a one vector
	 * @param b other vector
	 * @param c on return c = a x b
	 */
	public static void cross(Vector a, Vector b, Vector c) {
		c.x = a.y * b.z - a.z * b.y;
		c.y = a.z * b.x - a.x * b.z;
		c.z = a.x * b.y - a.y * b.x;
	}

	/**
	 * Scale a vector
	 * 
	 * @param scaleFactor the scale factor
	 */
	public void scale(double scaleFactor) {
		x *= scaleFactor;
		y *= scaleFactor;
		z *= scaleFactor;
	}

	/**
	 * Compute the angle between two vectors in radians
	 * 
	 * @param a one vector
	 * @param b the other vector
	 * @return the angle between two vectors in radians
	 */
	public static double angleBetween(Vector a, Vector b) {
		double alen = a.length();
		double blen = b.length();
		double denom = alen * blen;
		if (denom < Constants.TINY) {
			return Double.NaN;
		}
		double num = dot(a, b);
		return Math.acos(num / denom);
	}

	/**
	 * Compute the angle between two vectors in degrees
	 * 
	 * @param a one vector
	 * @param b the other vector
	 * @return the angle between two vectors in degrees
	 */
	public static double angleBetweenDeg(Vector a, Vector b) {
		return Math.toDegrees(angleBetween(a, b));
	}

	/**
	 * Get a unit vector in the same direction as this
	 * 
	 * @return a unit vector
	 */
	public Vector unitVector() {
		double len = length();
		if (len < Constants.TINY) {
			return null;
		}

		return new Vector(x / len, y / len, z / len);
	}

	/**
	 * project vector a in the direction of vector b
	 * 
	 * @param a the vector to project
	 * @param b the vector whose direction we project on
	 * @return the projection of a
	 */
	public static Vector project(Vector a, Vector b) {

		Vector ub = b.unitVector();
		if (ub == null) {
			return new Vector();
		}

		double dot = dot(a, ub);

		ub.scale(dot);
		return ub;
	}

	/**
	 * Get the difference between two vectors
	 * 
	 * @param a one vector
	 * @param b the other vector
	 * @return the difference between two vectors a - b
	 */
	public static Vector difference(Vector a, Vector b) {
		return new Vector(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	/**
	 * Get the in-place difference between two vectors
	 * 
	 * @param a one vector
	 * @param b the other vector
	 * @param c upon return the difference between two vectors a - b
	 */
	public static void difference(Vector a, Vector b, Vector c) {
		c.set(a.x - b.x, a.y - b.y, a.z - b.z);
	}

}