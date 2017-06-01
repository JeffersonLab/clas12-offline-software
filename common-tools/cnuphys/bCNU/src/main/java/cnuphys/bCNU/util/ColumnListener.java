/**
 * 
 */
package cnuphys.bCNU.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

/**
 * @author heddle
 */
public class ColumnListener extends MouseAdapter {

	// TODO fix to work with column rearrangement

	// parent table
	protected JTable _table;

	// ascending sort?
	protected boolean _ascendingSort;

	// what is the current sort column
	protected int _sortColumn;

	// will be notified to sort
	protected ITableSortListener _sortListener;

	public ColumnListener(JTable table, ITableSortListener sortListener) {
		_table = table;
		_sortListener = sortListener;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		TableColumnModel colModel = _table.getColumnModel();
		int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
		int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

		if (modelIndex < 0) {
			return;
		}

		// toggle sort direction?
		if (_sortColumn == modelIndex) {
			_ascendingSort = !_ascendingSort;
		} else {
			_sortColumn = modelIndex;
		}

		if (_sortListener != null) {
			_sortListener.sort(_table, _sortColumn, _ascendingSort);
		}
	}
}
