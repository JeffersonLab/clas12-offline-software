package cnuphys.magfield;

import java.util.Arrays;

/**
 * This class holds the grid parameters for one direction. The constructor is
 * provided the min, max and number of points (inclusive, i.e., including the
 * end points) and the step is computed.
 * 
 * @author David Heddle
 * @author Nicole Schumacher
 * @version 1.0
 */
public class GridCoordinate {

	/**
	 * The minimum value.
	 */
	private double _min;

	/**
	 * The maximum value.
	 */
	private double _max;

	/**
	 * The number of points.
	 */
	private int _numPoints;

	/** The step size (computed). */
	private double _delta;

	/** The name of the coordinate. */
	private String _name;

	private double _values[];

	/**
	 * Construct a grid coordinate.
	 *
	 * @param name
	 *            the name of the coordinate
	 * @param min
	 *            the minimum value
	 * @param max
	 *            the maximum value
	 * @param numPoints
	 *            the number of points (including ends)
	 */
	public GridCoordinate(String name, float min, float max, int numPoints) {
		super();
		_name = name;
		_min = min;
		_max = max;
		_numPoints = numPoints;
		_delta = (max - min) / (numPoints - 1);

		_values = new double[numPoints];
		for (int i = 0; i < numPoints; i++) {
			_values[i] = _min + i * _delta;
		}
	}

	/**
	 * Returns an index [0..numPoints-2] such that the grid values index and
	 * index+1 enclose the value. A return of 0 means the value is between min
	 * and min + delta; A return of numPoints-2 means the value is between max -
	 * delta and max. A return value of -1 indicates out of range.
	 * 
	 * @param val
	 *            the value to test
	 * @return an index [0..numPoints-2] such that the grid values index and
	 *         index+1 enclose the value.
	 */
	public int getIndex(double val) {
		if ((val < _min) || (val > _max)) {
			return -1;
		}

		int index = Arrays.binarySearch(_values, val);
		// index of the search key, if it is contained in the array; otherwise,
		// (-(insertion point) - 1).
		// The insertion point is defined as the point at which the key would be
		// inserted into the array:
		// the index of the first element greater than the key, or a.length if
		// all elements in the array are less than the specified key.
		// Note that this guarantees that the return value will be >= 0 if and
		// only if the key is found.

		if (index < 0) {
			index = -(index + 1); // now the insertion point.
			index = index - 1;
		}
		// pathology if val == max, we'd get numPoints-1
		if (index == _numPoints - 1) {
			index--;
		}

		return index;
	}


	/**
	 * Get the value at a given index.
	 * 
	 * @param index
	 *            the index
	 * @return the value at that index.
	 */
	public double getValue(int index) {
		if (index <= 0) {
			return _min;
		}
		if (index >= (_numPoints - 1)) {
			return _max;
		}

		return _values[index];
	}

	/**
	 * Create a string representation.
	 * 
	 * @return a string representation.
	 */
	@Override
	public String toString() {
		return String.format("%-7s min: %7.2f   max: %7.2f   Np: %3d   delta: %8.3f", _name, _min, _max, _numPoints,
				_delta);
	}

	/**
	 * Get the number of points on the grid (including endpoints).
	 * 
	 * @return the number of points on the grid (including endpoints).
	 */
	public int getNumPoints() {
		return _numPoints;
	}

	/**
	 * Get the computed coordinate spacing.
	 * 
	 * @return the coordinate spacing.
	 */
	public double getDelta() {
		return _delta;
	}

	/**
	 * Get the fractional part for use in interpolation.
	 *
	 * @param val
	 *            the value
	 * @return the fraction of spacing.
	 */
	public double getFraction(double val) {
		double vv = (val - _min) / _delta;
		return vv - Math.floor(vv);
	}

	/**
	 * Get the minimum value of the grid coordinate.
	 * 
	 * @return the minimum value of the grid coordinate.
	 */
	public double getMin() {
		return _min;
	}

	/**
	 * Get the maximum value of the grid coordinate.
	 * 
	 * @return the maximum value of the grid coordinate.
	 */
	public double getMax() {
		return _max;
	}

	/**
	 * Get the name of the coordinate
	 * 
	 * @return the name of the coordinate
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Set the name of the grid coordinate.
	 *
	 * @param name
	 *            the name of the grid coordinate
	 */
	public void setName(String name) {
		_name = name;
	}

}