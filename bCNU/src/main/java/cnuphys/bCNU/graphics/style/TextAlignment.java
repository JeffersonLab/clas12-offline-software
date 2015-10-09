package cnuphys.bCNU.graphics.style;

import java.util.EnumMap;

public enum TextAlignment {
	CENTER, LEFT, RIGHT;

	/**
	 * A map for the names of the fill styles.
	 */
	public static EnumMap<TextAlignment, String> names = new EnumMap<TextAlignment, String>(
			TextAlignment.class);

	static {
		names.put(CENTER, "Center");
		names.put(LEFT, "Left");
		names.put(RIGHT, "Right");
	}

	/**
	 * Returns the enum value from the name.
	 * 
	 * @param name
	 *            the name to match.
	 * @return the <code>FillStyle</code> that corresponds to the name. Returns
	 *         <code>null</code> if no match is found. Note it will check (case
	 *         insensitive) both the map and the <code>name()</code> result,
	 *         thus "Solid" or "SOLID" or "SoLiD" will return the
	 *         <code>SOLID</code> value.
	 */
	public static TextAlignment getValue(String name) {
		if (name == null) {
			return null;
		}

		for (TextAlignment fs : values()) {
			if (name.equalsIgnoreCase(fs.name())) {
				return fs;
			}
			if (name.equalsIgnoreCase(names.get(fs))) {
				return fs;
			}
		}
		return null;
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
	 * Returns the array of nice (more readable) names from the enum map. These
	 * are more suitable than the raw names for presentations in radio boxes,
	 * lists, etc.
	 * 
	 * @return the string array of nice names for display.
	 */
	public static String[] getNames() {
		String strArray[] = new String[names.size()];
		int i = 0;
		for (TextAlignment fs : values()) {
			strArray[i] = names.get(fs);
			i++;
		}
		return strArray;
	}

}
