package cnuphys.bCNU.drawable;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.Fonts;

/**
 * Draws a little 2D coordinate system indicator
 * 
 * @author heddle
 * 
 */
public class CoordinateSystemDrawable extends DrawableAdapter {

	// fill color
	private static final Color _fillColor = new Color(255, 255, 255, 128);

	// small font for labels
	private static Font smallFont = Fonts.commonFont(Font.PLAIN, 10);

	// margin off the edge
	private static final int _margin = 4;

	// canvas size
	private int _size;

	// label for x axis
	private String _xlabel;

	// label for y axis
	private String _ylabel;

	/**
	 * Create a little coordinate system indicator. Not very general-- assumes z
	 * axis is to the right and x-y plane perpendicular ro screen with x
	 * vertical if phi = 0.
	 * 
	 * @param size
	 *            the canvas size in pixels
	 * @param xlabel
	 *            the x axis label
	 * @param ylabel
	 *            the y axis label
	 */
	public CoordinateSystemDrawable(int size, String xlabel, String ylabel) {
		_size = size;
		_xlabel = xlabel;
		_ylabel = ylabel;
	}

	/**
	 * The drawing method
	 * 
	 * @param g
	 *            the graphics context
	 * @param container
	 *            the rendering container
	 */
	@Override
	public void draw(Graphics g, IContainer container) {
		// place in bottom left of container

		Rectangle b = container.getComponent().getBounds();

		int gap = 8;
		int x = _margin;
		int y = b.height - _margin - _size;

		GraphicsUtilities.drawSimple3DRect(g, x, y, _size, _size, _fillColor,
				true);
		g.setColor(Color.black);
		g.setFont(smallFont);

		FontMetrics fm = container.getComponent().getFontMetrics(smallFont);

		int x0 = x + gap;
		int y0 = b.height - _margin - gap;

		// horizontal axis
		int sw = fm.stringWidth(_xlabel);
		int x1 = x + _size - sw - gap;
		g.drawLine(x0, y0, x1, y0);
		g.drawString(_xlabel, x1 + 2, y0 + fm.getHeight() / 2 - 2);

		// vertical axis
		sw = fm.stringWidth(_ylabel);
		int y1 = y + _margin + fm.getHeight();
		g.drawLine(x0, y0, x0, y1);
		g.drawString(_ylabel, x0 - sw / 2, y1 - 4);

	}
}
