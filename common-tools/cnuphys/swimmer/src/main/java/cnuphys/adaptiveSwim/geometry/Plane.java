package cnuphys.adaptiveSwim.geometry;

/**
 * A plane is defined by the equation (r - ro).norm = 0 Where r is an arbitrary
 * point on the plane, ro is a given point on the plane and norm is the normal
 * to the plane
 * 
 * @author heddle
 *
 */
public class Plane {

	// unit vector normal to plane
	public  final Vector norm;

	// point on the plane
	public final Point p0;
	
	//for the form ax + by + cz = d;
	public final double a;
	public final double b;
	public final double c;
	public final double d;
	
	private double _denom = Double.NaN;

	/**
	 * Create a plane from a normal vector and a point on the plane
	 * 
	 * @param norm the normal vector
	 * @param p    a point in the plane
	 * @return the plane that contains p and its normal is norm
	 */
	public Plane(Vector anorm, Point p) {
		// lets make it a unit vector
		norm = anorm.unitVector();
		p0 = new Point(p);
		a = norm.x; // A
		b = norm.y; // B
		c = norm.z; // C
		d = a * p0.x + b * p0.y + c * p0.z; // D
	}
	
	/**
	 * Distance from a point to the plane
	 * @param p the point in question
	 * @return the distance to the plane
	 */
	public double distance(Point p) {
		return distance(p.x, p.y, p.z);
	}

	/**
	 * Distance from a point to the plane
	 * @param x the x coordinate 
	 * @param y the y coordinate 
	 * @param z the z coordinate 
	 * @return the distance to the plane
	 */
	public double distance(double x, double y, double z) {
		return Math.abs(signedDistance(x, y, z));
	}
	
	
	/**
	 * Signed distance from a point to the plane
	 * @param p the point in question
	 * @return the signed distance (indicates which side you are on where norm defines positive side)
	 */
	public double signedDistance(Point p) {
		return signedDistance(p.x, p.y, p.z);
	}

	
	/**
	 * Signed distance from a point to the plane
	 * @param x the x coordinate 
	 * @param y the y coordinate 
	 * @param z the z coordinate 
	 * @return the signed distance (indicates which side you are on where norm defines positive side)
	 */
	public double signedDistance(double x, double y, double z) {
		if (Double.isNaN(_denom)) {
			_denom = Math.sqrt(a*a + b*b + c*c);
		}
		return (a*x + b*y + c*z - d)/_denom;
	}

	
	/**
	 * Compute the intersection of an infinite line with the plane
	 * 
	 * @param line         the line
	 * @param intersection will hold the point of intersection
	 * @return the t parameter. If NaN it means the line is parallel to the plane.
	 *         If t [0,1] then the segment intersects the line. If t outside [0, 1]
	 *         the infinite line intersects the plane, but not the segment
	 */
	public double lineIntersection(Line line, Point intersection) {

		// use the formula for the t parameter
		// t = ((ro - po) dot n)/(l dot n)
		// po "start" point of line
		// l is the "dP" of the line
		// ro given point on plane
		// n normal to plane

		double ldotn = line.getDelP().dot(norm);

		// if ldotn is zero, the line is parallel to the plane

		if (Math.abs(ldotn) < Constants.TINY) {
			intersection.set(Double.NaN, Double.NaN, Double.NaN);
			return Double.NaN;
		}

		Point pmr = Point.difference(p0, line.getP0());
		double numer = pmr.dot(norm);

		double t = numer / ldotn;
		line.getP(t, intersection);
		return t;
	}

	/**
	 * 
	 * @param line         the line
	 * @param intersection will hold the point of intersection
	 * @param lineType     one of the Constants INFINITE or SEGMENT
	 * @return the t parameter. If NaN, then either the line is parallel to the
	 *         plane or we are treating as a line segment and the segment does not
	 *         "reach" the plane
	 */
	public double lineIntersection(Line line, Point intersection, int lineType) {
		double t = lineIntersection(line, intersection);

		if (!Double.isNaN(t)) { // at least infinite line intersects
			if (lineType == Constants.SEGMENT) {
				if ((t < 0) || (t > 1)) {
					t = Double.NaN;
					intersection.set(Double.NaN, Double.NaN, Double.NaN);
				}
			}
		}

		return t;
	}

