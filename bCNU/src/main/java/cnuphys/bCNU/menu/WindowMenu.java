package cnuphys.bCNU.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import cnuphys.bCNU.application.Desktop;
import cnuphys.bCNU.view.BaseView;

/**
 * This singleton class creates a JMenu for a primitive list of all internal
 * frames in the window.
 * 
 * TODO Make options for window layouts
 * 
 * @author deloso
 *
 */

@SuppressWarnings("serial")
public class WindowMenu extends JMenu {

    private static boolean _useWindowMenu = true;

    // singleton instance
    private static WindowMenu instance;

    // Class variables
    private ArrayList<BaseView> windowList;
    private ArrayList<JMenuItem> menuItemList;

    /**
     * Obtain the singleton.
     * 
     * @return the singleton ViewManager object.
     */
    public static WindowMenu getInstance() {
	if (!_useWindowMenu) {
	    return null;
	}

	if (instance == null) {
	    instance = new WindowMenu();
	}
	return instance;
    }

    /**
     * Private constructor used to create singleton.
     */
    private WindowMenu() {
	// Make JMenu and add it to the MenuManager singleton
	super("Window");
	MenuManager.getInstance().addMenu(this);
	windowList = new ArrayList<BaseView>();

	// Make menuitem list
	menuItemList = new ArrayList<JMenuItem>();

	// Get current, visible windows and add to list
	JInternalFrame[] frames = Desktop.getInstance().getAllFrames();
	for (int i = 0; i < frames.length; i++) {
	    BaseView v = (BaseView) frames[i];
	    if (v.isVisible()) {
		addWindowToList(v);
	    }
	}
    }

    /**
     * Adds a BaseView to the list
     * 
     * @param v
     *            the BaseView to add
     */
    public void addWindowToList(BaseView v) {

	if (!_useWindowMenu) {
	    return;
	}

	if (!windowList.contains(v)) {
	    // Add to ArrayList
	    windowList.add(v);

	    // Make an ActionListener
	    ActionListener action = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		    // Grab title
		    String title = e.getActionCommand();
		    for (int i = 0; i < windowList.size(); i++) {
			// Get correct frame selected from menu list
			if (title.equals(windowList.get(i).getTitle())) {
			    // If the window is not already on top, activate it
			    if (!windowList.get(i).isOnTop()) {
				Desktop.getInstance().getDesktopManager()
					.activateFrame(windowList.get(i));
			    }
			    if (windowList.get(i).isIcon()) {
				try {
				    windowList.get(i).setIcon(false);
				} catch (PropertyVetoException e1) {
				}
			    }
			}
		    }
		}
	    };

	    // Make JMenuItem
	    JMenuItem windowItem = MenuManager.addMenuItem(v.getTitle(),
		    MenuManager.getInstance().getMenu("Window"), action);
	    menuItemList.add(windowItem);
	}
    }

    /**
     * Removes a BaseView from the list
     */
    public void removeWindowFromList(BaseView v) {
	if (!_useWindowMenu) {
	    return;
	}

	int index = windowList.indexOf(v);
	MenuManager.getInstance().getMenu("Window")
		.remove(menuItemList.get(index));
	windowList.remove(index);
	menuItemList.remove(index);
    }

    /**
     * As asked, the following disables the button if its respected window is
     * already on top TODO not properly disabling, instead seems to make it
     * invisible
     */
    public void setDisabled(BaseView v) {
	if (!_useWindowMenu) {
	    return;
	}

	for (int i = 0; i < windowList.size(); i++) {
	    MenuManager.getInstance().getMenu("Window").getItem(i)
		    .setEnabled(true);
	}
	int index = windowList.indexOf(v);
	MenuManager.getInstance().getMenu("Window").getItem(index)
		.setEnabled(false);
    }

    public static boolean useWindowMenu() {
	return _useWindowMenu;
    }

    public static void setUseWindowMenu(boolean useWindowMenu) {
	_useWindowMenu = useWindowMenu;
    }

    /**
     * Singleton objects cannot be cloned, so we override clone to throw a
     * CloneNotSupportedException.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }
}
