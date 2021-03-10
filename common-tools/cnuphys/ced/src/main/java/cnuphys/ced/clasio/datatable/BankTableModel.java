package cnuphys.ced.clasio.datatable;

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataManager;

public class BankTableModel extends DefaultTableModel {

	private String _bankName;
	private String _columnNames[];

	// parent table
	private BankDataTable _table;

	// the data
	private DataEvent _event;

	public BankTableModel(String bankName) {
		super(getColumnNames(bankName), 2);
		_bankName = bankName;
		_columnNames = getColumnNames(bankName);
	}

	/**
	 * Provide a link to the owner table
	 * 
	 * @param table the owner table
	 */
	public void setTable(BankDataTable table) {
		_table = table;
	}

	/**
	 * Get the number of columns
	 * 
	 * @return the number of columns
	 */
	@Override
	public int getColumnCount() {
		return _columnNames.length;
	}

	public void setData(DataEvent event) {
		_event = event;
		fireTableDataChanged();
	}

	/**
	 * Get the number of rows
	 * 
	 * @return the number of rows
	 */
	@Override
	public int getRowCount() {
		if (_event == null) {
			return 0;
		}

		ArrayList<ColumnData> cds = DataManager.getInstance().hasData(_event, _bankName);
		int rowCount = 0;

		for (ColumnData cd : cds) {

			if (cd != null) {
				rowCount = Math.max(rowCount, cd.getLength(_event));
			}
		}

		return rowCount;
	}

	/**
	 * Get the value at a given row and column
	 * 
	 * @param row the 0-based row
	 * @param col the 0-based column
	 * @return the value at a given row and column
	 */
	@Override
	public Object getValueAt(int row, int col) {
		if ((row < 0) || (col < 0)) {
			return null;
		}

		if (col == 0) {
			return " " + (row + 1);
		}

		ColumnData cd = DataManager.getInstance().getColumnData(_bankName, _columnNames[col]);
		if (cd == null) {
			return "???";
		}
		int len = cd.getLength(_event);
		if ((len == 0) || (row >= len)) {
			return "";
		}

		String fullName = cd.getFullName();

		switch (cd.getType()) {
		case ColumnData.INT8:
			return "" + DataManager.getInstance().getByteArray(_event, fullName)[row];
		case ColumnData.INT16:
			return "" + DataManager.getInstance().getShortArray(_event, fullName)[row];
		case ColumnData.INT32:
			return "" + DataManager.getInstance().getIntArray(_event, fullName)[row];
		case ColumnData.INT64:
			return "" + DataManager.getInstance().getLongArray(_event, fullName)[row];
		case ColumnData.FLOAT32:
			float f = DataManager.getInstance().getFloatArray(_event, fullName)[row];
			;
			return DoubleFormat.doubleFormat(f, 5, 4);
		case ColumnData.FLOAT64:
			double d = DataManager.getInstance().getDoubleArray(_event, fullName)[row];
			;
			return DoubleFormat.doubleFormat(d, 5, 4);
		default:
			return "???";
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Get the bank name
	 * 
	 * @return the bank name
	 */
	public String getBankName() {
		return _bankName;
	}

	/**
	 * Get the table column names, which are the names of all the known banks for
	 * this bank
	 * 
	 * @return the tames of all known data columns for this bank
	 */
	public String[] columnNames() {
		return _columnNames;
	}

	// add an extra column name for index
	private static String[] getColumnNames(String bankName) {
		String cnames[] = DataManager.getInstance().getColumnNames(bankName);

		String expNames[] = new String[cnames.length + 1];
		expNames[0] = "";
		for (int i = 0; i < cnames.length; i++) {
			expNames[i + 1] = cnames[i];
		}

		return expNames;
	}
}
