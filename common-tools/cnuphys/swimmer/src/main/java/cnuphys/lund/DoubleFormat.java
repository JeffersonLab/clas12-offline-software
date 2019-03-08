package cnuphys.lund;

import java.text.DecimalFormat;
import java.util.Hashtable;

public class DoubleFormat {

	/**
	 * cache formats which are few and often repeated to avoid recreating
	 */
	private static Hashtable<String, DecimalFormat> formats = new Hashtable<String, DecimalFormat>(143);

	/**
	 * Format a double
	 * 
	 * @param value  the value to format.
	 * @param numdec the number of digits right of the decimal.
	 */
	public static String doubleFormat(double value, int numdec) {
		return doubleFormat(value, numdec, false);
	}

	/**
	 * Format a double
	 * 
	 * @param value  the value to format.
	 * @param numdec the number of digits right of the decimal.
	 * @param scinot if <code>true</code>, use scientific notation.
	 */
	public static String doubleFormat(double value, int numdec, boolean scinot) {

		StringBuffer pattern = new StringBuffer();
		pattern.append("0.");

		for (int i = 0; i < numdec; i++) {
			pattern.append("0");
		}

		if (scinot) {
			pattern.append("E0");
		}

		String patternStr = pattern.toString();
		DecimalFormat df = null;
		df = (formats.get(patternStr));

		if (df == null) {
			df = new DecimalFormat(patternStr);
			formats.put(patternStr, df);
		}

		return df.format(value);

	}

	/**
	 * Format a double, switching from fixed to scientific notation.
	 * <p>
	 * Example: <br>
	 * <code>
	 * numdec = 6 <br>
	 * minExponent = 2 <br>
	 * value starts at 12345.67 and keep dividing by 10 gives <br>
	 * 1.234567E4 <br>
	 * 1.234567E3 <br>
	 * 1.234567E2 <br>
	 * 12.345670 <br>
	 * 1.234567 <br>
	 * 0.123457 <br>
	 * 1.234567E-2 <br>
	 * 1.234567E-3  <br>
	 * 1.234567E-4 <br>
	 * 1.234567E-5 <br>
	 * 1.234567E-6 <br>
	 * 1.234567E-7 <br>
	 * </code>
	 * 
	 * @param value       the value to format.
	 * @param numdec      the number of digits right of the decimal. If the
	 *                    exponenent is >= n or <= -n it will use sci notation.
	 * @param minExponent the minimum (absolute value) index for scientific
	 *                    notation.
	 */

	public static String doubleFormat(double value, int numdec, int minExponent) {

		if (Math.abs(value) < 1.0e-30) {
			return "0.0";
		}

		int exponent = (int) Math.log10(Math.abs(value));
		if (exponent < 0) {
			exponent = -exponent + 1;
		}

		if (exponent < minExponent) {
			return doubleFormat(value, numdec, false);
		} else {
			return doubleFormat(value, numdec, true);
		}

	}

	/**
	 * main program for testing.
	 * 
	 * @param args command line arguments (ignored.)
	 */
	public static void main(String[] args) {
		double d = 12345.67;

		for (int i = 0; i < 12; i++) {
			System.err.println(doubleFormat(d, 6, 2));
			// System.err.println(doubleFormat(-d, 6, 3));
			d = d / 10;
		}
	}

}
