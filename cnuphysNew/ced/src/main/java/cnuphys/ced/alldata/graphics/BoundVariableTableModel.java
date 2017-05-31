package cnuphys.ced.alldata.graphics;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class BoundVariableTableModel extends DefaultTableModel {

	/**
	 * Constant used to designate display column;
	 */
	public static final int VARNAME = 0;

	/**
	 * Constant used to designate name column;
	 */
	public static final int BCNAME = 1;

	// the names of the columns
	protected static final String colNames[] = { "Variable", "Bank and Column" };

	// the widths of the columns
	protected static final int columnWidths[] = { 90, 240 };

	public BoundVariableTableModel() {
		super(colNames, 3);
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
		Vector<NameBinding> data = getData();
		if (data == null) {
			return 0;
		}
		return data.size();
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Get the value at a given row and column
	 * 
	 * @return the value at a given row and column
	 */
	@Override
	public Object getValueAt(int row, int col) {

		NameBinding nb = getNameBinding(row);

		if (nb == null) {
			return null;
		}

		switch (col) {

		case VARNAME:
			return nb.varName;

		case BCNAME:
			return nb.bankColumnName;
		}

		return null;
	}
	
	
	/**
	 * Get the collection of bindings
	 * 
	 * @return the bindings
	 */
	public Vector<NameBinding> getData() {
		return DefinitionManager.getInstance().getBindings();
	}

	/**
	 * Get the name binding at a given row
	 * 
	 * @param row
	 *            the zero based row
	 * @return the NameBinding or null.
	 */
	public NameBinding getNameBinding(int row) {
		if (row < 0) {
			return null;
		}

		Vector<NameBinding> data = getData();
		if ((data == null) || (row >= data.size())) {
			return null;
		}

		return data.elementAt(row);
	}

}
