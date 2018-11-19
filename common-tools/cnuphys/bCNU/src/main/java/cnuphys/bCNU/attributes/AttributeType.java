package cnuphys.bCNU.attributes;

import java.util.EnumMap;

import javax.swing.JSlider;

import cnuphys.bCNU.component.EnumComboBox;

/**
 * Use to designate the current type of attribute. We need an editor for each type.
 * @author heddle
 *
 */
public enum AttributeType {

	BOOLEAN, STRING, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, SLIDER, UNKNOWN;
	

	/**
	 * A map for the names of the attribute types
	 */
	public static EnumMap<AttributeType, String> names = new EnumMap<AttributeType, String>(
			AttributeType.class);

	static {
		names.put(BOOLEAN, "boolean");
		names.put(STRING, "float");
		names.put(BYTE, "byte");
		names.put(SHORT, "short");
		names.put(INT, "int");
		names.put(LONG, "long");
		names.put(FLOAT, "float");
		names.put(DOUBLE, "double");
		names.put(SLIDER, "slider");
		names.put(UNKNOWN, "Unknown");
	}
	
	/**
	 * A map for the editors for the types
	 */
	public static EnumMap<AttributeType, Class> editorClasses = new EnumMap<AttributeType, Class>(
			AttributeType.class);

	static {
		editorClasses.put(BOOLEAN, AttributeBooleanEditor.class);
		editorClasses.put(STRING, AttributeStringEditor.class);
		editorClasses.put(BYTE, AttributeByteEditor.class);
		editorClasses.put(SHORT, AttributeShortEditor.class);
		editorClasses.put(INT, AttributeIntegerEditor.class);
		editorClasses.put(LONG, AttributeLongEditor.class);
		editorClasses.put(FLOAT, AttributeFloatEditor.class);
		editorClasses.put(DOUBLE, AttributeDoubleEditor.class);
		editorClasses.put(SLIDER, AttributeSliderEditor.class);
	}
	
	/**
	 * From an object, obtain the type
	 * @param value the object
	 * @return the type
	 */
	public static AttributeType getType(Object value) {
		
		//set the attribute type
		if (value == null) {
			return null;
		}
		
		if (value instanceof Boolean) {
			return BOOLEAN;
		}
		else if (value instanceof String) {
			return STRING;
		}
		else if (value instanceof Byte) {
			return BYTE;
		}
		else if (value instanceof Short) {
			return SHORT;
		}
		else if (value instanceof Integer) {
			return INT;
		}
		else if (value instanceof Long) {
			return LONG;
		}
		else if (value instanceof Float) {
			return FLOAT;
		}
		else if (value instanceof Double) {
			return DOUBLE;
		}
		else if (value instanceof JSlider) {
			return SLIDER;
		}
		else {
			return UNKNOWN;	
		}		
	}

	
	/**
	 * Get the class for creating an editor
	 * @return the class for creating an editor
	 */
	public Class<?> getEditorClass() {
		return editorClasses.get(this);
	}

	/**
	 * Get the nice name of the enum.
	 * 
	 * @return the nice name, for combo boxes, menus, etc.
	 */
	public String getName() {
		return names.get(this);
	}

	/**
	 * Returns the enum value from the name.
	 * 
	 * @param name
	 *            the name to match.
	 * @return the <code>SymbolType</code> that corresponds to the name. Returns
	 *         <code>null</code> if no match is found. Note it will check (case
	 *         insensitive) both the map and the <code>name()</code> result,
	 *         thus "Up Triangle" or "UPTRIANGLE" or "dUpTrIaNgLe" will return
	 *         the <code>UPTRIANGLE</code> value.
	 */
	public static AttributeType getValue(String name) {
		if (name == null) {
			return null;
		}

		for (AttributeType val : values()) {
			// check the nice name
			if (name.equalsIgnoreCase(val.toString())) {
				return val;
			}
			// check the base name
			if (name.equalsIgnoreCase(val.name())) {
				return val;
			}
		}
		return null;
	}

	/**
	 * Obtain a combo box of choices.
	 * 
	 * @param defaultChoice
	 * @return the combo box of attribute choices
	 */
	public static EnumComboBox getComboBox(AttributeType defaultChoice) {
		return new EnumComboBox(names, defaultChoice);
	}
	

	
}
