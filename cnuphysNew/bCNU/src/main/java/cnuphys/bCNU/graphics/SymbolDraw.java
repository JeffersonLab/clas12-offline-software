package cnuphys.bCNU.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;

import cnuphys.bCNU.graphics.style.IStyled;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.style.SymbolType;

public class SymbolDraw {

	/**
	 * Draws the appropriate symbol at the provided screen location.
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the x screen coordinate.
	 * @param y
	 *            the y screen coordinate.
	 * @param style
	 *            the drawing style to use.
	 */

	public static void drawSymbol(Graphics g, int x, int y, IStyled style) {

		drawSymbol(g, x, y, style.getSymbolType(), style.getSymbolSize(),
				style.getLineColor(), style.getFillColor());
	}

	/**
	 * Draws the appropriate symbol at the provided screen location.
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the x screen coordinate.
	 * @param y
	 *            the y screen coordinate.
	 * @param style
	 *            the drawing style to use.
	 */

	public static void drawGhostSymbol(Graphics g, int x, int y, IStyled style) {

		drawSymbol(g, x, y + 1, style.getSymbolType(), style.getSymbolSize(),
				Color.white, style.getFillColor());
		drawSymbol(g, x, y, style.getSymbolType(), style.getSymbolSize(),
				style.getLineColor(), style.getFillColor());
		drawSymbol(g, x, y - 1, style.getSymbolType(), style.getSymbolSize(),
				style.getLineColor(), style.getFillColor());
	}

	/**
	 * Draws the appropriate symbol at the provided screen location.
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the x screen coordinate.
	 * @param y
	 *            the y screen coordinate.
	 * @param symbol
	 *            the symbol to use.
	 * @param symbolSize
	 *            the size of the symbol in pixels--typically around 8.
	 * @param lineColor
	 *            the outline color.
	 * @param fillColor
	 *            the fill color, which is not relevant for some symbols.
	 */
	public static void drawSymbol(Graphics g, int x, int y, SymbolType symbol,
			int symbolSize, Color lineColor, Color fillColor) {

		if (symbol == SymbolType.NOSYMBOL) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		Stroke oldStroke = g2.getStroke();
		Stroke newStroke = GraphicsUtilities.getStroke(1, LineStyle.SOLID);
		g2.setStroke(newStroke);

		int s2 = symbolSize / 2;

		switch (symbol) {
		case SQUARE:
			drawRectangle(g, x, y, s2, s2, lineColor, fillColor);
			break;

		case CIRCLE:
			drawOval(g, x, y, s2, s2, lineColor, fillColor);
			break;

		case DIAMOND:
			drawDiamond(g, x, y, s2, lineColor, fillColor);
			break;

		case CROSS:
			drawCross(g, x, y, s2, lineColor);
			break;

		case UPTRIANGLE:
			drawUpTriangle(g, x, y, s2, lineColor, fillColor);
			break;

		case DOWNTRIANGLE:
			drawDownTriangle(g, x, y, s2, lineColor, fillColor);
			break;
			
		case DAVID:
			drawDavid(g, x, y, s2, lineColor, fillColor);
			break;

		case X:
			drawX(g, x, y, s2, lineColor);
			break;
			
		case NOSYMBOL:
			break;

		}
		
		g2.setStroke(oldStroke);

	}

	/**
	 * Draw a simple rectangle
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the horizontal center
	 * @param y
	 *            the vertical center
	 * @param w2
	 *            the half width
	 * @param h2
	 *            the half height
	 * @param lc
	 *            tThe line color
	 * @param fc
	 *            tThe fillcolor
	 */
	public static void drawRectangle(Graphics g, int x, int y, int w2, int h2,
			Color lc, Color fc) {

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
	 * Draw a simple oval
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the horizontal center
	 * @param y
	 *            the vertical center
	 * @param w2
	 *            the half width
	 * @param h2
	 *            the half height
	 * @param lc
	 *            the line color
	 * @param fc
	 *            The fillcolor
	 */
	public static void drawOval(Graphics g, int x, int y, int w2, int h2,
			Color lc, Color fc) {

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
	 * Draw a simple up triangle
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the horizontal center
	 * @param y
	 *            the vertical center
	 * @param s2
	 *            the half-width
	 * @param lc
	 *            the line color
	 * @param fc
	 *            the fillcolor
	 */
	public static void drawUpTriangle(Graphics g, int x, int y, int s2,
			Color lc, Color fc) {

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
	 * Draw the david symbol
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the horizontal center
	 * @param y
	 *            the vertical center
	 * @param s2
	 *            the half-width
	 * @param lc
	 *            the line color
	 * @param fc
	 *            tThe fillcolor
	 */
	public static void drawDavid(Graphics g, int x, int y, int s2,
			Color lc, Color fc) {
		int l = x - s2;
		int t = y - s2;
		int r = x + s2;
		int b = y + s2;
		Polygon poly = new Polygon();
		poly.addPoint(l, t);
		poly.addPoint(r, t);
		poly.addPoint(x+1, y);
		poly.addPoint(r, b);
		poly.addPoint(l, b);
		poly.addPoint(x-1, y);
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
	 * Draw a simple down triangle
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the horizontal center
	 * @param y
	 *            the vertical center
	 * @param s2
	 *            the half-width
	 * @param lc
	 *            the line color
	 * @param fc
	 *            tThe fillcolor
	 */
	public static void drawDownTriangle(Graphics g, int x, int y, int s2,
			Color lc, Color fc) {

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
	 * Draw a simple cross
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the horizontal center
	 * @param y
	 *            the vertical center
	 * @param s2
	 *            the half-width
	 * @param lc
	 *            the line color
	 */
	public static void drawCross(Graphics g, int x, int y, int s2, Color lc) {

		if (lc != null) {
			g.setColor(lc);
			g.drawLine(x - s2, y, x + s2, y);
			g.drawLine(x, y - s2, x, y + s2);
		}
	}

	/**
	 * Draw a simple X symbol.
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the horizontal center
	 * @param y
	 *            the vertical center
	 * @param s2
	 *            the half-width width
	 * @param lc
	 *            the line color
	 */
	public static void drawX(Graphics g, int x, int y, int s2, Color lc) {

		if (lc != null) {
			g.setColor(lc);
			g.drawLine(x - s2, y - s2, x + s2, y + s2);
			g.drawLine(x - s2, y + s2, x + s2, y - s2);
		}
	}

	/**
	 * Draw a simple diamond
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the horizontal center
	 * @param y
	 *            the vertical center
	 * @param s2
	 *            the half-width
	 * @param lc
	 *            the line color
	 * @param fc
	 *            the fillcolor
	 */
	public static void drawDiamond(Graphics g, int x, int y, int s2, Color lc,
			Color fc) {

		// this will ensure all symbols correct size
		if (lc == null) {
			lc = fc;
		}

		int l = x - s2;
		int t = y - s2;
		int r = x + s2;
		int b = y + s2;
		Polygon poly = new Polygon();
		poly.addPoint(l, y);
		poly.addPoint(x, t);
		poly.addPoint(r, y);
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

}
