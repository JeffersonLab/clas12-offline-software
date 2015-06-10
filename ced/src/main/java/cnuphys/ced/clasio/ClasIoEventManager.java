package cnuphys.ced.clasio;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingWorker;
import javax.swing.event.EventListenerList;

import org.jlab.data.io.DataEvent;
import org.jlab.evio.clas12.EvioDataDictionary;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioFactory;
import org.jlab.evio.clas12.EvioSource;

import cnuphys.bCNU.et.ETSupport;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.ced.event.data.BSTDataContainer;
import cnuphys.ced.event.data.DCDataContainer;
import cnuphys.ced.event.data.ECDataContainer;
import cnuphys.ced.event.data.FTOFDataContainer;
import cnuphys.ced.event.data.GEMCMetaDataContainer;
import cnuphys.ced.event.data.GenPartDataContainer;
import cnuphys.ced.event.data.RecEventDataContainer;
import cnuphys.ced.frame.Ced;
import cnuphys.lund.LundId;
import cnuphys.swim.SwimMenu;
import cnuphys.swim.Swimming;

public class ClasIoEventManager {

    /** check box for file as event source */
    public static JRadioButtonMenuItem fileRadioMenuItem; // file

    /** check box for ET as event source */
    public static JRadioButtonMenuItem etRadioMenuItem; // ET system

    // Unique lund ids in the event (if any)
    private Vector<LundId> uniqueLundIds = new Vector<LundId>();

    // A list of known, sorted banks from the dictionary
    private String _knownBanks[];

    // A sorted list of banks present in the current event
    private String _currentBanks[];

    // used in pcal and ec hex gradient displays
    private double maxEDepCal[] = { Double.NaN, Double.NaN, Double.NaN };

    // sources of events (the type, not the actual source)
    public enum EventSourceType {
	FILE, ET
    }

    private boolean _enabled = true;
    
    
    // flag that set set to <code>true</code> if we are accumulating events
    private boolean _accumulating = false;

    // list of view listeners. There are actually three lists. Those in index 0
    // are notified first. Then those in index 1. Finally those in index 2. The
    // Data
    // containers should be in index 0. The trajectory and noise in index 1, and
    // the
    // regular views in index 2 (they are notified last)
    private EventListenerList _viewListenerList[] = new EventListenerList[3];

    // someone who can swim all MC particles
    private ISwimAll _allMCSwimmer;

    // someone who can swim all recon particles
    private ISwimAll _allReconSwimmer;

    // the current event file (meaningless if reading from ET)
    private File _currentEventFile;

    // the clas_io source of events
    private EvioSource _evioSource;

    // singleton
    private static ClasIoEventManager instance;

    // the current event
    private EvioDataEvent _currentEvent;

    // the data containers
    private DCDataContainer _dcData;
    private ECDataContainer _ecData;
    private FTOFDataContainer _ftofData;
    private BSTDataContainer _bstData;
    private GenPartDataContainer _genPartData;
    private RecEventDataContainer _recEventData;
    private GEMCMetaDataContainer _gemcMetaData;

    // private constructor for singleton
    private ClasIoEventManager() {
	_evioSource = new EvioSource();
	_dcData = new DCDataContainer(this);
	_ecData = new ECDataContainer(this);
	_ftofData = new FTOFDataContainer(this);
	_bstData = new BSTDataContainer(this);
	_genPartData = new GenPartDataContainer(this);
	_recEventData = new RecEventDataContainer(this);
	_gemcMetaData = new GEMCMetaDataContainer(this);
    }

    /**
     * Get the EC data
     * 
     * @return the EC data container
     */
    public ECDataContainer getECData() {
	return _ecData;
    }

    /**
     * Get the reconstructed event data
     * 
     * @return the reconstructed event data container
     */
    public RecEventDataContainer getReconEventData() {
	return _recEventData;
    }

    /**
     * Get the Generated particle data
     * 
     * @return the DC data container
     */
    public GenPartDataContainer getGenPartData() {
	return _genPartData;
    }

    /**
     * Get the DC data
     * 
     * @return the DC data container
     */
    public DCDataContainer getDCData() {
	return _dcData;
    }

