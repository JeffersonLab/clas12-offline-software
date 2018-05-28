/**
 * 
 */
package cnuphys.bCNU.attributes;

@SuppressWarnings("serial")
public class AttributeIntegerEditor extends AttributeStringEditor {

	/**
	 * The staring value.
	 */
	private int startValue = Integer.MIN_VALUE;

	/**
	 * Create a integer editor.
	 * 
	 * @param attributeTable the owner table.
	 * @param attribute the attribute
	 */
	public AttributeIntegerEditor(AttributeTable attributeTable,
			Attribute attribute) {
		super(attributeTable, attribute);
	}

	/**
	 * See if a string has changed. If so, fire a notice.
	 * 
	 * @param eventComponent
	 */

	@Override
	protected void checkTextChange() {

		try {
			String newText = component.getText();

			int newValue = startValue;
			try {
				newValue = Integer.parseInt(newText);
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
	
	
	/**
	 * Render the value for display
	 * @param value the
	 */
	@Override
	public void renderValue(Object value) {
		Integer val = (Integer)value;
		component.setText("" + val);
	}


}
