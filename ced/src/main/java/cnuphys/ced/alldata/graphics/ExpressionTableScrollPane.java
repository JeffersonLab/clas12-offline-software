package cnuphys.ced.alldata.graphics;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class ExpressionTableScrollPane extends JScrollPane {

	protected int width = 330;
	protected int height = 300;

	/**
	 * The table that will be on this scroll pane.
	 */
	private ExpressionTable _table;

	/**
	 * Constructor will also create the table itself.
	 * 
	 * @param cutList
	 *            the list of cuts
	 * @param label
	 *            a label for the list
	 */

	public ExpressionTableScrollPane(String label, int selectionMode) {
		super();
		_table = new ExpressionTable(selectionMode);
		getViewport().add(_table);

		setBorder(BorderFactory.createTitledBorder(null, label,
				TitledBorder.LEADING, TitledBorder.TOP, null, Color.blue));

	}

	/**
	 * Accessor for the underlying table.
	 * 
	 * @return the underlying expression table.
	 */
	public ExpressionTable getTable() {
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
	public ExpressionTableModel getExpressionModel() {
		if (_table == null) {
			return null;
		}

		return _table.getExpressionModel();
	}
	
	
	/**
	 * Get the selected (or first selected) named expression
	 * @return the selected (or first selected) named expression
	 */
	public NamedExpression getSelectedExpression() {
		return _table.getSelectedExpression();
	}
	
}
