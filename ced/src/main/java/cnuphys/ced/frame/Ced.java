package cnuphys.ced.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.jlab.data.io.DataEvent;

import cnuphys.bCNU.application.BaseMDIApplication;
import cnuphys.bCNU.attributes.AttributeType;
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
import cnuphys.ced.cedview.sectorview.DisplaySectors;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventMenu;
import cnuphys.ced.clasio.ClasIoEventView;
import cnuphys.ced.clasio.ClasIoMonteCarloView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoReconEventView;
import cnuphys.ced.dcnoise.edit.NoiseParameterDialog;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.ced.event.PlotManager;
import cnuphys.ced.geometry.DCGeometry;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.magfield.SwimAllMC;
import cnuphys.ced.magfield.SwimAllRecon;
import cnuphys.ced.noise.NoiseManager;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFields;
import cnuphys.swim.SwimMenu;
import cnuphys.bCNU.eliza.ElizaDialog;
import cnuphys.bCNU.et.ETSupport;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.log.ConsoleLogListener;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.bCNU.menu.MenuManager;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.view.LogView;
import cnuphys.bCNU.view.MiniShellView;
import cnuphys.bCNU.view.PlotView;
import cnuphys.bCNU.view.ViewManager;
//import cnuphys.bCNU.view.XMLView;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.bCNU.view.XMLView;

