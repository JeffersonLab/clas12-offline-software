package cnuphys.splot.pdata;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Vector;

import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;
import cnuphys.splot.fit.Fit;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.plot.DoubleFormat;
import cnuphys.splot.style.IStyled;
import cnuphys.splot.style.Styled;
import cnuphys.splot.style.SymbolType;

/**
 * This is essentially a table of not necessarily equal length columns.
 * 
 * @author heddle
 * 
 */
public class DataSet extends DefaultTableModel {

	// the data
	Vector<DataColumn> _columns = new Vector<DataColumn>();

	// List of dataset change listeners
	private EventListenerList _listenerList;

	// the dataset type
	private DataSetType _type = DataSetType.UNKNOWN;

	// most sets do not have x errors
	private boolean _hasXErrors;

	/**
	 * This constructor is for a 2D histogram
	 * 
	 * @param h2d
	 */
	public DataSet(Histo2DData h2d) throws DataSetException {
		if (h2d == null) {
			throw (new DataSetException("Must supply at least one histogram data object."));
		}

		_type = DataSetType.H2D;

		// just one column
		_columns.add(new DataColumn(DataColumnType.Y, h2d.getName()));
		getColumn(0).setHistoData2D(h2d);
		getColumn(0).initStyle();
		getColumn(0).getStyle().setSymbolType(SymbolType.NOSYMBOL);
		getColumn(0).initFit();

	}

	/**
	 * This constructor is used for 1D histograms
	 * 
	 * @param histos an array of histo data objects
	 */
	public DataSet(HistoData... histos) throws DataSetException {
		if ((histos == null) || (histos.length < 1)) {
			throw (new DataSetException("Must supply at least one histogram data object."));
		}
		_type = DataSetType.H1D;

		int colCount = histos.length;

		// create a y column for each histogram
		for (int i = 0; i < colCount; i++) {
			HistoData hd = histos[i];
			if (hd != null) {
				_columns.add(new DataColumn(DataColumnType.Y, hd.getName()));
				getColumn(i).setHistoData(hd);
				getColumn(i).initStyle();
				getColumn(i).getStyle().setSymbolType(SymbolType.NOSYMBOL);
				getColumn(i).initFit();
			}
		}
	}

	/**
	 * Create a dataset for a simple strip chart
	 * 
	 * @param stripData the StripData data object
	 * @param colNames  should be just two of them
	 */
	public DataSet(StripData stripData, String... colNames) {
		_type = DataSetType.STRIP;
		_columns.add(new DataColumn(DataColumnType.X, colNames[0]));
		_columns.add(new DataColumn(DataColumnType.Y, colNames[1]));
		getColumn(1).initStyle();
		getColumn(1).initFit();
		getColumn(1).getFit().setFitType(FitType.STAIRS);
		stripData.setDataSet(this);
	}

