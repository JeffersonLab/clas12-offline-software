package cnuphys.splot.style;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.util.EnumMap;

public enum SymbolType {
	NOSYMBOL, SQUARE, CIRCLE, CROSS, UPTRIANGLE, DOWNTRIANGLE, X, DIAMOND;

	/**
	 * A map for the names of the symbols
	 */
	public static EnumMap<SymbolType, String> names = new EnumMap<SymbolType, String>(SymbolType.class);

	static {
		names.put(SQUARE, "Square");
		names.put(CIRCLE, "Circle");
		names.put(CROSS, "Cross");
		names.put(DOWNTRIANGLE, "Down Triangle");
		names.put(UPTRIANGLE, "Up Triangle");
		names.put(X, "X");
		names.put(DIAMOND, "Diamond");
		names.put(NOSYMBOL, "No Symbol");
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
	 * @return the <code>SymbolType</code> that corresponds to the name. Returns
	 *         <code>null</code> if no match is found. Note it will check (case
	 *         insensitive) both the map and the <code>name()</code> result, thus
	 *         "Up Triangle" or "UPTRIANGLE" or "dUpTrIaNgLe" will return the
	 *         <code>UPTRIANGLE</code> value.
	 */
	public static SymbolType getValue(String name) {
		if (name == null) {
			return null;
		}

		for (SymbolType val : values()) {
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
	 * @return the combo box of symbol choices
	 */
	public static EnumComboBox getComboBox(SymbolType defaultChoice) {
		return new EnumComboBox(names, defaultChoice) {
			// size the combo box to just fit
			@Override
			protected void sizeComboBox() {
				Dimension d = getPreferredSize();

				FontMetrics fm = getFontMetrics(_font);
				int maxSW = 10;

				for (String s : names.values()) {
					maxSW = Math.max(maxSW, fm.stringWidth(s));
				}

				d.width = maxSW + 50;
				setPreferredSize(d);
				setMaximumSize(d);

			}
		};
	}

}