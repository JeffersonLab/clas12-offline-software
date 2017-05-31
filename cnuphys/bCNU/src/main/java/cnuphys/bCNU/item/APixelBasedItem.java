/**
 * 
 */
package cnuphys.bCNU.item;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.layer.LogicalLayer;

/**
 * This is for items that are pixel based
 * 
 * @author heddle
 * 
 */
public abstract class APixelBasedItem extends AItem {

	// it the placement absolute or relative
	public enum PlacementType {
		ABSOLUTE, RELATIVE
	};

	// used for relative placement only
	// x: min means left, max means right
	// y: min menas top, max means botton
	public enum PlacementEdge {
		MINIMUM, CENTER, MAXIMUM
	};

	// default placement is relative to lower left
	protected PlacementType placementType = PlacementType.RELATIVE;
	protected PlacementEdge horizontalPlacement = PlacementEdge.MINIMUM;
	protected PlacementEdge verticalPlacement = PlacementEdge.MAXIMUM;

	// the location is absolute for absolute placement and a delta for
	// relative placement
	protected Point location = new Point(0, 0);

	// the size (width and height)
	protected Dimension size = new Dimension(100, 100);

	/**
	 * Create a pixel based item
	 * 
	 * @param layer
	 *            the logical layer containing the item.
	 */
	public APixelBasedItem(LogicalLayer layer) {
		super(layer);

		// superclass added me, but in general pixel based items have no useful
		// feedback
		layer.getContainer().getFeedbackControl().removeFeedbackProvider(this);
		setDraggable(true);
		setDeletable(false); // tend to be permanent items
		setRotatable(false);
		setResizable(false);
		setLocked(false);
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
		return true;
	}

	/**
	 * @return the placementType
	 */
	public PlacementType getPlacementType() {
		return placementType;
	}

	/**
	 * @param placementType
	 *            the placementType to set
	 */
	public void setPlacementType(PlacementType placementType) {
		this.placementType = placementType;
	}

	/**
	 * @return the horizontalPlacement
	 */
	public PlacementEdge getHorizontalPlacement() {
		return horizontalPlacement;
	}

	/**
	 * @param horizontalPlacement
	 *            the horizontalPlacement to set
	 */
	public void setHorizontalPlacement(PlacementEdge horizontalPlacement) {
		this.horizontalPlacement = horizontalPlacement;
	}

	/**
	 * @return the verticalPlacement
	 */
	public PlacementEdge getVerticalPlacement() {
		return verticalPlacement;
	}

	/**
	 * @param verticalPlacement
	 *            the verticalPlacement to set
	 */
	public void setVerticalPlacement(PlacementEdge verticalPlacement) {
		this.verticalPlacement = verticalPlacement;
	}

	/**
	 * Get the upper left location of the item. The location is absolute for
	 * absolute placement and a delta for relative placement.
	 * 
	 * @return the location
	 */
	public Point getLocation() {
		return location;
	}

	/**
	 * Set the upper-left location of the item
	 * 
	 * @param location
	 *            the location to set. The location is absolute for absolute
	 *            placement and a delta for relative placement.
	 */
	public void setLocation(Point location) {
		this.location = location;
	}

	/**
	 * Get the width and height of the item
	 * 
	 * @return the size of the item
	 */
	public Dimension getSize() {
		return size;
	}

	/**
	 * Set the width and height of the item
	 * 
	 * @param size
	 *            the size to set for the item
	 */
	public void setSize(Dimension size) {
		this.size = size;
	}

	/**
	 * Get the bounds of the container
	 * 
	 * @return the rectangle giving the bounds
	 */
	@Override
	public Rectangle getBounds(IContainer container) {
		size = getSize();
		if (size == null) {
			return null;
		}

		Rectangle b = container.getComponent().getBounds();

		Rectangle r = new Rectangle(0, 0, size.width, size.height);

		switch (placementType) {
		case ABSOLUTE:
			r.x = location.x;
			r.y = location.y;
			break;

		case RELATIVE:
			switch (horizontalPlacement) {
			case MINIMUM:
				r.x = location.x;
				break;
			case CENTER:
				r.x = b.width / 2 - size.width / 2 + location.x;
				break;
			case MAXIMUM:
				r.x = b.width - size.width + location.x;
				break;
			}

			switch (verticalPlacement) {
			case MINIMUM:
				r.y = location.y;
				break;
			case CENTER:
				r.y = b.height / 2 - size.height / 2 + location.y;
				break;
			case MAXIMUM:
				r.y = b.height - size.height + location.y;
				break;
			}

			break;
		}

		return r;
	}

	/**
	 * Get the world bounding rectangle of the item.
	 * 
	 * @return the world box containing the item. For a pixel based item this is
	 *         <code>null</code>.
	 */
	@Override
	public Rectangle2D.Double getWorldBounds() {
		return null;
	}

}
