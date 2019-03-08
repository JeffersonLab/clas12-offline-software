package cnuphys.swim.util;

import cnuphys.lund.DoubleFormat;

/**
 * Static classes for treating arrays of doubles as 2D or 3D vectors
 * 
 * @author heddle
 * 
 */
public class VectorSupport {

	// tiny number check
	private static final double TINY = 1.0e-40;

	/**
	 * Create a vector as a double array from its components
	 * 
	 * @param comp an arbitrary number of components
	 * @return the vector
	 */
	public static double[] createVector(double... comp) {
		return comp;
	}

	/**
	 * Get the square of the magnitude of the vector.
	 * 
	 * @param v the vector (of any length)
	 * @return the square of the magnitude of the vector
	 */
	public static double lengthSquared(float v[]) {
		if (v == null) {
			logWarning("null vector in lengthSquared method");
			return Double.NaN;
		}
		double sum = 0;
		for (int i = 0; i < v.length; i++) {
			sum += (v[i] * v[i]);
		}
		return sum;
	}

	/**
	 * Get the square of the magnitude of the vector.
	 * 
	 * @param v the vector (of any length)
	 * @return the square of the magnitude of the vector
	 */
	public static double lengthSquared(double v[]) {
		if (v == null) {
			logWarning("null vector in lengthSquared method");
			return Double.NaN;
		}
		double sum = 0;
		for (int i = 0; i < v.length; i++) {
			sum += (v[i] * v[i]);
		}
		return sum;
	}

	/**
	 * Get the magnitude of the vector.
	 * 
	 * @param v the vector (of any length)
	 * @return the magnitude of the vector
	 */
	public static double length(float v[]) {
		if (v == null) {
			logWarning("null vector in length method");
			return Double.NaN;
		}
		return Math.sqrt(lengthSquared(v));
	}

	/**
	 * Get the magnitude of the vector.
	 * 
	 * @param v the vector (of any length)
	 * @return the magnitude of the vector
	 */
	public static double length(double v[]) {
		if (v == null) {
			logWarning("null vector in length method");
			return Double.NaN;
		}
		return Math.sqrt(lengthSquared(v));
	}

	/**
	 * Set all the components
	 * 
	 * @param v          the vector (of any length)
	 * @param components the vector components. The number of entries should match
	 *                   the length of v.
	 */
	public static void set(double v[], double... components) {
		if ((v == null) || (components == null)) {
			logWarning("null vector in set method");
			return;
		}

		int vlen = v.length;
		int clen = components.length;

		if (vlen != clen) {
			logWarning("unmatched lengths in set method. Vlen=" + vlen + " Clen=" + clen);
		}

		for (int i = 0; i < Math.min(vlen, clen); i++) {
			v[i] = components[i];
		}
	}

	/**
	 * Scale the vector
	 * 
	 * @param v           the vector
	 * @param scaleFactor the scale factor
	 */
	public void scale(double v[], double scaleFactor) {

		if (v == null) {
			logWarning("null vector in scale method");
			return;
		}

		for (int i = 0; i < v.length; i++) {
			v[i] *= scaleFactor;
		}
	}

