package cnuphys.bCNU.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JLabel;

import cnuphys.bCNU.graphics.GraphicsUtilities;

/**
 * This is a snippet of text. Used for latex-like strings. A snippet has an
 * offset relative to the origin of the main string and a font used for the
 * entire snippet.
 * 
 * @author heddle
 * 
 */
public class Snippet {

	// the control characters
	private static final char newline = 'n';
	private static final char base = 'd';
	private static final char bold = 'b';
	private static final char italic = 'i';
	private static final char unbold = 'B';
	private static final char unitalic = 'I';
	private static final char plain = 'p';
	private static final char sub = '_';
	private static final char sup = '^';
	private static final char bigger = '+';
	private static final char smaller = '-';
	private static final char smallspace = 's';
	private static final char bigspace = 'S';

	// Relative horizontal offset
	private int _deltaX;

	// Relative vertical offset
	private int _deltaY;

	// the font to use
	private Font _font;

	// the text for this snippet
	private String _text;

	public Snippet(int deltaX, int deltaY, Font font, String text) {
		super();
		_deltaX = deltaX;
		_deltaY = deltaY;
		_font = font;
		_text = text;
	}

	/**
	 * Draw rotated text.
	 * 
	 * @param g
	 *            the Graphics context.
	 * @param xo
	 *            the x pixel coordinate of baseline of the main string of which
	 *            this is a snippet.
	 * @param yo
	 *            the y pixel coordinate of baseline of the main string of which
	 *            this is a snippet.
	 * @param angleDegrees
	 *            the angle of rotation in decimal degrees.
	 */
	public void drawSnippet(Graphics g, int xo, int yo, double angleDegrees) {
		// GraphicsUtilities.drawRotatedText((Graphics2D)g, _text, _font,
		// xo + _deltaX, yo + _deltaY, angleDegrees);
		GraphicsUtilities.drawRotatedText((Graphics2D) g, _text, _font, xo, yo,
				_deltaX, _deltaY, angleDegrees);
	}

	/**
	 * @return the deltaX
	 */
	public int getDeltaX() {
		return _deltaX;
	}

	/**
	 * @return the deltaY
	 */
	public int getDeltaY() {
		return _deltaY;
	}

	/**
	 * @return the font
	 */
	public Font getFont() {
		return _font;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return _text;
	}

	/**
	 * Create a string representation
	 * 
	 * @return a string representation
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(256);
		sb.append("text: " + _text + "\n");
		sb.append("delX: " + _deltaX + "\n");
		sb.append("delY: " + _deltaY + "\n");
		sb.append("font: " + _font);
		return sb.toString();
	}

	/**
	 * Get the size of a Snippet
	 * 
	 * @param c
	 *            the compoment whose FontMetrics we use
	 * @return the dinesion of the snippet
	 */
	public Dimension size(Component c) {
		FontMetrics fm = c.getFontMetrics(_font);
		int w = fm.stringWidth(_text);
		return new Dimension(w, fm.getHeight());
	}