@SuppressWarnings("serial")
public class Ced extends BaseMDIApplication implements PropertyChangeListener,
	IAccumulationListener, MagneticFieldChangeListener {

    // the singleton
    private static Ced instance;

    // main version
    private static final int majorRelease = 0;

    // subversion
    private static final int minorRelease = 9;

    // used for one time inits
    private int _firstTime = 0;

    // using 3D?
    private static boolean _use3D;

    // the swim menu
    private SwimMenu _swimMenu;

    // event menu
    private ClasIoEventMenu _eventMenu;

    // progress bar
    private JProgressBar _progressBar;
    private JLabel _progressLabel;

    // event number label on menu bar
    private JLabel _eventNumberLabel;

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
    private ECView _ecView;
    private PCALView _pcalView;
    private LogView _logView;
    private XMLView _xmlView;
    private MiniShellView _shellView;
    private PlotView _plotView;
    private ForwardView3D _forward3DView;
    private CentralView3D _central3DView;
    private FTCalView3D _ftCal3DView;

    // the about string
    private static String _aboutString = "<html><span style=\"font-size:8px\">ced: the cLAS eVENT dISPLAY<br><br>Developed by Christopher Newport University";

    // icon for about ced dialog
    protected static ImageIcon _aboutIcon = ImageManager.getInstance()
	    .loadImageIcon("images/cnuicon.png");

    /**
     * Constructor (private--used to create singleton)
     * 
     * @param keyVals an optional variable length list of attributes in
     *            type-value pairs. For example, AttributeType.NAME,
     *            "my application", AttributeType.CENTER, true, etc.
     */
    private Ced(Object... keyVals) {
	super(keyVals);

	AccumulationManager.getInstance().addAccumulationListener(this);

	ComponentListener cl = new ComponentListener() {

	    @Override
	    public void componentHidden(ComponentEvent ce) {
	    }

	    @Override
	    public void componentMoved(ComponentEvent ce) {
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
	    _virtualView.moveTo(_allDCView, 0, 4);
	    _virtualView.moveTo(_eventView, 0, 5, VirtualView.BOTTOMRIGHT);
	    _virtualView.moveTo(_gemcView, 0, 5, VirtualView.BOTTOMLEFT);
	    _virtualView.moveTo(_bstXyView, 0, 2, VirtualView.BOTTOMLEFT);
	    _virtualView.moveTo(_bstZView, 0, 2, VirtualView.UPPERRIGHT);

	    // note no constraint means "center"
	    _virtualView.moveTo(_dcXyView, 0, 3);

	    _virtualView.moveTo(_pcalView, 0, 1, VirtualView.BOTTOMLEFT);
	    _virtualView.moveTo(_ecView, 0, 1, VirtualView.UPPERRIGHT);
	    _virtualView.moveTo(_logView, 0, 11, VirtualView.UPPERRIGHT);
	    _virtualView.moveTo(_shellView, 0, 11, VirtualView.BOTTOMLEFT);
	    _virtualView.moveTo(_xmlView, 0, 11, VirtualView.BOTTOMRIGHT);
	    _virtualView.moveTo(_monteCarloView, 0, 0, VirtualView.UPPERRIGHT);
	    _virtualView.moveTo(_reconEventView, 0, 0, VirtualView.BOTTOMRIGHT);
	    _virtualView.moveTo(_plotView, 0, 0, VirtualView.BOTTOMLEFT);

	    _virtualView.moveTo(_ftcalXyView, 0, 8, VirtualView.BOTTOMLEFT);

	    if (_use3D) {
		_virtualView.moveTo(_forward3DView, 0, 6, VirtualView.CENTER);
		_virtualView.moveTo(_central3DView, 0, 7, VirtualView.CENTER);
		_virtualView.moveTo(_ftCal3DView, 0, 8,
			VirtualView.BOTTOMRIGHT);
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
	_virtualView = VirtualView.createVirtualView(12);
	ViewManager.getInstance().getViewMenu().addSeparator();

	// add GEMC data view
	_gemcView = new GEMCView();

	// add event view
	_eventView = ClasIoEventView.createEventView();

	// add monte carlo view
	_monteCarloView = new ClasIoMonteCarloView();

	// add a reconstructed tracks view
	_reconEventView = ClasIoReconEventView.getInstance();

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
	SectorView.createSectorView(DisplaySectors.SECTORS36);
	SectorView.createSectorView(DisplaySectors.SECTORS25);
	SectorView.createSectorView(DisplaySectors.SECTORS14);

	// add logview
	ViewManager.getInstance().getViewMenu().addSeparator();
	_logView = new LogView();

	// add shellview
	_shellView = MiniShellView.getInstance();

	// plot view
	_plotView = new PlotView();

	// log some environment info
	Log.getInstance().config(Environment.getInstance().toString());

	// xml view
	_xmlView = new XMLView();

	// use config file info
	// Desktop.getInstance().configureViews();

	_virtualView.toFront();
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
	_swimMenu = new SwimMenu();
	mmgr.addMenu(_swimMenu);
	_swimMenu.addPropertyChangeListener(this);

	// remove the option menu until I need it
	// mmgr.removeMenu(mmgr.getOptionMenu());

	// add to the file menu
	JMenu fmenu = mmgr.getFileMenu();

	JMenuItem aboutItem = new JMenuItem("About ced...");
	ActionListener al0 = new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(Ced.getInstance(), _aboutString,
			"About ced", JOptionPane.INFORMATION_MESSAGE,
			_aboutIcon);

	    }
	};
	aboutItem.addActionListener(al0);

	fmenu.add(aboutItem, 0);

	// eliza!
	JMenuItem elizaItem = new JMenuItem("Eliza...");
	ActionListener al1 = new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		ElizaDialog.showEliza(instance);
	    }
	};
	elizaItem.addActionListener(al1);
	fmenu.add(elizaItem, 0);

	// add to the event menu
	addToEventMenu();
    }

    // create the options menu
    private void addToOptionMenu(JMenu omenu) {
	omenu.add(DCGeometry.getDCGeometryMenu());
	omenu.add(MagnifyWindow.magificationMenu());
    }
    
    /**
     * Refresh all views (with containers)
     */
    public void refresh() {
	ViewManager.getInstance().refreshAllContainerViews();
    }

    /**
     * Set the event number label 
     * @param num the event number
     */
    public void setEventNumberLabel(int num) {
	if (num < 0) {
	    _eventNumberLabel.setText("  Event Num:      is GEMC: false");
	}
	else {
	    _eventNumberLabel.setText("  Event Num: " + num + "  is GEMC: "
		    + ClasIoEventManager.getInstance().isGemcData());
	}
    }

    // add to the event menu
    private void addToEventMenu() {

	ClasIoEventManager.createSourceItems(_eventMenu);

	// add the noise parameter menu item
	ActionListener al2 = new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		NoiseParameterDialog dialog = new NoiseParameterDialog();
		dialog.setVisible(true);
	    }
	};
	MenuManager.addMenuItem("Noise Algorithm Parameters...", _eventMenu,
		al2);
    }

    /**
     * Public access to the Ced singleton.
     * 
     * @return the singleton Ced (the main application frame.)(
     */
    public static Ced getInstance() {
	if (instance == null) {
	    instance = new Ced(AttributeType.TITLE, "ced " + versionString(),
		    AttributeType.BACKGROUNDIMAGE, "images/cnu.png",
		    AttributeType.WINDOWMENU, false, AttributeType.FRACTION,
		    0.85);

	    instance.addInitialViews();
	    instance.createMenus();

	    // make sure plot manager is ready
	    PlotManager.getInstance();
	}
	return instance;
    }

    /**
     * Generate the version string
     * 
     * @return the version string
     */
    public static String versionString() {
	return String.format("Version %d.%d", majorRelease, minorRelease);
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

    /**
     * Accessor for the swim menu
     * 
     * @return the swim menu
     */
    public SwimMenu getSwimMenu() {
	return _swimMenu;
    }

    /**
     * Obtain the single shared plotview
     * 
     * @return the single shared plotview
     */
    public PlotView getPlotView() {
	return _plotView;
    }

    /**
     * Notification of accumulation events
     */
    @Override
    public void accumulationEvent(int reason) {
	switch (reason) {
	case AccumulationManager.ACCUMULATION_STARTED:
	    break;

	case AccumulationManager.ACCUMULATION_CANCELLED:
	    break;

	case AccumulationManager.ACCUMULATION_FINISHED:
	    break;
	}
    }

    public static Ced createCed(String dataPath, String torusPath,
	    String solenoidPath) {

	FileUtilities.setDefaultDir("data");

	if (torusPath != null) {
	    MagneticFields.setTorusFullPath(torusPath);
	    Log.getInstance().config("Torus Path: " + torusPath);
	    System.out.println("Torus Path: " + torusPath);
	}

	if (solenoidPath != null) {
	    MagneticFields.setSolenoidFullPath(solenoidPath);
	    Log.getInstance().config("Solenoid Path: " + solenoidPath);
	    System.out.println("Solenoid Path: " + solenoidPath);
	}

	final Ced ced = getInstance();

	// create initial canned plots
	PlotManager.getInstance();

	// now make the frame visible, in the AWT thread
	EventQueue.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		ced.setVisible(true);
	    }

	});
	Log.getInstance().info("CED 12.0 GeV is ready.");

	return ced;
    }

    public void loadEvent(final DataEvent event) {

	Runnable doRun = new Runnable() {

	    @Override
	    public void run() {
		ClasIoEventManager.getInstance().loadEvent(event);
	    }

	};
	try {
	    SwingUtilities.invokeAndWait(doRun);
	} catch (InvocationTargetException e) {
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

    }

    private void createProgressBar() {
	getJMenuBar().add(Box.createHorizontalStrut(20));
	_progressLabel = new JLabel("             ");
	_progressLabel.setOpaque(true);
	_progressLabel.setBackground(Color.white);
	_progressLabel.setForeground(Color.red);

	_progressBar = new JProgressBar() {
	    @Override
	    public void setString(String s) {
		_progressLabel.setText(s);
	    }
	};
	_progressBar.setIndeterminate(true);

	int mbh = getJMenuBar().getPreferredSize().height;
	int pbh = 10;
	final int vgap = Math.min(2, 1 + (mbh - pbh) / 2);

	_progressBar.setVisible(false);
	Dimension size = new Dimension(60, pbh);
	_progressBar.setMinimumSize(size);
	_progressBar.setMaximumSize(size);
	_progressBar.setPreferredSize(size);
	_progressBar.setOpaque(true);
	_progressBar.setBackground(Color.white);

	_progressLabel.setFont(Fonts.mediumFont);

	JPanel panel = new JPanel() {
	    @Override
	    public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(vgap, def.left + 2, 0, def.right + 2);
	    }
	};

	panel.setOpaque(true);
	panel.setBackground(Color.white);
	panel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

	panel.add(_progressLabel);
	panel.add(_progressBar);
	getJMenuBar().add(panel);
    }

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
	int index = title.indexOf("   Mag");
	if (index > 0) {
	    title = title.substring(0, index);
	}

	title += "   Magnetic Field: "
		+ MagneticFields.getActiveFieldDescription();
	setTitle(title);
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

	FileUtilities.setDefaultDir("data");

	// create a console log listener
	Log.getInstance().addLogListener(new ConsoleLogListener());

	// initialize geometry
	GeometryManager.getInstance();

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
		else if (arg[i].contains("3D")) {
		    _use3D = true;
		    System.err.println("Using 3D");
		}

		i++;
		done = (i >= len);
	    }
	}

	final Ced ced = getInstance();

	ced.createProgressBar();
	ced.createEventNumberLabel();
	MagneticFields.addMagneticFieldChangeListener(ced);

	// now make the frame visible, in the AWT thread
	EventQueue.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		ced.setVisible(true);
		ced.fixTitle();
	    }

	});
	Log.getInstance().info("CED 12.0 GeV is ready.");
    }

    @Override
    public void magneticFieldChanged() {
	fixTitle();
    }

    public JProgressBar getProgressBar() {
	return _progressBar;
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

}
