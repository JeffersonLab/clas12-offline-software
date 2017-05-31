package cnuphys.ced.alldata.graphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class CutTable extends JTable implements ItemListener {

	private DefaultTableCellRenderer def_renderer = new DefaultTableCellRenderer();
	

	/**
	 * Create a table for toggling activity of plot cuts
	 * 
	 * @param container
	 *            container holding the list of drawables (prpbably layers)
	 * @param cutList
	 *            the list of iCuts
	 */
	public CutTable(Vector<ICut> cutList) {
		super(new CutTableModel(cutList));

		// single selection
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCellSelectionEnabled(true);

		// set preferred widths
		for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
			column.setPreferredWidth(CutTableModel.columnWidths[i]);
		}

		setDragEnabled(false);

		showVerticalLines = false;

		def_renderer.setHorizontalAlignment(SwingConstants.LEFT);

		// check box renderer
		final DefaultTableCellRenderer checkBoxRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JCheckBox cb = new JCheckBox();
				cb.setSelected((Boolean) value);
				cb.setBackground(Color.white);
				return cb;

			}

		};

		// change renderer for active column
		JCheckBox cb = new JCheckBox();
		cb.addItemListener(this);
		TableColumn column = getColumnModel().getColumn(
				CutTableModel.ACTIVE);
		column.setCellRenderer(checkBoxRenderer);
		column.setCellEditor(new DefaultCellEditor(cb));

		// no reordering
		getTableHeader().setReorderingAllowed(false);

	}

	public CutTableModel getCutModel() {
		return (CutTableModel) getModel();
	}

	/**
	 * One of the vis toggles has changed. Refresh the container.
	 * 
	 * @param e
	 *            the causal event
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
	}


}
