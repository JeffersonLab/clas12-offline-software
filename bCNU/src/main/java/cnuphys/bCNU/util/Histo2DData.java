package cnuphys.bCNU.util;

import java.util.Arrays;

public class Histo2DData {

    private static int OUTOFRANGE = -100;

    // this is the name
    private String _name;

    // some counts
    private long _outOfRangeCount;

    // the X bins--it includes the limits so there is one more number
    // that the number of bins, eg {0, 1, 3, 7} means 3 bins. Note
    // equal spacing is not required.
    private double _gridX[];

    // the Y bins--it includes the limits so there is one more number
    // that the number of bins, eg {0, 1, 3, 7} means 3 bins. Note
    // equal spacing is not required.
    private double _gridY[];

    // the counts
    private long[][] _counts;

    /**
     * The data for a 2D histogram where the bin spacing is uniform.
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
    public Histo2DData(String name, double valMinX, double valMaxX,
	    int numBinsX, double valMinY, double valMaxY, int numBinsY) {

	this(name, evenBins(valMinX, valMaxX, numBinsX), evenBins(valMinY,
		valMaxY, numBinsY));
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
    public Histo2DData(String name, double[] gridX, double[] gridY) {
	_name = name;
	_gridX = gridX;
	_gridY = gridY;
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
	_outOfRangeCount = 0;
	_counts = null;
	int nbinX = getNumberBinsX();
	int nbinY = getNumberBinsY();

	if ((nbinX > 0) && (nbinY > 0)) {
	    _counts = new long[nbinX][nbinY];
	    for (int i = 0; i < nbinX; i++) {
		for (int j = 0; j < nbinY; j++) {
		    _counts[i][j] = 0;
		}
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
    public long[][] getCounts() {
	return _counts;
    }

    /**
     * Get the number on entries in the histogram (excluding out of range)
     * 
     * @return the number of entries
     */
    public long getGoodCount() {

	long sum = 0;
	if (_counts != null) {
	    int nbinX = getNumberBinsX();
	    int nbinY = getNumberBinsY();

	    for (int i = 0; i < nbinX; i++) {
		for (int j = 0; j < nbinY; j++) {
		    sum += _counts[i][j];
		}
	    }
	}
	return sum;
    }

    /**
     * Get the total count, including the out of range
     * 
     * @return the total count, including the out of range
     */
    public long getTotalCount() {
	return getGoodCount() + getOutOfRangeCount();
    }

    /**
     * Get the number on entries in the histogram that were out of range
     * 
     * @return the number of entries out of range
     */
    public long getOutOfRangeCount() {
	return _outOfRangeCount;
    }

    /**
     * Get the minimum "x" value. This is the minimum of the range being binned.
     * 
     * @return the minimum "x" value
     */
    public double getMinX() {
	return (_gridX == null) ? Double.NaN : _gridX[0];
    }

    /**
     * Get the maximum "x" value. This is the maximum of the range being binned.
     * 
     * @return the maximum "x" value
     */
    public double getMaxX() {
	return (_gridX == null) ? Double.NaN : _gridX[_gridX.length - 1];
    }

    /**
     * Get the minimum "y" value. This is the minimum of the range being binned.
     * 
     * @return the minimum "x" value
     */
    public double getMinY() {
	return (_gridY == null) ? Double.NaN : _gridY[0];
    }

    /**
     * Get the maximum y value. This is the maximum of the range being binned.
     * 
     * @return the maximum "y" value
     */
    public double getMaxY() {
	return (_gridY == null) ? Double.NaN : _gridY[_gridY.length - 1];
    }

    /**
     * Get the minimum "z" value. The z axis corresponds to "counts", so this
     * always returns 0
     */
    public double getMinZ() {
	return 0.;
    }

    /**
     * Get the maximum "z" value. The z axis corresponds to "counts", so this
     * always returns the count of the bin with the most counts.
     */
    public double getMaxZ() {
	if (_counts == null) {
	    return 1;
	}

	long maxCount = 0;

	if (_counts != null) {
	    int nbinX = getNumberBinsX();
	    int nbinY = getNumberBinsY();

	    for (int i = 0; i < nbinX; i++) {
		for (int j = 0; j < nbinY; j++) {
		    maxCount = Math.max(maxCount, _counts[i][j]);
		}
	    }
	}
	return maxCount;
    }

