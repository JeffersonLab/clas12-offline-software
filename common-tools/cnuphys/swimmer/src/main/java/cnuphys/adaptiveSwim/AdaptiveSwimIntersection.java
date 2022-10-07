package cnuphys.adaptiveSwim;

import cnuphys.adaptiveSwim.geometry.AGeometric;
import cnuphys.adaptiveSwim.geometry.Point;

/**
 * This is used to hold information about the intersection with a
 * boundary, for example the intersection of the plane in a swim-to-plane
 * method
 * @author heddle
 *
 */
public class AdaptiveSwimIntersection {

	//closest we get on the "left"
	//the original side is always called "left"
	private Point _left = new Point();
	private double _sLeft;
	
	private double _txLeft;
	private double _tyLeft;
	private double _tzLeft;

	//closest we get on the "right"
	private Point _right = new Point();
	private double _sRight;
	
	private double _txRight;
	private double _tyRight;
	private double _tzRight;



	//The estimate of the xyz intersection
	private Point _intersection = new Point();
	
	//interpolated s and t's
	private double _s;
	private double _tx;
	private double _ty;
	private double _tz;

	//distance from intersection point to object (should be very small)
	private double _distance = Double.NaN;


	public AdaptiveSwimIntersection() {
	}


	/**
	 * Set the next left (staring side) point
	 * @param u the state vector
	 * @param s the path length
	 */
	public void setLeft(double u[], double s) {
		_left.set(u[0], u[1], u[2]);
		_sLeft = s;
		_txLeft = u[3];
		_tyLeft = u[4];
		_tzLeft = u[5];
	}

	/**
	 * See what should be the only "far side" point
	 * @param u the state vector
	 * @param s the path length
	 */
	public void setRight(double u[], double s) {
		_right.set(u[0], u[1], u[2]);
		_sRight = s;
		_txRight = u[3];
		_tyRight = u[4];
		_tzRight = u[5];
	}

	/**
	 * Reset the parameters so the object can be reused in another swim.
	 */
	public void reset() {
		_left.set(Double.NaN, Double.NaN, Double.NaN);
		_right.set(Double.NaN, Double.NaN, Double.NaN);
		_intersection.set(Double.NaN, Double.NaN, Double.NaN);
		_distance = Double.NaN;

	}
	
	/**
	 * Get the point on the "left" side of the intersection
	 * @return the point on the "left" side of the intersection
	 */
	public Point getLeft() {
		return _left;
	}
	
	/**
	 * Get the point on the "right" side of the intersection
	 * @return the point on the "right" side of the intersection
	 */
	public Point getRight() {
		return _right;
	}


	/**
	 * Compute and store the interpolation ad distance from the object.
	 * Note that if the intersection is successful, the distance should be zero!
	 * @param geom the object (e.g., plane) that is being intersected.
	 */
	public void computeIntersection(AGeometric geom) {
		double t = geom.interpolate(_left, _right, _intersection);
		_s = _sLeft + t * (_sRight - _sLeft);
		_tx = _txLeft + t * (_txRight - _txLeft);
		_ty = _tyLeft + t * (_tyRight - _tyLeft);
		_tz = _tzLeft + t * (_tzRight - _tzLeft);

		_distance = geom.distance(_intersection);
	}

	/**
	 * Get the intersection distance. Note that if the intersection
	 * is successful, the distance should be zero! This assumes
	 * a call to computeIntersection has been made at the end
	 * of a swim.
	 * @return the intersection distance
	 */
	public double getIntersectDistance() {
		return _distance;
	}

	/**
     * Get the intersection distance. This assumes
	 * a call to computeIntersection has been made at the end
	 * of a swim.
	 * @return the intersection point
	 */
	public Point getIntersectionPoint() {
		return _intersection;
	}
	
	/**
	 * Set a u based on interpolated values
	 * @param u the state vector to fill
	 */
	public void setU(double u[]) {
		u[0] = _intersection.x;
		u[1] = _intersection.y;
		u[2] = _intersection.z;
		u[3] = _tx;
		u[4] = _ty;
		u[5] = _tz;
	}
	
	/**
	 * Get the interpolated pathlength
	 * @return the pathlength
	 */
	public double getS() {
		return _s;
	}

	@Override
	public String toString() {
		String istr = _intersection.toString();
		return String.format("  int: %s  D: %-6.2e",
				istr, _distance);
	}

}
