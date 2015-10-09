package cnuphys.bCNU.menu;

@SuppressWarnings("serial")
public class OptionMenu extends ABaseMenu {

	// menu label
	public static final String menuLabel = "Options";

	/**
	 * Create the option menu.
	 */
	public OptionMenu() {
		super(menuLabel);
	}

	/**
	 * Create the option menu.
	 * 
	 * @param addDefaults
	 *            if <code>true</code> add the default menu items.
	 */
	public OptionMenu(boolean addDefaults) {
		super(menuLabel, addDefaults);
	}

	@Override
	protected void addDefaultItems() {
	}

}
