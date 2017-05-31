package cnuphys.ced.cedview;

import java.awt.Point;
import java.awt.geom.Point2D;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

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
	
	/**
	 * From detector xyz get the projected world point.
	 * 
	 * @param x
	 *            the detector x coordinate
	 * @param y
	 *            the detector y coordinate
	 * @param z
	 *            the detector z coordinate
	 * @param wp
	 *            the projected 2D world point.
	 */
	@Override
	public void projectClasToWorld(double x, double y, double z,
			Plane3D projectionPlane, Point2D.Double wp) {
		
		projectedPoint(x, y, z, projectionPlane, wp);
	}
	
	/**
	 * Project a space point. Projected by finding the closest point on the plane.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param wp will hold the projected 2D world point
	 * @return the projected 3D space point
	 */
	@Override
	public Point3D projectedPoint(double x, double y, double z, Plane3D projectionPlane, Point2D.Double wp) {
		Point3D p1 = new Point3D(x, y, z);
		Vector3D normal = projectionPlane.normal();
		Point3D p2 = new Point3D(p1.x() + normal.x(),
				p1.y() + normal.y(), p1.z() + normal.z());
		Line3D perp = new Line3D(p1, p2);
		Point3D pisect = new Point3D();
		projectionPlane.intersection(perp, pisect);
		
		wp.x = pisect.x();
		wp.y = pisect.y();
		return pisect;
	}



}
