package cnuphys.ced.alldata.graphics;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

public class ExpressionTable extends JTable {

	public ExpressionTable(int selectionMode) {
		super(new ExpressionTableModel());
		
		// multiple selection
		setSelectionMode(selectionMode);

		// set preferred widths
		for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
			column.setPreferredWidth(ExpressionTableModel.columnWidths[i]);
		}

		setDragEnabled(false);
		showVerticalLines = true;

		// no reordering
		getTableHeader().setReorderingAllowed(false);

	}
	
	/**
	 * Get the Expression table model
	 * @return the model
	 */
	public ExpressionTableModel getExpressionModel() {
		return (ExpressionTableModel) getModel();
	}

	/**
	 * Get the selected (or first selected) named expression
	 * @return the selected (or first selected) named expression
	 */
	public NamedExpression getSelectedExpression() {
		NamedExpression ne = null;
		int row =getSelectedRow();
		
		if (row >= 0) {
			ne = getExpressionModel().getNamedExpression(row);
		}
		return ne;
	}
}
