package cnuphys.swim;

import java.awt.Point;

public interface IProjector {

	/**
	 * Project a 3D point onto a 2D plane. This controls how the trajectory will be
	 * projected.
	 * 
	 * @param p3d the 3D point in an array
	 * @param wp  the resulting 2D world point
	 */
	public void project(double p3d[], Point.Double wp);
}