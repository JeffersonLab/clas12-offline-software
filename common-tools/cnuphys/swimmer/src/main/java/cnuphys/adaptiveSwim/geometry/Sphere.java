package cnuphys.adaptiveSwim.geometry;

/**
 * A sphere centered at an arbitrary point
 * @author heddle
 *
 */
public class Sphere {
	

	//the center
	private Point _center;
	
	//the radius
	private double _radius;
	
	/**
	 * Create a sphere
	 * @param center the center of the sphere
	 * @param radius the radius of the sphere
	 */
	public Sphere(Point center, double radius) {
		_center = new Point(center);
		_radius = radius;
	}
	

	/**
	 * Get the shortest distance between the surface of this sphere and a point.
	 * If the value is negative, we are inside the sphere.
	 * @param p a point
	 * @return the distance to the sphere
	 */
	public double distance(Point p) {
		double centDist = _center.distance(p);
		return centDist - _radius;
	}
	
	/**
	 * Get the shortest distance between the surface of this sphere and a point.
	 * If the value is negative, we are inside the sphere.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the distance to the sphere
	 */
	public double distance(double x, double y, double z) {
		Point p = new Point(x, y, z);
		return distance(p);
	}

}
