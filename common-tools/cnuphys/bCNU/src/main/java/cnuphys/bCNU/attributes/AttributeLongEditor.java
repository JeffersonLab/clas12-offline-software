package cnuphys.bCNU.attributes;

public class AttributeLongEditor extends AttributeIntegerValueEditor<Long> {

	/**
	 * Create a long integer editor.
	 * 
	 * @param attributeTable the owner table.
	 * @param attribute the attribute
	 */
	public AttributeLongEditor(AttributeTable attributeTable,
			Attribute attribute) {
		super(attributeTable, attribute);
	}
	

	@Override
	protected void setStartValue() {
		startValue = Long.MIN_VALUE;
		
	}

	@Override
	protected Long parse(String vText) {
		Long newValue = startValue;
		try {
			newValue = Long.parseLong(vText);
		}
		catch (NumberFormatException e) {
			newValue = startValue;
		}
		return newValue;
	}


}
