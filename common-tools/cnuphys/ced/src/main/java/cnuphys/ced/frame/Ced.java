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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import cnuphys.bCNU.application.BaseMDIApplication;
import cnuphys.bCNU.application.Desktop;
import cnuphys.bCNU.component.MagnifyWindow;
import cnuphys.bCNU.dialog.TextDisplayDialog;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.alldata.graphics.DefinitionManager;
import cnuphys.ced.ced3d.view.CentralView3D;
import cnuphys.ced.ced3d.view.FTCalView3D;
import cnuphys.ced.ced3d.view.ForwardView3D;
import cnuphys.ced.cedview.alldc.AllDCView;
import cnuphys.ced.cedview.allec.ECView;
import cnuphys.ced.cedview.allpcal.PCALView;
import cnuphys.ced.cedview.alltof.TOFView;
import cnuphys.ced.cedview.central.CentralXYView;
import cnuphys.ced.cedview.central.CentralZView;
import cnuphys.ced.cedview.dcxy.DCXYView;
import cnuphys.ced.cedview.ft.FTCalXYView;
import cnuphys.ced.cedview.projecteddc.ProjectedDCView;
import cnuphys.ced.cedview.sectorview.DisplaySectors;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventMenu;
import cnuphys.ced.clasio.ClasIoEventView;
import cnuphys.ced.clasio.ClasIoMonteCarloView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoReconEventView;
import cnuphys.ced.dcnoise.edit.NoiseParameterDialog;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.AllEC;
import cnuphys.ced.event.data.BMT;
import cnuphys.ced.event.data.BMTCrosses;
import cnuphys.ced.event.data.CTOF;
import cnuphys.ced.event.data.Cosmics;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.FMTCrosses;
import cnuphys.ced.event.data.FTCAL;
import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.event.data.HBCrosses;
import cnuphys.ced.event.data.HBSegments;
import cnuphys.ced.event.data.HTCC2;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.event.data.BSTCrosses;
import cnuphys.ced.event.data.TBCrosses;
import cnuphys.ced.event.data.TBSegments;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.ced.geometry.FTOFGeometry;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.magfield.PlotFieldDialog;
import cnuphys.ced.magfield.SwimAllMC;
import cnuphys.ced.magfield.SwimAllRecon;
import cnuphys.ced.noise.NoiseManager;
import cnuphys.ced.properties.PropertiesManager;
import cnuphys.ced.trigger.TriggerDialog;
import cnuphys.ced.trigger.TriggerManager;
import cnuphys.ced.trigger.TriggerMenuPanel;
import cnuphys.lund.X11Colors;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFields;
import cnuphys.splot.example.MemoryUsageDialog;
import cnuphys.splot.plot.PlotPanel;
import cnuphys.swim.SwimMenu;
import cnuphys.swim.Swimmer;
import cnuphys.bCNU.eliza.ElizaDialog;
import cnuphys.bCNU.fortune.FortuneManager;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.bCNU.menu.MenuManager;
import cnuphys.bCNU.simanneal.example.ising2D.Ising2DDialog;
import cnuphys.bCNU.simanneal.example.ts.TSDialog;
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
	
	private static final String _release = "build 1.005a";

	// used for one time inits
	private int _firstTime = 0;
	
	//for the event count
	private JMenuItem _eventCountLabel;

	// using 3D?
	private static boolean _use3D = true;
	
	// event menu
	private ClasIoEventMenu _eventMenu;
	
	//weird menu
	private JMenu _weirdMenu;
	
	//warning label that filtering is active
	private JLabel _filterLabel;
	
	// busy panel shows working when reading file
