package cnuphys.magfield;

public class FastMath {

	/**
	 * Use apache or JDK? Test show apache is faster for only these:
	 * hypot, asin, and acos
	 */

	/**
	 * ArcTan in degrees
	 *
	 * @param y the y value
	 * @param x the x value
	 * @return arctan(y/x) over 360 degrees
	 */
	public static double atan2Deg(double y, double x) {
		return Math.toDegrees(Math.atan2(y, x));
	}

	/**
	 * Usual hypot function, for which apache is faster
	 * @param x one value
	 * @param y other value
	 * @return the hypot of the two values
	 */
	public static double hypot(double x, double y) {
		return org.apache.commons.math3.util.FastMath.hypot(x, y);
	}

	/**
	 * Usual arc cosine, for which apache is faster
	 * @param x the value
	 * @return the acos of the value
	 */
	public static double acos(double x) {
		return org.apache.commons.math3.util.FastMath.acos(x);
	}

	/**
	 * Arc cosine returned in degrees
	 *
	 * @param x the cosine value
	 * @return acos in degrees
	 */
	public static double acos2Deg(double x) {
		return Math.toDegrees(acos(x));
	}

	/**
	 * Usual arc sine, for which apache is faster
	 * @param x the value
	 * @return the asin of the value
	 */
	public static double asin(double x) {
		return org.apache.commons.math3.util.FastMath.asin(x);
	}

	/**
	 * Arc sine returned in degrees
	 *
	 * @param x the sine value
	 * @return asin in degrees
	 */
	public static double asin2Deg(double x) {
		return Math.toDegrees(asin(x));
	}


	/**
	 * Vector length.
	 *
	 * @param v the v
	 * @return the float
	 */
	public static float vectorLength(float v[]) {
		float vx = v[0];
		float vy = v[1];
		float vz = v[2];
		return (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
	}


	/**
	 * atan2 added because somewhere in coatjava someone is calling
	 * FastMath.atan2
	 *
	 * @param y the y value
	 * @param x the x value
	 * @return arctan(y/x) over 2Pi
	 */
	public static double atan2(double y, double x) {
		return Math.atan2(y, x);
	}


}
