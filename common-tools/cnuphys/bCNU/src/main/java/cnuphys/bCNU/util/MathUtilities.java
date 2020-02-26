package cnuphys.bCNU.util;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Comparator;

public class MathUtilities {
	
	/**
	 * Get an int as an unsigned long
	 * @param x the int
	 * @return the unsigned long
	 */
	public static long getUnsignedInt(int x) {
	    return x & 0x00000000ffffffffL;
	}

	/**
	 * Given two points p0 and p1, imagine a line from p0 to p1. Take the line
	 * to be parameterized by parameter t so that at t = 0 we are at p0 and t =
	 * 1 we are at p1.
	 * 
	 * @param p0
	 *            start point of main line
	 * @param p1
	 *            end point of main line
	 * @param wp
	 *            the point from which we drop a perpendicular to p0 -> p1
	 * @param pintersect
	 *            the intersection point of the perpendicular and the line
	 *            containing p0-p1. It may or may not actually be between p0 and
	 *            p1, as specified by the value of t.
	 * @return the perpendicular distance to the line. If t is between 0 and 1
	 *         the intersection is on the line. If t < 0 the intersection is on
	 *         the "infinite line" but not on p0->p1, it is on the p0 side; this
	 *         returns the distance to p0. If t > 1 the intersection is on the
	 *         p1 side; this returns the distance to p1.
	 */
	public static double perpendicularDistance(Point2D.Double p0,
			Point2D.Double p1, Point2D.Double wp, Point2D.Double pintersect) {
		double delx = p1.x - p0.x;
		double dely = p1.y - p0.y;

		double numerator = delx * (wp.x - p0.x) + dely * (wp.y - p0.y);
		double denominator = delx * delx + dely * dely;
		double t = numerator / denominator;
		pintersect.x = p0.x + t * delx;
		pintersect.y = p0.y + t * dely;

		if (t < 0.0) { // intersection not on line, on p0 side
			return p0.distance(wp);
		} else if (t > 1.0) {// intersection not on line, on p1 side
			return p1.distance(wp);
		}
		// intersection on line
		return pintersect.distance(wp);
	}

	/**
	 * Given two points p0 and p1, imagine a line from p0 to p1. Take the line
	 * to be parameterized by parameter t so that at t = 0 we are at p0 and t =
	 * 1 we are at p1.
	 * 
	 * @param p0
	 *            start point of main line
	 * @param p1
	 *            end point of main line
	 * @param wp
	 *            the point from which we drop a perpendicular to p0 -> p1
	 * @param pintersect
	 *            the intersection point of the perpendicular and the line
	 *            containing p0-p1. It may or may not actually be between p0 and
	 *            p1, as specified by the return argument.
	 * @return the value of the t parameter. If it is between 0 and 1 the
	 *         intersection is on the line. If t < 0 the intersection is on the
	 *         "infinite line" but not on p0->p1, it is on the p0 side. If t > 1
	 *         the intersection is on the p1 side.
	 */
	public static double perpendicularIntersection(Point2D.Double p0,
			Point2D.Double p1, Point2D.Double wp, Point2D.Double pintersect) {
		double delx = p1.x - p0.x;
		double dely = p1.y - p0.y;

		double numerator = delx * (wp.x - p0.x) + dely * (wp.y - p0.y);
		double denominator = delx * delx + dely * dely;
		double t = numerator / denominator;
		pintersect.x = p0.x + t * delx;
		pintersect.y = p0.y + t * dely;
		return t;
	}

	/**
	 * Given two points p0 and p1, imagine a line from p0 to p1. Take the line
	 * to be parameterized by parameter t so that at t = 0 we are at p0 and t =
	 * 1 we are at p1.
	 * 
	 * @param x1
	 *            x coordinate of one endpoint
	 * @param y1
	 *            y coordinate of one endpoint
	 * @param x2
	 *            x coordinate of other endpoint
	 * @param y2
	 *            y coordinate of other endpoint
	 * @param wp
	 *            the point from which we drop a perpendicular to p0 -> p1
	 * @param pintersect
	 *            the intersection point of the perpendicular and the line
	 *            containing p0-p1. It may or may not actually be between p0 and
	 *            p1, as specified by the return argument.
	 * @return the value of the t parameter. If it is between 0 and 1 the
	 *         intersection is on the line. If t < 0 the intersection is on the
	 *         "infinite line" but not on p0->p1, it is on the p0 side. If t > 1
	 *         the intersection is on the p1 side.
	 */
	public static double perpendicularIntersection(double x1, double y1,
			double x2, double y2, Point2D.Double wp, Point2D.Double pintersect) {
		double delx = x2 - x1;
		double dely = y2 - y1;

		double numerator = delx * (wp.x - x1) + dely * (wp.y - y1);
		double denominator = delx * delx + dely * dely;
		double t = numerator / denominator;
		pintersect.x = x1 + t * delx;
		pintersect.y = y1 + t * dely;
		return t;
	}

