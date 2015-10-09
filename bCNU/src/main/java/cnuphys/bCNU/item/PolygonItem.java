package cnuphys.bCNU.item;

import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;

public class PolygonItem extends PathBasedItem {

	/**
	 * Create a world polygon item
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param points
	 *            the points of the polygon
	 */
	public PolygonItem(LogicalLayer layer, Point2D.Double points[]) {
		super(layer);

		// set the path
		if (points != null) {
			setPath(points);
		}
	}

	/**
	 * Create a world polygon item
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 */
	public PolygonItem(LogicalLayer layer) {
		super(layer);
	}

	/**
	 * Set the path from a world polygon.
	 * 
	 * @param points
	 *            the points of the polygon.
	 */
	public void setPath(Point2D.Double points[]) {
		_path = WorldGraphicsUtilities.worldPolygonToPath(points);
		_focus = WorldGraphicsUtilities.getCentroid(_path);
	}

	/**
	 * Reshape the polygon based on the modification. Not much we can do to a
	 * polygon except move the selected point. Keep in mind that if control or
	 * shift was pressed, the polygon will scale rather than coming here.
	 */
	@Override
	protected void reshape() {
		int index = _modification.getSelectIndex();
		Point2D.Double[] wpoly = WorldGraphicsUtilities
				.pathToWorldPolygon(_path);
		Point2D.Double wp = _modification.getCurrentWorldPoint();
		wpoly[index] = wp;
		setPath(wpoly);
	}

}
