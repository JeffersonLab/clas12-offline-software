package cnuphys.tinyMS.common;

/**
 * This an enum of payload data types.
 * 
 * @author heddle
 * 
 */
public enum DataType {

	NO_DATA, BYTE_ARRAY, SHORT_ARRAY, INT_ARRAY, LONG_ARRAY, FLOAT_ARRAY, DOUBLE_ARRAY, STRING_ARRAY, STRING, SERIALIZED_OBJECT, STREAMED;

	/**
	 * Obtain the name of the DataType enum from the ordinal value.
	 * 
	 * @param value
	 *            the ordinal value to match.
	 * @return the name, or "UNKNOWN".
	 */
	public static String getName(int value) {
		DataType dt = getDataType(value);

		return (dt == null) ? "UNKNOWN" : dt.name();
	}

	/**
	 * Obtain the DataType enum from the ordinal value.
	 * 
	 * @param value
	 *            the ordinal value to match.
	 * @return the matching enum, or <code>null</code>.
	 */
	public static DataType getDataType(int value) {
		if (value < 0) {
			return null;
		}
		DataType datatypes[] = DataType.values();

		if (value >= datatypes.length) {
			return null;
		}

		return datatypes[value];
	}

	/**
	 * Is this a valid type?
	 * 
	 * @param value
	 *            the ordinal value to match.
	 * @return <code>true</code> if this is a valid type..
	 */
	public static boolean isValidDataType(int value) {
		return (getDataType(value) != null);
	}

}
