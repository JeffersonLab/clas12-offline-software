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

import org.jlab.coda.jevio.EvioNode;
import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.EvioNodeSupport;
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
	public EvioNode getNode(int row) {
		return getNodeModel().getNode(row);
	}

	/**
	 * Set the model data based on a clasIO EvioDataEvent
	 * 
	 * @param event
	 *            the event
	 */
	public void setData(EvioDataEvent event) {
		getNodeModel().setData(EvioNodeSupport.getNodes(event));
	}

	class CustomRenderer extends DefaultTableCellRenderer {

		private NodeTable _table;

		private Font ifont = new Font("Dialog", Font.ITALIC, 10);
		private Font pfont = new Font("Dialog", Font.PLAIN, 10);

		public CustomRenderer(NodeTable table) {
			_table = table;
			setHorizontalAlignment(SwingConstants.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component cellComponent = super.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, column);

			cellComponent.setFont(pfont);

			EvioNode node = _table.getNode(row);
			if (node == null) {
				cellComponent.setBackground(Color.red);
				cellComponent.setForeground(Color.white);
			} else {
				if (EvioNodeSupport.bankOfBanks(node)) {
					cellComponent.setFont(ifont);
					// cellComponent.setBackground(Color.black);
					// cellComponent.setForeground(Color.yellow);
					cellComponent.setBackground(Color.lightGray);
					cellComponent.setForeground(Color.darkGray);
				} else { // not a bank of banks
					if (isSelected) {
						cellComponent.setBackground(Color.yellow);
						cellComponent.setForeground(Color.black);
					} else if (EvioNodeSupport.isLeaf(node)) {
						cellComponent.setBackground(X11Colors
								.getX11Color("alice blue"));
						cellComponent.setForeground(X11Colors
								.getX11Color("dark blue"));
					} else { // shouldn't happen
						cellComponent.setBackground(Color.darkGray);
						cellComponent.setForeground(Color.lightGray);
					}
				} // end leaf
			}

			return cellComponent;
		}
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
