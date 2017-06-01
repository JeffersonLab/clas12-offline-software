/**
 * 
 */
package cnuphys.bCNU.util;

/**
 * Simple class that holds a row and column index.
 * 
 * @author heddle
 * 
 */
public final class RowColumnPoint {

	/**
	 * The row, in the sense that the grid is unrotated.
	 */
	private int _row;

	/**
	 * The column, in the sense that the grid is unrotated.
	 */
	private int _column;

	/**
	 * Construct a RowColumn point.
	 */
	public RowColumnPoint() {
		this(-1, -1);
	}

	/**
	 * Construct a RowColumn point.
	 * 
	 * @param row
	 *            the row.
	 * @param column
	 *            the column.
	 */
	public RowColumnPoint(int row, int column) {
		_row = row;
		_column = column;
	}

	/**
	 * Generate a useful string represenation.
	 * 
	 * @see java.lang.Object#toString()
	 * @return A string representation.
	 */
	@Override
	public String toString() {
		return String.format("[%d ,%d]", _row, _column);
	}

	/**
	 * Returns the column
	 * 
	 * @return the column.
	 */
	public int getColumn() {
		return _column;
	}

	/**
	 * Returns the row
	 * 
	 * @return the row.
	 */
	public int getRow() {
		return _row;
	}

	/**
	 * Set the row and column indices.
	 * 
	 * @param row
	 *            the row index.
	 * @param column
	 *            the column index.
	 */
	public void set(int row, int column) {
		_row = row;
		_column = column;
	}

	/**
	 * Set just the column index.
	 * 
	 * @param column
	 *            the column index.
	 */
	public void setColumn(int column) {
		_column = column;
	}

	/**
	 * Set just the row index.
	 * 
	 * @param row
	 *            the row index.
	 */
	public void setRow(int row) {
		_row = row;
	}
}
