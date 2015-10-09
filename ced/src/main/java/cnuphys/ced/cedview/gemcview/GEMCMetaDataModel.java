package cnuphys.ced.cedview.gemcview;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import cnuphys.ced.clasio.ClasIoEventManager;

public class GEMCMetaDataModel extends DefaultTableModel {

	// the event manager
	protected ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	protected static final String colNames[] = { "Option", "Value" };

	protected static final int columnWidths[] = { 170, // key
			400 // value
	};

	// the data [key, val, key, val, ...]
	private Vector<String> _data;

	public GEMCMetaDataModel() {
		super(colNames, 200);
	}

	/**
	 * Get the number of columns
	 * 
	 * @return the number of columns
	 */
	@Override
	public int getColumnCount() {
		return colNames.length;
	}

	/**
	 * Get the number of rows
	 * 
	 * @return the number of rows
	 */
	@Override
	public int getRowCount() {
		if ((_data == null) || (_data.size() < 1)) {
			return 0;
		}

		int rowcount = 1 + ((_data.size() - 1) / 2);
		return rowcount;
	}

	/**
	 * Get the value at a given row and column
	 * 
	 * @return the value at a given row and column
	 */
	@Override
	public Object getValueAt(int row, int col) {

		if ((_data == null) || (row >= getRowCount())) {
			return null;
		}

		int keyIndex = row * 2;

		return (col == 0) ? _data.get(keyIndex) : _data.get(keyIndex + 1);
	}

	/**
	 * Clear all the data
	 */
	public void clear() {
		System.err.println("CLEARING TABLE B");
		if (_data != null) {
			_data.clear();
		}
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(Vector<String> data) {
		_data = data;
		fireTableDataChanged();
	}

	/**
	 * Forces the cell to not be editable.
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

}
