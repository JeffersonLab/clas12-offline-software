/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;

import cnuphys.bCNU.component.EnumComboBox;

/**
 * This is an editor for any Enum. It uses an EnumMap where it is assumed that
 * the value is a nice display string. Thus any enum that wants to be edited
 * should provide and EnumMap<?, String>
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class AttributeEnumEditor extends EnumComboBox implements ActionListener {

    /**
     * The attribute name.
     */
    protected String attributeName = null;

    /**
     * The owner table.
     */
    protected AttributeTable attributeTable = null;

    /**
     * The EnumMap the editor is based upon.
     */
    protected EnumMap<?, String> enumMap;

    /**
     * Create a Symbol editor based on a combo box.
     * 
     * @param attributeTable
     *            The owner table.
     * @param enumMap
     * @param attributeName
     *            The name of the property being edited.
     * @param defaultValue
     *            The current value.
     */

    public AttributeEnumEditor(AttributeTable attributeTable,
	    EnumMap<?, String> enumMap, String attributeName,
	    Enum<?> defaultValue) {

	super(enumMap, defaultValue);
	this.enumMap = enumMap;
	this.attributeTable = attributeTable;
	this.attributeName = attributeName;

	if ((attributeTable == null) || (attributeName == null)) {
	    return;
	}

	setBackground(Color.white);
	addActionListener(this);
    }

    /**
     * The action listener
     * 
     * @param e
     *            the causal event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
	try {
	    attributeTable.setAttribute(attributeName, getSelectedEnum());
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
