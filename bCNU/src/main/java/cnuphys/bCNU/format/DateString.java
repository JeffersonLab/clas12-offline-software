package cnuphys.bCNU.format;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateString {

    /**
     * A default time zone
     */
    private static TimeZone defTZ = null;

    /**
     * A formatter to get the time in down to minutes.
     */
    private static SimpleDateFormat formattermm = null;

    /**
     * A formatter to get the time in down to seconds.
     */
    private static SimpleDateFormat formatterss = null;

    /**
     * A formatter to get the time in down to seconds (no day info).
     */
    private static SimpleDateFormat formattershort = null;

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

	if (defTZ == null)
	    defTZ = TimeZone.getDefault();

	if (formattermm == null) {
	    formattermm = new SimpleDateFormat("EEE MMM d  h:mm a");
	    formattermm.setTimeZone(defTZ);
	}
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

	if (defTZ == null)
	    defTZ = TimeZone.getDefault();

	if (formatterss == null) {
	    formatterss = new SimpleDateFormat("EEE MMM d  h:mm:ss a");
	    formatterss.setTimeZone(defTZ);
	}
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

	if (defTZ == null)
	    defTZ = TimeZone.getDefault();

	if (formattershort == null) {
	    formattershort = new SimpleDateFormat("h:mm:ss");
	    formattershort.setTimeZone(defTZ);
	}
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
