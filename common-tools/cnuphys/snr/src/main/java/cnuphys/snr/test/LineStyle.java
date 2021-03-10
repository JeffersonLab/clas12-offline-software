package cnuphys.snr.test;

import java.util.EnumMap;

public enum LineStyle {
	SOLID, DASH, DOT_DASH, DOT, DOUBLE_DASH, LONG_DASH, LONG_DOT_DASH;

	/**
	 * A map for the names of the line styles
	 */
	public static EnumMap<LineStyle, String> names = new EnumMap<LineStyle, String>(LineStyle.class);

	static {
		names.put(SOLID, "Solid");
		names.put(DASH, "Dashed");
		names.put(DOT_DASH, "Dot Dash");
		names.put(DOT, "Dotted");
		names.put(DOUBLE_DASH, "Double Dash");
		names.put(LONG_DASH, "Long Dash");
		names.put(LONG_DOT_DASH, "Long Dot Dash");
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
	 * @param name the name to match.
	 * @return the <code>LineStyle</code> that corresponds to the name. Returns
	 *         <code>null</code> if no match is found. Note it will check (case
	 *         insensitive) both the map and the <code>name()</code> result, thus
	 *         "Up Triangle" or "UPTRIANGLE" or "UpTrIaNgLe" will return the
	 *         <code>UPTRIANGLE</code> value.
	 */
	public static LineStyle getValue(String name) {
		if (name == null) {
			return null;
		}

		for (LineStyle val : values()) {
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

}
