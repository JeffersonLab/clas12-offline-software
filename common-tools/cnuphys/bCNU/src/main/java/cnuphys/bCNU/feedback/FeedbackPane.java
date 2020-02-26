package cnuphys.bCNU.feedback;

import java.awt.Color;
import java.util.Hashtable;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.text.SimpleAttributeSet;

import cnuphys.bCNU.graphics.component.TextPaneScrollPane;
import cnuphys.bCNU.util.X11Colors;

/**
 * A FeedbackPane for displaying mouse over feedback
 * 
 * @author heddle
 */

@SuppressWarnings("serial")
public class FeedbackPane extends TextPaneScrollPane {

	// stores styles based on a string. When a feedback string is
	// appended, if it begins with one of the keys the style will be
	// used. If not, the default style is used.
	private Hashtable<String, SimpleAttributeSet> styles = new Hashtable<String, SimpleAttributeSet>(
			101);

	/**
	 * Default fontsize for feedback text.
	 */
	private static int _fontSize = 10;

	/**
	 * Background for feedback panel
	 */
	private static Color _background = X11Colors.getX11Color("Black");

	/**
	 * Cyan style. The last two booleans represent italics and bold.
	 */
	public static final SimpleAttributeSet _defaultStyle = createStyle(
			Color.cyan, _background, "SansSerif", _fontSize, false, true);

	/**
	 * Constructor Create a feedback pane to display mouse-over feedback. This
	 * is a "low tech" alternative to using the HUD.
	 * 
	 * @param width
	 *            the preferred width.
	 */
	public FeedbackPane(int width) {
		this(width, 500);
	}

	/**
	 * Constructor Create a feedback pane to display mouse-over feedback. This
	 * is a "low tech" alternative to using the HUD.
	 * 
	 * @param width
	 *            the preferred width.
	 */
	public FeedbackPane(int width, int height) {
//		setPreferredSize(new Dimension(width, height));
		
		Border etchedBorder = BorderFactory.createEtchedBorder();
		Border lineBorder = BorderFactory.createLineBorder(Color.black, 2);
		setBorder(BorderFactory.createCompoundBorder(etchedBorder, lineBorder));

		setBackground(_background);
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
	 * Append the message with the default style. To use the default style, just
	 * send the message. To use a different x11 color, prepend the message with
	 * the x11 name embedded between $ characters. For example<br>
	 * append("yomama") will append "yomama" in the default style.
	 * append("$red$yomama") will append "yomama" in red.
	 * append("$dark blue$yomama") will append "yomama" in dark blue.
	 * 
	 * @param message
	 *            the message text.
	 */
	@Override
	public void append(String message) {
		SimpleAttributeSet style = null;

		if (message.startsWith("$")) {
			int nextIndex = message.indexOf("$", 1);
			if ((nextIndex > 3) && (nextIndex < 30)) {
				String x11color = message.substring(1, nextIndex).toLowerCase();

				message = message.substring(nextIndex + 1);
				style = styles.get(x11color);
				if (style == null) {
					Color color = X11Colors.getX11Color(x11color);
					if (color != null) {
						style = createStyle(color, _background, "SansSerif",
								_fontSize, false, true);
						styles.put(x11color, style);
					}
				}
			}
		}
		append((style == null) ? _defaultStyle : style, message);
	}

	/**
	 * Append the message with the provided style.
	 * 
	 * @param style
	 *            the style to use
	 * @param message
	 *            the message text.
	 */
	public void append(SimpleAttributeSet style, String message) {
		append(fixMessage(message), style, false);
	}

	/**
	 * This updates the feedback pane.
	 * 
	 * @param feedbackStrings
	 *            contains the new feedback strings.
	 */
	public void updateFeedback(List<String> feedbackStrings) {
		clear();
		try {
			for (String s : feedbackStrings) {
				append(s);
			}
		} catch (Exception e) {

		}
	}

}
