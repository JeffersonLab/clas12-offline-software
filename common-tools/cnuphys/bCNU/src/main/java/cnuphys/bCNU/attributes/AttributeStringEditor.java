/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

@SuppressWarnings("serial")
public class AttributeStringEditor extends AttributeEditor<JTextField> implements FocusListener,
		KeyListener {
	
	
	String oldText = null;

	/**
	 * Create a String editor.
	 * 
	 * @param attributeTable the owner table.
	 * @param attribute the attribute
	 */
	public AttributeStringEditor(AttributeTable attributeTable,
			Attribute attribute) {

		super(attributeTable, attribute, new JTextField());

		if ((attributeTable == null) || (attribute == null)) {
			return;
		}

		component.addFocusListener(this);
		component.addKeyListener(this);
	}

	/**
	 * See if a string has changed. If so, fire a notice.
	 * 
	 * @param eventComponent
	 */

	protected void checkTextChange() {

		try {
			String newText = component.getText();
			if (newText == null) {
				newText = "";
			}

			if ((oldText == null) || (oldText.compareTo(newText) != 0)) {
				attribute.setValue(newText);
			}

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		checkTextChange();
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		checkTextChange();
	}
	
	
	/**
	 * Render the value for display
	 * @param value the
	 */
	@Override
	public void renderValue(Object value) {
		String val = (String)value;
		component.setText(val);
	}


}
