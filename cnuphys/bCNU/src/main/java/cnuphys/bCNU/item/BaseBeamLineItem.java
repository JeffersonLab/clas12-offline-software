package cnuphys.bCNU.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.X11Colors;

public class BaseBeamLineItem extends LineItem {

	// colors used to draw the beamline
	private static final Color _colors[] = { Color.black,
			X11Colors.getX11Color("Purple"), Color.lightGray,
			X11Colors.getX11Color("Violet"), Color.black, };

	/**
	 * Create a beamline item which is a glorified line.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 */
	public BaseBeamLineItem(LogicalLayer layer) {
		super(layer, new Point2D.Double(-1000.0, 0.0), new Point2D.Double(
				1000.0, 0.0));
		setName("Beamline");
	}

	/**
	 * Custom drawer for the item.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		Point p0 = new Point();
		Point p1 = new Point();

		Point2D.Double wp0 = new Point2D.Double(_line.x1, _line.y1);
		Point2D.Double wp1 = new Point2D.Double(_line.x2, _line.y2);

		WorldGraphicsUtilities.getPixelEnds(container, p0, p1, wp0, wp1);
		for (int i = 0; i < 5; i++) {
			g.setColor(_colors[i]);
			int y = p0.y + i - 2;
			g.drawLine(p0.x, y, p1.x, y);
		}
	}

}
