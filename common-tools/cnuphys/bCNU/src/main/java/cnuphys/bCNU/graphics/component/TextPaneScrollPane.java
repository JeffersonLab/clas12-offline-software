package cnuphys.bCNU.graphics.component;

import java.awt.Color;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import cnuphys.bCNU.format.DateString;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.X11Colors;

@SuppressWarnings("serial")
public class TextPaneScrollPane extends JScrollPane {

	private static final Color transparent = new Color(0, 0, 0, 0);

	// blue monospaced
	public static SimpleAttributeSet BLUE_M_10_B = createStyle(Color.blue,
			X11Colors.getX11Color("Alice Blue"), "monospaced", 10, false, true);

	// red terminal
	public static SimpleAttributeSet RED_TERMINAL = createStyle(Color.red,
			transparent, "monospaced", 11, false, true);

	// red terminal
	public static SimpleAttributeSet YELLOW_TERMINAL = createStyle(
			Color.yellow, transparent, "monospaced", 11, false, true);

	// blue terminal
	public static SimpleAttributeSet CYAN_TERMINAL = createStyle(Color.cyan,
			transparent, "monospaced", 11, false, true);

	// green terminal
	public static SimpleAttributeSet GREEN_TERMINAL = createStyle(
			X11Colors.getX11Color("Light Green"), transparent, "monospaced",
			11, false, true);

	// blue sans serif
	public static SimpleAttributeSet BLUE_SS_12_B = createStyle(Color.blue,
			X11Colors.getX11Color("Alice Blue"), "sansserif", 12, false, true);

	// green sans serif
	public static SimpleAttributeSet GREEN_SS_12_B = createStyle(
			X11Colors.getX11Color("Dark Green"),
			X11Colors.getX11Color("wheat"), "sansserif", 12, false, true);

	// red sans serif
	public static SimpleAttributeSet RED_SS_12_P = createStyle(Color.red,
			"sansserif", 12, false, false);

	public static SimpleAttributeSet GREEN_SS_12_P = createStyle(
			X11Colors.getX11Color("Dark Green"),
			X11Colors.getX11Color("wheat"), "sansserif", 12, false, false);

	public static SimpleAttributeSet BLACK_SS_12_P = createStyle(Color.black,
			"sansserif", 12, false, false);
	public static SimpleAttributeSet BLUE_SS_10_B = createStyle(Color.blue,
			X11Colors.getX11Color("Alice Blue"), "sansserif", 10, false, true);

	public static SimpleAttributeSet RED_SS_10_P = createStyle(Color.red,
			"sansserif", 10, false, false);

	public static SimpleAttributeSet GREEN_SS_10_P = createStyle(
			X11Colors.getX11Color("Dark Green"), Color.yellow, "sansserif", 10,
			false, false);

	public static SimpleAttributeSet BLACK_SS_10_P = createStyle(Color.black,
			"sansserif", 10, false, false);

	/**
	 * The text area that will be on this scroll pane.
	 */
	protected JTextPane textPane;

	protected StyledDocument document;

	protected SimpleAttributeSet defaultStyle = BLACK_SS_12_P;

	/**
	 * Constructor will also create the text pane itself.
	 */
	public TextPaneScrollPane() {
		this(null);
	}

	/**
	 * Constructor will also create the text pane itself.
	 * 
	 * @param label
	 *            if not null, will use for a border.
	 */
	public TextPaneScrollPane(String label) {
		super();
		if (label != null) {
			this.setBorder(new CommonBorder(label));
		}
		createTextPane();
		textPane.setBackground(Color.white);
		getViewport().add(textPane);
	}

	/**
	 * Set the background, by setting the underlying text pane's background.
	 * 
	 * @param c
	 *            the color to use.
	 */
	@Override
	public void setBackground(Color c) {
		// super.setBackground(c);
		if (textPane != null) {
			textPane.setBackground(c);
		}
	}

	/**
	 * Create a style, not underlined, no with default spacing.
	 * 
	 * @param fg
	 *            the foreground color.
	 * @param fontFamily
	 *            the font family to use,
	 * @param fontSize
	 *            the font size to use,
	 * @param italic
	 *            if <code>true</code>, use italic.
	 * @param bold
	 *            if <code>true</code>, make bold.
	 * @return the style.
	 */
	public static SimpleAttributeSet createStyle(Color fg, String fontFamily,
			int fontSize, boolean italic, boolean bold) {
		return createStyle(fg, Color.white, fontFamily, fontSize, italic, bold,
				false, 0, 2);
	}

	/**
	 * Create a style, not underlined with default spacing.
	 * 
	 * @param fg
	 *            the foreground color.
	 * @param bg
	 *            the background color.
	 * @param fontFamily
	 *            the font family to use,
	 * @param fontSize
	 *            the font size to use,
	 * @param italic
	 *            if <code>true</code>, use italic.
	 * @param bold
	 *            if <code>true</code>, make bold.
	 * @return the style.
	 */
	public static SimpleAttributeSet createStyle(Color fg, Color bg,
			String fontFamily, int fontSize, boolean italic, boolean bold) {
		return createStyle(fg, bg, fontFamily, fontSize, italic, bold, false,
				0, 2);
	}

