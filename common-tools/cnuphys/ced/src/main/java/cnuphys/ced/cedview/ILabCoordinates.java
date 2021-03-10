package cnuphys.ced.cedview;

import java.awt.Point;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;

public interface ILabCoordinates {
	
	/**
	 * Convert lab coordinates (CLAS x,y,z) to world coordinates (2D world system of the view)
	 * @param x the CLAS12 x coordinate
	 * @param y the CLAS12 y coordinate
	 * @param z the CLAS12 z coordinate
	 * @param wp holds the world point
	 */
	public void labToWorld(double x, double y, double z, Point2D.Double wp);

	/**
	 * Convert lab coordinates (CLAS x,y,z) to local screen coordinates
	 * @param container the graphics container
	 * @param x the CLAS12 x coordinate
	 * @param y the CLAS12 y coordinate
	 * @param z the CLAS12 z coordinate
	 * @param pp holds the screen point
	 */
	default void labToLocal(IContainer container, double x, double y, double z, Point pp) {
		 Point2D.Double wp = new Point2D.Double();
		 labToWorld(x, y, z, wp);
		 container.worldToLocal(pp, wp);
	}
}
