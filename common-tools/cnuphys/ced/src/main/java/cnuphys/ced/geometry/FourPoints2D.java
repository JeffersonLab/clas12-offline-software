package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

/**
 * A common class of four (probably not rectangular) 2D points. An example would
 * be the ecal u,v, and w strips
 * 
 * @author heddle
 *
 */
public class FourPoints2D {

	private Point2D.Double _points[];

	private int _currentIndex;

	/**
	 * Create with all null points. The points will be added later.
	 */
	public FourPoints2D() {
		_points = new Point2D.Double[4];
		for (int i = 0; i < 4; i++) {
			_points[i] = null;
		}
		_currentIndex = 0;
	}

	/**
	 * Add another point. The index will be incremented
	 * 
	 * @param x
	 *            the horizontal coordinate
	 * @param y
	 *            the vertical coordinate
	 */
	public void add(double x, double y) {
		if (_currentIndex > 3) {
			System.err
					.println("Cannot add a fifth point to this FourPoints2D object");
			return;
		}
		_points[_currentIndex] = new Point2D.Double(x, y);
		_currentIndex++;
	}

	/**
	 * Get the point for a given index
	 * 
	 * @param index
	 *            should be [0..3]
	 * @return the corresponding point
	 */
	public Point2D.Double get(int index) {
		return _points[index];
	}

	/**
	 * Get all the points
	 * 
	 * @return the points array with 4 points
	 */
	public Point2D.Double[] getPoints() {
		return _points;
	}
}
