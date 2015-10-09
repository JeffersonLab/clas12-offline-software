package cnuphys.bCNU.item;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import cnuphys.bCNU.layer.LogicalLayer;

@SuppressWarnings("serial")
public class ItemOrderingMenu extends JMenu implements ActionListener {
	/**
	 * Create the menu items one time only.
	 */

	protected static JMenuItem menuItems[] = null;

	protected static String menuLabels[] = null;

	protected static AItem hotItem = null;

	/**
	 * Used to get from resource bundle
	 */
	protected static String moveNames[] = { "Bring {0} to Front of Layer {1}",
			"Send {0} to Back of Layer {1}", "Bring {0} Forward in Layer {1}",
			"Send {0} Backward in Layer {1}" };

	protected static final int BRINGTOFRONT = 0;

	protected static final int SENDTOBACK = 1;

	protected static final int BRINGFORWARD = 2;

	protected static final int SENDBACKWARD = 3;

	/**
	 * Singleton
	 */

	private static ItemOrderingMenu orderingMenu = null;

	private ItemOrderingMenu() {
		super("Item Ordering");
	}

	/**
	 * Convenience routing to use the same static menu items on a regular
	 * menu--for example one that might be on the item popup or the main menu.
	 * 
	 * @param item
	 * @param insertItemName
	 */
	public static JMenu getItemOrderingMenu(AItem item, boolean insertItemName) {
		hotItem = item;
		createMenuItems();
		setLabels(item, insertItemName);
		return orderingMenu;
	}

	/**
	 * Create the ordering menu items (if not already created)
	 */

	protected static void createMenuItems() {

		// if already created, do nothing

		if (orderingMenu != null) {
			return;
		}

		orderingMenu = new ItemOrderingMenu();

		menuItems = new JMenuItem[4];
		menuLabels = new String[4];

		for (int i = 0; i < menuItems.length; i++) {
			menuItems[i] = new JMenuItem();
			menuItems[i].addActionListener(orderingMenu);

			// get the unformatted labels
			menuLabels[i] = moveNames[i];
			orderingMenu.add(menuItems[i]);
		}
	}

	/**
	 * Set the menu item text every time, since they might include the item
	 * name.
	 * 
	 * @param item
	 * @param insertItemName
	 */
	private static void setLabels(AItem item, boolean insertItemName) {
		String itemName = null;
		String layerName = null;

		if (insertItemName) {
			itemName = item.getName();
			layerName = item.getLayer().getName();
		}

		String o1 = "";
		String o2 = "";

		if (itemName != null) {
			o1 = "\"" + itemName + "\"";
		}
		if (layerName != null) {
			o2 = "\"" + layerName + "\"";
		}

		Object objects[] = { o1, o2 };

		for (int i = 0; i < menuItems.length; i++) {
			String s = MessageFormat.format(menuLabels[i], objects);
			menuItems[i].setText(s);
		}
	}

	/**
	 * A menu item has been selected
	 * 
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if (hotItem == null) {
			return;
		}

		LogicalLayer layer = hotItem.getLayer();

		Object source = e.getSource();
		if (source == menuItems[BRINGTOFRONT]) {
			layer.sendToFront(hotItem);
		} else if (source == menuItems[SENDTOBACK]) {
			layer.sendToBack(hotItem);
		} else if (source == menuItems[BRINGFORWARD]) {
			layer.sendForward(hotItem);
		} else if (source == menuItems[SENDBACKWARD]) {
			layer.sendBackward(hotItem);
		}
		layer.getContainer().refresh();
	}

}
