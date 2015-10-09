package cnuphys.ced.clasio.table;

import javax.swing.table.DefaultTableModel;

import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EvioNode;

import cnuphys.ced.clasio.EvioNodeSupport;

public class NodeTableModel extends DefaultTableModel {

	// indices
	private static final int TAG_INDEX = 0;
	private static final int NUM_INDEX = 1;
	private static final int NAME_INDEX = 2;
	private static final int TYPE_INDEX = 3;
	private static final int LENGTH_INDEX = 4;

	// the names of the columns
	protected static final String colNames[] = { "Tag", "Num", "Name", "Type",
			"Length" };

	// the column widths
	protected static final int columnWidths[] = { 75, // tag
			70, // num
			230, // name
			110, // type
			110, // length
	};

	// the model data
	protected EvioNode _nodes[];

	/**
	 * Constructor
	 */
	public NodeTableModel() {
		super(colNames, 2);
	}

	/**
	 * Find the row by the value in the name column
	 * 
	 * @param name
	 *            the name to search for
	 * @return the row, or -1
	 */
	public int findRowByName(String name) {
		int row = -1;

		if ((name != null) && (_nodes != null) && (_nodes.length > 0)) {
			int index = 0;
			for (EvioNode node : _nodes) {
				String nodeName = EvioNodeSupport.getName(node);
				if (name.equals(nodeName)) {
					return index;
				}
				index++;
			}
		}

		return row;
	}

	/**
	 * Get the number of columns
	 * 
	 * @return the number of columns
	 */
	@Override
	public int getColumnCount() {
		return colNames.length;
	}

	/**
	 * Get the number of rows
	 * 
	 * @return the number of rows
	 */
	@Override
	public int getRowCount() {
		return (_nodes == null) ? 0 : _nodes.length;
	}

	/**
	 * Get the value at a given row and column
	 * 
	 * @param row
	 *            the 0-based row
	 * @param col
	 *            the 0-based column
	 * @return the value at a given row and column
	 */
	@Override
	public Object getValueAt(int row, int col) {

		if (row < getRowCount()) {
			EvioNode node = _nodes[row];

			// System.err.println("TYPE: " + node.getDataType() + "  " +
			// DataType.getName(node.getDataType()));

			if (node != null) {
				switch (col) {
				case TAG_INDEX:
					return "" + node.getTag();

				case NUM_INDEX:
					return "" + node.getNum();

				case NAME_INDEX:
					return EvioNodeSupport.getName(node);

				case TYPE_INDEX:
					return DataType.getName(node.getDataType());

				case LENGTH_INDEX:
					return "" + 4 * node.getDataLength(); // bytes

				default:
					return "?";
				}
			}
		}

		return "";
	}

	/**
	 * Clear all the data
	 */
	public void clear() {
		_nodes = null;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(EvioNode[] nodes) {
		_nodes = nodes;
		fireTableDataChanged();
	}

	/**
	 * Get the node corresponding to the givens row
	 * 
	 * @param row
	 *            the row in question
	 * @return the corresponding node, or <code>null</code>
	 */
	public EvioNode getNode(int row) {
		EvioNode node = null;

		if ((row >= 0) && (_nodes != null)) {
			if (row < _nodes.length) {
				node = _nodes[row];
			}
		}

		return node;
	}

	/**
	 * Forces the cell to not be editable.
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

}
