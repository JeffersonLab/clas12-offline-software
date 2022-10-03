package cnuphys.adaptiveSwim.geometry;

/**
 * A sphere centered at an arbitrary point
 * @author heddle
 *
 */
public class Sphere extends AGeometric {
	
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
	 * Create a sphere centered on the origin
	 * @param radius the radius of the sphere
	 */
	public Sphere(double radius) {
		this(_origin, radius);
	}

	public double getRadius() {
		return _radius;
	}

	/**
	 * Get the shortest distance between the surface of this sphere and a point.
	 * If the value is negative, we are inside the sphere.
	 * @param p a point
	 * @return the distance to the sphere
	 */
	public double signedDistance(Point p) {
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
	public double signedDistance(double x, double y, double z) {
		Point p = new Point(x, y, z);
		return signedDistance(p);
	}
	


	@Override
	public double interpolate(Point p1, Point p2, Point p) {
		throw new UnsupportedOperationException("[interpolate] Not implemented yet for a Sphere object.");
	}
	
	/**
	 * Check whether a segment intersects the sphere
	 * @param x1 x coordinate of one end of segment
	 * @param y1 y coordinate of one end of segment
	 * @param z1 z coordinate of one end of segment
	 * @param x2 x coordinate of other end of segment
	 * @param y2 y coordinate of other end of segment
	 * @param z2 z coordinate of other end of segment
	 * @return true if the segment intersects the sphere
	 */
	public boolean segmentIntersects(double x1, double y1, double z1, double x2, double y2, double z2) {
		return (distToSegment(0, 0, 0, x1, y1, z1, x2, y2, z2) < _radius);
	}
	
	/**
	 * The closest distance of a line segment to a point 
	 * @param px x coordinate of point
	 * @param py y coordinate of point
	 * @param pz z coordinate of point
	 * @param x1 x coordinate of one end of segment
	 * @param y1 y coordinate of one end of segment
	 * @param z1 z coordinate of one end of segment
	 * @param x2 x coordinate of other end of segment
	 * @param y2 y coordinate of other end of segment
	 * @param z2 z coordinate of other end of segment
	 * @return the closest distance of the segment to point p
	 */
	private double distToSegment(double px, double py, double pz, 
			double x1, double y1, double z1, 
			double x2, double y2, double z2) {
		
		  double line_dist = distSq(x1, y1, z1, x2, y2, z2);
		  if (line_dist == 0) {
			  return distSq(px, py, pz, x1, y1, z1);
		  }
		  double t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1) + (pz - z1) * (z2 - z1)) / line_dist;
		  t = Math.max(0,  Math.min(1, t));
		  return Math.sqrt(distSq(px, py, pz, x1 + t * (x2 - x1), y1 + t * (y2 - y1), z1 + t * (z2 - z1)));
		}
	
	
	//the square of the distance between two points
	private double distSq(double x1, double y1, double z1, double x2, double y2, double z2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dz = z2 - z1;
		return dx*dx + dy*dy + dz*dz;

	}

}
