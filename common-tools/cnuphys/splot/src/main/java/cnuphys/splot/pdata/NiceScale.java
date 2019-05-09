package cnuphys.splot.pdata;

public class NiceScale {

	private double _min;
	private double _max;
	private int _numTicks = 10; // includes ends
	private double _tickSpacing;
	private double _niceMin;
	private double _niceMax;
	private boolean _includeZero;

	/**
	 * Instantiates a new instance of the NiceScale class.
	 *
	 * @param min      the minimum data point on the axis
	 * @param max      the maximum data point on the axis
	 * @param numTicks the number of ticks including the ends
	 */
	public NiceScale(double min, double max, int numTicks, boolean includeZero) {
		_min = min;
		_max = max;
		_numTicks = numTicks;
		_includeZero = includeZero;

		if (includeZero) {
			_min = Math.min(_min, 0);
			_max = Math.max(_max, 0);
		}
		calculate();
	}

	/**
	 * Calculate and update values for tick spacing and nice minimum and maximum
	 * data points on the axis.
	 */
	private void calculate() {
		double range = niceNum(_max - _min, false);
		_tickSpacing = niceNum(range / (_numTicks - 1), true);
		_niceMin = Math.floor(_min / _tickSpacing) * _tickSpacing;
		_niceMax = Math.ceil(_max / _tickSpacing) * _tickSpacing;
	}

	/**
	 * Get the tick spacing
	 * 
	 * @return the tick spacing
	 */
	public double getTickSpacing() {
		return _tickSpacing;
	}

	/**
	 * Get the nice plot min
	 * 
	 * @return the nice plot min
	 */
	public double getNiceMin() {
		return _niceMin;
	}

	/**
	 * Get the nice plot max
	 * 
	 * @return the nice plot max
	 */
	public double getNiceMax() {
		return _niceMax;
	}

	/**
	 * Returns a "nice" number approximately equal to range Rounds the number if
	 * round = true Takes the ceiling if round = false.
	 *
	 * @param range the data range
	 * @param round whether to round the result
	 * @return a "nice" number to be used for the data range
	 */
	private double niceNum(double range, boolean round) {
		double exponent;
		/** exponent of range */
		double fraction;
		/** fractional part of range */
		double niceFraction;
		/** nice, rounded fraction */

		exponent = Math.floor(Math.log10(range));
		fraction = range / Math.pow(10, exponent);

		if (round) {
			if (fraction < 1.5)
				niceFraction = 1;
			else if (fraction < 3)
				niceFraction = 2;
			else if (fraction < 7)
				niceFraction = 5;
			else
				niceFraction = 10;
		}
		else {
			if (fraction <= 1)
				niceFraction = 1;
			else if (fraction <= 2)
				niceFraction = 2;
			else if (fraction <= 5)
				niceFraction = 5;
			else
				niceFraction = 10;
		}

		return niceFraction * Math.pow(10, exponent);
	}

	/**
	 * Sets the minimum and maximum data points for the axis.
	 *
	 * @param min the minimum data point on the axis
	 * @param max the maximum data point on the axis
	 */
	public void setMinMaxPoints(double min, double max) {
		_min = min;
		_max = max;
		calculate();
	}

	/**
	 * Sets maximum number of tick marks we're comfortable with
	 *
	 * @param maxTicks the maximum number of tick marks for the axis
	 */
	public void setMaxTicks(int maxTicks) {
		_numTicks = maxTicks;
		calculate();
	}

	/**
	 * Get the number of ticks (including the ends)
	 * 
	 * @return the number of ticks
	 */
	public int getNumTicks() {
		return _numTicks;
	}
}
