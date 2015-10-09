package cnuphys.bCNU.menu;

import javax.swing.JMenu;

/**
 * Basic menu used for global bCNU menus such as file, view, etc.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public abstract class ABaseMenu extends JMenu {

	/**
	 * Constructor for an ABaseMenu. It calls the more general constructor with
	 * <code>true</code> as the second argument.
	 * 
	 * @param label
	 *            the menu label
	 */
	public ABaseMenu(String label) {
		this(label, true);
	}

	/**
	 * Constructor for an ABaseMenu
	 * 
	 * @param label
	 *            the menu label
	 * @param addDefaultItems
	 *            if <code>true</code> tells subclasses to add their default
	 *            menu items.
	 */
	public ABaseMenu(String label, boolean addDefaultItems) {
		super(label);
		if (addDefaultItems) {
			addDefaultItems();
		}
	}

	// add default menu items
	protected abstract void addDefaultItems();

}
