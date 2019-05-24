package cnuphys.splot.pdata;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import cnuphys.splot.plot.Environment;

public class DataTable extends JTable {

	private double _currentValue = Double.NaN;

	// a scroll pane for this table
	private JScrollPane _scrollPane;

	// the table font
	protected static Font _tableFont = Environment.getInstance().getCommonFont(10);

	public DataTable(DataSet dataSet) {
		super(dataSet);
		getFontMetrics(_tableFont).stringWidth(" 9.99999E-99 ");
		setFont(_tableFont);

		// listen for clicks on header so we can sort
		JTableHeader header = getTableHeader();

		setRowHeight(getFontMetrics(_tableFont).getHeight() + 4);

		// selection
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setRowSelectionAllowed(false);
		setColumnSelectionAllowed(false);
		setCellSelectionEnabled(true);

		// header rendering
		header.setPreferredSize(new Dimension(100, 20));
		header.setBackground(Color.orange);
		header.setBorder(BorderFactory.createLineBorder(Color.black));
		DefaultTableCellRenderer dtcr = (DefaultTableCellRenderer) header.getDefaultRenderer();
		dtcr.setHorizontalAlignment(SwingConstants.CENTER);
		header.setDefaultRenderer(dtcr);

		getTableHeader().setResizingAllowed(true);
		setShowGrid(true);
		setGridColor(Color.gray);

		if (dataSet != null) {
			for (int i = 0; i < dataSet.getColumnCount(); i++) {
				TableColumn col = getColumnModel().getColumn(i);
				dtcr = new DefaultTableCellRenderer();
				dtcr.setHorizontalAlignment(SwingConstants.CENTER);
				col.setCellRenderer(dtcr);
				col.setCellEditor(new MyTableCellEditor());
			}
		}

		ListSelectionListener sl = new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				int row = getSelectedRow();
				int col = getSelectedColumn();

				if ((row >= 0) && (col >= 0)) {
					selectedCell(row, col);
				}

			}

		};

		// yes you have to do both
		getSelectionModel().addListSelectionListener(sl);
		getColumnModel().getSelectionModel().addListSelectionListener(sl);

	}

	protected void selectedCell(int row, int col) {
		try {
			_currentValue = Double.parseDouble((String) getModel().getValueAt(row, col));
		}
		catch (Exception e) {
			_currentValue = Double.NaN;
		}
	}

	/**
	 * Get the scroll pane for this data table
	 * 
	 * @return the scroll pane for this data table
	 */
	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane(this);
		}
		return _scrollPane;
	}

	// @Override
	// public Dimension getPreferredSize() {
	// return new Dimension(getModel().getColumnCount()*DEFCOLWIDTH, 500);
	// }

	/**
	 * main program for testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		javax.swing.JFrame testFrame = new javax.swing.JFrame("test frame");
		java.awt.Container cp = testFrame.getContentPane();
		testFrame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

		cp.setLayout(new BorderLayout(4, 0));

		String names[] = { "X1", "Y1", "E1", "X2", "Y2", "E2" };
		DataSet ds = null;
		try {
			ds = new DataSet(DataSetType.XYEXYE, names);
		}
		catch (DataSetException e1) {
			e1.printStackTrace();
			System.exit(1);
		}

		for (int i = 0; i < 15; i++) {
			try {
				if (i < 10) {
					ds.add(i, i + 2 * Math.random(), 2.0 * Math.random(), i + 0.5, 10 - i + 2 * Math.random(),
							2.0 * Math.random());
				}
				else {
					ds.add(i, i + 2 * Math.random(), 2.0 * Math.random());
				}
			}
			catch (DataSetException e) {
				e.printStackTrace();
				System.exit(1);
			}
			// ds.add(i, i);
		}

		DataTable table = new DataTable(ds);
		cp.add(table.getScrollPane(), BorderLayout.CENTER);

		testFrame.pack();
		testFrame.setVisible(true);
	}

	public class MyTableCellEditor extends AbstractCellEditor implements TableCellEditor {

		// This is the component that will handle the editing of the cell value
		JTextField component = new JTextField();

		int column;

		// This method is called when a cell value is edited by the user.
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex,
				int colIndex) {

			column = colIndex;
			component.setFont(_tableFont);
			component.setHorizontalAlignment(SwingConstants.CENTER);
			Environment.getInstance().commonize(component, Color.yellow);

			// Configure the component with the specified value
			component.setText((String) value);

			// Return the configured component
			return component;
		}

		// This method is called when editing is completed.
		// It must return the new value to be stored in the cell.
		@Override
		public Object getCellEditorValue() {
			double val = Double.NaN;
			try {
				val = Double.parseDouble(component.getText());

				if (val != _currentValue) {
					((DefaultTableModel) getModel()).fireTableDataChanged();
					DataColumn dc = null;
					DataSet ds = (DataSet) getModel();
					ds.setAllFitsDirty();
				}
			}
			catch (NumberFormatException e) {
				val = _currentValue;
			}
			return val;
		}
	}

}
