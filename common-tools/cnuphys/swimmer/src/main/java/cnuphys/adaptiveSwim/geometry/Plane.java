package cnuphys.adaptiveSwim.geometry;


/**
 * A plane is defined by the equation (r - ro).norm = 0 Where r is an arbitrary
 * point on the plane, ro is a given point on the plane and norm is the normal
 * to the plane
 * 
 * @author heddle
 *
 */
public class Plane extends AGeometric {

	// unit vector normal to plane
	public final Vector norm;

	// point on the plane
	public final Point p0;

	// for the form ax + by + cz = d;
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
	 * Create a plane from the normal vector in an array of doubles and
	 * a point in the plane in an array, both (x, y, z)
	 * @param norm the normal
	 * @param point the point in the plane
	 */
	public Plane(double norm[], double point[]) {
		
		this(new Vector(norm[0], norm[1], norm[2]), 
				new Point(point[0], point[1], point[2]));
	}


	/**
	 * Create a plane from a normal vector and a point on the plane
	 * @param nx x component of normal vector
	 * @param ny y component of normal vector
	 * @param nz z component of normal vector
	 * @param px x component of point on plane
	 * @param py y component of point on plane
	 * @param pz z component of point on plane
	 */
	public Plane(double nx, double ny, double nz, double px, double py, double pz) {
		
		this(new Vector(nx, ny, nz), 
				new Point(px, py, pz));
	}

	/**
	 * Create a line from two points and then get the intersection with the plane
	 * @param p1 one point
	 * @param p2 another point
	 * @param p will hold the intersection, NaNs if no intersection
	 * @return the t parameter. If NaN it means the line is parallel to the plane.
	 *         If t [0,1] then the segment intersects the plane. If t outside [0, 1]
	 *         the infinite line intersects the plane, but not the segment
	 */
	@Override
	public double interpolate(Point p1, Point p2, Point p) {
		Line line = new Line(p1, p2);
		return lineIntersection(line, p);
	}

	/**
	 * Signed distance from a point to the plane
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the signed distance (indicates which side you are on where norm
	 *         defines positive side)
	 */
	@Override
	public double signedDistance(double x, double y, double z) {
		if (Double.isNaN(_denom)) {
			_denom = Math.sqrt(a * a + b * b + c * c);
		}
		return (a * x + b * y + c * z - d) / _denom;
	}

	/**
	 * Compute the intersection of an infinite line with the plane
	 * 
	 * @param line         the line
	 * @param intersection will hold the point of intersection
	 * @return the t parameter. If NaN it means the line is parallel to the plane.
	 *         If t [0,1] then the segment intersects the plane. If t outside [0, 1]
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
	 * Obtain the line resulting from the intersection of this plane and another
	 * plane
	 * 
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
		if (tiny(deter)) {
			return null;
		}

		double ans[] = new double[2];

		double deterx = d1 * b2 - d2 * b1;
		double detery = a1 * d2 - a2 * d1;
		ans[0] = deterx / deter;
		ans[1] = detery / deter;
		return ans;
	}

	// is the value essentially 0?
	private boolean tiny(double v) {
		return Math.abs(v) < Constants.TINY;
	}

	/**
	 * Find some coordinates suitable for drawing the plane as a Quad in 3D
	 * 
	 * @param scale an arbitrary big number, a couple times bigger than the drawing
	 *              extent
	 * @return the jogl coordinates for drawing a Quad
	 */
	public float[] planeQuadCoordinates(float scale) {

		int[] i1 = { -1, -1, 1, 1 };
		int[] i2 = { -1, 1, 1, -1 };

		if (tiny(a) && tiny(b) && tiny(c)) {
			return null;
		}

		float[] coords = new float[12];

		// another point in the plane
		Point p1 = new Point();

		if (tiny(b) && tiny(c)) { // constant x plane
			float fx = (float) (d / a);
			for (int k = 0; k < 4; k++) {
				int j = 3 * k;

				float y = scale * i1[k];
				float z = scale * i2[k];

				coords[j] = fx;
				coords[j + 1] = y;
				coords[j + 2] = z;
			}

		} else if (tiny(a) && tiny(c)) { // constant y plane
			float fy = (float) (d / b);
			for (int k = 0; k < 4; k++) {
				int j = 3 * k;

				float x = scale * i1[k];
				float z = scale * i2[k];

				coords[j] = x;
				coords[j + 1] = fy;
				coords[j + 2] = z;
			}
		} else if (tiny(a) && tiny(b)) { // constant z plane
			float fz = (float) (d / c);
			for (int k = 0; k < 4; k++) {
				int j = 3 * k;

				float x = scale * i1[k];
				float y = scale * i2[k];

				coords[j] = x;
				coords[j + 1] = y;
				coords[j + 2] = fz;
			}
		}

		else if (tiny(a)) {
			for (int k = 0; k < 4; k++) {
				int j = 3 * k;

				float x = scale * i1[k];
				float y = scale * i2[k];
				float z = (float) ((d - b * y) / c);

				coords[j] = x;
				coords[j + 1] = y;
				coords[j + 2] = z;
			}
		}

		else if (tiny(b)) {
			for (int k = 0; k < 4; k++) {
				int j = 3 * k;

				float x = scale * i1[k];
				float y = scale * i2[k];
				float z = (float) ((d - a * x) / c);

				coords[j] = x;
				coords[j + 1] = y;
				coords[j + 2] = z;
			}

		}

		else if (tiny(c)) {
			for (int k = 0; k < 4; k++) {
				int j = 3 * k;

				float x = scale * i1[k];
				float z = scale * i2[k];
				float y = (float) ((d - a * x) / b);

				coords[j] = x;
				coords[j + 1] = y;
				coords[j + 2] = z;
			}

		}
		
		else { //general case, no small constants
			for (int k = 0; k < 4; k++) {
				int j = 3 * k;

				float x = scale * i1[k];
				float y = scale * i2[k];
				float z = (float) ((d - a * x - b * y) / c);

				coords[j] = x;
				coords[j + 1] = y;
				coords[j + 2] = z;
			}
		}

		return coords;
	}

}