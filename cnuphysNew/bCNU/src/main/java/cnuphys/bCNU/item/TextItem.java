package cnuphys.bCNU.item;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.Snippet;
import cnuphys.bCNU.util.TextUtilities;
import cnuphys.bCNU.util.UnicodeSupport;

public class TextItem extends RectangleItem {

	// makes the bounds bigger than the text by a bit
	private static final int MARGIN = 4;

	// twice the margin
	private static final int MARGIN2 = 2 * MARGIN;

	// default font
	private static final Font _defaultFont = Fonts.commonFont(Font.PLAIN, 12);

	// font for rendering
	private Font _font = _defaultFont;

	// text being rendered
	private String _text;

	// text foreground
	private Color _textColor = Color.black;

	/**
	 * 
	 * @param layer
	 *            the layer which will hold the item.
	 * @param location
	 *            the location of the lower left
	 * @param font
	 *            the font to use.
	 * @param text
	 *            the text to display.
	 * @param textColor
	 *            the text foreground color.
	 * @param fillColor
	 *            the text background color.
	 * @param lineColor
	 *            the border color
	 */
	public TextItem(LogicalLayer layer, Point2D.Double location, Font font,
			String text, Color textColor, Color fillColor, Color lineColor) {
		super(layer, new Rectangle2D.Double(location.x, location.y, 1, 1));
		setFont(font);
		setText(text);
		_textColor = textColor;
		_style.setFillColor(fillColor);
		_style.setLineColor(lineColor);

		_focus = location;
		_resizePolicy = ResizePolicy.SCALEONLY;
	}

	/**
	 * Custom drawer for the text item.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		
		Point2D.Double oldFocus = _focus;
		setPath(getPoints(container));
		_focus = oldFocus;
		super.drawItem(g, container);

		if (_text == null) {
			return;
		}

		Point p = getFocusPoint(container);
		g.setColor((_textColor != null) ? _textColor : Color.black);

		GraphicsUtilities.drawSnippets(g, p.x, p.y, _font, _text,
				container.getComponent(), getAzimuth());
		g.setFont(_font);
	}

	/**
	 * Get the size of the text, including an invisible border of thickness
	 * MARGIN all around.
	 * 
	 * @return the size of the text, including an invisible border of thickness
	 *         MARGIN all around.
	 */
	public static Dimension sizeText(Component component, Font font, String text) {
		if (text == null) {
			return null;
		}

		int width = 0;
		int height = 0;
		Vector<Snippet> snippets = Snippet.getSnippets(font, text, component);
		for (Snippet s : snippets) {
			Dimension size = s.size(component);
			width = Math.max(width, s.getDeltaX() + size.width);
			height = Math.max(height, s.getDeltaY() + size.height);
		}

		return new Dimension(width + MARGIN2, height + MARGIN2);
	}

	/**
	 * @return the font
	 */
	public Font getFont() {
		return _font;
	}

	/**
	 * @param font
	 *            the font to set
	 */
	public void setFont(Font font) {
		_font = font;
		if (_font == null) {
			_font = _defaultFont;
		}
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return _text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		_text = UnicodeSupport.specialCharReplace(text);
		_text = text;
	}

	/**
	 * Get the text foreground color
	 * 
	 * @return the text foreground color
	 */
	public Color getTextColor() {
		return _textColor;
	}

	/**
	 * Set the text foreground color
	 * 
	 * @param textForeground
	 *            the text foreground color to set
	 */
	public void setTextColor(Color textForeground) {
		_textColor = textForeground;
	}

	/**
	 * Get the rotation point
	 * 
	 * @param container
	 *            the container bing rendered
	 * @return the rotation point where rotations are initiated
	 */
	@Override
	public Point getRotatePoint(IContainer container) {
		if (!isRotatable()) {
			return null;
		}

		if (_lastDrawnPolygon != null) {
			int x = (_lastDrawnPolygon.xpoints[1] + _lastDrawnPolygon.xpoints[2]) / 2;
			int y = (_lastDrawnPolygon.ypoints[1] + _lastDrawnPolygon.ypoints[2]) / 2;
			return new Point(x, y);
		}
		return null;
	}

