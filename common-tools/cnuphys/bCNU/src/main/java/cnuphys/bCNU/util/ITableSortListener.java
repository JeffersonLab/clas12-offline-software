/**
 * 
 */
package cnuphys.bCNU.util;

import javax.swing.JTable;

/**
 * @author heddle
 * 
 */
public interface ITableSortListener {

	/**
	 * Sort the data
	 * 
	 * @param table
	 *            the table in question
	 * @param columnIndex
	 *            the column index to sort
	 * @param ascendingSort
	 *            if <code>true</code> sort in ascending order.
	 */
	public void sort(JTable table, int columnIndex, boolean ascendingSort);

}
