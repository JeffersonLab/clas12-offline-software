package cnuphys.ced.clasio.table;

import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataManager;

public class NodeTableModel extends DefaultTableModel {

	// indices
	private static final int NAME_INDEX = 0;
	private static final int TYPE_INDEX = 1;
	private static final int COUNT_INDEX = 2;

	// the names of the columns
	protected static final String colNames[] = { "Name", "Type", "Count" };

	// the column widths
	protected static final int columnWidths[] = { 270, // name
			110, // type
			110, // length
	};

	// the model data
	private ArrayList<ColumnData> _data = new ArrayList<ColumnData>();

	// the current event
	private DataEvent _event;

	/**
	 * Constructor
	 */
	public NodeTableModel() {
		super(colNames, 2);
	}

	/**
	 * Find the row by the value in the name column
	 * 
	 * @param name
	 *            the column name to search for
	 * @return the row, or -1
	 */
	public int findRowByName(String name) {

		if ((name != null) && (_data != null) && !_data.isEmpty()) {
			int index = 0;
			for (ColumnData cd : _data) {
				if (name.equals(cd.getFullName())) {
					return index;
				}
				index++;
			}
		}

		return -1;
	}
	
	/**
	 * Get the event being displayed
	 * @return the event being displayed
	 */
	public DataEvent getCurrentEvent() {
		return _event;
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
		return (_data == null) ? 0 : _data.size();
	}

	/**
	 * Get the value at a given row and column
	 * 
	 * @param row
	 *            the 0-based row
	 * @param col
	 *            the 0-based column
	 * @return the value at a given row and column
	 */
	@Override
	public Object getValueAt(int row, int col) {

		if (row < getRowCount()) {
			ColumnData cd = _data.get(row);

			// System.err.println("TYPE: " + node.getDataType() + " " +
			// DataType.getName(node.getDataType()));

			if (cd != null) {
				switch (col) {
				case NAME_INDEX:
					return cd.getFullName();

				case TYPE_INDEX:
					return cd.getTypeName();

				case COUNT_INDEX:
					return "" + cd.length(_event);

				default:
					return "?";
				}
			}
		}

		return "";
	}

	/**
	 * Clear all the data
	 */
	public void clear() {
		_data = new ArrayList<ColumnData>();
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(DataEvent event) {
		clear();
		_event = event;

		if (event != null) {
			String banks[] = event.getBankList();
			if (banks != null) {
				for (String bank : banks) {
					
					_data = DataManager.getInstance().hasData(event);
					
//					String columns[] = event.getColumnList(bank);
//					if (columns != null) {
//						for (String column : columns) {
//							ColumnData cd = DataManager.getInstance().getColumnData(bank, column);
//							if (cd != null) {
//								_data.add(cd);
//							}
//						}
//					}
				}
			}
		}
		fireTableDataChanged();
	}

	/**
	 * Get the column name of the bank column at the given row
	 * 
	 * @param row
	 *            the row in question
	 * @return the corresponding data bank column name, or <code>null</code>
	 */
	public ColumnData getColumnData(int row) {
		if (row < 0) {
			return null;
		}
		return (_data == null) ? null : _data.get(row);
	}

	/**
	 * Forces the cell to not be editable.
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

}
