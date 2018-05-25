/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;

@SuppressWarnings("serial")
public class AttributeDoubleEditor extends AttributeEditor<JFormattedTextField> implements FocusListener,
KeyListener {

	String oldText = "";

	/**
	 * The owner table.
	 */
	protected AttributeTable attributeTable = null;

	protected static NumberFormat numberFormat = NumberFormat
			.getNumberInstance();
	static {
		numberFormat.setMaximumFractionDigits(6);
		numberFormat.setMinimumFractionDigits(0);
	}

	/**
	 * Create an editor for Doubles.
	 * 
	 * @param propertyData the attribute being edited.
	 * @param propertyCellEditor the owner Cell Editor.
	 */

	public AttributeDoubleEditor(AttributeTable attributeTable,
			Attribute attribute) {

		super(attributeTable, attribute, new JFormattedTextField(numberFormat));

		if ((attributeTable == null) || (attribute == null)) {
			return;
		}

		component.setColumns(12);
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

			if (oldText.compareTo(newText) != 0) {
				
				try {
					double dval = Double.parseDouble(newText);
					attribute.setValue(dval);
				}
				catch (NumberFormatException e) {
				}
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
		Double val = (Double)value;
		component.setText(String.format("%10.6G", value));
	}

}
