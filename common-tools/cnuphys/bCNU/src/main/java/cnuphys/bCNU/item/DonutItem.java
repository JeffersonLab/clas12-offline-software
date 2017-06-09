package cnuphys.bCNU.item;

import java.awt.Point;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;

public class DonutItem extends PolygonItem {
	
	/**
	 * Create a donut item.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param wpc
	 *            the center of the arc
	 * @param radiusInner
	 *            the inner radius
	 * @param radiusOuter
	 *            the outer radius
	 * @param startAngle
	 *            the startAngle in degrees measured like the usual polar angle
	 *            theta
	 * @param arcAngle
	 *            the opening angle COUNTERCLOCKWISE in degrees.
	 */
	public DonutItem(LogicalLayer layer, Point2D.Double wpc,
			double radiusInner, double radiusOuter, double startAngle,
			double arcAngle) {
		super(layer, WorldGraphicsUtilities.getDonutPoints(wpc, radiusInner,
				radiusOuter, startAngle, arcAngle));
		setAzimuth(90 - startAngle - arcAngle / 2);
		// System.err.println("AZIMUTH: " + (90 - startAngle - arcAngle/2));
		_focus = wpc;
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
			Point pp[] = new Point[4];

			int n = _lastDrawnPolygon.npoints;
			int n2 = n / 2;
			int x[] = _lastDrawnPolygon.xpoints;
			int y[] = _lastDrawnPolygon.ypoints;
			pp[0] = new Point(x[0], y[0]);
			pp[1] = new Point(x[n2 - 1], y[n2 - 1]);
			pp[2] = new Point(x[n2], y[n2]);
			pp[3] = new Point(x[n - 1], y[n - 1]);
			return pp;
		}
		return null;
	}

}
