package cnuphys.splot.pdata;

/**
 * This class supports named arrays that grow. They are an alternative to
 * Vectors or ArrayLists. The advantages are speed and usage for those cases
 * where arrays are more convenient. Clients only have access to minimal copies
 * which can be used as "just the right size" arrays.
 * 
 * @author heddle
 * 
 */
public class GrowableArray {

	// the actual array
	protected double _data[];

	// the current size (amount of data, not capacity) of the array
	// this will be the size of the minimal copy
	protected int _dataLen;

	// the increment when the array must grow
	protected int _increment;

	// the initial capacity
	protected int _initCap;

	// min and max data values
	protected double _minValue;
	protected double _maxValue;

	/**
	 * Creates a GrowableArray with initial capacity 100 and increment 100.
	 */
	public GrowableArray() {
		this(100, 100);
	}

	/**
	 * Create a GrowableArray
	 * 
	 * @param initCap   the initial capacity
	 * @param increment the increment when the array grows.
	 */
	public GrowableArray(int initCap, int increment) {
		_initCap = initCap;
		_increment = increment;
		clear();
	}

	/**
	 * Get the number of real data in the array, which in general is less than the
	 * length of the array. This is the effective length of the array, so loops
	 * should go from 0 to this value minus 1.
	 * 
	 * @return the number of real data.
	 */
	public int size() {
		return _dataLen;
	}

	/**
	 * Get the minimal copy of the data array. This copies the actual data into a
	 * new array of just the right size. This new array can be used normally, i.e.,
	 * it is safe to use its <code>length</code>.
	 * 
	 * @return a copy of the data array of just the right length
	 */
	public double[] getMinimalCopy() {
		if (_dataLen == 0) {
			return null;
		}
		double acopy[] = new double[_dataLen];

		try {
			System.arraycopy(_data, 0, acopy, 0, _dataLen);
		}
		catch (Exception e) {

			System.err.println("Exception in GrowableArray.getMinimalCopy: " + e.getMessage());
			System.err.println("_dataLen: " + _dataLen);
		}
		return acopy;
	}

	/**
	 * removes the first entry and moves all other entries up one
	 */
	public void removeFirst() {
		if (_dataLen > 0) {
			_dataLen--;

			_minValue = Double.POSITIVE_INFINITY;
			_maxValue = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < _dataLen; i++) {
				_data[i] = _data[i + 1];
				_minValue = Math.min(_data[i], _minValue);
				_maxValue = Math.max(_data[i], _maxValue);
			}
			_data[_dataLen] = Double.NaN;
		}
	}

	/**
	 * Add a value to the array, growing it if necessary.
	 * 
	 * @param val the value to add
	 */
	public void add(double val) {
		if (_dataLen == _data.length) {
			double newArray[] = new double[_dataLen + _increment];
			System.arraycopy(_data, 0, newArray, 0, _dataLen);

			// fill empty space with NaNs
			for (int i = _dataLen; i < newArray.length; i++) {
				newArray[i] = Double.NaN;
			}
			_data = newArray;
		}
		_data[_dataLen] = val;
		_dataLen++;

		// fix min and max vals
		if (_dataLen == 1) {
			_minValue = val;
			_maxValue = val;
		}
		else {
			_minValue = Math.min(_minValue, val);
			_maxValue = Math.max(_maxValue, val);
		}
	}

	/**
	 * Get the minimum data value
	 * 
	 * @return the minimum data value
	 */
	public double getMinValue() {
		return _minValue;
	}

	/**
	 * Get the maximum data value
	 * 
	 * @return the maximum data value
	 */
	public double getMaxValue() {
		return _maxValue;
	}

	/**
	 * Reset the data array to the initial capacity and fill with all NaNs.
	 */
	public void clear() {
		_data = new double[_initCap];
		_dataLen = 0;
		for (int i = 0; i < _initCap; i++) {
			_data[i] = Double.NaN;
		}

		_minValue = Double.NaN;
		_maxValue = Double.NaN;
	}

	/**
	 * Get the value at the given index
	 * 
	 * @param index the index
	 * @return the value at the index
	 */
	public double get(int index) {
		return _data[index];
	}

	/**
	 * Set the value at the given index
	 * 
	 * @param index the index
	 * @param val   the value at the index
	 */
	public void set(int index, double val) {
		_data[index] = val;
	}

}
