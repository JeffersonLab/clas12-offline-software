/**
 * 
 */
package cnuphys.bCNU.item;

import java.util.EnumMap;

/**
 * @author heddle
 * 
 */
public enum ResizePolicy {
	NORMAL, SCALEONLY;

	/**
	 * A map for the names of the fill styles.
	 */
	public static EnumMap<ResizePolicy, String> names = new EnumMap<ResizePolicy, String>(
			ResizePolicy.class);

	static {
		names.put(NORMAL, "Normal");
		names.put(SCALEONLY, "Scale Only");
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
	public static ResizePolicy getValue(String name) {
		if (name == null) {
			return null;
		}

		for (ResizePolicy fs : values()) {
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
		for (ResizePolicy resizePolicy : values()) {
			strArray[i] = names.get(resizePolicy);
			i++;
		}
		return strArray;
	}
}