	/**
	 * Create a plane of constant azimuthal angle phi
	 * 
	 * @param phi the azimuthal angle in degrees
	 * @return the plane of constant phi
	 */
	public static Plane constantPhiPlane(double phi) {
		phi = Math.toRadians(phi);

		double cphi = Math.cos(phi);
		double sphi = Math.sin(phi);

		// point in the plane
		Point p = new Point(cphi, sphi, 0);

		// normal
		Vector norm = new Vector(sphi, -cphi, 0);

		return new Plane(norm, p);
	}

	@Override
	public String toString() {
		String pstr = String.format("abcd = [%10.6G, %10.6G, %10.6G, %10.6G]", a, b, c, d);
		return pstr + "  p = " + p0 + " norm = " + norm;
	}

	/**
	 * Obtain the line resulting from the intersection of this plane and another plane
	 * @param plane the other plane
	 * @return line formed by the intersection
	 */
	public Line planeIntersection(Plane plane) {
		Vector s = Vector.cross(norm, plane.norm);

		if (s.length() < Constants.TINY) {
			return null;
		}

		// need to find one point
		Point p0 = new Point();

		// try setting z to 0
		double ans[] = solve(a, b, d, plane.a, plane.b, plane.d);
		if (ans != null) {
			p0.set(ans[0], ans[1], 0);
		} else {// try setting y to 0
			ans = solve(a, c, d, plane.a, plane.c, plane.d);
			if (ans != null) {
				p0.set(ans[0], 0, ans[1]);
			} else {// try setting x to 0
				ans = solve(b, c, d, plane.b, plane.c, plane.d);
				if (ans != null) {
					p0.set(0, ans[0], ans[1]);
				} else {// toast
					return null;
				}

			}

		}

		Point p1 = new Point(p0.x + s.x, p0.y + s.y, p0.z + s.z);
		return new Line(p0, p1);
	}

	// solve simultaneous
	// a1x + b1y = d1
	// a2x + b2y = d2
	// by Cramer's rule
	private double[] solve(double a1, double b1, double d1, double a2, double b2, double d2) {
		double deter = a1 * b2 - a2 * b1;
		if (Math.abs(deter) < Constants.TINY) {
			return null;
		}

		double ans[] = new double[2];

		double deterx = d1 * b2 - d2 * b1;
		double detery = a1 * d2 - a2 * d1;
		ans[0] = deterx / deter;
		ans[1] = detery / deter;
		return ans;
	}

	public static void main(String arg[]) {
//		Plane p = constantPhiPlane(30);

		Point p = new Point(1, 1, 1);
		Vector norm = new Vector(1, 1, 1);

		Plane plane = new Plane(norm, p);

		System.out.println("Init plane: " + plane);
		
		System.out.println("should be 0: " + plane.distance(p));

		Point po = new Point(2, 4, -7);
		Point p1 = new Point(0, 2, 5);
		Point intersection = new Point();
		System.out.println("distance to ((2, 4, -7)): " + plane.distance(po));

		Line line = new Line(po, p1);
		double t = plane.lineIntersection(line, intersection);
		System.out.println(" t = " + t + "   intersect: " + intersection);

		double phi = -210;
		plane = constantPhiPlane(phi);
		System.out.println("constant phi plane phi = " + phi);
		t = plane.lineIntersection(line, intersection);
		System.out.println(" t = " + t + "   intersect: " + intersection + "  phicheck = "
				+ Math.toDegrees(Math.atan2(intersection.y, intersection.x)));

		// intersection of two phi planes should be z axis
		Plane pp1 = constantPhiPlane(57);
		Plane pp2 = constantPhiPlane(99);
		System.out.println("Intersection of two phi planes: " + pp1.planeIntersection(pp2));
	}

}