	/**
	 * Given two points p0 and p1, imagine a line from p0 to p1. Take the line
	 * to be parameterized by parameter t so that at t = 0 we are at p0 and t =
	 * 1 we are at p1.
	 * 
	 * @param p0
	 *            start point of main line
	 * @param p1
	 *            end point of main line
	 * @param wp
	 *            the point from which we drop a perpendicular to p0 -> p1
	 * @return the value of the t parameter. If it is between 0 and 1 the
	 *         intersection is on the line. If t < 0 the intersection is on the
	 *         "infinite line" but not on p0->p1, it is on the p0 side. If t > 1
	 *         the intersection is on the p1 side.
	 */
	public static double perpendicularIntersection(Point2D.Double p0,
			Point2D.Double p1, Point2D.Double wp) {
		double delx = p1.x - p0.x;
		double dely = p1.y - p0.y;

		double numerator = delx * (wp.x - p0.x) + dely * (wp.y - p0.y);
		double denominator = delx * delx + dely * dely;
		double t = numerator / denominator;
		return t;
	}

	/**
	 * Given an array of pixel points, this rearranges the array into a convex
	 * hull.
	 * 
	 * @param points
	 *            the array of pixel points.
	 * @return an index which is less than or equal to the size of the points
	 *         array. Use that many points as the convex hull. In other words,
	 *         if points.length = 10 and this returns 6, then points[0] through
	 *         points[5] will be the convex hull.
	 */
	public static int getConvexHull(Point points[]) {
		if (points == null) {
			return 0;
		}
		int len = points.length;
		if (len < 4) {
			return len;
		}

		Point2D.Double wp[] = new Point2D.Double[len];
		for (int i = 0; i < len; i++) {
			wp[i] = new Point2D.Double(points[i].x, points[i].y);
		}

		int n = getConvexHull(wp);
		for (int i = 0; i < len; i++) {
			points[i].setLocation(wp[i].x, wp[i].y);
		}

		return n;
	}

	/**
	 * Given an array of world points, this rearranges the array into a convex
	 * hull.
	 * 
	 * @param points
	 *            the array of world points.
	 * @return an index which is less than or equal to the size of the points
	 *         array. Use that many points as the convex hull. In other words,
	 *         if points.length = 10 and this returns 6, then points[0] through
	 *         points[5] will be the convex hull.
	 */
	public static int getConvexHull(Point2D.Double points[]) {

		if ((points == null) || (points.length < 1)) {
			return -1;
		}

		if (points.length < 3) {
			return points.length;
		}

		// /*
		// * PSEUDO CODE
		// let N = number of points
		// let points[N+1] = the array of points
		// swap points[1] with the point with the lowest y-coordinate
		// sort points by polar angle with points[1]
		//
		// # We want points[0] to be a sentinel point that will stop the loop.
		// let points[0] = points[N]
		//
		// # M will denote the number of points on the convex hull.
		// let M = 2
		// for i = 3 to N:
		// # Find next valid point on convex hull.
		// while ccw(points[M-1], points[M], points[i]) <= 0:
		// M -= 1
		//
		// # Swap points[i] to the correct place and update M.
		// M += 1
		// swap points[M] with points[i]
		//
		// */

		int np = points.length;

		/* now the graham scan -- first find point with min y */
		int min = 0;
		for (int i = 1; i < np; i++) {
			if (points[i].y < points[min].y) {
				min = i;
			}
			// break an unlikely tie
			if (points[i].y == points[min].y) {
				if (points[i].x < points[min].x) {
					min = i;
				}
			}
		}

		/* swap min with zeroth */
		Point2D.Double twp = points[0];
		points[0] = points[min];
		points[min] = twp;

		// sort base on polar angle wrt points[0]

		final Point2D.Double p0 = points[0];
		Comparator<Point2D.Double> comparator = new Comparator<Point2D.Double>() {

			@Override
			public int compare(Point2D.Double wp1, Point2D.Double wp2) {
				Point2D.Double v1 = new Point2D.Double(wp1.x - p0.x, wp1.y
						- p0.y);
				Point2D.Double v2 = new Point2D.Double(wp2.x - p0.x, wp2.y
						- p0.y);
				double ang1 = Math.atan2(v1.y, v1.x);
				double ang2 = Math.atan2(v2.y, v2.x);
				if (ang1 < ang2) {
					return -1;
				}
				if (ang1 > ang2) {
					return 1;
				}
				return 0;
			}
		};

		Arrays.sort(points, comparator);

		int M = 2;
		for (int i = 3; i < np; i++) {
			while ((M > 0) && (ccw(points[M - 1], points[M], points[i]) <= 0.0)) {
				M--;
			}

			M++;

			twp = points[M];
			points[M] = points[i];
			points[i] = twp;
		}

		return M + 1;

	}

	// Three points are a counter-clockwise turn if ccw > 0, clockwise if
	// ccw < 0, and collinear if ccw = 0 because ccw is a determinant that
	// gives the signed area of the triangle formed by p1, p2, and p3.
	// Used by convex hull algorithm
	private static double ccw(Point2D.Double p1, Point2D.Double p2,
			Point2D.Double p3) {
		return (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x);
	}

