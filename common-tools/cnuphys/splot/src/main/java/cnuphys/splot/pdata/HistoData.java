package cnuphys.splot.pdata;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Arrays;
import cnuphys.splot.plot.DoubleFormat;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.plot.UnicodeSupport;

/**
 * Xontainer class for histogram data
 * 
 * @author heddle
 *
 */
public class HistoData {

	/** The XML root element name */
	public static final String XmlRootElementName = "HistoData";

	// cached statistical results
	// an array with the mean in the 0 index,
	// standard deviation is the 1 index, and rms in the 2 index
	private double _stats[];

	// out of range constants
	private static int UNDERFLOW = -200;
	private static int OVERFLOW = -100;

	// this is the "curve" name
	private String _name;

	// some counts
	private long _underCount;
	private long _overCount;

	// true for even bin spacing
	private boolean _uniformBins;

	// use rms or sigma in legend
	private boolean _rmsInHistoLegend = true;

	// draw sqrt(n) statistical errors
	private boolean _statErrors;

	// the bins--it includes the limits so there is one more number
	// that the number of bins, eg {0, 1, 3, 7} means 3 bins. Note
	// equal spacing is not required.
	private double _grid[];

	// the counts
	private long[] _counts;

	/**
	 * The data for a 1D histogram where the bin spacing is uniform.
	 * 
	 * @param name    the curve name of the histogram
	 * @param valMin  the data min
	 * @param valMax  the data max
	 * @param numBins the number of bins
	 */
	public HistoData(String name, double valMin, double valMax, int numBins) {
		this(name, evenBins(valMin, valMax, numBins));
		_uniformBins = true;
	}
	

