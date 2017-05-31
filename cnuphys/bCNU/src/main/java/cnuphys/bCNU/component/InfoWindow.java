package cnuphys.bCNU.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JLabel;
import javax.swing.JWindow;

import cnuphys.bCNU.util.X11Colors;

public class InfoWindow extends JWindow {

	private static InfoWindow _infoWindow;
	private static JLabel _infoLabel;

	private static final Font _defaultInfoFont = new Font("SansSerif",
			Font.PLAIN, 10);
	private static final Color _defaultTextColor = Color.black;
	private static final Color _defaultBackground = X11Colors
			.getX11Color("Alice Blue");

	/**
	 * Create a translucent window
	 */
	private InfoWindow() {
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
	public static void info(String label, Point screenPoint) {
		if (_infoWindow == null) {
			_infoWindow = new InfoWindow();
			_infoLabel = new JLabel(label);
			_infoLabel.setFont(_defaultInfoFont);
			_infoLabel.setForeground(_defaultTextColor);
			_infoWindow.getContentPane().add(_infoLabel);
		} else {
			_infoWindow.setVisible(false);
			_infoLabel.setText(label);
		}

		_infoWindow.pack();
		_infoWindow.setLocation(screenPoint);
		_infoWindow.setVisible(true);
	}

	/**
	 * Hide the info window
	 */
	public static void closeInfoWindow() {
		if ((_infoWindow != null) && (_infoWindow.isVisible())) {
			_infoWindow.setVisible(false);
		}
	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
				def.right + 2);
	}

}
