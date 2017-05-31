package cnuphys.ced.cedview;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.util.List;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.Point2DSupport;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.util.X11Colors;

public abstract class CedXYView extends CedView {

	// margins around active area
	protected static int LMARGIN = 50;
	protected static int TMARGIN = 20;
	protected static int RMARGIN = 20;
	protected static int BMARGIN = 50;

	// line stroke
	public static Stroke stroke = GraphicsUtilities.getStroke(2.0f,
			LineStyle.SOLID);
	public static Stroke stroke2 = GraphicsUtilities.getStroke(3f,
			LineStyle.SOLID);

	public static final Color TRANS1 = new Color(192, 192, 192, 128);
	public static final Color TRANS2 = new Color(192, 192, 192, 64);
	public static final Color LIGHT = new Color(240, 240, 240);
	public static final Color TEXT = X11Colors.getX11Color("dark green");

	/**
	 * Constructor
	 * 
	 * @param keyVals
	 *            variable length argument list
	 */
	public CedXYView(Object... keyVals) {
		super(keyVals);
		setBeforeDraw();
		setAfterDraw();
		addItems();
	}

	/**
	 * Create the view's before drawer.
	 */
	protected abstract void setBeforeDraw();

	/**
	 * Create the view's after drawer.
	 */
	protected abstract void setAfterDraw();

	/**
	 * This adds the detector items. The AllDC view is not faithful to geometry.
	 * All we really uses in the number of superlayers, number of layers, and
	 * number of wires.
	 */
	protected abstract void addItems();

	// draw the axes
	protected void drawAxes(Graphics g, IContainer container, Rectangle bounds,
			boolean drawPhi) {
		Rectangle sr = getActiveScreenRectangle(container);
		// Rectangle sr = container.getInsetRectangle();

		g.setFont(Fonts.mediumFont);
		FontMetrics fm = container.getComponent().getFontMetrics(g.getFont());
		int fh = fm.getAscent();

		Rectangle2D.Double wr = new Rectangle2D.Double();
		container.localToWorld(sr, wr);
		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();

		g.setColor(Color.black);
		g.drawRect(sr.x, sr.y, sr.width, sr.height);

		// x values
		double del = wr.width / 40.;
		wp.y = wr.y;
		int bottom = sr.y + sr.height;
		for (int i = 1; i <= 40; i++) {
			wp.x = wr.x + del * i;
			container.worldToLocal(pp, wp);
			if ((i % 5) == 0) {
				g.drawLine(pp.x, bottom, pp.x, bottom - 12);

				String vs = valueString(wp.x);
				int xs = pp.x - fm.stringWidth(vs) / 2;

				g.drawString(vs, xs, bottom + fh + 1);

			} else {
				g.drawLine(pp.x, bottom, pp.x, bottom - 5);
			}
		}

		del = wr.height / 40.;
		wp.x = wr.x;
		for (int i = 0; i <= 40; i++) {
			wp.y = wr.y + del * i;
			container.worldToLocal(pp, wp);
			if ((i % 5) == 0) {
				g.drawLine(sr.x, pp.y, sr.x + 12, pp.y);
				String vs = valueString(wp.y);
				int xs = sr.x - fm.stringWidth(vs) - 1;

				g.drawString(vs, xs, pp.y + fh / 2);
			} else {
				g.drawLine(sr.x, pp.y, sr.x + 5, pp.y);
			}
		}

		// draw phi axis
		if (drawPhi) {
			Shape oldClip = g.getClip();
			g.clipRect(sr.x, sr.y, sr.width, sr.height);
			Point2D.Double wp2 = new Point2D.Double();
			Point p2 = new Point();
			g.setColor(LIGHT);
			for (int i = 0; i < 3; i++) {
				double phi = Math.toRadians(i * 60);
				wp.x = 170 * Math.cos(phi);
				wp.y = 170 * Math.sin(phi);
				wp2.x = -wp.x;
				wp2.y = -wp.y;
				container.worldToLocal(pp, wp);
				container.worldToLocal(p2, wp2);
				g.drawLine(pp.x, pp.y, p2.x, p2.y);
			}
			g.setClip(oldClip);
		}

		// draw coordinate system

		int left = sr.x + 25;
		int right = left + 50;
		bottom = sr.y + sr.height - 20;
		int top = bottom - 50;
		// g.setFont(labelFont);
		// fm = getFontMetrics(labelFont);

		Rectangle r = new Rectangle(left - fm.stringWidth("x") - 4, top
				- fm.getHeight() / 2 + 1, (right - left + fm.stringWidth("x")
				+ fm.stringWidth("y") + 9), (bottom - top) + fm.getHeight() + 2);

		g.setColor(TRANS1);
		g.fillRect(r.x, r.y, r.width, r.height);

		g.setColor(X11Colors.getX11Color("dark red"));
		g.drawLine(left, bottom, right, bottom);
//		g.drawLine(left, bottom, left, top);
		g.drawLine(right, bottom, right, top);

		g.drawString("y", right + 3, top + fm.getHeight() / 2 - 1);
		g.drawString("x", left - fm.stringWidth("x") - 2,
				bottom + fm.getHeight() / 2);
//		g.drawString("y", left - fm.stringWidth("y") - 2, top + fm.getHeight() / 2 - 1);
//		g.drawString("x", right+3,
//				bottom + fm.getHeight() / 2);

	}

	protected String valueString(double val) {
		if (Math.abs(val) < 1.0e-3) {
			return "0";
		}
		if (Math.abs(val) < 1.0) {
			return DoubleFormat.doubleFormat(val, 1);
		} else {
			return "" + (int) Math.round(val);
		}
	}

	protected Rectangle getActiveScreenRectangle(IContainer container) {
		return container.getInsetRectangle();
	}

	// convenience method to create a feedback string
	protected void fbString(String color, String str, List<String> fbstrs) {
		fbstrs.add("$" + color + "$" + str);
	}

	@Override
	public int getSector(IContainer container, Point screenPoint,
			Double worldPoint) {
		return 0;
	}

	/**
	 * Some basic feedback
	 * 
	 * @param container
	 *            the drawing container
	 * @param screenPoint
	 *            the pixel location
	 * @param worldPoint
	 *            the world location
	 * @param units
	 *            the unit string e.g., "mm" or "cm"
	 * @param feedbackStrings
	 *            the list to add to
	 */
	protected void basicFeedback(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, String units,
			List<String> feedbackStrings) {

		// get the common information
		super.getFeedbackStrings(container, screenPoint, worldPoint,
				feedbackStrings);

		Rectangle sr = getActiveScreenRectangle(container);
		if (!sr.contains(screenPoint)) {
			return;
		}

		fbString("yellow", "xy " + Point2DSupport.toString(worldPoint) + " "
				+ units, feedbackStrings);
		fbString("yellow", "radius " + 
				DoubleFormat.doubleFormat(Math.hypot(worldPoint.x, worldPoint.y), 2) + " "
				+ units, feedbackStrings);
		double phi = Math.toDegrees(Math.atan2(worldPoint.y, worldPoint.x));
		fbString("yellow", "phi " + DoubleFormat.doubleFormat(phi, 2)
				+ UnicodeSupport.DEGREE, feedbackStrings);

	}

}
