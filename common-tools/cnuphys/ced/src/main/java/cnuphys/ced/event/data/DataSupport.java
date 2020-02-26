package cnuphys.ced.event.data;

import java.util.List;
import org.jlab.geom.prim.Point3D;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.util.VectorSupport;
import cnuphys.ced.cedview.CedView;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.splot.plot.DoubleFormat;

public class DataSupport {

	// for uniform feedback colors
	public static final String prelimColor = "$orange$";
	public static final String trueColor = "$Alice Blue$";
	public static final String dgtzColor = "$Moccasin$";
	public static final String reconColor = "$coral$";

	// ec/pcal constants
	public static final String uvwStr[] = { "U", "V", "W" };
	public static final int FB_CLAS_XYZ = 01;
	public static final int FB_CLAS_RTP = 02; // r, theta, phi
	public static final int FB_TOTEDEP = 04; // total energy dep (MeV)
	public static final int FB_LOCAL_XYZ = 010;


	/**
	 * Add the pid information
	 * 
	 * @param pid the pid.array
	 * @param hitIndex the hit index
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void truePidFeedback(int pid[], int hitIndex,
			List<String> feedbackStrings) {

		if ((pid == null) || (hitIndex < 0) || (hitIndex >= pid.length)) {
			return;
		}

		feedbackStrings.add(trueColor + "pid " + pidStr(pid[hitIndex]));

	}



	/**
	 * Add positional feedback
	 * @param hitIndex
	 * @param x
	 * @param y
	 * @param z
	 * @param lx
	 * @param ly
	 * @param lz
	 * @param feedbackStrings
	 */
	public static void addXYZFeedback(int hitIndex, double x[], double y[],
			double z[], double lx[], double ly[], double lz[],
			List<String> feedbackStrings) {

		if ((x != null) && (y != null) && (z != null)
				&& (hitIndex < x.length)) {
			double v[] = new double[3];
			// to cm
			v[0] = x[hitIndex] / 10;
			v[1] = y[hitIndex] / 10;
			v[2] = z[hitIndex] / 10;
			feedbackStrings.add(trueColor + "hit global xyz "
					+ VectorSupport.toString(v, 2));
		}

		if ((lx != null) && (ly != null) && (lz != null)
				&& (hitIndex < lx.length)) {
			double v[] = new double[3];
			// to cm
			v[0] = lx[hitIndex] / 10;
			v[1] = ly[hitIndex] / 10;
			v[2] = lz[hitIndex] / 10;
			feedbackStrings.add(trueColor + "hit local xyz "
					+ VectorSupport.toString(v, 2));
		}
	}
	
	/**
	 * Safe method for extracting a byte from an array
	 * @param array the array
	 * @param index the index
	 * @return the value at the index or errorVal  on any error
	 */
	public static byte safeValue(byte[] array, int index, byte errorVal) {
		if (array == null) {
			return errorVal;
		}
		else if ((index < 0) || (index >= array.length)) {
			return errorVal;
		}
		else {
			return array[index];
		}
	}
	
	/**
	 * Safe method for extracting a short from an array
	 * @param array the array
	 * @param index the index
	 * @return the value at the index or errorVal  on any error
	 */
	public static short safeValue(short[] array, int index, short errorVal) {
		if (array == null) {
			return errorVal;
		}
		else if ((index < 0) || (index >= array.length)) {
			return errorVal;
		}
		else {
			return array[index];
		}
	}


	
	/**
	 * Safe method for extracting an int from an array
	 * @param array the array
	 * @param index the index
	 * @return the value at the index or errorVal  on any error
	 */
	public static int safeValue(int[] array, int index, int errorVal) {
		if (array == null) {
			return errorVal;
		}
		else if ((index < 0) || (index >= array.length)) {
			return errorVal;
		}
		else {
			return array[index];
		}
	}
	
	/**
	 * Safe method for extracting a long from an array
	 * @param array the array
	 * @param index the index
	 * @return the value at the index or errorVal  on any error
	 */
	public static long safeValue(int[] array, int index, long errorVal) {
		if (array == null) {
			return errorVal;
		}
		else if ((index < 0) || (index >= array.length)) {
			return errorVal;
		}
		else {
			return array[index];
		}
	}
	
	/**
	 * Safe method for extracting a float from an array
	 * @param array the array
	 * @param index the index
	 * @return the value at the index or errorVal on any error
	 */
	public static float safeValue(float[] array, int index, float errorVal) {
		if (array == null) {
			return errorVal;
		}
		else if ((index < 0) || (index >= array.length)) {
			return errorVal;
		}
		else {
			return array[index];
		}
	}

	/**
	 * Safe method for extracting a double from an array
	 * @param array the array
	 * @param index the index
	 * @return the value at the index or errorVal on any error
	 */
	public static double safeValue(double[] array, int index, double errorVal) {
		if (array == null) {
			return errorVal;
		}
		else if ((index < 0) || (index >= array.length)) {
			return errorVal;
		}
		else {
			return array[index];
		}
	}


