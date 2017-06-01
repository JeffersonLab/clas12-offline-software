package cnuphys.tinyMS.log;

import java.util.EventListener;

public interface ILogListener extends EventListener {

	/**
	 * Send a error string to the listeners.
	 * 
	 * @param message
	 *            the error message string.
	 */
	public void error(String message);

	/**
	 * Send a config string to the listeners.
	 * 
	 * @param message
	 *            the config message string.
	 */

	public void config(String message);

	/**
	 * Send a warning to the logger.
	 * 
	 * @param message
	 *            the warning message.
	 */
	public void warning(String message);

	/**
	 * Send an info string to the logger.
	 * 
	 * @param message
	 *            the informational string.
	 */
	public void info(String message);

	/**
	 * Send an exception to the "severe" handler.
	 * 
	 * @param t
	 *            a Throwable--usually an Exception.
	 */
	public void exception(String t);

}