    /**
     * Get the GEMCMetaData data
     * 
     * @return the GEMCMetaData data container
     */
    public GEMCMetaDataContainer getGEMCMetaData() {
	return _gemcMetaData;
    }


    /**
     * Get the FTOF data
     * 
     * @return the FTOF data container
     */
    public FTOFDataContainer getFTOFData() {
	return _ftofData;
    }

    /**
     * Get the BST data
     * 
     * @return the BST data container
     */
    public BSTDataContainer getBSTData() {
	return _bstData;
    }

    /**
     * Get the unqique lund ids in the current event (if any)
     * 
     * @return the unqique lund ids in the current event (if any)
     */
    public Vector<LundId> getUniqueLundIds() {
	return uniqueLundIds;
    }

    /**
     * Access for the singleton
     * 
     * @return the singleton
     */
    public static ClasIoEventManager getInstance() {
	if (instance == null) {
	    instance = new ClasIoEventManager();
	}
	return instance;
    }

    /**
     * Get the underlying clas-io EvioSource
     * 
     * @return the EvioSource object
     */
    public EvioSource getEvioSource() {
	return _evioSource;
    }

    /**
     * @return the accumulating
     */
    public boolean isAccumulating() {
	return _accumulating;
    }

    /**
     * @param accumulating
     *            the accumulating to set
     */
    public void setAccumulating(boolean accumulating) {
	_accumulating = accumulating;
    }

    /**
     * Get the current event file
     * 
     * @return the current file
     */
    public File getCurrentEventFile() {
	return _currentEventFile;
    }

    /**
     * Get the current event
     * 
     * @return the current event
     */
    public EvioDataEvent getCurrentEvent() {
	return _currentEvent;
    }

    /**
     * Get the path of the current file
     * 
     * @return the path of the current file
     */
    public String getCurrentEventFilePath() {
	return (_currentEventFile == null) ? "(none)" : _currentEventFile
		.getPath();
    }

    /**
     * Set the clas-io evio source to read from an evio file
     * 
     * @param path
     *            the full path to the file
     * @return the current compact reader (might be <code>null</code>);
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void openEvioFile(String path) throws FileNotFoundException,
	    IOException {
	openEvioFile(new File(path));
    }

    /**
     * Set the clas-io evio source to read from an evio file
     * 
     * @param file
     *            the evio file
     * @return the current compact reader (might be <code>null</code>);
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void openEvioFile(File file) throws FileNotFoundException,
	    IOException {

	if (!file.exists()) {
	    throw (new FileNotFoundException("Evio event file not found"));
	}
	if (!file.canRead()) {
	    throw (new FileNotFoundException("Evio file cannot be read"));
	}
	
	threadedOpenEvioFile(file);
    }

    private void threadedOpenEvioFile(final File file) {

	final JProgressBar progressBar = Ced.getInstance().getProgressBar();
	progressBar.setString("Reading " + file.getPath());
	progressBar.setIndeterminate(true);
	setEnabled(false);

	class MyWorker extends SwingWorker<String, Void> {
	    @Override
	    protected String doInBackground() {
		progressBar.setVisible(true);
		_evioSource.close();
		_evioSource.open(file);
		notifyListeners(file.getPath());
		_currentEventFile = file;
		return "Done.";
	    }

	    @Override
	    protected void done() {
		progressBar.setString(file.getPath());
		progressBar.setIndeterminate(false);
		progressBar.setVisible(false);
		setEnabled(true);
		try {
		    getNextEvent();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}

	new MyWorker().execute();
    }

    /**
     * Get the current event source type
     * 
     * @return the current event source type
     */
    public static EventSourceType getEventSourceType() {
	if (etRadioMenuItem == null) {
	    return EventSourceType.FILE;
	}

	if (etRadioMenuItem.isSelected()) {
	    return EventSourceType.ET;
	} else {
	    return EventSourceType.FILE;
	}
    }

