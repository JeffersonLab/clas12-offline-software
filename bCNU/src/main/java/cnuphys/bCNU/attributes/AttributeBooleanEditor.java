/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

@SuppressWarnings("serial")
public class AttributeBooleanEditor extends JCheckBox implements ItemListener {

    /**
     * The attribute name.
     */
    protected String attributeName = null;

    /**
     * The owner table.
     */
    protected AttributeTable attributeTable = null;

    /**
     * Create an Boolean editor.
     * 
     * @param attributeTable
     *            the owner table.
     * @param attributeName
     *            the attribute name.
     * @param isSelected
     *            the starting value.
     */
    public AttributeBooleanEditor(AttributeTable attributeTable,
	    String attributeName, boolean isSelected) {

	this.attributeTable = attributeTable;
	this.attributeName = attributeName;

	if ((attributeTable == null) || (attributeName == null)) {
	    return;
	}

	setBorder(null);
	setBackground(Color.white);

	addItemListener(this);
    }

    /**
     * The ItemStateChanges interface method.
     * 
     * @param e
     *            the causal event.
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
	try {
	    attributeTable.setAttribute(attributeName, isSelected());
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
