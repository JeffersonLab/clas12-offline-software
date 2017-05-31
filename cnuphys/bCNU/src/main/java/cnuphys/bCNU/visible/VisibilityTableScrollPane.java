package cnuphys.bCNU.visible;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.IContainer;

/**
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class VisibilityTableScrollPane extends JScrollPane {

	protected int width = 150;
	protected int height = 200;

	/**
	 * The table that will be on this scroll pane.
	 */
	private VisibilityTable visTable;

	/**
	 * Constructor will also create the table itself.
	 * 
	 * @param container
	 *            container holding the list of drawables (prpbably layers)
	 * @param visList
	 *            the list of drawables
	 * @param label
	 *            a label for the list
	 */

	public VisibilityTableScrollPane(IContainer container,
			Vector<IDrawable> visList, String label) {
		super();
		visTable = new VisibilityTable(container, visList);
		getViewport().add(visTable);

		setBorder(BorderFactory.createTitledBorder(null, label,
				TitledBorder.LEADING, TitledBorder.TOP, null, Color.blue));
	}

	/**
	 * Accessor for the underlying table.
	 * 
	 * @return the underlying visibility table.
	 */
	public VisibilityTable getVisibilityTable() {
		return visTable;
	}

	/**
	 * Refresh the table.
	 */

	@SuppressWarnings("unused")
	// TODO: Use or Remove
	private void refresh() {
		if (visTable != null) {
			visTable.revalidate();
			visTable.repaint();
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	/**
	 * Simple accessor for underlying model.
	 * 
	 * @return The underlying table model.
	 */
	public VisibilityTableModel getVisibilityTableModel() {
		if (visTable == null) {
			return null;
		}

		return (VisibilityTableModel) (visTable.getModel());
	}

	/**
	 * @param height
	 *            The height to set.
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @param width
	 *            The width to set.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

}
