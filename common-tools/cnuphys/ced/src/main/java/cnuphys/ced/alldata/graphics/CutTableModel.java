package cnuphys.ced.alldata.graphics;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class CutTableModel extends DefaultTableModel {

	/**
	 * Constant used to designate display column;
	 */
	public static final int ACTIVE = 0;

	/**
	 * Constant used to designate name column;
	 */
	public static final int NAME = 1;

	// the names of the columns
	protected static final String colNames[] = { "", "" };

	// the widths of the columns
	protected static final int columnWidths[] = { 10, 150 };

	// the model data
	protected Vector<ICut> _data = new Vector<ICut>();

	/**
	 * Constructor
	 */
	public CutTableModel() {
		super(colNames, 3);
	}

	/**
	 * Constructor
	 */
	public CutTableModel(Vector<ICut> cutList) {
		super(colNames, 3);

		if (cutList != null) {
			setData(cutList);
		}
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
	
    @Override 
    public boolean isCellEditable(int row, int column) {
    	return (column == 0);
    }


	/**
	 * Set the value at a given row and column.
	 * 
	 * @param value The string to set.
	 * @param row The zero based row.
	 * @param col The zero based column.
	 */

	@Override
	public void setValueAt(Object value, int row, int col) {
		ICut icut = _data.elementAt(row);
		switch (col) {

		case ACTIVE:
			boolean enabled = (Boolean) value;
			icut.setActive(enabled);
			fireTableRowsUpdated(row, row);
			break;

		case NAME:
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

		ICut icut = _data.elementAt(row);

		if (icut == null) {
			return null;
		}

		switch (col) {

		case ACTIVE:
			return icut.isActive();

		case NAME:
			return icut.getName();
		}

		return null;
	}

	/**
	 * Add a new ICut into the table.
	 * 
	 * @param icut the new object to add to the model.
	 */
	public synchronized void add(ICut icut) {
		if (icut != null) {
			_data.add(icut);
		}
	}
	
	@Override
    public void removeRow(int row) {
		_data.removeElementAt(row);
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
	public synchronized void setData(Vector<ICut> data) {
		if (data == null) {
			data = new Vector<ICut>();
		}
		_data = data;
	}

	/**
	 * Get the ICut in the model at the given row.
	 * 
	 * @param row the zero based row
	 * @return the ICut corresponding to the row.
	 */
	public ICut getCutAtRow(int row) {
		if ((_data == null) || (row < 0)) {
			return null;
		}
		if (row >= _data.size()) {
			return null;
		}
		return _data.elementAt(row);
	}

}