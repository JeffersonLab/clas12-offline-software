package cnuphys.bCNU.component.led;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.Fonts;

@SuppressWarnings("serial")
public class Led extends JComponent {

	public static int LED_HEIGHT = 16;

	public static int LED_WIDTH = 55;

	private Color _fontColor = Color.cyan;

	private Color _bgColor = Color.darkGray;

	// label font
	private Font _font;

	// draw the border
	private boolean _drawBorder;

	// enum for click type
	private enum ClickType {
		SINGLE, DOUBLE, RIGHT
	};

	// LED label
	private String _label;

	/**
	 * Listener list for clicks on the LED.
	 */
	protected EventListenerList ledListenerList = null;

	// state of the LED
	private LedState _state;

	/**
	 * Constructor
	 * 
	 * @param state
	 *            the initial state.
	 * @param label
	 *            the label. Should be no more than a few characters.
	 */
	public Led(LedState state, String label) {
		this(state, label, LED_WIDTH, LED_HEIGHT, Color.cyan, Color.darkGray,
				Fonts.mediumFont, true);
	}

	/**
	 * Constructor
	 * 
	 * @param state
	 *            the initial state.
	 * @param label
	 *            the label. Should be no more than a few characters.
	 */
	public Led(LedState state, String label, int width, int height, Color fg,
			Color bg, Font font, boolean drawBorder) {

		_state = state;
		_label = label;
		_font = font;
		_drawBorder = drawBorder;

		_fontColor = fg;
		_bgColor = bg;

		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (e.getClickCount() == 1) {
						notifyLedListeners(ClickType.SINGLE);
					} else if (e.getClickCount() == 2) {
						notifyLedListeners(ClickType.DOUBLE);
					}
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					if (e.getClickCount() == 1) {
						notifyLedListeners(ClickType.RIGHT);
					}
				}
			}
		};
		addMouseListener(ml);

		if (height < 1) {
			FontMetrics fm = getFontMetrics(_font);
			height = fm.getHeight();
		}
		setPreferredSize(new Dimension(width, height));
		setMaximumSize(new Dimension(width, height));
	}

	/**
	 * Paint the LED
	 * 
	 * @param g
	 *            the graphics context
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// background
		Rectangle b = getBounds();

		if (_bgColor != null) {
			g.setColor(_bgColor);
			g.fillRect(0, 0, b.width, b.height);
		}

		FontMetrics fm = getFontMetrics(_font);
		int y = (b.height + fm.getAscent()) / 2;

		// icon
		ImageIcon icon = getState().getIcon();
		if (icon != null) {
			g.drawImage(icon.getImage(), 2,
					(b.height - icon.getIconHeight()) / 2, this);
		}

		// label
		if (_label != null) {
			g.setColor(_fontColor);
			g.setFont(_font);
			g.drawString(_label, 14, y);
		}

		// border
		if (_drawBorder) {
			GraphicsUtilities.drawSimple3DRect(g, 0, 0, b.width - 1,
					b.height - 1, false);
		}

	}

	/**
	 * Notify listeners of a click.
	 * 
	 * @param type
	 *            The causal event.
	 */

	protected void notifyLedListeners(ClickType type) {
		if ((ledListenerList == null) || (type == null)) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = ledListenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == LedListener.class) {

				switch (type) {
				case SINGLE:
					((LedListener) listeners[i + 1]).ledClicked(this);
					break;
				case DOUBLE:
					((LedListener) listeners[i + 1]).ledDoubleClicked(this);
					break;
				case RIGHT:
					((LedListener) listeners[i + 1]).ledRightClicked(this);
					break;
				}

			}
		}

	}

	/**
	 * Remove a LedListener.
	 * 
	 * @param fl
	 *            The LedListener listener to remove.
	 */
	public void removeLedListener(LedListener fl) {

		if ((fl == null) || (ledListenerList == null)) {
			return;
		}

		ledListenerList.remove(LedListener.class, fl);
	}

	/**
	 * Add a LedListener.
	 * 
	 * @param fl
	 *            the LedListener listener to add.
	 */
	public void addLedListener(LedListener fl) {

		if (fl == null) {
			return;
		}

		if (ledListenerList == null) {
			ledListenerList = new EventListenerList();
		}

		ledListenerList.add(LedListener.class, fl);
	}

	/**
	 * Get the state of the LED.
	 * 
	 * @return the current state of the LED.
	 */
	public LedState getState() {
		return _state;
	}

	/**
	 * Set the state of the LED.
	 * 
	 * @param state
	 *            the new state of the LED.
	 */
	public void setState(LedState state) {
		_state = state;
	}

	/**
	 * Get the LED label.
	 * 
	 * @return the label for the led.
	 */
	public String getLabel() {
		return _label;
	}

	public void setText(String label) {
		_label = label;
		repaint();
	}

}
