package cnuphys.bCNU.visible;

import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.ImageManager;

/**
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class VisibilityTableModel extends DefaultTableModel {

	protected static ImageIcon enabledIcon = ImageManager.getInstance()
			.loadImageIcon("images/pointer13.png");
	protected static ImageIcon disabledIcon = ImageManager.getInstance()
			.loadImageIcon("images/no13.png");

	/**
	 * Constant used to designate display column;
	 */
	public static final int DISPLAY_VALUE = 0;

	/**
	 * Constant used to designate enabled
	 */
	public static final int ENABLED = 1;

	/**
	 * Constant used to designate name column;
	 */
	public static final int NAME = 2;

	// the names of the columns
	protected static final String colNames[] = { "", "", "" };

	// the widths of the columns
	protected static final int columnWidths[] = { 20, 20, 90 };

	// the model data
	protected Vector<IDrawable> _data = new Vector<IDrawable>(100, 25);

	/**
	 * Constructor
	 */
	public VisibilityTableModel() {
		super(colNames, 3);
	}

	/**
	 * Constructor
	 */
	public VisibilityTableModel(Vector<IDrawable> visList) {
		super(colNames, 3);
		setData(visList);
	}

	/**
	 * Get the number of columns
	 * 
	 * @return the number of columns
	 */
	@Override
	public int getColumnCount() {
		return columnWidths.length;
	}

	/**
	 * Get the number of rows
	 * 
	 * @return the number of rows
	 */
	@Override
	public int getRowCount() {
		if (_data == null) {
			return 0;
		}
		return _data.size();
	}

	/**
	 * Set the value at a given row and column.
	 * 
	 * @param value
	 *            The string to set.
	 * @param row
	 *            The zero based row.
	 * @param col
	 *            The zero based column.
	 */

	@Override
	public void setValueAt(Object value, int row, int col) {
		IDrawable ivis = _data.elementAt(row);
		switch (col) {

		case DISPLAY_VALUE:
			ivis.setVisible((Boolean) value);
			break;

		case ENABLED:
			boolean enabled = enabledIcon.equals(value);
			ivis.setEnabled(enabled);
			break;

		case NAME:
			break;
		}

	}

	/**
	 * Get the value at a given row and column
	 * 
	 * @return the value at a given row and column
	 */
	@Override
	public Object getValueAt(int row, int col) {

		IDrawable ivis = _data.elementAt(row);

		if (ivis == null) {
			return null;
		}

		switch (col) {

		case DISPLAY_VALUE:
			return ivis.isVisible();

		case ENABLED:
			return ivis.isEnabled() ? enabledIcon : disabledIcon;

		case NAME:
			return ivis.getName();
		}

		return null;
	}

	/**
	 * Add a new IVisible into the table.
	 * 
	 * @param ivis
	 *            the new object to add to the model.
	 */
	public synchronized void add(IDrawable ivis) {
		if (ivis != null) {
			_data.add(ivis);
		}
	}

	/**
	 * remove an IVisible from the table.
	 * 
	 * @param ivis
	 *            the IVisible to remove.
	 */
	public synchronized void remove(IDrawable ivis) {
		if (ivis != null) {
			_data.remove(ivis);
		}
	}

	/**
	 * Clear all the data
	 */
	public synchronized void clear() {
		if (_data != null) {
			_data.clear();
		}
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public synchronized void setData(Vector<IDrawable> data) {
		_data = data;
	}

	/**
	 * Get the drawable in the model at the given row.
	 * 
	 * @param row
	 *            the zero based row
	 * @return the IDrawble corresponding to the row.
	 */
	public IDrawable getDrawableAtRow(int row) {
		if ((_data == null) || (row < 0)) {
			return null;
		}
		if (row >= _data.size()) {
			return null;
		}
		return _data.elementAt(row);
	}

}
