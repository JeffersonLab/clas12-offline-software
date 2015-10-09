package cnuphys.bCNU.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.component.filetree.ExtensionFileFilter;
import cnuphys.bCNU.component.filetree.FileTreePanel;
import cnuphys.bCNU.event.EventControl;
import cnuphys.bCNU.event.EventMenu;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.menu.MenuManager;
import cnuphys.bCNU.util.Environment;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioReader;

/**
 * This is the view that displays a tree of the evio event.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class EventView extends BaseView {
	/**
	 * Constant used to set the file panel width
	 */
	private static final int FILE_PANEL_WIDTH = 200;

	private final static String workingDir = Environment.getInstance()
			.getCurrentWorkingDirectory();
	// private final static String defaultDictionary = workingDir
	// + "/data/eviodict.xml";
	// private final static String secondaryDictionary = workingDir
	// + "/trunk/data/eviodict.xml";
	// private final static String defaultEventFileDir = workingDir + "/data";
	// private final static String secondaryEventFileDir = workingDir
	// + "/trunk/data";

	public static final String EVENTMENU = "Events";

	// public KeyAdapter _keyAdapter;

	// Holds the tree display and related widgets
	private static DragAndDropEventTreePanel eventTreePanel;

	// event tree menu
	private static EventMenu eventMenu;

	// for file dragdrop
	private static FileTreePanel fileTreePanel;

	// singleton--can only have one event view.
	private static EventView instance;

	private JCheckBox intsInHexButton;

	private JButton nextEventButton;
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
		extensions.add("dat");
		extensions.add("evio");
	}

	/**
	 * Constructor for the Event view, which manages events from an event file.
	 */
	private EventView() {
		super(AttributeType.TITLE, "Evio Event", AttributeType.ICONIFIABLE,
				true, AttributeType.MAXIMIZABLE, true, AttributeType.CLOSABLE,
				true, AttributeType.RESIZABLE, true, AttributeType.WIDTH, 950,
				AttributeType.HEIGHT, 600, AttributeType.VISIBLE, true);

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

		// next and prev event
		nextEventButton = new JButton(" Next ");
		prevEventButton = new JButton(" Prev ");
		GraphicsUtilities.setSizeMini(nextEventButton);
		GraphicsUtilities.setSizeMini(prevEventButton);

		ActionListener buttonAl = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				Object source = ae.getSource();

				if (source == nextEventButton) {
					// System.err.println("HEY MAN (button 2, from view not frame) source type: "
					// + EventControl.getEventSourceType());

					if (EventControl.getEventSourceType() == EventControl.EventSourceType.ET) {
						eventTreePanel.setEvent(eventMenu.nextEvent());
						EventMenu.fixItems();
						return;
					}

					if (!hasEventFile()) {
						Toolkit.getDefaultToolkit().beep();

					} else {
						if (numberOfRemainingEvents() > 0) {
							eventTreePanel.setEvent(eventMenu.nextEvent());
						} else {
							Toolkit.getDefaultToolkit().beep();
						}
					}
				}

				else if (source == prevEventButton) {
					if (!hasEventFile()) {
						Toolkit.getDefaultToolkit().beep();

					} else {
						if (numberOfRemainingEvents() > 0) {
							eventTreePanel.setEvent(eventMenu.prevEvent());
						} else {
							Toolkit.getDefaultToolkit().beep();
						}
					}

				}

				EventMenu.fixItems();
			}

		};

		nextEventButton.addActionListener(buttonAl);
		prevEventButton.addActionListener(buttonAl);

		// show ints as hex
		intsInHexButton.addItemListener(il);

		JPanel subPanel = new JPanel();
		subPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
		subPanel.add(nextEventButton);
		subPanel.add(prevEventButton);
		subPanel.add(intsInHexButton);

		subPanel.setBorder(BorderFactory.createEtchedBorder());

		sPanel.add(subPanel, BorderLayout.NORTH);
		sPanel.add(eventTreePanel, BorderLayout.CENTER);

		fileTreePanel = createFileTreePanel();

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				false, fileTreePanel, sPanel);
		splitPane.setResizeWeight(0.4);

		add(splitPane);

		// add the event menu to the main frame.

		MenuManager.getInstance().addMenu(createEventMenu());

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

	/**
	 * Create the event view, or return the already created singleton.
	 * 
	 * @return the event view singleton.
	 */
	public static EventView createEventView() {
		if (instance == null) {
			instance = new EventView();
		}
		return instance;
	}

	/**
	 * Access to the singleton (might be <code>null</code>
	 * 
	 * @return the event view singleton.
	 */
	public static EventView getInstance() {
		return instance;
	}

	public static EvioReader getEvioReader() {
		return EventControl.getEvioReader();
	}

	public static int getEventCount() {
		if (getEvioReader() == null) {
			return 0;
		}
		try {
			return getEvioReader().getEventCount();
		} catch (EvioException e) {
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
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
			int count = getEvioReader().getEventCount();
			EvioEvent currentEvent = EventControl.getCurrentEvent();
			int evNum = (currentEvent == null) ? 0 : currentEvent
					.getEventNumber();
			return count - evNum;
			// return getEvioReader().getNumEventsRemaining();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Check the state of the buttons
	 */
	public void checkButtons() {
		if (nextEventButton != null) {
			nextEventButton.setEnabled(EventControl.isNextOK());
		}
		if (prevEventButton != null) {
			prevEventButton.setEnabled(EventControl.isPrevOK());
		}
	}

	/**
	 * Create an event menu to add to the main menu bar.
	 * 
	 * @return JMenu for Events
	 */
	private JMenu createEventMenu() {
		JMenu menu = new JMenu(EVENTMENU);

		// weird note, neuther EventMenu nor its paranet class
		// is actually a menu
		eventMenu = EventMenu.create(eventTreePanel);
		eventMenu.setMenu(menu);

		addOpenMenuItem(menu, eventMenu);
		addNextMenuItem(menu, eventMenu);
		addPrevMenuItem(menu, eventMenu);
		addGotoSelector(menu, eventMenu);

		// menu.add(eventMenu.createFileMenu());
		// menu.add(eventMenu.createEventMenu());
		return menu;
	}

	/**
	 * Add an Open Event File item
	 * 
	 * @param menu
	 *            the Swing menu
	 * @param eventMenu
	 *            the jevio EventMenu
	 */
	public static void addOpenMenuItem(JMenu menu, final EventMenu eventMenu) {
		JMenuItem menuItem = new JMenuItem("Open Event File...");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				eventMenu.openEventFile();
			}
		};
		menuItem.addActionListener(al);
		menu.add(menuItem);
	}

	/**
	 * Add a Next Event item
	 * 
	 * @param menu
	 *            the Swing menu
	 * @param eventMenu
	 *            the jevio EventMenu
	 */
	public static void addNextMenuItem(JMenu menu, final EventMenu eventMenu) {
		JMenuItem menuItem = new JMenuItem("Next Event");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				eventMenu.nextEvent();
			}
		};
		menuItem.addActionListener(al);
		menu.add(menuItem);
		menuItem.setEnabled(false);
		EventMenu.setNextEventItem(menuItem);
	}

	/**
	 * Add a GoTo Event selector
	 * 
	 * @param menu
	 *            the Swing menu
	 * @param eventMenu
	 *            the jevio EventMenu
	 */
	public static void addGotoSelector(JMenu menu, final EventMenu eventMenu) {
		JPanel sp = createGoToPanel(eventMenu);
		menu.add(sp);
	}

	public static JPanel createGoToPanel(final EventMenu eventMenu) {
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

		JLabel label = new JLabel("Go To Event: ");

		final JTextField evnum = new JTextField("1", 10);

		KeyAdapter ka = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					MenuSelectionManager.defaultManager().clearSelectedPath();
					try {
						int enumber = Integer.parseInt(evnum.getText());
						eventMenu.parseEvent(enumber);
					} catch (Exception e) {

					}
				}
			}
		};
		evnum.addKeyListener(ka);

		sp.add(label);
		sp.add(evnum);
		evnum.setEnabled(false);
		EventMenu.setGoToTextField(evnum);
		return sp;
	}

	/**
	 * Add a Previous Event item
	 * 
	 * @param menu
	 *            the Swing menu
	 * @param eventMenu
	 *            the jevio EventMenu
	 */
	public static void addPrevMenuItem(JMenu menu, final EventMenu eventMenu) {
		JMenuItem menuItem = new JMenuItem("Previous Event");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				eventMenu.prevEvent();
			}
		};

		menuItem.addActionListener(al);
		menuItem.setEnabled(false);
		EventMenu.setPrevEventItem(menuItem);
		menu.add(menuItem);
	}

	/**
	 * Requests an event via the jevio library. Same as selecting "next event".
	 * The event is returned here, and it will also be passed by jevio to the
	 * registered IEvioListeners.
	 */
	public static EvioEvent requestEvent() {
		return eventMenu.nextEvent();
		// EvioEvent event = null;
		//
		// try {
		// try {
		// // event = getEvioReader().parseNextEvent();
		// int eventIndex = 0;
		// if (EventControl.getCurrentEvent() != null) {
		// eventIndex = EventControl.getCurrentEvent().getEventNumber();
		// }
		//
		// event = getEvioReader().parseEvent(eventIndex+1);
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// } catch (EvioException e) {
		// Log.getInstance().exception(e);
		// e.printStackTrace();
		// }
		// return event;
	}

	/**
	 * Handle an open manually, probably from a drag 'n drop
	 * 
	 * @param file
	 */
	public void openEventFile(File file) {
		try {
			// System.err.println("OPEN EVENT FILE from openEventFile (EventView)");

			eventMenu.manualOpenEventFile(file);
		} catch (Exception e) {
		}
		EventMenu.fixItems();
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

}
