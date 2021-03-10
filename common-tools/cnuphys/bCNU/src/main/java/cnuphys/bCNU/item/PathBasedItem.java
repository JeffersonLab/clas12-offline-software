/**
 * 
 */
package cnuphys.bCNU.item;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;

public class PathBasedItem extends AItem {

	// workspace
	private Point2D.Double workPoint = new Point2D.Double();

	/**
	 * Create an object that is based on a java Path2D object.
	 * 
	 * @param layer the Layer this item is on.
	 */
	public PathBasedItem(LogicalLayer layer) {
		super(layer);
	}

	/**
	 * Custom drawer for the item.
	 * 
	 * @param g         the graphics context.
	 * @param container the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		// TODO use dirty. If the item is not dirty, should be able to draw
		// the _lastDrawnPolygon directly;
		_lastDrawnPolygon = WorldGraphicsUtilities.drawPath2D(g, container, _path, _style, true);

	}

	/**
	 * Checks whether the item should be drawn. This is an additional check, beyond
	 * the simple visibility flag check. For example, it might check whether the
	 * item intersects the area being drawn.
	 * 
	 * @param g         the graphics context.
	 * @param container the graphical container being rendered.
	 * @return <code>true</code> if the item passes any and all tests, and should be
	 *         drawn.
	 */
	@Override
	public boolean shouldDraw(Graphics g, IContainer container) {
//		if (_path == null) {
//			return false;
//		}

		if (WorldGraphicsUtilities.getPathPointCount(_path) == 1) {
			Rectangle spr = singlePointBounds(container);
			return container.getComponent().getBounds().intersects(spr);
		}

		Rectangle2D.Double wr = getWorldBounds();
		Rectangle r = new Rectangle();
		container.worldToLocal(r, wr);

		Rectangle b = container.getComponent().getBounds();
		b.x = 0;
		b.y = 0;

		boolean shouldDraw = b.intersects(r);
		if (!shouldDraw) {
			_lastDrawnPolygon = null;
		}

		return shouldDraw;
	}

	/**
	 * Check whether the (rendered) item contains the given screen point.
	 * 
	 * @param container   the graphical container rendering the item.
	 * @param screenPoint a pixel location.
	 * @return <code>true</code> if the item, as rendered on the given container,
	 *         contains the given screen point.
	 */
	@Override
	public boolean contains(IContainer container, Point screenPoint) {

		if (_path == null) {
			return false;
		}
		if (inASelectRect(container, screenPoint)) {
			return true;
		}
		container.localToWorld(screenPoint, workPoint);
		return _path.contains(workPoint);
	}

	/**
	 * get the bounding rect.
	 * 
	 * @param container the container being rendered
	 * @return the bounding box.
	 */
	@Override
	public Rectangle getBounds(IContainer container) {
		if (_path == null) {
			return null;
		}

		int count = WorldGraphicsUtilities.getPathPointCount(_path);

		if (count < 1) {
			return null;
		}
		if (count == 1) {
			return singlePointBounds(container);
		}

		Rectangle2D.Double wr = getWorldBounds();
		Rectangle r = new Rectangle();
		container.worldToLocal(r, wr);
		return r;
	}

	/**
	 * Get the rotation point
	 * 
	 * @param container the container bing rendered
	 * @return the rotation point where rotations are initiated
	 */
	@Override
	public Point getRotatePoint(IContainer container) {

		if (!isRotatable()) {
			return null;
		}

		Point2D.Double wp = WorldGraphicsUtilities.polygonIntersection(_focus, getAzimuth(), _path);

		// now try to extend by 15 pixels
		if (_focus != null) {
			double dist = _focus.distance(wp);
			Point pf = this.getFocusPoint(container);
			Point p1 = new Point();
			container.worldToLocal(p1, wp);
			double dx = p1.x - pf.x;
			double dy = p1.y - pf.y;
			double pixlen = Math.sqrt(dx * dx + dy * dy);

			if (pixlen > 1) {
				dist += 15.0 * dist / pixlen;
				WorldGraphicsUtilities.project(_focus, dist, getAzimuth(), wp);
			}
		}

		if (wp == null) {
			return null;
		}
		Point pp = new Point();
		container.worldToLocal(pp, wp);
		return pp;
	}

