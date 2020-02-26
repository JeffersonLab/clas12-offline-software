package cnuphys.bCNU.attributes;

import java.awt.Color;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JSlider;

public class AttributeSliderEditor extends AttributeEditor<JSlider> {

	
	/**
	 * Create an Boolean editor.
	 * 
	 * @param attributeTable the owner table.
	 * @param attribute the attribute
	 */
	public AttributeSliderEditor(AttributeTable attributeTable,
			Attribute attribute) {

		super(attributeTable, attribute, (JSlider)(attribute.getValue()));

		if ((attributeTable == null) || (attribute == null)) {
			return;
		}

//		component.setSelected((Boolean)(attribute.getValue()));
//		component.setBackground(Color.white);
//		component.addItemListener(this);
	}

	@Override
	public void renderValue(Object value) {
	}

}
