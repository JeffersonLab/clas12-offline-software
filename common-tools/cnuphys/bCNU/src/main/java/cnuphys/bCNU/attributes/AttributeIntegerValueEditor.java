package cnuphys.bCNU.attributes;

import java.text.NumberFormat;

public abstract class AttributeIntegerValueEditor<T>  extends AttributeStringEditor { 
	

	protected T startValue;

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

	public AttributeIntegerValueEditor(AttributeTable attributeTable,
			Attribute attribute) {

		super(attributeTable, attribute);
	}
	
	protected abstract void setStartValue();
	

	/**
	 * See if a string has changed. If so, fire a notice.
	 * 
	 * @param eventComponent
	 */

	@Override
	protected void checkTextChange() {

		try {
			String newText = component.getText();

			T newValue = startValue;
			try {
				newValue = parse(newText);
			}
			catch (NumberFormatException e) {
				newValue = startValue;
			}
			if (newValue != startValue) {
				attribute.setValue(newValue);
				startValue = newValue;
			}

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	protected abstract T parse(String vtext);
	
	/**
	 * Render the value for display
	 * @param value the
	 */
	@Override
	public void renderValue(Object value) {
		component.setText("" + value);
	}
	

}