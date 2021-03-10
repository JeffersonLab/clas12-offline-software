package cnuphys.bCNU.graphics.world;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.JComponent;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.IStyled;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.util.MathUtilities;
import cnuphys.bCNU.util.Point2DSupport;
import cnuphys.bCNU.util.TextUtilities;
import cnuphys.lund.LundId;
import cnuphys.lund.LundStyle;
import cnuphys.lund.LundSupport;

/**
 * Drawing methods for world based objects.
 * 
 * @author heddle
 * 
 */
public class WorldGraphicsUtilities {

	// For drawing etched lines
	private static final Color ETCH_LIGHT = Color.white;

	// For drawing etched lines
	private static final Color ETCH_DARK = Color.black;

	// Small number check
	private static final double TINY = 1.0e-8;

	// for approximating arcs
	private static final int NUMCIRCSTEP = 60;

	// default stroke
	public static final Stroke DEFAULTSTROKE = GraphicsUtilities.getStroke(1, LineStyle.SOLID);

	/**
	 * Draw a horizontal "etched" world line
	 * 
	 * @param g          the graphics context.
	 * @param container  the container on which it is rendered.
	 * @param x          x coordinate of start point.
	 * @param y          y coordinate of horizontal line
	 * @param w          the width of the horizontal line
	 * @param lightAbove if <code>true</code> will be as if lit from above.
	 */
	public static void drawEtchedHorizontalWorldLine(Graphics g, IContainer container, double x, double y, double w,
			boolean lightAbove) {

		Graphics2D g2 = (Graphics2D) g;

		Point p1 = new Point();
		Point p2 = new Point();

		container.worldToLocal(p1, x, y);
		container.worldToLocal(p2, x + w, y);

		if (lightAbove) {
			g2.setColor(ETCH_LIGHT);
			g2.drawLine(p1.x, p1.y, p2.x, p2.y);
			g2.setColor(ETCH_DARK);
			g2.drawLine(p1.x, p1.y + 1, p2.x, p2.y + 1);
		} else {
			g2.setColor(ETCH_DARK);
			g2.drawLine(p1.x, p1.y, p2.x, p2.y);
			g2.setColor(ETCH_LIGHT);
			g2.drawLine(p1.x, p1.y + 1, p2.x, p2.y + 1);
		}
	}

	/**
	 * Draw a verticle "etched" world line
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param x         x coordinate of vertical line
	 * @param y         y coordinate of horizontal line
	 * @param h         the width of the horizontal line
	 * @param lightLeft if <code>true</code> will be as if lit from left.
	 */
	public static void drawEtchedVerticalWorldLine(Graphics g, IContainer container, double x, double y, double h,
			boolean lightLeft) {

		Graphics2D g2 = (Graphics2D) g;

		Point p1 = new Point();
		Point p2 = new Point();

		container.worldToLocal(p1, x, y);
		container.worldToLocal(p2, x, y + h);

		if (lightLeft) {
			g2.setColor(ETCH_LIGHT);
			g2.drawLine(p1.x, p1.y, p2.x, p2.y);
			g2.setColor(ETCH_DARK);
			g2.drawLine(p1.x + 1, p1.y, p2.x + 1, p2.y);
		} else {
			g2.setColor(ETCH_DARK);
			g2.drawLine(p1.x, p1.y, p2.x, p2.y);
			g2.setColor(ETCH_LIGHT);
			g2.drawLine(p1.x + 1, p1.y, p2.x + 1, p2.y);
		}
	}

	/**
	 * Draw a world line
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param wp0       one end point.
	 * @param wp1       another end point.
	 * @param style     the style object to use for color, width, etc.
	 */
	public static void drawWorldLine(Graphics g, IContainer container, Point2D.Double wp0, Point2D.Double wp1,
			IStyled style) {
		drawWorldLine(g, container, wp0.x, wp0.y, wp1.x, wp1.y, style);
	}

	/**
	 * Draw a world line
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param x1        x coordinate of start point.
	 * @param y1        y coordinate of start point.
	 * @param x2        x coordinate of end point.
	 * @param y2        y coordinate of end point.
	 * @param style     the style object to use for color, width, etc.
	 */
	public static void drawWorldLine(Graphics g, IContainer container, double x1, double y1, double x2, double y2,
			IStyled style) {
		drawWorldLine(g, container, x1, y1, x2, y2, style.getLineColor(), style.getLineWidth(), style.getLineStyle());
	}

	/**
	 * Draw a world line
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param x1        x coordinate of start point.
	 * @param y1        y coordinate of start point.
	 * @param x2        x coordinate of end point.
	 * @param y2        y coordinate of end point.
	 * @param lineColor the line color (can be <code>null</code>).
	 * @param lineWidth the line width in pixels.
	 * @param lineStyle the line style.
	 */
	public static void drawWorldLine(Graphics g, IContainer container, double x1, double y1, double x2, double y2,
			Color lineColor, int lineWidth, LineStyle lineStyle) {

		if (lineColor == null) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		Point p1 = new Point();
		Point p2 = new Point();

		container.worldToLocal(p1, x1, y1);
		container.worldToLocal(p2, x2, y2);

		// outline?
		Stroke oldStroke = g2.getStroke();
		Stroke newStroke = GraphicsUtilities.getStroke(lineWidth, lineStyle);
		g2.setStroke(newStroke);
		g2.setColor(lineColor);
		g2.drawLine(p1.x, p1.y, p2.x, p2.y);
		g2.setStroke(oldStroke);
	}

	/**
	 * Set the stlye defaults
	 * 
	 * @param g the graphics context
	 */
	public static void setStyleDefaults(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g.setColor(Color.black);
		g2.setStroke(DEFAULTSTROKE);
	}

	/**
	 * Convert two world points (possible line endpoints) to pixel coordinates.
	 * 
	 * @param container
	 * @param p0        will hold one pixel point.
	 * @param p1        will hold other pixel point.
	 * @param wp0       one end point.
	 * @param wp1       another end point.
	 */
	public static void getPixelEnds(IContainer container, Point p0, Point p1, Point2D.Double wp0, Point2D.Double wp1) {
		container.worldToLocal(p0, wp0.x, wp0.y);
		container.worldToLocal(p1, wp1.x, wp1.y);
	}

	/**
	 * Draw a world line 1 pixel wide in solid style
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param x1        x coordinate of start point.
	 * @param y1        y coordinate of start point.
	 * @param x2        x coordinate of end point.
	 * @param y2        y coordinate of end point.
	 * @param lineColor the line color (can be <code>null</code>).
	 */
	public static void drawWorldLine(Graphics g, IContainer container, double x1, double y1, double x2, double y2,
			Color lineColor) {
		drawWorldLine(g, container, x1, y1, x2, y2, lineColor, 1, LineStyle.SOLID);
	}

