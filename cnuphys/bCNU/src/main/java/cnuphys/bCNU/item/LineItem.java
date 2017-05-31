package cnuphys.bCNU.item;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.MathUtilities;

public class LineItem extends AItem {

	/**
	 * Create a world line object.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param wp0
	 *            start of the line.
	 * @param wp1
	 *            end of the line of the line.
	 */
	public LineItem(LogicalLayer layer, Point2D.Double wp0, Point2D.Double wp1) {
		super(layer);
		_line = new Line2D.Double(wp0, wp1);
		_focus = new Point2D.Double(0.5 * (wp0.x + wp1.x),
				0.5 * (wp0.y + wp1.y));
	}

	/**
	 * Check whether the (rendered) item contains the given screen point.
	 * 
	 * @param container
	 *            the graphical container rendering the item.
	 * @param screenPoint
	 *            a pixel location.
	 * @return <code>true</code> if the item, as rendered on the given
	 *         container, contains the given screen point.
	 */
	@Override
	public boolean contains(IContainer container, Point screenPoint) {
		Point2D.Double wp = new Point2D.Double();
		container.localToWorld(screenPoint, wp);

		// get intersection
		Point2D.Double wpi = new Point2D.Double();
		MathUtilities.perpendicularIntersection(_line.x1, _line.y1, _line.x2,
				_line.y2, wp, wpi);

		Point pi = new Point();
		container.worldToLocal(pi, wpi);

		return (Math.abs(screenPoint.x - pi.x) + Math.abs(screenPoint.y - pi.y)) < 20;
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
		WorldGraphicsUtilities.drawWorldLine(g, container, _line.x1, _line.y1,
				_line.x2, _line.y2, _style);
	}

	/**
	 * Checks whether the item should be drawn. This is an additional check,
	 * beyond the simple visibility flag check. For example, it might check
	 * whether the item intersects the area being drawn.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 * @return <code>true</code> if the item passes any and all tests, and
	 *         should be drawn.
	 */
	@Override
	public boolean shouldDraw(Graphics g, IContainer container) {
		Rectangle r = WorldGraphicsUtilities.getBounds(container, _line.x1,
				_line.y1, _line.x2, _line.y2);
		return container.getComponent().getBounds().intersects(r);
	}

	/**
	 * Obtain the selection points used to indicate this item is selected.
	 * 
	 * @return the selection points used to indicate this item is selected.
	 */
	@Override
	public Point[] getSelectionPoints(IContainer container) {
		Point points[] = new Point[2];
		points[0] = new Point();
		points[1] = new Point();
		container.worldToLocal(points[0], _line.x1, _line.y1);
		container.worldToLocal(points[1], _line.x2, _line.y2);
		return points;
	}

	/**
	 * A modification such as a drag, resize or rotate is continuing.
	 */
	@Override
	public void modify() {
		switch (_modification.getType()) {
		case DRAG:
			_line = (Line2D.Double) (_modification.getStartLine().clone());

			Point2D.Double swp = _modification.getStartWorldPoint();
			Point2D.Double cwp = _modification.getCurrentWorldPoint();
			double dx = cwp.x - swp.x;
			double dy = cwp.y - swp.y;
			_line.x1 += dx;
			_line.y1 += dy;
			_line.x2 += dx;
			_line.y2 += dy;

			// fix focus
			updateFocus();
			break;

		case ROTATE:
			break;

		case RESIZE:
			_line = (Line2D.Double) (_modification.getStartLine().clone());
			swp = _modification.getStartWorldPoint();
			cwp = _modification.getCurrentWorldPoint();
			dx = cwp.x - swp.x;
			dy = cwp.y - swp.y;

			if (_modification.getSelectIndex() == 0) {
				_line.x1 += dx;
				_line.y1 += dy;
			} else {
				_line.x2 += dx;
				_line.y2 += dy;
			}

			// fix focus
			updateFocus();
			break;
		}
		setDirty(true);
		_modification.getContainer().refresh();
	}

	// called by the modify method to tell the item to update its focus.
	// (because the item was dragged.) The default method is the path centroid.
	protected void updateFocus() {
		_focus = new Point2D.Double(0.5 * (_line.x1 + _line.x2),
				0.5 * (_line.y1 + _line.y2));
	}

	/**
	 * Get the world bounding rectangle of the item.
	 * 
	 * @return the world box containing the item.
	 */
	@Override
	public Rectangle2D.Double getWorldBounds() {
		if (_line == null) {
			return null;
		}
		Rectangle2D r2d = _line.getBounds2D();
		return new Rectangle2D.Double(r2d.getX(), r2d.getY(), r2d.getWidth(),
				r2d.getHeight());
	}

}
