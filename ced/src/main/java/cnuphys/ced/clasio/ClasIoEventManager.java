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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingWorker;
import javax.swing.event.EventListenerList;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.evio.clas12.EvioDataDictionary;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioFactory;
import org.jlab.evio.clas12.EvioSource;

import cnuphys.bCNU.component.BusyPanel;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.event.data.ColumnData;
import cnuphys.ced.event.data.EC;
import cnuphys.ced.event.data.GEMCMetaDataContainer;
import cnuphys.ced.event.data.PCAL;
import cnuphys.ced.fastmc.FastMCManager;
import cnuphys.ced.frame.Ced;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.swim.SwimMenu;
import cnuphys.swim.Swimming;

public class ClasIoEventManager {

	/** check box for file as event source */
	public static JRadioButtonMenuItem fileRadioMenuItem; // file

	/** check box for ET as event source */
	public static JRadioButtonMenuItem etRadioMenuItem; // ET system
	
	/** check box for FastMC as an event source */
	public static JRadioButtonMenuItem fastMCRadioMenuItem; // FastMC system

	// Unique lund ids in the event (if any)
	private Vector<LundId> _uniqueLundIds = new Vector<LundId>();

	// A list of known, sorted banks from the dictionary
	private String _knownBanks[];

	// A sorted list of banks present in the current event
	private String _currentBanks[];

	// used in pcal and ec hex gradient displays
	private double maxEDepCal[] = { Double.NaN, Double.NaN, Double.NaN };

	// sources of events (the type, not the actual source)
	public enum EventSourceType {
		FILE, ET, FASTMC
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

	// the gemc metadata container
	private GEMCMetaDataContainer _gemcMetaData;

	// private constructor for singleton
	private ClasIoEventManager() {
		_evioSource = new EvioSource();
		_gemcMetaData = new GEMCMetaDataContainer(this);
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
	 * Get a collection of unique LundIds in the current event
	 * @return a collection of unique LundIds
	 */
	public Vector<LundId> uniqueLundIds() {
		
		if (_uniqueLundIds != null) {
			return _uniqueLundIds;
		}

		_uniqueLundIds = new Vector<LundId>();

		if (this.isSourceFastMC()) {
			PhysicsEvent event = FastMCManager.getInstance().getCurrentGenEvent();
			if ((event != null) && (event.count() > 0)) {
				for (int index = 0; index < event.count(); index++) {
					Particle particle = event.getParticle(index);
					LundId lid = LundSupport.getInstance().get(particle.pid());
					_uniqueLundIds.remove(lid);
					_uniqueLundIds.add(lid);

				}
			}

		} else {
			if (_currentEvent != null) {
				// use any bank with a true pid column
				String[] knownBanks = ClasIoEventManager.getInstance().getKnownBanks();
				if (knownBanks != null) {
					for (String bankName : knownBanks) {
						if (bankName.contains("::true")) {
							ColumnData cd = ColumnData.getColumnData(bankName, "pid");
							if (cd != null) {
								int pid[] = (int[]) (cd.getDataArray());
								if (pid != null) {
									for (int pdgid : pid) {
										LundId lid = LundSupport.getInstance().get(pdgid);
										if (lid != null) {
											_uniqueLundIds.remove(lid);
											_uniqueLundIds.add(lid);
										}
									}
								}
							}
						}
					}
				}
			} // currentevent != null
		}
		
		return _uniqueLundIds;
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
	 * @param accumulating the accumulating to set
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
		return (_currentEventFile == null) ? "(none)"
				: _currentEventFile.getPath();
	}

	/**
	 * Get the base simple name of the current file
	 * 
	 * @return the path of the current file
	 */
	public String getCurrentEventFileName() {

		String s = "(none)";

		EventSourceType estype = ClasIoEventManager.getEventSourceType();

		switch (estype) {
		case FILE:
			return (_currentEventFile == null) ? "(none)" : _currentEventFile.getName();
		case ET:
			return "ET";
		case FASTMC:
			File lundFile = FastMCManager.getInstance().getCurrentFile();
			return (lundFile == null) ? "(none)" : lundFile.getName();
		}

		return s;
	}
	
	/**
	 * Set the clas-io evio source to read from an evio file
	 * 
	 * @param path the full path to the file
	 * @return the current compact reader (might be <code>null</code>);
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void openEvioFile(String path)
			throws FileNotFoundException, IOException {
		openEvioFile(new File(path));
	}

	/**
	 * Open an evio file
	 * 
	 * @param file the evio file
	 * @return the current compact reader (might be <code>null</code>);
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void openEvioFile(File file)
			throws FileNotFoundException, IOException {

		if (!file.exists()) {
			throw (new FileNotFoundException("Evio event file not found"));
		}
		if (!file.canRead()) {
			throw (new FileNotFoundException("Evio file cannot be read"));
		}

		threadedOpenEvioFile(file);
	}	
	
	//read in a separate thread
	private void threadedOpenEvioFile(final File file) {

		final BusyPanel busyPanel = Ced.getBusyPanel();
		busyPanel.setText("Reading " + file.getPath());
		busyPanel.setVisible(true);
		
		_evioSource.close();
		_currentEvent = null;

		class MyWorker extends SwingWorker<String, Void> {
			@Override
			protected String doInBackground() {
				_evioSource.open(file);
				notifyEventListeners(file.getPath());
				_currentEventFile = file;
				return "Done.";
			}

			@Override
			protected void done() {
				
				busyPanel.setVisible(false);
				Ced.getCed().fixTitle();
				setEnabled(true);
				try {
					getNextEvent();
				} catch (Exception e) {
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
		if ((fileRadioMenuItem == null) || (etRadioMenuItem == null) || (fastMCRadioMenuItem == null)) {
			return EventSourceType.FILE;
		}

		if (fileRadioMenuItem.isSelected()) {
			return EventSourceType.FILE;
		}
		else if (etRadioMenuItem.isSelected()) {
			return EventSourceType.ET;
		}
		else if (fastMCRadioMenuItem.isSelected()) {
			return EventSourceType.FASTMC;
		}
		else {
			return EventSourceType.FILE;
		}
	}

	/**
	 * Create the source *file, et) radio buttons
	 * 
	 * @param menu the menu to add them to (an event menu)
	 */
	public void createSourceItems(final ClasIoEventMenu menu) {

		Log.getInstance().info("Adding late items to the event menu.");

		ButtonGroup bg = new ButtonGroup();

		ActionListener sal = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				menu.fixState();
				notifyEventListeners(getEventSourceType());
				Ced.getCed().fixTitle();
			}

		};

