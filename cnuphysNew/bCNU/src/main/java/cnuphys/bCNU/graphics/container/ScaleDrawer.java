/**
 * 
 */
package cnuphys.bCNU.graphics.container;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.util.Fonts;

/**
 * @author heddle
 *
 */
public class ScaleDrawer extends DrawableAdapter {

	// corners
	public static final int UPPERLEFT = 0;
	public static final int UPPERRIGHT = 1;
	public static final int BOTTOMLEFT = 2;
	public static final int BOTTOMRIGHT = 3;

	// scale length in pixels
	private static final int SCALELEN = 150;

	// tick length in pixels
	private static final int TICKLEN = 8;

	// font
	private static Font SCALEFONT = Fonts.commonFont(Font.PLAIN, 9);

	// the unit string
	private String _unitStr;

	private int _corner;

	public ScaleDrawer(String unitStr) {
		this(unitStr, BOTTOMRIGHT);
	}

	public ScaleDrawer(String unitStr, int corner) {
		if (unitStr != null) {
			_unitStr = " " + unitStr.trim();
		}
		_corner = corner;
	}

	/**
	 * Draw the scale object on the lower right
	 * 
	 * @param g
	 *            the graphics context
	 * @param container
	 *            the container
	 */
	@Override
	public void draw(Graphics g, IContainer container) {

		if (!isVisible()) {
			return;
		}

		Rectangle b = container.getComponent().getBounds();

		if (b.width < 200) {
			return;
		}

		int x = -9999;
		int y = -9999;

		switch (_corner) {
		case UPPERLEFT:
			x = 30;
			y = 30;
			break;
		case UPPERRIGHT:
			x = b.width - SCALELEN - 10;
			y = 30;
			break;
		case BOTTOMLEFT:
			x = 30;
			y = b.height - 30;
			break;
		default: // BOTTOMRIGHT
			x = b.width - SCALELEN - 10;
			y = b.height - 30;
			break;
		}

		Point p0 = new Point(x, y);
		Point p1 = new Point(x + SCALELEN, y);
		Point2D.Double wp0 = container.getWorldPoint();
		Point2D.Double wp1 = container.getWorldPoint();
		container.localToWorld(p0, wp0);
		container.localToWorld(p1, wp1);

		double distance = wp0.distance(wp1);
		if (distance > 0.1) {

			String str = null;
			if (distance > 100.0) {
				str = "" + (int) distance;
			} else {
				str = DoubleFormat.doubleFormat(distance, 1);
			}

			if (_unitStr != null) {
				str += _unitStr;
			}

			FontMetrics fm = container.getComponent().getFontMetrics(SCALEFONT);

			sdraw(g, x, y, str, Color.lightGray, fm);
			sdraw(g, ++x, ++y, str, Color.black, fm);
		}
	}

	// draws the scale i.e., does the dirty work
	private void sdraw(Graphics g, int x, int y, String str, Color color,
			FontMetrics fm) {
		int sw = fm.stringWidth(str);
		g.setColor(color);
		g.drawLine(x, y, x + SCALELEN, y);
		g.drawLine(x, y, x, y - TICKLEN);
		g.drawLine(x + SCALELEN, y, x + SCALELEN, y - TICKLEN);
		g.drawString(str, x + (SCALELEN - sw) / 2, y + fm.getAscent() + 2);
	}
}
