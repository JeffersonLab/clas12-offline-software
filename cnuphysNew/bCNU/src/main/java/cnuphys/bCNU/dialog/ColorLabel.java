package cnuphys.bCNU.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

/**
 * @author heddle
 */
@SuppressWarnings("serial")
public class ColorLabel extends JComponent {

	// the current color
	private Color _currentColor;

	// the listener for color changes
	private IColorChangeListener _colorChangeListener;

	// the prompt label
	private String _prompt;

	// preferred total width for the label
	private int _desiredWidth = -1;

	// the size of the color box
	private int _rectSize = 12;

	// used for sizing
	private Dimension _size;

	private Font _font;

	/**
	 * Create a clickable color label.
	 * 
	 * @param colorChangeListener
	 *            the listener for color changes.
	 * @param intitialColor
	 *            the intial color.
	 * @param prompt
	 *            the prompt string.
	 */
	public ColorLabel(IColorChangeListener colorChangeListener,
			Color intitialColor, String prompt) {
		this(colorChangeListener, intitialColor, prompt, -1);
	}

	/**
	 * Create an inert color label.
	 * 
	 * @param color
	 *            the color.
	 * @param prompt
	 *            the prompt string.
	 */
	public ColorLabel(Color color, Font font, String prompt) {

		_currentColor = color;
		_font = font;
		_prompt = prompt;

		FontMetrics fm = this.getFontMetrics(_font);
		int sw = fm.stringWidth(prompt);
		_size = new Dimension(sw + _rectSize + 10, 18);
	}

	/**
	 * Create a clickable color label.
	 * 
	 * @param colorChangeListener
	 *            the listener for color changes.
	 * @param intitialColor
	 *            the initial color.
	 * @param prompt
	 *            the prompt string.
	 * @param desiredWidth
	 *            if positive the desired total width.
	 */
	public ColorLabel(IColorChangeListener colorChangeListener,
			Color intitialColor, String prompt, int desiredWidth) {
		_colorChangeListener = colorChangeListener;
		_currentColor = intitialColor;
		_prompt = prompt;
		_desiredWidth = desiredWidth;

		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				ColorDialog cd = new ColorDialog(_currentColor, true, true);

				cd.setVisible(true);

				if (cd.answer == DialogUtilities.OK_RESPONSE) {
					setColor(cd.getColor());
					repaint();
				}

			}
		};

		// given a reasonable size?
		if (_desiredWidth > 10) {
			_size = new Dimension(_desiredWidth, 18);
		}

		addMouseListener(ma);
	}

	@Override
	public void paintComponent(Graphics g) {
		FontMetrics fm = getFontMetrics(getFont());
		g.setFont(getFont());

		if (_currentColor == null) {
			g.setColor(Color.red);
			g.drawLine(4, 4, _rectSize, _rectSize);
			g.drawLine(4, _rectSize, _rectSize, 4);
		} else {
			g.setColor(_currentColor);
			g.fillRect(2, 2, _rectSize, _rectSize);
		}
		g.setColor(Color.black);
		g.drawRect(2, 2, _rectSize, _rectSize);
		g.setColor(Color.black);

		if (_font != null) {
			g.setFont(_font);
		}
		g.drawString(_prompt, _rectSize + 6, fm.getHeight() - 4);
	}

	@Override
	public Dimension getPreferredSize() {
		if (_size == null) {
			return super.getPreferredSize();
		}
		return _size;
	}

	/**
	 * Return the current color.
	 * 
	 * @return the current color.
	 */
	public Color getColor() {
		return _currentColor;
	}

	/**
	 * Set the new color.
	 * 
	 * @param newColor
	 *            the new color index.
	 */
	public void setColor(Color newColor) {
		setBackground(newColor);
		if (_colorChangeListener != null) {
			_colorChangeListener.colorChanged(this, newColor);
		}
		_currentColor = newColor;
		repaint();
	}

}
