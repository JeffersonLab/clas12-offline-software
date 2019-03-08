package cnuphys.splot.edit;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import cnuphys.splot.pdata.DataColumn;

public class CurveDataModel extends DefaultTableModel {

	/**
	 * Constant used to designate display column;
	 */
	public static final int VIS_COLUMN = 0;

	/**
	 * Constant used to designate name column;
	 */
	public static final int NAME_COLUMN = 1;

	// the names of the columns
	protected static final String colNames[] = { "Vis", "Curve Name" };

	// the widths of the columns
	protected static final int columnWidths[] = { 30, 220 };

	// the model data
	protected Vector<DataColumn> _data;

	/**
	 * Constructor
	 */
	public CurveDataModel() {
		super(colNames, 2);
	}

	/**
	 * Constructor
	 */
	public CurveDataModel(Vector<DataColumn> curves) {
		super(colNames, 2);
		setData(curves);
	}

	/**
	 * Get the number of columns
	 * 
	 * @return the number of columns
	 */
	@Override
	public int getColumnCount() {
		return columnWidths.length;
	}

	/**
	 * Get the number of rows
	 * 
	 * @return the number of rows
	 */
	@Override
	public int getRowCount() {
		if (_data == null) {
			return 0;
		}
		return _data.size();
	}

	/**
	 * Set the value at a given row and column.
	 * 
	 * @param value The string to set.
	 * @param row   The zero based row.
	 * @param col   The zero based column.
	 */

	@Override
	public void setValueAt(Object value, int row, int col) {
		DataColumn curve = _data.elementAt(row);
		switch (col) {

		case VIS_COLUMN:
			curve.setVisible((Boolean) value);
			break;

		case NAME_COLUMN:
			curve.setName((String) value);
			break;
		}

	}

	/**
	 * Get the value at a given row and column
	 * 
	 * @return the value at a given row and column
	 */
	@Override
	public Object getValueAt(int row, int col) {

		DataColumn curve = _data.elementAt(row);

		if (curve == null) {
			return null;
		}

		switch (col) {

		case VIS_COLUMN:
			return curve.isVisible();

		case NAME_COLUMN:
			return curve.getName();
		}

		return null;
	}

	/**
	 * Add a new curve into the table.
	 * 
	 * @param curve the new object to add to the model.
	 */
	public synchronized void add(DataColumn curve) {
		if (curve != null) {
			_data.add(curve);
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	/**
	 * remove an curve from the table.
	 * 
	 * @param curve the curveto remove.
	 */
	public synchronized void remove(DataColumn curve) {
		if (curve != null) {
			_data.remove(curve);
		}
	}

	/**
	 * Clear all the data
	 */
	public synchronized void clear() {
		if (_data != null) {
			_data.clear();
		}
	}

	/**
	 * @param data the data to set
	 */
	public synchronized void setData(Vector<DataColumn> data) {
		_data = data;
	}

	/**
	 * Get the curve (DataColumn) in the model at the given row.
	 * 
	 * @param row the zero based row
	 * @return the curve corresponding to the row.
	 */
	public DataColumn getCurveAtRow(int row) {
		if ((_data == null) || (row < 0)) {
			return null;
		}
		if (row >= _data.size()) {
			return null;
		}
		return _data.elementAt(row);
	}

}
