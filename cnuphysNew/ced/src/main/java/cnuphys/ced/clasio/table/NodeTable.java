package cnuphys.ced.clasio.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.util.Fonts;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.splot.plot.X11Colors;

public class NodeTable extends JTable {

	// a scroll pane for this table
	private JScrollPane _scrollPane;

	public NodeTable() {
		super(new NodeTableModel());

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
		dtcr.setBackground(X11Colors.getX11Color("wheat"));
		dtcr.setBackground(Color.black);
		dtcr.setFont(Fonts.defaultBoldFont);
		header.setDefaultRenderer(dtcr);

		getTableHeader().setResizingAllowed(true);
		setShowGrid(true);
		setGridColor(Color.gray);

		// custom rendering
		CustomRenderer renderer = new CustomRenderer(this);
		for (int i = 0; i < getColumnCount(); i++) {
			getColumnModel().getColumn(i).setCellRenderer(renderer);
		}

		// set preferred widths
		TableColumn column = null;
		for (int i = 0; i < getColumnCount(); i++) {
			column = getColumnModel().getColumn(i);
			column.setPreferredWidth(NodeTableModel.columnWidths[i]);
		}

	}

	/**
	 * Get the node data model.
	 * 
	 * @return the node data model.
	 */
	public NodeTableModel getNodeModel() {
		return (NodeTableModel) getModel();
	}

	/**
	 * Clear all the data from the table
	 */
	public void clear() {
		getNodeModel().clear();
	}

	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane(this);
		}
		return _scrollPane;
	}

	/**
	 * Get the node corresponding to the givens row
	 * 
	 * @param row
	 *            the row in question
	 * @return the corresponding node, or <code>null</code>
	 */
	public ColumnData getColumnData(int row) {
		return getNodeModel().getColumnData(row);
	}

	/**
	 * Set the model data based on a clasIO DataEvent
	 * 
	 * @param event
	 *            the event
	 */
	public void setData(DataEvent event) {
		getNodeModel().setData(event);
	}

	class CustomRenderer extends DefaultTableCellRenderer {

		private NodeTable _table;

		private Font ifont = Fonts.tweenItalicFont;
		private Font pfont = Fonts.tweenFont;

		public CustomRenderer(NodeTable table) {
			_table = table;
			setHorizontalAlignment(SwingConstants.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
					column);

			cellComponent.setFont(pfont);

			ColumnData cd = _table.getColumnData(row);
			if (cd == null) {
				cellComponent.setBackground(Color.red);
				cellComponent.setForeground(Color.white);
			} else {
				if (isSelected) {
					cellComponent.setBackground(Color.yellow);
					cellComponent.setForeground(Color.black);
				} else {
					if ((cd.bankIndex % 2) == 0) {
						cellComponent.setBackground(X11Colors.getX11Color("alice blue"));
						cellComponent.setForeground(X11Colors.getX11Color("dark blue"));
					}
					else {
						cellComponent.setBackground(X11Colors.getX11Color("misty rose"));
						cellComponent.setForeground(X11Colors.getX11Color("dark red"));
					}
				}
			}

			return cellComponent;
		}
	}
	
	/**
	 * Get the event being displayed
	 * @return the event being displayed
	 */
	public DataEvent getCurrentEvent() {
		return getNodeModel().getCurrentEvent();
	}


	/**
	 * Make sure the row with the given name is visible
	 * 
	 * @param name
	 *            the name to look for
	 */
	public void makeNameVisible(String name) {

		int row = getNodeModel().findRowByName(name);
		if (row >= 0) {
			scrollRectToVisible(getCellRect(getRowCount() - 1, 0, true));
			scrollRectToVisible(getCellRect(row, 0, true));
			getSelectionModel().setSelectionInterval(row, row);
		}
	}

}
