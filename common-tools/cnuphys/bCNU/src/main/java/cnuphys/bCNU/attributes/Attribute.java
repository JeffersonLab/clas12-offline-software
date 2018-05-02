package cnuphys.bCNU.attributes;

public class Attribute implements Comparable<Attribute> {
	
	//is the Attribute editable
	private boolean _editable;
	
	//is the attibute hidden from the table
	private boolean _hidden;
	
	//the object (the value)
	private Object _value;
	
	//the key or name
	private String _key;
	
	
	/**
	 * Create an attribute
	 * @param value the value
	 * @param editable whether it is editable
	 * @param hidden whether it is hidden (not on the table)
	 */
	public Attribute(String key, Object value, boolean editable, boolean hidden) {
		_key = key;
		_editable = editable;
		_hidden = hidden;
		
		setValue(value);
	}
	
	/**
	 * Set whether this is editable
	 * @param editable the editable flag
	 */
	public void setEditable(boolean editable) {
		_editable = editable;
	}
	
	/**
	 * Check whether it is editable
	 * @return the editable flag
	 */
	public boolean isEditable() {
		return _editable;
	}
	
	
	/**
	 * Set whether this is hidden
	 * @param hidden the hidden flag
	 */
	public void setHidden(boolean hidden) {
		_hidden = hidden;
	}
	
	/**
	 * Check whether it is hidden
	 * @return the hidden flag
	 */
	public boolean isHidden() {
		return _hidden;
	}
	
	/**
	 * Get the value
	 * @return the value
	 */
	public Object getValue() {
		return _value;
	}
	
	/**
	 * Set the value to a new object
	 * @param value
	 */
	public void setValue(Object value) {
		_value = value;
	}
	
	/**
	 * Get the type based on the value
	 * @return the type
	 */
	public AttributeType getType() {
		return AttributeType.getType(_value);
	}
	
	@Override
	public int compareTo(Attribute o) {
		return _key.compareTo(o._key);
	}
	
	/**
	 * Get the key for this attribute
	 * @return the key, which is also kinda the name
	 */
	public String getKey() {
		return _key;
	}

	/**
	 * Clone this attribute
	 * @return the cloned value
	 */
	public Attribute clone() {
		String newKey = new String(_key);
		Object newVal = AttributeType.cloneValue(_value);
		return new Attribute(newKey, newVal, _editable, _hidden);
	}

}