	private static void logWarning(String warning) {
		String wstr = "[VectorSupport] " + warning;
		System.err.println(wstr);
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param v      the vector (of any length)
	 * @param numDec the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	public static String toString(double v[], int numDec) {

		if (v == null) {
			return null;
		}

		int len = v.length;
		StringBuffer sb = new StringBuffer(256);

		sb.append("(");
		for (int i = 0; i < len; i++) {
			sb.append(DoubleFormat.doubleFormat(v[i], numDec));
			if (i != (len - 1)) {
				sb.append(", ");
			}
		}
		sb.append(")");

		return sb.toString();
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)" using three decimals
	 * points.
	 * 
	 * @param v      the vector (of any length)
	 * @param numDec the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	public static String toString(double v[]) {
		return toString(v, 3);
	}

	/**
	 * Obtain a unit vector in the same direction as this vector
	 * 
	 * @param v the vector (of any length)
	 * @return a unit vector in the same direction as this vector
	 */
	public static double[] unitVector(double v[]) {

		if (v == null) {
			return null;
		}

		double len = length(v);

		if (Double.isNaN(len) || (len < TINY)) {
			logWarning("null or zero vector in unitVector method");
			return null;
		}

		double u[] = new double[v.length];
		for (int i = 0; i < v.length; i++) {
			u[i] = v[i] / len;
		}
		return u;
	}

	/**
	 * Usual inner product
	 * 
	 * @param v1 one vector on any length
	 * @param v2 another vector of matching length
	 * @return the dot product
	 */
	public static double dot(double[] v1, double[] v2) {
		if ((v1 == null) || (v2 == null)) {
			logWarning("null vector in dot (product) method");
			return Double.NaN;
		}

		int len1 = v1.length;
		int len2 = v2.length;

		if (len1 != len2) {
			logWarning("unmatched lengths in dot (product) method. len1=" + len1 + " len2=" + len2);
		}

		double sum = 0;
		for (int i = 0; i < Math.min(len1, len2); i++) {
			sum += (v1[i] * v2[i]);
		}

		return sum;
	}

	/**
	 * Get the vector cross product. The vectors must match and be 2D or 3D. A 3D
	 * vector is return. For for two 2D inputs, the result look like
	 * 
	 * 
	 * @param v1 one vector
	 * @param v2 another vector
	 * @return the magnitude of the cross product
	 */
	public static double[] cross(double[] v1, double[] v2) {
		if ((v1 == null) || (v2 == null)) {
			logWarning("null vector in cross (product) method");
			return null;
		}

		int len1 = v1.length;
		int len2 = v2.length;

		if (len1 != len2) {
			logWarning("unmatched lengths in cross (product) method. len1=" + len1 + " len2=" + len2);
			return null;
		}

		if ((len1 < 2) || (len1 > 3)) {
			logWarning("weird lengths in cross (product) method. len1=" + len1 + " len2=" + len2);
			return null;
		}

		if (len1 == 2) {
			v1 = make3D(v1);
			v2 = make3D(v2);
		}

		return crossProduct(v1, v2);
	}

	// make a 3d vector from a 2D vector put 0 in z component
	private static double[] make3D(double v[]) {
		if (v == null) {
			return null;
		}

		int len = v.length;
		if (len == 3) {
			return v;
		} else if (len == 2) {
			double v3[] = new double[3];
			v3[0] = v[0];
			v3[1] = v[1];
			v3[2] = 0;
			return v3;
		} else {
			logWarning("weird vector of length " + len + " in make3D method");
			return null;
		}
	}

	// usual cross product
	private static double[] crossProduct(double a[], double b[]) {
		double c[] = new double[3];

		c[0] = a[1] * b[2] - a[2] * b[1];
		c[1] = a[2] * b[0] - a[0] * b[2];
		c[2] = a[0] * b[1] - a[1] * b[0];

		return c;
	}

	/**
	 * Get the angle between two vectors
	 * 
	 * @param v1 one vector of any length (2 or 3)
	 * @param v2 another vector of matching length
	 * @return the angle between the vectors in degrees.
	 */
	public static double angleBetween(double[] v1, double[] v2) {

		if ((v1 == null) || (v2 == null)) {
			logWarning("null vector in dot (product) method");
			return Double.NaN;
		}

		double length1 = length(v1);
		if (length1 < TINY) {
			return 0.0;
		}

		double length2 = length(v2);
		if (length2 < TINY) {
			return 0.0;
		}

		double ang = Math.acos(dot(v1, v2) / (length1 * length2));
		return Math.toDegrees(ang);
	}

	/**
	 * Project one vector onto another vector
	 * 
	 * @param v the vector being projected
	 * @param a the vector defining the projection direction.
	 * @return the result of projecting v in the direction of a.
	 */
	public static double[] project(double[] v, double[] a) {

		int lenv = v.length;
		int lena = a.length;

		if (lenv != lena) {
			logWarning("unmatched lengths in project method. lenv=" + lenv + " lena=" + lena);
		}
		int len = Math.min(lenv, lena);

		double aunit[] = unitVector(a);
		if (aunit == null) {
			return null;
		}

		double dot = dot(v, aunit);

		double projectedV[] = new double[len];

		for (int i = 0; i < len; i++) {
			projectedV[i] = dot * aunit[i];
		}

		return projectedV;
	}

	/**
	 * The vector difference a - b
	 * 
	 * @param a one vector
	 * @param b the vector being subtracted
	 * @return the difference a - b
	 */
	public static double[] diff(double a[], double b[]) {
		if ((a == null) || (b == null)) {
			logWarning("null vector in diff method");
			return null;
		}

		int lenb = b.length;
		int lena = a.length;

		if (lenb != lena) {
			logWarning("unmatched lengths in project method. lena=" + lena + " lenb=" + lenb);
		}
		int len = Math.min(lenb, lena);
		double c[] = new double[len];

		for (int i = 0; i < len; i++) {
			c[i] = a[i] - b[i];
		}
		return c;
	}

}