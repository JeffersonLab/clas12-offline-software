package cnuphys.bCNU.simanneal.example;

import java.util.EnumMap;

import cnuphys.bCNU.component.EnumComboBox;

public enum River {

	NORIVER, RIVERPENALTY, RIVERBONUS;
	
	/**
	 * A map for the names of the river values.
	 */
	public static EnumMap<River, String> names = new EnumMap<River, String>(
			River.class);

	static {
		names.put(NORIVER, "No River");
		names.put(RIVERPENALTY, "River Penalty");
		names.put(RIVERBONUS, "River Bonus");
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
	public static River getValue(String name) {
		if (name == null) {
			return null;
		}

		for (River riverVal : values()) {
			if (name.equalsIgnoreCase(riverVal.name())) {
				return riverVal;
			}
			if (name.equalsIgnoreCase(names.get(riverVal))) {
				return riverVal;
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
	 * Obtain a combo box of choices.
	 * 
	 * @param defaultChoice
	 * @return the combo box of symbol choices
	 */
	public static EnumComboBox getComboBox(River defaultChoice) {
		return new EnumComboBox(names, defaultChoice);
	}

}