	/**
	 * A modification such as a drag, resize or rotate is continuing.
	 */
	@Override
	public void modify() {

		// was a key mod pressed?
		boolean keymod = _modification.isShift() || _modification.isControl();

		switch (_modification.getType()) {

		case DRAG:
			if (!isDraggable()) {
				break;
			}
			_path = (Path2D.Double) (_modification.getStartPath().clone());

			Point2D.Double swp = _modification.getStartWorldPoint();
			Point2D.Double cwp = _modification.getCurrentWorldPoint();
			double dx = cwp.x - swp.x;
			double dy = cwp.y - swp.y;
			AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
			_path.transform(at);

			if (_secondaryPoints != null) {
				Path2D.Double path2 = (Path2D.Double) _modification.getSecondaryPath().clone();
				path2.transform(at);
				WorldGraphicsUtilities.pathToWorldPolygon(path2, _secondaryPoints);
			}

			// fix focus
			Point2D.Double sf = _modification.getStartFocus();

			if ((sf != null) && (_focus != null)) {
				at.transform(sf, _focus);
			}
			// updateFocus();
			break;

		case ROTATE:

			if (WorldGraphicsUtilities.getPathPointCount(_path) < 2) {
				return;
			}

			_path = (Path2D.Double) (_modification.getStartPath().clone());

			Point p1 = _modification.getStartMousePoint();
			Point vertex = _modification.getStartFocusPoint();
			Point p2 = _modification.getCurrentMousePoint();
			Point2D.Double anchor = _modification.getStartFocus();
			double angle = threePointAngle(p1, vertex, p2);

			// trial, limit to int values
			angle = ((int) angle);

			// minus is for cw v. ccw
			at = AffineTransform.getRotateInstance(Math.toRadians(-angle), anchor.x, anchor.y);
			_path.transform(at);

			if (_secondaryPoints != null) {
				Path2D.Double path2 = (Path2D.Double) _modification.getSecondaryPath().clone();
				path2.transform(at);
				WorldGraphicsUtilities.pathToWorldPolygon(path2, _secondaryPoints);
			}

			setAzimuth(_modification.getStartAzimuth() + angle);
			break;

		case RESIZE:

			if (WorldGraphicsUtilities.getPathPointCount(_path) < 2) {
				return;
			}

			// if shifted we do a simple scaling.
			// otherwise the subclass has to figure out the right thing to do
			if (keymod || (_resizePolicy == ResizePolicy.SCALEONLY)) {
				scale();
			} else {
				reshape();
			}
			break;
		}
		setDirty(true);
		_modification.getContainer().refresh();
	}

	/**
	 * Rotate the item
	 * 
	 * @param angle the angle in degrees
	 */
	public void rotate(double angle) {
		if (Math.abs(angle) < 0.05) {
			return;
		}
		double azim = getAzimuth();
		Point2D.Double anchor = WorldGraphicsUtilities.getCentroid(_path);

		// minus is for cw v. ccw
		AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(-angle), anchor.x, anchor.y);
		_path.transform(at);

		if (_secondaryPoints != null) {
			Path2D.Double path2 = (Path2D.Double) _modification.getSecondaryPath().clone();
			path2.transform(at);
			WorldGraphicsUtilities.pathToWorldPolygon(path2, _secondaryPoints);
		}

		setAzimuth(azim + angle);
	}

	/**
	 * Simple scaling of the object.
	 */
	protected void scale() {
		_path = (Path2D.Double) (_modification.getStartPath().clone());
		Point2D.Double startPoint = _modification.getStartWorldPoint();
		Point2D.Double currentPoint = _modification.getCurrentWorldPoint();
		double scale = currentPoint.distance(_focus) / startPoint.distance(_focus);
		AffineTransform at = AffineTransform.getTranslateInstance(_focus.x, _focus.y);
		at.concatenate(AffineTransform.getScaleInstance(scale, scale));
		at.concatenate(AffineTransform.getTranslateInstance(-_focus.x, -_focus.y));
		_path.transform(at);

		if (_secondaryPoints != null) {
			Path2D.Double path2 = (Path2D.Double) _modification.getSecondaryPath().clone();
			path2.transform(at);
			WorldGraphicsUtilities.pathToWorldPolygon(path2, _secondaryPoints);
		}

	}

	/**
	 * Reshape the items based on the modification. This is type dependent-- for
	 * example a rectangle will want to keep looking like a rectangle. The default
	 * implementation is to scale
	 */
	protected void reshape() {
		scale();
	}

	// called by the modify method to tell the item to update its focus.
	// (because the item was dragged.) The default method is the path centroid.
	protected void updateFocus() {
		setFocus(WorldGraphicsUtilities.getCentroid(_path));
	}

	/**
	 * Give two points (presumably the start and current mouse positions) and a
	 * vertex (presumable the focus as a pixel point) this computes and angle. Give
	 * the assumptions above, it is how much the original path will need to be
	 * rotated.
	 * 
	 * @param p1     one end point
	 * @param vertex the vertex point
	 * @param p2     the other end point
	 * @return the angle in degrees.
	 */
	protected double threePointAngle(Point p1, Point vertex, Point p2) {
		double ax = p1.x - vertex.x;
		double ay = p1.y - vertex.y;
		double bx = p2.x - vertex.x;
		double by = p2.y - vertex.y;

		double a = Math.sqrt(ax * ax + ay * ay);
		if (a < 1.0e-10) {
			return Double.NaN;
		}
		double b = Math.sqrt(bx * bx + by * by);
		if (b < 1.0e-10) {
			return Double.NaN;
		}

		double adotb = ax * bx + ay * by;
		double acrossb = ax * by - ay * bx;

		double ang = Math.toDegrees(Math.acos(adotb / (a * b)));
		if (acrossb < 0.0) {
			ang = 360.0 - ang;
		}
		return ang;
	}

	/**
	 * Get the world bounding rectangle of the item.
	 * 
	 * @return the world box containing the item.
	 */
	@Override
	public Rectangle2D.Double getWorldBounds() {
		if (_path == null) {
			return null;
		}
		Rectangle2D r2d = _path.getBounds2D();
		return new Rectangle2D.Double(r2d.getX(), r2d.getY(), r2d.getWidth(), r2d.getHeight());
	}

	/**
	 * Get the bounds if the path contains only a single point
	 * 
	 * @param container the container being rendered.
	 * @return the bounds if the path contains only a single point
	 */
	protected Rectangle singlePointBounds(IContainer container) {
		Point2D.Double wp = WorldGraphicsUtilities.getPathPointAt(0, _path);
		Point pp = new Point();
		container.worldToLocal(pp, wp);
		Rectangle r = new Rectangle(pp.x - 8, pp.y - 8, 16, 16);
		return r;
	}

}