    /**
     * Create the source *file, et) radio buttons
     * 
     * @param menu
     *            the menu to add them to (an event menu)
     */
    public static void createSourceItems(final ClasIoEventMenu menu) {

	Log.getInstance().info("Adding late items to the event menu.");

	ButtonGroup bg = new ButtonGroup();

	ActionListener sal = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		menu.fixState();
	    }

	};

	fileRadioMenuItem = new JRadioButtonMenuItem("Events From Files",
		getEventSourceType() == EventSourceType.FILE);
	etRadioMenuItem = new JRadioButtonMenuItem("Events From ET",
		getEventSourceType() == EventSourceType.ET);

	fileRadioMenuItem.addActionListener(sal);
	etRadioMenuItem.addActionListener(sal);

	etRadioMenuItem.setEnabled(false);

	bg.add(fileRadioMenuItem);
	bg.add(etRadioMenuItem);

	menu.add(fileRadioMenuItem, 0);
	menu.add(etRadioMenuItem, 1);
	menu.insertSeparator(2);
    }

    /**
     * Check whether current event source type is a file
     * 
     * @return <code>true</code> is source type is a file.
     */
    public boolean isSourceFile() {
	return getEventSourceType() == EventSourceType.FILE;
    }

    /**
     * Check whether current event source type is ET
     * 
     * @return <code>true</code> is source type is ET.
     */
    public boolean isSourceET() {
	return getEventSourceType() == EventSourceType.ET;
    }

    /**
     * Get the number of events available, 0 for ET since that is unknown.
     * 
     * @return the number of events available
     */
    public int getEventCount() {

	int evcount = 0;
	if (isSourceFile()) {
	    evcount = _evioSource.getSize();
	} else if (isSourceET()) {
	}
	return evcount;
    }

    /**
     * Get the number of the current event, 0 if there is none
     * 
     * @return the number of the current event.
     */
    public int getEventNumber() {
	int evnum = 0;
	if (isSourceFile()) {
	    evnum = _evioSource.getCurrentIndex() - 1;
	} else if (isSourceET()) {
	    evnum = (int) ETSupport.getETEventNumber();
	}
	return evnum;
    }

    /**
     * Determines whether any next event control should be enabled.
     * 
     * @return <code>true</code> if any next event control should be enabled.
     */
    public boolean isNextOK() {
	return (isSourceFile() && (getEventCount() > 0) && (getEventNumber() < getEventCount()))
		|| (isSourceET() && ETSupport.isReady());
    }

    /**
     * Obtain the number of remaining events. For a file source it is what you
     * expect. For an et source, it is arbitrarily set to a large number
     * 
     * @return the number of remaining events
     */
    public int getNumRemainingEvents() {
	int numRemaining = 0;

	if (isSourceFile() && (getEventCount() > 0)
		&& (getEventNumber() < getEventCount())) {
	    numRemaining = getEventCount() - getEventNumber();
	} else if (isSourceET() && ETSupport.isReady()) {
	    numRemaining = ClasIoAccumulationDialog.MAXACCUMULATIONCOUNT;
	}
	return numRemaining;
    }

    /**
     * Determines whether any prev event control should be enabled.
     * 
     * @return <code>true</code> if any prev event control should be enabled.
     */
    public boolean isPrevOK() {
	return isSourceFile() && (getEventNumber() > 1);
    }

    /**
     * Determines whether any goto event control should be enabled.
     * 
     * @return <code>true</code> if any prev event control should be enabled.
     */
    public boolean isGotoOK() {
	return isSourceFile() && (getEventCount() > 0);
    }

    /**
     * Get the object that can swim all MonteCarlo particles
     * 
     * @return the object that can swim all MonteCarlo particles
     */
    public ISwimAll getMCSwimmer() {
	return _allMCSwimmer;
    }

    /**
     * Set the object that can swim all MonteCarlo particles
     * 
     * @param allSwimmer
     *            the object that can swim all MonteCarlo particles
     */
    public void setAllMCSwimmer(ISwimAll allSwimmer) {
	_allMCSwimmer = allSwimmer;
    }

    /**
     * Get the object that can swim all reconstructed particles
     * 
     * @return the object that can swim all reconstructed particles
     */
    public ISwimAll getReconSwimmer() {
	return _allReconSwimmer;
    }

    /**
     * Set the object that can swim all reconstructed particles
     * 
     * @param allSwimmer
     *            the object that can swim all reconstructed particles
     */
    public void setAllReconSwimmer(ISwimAll allSwimmer) {
	_allReconSwimmer = allSwimmer;
    }

    /**
     * Get the next event from the current compact reader
     * 
     * @return the next event, if possible
     */
    public EvioDataEvent getNextEvent() {
	
	if (!isEnabled()) {
	    Toolkit.getDefaultToolkit().beep();
	    return null;
	}
	
	_currentEvent = (EvioDataEvent) _evioSource.getNextEvent();
	notifyListeners();
	return _currentEvent;
    }

    public void loadEvent(DataEvent event) {
	_currentEvent = (EvioDataEvent) event;
	notifyListeners();
    }

    /**
     * Get the previous event from the current compact reader
     * 
     * @return the previous event, if possible.
     */
    public EvioDataEvent getPreviousEvent() {
	_currentEvent = (EvioDataEvent) _evioSource.getPreviousEvent();
	notifyListeners();
	return _currentEvent;
    }

    /**
     * 
     * @param eventNumber
     *            a 1-based number 1..num events in file
     * @return the event at the given number (if possible).
     */
    public EvioDataEvent gotoEvent(int eventNumber) {
	_currentEvent = (EvioDataEvent) _evioSource.gotoEvent(eventNumber);
	notifyListeners();
	return _currentEvent;
    }

    /**
     * Reload the current event
     * 
     * @return the same current event
     */
    public EvioDataEvent reloadCurrentEvent() {
	if (_currentEvent != null) {
	    notifyListeners();
	}
	return _currentEvent;
    }

    /**
     * Notify listeners we have opened a new file
     * 
     * @param path
     *            the path to the new file
     */
    private void notifyListeners(String path) {

	Swimming.clearMCTrajectories();
	Swimming.clearReconTrajectories();

	for (int index = 0; index < 3; index++) {
	    if (_viewListenerList[index] != null) {
		// Guaranteed to return a non-null array
		Object[] listeners = _viewListenerList[index].getListenerList();

		// This weird loop is the bullet proof way of notifying all
		// listeners.
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
		    if (listeners[i] == IClasIoEventListener.class) {
			((IClasIoEventListener) listeners[i + 1])
				.openedNewEventFile(path);
		    }
		}
	    }
	}

    }

    /**
     * Notify listeners we have a new event ready for display. All they may want
     * is the notification that a new event has arrived. But the event itself is
     * passed along.
     * 
     */
    private void notifyListeners() {

	Ced.getInstance().setEventNumberLabel(getEventNumber());

	Swimming.clearMCTrajectories();
	Swimming.clearReconTrajectories();
	uniqueLundIds.clear();

	_currentBanks = (_currentEvent == null) ? null : _currentEvent
		.getBankList();
	if (_currentBanks != null) {
	    Arrays.sort(_currentBanks);
	}

	for (int index = 0; index < 3; index++) {
	    if (_viewListenerList[index] != null) {
		// Guaranteed to return a non-null array
		Object[] listeners = _viewListenerList[index].getListenerList();

		// This weird loop is the bullet proof way of notifying all
		// listeners.
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
		    if (listeners[i] == IClasIoEventListener.class) {
			((IClasIoEventListener) listeners[i + 1])
				.newClasIoEvent(_currentEvent);
		    }
		}
	    }

	} // index loop

	// some scaling factors for gradient displays
	computeSomeScalingFactors();

	// auto swim?
	if (Ced.getInstance().getSwimMenu().isAlwaysSwimMC()) {
	    Ced.getInstance().getSwimMenu()
		    .firePropertyChange(SwimMenu.SWIM_ALL_MC_PROP, 0, 1);
	}
	if (Ced.getInstance().getSwimMenu().isAlwaysSwimRecon()) {
	    Ced.getInstance().getSwimMenu()
		    .firePropertyChange(SwimMenu.SWIM_ALL_RECON_PROP, 0, 1);
	}
    }

    // compute some factors used in gradient displays
    private void computeSomeScalingFactors() {

	ECDataContainer ecData = getECData();
	if (ecData == null) {
	    return;
	}

	double edep[] = null;

	// pcal (plane = 0)
	edep = ecData.pcal_true_totEdep;
	if (edep != null) {
	    maxEDepCal[0] = 0;
	    for (double e : edep) {
		maxEDepCal[0] = Math.max(e, maxEDepCal[0]);
	    }
	}

	// ec
	edep = ecData.ec_true_totEdep;
	if (edep != null) {
	    maxEDepCal[1] = 0;
	    maxEDepCal[2] = 0;
	    for (int i = 0; i < edep.length; i++) {
		int plane = ecData.ec_dgtz_stack[i];
		maxEDepCal[plane] = Math.max(edep[i], maxEDepCal[plane]);
	    }
	}
    }

    /**
     * Get the maximum energy deposited in the cal for the current event. Might
     * be NaN if there are no "true" (gemc) banks
     * 
     * @param plane
     *            (0, 1, 2) for (PCAL, EC_INNER, EC_OUTER)
     * @return the max energy deposited in that cal plane in MeV
     */
    public double getMaxEdepCal(int plane) {
	return maxEDepCal[plane];
    }

    /**
     * Remove a PhysicsEvent listener. PhysicsEvent listeners listen for new
     * events and event arrays.
     * 
     * @param listener
     *            the PhysicsEvent listener to remove.
     */
    public void removePhysicsEventListener(IClasIoEventListener listener) {

	if (listener == null) {
	    return;
	}

	for (int i = 0; i < 3; i++) {
	    if (_viewListenerList[i] != null) {
		_viewListenerList[i].remove(IClasIoEventListener.class,
			listener);
	    }
	}
    }

    /**
     * Add a PhysicsEvent listener. PhysicsEvent listeners listen for new events
     * and event arrays.
     * 
     * @param listener
     *            the PhysicsEvent listener to add.
     * @param index
     *            Determines gross notification order. Those in index 0 are
     *            notified first. Then those in index 1. Finally those in index
     *            2. The Data containers should be in index 0. The trajectory
     *            and noise in index 1, and the regular views in index 2 (they
     *            are notified last)
     */
    public void addPhysicsListener(IClasIoEventListener listener, int index) {

	if (listener == null) {
	    return;
	}

	if (_viewListenerList[index] == null) {
	    _viewListenerList[index] = new EventListenerList();
	}

	_viewListenerList[index].add(IClasIoEventListener.class, listener);
    }

    /**
     * Get the names of the banks in the current event
     * 
     * @return the names of the banks in the current event
     */
    public String[] getCurrentBanks() {
	return _currentBanks;
    }

    /**
     * Checks if a bank, identified by a string such as "FTOF1B::dgtz", is in
     * the current event.
     * 
     * @param bankName
     *            the bank name
     * @return <code>true</code> if the bank is in the curent event.
     */
    public boolean isBankInCurrentEvent(String bankName) {
	if ((bankName == null) || (_currentBanks == null)) {
	    return false;
	}

	int index = Arrays.binarySearch(_currentBanks, bankName);
	return index >= 0;
    }

    /**
     * Get a sorted list of known banks from the dictinary
     * 
     * @return a sorted list of known banks
     */
    public String[] getKnownBanks() {

	if (_knownBanks == null) {
	    EvioDataDictionary dataDict = EvioFactory.getDictionary();
	    if (dataDict != null) {
		_knownBanks = dataDict.getDescriptorList();
		Arrays.sort(_knownBanks);
	    }
	}

	return _knownBanks;
    }

    /**
     * Check whether a given bank is a known bank
     * 
     * @param bankName
     *            the bank name
     * @return <code>true</code> if the name is recognized.
     */
    public boolean isKnownBank(String bankName) {
	String allBanks[] = getKnownBanks();
	if (allBanks == null) {
	    return false;
	}
	int index = Arrays.binarySearch(allBanks, bankName);
	return index >= 0;
    }
    
    public void setEnabled(boolean enabled) {
	_enabled = enabled;
    }
    
    public boolean isEnabled() {
	return _enabled;
    }

}