	/**
	 * Create a dataset of a specific type
	 * 
	 * @param type     the dataset type
	 * @param colNames the column names. There should be one name for every expected
	 *                 column based on the type.
	 * @throws DataSetException When the number of columns is inconsistent with the
	 *                          type
	 */
	public DataSet(DataSetType type, String... colNames) throws DataSetException {
		super(colNames, 0);
		_type = type;
		int colCount = (colNames == null) ? 0 : colNames.length;

		switch (type) {

		case XYY: // shared x, any number of y
			_columns.add(new DataColumn(DataColumnType.X, colNames[0]));
			// the y columns, if any
			for (int i = 1; i < colCount; i++) {
				_columns.add(new DataColumn(DataColumnType.Y, colNames[i]));
				getColumn(i).initStyle();
				getColumn(i).initFit();
			}
			break;

		case XYXY:
			// repeated x,y columns. Number of columns should be divisible by
			// two

			if ((colCount % 2) != 0) {
				throw new DataSetException("The number of columns " + colCount + " is not divisible by 2.");
			}
			for (int i = 0; i < colCount / 2; i++) {
				int j = i * 2;
				_columns.add(new DataColumn(DataColumnType.X, colNames[j]));
				_columns.add(new DataColumn(DataColumnType.Y, colNames[j + 1]));
				getColumn(j + 1).initStyle();
				getColumn(j + 1).initFit();
			}
			break;

		case XYEXYE:
			// repeated x,y,yerr. Number of columns should be divisible by three

			if ((colCount % 3) != 0) {
				throw new DataSetException(
						"The number of columns for type XYEXYE " + colCount + " is not divisible by 3.");
			}
			for (int i = 0; i < colCount / 3; i++) {
				int j = i * 3;
				_columns.add(new DataColumn(DataColumnType.X, colNames[j]));
				_columns.add(new DataColumn(DataColumnType.Y, colNames[j + 1]));
				_columns.add(new DataColumn(DataColumnType.YERR, colNames[j + 2]));
				getColumn(j + 1).initStyle();
				getColumn(j + 1).initFit();
			}
			break;

		case XYEEXYEE:
			// repeated x,y,xerr, yerr. Number of columns should be divisible by
			// four

			_hasXErrors = true;
			if ((colCount % 4) != 0) {
				throw new DataSetException(
						"The number of columns for type XYEEXYEE " + colCount + " is not divisible by 4.");
			}
			for (int i = 0; i < colCount / 4; i++) {
				int j = i * 3;
				_columns.add(new DataColumn(DataColumnType.X, colNames[j]));
				_columns.add(new DataColumn(DataColumnType.Y, colNames[j + 1]));
				_columns.add(new DataColumn(DataColumnType.XERR, colNames[j + 2]));
				_columns.add(new DataColumn(DataColumnType.YERR, colNames[j + 3]));
				getColumn(j + 1).initStyle();
				getColumn(j + 1).initFit();
			}
			break;

		case H1D:
			throw (new DataSetException("Use DataSet(HistoData[]) constructor for 1D histograms."));

		default:
			break;
		}
	}
	
	/**
	 * Get the number of data points in the first column of
	 * a plot.
	 * @return the number of data points
	 */
	public long size() {

		long count = -1;
		if (getColumnCount() != 0) {
			DataColumn dc = _columns.firstElement();

			if (is1DHistoSet()) {
				HistoData hd = dc.getHistoData();
				count =  hd.getTotalCount();
			}
			else if (is2DHistoSet()) {
				Histo2DData h2d = dc.getHistoData2D();
				count =  h2d.getTotalCount();
			}
			else {
				count = dc.size();
			}
		}
		return count;
	}


	// XYY, XYXY, XYEXYE, XYEEXYEE, H1D, UNKNOWN;

	/**
	 * Add a curve to an existing data set. For now limited to type XYXY only.
	 * 
	 * @param xname the name for the new xdata (ignored for some types)
	 * @param yname the name for the new y data
	 */
	public DataColumn addCurve(String xname, String yname) {

		DataColumn newCurve = null;

		switch (getType()) {
		case XYY:
			System.err.println("[sPlot] Can not add curve for type: " + getType());
			break;

		case XYXY:
			addColumn(xname);
			addColumn(yname);
			_columns.add(new DataColumn(DataColumnType.X, xname));

			newCurve = new DataColumn(DataColumnType.Y, yname);
			newCurve.initFit();
			newCurve.initStyle();
			_columns.add(newCurve);
			break;

		case XYEXYE:
			System.err.println("[sPlot] Can not add curve for type: " + getType());
			break;

		case XYEEXYEE:
			System.err.println("[sPlot] Can not add curve for type: " + getType());
			break;

		case H1D:
			System.err.println("[sPlot] Can not add curve for type: " + getType());
			break;

		case STRIP:
			System.err.println("[sPlot] Can not add curve for type: " + getType());
			break;

		case UNKNOWN:
			System.err.println("[sPlot] Can not add curve for type: " + getType());
			break;
		}

		// Vector<String> colNames = new Vector<String>();
		// for (DataColumn dc : _columns) {
		// colNames.add(dc.getName());
		// }
		// setColumnIdentifiers(colNames);

		return newCurve;
	}

