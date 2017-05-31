package cnuphys.bCNU.log;

import java.awt.Color;
import java.awt.Dimension;
import java.util.EnumMap;

import javax.swing.text.SimpleAttributeSet;

import cnuphys.bCNU.graphics.component.TextPaneScrollPane;
import cnuphys.splot.plot.X11Colors;

/**
 * Combines all log messages into one text pane. Uses different colors to
 * highlight different types of log messages.
 * 
 * @author heddle
 */

@SuppressWarnings("serial")
public class SimpleLogPane extends TextPaneScrollPane {

	private static int CONFIGFONTSIZE = 12;
	private static int WARNINGFONTSIZE = 11;
	private static int INFOFONTSIZE = 12;
	private static int ERRORFONTSIZE = 11;

	// reduce from seven levels
	private static enum Grade {
		CONFIG, INFO, WARNING, ERROR
	}

	private static EnumMap<Grade, SimpleAttributeSet> styles;
	
	static {
		styles = new EnumMap<Grade, SimpleAttributeSet>(Grade.class);
		styles.put(
				Grade.INFO,
				createStyle(Color.black, "sansserif", INFOFONTSIZE, false,
						false));
		styles.put(
				Grade.CONFIG,
				createStyle(Color.blue, "sansserif", CONFIGFONTSIZE, false,
						false));
		styles.put(
				Grade.WARNING,
				createStyle(X11Colors.getX11Color("orange red"), "monospaced", WARNINGFONTSIZE,
						false, true));
		styles.put(
				Grade.ERROR,
				createStyle(Color.red, "monospaced", ERRORFONTSIZE, false,
						true));
	}

	public SimpleLogPane() {
		setPreferredSize(new Dimension(800, 400));

		ILogListener ll = new ILogListener() {

			@Override
			public void config(String message) {
				append(Grade.CONFIG, message);
			}

			@Override
			public void info(String message) {
				append(Grade.INFO, message);
			}

			@Override
			public void error(String message) {
				append(Grade.ERROR, message);
			}

			@Override
			public void exception(String message) {
				append(Grade.ERROR, message);
			}

			@Override
			public void warning(String message) {
				append(Grade.WARNING, message);
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
	private void append(Grade grade, String message) {
		append(fixMessage(message), styles.get(grade), (grade == Grade.ERROR));
	}

}