	/**
	 * Sort an array with an index sort
	 * 
	 * @param <T>
	 * @param a
	 *            the array to sort
	 * @param c
	 *            the comparator
	 * @return the index sorted array
	 */
	public static <T> int[] indexSort(final T[] a, final Comparator<? super T> c) {
		Integer indexArray[] = new Integer[a.length];
		for (int i = 0; i < indexArray.length; i++) {
			indexArray[i] = i;
		}

		Comparator<Integer> comparator = new Comparator<Integer>() {

			@Override
			public int compare(Integer i1, Integer i2) {
				return c.compare(a[i1], a[i2]);
			}
		};

		Arrays.sort(indexArray, comparator);

		int iarray[] = new int[indexArray.length];
		for (int i = 0; i < indexArray.length; i++) {
			iarray[i] = indexArray[i];
		}
		return iarray;
	}

	/**
	 * Sort an array of doubles with an index sort
	 * 
	 * @param a
	 *            the array to sort
	 * @return the resulting index sorted array.
	 */
	@SuppressWarnings("unchecked")
	public static int[] indexSort(final double[] a) {
		Integer indexArray[] = new Integer[a.length];
		for (int i = 0; i < indexArray.length; i++) {
			indexArray[i] = i;
		}

		final Comparator cdouble = new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				Double d1 = (Double) o1;
				Double d2 = (Double) o2;

				if (d1 < d2) {
					return -1;
				}
				if (d1 > d2) {
					return 1;
				}
				return 0;
			}
		};

		Comparator<Integer> comparator = new Comparator<Integer>() {

			@Override
			public int compare(Integer i1, Integer i2) {
				return cdouble.compare(a[i1], a[i2]);
			}
		};

		Arrays.sort(indexArray, comparator);

		int iarray[] = new int[indexArray.length];
		for (int i = 0; i < indexArray.length; i++) {
			iarray[i] = indexArray[i];
		}
		return iarray;
	}

	// main program for testing
	public static void main(String arg[]) {
		Point2D.Double wp0 = new Point2D.Double(0.0, 0.0);
		Point2D.Double wp1 = new Point2D.Double(0.0, 1.0);
		Point2D.Double wp2 = new Point2D.Double(1.0, 0.0);
		Point2D.Double wp3 = new Point2D.Double(1.0, 1.0);

		int np = 100;
		Point2D.Double wp[] = new Point2D.Double[np];
		wp[0] = wp0;
		wp[1] = wp1;
		wp[2] = wp2;
		wp[3] = wp3;
		for (int i = 4; i < np; i++) {
			wp[i] = new Point2D.Double(Math.random(), Math.random());
		}

		for (int i = 0; i < 1000; i++) {
			int j1 = (int) (np * Math.random());
			int j2 = (int) (np * Math.random());

			Point2D.Double twp = wp[j1];
			wp[j1] = wp[j2];
			wp[j2] = twp;
		}

		int m = getConvexHull(wp);
		System.out.println("m = " + m);

		for (int i = 0; i < m; i++) {
			System.out.println(wp[i].toString());
		}

		int index = 0;
		wp = new Point2D.Double[36];
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				wp[(index++)] = new Point2D.Double(i, j);
			}
		}

		m = getConvexHull(wp);
		System.out.println("m = " + m);

		for (int i = 0; i < m; i++) {
			System.out.println(wp[i].toString());
		}

		wp = new Point2D.Double[11];

		wp[0] = new Point2D.Double(130.415, 33.5681);
		wp[1] = new Point2D.Double(132.4828, 34.3936);
		wp[2] = new Point2D.Double(135.2542, 34.7356);
		wp[3] = new Point2D.Double(135.6694, 34.9519);
		wp[4] = new Point2D.Double(136.9044, 35.1428);
		wp[5] = new Point2D.Double(135.4703, 34.6594);
		wp[6] = new Point2D.Double(141.3347, 43.0639);
		wp[7] = new Point2D.Double(140.8922, 38.2611);
		wp[8] = new Point2D.Double(139.7847, 35.6828);
		wp[9] = new Point2D.Double(139.6697, 35.5231);
		wp[10] = new Point2D.Double(130.8653, 33.8711);

		m = getConvexHull(wp);
		System.out.println("m = " + m);

		for (int i = 0; i < m; i++) {
			System.out.println(wp[i].toString());
		}

		Integer array[] = { 23, 64, 10, 0, 6, 9, 6, 23, -27 };
		Comparator<Integer> c = new Comparator<Integer>() {

			@Override
			public int compare(Integer i1, Integer i2) {
				if (i1 < i2) {
					return -1;
				}
				if (i1 > i2) {
					return 1;
				}
				return 0;
			}
		};

		int indexArray[] = indexSort(array, c);

		for (int i = 0; i < indexArray.length; i++) {
			System.err.println(array[i] + ", " + indexArray[i] + ",  "
					+ array[indexArray[i]]);
		}

		System.err.println("\ndouble array test");
		double darray[] = { 23, 64, 10, 0, 6, 9, 6, 23, -27 };
		indexArray = indexSort(darray);
		for (int i = 0; i < indexArray.length; i++) {
			System.err.println(darray[i] + ", " + indexArray[i] + ",  "
					+ darray[indexArray[i]]);
		}

	}

}
