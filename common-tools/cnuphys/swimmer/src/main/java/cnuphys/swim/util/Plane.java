package cnuphys.swim.util;

/**
 * A plane is defined by the equation ax + by + cz = d
 * 
 * @author heddle
 *
 */
public class Plane {

	// the angle in degrees for rotating between tilted and sector CS
	private static double _angle = 25.0;
	private static double _sin25 = Math.sin(Math.toRadians(_angle));
	private static double _cos25 = Math.cos(Math.toRadians(_angle));

	// effectively zero
	private static double TINY = 1.0e-10;

	// the plane constants
	private double _a;
	private double _b;
	private double _c;
	private double _d;

	// use to compute distance to the plane
	private double _distDenom = Double.NaN;

	/**
	 * Create a plane of the form ax + by + cz = d
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 */
	public Plane(double a, double b, double c, double d) {
		_a = a;
		_b = b;
		_c = c;
		_d = d;
	}

	/**
	 * Create a plane from a normal vector and a distance to the plane
	 * 
	 * @param normX the x component of the normal vector
	 * @param normY the y component of the normal vector
	 * @param normZ the z component of the normal vector
	 * @param d     the d value of the plane ax + by + cz = d
	 * @return the plane that contains p and its normal is norm
	 */
	public static Plane createPlane(double normX, double normY, double normZ, double d) {
		double norm[] = { normX, normY, normZ };
		return createPlane(norm, d);
	}

	/**
	 * Create a plane from a normal vector and a distance to the plane
	 * 
	 * @param norm the outward normal vector where the components (0, 1, 2) map to
	 *             (x, y, z)
	 * @param d    the distance from the origin to the plane
	 * @return the plane that contains p and its normal is norm
	 */
	public static Plane createPlane(double norm[], double d) {
		// lets make it a unit vector
		double u[] = VectorSupport.unitVector(norm);

		double p[] = new double[3];
		p[0] = d * u[0];
		p[1] = d * u[1];
		p[2] = d * u[2];
		return createPlane(u, p);
	}

	/**
	 * Create a plane from a normal vector and a point on the plane
	 * 
	 * @param norm the normal vector where the components (0, 1, 2) map to (x, y, z)
	 * @param p    a point in the plane where the components (0, 1, 2) map to (x, y,
	 *             z)
	 * @return the plane that contains p and its normal is norm
	 */
	public static Plane createPlane(double norm[], double p[]) {
		// lets make it a unit vector
		double u[] = VectorSupport.unitVector(norm);

		double a = u[0];
		double b = u[1];
		double c = u[2];
		double d = a * p[0] + b * p[1] + c * p[2];
		return new Plane(a, b, c, d);
	}

	/**
	 * Create a plane in the clas12 tilted system
	 * 
	 * @param d the distance from the nominal target to the plane
	 * @return The plane
	 */
	public static Plane createTiltedPlane(double d) {
		double p[] = { d * _sin25, 0, d * _cos25 };
		double norm[] = { _sin25, 0, _cos25 };
		return createPlane(norm, p);
	}

	/**
	 * See if a point is contained (on) a plane
	 * 
	 * @param x         x coordinate
	 * @param y         y coordinate
	 * @param z         z coordinate
	 * @param tolerance the maximum distance from the plane in the same units as the
	 *                  coordinates
	 * @return
	 */
	public boolean contained(double x, double y, double z, double tolerance) {
		return distanceToPlane(x, y, z) < tolerance;
	}

	/**
	 * Get the perpendicular distance for a point to the plane
	 * 
	 * @param x the x coordinate of the point.
	 * @param y the y coordinate of the point.
	 * @param z the z coordinate of the point.
	 * @return the perpendicular distance
	 */
	public double distanceToPlane(double x, double y, double z) {
		if (Double.isNaN(_distDenom)) {
			_distDenom = Math.sqrt(_a * _a + _b * _b + _c * _c);
		}

		return Math.abs(dot(x, y, z)) / _distDenom;
	}

	/**
	 * Given a current position and velocity, compute the time to intersect the
	 * plane
	 * 
	 * @param x  x coordinate
	 * @param y  y coordinate
	 * @param z  z coordinate
	 * @param vx x component of velocity (arbitrary units)
	 * @param vy y component of velocity
	 * @param vz z component of velocity
	 * @return the time to intersect the plane
	 */
	public double timeToPlane(double x, double y, double z, double vx, double vy, double vz) {
		double denom = _a * vx + _b * vy + _c * vz;
		return dot(x, y, z) / denom;
	}

	/**
	 * Get the common construct a*x + b*y + c*z -d for the plane defined by ax + by
	 * + cz = d
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public double dot(double x, double y, double z) {
		return _a * x + _b * y + _c * z - _d;
	}

	/**
	 * Compute the intersection of an infinite line with the plane
	 * 
	 * @param p1 a point on the line where the components (0, 1, 2) map to (x, y, z)
	 * @param p2 a point on the line where the components (0, 1, 2) map to (x, y, z)
	 * @return the intersection p where the components (0, 1, 2) map to (x, y, z).
	 *         If there is no intersection, returns null.
	 */
	public double[] infiniteLineIntersection(double p1[], double p2[]) {

		double u = u(p1, p2);
		if (Double.isNaN(u)) {
			return null;
		}

		double p[] = new double[3];
		for (int i = 0; i < 3; i++) {
			p[i] = p1[i] + u * (p2[i] - p1[i]);
		}
		return p;
	}

	//
	// Get the u parameter of a line with the plane.
	// p1 one point on the
	// return NaN if the infinite line does not intersect the plane. Otherwise
	// return u. If 0<u<1 the segment intersects the plane. Otherwise the infinite
	// line
	// intersects the plane, but not the segment
	// @param p1 a point on the line where the components (0, 1, 2) map to (x, y, z)
	// @param p2 a point on the line where the components (0, 1, 2) map to (x, y, z)

	private double u(double p1[], double p2[]) {

		double denom = _a * (p1[0] - p2[0]) + _b * (p1[1] - p2[1]) + _c * (p1[2] - p2[2]);
		if (Math.abs(denom) < 1.0e-20) {
			return Double.NaN;
		}
		double numer = _a * p1[0] + _b * p1[1] + _c * p1[2] + _d;
		return numer / denom;
	}

	/**
	 * Get the sign of a point relative to a plane. If it is -1 it is on one side of
	 * the plane (call it left). If it is +1 it is on the other. Thus you can
	 * determine when you cross the plane: when the sign changes.
	 * 
	 * @param x the x coordinate of the point
	 * @param y the y coordinate of the point
	 * @param z the z coordinate of the point
	 * @return 0 if the point is on (or almost on modulo TINY) the plane, -1 if it
	 *         is on one side, + 1 if it is on the other.
	 */
	public int directionSign(double x, double y, double z) {
		double dot = _a * x + _b * y + _c * z - _d;

		if (Math.abs(dot) < TINY) {
			return 0;
		}

		return (dot < 0) ? -1 : 1;
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
		double p[] = new double[3];
		p[0] = cphi;
		p[1] = sphi;
		p[2] = 0;

		// normal
		double norm[] = new double[3];
		norm[0] = sphi;
		norm[1] = -cphi;
		norm[2] = 0;

		return createPlane(norm, p);
	}

	public static void main(String arg[]) {

	}

}
