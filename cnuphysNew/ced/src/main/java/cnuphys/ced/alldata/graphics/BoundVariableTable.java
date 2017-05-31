package cnuphys.ced.alldata.graphics;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

public class BoundVariableTable extends JTable {

	public BoundVariableTable() {
		super(new BoundVariableTableModel());
		
		// multiple selection
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// set preferred widths
		for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
			column.setPreferredWidth(BoundVariableTableModel.columnWidths[i]);
		}

		setDragEnabled(false);
		showVerticalLines = true;

		// no reordering
		getTableHeader().setReorderingAllowed(false);

	}
	
	public BoundVariableTableModel getBoundVariableModel() {
		return (BoundVariableTableModel) getModel();
	}

}
