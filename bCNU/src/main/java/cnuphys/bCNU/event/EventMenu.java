package cnuphys.bCNU.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;

import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioReader;

import cnuphys.bCNU.et.ETSupport;
import cnuphys.bCNU.event.graphics.EventInfoPanel;
import cnuphys.bCNU.event.graphics.EventTreeMenu;
import cnuphys.bCNU.event.graphics.EventTreePanel;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.view.EventView;

public class EventMenu extends EventTreeMenu {

    private static String _recentFileKey = "RecentEvioFiles";

    /**
     * Extensions used for evio files
     */
    public static String _extensions[] = { "ev", "ev0", "ev1", "ev2", "ev3",
	    "ev4", "ev5", "ev6", "ev7", "ev8", "ev9", "evio" };

    /** check box for file as event source */
    private static JRadioButtonMenuItem fileRadioMenuItem; // file

    /** check box for ET as event source */
    private static JRadioButtonMenuItem etRadioMenuItem; // ET system

    // for opening files recently openened
    private static JMenu _recentMenu;

    // singleton
    private static EventMenu instance;

    // a hash table of menu items
    private static Hashtable<String, JMenuItem> _menuItems;

    // next event
    protected static JMenuItem nextEventItem;

    // prev event
    protected static JMenuItem prevEventItem;

    // accumulation item
    protected static JMenuItem accumulationItem;

    // got an event
    protected static JTextField _gotoTextField;

    // in spite of its name, this class is not a descendant of
    // JMenu. The actial event menu is stored here
    protected static JMenu _menu;

    /**
     * Constructor. Holds the menus for a frame or internal frame that wants to
     * manage a tree panel.
     * 
     * @param eventTreePanel
     *            holds the tree and all associated the widgets.
     */
    private EventMenu(final EventTreePanel eventTreePanel) {
	super(eventTreePanel);
    }

    /**
     * Create the singleton
     * 
     * @param eventTreePanel
     *            the required EventTreePanel
     * @return the singleton
     */
    public static EventMenu create(EventTreePanel eventTreePanel) {
	if (instance == null) {
	    instance = new EventMenu(eventTreePanel);
	}

	return instance;
    }

    /**
     * Set the actual JMenu event menu object.
     * 
     * @param menu
     *            the actual JMenu event menu object.
     */
    public void setMenu(JMenu menu) {
	_menu = menu;
    }

    /**
     * Get the actual JMenu event menu object.
     * 
     * @return the actual JMenu event menu.
     */
    public JMenu getMenu() {
	return _menu;
    }

    /**
     * Access to the singleton. Might be <code>null</code>.
     * 
     * @return the singleton
     */
    public static EventMenu getInstance(EventTreePanel eventTreePanel) {
	if (instance == null) {
	    instance = create(eventTreePanel);
	}
	return instance;
    }

