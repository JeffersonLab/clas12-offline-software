/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class AttributeTableModel extends AbstractTableModel implements
		Comparator<Object> {

	/**
	 * Constant used to designate name column;
	 */
	public static final int PROP_NAME = 0;

	/**
	 * Constant used to designate value column;
	 */
	public static final int PROP_VALUE = 1;

	/**
	 * array of column names
	 */
	protected String cnames[] = { "Name", "Value" };

	/**
	 * The displayed attributes.
	 */
	protected Attributes displayedAttributes;

	/**
	 * A set of names that cannot be edited
	 */
	protected Collection<String> uneditableKeys;

	/**
	 * Clear the table
	 */
	public void clear() {
		displayedAttributes = null;
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
	 * Get the attribute data at a given row.
	 * 
	 * @param row
	 *            the zero based row.
	 * @return the attributes name.
	 */

	public String getAttributeNameAt(int row) {

		if ((row < 0) || (displayedAttributes == null)) {
			return null;
		}

		return (String) (getValueAt(row, PROP_NAME));
	}

	/**
	 * Tries to find the row that contains a givven attribute;
	 * 
	 * @param attributeName
	 * @return the corresponding row
	 */
	public int getRowFromName(String attributeName) {
		if (attributeName != null) {
			int rowCount = getRowCount();
			for (int i = 0; i < rowCount; i++) {
				String s = getAttributeNameAt(i);
				if (attributeName.equals(s)) {
					return i;
				}
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
		return (displayedAttributes == null) ? 0 : displayedAttributes.size();
	}

	/**
	 * Get the object at a given row and column.
	 * 
	 * @param rowIndex
	 *            the zero based row index.
	 * @param columnIndex
	 *            the zero based column index.
	 * @return the value as an Object.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (displayedAttributes == null) {
			return null;
		}

		Object[] keys = displayedAttributes.keySet().toArray();

		Arrays.sort(keys, this);

		switch (columnIndex) {

		case PROP_NAME:
			return keys[rowIndex];

		case PROP_VALUE:
			return displayedAttributes.get(keys[rowIndex]);
		}

		return null;
	}

	/**
	 * Set the value at a given row and column.
	 * 
	 * @param s
	 *            The string to set.
	 * @param row
	 *            The zero based row.
	 * @param col
	 *            The zero based column.
	 */

	@Override
	public void setValueAt(Object s, int row, int col) {

		switch (col) {

		case PROP_NAME:
			break;

		case PROP_VALUE:
			displayedAttributes.put(getAttributeNameAt(row), s);
			break;
		}

	}

	/**
	 * Check whether the cell is editable.
	 * 
	 * @param row
	 *            the zero based row.
	 * @param col
	 *            the zero based column.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		if (col == PROP_VALUE) {
			if (uneditableKeys == null) {
				return true;
			}

			String name = getAttributeNameAt(row);
			return !uneditableKeys.contains(name);
		} else {
			return false;
		}
	}

	/**
	 * Return the column name.
	 * 
	 * @param col
	 *            the zero based column.
	 * @return the column name.
	 */
	@Override
	public String getColumnName(int col) {
		if ((col < 0) || (col >= cnames.length)) {
			return null;
		} else {
			return cnames[col];
		}
	}

	/**
	 * Get the displayedAttributes object.
	 * 
	 * @return the displayedAttributes object.
	 */
	public Attributes getDisplayedAttributes() {
		return displayedAttributes;
	}

	/**
	 * Get the displayedAttributes object.
	 * 
	 * @param displayedAttributes
	 *            the new displayedAttributes object.
	 */
	public void setDisplayedAttributes(Attributes displayedAttributes) {
		this.displayedAttributes = displayedAttributes;
	}

	/**
	 * This will compare such that attributes of the same type (such as Color)
	 * will be grouped together. Ties are broken using the name of the
	 * attribute. E.g., FILLCOLOR will appear before line color.
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object o1, Object o2) {

		return o1.toString().compareToIgnoreCase(o2.toString());

		// try {
		//
		// System.err.println("o1: " + o1);
		// System.err.println("o2: " + o2);
		//
		//
		// if (o1 == null) {
		// return 1;
		// }
		// if (o2 == null) {
		// return -1;
		// }
		//
		// System.err.println("o1: " + o1.getClass().getSimpleName());
		// System.err.println("o2: " + o2.getClass().getSimpleName());
		//
		// Object value1 = displayedAttributes.get(o1);
		// Object value2 = displayedAttributes.get(o2);
		//
		// System.err.println("v1: " + value1);
		// System.err.println("v2: " + value2);
		//
		// if (value1 == null) {
		// return 1;
		// }
		// if (value2 == null) {
		// return -1;
		// }
		//
		// String class1 = value1.getClass().getSimpleName();
		// String class2 = value2.getClass().getSimpleName();
		// int compareResult = class1.compareTo(class2);
		// if (compareResult == 0) {
		// String s1 = o1.toString();
		// String s2 = o2.toString();
		// return s1.compareTo(s2);
		// }
		//
		// return compareResult;
		// }
		// catch (Exception e) {
		// e.printStackTrace();
		// return 0;
		// }
	}

	/**
	 * Sets the names (keys) of attributes that are display only.
	 * 
	 * @param uneditableKeys
	 *            the uneditableKeys to set
	 */
	public void setUneditableKeys(Collection<String> uneditableKeys) {
		this.uneditableKeys = uneditableKeys;
	}

}
