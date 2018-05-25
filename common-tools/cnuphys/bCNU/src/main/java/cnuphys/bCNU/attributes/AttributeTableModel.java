/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class AttributeTableModel extends AbstractTableModel {

	/**
	 * Constant used to designate name column;
	 */
	public static final int NAME = 0;

	/**
	 * Constant used to designate value column;
	 */
	public static final int VALUE = 1;

	/**
	 * array of column names
	 */
	private String cnames[] = { "Key", "Value" };


	//the model data
	private Vector<Attribute> _data = new Vector<>();
	
	
	/**
	 * Get the data
	 * @return the table data
	 */
	public List<Attribute> getData() {
		return _data;
	}
	
	/**
	 * Get the number of columns.
	 * 
	 * @return the number of columns.
	 */
	@Override
	public int getColumnCount() {
		return cnames.length;
	}

	/**
	 * Tries to find the 0-based row that contains a given attribute.
	 * 
	 * @param attributeName match to the key
	 * @return the Attribute, or null.
	 */
	public int getRowFromName(String attributeName) {
		
		int numRow = getRowCount();
		
		for (int index = 0; index < numRow; index++) {
			Attribute attribute = _data.elementAt(index);
			String key = attribute.getKey();
			if (attributeName.equals(key)) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Get the number of rows.
	 * 
	 * @return the number of rows.
	 */
	@Override
	public int getRowCount() {
		return (_data == null) ? 0 : _data.size();
	}

	/**
	 * Get the object at a given row and column.
	 * 
	 * @param rowIndex the zero based row index.
	 * @param columnIndex the zero based column index.
	 * @return the value as an Object.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		Attribute attribute = getAttribute(rowIndex);
		if (attribute == null) {
			return null;
		}

		switch (columnIndex) {

		case NAME:
			return attribute.getKey();

		case VALUE:
			return attribute.getValue();
		}

		return null;
	}
	
	/**
	 * Clear the table
	 */
	public void clear() {
		_data.clear();
	}

	/**
	 * Get the Attribute at the given row
	 * @param row the row
	 * @return the Attribute
	 */
	public Attribute getAttribute(int row) {
		int numRows = getRowCount();
		if ((row < 0) || (row >= numRows)) {
			return null;
		}
		
		return _data.get(row);
	}

	/**
	 * Set the value at a given row and column.
	 * 
	 * @param object the object to set
	 * @param row the zero based row.
	 * @param col the zero based column.
	 */

	@Override
	public void setValueAt(Object object, int row, int col) {
		
		if ((object == null) || !(object instanceof Attribute)) {
			return;
		}
		
		if (row < 0) {
			return;
		}

		Attribute attribute = (Attribute)object;
		int numRows = getRowCount();
		
		if (row < numRows) {
			_data.insertElementAt(attribute, row);
		}
		else {
			_data.add(attribute);
		}

	}

	/**
	 * Check whether the cell is editable.
	 * 
	 * @param row the zero based row.
	 * @param col the zero based column.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		
		if (col == NAME) {
			return false;
		}
		
		Attribute attribute = getAttribute(row);
		return (attribute == null) ? false : attribute.isEditable();
	}

	/**
	 * Return the column name.
	 * 
	 * @param col the zero based column.
	 * @return the column name.
	 */
	@Override
	public String getColumnName(int col) {
		if ((col < 0) || (col >= cnames.length)) {
			return null;
		}
		else {
			return cnames[col];
		}
	}

	/**
	 * Get the displayedAttributes object.
	 * 
	 * @param displayedAttributes the new displayedAttributes object.
	 */
	public void setData(Attributes attributes) {
		_data.clear();
		if (attributes != null) {
			for (Attribute attribute : attributes) {
				if (!attribute.isHidden()) {
					_data.add(attribute);
				}
			}
			
			Collections.sort(_data);
		}
	}


}
