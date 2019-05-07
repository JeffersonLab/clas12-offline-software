package cnuphys.cnf.frame;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import cnuphys.bCNU.application.BaseMDIApplication;
import cnuphys.bCNU.application.Desktop;
import cnuphys.bCNU.component.MagnifyWindow;
import cnuphys.bCNU.dialog.TextDisplayDialog;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.menu.MenuManager;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.LogView;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.cnf.alldata.DataManager;
import cnuphys.cnf.event.EventManager;
import cnuphys.cnf.event.EventMenu;
import cnuphys.cnf.event.EventView;
import cnuphys.cnf.properties.PropertiesManager;
import cnuphys.lund.X11Colors;
import cnuphys.splot.example.MemoryUsageDialog;

@SuppressWarnings("serial")
public class Cnf extends BaseMDIApplication {

	// singleton
	private static Cnf _instance;

	// release string
	private static final String _release = "build 0.01";

	// used for one time inits
	private int _firstTime = 0;

	// for the event count
	private JMenuItem _eventCountLabel;

	// the virtual view
	private VirtualView _virtualView;

	// the log view
	private LogView _logView;

	// event menu
	private EventMenu _eventMenu;
	
	// event number label on menu bar
	private static JLabel _eventNumberLabel;
	
	// memory usage dialog
	private MemoryUsageDialog _memoryUsage;

	// Environment display
	private TextDisplayDialog _envDisplay;

	/** Last selected data file */
	private static String dataFilePath;
	
	//views
	private EventView _eventView;


	//stream events menu item
	private static JMenuItem _streamItem;

	/**
	 * Constructor (private--used to create singleton)
	 * 
	 * @param keyVals an optional variable length list of attributes in type-value
	 *                pairs. For example, PropertySupport.NAME, "my application",
	 *                PropertySupport.CENTER, true, etc.
	 */
	private Cnf(Object... keyVals) {
		super(keyVals);

		ComponentListener cl = new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent ce) {
			}

			@Override
			public void componentMoved(ComponentEvent ce) {
				placeViewsOnVirtualDesktop();
			}

			@Override
			public void componentResized(ComponentEvent ce) {
				placeViewsOnVirtualDesktop();
			}

