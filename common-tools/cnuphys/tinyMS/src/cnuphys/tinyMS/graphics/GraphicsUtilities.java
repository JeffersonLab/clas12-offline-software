package cnuphys.tinyMS.graphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
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
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import cnuphys.tinyMS.log.Log;

/**
 * A class of generic static methods for simple graphics utilities.
 * 
 * @author heddle
 * 
 */
public class GraphicsUtilities {

	/**
	 * Set component to small size variant. Probably only works on Mac.
	 * 
	 * @param component
	 *            the component to change size
	 */
	public static void setSizeSmall(JComponent component) {
		component.putClientProperty("JComponent.sizeVariant", "small");
	}

	/**
	 * Set component to mini size variant. Probably only works on Mac.
	 * 
	 * @param component
	 *            the component to change size
	 */
	public static void setSizeMini(JComponent component) {
		component.putClientProperty("JComponent.sizeVariant", "mini");
	}

	/**
	 * Set button for square style. For mac so you don't have to use those
	 * wasteful oval buttons when space is tight
	 * 
	 * @param button
	 *            the button to change style
	 */
	public static void setSquareButton(JButton button) {
		button.putClientProperty("JButton.buttonType", "square");
	}

	/**
	 * Set button for textured style. For mac so you don't have to use those
	 * wasteful oval buttons when space is tight
	 * 
	 * @param button
	 *            the button to change style
	 */
	public static void setTexturedButton(JButton button) {
		button.putClientProperty("JButton.buttonType", "textured");
	}

	/**
	 * Compute a minimal clip from the intersection of the bounds of the current
	 * clip and a rectangle
	 * 
	 * @param currentClip
	 *            the current clip
	 * @param rect
	 *            the rectangle of interest
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
	 * Center a component.
	 * 
	 * @param component
	 *            The Component to center
	 */
	public static void centerComponent(Component component) {
		centerComponent(component, 0, 0);
	}

	/**
	 * Center a component.
	 * 
	 * @param component
	 *            The Component to center.
	 * @param dh
	 *            offset from horizontal center.
	 * @param dv
	 *            offset from vertical center.
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
			Log.getInstance().exception(e);
			component.setLocation(200, 200);
			e.printStackTrace();
		}
	}

	/**
	 * Returns a dimension that is a specified fraction of the screen size. This
	 * is useful, for example, to make a farme 85% of the screen.
	 * 
	 * @param fraction
	 *            the fraction desired, e.g., 0.85. No check for reasonableness
	 *            is made.
	 * @return the requested dimension.
	 */
	public static Dimension screenFraction(double fraction) {
		Dimension d = getDisplaySize();
		d.width = (int) (fraction * d.width);
		d.height = (int) (fraction * d.height);
		return d;
	}

	/**
	 * Get the screen size of the biggest display among the devices
	 * 
	 * @return the screen size of the biggest display among the devices
	 */
	public static Dimension getDisplaySize() {

		GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = g.getScreenDevices();

		int maxw = 0;
		int maxh = 0;

		for (int i = 0; i < devices.length; i++) {
			// maxw = Math.max(maxw, devices[i].getDisplayMode().getWidth());
			maxh = Math.max(maxh, devices[i].getDisplayMode().getHeight());
		}

		maxw = (16 * maxh) / 9;
		return new Dimension(maxw, maxh);
	}

	/**
	 * Adjust a second point so that the rectangle it forms with the first point
	 * matches an aspect ratio of a given rectangle. Useful for rubber banding
	 * when you want to preserve the aspect ratio.
	 * 
	 * @param r
	 *            the rectangle whose aspect ration will be mayched.
	 * @param p0
	 *            anchor point
	 * @param p
	 *            will be adjusted so that p0, p form a rectangle with the same
	 *            aspected ratio as r.
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
	 * Get a rectangle with the same apsect ratio. Useful for rubber banding
	 * when you want to preserve the aspect ratio.
	 * 
	 * @param r
	 *            the rectangle whose aspect ratio you want to match
	 * @param p0
	 *            one point
	 * @param p
	 *            a second point
	 * @return a rectangle with the same aspect ratio
	 */
	public static Rectangle rectangleARFixed(Rectangle r, Point p0, Point p) {
		rectangleARFixedAdjust(r, p0, p);
		return rectangleFromPoints(p0, p);
	}

	/**
	 * Given two points, return the rectangle
	 * 
	 * @param p1
	 *            one point
	 * @param p2
	 *            the other point
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
	 * @param frame
	 *            the frame to size.
	 * @param fractionalSize
	 *            the fraction desired of the screen--e.g., 0.85 for 85%.
	 */
	public void sizeToScreen(JFrame frame, double fractionalSize) {
		Dimension d = screenFraction(fractionalSize);
		frame.setSize(d);
		centerComponent(frame);
	}

	/**
	 * Draw an oval with a 3D etching.
	 * 
	 * @param g
	 *            the Graphics context.
	 * @param r
	 *            the bounding rectangle.
	 * @param fc
	 *            an optional fill color.
	 * @param ic
	 *            an optional inner fill color.
	 * @param outsie
	 *            if <code>true</code>, the 3D effect is "out", otherwise "in".
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
	 * @param g
	 *            the Graphics context.
	 * @param x
	 *            the left of the rectangle.
	 * @param y
	 *            the top of the rectangle.
	 * @param w
	 *            the width of the rectangle.
	 * @param h
	 *            the height of the rectangle.
	 * @param outsie
	 *            if <code>true</code>, the 3D effect is "out", otherwise "in".
	 */

