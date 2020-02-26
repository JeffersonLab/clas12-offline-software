package cnuphys.ced.clasio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;

import org.jlab.detector.decode.CLASDecoder;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSource;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioETSource;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataBank;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSource;

import cnuphys.bCNU.application.Desktop;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.IpField;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.alldata.graphics.DefinitionManager;
import cnuphys.ced.alldata.graphics.PlotDialog;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.ECAL;
import cnuphys.ced.event.data.PCAL;
import cnuphys.ced.frame.Ced;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.swim.SwimMenu;
import cnuphys.swim.Swimming;

public class ClasIoEventManager {

	// Unique lund ids in the event (if any)
	private Vector<LundId> _uniqueLundIds = new Vector<LundId>();

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
	private CLASDecoder _decoder;

	// reset everytime hipo or evio file is opened
	private int _eventIndex;

	// all the filters
	private ArrayList<IEventFilter> _eventFilters = new ArrayList<>();

	// sources of events (the type, not the actual source)
	public enum EventSourceType {
		// HIPOFILE, HIPORING, ET, EVIOFILE
		HIPOFILE, ET, EVIOFILE
	}

	// for firing property changes
	public static final String SWIM_ALL_MC_PROP = "SWIM ALL MC";
	public static final String SWIM_ALL_RECON_PROP = "SWIM ALL RECON";

	// the current source type
	private EventSourceType _sourceType = EventSourceType.HIPOFILE;

	// ET dialog
	// private ETDialog _etDialog;
	// private ConnectionDialog _connectionDialog;

	private ConnectETDialog _etDialog;

	// hipo ring dialog
	// private RingDialog _ringDialog;
	// private ConnectionDialogHipo _hipoDialog;

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
	
	//the current port
	private int _currentPort;

	// the current hipo event file
	private File _currentHipoFile;

	// the current evio event file
	private File _currentEvioFile;

	// current ip address of HIPO ring
	// private String _currentHIPOAddress;

	// current ip address of ET ring
	// private String _currentETAddress;
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

