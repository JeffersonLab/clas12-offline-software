/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JComponent;
import javax.swing.JSlider;

public abstract class AttributeEditor<T extends JComponent> {

	/**
	 * The attribute 
	 */
	protected Attribute attribute;

	/**
	 * The owner table.
	 */
	protected AttributeTable attributeTable;
	
	/**
	 * The editor component
	 */
	protected T component;
	
	/**
	 * Create an editor
	 * @param attributeTable the table
	 * @param attribute the attribute being edited
	 */
	public AttributeEditor(AttributeTable attributeTable, Attribute attribute, T component) {
		this.attributeTable = attributeTable;
		this.attribute = attribute;
	    this.component = component;
		this.component.setBorder(null);
	}

	/**
	 * Create the appropriate editor for the attribute
	 * @param attributeTable the table 
	 * @param attribute the attribute
	 * @return the appropriate editor
	 */
	public static AttributeEditor AttributeEditorFactory(AttributeTable attributeTable, Attribute attribute, Object value) {

		
		AttributeEditor editor = null;

		if (attribute != null) {
			
			Object valueObj = attribute.getValue();
			if (valueObj instanceof JSlider) {
				valueObj = "" + ((JSlider)valueObj).getValue();
			}
			AttributeType type = attribute.getType();
			Class claz = type.getEditorClass();
			try {
				Constructor cons = claz.getConstructor(AttributeTable.class, Attribute.class);
				try {
					editor = (AttributeEditor) cons.newInstance(attributeTable, attribute);
					editor.renderValue(value);

				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}

		return editor;
	}
	
	/**
	 * Render the value for display
	 * @param value the
	 */
	public abstract void renderValue(Object value);


}