	/**
	 * Create a style
	 * 
	 * @param fg
	 *            the foreground color.
	 * @param bg
	 *            the background color.
	 * @param fontFamily
	 *            the font family to use,
	 * @param fontSize
	 *            the font size to use,
	 * @param italic
	 *            if <code>true</code>, use italic.
	 * @param bold
	 *            if <code>true</code>, make bold.
	 * @param underline
	 *            if <code>true</code>, underline.
	 * @param spaceAbove
	 *            space above.
	 * @param spaceBelow
	 *            space below.
	 * @return the style.
	 */
	public static SimpleAttributeSet createStyle(Color fg, Color bg,
			String fontFamily, int fontSize, boolean italic, boolean bold,
			boolean underline, int spaceAbove, int spaceBelow) {
		SimpleAttributeSet style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, fg);
		StyleConstants.setBackground(style, bg);
		StyleConstants.setFontFamily(style, fontFamily);
		StyleConstants.setFontSize(style, fontSize);
		StyleConstants.setItalic(style, italic);
		StyleConstants.setBold(style, bold);
		StyleConstants.setUnderline(style, underline);
		StyleConstants.setSpaceAbove(style, spaceAbove);
		StyleConstants.setSpaceBelow(style, spaceBelow);
		return style;
	}

	/**
	 * Create a text pane that will support styled text.
	 */
	private void createTextPane() {
		StyleContext context = new StyleContext();
		document = new DefaultStyledDocument(context);
		textPane = new JTextPane(document);
		textPane.setEditable(false);
	}

	/**
	 * Refresh the text pane.
	 */
	public void refresh() {
		if (textPane != null) {
			textPane.repaint();
		}
	}

	/**
	 * Append a message to the underlying text area.
	 * 
	 * @param text
	 *            the message to append.
	 */

	public void append(String text) {
		append(text, defaultStyle);
	}

	/**
	 * Append some text with a specific style.
	 * 
	 * @param text
	 *            the text to append.
	 * @param style
	 *            the style to use, can be one of the class constants such as
	 *            BOLD_RED.
	 */
	public void append(String text, AttributeSet style) {
		append(text, style, false);
	}

	/**
	 * Append some text with a specific style.
	 * 
	 * @param text
	 *            the text to append.
	 * @param style
	 *            the style to use, can be one of the class constants such as
	 *            BOLD_RED.
	 * @param writeTime
	 *            if <code>true</code> writes out a time stamp.
	 */
	public void append(final String text, final AttributeSet style,
			final boolean writeTime) {

		baseAppend(text, style, writeTime);
		// if (SwingUtilities.isEventDispatchThread()) {
		// baseAppend(text, style, writeTime);
		// }
		// else {
		// Runnable doRun = new Runnable() {
		// @Override
		// public void run() {
		// baseAppend(text, style, writeTime);
		// }
		// };
		//
		// SwingUtilities.invokeLater(doRun);
		// }
	}

	// SwingUtilities.isEventDispatchThread()

	/**
	 * Append some text with a specific style.
	 * 
	 * @param text
	 *            the text to append.
	 * @param style
	 *            the style to use, can be one of the class constants such as
	 *            BOLD_RED.
	 * @param writeTime
	 *            if <code>true</code> writes out a time stamp.
	 */
	private void baseAppend(final String text, final AttributeSet style,
			final boolean writeTime) {
		if (text == null) {
			return;
		}

		if (document == null) {
			return;
		}

		try {
			if (writeTime) {
				writeTime();
			}
			document.insertString(document.getLength(), text, style);
		} catch (BadLocationException e) {
			Log.getInstance().exception(e);
		}

		try {
			textPane.setCaretPosition(Math.max(0, document.getLength() - 1));
		} catch (Exception e) {
			// System.err.println("TextPaneScrollPane exception " +
			// e.getMessage());
		}
	}

	/**
	 * Write the date string in a small blue font.
	 */
	private void writeTime() {
		String s = DateString.dateStringSS();
		try {
			document.insertString(document.getLength(), s, BLUE_M_10_B);
			document.insertString(document.getLength(), "  ", BLACK_SS_12_P);
		} catch (BadLocationException e) {
			Log.getInstance().exception(e);
			e.printStackTrace();
		}
	}

	/**
	 * Clear all the text.
	 * 
	 */
	public void clear() {
		if (textPane == null) {
			return;
		}

		textPane.setText(null);
		textPane.setCaretPosition(0);
	}

	/**
	 * Get the current default style.
	 * 
	 * @return the current default style.
	 */
	public SimpleAttributeSet getDefaultStyle() {
		return defaultStyle;
	}

	/**
	 * Set the default style.
	 * 
	 * @param defaultStyle
	 *            the new default style
	 */
	public void setDefaultStyle(SimpleAttributeSet defaultStyle) {
		this.defaultStyle = defaultStyle;
	}
}