    /**
     * Add some menu items to the event menu
     */
    public static void menuAdditions() {

	if (_menu == null) {
	    Log.getInstance()
		    .warning(
			    "Tried to add items to the event menu but it has not been set.");
	}
	Log.getInstance().info("Adding late items to the event menu.");

	ButtonGroup bg = new ButtonGroup();

	ActionListener sal = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		EventMenu.fixItems();
	    }

	};

	fileRadioMenuItem = new JRadioButtonMenuItem(
		"Events From Files",
		EventControl.getEventSourceType() == EventControl.EventSourceType.FILE);
	etRadioMenuItem = new JRadioButtonMenuItem(
		"Events From ET",
		EventControl.getEventSourceType() == EventControl.EventSourceType.ET);

	EventMenu.fileRadioMenuItem.addActionListener(sal);
	EventMenu.etRadioMenuItem.addActionListener(sal);

	etRadioMenuItem.setEnabled(false);

	// ImageIcon icon = ImageManager.getInstance().loadImageIcon(
	// "images/et.png");
	// EventMenu.etRadioMenuItem.setIcon(icon);

	bg.add(EventMenu.fileRadioMenuItem);
	bg.add(EventMenu.etRadioMenuItem);

	_menu.add(fileRadioMenuItem, 0);
	_menu.add(etRadioMenuItem, 1);
	_menu.insertSeparator(2);
    }

    /**
     * Get the radio button that sets the event source to an event file
     * 
     * @return the radio button that sets the event source to an event file
     */
    public static JRadioButtonMenuItem getFileSourceRadioButton() {
	return fileRadioMenuItem;
    }

    /**
     * Get the radio button that sets the event source to ET
     * 
     * @return the radio button that sets the event source to ET
     */
    public static JRadioButtonMenuItem getETSourceRadioButton() {
	return etRadioMenuItem;
    }

    /**
     * Set the menu's goto text field
     * 
     * @param gotoTF
     *            the goto text field
     */
    public static void setGoToTextField(JTextField gotoTF) {
	_gotoTextField = gotoTF;
    }

    /**
     * Set the menu's next event menu item
     * 
     * @param mi
     *            the next event menu item
     */
    public static void setNextEventItem(JMenuItem mi) {
	nextEventItem = mi;
    }

    /**
     * Set the menu's previous event menu item
     * 
     * @param mi
     *            the previous event menu item
     */
    public static void setPrevEventItem(JMenuItem mi) {
	prevEventItem = mi;
    }

    /**
     * Set the menu's accumulate event menu item
     * 
     * @param mi
     *            the accumulate event menu item
     */
    public static void setAccumulationItem(JMenuItem mi) {
	accumulationItem = mi;
    }

    /**
     * Give all the items the correct selectability
     */
    public static void fixItems() {
	if (instance == null) {
	    return;
	}

	EventInfoPanel infoPanel = null;
	if (instance != null) {
	    infoPanel = instance.eventTreePanel.getEventInfoPanel();
	}

	if (infoPanel != null) {
	    if (EventControl.isSourceFile()) {

		EvioReader reader = EventControl.getEvioReader();
		if (reader == null) {
		    infoPanel.setSource("");
		    infoPanel.setNumberOfEvents(-1);
		} else {
		    try {
			infoPanel.setSource(instance.dataFilePath);
			infoPanel.setNumberOfEvents(reader.getEventCount());
		    } catch (IOException e) {
			e.printStackTrace();
		    } catch (EvioException e) {
			e.printStackTrace();
		    }
		}
	    } else if (EventControl.isSourceET()) {
		if (instance != null) {
		    instance.eventTreePanel.getEventInfoPanel().setSource(
			    "ET System");
		    instance.eventTreePanel.getEventInfoPanel()
			    .setNumberOfEvents(-1);
		}
	    }
	} // infopanel not null

	if (nextEventItem != null) {
	    nextEventItem.setEnabled(EventControl.isNextOK());
	}
	if (prevEventItem != null) {
	    prevEventItem.setEnabled(EventControl.isPrevOK());
	}

	if (_gotoTextField != null) {
	    _gotoTextField.setEnabled(EventControl.isSourceFile()
		    && EventControl.isNextOK());
	}

	// stand alone viewer has no accumulation
	if (accumulationItem != null) {
	    accumulationItem.setEnabled(EventControl.isNextOK());
	}

	if (etRadioMenuItem != null) {
	    etRadioMenuItem.setEnabled(ETSupport.isReady());
	}

	if (EventView.getInstance() != null) {
	    EventView.getInstance().checkButtons();
	}
    }

    /**
     * Select and open an event file.
     * 
     * @return the opened file reader, or <code>null</code>
     */
    @Override
    public EvioReader openEventFile() {

	File file = FileUtilities.openFile(FileUtilities.getDefaultDir(),
		"evio Files", _extensions);

	if (file == null) {
	    return null;
	}

	return openEventFile(file);
    }

    /**
     * Open an event file using a given file.
     * 
     * @param file
     *            the file to use, i.e., an event file
     * @return the opened file reader, or <code>null</code>
     */
    @Override
    public EvioReader openEventFile(File file) {
	EventMenu.updateRecentFiles(file);
	// System.err.println("OPEN EVENT FILE from openEventFile (Event Menu)");

	EvioReader reader = null;
	try {
	    reader = super.openEventFile(file);
	} catch (Exception e) {
	    System.err.println("Exception in EventMenu.openEventFile "
		    + e.getMessage());
	}
	EventControl.setEvioReader(reader);

	fixItems();
	return reader;
    }

    /**
     * Requests an event via the jevio library. Same as selecting "next event".
     * The event is returned here, and it will also be passed by jevio to the
     * registered IEvioListeners.
     */
    public EvioEvent nextEvent() {
	// comes here as a result of the next event menu item

	if (EventControl.getEventSourceType() == EventControl.EventSourceType.ET) {
	    EvioEvent event = ETSupport.nextEvent();
	    eventTreePanel.setEvent(event);

	    fixItems();
	    return event;
	}

	if (EventControl.getEvioReader() == null) {
	    fixItems();
	    return null;
	}

	EvioEvent event = null;
	try {

	    int eventIndex = 0;
	    if (EventControl.getCurrentEvent() != null) {
		eventIndex = EventControl.getCurrentEvent().getEventNumber();
	    }

	    // event = EventControl.getEvioReader().parseNextEvent();
	    event = EventControl.getEvioReader().parseEvent(eventIndex + 1);

	    // byte[] rb = event.getRawBytes();
	    // int len = rb.length;
	    // System.err.println("RB LEN: " + len);
	    // System.err.println("First four bytes: " + rb[0] + " "+ rb[1] +
	    // " "+ rb[2] + " "+ rb[3] + " ");
	    // System.err.println("Last four bytes: " + rb[len-4] + " "+
	    // rb[len-3] + " "+ rb[len-2] + " "+ rb[len-1] + " ");
	    eventTreePanel.setEvent(event);
	} catch (IOException e) {
	    Log.getInstance().exception(e);
	    e.printStackTrace();
	} catch (EvioException e) {
	    Log.getInstance().exception(e);
	    e.printStackTrace();
	}
	fixItems();
	return event;
    }

    /**
     * Requests an specific event via the jevio library. The event is returned
     * here, and it will also be passed by jevio to the registered
     * IEvioListeners.
     */
    public EvioEvent parseEvent(int index) {
	if (EventControl.getEvioReader() == null) {
	    return null;
	}

	if (index < 1) {
	    return null;
	}

	EvioEvent event = null;
	try {
	    event = EventControl.getEvioReader().parseEvent(index);
	    eventTreePanel.setEvent(event);
	} catch (IOException e) {
	    Log.getInstance().exception(e);
	    e.printStackTrace();
	} catch (EvioException e) {
	    Log.getInstance().exception(e);
	    e.printStackTrace();
	}
	return event;
    }

    /**
     * Requests an event via the jevio library. Same as selecting "next event".
     * The event is returned here, and it will also be passed by jevio to the
     * registered IEvioListeners.
     */
    public EvioEvent prevEvent() {

	if (EventControl.getEvioReader() == null) {
	    fixItems();
	    return null;
	}

	if (EventControl.getCurrentEvent() == null) {
	    fixItems();
	    return null;
	}

	int evNum = EventControl.getCurrentEvent().getEventNumber();
	if (evNum < 2) {
	    return EventControl.getCurrentEvent();
	}

	EvioEvent event = null;
	try {
	    try {
		System.err.println("Will try to go to event number: "
			+ (evNum - 1));
		event = EventControl.getEvioReader().parseEvent(evNum - 1);
	    } catch (EvioException e) {
		e.printStackTrace();
	    }
	    eventTreePanel.setEvent(event);
	} catch (IOException e) {
	    Log.getInstance().exception(e);
	    e.printStackTrace();
	}

	fixItems();
	return event;
    }

    /**
     * Get the menu from which you can choose a recently opened file
     * 
     * @return the menu from which you can choose a recently opened file
     */
    public static JMenu getRecentEvioFileMenu() {
	if (_recentMenu == null) {
	    _recentMenu = new JMenu("Recent Event Files");

	    // get the recent files from the prefs
	    Vector<String> recentFiles = Environment.getInstance()
		    .getPreferenceList(_recentFileKey);

	    if (recentFiles != null) {
		for (String fn : recentFiles) {
		    addMenu(fn, false);
		}
	    }

	}
	return _recentMenu;
    }

    private static void addMenu(String path, boolean atTop) {
	JMenu menu = getRecentEvioFileMenu();

	// if it is in the hash, remove from has and from menu
	if (_menuItems != null) {
	    JMenuItem item = _menuItems.remove(path);
	    if (item != null) {
		menu.remove(item);
	    }
	} else {
	    _menuItems = new Hashtable<String, JMenuItem>(41);
	}

	ActionListener al = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent ae) {
		try {
		    String fn = ae.getActionCommand();
		    File file = new File(fn);
		    if (file.exists()) {
			instance.openEventFile(file);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }

	};
	JMenuItem item = new JMenuItem(path);
	item.addActionListener(al);
	_menuItems.put(path, item);
	if (atTop) {
	    menu.add(item, 0);
	} else {
	    menu.add(item);
	}

    }

    /**
     * Update the recent files for the recent file menu.
     * 
     * @param file
     *            the newly visited file.
     */
    public static void updateRecentFiles(File file) {
	if (file == null) {
	    return;
	}
	Vector<String> recentFiles = Environment.getInstance()
		.getPreferenceList(_recentFileKey);
	if (recentFiles == null) {
	    recentFiles = new Vector<String>(10);
	}

	// keep no more than 10
	recentFiles.remove(file.getPath());
	recentFiles.add(0, file.getPath());
	if (recentFiles.size() > 9) {
	    recentFiles.removeElementAt(9);
	}
	Environment.getInstance().savePreferenceList(_recentFileKey,
		recentFiles);

	// add to menu
	addMenu(file.getPath(), true);
    }
}
