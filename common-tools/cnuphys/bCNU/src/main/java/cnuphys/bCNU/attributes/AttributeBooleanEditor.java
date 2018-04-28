/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

@SuppressWarnings("serial")
public class AttributeBooleanEditor extends AttributeEditor<JCheckBox> implements ItemListener {

	/**
	 * Create an Boolean editor.
	 * 
	 * @param attributeTable the owner table.
	 * @param attribute the attribute
	 */
	public AttributeBooleanEditor(AttributeTable attributeTable,
			Attribute attribute) {

		super(attributeTable, attribute, new JCheckBox());

		if ((attributeTable == null) || (attribute == null)) {
			return;
		}

		component.setSelected((Boolean)(attribute.getValue()));
		component.setBackground(Color.white);
		component.addItemListener(this);
	}

	/**
	 * The ItemStateChanges interface method.
	 * 
	 * @param e the causal event.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		try {
            attribute.setValue(component.isSelected());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	/**
	 * Render the value for display
	 * @param value the
	 */
	@Override
	public void renderValue(Object value) {
		Boolean b = (Boolean)value;
		component.setSelected(b);
	}


}
