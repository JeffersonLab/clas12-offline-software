package cnuphys.bCNU.util;

/**
 * This is used to indicate an error when parsing an ascii file.
 * 
 * @author heddle
 *
 */
@SuppressWarnings("serial")
public class BadRecordException extends Exception {

	/**
	 * Constructor for a BadRecordException, used to indicate an error when
	 * parsing an ascii file.
	 * 
	 * @param message
	 *            the message that will be displayed in a printStackTrace.
	 */
	public BadRecordException(String message) {
		super(message);
	}
}
