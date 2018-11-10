package cnuphys.fastMCed.view.sector;

import java.util.EnumMap;

import cnuphys.bCNU.component.EnumComboBox;

public enum DisplaySectors {
	SECTORS14, SECTORS25, SECTORS36;

	/**
	 * A map for the names of the symbols
	 */
	public static EnumMap<DisplaySectors, String> names = new EnumMap<DisplaySectors, String>(
			DisplaySectors.class);

	static {
		names.put(SECTORS14, "Sectors 1 and 4");
		names.put(SECTORS25, "Sectors 2 and 5");
		names.put(SECTORS36, "Sectors 3 and 6");
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
	 * @return the <code>DisplaySectors</code> that corresponds to the name. Returns
	 *         <code>null</code> if no match is found. Note it will check (case
	 *         insensitive) both the map and the <code>name()</code> result.
	 */
	public static DisplaySectors getValue(String name) {
		if (name == null) {
			return null;
		}

		for (DisplaySectors val : values()) {
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
	 *            the default selection
	 * @return
	 */
	public static EnumComboBox getComboBox(DisplaySectors defaultChoice) {
		return new EnumComboBox(names, defaultChoice);
	}

}