//	private static BusyPanel _busyPanel;

	// event number label on menu bar
	private static JLabel _eventNumberLabel;
		
	// memory usage dialog
	private MemoryUsageDialog _memoryUsage;
	
	//Environment display
	private TextDisplayDialog _envDisplay;
	
	//show which filters are active
	private JMenu _eventFilterMenu;

	//for plotting the field
	private  PlotFieldDialog _plotFieldDialog;
	
	// some views
	private AllDCView _allDCView;
	private VirtualView _virtualView;
	private ClasIoMonteCarloView _monteCarloView;
	private ClasIoReconEventView _reconEventView;
	private ClasIoEventView _eventView;
	private CentralXYView _centralXYView;
	private CentralZView _centralZView;
	private FTCalXYView _ftcalXyView;
	private DCXYView _dcXyView;
	private ProjectedDCView _projectedDCView;
	private ECView _ecView;
	private PCALView _pcalView;
	private LogView _logView;
	private ForwardView3D _forward3DView;
	private CentralView3D _central3DView;
	private FTCalView3D _ftCal3DView;
	private TOFView _tofView;
	
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
	
	//plot view
	private PlotView _plotView;

	
	// the about string
	private static String _aboutString = "<html><span style=\"font-size:12px\">ced: the cLAS eVENT dISPLAY&nbsp;&nbsp;&nbsp;&nbsp;" + _release + 
	"<br><br>Developed by Christopher Newport University" + 
	"<br><br>Download the latest version at <a href=\"https://userweb.jlab.org/~heddle/ced/builds/\">https://userweb.jlab.org/~heddle/ced/builds/</a>" +
	"<br><br>Email bug reports to david.heddle@cnu.edu";
	
	//"play" dc occupancy?
	private JCheckBoxMenuItem _playDCOccupancy;
	
	//use old BST geometry
	private JCheckBoxMenuItem _oldBSTGeometry;
	
	//for the traveling salesperson dialog
	private TSDialog _tsDialog;

	//for the ising model 2D dialog
	private Ising2DDialog _i2dDialog;

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
			// rearrange some views in virtual space
			_virtualView.reconfigure();
			restoreDefaultViewLocations();
			
			//now load configuration
			Desktop.getInstance().loadConfigurationFile();
			Desktop.getInstance().configureViews();
		}
		_firstTime++;
	}
	
	/**
	 * Restore the default locations of the default views.
	 * Cloned views are unaffected.
	 */
	private void restoreDefaultViewLocations() {
		
		_virtualView.moveToStart(_sectorView14, 0, VirtualView.UPPERLEFT);
		_virtualView.moveToStart(_sectorView25, 0, VirtualView.UPPERLEFT);
		_virtualView.moveToStart(_sectorView36, 0, VirtualView.UPPERLEFT);
		
		_virtualView.moveTo(_plotView, 0, VirtualView.CENTER);
		
		_virtualView.moveTo(dcHistoGrid, 13);
		_virtualView.moveTo(ftofHistoGrid, 14);
		_virtualView.moveTo(bstHistoGrid, 15);
		_virtualView.moveTo(pcalHistoGrid, 16);
		_virtualView.moveTo(ecHistoGrid, 17);
		
    	_virtualView.moveTo(_allDCView, 3);
		_virtualView.moveTo(_eventView, 6, VirtualView.CENTER);
		_virtualView.moveTo(_centralXYView, 2, VirtualView.BOTTOMLEFT);
		_virtualView.moveTo(_centralZView, 2, VirtualView.UPPERRIGHT);

		// note no constraint means "center"
		_virtualView.moveTo(_dcXyView, 7);
		_virtualView.moveTo(_projectedDCView, 8);

		_virtualView.moveTo(_pcalView, 4);
		_virtualView.moveTo(_ecView, 5);
		_virtualView.moveTo(_logView, 12, VirtualView.UPPERRIGHT);
		_virtualView.moveTo(_monteCarloView, 1, VirtualView.TOPCENTER);
		_virtualView.moveTo(_reconEventView, 1, VirtualView.BOTTOMCENTER);

		_virtualView.moveTo(_tofView, 11, VirtualView.CENTER);

		_virtualView.moveTo(_ftcalXyView, 12, VirtualView.CENTER);

		if (_use3D) {
			_virtualView.moveTo(_forward3DView, 9, VirtualView.CENTER);
			_virtualView.moveTo(_central3DView, 10, VirtualView.BOTTOMLEFT);
			_virtualView.moveTo(_ftCal3DView, 10, VirtualView.BOTTOMRIGHT);
		}
		
		Log.getInstance().config("reset views on virtual dekstop");
		
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

		// add event view
		_eventView = ClasIoEventView.createEventView();
		
		// add three sector views
		ViewManager.getInstance().getViewMenu().addSeparator();
		_sectorView36=SectorView.createSectorView(DisplaySectors.SECTORS36);
		_sectorView25=SectorView.createSectorView(DisplaySectors.SECTORS25);
		_sectorView14=SectorView.createSectorView(DisplaySectors.SECTORS14);
		ViewManager.getInstance().getViewMenu().addSeparator();

		// add monte carlo view
		_monteCarloView = new ClasIoMonteCarloView();
		

		// add a reconstructed tracks view
		_reconEventView = ClasIoReconEventView.getInstance();

		ViewManager.getInstance().getViewMenu().addSeparator();

		// add an alldc view
		_allDCView = AllDCView.createAllDCView();

		_tofView = TOFView.createTOFView();

		// add a bstZView
		_centralZView = CentralZView.createCentralZView();

		// add a bstXYView
		_centralXYView = CentralXYView.createCentralXYView();

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
			ViewManager.getInstance().getViewMenu().addSeparator();
			_forward3DView = new ForwardView3D();
			_central3DView = new CentralView3D();
			_ftCal3DView = new FTCalView3D();
		}

		// add logview
		ViewManager.getInstance().getViewMenu().addSeparator();
		//plot view
		_plotView = new PlotView();

		_logView = new LogView();

		
        //add histograms
		addDcHistogram();
		addFtofHistogram();
		addBstHistogram();
		addPcalHistogram();
		addEcHistogram();
		
		//the trigger bit "view"
		ActionListener al3 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TriggerDialog.showDialog();
			}
		};
		
		JMenuItem menuItem = new JMenuItem("Trigger Bits");
		menuItem.addActionListener(al3);
		ViewManager.getInstance().getViewMenu().add(menuItem, 1);


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
	
	//ftof paddle histogram
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
	
	/**
	 * Accessor for the event menu
	 * @return the event menu
	 */
	public ClasIoEventMenu getEventMenu() {
		return _eventMenu;
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
		MagneticFields.getInstance().setActiveField(MagneticFields.FieldType.TORUS);
		addToMagneticFieldMenu();
		

		// the swimmer menu
		mmgr.addMenu(SwimMenu.getInstance());
		SwimMenu.getInstance().addPropertyChangeListener(this);

		// remove the option menu until I need it
		// mmgr.removeMenu(mmgr.getOptionMenu());
		
		//add to swim menu
        addToSwimMenu();
		
		// add to the file menu
		addToFileMenu();

		// add to the event menu
		addToEventMenu();
				
	}
	
	// add items to the basic mag field menu
	private void addToMagneticFieldMenu() {
		JMenu magMenu = MagneticFields.getInstance().getMagneticFieldMenu();
		final JMenuItem plotItem = new JMenuItem("Plot the Field...");
//		final JMenuItem reconfigItem = new JMenuItem("Remove Solenoid and Torus Overlap");
//		final JMenuItem samenessItem = new JMenuItem("Sameness Test with/without Overlap Removal");
		magMenu.addSeparator();

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (e.getSource() == plotItem) {
					if (_plotFieldDialog == null) {
						_plotFieldDialog = new PlotFieldDialog(getCed(), false);
					}

					_plotFieldDialog.setVisible(true);

				} 
//				else if (e.getSource() == reconfigItem) {
//					MagneticFields.getInstance().removeMapOverlap();
//				}
//				else if (e.getSource() == samenessItem) {
//					MagTests.samenessTest();
//				}
			}
		};

