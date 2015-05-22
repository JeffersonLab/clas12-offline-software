package cnuphys.bCNU.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioReader;

import cnuphys.bCNU.component.filetree.ExtensionFileFilter;
import cnuphys.bCNU.component.filetree.FileTreePanel;
import cnuphys.bCNU.event.EventControl;
import cnuphys.bCNU.event.EventMenu;
import cnuphys.bCNU.event.IPhysicsEventListener;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.menu.MenuManager;

@SuppressWarnings("serial")
public class EventPanel extends JPanel implements IPhysicsEventListener {

    /**
     * Constant used to set the file panel width
     */
    private static final int FILE_PANEL_WIDTH = 220;

    public static final String EVENTMENU = "Events";

    // public KeyAdapter _keyAdapter;

    // Holds the tree display and related widgets
    private static DragAndDropEventTreePanel eventTreePanel;

    // event tree menu
    private static EventMenu eventMenu;

    // for file dragdrop
    private static FileTreePanel fileTreePanel;

    private JCheckBox intsInHexButton;

    // get the next event
    private JButton nextEventButton;

    // get the previous event
    private JButton prevEventButton;

    /**
     * List of extension that appear in the file tree
     */
    public static ArrayList<String> extensions;

    /**
     * These are the extension the file filter will show
     */
    static {
	extensions = new ArrayList<String>(10);
	extensions.add("sev");
	extensions.add("ev");
	extensions.add("ev0");
	extensions.add("ev1");
	extensions.add("ev2");
	extensions.add("ev3");
	extensions.add("ev4");
	extensions.add("ev5");
	extensions.add("ev6");
	extensions.add("ev7");
	extensions.add("ev8");
	extensions.add("ev9");
	extensions.add("evt");
	extensions.add("evio");
    }

    public EventPanel() {
	setLayout(new BorderLayout());
	addContent();
	EventControl.getInstance().addPhysicsListener(this);
    }

