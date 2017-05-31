package cnuphys.bCNU.log;

import java.awt.Color;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cnuphys.bCNU.graphics.component.TextPaneScrollPane;

@SuppressWarnings("serial")
public class LogTabbedPane extends JTabbedPane implements ChangeListener {

	private static final int ERROR_INDEX = 0;
	private static final int WARNING_INDEX = 1;
	private static final int INFO_INDEX = 2;
	private static final int CONFIG_INDEX = 3;
	private static final int EXCEPTION_INDEX = 4;

	private TextPaneScrollPane error = new TextPaneScrollPane();
	private TextPaneScrollPane warning = new TextPaneScrollPane();
	private TextPaneScrollPane info = new TextPaneScrollPane();
	private TextPaneScrollPane config = new TextPaneScrollPane();
	private TextPaneScrollPane exception = new TextPaneScrollPane();

	public LogTabbedPane() {

		add("error", error);
		add("warning", warning);
		add("info", info);
		add("config", config);
		add("exception", exception);

		ILogListener ll = new ILogListener() {

			@Override
			public void config(String message) {
				if (getSelectedIndex() != CONFIG_INDEX) {
					setForegroundAt(CONFIG_INDEX, Color.red);
				}
				config.append(fixMessage(message));
			}

			@Override
			public void exception(String message) {
				if (getSelectedIndex() != EXCEPTION_INDEX) {
					setForegroundAt(EXCEPTION_INDEX, Color.red);
				}
				exception.append(fixMessage(message));
			}

			@Override
			public void info(String message) {
				if (getSelectedIndex() != INFO_INDEX) {
					setForegroundAt(INFO_INDEX, Color.red);
				}
				info.append(fixMessage(message));
			}

			@Override
			public void error(String message) {
				if (getSelectedIndex() != ERROR_INDEX) {
					setForegroundAt(ERROR_INDEX, Color.red);
				}
				error.append(fixMessage(message));
			}

			@Override
			public void warning(String message) {
				if (getSelectedIndex() != WARNING_INDEX) {
					setForegroundAt(WARNING_INDEX, Color.red);
				}
				warning.append(fixMessage(message));
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
	 * The method from the ChangeListener interface.
	 * 
	 * @param ce
	 *            the causal ChangeEvent.
	 */
	@Override
	public void stateChanged(ChangeEvent ce) {
		setForegroundAt(getSelectedIndex(), Color.black);
	}

}