//		reconfigItem.addActionListener(al);
//		samenessItem.addActionListener(al);
		plotItem.addActionListener(al);
//		magMenu.add(reconfigItem);
//		magMenu.add(samenessItem);
		magMenu.add(plotItem);

		MenuManager.getInstance().addMenu(magMenu);
	}

	// add some fun stuff
	
	private void addWeirdMenu(JMenu menu) {
		String weirdTitle = "w" + "\u018e" + "i" + "\u1d19" + "d";
		_weirdMenu = new JMenu(weirdTitle);

		// eliza!
		final JMenuItem elizaItem = new JMenuItem("Eliza...");
		final JMenuItem fortuneItem = new JMenuItem("Fortune...");
		final JMenuItem tsItem = new JMenuItem("Traveling Salesperson ...");
		final JMenuItem i2dItem = new JMenuItem("2D Ising Model ...");

		ActionListener al1 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();

				if (source == elizaItem) {
					ElizaDialog.showEliza(_instance);
				}
				else if (source == fortuneItem) {
					FortuneManager.getInstance().showDialog();
				}
				else if (source == tsItem) {
					if (_tsDialog == null) {
						_tsDialog = new TSDialog();
					}
					_tsDialog.setVisible(true);
				}
				else if (source == i2dItem) {
					if (_i2dDialog == null) {
						_i2dDialog = new Ising2DDialog();
					}
					_i2dDialog.setVisible(true);
				}
			}
		};

		elizaItem.addActionListener(al1);
		fortuneItem.addActionListener(al1);
		tsItem.addActionListener(al1);
		i2dItem.addActionListener(al1);
		_weirdMenu.add(elizaItem);
		_weirdMenu.add(fortuneItem);
		_weirdMenu.add(tsItem);
		_weirdMenu.add(i2dItem);
		
		menu.add(_weirdMenu, 0);

	}
	
	//add to the file menu
	private void addToSwimMenu() {
	}
	
	
	//private void run some swim test
	
	//add to the file menu
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
//		fmenu.add(defConItem, 6);
		fmenu.add(defConItem, 0);
		
		addWeirdMenu(fmenu);



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
		
		//some event file menus
		
		fmenu.insertSeparator(0);
		fmenu.add(ClasIoEventMenu.getConnectETItem(), 0);
