package cnuphys.bCNU.component.led;

import java.awt.Color;
import java.util.EnumMap;

import javax.swing.ImageIcon;

import cnuphys.bCNU.graphics.ImageManager;

public enum LedState {
	RED, YELLOW, GREEN, UNKNOWN;

	/**
	 * Red light icon.
	 */
	private static ImageIcon red_icon;

	/**
	 * Yellow light icon.
	 */
	private static ImageIcon yellow_icon;

	/**
	 * Green light icon.
	 */
	private static ImageIcon green_icon;

	/**
	 * Unknown icon.
	 */
	private static ImageIcon unknown_icon;

	static {
		red_icon = ImageManager.getInstance().loadImageIcon(
				"images/red-led.png");
		green_icon = ImageManager.getInstance().loadImageIcon(
				"images/green-led.png");
		yellow_icon = ImageManager.getInstance().loadImageIcon(
				"images/yellow-led.png");
		unknown_icon = ImageManager.getInstance().loadImageIcon(
				"images/off-led.png");
	};

	/**
	 * A map for names of the <code>LedStyle</code> enum values.
	 */
	public static final EnumMap<LedState, String> names = new EnumMap<LedState, String>(
			LedState.class);

	static {
		names.put(LedState.RED, "Red");
		names.put(LedState.YELLOW, "Yellow");
		names.put(LedState.GREEN, "Green");
		names.put(LedState.UNKNOWN, "Unknown");
	}

	/**
	 * A map for colors of the <code>LedStyle</code> enum values.
	 */
	public static final EnumMap<LedState, Color> colors = new EnumMap<LedState, Color>(
			LedState.class);

	static {
		colors.put(LedState.RED, Color.red);
		colors.put(LedState.YELLOW, Color.yellow);
		colors.put(LedState.GREEN, Color.green.darker());
		colors.put(LedState.UNKNOWN, Color.lightGray);
	}

	/**
	 * A map for icons of the <code>LedStyle</code> enum values.
	 */
	public static final EnumMap<LedState, ImageIcon> icons = new EnumMap<LedState, ImageIcon>(
			LedState.class);

	static {
		icons.put(LedState.RED, red_icon);
		icons.put(LedState.YELLOW, yellow_icon);
		icons.put(LedState.GREEN, green_icon);
		icons.put(LedState.UNKNOWN, unknown_icon);
	}

	/**
	 * Returns the enum value from the name.
	 * 
	 * @param name
	 *            the name to match.
	 * @return the <code>LineStyle</code> that corresponds to the name. Returns
	 *         <code>null</code> if no match is found. Note it will check (case
	 *         insensitive) both the map and the <code>name()</code> result,
	 *         thus "Dot Dash" or "DOT_DASH" or "dOT_dASh" will return the
	 *         <code>DOT_DASH</code> value.
	 */
	public static LedState getValue(String name) {
		if (name == null) {
			return null;
		}

		for (LedState cs : values()) {
			if (name.equalsIgnoreCase(cs.name())) {
				return cs;
			}
			if (name.equalsIgnoreCase(names.get(cs))) {
				return cs;
			}
		}
		return null;
	}

	/**
	 * Get the color to display.
	 * 
	 * @return the color to display.
	 */
	public Color getColor() {
		return colors.get(this);
	}

	/**
	 * Get the icon to display.
	 * 
	 * @return the icon to display.
	 */
	public ImageIcon getIcon() {
		return icons.get(this);
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
		for (LedState cs : values()) {
			strArray[i] = names.get(cs);
			i++;
		}
		return strArray;
	}
}
