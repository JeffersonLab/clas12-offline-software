package cnuphys.ced.event.data;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

public class ExpressionTable extends JTable {

	public ExpressionTable() {
		super(new ExpressionTableModel());
		
		// multiple selection
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

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
	
	public ExpressionTableModel getExpressionModel() {
		return (ExpressionTableModel) getModel();
	}

}
