package cnuphys.splot.edit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

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

	/**
	 * Create an inert color label.
	 * 
	 * @param color  the color.
	 * @param prompt the prompt string.
	 */
	public ColorLabel(IColorChangeListener listener, Color color, Font font, String prompt) {

		_currentColor = color;
		setFont(font);
		_prompt = prompt;
		_colorChangeListener = listener;

		FontMetrics fm = this.getFontMetrics(font);
		int sw = fm.stringWidth(prompt);
		_size = new Dimension(sw + _rectSize + 10, 18);

		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
//				if (!isEnabled()) {
//					return;
//				}

				ColorDialog cd = new ColorDialog(_prompt, _currentColor, true, true);

				cd.setVisible(true);

				if (cd.answer == DialogUtilities.OK_RESPONSE) {
					setColor(cd.getColor());
					repaint();
				}

			}
		};

		addMouseListener(ma);

	}

	/**
	 * Set the color change listener
	 * 
	 * @param listener the color change listener
	 */
	public void setColorListener(IColorChangeListener listener) {
		_colorChangeListener = listener;
	}

	@Override
	public void paintComponent(Graphics g) {
		FontMetrics fm = getFontMetrics(getFont());
		g.setFont(getFont());
		boolean enabled = isEnabled();

		if (_currentColor == null) {
			g.setColor(Color.red);
			g.drawLine(4, 4, _rectSize, _rectSize);
			g.drawLine(4, _rectSize, _rectSize, 4);
		}
		else {
			g.setColor(enabled ? _currentColor : Color.gray);
			g.fillRect(2, 2, _rectSize, _rectSize);
		}
		g.setColor(enabled ? Color.black : Color.gray);
		g.drawRect(2, 2, _rectSize, _rectSize);

		g.drawString(_prompt, _rectSize + 6, fm.getHeight());
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
	 * @param newColor the new color index.
	 */
	public void setColor(Color newColor) {
		_currentColor = newColor;
		setBackground(_currentColor);
		if (_colorChangeListener != null) {
			_colorChangeListener.colorChanged(this, _currentColor);
		}
		repaint();
	}

}