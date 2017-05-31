package cnuphys.bCNU.item;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;

import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.SymbolType;
import cnuphys.bCNU.layer.LogicalLayer;

/**
 * Basic point item
 * 
 * @author heddle
 * 
 */
public class PointItem extends AItem {

	// alignment constants used for points with icons
	public static final int LEFT = 1;
	public static final int CENTER = 2;
	public static final int RIGHT = 3;

	public static final int TOP = 1;
	public static final int BOTTOM = 3;

	// the alignment values
	private int _xAlignment = CENTER;
	private int _yAlignment = CENTER;

	// some point items will display
	protected ImageIcon _imageIcon;

	/**
	 * Constructor for a basic point item.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 */
	public PointItem(LogicalLayer layer) {
		super(layer);
		_focus = new Point2D.Double(Double.NaN, Double.NaN);
	}

	/**
	 * Constructor for a basic point item.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param icon
	 *            an icon to draw at the point
	 */
	public PointItem(LogicalLayer layer, ImageIcon icon) {
		super(layer);
		_imageIcon = icon;
		_focus = new Point2D.Double(Double.NaN, Double.NaN);
	}

	/**
	 * Constructor for a basic point item.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param location
	 *            the location for the point.
	 */
	public PointItem(LogicalLayer layer, Point2D.Double location) {
		super(layer);
		_focus = new Point2D.Double(location.x, location.y);
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

		// draw icon?
		if (_imageIcon != null) {
			Point p = getFocusPoint(container);
			int w = _imageIcon.getIconWidth();
			int h = _imageIcon.getIconHeight();

			int x = p.x;
			int y = p.y;

			switch (_xAlignment) {
			case LEFT:
				break;
			case CENTER:
				x -= w / 2;
				break;
			case RIGHT:
				x -= w;
				break;
			}

			switch (_yAlignment) {
			case TOP:
				break;
			case CENTER:
				y -= h / 2;
				break;
			case BOTTOM:
				y -= h;
				break;
			}

			g.drawImage(_imageIcon.getImage(), x, y, container.getComponent());
		} else {
			// draw symbol?
			if (_style.getSymbolType() != SymbolType.NOSYMBOL) {
				Rectangle r = getBounds(container);
				int xc = r.x + r.width / 2;
				int yc = r.y + r.height / 2;
				SymbolDraw.drawSymbol(g, xc, yc, _style);
			}
		}
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
	 *         should be drwan.
	 */
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

		if (_imageIcon != null) {
			Point p = new Point();
			container.worldToLocal(p, _focus);

			int w = _imageIcon.getIconWidth();
			int h = _imageIcon.getIconHeight();

			int x = p.x;
			int y = p.y;

			switch (_xAlignment) {
			case LEFT:
				break;
			case CENTER:
				x -= w / 2;
				break;
			case RIGHT:
				x -= w;
				break;
			}

			switch (_yAlignment) {
			case TOP:
				break;
			case CENTER:
				y -= h / 2;
				break;
			case BOTTOM:
				y -= h;
				break;
			}

			return new Rectangle(x, y, w, h);
		}

		Point p = new Point();
		container.worldToLocal(p, _focus);
		int size = _style.getSymbolSize();
		int size2 = size / 2;
		Rectangle r = new Rectangle(p.x - size2, p.y - size2, size, size);
		return r;
	}

	/**
	 * A modification (can only be a drag for a point item) has occurred.
	 */
	@Override
	public void startModification() {
		_modification.setStartFocus(getFocus());
		_modification.setStartFocusPoint(getFocusPoint(_modification
				.getContainer()));
	}

	/**
	 * A modification such as a drag, resize or rotate is continuing.
	 */
	@Override
	public void modify() {
		Point startFocusPoint = _modification.getStartFocusPoint();
		if (startFocusPoint != null) {
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
	 * Get the world bounding rectangle of the item.
	 * 
	 * @return the world box containing the item. For a point item, which has no
	 *         extent, this is <code>null</code>.
	 */
	@Override
	public Rectangle2D.Double getWorldBounds() {
		return null;
	}

	/**
	 * Get the horizontal alignment for the image icon
	 * 
	 * @return the horizontal alignment for the image icon
	 */
	public int getAlignmentH() {
		return _xAlignment;
	}

	/**
	 * Set the horizontal alignment for the image icon
	 * 
	 * @param xAlignment
	 *            the xAlignment to set
	 */
	public void setAlignmentH(int xAlignment) {
		this._xAlignment = xAlignment;
	}

	/**
	 * Get the vertical alignment for the image icon
	 * 
	 * @return the vertical alignment for the image icon
	 */
	public int getAlignmentV() {
		return _yAlignment;
	}

	/**
	 * Set the vertical alignment for the image icon
	 * 
	 * @param yAlignment
	 *            the yAlignment to set
	 */
	public void setAlignmentV(int yAlignment) {
		this._yAlignment = yAlignment;
	}

}
