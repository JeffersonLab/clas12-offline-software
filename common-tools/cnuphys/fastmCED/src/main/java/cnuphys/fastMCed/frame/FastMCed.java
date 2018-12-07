package cnuphys.fastMCed.frame;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.geom.DetectorId;

import cnuphys.bCNU.application.BaseMDIApplication;
import cnuphys.bCNU.application.Desktop;
import cnuphys.bCNU.component.MagnifyWindow;
import cnuphys.bCNU.dialog.TextDisplayDialog;
import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.menu.MenuManager;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.LogView;
import cnuphys.bCNU.view.PlotView;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.consumers.ConsumerManager;
import cnuphys.fastMCed.eventgen.AEventGenerator;
import cnuphys.fastMCed.eventgen.GeneratorManager;
import cnuphys.fastMCed.eventio.IPhysicsEventListener;
import cnuphys.fastMCed.fastmc.FastMCMenuAddition;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.geometry.GeometryManager;
import cnuphys.fastMCed.properties.PropertiesManager;
import cnuphys.fastMCed.snr.SNRManager;
import cnuphys.fastMCed.streaming.IStreamProcessor;
import cnuphys.fastMCed.streaming.StreamManager;
import cnuphys.fastMCed.streaming.StreamProcessStatus;
import cnuphys.fastMCed.streaming.StreamReason;
import cnuphys.fastMCed.view.alldc.AllDCView;
import cnuphys.fastMCed.view.data.DataView;
import cnuphys.fastMCed.view.sector.DisplaySectors;
import cnuphys.fastMCed.view.sector.SectorView;
import cnuphys.fastMCed.view.trajinfo.TrajectoryInfoView;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFields;
import cnuphys.splot.example.MemoryUsageDialog;
import cnuphys.swim.SwimMenu;
import cnuphys.swim.Swimming;

/**
 * This is a brother to ced that works with the fastMC system rather than evio
 * or hipo
 * 
 * @author heddle
 *
 */
