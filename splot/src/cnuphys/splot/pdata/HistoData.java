package cnuphys.splot.pdata;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import cnuphys.splot.plot.DoubleFormat;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.plot.UnicodeSupport;
import cnuphys.splot.xml.XmlPrintStreamWritable;
import cnuphys.splot.xml.XmlPrintStreamWriter;

/**
 * Xontainer class for histogram data
 * 
 * @author heddle
 *
 */
public class HistoData implements XmlPrintStreamWritable {

    /** The XML root element name */
    public static final String XmlRootElementName = "HistoData";

    // cached statistical results
    // an array with the mean in the 0 index,
    // standard deviation is the 1 index, and rms in the 2 index
    private double _stats[];

    // out of range constants
    private static int UNDERFLOW = -200;
    private static int OVERFLOW = -100;

    // basic data
    public static final String XmlHistoBasicDataElementName = "HistoBasicData";
    public static final String XmlHistoNameAttName = "histoname";
    public static final String XmlHistoUnderCountAttName = "histoundercount";
    public static final String XmlHistoOverCountAttName = "histoovercount";
    public static final String XmlHistoUniformBinsAttName = "histoiniformbins";
    public static final String XmlHistoNumBinsAttName = "histonumbins";
    public static final String XmlHistoBinMinAttName = "histoxmin";
    public static final String XmlHistoBinMaxAttName = "histoxmax";
    public static final String XmlHistoMeanAttName = "histomean";
    public static final String XmlHistoStdDevAttName = "histostddev";
    public static final String XmlHistoRMSAttName = "historms";

    // from checkboxes
    public static final String XmlHistoRMSLegendName = "histormslegend";
    public static final String XmlHistoDrawStatErrName = "histostaterr";

    // this is the "curve" name
    private String _name;

    // rebinning
    private int _rebinLimit;
    private boolean _rebinEnabled;

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

    // raw data kept until some limit
    private GrowableArray _rawData;