	/**
	 * Get an array of snippets from a compound string
	 * 
	 * @param baseFont
	 *            the baseline font. Each snippet will have this font or an
	 *            excursion from this font.
	 * @param cstr
	 *            the compound string
	 * @return the array of snippets
	 */
	public static Vector<Snippet> getSnippets(Font baseFont, String cstr,
			Component component) {

		// first replace all special characters
		cstr = UnicodeSupport.specialCharReplace(cstr);

		if ((baseFont == null) || (cstr == null) || (component == null)) {
			return null;
		}

		// note tokenizer used will return backslash
		String tokens[] = tokens(cstr, "\\");
		int len = tokens.length;

		boolean slashSet = false;
		Font currentFont = cloneFont(baseFont);
		Vector<Snippet> snippets = new Vector<Snippet>(tokens.length);

		int delX = 0;
		int delY = 0;

		int extra = 0; // extra plus or minus spacing

		for (int index = 0; index < len; index++) {
			String s = tokens[index];

			// is it the delimiter?
			if (!slashSet && isDelimitter(s)) {
				slashSet = true;
			} else { // not delimitter (First token only)
				if (slashSet) {
					char firstChar = s.charAt(0);

					if (newline == firstChar) { // new line
						s = s.substring(1);
						delX = 0;
						delY += component.getFontMetrics(currentFont)
								.getHeight();
					}
					if (base == firstChar) { // rest to base font
						s = s.substring(1);
						currentFont = cloneFont(baseFont);
						extra = 0;
					} else if (bold == firstChar) {
						s = s.substring(1);
						currentFont = bold(currentFont);
					} else if (unbold == firstChar) {
						s = s.substring(1);
						currentFont = unbold(currentFont);
					} else if (italic == firstChar) {
						s = s.substring(1);
						currentFont = italic(currentFont);
					} else if (unitalic == firstChar) {
						s = s.substring(1);
						currentFont = unitalic(currentFont);
					} else if (plain == firstChar) {
						s = s.substring(1);
						currentFont = plain(currentFont);
					} else if (smallspace == firstChar) {
						s = s.substring(1);
						delX += 2;
					} else if (bigspace == firstChar) {
						s = s.substring(1);
						delX += 8;
					} else if (sub == firstChar) {
						int vs = currentFont.getSize() / 4;
						int del = currentFont.getSize() / 5;
						extra += vs;
						s = s.substring(1);
						currentFont = smaller(currentFont, del);
					} else if (sup == firstChar) {
						int vs = currentFont.getSize() / 4;
						int del = currentFont.getSize() / 5;
						extra -= vs;
						s = s.substring(1);
						currentFont = smaller(currentFont, del);
					} else if (bigger == firstChar) {
						int del = currentFont.getSize() / 4;
						s = s.substring(1);
						currentFont = bigger(currentFont, del);
					} else if (smaller == firstChar) {
						int del = currentFont.getSize() / 4;
						s = s.substring(1);
						currentFont = smaller(currentFont, del);
					}
				}

				Snippet snippet = new Snippet(delX, delY + extra, currentFont,
						s);
				snippets.add(snippet);
				Dimension d = snippet.size(component);
				delX += d.width;

				slashSet = false;

				// private static final char newline = 'n';
				// private static final char backslash = '\\';
				// private static final char base = 'd';
				// private static final char greek = 'g';
				// private static final char bold = 'b';
				// private static final char plain = 'p';
				// private static final char sub = '_';
				// private static final char sup = '^';
				// private static final char roman = 'r';
				// private static final char bigger = '+';
				// private static final char smaller = '-';
				// private static final char nullChar = '\0';

				// change font or delY based on new control char

			} // not demitter
		}

		return snippets;
	}

	// clone a font
	private static Font cloneFont(Font font) {
		return new Font(font.getName(), font.getStyle(), font.getSize());
	}

	private static Font smaller(Font font, int del) {
		return new Font(font.getName(), font.getStyle(), Math.max(4,
				font.getSize() - del));
	}

	private static Font bigger(Font font, int del) {
		return new Font(font.getName(), font.getStyle(), font.getSize() + del);
	}

	private static Font plain(Font font) {
		return Fonts.commonFont(Font.PLAIN, font.getSize());
	}

	private static Font bold(Font font) {
		return Fonts.commonFont(font.getStyle() | Font.BOLD, font.getSize());
	}

	private static Font italic(Font font) {
		return Fonts.commonFont(font.getStyle() | Font.ITALIC, font.getSize());
	}

	private static Font unbold(Font font) {
		return Fonts.commonFont(font.getStyle() & ~Font.BOLD, font.getSize());
	}

	private static Font unitalic(Font font) {
		return Fonts.commonFont(font.getStyle() & ~Font.ITALIC, font.getSize());
	}

	// is this the delimiter (Backslash?)
	private static boolean isDelimitter(String s) {
		if (s == null) {
			return false;
		}
		return (('\\' == s.charAt(0)) && (s.length() == 1));
	}

	/**
	 * This method breaks a string into an array of tokens.
	 * 
	 * @param str
	 *            the string to decompose.
	 * @param delimiter
	 *            the delimiter
	 * @return an array of tokens
	 */

	private static String[] tokens(String str, String delimiter) {

		StringTokenizer t = new StringTokenizer(str, delimiter, true);
		int num = t.countTokens();
		String lines[] = new String[num];

		for (int i = 0; i < num; i++) {
			lines[i] = t.nextToken();
		}

		return lines;
	}

	public static void main(String arg[]) {

		Font baseFont = Fonts.commonFont(Font.PLAIN, 12);
		JLabel label = new JLabel("");

		String strs[] = {
				"\\\\This is a simple \\ntwo line string.\\gSome Greek\\rSome Roman.",
				"plain\\iitalic\\bbold italic\\Bitalic" };

		for (String str : strs) {
			System.err.println(">>>>>>");
			Vector<Snippet> snippets = getSnippets(baseFont, str, label);

			if (snippets != null) {
				for (Snippet s : snippets) {
					System.err.println("--------------");
					System.err.println(s.toString());
				}
			}

		}

	}
}
