package cnuphys.ced.cedview;

import java.awt.Point;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;

@SuppressWarnings("serial")
public abstract class HexView extends CedView {

    /**
     * Create a hex view that lays items out in six sectors NOTE: In Hex views,
     * the world system should be the same as the 2D (xy) lab system
     * 
     * @param title
     *            the title of the view
     */
    public HexView(Object... keyVals) {
	super(keyVals);
	addControls();
	addItems();
	pack();
    }

    // add items to the view
    protected abstract void addItems();

    // add the control panel
    protected abstract void addControls();

    /**
     * Get the 1-based sector.
     * 
     * @return the 1-based sector
     */
    @Override
    public int getSector(IContainer container, Point screenPoint,
	    Point2D.Double worldPoint) {
	return getSector(worldPoint);
    }

    /**
     * Get the 1-based sector.
     * 
     * @return the 1-based sector
     */
    public int getSector(Point2D.Double worldPoint) {
	double phi = getPhi(worldPoint);

	if ((phi > 30.0) && (phi <= 90.0)) {
	    return 2;
	} else if ((phi > 90.0) && (phi <= 150.0)) {
	    return 3;
	} else if ((phi > 150.0) && (phi <= 210.0)) {
	    return 4;
	} else if ((phi > 210.0) && (phi <= 270.0)) {
	    return 5;
	} else if ((phi > 270.0) && (phi <= 330.0)) {
	    return 6;
	} else {
	    return 1;
	}
    }

    /**
     * Get the azimuthal angle
     * 
     * @param worldPoint
     *            the world point
     * @return the value of phi in degrees.
     */
    public double getPhi(Point2D.Double worldPoint) {
	double phi = Math.toDegrees(Math.atan2(worldPoint.y, worldPoint.x));
	if (phi < 0) {
	    phi += 360.0;
	}
	return phi;
    }

}