    // add the panel's content
    private void addContent() {
	JPanel sPanel = new JPanel();
	sPanel.setLayout(new BorderLayout(2, 2));
	eventTreePanel = new DragAndDropEventTreePanel(this);
	intsInHexButton = new JCheckBox("Show ints in hex", false);
	GraphicsUtilities.setSizeSmall(intsInHexButton);

	ItemListener il = new ItemListener() {

	    @Override
	    public void itemStateChanged(ItemEvent arg0) {
		eventTreePanel.setIntsInHex(intsInHexButton.isSelected());
		eventTreePanel.refreshDisplay();
	    }

	};

	// next event
	nextEventButton = new JButton("Next Event");
	GraphicsUtilities.setSizeMini(nextEventButton);

	ActionListener buttonAl = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent ae) {
		Object source = ae.getSource();
		if (source == nextEventButton) {
		    if (!hasEventFile()) {
			Toolkit.getDefaultToolkit().beep();
		    } else {
			// System.err.println("HEY MAN (button 1--from frame, not view) source type: "
			// + EventControl.getEventSourceType());
			eventTreePanel.setEvent(requestEvent());
		    }
		} else if (source == prevEventButton) {
		    if (!hasEventFile()) {
			Toolkit.getDefaultToolkit().beep();

		    } else {
			eventMenu.prevEvent();
		    }
		}
	    }

	};

	nextEventButton.addActionListener(buttonAl);

	// previous event
	prevEventButton = new JButton("Previous Event");
	GraphicsUtilities.setSizeMini(prevEventButton);
	prevEventButton.addActionListener(buttonAl);

	// show ints as hex
	intsInHexButton.addItemListener(il);

	JPanel subPanel = new JPanel();
	subPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
	subPanel.add(nextEventButton);
	subPanel.add(prevEventButton);

	subPanel.setBorder(BorderFactory.createEtchedBorder());

	sPanel.add(subPanel, BorderLayout.NORTH);
	sPanel.add(eventTreePanel, BorderLayout.CENTER);

	fileTreePanel = createFileTreePanel();

	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		false, fileTreePanel, sPanel);
	splitPane.setResizeWeight(0.2);

	add(splitPane, BorderLayout.CENTER);

	// add the event menu to the main frame.

	MenuManager.getInstance().addMenu(createEventMenu());

	JMenuBar mbar = MenuManager.getInstance().getMenuBar();
	mbar.add(Box.createHorizontalStrut(20));
	mbar.add(Box.createHorizontalGlue());
	subPanel.add(EventView.createGoToPanel(eventMenu));
	subPanel.add(intsInHexButton);
    }

    /**
     * Creates the file tree panel.
     */
    private FileTreePanel createFileTreePanel() {
	// the file tree
	ExtensionFileFilter filter = new ExtensionFileFilter(extensions);
	FileTreePanel fileTree = new FileTreePanel(filter);
	Dimension size = fileTree.getPreferredSize();
	size.width = FILE_PANEL_WIDTH;
	fileTree.setPreferredSize(size);

	// fileTree.setMaximumSize(new Dimension(10000, 10000));
	fileTree.addFileTreeListener(eventTreePanel);
	return fileTree;
    }

    /**
     * Get the event tree panel on this view.
     * 
     * @return the event tree panel on this view.
     */
    public DragAndDropEventTreePanel getEventTreePanel() {
	return eventTreePanel;
    }

    public static EvioReader getEvioReader() {
	return EventControl.getEvioReader();
    }

    private void checkState() {
    }

    // get the number of events
    public static int getEventCount() {
	if (getEvioReader() == null) {
	    return 0;
	}
	try {
	    return getEvioReader().getEventCount();
	} catch (IOException e) {
	    e.printStackTrace();
	    return 0;
	} catch (EvioException e) {
	    e.printStackTrace();
	    return 0;
	}
    }

    /**
     * Checks whether we have an event file
     * 
     * @return <code>true</code> if we have a current event file.
     */
    public static boolean hasEventFile() {
	return (getEventCount() > 0);
	// return eventTreePanel.getEvioFile() != null;
    }

    /**
     * Gets the event file path, if we have one.
     * 
     * @return the event file path, if we have one.
     */
    public static String eventFilePath() {
	if (getEvioReader() == null) {
	    return null;
	} else {
	    return getEvioReader().getPath();
	}
    }

    /**
     * Total number of events in the file.
     * 
     * @return the total number of events in the file.
     */
    public static int numberOfEvents() {
	if (getEvioReader() == null) {
	    return 0;
	}
	try {
	    return getEvioReader().getEventCount();
	} catch (Exception e) {
	    e.printStackTrace();
	    return 0;
	}
    }

    /**
     * Total number of events remaining in the file.
     * 
     * @return the total number of events remaining in the file.
     */
    public static int numberOfRemainingEvents() {
	if (getEvioReader() == null) {
	    return 0;
	}
	try {
	    return getEvioReader().getNumEventsRemaining();
	} catch (Exception e) {
	    e.printStackTrace();
	    return 0;
	}
    }

    /**
     * Create an event menu to add to the main menu bar.
     * 
     * TODO separate out file menu and move to other file menu
     * 
     * @return JMenu for Event
     */
    private JMenu createEventMenu() {
	JMenu menu = new JMenu(EVENTMENU);
	eventMenu = EventMenu.getInstance(eventTreePanel);

	EventView.addOpenMenuItem(menu, eventMenu);
	EventView.addNextMenuItem(menu, eventMenu);
	EventView.addPrevMenuItem(menu, eventMenu);
	EventView.addGotoSelector(menu, eventMenu);

	return menu;
    }

    /**
     * Requests an event via the jevio library. Same as selecting "next event".
     * The event is returned here, and it will also be passed by jevio to the
     * registered IEvioListeners.
     */
    public static EvioEvent requestEvent() {
	if (getEvioReader() == null) {
	    return null;
	}

	EvioEvent event = null;
	try {
	    // event = getEvioReader().parseNextEvent();
	    int eventIndex = 0;
	    if (EventControl.getCurrentEvent() != null) {
		eventIndex = EventControl.getCurrentEvent().getEventNumber();
	    }

	    event = getEvioReader().parseEvent(eventIndex + 1);
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
     * Handle an open manually, probably from a drag 'n drop
     * 
     * @param file
     */
    public void openEventFile(File file) {
	// System.err.println("OPEN EVENT FILE from openEventFile (EventPanel)");
	eventMenu.openEventFile(file);
	checkState();
    }

    /**
     * Check if a given string is an acceptable extension for events
     * 
     * @param name
     *            the name to check.
     * @return <code>true</code> if the name passes.
     */
    public static boolean goodExtension(String name) {

	for (String s : extensions) {
	    if (s.equalsIgnoreCase(name)) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public void newPhysicsEvent(EvioEvent event) {
	// System.err.println("Got a physics event");
    }

}
