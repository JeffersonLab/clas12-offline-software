package cnuphys.tinyMS.Environment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateString {

	/**
	 * A formatter to get the time in down to minutes.
	 */
	private static SimpleDateFormat formattermm;

	/**
	 * A formatter to get the time in down to seconds.
	 */
	private static SimpleDateFormat formatterss;

	/**
	 * A formatter to get the time in down to seconds (no day info).
	 */
	private static SimpleDateFormat formattershort;

	static {
		TimeZone tz = TimeZone.getDefault();
		formattermm = new SimpleDateFormat("EEE MMM d  h:mm a");
		formattermm.setTimeZone(tz);

		formatterss = new SimpleDateFormat("EEE MMM d  h:mm:ss a");
		formatterss.setTimeZone(tz);

		formattershort = new SimpleDateFormat("h:mm:ss");
		formattershort.setTimeZone(tz);
	}

	/**
	 * Returns the current time.
	 * 
	 * @return a string representation of the current time, down to seconds.
	 */
	public static String dateString() {
		return dateStringSS();
	}

	/**
	 * Returns the current time.
	 * 
	 * @return a string representation of the current time, down to minutes.
	 */
	public static String dateStringMM() {
		return dateStringMM(System.currentTimeMillis());
	}

	/**
	 * Returns the current time.
	 * 
	 * @param longtime
	 *            the time in millis.
	 * @return a string representation of the current time, down to minutes.
	 */
	public static String dateStringMM(long longtime) {
		return formattermm.format(new Date(longtime));
	}

	/**
	 * Returns the current time.
	 * 
	 * @param longtime
	 *            The time in millis.
	 * @return a string representation of the current time, down to seconds.
	 */
	public static String dateStringSS(long longtime) {
		return formatterss.format(new Date(longtime));
	}

	/**
	 * Returns the current time.
	 * 
	 * @return a string representation of the current time, down to seconds.
	 */
	public static String dateStringSS() {
		return dateStringShort(System.currentTimeMillis());
	}

	/**
	 * Returns the current time.
	 * 
	 * @param ltime
	 *            a time in milliseconds.
	 * @return a string representation of the current time, down to seconds but
	 *         without day information.
	 */
	public static String dateStringShort(long ltime) {
		return formattershort.format(new Date(ltime));
	}

	/**
	 * Returns the current time.
	 * 
	 * @return a string representation of the current time, down to seconds but
	 *         without day information.
	 */
	public static String dateStringShort() {
		return dateStringShort(System.currentTimeMillis());
	}

}