		//radio buttons for event source
		fileRadioMenuItem = new JRadioButtonMenuItem("Events From Files",
				getEventSourceType() == EventSourceType.FILE);
		etRadioMenuItem = new JRadioButtonMenuItem("Events From ET",
				getEventSourceType() == EventSourceType.ET);
		fastMCRadioMenuItem = new JRadioButtonMenuItem("Events From FastMC",
				getEventSourceType() == EventSourceType.FASTMC);

		fileRadioMenuItem.addActionListener(sal);
		etRadioMenuItem.addActionListener(sal);
		fastMCRadioMenuItem.addActionListener(sal);

		etRadioMenuItem.setEnabled(false);

		bg.add(fileRadioMenuItem);
		bg.add(etRadioMenuItem);
		bg.add(fastMCRadioMenuItem);

		menu.addSeparator();
		menu.add(fileRadioMenuItem);
		menu.add(etRadioMenuItem);
		menu.add(fastMCRadioMenuItem);
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
	 * Check whether current event source type is FastMC
	 * 
	 * @return <code>true</code> is source type is FASTMC.
	 */
	public boolean isSourceFastMC() {
		return getEventSourceType() == EventSourceType.FASTMC;
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
		}
		else if (isSourceET()) {
		}
		else if (isSourceFastMC()) {
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
		}
		else if (isSourceFastMC()) {
			evnum = FastMCManager.getInstance().getEventNumber();
		}
		return evnum;
	}

	/**
	 * Determines whether any next event control should be enabled.
	 * 
	 * @return <code>true</code> if any next event control should be enabled.
	 */
	public boolean isNextOK() {
		
		boolean isOK = true;
		EventSourceType estype = ClasIoEventManager.getEventSourceType();
		
		switch (estype) {
		case FILE:
			isOK = (isSourceFile() && (getEventCount() > 0)
					&& (getEventNumber() < getEventCount()));
			break;
		case ET:
			break;
		case FASTMC:
			isOK = (FastMCManager.getInstance().getCurrentFile() != null);
			break;
		}

		return isOK;
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
	 * @param allSwimmer the object that can swim all MonteCarlo particles
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
	 * @param allSwimmer the object that can swim all reconstructed particles
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

		EventSourceType estype = ClasIoEventManager.getEventSourceType();
		switch (estype) {
		case FILE:
			_currentEvent = (EvioDataEvent) _evioSource.getNextEvent();
			notifyEventListeners();
			break;
		case ET:
			break;
		case FASTMC:
			FastMCManager.getInstance().nextEvent();
			break;
		}

		return _currentEvent;
	}

	/**
	 * Get the previous event from the current compact reader
	 * 
	 * @return the previous event, if possible.
	 */
	public EvioDataEvent getPreviousEvent() {
		
		int evNum = getEventNumber();
		_currentEvent = (EvioDataEvent) _evioSource.getPreviousEvent();
		
		//evioSource.getPreviousEvent() doesn't work at the end of the file
		//so hack
		if ((_currentEvent == null) && (evNum > 0)) {
			return gotoEvent(evNum-1);
		}
		
		notifyEventListeners();
		return _currentEvent;
	}

