package cnuphys.bCNU.showhide;

import java.util.Vector;

import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class ShowHideTableModel extends DefaultTableModel {

	/**
	 * Constant used to designate show/hide checkbox column;
	 */
	public static final int DISPLAY_VALUE = 0;

	/**
	 * Constant used to designate name column;
	 */
	public static final int NAME = 1;

	// the widths of the columns
	protected static final int _columnWidths[] = { 30, 500 };

	// the model data
	protected Vector<IShowHide> _data = new Vector<IShowHide>(20, 10);

	// Listener for model events
	private EventListenerList _listenerList = null;

	/**
	 * Constructor
	 */
	public ShowHideTableModel(Vector<IShowHide> visList, String[] colNames) {
		super(colNames, 2);
		setData(visList);
	}

	/**
	 * Get the number of columns
	 * 
	 * @return the number of columns
	 */
	@Override
	public int getColumnCount() {
		return _columnWidths.length;
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
	 * Get the value at a given row and column
	 * 
	 * @return the value at a given row and column
	 */
	@Override
	public Object getValueAt(int row, int col) {

		IShowHide ivis = _data.get(row);

		if (ivis == null) {
			return null;
		}

		switch (col) {

		case DISPLAY_VALUE:
			return ivis.isVisible();

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
	public void add(IShowHide ivis) {
		if (ivis != null) {
			if (_data == null) {
				_data = new Vector<IShowHide>(20, 10);
			}
			_data.add(ivis);
			fireTableDataChanged();
		}
	}

	/**
	 * remove an IVisible from the table.
	 * 
	 * @param ivis
	 *            the IVisible to remove
	 */
	public void remove(IShowHide ivis) {
		if (ivis != null) {
			_data.remove(ivis);
		}
	}

	/**
	 * Clear all the data
	 */
	public void clear() {
		if (_data != null) {
			_data.clear();
		}
	}

	/**
	 * Set the entire model
	 * 
	 * @param data
	 *            the data to set
	 */
	public void setData(Vector<IShowHide> data) {
		_data = data;
		fireTableDataChanged();
	}

	/**
	 * Get the IShowHide in the model at the given row.
	 * 
	 * @param row
	 *            the zero based row
	 * @return the IShowHide corresponding to the row.
	 */
	public IShowHide getElementAtRow(int row) {
		if ((_data == null) || (row < 0)) {
			return null;
		}
		if (row >= _data.size()) {
			return null;
		}
		return _data.get(row);
	}

	/**
	 * Add an <code>IShowHideListener</code>.
	 * 
	 * @see IShowHideListener
	 * @param modelListener
	 *            the <code>IShowHideListener</code> to add.
	 */
	public void addModelListener(IShowHideListener modelListener) {

		if (modelListener == null) {
			return;
		}

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		_listenerList.add(IShowHideListener.class, modelListener);
	}

	/**
	 * Remove an <code>IShowHideListener</code>.
	 * 
	 * @see IShowHideListener
	 * @param modelListener
	 *            the <code>IShowHideListener</code> to remove.
	 */
	public void removeModelListener(IShowHideListener modelListener) {

		if ((modelListener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(IShowHideListener.class, modelListener);
	}

	/**
	 * Notify interested parties that a visibility has changed
	 * 
	 */
	public void notifyModelChangeListeners(IShowHide ish) {

		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		System.err.println("NOTIFY LLEN: " + listeners.length);

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IShowHideListener.class) {
				((IShowHideListener) listeners[i + 1]).visibilityChanged(ish);
			}
		}
	}

}
