/**
 * 
 */
package cnuphys.bCNU.attributes;

import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class AttributeTableScrollPane extends JScrollPane {

	/**
	 * The table that will be on this scroll pane.
	 */
	protected AttributeTable attributeTable = null;

	/**
	 * Create a scroll panel controlling an attribute table.
	 */
	public AttributeTableScrollPane() {
		super();
		createAttributeTable();
		setPreferredSize(new java.awt.Dimension(280, 300));
		getViewport().add(attributeTable);
	}

	/**
	 * Accessor for the table.
	 * 
	 * @return the property table.
	 */

	public AttributeTable getAttributeTable() {
		return attributeTable;
	}

	/**
	 * Refresh the table.
	 */

	public void refresh() {
		if (attributeTable != null) {
			attributeTable.repaint();
		}
	}

	/**
	 * Create the table (and the table model).
	 */
	protected void createAttributeTable() {
		attributeTable = new AttributeTable();
	}

}
