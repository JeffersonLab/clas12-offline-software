package cnuphys.ced.alldata.graphics;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class BoundVariableTableScrollPane extends JScrollPane {

	protected int width = 330;
	protected int height = 300;

	/**
	 * The table that will be on this scroll pane.
	 */
	private BoundVariableTable _table;

	/**
	 * Constructor will also create the table itself.
	 * 
	 * @param cutList
	 *            the list of cuts
	 * @param label
	 *            a label for the list
	 */

	public BoundVariableTableScrollPane(String label) {
		super();
		_table = new BoundVariableTable();
		getViewport().add(_table);

		setBorder(BorderFactory.createTitledBorder(null, label,
				TitledBorder.LEADING, TitledBorder.TOP, null, Color.blue));

	}

	/**
	 * Accessor for the underlying table.
	 * 
	 * @return the underlying bound variable table.
	 */
	public BoundVariableTable getTable() {
		return _table;
	}

	/**
	 * Refresh the table.
	 */

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	/**
	 * Simple accessor for underlying model.
	 * 
	 * @return The underlying table model.
	 */
	public BoundVariableTableModel getBoundVariableModel() {
		if (_table == null) {
			return null;
		}

		return _table.getBoundVariableModel();
	}
	
}
