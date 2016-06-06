package cnuphys.ced.frame;

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
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import cnuphys.bCNU.application.BaseMDIApplication;
import cnuphys.bCNU.component.BusyPanel;
import cnuphys.bCNU.component.MagnifyWindow;
import cnuphys.ced.ced3d.CentralView3D;
import cnuphys.ced.ced3d.FTCalView3D;
import cnuphys.ced.ced3d.ForwardView3D;
import cnuphys.ced.cedview.alldc.AllDCView;
import cnuphys.ced.cedview.allec.ECView;
import cnuphys.ced.cedview.allpcal.PCALView;
import cnuphys.ced.cedview.bst.BSTxyView;
import cnuphys.ced.cedview.bst.BSTzView;
import cnuphys.ced.cedview.dcxy.DCXYView;
import cnuphys.ced.cedview.ft.FTCalXYView;
import cnuphys.ced.cedview.gemcview.GEMCView;
import cnuphys.ced.cedview.projecteddc.ProjectedDCView;
import cnuphys.ced.cedview.sectorview.DisplaySectors;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventMenu;
import cnuphys.ced.clasio.ClasIoEventView;
import cnuphys.ced.clasio.ClasIoMonteCarloView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.ClasIoReconEventView;
import cnuphys.ced.dcnoise.edit.NoiseParameterDialog;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.ColumnData;
import cnuphys.ced.event.data.DefinitionManager;
import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.fastmc.FastMCManager;
import cnuphys.ced.fastmc.FastMCMenu;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.ced.geometry.FTOFGeometry;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.GeometryReportView;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.magfield.SwimAllMC;
import cnuphys.ced.magfield.SwimAllRecon;
import cnuphys.ced.noise.NoiseManager;
import cnuphys.ced.plugin.CedPluginManager;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFields;
import cnuphys.splot.example.MemoryUsageDialog;
import cnuphys.splot.plot.PlotPanel;
import cnuphys.swim.SwimMenu;
import cnuphys.bCNU.eliza.ElizaDialog;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.splashscreen.SplashWindow;
import cnuphys.bCNU.log.ConsoleLogListener;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.bCNU.menu.MenuManager;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.HistoGridView;
import cnuphys.bCNU.view.IHistogramMaker;
import cnuphys.bCNU.view.LogView;
import cnuphys.bCNU.view.PlotView;
import cnuphys.bCNU.view.ViewManager;
//import cnuphys.bCNU.view.XMLView;
import cnuphys.bCNU.view.VirtualView;

