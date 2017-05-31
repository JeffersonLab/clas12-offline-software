package cnuphys.bCNU.graphics.rubberband;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.lang.reflect.Method;

import javax.swing.SwingUtilities;

/**
 * This is an experimental class for overlaying a clear window on a component.
 * It is needed for rubberbanding over a 3D canvas.
 * 
 * @author heddle
 *
 */

@SuppressWarnings("serial")
public class RubberBandClearWindow extends Window {

	private Color shapeFill = Color.cyan;

	// a rubber band rectangle if we are rb'ing a rect
	private Rectangle _rubberBandRectangle;

	/**
	 * Create a translucent window
	 * 
	 * @param opacity
	 *            the smaller the number, the more transparent
	 * @throws HeadlessException
	 */
	RubberBandClearWindow(Component parent, float opacity)
			throws HeadlessException {
		super(null);
		try {
			Class<?> awtUtilitiesClass = Class
					.forName("com.sun.awt.AWTUtilities");
			Method mSetWindowOpacity = awtUtilitiesClass.getMethod(
					"setWindowOpacity", Window.class, float.class);
			mSetWindowOpacity.invoke(null, this, opacity);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Rectangle bounds = parent.getBounds();
		Point pOrig = new Point(0, 0);
		SwingUtilities.convertPointToScreen(pOrig, parent);
		// setBounds(pOrig.x, pOrig.y, bounds.width, bounds.height);
		setBounds(pOrig.x, pOrig.y, 0, 0);
	}

	@Override
	public void paint(Graphics g) {
		if (_rubberBandRectangle != null) {
			g.setColor(shapeFill);
			g.fillRect(_rubberBandRectangle.x, _rubberBandRectangle.y,
					_rubberBandRectangle.width, _rubberBandRectangle.height);
		}
	}

	@Override
	public void setVisible(boolean vis) {
		super.setVisible(vis);
		if (!vis) {
			_rubberBandRectangle = null;
		}
	}

	public void setRubberBandRectangle(Component parent, Rectangle rbr) {
		Point pp = new Point(rbr.x, rbr.y);
		SwingUtilities.convertPointToScreen(pp, parent);
		setBounds(pp.x, pp.y, rbr.width, rbr.height);
	}

}
