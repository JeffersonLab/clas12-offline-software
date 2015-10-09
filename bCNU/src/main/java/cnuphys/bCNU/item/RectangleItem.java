package cnuphys.bCNU.item;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Point2DSupport;

public class RectangleItem extends PolygonItem {

	/**
	 * Create a world rectangle object.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param wr
	 *            the initial bounds of the item.
	 */
	public RectangleItem(LogicalLayer layer, Rectangle2D.Double wr) {
		super(layer, WorldGraphicsUtilities.getPoints(wr));
	}

	/**
	 * Reshape the item based on the modification. Keep in mind that if control
	 * or shift was pressed, the item will scale rather than coming here.
	 */
	@Override
	protected void reshape() {
		int j = _modification.getSelectIndex();
		int i = (j + 3) % 4;
		int k = (j + 1) % 4;

		Point2D.Double pp = _modification.getCurrentWorldPoint();

		_path = (Path2D.Double) (_modification.getStartPath().clone());
		Point2D.Double[] wpoly = WorldGraphicsUtilities
				.pathToWorldPolygon(_path);

		Point2D.Double pj = wpoly[j];
		Point2D.Double pi = wpoly[i];
		Point2D.Double pk = wpoly[k];

		Point2D.Double vj = Point2DSupport.pointDelta(pj, pp);
		Point2D.Double a = Point2DSupport.pointDelta(pi, pj);
		Point2D.Double b = Point2DSupport.pointDelta(pk, pj);

		Point2D.Double vi = Point2DSupport.project(vj, b);
		Point2D.Double vk = Point2DSupport.project(vj, a);

		wpoly[j] = pp;
		wpoly[i].x += vi.x;
		wpoly[i].y += vi.y;
		wpoly[k].x += vk.x;
		wpoly[k].y += vk.y;
		_path = WorldGraphicsUtilities.worldPolygonToPath(wpoly);
		updateFocus();
	}

}
