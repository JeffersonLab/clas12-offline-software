package cnuphys.bCNU.attributes;

import javax.management.modelmbean.InvalidTargetObjectTypeException;

public class Attribute implements Comparable<Attribute> {
	
	//is the Attribute editable
	private boolean _editable;
	
	//is the attribute hidden from the table
	private boolean _hidden;
	
	//the object (the value)
	private Object _value;
	
	//the key or name
	private String _key;
	
	
	/**
	 * Create an attribute
	 * @param key the key (name)
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
	 * Create an attribute that is not hidden
	 * @param key the key (name)
	 * @param value the value
	 * @param editable whether it is editable
	 */
	public Attribute(String key, Object value, boolean editable) {
		this(key, value, editable, false);
	}

	
	/**
	 * Create an attribute that is editable and not hidden
	 * @param key the key (name)
	 * @param value the value
	 */
	public Attribute(String key, Object value) {
		this(key, value, true, false);
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
	 * Retrieve a String value
	 * @return the value as a String
	 * @throws InvalidTargetObjectTypeException if the value is not a String
	 */
	public String getString() throws InvalidTargetObjectTypeException {
		if (_value instanceof String) {
			return (String)_value;
		}
		else {
			throw new InvalidTargetObjectTypeException("Attribute Value is not a String");
		}
	}
	
	/**
	 * Retrieve a boolean value
	 * @return the value as a boolean
	 * @throws InvalidTargetObjectTypeException if the value is not a Boolean
	 */
	public boolean getBoolean() throws InvalidTargetObjectTypeException {
		if (_value instanceof Boolean) {
			return (Boolean)_value;
		}
		else {
			throw new InvalidTargetObjectTypeException("Attribute Value is not a Boolean");
		}
	}

	/**
	 * Retrieve a byte value
	 * @return the value as a byte
	 * @throws InvalidTargetObjectTypeException if the value is not a Byte
	 */
	public byte getByte() throws InvalidTargetObjectTypeException {
		if (_value instanceof Byte) {
			return (Byte)_value;
		}
		else {
			throw new InvalidTargetObjectTypeException("Attribute Value is not a Byte");
		}
	}

	/**
	 * Retrieve a short value
	 * @return the value as a short
	 * @throws InvalidTargetObjectTypeException if the value is not a Short
	 */
	public short getShort() throws InvalidTargetObjectTypeException {
		if (_value instanceof Short) {
			return (Short)_value;
		}
		else {
			throw new InvalidTargetObjectTypeException("Attribute Value is not a Short");
		}
	}

	/**
	 * Retrieve an int value
	 * @return the value as an int
	 * @throws InvalidTargetObjectTypeException if the value is not an Integer
	 */
	public int getInt() throws InvalidTargetObjectTypeException {
		if (_value instanceof Integer) {
			return (Integer)_value;
		}
		else {
			throw new InvalidTargetObjectTypeException("Attribute Value is not a Integer");
		}
	}

	
	/**
	 * Retrieve a long value
	 * @return the value as a long
	 * @throws InvalidTargetObjectTypeException if the value is not a Long
	 */
	public long getLong() throws InvalidTargetObjectTypeException {
		if (_value instanceof Long) {
			return (Long)_value;
		}
		else {
			throw new InvalidTargetObjectTypeException("Attribute Value is not a Long");
		}
	}
	
	/**
	 * Retrieve a float value
	 * @return the value as a float
	 * @throws InvalidTargetObjectTypeException if the value is not a Float
	 */
	public float getFloat() throws InvalidTargetObjectTypeException {
		if (_value instanceof Float) {
			return (Float)_value;
		}
		else {
			throw new InvalidTargetObjectTypeException("Attribute Value is not a Float");
		}
	}

	
	/**
	 * Retrieve a double value
	 * @return the value as a double
	 * @throws InvalidTargetObjectTypeException if the value is not a Double
	 */
	public double getDouble() throws InvalidTargetObjectTypeException {
		if (_value instanceof Double) {
			return (Double)_value;
		}
		else {
			throw new InvalidTargetObjectTypeException("Attribute Value is not a Double");
		}
	}

}
