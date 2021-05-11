package cnuphys.splot.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalIconFactory;

import cnuphys.splot.style.LineStyle;

public class GraphicsUtilities {

	/**
	 * One default color for highlighted drawing.
	 */
	public static Color highlightColor1 = Color.red;

	/**
	 * Second default color for highlighted drawing.
	 */
	public static Color highlightColor2 = Color.yellow;

	/**
	 * Slop value for checking line selection
	 */
	private static final double SELECTRES = 3.01;

	/**
	 * Stipple used for highlight drawing.
	 */
	final private static float DASH[] = { 8.0f };

	/**
	 * A stroke used for highlight drawing.
	 */
	final public static BasicStroke dash1 = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 8.0f,
			DASH, 0.0f);

	/**
	 * Another stroke used for highlight drawing.
	 */
	final public static BasicStroke dash2 = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 8.0f,
			DASH, DASH[0]);

	/**
	 * A stroke used for highlight drawing.
	 */
	final public static BasicStroke dash1_2 = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 8.0f,
			DASH, 0.0f);

	/**
	 * Another stroke used for highlight drawing.
	 */
	final public static BasicStroke dash2_2 = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 8.0f,
			DASH, DASH[0]);

	/**
	 * A stroke used for common dashed lines.
	 */
	final public static BasicStroke simpleDash = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
			8.0f, DASH, DASH[0]);

	final public static BasicStroke simpleDash2 = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
			8.0f, DASH, 0.5f);

	/**
	 * Hashtable of strokes. As strokes are created, they are cached so that they
	 * aren't created over and over again. There should not be many--each linewidth
	 * and solid or dashed combination--probably no more than six or eight.
	 */
	protected static Hashtable<String, Stroke> strokes = new Hashtable<String, Stroke>(47);

	/**
	 * Set component to small size variant. Probably only works on Mac.
	 * 
	 * @param component the component to change size
	 */
	public static void setSizeSmall(JComponent component) {
		component.putClientProperty("JComponent.sizeVariant", "small");
	}

	/**
	 * Set component to mini size variant. Probably only works on Mac.
	 * 
	 * @param component the component to change size
	 */
	public static void setSizeMini(JComponent component) {
		component.putClientProperty("JComponent.sizeVariant", "mini");
	}

	/**
	 * Set button for square style. For mac so you don't have to use those wasteful
	 * oval buttons when space is tight
	 * 
	 * @param button the button to change style
	 */
	public static void setSquareButton(JButton button) {
		button.putClientProperty("JButton.buttonType", "square");
	}

	/**
	 * Set button for textured style. For mac so you don't have to use those
	 * wasteful oval buttons when space is tight
	 * 
	 * @param button the button to change style
	 */
	public static void setTexturedButton(JButton button) {
		button.putClientProperty("JButton.buttonType", "textured");
	}

	/**
	 * Compute a minimal clip from the intersection of the bounds of the current
	 * clip and a rectangle
	 * 
	 * @param currentClip the current clip
	 * @param rect        the rectangle of interest
	 * @return the intersection bounding rect, or <code>null</code>
	 */
	public static Rectangle minClip(Shape currentClip, Rectangle rect) {
		if ((currentClip == null) || (rect == null)) {
			return null;
		}

		if ((rect.width == 0) || (rect.height == 0)) {
			return null;
		}

		Rectangle cb = currentClip.getBounds();
		if ((cb == null) || (cb.width == 0) || (cb.height == 0)) {
			return null;
		}

		SwingUtilities.computeIntersection(rect.x, rect.y, rect.width, rect.height, cb);
		return cb;
	}

	/**
	 * Gets a stroke appropriate for the line width and line type. Try the hash
	 * table first, if not found create the Stroke and place it in the hashtable for
	 * future use.
	 * 
	 * @param lineWidth the desired line width in pixels.
	 * @param lineStyle the desired line style.
	 * @return the appropriate stroke.
	 */
	public static Stroke getStroke(float lineWidth, LineStyle lineStyle) {

		String hashKey = "STROKE_LW_" + lineWidth + "_LT_" + lineStyle;
		Stroke stroke = null;

		if (strokes != null) {
			stroke = (strokes.get(hashKey));
		}

		if (stroke == null) { // not in hashtable
			float fLineWidth = lineWidth;
			if (lineStyle.equals(LineStyle.SOLID)) {
				stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			}
			else if (lineStyle.equals(LineStyle.DASH)) {
				stroke = new BasicStroke(fLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 8.0f,
						new float[] { 10.0f, 10.0f }, 0.0f);
			}
			else if (lineStyle.equals(LineStyle.DOT_DASH)) {
				stroke = new BasicStroke(fLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 8.0f,
						new float[] { 4.0f, 4.0f, 10.0f, 4.0f }, 0.0f);
			}
			else if (lineStyle.equals(LineStyle.DOT)) {
				stroke = new BasicStroke(fLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 8.0f,
						new float[] { 4.0f, 4f }, 0.0f);
			}
			else if (lineStyle.equals(LineStyle.DOUBLE_DASH)) {
				stroke = new BasicStroke(fLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 8.0f,
						new float[] { 10.0f, 4.0f, 10.0f, 10.0f }, 0.0f);
			}
			else if (lineStyle.equals(LineStyle.LONG_DASH)) {
				stroke = new BasicStroke(fLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 8.0f,
						new float[] { 15.0f, 15.0f }, 0.0f);
			}
			else if (lineStyle.equals(LineStyle.LONG_DOT_DASH)) {
				stroke = new BasicStroke(fLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 8.0f,
						new float[] { 6.0f, 4.0f, 15.0f, 4.0f }, 0.0f);
			}
			else {
				stroke = new BasicStroke(lineWidth);
			}

			strokes.put(hashKey, stroke);
		}
		return stroke;
	}

	/**
	 * Create four points corresponding to the corners of a rectangle. Useful for
	 * item selection points.
	 * 
	 * @param rect the rectangle in question.
	 * @return points corresponding to the corners.
	 */
	public static Point[] rectangleToPoints(Rectangle rect) {
		Point pp[] = new Point[4];
		int l = rect.x;
		int t = rect.y;
		int r = l + rect.width;
		int b = t + rect.height;

		pp[0] = new Point(l, t);
		pp[1] = new Point(r, t);
		pp[2] = new Point(r, b);
		pp[3] = new Point(l, b);

		return pp;
	}

	/**
	 * Obtain a translucent buffer big enough for offscreen drawing.
	 * 
	 * @param c the component being rendered offscreen.
	 * @return a translucent buffer big enough for offscreen drawing.
	 */
	public static BufferedImage getComponentTranslucentImageBuffer(Component c) {
		if (c == null) {
			return null;
		}
		Dimension size = c.getSize();
		if ((size.width < 1) || (size.height < 1)) {
			return null;
		}
		BufferedImage myImage = new BufferedImage(size.width, size.height, Transparency.TRANSLUCENT);
		return myImage;
	}

	/**
	 * Obtain a buffer big enough for offscreen drawing. This does not do the
	 * drawing.
	 * 
	 * @param c the component to be rendered offscreen.
	 * @return an image big enough for the job.
	 */
	public static BufferedImage getComponentImageBuffer(Component c) {
		if (c == null) {
			return null;
		}
		Dimension size = c.getSize();
		if ((size.width < 1) || (size.height < 1)) {
			return null;
		}

		BufferedImage myImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		return myImage;
	}

	/**
	 * Paints the component on an existing image.
	 * 
	 * @param c     the component to paint.
	 * @param image the image to paint on, already created.
	 */
	public static void paintComponentOnImage(Component c, BufferedImage image) {
		if ((c == null) || (image == null)) {
			return;
		}
		Dimension size = c.getSize();

		if ((size.width < 1) || (size.height < 1)) {
			return;
		}
		Graphics2D g2 = image.createGraphics();
		c.paint(g2);
		g2.dispose();
	}

	/**
	 * return an image resulting from offscreen drawing.
	 * 
	 * @param c the component being rendered.
	 * @return an image upon which the component was drawn.
	 */
	public static BufferedImage getComponentImage(Component c) {
		BufferedImage image = getComponentImageBuffer(c);
		paintComponentOnImage(c, image);
		return image;
	}

	/**
	 * Center a component.
	 * 
	 * @param component The Component to center
	 */
	public static void centerComponent(Component component) {
		centerComponent(component, 0, 0);
	}

	/**
	 * Center a component.
	 * 
	 * @param component The Component to center.
	 * @param dh        offset from horizontal center.
	 * @param dv        offset from vertical center.
	 */
	public static void centerComponent(Component component, int dh, int dv) {

		if (component == null)
			return;

		try {

			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] allScreens = env.getScreenDevices();
			GraphicsConfiguration gc = allScreens[0].getDefaultConfiguration();

			Rectangle bounds = gc.getBounds();
			Dimension componentSize = component.getSize();
			if (componentSize.height > bounds.height) {
				componentSize.height = bounds.height;
			}
			if (componentSize.width > bounds.width) {
				componentSize.width = bounds.width;
			}

			int x = bounds.x + ((bounds.width - componentSize.width) / 2) + dh;
			int y = bounds.y + ((bounds.height - componentSize.height) / 2) + dv;

			component.setLocation(x, y);

		}
		catch (Exception e) {
			component.setLocation(200, 200);
			e.printStackTrace();
		}
	}

	/**
	 * Returns a dimension that is a specified fraction of the screen size. This is
	 * useful, for example, to make a farme 85% of the screen.
	 * 
	 * @param fraction the fraction desired, e.g., 0.85. No check for reasonableness
	 *                 is made.
	 * @return the requested dimension.
	 */
	public static Dimension screenFraction(double fraction) {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		d.width = (int) (fraction * d.width);
		d.height = (int) (fraction * d.height);
		return d;
	}

	/**
	 * Adjust a second point so that the rectangle it forms with the first point
	 * matches an aspect ratio of a given rectangle. Useful for rubber banding when
	 * you want to preserve the aspect ratio.
	 * 
	 * @param r  the rectangle whose aspect ration will be mayched.
	 * @param p0 anchor point
	 * @param p  will be adjusted so that p0, p form a rectangle with the same
	 *           aspected ratio as r.
	 */

	public static void rectangleARFixedAdjust(Rectangle r, Point p0, Point p) {
		if (r == null) {
			return;
		}

		double rw = r.getWidth();
		double rh = r.getHeight();

		if (rw < rh) {
			int sign_x = (p.x > p0.x) ? 1 : -1;
			double ar = rw / rh;
			double pw = sign_x * ar * Math.abs(p.y - p0.y);
			p.x = p0.x + (int) pw;
		}
		else {
			int sign_y = (p.y > p0.y) ? 1 : -1;
			double ar = rh / rw;
			double ph = sign_y * ar * Math.abs(p.x - p0.x);
			p.y = p0.y + (int) ph;
		}

	}

	/**
	 * Get a rectangle with the same apsect ratio. Useful for rubber banding when
	 * you want to preserve the sapect ratio.
	 */
	public static Rectangle rectangleARFixed(Rectangle r, Point p0, Point p) {
		rectangleARFixedAdjust(r, p0, p);
		return rectangleFromPoints(p0, p);
	}

	/**
	 * Given two points, return the rectangle
	 * 
	 * @param p1 one point
	 * @param p2 the other point
	 * @return the rectangle created from two points
	 */

	public static Rectangle rectangleFromPoints(Point p1, Point p2) {

		if ((p1 == null) && (p2 == null)) {
			return null;
		}

		if (p1 == null) {
			return new Rectangle(p2.x, p2.y, 0, 0);
		}
		else if (p2 == null) {
			return new Rectangle(p1.x, p1.y, 0, 0);
		}

		int w = Math.abs(p2.x - p1.x);
		int h = Math.abs(p2.y - p1.y);
		int x = Math.min(p1.x, p2.x);
		int y = Math.min(p1.y, p2.y);
		return new Rectangle(x, y, w, h);
	}

	/**
	 * Size and center a JFrame relative to the screen.
	 * 
	 * @param frame          the frame to size.
	 * @param fractionalSize the fraction desired of the screen--e.g., 0.85 for 85%.
	 */
	public void sizeToScreen(JFrame frame, double fractionalSize) {
		Dimension d = screenFraction(fractionalSize);
		frame.setSize(d);
		centerComponent(frame);
	}

	/**
	 * Draw an oval with a 3D etching.
	 * 
	 * @param g      the Graphics context.
	 * @param r      the bounding rectangle.
	 * @param fc     an optional fill color.
	 * @param ic     an optional inner fill color.
	 * @param outsie if <code>true</code>, the 3D effect is "out", otherwise "in".
	 */
	static public void drawSimple3DOval(Graphics g, Rectangle r, Color fc, Color ic, boolean outsie) {

		Color tc;
		Color bc;

		if (fc != null) {
			g.setColor(fc);
			g.fillArc(r.x, r.y, r.width - 1, r.height - 1, 0, 360);
		}

		if (ic != null) {
			g.setColor(ic);
			g.fillArc(r.x + 3, r.y + 3, r.width - 7, r.height - 7, 0, 360);
			g.setColor(Color.gray);
			g.drawArc(r.x + 3, r.y + 3, r.width - 7, r.height - 7, 0, 360);
		}

		// choose tc & bc to generate outsies or insies

		if (outsie) {
			tc = Color.white;
			bc = Color.black;
		}
		else {
			tc = Color.black;
			bc = Color.white;
		}

		// manually draw the lines that give the 3D effect

		g.setColor(tc);
		g.drawArc(r.x, r.y, r.width - 1, r.height - 1, 45, 180);
		g.setColor(bc);
		g.drawArc(r.x, r.y, r.width - 1, r.height - 1, 45, -180);
	}

	/**
	 * Draw a 3d "etching" around a rectangular area..
	 * 
	 * @param g      the Graphics context.
	 * @param x      the left of the rectangle.
	 * @param y      the top of the rectangle.
	 * @param w      the width of the rectangle.
	 * @param h      the height of the rectangle.
	 * @param outsie if <code>true</code>, the 3D effect is "out", otherwise "in".
	 */

	static public void drawSimple3DRect(Graphics g, int x, int y, int w, int h, boolean outsie) {
		drawSimple3DRect(g, x, y, w, h, null, outsie);
	}

	/**
	 * Draw a 3d rectangle.
	 * 
	 * @param g      the Graphics context.
	 * @param x      the left of the rectangle.
	 * @param y      the top of the rectangle.
	 * @param w      the width of the rectangle.
	 * @param h      the height of the rectangle.
	 * @param fc     the fill color (if null, no fill).
	 * @param outsie if <code>true</code>, the 3D effect is "out", otherwise "in".
	 */

	static public void drawSimple3DRect(Graphics g, int x, int y, int w, int h, Color fc, boolean outsie) {

		Color tc;
		Color bc;
		Color oldcolor = g.getColor();

		if (fc != null) {
			g.setColor(fc);
			g.fillRect(x, y, w, h);
		}

		// choose tc & bc to generate outsies or insies

		if (outsie) {
			tc = Color.white;
			bc = Color.black;
		}
		else {
			tc = Color.black;
			bc = Color.white;
		}

		// manually draw the lines that give the 3D effect

		int x2 = x + w;
		int y2 = y + h;
		g.setColor(tc);
		g.drawLine(x, y, x2, y);
		g.drawLine(x, y, x, y2);
		g.setColor(bc);
		g.drawLine(x, y2, x2, y2);
		g.drawLine(x2, y2, x2, y);
		g.setColor(oldcolor);
	}

	/**
	 * Draw a 3d rectangle.
	 * 
	 * @param g      the Graphics context.
	 * @param r      the rectangle.
	 * @param fc     the fill color.
	 * @param outsie if <code>true</code>, the 3D effect is "out", otherwise "in".
	 */

	static public void drawSimple3DRect(Graphics g, Rectangle r, Color fc, boolean outsie) {

		drawSimple3DRect(g, r.x, r.y, r.width - 1, r.height - 1, fc, outsie);
	}

	/**
	 * Fill and frame a rectangle
	 * 
	 * @param g     the Graphics context.
	 * @param r     the bounding rectangle.
	 * @param fill  the fill color.
	 * @param frame the frame (line) color.
	 */

	public static void fillAndFrameRect(Graphics g, Rectangle r, Color fill, Color frame) {

		Color old = g.getColor();

		if (fill != null) {
			g.setColor(fill);
			g.fillRect(r.x, r.y, r.width, r.height);
		}

		if (frame != null) {
			g.setColor(frame);
			g.drawRect(r.x, r.y, r.width, r.height);
		}

		g.setColor(old);

	}

	/**
	 * Draw a 3d diamond.
	 * 
	 * @param g      the Graphics context.
	 * @param r      the bounding rectangle.
	 * @param fc     the fill color.
	 * @param outsie if <code>true</code>, the 3D effect is "out", otherwise "in".
	 */

	static public void drawSimple3DDiamond(Graphics g, Rectangle r, Color fc, boolean outsie) {
		Color tc;
		Color bc;
		int x[] = new int[4];
		int y[] = new int[4];
		int xp[] = new int[3];
		int yp[] = new int[3];

		// choose tc & bc to generate outsies or insies

		if (outsie) {
			tc = Color.white;
			bc = Color.black;
		}
		else {
			tc = Color.black;
			bc = Color.white;
		}

		int xc = r.x + r.width / 2;
		int yc = r.y + r.height / 2;

		x[0] = r.x;
		y[0] = yc;
		x[1] = xc;
		y[1] = r.y;
		x[2] = r.x + r.width;
		y[2] = yc;
		x[3] = xc;
		y[3] = r.y + r.height;

		xp[0] = r.x;
		yp[0] = yc;
		xp[1] = xc;
		yp[1] = r.y;
		xp[2] = r.x + r.width;
		yp[2] = yc;

		g.setColor(tc);
		g.drawPolygon(xp, yp, 3);

		xp[0] = r.x + r.width;
		yp[0] = yc;
		xp[1] = xc;
		yp[1] = r.y + r.height;
		xp[2] = r.x;
		yp[2] = yc;

		g.setColor(bc);
		g.drawPolygon(xp, yp, 3);

		x[0]++;

		g.setColor(fc);
		g.fillPolygon(x, y, 4);

	}

	/**
	 * Draw a simple rectangle symbol.
	 * 
	 * @param g  the Graphics context.
	 * @param x  the horizontal center.
	 * @param y  the vertical center.
	 * @param w2 the half width.
	 * @param h2 the half height.
	 * @param lc the line color.
	 * @param fc the fill color.
	 */

	public static void drawRectangle(Graphics g, int x, int y, int w2, int h2, Color lc, Color fc) {

		// this will ensure all symbols correct size
		if (lc == null) {
			lc = fc;
		}

		if (fc != null) {
			g.setColor(fc);
			g.fillRect(x - w2, y - h2, 2 * w2, 2 * h2);
		}
		if (lc != null) {
			g.setColor(lc);
			g.drawRect(x - w2, y - h2, 2 * w2, 2 * h2);
		}
	}

	/**
	 * Draw a simple oval symbol.
	 * 
	 * @param g  the Graphics context.
	 * @param x  the horizontal center.
	 * @param y  the vertical center.
	 * @param w2 the half width.
	 * @param h2 the half height.
	 * @param lc the line color.
	 * @param fc the fill color.
	 */

	public static void drawOval(Graphics g, int x, int y, int w2, int h2, Color lc, Color fc) {

		// this will ensure all symbols correct size
		if (lc == null) {
			lc = fc;
		}

		if (fc != null) {
			g.setColor(fc);
			g.fillOval(x - w2, y - h2, 2 * w2, 2 * h2);
		}
		if (lc != null) {
			g.setColor(lc);
			g.drawOval(x - w2, y - h2, 2 * w2, 2 * h2);
		}
	}

	/**
	 * Draw a simple up triangle symbol.
	 * 
	 * @param g  the Graphics context.
	 * @param x  the horizontal center.
	 * @param y  the vertical center.
	 * @param s2 the half width.
	 * @param lc the line color.
	 * @param fc the fill color.
	 */

	public static void drawUpTriangle(Graphics g, int x, int y, int s2, Color lc, Color fc) {

		// this will ensure all symbols correct size
		if (lc == null) {
			lc = fc;
		}

		int l = x - s2;
		int t = y - s2;
		int r = x + s2;
		int b = y + s2;
		Polygon poly = new Polygon();
		poly.addPoint(l, b);
		poly.addPoint(x, t);
		poly.addPoint(r, b);
		if (fc != null) {
			g.setColor(fc);
			g.fillPolygon(poly);
		}
		if (lc != null) {
			g.setColor(lc);
			g.drawPolygon(poly);
		}
	}

	/**
	 * Draw a simple down triangle symbol.
	 * 
	 * @param g  the Graphics context.
	 * @param x  the horizontal center.
	 * @param y  the vertical center.
	 * @param s2 the half width.
	 * @param lc the line color.
	 * @param fc the fill color.
	 */

	public static void drawDownTriangle(Graphics g, int x, int y, int s2, Color lc, Color fc) {

		// this will ensure all symbols correct size
		if (lc == null) {
			lc = fc;
		}

		int l = x - s2;
		int t = y - s2;
		int r = x + s2;
		int b = y + s2;
		Polygon poly = new Polygon();
		poly.addPoint(l, t);
		poly.addPoint(r, t);
		poly.addPoint(x, b);
		if (fc != null) {
			g.setColor(fc);
			g.fillPolygon(poly);
		}
		if (lc != null) {
			g.setColor(lc);
			g.drawPolygon(poly);
		}
	}

	/**
	 * Draw a simple cross symbol.
	 * 
	 * @param g  the Graphics context.
	 * @param x  the horizontal center.
	 * @param y  the vertical center.
	 * @param s2 the half width.
	 * @param lc the line color.
	 */

	public static void drawCross(Graphics g, int x, int y, int s2, Color lc) {

		if (lc != null) {
			g.setColor(lc);
			g.drawLine(x - s2, y, x + s2, y);
			g.drawLine(x, y - s2, x, y + s2);
		}
	}

	/**
	 * Draw a simple "X" symbol.
	 * 
	 * @param g  the Graphics context.
	 * @param x  the horizontal center.
	 * @param y  the vertical center.
	 * @param s2 the half width.
	 * @param lc the line color.
	 */

	public static void drawX(Graphics g, int x, int y, int s2, Color lc) {

		if (lc != null) {
			g.setColor(lc);
			g.drawLine(x - s2, y - s2, x + s2, y + s2);
			g.drawLine(x - s2, y + s2, x + s2, y - s2);
		}
	}

	/**
	 * Draws a highlighted rectangle using default colors.
	 * 
	 * @param g the graphics context.
	 * @param r the rectangle being highlighted.
	 */

	public static void drawHighlightedRectangle(Graphics g, Rectangle r) {
		drawHighlightedRectangle(g, r, highlightColor1, highlightColor2);
	}

	/**
	 * Draws a highlighted rectangle.
	 * 
	 * @param g      the graphics context.
	 * @param r      the rectangle being highlighted.
	 * @param color1 one color for the alternating dash.
	 * @param color2 the other color for the alternating dash.
	 */

	public static void drawHighlightedRectangle(Graphics g, Rectangle r, Color color1, Color color2) {
		if ((g == null) || (r == null)) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();

		g2.setStroke(dash1);
		g2.setColor(color1);
		g2.drawRect(r.x, r.y, r.width, r.height);
		g2.setStroke(dash2);

		g2.setColor(color2);
		g2.drawRect(r.x, r.y, r.width, r.height);

		// restore the stroke
		g2.setStroke(oldStroke);

	}

	/**
	 * @param g
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param color1
	 * @param color2
	 */
	public static void drawHighlightedLine(Graphics g, int x1, int y1, int x2, int y2, Color color1, Color color2) {

		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();

		g2.setStroke(dash1_2);
		g2.setColor(color1);
		g2.drawLine(x1, y1, x2, y2);
		g2.setStroke(dash2_2);

		g2.setColor(color2);
		g2.drawLine(x1, y1, x2, y2);

		// restore the stroke
		g2.setStroke(oldStroke);

	}

	public static void drawHighlightedArc(Graphics g, int x, int y, int width, int height, int startAngle, int arcAngle,
			Color color1, Color color2) {
		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();

		g2.setStroke(dash1);
		g2.setColor(color1);
		g2.drawArc(x, y, width, height, startAngle, arcAngle);
		g2.setStroke(dash2);

		g2.setColor(color2);
		g2.drawArc(x, y, width, height, startAngle, arcAngle);

		// restore the stroke
		g2.setStroke(oldStroke);

	}

	/**
	 * Draws a highlighted rectangle using default colors.
	 * 
	 * @param g the graphics context.
	 * @param r the rectangle bounding the oval being highlighted.
	 */

	public static void drawHighlightedOval(Graphics g, Rectangle r) {
		drawHighlightedOval(g, r, highlightColor1, highlightColor2);
	}

	/**
	 * Draws a highlighted oval.
	 * 
	 * @param g      the graphics context.
	 * @param r      the rectangle bounding the oval being highlighted.
	 * @param color1 one color for the alternating dash.
	 * @param color2 the other color for the alternating dash.
	 */

	public static void drawHighlightedOval(Graphics g, Rectangle r, Color color1, Color color2) {
		if ((g == null) || (r == null)) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();

		g2.setStroke(dash1);
		g2.setColor(color1);
		g2.drawOval(r.x, r.y, r.width, r.height);
		g2.setStroke(dash2);

		g2.setColor(color2);
		g2.drawOval(r.x, r.y, r.width, r.height);

		// restore the stroke
		g2.setStroke(oldStroke);

	}

	/**
	 * Draws a highlighted polyline (set of unclosed points) using default colors.
	 * 
	 * @param g the graphics context.
	 * @param x the x coordinate array.
	 * @param y the y coordinate array.
	 * @param n the number of points to draw.
	 */

	public static void drawHighlightedPolyline(Graphics g, int x[], int y[], int n) {
		drawHighlightedPolyline(g, x, y, n, highlightColor1, highlightColor2);
	}

	/**
	 * Draws a highlighted polyline (set of unclosed points).
	 * 
	 * @param g      the graphics context.
	 * @param x      the x coordinate array.
	 * @param y      the y coordinate array.
	 * @param n      the number of points to draw.
	 * @param color1 one color for the alternating dash.
	 * @param color2 the other color for the alternating dash.
	 */

	public static void drawHighlightedPolyline(Graphics g, int x[], int y[], int n, Color color1, Color color2) {

		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();

		g2.setStroke(dash1);
		g.setColor(color1);
		g2.drawPolyline(x, y, n);
		g2.setStroke(dash2);

		g.setColor(color2);
		g2.drawPolyline(x, y, n);

		// restore the stroke
		g2.setStroke(oldStroke);

	}

	/**
	 * Draws a highlighted shape using default colors.
	 * 
	 * @param g     the graphics context.
	 * @param shape the shape being highlighted.
	 */

	public static void drawHighlightedShape(Graphics g, Shape shape) {
		drawHighlightedShape(g, shape, highlightColor1, highlightColor2);
	}

	/**
	 * Draws a highlighted shape.
	 * 
	 * @param g      the graphics context.
	 * @param shape  the shape being highlighted.
	 * @param color1 one color for the alternating dash.
	 * @param color2 the other color for the alternating dash.
	 */

	public static void drawHighlightedShape(Graphics g, Shape shape, Color color1, Color color2) {
		if ((g == null) || (shape == null)) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		Stroke oldStroke = g2.getStroke();

		g2.setStroke(dash1);
		g.setColor(color1);
		g2.draw(shape);
		g2.setStroke(dash2);

		g.setColor(color2);
		g2.draw(shape);

		// restore the stroke
		g2.setStroke(oldStroke);
	}

	/**
	 * Returns <code>true</code> if the point is on the line, with an amount of slop
	 * controlled by the class constant <code>SELECTRES</code>.
	 * 
	 * @param px     the x coordinate of the point to test.
	 * @param py     the y coordinate of the point to test.
	 * @param startx the x coordinate of the start of the line.
	 * @param starty the y coordinate of the start of the line.
	 * @param endx   the x coordinate of the end of the line.
	 * @param endy   the y coordinate of the end of the line.
	 * @return <code>true</code> if the point is on the line.
	 */
	public static boolean pointOnLine(int px, int py, int startx, int starty, int endx, int endy) {

		int delx = endx - startx;
		int dely = endy - starty;

		int fdelx = Math.abs(delx);
		int fdely = Math.abs(dely);

		if ((fdelx < 2) && (fdely < 2)) {
			return false;
		}

		double x = px;
		double y = py;

		double x1 = startx;
		double y1 = starty;

		double t;
		double dx = delx;
		double dy = dely;

		if (fdelx > fdely) {
			t = (x - x1) / dx;
			if ((t < 0.0) || (t > 1.0)) {
				return false;
			}

			double yt = y1 + t * dy;

			if (Math.abs(yt - y) < SELECTRES) {
				return true;

			}
		}
		else {
			t = (y - y1) / dy;
			if ((t < 0.0) || (t > 1.0)) {
				return false;
			}

			double xt = x1 + t * dx;

			if (Math.abs(xt - x) < SELECTRES) {
				return true;

			}

		}

		return false;
	}

	/**
	 * Returns <code>true</code> if the point is on the line, with an amount of slop
	 * controlled by the class constant <code>SELECTRES</code>.
	 * 
	 * @param p     the point to test.
	 * @param start the start of the line.
	 * @param end   the end of the line.
	 * @return <code>true</code> if the point is on the line.
	 */
	public static boolean pointOnLine(Point p, Point start, Point end) {
		if ((p == null) || (start == null) || (end == null)) {
			return false;
		}
		return pointOnLine(p.x, p.y, start.x, start.y, end.x, end.y);
	}

	/**
	 * Draw rotated text.
	 * 
	 * @param g            the Graphics context.
	 * @param s            the text to draw.
	 * @param x            the x pixel coordinate of baseline.
	 * @param y            the y pixel coordinate of baseline.
	 * @param angleDegrees the angle of rotation in decimal degrees.
	 */
	public static void drawRotatedText(Graphics2D g, String s, Font font, int x, int y, double angleDegrees) {

		drawRotatedText(g, s, font, x, y, 0, 0, angleDegrees);
	}

	/**
	 * Draw rotated text.
	 * 
	 * @param g            the Graphics context.
	 * @param s            the text to draw.
	 * @param xo           the x pixel coordinate of rotation point.
	 * @param yo           the y pixel coordinate of rotation point.
	 * @param delX         unrotated offset from rotation anchor
	 * @param delY         unrotated offset from rotation anchor
	 * @param angleDegrees the angle of rotation in decimal degrees.
	 */
	public static void drawRotatedText(Graphics2D g, String s, Font font, int xo, int yo, int delX, int delY,
			double angleDegrees) {

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (Math.abs(angleDegrees) < 0.5) {
			g.setFont(font);
			g.drawString(s, xo + delX, yo + delY);
			return;
		}

		AffineTransform rotation = new AffineTransform();
		rotation.rotate(Math.toRadians(angleDegrees));

		Point2D.Double offset = new Point2D.Double(delX, delY);
		Point2D.Double rotOffset = new Point2D.Double();
		rotation.transform(offset, rotOffset);

		AffineTransform translation = AffineTransform.getTranslateInstance(xo + rotOffset.x, yo + rotOffset.y);

		g.transform(translation);

		Font rotatedFont = font.deriveFont(rotation);
		g.setFont(rotatedFont);
		g.drawString(s, 0, 0);
		try {
			g.transform(translation.createInverse());
		}
		catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Draws an arrow on the given Graphics2D context
	 * 
	 * @param g1 The Graphics context to draw on
	 * @param x  The x location of the "tail" of the arrow
	 * @param y  The y location of the "tail" of the arrow
	 * @param xx The x location of the "head" of the arrow
	 * @param yy The y location of the "head" of the arrow
	 */
	public static void drawArrow(Graphics g1, int x, int y, int xx, int yy) {
		drawArrow(g1, x, y, xx, yy, false, null, null);
	}

	/**
	 * Draws an arrow on the given Graphics2D context
	 * 
	 * @param g1 The Graphics context to draw on
	 * @param x  The x location of the "tail" of the arrow
	 * @param y  The y location of the "tail" of the arrow
	 * @param xx The x location of the "head" of the arrow
	 * @param yy The y location of the "head" of the arrow
	 */
	public static void drawArrow(Graphics g1, int x, int y, int xx, int yy, boolean highlight, Color c1, Color c2) {

		if ((Math.abs(x - xx) < 4) && (Math.abs(y - yy) < 4)) {
			return;
		}

		Graphics2D g = (Graphics2D) g1;

		float arrowWidth = 8.0f;
		float theta = 0.423f;
		int[] xPoints = new int[3];
		int[] yPoints = new int[3];
		float[] vecLine = new float[2];
		float[] vecLeft = new float[2];
		float fLength;
		float th;
		float ta;
		float baseX, baseY;

		xPoints[0] = xx;
		yPoints[0] = yy;

		// build the line vector
		vecLine[0] = (float) xPoints[0] - x;
		vecLine[1] = (float) yPoints[0] - y;

		// build the arrow base vector - normal to the line
		vecLeft[0] = -vecLine[1];
		vecLeft[1] = vecLine[0];

		// setup length parameters
		fLength = (float) Math.sqrt(vecLine[0] * vecLine[0] + vecLine[1] * vecLine[1]);
		th = arrowWidth / (2.0f * fLength);
		ta = arrowWidth / (2.0f * ((float) Math.tan(theta) / 2.0f) * fLength);

		// find the base of the arrow
		baseX = (xPoints[0] - ta * vecLine[0]);
		baseY = (yPoints[0] - ta * vecLine[1]);

		// build the points on the sides of the arrow
		xPoints[1] = (int) (baseX + th * vecLeft[0]);
		yPoints[1] = (int) (baseY + th * vecLeft[1]);
		xPoints[2] = (int) (baseX - th * vecLeft[0]);
		yPoints[2] = (int) (baseY - th * vecLeft[1]);

		if (highlight) {
			c1 = (c1 == null) ? highlightColor1 : c1;
			c2 = (c2 == null) ? highlightColor2 : c2;
			drawHighlightedLine(g, x, y, (int) baseX, (int) baseY, c1, c2);

		}
		else {
			g.drawLine(x, y, (int) baseX, (int) baseY);
		}
		g.fillPolygon(xPoints, yPoints, 3);

		g.setColor(g.getColor().darker());
		g.drawPolygon(xPoints, yPoints, 3);
	}

	/**
	 * Draw a styled line
	 * 
	 * @param g     the graphics context
	 * @param style the style
	 * @param x1    starting x
	 * @param y1    starting y
	 * @param x2    ending x
	 * @param y2    ending y
	 */
	public static void drawStyleLine(Graphics g, Color lineColor, float lineWidth, LineStyle lineStyle, int x1, int y1,
			int x2, int y2) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(lineColor);

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(GraphicsUtilities.getStroke(lineWidth, lineStyle));

		g2.drawLine(x1, y1, x2, y2);
		g2.setStroke(oldStroke);
	}

	/**
	 * Get the hex format of the color in the form #rrggbbaa
	 * 
	 * @param color the color to convert
	 * @return the hex string for the color
	 */
	public static String colorToHex(Color color) {
		if (color == null) {
			return "#000000ff";
		}

		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = color.getAlpha();

		return String.format("#%02x%02x%02x%02x", r, g, b, a);
	}

	/**
	 * Obtain a color from a hex string in common internet format #rrggbbaa. The "#"
	 * and the aa are optional
	 * 
	 * @param hex the hex string
	 * @return the corresponding color or black on error.
	 */
	public static Color colorFromHex(String hex) {
		if (hex == null) {
			return Color.black;
		}

		// remove leading # if present
		if (hex.startsWith("#")) {
			hex = hex.substring(1);
		}

		while (hex.length() < 6) {
			hex += "0";
		}
		while (hex.length() < 8) {
			hex += "f";
		}

		try {
			int r = Integer.parseInt(hex.substring(0, 2), 16);
			int g = Integer.parseInt(hex.substring(2, 4), 16);
			int b = Integer.parseInt(hex.substring(4, 6), 16);
			int a = Integer.parseInt(hex.substring(6, 8), 16);
			return new Color(r, g, b, a);
		}
		catch (Exception e) {
			e.printStackTrace();
			return Color.black;
		}
	}

	public static Container getParentContainer(Component c) {
		if (c == null) {
			return null;
		}

		Container container = c.getParent();
		while (container != null) {
			if (container instanceof JInternalFrame) {
				return container;
			}
			if (container instanceof JFrame) {
				return container;
			}
			if (container instanceof JDialog) {
				return container;
			}
			if (container instanceof Window) {
				return container;
			}

			container = container.getParent();
		}

		return null;
	}

	/**
	 * Initialize the look and feel.
	 * 
	 * @param desiredLnf the desired look and feel.
	 */

	public static void initializeLookAndFeel() {

		LookAndFeelInfo[] lnfinfo = UIManager.getInstalledLookAndFeels();

		String preferredLnF[] = { "Mac OS X", "Windows", "Nimbus", "CDE/Motif", "Metal",
				UIManager.getCrossPlatformLookAndFeelClassName() };

		if ((lnfinfo == null) || (lnfinfo.length < 1)) {
			return;
		}

		for (String targetLnF : preferredLnF) {
			for (int i = 0; i < lnfinfo.length; i++) {
				if (lnfinfo[i].getName().indexOf(targetLnF) >= 0) {
					try {
						UIManager.setLookAndFeel(lnfinfo[i].getClassName());
						UIDefaults defaults = UIManager.getDefaults();

						// replace the horrible windows check icon
						if ("Windows".equalsIgnoreCase(lnfinfo[i].getName())) {
							// defaults.put("RadioButtonMenuItem.checkIcon",
							// defaults.get("RadioButton.icon"));
							defaults.put("RadioButtonMenuItem.checkIcon",
									MetalIconFactory.getRadioButtonMenuItemIcon());
						}
						return;
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static void main(String arg[]) {
		Color colors[] = { Color.red, Color.blue, Color.green, new Color(16, 32, 64, 128) };

		for (Color c : colors) {
			String s = colorToHex(c);
			Color tc = colorFromHex(s);

			System.out.println(s);
			System.out.println(tc.toString());
		}
	}

}