	/**
	 * Safe way to get an integer element from an array for printing
	 * 
	 * @param array the array in question
	 * @param index the array index
	 * @return a string for printing
	 */
	public static String safeString(byte[] array, int index) {
		if (array == null) {
			return "null";
		}
		else if ((index < 0) || (index >= array.length)) {
			return "BADIDX: " + index;
		}
		else {
			return "" + array[index];
		}
	}
	/**
	 * Safe way to get an integer element from an array for printing
	 * 
	 * @param array the array in question
	 * @param index the array index
	 * @return a string for printing
	 */
	public static String safeString(short[] array, int index) {
		if (array == null) {
			return "null";
		}
		else if ((index < 0) || (index >= array.length)) {
			return "BADIDX: " + index;
		}
		else {
			return "" + array[index];
		}
	}

	/**
	 * Safe way to get an integer element from an array for printing
	 * 
	 * @param array the array in question
	 * @param index the array index
	 * @return a string for printing
	 */
	public static String safeString(int[] array, int index) {
		if (array == null) {
			return "null";
		}
		else if ((index < 0) || (index >= array.length)) {
			return "BADIDX: " + index;
		}
		else {
			return "" + array[index];
		}
	}


	/**
	 * Safe way to get an double element from an array for printing
	 * 
	 * @param array the array in question
	 * @param index the array index
	 * @param numdec the number of decimal points
	 * @return a string for printing
	 */
	public static String safeString(double[] array, int index, int numdec) {
		if (array == null) {
			return "null";
		}
		else if ((index < 0) || (index >= array.length)) {
			return "BADIDX: " + index;
		}
		else {
			return DoubleFormat.doubleFormat(array[index], numdec);
		}
	}


	/**
	 * Get a string for the particle Id
	 * 
	 * @param pid the Lund particle Id
	 * @return the string representation of the particle Id
	 */
	public static String pidStr(int pid) {

		if (pid == 0) {
			return "";
		}
		LundId lid = LundSupport.getInstance().get(pid);

		if (lid == null) {
			return "(" + pid + ") ??";
		}
		else {
			return lid.getName();
		}
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param v the vector (any double array)
	 * @param numDec the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	public static String vecStr(String prefix, double v[], int numDec,
			String postfix) {

		StringBuilder sb = new StringBuilder(128);
		int lm1 = v.length - 1;

		if (prefix != null) {
			sb.append(prefix + " ");
		}

		sb.append("(");

		for (int i = 0; i <= lm1; i++) {
			sb.append(DoubleFormat.doubleFormat(v[i], numDec));
			if (i != lm1) {
				sb.append(", ");
			}
		}

		sb.append(")");

		if (postfix != null) {
			sb.append(" " + postfix);
		}

		return sb.toString();
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param p3d the 3D point
	 * @param numDec the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	public static String p3dStr(String prefix, Point3D p3d, int numDec,
			String postfix) {

		StringBuilder sb = new StringBuilder(128);

		if (prefix != null) {
			sb.append(prefix + " ");
		}

		sb.append("(");

		sb.append(DoubleFormat.doubleFormat(p3d.x(), numDec) + ", ");
		sb.append(DoubleFormat.doubleFormat(p3d.y(), numDec) + ", ");
		sb.append(DoubleFormat.doubleFormat(p3d.z(), numDec));

		sb.append(")");

		if (postfix != null) {
			sb.append(" " + postfix);
		}

		return sb.toString();
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param xyz the Cartesian coordinates in cm
	 * @param numDec the number of decimal places for each value.
	 * @return a String representation of the vector in spherical coordinates
	 */
	public static String sphericalStr(String prefix, double xyz[], int numDec) {
		double r = Math
				.sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2] * xyz[2]);
		double theta = Math.toDegrees(Math.acos(xyz[2] / r));
		double phi = Math.toDegrees(Math.atan2(xyz[1], xyz[0]));

		StringBuilder sb = new StringBuilder(128);

		if (prefix != null) {
			sb.append(prefix + " ");
		}

		sb.append(CedView.rThetaPhi + " (");

		sb.append(DoubleFormat.doubleFormat(r, numDec) + "cm, ");
		sb.append(DoubleFormat.doubleFormat(theta, numDec));
		sb.append(UnicodeSupport.DEGREE + ", ");
		sb.append(DoubleFormat.doubleFormat(phi, numDec));
		sb.append(UnicodeSupport.DEGREE + ", ");
		sb.append(")");

		return sb.toString();

	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param v the value
	 * @param numDec the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	public static String scalarStr(String prefix, double v, int numDec,
			String postfix) {

		StringBuilder sb = new StringBuilder(128);

		if (prefix != null) {
			sb.append(prefix + " ");
		}

		sb.append(DoubleFormat.doubleFormat(v, numDec));

		if (postfix != null) {
			sb.append(" " + postfix);
		}

		return sb.toString();
	}



}
