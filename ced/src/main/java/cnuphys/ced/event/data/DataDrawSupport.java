package cnuphys.ced.event.data;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import cnuphys.ced.event.FeedbackRect;
import cnuphys.splot.plot.X11Colors;

public class DataDrawSupport {

	private static final Color gemc_hit_fillColor = new Color(255, 255, 0, 128);
	private static final Color gemc_hit_lineColor = X11Colors
			.getX11Color("dark green");

	private static final Color rec_hit_fillColor = new Color(0, 255, 255, 128);
	private static final Color rec_hit_lineColor = X11Colors
			.getX11Color("dark red");

	public static final String[] EC_PLANE_NAMES = { "?", "Inner", "Outer" };
	public static final String[] EC_VIEW_NAMES = { "?", "U", "V", "W" };

	// some colors
	public static final Color DC_TB_COLOR = X11Colors.getX11Color("Orange Red");
	public static final Color DC_HB_COLOR = Color.yellow;

	/**
	 * Draw a GEMC truth hit at the given screen location
	 * 
	 * @param g
	 *            the graphics context
	 * @param pp
	 *            the screen location
	 */
	public static void drawGemcHit(Graphics g, Point pp) {
		g.setColor(gemc_hit_fillColor);
		g.fillOval(pp.x - 3, pp.y - 3, 6, 6);
		// now the cross
		g.setColor(gemc_hit_lineColor);
		g.drawOval(pp.x - 3, pp.y - 3, 6, 6);
		g.drawLine(pp.x - 3, pp.y, pp.x + 3, pp.y);
		g.drawLine(pp.x, pp.y - 3, pp.x, pp.y + 3);
	}

	/**
	 * Draw a reconstructed hit at the given screen location
	 * 
	 * @param g
	 *            the graphics context
	 * @param pp
	 *            the screen location
	 */
	public static void drawReconHit(Graphics g, Point pp) {
		g.setColor(rec_hit_fillColor);
		g.fillOval(pp.x - 3, pp.y - 3, 6, 6);
		// now the cross
		g.setColor(rec_hit_lineColor);
		g.drawOval(pp.x - 3, pp.y - 3, 6, 6);
		g.drawLine(pp.x - 3, pp.y - 3, pp.x + 3, pp.y + 3);
		g.drawLine(pp.x - 3, pp.y + 3, pp.x + 3, pp.y - 3);
	}

	/**
	 * Get a string representing the id array
	 * 
	 * @param ids
	 *            variable length ids
	 * @return a string representing the id array
	 */
	public static String getIDString(int... ids) {
		if ((ids == null) || (ids.length < 1)) {
			return "???";
		}
		StringBuilder sb = new StringBuilder(50);
		sb.append("]");
		for (int i = 0; i < ids.length; i++) {
			sb.append(ids[i]);
			if (i < (ids.length - 1)) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	/**
	 * Draw a circle with a cross
	 * 
	 * @param g
	 *            the graphics object
	 * @param fbr
	 *            the enclosing rectangle
	 * @param circColor
	 *            the circle color
	 * @param crossColor
	 *            the cross color
	 */
	public static void drawCircleCross(Graphics g, FeedbackRect fbr,
			Color circColor, Color crossColor) {
		int l = fbr.x + 2;
		int t = fbr.y + 2;
		int r = l + fbr.width - 4;
		int b = t + fbr.height - 4;

		g.setColor(crossColor);
		if (fbr.option == 0) {
			g.drawLine(l, b, r, t);
			g.drawLine(l, t, r, b);
		} else {
			int xc = (l + r) / 2;
			int yc = (t + b) / 2;
			g.drawLine(l, yc, r, yc);
			g.drawLine(xc, t, xc, b);
		}

		g.setColor(circColor);
		g.drawOval(fbr.x, fbr.y, fbr.width, fbr.height);

	}

}
