/**
 * 
 */
package cnuphys.bCNU.util;

import java.util.Comparator;

/**
 * @author heddle
 * @param <T>
 * 
 */
public abstract class DirectionComparator<T> implements Comparator<T> {

	protected boolean _ascendingSort = true;

	public DirectionComparator(boolean ascendingSort) {
		_ascendingSort = ascendingSort;
	}

	/**
	 * Is this an ascending sort?
	 * 
	 * @return <code>true</code> if this is an ascending sort
	 */
	public boolean is_ascendingSort() {
		return _ascendingSort;
	}

	/**
	 * Set whether this is an ascending sort
	 * 
	 * @param ascendingSort
	 *            the value of the sort direction flag.
	 */
	public void set_ascendingSort(boolean ascendingSort) {
		_ascendingSort = ascendingSort;
	}
}
