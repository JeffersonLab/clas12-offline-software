/**
 * 
 */
package cnuphys.bCNU.attributes;

@SuppressWarnings("serial")
public class AttributeLongEditor extends AttributeStringEditor {

	/**
	 * The staring value.
	 */
	private long startValue = Long.MIN_VALUE;

	/**
	 * Create a integer editor.
	 * 
	 * @param attributeTable the owner table.
	 * @param attribute the attribute
	 */
	public AttributeLongEditor(AttributeTable attributeTable,
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

			long newValue = startValue;
			try {
				newValue = Long.parseLong(newText);
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
		Long val = (Long)value;
		component.setText("" + val);
	}


}