	/**
	 * 
	 * @param eventNumber a 1-based number 1..num events in file
	 * @return the event at the given number (if possible).
	 */
	public EvioDataEvent gotoEvent(int eventNumber) {
		_currentEvent = (EvioDataEvent) _evioSource.gotoEvent(eventNumber);
		notifyEventListeners();
		return _currentEvent;
	}

	/**
	 * Reload the current event
	 * 
	 * @return the same current event
	 */
	public EvioDataEvent reloadCurrentEvent() {
		if (_currentEvent != null) {
			notifyEventListeners();
		}
		return _currentEvent;
	}

	/**
	 * Notify listeners we have opened a new file
	 * 
	 * @param path the path to the new file
	 */
	private void notifyEventListeners(EventSourceType source) {

		Swimming.clearMCTrajectories();
		Swimming.clearReconTrajectories();
		
		if (_evioSource != null) {
			_evioSource.close();
			_currentEvent = null;
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
								.changedEventSource(source);
					}
				}
			}
		}

	}
	
	private void notifyEventListeners(String path) {

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
	
	public void notifyListenersFastMCGenEvent(PhysicsEvent event) {

		Swimming.clearMCTrajectories();
		Swimming.clearReconTrajectories();
		_uniqueLundIds = null;


		for (int index = 0; index < 3; index++) {
			if (_viewListenerList[index] != null) {
				// Guaranteed to return a non-null array
				Object[] listeners = _viewListenerList[index].getListenerList();

				// This weird loop is the bullet proof way of notifying all
				// listeners.
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == IClasIoEventListener.class) {
						((IClasIoEventListener) listeners[i + 1])
								.newFastMCGenEvent(event);;
					}
				}
			}
		} //index loop

		finalSteps();
	}



	/**
	 * Notify listeners we have a new event ready for display. All they may want
	 * is the notification that a new event has arrived. But the event itself is
	 * passed along.
	 * 
	 */
	private void notifyEventListeners() {

		Swimming.clearMCTrajectories();
		Swimming.clearReconTrajectories();
		_uniqueLundIds = null;

		_currentBanks = (_currentEvent == null) ? null
				: _currentEvent.getBankList();
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
		
		finalSteps();
	}
	
	private void finalSteps() {
		// some scaling factors for gradient displays
		computeSomeScalingFactors();

		// auto swim?
		if (SwimMenu.getInstance().isAlwaysSwimMC()) {
			SwimMenu.getInstance().firePropertyChange(SwimMenu.SWIM_ALL_MC_PROP, 0,
					1);
		}
		if (SwimMenu.getInstance().isAlwaysSwimRecon()) {
			SwimMenu.getInstance().firePropertyChange(SwimMenu.SWIM_ALL_RECON_PROP,
					0, 1);
		}


		Ced.setEventNumberLabel(getEventNumber());

	}

	// compute some factors used in gradient displays
	private void computeSomeScalingFactors() {


		double[] pcalEdep = PCAL.totEdep();
		double[] ecEdep = EC.totEdep();
		int stack[] = EC.stack();
		// pcal (plane = 0)
		if (pcalEdep != null) {
			maxEDepCal[0] = 0;
			for (double e : pcalEdep) {
				maxEDepCal[0] = Math.max(e, maxEDepCal[0]);
			}
		}

		// ec
		if ((ecEdep != null) && (stack != null)) {
			maxEDepCal[1] = 0;
			maxEDepCal[2] = 0;
			for (int i = 0; i < ecEdep.length; i++) {
				int plane = stack[i];
				maxEDepCal[plane] = Math.max(ecEdep[i], maxEDepCal[plane]);
			}
		}
	}

	/**
	 * Get the maximum energy deposited in the cal for the current event. Might
	 * be NaN if there are no "true" (gemc) banks
	 * 
	 * @param plane (0, 1, 2) for (PCAL, EC_INNER, EC_OUTER)
	 * @return the max energy deposited in that cal plane in MeV
	 */
	public double getMaxEdepCal(int plane) {
		return maxEDepCal[plane];
	}

	/**
	 * Remove a IClasIoEventListener. IClasIoEventListener listeners listen for
	 * new events.
	 * 
	 * @param listener the IClasIoEventListener listener to remove.
	 */
	public void removeClasIoEventListener(IClasIoEventListener listener) {

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
	 * Add a IClasIoEventListener. IClasIoEventListener listeners listen for new
	 * events.
	 * 
	 * @param listener the IClasIoEventListener listener to add.
	 * @param index Determines gross notification order. Those in index 0 are
	 *            notified first. Then those in index 1. Finally those in index
	 *            2. The Data containers should be in index 0. The trajectory
	 *            and noise in index 1, and the regular views in index 2 (they
	 *            are notified last)
	 */
	public void addClasIoEventListener(IClasIoEventListener listener,
			int index) {

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
	 * @param bankName the bank name
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
	 * @param bankName the bank name
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
