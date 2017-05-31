package cnuphys.bCNU.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JLabel;
import javax.swing.JWindow;

import cnuphys.bCNU.util.X11Colors;

public class TranslucentWindow extends JWindow {

	private static TranslucentWindow _transWindow;
	private static JLabel _transLabel;

	private static final Font _defaultInfoFont = new Font("SansSerif",
			Font.PLAIN, 10);
	private static final Color _defaultTextColor = Color.black;
	private static final Color _defaultBackground = X11Colors
			.getX11Color("Alice Blue");

	/**
	 * Create a translucent window
	 * 
	 * @param opacity
	 *            how opaque (0: transparent, 1: opaque)
	 */
	private TranslucentWindow(float opacity) {
		setOpacity(opacity);
		setBackground(_defaultBackground);
	}

	/**
	 * Create (or reuse) a translucent window for displaying a label. Probably
	 * used for hovering window.
	 * 
	 * @param label
	 *            the label
	 * @param opacity
	 * @param screenPoint
	 */
	public static void info(String label, float opacity, Point screenPoint) {
		if (_transWindow == null) {
			_transWindow = new TranslucentWindow(opacity);
			_transLabel = new JLabel(label);
			_transLabel.setFont(_defaultInfoFont);
			_transLabel.setForeground(_defaultTextColor);
			_transWindow.getContentPane().add(_transLabel);
		} else {
			_transWindow.setVisible(false);
			_transLabel.setText(label);
		}

		_transWindow.pack();
		_transWindow.setLocation(screenPoint);
		_transWindow.setVisible(true);
	}

	/**
	 * Hide the info window
	 */
	public static void closeInfoWindow() {
		if ((_transWindow != null) && (_transWindow.isVisible())) {
			_transWindow.setVisible(false);
		}
	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
				def.right + 2);
	}

	/**
	 * Check whether the graphics device supports translucency
	 * 
	 * @return
	 */
	public static boolean isTranslucencySupported() {
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();

		return gd
				.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
	}
}