			@Override
			public void componentShown(ComponentEvent ce) {
				placeViewsOnVirtualDesktop();
			}

		};

		addComponentListener(cl);
	}

	// arrange the views on the virtual desktop
	private void placeViewsOnVirtualDesktop() {
		if (_firstTime == 1) {
			// rearrange some views in virtual space
			_virtualView.reconfigure();
			restoreDefaultViewLocations();

			// now load configuration
			Desktop.getInstance().loadConfigurationFile();
			Desktop.getInstance().configureViews();
		}
		_firstTime++;
	}


	/**
	 * Restore the default locations of the default views. Cloned views are
	 * unaffected.
	 */
	private void restoreDefaultViewLocations() {

		_virtualView.moveTo(_logView, 7, VirtualView.CENTER);
		_virtualView.moveTo(_eventView, 0, VirtualView.CENTER);


		Log.getInstance().config("reset views on virtual dekstop");

	}

	/**
	 * Add the initial views to the desktop.
	 */
	private void addInitialViews() {

		// add a virtual view
		_virtualView = VirtualView.createVirtualView(8);
		ViewManager.getInstance().getViewMenu().addSeparator();
		
		// add event view
		_eventView = EventView.createEventView();


		// add the log view
		_logView = new LogView(800, 750, true);
//		_logView.setVisible(true);

		// log some environment info
		Log.getInstance().config(Environment.getInstance().toString());

		// use config file info
		// Desktop.getInstance().configureViews();

		_virtualView.toFront();
	}

	/**
	 * Accessor for the event menu
	 * 
	 * @return the event menu
	 */
	public EventMenu getEventMenu() {
		return _eventMenu;
	}

	/**
	 * Fix the event count label
	 */
	public void fixEventCount() {
		int count = EventManager.getInstance().getEventCount();
		if (count < Integer.MAX_VALUE) {
			_eventCountLabel.setText("Event Count: " + count);
		} else {
			_eventCountLabel.setText("Event Count: N/A");
		}
	}

	// add to the event menu
	private void addToEventMenu() {

		_eventMenu.remove(0);
		createStreamMenuItem();
		_eventCountLabel = new JMenuItem("Event Count: N/A");
		_eventCountLabel.setOpaque(true);
		_eventCountLabel.setBackground(Color.white);
		_eventCountLabel.setForeground(X11Colors.getX11Color("Dark Blue"));
		_eventMenu.add(_eventCountLabel);
	}
	
	//create the menu item to stream to the end of the file
	private void createStreamMenuItem() {
		
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
			}
			
		};
		_streamItem = new JMenuItem("Stream to End of File");
		_streamItem.setEnabled(false);
		_streamItem.addActionListener(al);
		_eventMenu.add(_streamItem, 0);
	}

	
	/**
	 * Notification of a change
	 */
	public void propertyChange(Object source, String name, Object oldVal, Object newVal) {
		if (name.equals(PropertiesManager.STATE_CHANGE)) {
			System.err.println("Cnf received a STATE_CHANGE notice");
			
			//any events remaining
			int numRemaining = EventManager.getInstance().getNumRemainingEvents();
			
			//set selectability
			_streamItem.setEnabled(numRemaining > 0);
		}

	}

	/**
	 * private access to the Cnf singleton.
	 * 
	 * @return the singleton Cnf (the main application frame.)
	 */
	public static Cnf getInstance() {
		if (_instance == null) {
			_instance = new Cnf(PropertySupport.TITLE, "cnf " + _release, PropertySupport.BACKGROUNDIMAGE,
					"images/cnu.png", PropertySupport.FRACTION, 0.9,
					PropertySupport.BACKGROUNDIMAGE, "images/cnfbg.png");

			_instance.addInitialViews();
			_instance.createMenus();
			_instance.placeViewsOnVirtualDesktop();
			_instance.createEventNumberLabel();

		}
		return _instance;
	}
	

	// create the event number label
	private void createEventNumberLabel() {
		_eventNumberLabel = new JLabel("  Event Num:      is GEMC: false");
		_eventNumberLabel.setOpaque(true);
		_eventNumberLabel.setBackground(Color.black);
		_eventNumberLabel.setForeground(Color.yellow);
		_eventNumberLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		_eventNumberLabel.setBorder(BorderFactory.createLineBorder(Color.cyan, 1));
		setEventNumberLabel(-1);

		getJMenuBar().add(Box.createHorizontalGlue());
		getJMenuBar().add(_eventNumberLabel);
		getJMenuBar().add(Box.createHorizontalStrut(5));
	}

	/**
	 * Set the event number label
	 * 
	 * @param num the event number
	 */
	public static void setEventNumberLabel(int num) {

		if (num < 0) {
			_eventNumberLabel.setText("  Event Num:      ");
		} else {
			_eventNumberLabel.setText("  Event Num: " + num);
		}
	}


	// create the options menu
	private void addToOptionMenu(JMenu omenu) {
		omenu.add(MagnifyWindow.magificationMenu());
		omenu.addSeparator();

		final JMenuItem memPlot = new JMenuItem("Memory Usage...");
		final JMenuItem environ = new JMenuItem("Environment...");
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				Object source = e.getSource();

				if (source == memPlot) {
					if (_memoryUsage == null) {
						_memoryUsage = new MemoryUsageDialog(_instance);
					}

					_memoryUsage.setVisible(true);
				}

				else if (source == environ) {
					if (_envDisplay == null) {
						_envDisplay = new TextDisplayDialog("Environment Information");
					}
					_envDisplay.setText(Environment.getInstance().toString());
					_envDisplay.setVisible(true);
				}

			}

		};
		environ.addActionListener(al);
		memPlot.addActionListener(al);
		omenu.add(environ);
		omenu.add(memPlot);

	}

	/**
	 * Add items to existing menus and/or create new menus NOTE: Swim menu is
	 * created by the SwimManager
	 */
	private void createMenus() {
		MenuManager mmgr = MenuManager.getInstance();

		_eventMenu = new EventMenu(true, false);
		mmgr.addMenu(_eventMenu);
		
		// the options menu
		addToOptionMenu(mmgr.getOptionMenu());
		
		// add to the file menu
		addToFileMenu();

		//add to the event menu
		addToEventMenu();

	}
	
	// add to the file menu
	private void addToFileMenu() {
		MenuManager mmgr = MenuManager.getInstance();
		JMenu fmenu = mmgr.getFileMenu();

		fmenu.insertSeparator(0);

		// restore default config
		final JMenuItem defConItem = new JMenuItem("Restore Default Configuration");

		ActionListener al1 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();

				if (source == defConItem) {
					restoreDefaultViewLocations();
					refresh();
				}
			}
		};

		defConItem.addActionListener(al1);
		fmenu.add(defConItem, 0);


		// some event file menus

		fmenu.insertSeparator(0);

		fmenu.add(EventMenu.getRecentEventFileMenu(), 0);
		fmenu.add(EventMenu.getOpenHipoEventFileItem(), 0);
	}
	



	/**
	 * Set the default directory in which to look for event files.
	 * 
	 * @param defaultDataDir default directory in which to look for event files
	 */
	public static void setDefaultDataDir(String defaultDataDir) {
		dataFilePath = defaultDataDir;
	}


	/**
	 * Refresh all views (with containers)
	 */
	public static void refresh() {
		ViewManager.getInstance().refreshAllViews();
	}


	// this is so we can find json files
	private static void initClas12Dir() throws IOException {

		// for running from runnable jar (for coatjava)
		String clas12dir = System.getProperty("CLAS12DIR");

		if (clas12dir == null) {
			clas12dir = "coatjava";
		}

		File clasDir = new File(clas12dir);

		if (clasDir.exists() && clasDir.isDirectory()) {
			System.err.println("**** Found CLAS12DIR [" + clasDir.getCanonicalPath() + "]");
			System.setProperty("CLAS12DIR", clas12dir);
			Log.getInstance().config("CLAS12DIR: " + clas12dir);
			return;
		} else {
			System.err.println("**** Did not find CLAS12DIR [" + clasDir.getCanonicalPath() + "]");
		}

		String cwd = Environment.getInstance().getCurrentWorkingDirectory();
		clas12dir = cwd + "/../../../coatjava";
		clasDir = new File(clas12dir);

		if (clasDir.exists() && clasDir.isDirectory()) {
			System.err.println("**** Found CLAS12DIR [" + clasDir.getCanonicalPath() + "]");
			System.setProperty("CLAS12DIR", clas12dir);
			Log.getInstance().config("CLAS12DIR: " + clas12dir);
			return;
		} else {
			System.err.println("**** Did not find CLAS12DIR [" + clasDir.getCanonicalPath() + "]");
		}

		throw (new IOException("Could not locate the coatjava directory."));
	}

	/**
	 * Fix the title of the main frame
	 */
	public void fixTitle() {
		String title = getTitle();

		// adjust title as needed
		setTitle(title);
	}

	/**
	 * Main program launches the cnf gui.
	 * <p>
	 * Command line arguments:</br>
	 * -p [dir] dir is the default directory
	 * 
	 * @param arg the command line arguments.
	 */
	public static void main(String[] arg) {
		// read in userprefs
		PropertiesManager.getInstance();

		// init the clas 12 dir wherev the json files are
		try {
			initClas12Dir();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		FileUtilities.setDefaultDir("data");

//process command args
		if ((arg != null) && (arg.length > 0)) {
			int len = arg.length;
			int lm1 = len - 1;
			boolean done = false;
			int i = 0;
			while (!done) {
				if (arg[i].equalsIgnoreCase("-p")) {
					if (i < lm1) {
						i++;
						FileUtilities.setDefaultDir(arg[i]);
					}
				}

				i++;
				done = (i >= len);
			} // !done
		} // end command arg processing
		
		// initialize data columns
		DataManager.getInstance();


		// now make the frame visible, in the AWT thread
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				getInstance();
				getInstance().setVisible(true);
				getInstance().fixTitle();
				
				System.out.println("cnf  " + _release + " is ready.");
			}

		});
		Log.getInstance().info(Environment.getInstance().toString());

		Log.getInstance().info("cnf is ready.");

	} // end main

}