@SuppressWarnings("serial")
public class Ced extends BaseMDIApplication implements PropertyChangeListener,
		MagneticFieldChangeListener {

	// the singleton
	private static Ced _instance;
	
	private static final String _release = "build 0.97.02";

	// used for one time inits
	private int _firstTime = 0;

	// using 3D?
	private static boolean _use3D = true;

	// if plugin only, do not create intial detector views
	private static boolean _pluginOnly;

	// plugin folder
	private static String _pluginFolder;

	// plugin manager
	private CedPluginManager _pluginManager;

	// event menu
	private ClasIoEventMenu _eventMenu;

	// busy panel shows working when reading file
	private static BusyPanel _busyPanel;

	// event number label on menu bar
	private static JLabel _eventNumberLabel;
		
	// memory usage dialog
	private MemoryUsageDialog _memoryUsage;
	
	// some views
	private AllDCView _allDCView;
	private VirtualView _virtualView;
	private ClasIoMonteCarloView _monteCarloView;
	private ClasIoReconEventView _reconEventView;
	private ClasIoEventView _eventView;
	private GEMCView _gemcView;
	private BSTxyView _bstXyView;
	private BSTzView _bstZView;
	private FTCalXYView _ftcalXyView;
	private DCXYView _dcXyView;
	private ProjectedDCView _projectedDCView;
	private ECView _ecView;
	private PCALView _pcalView;
	private LogView _logView;
	private PlotView _plotView;
	private ForwardView3D _forward3DView;
	private CentralView3D _central3DView;
	private FTCalView3D _ftCal3DView;
	private GeometryReportView _geoReportView;
	
	//sector views
	private SectorView _sectorView14;
	private SectorView _sectorView25;
	private SectorView _sectorView36;
	
	//histogram grids (which are also views)
	protected HistoGridView dcHistoGrid;
	protected HistoGridView ftofHistoGrid;
	protected HistoGridView bstHistoGrid;
	protected HistoGridView pcalHistoGrid;
	protected HistoGridView ecHistoGrid;
	
	// the about string
	private static String _aboutString = "<html><span style=\"font-size:8px\">ced: the cLAS eVENT dISPLAY<br><br>Developed by Christopher Newport University";

	/**
	 * Constructor (private--used to create singleton)
	 * 
	 * @param keyVals an optional variable length list of attributes in
	 *            type-value pairs. For example, PropertySupport.NAME,
	 *            "my application", PropertySupport.CENTER, true, etc.
	 */
	private Ced(Object... keyVals) {
		super(keyVals);

		//histogram filler
		new CedHistoFiller(this);

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
			// reaarange some views in virtual space
			_virtualView.reconfigure();
						
			_virtualView.moveTo(dcHistoGrid, 13);
			_virtualView.moveTo(ftofHistoGrid, 14);
			_virtualView.moveTo(bstHistoGrid, 15);
			_virtualView.moveTo(pcalHistoGrid, 16);
			_virtualView.moveTo(ecHistoGrid, 17);
			
	    	_virtualView.moveTo(_allDCView, 3);
			_virtualView.moveTo(_eventView, 6, VirtualView.BOTTOMRIGHT);
			_virtualView.moveTo(_gemcView, 6, VirtualView.BOTTOMLEFT);
			_virtualView.moveTo(_bstXyView, 2, VirtualView.BOTTOMLEFT);
			_virtualView.moveTo(_bstZView, 2, VirtualView.UPPERRIGHT);

			// note no constraint means "center"
			_virtualView.moveTo(_dcXyView, 7);
			_virtualView.moveTo(_projectedDCView, 8);

			_virtualView.moveTo(_pcalView, 4);
			_virtualView.moveTo(_ecView, 5);
			_virtualView.moveTo(_logView, 12, VirtualView.UPPERRIGHT);
			_virtualView.moveTo(_monteCarloView, 1, VirtualView.TOPCENTER);
			_virtualView.moveTo(_reconEventView, 1, VirtualView.BOTTOMCENTER);
			_virtualView.moveTo(_plotView, 12, VirtualView.BOTTOMLEFT);
			_virtualView.moveTo(_geoReportView, 18, VirtualView.CENTER);

			_virtualView.moveTo(_ftcalXyView, 10, VirtualView.BOTTOMLEFT);

			if (_use3D) {
				_virtualView.moveTo(_forward3DView, 9, VirtualView.CENTER);
				_virtualView.moveTo(_central3DView, 10, VirtualView.CENTER);
				_virtualView.moveTo(_ftCal3DView, 11, VirtualView.BOTTOMRIGHT);
			}
			Log.getInstance().config("reset views on virtual dekstop");
		}
		_firstTime++;
	}

	/**
	 * Add the initial views to the desktop.
	 */
	private void addInitialViews() {

		// make sure noise listener is instantiated
		NoiseManager.getInstance();

		// add an object that can respond to a "swim all MC" request.

		ClasIoEventManager.getInstance().setAllMCSwimmer(new SwimAllMC());
		ClasIoEventManager.getInstance().setAllReconSwimmer(new SwimAllRecon());

		// make sure accumulation manager is instantiated
		AccumulationManager.getInstance();

		// add a virtual view
		_virtualView = VirtualView.createVirtualView(19);
		ViewManager.getInstance().getViewMenu().addSeparator();

		// add GEMC data view
		_gemcView = new GEMCView();

		// add event view
		_eventView = ClasIoEventView.createEventView();

		// add monte carlo view
		_monteCarloView = new ClasIoMonteCarloView();

		// add a reconstructed tracks view
		_reconEventView = ClasIoReconEventView.getInstance();

		//geometry report view
		_geoReportView = new GeometryReportView();
		
		if (!pluginsOnly()) {
			ViewManager.getInstance().getViewMenu().addSeparator();

			// add an alldc view
			_allDCView = AllDCView.createAllDCView();

			// add a bstZView
			_bstZView = BSTzView.createBSTzView();

			// add a bstXYView
			_bstXyView = BSTxyView.createBSTxyView();

			// add a ftcalxyYView
			_ftcalXyView = FTCalXYView.createFTCalXYView();

			// add a DC XY View
			_dcXyView = DCXYView.createDCXYView();

			
			// projected dC view
			_projectedDCView = ProjectedDCView.createProjectedDCView();
			
			// add an ec view
			_ecView = ECView.createECView();

			// add an pcal view
			_pcalView = PCALView.createPCALView();

			// 3D view?
			if (_use3D) {
				_forward3DView = new ForwardView3D();
				_central3DView = new CentralView3D();
				_ftCal3DView = new FTCalView3D();
			}

			// add three sector views
			ViewManager.getInstance().getViewMenu().addSeparator();
			_sectorView36=SectorView.createSectorView(DisplaySectors.SECTORS36);
			_sectorView25=SectorView.createSectorView(DisplaySectors.SECTORS25);
			_sectorView14=SectorView.createSectorView(DisplaySectors.SECTORS14);
		}

		// add logview
		ViewManager.getInstance().getViewMenu().addSeparator();
		_logView = new LogView();

		// plot view
		_plotView = new PlotView();
		
        //add histograms
		addDcHistogram();
		addFtofHistogram();
		addBstHistogram();
		addPcalHistogram();
		addEcHistogram();
		
		

		// log some environment info
		Log.getInstance().config(Environment.getInstance().toString());

		// use config file info
		// Desktop.getInstance().configureViews();

		_virtualView.toFront();
	}

	//dc wire histogram
	private void addDcHistogram() {
		IHistogramMaker maker = new IHistogramMaker() {

			@Override
			public PlotPanel addHistogram(int row, int col, int w, int h) {
				
				PlotPanel panel;
				
				int lay = col;
				int sect = 1 + (row-1) / 6;
				int supl = 1 + (row-1) % 6;
				String title = "DC sect_" + sect + "  supl_" + supl + "  lay_" + lay;
				
				panel = HistoGridView.createHistogram(dcHistoGrid, w, h, title, "wire", "count", -0.5, 112, 112);

				return panel;
			}
			
		};
		dcHistoGrid = HistoGridView.createHistoGridView("DC Wire Histograms", 36, 6, 260, 240, 0.7, maker);
	}
	
	//ftof wire histogram
	private void addFtofHistogram() {
		IHistogramMaker maker = new IHistogramMaker() {

			@Override
			public PlotPanel addHistogram(int row, int col, int w, int h) {
				
				PlotPanel panel;

				int panelType = col-1; // 1A, 1B, 2 for 0,1,2
				int numPaddle = FTOFGeometry.numPaddles[panelType];
				int sect = 1 + (row - 1) % 6;
				String title = "FTOF sect_" + sect + "  " +
						FTOF.name(panelType);

				switch (panelType) {
				case 0: // 1A has 23 paddles
					panel = HistoGridView.createHistogram(ftofHistoGrid, w, h, title, "paddle", "count", -0.5, numPaddle-0.5, numPaddle);
					break;
				case 1: // 1B has 62 paddles
					panel = HistoGridView.createHistogram(ftofHistoGrid, w, h, title, "paddle", "count", -0.5, numPaddle-0.5, numPaddle);
					break;
				default: //2 has 5 paddles
					panel = HistoGridView.createHistogram(ftofHistoGrid, w, h, title, "paddle", "count", -0.5, numPaddle-0.5, numPaddle);
					break;
				}

				return panel;
			}
			
		};
		ftofHistoGrid = HistoGridView.createHistoGridView("FTOF Histograms", 6, 3, 260, 240, 0.7, maker);
	}
	
	//ftof wire histogram
	private void addBstHistogram() {
		IHistogramMaker maker = new IHistogramMaker() {

			@Override
			public PlotPanel addHistogram(int row, int col, int w, int h) {
				
				int layer = row;
				int sector = col;
				
				int supl0 = (layer-1) / 2;
				
				int maxSector = BSTGeometry.sectorsPerSuperlayer[supl0];
				if (sector > maxSector) {
					return null;
				}
				
				PlotPanel panel;
				String title = "BST layer_" + layer + " sector_" + sector;
				panel = HistoGridView.createHistogram(ftofHistoGrid, w, h, title, "strip", "count", -0.5, 256-0.5, 256);
				return panel;
			}
			
		};
		bstHistoGrid = HistoGridView.createHistoGridView("BST Histograms", 8, 24, 300, 240, 0.7, maker);
	}
	
	//pcal strip histogram
	private void addPcalHistogram() {
		IHistogramMaker maker = new IHistogramMaker() {

			@Override
			public PlotPanel addHistogram(int row, int col, int w, int h) {
				
				int sector = row;
				int plane = col-1; //u, v, w
				int numStrip = PCALGeometry.PCAL_NUMSTRIP[plane];
								
				PlotPanel panel;
				String title = "PCAL sector_" + sector + "_" + PCALGeometry.PLANE_NAMES[plane];
				panel = HistoGridView.createHistogram(pcalHistoGrid, w, h, title, "strip", "count", -0.5, numStrip-0.5, numStrip);
				return panel;
			}
			
		};

		pcalHistoGrid = HistoGridView.createHistoGridView("PCAL Histograms", 6, 3, 240, 240, 0.7, maker);
	}

	//ec strip histogram
	private void addEcHistogram() {
		IHistogramMaker maker = new IHistogramMaker() {

			@Override
			public PlotPanel addHistogram(int row, int col, int w, int h) {
				
				int sector = row;
				int stack = (col - 1) / 3; //inner outer
				int plane = (col - 1) % 3; //u, v, w
				int numStrip = 36;
								
				PlotPanel panel;
				String stackName = ECGeometry.STACK_NAMES[stack];
				String planeName = ECGeometry.PLANE_NAMES[plane];
				
				String title = "EC sector_" + sector + "_" + stackName +  "_" + planeName;
				panel = HistoGridView.createHistogram(ecHistoGrid, w, h, title, "strip", "count", -0.5, numStrip-0.5, numStrip);
				return panel;
			}
			
		};
		ecHistoGrid = HistoGridView.createHistoGridView("EC Histograms", 6, 6, 240, 240, 0.7, maker);
	}
	


	/**
	 * Add items to existing menus and/or create new menus NOTE: Swim menu is
	 * created by the SwimManager
	 */
	private void createMenus() {
		MenuManager mmgr = MenuManager.getInstance();

		_eventMenu = new ClasIoEventMenu(true, false);
		mmgr.addMenu(_eventMenu);

		// the options menu
		addToOptionMenu(mmgr.getOptionMenu());

		// ET menu
		// mmgr.addMenu(ETSupport.getETMenu());

		// create the mag field menu
		MagneticFields.setActiveField(MagneticFields.FieldType.TORUS);
		mmgr.addMenu(MagneticFields.getMagneticFieldMenu());

		// the swimmer menu
		mmgr.addMenu(SwimMenu.getInstance());
		SwimMenu.getInstance().addPropertyChangeListener(this);

		// remove the option menu until I need it
		// mmgr.removeMenu(mmgr.getOptionMenu());

		// add to the file menu
		addToFileMenu();

		// add to the event menu
		addToEventMenu();
		
		//define menu
		mmgr.addMenu(DefinitionManager.getInstance().getMenu());
		
		//FastMC
		mmgr.addMenu(new FastMCMenu());
	}
	
	//add to the file menu
	private void addToFileMenu() {
		MenuManager mmgr = MenuManager.getInstance();
		JMenu fmenu = mmgr.getFileMenu();
		
		fmenu.insertSeparator(0);

		// eliza!
		JMenuItem elizaItem = new JMenuItem("Eliza...");
		ActionListener al1 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ElizaDialog.showEliza(_instance);
			}
		};
		elizaItem.addActionListener(al1);
		fmenu.add(elizaItem, 0);

		JMenuItem aboutItem = new JMenuItem("About ced...");
		ActionListener al0 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(Ced.getInstance(), _aboutString,
						"About ced", JOptionPane.INFORMATION_MESSAGE,
						ImageManager.cnuIcon);

			}
		};
		aboutItem.addActionListener(al0);
		fmenu.add(aboutItem, 0);
		
		//some evio file menus
		
		fmenu.insertSeparator(0);

		fmenu.add(ClasIoEventMenu.getRecentEvioFileMenu(), 0);
		fmenu.add(ClasIoEventMenu.getOpenEventFileItem(), 0);

		
	}

	// create the options menu
	private void addToOptionMenu(JMenu omenu) {
		omenu.add(MagnifyWindow.magificationMenu());
		omenu.addSeparator();
		
		final JMenuItem memPlot = new JMenuItem("Memory Usage");
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (_memoryUsage == null) {
					_memoryUsage = new MemoryUsageDialog(Ced.getFrame());
				}

				_memoryUsage.setVisible(true);
			}
			
		};
		memPlot.addActionListener(al);
		omenu.add(memPlot);
	}

	/**
	 * Refresh all views (with containers)
	 */
	public static void refresh() {
		ViewManager.getInstance().refreshAllContainerViews();
	}

	/**
	 * Set the event number label
	 * 
	 * @param num the event number
	 */
	public static void setEventNumberLabel(int num) {
		
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}
		if (num < 0) {
			_eventNumberLabel.setText("  Event Num:      ");
		}
		else {
			_eventNumberLabel.setText("  Event Num: " + num);
		}
	}

	// add to the event menu
	private void addToEventMenu() {

		ClasIoEventManager.getInstance().createSourceItems(_eventMenu);

		// add the noise parameter menu item
		ActionListener al2 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NoiseParameterDialog dialog = new NoiseParameterDialog();
				dialog.setVisible(true);
			}
		};
		
		_eventMenu.addSeparator();
		MenuManager.addMenuItem("Noise Algorithm Parameters...", _eventMenu,
				al2);
	}

	/**
	 * private access to the Ced singleton.
	 * 
	 * @return the singleton Ced (the main application frame.)
	 */
	private static Ced getInstance() {
		if (_instance == null) {
			_instance = new Ced(PropertySupport.TITLE, "ced " + versionString(),
					PropertySupport.BACKGROUNDIMAGE, "images/cnu.png",
					PropertySupport.FRACTION, 0.85);

			_instance.addInitialViews();
			_instance.createMenus();
			_instance.placeViewsOnVirtualDesktop();

			_instance.createBusyPanel();
			_instance.createEventNumberLabel();
			MagneticFields.addMagneticFieldChangeListener(_instance);

		}
		return _instance;
	}
	
	/**
	 * public access to the singleton
	 * @return the singleton Ced (the main application frame.)
	 */
	public static Ced getCed() {
		return _instance;
	}

	/**
	 * Generate the version string
	 * 
	 * @return the version string
	 */
	public static String versionString() {
		return _release;
	}

	@Override
	public void propertyChange(PropertyChangeEvent pev) {
		String pname = pev.getPropertyName();

		if (pname.equals(SwimMenu.TRAJ_CLEARED_MC_PROP)) {
		}
		else if (pname.equals(SwimMenu.TRAJ_CLEARED_RECON_PROP)) {
		}
		else if (pname.equals(SwimMenu.SWIM_ALL_MC_PROP)) {
			ISwimAll allSwimmer = ClasIoEventManager.getInstance()
					.getMCSwimmer();
			if (allSwimmer != null) {
				allSwimmer.swimAll();
			}
		}
		else if (pname.equals(SwimMenu.SWIM_ALL_RECON_PROP)) {
			ISwimAll allSwimmer = ClasIoEventManager.getInstance()
					.getReconSwimmer();
			if (allSwimmer != null) {
				allSwimmer.swimAll();
			}
		}

	}

	private void createBusyPanel() {
		getJMenuBar().add(Box.createHorizontalStrut(20));
		_busyPanel = new BusyPanel();
		_busyPanel.setVisible(false);
		getJMenuBar().add(_busyPanel);
	}

	// create the event number label
	private void createEventNumberLabel() {
		_eventNumberLabel = new JLabel("  Event Num:      is GEMC: false");
		_eventNumberLabel.setOpaque(true);
		_eventNumberLabel.setBackground(Color.black);
		_eventNumberLabel.setForeground(Color.yellow);
		_eventNumberLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		_eventNumberLabel
				.setBorder(BorderFactory.createLineBorder(Color.cyan, 1));
		setEventNumberLabel(-1);

		getJMenuBar().add(Box.createHorizontalGlue());
		getJMenuBar().add(_eventNumberLabel);
		getJMenuBar().add(Box.createHorizontalStrut(5));
	}

	/**
	 * Fix the title of te main frame
	 */
	public void fixTitle() {
		String title = getTitle();
		int index = title.indexOf("   [Mag");
		if (index > 0) {
			title = title.substring(0, index);
		}

		title += "   [Magnetic Field: "
				+ MagneticFields.getActiveFieldDescription();
		title += "]";
		
		EventSourceType estype = ClasIoEventManager.getEventSourceType();
		switch (estype) {
		case FILE:
			File file = ClasIoEventManager.getInstance().getCurrentEventFile();
			if (file != null) {
				title += "   [Evio: " + file.getName() + "]";
			}
			break;
		case ET:
			title += "   [ET]";
			break;
		case FASTMC:
			file = FastMCManager.getInstance().getCurrentFile();
			if (file != null) {
				title += "   [FastMC: " + file.getName() + "]";
			}
			break;
		}
		
		//current event file?
		
		setTitle(title);
	}

	@Override
	public void magneticFieldChanged() {
		fixTitle();
	}
	
	/**
	 * Get the shared busy panel
	 * 
	 * @return the shared progress bar
	 */
	public static BusyPanel getBusyPanel() {
		return _busyPanel;
	}


	/**
	 * Get the GEMC view
	 * 
	 * @return the GEMC view
	 */
	public GEMCView getGEMCView() {
		return _gemcView;
	}

	/**
	 * Check whether we use 3D
	 * 
	 * @return <code>true</code> if we use 3D
	 */
	public static boolean use3D() {
		return _use3D;
	}

	/**
	 * Check whether we use Plugins onlyD
	 * 
	 * @return <code>true</code> if we use plugins only
	 */
	public static boolean pluginsOnly() {
		return _pluginOnly;
	}

	/**
	 * Get the parent frame
	 * 
	 * @return the parent frame
	 */
	public static JFrame getFrame() {
		return _instance;
	}
	
	/**
	 * Main program launches the ced gui.
	 * <p>
	 * Command line arguments:</br>
	 * -p [dir] dir is the default directory
	 * 
	 * @param arg the command line arguments.
	 */
	public static void main(String[] arg) {
		
		//for running from runnable jar (for coatjava)
		String clas12dir = System.getProperty("CLAS12DIR");
		
		if (clas12dir == null) {
			clas12dir = "coatjava";
			System.setProperty("CLAS12DIR", clas12dir);
		}

		FileUtilities.setDefaultDir("data");

		// create a console log listener
		Log.getInstance().addLogListener(new ConsoleLogListener());
		
		// default plugin folder
		_pluginFolder = Environment.getInstance().getHomeDirectory()
				+ File.separator + "cedplugins";

		
		//splash frame
		final SplashWindow splashWindow = new SplashWindow("ced", null, 800, "images/cnu.png", _release);
		// now make the frame visible, in the AWT thread
		try {
			EventQueue.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					splashWindow.setVisible(true);
				}

			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

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
				else if (arg[i].equalsIgnoreCase("-torus")) {
					i++;
					MagneticFields.setTorusFullPath(arg[i]);
					Log.getInstance().config("Torus Path: " + arg[i]);
					System.out.println("Torus Path: " + arg[i]);
				}
				else if (arg[i].equalsIgnoreCase("-solenoid")) {
					i++;
					MagneticFields.setSolenoidFullPath(arg[i]);
					Log.getInstance().config("Solenoid Path: " + arg[i]);
					System.out.println("Solenoid Path: " + arg[i]);
				}
				else if (arg[i].contains("NO3D")) {
					_use3D = false;
					System.err.println("Not using 3D");
				}
				else if (arg[i].contains("PLUGINONLY")) {
					_pluginOnly = true;
					System.err.println("Using Plugins Only");
				}
				else if (arg[i].equalsIgnoreCase("-plugindir")) {
					i++;
					_pluginFolder = arg[i];
					Log.getInstance().config("Plugin directory: " + arg[i]);
					System.out.println("Plugin directory: " + arg[i]);
				}

				i++;
				done = (i >= len);
			} // !done
		} // end command arg processing
		
		//initialize magnetic fields
		MagneticFields.initializeMagneticFields();


		// initialize geometry
		GeometryManager.getInstance();

		// now make the frame visible, in the AWT thread
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				Ced ced = getInstance();
				splashWindow.setVisible(false);
				ced.setVisible(true);
				splashWindow.writeCachedText();
				ced.fixTitle();
				//initialize data columns
				ColumnData.intitialize();
				// get plugin manager
				ced._pluginManager = new CedPluginManager(_pluginFolder);
			}

		});
		Log.getInstance().info(Environment.getInstance().toString());
		Log.getInstance().config("CLAS12DIR: " + clas12dir);
		Log.getInstance().info("ced is ready.");

		// test demo plugin
		// new CedDemoPlugin();
		//new BrookDemoPlugin();

		// //test event queue
		// ClasIoEventQueue queue = new ClasIoEventQueue();
		// new EventProducer(queue);
		// IEventProcessor processor = new IEventProcessor() {
		//
		// @Override
		// public void processEvent(EvioDataEvent event) {
		// System.err.println("GOT EVENT TO PROCESS");
		// }
		//
		// };
		//
		// new EventConsumer(queue, processor);

	} // end main

}
