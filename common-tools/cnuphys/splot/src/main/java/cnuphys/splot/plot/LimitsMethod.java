package cnuphys.splot.plot;

import java.util.EnumMap;

import cnuphys.splot.style.EnumComboBox;


/**
 * How we want to choose axes limits
 * @author heddle
 *
 */

public enum LimitsMethod {
	
	MANUALLIMITS, ALGORITHMICLIMITS, USEDATALIMITS;
	
	/**
	 * A map for the names of the limit methods
	 */
	public static EnumMap<LimitsMethod, String> names = new EnumMap<LimitsMethod, String>(LimitsMethod.class);

	static {
		names.put(MANUALLIMITS,      "Manually enter limits");
		names.put(ALGORITHMICLIMITS, "Algorithmic limits");
		names.put(USEDATALIMITS,     "Use data limits");
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
	 * @return the <code>LimitsMethod</code> that corresponds to the name. Returns
	 *         <code>null</code> if no match is found.
	 */
	public static LimitsMethod getValue(String name) {
		if (name == null) {
			return null;
		}

		for (LimitsMethod val : values()) {
			// check the nice name
			if (name.equalsIgnoreCase(val.getName())) {
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
	 * @return the combo box of limit methods
	 */
	public static EnumComboBox getComboBox(LimitsMethod defaultChoice) {
		return new EnumComboBox(names, defaultChoice);
	}

}
