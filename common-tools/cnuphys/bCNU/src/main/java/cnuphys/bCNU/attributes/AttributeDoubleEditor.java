/**
 * 
 */
package cnuphys.bCNU.attributes;

@SuppressWarnings("serial")
public class AttributeDoubleEditor extends AttributeRealValueEditor {


	public AttributeDoubleEditor(AttributeTable attributeTable,
			Attribute attribute) {

		super(attributeTable, attribute);
	}

	@Override
	protected void parse(String vText) {
		double dval = Double.parseDouble(vText);
		attribute.setValue(dval);
	}

	/**
	 * Render the value for display
	 * @param value the
	 */
	@Override
	public void renderValue(Object value) {
		Double val = (Double)value;
		component.setText(String.format("%10.6G", val));
	}
	

}
