package cnuphys.bCNU.attributes;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;

public abstract class AttributeRealValueEditor  extends AttributeEditor<JFormattedTextField> implements FocusListener,
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

	public AttributeRealValueEditor(AttributeTable attributeTable,
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
					parse(newText);
				}
				catch (NumberFormatException e) {
				}
			}

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	protected abstract void parse(String vtext);
	
	
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


}