	/**
	 * Get pixel rectangle boundary of two wotld points
	 * 
	 * @param container
	 * @param wp0       one end point.
	 * @param wp1       another end point.
	 * @return pixel rectangle boundary.
	 */
	public static Rectangle getBounds(IContainer container, Point2D.Double wp0, Point2D.Double wp1) {
		Point p0 = new Point();
		Point p1 = new Point();
		getPixelEnds(container, p0, p1, wp0, wp1);
		Rectangle r = GraphicsUtilities.rectangleFromPoints(p0, p1);

		if (r.height < 2) {
			r.y -= 1;
			r.height += 2;
		}
		if (r.width < 2) {
			r.x -= 1;
			r.width += 2;
		}

		return r;
	}

	/**
	 * Get pixel rectangle boundary of two wotld points
	 * 
	 * @param container
	 * @param x1        x coordinate of one end point.
	 * @param y1        y coordinate of one end point.
	 * @param x2        x coordinate of other end point.
	 * @param y2        y coordinate of other end point.
	 * @return pixel rectangle boundary.
	 */
	public static Rectangle getBounds(IContainer container, double x1, double y1, double x2, double y2) {
		Point p0 = new Point();
		Point p1 = new Point();
		container.worldToLocal(p0, x1, y1);
		container.worldToLocal(p1, x2, y2);

		Rectangle r = GraphicsUtilities.rectangleFromPoints(p0, p1);

		if (r.height < 2) {
			r.y -= 1;
			r.height += 2;
		}
		if (r.width < 2) {
			r.x -= 1;
			r.width += 2;
		}

		return r;
	}

	/**
	 * Draw a world radarc. This is something with an inner and outer radius and an
	 * opening and closing angle.
	 * 
	 * @param g          the graphics context.
	 * @param container  the container on which it is rendered.
	 * @param xc         the horizontal center.
	 * @param yc         the vertical center.
	 * @param rmin       minimum (inner) radius.
	 * @param rmax       maximum (outer) radius.
	 * @param startAngle start angle in degrees.
	 * @param stopAngle  stop angle in degrees.
	 * @param fillColor  the fill color (can be <code>null</code>).
	 * @param lineColor  the line color (can be <code>null</code>).
	 * @param lineWidth  the line width in pixels.
	 * @param lineStyle  the line style.
	 */
	public static void drawWorldRadArc(Graphics g, IContainer container, double xc, double yc, double rmin, double rmax,
			double startAngle, double stopAngle, Color fillColor, Color lineColor, int lineWidth, LineStyle lineStyle) {

		Graphics2D g2 = (Graphics2D) g;

		Polygon p0 = createWorldArc(container, xc, yc, rmin, startAngle, stopAngle);
		Polygon p1 = createWorldArc(container, xc, yc, rmax, stopAngle, startAngle);

		GeneralPath path = new GeneralPath(Path2D.WIND_EVEN_ODD);
		path.append(p0, false);
		path.append(p1, false);

		// fill?
		if (fillColor != null) {
			g2.setColor(fillColor);
			g2.fill(path);
		}

		// outline?
		if (lineColor != null) {
			Stroke oldStroke = g2.getStroke();
			Stroke newStroke = GraphicsUtilities.getStroke(lineWidth, lineStyle);
			g2.setStroke(newStroke);
			g2.setColor(lineColor);
			g2.draw(path);
			g2.setStroke(oldStroke);
		}

	}

	/**
	 * Draw a world radarc. This is something with an inner and outer radius and an
	 * opening and closing angle. This will draw using a 1 pixel wide solid line.
	 * 
	 * @param g          the graphics context.
	 * @param container  the container on which it is rendered.
	 * @param xc         the horizontal center.
	 * @param yc         the vertical center.
	 * @param rmin       minimum (inner) radius.
	 * @param rmax       maximum (outer) radius.
	 * @param startAngle start angle in degrees.
	 * @param stopAngle  stop angle in degrees.
	 * @param fillColor  the fill color (can be <code>null</code>).
	 * @param lineColor  the line color (can be <code>null</code>).
	 */
	public static void drawWorldRadArc(Graphics g, IContainer container, double xc, double yc, double rmin, double rmax,
			double startAngle, double stopAngle, Color fillColor, Color lineColor) {

		drawWorldRadArc(g, container, xc, yc, rmin, rmax, startAngle, stopAngle, fillColor, lineColor, 1,
				LineStyle.SOLID);
	}

	/**
	 * Draw a world radarc. This is something with an inner and outer radius and an
	 * opening and closing angle. This will draw using a 1 pixel wide solid line.
	 * 
	 * @param g          the graphics context.
	 * @param container  the container on which it is rendered.
	 * @param xc         the horizontal center.
	 * @param yc         the vertical center.
	 * @param rmin       minimum (inner) radius.
	 * @param rmax       maximum (outer) radius.
	 * @param startAngle start angle in degrees.
	 * @param stopAngle  stop angle in degrees.
	 * @param style      An IStyled object, such as an Item.
	 */
	public static void drawWorldRadArc(Graphics g, IContainer container, double xc, double yc, double rmin, double rmax,
			double startAngle, double stopAngle, IStyled style) {

		drawWorldRadArc(g, container, xc, yc, rmin, rmax, startAngle, stopAngle, style.getFillColor(),
				style.getLineColor(), style.getLineWidth(), style.getLineStyle());
	}

	/**
	 * Create a screen representation of world circle.
	 * 
	 * @param container the container on which it is rendered.
	 * @param xc        the horizontal center.
	 * @param yc        the vertical center.
	 * @param radius    the radius.
	 * @return a polygon approximating the world circle.
	 */
	public static Polygon createWorldCircle(IContainer container, double xc, double yc, double radius) {
		int x[] = new int[NUMCIRCSTEP];
		int y[] = new int[NUMCIRCSTEP];

		double delAng = (2 * Math.PI) / (NUMCIRCSTEP - 1);

		Point pp = new Point();

		for (int i = 0; i < NUMCIRCSTEP; i++) {
			double theta = i * delAng;
			double wx = xc + radius * Math.cos(theta);
			double wy = yc + radius * Math.sin(theta);

			container.worldToLocal(pp, wx, wy);
			x[i] = pp.x;
			y[i] = pp.y;
		}

		return new Polygon(x, y, NUMCIRCSTEP);
	}

