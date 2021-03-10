 package cnuphys.cnf.frame;

 /**
  * This this the frame (GUI window) for the Data Exporter Framework (def).
  * def is an application developed for the Center for Nuclear Femtogrophy funded 
  * short-term project: Visualizing Femto-Scale Dynamics (proposal CNF19-09). 
  */
 
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.application.BaseMDIApplication;
import cnuphys.bCNU.application.Desktop;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.menu.FileMenu;
import cnuphys.bCNU.menu.MenuManager;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.LogView;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.cnf.alldata.DataManager;
import cnuphys.cnf.alldata.graphics.DefinitionManager;
import cnuphys.cnf.event.EventManager;
import cnuphys.cnf.event.EventMenu;
import cnuphys.cnf.event.EventView;
import cnuphys.cnf.event.IEventListener;
import cnuphys.cnf.export.ExportManager;
import cnuphys.cnf.grid.GridManager;
import cnuphys.cnf.plot.DefGridView;
import cnuphys.cnf.plot.PlotManager;
import cnuphys.cnf.properties.PropertiesManager;
import cnuphys.cnf.stream.StreamManager;

@SuppressWarnings("serial")
public class Def extends BaseMDIApplication implements IEventListener {
	
	private static JFrame _frame;

	// singleton
	private static Def _instance;

	// release string
	protected static final String _release = "build 0.20";

	// used for one time inits
	private int _firstTime = 0;

	// for the event count
	private JMenuItem _eventCountLabel;

	// event remaining label 
	private JMenuItem _eventRemainingLabel;

	// the virtual view
	private VirtualView _virtualView;

	// the log view
	private LogView _logView;
	
	//the container gridview
	private DefGridView _gridView;

	// event menu
	private EventMenu _eventMenu;
	
	// event menu
	private FileMenu _fileMenu;
	
	//export menu
	private JMenu _exportMenu;

	//definition menu
	private JMenu _definitionMenu;
	
	// event number label 
	private static JLabel _eventNumberLabel;
	

	/** Last selected data file */
	private static String dataFilePath;
	
	//views
	private EventView _eventView;

	//rewind item
	private static JMenuItem _rewindItem;

	//stream events menu item
	private static JMenuItem _streamItem;
	
	/**
	 * Constructor (private--used to create singleton)
	 * 
	 * @param keyVals an optional variable length list of attributes in type-value
	 *                pairs. For example, PropertySupport.NAME, "my application",
	 *                PropertySupport.CENTER, true, etc.
	 */
	private Def(Object... keyVals) {
		super(keyVals);

		_frame = this;
		
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
		EventManager.getInstance().addEventListener(this, 2);
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

		_virtualView.moveTo(_logView, 3, VirtualView.CENTER);
		_virtualView.moveTo(_gridView, 1, VirtualView.CENTER);
		_virtualView.moveTo(_eventView, 0, VirtualView.CENTER);

		Log.getInstance().config("reset views on virtual dekstop");

	}

	/**
	 * Add the initial views to the desktop.
	 */
	private void addInitialViews() {

		// add a virtual view
		_virtualView = VirtualView.createVirtualView(4);
		ViewManager.getInstance().getViewMenu().addSeparator();
		
		// add event view
		_eventView = EventView.createEventView();
		
		//add the grid view
		_gridView = PlotManager.getInstance().getDefGridView();

		// add the log view
		_logView = new LogView(800, 750, true);
		
		// log some environment info
		Log.getInstance().config(Environment.getInstance().toString());

		// use config file info
		// Desktop.getInstance().configureViews();

		_virtualView.toFront();
	}


	// add to the event menu
	private void addToEventMenu() {
		createStreamMenuItem();
		_eventCountLabel = DefCommon.addEventCountToEventMenu(_eventMenu);
		_eventRemainingLabel = DefCommon.addEventRemainingToEventMenu(_eventMenu);
	}
	
