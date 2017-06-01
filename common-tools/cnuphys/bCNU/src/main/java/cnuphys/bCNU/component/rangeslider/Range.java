package cnuphys.bCNU.component.rangeslider;

public class Range {

	// the minimum value of the range
	private long _minValue;

	// the maximum value of the range
	private long _maxValue;

	public Range(long minValue, long maxValue) {
		super();
		_minValue = minValue;
		_maxValue = maxValue;
	}

	public Range(Range sourceRange) {
		this(sourceRange.getMinValue(), sourceRange.getMaxValue());
	}

	public long getMinValue() {
		return _minValue;
	}

	public void setMinValue(long minValue) {
		_minValue = minValue;
	}

	public long getMaxValue() {
		return _maxValue;
	}

	public void setMaxValue(long maxValue) {
		_maxValue = maxValue;
	}

	/**
	 * Check whether the given value is in range. It is an inclusive check.
	 * 
	 * @param val
	 *            the value to test.
	 * @return <code>true</code> if the value is equal to a limit or between the
	 *         limits.
	 */
	public boolean inRange(long val) {
		return ((val >= _minValue) && (val <= _maxValue));
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		Range or = (Range) o;

		return (_minValue == or._minValue) && (_maxValue == or._maxValue);
	}

}