	static public void drawSimple3DRect(Graphics g, int x, int y, int w, int h, boolean outsie) {
		drawSimple3DRect(g, x, y, w, h, null, outsie);
	}

	/**
	 * Draw a 3d rectangle.
	 * 
	 * @param g
	 *            the Graphics context.
	 * @param x
	 *            the left of the rectangle.
	 * @param y
	 *            the top of the rectangle.
	 * @param w
	 *            the width of the rectangle.
	 * @param h
	 *            the height of the rectangle.
	 * @param fc
	 *            the fill color (if null, no fill).
	 * @param outsie
	 *            if <code>true</code>, the 3D effect is "out", otherwise "in".
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
	 * @param g
	 *            the Graphics context.
	 * @param r
	 *            the rectangle.
	 * @param fc
	 *            the fill color.
	 * @param outsie
	 *            if <code>true</code>, the 3D effect is "out", otherwise "in".
	 */

	static public void drawSimple3DRect(Graphics g, Rectangle r, Color fc, boolean outsie) {

		drawSimple3DRect(g, r.x, r.y, r.width - 1, r.height - 1, fc, outsie);
	}

	/**
	 * Fill and frame a rectangle
	 * 
	 * @param g
	 *            the Graphics context.
	 * @param r
	 *            the bounding rectangle.
	 * @param fill
	 *            the fill color.
	 * @param frame
	 *            the frame (line) color.
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
	 * @param g
	 *            the Graphics context.
	 * @param r
	 *            the bounding rectangle.
	 * @param fc
	 *            the fill color.
	 * @param outsie
	 *            if <code>true</code>, the 3D effect is "out", otherwise "in".
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
	 * @param g
	 *            the Graphics context.
	 * @param x
	 *            the horizontal center.
	 * @param y
	 *            the vertical center.
	 * @param w2
	 *            the half width.
	 * @param h2
	 *            the half height.
	 * @param lc
	 *            the line color.
	 * @param fc
	 *            the fill color.
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
	 * @param g
	 *            the Graphics context.
	 * @param x
	 *            the horizontal center.
	 * @param y
	 *            the vertical center.
	 * @param w2
	 *            the half width.
	 * @param h2
	 *            the half height.
	 * @param lc
	 *            the line color.
	 * @param fc
	 *            the fill color.
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
	 * @param g
	 *            the Graphics context.
	 * @param x
	 *            the horizontal center.
	 * @param y
	 *            the vertical center.
	 * @param s2
	 *            the half width.
	 * @param lc
	 *            the line color.
	 * @param fc
	 *            the fill color.
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
	 * @param g
	 *            the Graphics context.
	 * @param x
	 *            the horizontal center.
	 * @param y
	 *            the vertical center.
	 * @param s2
	 *            the half width.
	 * @param lc
	 *            the line color.
	 * @param fc
	 *            the fill color.
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
	 * @param g
	 *            the Graphics context.
	 * @param x
	 *            the horizontal center.
	 * @param y
	 *            the vertical center.
	 * @param s2
	 *            the half width.
	 * @param lc
	 *            the line color.
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
	 * @param g
	 *            the Graphics context.
	 * @param x
	 *            the horizontal center.
	 * @param y
	 *            the vertical center.
	 * @param s2
	 *            the half width.
	 * @param lc
	 *            the line color.
	 */

	public static void drawX(Graphics g, int x, int y, int s2, Color lc) {

		if (lc != null) {
			g.setColor(lc);
			g.drawLine(x - s2, y - s2, x + s2, y + s2);
			g.drawLine(x - s2, y + s2, x + s2, y - s2);
		}
	}

	/**
	 * Draw rotated text.
	 * 
	 * @param g
	 *            the Graphics context.
	 * @param s
	 *            the text to draw.
	 * @param x
	 *            the x pixel coordinate of baseline.
	 * @param y
	 *            the y pixel coordinate of baseline.
	 * @param angleDegrees
	 *            the angle of rotation in decimal degrees.
	 */
	public static void drawRotatedText(Graphics2D g, String s, Font font, int x, int y, double angleDegrees) {

		drawRotatedText(g, s, font, x, y, 0, 0, angleDegrees);
	}

	/**
	 * Draw rotated text.
	 * 
	 * @param g
	 *            the Graphics context.
	 * @param s
	 *            the text to draw.
	 * @param xo
	 *            the x pixel coordinate of rotation point.
	 * @param yo
	 *            the y pixel coordinate of rotation point.
	 * @param delX
	 *            unrotated offset from rotation anchor
	 * @param delY
	 *            unrotated offset from rotation anchor
	 * @param angleDegrees
	 *            the angle of rotation in decimal degrees.
	 */
	public static void drawRotatedText(Graphics2D g,
			String s,
			Font font,
			int xo,
			int yo,
			int delX,
			int delY,
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
	 * Get the hex format of the color in the form #rrggbbaa
	 * 
	 * @param color
	 *            the color to convert
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
	 * Obtain a color from a hex string in common internet format #rrggbbaa. The
	 * "#" and the aa are optional
	 * 
	 * @param hex
	 *            the hex string
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
	 * Run a runnable directly if this is not the EDT (AWT) thread. If it is,
	 * run on the invokeLater method. Note in either case the runnable WILL be
	 * run on the EDT.
	 * 
	 * @param runnable
	 *            the runnable to run
	 */
	public static void invokeInDispatchThreadIfNeeded(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			// run directly
			runnable.run();
		}
		else {
			// do all gui events, then run on EDT
			SwingUtilities.invokeLater(runnable);
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
