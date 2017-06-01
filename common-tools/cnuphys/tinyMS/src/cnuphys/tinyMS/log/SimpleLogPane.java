package cnuphys.tinyMS.log;

import java.awt.Color;
import java.awt.Dimension;
import java.util.EnumMap;

import javax.swing.text.SimpleAttributeSet;

import cnuphys.tinyMS.graphics.TextPaneScrollPane;
import cnuphys.tinyMS.graphics.X11Colors;

/**
 * Combines all log messages into one text pane. Uses different colors to
 * highlight different types of log messages.
 * 
 * @author heddle
 */

@SuppressWarnings("serial")
public class SimpleLogPane extends TextPaneScrollPane {

	private static int CONFIGFONTSIZE = 11;
	private static int WARNINGFONTSIZE = 11;
	private static int INFOFONTSIZE = 11;
	private static int ERRORFONTSIZE = 11;


	private static EnumMap<Log.Level, SimpleAttributeSet> styles;

	static {
		styles = new EnumMap<Log.Level, SimpleAttributeSet>(Log.Level.class);
		styles.put(Log.Level.INFO, createStyle(Color.black, "sansserif", INFOFONTSIZE, false, false));
		styles.put(Log.Level.CONFIG, createStyle(Color.blue, "sansserif", CONFIGFONTSIZE, false, false));
		styles.put(Log.Level.WARNING,
				createStyle(X11Colors.getX11Color("orange red"), "sansserif", WARNINGFONTSIZE, false, true));
		styles.put(Log.Level.ERROR, createStyle(Color.red, "sansserif", ERRORFONTSIZE, false, true));
		styles.put(Log.Level.EXCEPTION, createStyle(Color.red, "sansserif", ERRORFONTSIZE, false, true));
	}

	public SimpleLogPane() {
		setPreferredSize(new Dimension(800, 400));

		ILogListener ll = new ILogListener() {

			@Override
			public void config(String message) {
				append(Log.Level.CONFIG, message);
			}

			@Override
			public void info(String message) {
				append(Log.Level.INFO, message);
			}

			@Override
			public void error(String message) {
				append(Log.Level.ERROR, message);
			}

			@Override
			public void exception(String message) {
				append(Log.Level.ERROR, message);
			}

			@Override
			public void warning(String message) {
				append(Log.Level.WARNING, message);
			}
		};

		Log.getInstance().addLogListener(ll);

	}

	/**
	 * Fix the message so it gets appended nicely.
	 * 
	 * @param message
	 *            the input message.
	 * @return the fixed message.
	 */
	private String fixMessage(String message) {
		if (message == null) {
			return "";
		}

		if (!(message.endsWith("\n"))) {
			return message + "\n";
		}
		return message;
	}

	/**
	 * Append the message with the appropriate style.
	 * 
	 * @param grade
	 *            the grade of the messaged.
	 * @param message
	 *            the message text.
	 */
	private void append(Log.Level grade, String message) {
		append(fixMessage(message), styles.get(grade), true);
	}

}
