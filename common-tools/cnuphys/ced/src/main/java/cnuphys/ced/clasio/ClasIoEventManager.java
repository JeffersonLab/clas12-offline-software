package cnuphys.ced.clasio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;

import org.jlab.detector.decode.CLASDecoder4;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSource;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioETSource;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.utils.system.ClasUtilsFile;

import cnuphys.bCNU.application.Desktop;
import cnuphys.bCNU.dialog.DialogUtilities;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.IpField;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.bCNU.util.RingBuffer;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.alldata.graphics.DefinitionManager;
import cnuphys.ced.alldata.graphics.PlotDialog;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.filter.FilterManager;
import cnuphys.ced.clasio.filter.IEventFilter;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.ECAL;
import cnuphys.ced.event.data.PCAL;
import cnuphys.ced.frame.Ced;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.Solenoid;
import cnuphys.magfield.Torus;
import cnuphys.swim.SwimMenu;
import cnuphys.swim.Swimming;

public class ClasIoEventManager {

	// Unique lund ids in the event (if any)
	private Vector<LundId> _uniqueLundIds = new Vector<>();

	// listen for events even in accumulation mode
	private Vector<IClasIoEventListener> _specialListeners = new Vector<>();

	// A sorted list of banks present in the current event
	private String _currentBanks[];

	// used in pcal and ec hex gradient displays
	private double maxEDepCal[] = { Double.NaN, Double.NaN, Double.NaN };

	// Data from the special run bank
	private RunData _runData = new RunData();

	// for HIPO ring
	public IpField _ipField;

	// connect to ring
	public JButton _connectButton;

	// decode evio to hipo
	private CLASDecoder4 _decoder;
	private SchemaFactory _schemaFactory;

	// reset everytime hipo or evio file is opened
	private int _currentEventIndex;
	
	
	//a ringbuffer for previous events
	private RingBuffer<PrevIndexedEvent> _prevEventBuffer;
	
	//maps sequential event number to true event numbers
	private long[] _numberMap;
	private int _numberMapCount = 0;


	// sources of events (the type, not the actual source)
	public enum EventSourceType {
		HIPOFILE, ET, EVIOFILE
	}

	// for firing property changes
	public static final String SWIM_ALL_MC_PROP = "SWIM ALL MC";
	public static final String SWIM_ALL_RECON_PROP = "SWIM ALL RECON";

	// the current source type
	private EventSourceType _sourceType = EventSourceType.HIPOFILE;

	// ET dialog
	private ConnectETDialog _etDialog;

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

	// the current port
	private int _currentPort;

	// the current hipo event file
	private File _currentHipoFile;

	// the current evio event file
	private File _currentEvioFile;

	private String _currentMachine;
	private String _currentStation;

	// current ET file
	private String _currentETFile;

	// the clas_io source of events
	private DataSource _dataSource;

	// singleton
	private static ClasIoEventManager instance;

	// the current event
	private DataEvent _currentEvent;
	
	// the previous event and index
	private PrevIndexedEvent _prevIndexedEvent = new PrevIndexedEvent(null, -1);

	
	// private constructor for singleton
	private ClasIoEventManager() {
		_dataSource = new HipoDataSource();
		_prevEventBuffer = new RingBuffer<>(10);
	}

	/**
	 * Get the run data, changed every time a run bank is encountered
	 * 
	 * @return the run data
	 */
	public RunData getRunData() {
		return _runData;
	}

	/**
	 * Set the displayed event to the current event
	 */
	private void setCurrentEvent() {
		
		if (_currentEvent != null) {

			if (isAccumulating()) {
				AccumulationManager.getInstance().newClasIoEvent(_currentEvent);
				notifyAllDefinedPlots(_currentEvent);

				// notify special listeners
				// the get events even if we are accumulating
				// e.g., AddDCAccumView
				for (IClasIoEventListener listener : _specialListeners) {
					listener.newClasIoEvent(_currentEvent);
				}

			} else {
				_runData.set(_currentEvent);
				notifyEventListeners();
				// notifyAllDefinedPlots(event);
			}
		}
		
	}

