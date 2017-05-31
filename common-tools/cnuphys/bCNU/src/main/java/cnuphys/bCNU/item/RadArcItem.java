/**
 * 
 */
package cnuphys.bCNU.item;

import java.awt.Point;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Point2DSupport;

public class RadArcItem extends PolygonItem {

    // used for resizing
    private int lastQuadrant = 0;
    private double extra;

    /**
     * Create a radius-arc (pie shape) item.
     * 
     * @param layer the Layer this item is on.
     * @param wpc the center of the arc
     * @param wp1 the point at the end of the first leg. Thus wpc->wp1 determine
     *            the radius.
     * @param arcAngle the opening angle COUNTERCLOCKWISE in degrees.
     */
    public RadArcItem(LogicalLayer layer, Point2D.Double wpc,
	    Point2D.Double wp1, double arcAngle) {
	super(layer,
		WorldGraphicsUtilities.getRadArcPoints(wpc, wp1, arcAngle));
	setAzimuth(Point2DSupport.azimuth(wpc, wp1) - arcAngle / 2);
	_focus = wpc;
    }

    /**
     * Reshape the item based on the modification. Keep in mind that if control
     * or shift was pressed, the item will scale rather than coming here.
     */

    @Override
    protected void reshape() {
	if ((_lastDrawnPolygon != null) && (_lastDrawnPolygon.npoints > 1)) {

	    int n = _lastDrawnPolygon.npoints;
	    int x[] = _lastDrawnPolygon.xpoints;
	    int y[] = _lastDrawnPolygon.ypoints;

	    System.err.println("Index: " + _modification.getSelectIndex());

	    Point2D.Double wp1 = _modification.getCurrentWorldPoint();
	    Point p0 = new Point(x[0], y[0]);
	    Point p2;
	    if (_modification.getSelectIndex() == 1) {
		p2 = new Point(x[1], y[1]);
		_modification.setSelectIndex(0);
	    }
	    else {
		p2 = new Point(x[n - 1], y[n - 1]);
	    }

	    Point2D.Double wp0 = new Point2D.Double();
	    Point2D.Double wp2 = new Point2D.Double();
	    getContainer().localToWorld(p0, wp0);
	    getContainer().localToWorld(p2, wp2);

	    Point2D.Double v1 = Point2DSupport.pointDelta(wp0, wp1);
	    Point2D.Double v2 = Point2DSupport.pointDelta(wp0, wp2);

	    double arcAngle = Point2DSupport.angleBetween(v1, v2);

	    double cross = Point2DSupport.cross(v1, v2);
	    int quadrant;

	    if (arcAngle < 90.0) {
		if (cross > 0.0) {
		    quadrant = 1;
		}
		else {
		    quadrant = 4;
		    arcAngle = -arcAngle;
		}
	    }
	    else {
		if (cross > 0.0) {
		    quadrant = 2;
		}
		else {
		    quadrant = 3;
		    arcAngle = -arcAngle;
		}
	    }

	    if ((lastQuadrant == 2) && (quadrant == 3)) {
		extra += 360;
	    }
	    if ((lastQuadrant == 3) && (quadrant == 2)) {
		extra -= 360;
	    }
	    arcAngle += extra;
	    while (arcAngle > 360.0) {
		arcAngle -= 360.0;
	    }
	    while (arcAngle < -360.0) {
		arcAngle += 360.0;
	    }

	    lastQuadrant = quadrant;

	    System.out.println(
		    "arcAngle: " + arcAngle + "  quadrant: " + quadrant);

	    _path = WorldGraphicsUtilities.worldPolygonToPath(
		    WorldGraphicsUtilities.getRadArcPoints(wp0, wp1, arcAngle));
	    updateFocus();

	}
    }

    /**
     * A modification such as a drag, resize or rotate has ended.
     */
    @Override
    public void stopModification() {
	super.stopModification();
	lastQuadrant = 0;
	extra = 0.0;
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
	    pp[0] = new Point(x[1], y[1]);
	    pp[1] = new Point(x[n - 1], y[n - 1]);
	    return pp;
	}

	return null;
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
	_focus = WorldGraphicsUtilities.getPathPointAt(0, _path);
    }

}