    /**
     * Add a value to the histogram
     * 
     * @param value
     *            the value to ad
     */
    public void add(double valueX, double valueY) {

	int binX = getBinX(valueX);
	if (binX == OUTOFRANGE) {
	    _outOfRangeCount++;
	    return;
	}
	int binY = getBinY(valueY);
	if (binY == OUTOFRANGE) {
	    _outOfRangeCount++;
	    return;
	}
	_counts[binX][binY]++;
    }

    /**
     * Get the number of x bins
     * 
     * @return the number of x bins
     */
    public int getNumberBinsX() {
	return (_gridX == null) ? 0 : _gridX.length - 1;
    }

    /**
     * Get the number of x bins
     * 
     * @return the number of x bins
     */
    public int getNumberBinsY() {
	return (_gridY == null) ? 0 : _gridY.length - 1;
    }

    /**
     * Get the "x" value of the middle of the bin
     * 
     * @param bin
     *            the bin in question
     * @return the x mid value
     */
    public double getBinMidValueX(int bin) {
	if ((bin < 0) || (bin >= getNumberBinsX())) {
	    return Double.NaN;
	}
	return 0.5 * (_gridX[bin] + _gridX[bin + 1]);
    }

    /**
     * Get the "y" value of the middle of the bin
     * 
     * @param bin
     *            the bin in question
     * @return the y mid value
     */
    public double getBinMidValueY(int bin) {
	if ((bin < 0) || (bin >= getNumberBinsY())) {
	    return Double.NaN;
	}
	return 0.5 * (_gridY[bin] + _gridY[bin + 1]);
    }

    /**
     * Get the "y" value of the right side of the bin
     * 
     * @param bin
     *            the bin in question
     * @return the y value of the right side
     */
    public double getBinMaxY(int bin) {
	if ((bin < 0) || (bin >= getNumberBinsY())) {
	    return Double.NaN;
	}
	return _gridY[bin + 1];
    }

    /**
     * Get the "x" value of the right side of the bin
     * 
     * @param bin
     *            the bin in question
     * @return the x value of the right side
     */
    public double getBinMaxX(int bin) {
	if ((bin < 0) || (bin >= getNumberBinsX())) {
	    return Double.NaN;
	}
	return _gridX[bin + 1];
    }

    /**
     * Get the "x" value of the left side of the bin
     * 
     * @param bin
     *            the bin in question
     * @return the x value of the left side
     */
    public double getBinMinX(int bin) {
	if ((bin < 0) || (bin >= getNumberBinsX())) {
	    return Double.NaN;
	}
	return _gridX[bin];
    }

    /**
     * Get the "y" value of the left side of the bin
     * 
     * @param bin
     *            the bin in question
     * @return the y value of the left side
     */
    public double getBinMinY(int bin) {
	if ((bin < 0) || (bin >= getNumberBinsY())) {
	    return Double.NaN;
	}
	return _gridY[bin];
    }

    /**
     * Get the x bin for a given x value. Will return the zero-based bin number
     * or OUTOFRANGE.
     * 
     * @param val
     *            the x value.
     * @return return the x bin: [0..(numBinX-1)] or an error
     */
    public int getBinX(double val) {
	if (val < getMinX()) {
	    return OUTOFRANGE;
	} else if (val > getMaxX()) {
	    return OUTOFRANGE;
	}

	int index = Arrays.binarySearch(_gridX, val);
	// unlikely, but maybe we are exactly on a value
	if (index < 0) {
	    index = -(index + 1); // now the insertion point.
	}
	int bin = index - 1;

	return bin;
    }

    /**
     * Get the y bin for a given y value. Will return the zero-based bin number
     * or OUTOFRANGE.
     * 
     * @param val
     *            the y value.
     * @return return the y bin: [0..(numBinY-1)] or an error
     */
    public int getBinY(double val) {
	if (val < getMinY()) {
	    return OUTOFRANGE;
	} else if (val > getMaxY()) {
	    return OUTOFRANGE;
	}

	int index = Arrays.binarySearch(_gridY, val);
	// unlikely, but maybe we are exactly on a value
	if (index < 0) {
	    index = -(index + 1); // now the insertion point.
	}
	int bin = index - 1;

	return bin;
    }

}
