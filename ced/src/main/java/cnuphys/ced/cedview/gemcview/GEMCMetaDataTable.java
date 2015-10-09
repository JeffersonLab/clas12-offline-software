package cnuphys.ced.cedview.gemcview;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.splot.plot.X11Colors;

public class GEMCMetaDataTable extends JTable {

	// a scroll pane for this table
	private JScrollPane _scrollPane;

	public GEMCMetaDataTable() {
		super(new GEMCMetaDataModel());
		// single selection
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// header rendering
		JTableHeader header = getTableHeader();
		header.setPreferredSize(new Dimension(100, 20));
		header.setBackground(X11Colors.getX11Color("wheat"));
		header.setBorder(BorderFactory.createLineBorder(Color.black));
		DefaultTableCellRenderer dtcr = (DefaultTableCellRenderer) header
				.getDefaultRenderer();
		dtcr.setHorizontalAlignment(SwingConstants.CENTER);
		header.setDefaultRenderer(dtcr);

		getTableHeader().setResizingAllowed(true);
		setShowGrid(true);
		setGridColor(Color.gray);

		// set preferred widths
		TableColumn column = null;
		for (int i = 0; i < getColumnCount(); i++) {
			column = getColumnModel().getColumn(i);
			column.setPreferredWidth(GEMCMetaDataModel.columnWidths[i]);
		}

		_scrollPane = new JScrollPane(this);
		_scrollPane.setBackground(Color.red);
		_scrollPane.setBorder(new CommonBorder());
	}

	public static int preferredWidth() {
		int w = 10;
		for (Integer i : GEMCMetaDataModel.columnWidths) {
			w += i;
		}
		return w;
	}

	public JScrollPane getScrollPane() {
		return _scrollPane;
	}

	/**
	 * Get the trajectory data model.
	 * 
	 * @return the trajectory data model.
	 */
	public GEMCMetaDataModel getGEMCMetaDataModel() {
		return (GEMCMetaDataModel) getModel();
	}

	/**
	 * Clear all the data from the table
	 */
	public void clear() {
		System.err.println("CLEARING TABLE A");
		getGEMCMetaDataModel().clear();
	}

}
