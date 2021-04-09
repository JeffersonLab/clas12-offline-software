package cnuphys.adaptiveSwim.geometry;

/**
 * 3D line of the form p(t) = po + t*dp where p(t) is a point on the line, po is
 * one point and dp = (p1-po) where p1 is another point. If this is an infinite
 * line, the t = [-infinity, infinity]. If this is a directed segment, t = [0,
 * 1]
 * 
 * @author heddle
 *
 */
public class Line {

	private Point _po; // "start" point on the line
	private Vector _dp; // segment from start to end
	private double _dpLen; // length of start to end segment

	/**
	 * Create a line from two points on the line. If this is a directed line
	 * segment, the line will go from po to p1.
	 * 
	 * @param po one point
	 * @param p1 the other point
	 */
	public Line(Point po, Point p1) {
		_po = new Point(po);
		_dp = new Vector(Point.difference(p1, po));
		_dpLen = _dp.length();
	}
	
	/**
	 * Copy constructor
	 * @param line the line to copy
	 */
	public Line(Line line) {
		_po = new Point(line._po);
		_dp = new Vector(line._dp);
		_dpLen = line._dpLen;
	}

	/**
	 * Get the po "start" point. This is just an arbitrary point on an infinite
	 * line, but the starting point if this is a directed line segment
	 * 
	 * @return the "starting" point.
	 */
	public Point getP0() {
		return _po;
	}

	/**
	 * Get the p1-po "dP" segment
	 * 
	 * @return dP = p1 - po
	 */
	public Vector getDelP() {
		return _dp;
	}

	/**
	 * Get one of the endpoints
	 * 
	 * @param end one of the Constants START or END
	 * @return the requested endpoint
	 */
	public Point getEndpoint(int end) {

		switch (end) {
		case Constants.START:
			return getP0();

		case Constants.END:
			return getP1();

		default:
			return null;
		}
	}

	/**
	 * Get the p1 "end" point. This is just an arbitrary point on an infinite line,
	 * but the end point if this is a directed line segment
	 * 
	 * @return the "end" point.
	 */
	public Point getP1() {
		return new Point(_po.x + _dp.x, _po.y + _dp.y, _po.z + _dp.z);
	}

	/**
	 * Get a point on the line
	 * 
	 * @param t the t parameter. If this is a directed line segment, t should be
	 *          restricted to [0, 1]
	 * @return a point on the line
	 */
	public Point getP(double t) {
		Point p = new Point();
		getP(t, p);
		return p;
	}

	/**
	 * Get a point on the line (in place)
	 * 
	 * @param t the t parameter. If this is a directed line segment, t should be
	 *          restricted to [0, 1]
	 * @param p upon return, a point on the line
	 */
	public void getP(double t, Point p) {
		p.x = _po.x + t * _dp.x;
		p.y = _po.y + t * _dp.y;
		p.z = _po.z + t * _dp.z;
	}

	/**
	 * Get the shortest distance between this line (as an infinite line) and a point
	 * 
	 * @param p a point
	 * @return the perpendicular distance
	 */
	public double distance(Point p) {
		Vector ap = new Vector(Point.difference(p, _po));
		Vector c = Vector.cross(ap, _dp);
		return c.length() / _dpLen;
	}
	
	/**
	 * Get the shortest distance between this line (as an infinite line) and a point
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the perpendicular distance
	 */
	public double distance(double x, double y, double z) {
		Point p = new Point(x, y, z);
		return distance(p);
	}


	/**
	 * Decide whether a point is on the line
	 * 
	 * @param p           the point
	 * @param maxDistance the max distance it can be off the line and still
	 *                    considered on the line
	 * @return <code>true</code> if the point is considered on the line
	 */
	public boolean pointOnLine(Point p, double maxDistance) {
		double dist = distance(p);
		return dist < maxDistance;
	}

	/**
	 * Decide whether a point is on the line using the constant TINY as the max
	 * distance
	 * 
	 * @param p the point
	 * @return <code>true</code> if the point is considered on the line
	 */
	public boolean pointOnLine(Point p) {
		return pointOnLine(p, Constants.TINY);
	}

	/**
	 * Get a String representation
	 * 
	 * @return a String representation of the Line
	 */
	@Override
	public String toString() {
		return "Line from " + getP0() + " to " + getP1();
	}

	/**
	 * Get the center of the line
	 * 
	 * @return the center of the line
	 */
	public Point getCenter() {
		return getP(0.5);
	}

	// testing
	public static void main(String arg[]) {
		Point p1 = new Point(0, 0, 0);
		Point p2 = new Point(1, 1, 0);

		Line line = new Line(p1, p2);

		Point p = new Point(-10, 10, 5);

		System.out.println(p.toString() + "     distance = " + line.distance(p) + " on line: " + line.pointOnLine(p));

		p.set(999., 999., 0.001);
		System.out.println(
				p.toString() + "     distance = " + line.distance(p) + " on line: " + line.pointOnLine(p, 0.01));
		
		
		Point p3 = new Point(0, 0, 0);
		Point p4 = new Point(0, 0, 1);

		Line zaxis = new Line(p3, p4);

		System.out.println("Created a line corresponding to the z axis");
		System.out.println("Should be 0: " + zaxis.distance(0, 0, 99));
		System.out.println("Should be 100: " + zaxis.distance(-100, 0, 99));
		
		double d = zaxis.distance(-10, 10, -999);
		System.out.println("Should be 200: " + (d*d));

	}

}