package cnuphys.bCNU.log;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.event.EventListenerList;

public class Log {

	public enum Level {
		ERROR, CONFIG, WARNING, INFO, EXCEPTION
	}

	// singleton
	private static Log instance = null;

	// List of feedback providers for the parent container
	private EventListenerList _listenerList;

	/**
	 * Private constructor since we use the singleton pattern.
	 */
	private Log() {
	}

	/**
	 * Access to the Log singleton.
	 * 
	 * @return the Log singleton.
	 */
	public static Log getInstance() {
		if (instance == null) {
			try {
				instance = new Log();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return instance;
	}

	// notify listeners of message
	private void notifyListeners(String message, Level level) {

		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// This weird loop is the bullet proof way of notifying all listeners.
		// for (int i = listeners.length - 2; i >= 0; i -= 2) {
		// order is flipped so it goes in order as added
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == ILogListener.class) {

				ILogListener listener = (ILogListener) listeners[i + 1];

				switch (level) {
				case ERROR:
					listener.error(message);
					break;

				case CONFIG:
					listener.config(message);
					break;

				case WARNING:
					listener.warning(message);
					break;

				case INFO:
					listener.info(message);
					break;

				case EXCEPTION:
					listener.exception(message);
					break;

				}

			}
		}

	}

	public void addLogListener(ILogListener logListener) {

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		// avoid adding duplicates
		_listenerList.remove(ILogListener.class, logListener);
		_listenerList.add(ILogListener.class, logListener);
	}

	/**
	 * Remove a LogListener.
	 * 
	 * @param logListener
	 *            the LogListener to remove.
	 */

	public void removeLogListener(ILogListener logListener) {

		if ((logListener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(ILogListener.class, logListener);
	}

	/**
	 * Send a error string to the listeners.
	 * 
	 * @param message
	 *            the error message string.
	 */
	public void error(String message) {
		notifyListeners(message, Level.ERROR);
	}

	/**
	 * Send a config string to the listeners.
	 * 
	 * @param message
	 *            the config message string.
	 */

	public void config(String message) {
		notifyListeners(message, Level.CONFIG);
	}

	/**
	 * Send a warning to the logger.
	 * 
	 * @param message
	 *            the warning message.
	 */
	public void warning(String message) {
		notifyListeners(message, Level.WARNING);
	}

	/**
	 * Send an info string to the logger.
	 * 
	 * @param message
	 *            the informational string.
	 */
	public void info(String message) {
		notifyListeners(message, Level.INFO);
	}

	/**
	 * Send an exception to the "severe" handler.
	 * 
	 * @param t
	 *            a Throwable--usually an Exception.
	 */
	public void exception(Throwable t) {
		notifyListeners(throwableToString(t), Level.EXCEPTION);
	}

	/**
	 * Place the stack trace from a throwable into a string.
	 * 
	 * @param t
	 *            the Throwable (Exception or Error)
	 * @return the string with the stack trace info.
	 */
	private String throwableToString(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	/**
	 * Singleton objects cannot be cloned, so we override clone to throw a
	 * CloneNotSupportedException.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

}
