package cnuphys.bCNU.attributes;

public class AttributeIntegerEditor extends AttributeIntegerValueEditor<Integer> {

	/**
	 * Create an integer editor.
	 * 
	 * @param attributeTable the owner table.
	 * @param attribute the attribute
	 */
	public AttributeIntegerEditor(AttributeTable attributeTable,
			Attribute attribute) {
		super(attributeTable, attribute);
	}
	

	@Override
	protected void setStartValue() {
		startValue = Integer.MIN_VALUE;
		
	}

	@Override
	protected Integer parse(String vText) {
		Integer newValue = startValue;
		try {
			newValue = Integer.parseInt(vText);
		}
		catch (NumberFormatException e) {
			newValue = startValue;
		}
		return newValue;
	}


}