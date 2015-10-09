/**
 * 
 */
package cnuphys.bCNU.attributes;

@SuppressWarnings("serial")
public class AttributeIntegerEditor extends AttributeStringEditor {

	/**
	 * The staring value.
	 */
	private int startValue;

	/**
	 * Create an Integer editor.
	 * 
	 * @param attributeTable
	 *            the owner table.
	 * @param attributeName
	 *            the attribute name.
	 * @param startValue
	 *            the starting value.
	 */
	public AttributeIntegerEditor(AttributeTable attributeTable,
			String attributeName, Integer startValue) {

		super(attributeTable, attributeName, "" + startValue.intValue());
		this.startValue = startValue.intValue();
	}

	/**
	 * See if a string has changed. If so, fire a notice.
	 * 
	 * @param eventComponent
	 */

	@Override
	protected void checkTextChange() {

		try {
			String newText = getText();

			int newValue = startValue;
			try {
				newValue = Integer.parseInt(newText);
			} catch (NumberFormatException e) {
				newValue = startValue;
			}
			if (newValue != startValue) {
				attributeTable.setAttribute(attributeName, newValue);
				startValue = newValue;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
