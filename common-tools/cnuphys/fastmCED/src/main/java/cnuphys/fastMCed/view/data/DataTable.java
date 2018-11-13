package cnuphys.fastMCed.view.data;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.jlab.geom.DetectorId;

import cnuphys.lund.HeaderRenderer;
import cnuphys.lund.SimpleRenderer;

public class DataTable extends JTable {
	
	// a scroll pane for this table
	private JScrollPane _scrollPane;

	public DataTable(DetectorId detector) {
		super(new DataTableModel(detector));

		setFont(new Font("SansSerif", Font.PLAIN, 10));
		HeaderRenderer hrender = new HeaderRenderer();
		SimpleRenderer renderer = new SimpleRenderer();

		// set preferred widths
		TableColumn column = null;
		for (int i = 0; i < getColumnCount(); i++) {
			column = getColumnModel().getColumn(i);
			column.setCellRenderer(renderer);
			column.setHeaderRenderer(hrender);
			column.setPreferredWidth(DataTableModel.columnWidths[i]);
		}

		setGridColor(Color.lightGray);
		showVerticalLines = true;

	}

	/**
	 * Get the data model.
	 * 
	 * @return the data model.
	 */
	public DataTableModel getDataModel() {
		return (DataTableModel) getModel();
	}

	/**
	 * Clear all the data from the table
	 */
	public void clear() {
		getDataModel().clear();
	}

	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane(this);
		}
		return _scrollPane;
	}

}