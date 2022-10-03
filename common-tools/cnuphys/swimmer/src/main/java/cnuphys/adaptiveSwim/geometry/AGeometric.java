package cnuphys.adaptiveSwim.geometry;

/**
 * For objects that we might use in stoppers, such as planes.
 * @author heddle
 *
 */
public abstract class AGeometric {
	
	/**
	 * Often we have geomtric objects centered on the origin
	 */
	protected static final Point _origin = new Point(0, 0, 0);
	
	/**
	 * Signed distance from a point to the object
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the signed distance (indicates which side you are on)
	 */
	public abstract double signedDistance(double x, double y, double z);

	/**
	 * Create a line from two points and then get the intersection with the object
	 * @param p1 one point
	 * @param p2 another point
	 * @param p will hold the intersection, NaNs if no intersection
	 * @return the t parameter. If NaN it means the line is parallel to the object.
	 *         If t [0,1] then the segment intersects the object. If t outside [0, 1]
	 *         the infinite line intersects the object, but not the segment
	 */
	public abstract double interpolate(Point p1, Point p2, Point p);

	
	/**
	 * Distance from a point to the object
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the distance to the object
	 */
	public double distance(double x, double y, double z) {
		return Math.abs(signedDistance(x, y, z));
	}
	
	/**
	 * Distance from a point to the object
	 * 
	 * @param p the point in question
	 * @return the distance to the plane
	 */
	public double distance(Point p) {
		return distance(p.x, p.y, p.z);
	}
	
	/**
	 * Distance from a point to the plane
	 * 
	 * @param u the point in question u[0]=x, u[1]=y, u[2]=z
	 * @return the distance to the plane
	 */
	public double distance(double[] u) {
		return distance(u[0], u[1], u[2]);
	}
	
	/**
	 * Signed distance from a point to the plane
	 * 
	 * @param u the point in question u[0]=x, u[1]=y, u[2]=z
	 * @return the signed distance to the plane
	 */
	public double signedDistance(double[] u) {
		return signedDistance(u[0], u[1], u[2]);
	}

	/**
	 * Signed distance from a point to the object
	 * 
	 * @param p the point in question
	 * @return the signed distance (indicates which side you are on)
	 */
	public double signedDistance(Point p) {
		return signedDistance(p.x, p.y, p.z);
	}

}