//		fmenu.add(ClasIoEventMenu.getConnectAnyRingItem(), 0);
		fmenu.insertSeparator(0);

		fmenu.add(ClasIoEventMenu.getRecentEventFileMenu(), 0);
		fmenu.add(ClasIoEventMenu.getOpenEventFileItem(), 0);
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
						_memoryUsage = new MemoryUsageDialog(Ced.getFrame());
					}

					_memoryUsage.setVisible(true);
				}
				
				else if (source == environ) {
					if (_envDisplay == null)  {
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
		
		//define menu
		omenu.addSeparator();
		omenu.add(DefinitionManager.getInstance().getMenu());

		
		
//		omenu.addSeparator();
//		
//		ActionListener al2 = new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				refresh();
//			}			
//		};
//		
//		_oldBSTGeometry = new JCheckBoxMenuItem("Use old (four-region) BST"
//				+ " Geometry", false);
//		_oldBSTGeometry.addActionListener(al2);
//		omenu.add(_oldBSTGeometry);
	}

	/**
	 * Refresh all views (with containers)
	 */
	public static void refresh() {
		ViewManager.getInstance().refreshAllViews();
	}
	
	/**
	 * Change the label to reflect whether or not we are filtering events
	 * @param filtering if <code>true</code> we are filtering
	 */
	public void setEventFilteringLabel(boolean filtering) {
		_filterLabel.setVisible(filtering);
	}
	
	/**
	 * Change the label to reflect whether or not we are filtering events
	 * @param filtering if <code>true</code> we are filtering
	 */
	public void fixEventFilteringLabel() {
		_filterLabel.setVisible(ClasIoEventManager.getInstance().isFilteringOn());
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

	/**
	 * Fix the event count label
	 */
	public void fixEventCount() {
		int count = ClasIoEventManager.getInstance().getEventCount();
		if (count < Integer.MAX_VALUE) {
			_eventCountLabel.setText("Event Count: " + count);
		}
		else {
			_eventCountLabel.setText("Event Count: N/A");
		}
	}
	
	/**
	 * Get the event filter menu
	 * @return the event filter menu
	 */
	public JMenu getEventFilterMenu() {
		return _eventFilterMenu;
	}
	
	// add to the event menu
	private void addToEventMenu() {
		
		_eventCountLabel = new JMenuItem("Event Count: N/A");
		_eventCountLabel.setOpaque(true);
		_eventCountLabel.setBackground(Color.white);
		_eventCountLabel.setForeground(X11Colors.getX11Color("Dark Blue"));
		_eventMenu.add(_eventCountLabel);
		
		// add the event filter menu
		_eventFilterMenu = new JMenu("Event Filters");
		_eventMenu.add(_eventFilterMenu);
		
		
		

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
		
		
		_eventMenu.addSeparator();

		_playDCOccupancy = new JCheckBoxMenuItem("\"Play\" Drift Chamber Occupancy", false);
		_eventMenu.add(_playDCOccupancy);
	}
	
	/**
	 * Flag controlling whether a tone indicating the DC occupancy is played
	 * @return <code>true</code> if the tone should be played
	 */
	public boolean playDCOccupancy() {
		if (_playDCOccupancy != null) {
			return _playDCOccupancy.getState();
		}
		return false;
	}
	
	/**
	 * Flag controlling whether we use the old BST geometry
	 * @return <code>true</code> if the tone should be played
	 */
	public boolean useOldBSTGeometry() {
		if (_oldBSTGeometry != null) {
			return _oldBSTGeometry.getState();
		}
		return false;
	}
	
	/**
	 * Get the virtual view
	 * @return the virtual view
	 */
	public VirtualView getVirtualView() {
		return _virtualView;
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
					PropertySupport.FRACTION, 0.9);

			_instance.addInitialViews();
			_instance.createMenus();
			_instance.placeViewsOnVirtualDesktop();

//			_instance.createBusyPanel();
			_instance.createFilterLabel();

			_instance.createTriggerPanel();

			_instance.createEventNumberLabel();
			MagneticFields.getInstance().addMagneticFieldChangeListener(_instance);

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

		if (pname.equals(ClasIoEventManager.SWIM_ALL_MC_PROP)) {
			ISwimAll allSwimmer = ClasIoEventManager.getInstance()
					.getMCSwimmer();
			if (allSwimmer != null) {
				allSwimmer.swimAll();
			}
		}
		else if (pname.equals(ClasIoEventManager.SWIM_ALL_RECON_PROP)) {
			ISwimAll allSwimmer = ClasIoEventManager.getInstance()
					.getReconSwimmer();
			if (allSwimmer != null) {
				allSwimmer.swimAll();
			}
		}

	}
	
	private void createTriggerPanel() {
		getJMenuBar().add(Box.createHorizontalStrut(20));
		getJMenuBar().add(Box.createHorizontalGlue());
		getJMenuBar().add(new TriggerMenuPanel());
	}

//	private void createBusyPanel() {
//		getJMenuBar().add(Box.createHorizontalStrut(15));
//		_busyPanel = new BusyPanel();
//		_busyPanel.setVisible(false);
//		getJMenuBar().add(_busyPanel);
//	}
	
	// create the event number label
	private void createFilterLabel() {
		_filterLabel = new JLabel(" Event Filtering On ");
		_filterLabel.setOpaque(true);
		_filterLabel.setBackground(Color.white);
		_filterLabel.setForeground(Color.red);
		_filterLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		_filterLabel
				.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		
		getJMenuBar().add(Box.createHorizontalGlue());
		getJMenuBar().add(_filterLabel);
		getJMenuBar().add(Box.createHorizontalStrut(5));
		
		setEventFilteringLabel(false);
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
	 * Get the plot view
	 * @return the plot voew;
	 */
	public PlotView getPlotView() {
		return _plotView;
	}

	/**
	 * Fix the title of the main frame
	 */
	public void fixTitle() {
		String title = getTitle();
		int index = title.indexOf("   [Mag");
		if (index > 0) {
			title = title.substring(0, index);
		}

		title += "   [Magnetic Field (" +
		MagneticFields.getInstance().getVersion() + ") "
				+ MagneticFields.getInstance().getActiveFieldDescription();
		
		if (MagneticFields.getInstance().hasActiveTorus()) {
			String path = MagneticFields.getInstance().getTorusBaseName();
			title  += " (" + path + ")";
		}
		
		title += "] [Swimmer (" + Swimmer.getVersion() + ")]";
		
		title += ("  " + ClasIoEventManager.getInstance().getCurrentSourceDescription());
		setTitle(title);
	}

	@Override
	public void magneticFieldChanged() {
//		Swimming.clearAllTrajectories();
		fixTitle();
		ClasIoEventManager.getInstance().reloadCurrentEvent();
	}
	
	/**
	 * Get the shared busy panel
	 * 
	 * @return the shared progress bar
	 */
//	public static BusyPanel getBusyPanel() {
//		return _busyPanel;
//	}

	/**
	 * Check whether we use 3D
	 * 
	 * @return <code>true</code> if we use 3D
	 */
	public static boolean use3D() {
		return _use3D;
	}
	

	/**
	 * Get the parent frame
	 * 
	 * @return the parent frame
	 */
	public static JFrame getFrame() {
		return _instance;
	}
	
	//this is so we can find json files
	private static void initClas12Dir() throws IOException {
		
		//for running from runnable jar (for coatjava)
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
		}
		else {
			System.err.println("**** Did not find CLAS12DIR [" + clasDir.getCanonicalPath() + "]");
		}
		
		String cwd = Environment.getInstance().getCurrentWorkingDirectory();
		clas12dir = cwd + "/../../../../../cnuphys/coatjava";
		clasDir = new File(clas12dir);
		
		if (clasDir.exists() && clasDir.isDirectory()) {
			System.err.println("**** Found CLAS12DIR [" + clasDir.getCanonicalPath() + "]");
			System.setProperty("CLAS12DIR", clas12dir);
			Log.getInstance().config("CLAS12DIR: " + clas12dir);
			return;
		}
		else {
			System.err.println("**** Did not find CLAS12DIR [" + clasDir.getCanonicalPath() + "]");
		}

		throw(new IOException("Could not locate the coatjava directory."));
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
		FastMath.setMathLib(FastMath.MathLib.SUPERFAST);
		
		//read in userprefs
		PropertiesManager.getInstance();
		
		//initialize the trigger manager
		TriggerManager.getInstance();
		
		//init the clas 12 dir wherev the json files are
		try {
			initClas12Dir();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		FileUtilities.setDefaultDir("data");

		// create a console log listener
		//Log.getInstance().addLogListener(new ConsoleLogListener());
		
		
		//splash frame
		final SplashWindowCED splashWindow = new SplashWindowCED("ced", null, 920, _release);

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
//				else if (arg[i].equalsIgnoreCase("-torus")) {
//					i++;
//					MagneticFields.getInstance().setTorusFullPath(arg[i]);
//					Log.getInstance().config("Torus Path: " + arg[i]);
//					System.out.println("Torus Path: " + arg[i]);
//				}
//				else if (arg[i].equalsIgnoreCase("-solenoid")) {
//					i++;
//					MagneticFields.getInstance().setSolenoidFullPath(arg[i]);
//					Log.getInstance().config("Solenoid Path: " + arg[i]);
//					System.out.println("Solenoid Path: " + arg[i]);
//				}
				else if (arg[i].contains("NO3D")) {
					_use3D = false;
					System.err.println("Not using 3D");
				}

				i++;
				done = (i >= len);
			} // !done
		} // end command arg processing
		
		//initialize magnetic fields
		MagneticFields.getInstance().initializeMagneticFields();
		
		// initialize geometry
		GeometryManager.getInstance();
		
		//Initialize data collectors
		DC.getInstance();
		FTOF.getInstance();
		BMTCrosses.getInstance();
		FMTCrosses.getInstance();
		BSTCrosses.getInstance();
		TBCrosses.getInstance();
		HBCrosses.getInstance();
		TBSegments.getInstance();
		HBSegments.getInstance();
		AllEC.getInstance();
		HTCC2.getInstance();
		FTCAL.getInstance();
		CTOF.getInstance();
		BST.getInstance();
		BMT.getInstance();
		Cosmics.getInstance();
		DataManager.getInstance();

//	    getInstance();  //creates ced frame


		// now make the frame visible, in the AWT thread
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
			    getInstance();
				splashWindow.setVisible(false);
				getCed().setVisible(true);
				getCed().fixTitle();
				
				ClasIoEventManager.getInstance().setUpFilterMenu();
				//initialize data columns
//				DataManager.getInstance();
				System.out.println("ced  " + _release + " is ready.");
			}

		});
		Log.getInstance().info(Environment.getInstance().toString());
		
		//try to update the log for fun
//		try {
//			updateCedLog();
//		}
//		catch (Exception e) {
//		}
		
		Log.getInstance().info("ced is ready.");
//		Environment.getInstance().say("c e d is ready");


	} // end main


}