	/**
	 * Add data to a specific curve. For now limited to type XYXY only.
	 * 
	 */
	public void addToCurve(int curveIndex, double... vals) throws DataSetException {

		int count = getColumnCount();

		switch (getType()) {
		case XYY:
			System.err.println("[sPlot] Can not add to curve for type: " + getType());
			break;

		case XYXY:
			int curveCount = count / 2;
			if (curveIndex < curveCount) {
				DataColumn xcol = _columns.get(2 * curveIndex);
				DataColumn ycol = _columns.get(2 * curveIndex + 1);
				xcol.add(vals[0]);
				ycol.add(vals[1]);
			}
			else {
				System.err.println("[sPlot] Curve index out of range for type: " + getType());
			}
			break;

		case XYEXYE:
			System.err.println("[sPlot] Can not add to curve for type: " + getType());
			break;

		case XYEEXYEE:
			System.err.println("[sPlot] Can not add to curve for type: " + getType());
			break;

		case H1D:
			System.err.println("[sPlot] Can not add to curve for type: " + getType());
			break;

		case H2D:
			System.err.println("[sPlot] Can not add to curve for type: " + getType());
			break;

		case STRIP:
			System.err.println("[sPlot] Can not add to curve for type: " + getType());
			break;

		case UNKNOWN:
			System.err.println("[sPlot] Can not add to curve for type: " + getType());
			break;
		}
		notifyListeners();
	}

	/**
	 * Check to see if this data set has x errors
	 * 
	 * @return <code>true</code> if this set has x errors
	 */
	public boolean hasXErrors() {
		return _hasXErrors;
	}

	/**
	 * Check to see if this data set is for 1D Histograms
	 * 
	 * @return <code>true</code> if this is for 1D histograms
	 */
	public boolean is1DHistoSet() {
		return (_type == DataSetType.H1D);
	}

	/**
	 * Check to see if this data set is for 2D Histograms
	 * 
	 * @return <code>true</code> if this is for 2D histograms
	 */
	public boolean is2DHistoSet() {
		return (_type == DataSetType.H2D);
	}

	/**
	 * Set all fits to dirty.
	 */
	public void setAllFitsDirty() {
		for (DataColumn dc : _columns) {
			if (dc.getFit() != null) {
				dc.getFit().setDirty();
			}
		}
	}

	/**
	 * Get a curve from an index. E.g., f the index is 0 it will return the 1st Y
	 * column. Y Columns are also known as "curves".
	 * 
	 * @param index the index
	 * @return the corresponding curve
	 */
	public DataColumn getCurve(int index) {
		Vector<DataColumn> ycols = (Vector<DataColumn>) getAllColumnsByType(DataColumnType.Y);

		if ((ycols == null) || (index >= ycols.size())) {
			return null;
		}

		return ycols.get(index);
	}

	/**
	 * Get a x column from an index. E.g., f the index is 0 it will return the 1st X
	 * column.
	 * 
	 * @param index the index
	 * @return the corresponding X column
	 */
	public DataColumn getXColumn(int index) {
		Vector<DataColumn> xcols = (Vector<DataColumn>) getAllColumnsByType(DataColumnType.X);

		if ((xcols == null) || (index >= xcols.size())) {
			return null;
		}

		return xcols.get(index);
	}

	/**
	 * Get the style for the curve at the given index.
	 * 
	 * @param index the curve index.
	 * @return the style
	 */
	public IStyled getCurveStyle(int index) {
		DataColumn curve = getCurve(index);
		return (curve == null) ? null : curve.getStyle();
	}

	/**
	 * Compute the "standard" standard deviation (divide variance by N) using an
	 * accurate one-pass method.
	 * 
	 * @param x the data
	 * @return the standard deviation
	 */
	public static double standardDev(double x[]) {
		if ((x == null) || (x.length == 0)) {
			return Double.NaN;
		}

		int n = x.length;
		if (n == 1) {
			return 0;
		}

		double m = x[0];
		double q = 0;
		for (int k = 2; k <= n; k++) {
			double fac = (x[k - 1] - m);
			double fac2 = fac / k;
			m = m + fac2;
			q = q + (k - 1) * fac * fac2;
		}
		double var = q / n;

		if (var <= 0.) {
			return 0.0;
		}
		return Math.sqrt(var);

	}

