package cnuphys.magfield;

public class FastMath {

	/** Which atan2, etc. algorithms to use */
	public static enum MathLib {
		DEFAULT, FAST, SUPERFAST;
	}

	// controls which algorithms to use
	private static MathLib _mathLib = MathLib.FAST;

	/**
	 * Might use standard or fast atan2
	 * 
	 * @param y
	 * @param x
	 * @return arctan(y/x) over 2Pi radians
	 */
	public static double atan2(float y, float x) {

		switch (_mathLib) {
		case FAST:
			return org.apache.commons.math3.util.FastMath.atan2(y, x);
		case SUPERFAST:
			return Icecore.atan2(y, x);
		default:
			return Math.atan2(y, x);
		}
	}

	/**
	 * Might use standard or fast atan2
	 * 
	 * @param y
	 * @param x
	 * @return arctan(y/x) over 2Pi radians
	 */
	public static double atan2(double y, double x) {

		switch (_mathLib) {
		case FAST:
			return org.apache.commons.math3.util.FastMath.atan2(y, x);
		case SUPERFAST:
			return Icecore.atan2((float) y, (float) x);
		default:
			return Math.atan2(y, x);
		}

	}

	/**
	 * Might use standard or fast atan2
	 * 
	 * @param y
	 * @param x
	 * @return arctan(y/x) over 360 degrees
	 */
	public static double atan2Deg(float y, float x) {
		return Math.toDegrees(atan2(y, x));
	}

	/**
	 * Might use standard or fast atan2
	 * 
	 * @param y
	 * @param x
	 * @return atan2(y, x)
	 */
	public static double atan2Deg(double y, double x) {
		return Math.toDegrees(atan2(y, x));
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static double hypot(double x, double y) {
		return sqrt(x * x + y * y);
	}

	/**
	 * 
	 * @param x
	 * @return
	 */
	public static double acos(double x) {

		switch (_mathLib) {
		case FAST:
		case SUPERFAST:
			return org.apache.commons.math3.util.FastMath.acos(x);
		default:
			return Math.acos(x);
		}

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
	 * Fast version of usual square root
	 * 
	 * @param x the value
	 * @return the square root of x
	 */
	public static double sqrt(double x) {
		switch (_mathLib) {
		case FAST:
		case SUPERFAST:
			return org.apache.commons.math3.util.FastMath.sqrt(x);
		default:
			return Math.sqrt(x);
		}

	}

	/**
	 * Might use standard or fast sin
	 * 
	 * @param x the angle in radians
	 * @return the sine
	 */
	public static double sin(double x) {

		switch (_mathLib) {
		case FAST:
			return org.apache.commons.math3.util.FastMath.sin(x);
		case SUPERFAST:
			return Riven.sin((float) x);
		default:
			return Math.sin(x);
		}

	}

	/**
	 * Might use standard or fast cos
	 * 
	 * @param x the angle in radians
	 * @return the cosine
	 */
	public static double cos(double x) {

		switch (_mathLib) {
		case FAST:
			return org.apache.commons.math3.util.FastMath.cos(x);
		case SUPERFAST:
			return Riven.cos((float) x);
		default:
			return Math.cos(x);
		}
	}

	/**
	 * Get the math lib being used
	 * 
	 * @return the math lib being used
	 */
	public static MathLib getMathLib() {
		return _mathLib;
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
		return (float) sqrt(vx * vx + vy * vy + vz * vz);
	}

	/**
	 * Set the math library to use
	 * 
	 * @param lib the math library enum
	 */
	public static void setMathLib(MathLib lib) {
		_mathLib = lib;
	}

	// faster sin and cos from gamers
	public static final class Riven {

		private static final int SIN_BITS, SIN_MASK, SIN_COUNT;
		private static final float radFull, radToIndex;
		private static final float degFull, degToIndex;
		private static final float[] sin, cos;

		static {
			SIN_BITS = 12;
			SIN_MASK = ~(-1 << SIN_BITS);
			SIN_COUNT = SIN_MASK + 1;

			radFull = (float) (Math.PI * 2.0);
			degFull = (float) (360.0);
			radToIndex = SIN_COUNT / radFull;
			degToIndex = SIN_COUNT / degFull;

			sin = new float[SIN_COUNT];
			cos = new float[SIN_COUNT];

			for (int i = 0; i < SIN_COUNT; i++) {
				sin[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
				cos[i] = (float) Math.cos((i + 0.5f) / SIN_COUNT * radFull);
			}

			// Four cardinal directions (credits: Nate)
			for (int i = 0; i < 360; i += 90) {
				sin[(int) (i * degToIndex) & SIN_MASK] = (float) Math.sin(i * Math.PI / 180.0);
				cos[(int) (i * degToIndex) & SIN_MASK] = (float) Math.cos(i * Math.PI / 180.0);
			}
		}

		public static final float sin(float rad) {
			return sin[(int) (rad * radToIndex) & SIN_MASK];
		}

		public static final float cos(float rad) {
			return cos[(int) (rad * radToIndex) & SIN_MASK];
		}
	}

}
