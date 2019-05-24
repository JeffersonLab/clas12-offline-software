package cnuphys.lund;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

public class TrajectoryTable extends JTable {

	// a scroll pane for this table
	private JScrollPane _scrollPane;

	public TrajectoryTable() {
		super(new TrajectoryTableModel());

		setFont(new Font("SansSerif", Font.PLAIN, 10));
		HeaderRenderer hrender = new HeaderRenderer();
		SimpleRenderer renderer = new SimpleRenderer();

		// set preferred widths
		TableColumn column = null;
		for (int i = 0; i < getColumnCount(); i++) {
			column = getColumnModel().getColumn(i);
			column.setCellRenderer(renderer);
			column.setHeaderRenderer(hrender);
			column.setPreferredWidth(TrajectoryTableModel.columnWidths[i]);
		}

		setGridColor(Color.lightGray);
		showVerticalLines = true;

	}

	/**
	 * Get the trajectory data model.
	 * 
	 * @return the trajectory data model.
	 */
	public TrajectoryTableModel getTrajectoryModel() {
		return (TrajectoryTableModel) getModel();
	}

	/**
	 * Clear all the data from the table
	 */
	public void clear() {
		getTrajectoryModel().clear();
	}

	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane(this);
		}
		return _scrollPane;
	}
//
//	@Override
//	public Dimension getPreferredSize() {
//		int w = 0;
//		for (int i = 0; i < TrajectoryTableModel.columnWidths.length; i++) {
//			w += TrajectoryTableModel.columnWidths[i];
//		}
//		return new Dimension(w, 200);
//	}

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

		TrajectoryTable table = new TrajectoryTable();
		cp.add(table.getScrollPane(), BorderLayout.CENTER);

		testFrame.pack();
		testFrame.setVisible(true);
	}

}