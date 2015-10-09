package cnuphys.bCNU.util;

import java.awt.geom.Point2D;

public class Point2DSupport {

	// tiny number check
	private static final double TINY = 1.0e-40;

	/**
	 * This is creation by subtraction. Be very careful of the order. This
	 * return wp2 - wp1
	 * 
	 * @param wp1
	 *            the first point
	 * @param wp2
	 *            the second point
	 * @return the delta, wp2-wp1
	 */
	public static Point2D.Double pointDelta(Point2D.Double wp1,
			Point2D.Double wp2) {
		return new Point2D.Double(wp2.x - wp1.x, wp2.y - wp1.y);
	}

	/**
	 * The length of the vector
	 * 
	 * @param wp
	 *            the point in question
	 * @return the usual vector magnitude
	 */
	public static double length(Point2D.Double wp) {
		return Math.sqrt(wp.x * wp.x + wp.y * wp.y);
	}

	/**
	 * The length squared of the vector. This can be usefor for faster sorting.
	 * 
	 * @param wp
	 *            the point in question
	 * @return the square of the usual vector magnitude
	 */
	public static double lengthSquare(Point2D.Double wp) {
		return wp.x * wp.x + wp.y * wp.y;
	}

	/**
	 * Obtain a unit vector in the same direction as this vector
	 * 
	 * @param wp
	 *            the point in question
	 * @return a unit vector in the same direction as this vector
	 */
	public static Point2D.Double unitVector(Point2D.Double wp) {
		double len = length(wp);
		if (len < TINY) {
			return null;
		}
		return new Point2D.Double(wp.x / len, wp.y / len);
	}

	/**
	 * Usual inner product
	 * 
	 * @param v1
	 *            one vector
	 * @param v2
	 *            another vector
	 * @return the dot product
	 */
	public static double dot(Point2D.Double v1, Point2D.Double v2) {
		return v1.x * v2.x + v1.y * v2.y;
	}

	/**
	 * Get the magnitude of the cross product. The sign tells us whether we are
	 * in +z or -z direction.
	 * 
	 * @param v1
	 *            one vector
	 * @param v2
	 *            another vector
	 * @return the magnitude of the cross product
	 */
	public static double cross(Point2D.Double v1, Point2D.Double v2) {
		return v1.x * v2.y - v1.y * v2.x;
	}

	/**
	 * Get the angle between two vectors
	 * 
	 * @param v1
	 *            one vector
	 * @param v2
	 *            another vector
	 * @return the angle between the vectors in degrees.
	 */
	public static double angleBetween(Point2D.Double v1, Point2D.Double v2) {
		double len1 = length(v1);
		if (len1 < TINY) {
			return 0.0;
		}

		double len2 = length(v2);
		if (len2 < TINY) {
			return 0.0;
		}

		double ang = Math.acos(dot(v1, v2) / (len1 * len2));
		return Math.toDegrees(ang);
	}

	/**
	 * Project one vector onto another vector
	 * 
	 * @param v
	 *            the vector being projected
	 * @param a
	 *            the vector defining the projection direction.
	 * @return the result of projecting v in the direction of a.
	 */
	public static Point2D.Double project(Point2D.Double v, Point2D.Double a) {
		Point2D.Double aunit = unitVector(a);
		if (aunit == null) {
			return null;
		}

		double dot = dot(v, aunit);
		return new Point2D.Double(dot * aunit.x, dot * aunit.y);
	}

	/*
	 * Get the polar coordinate angle.
	 * 
	 * @return the angle, in degrees, measured ccw from the x axis.
	 */
	public static double angle(Point2D.Double wp) {
		return Math.toDegrees(Math.atan2(wp.y, wp.x));
	}

	/**
	 * Computes the distance between this world point and another.
	 * 
	 * @param wp
	 *            the other world point.
	 * @return the distance, i.e., the length of (wp - this).
	 */
	public static double distance(Point2D.Double wp0, Point2D.Double wp1) {
		double delx = wp1.x - wp0.x;
		double dely = wp1.y - wp0.y;
		return Math.sqrt(delx * delx + dely * dely);
	}

	/**
	 * Computes the azimuth between this world point and another.
	 * 
	 * @param wp
	 *            the other world point.
	 * @return the azimuth in degrees, where 0 is north, 90 east, etc.
	 */
	public static double azimuth(Point2D.Double wp0, Point2D.Double wp1) {
		double delx = wp1.x - wp0.x;
		double dely = wp1.y - wp0.y;
		return 90.0 - Math.toDegrees(Math.atan2(dely, delx));
	}

	/*
	 * Get a string representation.
	 * 
	 * @return a string representation.
	 */
	public static String toString(Point2D.Double wp) {
		return String.format("(%-7.4f, %-7.4f)", wp.x, wp.y);
	}
}