	// private constructor for singleton
	private ClasIoEventManager() {
		_dataSource = new HipoDataSource();

		// test trigger filter
		// TriggerFilter testFilter = new TriggerFilter.Builder()
		// .setType(TriggerFilter.TRIG_FILT_TYPE.EXACT)
		// .setBits(17)
		// .setActive(true)
		// .setName("Test Trigger")
		// .build();
		//
		// this.addEventFilter(testFilter);

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
	 * Set the next event (after a getNextEvent)
	 * 
	 * @param event
	 *            the new event
	 */
	protected void setNextEvent(DataEvent event) {
		_currentEvent = event;

		if (event != null) {
			// _runData.set(_currentEvent);

			if (isAccumulating()) {
				AccumulationManager.getInstance().newClasIoEvent(event);
				notifyAllDefinedPlots(event);
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
					if (bankName.contains("::true") || (bankName.equals("MC::Particle"))) {

						// boolean hasBank = _currentEvent.hasBank(bankName);
						//
						//
						// boolean hasData = (DataManager.getInstance().hasData(_currentEvent, bankName)
						// != null);
						// System.out.println("****** BANK NAME [" + bankName + "] + hasData: " +
						// hasData + " hasBank: " + hasBank);

						ColumnData cd = DataManager.getInstance().getColumnData(bankName, "pid");

						// System.out.println("****** pid column null: " + (cd == null));

						if (cd != null) {
							int pid[] = (int[]) (cd.getDataArray(_currentEvent));
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
	 * Get the HIPO decoder
	 * 
	 * @return the decoder
	 */
	public CLASDecoder getDecoder() {

		if (_decoder == null) {
			_decoder = new CLASDecoder();
		}

		return _decoder;
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

	// /**
	// * Get the current event file
	// *
	// * @return the current file
	// */
	// public File getCurrentEventFile() {
	// return _currentEventFile;
	// }

	/**
	 * Get the current event
	 * 
	 * @return the current event
	 */
	public DataEvent getCurrentEvent() {
		return _currentEvent;
	}

	// /**
	// * Get the path of the current file
	// *
	// * @return the path of the current file
	// */
	// public String getCurrentEventFilePath() {
	// return (_currentEventFile == null) ? "(none)" :
	// _currentEventFile.getPath();
	// }

	public String getCurrentSourceDescription() {

		if ((_sourceType == EventSourceType.HIPOFILE) && (_currentHipoFile != null)) {
			return "Hipo " + _currentHipoFile.getName();
		} else if ((_sourceType == EventSourceType.EVIOFILE) && (_currentEvioFile != null)) {
			return "Evio " + _currentEvioFile.getName();
		}
		// else if ((_sourceType == EventSourceType.HIPORING) && (_currentHIPOAddress !=
		// null)) {
		// return "Hipo Ring " + _currentHIPOAddress;
		// }
		else if ((_sourceType == EventSourceType.ET) && (_currentMachine != null) && (_currentETFile != null)) {
			return "ET " + _currentMachine + " " + _currentETFile;
		}
		return "(none)";
	}

	/**
	 * Open an event file
	 * 
	 * @param file
	 *            the event file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void openHipoEventFile(File file) throws FileNotFoundException, IOException {

		System.err.println("opening hipo file " + file.getPath());

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
		_eventIndex = 0;

		// TODO check if I need to skip the first event

		try {
			getNextEvent();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open an evio event file
	 * 
	 * @param file
	 *            the event file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void openEvioEventFile(File file) throws FileNotFoundException, IOException {

		System.err.println("opening evio file " + file.getPath());

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
		_eventIndex = 0;

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
			_eventIndex = 0;

			_dataSource = null;
			_currentMachine = _etDialog.getMachine();
			_currentETFile = _etDialog.getFile();
			_currentStation = _etDialog.getStation();
			_currentPort = _etDialog.getPort();
			
	//		System.err.println("CURRENT PORT: " + _currentPort);

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
	 * Connect to a HIPO ring
	 */
	// public void ConnectToHipoRing() {
	// if (_hipoDialog == null) {
	// _hipoDialog = new ConnectionDialogHipo();
	// _hipoDialog.setTitle("Connect to Hipo Ring");
	// // _hipoDialog.setIconImage(ImageManager.cnuIcon.getImage());
	// _hipoDialog.setIconImage(ImageManager.getInstance().loadImageIcon("images/hipo2.png").getImage());
	// }
	// _hipoDialog.setVisible(true);
	// if (_hipoDialog.reason() == DialogUtilities.OK_RESPONSE) {
	// _runData.reset();
	// _currentEvent = null;
	// _eventIndex = 0;
	//
	// _dataSource = null;
	// _currentHIPOAddress = "";
	// int connType = _hipoDialog.getConnectionType();
	//
	// // let's try to connect
	// try {
	// if (connType == RingDialog.CONNECTSPECIFIC) {
	// _dataSource = new HipoRingSource();
	// _currentHIPOAddress = _hipoDialog.getIpAddress();
	// _dataSource.open(_currentHIPOAddress);
	// } else if (connType == RingDialog.CONNECTDAQ) {
	// _dataSource = HipoRingSource.createSourceDaq();
	// _currentHIPOAddress = " DAQ ";
	// }
	// } catch (Exception e) {
	// _dataSource = null;
	// _currentHIPOAddress = "";
	// Log.getInstance().warning(e.getMessage());
	// }
	//
	// if (_dataSource != null) {
	// setEventSourceType(EventSourceType.HIPORING);
	// try {
	// getNextEvent();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// } //end ok
	// }

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
	 * @param type
	 *            the new source type
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
	 * Check whether current event source type is the hippo ring
	 * 
	 * @return <code>true</code> is source type is the hippo ring.
	 */
	// public boolean isSourceHipoRing() {
	// return getEventSourceType() == EventSourceType.HIPORING;
	// }

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
		// else if (isSourceHipoRing()) {
		// return Integer.MAX_VALUE;
		// }
		else if (isSourceET()) {
			return Integer.MAX_VALUE;
		}
		return evcount;
	}

	/**
	 * Get the number of the current event, 0 if there is none
	 * 
	 * @return the number of the current event.
	 */
	public int getEventNumber() {
		return _eventIndex;
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
			isOK = (isSourceHipoFile() && (getEventCount() > 0) && (getEventNumber() < getEventCount()));
			break;
		case EVIOFILE:
			isOK = (isSourceEvioFile() && (getEventCount() > 0) && (getEventNumber() < getEventCount()));
			break;
		// case HIPORING:
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
			numRemaining = getEventCount() - getEventNumber();
			break;
		// case HIPORING:
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
		return (isSourceHipoFile() || isSourceEvioFile()) && (_eventIndex > 1);
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

	/**
	 * Get the next event from the current compact reader
	 * 
	 * @return the next event, if possible
	 */
	public DataEvent getNextEvent() {

		EventSourceType estype = getEventSourceType();

		// System.err.println("ET DEBUG: in getNextEvent estype: " + estype);

		switch (estype) {
		// case HIPORING:
		// if (_dataSource.hasEvent()) {
		// _currentEvent = _dataSource.getNextEvent();
		//
		// // look for the run bank
		// _eventIndex++;
		// ifPassSetEvent(_currentEvent, 0);
		// }
		// break;
		case HIPOFILE:
			if (_dataSource.hasEvent()) {
				_currentEvent = _dataSource.getNextEvent();
				_eventIndex++;
				ifPassSetEvent(_currentEvent, 0);
			}
			break;

		case EVIOFILE:
			if (_dataSource.hasEvent()) {
				_currentEvent = _dataSource.getNextEvent();

				if ((_currentEvent != null) && (_currentEvent instanceof EvioDataEvent)) {
					_currentEvent = getDecoder().getDataEvent(_currentEvent);
					HipoDataBank trigger = _decoder.createTriggerBank(_currentEvent);
					_currentEvent.appendBanks(trigger);

					// System.err.println("Decoded to HIPO");
					// _currentEvent.show();
					_eventIndex++;
					ifPassSetEvent(_currentEvent, 0);
				}
			}

			break; // end case eviofile

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
			// boolean goodEvent = false;

			if (_dataSource.hasEvent()) {

				_currentEvent = _dataSource.getNextEvent();

				if ((_currentEvent != null) && (_currentEvent instanceof EvioDataEvent)) {

					// decode the event from evio to hipo
					_currentEvent = getDecoder().getDataEvent(_currentEvent);

					// manually create trigger bank
					HipoDataBank trigger = _decoder.createTriggerBank(_currentEvent);
					_currentEvent.appendBanks(trigger);

					_eventIndex++;
					ifPassSetEvent(_currentEvent, 0);
					break;
				} else {
					_currentEvent = null;
				}

			}

			break; // end case ET

		} // end switch

		return _currentEvent;
	}

	// set the event only if it passes filtering
	private void ifPassSetEvent(DataEvent event, int option) {
		if (event != null) {
			if (passFilters(event)) {
				setNextEvent(event);
			} else {
				if (option == 0) {
					getNextEvent();
				} else if (option == 1) {
					if (_eventIndex == 0) {
						setNextEvent(event);
					} else {
						getPreviousEvent();
					}
				}
			}
		}

	}

	/**
	 * Get the bytes for serialization
	 * 
	 * @param dataEvent
	 *            the dataEvent
	 * @return bytes for serialization
	 */
	public byte[] getEventBytesForSerializing(DataEvent dataEvent) {

		if (dataEvent != null) {
			if (dataEvent instanceof HipoDataEvent) {
				ByteBuffer bb = _currentEvent.getEventBuffer();
				return bb.array();
			}
		}

		return null;
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
			// case HIPORING:
		case ET:
		case EVIOFILE:
			boolean hasETEvent = ((_dataSource != null) && _dataSource.hasEvent());
			// System.err.println("ET DEBUG: has event: " + hasETEvent);
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

		EventSourceType estype = getEventSourceType();

		switch (estype) {
		// case HIPORING:
		// _eventIndex--;
		// break;
		case HIPOFILE:
			_eventIndex--;
			System.err.println("EVENT INDEX: " + _eventIndex);
			_currentEvent = _dataSource.getPreviousEvent();
			break;

		case EVIOFILE:
			_currentEvent = _dataSource.getPreviousEvent();
			if ((_currentEvent != null) && (_currentEvent instanceof EvioDataEvent)) {

				if (_decoder == null) {
					_decoder = new CLASDecoder();
				}
				_eventIndex--;
				_currentEvent = _decoder.getDataEvent(_currentEvent);

				HipoDataBank trigger = _decoder.createTriggerBank(_currentEvent);
				_currentEvent.appendBanks(trigger);
			}
			break; // end case eviofile

		case ET:
			_eventIndex--;
			break;
		}

		ifPassSetEvent(_currentEvent, 1);

		return _currentEvent;
	}

	private void skipEvents(int n) {
		if (n < 1) {
			return;
		}

		EventSourceType estype = getEventSourceType();

		switch (estype) {
		case HIPOFILE:
		case EVIOFILE:
			int numRemaining = getNumRemainingEvents();
			n = Math.min(numRemaining, n);

			for (int i = 0; i < n; i++) {
				if (_dataSource.hasEvent()) {
					_dataSource.getNextEvent();
					_eventIndex++;
				}
			}

			break;

		// case HIPORING:
		case ET:
			break;
		}
	}

	/**
	 * 
	 * @param eventNumber
	 *            a 1-based number 1..num events in file
	 * @return the event at the given number (if possible).
	 */
	public DataEvent gotoEvent(int eventNumber) {

		if ((eventNumber < 1) || (eventNumber == _eventIndex) || (eventNumber > getEventCount())) {
			return _currentEvent;
		}

		EventSourceType estype = getEventSourceType();
		switch (estype) {

		case HIPOFILE:
			if (eventNumber > _eventIndex) {
				int numToSkip = (eventNumber - _eventIndex) - 1;
				skipEvents(numToSkip);
				getNextEvent();
			} else {
				_dataSource.close();
				_currentEvent = null;
				_eventIndex = 0;
				_dataSource.open(_currentHipoFile);
				gotoEvent(eventNumber);
			}

			break;

		case EVIOFILE:
			_currentEvent = _dataSource.gotoEvent(eventNumber);
			if ((_currentEvent != null) && (_currentEvent instanceof EvioDataEvent)) {

				if (_decoder == null) {
					_decoder = new CLASDecoder();
				}
				_currentEvent = _decoder.getDataEvent(_currentEvent);
				_eventIndex = eventNumber;
			}
			break;
		}

		//
		//
		// if (isSourceHipoFile() && (eventNumber > 0) && (eventNumber <
		// getEventCount())) {
		//
		// if (eventNumber > _eventIndex) {
		// int numToGo = (eventNumber = _eventIndex);
		// }
		//
		// _currentEvent = _dataSource.gotoEvent(eventNumber);
		// _eventIndex = eventNumber;
		// }
		//
		// else if (isSourceEvioFile() && (eventNumber > 0) && (eventNumber <
		// getEventCount())) {
		// _currentEvent = _dataSource.gotoEvent(eventNumber);
		// if ((_currentEvent != null) && (_currentEvent instanceof
		// EvioDataEvent)) {
		//
		// if (_decoder == null) {
		// _decoder = new CLASDecoder();
		// }
		// _currentEvent = _decoder.getDataEvent(_currentEvent);
		// _eventIndex = eventNumber;
		// }
		// }

		setNextEvent(_currentEvent);

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
	 * @param path
	 *            the path to the new file
	 */
	private void notifyEventListeners(EventSourceType source) {

		Swimming.clearAllTrajectories();

		if (_dataSource != null) {
			_dataSource.close();
			_currentEvent = null;
			_eventIndex = 0;
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
	 * Check if there are any active filters
	 * 
	 * @return <code>true</code> if there are any active filters
	 */
	public boolean isFilteringOn() {
		if (_eventFilters != null) {
			for (IEventFilter filter : _eventFilters) {
				if (filter.isActive()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Notify listeners we have a new event ready for display. All they may want is
	 * the notification that a new event has arrived. But the event itself is passed
	 * along.
	 */
	protected void notifyEventListeners() {

		Swimming.setNotifyOn(false); //prevent refreshes
		Swimming.clearAllTrajectories();
		Swimming.setNotifyOn(true); //prevent refreshes
		
		_uniqueLundIds = null;

		Ced.getCed().setEventFilteringLabel(isFilteringOn());

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

		// System.err.println("FINAL STEPS");
		// some scaling factors for gradient displays
		computeSomeScalingFactors();

		SwimMenu.getInstance().firePropertyChange(SWIM_ALL_MC_PROP, 0, 1);
		SwimMenu.getInstance().firePropertyChange(SWIM_ALL_RECON_PROP, 0, 1);

		Ced.setEventNumberLabel(getEventNumber());

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
	 * @param plane
	 *            (0, 1, 2) for (PCAL, EC_INNER, EC_OUTER)
	 * @return the max energy deposited in that cal plane in MeV
	 */
	public double getMaxEdepCal(int plane) {
		return maxEDepCal[plane];
	}

	/**
	 * Remove a IClasIoEventListener. IClasIoEventListener listeners listen for new
	 * events.
	 * 
	 * @param listener
	 *            the IClasIoEventListener listener to remove.
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
	 * @param listener
	 *            the IClasIoEventListener listener to add.
	 * @param index
	 *            Determines gross notification order. Those in index 0 are notified
	 *            first. Then those in index 1. Finally those in index 2. The Data
	 *            containers should be in index 0. The trajectory and noise in index
	 *            1, and the regular views in index 2 (they are notified last)
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
		return DataManager.getInstance().getKnownBanks();
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

	// does the event pass all the active filters?
	private boolean passFilters(DataEvent event) {

		if ((event != null) && !_eventFilters.isEmpty()) {
			for (IEventFilter filter : _eventFilters) {
				if (filter.isActive()) {
					boolean pass = filter.pass(event);
					if (!pass) {
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Add an event filter
	 * 
	 * @param filter
	 *            the filter to add
	 */
	public void addEventFilter(IEventFilter filter) {
		if (filter != null) {
			if (!_eventFilters.contains(filter)) {
				_eventFilters.add(filter);
			}
		}
	}

	/**
	 * Do this late in ced initialization
	 */
	public void setUpFilterMenu() {
		if (_eventFilters != null) {
			for (IEventFilter filter : _eventFilters) {
				Ced.getCed().getEventFilterMenu().add(filter.getMenuComponent());
			}
		}
	}

}
