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

	private boolean _isUniform = true;

	/**
	 * The overall minimum value.
	 */
	private double _min;

	/**
	 * The overall maximum value.
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
	 * @param name      the name of the coordinate
	 * @param min       the minimum value
	 * @param max       the maximum value
	 * @param numPoints the number of points (including ends)
	 */
	public GridCoordinate(String name, float min, float max, int numPoints) {
		this(name, (double) min, (double) max, numPoints);
	}

	/**
	 * Construct a grid coordinate.
	 *
	 * @param name      the name of the coordinate
	 * @param min       the minimum value
	 * @param max       the maximum value
	 * @param numPoints the number of points (including ends)
	 */
	public GridCoordinate(String name, double min, double max, int numPoints) {
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
	 * Get the values of the grid points
	 * @return the values of the grid points
	 */
	public double[] getValues() {
		return _values;
	}

	/**
	 * Returns an index [0..numPoints-2] such that the grid values index and index+1
	 * enclose the value. A return of 0 means the value is between min and min +
	 * delta; A return of numPoints-2 means the value is between max - delta and
	 * max. A return value of -1 indicates out of range.
	 * 
	 * @param val the value to test
	 * @return an index [0..numPoints-2] such that the grid values index and index+1
	 *         enclose the value.
	 */
	public int getIndex(double val) {
		if ((val < _min) || (val > _max)) {
			return -1;
		}

		if (_isUniform) {
			int uindex = (int) ((val - _min) / _delta);
			uindex = Math.max(0, Math.min(uindex, (_numPoints - 1)));
			return uindex;
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
	 * @param index the index
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
		return String.format("%-7s min: %6.1f   max: %6.1f   Np: %4d   delta: %7.2f", _name, _min, _max, _numPoints,
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
	 * @param val the value
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
	 * @param name the name of the grid coordinate
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Get the minimum for a given index
	 * 
	 * @param index the index
	 * @return the min for the given index
	 */
	public double getMin(int index) {
		return _min + index * _delta;
	}

	/**
	 * Get the maximum for a given index
	 * 
	 * @param index the index
	 * @return the max for the given index
	 */
	public double getMax(int index) {
		return _min + (index + 1) * _delta;
	}

	/**
	 * Check whether a value is in range
	 * 
	 * @param q the value
	 * @return <code>true</code> if the value is between min and max
	 */
	public boolean inRange(double q) {
		return (q >= _min) && (q <= _max);
	}

	/**
	 * Make a clone of this grid coordinate
	 */
	@Override
	public GridCoordinate clone() {
		return new GridCoordinate(_name, _min, _max, _numPoints);
	}

}