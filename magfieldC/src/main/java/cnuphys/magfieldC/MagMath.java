package cnuphys.magfieldC;

public class MagMath {

	// use apache common math library "fastmath"
	private static boolean USEAPACHECOMMONMATH = true;

	/**
	 * Might use standard or fast cos
	 * 
	 * @param theta the angle in radians
	 * @return the cosine of the angle
	 */
	public static double cos(double theta) {
		if (USEAPACHECOMMONMATH) {
			return org.apache.commons.math3.util.FastMath.cos(theta);
		} else {
			return Math.cos(theta);
		}
	}
	
	/**
	 * Might use standard or fast sin
	 * 
	 * @param theta the angle in radians
	 * @return the sin of the angle
	 */
	public static double sin(double theta) {
		if (USEAPACHECOMMONMATH) {
			return org.apache.commons.math3.util.FastMath.sin(theta);
		} else {
			return Math.sin(theta);
		}
	}
	
	/**
	 * Might use standard or fast cos
	 * 
	 * @param theta the angle in degrees
	 * @return the cosine of the angle
	 */
	public static double cosDeg(double theta) {
		return cos(Math.toRadians(theta));
	}

	/**
	 * Might use standard or fast sin
	 * 
	 * @param theta the angle in degrees
	 * @return the sine of the angle
	 */
	public static double sinDeg(double theta) {
		return sin(Math.toRadians(theta));
	}

	
	/**
	 * Might use standard or fast sqrt
	 * 
	 * @param x the value
	 * @return the square root
	 */
	public static double sqrt(double x) {
		if (USEAPACHECOMMONMATH) {
			return org.apache.commons.math3.util.FastMath.sqrt(x);
		} else {
			return Math.sqrt(x);
		}
	}
	
	/**
	 * Might use standard or fast atan2
	 * 
	 * @param y the y coordinate
	 * @param x the x coordinate
	 * @return atan2(y, x) in radians
	 */
	public static double atan2(double y, double x) {
		if (USEAPACHECOMMONMATH) {
			return org.apache.commons.math3.util.FastMath.atan2(y, x);
		} else {
			return Math.atan2(y, x);
		}
	}

	/**
	 * Might use standard or fast atan2
	 * 
	 * @param y the y coordinate
	 * @param x the x coordinate
	 * @return atan2(y, x) in degrees
	 */
	public static double atan2Deg(double y, double x) {
		return Math.toDegrees(atan2(y, x));
	}

	/**
	 * The usual hypot function returns sqrt(x^2 + y^2)
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the hypot function of x, y
	 */
	public static double hypot(double x, double y) {
		if (USEAPACHECOMMONMATH) {
			return org.apache.commons.math3.util.FastMath.hypot(x, y);
		} else {
			return Math.sqrt(x * x + y * y);
		}
	}

	/**
	 * Set whether to use the fast (less accurate) math functions
	 * 
	 * @param useFast
	 *            the value of the flag.
	 */
	public static void setUseFastMath(boolean useFast) {
		USEAPACHECOMMONMATH = useFast;
	}

	/**
	 * Check whether we are using the fast (less accurate) math functions
	 * 
	 * @return <code>true</code> if we will use the fast (less accurate) version
	 *         of atan2
	 */
	public static boolean useFastMath() {
		return USEAPACHECOMMONMATH;
	}
}
