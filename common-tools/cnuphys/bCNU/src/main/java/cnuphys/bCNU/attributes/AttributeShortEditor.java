package cnuphys.bCNU.attributes;

public class AttributeShortEditor extends AttributeIntegerValueEditor<Short> {

	/**
	 * Create a Short editor.
	 * 
	 * @param attributeTable the owner table.
	 * @param attribute the attribute
	 */
	public AttributeShortEditor(AttributeTable attributeTable,
			Attribute attribute) {
		super(attributeTable, attribute);
	}
	

	@Override
	protected void setStartValue() {
		startValue = Short.MIN_VALUE;
		
	}

	@Override
	protected Short parse(String vText) {
		Short newValue = startValue;
		try {
			newValue = Short.parseShort(vText);
		}
		catch (NumberFormatException e) {
			newValue = startValue;
		}
		return newValue;
	}


}