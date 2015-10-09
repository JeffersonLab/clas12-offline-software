/**
 * 
 */
package cnuphys.bCNU.util;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.log.Log;

/**
 * @author heddle
 * 
 */
public class RowColumnGrid {

	/**
	 * Default size for a grid
	 */
	private static final int DEFAULT_SIZE = 10000;

	/**
	 * The number of rows in the grid.
	 */
	private int numRows;

	/**
	 * The number of columns in the grid.
	 */
	private int numColumns;

	/**
	 * Create a grid with the default number of rows and columns.
	 */
	public RowColumnGrid() {
		this(DEFAULT_SIZE, DEFAULT_SIZE);
	}

	/**
	 * Create a grid with the same number of rows and columns. (I.e., a square
	 * grid).
	 * 
	 * @param size
	 *            the number of rows and columns.
	 */
	public RowColumnGrid(int size) {
		this(size, size);
	}

	/**
	 * Construct a grid with the given number of rows and columns.
	 * 
	 * @param numRows
	 *            the number of rows.
	 * @param numColumns
	 *            the number of columns.
	 */
	public RowColumnGrid(int numRows, int numColumns) {
		super();
		this.numRows = numRows;
		this.numColumns = numColumns;
	}

	/**
	 * Given world coordinates, this returns the row and column indices.
	 * 
	 * @param bounds
	 *            an world rectangle defining the physical extent of the grid.
	 * @param x
	 *            the x coordinate.
	 * @param y
	 *            the y coordinate.
	 * @return a RowColumnPoint that will hold the row and column indices.
	 */
	public RowColumnPoint getRowCol(Rectangle2D.Double bounds, double x,
			double y) {

		try {

			double delY = Math.abs(y - bounds.y);
			double delX = Math.abs(x - bounds.x);

			// fractional offsets
			double fy = delY / bounds.height;
			double fx = delX / bounds.width;

			int row = (int) (numRows * fy);
			int col = (int) (numColumns * fx);

			return new RowColumnPoint(row, col);
		} catch (Exception e) {
			Log.getInstance().exception(e);
			return new RowColumnPoint(-1, -1);
		}

	}

	/**
	 * Given a RowColumnPoint, and the size of the boundary
	 * 
	 * @param bounds
	 *            a world rectangle describing the physical extent of the grid.
	 * @param p
	 *            a world point that will be modified--given the vale of the
	 *            cell on the logical grid imposed on the world rectangle.
	 * @param rc
	 *            the input row and column.
	 */
	public void setPointFromRowCol(Rectangle2D.Double bounds, Point2D.Double p,
			RowColumnPoint rc) {
		// grid fractional spacing
		double fx = (double) rc.getColumn() / numColumns;
		double fy = (double) rc.getRow() / numRows;

		// actual offests
		double delX = fx * bounds.width;
		double delY = fy * bounds.height;

		// world value of the given row and column
		p.x = bounds.x + delX;
		p.y = bounds.y + delY;
	}

	/**
	 * Get the number of rows in this grid.
	 * 
	 * @return the number of rows.
	 */
	public int getNumRows() {
		return numRows;
	}

	/**
	 * Get the number of columns in this grid.
	 * 
	 * @return the number of columns.
	 */
	public int getNumColumns() {
		return numColumns;
	}

}
