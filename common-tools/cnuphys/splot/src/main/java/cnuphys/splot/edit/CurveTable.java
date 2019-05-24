package cnuphys.splot.edit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.plot.Environment;
import cnuphys.splot.plot.PlotCanvas;

public class CurveTable extends JTable {

	private DefaultTableCellRenderer def_renderer = new DefaultTableCellRenderer();

	private PlotCanvas _canvas;

	// a scroll pane for this table
	private JScrollPane _scrollPane;

	protected static Font _listFont = Environment.getInstance().getCommonFont(12);

	private static final Color _unselectedColor = new Color(240, 240, 240);

	/**
	 * Create a table for toggling visibility (probably of logical layers)
	 * 
	 * @param plotCanvas the plot
	 */
	public CurveTable(PlotCanvas plotCanvas) {
		super(new CurveDataModel((Vector<DataColumn>) plotCanvas.getDataSet().getAllColumnsByType(DataColumnType.Y)));

		_canvas = plotCanvas;
		setFont(_listFont);

		setRowHeight(getFontMetrics(_listFont).getHeight() + 6);
		getCurveModel().addTableModelListener(this);
		// single selection
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCellSelectionEnabled(true);

		// set preferred widths
		for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
			column.setMinWidth(CurveDataModel.columnWidths[i]);
			// column.setPreferredWidth(CurveDataModel.columnWidths[i]);
		}

		// don't show vertical line?
		showVerticalLines = true;

		// def_renderer.setHorizontalAlignment(SwingConstants.LEFT);
		initVisColumn();
		initNameColumn();

		// no reordering
		getTableHeader().setReorderingAllowed(false);
	}

	/**
	 * Get the curve data model
	 * 
	 * @return the curve data model
	 */
	public CurveDataModel getCurveModel() {
		return (CurveDataModel) getModel();
	}

	/**
	 * Clear the data
	 */
	public void clear() {
		getCurveModel().clear();
	}

	private void initVisColumn() {
		// check box renderer
		DefaultTableCellRenderer checkBoxRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				JCheckBox cb = new JCheckBox();
				cb.setSelected((Boolean) value);
				// cb.setBackground(Color.white);
				return cb;

			}

		};

		CellEditorListener visListener = new CellEditorListener() {
			@Override
			public void editingStopped(ChangeEvent e) {
				_canvas.repaint();
			}

			@Override
			public void editingCanceled(ChangeEvent e) {
			}
		};

		// change renderer for value column
		JCheckBox cb = new JCheckBox();
		TableColumn column = getColumnModel().getColumn(CurveDataModel.VIS_COLUMN);
		column.setCellRenderer(checkBoxRenderer);
		column.setCellEditor(new DefaultCellEditor(cb));
		column.getCellEditor().addCellEditorListener(visListener);

	}

	private void initNameColumn() {
		// check box renderer
		DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				JTextField jtf = new JTextField((String) value);
				jtf.setFont(_listFont);

				boolean rowSelected = table.getSelectedRow() == row;
				if (!rowSelected) {
					jtf.setBackground(_unselectedColor);
				}
				else {
					jtf.setBackground(Color.yellow);
				}
				return jtf;

			}

		};

		CellEditorListener nameListener = new CellEditorListener() {
			@Override
			public void editingStopped(ChangeEvent e) {
				_canvas.repaint();
			}

			@Override
			public void editingCanceled(ChangeEvent e) {
			}
		};

		JTextField jtf = new JTextField();
		jtf.setBackground(Color.yellow);
		jtf.setFont(_listFont);
		TableColumn column = getColumnModel().getColumn(CurveDataModel.NAME_COLUMN);
		column.setCellRenderer(textRenderer);
		column.setCellEditor(new DefaultCellEditor(jtf));
		column.getCellEditor().addCellEditorListener(nameListener);

	}

	/**
	 * Get the selected curve
	 * 
	 * @return the selected curve
	 */
	public DataColumn getSelectedCurve() {
		int selectedRow = getSelectedRow();
		return getCurveModel().getCurveAtRow(selectedRow);
	}

	/**
	 * Get the scroll pane for this data table
	 * 
	 * @return the scroll pane for this data table
	 */
	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane(this);
			Environment.getInstance().commonize(_scrollPane, null);

			int totalWidth = 0;
			for (int i = 0; i < getColumnCount(); i++) {
				TableColumn column = getColumnModel().getColumn(i);
				column.setPreferredWidth(CurveDataModel.columnWidths[i]);
				totalWidth += CurveDataModel.columnWidths[i];
			}

			Dimension d = _scrollPane.getPreferredSize();
			d.width = totalWidth + 10;
			FontMetrics fm = getFontMetrics(_listFont);
			int height = (1 + getRowCount()) * (fm.getHeight() + 8) + 30;
			d.height = Math.max(80, height);
			_scrollPane.setPreferredSize(d);

		}
		return _scrollPane;
	}

}