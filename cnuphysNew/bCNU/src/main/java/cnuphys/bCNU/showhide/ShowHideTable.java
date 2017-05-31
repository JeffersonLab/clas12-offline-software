package cnuphys.bCNU.showhide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;

@SuppressWarnings("serial")
public class ShowHideTable extends JTable {

	private DefaultTableCellRenderer def_renderer = new DefaultTableCellRenderer();

	private ItemListener _itemListener;

	private JScrollPane _scrollPane;

	private JPanel _buttonPanel;

	/**
	 * Create a table for toggling visibility (or any check box list)
	 * 
	 * @param showHideList
	 *            the objects that can be hidden
	 * @param colNames
	 *            the column names
	 */
	public ShowHideTable(Vector<IShowHide> showHideList, String[] colNames) {
		super(new ShowHideTableModel(showHideList, colNames));

		// single selection
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setCellSelectionEnabled(true);

		// set preferred widths
		for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
			column.setPreferredWidth(ShowHideTableModel._columnWidths[i]);
		}

		// don't show vertical lines
		showVerticalLines = false;

		def_renderer.setHorizontalAlignment(SwingConstants.LEFT);
		def_renderer.setFont(Fonts.smallFont);

		// check box renderer
		DefaultTableCellRenderer dcr = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JCheckBox cb = new JCheckBox();
				GraphicsUtilities.setSizeSmall(cb);
				cb.setSelected((Boolean) value);
				cb.setBackground(Color.white);
				return cb;

			}

		};

		// change renderer for value column
		JCheckBox cb = new JCheckBox();
		cb.addItemListener(_itemListener);
		TableColumn column = getColumnModel().getColumn(
				ShowHideTableModel.DISPLAY_VALUE);
		column.setCellRenderer(dcr);
		column.setCellEditor(new DefaultCellEditor(cb));

		// no reordering
		getTableHeader().setReorderingAllowed(false);

		// add a mouse listener for right clicks
		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// Left mouse click
				if (SwingUtilities.isLeftMouseButton(e)) {
					Point p = e.getPoint();

					// get the row index that contains that coordinate
					int row = rowAtPoint(p);
					int col = columnAtPoint(p);

					if (col == 0) {
						IShowHide ish = getShowHideDataModel().getElementAtRow(
								row);
						boolean vis = ish.isVisible();
						ish.setVisible(!vis);
						getShowHideDataModel().notifyModelChangeListeners(ish);
						// System.err.println("CLICKED on row, col = " + row +
						// ", " + col + " val: " + ish.isVisible());
					}
				}
				// Right mouse click
			}

		};

		addMouseListener(ma);

	}

	/**
	 * Get the underlying ShowHideDataModel
	 * 
	 * @return the underlying ShowHideDataModel
	 */
	public ShowHideTableModel getShowHideDataModel() {
		return (ShowHideTableModel) getModel();
	}

	/**
	 * Convenience method to get a scroll pane that controls the table.
	 * 
	 * @return a scroll pane with the table inside.
	 */
	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane();

			Dimension size = getPreferredSize();
			// size.width += 20;
			size.width = 200;
			size.height = 150;
			_scrollPane.setPreferredSize(size);
			_scrollPane.getViewport().add(this);
		}
		return _scrollPane;
	}

	public JButton makeCommonButton(String label) {
		JButton button = new JButton(label);
		GraphicsUtilities.setSquareButton(button);
		button.setFont(Fonts.smallFont);
		_buttonPanel.add(button);
		return button;
	}

	public JPanel getFullPanel(String borderTitle) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(2, 2));

		panel.add(getScrollPane(), BorderLayout.CENTER);
		Dimension size = getPreferredSize();
		// size.width += 20;
		size.width = 200;
		panel.setSize(new Dimension(220, 220));

		_buttonPanel = new JPanel();
		_buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));

		final JButton showAll = makeCommonButton("Show All");
		final JButton hideAll = makeCommonButton("Hide All");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object source = arg0.getSource();
				setAllVisible(source == showAll);
			}

		};

		showAll.addActionListener(al);
		hideAll.addActionListener(al);
		panel.add(_buttonPanel, BorderLayout.NORTH);

		if (borderTitle != null) {
			panel.setBorder(new CommonBorder(borderTitle));
		}
		return panel;
	}

	public void refresh() {
		revalidate();
		repaint();
	}

	/**
	 * Add an <code>IShowHideListener</code>.
	 * 
	 * @see IShowHideListener
	 * @param modelListener
	 *            the <code>IShowHideListener</code> to add.
	 */
	public void addModelListener(IShowHideListener modelListener) {
		getShowHideDataModel().addModelListener(modelListener);
	}

	/**
	 * Convenience method to see if all the visibles are visible.
	 * 
	 * @return <code>true</code> if all are visible.
	 */
	public boolean allVisible() {
		ShowHideTableModel model = getShowHideDataModel();

		Vector<IShowHide> v = model._data;

		if ((v == null) || (v.size() < 1)) {
			return true;
		}

		for (IShowHide ish : v) {
			if (!ish.isVisible()) {
				return false;
			}
		}

		return true;
	}

	public void setAllVisible(boolean vis) {
		ShowHideTableModel model = getShowHideDataModel();

		Vector<IShowHide> v = model._data;

		if ((v == null) || (v.size() < 1)) {
			return;
		}

		for (IShowHide ish : v) {
			ish.setVisible(vis);
		}

		model.fireTableDataChanged();
		model.notifyModelChangeListeners(null);
	}

	public boolean nameInModel(String name) {
		if (name == null) {
			return false;
		}
		ShowHideTableModel model = getShowHideDataModel();

		Vector<IShowHide> v = model._data;

		if ((v == null) || (v.size() < 1)) {
			return false;
		}

		for (IShowHide ish : v) {
			String vname = ish.getName();
			if (name.equalsIgnoreCase(vname)) {
				return true;
			}
		}

		return false;
	}

	public IShowHide getElementFromName(String name) {
		if (name == null) {
			return null;
		}
		ShowHideTableModel model = getShowHideDataModel();

		Vector<IShowHide> v = model._data;

		if ((v == null) || (v.size() < 1)) {
			return null;
		}

		for (IShowHide ish : v) {
			String vname = ish.getName();
			if (name.equalsIgnoreCase(vname)) {
				return ish;
			}
		}

		return null;
	}

	public void addNameToModelIfUnique(String name) {
		if (nameInModel(name)) {
			return;
		}

		ShowHideString shs = new ShowHideString(name);
		ShowHideTableModel model = getShowHideDataModel();
		model.add(shs);
	}

	/**
	 * @return the buttonPanel
	 */
	public JPanel getButtonPanel() {
		return _buttonPanel;
	}

}
