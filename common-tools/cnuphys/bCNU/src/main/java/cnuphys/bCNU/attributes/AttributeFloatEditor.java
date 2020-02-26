package cnuphys.bCNU.attributes;

@SuppressWarnings("serial")
public class AttributeFloatEditor extends AttributeRealValueEditor {


	public AttributeFloatEditor(AttributeTable attributeTable,
			Attribute attribute) {

		super(attributeTable, attribute);
	}

	@Override
	protected void parse(String vText) {
		float dval = Float.parseFloat(vText);
		attribute.setValue(dval);
	}

	/**
	 * Render the value for display
	 * @param value the
	 */
	@Override
	public void renderValue(Object value) {
		Float val = (Float)value;
		component.setText(String.format("%10.6G", val));
	}
	

}