    /**
     * The data for a 1D histogram where the bin spacing is uniform.
     * 
     * @param name
     *            the curve name of the histogram
     * @param valMin
     *            the data min
     * @param valMax
     *            the data max
     * @param numBins
     *            the number of bins
     * @param rebinLimit
     *            store raw data to here, then store binned data only, at which
     *            point rebinning is disabled;
     */
    public HistoData(String name, double valMin, double valMax, int numBins,
	    int rebinLimit) {
	this(name, evenBins(valMin, valMax, numBins), rebinLimit);
	_uniformBins = true;
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
     * @param rebinLimit
     *            store raw data to here, then store binned data only, at which
     *            point rebinning is disabled;
     */
    public HistoData(String name, double[] grid, int rebinLimit) {
	_name = name;
	_grid = grid;
	_rebinLimit = rebinLimit;
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
     * @param bin
     *            the bin
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
	_rebinEnabled = _rebinLimit > 1;
	_rawData = null;
	if (_rebinEnabled) {
	    _rawData = new GrowableArray(1000, 1000);
	}
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
     * @return an array with the mean in the 0 index, standard deviation is the
     *         1 index, and rms in the 2 index
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
     * @param useRms
     *            if <code>true</code> use rms, else use sigma
     * @return a string with statistics info
     */
    public String statStr() {
	double res[] = getBasicStatistics();
	if (_rmsInHistoLegend) {
	    return String.format(UnicodeSupport.SMALL_MU + ": %-6.2f "
		    + "rms: %-6.2f", res[0], res[2]);
	} else {
	    return String.format(UnicodeSupport.SMALL_MU + ": %-6.2f "
		    + UnicodeSupport.SMALL_SIGMA + ": %-6.2f", res[0], res[1]);
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
     * Check whether rebinning is enabled
     * 
     * @return <code>true</code> if rebinning is enabled
     */
    public boolean isRebinEnabled() {
	return _rebinEnabled;
    }

    /**
     * Add a value to the histogram
     * 
     * @param value
     *            the value to ad
     */
    public void add(double value) {
	_stats = null;
	if (_rebinEnabled) {
	    if (_rawData.size() >= _rebinLimit) {
		System.err
			.println("Threshold exceeded, deleting raw data. Rebinning disabled");
		_rebinEnabled = false;
		_rawData = null;
	    } else {
		_rawData.add(value);
	    }
	}
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
     * Rebin with a new number of bins. This will create a uniform spacing
     * 
     * @param numBins
     *            the new number of bins.
     */
    public void rebin(int numBins) {
	if (!isRebinEnabled() || (numBins == getNumberBins())) {
	    System.err.println("Rebinning disabled.");
	    return;
	}
	_grid = evenBins(getMinX(), getMaxX(), numBins);
	reset();

	for (int i = 0; i < _rawData.size(); i++) {
	    double value = _rawData.get(i);
	    int bin = getBin(value);
	    if (bin == UNDERFLOW) {
		_underCount++;
	    } else if (bin == OVERFLOW) {
		_overCount++;
	    } else {
		_counts[bin]++;
	    }
	}
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

	if (bin < 0) {
	    // System.err.println("Fix negative bin " + bin);
	    bin = 0;
	}
	return bin;
    }

    /**
     * Get the status string
     * 
     * @param canvas
     *            the plot canvas
     * @param histo
     *            the histo data object
     * @param mousePoint
     *            where the mouse is
     * @param wp
     *            the data coordinates of the mouse
     * @return a status string
     */
    public static String statusString(PlotCanvas canvas, HistoData histo,
	    Point mousePoint, Point.Double wp) {
	String s = null;

	Polygon poly = GetPolygon(canvas, histo);
	if (poly.contains(mousePoint)) {
	    int bin = histo.getBin(wp.x);

	    PlotParameters params = canvas.getParameters();
	    String minstr = DoubleFormat.doubleFormat(histo.getBinMinX(bin),
		    params.getNumDecimalX(), params.getMinExponentX());
	    String maxstr = DoubleFormat.doubleFormat(histo.getBinMaxX(bin),
		    params.getNumDecimalX(), params.getMinExponentX());

	    s = "[" + histo.getName() + "] bin: " + bin + " [" + minstr + " - "
		    + maxstr + "]";
	    s += " counts: " + histo.getCount(bin);
	}

	return s;
    }

    /**
     * Get the drawing polygon
     * 
     * @param canvas
     *            the drawing canvas
     * @param histo
     *            the histo data
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
     * @param useRMS
     *            if <code>true</code> use rms, else use sigma
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
     * @param statErr
     *            if <code>true</code> draw statistical errors
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

    public static void main(String arg[]) {
	double grid[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	HistoData hd = new HistoData("Histo Example", grid, 1000000);

	for (int i = 0; i < 1000000; i++) {
	    double val = -2 + 14 * Math.random();
	    hd.add(val);
	}

	long goodCount = hd.getGoodCount();
	long under = hd.getUnderCount();
	long over = hd.getOverCount();
	System.err.println("Good Count: " + goodCount);
	System.err.println("Under Count: " + under);
	System.err.println("Over Count: " + over);
	System.err.println("Total Count: " + (goodCount + over + under));

	for (int i = 0; i < hd.getNumberBins(); i++) {
	    System.err.println("bin: " + i + "  counts: " + hd.getCounts()[i]
		    + "  mid val: " + hd.getBinMidValue(i));
	}

	System.err.println("\nREBINNING\n");
	hd.rebin(20);
	goodCount = hd.getGoodCount();
	under = hd.getUnderCount();
	over = hd.getOverCount();
	System.err.println("Good Count: " + goodCount);
	System.err.println("Under Count: " + under);
	System.err.println("Over Count: " + over);
	System.err.println("Total Count: " + (goodCount + over + under));

	for (int i = 0; i < hd.getNumberBins(); i++) {
	    System.err.println("bin: " + i + "  counts: " + hd.getCounts()[i]
		    + "  mid val: " + hd.getBinMidValue(i));
	}
	System.err.println("done.");
    }

    @Override
    public void writeXml(XmlPrintStreamWriter writer) {
	try {
	    writer.writeStartElement(XmlRootElementName);
	    writeBasicData(writer);
	    writeData(writer);
	    writer.writeEndElement();

	} catch (XMLStreamException e) {
	    e.printStackTrace();
	}
    }

    // write a little basic data
    private void writeBasicData(XmlPrintStreamWriter writer)
	    throws XMLStreamException {
	Properties props = new Properties();
	props.put(XmlHistoNameAttName, _name);
	props.put(XmlHistoUnderCountAttName, _underCount);
	props.put(XmlHistoOverCountAttName, _overCount);
	props.put(XmlHistoUniformBinsAttName, _uniformBins);
	props.put(XmlHistoNumBinsAttName, getNumberBins());
	props.put(XmlHistoBinMinAttName, getMinX());
	props.put(XmlHistoBinMaxAttName, getMaxX());
	props.put(XmlHistoMeanAttName, _stats[0]);
	props.put(XmlHistoStdDevAttName, _stats[1]);
	props.put(XmlHistoRMSAttName, _stats[2]);

	props.put(XmlHistoRMSLegendName, _rmsInHistoLegend);
	props.put(XmlHistoDrawStatErrName, _statErrors);

	writer.writeElementWithProps(XmlHistoBasicDataElementName, props);
    }

    private void writeData(XmlPrintStreamWriter writer)
	    throws XMLStreamException {
	writer.writeArray("histogrid", _grid);
	writer.writeArray("histocounts", _counts);
    }

}