	/**
	 * Create a screen representation of world donut.
	 * 
	 * @param container the container on which it is rendered.
	 * @param xc        the horizontal center.
	 * @param yc        the vertical center.
	 * @param minRadius the min radius.
	 * @param maxRadius the max radius.
	 * @return a polygon approximating the world donut.
	 */
	public static Polygon createWorldDonut(IContainer container, double xc, double yc, double minRadius,
			double maxRadius) {

		if (minRadius < TINY) {
			return createWorldCircle(container, xc, yc, maxRadius);
		}

		int N2 = 2 * NUMCIRCSTEP;
		int x[] = new int[N2];
		int y[] = new int[N2];

		double delAng = (2 * Math.PI) / (NUMCIRCSTEP - 1);

		Point pp = new Point();

		for (int i = 0; i < NUMCIRCSTEP; i++) {
			int j = i + NUMCIRCSTEP;
			double theta = i * delAng;

			double cos = Math.cos(theta);
			double sin = Math.sin(theta);

			double wx = xc + minRadius * cos;
			double wy = yc + minRadius * sin;
			container.worldToLocal(pp, wx, wy);

			x[i] = pp.x;
			y[i] = pp.y;

			wx = xc + maxRadius * cos;
			wy = yc + maxRadius * sin;
			container.worldToLocal(pp, wx, wy);

			x[j] = pp.x;
			y[j] = pp.y;
		}

		return new Polygon(x, y, N2);
	}

	/**
	 * Create a screen representation of world arc.
	 * 
	 * @param container  the container on which it is rendered.
	 * @param xc         the horizontal center.
	 * @param yc         the vertical center.
	 * @param radius     the radius.
	 * @param startAngle start angle in degrees.
	 * @param stopAngle  stop angle in degrees.
	 * @return the polygon representing the arc.
	 */
	public static Polygon createWorldArc(IContainer container, double xc, double yc, double radius, double startAngle,
			double stopAngle) {

		if (Math.abs(stopAngle - startAngle) > (360.0 - TINY)) {
			return createWorldCircle(container, xc, yc, radius);
		}

		int x[] = new int[NUMCIRCSTEP];
		int y[] = new int[NUMCIRCSTEP];

		double rad0 = Math.toRadians(startAngle);
		double rad1 = Math.toRadians(stopAngle);
		double delAng = (rad1 - rad0) / (NUMCIRCSTEP - 1);

		Point pp = new Point();

		for (int i = 0; i < NUMCIRCSTEP; i++) {
			double theta = rad0 + i * delAng;
			double wx = xc + radius * Math.cos(theta);
			double wy = yc + radius * Math.sin(theta);

			container.worldToLocal(pp, wx, wy);
			x[i] = pp.x;
			y[i] = pp.y;
		}

		return new Polygon(x, y, NUMCIRCSTEP);
	}
	