	/**
	 * The data for a 1D histogram where the bin spacing is arbitrary (i.e., not
	 * uniform)
	 * 
	 * @param name the curve name of the histogram
	 * @param grid the binning grid. It must be in ascending order but is otherwise
	 *             arbitrary.
	 */
	public HistoData(String name, double[] grid) {
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
	 * @param name the name of the histogram
	 */
	public void setName(String name) {
		_name = name;
	}

	// reset some data
	private void reset() {
		_underCount = 0;
		_overCount = 0;
		_stats = null;
		int nbin = getNumberBins();
		if (nbin > 0) {
			_counts = new long[nbin];
			for (int i = 0; i < nbin; i++) {
				_counts[i] = 0;
			}
		}
	}

	/**
	 * Get the count for a given bin
	 * 
	 * @param bin the bin
	 * @return the count for that bin
	 */
	public long getCount(int bin) {
		if (_counts == null) {
			return 0;
		}

		if ((bin < 0) || (bin >= _counts.length)) {
			return 0;
		}

		return _counts[bin];
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
	 * Get the means and standard deviation
	 * 
	 * @return an array with the mean in the 0 index, standard deviation is the 1
	 *         index, and rms in the 2 index
	 */
	public double[] getBasicStatistics() {

		if (_stats != null) {
			return _stats;
		}

		_stats = new double[3];
		_stats[0] = Double.NaN;
		_stats[1] = Double.NaN;
		_stats[2] = Double.NaN;
		int nbin = getNumberBins();
		long totcount = getGoodCount();
		if ((nbin > 0) && (totcount > 0)) {

			double sum = 0;
			double sumsq = 0;
			for (int bin = 0; bin < nbin; bin++) {
				double x = getBinMidValue(bin);
				double wx = _counts[bin] * x;
				sum += wx;
				sumsq += x * wx;
			}

			double avgSq = sumsq / totcount;

			_stats[0] = sum / totcount; // mean
			_stats[1] = Math.sqrt(avgSq - (_stats[0] * _stats[0])); // stdDev
			_stats[2] = Math.sqrt(avgSq); // rms
		}

		return _stats;
	}

	/**
	 * A string displaying some statistics
	 * 
	 * @param useRms if <code>true</code> use rms, else use sigma
	 * @return a string with statistics info
	 */
	public String statStr() {
		double res[] = getBasicStatistics();
		if (_rmsInHistoLegend) {
			return String.format(UnicodeSupport.SMALL_MU + ": %-4.2g " + "rms: %-4.2g under: %d over: %d", res[0], res[2], _underCount, _overCount);
		}
		else {
			return String.format(UnicodeSupport.SMALL_MU + ": %-4.2g " + UnicodeSupport.SMALL_SIGMA + ": %-4.2g under: %d over: %d",
					res[0], res[1], _underCount, _overCount);
		}
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
	 * Get the number on entries in the histogram that were below the minimum value
	 * 
	 * @return the number of entries below the minimum value
	 */
	public long getUnderCount() {
		return _underCount;
	}

	/**
	 * Get the number on entries in the histogram that were above the maximum value
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
	 * Get the minimum "y" value. The y axis corresponds to "counts", so this always
	 * returns 0
	 */
	public double getMinY() {
		return 0.;
	}

	/**
	 * Get the maximum "y" value. The y axis corresponds to "counts", so this always
	 * returns the count of the bin with the most counts.
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
	 * @param value the value to ad
	 */
	public void add(double value) {
		_stats = null;
		int bin = getBin(value);
		if (bin == UNDERFLOW) {
			_underCount++;
		}
		else if (bin == OVERFLOW) {
			_overCount++;
		}
		else {
			_counts[bin]++;
		}

	}

	/**
	 * Set a bin to a given count
	 * 
	 * @param val   the x val will determine bin
	 * @param count the count
	 */
	public void setCount(double val, int count) {
		_stats = null;
		int bin = getBin(val);
		if (bin == UNDERFLOW) {
			_underCount += count;
		}
		else if (bin == OVERFLOW) {
			_overCount += count;
		}
		else {
			_counts[bin] = count;
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
	 * @param bin the bin in question
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
	 * @param bin the bin in question
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
	 * @param bin the bin in question
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
	 * @param val the value.
	 * @return return the bin: [0..(numBin-1)] or an error
	 */
	public int getBin(double val) {
		if (val < getMinX()) {
			return UNDERFLOW;
		}
		else if (val > getMaxX()) {
			return OVERFLOW;
		}

		int index = Arrays.binarySearch(_grid, val);
		// unlikely, but maybe we are exactly on a value
		if (index < 0) {
			index = -(index + 1); // now the insertion point.
		}
		int bin = index - 1;

		if (bin < 0) {
			// System.err.println("Fix negative bin " + bin);
			bin = 0;
		}
		return bin;
	}

	/**
	 * Get the status string
	 * 
	 * @param canvas     the plot canvas
	 * @param histo      the histo data object
	 * @param mousePoint where the mouse is
	 * @param wp         the data coordinates of the mouse
	 * @return a status string
	 */
	public static String statusString(PlotCanvas canvas, HistoData histo, Point mousePoint, Point.Double wp) {
		String s = null;

		Polygon poly = GetPolygon(canvas, histo);
		if (poly.contains(mousePoint)) {
			int bin = histo.getBin(wp.x);

			PlotParameters params = canvas.getParameters();
			String minstr = DoubleFormat.doubleFormat(histo.getBinMinX(bin), params.getNumDecimalX(),
					params.getMinExponentX());
			String maxstr = DoubleFormat.doubleFormat(histo.getBinMaxX(bin), params.getNumDecimalX(),
					params.getMinExponentX());
			
			String name = histo.getName();
			if ((name != null) && (name.length() > 0)) {
				name = "[" + name + "]";
			}
			else {
				name = "";
			}

			s = name + " bin: " + bin + " [" + minstr + " - " + maxstr + "]";
			s += " counts: " + histo.getCount(bin);
		}

		return s;
	}

	/**
	 * Get the drawing polygon
	 * 
	 * @param canvas the drawing canvas
	 * @param histo  the histo data
	 * @return the polygon
	 */
	public static Polygon GetPolygon(PlotCanvas canvas, HistoData histo) {
		Polygon poly = new Polygon();
		long counts[] = histo.getCounts();
		Point pp = new Point();
		Point.Double wp = new Point.Double();

		for (int bin = 0; bin < histo.getNumberBins(); bin++) {
			double xmin = histo.getBinMinX(bin);
			double xmax = histo.getBinMaxX(bin);
			double y = counts[bin];

			if (bin == 0) {
				wp.setLocation(xmin, 0);
				canvas.worldToLocal(pp, wp);
				poly.addPoint(pp.x, pp.y);
			}

			wp.setLocation(xmin, y);
			canvas.worldToLocal(pp, wp);
			poly.addPoint(pp.x, pp.y);
			wp.setLocation(xmax, y);
			canvas.worldToLocal(pp, wp);
			poly.addPoint(pp.x, pp.y);

			if (bin == (histo.getNumberBins() - 1)) {
				wp.setLocation(xmax, 0);
				canvas.worldToLocal(pp, wp);
				poly.addPoint(pp.x, pp.y);
			}
		}
		return poly;
	}

	/**
	 * Set whether we use rms or sigma in histogram legends
	 * 
	 * @param useRMS if <code>true</code> use rms, else use sigma
	 */
	public void setRmsInHistoLegend(boolean useRMS) {
		_rmsInHistoLegend = useRMS;
	}

	/**
	 * Check whether we use rms or sigma in histogram legends
	 * 
	 * @return <code>true</code> use rms, else use sigma
	 */
	public boolean useRmsInHistoLegend() {
		return _rmsInHistoLegend;
	}

	/**
	 * Set whether we draw sqrt(n) statistical errors
	 * 
	 * @param statErr if <code>true</code> draw statistical errors
	 */
	public void setDrawStatisticalErrors(boolean statErr) {
		_statErrors = statErr;
	}

	/**
	 * Check whether we draw sqrt(n) statistical errors
	 * 
	 * @return <code>true</code> if <code>true</code> draw statistical errors
	 */
	public boolean drawStatisticalErrors() {
		return _statErrors;
	}

	public String maxBinString() {
		long maxCount = -1;
		long counts[] = getCounts();
		for (long lv : counts) {
			maxCount = Math.max(maxCount, lv);
		}
		if (maxCount < 1) {
			return "";
		}

		String s = "Max count: " + maxCount + " in 1-based bin(s):";
		for (int bin = 0; bin < getNumberBins(); bin++) {

			if (counts[bin] == maxCount) {
				s += " " + (bin + 1); // make it 1-based
			}
		}
		return s;
	}
}
