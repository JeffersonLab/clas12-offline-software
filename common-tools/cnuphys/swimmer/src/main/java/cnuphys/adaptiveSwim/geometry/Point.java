package cnuphys.adaptiveSwim.geometry;

public class Point {

	/** x component */
	public double x;
	/** y component */
	public double y;
	/** z component */
	public double z;

	/**
	 * Create a point at the origin
	 */
	public Point() {
		this(0, 0, 0);
	}

	/**
	 * Copy constructor
	 * 
	 * @param p the point to copy
	 */
	public Point(Point p) {
		this(p.x, p.y, p.z);
	}

	/**
	 * Create a point
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Set the components of the vector
	 * 
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component
	 */
	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Get the difference between two points
	 * 
	 * @param a one point
	 * @param b the other point
	 * @return the difference between two points a - b
	 */
	public static Point difference(Point a, Point b) {
		return new Point(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	/**
	 * Get the in-place difference between two points
	 * 
	 * @param a one point
	 * @param b the other point
	 * @param c upon return the difference between two points a - b
	 */
	public static void difference(Point a, Point b, Point c) {
		c.set(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	/**
	 * The dot product of this "vector" with another vector
	 * 
	 * @param v the other vector or point
	 * @return the dot product
	 */
	public double dot(Point v) {
		return x * v.x + y * v.y + z * v.z;
	}

	/**
	 * The dot product of two vectors or points
	 * 
	 * @param a one vector or point
	 * @param b the other vector or point
	 * @return the dot product
	 */
	public static double dot(Point a, Point b) {
		return a.dot(b);
	}

	/**
	 * Get a string representation of the Point
	 * 
	 * @return a String representation
	 */
	@Override
	public String toString() {
		return String.format("(%10.6G, %10.6G, %10.6G)", x, y, z);
	}
	
	/**
	 * Compute the distance to another point
	 * @param x the x coordinate of the other point
	 * @param y the y coordinate of the other point
	 * @param z the z coordinate of the other point
	 * @return the distance between the points
	 */
	public double distance(double x, double y, double z) {
		double dx = x - this.x;
		double dy = y - this.y;
		double dz = z - this.z;
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
	
	/**
	 * Compute the distance to another point
	 * @param p the other point
	 * @return the distance between the points
	 */
	public double distance(Point p) {
		return distance(p.x, p.y, p.z);
	}
}