	/**
	 * Draw a world oval.
	 * 
	 * @param g              the graphics context.
	 * @param container      the container on which it is rendered.
	 * @param worldRectangle the world rectangle defining the enclosed oval
	 * @param fillColor      the fill color (can be <code>null</code>).
	 * @param lineColor      the line color (can be <code>null</code>).
	 * @param lineWidth      the line width in pixels.
	 * @param lineStyle      the line style.
	 */
	public static void drawWorldOval(Graphics g, IContainer container, Rectangle2D.Double worldRectangle,
			Color fillColor, Color lineColor, int lineWidth, LineStyle lineStyle) {

		Graphics2D g2 = (Graphics2D) g;

		Rectangle rectangle = new Rectangle();
		container.worldToLocal(rectangle, worldRectangle);

		// fill?
		if (fillColor != null) {
			g2.setColor(fillColor);
			g2.fillOval(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
		}

		// outline?
		if (lineColor != null) {
			Stroke oldStroke = g2.getStroke();
			Stroke newStroke = GraphicsUtilities.getStroke(lineWidth, lineStyle);
			g2.setStroke(newStroke);
			g2.setColor(lineColor);
			g2.drawOval(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
			g2.setStroke(oldStroke);
		}

	}

	/**
	 * Draw a world rectangle.
	 * 
	 * @param g              the graphics context.
	 * @param container      the container on which it is rendered.
	 * @param worldRectangle the world rectangle being drawn.
	 * @param fillColor      the fill color (can be <code>null</code>).
	 * @param lineColor      the line color (can be <code>null</code>).
	 * @param lineWidth      the line width in pixels.
	 * @param lineStyle      the line style.
	 */
	public static void drawWorldRectangle(Graphics g, IContainer container, Rectangle2D.Double worldRectangle,
			Color fillColor, Color lineColor, int lineWidth, LineStyle lineStyle) {

		Graphics2D g2 = (Graphics2D) g;

		Rectangle rectangle = new Rectangle();
		container.worldToLocal(rectangle, worldRectangle);

		// fill?
		if (fillColor != null) {
			g2.setColor(fillColor);
			g2.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
		}

		// outline?
		if (lineColor != null) {
			Stroke oldStroke = g2.getStroke();
			Stroke newStroke = GraphicsUtilities.getStroke(lineWidth, lineStyle);
			g2.setStroke(newStroke);
			g2.setColor(lineColor);
			g2.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
			g2.setStroke(oldStroke);
		}

	}
	
	/**
	 * Draw a world oval with a solid, 1 pixel outline.
	 * 
	 * @param g              the graphics context.
	 * @param container      the container on which it is rendered.
	 * @param worldRectangle the world rectangle defining the enclosed oval
	 * @param fillColor      the fill color (can be <code>null</code>).
	 * @param lineColor      the line color (can be <code>null</code>).
	 */
	public static void drawWorldOval(Graphics g, IContainer container, Rectangle2D.Double worldRectangle,
			Color fillColor, Color lineColor) {
		drawWorldOval(g, container, worldRectangle, fillColor, lineColor, 1, LineStyle.SOLID);
	}

	/**
	 * Draw a world rectangle with a solid, 1 pixel outline.
	 * 
	 * @param g              the graphics context.
	 * @param container      the container on which it is rendered.
	 * @param worldRectangle the world rectangle being drawn.
	 * @param fillColor      the fill color (can be <code>null</code>).
	 * @param lineColor      the line color (can be <code>null</code>).
	 */
	public static void drawWorldRectangle(Graphics g, IContainer container, Rectangle2D.Double worldRectangle,
			Color fillColor, Color lineColor) {
		drawWorldRectangle(g, container, worldRectangle, fillColor, lineColor, 1, LineStyle.SOLID);
	}

	/**
	 * Draw a world oval based on an IStyled object.
	 * 
	 * @param g              the graphics context.
	 * @param container      the container on which it is rendered.
	 * @param worldRectangle the world rectangle defining the enclosed oval
	 * @param style          An IStyled object, such as an Item.
	 */
	public static void drawWorldOval(Graphics g, IContainer container, Rectangle2D.Double worldRectangle,
			IStyled style) {
		drawWorldOval(g, container, worldRectangle, style.getFillColor(), style.getLineColor(),
				style.getLineWidth(), style.getLineStyle());
	}
	/**
	 * Draw a world rectangle based on an IStyled object.
	 * 
	 * @param g              the graphics context.
	 * @param container      the container on which it is rendered.
	 * @param worldRectangle the world rectangle being drawn.
	 * @param style          An IStyled object, such as an Item.
	 */
	public static void drawWorldRectangle(Graphics g, IContainer container, Rectangle2D.Double worldRectangle,
			IStyled style) {
		drawWorldRectangle(g, container, worldRectangle, style.getFillColor(), style.getLineColor(),
				style.getLineWidth(), style.getLineStyle());
	}

	/**
	 * Draws outline a highlighted world rectangle.
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param wr        the rectangle being highlighted.
	 * @param color1    one color for the alternating dash.
	 * @param color2    the other color for the alternating dash.
	 */

	public static void drawHighlightedRectangle(Graphics g, IContainer container, Rectangle2D.Double wr, Color color1,
			Color color2) {

		Rectangle rectangle = new Rectangle();
		container.worldToLocal(rectangle, wr);
		GraphicsUtilities.drawHighlightedRectangle(g, rectangle, color1, color2);
	}

	/**
	 * Draw a world polygon.
	 * 
	 * @param g            the graphics context.
	 * @param container    the container on which it is rendered.
	 * @param worldPolygon the world polygon being drawn.
	 * @param fillColor    the fill color (can be <code>null</code>).
	 * @param lineColor    the line color (can be <code>null</code>).
	 * @param lineWidth    the line width in pixels.
	 * @param lineStyle    the line style.
	 */
	public static void drawWorldPolygon(Graphics g, IContainer container, WorldPolygon worldPolygon, Color fillColor,
			Color lineColor, float lineWidth, LineStyle lineStyle) {

		Graphics2D g2 = (Graphics2D) g;

		Polygon polygon = new Polygon();

		container.worldToLocal(polygon, worldPolygon);

		// fill?
		if (fillColor != null) {
			g2.setColor(fillColor);
			g2.fillPolygon(polygon);
		}

		// outline?
		if (lineColor != null) {
			Stroke oldStroke = g2.getStroke();
			Stroke newStroke = GraphicsUtilities.getStroke(lineWidth, lineStyle);
			g2.setStroke(newStroke);
			g2.setColor(lineColor);
			g2.drawPolygon(polygon);
			g2.setStroke(oldStroke);
		}

	}

	/**
	 * Draws a hit based on the Lund Id.
	 * 
	 * @param g            the graphics context.
	 * @param container    the container on which it is rendered.
	 * @param bounds       the world bounds of the hit being drawn.
	 * @param id           the Id. We will set up special drawing for the common
	 *                     particles. The rest will be drawn normally.
	 * @param defFillColor if non matching Id (LundId not found) fill in default
	 *                     color.
	 * @param defLineColor if non matching Id (LundId not found) draw in default
	 *                     color.
	 */
	public static void drawLundHit(Graphics g, IContainer container, Rectangle2D.Double bounds, int id,
			Color defFillColor, Color defLineColor) {

		LundId lundId = LundSupport.getInstance().get(id);
		LundStyle style = lundId.getStyle();

		if (style == null) {
			drawWorldRectangle(g, container, bounds, defFillColor, defFillColor.darker(), 1, LineStyle.SOLID);
		} else {
			drawWorldRectangle(g, container, bounds, style.getFillColor(), style.getFillColor(), 1, LineStyle.SOLID);
		}

		// switch (id) {
		// case 11: //electron
		// defFillColor = Color.red;
		// break;
		//
		// case -11: //positron
		// defFillColor = Color.magenta;
		// break;
		//
		// case 211: //pi +
		// defFillColor = Color.yellow;
		// break;
		//
		// case -211: //pi -
		// defFillColor = darkOrange;
		// break;
		//
		// case 321: //K+
		// defFillColor = Color.green;
		// break;
		//
		// case -321: //K-
		// defFillColor = darkGreen;
		// break;
		//
		// case 13: //mu-
		// defFillColor = Color.cyan;
		// break;
		//
		// case -13: //mu+
		// defFillColor = brown;
		// break;
		//
		// case 2212: //proton
		// defFillColor = Color.blue;
		// break;
		//
		// default: //fill the rect with default colors
		// drawWorldRectangle(g, container, bounds, defFillColor, defLineColor,
		// 1,
		// LineStyle.SOLID);
		//
		// return;
		// }
		//

	}

	/**
	 * Create a color legend component for a specific lund id.
	 * 
	 * @param id the id
	 * @return the component, which will be placed in a legend.
	 */
	private static JComponent getLegendComponent(int id) {
		LundId lundId = LundSupport.getInstance().get(id);
		if (lundId == null) {
			return LundId.unknownLabel;
		} else {
			return lundId.getLabelForLegend();
		}
	}

	/**
	 * Draws a hit based on the Lund Id. This accepts a double Id which is converted
	 * to an int to support GEMC
	 * 
	 * @param g            the graphics context.
	 * @param container    the container on which it is rendered.
	 * @param bounds       the world bounds of the hit being drawn.
	 * @param lundId       the Id. We will set up special drawing for the common
	 *                     particles. The rest will be drawn normally.
	 * @param defFillColor if non matching Id (LundId not found) fill in default
	 *                     color.
	 * @param defLineColor if non matching Id (LundId not found) draw in default
	 *                     color.
	 */
	public static void drawLundHit(Graphics g, IContainer container, Rectangle2D.Double bounds, double lundId,
			Color defFillColor, Color defLineColor) {
		int intId = (int) Math.round(lundId);
		drawLundHit(g, container, bounds, intId, defFillColor, defLineColor);
	}

	/**
	 * Draw ghosted (etched) text at the given world point.
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param x         x coordinate of left of text.
	 * @param y         y coordinate of text baseline.
	 * @param text      the text to write.
	 */
	public static void drawWorldGhostText(Graphics g, IContainer container, double x, double y, String text) {
		drawWorldGhostText(g, container, x, y, text, 0, 0);
	}

	/**
	 * Draw ghosted (etched) text at the given world point.
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param x         x coordinate of left of text.
	 * @param y         y coordinate of text baseline.
	 * @param text      the text to write.
	 * @param dh        additional horizontal offset.
	 * @param dv        additional vertical offset.
	 */
	public static void drawWorldGhostText(Graphics g, IContainer container, double x, double y, String text, int dh,
			int dv) {
		if (text == null) {
			return;
		}

		Point p = new Point();
		container.worldToLocal(p, x, y);
		TextUtilities.drawGhostText(g, text, p.x + dh, p.y + dv);
	}

	/**
	 * Draw text at the given world point.
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param x         x coordinate of left of text.
	 * @param y         y coordinate of text baseline.
	 * @param text      the text to write.
	 * @param dh        additional horizontal offset.
	 * @param dv        additional vertical offset.
	 */
	public static void drawWorldText(Graphics g, IContainer container, double x, double y, String text, int dh,
			int dv) {
		if (text == null) {
			return;
		}

		Point p = new Point();
		container.worldToLocal(p, x, y);
		g.drawString(text, p.x + dh, p.y + dv);
	}

	/**
	 * Draw halo text at the given world point.
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param x         x coordinate of left of text.
	 * @param y         y coordinate of text baseline.
	 * @param text      the text to write.
	 */
	public static void drawWorldHaloText(Graphics g, IContainer container, double x, double y, String text) {
		drawWorldHaloText(g, container, x, y, text, 0, 0);
	}

	/**
	 * Draw halo text at the given world point.
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param x         x coordinate of left of text.
	 * @param y         y coordinate of text baseline.
	 * @param text      the text to write.
	 * @param dh        additional horizontal offset.
	 * @param dv        additional vertical offset.
	 */
	public static void drawWorldHaloText(Graphics g, IContainer container, double x, double y, String text, int dh,
			int dv) {
		if (text == null) {
			return;
		}

		Point p = new Point();
		container.worldToLocal(p, x, y);
		TextUtilities.drawHaloText(g, text, p.x + dh, p.y + dv);

	}

	/**
	 * Draw a world path.
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param path      the path rectangle being drawn.
	 * @param style     the style for drawing.
	 * @param closed    if true, treat as polygon, else treat as polyline
	 * @return the polygon that was drawn
	 */
	public static Polygon drawPath2D(Graphics g, IContainer container, Path2D.Double path, IStyled style,
			boolean closed) {
		return drawPath2D(g, container, path, style.getFillColor(), style.getLineColor(), style.getLineWidth(),
				style.getLineStyle(), closed);
	}

	/**
	 * Draw a world path.
	 * 
	 * @param g         the graphics context.
	 * @param container the container on which it is rendered.
	 * @param path      the path rectangle being drawn.
	 * @param fillColor the fill color (can be <code>null</code>).
	 * @param lineColor the line color (can be <code>null</code>).
	 * @param lineWidth the line width in pixels.
	 * @param lineStyle the line style.
	 * @param closed    if true, treat as polygon, else treat as polyline
	 * @return the polygon that was drawn
	 */
	public static Polygon drawPath2D(Graphics g, IContainer container, Path2D.Double path, Color fillColor,
			Color lineColor, int lineWidth, LineStyle lineStyle, boolean closed) {

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		float flw = lineWidth;
		if (flw == 0) {
			flw = 0.5f;
		}

		Polygon poly = pathToPolygon(container, path);
		if (poly == null) {
			return null;
		}

		// fill?
		if (fillColor != null) {
			g2.setColor(fillColor);
			if (closed) {
				g2.fillPolygon(poly);
			}
		}

		// outline?
		if (lineColor != null) {
			Stroke oldStroke = g2.getStroke();
			Stroke newStroke = GraphicsUtilities.getStroke(flw, lineStyle);
			g2.setStroke(newStroke);
			g2.setColor(lineColor);
			if (closed) {
				g2.drawPolygon(poly);
			} else {
				g2.drawPolyline(poly.xpoints, poly.ypoints, poly.npoints);
			}

			g2.setStroke(oldStroke);
		}
		return poly;
	}

	/**
	 * Get the number of points in a path
	 * 
	 * @param path the path being examined.
	 * @return the number of elementary points (from moveto and lineto)
	 */
	public static int getPathPointCount(Path2D path) {
		if (path == null) {
			return 0;
		}

		int count = 0;

		PathIterator pi = path.getPathIterator(null);

		double coords[] = new double[6];
		while (!pi.isDone()) {
			int type = pi.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				count++;
				break;

			// TODO curves
			}
			pi.next();
		}

		return count;
	}

	/**
	 * Get the convex hull of a path
	 * 
	 * @param path the path in question
	 * @return the convex hull
	 */
	public static Path2D getConvexHull(Path2D path) {
		int count = getPathPointCount(path);
		if (count < 4) {
			return path;
		}

		Point2D.Double wp[] = new Point2D.Double[count];
		int index = 0;
		PathIterator pi = path.getPathIterator(null);

		double coords[] = new double[6];
		while (!pi.isDone()) {
			int type = pi.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				wp[index] = new Point2D.Double(coords[0], coords[1]);
				index++;
				break;

			// TODO curves
			}
			pi.next();
		}

		// now get convex hull
		int n = MathUtilities.getConvexHull(wp);
		if (n < 3) {
			return null;
		}

		Path2D newPath = new Path2D.Double();
		newPath.moveTo(wp[0].x, wp[0].y);
		for (int i = 1; i < n; i++) {
			newPath.lineTo(wp[i].x, wp[i].y);
		}
		newPath.lineTo(wp[0].x, wp[0].y);

		return newPath;
	}

