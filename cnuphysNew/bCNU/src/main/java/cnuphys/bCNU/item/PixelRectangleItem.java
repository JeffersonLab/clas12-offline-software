package cnuphys.bCNU.item;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.drawable.DrawableChangeType;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.layer.LogicalLayer;

public class PixelRectangleItem extends AItem {

	// the pixel widths and heights
	protected int _width;
	protected int _height;

	private int _startW;
	private int _startH;

	private double _startX;
	private double _startY;
	
	
	// the last upper right in world coordinates
	protected Point2D.Double _lastUpperRight = new Point2D.Double(Double.NaN,
			Double.NaN);
	
	/**
	 * Create a rectangle whose location is based on world coordinates but whose
	 * extent is in pixels. And example might be a plot item or image.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param location
	 *            the location of the lower-left in world coordinates
	 * @param width
	 *            the width in pixels
	 * @param height
	 *            the height in pixels
	 */
	public PixelRectangleItem(LogicalLayer layer, Point2D.Double location,
			int width, int height) {
		super(layer);
		_width = width;
		_height = height;
		_focus = new Point2D.Double(location.x, location.y);
	}
	

	@Override
	public void drawItem(Graphics g, IContainer container) {
		Graphics2D g2 = (Graphics2D) g;
		Rectangle r = getBounds(container);
		
		// fill
		if (_style.getFillColor() != null) {
			g2.setColor(_style.getFillColor());
			g2.fillRect(r.x, r.y, r.width, r.height);
		}

		// frame
		if (_style.getLineColor() != null) {

			float flw = _style.getLineWidth();
			if (flw == 0) {
				flw = 0.5f;
			}

			Stroke oldStroke = g2.getStroke();
			Stroke newStroke = GraphicsUtilities.getStroke(flw,
					_style.getLineStyle());
			g2.setStroke(newStroke);
			g2.setColor(_style.getLineColor());
			g2.drawRect(r.x, r.y, r.width, r.height);
			g2.setStroke(oldStroke);
		}
		
	}

	@Override
	public boolean shouldDraw(Graphics g, IContainer container) {
		Rectangle r = getBounds(container);
		Rectangle b = container.getComponent().getBounds();
		b.x = 0;
		b.y = 0;
		return b.intersects(r);
	}

	/**
	 * get the bounding rect in pixels.
	 * 
	 * @param container
	 *            the container being rendered
	 * @return the box around the active part of the image.
	 */
	@Override
	public Rectangle getBounds(IContainer container) {
		Point pp = new Point();
		container.worldToLocal(pp, _focus);
		return new Rectangle(pp.x, pp.y - _height, _width, _height);
	}

	/**
	 * Get the world bounding rectangle of the item.
	 * 
	 * @return the world box containing the item. For a point item, which has no
	 *         extent, this is <code>null</code>.
	 */
	@Override
	public Rectangle2D.Double getWorldBounds() {
		if (Double.isNaN(_lastUpperRight.x)) {
			return new Rectangle2D.Double(_focus.x, _focus.y, 0, 0);
		}

		double w = _lastUpperRight.x - _focus.x;
		double h = _lastUpperRight.y - _focus.y;
		return new Rectangle2D.Double(_focus.x, _focus.y, w, h);
	}

	@Override
	public void startModification() {
		super.startModification();
		_startW = _width;
		_startH = _height;

		_startX = _focus.x;
		_startY = _focus.y;
	}

	@Override
	public void modify() {

		Point startFocusPoint = _modification.getStartFocusPoint();
		if (startFocusPoint == null) {
			return;
		}

		switch (_modification.getType()) {

		case DRAG:
			// compute the total mouse delta
			Point startMouse = _modification.getStartMousePoint();
			Point currentMouse = _modification.getCurrentMousePoint();
			int dx = currentMouse.x - startMouse.x;
			int dy = currentMouse.y - startMouse.y;

			Point newFocusPoint = new Point(startFocusPoint.x + dx,
					startFocusPoint.y + dy);
			Point2D.Double wp = new Point2D.Double();
			_modification.getContainer().localToWorld(newFocusPoint, wp);
			setFocus(wp);
			_modification.getContainer().refresh();
			_layer.notifyDrawableChangeListeners(this, DrawableChangeType.MOVED);
			break;

		case ROTATE:
			_layer.notifyDrawableChangeListeners(this,
					DrawableChangeType.ROTATED);
			break;

		case RESIZE:
			startMouse = _modification.getStartMousePoint();
			currentMouse = _modification.getCurrentMousePoint();
			dx = currentMouse.x - startMouse.x;
			dy = currentMouse.y - startMouse.y;

			Point2D.Double swp = _modification.getStartWorldPoint();
			Point2D.Double cwp = _modification.getCurrentWorldPoint();
			double wdx = cwp.x - swp.x;
			double wdy = cwp.y - swp.y;

			switch (_modification.getSelectIndex()) {

			case 0:
				_width = _startW - dx;
				_height = _startH - dy;
				_focus.x = _startX + wdx;
				break;
			case 1:
				_width = _startW - dx;
				_height = _startH + dy;
				_focus.x = _startX + wdx;
				_focus.y = _startY + wdy;
				break;
			case 2:
				_width = _startW + dx;
				_height = _startH + dy;
				_focus.y = _startY + wdy;
				break;
			case 3:
				_width = _startW + dx;
				_height = _startH - dy;
				break;
			}

			_width = Math.max(4, _width);
			_height = Math.max(4, _height);

			_modification.getContainer().refresh();
			_layer.notifyDrawableChangeListeners(this,
					DrawableChangeType.RESIZED);
			break;
		}
	}

	/**
	 * Set the current location.
	 * 
	 * @param currentLocation
	 *            the new location to set
	 */
	@Override
	public void setFocus(Point2D.Double currentLocation) {
		if (currentLocation == null) {
			_focus.x = Double.NaN;
			_focus.y = Double.NaN;
		} else {
			_focus.x = currentLocation.x;
			_focus.y = currentLocation.y;
		}
	}

	/**
	 * Obtain the selection points used to indicate this item is selected.
	 * 
	 * @return the selection points used to indicate this item is selected.
	 */
	@Override
	public Point[] getSelectionPoints(IContainer container) {
		Rectangle r = getBounds(container);
		Point p[] = new Point[4];
		int bottom = r.y + r.height;
		int right = r.x + r.width;
		p[0] = new Point(r.x, r.y);
		p[1] = new Point(r.x, bottom);
		p[2] = new Point(right, bottom);
		p[3] = new Point(right, r.y);
		return p;
	}

}