	/**
	 * Get a collection of unique LundIds in the current event
	 * 
	 * @return a collection of unique LundIds
	 */
	public Vector<LundId> uniqueLundIds() {

		if (_uniqueLundIds != null) {
			return _uniqueLundIds;
		}

		_uniqueLundIds = new Vector<LundId>();

		if (_currentEvent != null) {
			// use any bank with a true pid column
			// String[] knownBanks =
			// ClasIoEventManager.getInstance().getKnownBanks();

			String[] cbanks = _currentEvent.getBankList();
			if (cbanks != null) {
				for (String bankName : cbanks) {
					if (bankName.contains("::true") || bankName.contains("::Particle")
							|| bankName.contains("::Lund")) {

						ColumnData cd = DataManager.getInstance().getColumnData(bankName, "pid");

						if (cd != null) {
							int pid[] = (cd.getIntArray(_currentEvent));
							if ((pid != null) && (pid.length > 0)) {
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
	 * Get the underlying clas-io data source
	 * 
	 * @return the DataSource object
	 */
	public DataSource getDataSource() {
		return _dataSource;
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
	 * Get the current event
	 * 
	 * @return the current event
	 */
	public DataEvent getCurrentEvent() {
		return _currentEvent;
	}

	/**
	 * Get a description of the current event source.
	 * @return a description of the current event source.
	 */
	public String getCurrentSourceDescription() {

		if ((_sourceType == EventSourceType.HIPOFILE) && (_currentHipoFile != null)) {
			return "Hipo " + _currentHipoFile.getName();
		} else if ((_sourceType == EventSourceType.EVIOFILE) && (_currentEvioFile != null)) {
			return "Evio " + _currentEvioFile.getName();
		}
		else if ((_sourceType == EventSourceType.ET) && (_currentMachine != null) && (_currentETFile != null)) {
			return "ET " + _currentMachine + " " + _currentETFile;
		}
		return "(none)";
	}

	/**
	 * Open an event file
	 * 
	 * @param file the event file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void openHipoEventFile(File file) throws FileNotFoundException, IOException {

		if (!file.exists()) {
			throw (new FileNotFoundException("Event event file not found"));
		}
		if (!file.canRead()) {
			throw (new FileNotFoundException("Event file cannot be read"));
		}

		_currentHipoFile = file;

		_dataSource = new HipoDataSource();
		_dataSource.open(file.getPath());
		notifyEventListeners(_currentHipoFile);
		setEventSourceType(EventSourceType.HIPOFILE);

		_runData.reset();
		_currentEvent = null;
		_prevIndexedEvent.reset();
		_currentEventIndex = 0;
		_prevEventBuffer.clear();
		resetIndexMap();
		
		// TODO check if I need to skip the first event

		try {
			getNextEvent();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reset the index map
	 */
	public void resetIndexMap() {
		_numberMap = null;
		_numberMapCount = 0;
	}

	/**
	 * Open an evio event file
	 * 
	 * @param file the event file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void openEvioEventFile(File file) throws FileNotFoundException, IOException {

		if (!file.exists()) {
			throw (new FileNotFoundException("Event event file not found"));
		}
		if (!file.canRead()) {
			throw (new FileNotFoundException("Event file cannot be read"));
		}

		_currentEvioFile = file;

		_dataSource = new EvioSource();
		_dataSource.open(file.getPath());
		notifyEventListeners(_currentEvioFile);
		setEventSourceType(EventSourceType.EVIOFILE);

		_runData.reset();
		_currentEvent = null;
		_prevIndexedEvent.reset();
		_currentEventIndex = 0;
		_prevEventBuffer.clear();
		resetIndexMap();

		// TODO check if I need to skip the first event

		try {
			getNextEvent();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Connect to an ET ring
	 */
	public void ConnectToETRing() {

		if (_etDialog == null) {
			_etDialog = new ConnectETDialog();
		}
		_etDialog.setVisible(true);

		if (_etDialog.reason() == DialogUtilities.OK_RESPONSE) {
			_runData.reset();
			_currentEvent = null;
			_prevIndexedEvent.reset();
			_currentEventIndex = 0;
			_prevEventBuffer.clear();
			resetIndexMap();

			_dataSource = null;
			_currentMachine = _etDialog.getMachine();
			_currentETFile = _etDialog.getFile();
			_currentStation = _etDialog.getStation();
			_currentPort = _etDialog.getPort();

			// does the file exist?

			Log.getInstance().info("Attempting to connect to ET ring");
			Log.getInstance().info("ET Filename: [" + _currentETFile + "]");
			Log.getInstance().info("ET Station Name: [" + _currentStation + "]");
			System.err.println("ET File Name:_currentETFile [" + _currentETFile + "]");

			try {
				Log.getInstance().info("Attempting to create EvioETSource.");

				_dataSource = new EvioETSource(_currentMachine, _currentPort, _currentStation);

				if (_dataSource == null) {
					Log.getInstance().error("null EvioETSource.  Cannot connect to ET.");
					JOptionPane.showMessageDialog(null, "The ET Data Source is null, used Machine: " + _currentMachine,
							"ET null Data Source", JOptionPane.INFORMATION_MESSAGE, ImageManager.cnuIcon);
					return;
				}

				System.err.println("trying to connect using et file: " + _currentETFile);
				setEventSourceType(EventSourceType.ET);
				Log.getInstance().info("Attempting to open EvioETSource.");
				_dataSource.open(_currentETFile);
			} catch (Exception e) {
				String message = "Could not connect to ET Ring [" + e.getMessage() + "]";
				Log.getInstance().error(message);
			}

		} // end ok

	}


	/**
	 * Get the current event source type
	 * 
	 * @return the current event source type
	 */
	public EventSourceType getEventSourceType() {
		return _sourceType;
	}

	/**
	 * Set the soure type
	 * 
	 * @param type the new source type
	 */
	public void setEventSourceType(EventSourceType type) {
		if (_sourceType != type) {
			_sourceType = type;
			notifyEventListeners(_sourceType);
		}
		Ced.getCed().fixEventCount();
	}

	/**
	 * Check whether current event source type is a hipo file
	 * 
	 * @return <code>true</code> is source type is a hipo file.
	 */
	public boolean isSourceHipoFile() {
		return getEventSourceType() == EventSourceType.HIPOFILE;
	}

	/**
	 * Check whether current event source type is an evio file
	 * 
	 * @return <code>true</code> is source type is an evio file.
	 */
	public boolean isSourceEvioFile() {
		return getEventSourceType() == EventSourceType.EVIOFILE;
	}

	/**
	 * Check whether current event source type is the ET ring
	 * 
	 * @return <code>true</code> is source type is the ET ring.
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
		if (isSourceHipoFile()) {
			evcount = (_dataSource == null) ? 0 : _dataSource.getSize();
		} else if (isSourceEvioFile()) {
			evcount = (_dataSource == null) ? 0 : _dataSource.getSize();
		}
		else if (isSourceET()) {
			return Integer.MAX_VALUE;
		}
		return evcount;
	}

	/**
	 * Get the sequential number of the current event, 0 if there is none
	 * 
	 * @return the sequential number of the current event.
	 */
	public int getSequentialEventNumber() {
		return _currentEventIndex;
	}
	
	/**
	 * Get the true event number of the current event, 1 if there is none.
	 * The value comes from the RUN::config bank
	 * 
	 * @return the true number of the current event.
	 */
	public int getTrueEventNumber() {
		if (_currentEvent != null) {
			DataBank db = _currentEvent.getBank("RUN::config");
			if (db != null) {
				int[] ia = db.getInt("event");
				if ((ia != null) && (ia.length > 0)) {
					return ia[0];
				}
			}
		}
		return -1;
	}


	/**
	 * Determines whether any next event control should be enabled.
	 * 
	 * @return <code>true</code> if any next event control should be enabled.
	 */
	public boolean isNextOK() {

		boolean isOK = true;
		EventSourceType estype = getEventSourceType();

		switch (estype) {
		case HIPOFILE:
			isOK = (isSourceHipoFile() && (getEventCount() > 0) && (getSequentialEventNumber() < getEventCount()));
			break;
		case EVIOFILE:
			isOK = (isSourceEvioFile() && (getEventCount() > 0) && (getSequentialEventNumber() < getEventCount()));
			break;
		case ET:
			isOK = true;
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
		EventSourceType estype = getEventSourceType();

		switch (estype) {
		case HIPOFILE:
		case EVIOFILE:
			numRemaining = getEventCount() - getSequentialEventNumber();
			break;
		case ET:
			numRemaining = Integer.MAX_VALUE;
		}

		return numRemaining;
	}

	/**
	 * Determines whether any prev event control should be enabled.
	 * 
	 * @return <code>true</code> if any prev event control should be enabled.
	 */
	public boolean isPrevOK() {
		return (_prevEventBuffer.size() > 0);
	}

	/**
	 * Determines whether any goto event control should be enabled.
	 * 
	 * @return <code>true</code> if any prev event control should be enabled.
	 */
	public boolean isGotoOK() {
		return (isSourceHipoFile() || isSourceEvioFile()) && (getEventCount() > 0);
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

	// During accumulation, notify any plots
	// define through the Define menu
	protected void notifyAllDefinedPlots(DataEvent event) {
		if (isAccumulating() && (event != null)) {
			Vector<PlotDialog> plots = DefinitionManager.getInstance().getAllPlots();

			if ((plots != null) && !plots.isEmpty()) {
				for (PlotDialog plot : plots) {
					if (plot != null) {
						plot.newClasIoEvent(event);
					}
				}
			}
		}
	}

	// decode an evio event to hipo
	private HipoDataEvent decodeEvioToHipo(EvioDataEvent event) {
				
		if (_decoder == null) {
	        _schemaFactory  =  new SchemaFactory();
	        
	        String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
	        _schemaFactory.initFromDirectory(dir);
	        
			_decoder = new CLASDecoder4();
		}

		Event decodedEvent = _decoder.getDataEvent(event);
		
		Bank trigger = _decoder.createTriggerBank();
		
        if(trigger != null) {
        	decodedEvent.write(trigger);
        }
        
        //best I can do since I don't have the actual
        //values from the file
        
        Torus torus = MagneticFields.getInstance().getTorus();
        Solenoid solenoid = MagneticFields.getInstance().getSolenoid();
        
        double tScale = (torus == null) ? -1 : torus.getScaleFactor();
        double sScale = (solenoid == null) ? 1 : solenoid.getScaleFactor();
        
        Bank header= _decoder.createHeaderBank(-1, 0, (float)tScale, (float)sScale);
        if(header != null) {
        	decodedEvent.write(header);
        }
        
        return new HipoDataEvent(decodedEvent, _schemaFactory);
	}

	/**
	 * Get the next event from the current  reader
	 * @return the next event, if possible
	 */
	public DataEvent getNextEvent() {
		return getNextEvent(true);
	}

	/**
	 * Get the next event from the current  reader
	 * @return the next event, if possible
	 */
	private DataEvent getNextEvent(boolean addToBuffer) {

		EventSourceType estype = getEventSourceType();
		boolean done = false; //for filters 

		if (addToBuffer) {
			_prevIndexedEvent.set(_currentEvent, _currentEventIndex);
		}

		switch (estype) {

		case HIPOFILE:
		case EVIOFILE:

			while (!done) {

				if (_dataSource.hasEvent()) {
					_currentEvent = _dataSource.getNextEvent();
				} else {
					_currentEvent = null;
				}

				if ((_currentEvent != null) && (_currentEvent instanceof EvioDataEvent)) {
					_currentEvent = decodeEvioToHipo((EvioDataEvent) _currentEvent);
				}

				done = (_currentEvent == null) || FilterManager.getInstance().pass(_currentEvent);
			}
			if (_currentEvent != null) {
				_currentEventIndex++;
			}

			break;

		case ET:
			int maxTries = 30;
			int attempts = 0;

			_dataSource.waitForEvents();
			while ((attempts < maxTries) && !_dataSource.hasEvent()) {
				try {
					attempts++;
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				_dataSource.waitForEvents();
			}

			_currentEvent = null;

			if (_dataSource.hasEvent()) {

				_currentEvent = _dataSource.getNextEvent();

				if ((_currentEvent != null) && (_currentEvent instanceof EvioDataEvent)) {
					_currentEvent = decodeEvioToHipo((EvioDataEvent) _currentEvent);
				}

				if (FilterManager.getInstance().pass(_currentEvent)) {
					_currentEventIndex++;
				} else {
					_currentEvent = null;
				}

				break;
			} 

			break; // end case ET

		} // end switch

		if (addToBuffer) {
			if ((_prevIndexedEvent.event != null) && (_prevIndexedEvent.event != _currentEvent)) {
				_prevEventBuffer.add(new PrevIndexedEvent(_prevIndexedEvent.event, _prevIndexedEvent.index));
			}
		}

		setCurrentEvent();
		return _currentEvent;
	}


	/**
	 * See if another event is available
	 * 
	 * @return <code>true</code> if another event is available
	 */
	public boolean hasEvent() {
		EventSourceType estype = getEventSourceType();
		switch (estype) {
		case HIPOFILE:
		case ET:
		case EVIOFILE:
			boolean hasETEvent = ((_dataSource != null) && _dataSource.hasEvent());
			return hasETEvent;
		default:
			return true;
		}
	}

	/**
	 * Get the previous event from the current compact reader
	 * 
	 * @return the previous event, if possible.
	 */
	public DataEvent getPreviousEvent() {
		
		PrevIndexedEvent prev = _prevEventBuffer.previous();
		if ((prev == null) || (prev.event == _currentEvent)) {
			return _currentEvent;
		}
		
		_currentEvent = prev.event;
		_currentEventIndex = prev.index;
		
		setCurrentEvent();
		return _currentEvent;
	}

	// skip a number of events
	private void skipEvents(int n) {
		if (n < 1) {
			return;
		}

		EventSourceType estype = getEventSourceType();
		
		if (_currentEvent != null) {
			_prevEventBuffer.add(new PrevIndexedEvent(_currentEvent, _currentEventIndex));
		}


		switch (estype) {
		case HIPOFILE:
		case EVIOFILE:
			int numRemaining = getNumRemainingEvents();
			n = Math.min(numRemaining, n);

			int stopIndex = _currentEventIndex + n;
			boolean done = false;
			
			while (!done && (_currentEventIndex < stopIndex)) {
				if (_dataSource.hasEvent()) {
					DataEvent event = _dataSource.getNextEvent();
					if (FilterManager.getInstance().pass(event)) {
						_currentEventIndex++;
					}
				}
				else {
					done = true;
				}
			}

			break;

		case ET:
			break;
		}
	}
	
	/**
	 * Go t the event with the desired true event number
	 * @param desesiredTrueNumber the desired true event number
	 * @return the event
	 */
	public DataEvent gotoTrueEvent(int desiredTrueNumber) {		
		
		int currentTrueEvent = getTrueEventNumber();
		if (currentTrueEvent < 0) {
			return _currentEvent;
		}

		EventSourceType estype = getEventSourceType();
		switch (estype) {

		case HIPOFILE:
			break;

		case EVIOFILE: // assume the event numbers are sequential
			return gotoEvent(_currentEventIndex + (desiredTrueNumber - currentTrueEvent));

		default:
			return _currentEvent;
		}
		
		
		// only hipo

		if (_numberMap == null) {
			runThroughEvents(desiredTrueNumber);
		} else {

			int saveIndex = _currentEventIndex;
			int seqIndex = getSequentialFromTrue(desiredTrueNumber);

			if (seqIndex >= 0) {
				gotoEvent(seqIndex);
			} else {
				gotoEvent(saveIndex);
			}
		}

		return _currentEvent;
	}

	/**
	 * 
	 * @param eventNumber a 1-based number 1..num events in file
	 * @return the event at the given number (if possible).
	 */
	public DataEvent gotoEvent(int eventNumber) {
		
		if ((eventNumber < 1) || (eventNumber == _currentEventIndex) || (eventNumber > getEventCount())) {
			return _currentEvent;
		}

		EventSourceType estype = getEventSourceType();
		switch (estype) {

		case HIPOFILE:
			if (eventNumber > _currentEventIndex) {
				int numToSkip = (eventNumber - _currentEventIndex) - 1;
				skipEvents(numToSkip);
				getNextEvent(false);
			} else {
				_dataSource.close();
				_currentEvent = null;
				_currentEventIndex = 0;
				_dataSource.open(_currentHipoFile);
				gotoEvent(eventNumber);
			}

			break;

		case EVIOFILE:
			_currentEvent = _dataSource.gotoEvent(eventNumber);
			if ((_currentEvent != null) && (_currentEvent instanceof EvioDataEvent)) {		
				_currentEvent = decodeEvioToHipo((EvioDataEvent)_currentEvent);
				_currentEventIndex = eventNumber;
			}
			break;
			
			default:
				break;
		}


		setCurrentEvent();
		return _currentEvent;
	}

	/**
	 * Reload the current event
	 * 
	 * @return the same current event
	 */
	public DataEvent reloadCurrentEvent() {

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

		Swimming.clearAllTrajectories();

		if (_dataSource != null) {
			_dataSource.close();
			_currentEvent = null;
			_currentEventIndex = 0;
			_prevEventBuffer.clear();
		}

		for (int index = 0; index < 3; index++) {
			if (_viewListenerList[index] != null) {
				// Guaranteed to return a non-null array
				Object[] listeners = _viewListenerList[index].getListenerList();

				// This weird loop is the bullet proof way of notifying all
				// listeners.
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == IClasIoEventListener.class) {
						((IClasIoEventListener) listeners[i + 1]).changedEventSource(source);
					}
				}
			}
		}

		Ced.getCed().fixTitle();
	}

	// new event file notification
	private void notifyEventListeners(File file) {

		Swimming.clearAllTrajectories();

		for (int index = 0; index < 3; index++) {
			if (_viewListenerList[index] != null) {
				// Guaranteed to return a non-null array
				Object[] listeners = _viewListenerList[index].getListenerList();

				// This weird loop is the bullet proof way of notifying all
				// listeners.
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == IClasIoEventListener.class) {
						((IClasIoEventListener) listeners[i + 1]).openedNewEventFile(file.getAbsolutePath());
					}
				}
			}
		}
		Ced.getCed().fixTitle();

	}


	/**
	 * Notify listeners we have a new event ready for display. All they may want is
	 * the notification that a new event has arrived. But the event itself is passed
	 * along.
	 */
	protected void notifyEventListeners() {

		Swimming.setNotifyOn(false); // prevent refreshes
		Swimming.clearAllTrajectories();
		Swimming.setNotifyOn(true); // prevent refreshes

		_uniqueLundIds = null;

		Ced.getCed().setEventFilteringLabel(FilterManager.getInstance().isFilteringOn());
						
		_currentBanks = (_currentEvent == null) ? null : _currentEvent.getBankList();
		
		
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
					IClasIoEventListener listener = (IClasIoEventListener) listeners[i + 1];
					if (listeners[i] == IClasIoEventListener.class) {
						boolean notify = true;
						if (this.isAccumulating()) {
							notify = !listener.ignoreIfAccumulating();
						}

						if (notify) {
							listener.newClasIoEvent(_currentEvent);
						}
					}
				}
			}

		} // index loop

		finalSteps();
	}

	// final steps
	private void finalSteps() {
		if (isAccumulating()) {
			return;
		}

		// some scaling factors for gradient displays
		computeSomeScalingFactors();

		SwimMenu.getInstance().firePropertyChange(SWIM_ALL_MC_PROP, 0, 1);
		SwimMenu.getInstance().firePropertyChange(SWIM_ALL_RECON_PROP, 0, 1);

		Ced.setEventNumberLabel(getSequentialEventNumber());

		for (JInternalFrame jif : Desktop.getInstance().getAllFrames()) {
			if (jif instanceof CedView) {
				((CedView) jif).getContainer().redoFeedback();
			}
		}

	}

	// compute some factors used in gradient displays
	private void computeSomeScalingFactors() {

		double[] pcalEdep = PCAL.totEdep();
		double[] ecEdep = ECAL.totEdep();
		int stack[] = ECAL.stack();
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
	 * Get the maximum energy deposited in the cal for the current event. Might be
	 * NaN if there are no "true" (gemc) banks
	 * 
	 * @param plane (0, 1, 2) for (PCAL, EC_INNER, EC_OUTER)
	 * @return the max energy deposited in that cal plane in MeV
	 */
	public double getMaxEdepCal(int plane) {
		return maxEDepCal[plane];
	}

	/**
	 * Remove a IClasIoEventListener. IClasIoEventListener listeners listen for new
	 * events.
	 * 
	 * @param listener the IClasIoEventListener listener to remove.
	 */
	public void removeClasIoEventListener(IClasIoEventListener listener) {

		if (listener == null) {
			return;
		}

		for (int i = 0; i < 3; i++) {
			if (_viewListenerList[i] != null) {
				_viewListenerList[i].remove(IClasIoEventListener.class, listener);
			}
		}
	}

	/**
	 * Add a IClasIoEventListener. IClasIoEventListener listeners listen for new
	 * events.
	 * 
	 * @param listener the IClasIoEventListener listener to add.
	 * @param index    Determines gross notification order. Those in index 0 are
	 *                 notified first. Then those in index 1. Finally those in index
	 *                 2. The Data containers should be in index 0. The trajectory
	 *                 and noise in index 1, and the regular views in index 2 (they
	 *                 are notified last)
	 */
	public void addClasIoEventListener(IClasIoEventListener listener, int index) {

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
	 * Checks if a bank, identified by a string such as "FTOF1B::dgtz", is in the
	 * current event.
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
	 * Get a sorted list of known banks from the dictionary
	 * 
	 * @return a sorted list of known banks
	 */
	public String[] getKnownBanks() {
		return DataManager.getInstance().getKnownBanks();
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


	/**
	 * Add a special listener that gets events even if we are accumulating. Example:
	 * AllDCAccumView
	 * 
	 * @param listener the event listener
	 */
	public void addSpecialEventListener(IClasIoEventListener listener) {
		_specialListeners.remove(listener);
		_specialListeners.add(listener);
	}

	
	/**
	 * Minimal event getter for running through events as fast as possible
	 * @return the next event
	 */
	public DataEvent bareNextEvent() {

		EventSourceType estype = getEventSourceType();
		
		boolean done = false;

		switch (estype) {

		case HIPOFILE:
		case EVIOFILE:

			while (!done) {
				if (_currentEventIndex >= getEventCount()) {
					return null;
				}

				try {
					if (_dataSource.hasEvent()) {
						_currentEvent = _dataSource.getNextEvent();
					} else {
						_currentEvent = null;
					}
				} catch (Exception e) {
					System.err.println("Exception in bareNextEvent: " + e.getMessage());
					_currentEvent = null;
				}

				done = (_currentEvent == null) || FilterManager.getInstance().pass(_currentEvent);

				if ((_currentEvent != null) && (_currentEvent instanceof EvioDataEvent)) {
					_currentEvent = decodeEvioToHipo((EvioDataEvent) _currentEvent);
				}

				if (_currentEvent != null) {
					_currentEventIndex++;
				}

				else {
					return null;
				}
			}
			break;

		case ET:
			break;
		}
		
		
		return _currentEvent;
	}
	
	//the sequential index is in the lower 32 bits
	private int getSequentialFromTrue(int trueIndex) {
		int index =  Arrays.binarySearch(_numberMap, 0, _numberMapCount-1, ((long)trueIndex << 32));
		
		if (index < 0) {
			index = -(index + 1); // now the insertion point.
		}
		
		int seqIndex = 1;
		try {
			seqIndex = (int)(_numberMap[index] & 0xffffffffL);
		}
		catch (Exception e ) {
			System.err.println("Problem in getSequentialFromTrue: " + e.getMessage());
			System.err.println("Requested true index: " + trueIndex);
			System.err.println("Index from binary search: " + index);
			System.err.println("Length of map array: " + _numberMap.length);
		}
		
		return seqIndex;
	}
	
	//run through the hipo file to make a mapping of sequential to true
	private void runThroughEvents(int gotoIndex) {
		
		int saveIndex = _currentEventIndex;

		EventSourceType estype = getEventSourceType();
		switch (estype) {

		case HIPOFILE: 
			gotoEvent(0);
			
			int count = getEventCount();

			if (count > 0) {
				_numberMap = new long[count];
			} else {
				_numberMap = null;
			}
			break;
			
		case EVIOFILE:
			gotoEvent(0);
			return;

			
			default:
				return;
		}
		
		int trueNum = getTrueEventNumber();
		_numberMap[0] = ((long)trueNum << 32) + 1;

		IRunThrough rthrough = new IRunThrough() {

			int index = 1;

			@Override
			public void nextRunthroughEvent(DataEvent event) {
				if ((event != null) && FilterManager.getInstance().pass(event)) {
					try {
						int trueNum = getTrueEventNumber();
						_numberMap[index] = ((long)trueNum << 32) + index + 1;
						_numberMapCount++;
						index++;
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				else {
					System.err.println("null event in nextRunthroughEvent");
				}

			}

			@Override
			public void runThroughtDone() {		
				
				
				int num = 50;
				
				System.err.println("Before sort");
				for (int i = 0; i < num; i++) {
					long v = _numberMap[i];
					System.err.println("Seq: [" + (v & 0xffffffffL) + "]  true: " + (v >> 32));
				}
				
				
				Arrays.sort(_numberMap, 0, _numberMapCount-1);
				
				System.err.println("\nAfter sort");
				for (int i = 0; i < num; i++) {
					long v = _numberMap[i];
					System.err.println("Seq: [" + (v & 0xffffffffL) + "]  true: " + (v >> 32));
				}

				
				int seqIndex = getSequentialFromTrue(gotoIndex);

				
				if (seqIndex >= 0) {
					gotoEvent(seqIndex);
				} else {
					gotoEvent(saveIndex);
				}

			}
			
		};
		
		RunThroughDialog dialog = new RunThroughDialog(rthrough);
		dialog.setVisible(true);
		dialog.process();

	}
	
	
	//inner class for storing previous events in a ring buffer
	class PrevIndexedEvent {
		public DataEvent event;
		public int index;
		
		public PrevIndexedEvent(DataEvent event, int index) {
			set(event, index);
		}
		
		public void set(DataEvent event, int index) {
			this.event = event;
			this.index = index;			
		}
		
		public void reset() {
			set(null, -1);
		}
	}

	
	
}


