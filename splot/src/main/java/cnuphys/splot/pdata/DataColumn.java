package cnuphys.splot.pdata;

import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import cnuphys.splot.fit.Fit;
import cnuphys.splot.style.Styled;
import cnuphys.splot.xml.XmlPrintStreamWritable;
import cnuphys.splot.xml.XmlPrintStreamWriter;
import cnuphys.splot.xml.XmlSupport;

public class DataColumn extends GrowableArray
		implements XmlPrintStreamWritable {

	/** The XML root element name */
	public static final String XmlRootElementName = "DataColumn";

	// diagnostic counter
	private static int _count = 0;

	// drawing style
	private Styled _style;

	// the fit (Y columns only)
	protected Fit _fit;

	// basic data
	public static final String XmlDataColBasicDataElementName = "DataColumnBasicData";
	public static final String XmlDataColTypeAttName = "dctype";
	public static final String XmlDataColNameAttName = "dcname";
	public static final String XmlDataColIsHistoAttName = "dcishisto";
	public static final String XmlDataColVisibleAttName = "dcvisible";

	// the column name
	protected String _name;

	// data type
	protected DataColumnType _type;

	// determines whether this column is shown. Only relevant
	// for Y columns. Can be used to hide a curve
	protected boolean _visible = true;

	// keep some running sums for getting variance
	private double _sumVal;
	private double _M;
	private double _Q;

	// used by 1D histograms
	private HistoData _histoData;

	// set if this is a histogram
	private boolean _isHisto = false;

	/**
	 * Creates a DataColumn with initial capacity 100 and increment 100.
	 * 
	 * @param name the column name
	 */
	public DataColumn(DataColumnType type, String name) {
		this(type, name, 100, 100);
	}

	/**
	 * Create a DataColumn
	 * 
	 * @param name the column name
	 * @param initCap the initial capacity
	 * @param increment the increment when the array grows.
	 */
	public DataColumn(DataColumnType type, String name, int initCap,
			int increment) {
		super(initCap, increment);
		_type = type;
		_name = name;
		clear();
	}

	/**
	 * This gets the histogram data, which for non-histograms will be
	 * <code>null</code>.
	 * 
	 * @return the histogram data
	 */
	public HistoData getHistoData() {
		return _histoData;
	}

	/**
	 * Set the histogram data container
	 * 
	 * @param histo the histogram data
	 */
	protected void setHistoData(HistoData histo) {
		if (histo != null) {
			_isHisto = true;
			_histoData = histo;
		}
	}

	/**
	 * Checks whether this is a histogram
	 * 
	 * @return <code>true</code> if this is a histogram
	 */
	public boolean isHistogram() {
		return _isHisto;
	}

	/**
	 * Check whether this "curve" is visible. Only relevant for Y columns.
	 * 
	 * @return the visibility flag
	 */
	public boolean isVisible() {
		return _visible;
	}

	/**
	 * Set the name
	 * 
	 * @param name the name of the data column
	 */
	public void setName(String name) {
		_name = name;
		if (_isHisto) {
			_histoData.setName(name);
		}
	}

	/**
	 * Get the name of the array
	 * 
	 * @return the name of the array
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Set whether this "curve" is visible. Only relevant for Y columns.
	 * 
	 * @param visible the visibility flag.
	 */
	public void setVisible(boolean visible) {
		_visible = visible;
	}

	/**
	 * Get the type of this data column
	 * 
	 * @return the type of this data column
	 */
	public DataColumnType getType() {
		return _type;
	}

	/**
	 * Init the style sort of randomly
	 */
	protected void initStyle() {
		_style = new Styled(_count++);
	}

	/**
	 * Initialize a fit object
	 */
	protected void initFit() {
		_fit = new Fit();
	}

	/**
	 * Set the style
	 * 
	 * @param style the new style
	 */
	protected void setStyle(Styled style) {
		_style = style;
	}

	/**
	 * Get the style for this column
	 * 
	 * @return the style for this column
	 */
	public Styled getStyle() {
		return _style;
	}

	/**
	 * Get a string representation
	 * 
	 * @return a string representation
	 */
	@Override
	public String toString() {
		return _name;
	}

	/**
	 * Get the Fit
	 * 
	 * @return the fit
	 */
	public Fit getFit() {
		return _fit;
	}

	/**
	 * Add a value to the array, growing it if necessary.
	 * 
	 * @param val the value to add
	 */
	@Override
	public void add(double val) {

		if (_isHisto) {
			_histoData.add(val);
		}
		else {
			super.add(val);
		}

		if (_fit != null) {
			_fit.setDirty();
		}

		// some running sums for statistics

		long n;
		if (_isHisto) {
			n = _histoData.getTotalCount();
		}
		else {
			n = size();
		}
		_sumVal += val;

		if (n == 1) {
			_M = val;
			_Q = 0;
		}
		else {
			double fac = (val - _M);
			double fac2 = fac / n;
			_M = _M + fac2;
			_Q = _Q + (n - 1) * fac * fac2;
		}
	}

	/**
	 * Get the mean
	 * 
	 * @return the mean
	 */
	public double getMean() {
		long n;
		if (_isHisto) {
			n = _histoData.getTotalCount();
		}
		else {
			n = size();
		}

		if (n == 0) {
			return Double.NaN;
		}
		return _sumVal / n;
	}

	/**
	 * Get the variance. Note: divides by N, not N-1
	 * 
	 * @return the variance.
	 */
	public double getVariance() {
		long n;
		if (_isHisto) {
			n = _histoData.getTotalCount();
		}
		else {
			n = size();
		}

		if (n == 0) {
			return Double.NaN;
		}
		else if (n == 1) {
			return 0.;
		}
		else {
			return _Q / n;
		}
	}

	/**
	 * Get the standard deviation
	 * 
	 * @return the standard deviation
	 */
	public double getStandardDeviation() {
		double var = getVariance();

		if (Double.isNaN(var)) {
			return Double.NaN;
		}
		if (var <= 0.) {
			return 0.0;
		}

		return Math.sqrt(var);
	}

	/**
	 * Reset the data array to the initial capacity and fill with all NaNs.
	 */
	@Override
	public void clear() {
		super.clear();
		if (_isHisto) {
			_histoData.clear();
		}

		if (_fit != null) {
			_fit.setDirty();
		}
	}

	/**
	 * Set the value at the given index
	 * 
	 * @param index the index
	 * @param val the value at the index
	 */
	@Override
	public void set(int index, double val) {
		// TODO something for histograms this is called from datatable
		super.set(index, val);
		if (_fit != null) {
			_fit.setDirty();
		}
	}

	@Override
	public void writeXml(XmlPrintStreamWriter writer) {
		try {
			writer.writeStartElement(XmlRootElementName);
			writeBasicData(writer);

			// x columns have no style
			if (_style != null) {
				_style.writeXml(writer);
			}

			if (_fit != null) {
				_fit.writeXml(writer);
			}

			if (_histoData != null) {
				_histoData.writeXml(writer);
			}
			else {
				super.writeXml(writer);
			}
			writer.writeEndElement();

		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}

	// write a little basic data
	private void writeBasicData(XmlPrintStreamWriter writer)
			throws XMLStreamException {
		Properties props = new Properties();
		props.put(XmlDataColTypeAttName, _type);
		props.put(XmlDataColNameAttName, XmlSupport.validXML(_name));
		props.put(XmlDataColIsHistoAttName, _isHisto);
		props.put(XmlDataColVisibleAttName, _visible);
		writer.writeElementWithProps(XmlDataColBasicDataElementName, props);
	}

}