public class FastMCed extends BaseMDIApplication
		implements MagneticFieldChangeListener, IPhysicsEventListener, IStreamProcessor {

	// the singleton
	private static FastMCed _instance;

	// release (version) string
	private static final String _release = "build 0.52";
	
	// used for one time inits
	private int _firstTime = 0;
	
	//stream "state" label;
	private static JLabel _streamLabel;

	// event number label on menu bar
	private static JLabel _eventNumberLabel;
	
	// generator
	private static JLabel _generatorLabel;

	// memory usage dialog
	private MemoryUsageDialog _memoryUsage;

	// Environment display
	private TextDisplayDialog _envDisplay;
		
	//background string and related
	private static final String backgroundStr = "FastMCed from CNU";
	private int bstrW = -1;
	private int bstrH = -1;
	private static Font bstrFont = new Font("SansSerif", Font.ITALIC + Font.BOLD, 44);
	private static final Color bstrColor = X11Colors.getX11Color("Navy", 100);


	// the views
	private VirtualView _virtualView;
	private TrajectoryInfoView _trajInfoView;
	private AllDCView _allDCView;
	private DataView _dcDataView;
	private DataView _ftofDataView;
	private SectorView _sectorView14;
	private SectorView _sectorView25;
	private SectorView _sectorView36;
	private PlotView _plotView;
	private LogView _logView;

	//consumer classes dir provide by -p command line argument
	private static String _userConsumerDir;

	/**
	 * Constructor (private--used to create singleton)
	 * 
	 * @param keyVals
	 *            an optional variable length list of attributes in type-value
	 *            pairs. For example, PropertySupport.NAME, "my application",
	 *            PropertySupport.CENTER, true, etc.
	 */
	private FastMCed(Object... keyVals) {
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
		
	//	addHeadsUp();
		
		DrawableAdapter drawable = new DrawableAdapter() {
			@Override
			public void draw(Graphics g, IContainer container) {
				if (bstrW < 0) {
					FontMetrics fm = FastMCed.getInstance().getFontMetrics(bstrFont);
					bstrW = fm.stringWidth(backgroundStr);
					bstrH = fm.getHeight();
				}
				
				int hgap = 200;
				int vgap = 200;
				
				int tileW = bstrW + hgap;
				int tileH = bstrH + vgap;
				
				Rectangle b = Desktop.getInstance().getBounds();
				
				int numCol = 1 + b.width/tileW;
				int numRow = 1 + b.height/tileH;
				
				
				g.setFont(bstrFont);
				g.setColor(bstrColor);
				
				for (int col = 0; col < numCol; col++) {
					int x = 20 + col*tileW;
					for (int row = 0; row < numRow; row++) {
						int y = 80 + row*tileH;
						g.drawString(backgroundStr, x, y);
					}
				}
			}
		};
		
		Desktop.getInstance().setAfterDraw(drawable);
		
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
		
		_virtualView.moveToStart(_sectorView14, 0, VirtualView.UPPERLEFT);
		_virtualView.moveToStart(_sectorView25, 0, VirtualView.UPPERLEFT);
		_virtualView.moveToStart(_sectorView36, 0, VirtualView.UPPERLEFT);

		_virtualView.moveTo(_plotView, 0, VirtualView.CENTER);

		_virtualView.moveTo(_allDCView, 1);
		_virtualView.moveTo(_trajInfoView, 0, VirtualView.UPPERRIGHT);
		_virtualView.moveTo(_dcDataView, 2, VirtualView.BOTTOMLEFT);
		_virtualView.moveTo(_ftofDataView, 2, VirtualView.BOTTOMRIGHT);

		_virtualView.moveTo(_logView, 5, VirtualView.UPPERRIGHT);

		Log.getInstance().config("reset views on virtual dekstop");

	}

	/**
	 * Add the initial views to the desktop.
	 */
	private void addInitialViews() {

		// add an object that can respond to a "swim all MC" request.

		// FastMCEventManager.getInstance().setAllMCSwimmer(new SwimAllMC());

		// add a virtual view
		_virtualView = VirtualView.createVirtualView(8);
		ViewManager.getInstance().getViewMenu().addSeparator();
		
		_sectorView36=SectorView.createSectorView(DisplaySectors.SECTORS36);
		_sectorView25=SectorView.createSectorView(DisplaySectors.SECTORS25);
		_sectorView14=SectorView.createSectorView(DisplaySectors.SECTORS14);
		ViewManager.getInstance().getViewMenu().addSeparator();


		// add an alldc view
		_allDCView = AllDCView.createAllDCView();

		// add monte carlo view
		_trajInfoView = new TrajectoryInfoView();

		// data views
		_dcDataView = new DataView("Drift Chamber Hits", DetectorId.DC);
		_ftofDataView = new DataView("FTOF Hits", DetectorId.FTOF);
		
		ViewManager.getInstance().getViewMenu().addSeparator();
		//plot view
		_plotView = new PlotView();
		_logView = new LogView();



		// log some environment info
		Log.getInstance().config(Environment.getInstance().toString());

		// use config file info
		// Desktop.getInstance().configureViews();

		_virtualView.toFront();
	}

	/**
	 * private access to the FastMCed singleton.
	 * 
	 * @return the singleton FastMCed (the main application frame.)
	 */
	private static FastMCed getInstance() {
		if (_instance == null) {
			_instance = new FastMCed(PropertySupport.TITLE, "fastmCED " + versionString(),
//					PropertySupport.BACKGROUNDIMAGE, "images/cnuinv.png", 
					PropertySupport.BACKGROUND, new Color(48, 48, 48),
					PropertySupport.FRACTION, 0.9);

			_instance.addInitialViews();
			_instance.createMenus();
			_instance.placeViewsOnVirtualDesktop();

			_instance._generatorLabel= _instance.createLabel(" GENERATOR  none");
	     	_instance._streamLabel= _instance.createLabel(" STREAM "  + StreamReason.STOPPED);
			_instance._eventNumberLabel= _instance.createLabel("  Event #                 ");
			MagneticFields.getInstance().addMagneticFieldChangeListener(_instance);

		}
		return _instance;
	}

	/**
	 * Add items to existing menus and/or create new menus NOTE: Swim menu is
	 * created by the SwimManager
	 */
	private void createMenus() {
		MenuManager mmgr = MenuManager.getInstance();

		// create the mag field menu
		MagneticFields.getInstance().setActiveField(MagneticFields.FieldType.TORUS);
		mmgr.addMenu(MagneticFields.getInstance().getMagneticFieldMenu());

		// the swimmer menu
		mmgr.addMenu(SwimMenu.getInstance());

		// add to the file menu
		addToFileMenu();

		// FastMC
		new FastMCMenuAddition(mmgr.getFileMenu());

		// the options menu
		addToOptionMenu(mmgr.getOptionMenu());
		
		//the generator menu
		mmgr.addMenu(GeneratorManager.getInstance().getMenu());
		
		//consumer menu
		mmgr.addMenu(ConsumerManager.getInstance().getMenu());

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

	}

	/**
	 * Refresh all views (with containers)
	 */
	public static void refresh() {
		ViewManager.getInstance().refreshAllViews();
	}

	//fix the event number label
	private void fixEventNumberLabel() {
		
		int evNum = PhysicsEventManager.getInstance().eventNumber();
		int evCount = PhysicsEventManager.getInstance().getEventCount();
		
		_eventNumberLabel.setText("  Event #" + evNum + " of " + evCount);
	}
	
	
	//fix the stream state label
	private void fixStreamLabel(StreamReason reason) {
		_streamLabel.setText(" STREAM "  + reason);
	}
	
	//fix the generator label
	private void fixGeneratorLabel() {
		_generatorLabel.setText(" GENERATOR "  + PhysicsEventManager.getInstance().getGeneratorDescription());
	}

	/**
	 * public access to the singleton
	 * 
	 * @return the singleton FastMCed (the main application frame.)
	 */
	public static FastMCed getFastMCed() {
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

	//create a label for the menu =bar
	private JLabel createLabel(String defStr) {
		JLabel label = new JLabel(defStr);
		label.setOpaque(true);
		label.setBackground(Color.black);
		label.setForeground(Color.yellow);
		label.setFont(new Font("Dialog", Font.BOLD, 12));
		label.setBorder(BorderFactory.createLineBorder(Color.cyan, 1));

		getJMenuBar().add(Box.createHorizontalGlue());
		getJMenuBar().add(label);
		getJMenuBar().add(Box.createHorizontalStrut(5));
		return label;
	}

	@Override
	public void magneticFieldChanged() {
		Swimming.clearMCTrajectories();
		fixTitle();
		PhysicsEventManager.getInstance().reloadCurrentEvent();
	}
	
	/**
	 * Get the plot view
	 * @return the plot voew;
	 */
	public PlotView getPlotView() {
		return _plotView;
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

		title += "   [Field: " + MagneticFields.getInstance().getActiveFieldDescription();
		title += "]";

		title += " [Generator: " + PhysicsEventManager.getInstance().getGeneratorDescription() + "]";

		setTitle(title);
	}

	/**
	 * Get the parent frame
	 * 
	 * @return the parent frame
	 */
	public static JFrame getFrame() {
		return _instance;
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
						_memoryUsage = new MemoryUsageDialog(getFrame());
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
	 * A new event generator is active
	 * @param generator the now active generator
	 */
	@Override
	public void newEventGenerator(final AEventGenerator generator) {
		fixGeneratorLabel();
	}

	@Override
	public void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		fixEventNumberLabel();
	}
	
	@Override
	public void streamingChange(StreamReason reason) {
		fixStreamLabel(reason);
	}

	@Override
	public StreamProcessStatus streamingPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		return StreamProcessStatus.CONTINUE;
	}

	@Override
	public String flagExplanation() {
		return "This should not have happened (A).";
	}
	
	/**
	 * Get the optional consumer classes directory provide by -p command line argument
	 * @return the optional consumer classes directory path.
	 */
	public static String getUserConsumerDir() {
		return _userConsumerDir;
	}
	
	/**
	 * Launch the FastMCed GUI
	 * @param consumerPath an optional path to a folder that contains your consumer classes.
	 * This allows you to load consumers at startup.
	 */
	public static void launch(String consumerPath) {
		if (consumerPath == null) {
			main(null);
		}
		else {
			String arg[] = {"-p", consumerPath};
			main(arg);
		}
	}

	/**
	 * Main program launches the ced gui.
	 * <p>
	 * Command line arguments:</br>
	 * -p [dir] dir is the default directory
	 * 
	 * @param arg
	 *            the command line arguments.
	 */
	public static void main(String[] arg) {
		FastMath.setMathLib(FastMath.MathLib.SUPERFAST);

		// read in userprefs
		PropertiesManager.getInstance();

		FileUtilities.setDefaultDir("data");

		// splash frame
		final SplashWindowFastMCed splashWindow = new SplashWindowFastMCed("fastmCED", null, 920, _release);

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

		// process command args
		if ((arg != null) && (arg.length > 0)) {
			int len = arg.length;
			int lm1 = len - 1;
			boolean done = false;
			int i = 0;
			while (!done) {
				if (arg[i].equalsIgnoreCase("-p")) {
					if (i < lm1) {
						i++;
						_userConsumerDir = arg[i];
						System.err.println("Consumer Dir: [" + _userConsumerDir + "]");
					}
				}

				i++;
				done = (i >= len);
			} // !done
		} // end command arg processing

		// initialize magnetic fields
		MagneticFields.getInstance().initializeMagneticFields();

		// initialize some managers
		GeometryManager.getInstance();
		StreamManager.getInstance();
		ConsumerManager.getInstance();
		SNRManager.getInstance();
		GeneratorManager.getInstance();

		// now make the frame visible, in the AWT thread
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				FastMCed app = getInstance();
				PhysicsEventManager.getInstance().addPhysicsListener(app, 2);
				StreamManager.getInstance().addStreamListener(app);
				splashWindow.setVisible(false);
				getFastMCed().setVisible(true);
				getFastMCed().fixTitle();

				System.out.println("fastmCED  " + _release + " is ready.");
			}

		});
		Log.getInstance().info(Environment.getInstance().toString());

		Log.getInstance().info("fastmCED is ready.");

	} // end main



}
