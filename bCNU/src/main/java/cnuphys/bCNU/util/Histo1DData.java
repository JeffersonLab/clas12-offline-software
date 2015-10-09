package cnuphys.bCNU.util;

import java.util.Arrays;

public class Histo1DData {

	// out of range constants
	private static int UNDERFLOW = -200;
	private static int OVERFLOW = -100;

	// this is the name
	private String _name;

	// some counts
	private long _underCount;
	private long _overCount;

	// the bins--it includes the limits so there is one more number
	// that the number of bins, eg {0, 1, 3, 7} means 3 bins. Note
	// equal spacing is not required.
	private double _grid[];

	// the counts
	private long[] _counts;

	/**
	 * The data for a 1D histogram where the bin spacing is uniform.
	 * 
	 * @param name
	 *            the cname
	 * @param valMin
	 *            the data min
	 * @param valMax
	 *            the data max
	 * @param numBins
	 *            the number of bins
	 */
	public Histo1DData(String name, double valMin, double valMax, int numBins) {
		this(name, evenBins(valMin, valMax, numBins));
	}

	/**
	 * The data for a 1D histogram where the bin spacing is arbitrary (i.e., not
	 * uniform)
	 * 
	 * @param name
	 *            the curve name of the histogram
	 * @param grid
	 *            the binning grid. It must be in ascending order but is
	 *            otherwise arbitrary.
	 */
	public Histo1DData(String name, double[] grid) {
		_name = name;
		_grid = grid;
		clear();
	}

	/**
	 * Get the name of the histogram
	 * 
	 * @return the name of the histogram
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Set the name
	 * 
	 * @param name
	 *            the name of the histogram
	 */
	public void setName(String name) {
		_name = name;
	}

	// reset some data
	private void reset() {
		_underCount = 0;
		_overCount = 0;
		int nbin = getNumberBins();
		if (nbin > 0) {
			_counts = new long[nbin];
			for (int i = 0; i < nbin; i++) {
				_counts[i] = 0;
			}
		}
	}

	/**
	 * Clear the data
	 */
	public void clear() {
		reset();
	}

	// get an array of equally spaced bins
	// suitable for most histograms
	private static double[] evenBins(double vmin, double vmax, int numBins) {
		double grid[] = new double[numBins + 1];
		double del = (vmax - vmin) / numBins;

		grid[0] = vmin;
		grid[numBins] = vmax;
		for (int i = 1; i < numBins; i++) {
			grid[i] = vmin + i * del;
		}
		return grid;
	}

	/**
	 * Get the counts
	 * 
	 * @return the counts array
	 */
	public long[] getCounts() {
		return _counts;
	}

	/**
	 * Get the number on entries in the histogram (excluding underflows and
	 * overflows)
	 * 
	 * @return the number of entries
	 */
	public long getGoodCount() {

		long sum = 0;
		if (_counts != null) {
			for (long lv : _counts) {
				sum += lv;
			}
		}
		return sum;
	}

	/**
	 * Get the total count, including the unders and overs
	 * 
	 * @return the total count, including the unders and overs
	 */
	public long getTotalCount() {
		return getGoodCount() + getUnderCount() + getOverCount();
	}

	/**
	 * Get the number on entries in the histogram that were below the minimum
	 * value
	 * 
	 * @return the number of entries below the minimum value
	 */
	public long getUnderCount() {
		return _underCount;
	}

	/**
	 * Get the number on entries in the histogram that were above the maximum
	 * value
	 * 
	 * @return the number of entries above the maximum value
	 */
	public long getOverCount() {
		return _overCount;
	}

	/**
	 * Get the minimum "x" value. This is the minimum of the range being binned.
	 * 
	 * @return the minimum "x" value
	 */
	public double getMinX() {
		return (_grid == null) ? Double.NaN : _grid[0];
	}

	/**
	 * Get the minimum value. This is the maximum of the range being binned.
	 * 
	 * @return the minimum "x" value
	 */
	public double getMaxX() {
		return (_grid == null) ? Double.NaN : _grid[_grid.length - 1];
	}

	/**
	 * Get the minimum "y" value. The y axis corresponds to "counts", so this
	 * always returns 0
	 */
	public double getMinY() {
		return 0.;
	}

	/**
	 * Get the maximum "y" value. The y axis corresponds to "counts", so this
	 * always returns the count of the bin with the most counts.
	 */
	public double getMaxY() {
		if (_counts == null) {
			return 1;
		}

		long maxCount = 1;
		for (long bc : _counts) {
			maxCount = Math.max(maxCount, bc);
		}

		return maxCount;
	}

	/**
	 * Add a value to the histogram
	 * 
	 * @param value
	 *            the value to ad
	 */
	public void add(double value) {

		int bin = getBin(value);
		if (bin == UNDERFLOW) {
			_underCount++;
		} else if (bin == OVERFLOW) {
			_overCount++;
		} else {
			_counts[bin]++;
		}

	}

	/**
	 * Get the number of bins
	 * 
	 * @return the number of bins
	 */
	public int getNumberBins() {
		return (_grid == null) ? 0 : _grid.length - 1;
	}

	/**
	 * Get the "x" value of the middle of the bin
	 * 
	 * @param bin
	 *            the bin in question
	 * @return the mid value
	 */
	public double getBinMidValue(int bin) {
		if ((bin < 0) || (bin >= getNumberBins())) {
			return Double.NaN;
		}
		return 0.5 * (_grid[bin] + _grid[bin + 1]);
	}

	/**
	 * Get the "x" value of the right side of the bin
	 * 
	 * @param bin
	 *            the bin in question
	 * @return the x value of the right side
	 */
	public double getBinMaxX(int bin) {
		if ((bin < 0) || (bin >= getNumberBins())) {
			return Double.NaN;
		}
		return _grid[bin + 1];
	}

	/**
	 * Get the "x" value of the left side of the bin
	 * 
	 * @param bin
	 *            the bin in question
	 * @return the x value of the left side
	 */
	public double getBinMinX(int bin) {
		if ((bin < 0) || (bin >= getNumberBins())) {
			return Double.NaN;
		}
		return _grid[bin];
	}

	/**
	 * Get the bin for a given value. Will return the zero-based bin number or
	 * UNDERFLOW or OVERFLOW.
	 * 
	 * @param val
	 *            the value.
	 * @return return the bin: [0..(numBin-1)] or an error
	 */
	public int getBin(double val) {
		if (val < getMinX()) {
			return UNDERFLOW;
		} else if (val > getMaxX()) {
			return OVERFLOW;
		}

		int index = Arrays.binarySearch(_grid, val);
		// unlikely, but maybe we are exactly on a value
		if (index < 0) {
			index = -(index + 1); // now the insertion point.
		}
		int bin = index - 1;

		return bin;
	}

}
