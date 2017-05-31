package cnuphys.bCNU.item;

import java.awt.Point;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Point2DSupport;

public class ArcItem extends PolylineItem {

	/**
	 * Create an arc item.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param wpc
	 *            the center of the arc
	 * @param wp1
	 *            the point at the end of the first leg. Thus wpc->wp1 determine
	 *            the radius.
	 * @param arcAngle
	 *            the opening angle COUNTERCLOCKWISE in degrees.
	 */
	public ArcItem(LogicalLayer layer, Point2D.Double wpc, Point2D.Double wp1,
			double arcAngle) {
		super(layer, WorldGraphicsUtilities.getArcPoints(wpc, wp1, arcAngle));
		setAzimuth(Point2DSupport.azimuth(wpc, wp1) - arcAngle / 2);
		_focus = wpc;
	}
	
	/**
	 * Create an arc 
	 * @param layer the Layer this item is on
	 * @param wpc
	 *            the center of the arc
	 * @param radius the radius of the  arc
	 * @param startAngle the starting angle
	 * @param arcAngle the opening angle COUNTERCLOCKWISE in degrees.
	 */
	public ArcItem(LogicalLayer layer, Point2D.Double wpc, double radius, double startAngle, 
			double arcAngle) {
		this(layer, wpc, WorldGraphicsUtilities.radiusPoint(wpc, radius, startAngle), arcAngle);
	}
	

	/**
	 * Reshape the item based on the modification. Keep in mind that if control
	 * or shift was pressed, the item will scale rather than coming here.
	 */

	@Override
	protected void reshape() {
	}

	/**
	 * A modification such as a drag, resize or rotate has ended.
	 */
	@Override
	public void stopModification() {
		super.stopModification();
	}

	/**
	 * Obtain the selection points used to indicate this item is selected.
	 * 
	 * @return the selection points used to indicate this item is selected.
	 */
	@Override
	public Point[] getSelectionPoints(IContainer container) {
		// if the item cached a last drawn polygon lets use it--it better be
		// right!

		if ((_lastDrawnPolygon != null) && (_lastDrawnPolygon.npoints > 1)) {
			Point pp[] = new Point[2];

			int n = _lastDrawnPolygon.npoints;
			int x[] = _lastDrawnPolygon.xpoints;
			int y[] = _lastDrawnPolygon.ypoints;
			pp[0] = new Point(x[0], y[0]);
			pp[1] = new Point(x[n - 1], y[n - 1]);
			return pp;
		}

		return null;
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

		if ((_lastDrawnPolygon != null) && (_lastDrawnPolygon.npoints > 1)) {
			int n = 1 + _lastDrawnPolygon.npoints / 2;
			int x[] = _lastDrawnPolygon.xpoints;
			int y[] = _lastDrawnPolygon.ypoints;
			return new Point(x[n], y[n]);
		}

		return null;
	}

	// called by the modify method to tell the item to update its focus.
	// (because the item was dragged.) The default method is the path centroid.
	@Override
	protected void updateFocus() {
		_focus = WorldGraphicsUtilities.getCentroid(_path);
		// _focus = WorldGraphicsUtilities.getPathPointAt(0, _path);
	}

}
