package cnuphys.bCNU.attributes;

public class AttributeByteEditor extends AttributeIntegerValueEditor<Byte> {

	/**
	 * Create a Byte editor.
	 * 
	 * @param attributeTable the owner table.
	 * @param attribute the attribute
	 */
	public AttributeByteEditor(AttributeTable attributeTable,
			Attribute attribute) {
		super(attributeTable, attribute);
	}
	

	@Override
	protected void setStartValue() {
		startValue = Byte.MIN_VALUE;
		
	}

	@Override
	protected Byte parse(String vText) {
		Byte newValue = startValue;
		try {
			newValue = Byte.parseByte(vText);
		}
		catch (NumberFormatException e) {
			newValue = startValue;
		}
		return newValue;
	}


}