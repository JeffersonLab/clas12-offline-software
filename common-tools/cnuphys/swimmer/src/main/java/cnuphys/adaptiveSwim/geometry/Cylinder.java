package cnuphys.adaptiveSwim.geometry;

/**
 * A cylinder is defined by a centerline and a radius
 * @author heddle
 *
 */
public class Cylinder {

	//the centerline
	private Line _centerLine;
	
	//the radius
	private double _radius;
	
	/**
	 * Create a cylinder
	 * @param centerLine the center line
	 * @param radius the radius
	 */
	public Cylinder(Line centerLine, double radius) {
		_centerLine = new Line(centerLine);
		_radius = radius;
	}
	

	/**
	 * Get the shortest distance between the surface of this infinite cylinder and a point.
	 * If the value is negative, we are inside the cylinder.
	 * @param p a point
	 * @return the perpendicular distance
	 */
	public double distance(Point p) {
		double lineDist = _centerLine.distance(p);
		return lineDist - _radius;
	}
	
	/**
	 * Get the shortest distance between the surface of this infinite cylinder and a point.
	 * If the value is negative, we are inside the cylinder.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the perpendicular distance
	 */
	public double distance(double x, double y, double z) {
		Point p = new Point(x, y, z);
		return distance(p);
	}

}