	/**
	 * Convert a path to a collection of points. This is not a general method, it
	 * only works for simple paths of line segments. It does not deal with curves.
	 * 
	 * @param path the path in question
	 * @return a collection of vertices
	 */
	public static Vector<Point2D.Double> getPoints(Path2D path) {
		Vector<Point2D.Double> pointList = new Vector<Point2D.Double>();
		double[] coords = new double[2];
		int numSubPaths = 0;
		for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {
			switch (pi.currentSegment(coords)) {
			case PathIterator.SEG_MOVETO:
				pointList.add(new Point2D.Double(coords[0], coords[1]));
				++numSubPaths;
				break;
			case PathIterator.SEG_LINETO:
				pointList.add(new Point2D.Double(coords[0], coords[1]));
				break;
			case PathIterator.SEG_CLOSE:
				if (numSubPaths > 1) {
					throw new IllegalArgumentException("Path contains multiple subpaths");
				}
				return pointList;
			default:
				throw new IllegalArgumentException("Path contains curves");
			}
		}
		return pointList;
	}

	/**
	 * Get an indexed point of a path.
	 * 
	 * @param index the index of the point we seek, starting at zero.
	 * @param path  the path being examined.
	 * @return the point, or <code>null</code>
	 */
	public static Point2D.Double getPathPointAt(int index, Path2D path) {
		int count = 0;

		PathIterator pi = path.getPathIterator(null);

		double coords[] = new double[6];
		while (!pi.isDone()) {
			int type = pi.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				if (count == index) {
					Point2D.Double wp = new Point2D.Double(coords[0], coords[1]);
					return wp;
				}
				count++;
				break;

			// TODO curves
			}
			pi.next();
		}

