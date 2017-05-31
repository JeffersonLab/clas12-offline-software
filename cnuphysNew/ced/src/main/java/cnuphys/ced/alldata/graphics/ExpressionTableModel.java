package cnuphys.ced.alldata.graphics;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class ExpressionTableModel extends DefaultTableModel {

	/**
	 * Constant used to designate display column;
	 */
	public static final int ENAME = 0;

	/**
	 * Constant used to designate name column;
	 */
	public static final int ESTRING = 1;

	// the names of the columns
	protected static final String colNames[] = { "Name", "Expression" };

	// the widths of the columns
	protected static final int columnWidths[] = { 90, 240 };

	public ExpressionTableModel() {
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
		Vector<NamedExpression> data = getData();
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

		NamedExpression nb = getNamedExpression(row);

		if (nb == null) {
			return null;
		}

		switch (col) {

		case ENAME:
			return nb._expName;

		case ESTRING:
			return nb._expString;
		}

		return null;
	}
	
	

	/**
	 * Get the collection of expressions
	 * 
	 * @return the expressions
	 */
	public Vector<NamedExpression> getData() {
		return DefinitionManager.getInstance().getExpressions();
	}

	/**
	 * Get the named expression at a given row
	 * 
	 * @param row
	 *            the zero based row
	 * @return the NamedExpression or null.
	 */
	public NamedExpression getNamedExpression(int row) {
		if (row < 0) {
			return null;
		}

		Vector<NamedExpression> data = getData();
		if ((data == null) || (row >= data.size())) {
			return null;
		}

		return data.elementAt(row);
	}

}