	//create the menu item to stream to the end of the file
	private void createStreamMenuItem() {

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				if (source == _streamItem) {
					setBusy(true);
					
					Runnable runner = new Runnable() {

						@Override
						public void run() {
							EventManager.getInstance().streamToEndOfFile();
						}

					};				
					
					(new Thread(runner)).start();
					setBusy(false);
				}
				else if (source == _rewindItem) {
					EventManager.getInstance().rewindFile();
				}
			}

		};
		
		_eventMenu.insertSeparator(1);
		
		_rewindItem = new JMenuItem("Rewind to Start of File");
		_rewindItem.setEnabled(false);
		_rewindItem.addActionListener(al);
		_eventMenu.add(_rewindItem, 2);

		_streamItem = new JMenuItem("Stream to End of File");
		_streamItem.setEnabled(false);
		_streamItem.addActionListener(al);
		_eventMenu.add(_streamItem, 3);
		_eventMenu.insertSeparator(4);
	}

	private void setBusy(boolean busy) {
		_fileMenu.setEnabled(!busy);
		_eventMenu.setEnabled(!busy);
		_exportMenu.setEnabled(!busy);
		_definitionMenu.setEnabled(!busy);
	}

	
	/**
	 * a new event has arrived.
	 * 
	 * @param event the new event
	 * @param isStreaming <code>true</code> if this is during file streaming
	 */
	@Override
	public void newEvent(final DataEvent event, boolean isStreaming) {
		
		if (isStreaming) {
			int evNum = EventManager.getInstance().getEventNumber();
			if ((evNum % 1000) == 0) {
				setEventNumberLabel(evNum);
			}
		}
		else {
			fixState();
		}
		
		
	}

	/**
	 * Opened a new event file
	 * 
	 * @param file the new file
	 */
	@Override
	public void openedNewEventFile(File file) {
		fixState();
	}
	
	/**
	 * Rewound the current file
	 * @param file the file
	 */
	@Override
	public void rewoundFile(File file) {
		fixState();
	}
	
	/**
	 * Streaming start message
	 * @param file file being streamed
	 * @param numToStream number that will be streamed
	 */
	@Override
	public void streamingStarted(File file, int numToStream) {
	}
	
	/**
	 * Streaming ended message
	 * @param file the file that was streamed
	 * @param int the reason the streaming ended
	 */
	@Override
	public void streamingEnded(File file, int reason) {
		fixState();
	}
	
	//fix the state
	private void fixState() {		
		setEventNumberLabel(EventManager.getInstance().getEventNumber());

		//any events remaining
		int numRemaining = EventManager.getInstance().getNumRemainingEvents();
		
		//number of events
		int eventCount = EventManager.getInstance().getEventCount();
		
		
		//set selectability
		_streamItem.setEnabled(numRemaining > 0);
		
		_rewindItem.setEnabled(eventCount > 0);
		
		//fix labels
		DefCommon.fixEventMenuLabels(_eventCountLabel, _eventRemainingLabel);
	}
	
	/**
	 * Get the frame, which could be this or the DefApp frame
	 * @return
	 */
	public static JFrame getFrame() {
		return _frame;
	}
	
	protected static void setFrame(JFrame frame) {
		_frame = frame;
	}

	/**
	 * private access to the Def singleton.
	 * 
	 * @return the singleton Def (the main application frame.)
	 */
	public static Def getInstance() {
		if (_instance == null) {
			_instance = new Def(PropertySupport.TITLE, "def " + _release, PropertySupport.BACKGROUNDIMAGE,
					"images/cnu.png", PropertySupport.FRACTION, 0.85,
					PropertySupport.BACKGROUNDIMAGE, "images/cnfbg.png");

			_instance.addInitialViews();
			_instance.createMenus();
			_instance.placeViewsOnVirtualDesktop();
			_instance.createEventNumberLabel();
			
			//initialize
			StreamManager.getInstance();

		}
		return _instance;
	}
	

	// create the event number label
	private void createEventNumberLabel() {
		_eventNumberLabel = DefCommon.createEventNumberLabel(this);
	}

	
	/**
	 * Fix the title of the main frame
	 */
	public void fixTitle() {
		DefCommon.fixTitle(this);
	}

	public void setEventNumberLabel(int num) {
		DefCommon.setEventNumberLabel(_eventNumberLabel, num);
	}

	/**
	 * Add items to existing menus and/or create new menus NOTE: Swim menu is
	 * created by the SwimManager
	 */
	private void createMenus() {
		MenuManager mmgr = MenuManager.getInstance();

		_eventMenu = new EventMenu(false);
		mmgr.addMenu(_eventMenu);
		
		//no option menu
		mmgr.removeMenu(mmgr.getOptionMenu());
		
		// add to the file menu
		addToFileMenu();

		//add to the event menu
		addToEventMenu();
		
		//the definition menu
		_definitionMenu = DefinitionManager.getInstance().getMenu();
		getJMenuBar().add(_definitionMenu);
		
		//the grid menu
		getJMenuBar().add(GridManager.getInstance().getGridMenu());
		
	//	System.out.println("Menu font " + _definitionMenu.getFont());
		
		//the export menu with weird font hack
		_exportMenu = ExportManager.getExportMenu();
		_exportMenu.setFont(_definitionMenu.getFont());
		getJMenuBar().add(_exportMenu);


	}
	
	// add to the file menu
	private void addToFileMenu() {
		MenuManager mmgr = MenuManager.getInstance();
		_fileMenu = (FileMenu)(mmgr.getFileMenu());

		_fileMenu.insertSeparator(0);

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
		_fileMenu.add(defConItem, 0);


		// some event file menus

		_fileMenu.insertSeparator(0);

		_fileMenu.add(EventMenu.getRecentEventFileMenu(), 0);
		_fileMenu.add(EventMenu.getOpenHipoEventFileItem(), 0);
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



	/**
	 * Main program launches the def gui.
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
			DefCommon.initClas12Dir(false);
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
		
		// initialize managers
		DataManager.getInstance(); //data columns
		DefinitionManager.getInstance(); 
		ExportManager.getInstance(); //exporters
		

		// now make the frame visible, in the AWT thread
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				getInstance();
				getInstance().setVisible(true);
				
				System.out.println("def  " + _release + " is ready.");
			}

		});
		Log.getInstance().info(Environment.getInstance().toString());

		Log.getInstance().info("def is ready.");

	} // end main

}