	// get the world rectangle bounds
	private static Rectangle2D.Double getWorldRectangle(IContainer container,
			Point2D.Double location, Font font, String text) {
		
		Dimension size = sizeText(container.getComponent(), font, text);
		FontMetrics fm = container.getComponent().getFontMetrics(font);
		Point p = new Point();
		container.worldToLocal(p, location);
		Rectangle r = new Rectangle(p.x - MARGIN,
				p.y - fm.getAscent() - MARGIN, size.width, size.height);
		Rectangle2D.Double wr = new Rectangle2D.Double();
		container.localToWorld(r, wr);
		return wr;
	}

	// get the corners
	private Point2D.Double[] getPoints(IContainer container) {
		// step one: get unrotated worlds rect
		Rectangle2D.Double wr = getWorldRectangle(container, _focus, _font,
				_text);

		// step two: get rectangle points
		Point2D.Double points[] = WorldGraphicsUtilities.getPoints(wr);

		// rotate ?
		if (Math.abs(getAzimuth()) > 0.001) {
			AffineTransform at = AffineTransform.getRotateInstance(
					Math.toRadians(-getAzimuth()), _focus.x, _focus.y);
			for (Point2D.Double wp : points) {
				at.transform(wp, wp);
			}
		}
		return points;
	}

	@Override
	public void stopModification() {
		_path = WorldGraphicsUtilities
				.worldPolygonToPath(getPoints(getContainer()));
		super.stopModification();
	}

	/**
	 * Simple scaling of the object.
	 */
	@Override
	protected void scale() {
		_path = (Path2D.Double) (_modification.getStartPath().clone());
		Point2D.Double startPoint = _modification.getStartWorldPoint();
		Point2D.Double currentPoint = _modification.getCurrentWorldPoint();

		Point2D.Double center = WorldGraphicsUtilities.getCentroid(_path);

		Point2D.Double startFocus = _modification.getStartFocus();
		double oldFocusDx = center.x - startFocus.x;
		double oldFocusDy = center.y - startFocus.y;

		double scale = currentPoint.distance(center)
				/ startPoint.distance(center);
		AffineTransform at = AffineTransform.getTranslateInstance(center.x,
				center.y);
		at.concatenate(AffineTransform.getScaleInstance(scale, scale));
		at.concatenate(AffineTransform.getTranslateInstance(-center.x,
				-center.y));
		_path.transform(at);

		_focus.x = center.x - scale * oldFocusDx;
		_focus.y = center.y - scale * oldFocusDy;

		// updateFocus();

		// change font size?

		if (this._lastDrawnPolygon != null) {
			int x0 = _lastDrawnPolygon.xpoints[0];
			int x1 = _lastDrawnPolygon.xpoints[1];
			int x2 = _lastDrawnPolygon.xpoints[2];

			int y0 = _lastDrawnPolygon.ypoints[0];
			int y1 = _lastDrawnPolygon.ypoints[1];
			int y2 = _lastDrawnPolygon.ypoints[2];

			double delx = x2 - x1;
			double dely = y2 - y1;
			int w = (int) Math.sqrt(delx * delx + dely * dely);

			delx = x1 - x0;
			dely = y1 - y0;

			// get present size requirement
			Dimension size = sizeText(getContainer().getComponent(), _font,
					_text);

			if (size.width > w) {
				while (size.width > w) {
					_font = TextUtilities.nextSmallerFont(_font, 1);
					size = sizeText(getContainer().getComponent(), _font, _text);
				}
			} else if (size.width < w) {
				while (size.width <= w) {
					_font = TextUtilities.nextBiggerFont(_font, 1);
					size = sizeText(getContainer().getComponent(), _font, _text);
				}
				_font = TextUtilities.nextSmallerFont(_font, 1);
			}

		}
	}

}