	/**
	 * Get the DataSet type
	 * 
	 * @return the data set type
	 */
	public DataSetType getType() {
		return _type;
	}

	/**
	 * Get the minimum value of a column
	 * 
	 * @param index the index of the column
	 * @return the minimum value of the x data
	 */
	public double getColumnMin(int index) {
		return getColumn(index).getMinValue();
	}

	/**
	 * Get the maximum value of a column
	 * 
	 * @param index the index of the column
	 * @return the maximum value of the x data
	 */
	public double getColumnMax(int index) {
		return getColumn(index).getMaxValue();
	}

	/**
	 * Get a collection of all the curves that are set visible.
	 * 
	 * @return a collection of all the curves that are set visible.
	 */
	public Collection<DataColumn> getAllVisibleCurves() {
		Vector<DataColumn> v = new Vector<DataColumn>();
		for (DataColumn col : _columns) {
			if (col.getType() == DataColumnType.Y) {
				if (col.isVisible()) {
					v.add(col);
				}
			}
		}
		return v;
	}

	/**
	 * Get all curves, visible or not
	 * 
	 * @return a collection of all the curves, visible or not
	 */
	public Collection<DataColumn> getAllCurves() {
		Vector<DataColumn> v = new Vector<DataColumn>();
		for (DataColumn col : _columns) {
			if (col.getType() == DataColumnType.Y) {
				v.add(col);
			}
		}
		return v;
	}

	/**
	 * Get a count of all the curves
	 * 
	 * @return a count of all the curves
	 */
	public int getCurveCount() {
		Collection<DataColumn> v = getAllCurves();
		return v.size();
	}

