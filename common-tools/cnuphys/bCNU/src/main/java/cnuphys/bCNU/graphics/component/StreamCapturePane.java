package cnuphys.bCNU.graphics.component;

import java.awt.Color;
import java.io.PrintStream;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;


public class StreamCapturePane extends JScrollPane {

	private static final Color transparent = new Color(0, 0, 0, 0);

	// red terminal
	public static SimpleAttributeSet RED_TERMINAL = createStyle(Color.red,
			transparent, "monospaced", 11, false, false);

	// black terminal
	public static SimpleAttributeSet BLACK_TERMINAL = createStyle(Color.black,
			transparent, "monospaced", 11, false, false);
	
	private Vector<CachedText> _cachedText = new Vector<CachedText>();

	/**
	 * The text area that will be on this scroll pane.
	 */
	protected JTextPane textPane;

	protected StyledDocument document;

	protected SimpleAttributeSet defaultStyle = BLACK_TERMINAL;

	//captured printstreams for err and out
	private CapturedPrintStream _outCps;
	private CapturedPrintStream _errCps;
	
	private PrintStream stdErr;
	private PrintStream stdOut;
	
	/**
	 * Constructor will also create the text pane itself.
	 * 
	 */
	public StreamCapturePane() {
		super();
		
		stdErr = System.err;
		stdOut = System.out;
				
		
		createTextPane();
		textPane.setBackground(Color.white);
		getViewport().add(textPane);
		
		//capture stdout and stdin
		_outCps = new CapturedPrintStream() {

			@Override
			public void println(String s) {
				baseAppend(s + "\n", BLACK_TERMINAL);
				_cachedText.add(new CachedText(s + "\n", CachedText.STDOUT));
			}

			@Override
			public void print(String s) {
				baseAppend(s, BLACK_TERMINAL);
				_cachedText.add(new CachedText(s, CachedText.STDOUT));
			}
		};
		
		//capture stdout and stdin
		_errCps = new CapturedPrintStream() {

			@Override
			public void println(String s) {
				baseAppend(s + "\n", RED_TERMINAL);
				_cachedText.add(new CachedText(s + "\n", CachedText.STDERR));
			}

			@Override
			public void print(String s) {
				baseAppend(s, RED_TERMINAL);
				_cachedText.add(new CachedText(s, CachedText.STDERR));
			}
		};
		
		System.setOut(_outCps);
		System.setErr(_errCps);
	}
	
	public void writeCachedText() {
		try {
			for (CachedText ct : _cachedText) {
				ct.write();
			}
		} catch (Exception e) {

		}
	}
	
	public void unCapture() {
		System.setOut(stdOut);
		System.setErr(stdErr);
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
	public void append(final String text, final AttributeSet style) {
		baseAppend(text, style);
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
	private void baseAppend(final String text, final AttributeSet style) {
		if (text == null) {
			return;
		}

		if (document == null) {
			return;
		}

		try {
			document.insertString(document.getLength(), text, style);
		} catch (BadLocationException e) {
		}
		textPane.setCaretPosition(Math.max(0, document.getLength() - 1));
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