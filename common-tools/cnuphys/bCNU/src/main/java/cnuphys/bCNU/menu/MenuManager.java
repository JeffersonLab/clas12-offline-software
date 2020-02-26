package cnuphys.bCNU.menu;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.Hashtable;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import cnuphys.bCNU.log.Log;

public class MenuManager {

	// Singleton object
	private static MenuManager instance;

	// file menu
	private static JMenu _fileMenu;

	// option menu
	private static JMenu _optionMenu;
	/**
	 * The BaseMDIApplication being managed.
	 */
	private JMenuBar _menuBar;

	// keep track of the menus added
	private Hashtable<String, JMenu> _menus = new Hashtable<String, JMenu>(41);

	/**
	 * private constructor for singleton.
	 * 
	 * @param menuBar the main menubar
	 */
	private MenuManager(JMenuBar menuBar) {
		_menuBar = menuBar;
	}

	/**
	 * Public access for the singleton.
	 * 
	 * @param menubar the main menu bar
	 * @return the menu manager for the one and only BaseMDIApplication.
	 */
	public static MenuManager createMenuManager(JMenuBar menubar) {
		if (instance == null) {
			instance = new MenuManager(menubar);
		}
		return instance;
	}

	/**
	 * This one is used after the menu manager is created. Then you can add
	 * menus to the main frame without a reference to it.
	 * 
	 * @return the menu manager for the one and only BaseMDIApplication.
	 */
	public static MenuManager getInstance() {
		return instance;
	}

	/**
	 * Add a menu to the main menu bar.
	 * 
	 * @param menu the menu to add.
	 */
	public void addMenu(JMenu menu) {
		if (_menuBar != null) {
			_menuBar.add(menu);
		}
		// put into the menu hash
		_menus.put(menu.getText(), menu);
		
		//seems to be necessary on some linus platforms
		menu.setForeground(Color.black);
	}

	/**
	 * Get a menu based on its name.
	 * 
	 * @param text the name of the menu, e.g., "File".
	 * @return the menu, if it finds it.
	 */
	public JMenu getMenu(String text) {
		return _menus.get(text);
	}

	/**
	 * Get the file menu.
	 * 
	 * @return the file menu, if it has been set.
	 */
	public JMenu getFileMenu() {
		if (_fileMenu == null) {
			_fileMenu = getMenu(FileMenu.menuLabel);
		}
		return _fileMenu;
	}

	/**
	 * Get the option menu.
	 * 
	 * @return the option menu, if it has been set.
	 */
	public JMenu getOptionMenu() {
		if (_optionMenu == null) {
			_optionMenu = getMenu(OptionMenu.menuLabel);
		}
		return _optionMenu;
	}

	/**
	 * Remove an unwanted menu from the menu bar
	 * 
	 * @param menu the menu to remove
	 */
	public void removeMenu(JMenu menu) {
		_menuBar.remove(menu);
	}

	/**
	 * Set the file menu
	 * 
	 * @param menu the file menu
	 */
	public static void setFileMenu(JMenu menu) {
		_fileMenu = menu;
	}

	/**
	 * Convenience routine for adding a checkbox menu item.
	 * 
	 * @param label the menu label.
	 * @param menu optional menu to add the item to.
	 * @param ilist optional ItemListener.
	 */

	public static JCheckBoxMenuItem addCheckboxMenuItem(String label,
			JMenu menu, ItemListener ilist) {
		return addCheckboxMenuItem(label, menu, ilist, false);
	}

	/**
	 * Convenience routine for adding a checkbox menu item.
	 * 
	 * @param label the menu label.
	 * @param menu optional menu to add the item to.
	 * @param ilist optional ItemListener.
	 * @param state initial state.
	 */

	public static JCheckBoxMenuItem addCheckboxMenuItem(String label,
			JMenu menu, ItemListener ilist, boolean state) {

		JCheckBoxMenuItem mitem = null;

		mitem = new JCheckBoxMenuItem((label != null) ? label : "???", state);

		if (menu != null) {
			menu.add(mitem);
		}

		if (ilist != null) {
			mitem.addItemListener(ilist);
		}

		return mitem;
	}

	/**
	 * Convenience routine for adding a menu item.
	 * 
	 * @param label the menu label.
	 * @param mnemonic the mnemonic character.
	 * @param index the index (0 based) at which to add the item.
	 * @param menu the menu to add the item to.
	 * @param alist optional action listener.
	 */

	public static JMenuItem addMenuItem(String label, char mnemonic, int index,
			JMenu menu, ActionListener alist) {
		JMenuItem mitem = null;

		if ((label != null) && (menu != null)) {
			try {
				mitem = new JMenuItem(label);
				menu.add(mitem, index);
				if (mnemonic != '\0') {
					mitem.setMnemonic(mnemonic);
				}

				if (alist != null) {
					mitem.addActionListener(alist);
				}

			} catch (Exception e) {
				Log.getInstance().exception(e);
				e.printStackTrace();
			}
		}

		return mitem;
	}

	/**
	 * Convenience routine for adding a menu item.
	 * 
	 * @param label the menu label.
	 * @param mnemonic the mnemonic character.
	 * @param menu the menu to add the item to.
	 * @param alist optional action listener.
	 */

	public static JMenuItem addMenuItem(String label, char mnemonic, JMenu menu,
			ActionListener alist) {

		return addMenuItem(label, mnemonic, -1, menu, alist);
	}

	/**
	 * Convenience routine for adding a menu item.
	 * 
	 * @param label the menu label.
	 * @param index the index (0 based) at which to add the item.
	 * @param menu the menu to add the item to.
	 * @param alist optional action listener.
	 */

	public static JMenuItem addMenuItem(String label, int index, JMenu menu,
			ActionListener alist) {

		return addMenuItem(label, '\0', index, menu, alist);
	}

	/**
	 * Convenience routine for adding a menu item.
	 * 
	 * @param label the menu label;
	 * @param menu the menu to add the item to.
	 * @param alist optional action listener.
	 */

	public static JMenuItem addMenuItem(String label, JMenu menu,
			ActionListener alist) {

		return addMenuItem(label, '\0', menu, alist);
	}

	/**
	 * Singleton objects cannot be cloned, so we override clone to throw a
	 * CloneNotSupportedException.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * @return the menuBar
	 */
	public JMenuBar getMenuBar() {
		return _menuBar;
	}

}
