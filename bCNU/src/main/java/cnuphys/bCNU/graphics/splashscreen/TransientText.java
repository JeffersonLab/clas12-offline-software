package cnuphys.bCNU.graphics.splashscreen;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import cnuphys.bCNU.util.Fonts;

public class TransientText {

	// text that will be written on the splash screen window.
	private String _text;

	// the color of the text
	private Color _textColor;

	// the time in milliseconds relative to the splash screen opening
	// that the text will be drawn
	private long _startTime;

	// the duration in milliseconds that the text will be visible
	private long _duration;

	// the font for drawing the text
	private Font _font;

	// the x location of the baseline relative to the left
	private int _x;

	// the y location of the baseline relative to the top
	private int _y;

	// the time in milliseconds relative to the splash screen opening
	// that the text will stop being drawn
	private long _endTime;

	/**
	 * Transient text that will be written on the splash screen window.
	 * 
	 * @param text
	 *            the text to be written
	 * @param textColor
	 *            the color of the text
	 * @param startTime
	 *            the time in milliseconds relative to the splash screen opening
	 *            that the text will be drawn
	 * @param duration
	 *            the duration in milliseconds that the text will be visible
	 * @param font
	 *            the font for drawing the text
	 * @param x
	 *            the x location of the baseline relative to the left
	 * @param y
	 *            the y location of the baseline relative to the top
	 */
	public TransientText(String text, Color textColor, long startTime,
			long duration, Font font, int x, int y) {

		_text = text;
		_textColor = textColor != null ? textColor : Color.black;
		_startTime = Math.max(0, startTime);
		_duration = Math.max(50, duration);
		_font = font != null ? font : Fonts.smallFont;
		_x = x;
		_y = y;

		_endTime = _startTime + duration;
	}

	/**
	 * Draw the string if the time is right.
	 * 
	 * @param g
	 *            the graphics context
	 * @param splashStartTime
	 *            the system time in millis when the splashscreen started.
	 */
	public void draw(Graphics g, long splashStartTime) {

		if (splashStartTime < 0) {
			return;
		}

		long timeFromStart = System.currentTimeMillis() - splashStartTime;

		if ((timeFromStart < _startTime) || (timeFromStart > _endTime)) {
			return;
		}

		g.setFont(_font);
		g.setColor(_textColor);
		g.drawString(_text, _x, _x);
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return _text;
	}

	/**
	 * @return the textColor
	 */
	public Color getTextColor() {
		return _textColor;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return _startTime;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return _duration;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return _x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return _y;
	}

	/**
	 * @return the font
	 */
	public Font getFont() {
		return _font;
	}

}