	/**
	 * Has any data been added
	 * 
	 * @return <code>true<.code> if any data has been added.
	 */
	public boolean dataAdded() {
		for (DataColumn col : _columns) {
			if (col.getType() == DataColumnType.Y) {
				if (col.size() > 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get all the columns of a given type. For example, all the Y columns
	 * 
	 * @param type the type to match
	 * @return all the matching columns
	 */
	public Collection<DataColumn> getAllColumnsByType(DataColumnType type) {
		Vector<DataColumn> v = new Vector<DataColumn>();
		for (DataColumn col : _columns) {
			if (col.getType() == type) {
				v.add(col);
			}
		}
		return v;
	}

	/**
	 * Get all the histo data
	 * 
	 * @return a collection of the histo data
	 */
	public Vector<HistoData> getAllHistos() {
		Vector<HistoData> v = new Vector<HistoData>();
		if (is1DHistoSet()) {
			Collection<DataColumn> ycols = getAllColumnsByType(DataColumnType.Y);
			if (!ycols.isEmpty()) {
				for (DataColumn yc : ycols) {
					if (yc.isHistogram1D()) {
						v.add(yc.getHistoData());
					}
					else {
						System.err.println("All Y columns in a HistoDataset must be hitograms");
						System.exit(1);
					}
				}
			}
		}
		else {
			System.err.println("Should not be asking for histos in a non-histo dataset.");
			System.exit(1);
		}
		return v;
	}

	/**
	 * Convenience method to get the minimum value of the x data
	 * 
	 * @return the minimum value of the x data
	 */
	public double getXmin() {
		double xmin = Double.POSITIVE_INFINITY;
		if (is1DHistoSet()) {
			Vector<HistoData> histos = getAllHistos();
			if (!histos.isEmpty()) {
				for (HistoData hd : histos) {
					xmin = Math.min(xmin, hd.getMinX());
				}
			}
		}
		else {
			xmin = getDataMin(DataColumnType.X);
		}
		return xmin;
	}

	/**
	 * Convenience method to get the maximum value of the x data
	 * 
	 * @return the minimum value of the x data
	 */
	public double getXmax() {

		double xmax = Double.NEGATIVE_INFINITY;
		if (is1DHistoSet()) {
			Vector<HistoData> histos = getAllHistos();
			if (!histos.isEmpty()) {
				for (HistoData hd : histos) {
					xmax = Math.max(xmax, hd.getMaxX());
				}
			}
		}
		else {
			xmax = getDataMax(DataColumnType.X);
		}
		return xmax;
	}

	/**
	 * Convenience function to get the minimum value of the y data. If there is more
	 * than one y column, it returns the overall minimum.
	 * 
	 * @return the minimum value of the y data
	 */
	public double getYmin() {
		double ymin = Double.POSITIVE_INFINITY;
		if (is1DHistoSet()) {
			Vector<HistoData> histos = getAllHistos();
			if (!histos.isEmpty()) {
				for (HistoData hd : histos) {
					ymin = Math.min(ymin, hd.getMinY());
				}
			}
		}
		else {
			ymin = getDataMin(DataColumnType.Y);
		}
		return ymin;
	}

	/**
	 * Convenience function to get the maximum value of the y data. If there is more
	 * than one y column, it returns the overall maximum.
	 * 
	 * @return the maximum value of the y data
	 */
	public double getYmax() {
		double ymax = Double.NEGATIVE_INFINITY;
		if (is1DHistoSet()) {
			Vector<HistoData> histos = getAllHistos();
			if (!histos.isEmpty()) {
				for (HistoData hd : histos) {
					ymax = Math.max(ymax, hd.getMaxY());
				}
			}
		}
		else {
			ymax = getDataMax(DataColumnType.Y);
		}
		return ymax;
	}

	/**
	 * Get the overall min for all columns of the given type
	 * 
	 * @param type the type to match
	 * @return the overall min for the given type
	 */
	public double getDataMin(DataColumnType type) {
		if (is1DHistoSet()) {
			System.err.println("Should not be calling \"getDataMin\" for histo datasets");
			System.exit(1);
		}

		double vmin = Double.POSITIVE_INFINITY;

		for (DataColumn dc : _columns) {
			if (dc.getType() == type) {
				vmin = Math.min(vmin, dc.getMinValue());
			}
		}
		return vmin;
	}

	/**
	 * Get the overall max for all columns of the given type
	 * 
	 * @param type the type to match
	 * @return the overall max for the given type
	 */
	public double getDataMax(DataColumnType type) {
		if (is1DHistoSet()) {
			System.err.println("Should not be calling \"getDataMax\" for histo datasets");
			System.exit(1);
		}

		double vmax = Double.NEGATIVE_INFINITY;

		for (DataColumn dc : _columns) {
			if (dc.getType() == type) {
				vmax = Math.max(vmax, dc.getMaxValue());
			}
		}
		return vmax;
	}

	/**
	 * Get the minimal array of a column. The minimal array is a copy from the
	 * underlying GrowableArray that is the same size as the amount of real data.
	 * 
	 * @param index the column index
	 * @return the minimal array
	 */
	public double[] getMinimalArray(int index) {
		return getColumn(index).getMinimalCopy();
	}

	/**
	 * Get the size of the real data, This is the same as the row count.
	 * 
	 * @return the data count
	 */
	public int getSize() {
		return getRowCount();
	}

	/**
	 * Get the fit for a given column
	 * 
	 * @param index the column index
	 * @return the fit for the column
	 */
	public Fit getFit(int index) {
		return getColumn(index).getFit();
	}

	/**
	 * Add data values. This is the only entry point.
	 * 
	 * @param vals a variable number of entries for the columns
	 */
	public void add(double... vals) throws DataSetException {

		int count = (vals == null) ? 0 : vals.length;

		if (_type == DataSetType.H2D) {
			if (count == 2) {
				getColumn(0).histo2DAdd(vals[0], vals[1]);
			}
			return;
		}

		if (count > getColumnCount()) {
			String msg = "Expected " + getColumnCount() + " values in add, but got: " + count;
			throw new DataSetException(msg);
		}

		for (int i = 0; i < count; i++) {
			getColumn(i).add(vals[i]);
		}

		notifyListeners();
	}

	/**
	 * Add data value to a specific column.
	 * 
	 * @param column the column
	 * @param val    the value
	 */
	public void add(int column, double val) throws DataSetException {

		if (column >= getColumnCount()) {
			String msg = "In DataSet add, only " + getColumnCount() + " columns, but got column index of : " + column;
			throw new DataSetException(msg);
		}

		getColumn(column).add(val);

		notifyListeners();
	}

	/**
	 * Clear all the data.
	 */
	public void clear() {
		for (DataColumn dc : _columns) {
			dc.clear();
		}
		notifyListeners();
	}

	// notify listeners of a change in the data
	public void notifyListeners() {

//		fireTableDataChanged();

		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// This weird loop is the bullet proof way of notifying all listeners.
		// for (int i = listeners.length - 2; i >= 0; i -= 2) {
		// order is flipped so it goes in order as added
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == DataChangeListener.class) {
				DataChangeListener listener = (DataChangeListener) listeners[i + 1];
				listener.dataSetChanged(this);
			}

		}
	}

	/**
	 * Add a data change listener
	 * 
	 * @param DataChangeListener the listener to add
	 */
	public void addDataChangeListener(DataChangeListener DataChangeListener) {

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		// avoid adding duplicates
		_listenerList.remove(DataChangeListener.class, DataChangeListener);
		_listenerList.add(DataChangeListener.class, DataChangeListener);
	}

	/**
	 * Remove a DataChangeListener.
	 * 
	 * @param DataChangeListener the DataChangeListener to remove.
	 */

	public void removeDataChangeListener(DataChangeListener DataChangeListener) {

		if ((DataChangeListener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(DataChangeListener.class, DataChangeListener);
	}

	/**
	 * Get the style for a column
	 * 
	 * @param index the column index
	 * @return the style for this dataset
	 */
	public Styled getColumnStyle(int index) {
		return getColumn(index).getStyle();
	}

	/**
	 * Get all the data columns
	 * 
	 * @return all the data columns
	 */
	public Collection<DataColumn> getColumns() {
		return _columns;
	}

	/**
	 * Get the column at a specific index
	 * 
	 * @param index the column index
	 * @return the column at a specific index
	 */
	public DataColumn getColumn(int index) {
		return _columns.get(index);
	}

	@Override
	public int getRowCount() {

		int rowCount = 0;
		if (getColumnCount() != 0) {
			DataColumn dc = _columns.firstElement();

			if (is1DHistoSet()) {
				HistoData hd = dc.getHistoData();
				return hd.getNumberBins();
			}
			else if (is2DHistoSet()) {
				Histo2DData h2d = dc.getHistoData2D();
				return (h2d == null) ? 0 : 1;
			}
			else {
				rowCount = dc.size();
			}
		}
		return rowCount;
	}

	@Override
	public int getColumnCount() {
		return _columns == null ? 0 : _columns.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return DataColumn.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex >= getColumnCount()) {
			return false;
		}
		DataColumn col = getColumn(columnIndex);
		return rowIndex < col.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		DataColumn dc = getColumn(columnIndex);

		if (rowIndex >= dc.size()) {
			return "";
		}

		double val = dc.get(rowIndex);

		if (Double.isNaN(val)) {
			return "";
		}

		double split[] = intFract(-val);
		boolean asInt = (split[0] != 0.) && Math.abs(split[1]) < 1.0e-6;

		String s = asInt ? DoubleFormat.doubleFormat(val, 0) : DoubleFormat.doubleFormat(val, 4, 2);
		return s;
	}

	private static double[] intFract(double d) {
		BigDecimal bd = BigDecimal.valueOf(d);
		return new double[] { bd.intValue(), bd.remainder(BigDecimal.ONE).doubleValue() };
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		DataColumn dc = getColumn(columnIndex);
		dc.set(rowIndex, (Double) aValue);
	}
}
