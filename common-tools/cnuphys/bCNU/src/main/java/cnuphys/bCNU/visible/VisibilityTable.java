package cnuphys.bCNU.visible;

import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import cnuphys.bCNU.component.IconTableCellRenderer;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.IContainer;

@SuppressWarnings("serial")
public class VisibilityTable extends JTable implements ItemListener {

	private DefaultTableCellRenderer def_renderer = new DefaultTableCellRenderer();

	private IContainer _container;

	/**
	 * Create a table for toggling visibility (probably of logical layers)
	 * 
	 * @param container
	 *            container holding the list of drawables (prpbably layers)
	 * @param visList
	 *            the list of drawables
	 */
	public VisibilityTable(IContainer container, Vector<IDrawable> visList) {
		super(new VisibilityTableModel(visList));

		// single selection
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCellSelectionEnabled(true);

		_container = container;

		// set preferred widths
		for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
			column.setPreferredWidth(VisibilityTableModel.columnWidths[i]);
		}

		setDragEnabled(true);

		setTransferHandler(new ShapeLayerNameTransferHandler(this));
		// don't show vertical lines
		showVerticalLines = false;

		def_renderer.setHorizontalAlignment(SwingConstants.LEFT);

		// check box renderer
		DefaultTableCellRenderer checkBoxRenderer = new DefaultTableCellRenderer() {
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

		// change renderer for value column
		JCheckBox cb = new JCheckBox();
		cb.addItemListener(this);
		TableColumn column = getColumnModel().getColumn(
				VisibilityTableModel.DISPLAY_VALUE);
		column.setCellRenderer(checkBoxRenderer);
		column.setCellEditor(new DefaultCellEditor(cb));

		// change renderer for enabled column

		IconTableCellRenderer itcr = new IconTableCellRenderer();

		TableColumn column2 = getColumnModel().getColumn(
				VisibilityTableModel.ENABLED);
		column2.setCellRenderer(itcr);

		// no reordering
		getTableHeader().setReorderingAllowed(false);

		// add mouse adapter
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {

				if (me.getClickCount() == 1) {
					VisibilityTableModel model = getVisModel();
					JTable target = (JTable) me.getSource();
					int row = target.getSelectedRow();
					int column = target.getSelectedColumn();

					if (column == VisibilityTableModel.ENABLED) {
						IDrawable drawable = model.getDrawableAtRow(row);
						if (drawable != null) {
							boolean enabled = !drawable.isEnabled();
							drawable.setEnabled(enabled);
							model.fireTableDataChanged();
						}
					}

				}

			}
		});
	}

	private VisibilityTableModel getVisModel() {
		return (VisibilityTableModel) getModel();
	}

	/**
	 * One of the vis toggles has changed. Refresh the container.
	 * 
	 * @param e
	 *            the causal event
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (_container != null) {
			_container.refresh();
		}
	}

	public StringSelection transferContent() {
		int row = getSelectedRow();
		String s = (String) (getValueAt(row, VisibilityTableModel.NAME));
		return new StringSelection(s);
	}

	class ShapeLayerNameTransferHandler extends TransferHandler {

		private VisibilityTable vistable = null;

		public ShapeLayerNameTransferHandler(VisibilityTable vistable) {
			super();
			this.vistable = vistable;
		}

		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY_OR_MOVE;
		}

		@Override
		public boolean canImport(JComponent comp, DataFlavor transferFlavors[]) {
			return true;
		}

		@Override
		public Transferable createTransferable(JComponent comp) {
			return transferContent();
		}

		@Override
		public void exportDone(JComponent c, Transferable contents, int action) {
		}

		@Override
		public boolean importData(JComponent c, Transferable contents) {
			if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				return false;
			} else if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				int newRow = getSelectedRow();
				try {
					String layerName = (String) contents
							.getTransferData(DataFlavor.stringFlavor);
					vistable.reorderTable(layerName, newRow);
				} catch (Throwable e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Reorder the table after a drag.
	 * 
	 * @param layerName
	 * @param newRow
	 */
	public void reorderTable(String layerName, int newRow) {
		//
		// if (newRow < 0) {
		// return;
		// }
		//
		// Layer layer = omc.getLayer(layerName);
		//
		// if (layer == null) {
		// return;
		// }
		//
		// omc.moveLayer(layer, newRow);
		//
		// revalidate();
		// repaint();
	}

}
