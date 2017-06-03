package cnuphys.tinyMS.graphics;

import java.awt.FontMetrics;

import javax.swing.JTable;

public class RowHeightController {
	
	//slop
	private int _slop = 4;

	private JTable _table;
	private int[] _columnWidths;
	
	/**
	 * Controller for row height
	 * @param table
	 * @param columnWidths
	 */
	public RowHeightController(JTable table, int[] columnWidths) {
		_table = table;
		_columnWidths = columnWidths;
	}
	
	/**
	 * Fix the row heights of the table this is attacjed to
	 */
	public void fixRowHeights() {
		for (int row = 0; row < _table.getRowCount(); row++)  {
			fixRowHeight(row);
		}
	}
	
	//fix a single row height
	public void fixRowHeight(int row) {
		
		if ((row < 0) || (row >= _table.getRowCount())) {
			return;
		}
		
		FontMetrics fm =_table. getFontMetrics(_table.getFont());
		int rh = fm.getHeight() + _slop;
				
		for (int col = 0; col < _table.getColumnCount(); col++)  {
			int rrh = getRequiredRowHeight(row, col);
			rh = Math.max(rh,  rrh);
		}
		
		if (_table.getRowHeight() < rh) {
			_table.setRowHeight(row, rh);
		}
	}
	
	//get a required row height for a cell
	private int getRequiredRowHeight(int row, int col) {
		if ((row < 0) || (row >= _table.getRowCount())) {
			return 0;
		}
		
		if ((col < 0) || (col >= _table.getColumnCount())) {
			return 0;
		}

		FontMetrics fm =_table. getFontMetrics(_table.getFont());
		
		int fontHeight = fm.getHeight() + _slop;
		Object obj = _table.getModel().getValueAt(row, col);
		String s = (obj == null) ? "" : obj.toString();
		int sw = fm.stringWidth(s);
		
//		int cw = Math.max(1, getColumnModel().getColumn(col).getWidth());
		int cw = Math.max(1, (_columnWidths[col] - 4));
		
		int lines = sw /cw  + 1;    
		
		int height = fontHeight * lines;            

		return height;
	}
}