		return null;
	}

	/**
	 * Convert a world path to a pixel polygon
	 * 
	 * @param container the container being rendered
	 * @param path      the path being converted
	 * @return the pixel polygon
	 */
	public static Polygon pathToPolygon(IContainer container, Path2D.Double path) {
		if (path == null) {
			return null;
		}

		Polygon poly = null;
		PathIterator pi = path.getPathIterator(null);

		double coords[] = new double[6];
		while (!pi.isDone()) {
			int type = pi.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				if (poly == null) {
					poly = new Polygon();
				}
				Point p = new Point();
				container.worldToLocal(p, coords[0], coords[1]);
				poly.addPoint(p.x, p.y);
				break;

			// TODO curves
			}
			pi.next();
		}

		return poly;
	}

	/**
	 * Counts the needed capacity if you are going to pull out the points an put
	 * them in a collection. Note: for now only handles moveto and lineto.
	 * 
	 * @param path the input path
	 * @return the size needed for a collection
	 */
	public static int neededCapacity(Path2D.Double path) {
		if (path == null) {
			return 0;
		}

		int count = 0;
		PathIterator pi = path.getPathIterator(null);

		double coords[] = new double[6];
		while (!pi.isDone()) {
			int type = pi.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				count++;
				break;

			// TODO curves
			}
			pi.next();
		}
		return count;
	}

	/**
	 * Convert a rectangle into a path.
	 * 
	 * @param wr the input rectangle
	 * @return the path corresponding to the rectangle
	 */
	public static Path2D.Double worldRectangleToPath(Rectangle2D.Double wr) {
		double xmax = wr.getMaxX();
		double ymax = wr.getMaxY();
		Path2D.Double path = new Path2D.Double();
		path.moveTo(wr.x, wr.y);
		path.lineTo(wr.x, ymax);
		path.lineTo(xmax, ymax);
		path.lineTo(xmax, wr.y);
		path.closePath();

		return path;
	}

	/**
	 * Convert a polygon into a path.
	 * 
	 * @param points the input points
	 * @return the path corresponding to the points
	 */
	public static Path2D.Double worldPolygonToPath(Point2D.Double points[]) {

		if (points == null) {
			return null;
		}

		Path2D.Double path = null;
		for (Point2D.Double wp : points) {
			if (path == null) {
				path = new Path2D.Double();
				path.moveTo(wp.x, wp.y);
			} else {
				path.lineTo(wp.x, wp.y);
			}
		}
		return path;
	}

	/**
	 * Computes the intersection of two lines, p1 -> p2, and q1 -> q2.
	 * 
	 * @param p1 the first end point on line 1
	 * @param p2 the second end point on line 1
	 * @param q1 the first end point on line 2
	 * @param q2 the second end point on line 2
	 * @return the intersection of the two lines.
	 */
	public static Point2D.Double intersection(Point2D.Double p1, Point2D.Double p2, Point2D.Double q1,
			Point2D.Double q2) {
		double x1 = p1.x;
		double x2 = p2.x;
		double x3 = q1.x;
		double x4 = q2.x;
		double y1 = p1.y;
		double y2 = p2.y;
		double y3 = q1.y;
		double y4 = q2.y;

		double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (Math.abs(denom) < 1.0e-12) {
			return null; // lines are parallel
		}

		double numer1 = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
		double t1 = numer1 / denom;

		if ((t1 < 0.0) || (t1 > 1.0)) {
			return null;
		}

		double numer2 = (x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3);
		double t2 = numer2 / denom;

		if ((t2 < 0.0) || (t2 > 1.0)) {
			return null;
		}

		// could use either t1 (p1 to p2) or t2 (q1 to q2)
		return new Point2D.Double(p1.x + t1 * (p2.x - p1.x), p1.y + t1 * (p2.y - p1.y));
	}

	/**
	 * Get the for corners of a rectangle as a point array
	 * 
	 * @param wr the rectangle in question
	 * @return the point array
	 */
	public static Point2D.Double[] getPoints(Rectangle2D.Double wr) {
		Point2D.Double points[] = new Point2D.Double[4];
		double x1 = wr.x;
		double x2 = x1 + wr.width;
		double y1 = wr.y;
		double y2 = y1 + wr.height;
		points[0] = new Point2D.Double(x1, y1);
		points[1] = new Point2D.Double(x1, y2);
		points[2] = new Point2D.Double(x2, y2);
		points[3] = new Point2D.Double(x2, y1);
		return points;
	}

	/**
	 * Get donut points given some defining data
	 * 
	 * @param wpc         the center of the donut
	 * @param radiusInner the inner radius
	 * @param radiusOuter the outer radius
	 * @param startAngle  the startAngle in degrees measured like the usual polar
	 *                    angle theta
	 * @param arcAngle    the opening angle COUNTERCLOCKWISE in degrees.
	 * @return the world point array simulating the radarc
	 */
	public static Point2D.Double[] getDonutPoints(Point2D.Double wpc, double radiusInner, double radiusOuter,
			double startAngle, double arcAngle) {

		double theta = Math.toRadians(startAngle);
		double dTheta = Math.toRadians(arcAngle);
		double del = dTheta / (NUMCIRCSTEP - 1);

		Point2D.Double wp[] = new Point2D.Double[2 * NUMCIRCSTEP];

		for (int i = 0; i < NUMCIRCSTEP; i++) {
			int j = 2 * NUMCIRCSTEP - 1 - i;
			double ang = theta + i * del;
			double cos = Math.cos(ang);
			double sin = Math.sin(ang);
			double x1 = wpc.x + radiusInner * cos;
			double y1 = wpc.y + radiusInner * sin;
			double x2 = wpc.x + radiusOuter * cos;
			double y2 = wpc.y + radiusOuter * sin;
			wp[i] = new Point2D.Double(x1, y1);
			wp[j] = new Point2D.Double(x2, y2);
		}

		return wp;
	}

	/**
	 * Get radarc points given some defining data
	 * 
	 * @param wpc      the center of the arc
	 * @param wp1      the point at the end of the first leg. Thus wpc->wp1
	 *                 determine the radius.
	 * @param arcAngle the opening angle COUNTERCLOCKWISE in degrees.
	 * @return the world point array simulating the radarc
	 */
	public static Point2D.Double[] getRadArcPoints(Point2D.Double wpc, Point2D.Double wp1, double arcAngle) {

		double dTheta = Math.toRadians(arcAngle);

		double x = wp1.x - wpc.x;
		double y = wp1.y - wpc.y;

		double del = dTheta / NUMCIRCSTEP;
		Point2D.Double wp[] = new Point2D.Double[NUMCIRCSTEP + 2];
		wp[0] = wpc;
		wp[1] = wp1;

		for (int i = 1; i <= NUMCIRCSTEP; i++) {
			double ang = i * del;
			double sinT = Math.sin(ang);
			double cosT = Math.cos(ang);

			double xp = x * cosT - y * sinT;
			double yp = x * sinT + y * cosT;

			wp[i + 1] = new Point2D.Double(wpc.x + xp, wpc.y + yp);
		}

		return wp;
	}

	/**
	 * Get arc points given some defining data
	 * 
	 * @param wpc      the center of the arc
	 * @param wp1      the point at the end of the first leg. Thus wpc->wp1
	 *                 determine the radius.
	 * @param arcAngle the opening angle COUNTERCLOCKWISE in degrees.
	 * @return the world point array simulating the arc
	 */
	public static Point2D.Double[] getArcPoints(Point2D.Double wpc, Point2D.Double wp1, double arcAngle) {

		double dTheta = Math.toRadians(arcAngle);

		double x = wp1.x - wpc.x;
		double y = wp1.y - wpc.y;

		double del = dTheta / NUMCIRCSTEP;
		Point2D.Double wp[] = new Point2D.Double[NUMCIRCSTEP + 1];
		wp[0] = wp1;

		for (int i = 0; i < NUMCIRCSTEP; i++) {
			double ang = i * del;
			double sinT = Math.sin(ang);
			double cosT = Math.cos(ang);

			double xp = x * cosT - y * sinT;
			double yp = x * sinT + y * cosT;

			wp[i + 1] = new Point2D.Double(wpc.x + xp, wpc.y + yp);
		}

		return wp;
	}

	/**
	 * Create a point from a center with a given radius and angle
	 * 
	 * @param center the center
	 * @param radius the radius
	 * @param theta  the angle measured as the usual polar theta
	 * @return the point at the given radius and angle from the center
	 */
	public static Point2D.Double radiusPoint(Point2D.Double center, double radius, double theta) {
		theta = Math.toRadians(theta);
		double x = center.x + radius * Math.cos(theta);
		double y = center.y + radius * Math.sin(theta);
		return new Point2D.Double(x, y);
	}

	/**
	 * Get a world polygon from a path, using (for now at least) only the MoveTo and
	 * LineTo pats of the path.
	 * 
	 * @param path the path in question
	 * @return the vertex points
	 */
	public static Point2D.Double[] pathToWorldPolygon(Path2D.Double path) {

		if (path == null) {
			return null;
		}

		PathIterator pi = path.getPathIterator(null);

		Vector<Point2D.Double> v = null;

		double coords[] = new double[6];
		while (!pi.isDone()) {
			int type = pi.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				if (v == null) {
					v = new Vector<Point2D.Double>(50, 25);
				}
				v.add(new Point2D.Double(coords[0], coords[1]));
				break;

			// TODO curves
			}
			pi.next();
		}

		if (v == null) {
			return null;
		}

		Point2D.Double points[] = new Point2D.Double[v.size()];
		for (int index = 0; index < v.size(); index++) {
			points[index] = v.elementAt(index);
		}
		return points;

	}

	/**
	 * Set a world polygon from a path, using (for now at least) only the MoveTo and
	 * LineTo pats of the path.
	 * 
	 * @param path the path in question
	 * @param wp   the point array--assumes it is big enough and the points have
	 *             already been created.
	 */
	public static void pathToWorldPolygon(Path2D.Double path, Point2D.Double wp[]) {

		if (path == null) {
			return;
		}

		PathIterator pi = path.getPathIterator(null);

		double coords[] = new double[6];
		int index = 0;
		while (!pi.isDone()) {
			int type = pi.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				wp[index].setLocation(coords[0], coords[1]);
				index++;

				if (index >= wp.length) {
					return;
				}
				break;

			// TODO curves
			}
			pi.next();
		}

	}

	/**
	 * Contains test
	 * 
	 * @param poly the polygon
	 * @param wp   the point to test
	 * @return <code>true</code> if the point is contained
	 */
	public static boolean contains(Point2D.Double poly[], Point2D.Double wp) {
		Path2D.Double path = worldPolygonToPath(poly);
		return path.contains(wp);
	}

	/**
	 * @param poly array of world points
	 * @return the centroid of the polygon. Essentially the center of mass treating
	 *         each vertex as a mass point.
	 */
	public static Point2D.Double getCentroid(Point2D.Double poly[]) {
		double area = area(poly);
		if (Math.abs(area) < 1.0e-12) {
			return null;
		}
		double sumx = 0;
		double sumy = 0;
		for (int i = 0; i < poly.length; i++) {
			Point2D.Double pi = poly[i];
			int j = (i + 1) % poly.length;
			Point2D.Double pj = poly[j];

			double cp = (pi.x * pj.y - pj.x * pi.y);
			sumx += (pi.x + pj.x) * cp;
			sumy += (pi.y + pj.y) * cp;
		}

		double sixa = 6.0 * area;
		return new Point2D.Double(sumx / sixa, sumy / sixa);
	}

	/**
	 * @param path the path whose centroid we want
	 * @return the centroid of the path. Essentially the center of mass treating
	 *         each vertex as a mass point.
	 */
	public static Point2D.Double getCentroid(Path2D.Double path) {
		return getCentroid(pathToWorldPolygon(path));
	}

	/**
	 * Obtain the area of a polygon.
	 * 
	 * @param poly the polygon in question
	 * @return the area.
	 */
	public static double area(Point2D.Double poly[]) {
		if ((poly == null) || (poly.length < 3)) {
			return 0.0;
		}

		double sum = 0;
		for (int i = 0; i < poly.length; i++) {
			Point2D.Double pi = poly[i];
			int j = (i + 1) % poly.length;
			Point2D.Double pj = poly[j];

			sum += (pi.x * pj.y - pj.x * pi.y);
		}

		return 0.5 * sum;
	}

	public static double longestDiagonal(Point2D.Double focus, Point2D.Double poly[]) {
		if ((focus == null) || (poly == null) || (poly.length < 1)) {
			return Double.NaN;
		}

		double longest = 0.0;
		for (Point2D.Double wp : poly) {
			double d = focus.distanceSq(wp);
			if (d > longest) {
				longest = d;
			}
		}
		return Math.sqrt(longest);
	}

	/**
	 * This takes a point and returns the first intersection in finds with a path.
	 * The intersection is with a line drawn from the point along the given azimuth.
	 * This may not be general, but for the case of a "focus" inside the polygon and
	 * not too weird of a polygon it should be fine.
	 * 
	 * @param p0      the focus point, or the start of the line.
	 * @param azimuth the angle in degrees at which the line radiates.
	 * @param path    the path that will be intersected.
	 * @return the intersection point, or <code>null</code>.
	 */
	public static Point2D.Double polygonIntersection(Point2D.Double p0, double azimuth, Path2D.Double path) {
		return polygonIntersection(p0, azimuth, pathToWorldPolygon(path));
	}

	/**
	 * This takes a point and returns the first intersection in finds with a
	 * polygon. The intersection is with a line drawn from the point along the given
	 * azimuth. This may not be general, but for the case of a "focus" inside the
	 * polygon and not too weird of a polygon it should be fine.
	 * 
	 * @param p0      the focus point, or the start of the line.
	 * @param azimuth the angle in degrees at which the line radiates.
	 * @param poly    the polygon that will be intersected.
	 * @return the intersection point, or <code>null</code>.
	 */
	public static Point2D.Double polygonIntersection(Point2D.Double p0, double azimuth, Point2D.Double poly[]) {
		double longest = longestDiagonal(p0, poly);
		if (Double.isNaN(longest)) {
			return null;
		}

		Point2D.Double p1 = new Point2D.Double();
		// get the projected point. Make it twice as long as the longesy
		project(p0, 2.0 * longest, azimuth, p1);

		int n = poly.length;
		for (int i = 0; i < n; i++) {
			Point2D.Double q0 = poly[i];
			Point2D.Double q1 = poly[(i + 1) % n];

			Point2D.Double intersectWP = intersection(p0, p1, q0, q1);
			if (intersectWP != null) {
				return intersectWP;
			}
		}
		return null;
	}

	/**
	 * Project a world point a length and azimuth angle.
	 * 
	 * @param focus    the point being projected
	 * @param distance the distance of the offset.
	 * @param angle    the direction of the offset, in degrees, measured as an
	 *                 azimuth: 0 is north, 90 east, etc. the x axis.
	 * @param wp       holds the result of the operation.
	 */
	public static void project(Point2D.Double focus, double distance, double angle, Point2D.Double wp) {
		angle = Math.toRadians(angle);
		double x = distance * Math.sin(angle);
		double y = distance * Math.cos(angle);
		wp.setLocation(focus.x + x, focus.y + y);
	}

	/**
	 * Simulate an ellipse
	 * 
	 * @param w       the width of the ellipse in km
	 * @param h       the height of the ellipse in km
	 * @param azimuth the rotation of the ellipse in degrees. 0 is north, 90 east,
	 *                etc.
	 * @param numFill the number of points to use to create the ellipse
	 * @param center  the center of the ellipse.
	 * @return an array of points to simulate an ellipse
	 */
	public static Point2D.Double[] getEllipsePoints(double w, double h, double azimuth, int numFill,
			Point2D.Double center) {

		double w2 = w / 2.0;
		double h2 = h / 2.0;

		Point2D.Double wpv[] = new Point2D.Double[4];

		for (int i = 0; i < 4; i++) {
			wpv[i] = new Point2D.Double();
		}

		// get the four "primary" points

		WorldGraphicsUtilities.project(center, h2, 0, wpv[0]);
		WorldGraphicsUtilities.project(center, w2, 90.0, wpv[1]);
		WorldGraphicsUtilities.project(center, h2, 180.0, wpv[2]);
		WorldGraphicsUtilities.project(center, w2, 270.0, wpv[3]);
		center = WorldGraphicsUtilities.getCentroid(wpv);

		int n = 4 * numFill;

		Point2D.Double bp[] = new Point2D.Double[n + 1];

		double ww = wpv[1].distance(wpv[3]);
		double hh = wpv[0].distance(wpv[2]);

		double rx = ww / 2.0;
		double ry = hh / 2.0;

		double deltheta = 90.0 / numFill;

		for (int i = 0; i < n; i++) {
			bp[i] = new Point2D.Double();

			double theta = Math.toRadians(i * deltheta);

			double yy = ry * Math.cos(theta);
			double xx = rx * Math.sin(theta);

			if (xx > 0.0) {
				WorldGraphicsUtilities.project(center, xx, 90.0, bp[i]);
			} else {
				WorldGraphicsUtilities.project(center, -xx, 270.0, bp[i]);
			}
			if (yy > 0.0) {
				WorldGraphicsUtilities.project(bp[i], yy, 0.0, bp[i]);
			} else {
				WorldGraphicsUtilities.project(bp[i], -yy, 180.0, bp[i]);
			}

		}
		bp[n] = new Point2D.Double(bp[0].x, bp[0].y); // force closed

		// rotate?

		if (Math.abs(azimuth) > 1.0e-12) {

			for (int ii = 0; ii < bp.length; ii++) {
				Point2D.Double tmp = bp[ii];
				double gcd = center.distance(tmp);

				double az2 = Point2DSupport.azimuth(center, tmp);
				WorldGraphicsUtilities.project(center, gcd, az2 + azimuth, tmp);
			}
		}

		return bp;

	}

	/**
	 * Obtain roughly the pixel/unit where unit is the length unit of our world
	 * system. This is approximate. The bigger this number is the more zoomed in we
	 * are.
	 * 
	 * @param container the container in question.
	 * @return the rough pixel density, e.g. pixel/cm is cm is the world unit.
	 */
	public static double getMeanPixelDensity(IContainer container) {
		Rectangle b = container.getComponent().getBounds();
		if ((b == null) || (b.width < 3) || (b.height < 3)) {
			return 0.0;
		}

		Point p0 = new Point(0, 0);
		Point p1 = new Point(b.width, b.height);

		Point2D.Double wp0 = new Point2D.Double();
		Point2D.Double wp1 = new Point2D.Double();

		container.localToWorld(p0, wp0);
		container.localToWorld(p1, wp1);

		double dist = Math.max(TINY, wp0.distance(wp1));

		double pixdist = Math.sqrt(p1.x * p1.x + p1.y * p1.y);
		return pixdist / dist;
	}

}
