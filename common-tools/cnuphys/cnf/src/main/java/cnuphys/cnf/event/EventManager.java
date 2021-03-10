package cnuphys.cnf.event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSource;
import org.jlab.io.hipo.HipoDataSource;

import cnuphys.cnf.alldata.DataManager;

public class EventManager {
	
	//streaming related constants
	private static final int START_STREAMING      = 0;
	private static final int STOP_STREAMING       = 1;
	
	/** Constant indicated streaming successfully completed */
	public static final int STREAMING_COMPLETED   = 0;

	/** Constant indicated streaming was cancelled before completion */
	public static final int STREAMING_CANCELLED   = 1;

	
	//are we streaming?
	private static boolean _streaming = false;

	// A sorted list of banks present in the current event
	private String _currentBanks[];

	// reset everytime hipo file is opened
	private int _eventIndex;

	// all the filters
	private ArrayList<IEventFilter> _eventFilters = new ArrayList<>();


	// list of view listeners. There are actually three lists. Those in index 0
	// are notified first. Then those in index 1. Finally those in index 2. The
	// Data
	// containers should be in index 0. The trajectory and noise in index 1, and
	// the
	// regular views in index 2 (they are notified last)
	private EventListenerList _viewListenerList[] = new EventListenerList[3];

	// the current hipo event file
	private File _currentHipoFile;

	// the clas_io source of events
	private DataSource _dataSource;

	// singleton
	private static EventManager instance;

	// the current event
	private DataEvent _currentEvent;

	// private constructor for singleton
	private EventManager() {
		_dataSource = new HipoDataSource();
	}

	/**
	 * Set the next event (after a getNextEvent)
	 * 
	 * @param event the new event
	 */
	protected void setNextEvent(DataEvent event) {
		_currentEvent = event;

		if (event != null) {
			notifyEventListeners();
		}
	}

	/**
	 * Access for the singleton
	 * 
	 * @return the singleton
	 */
	public static EventManager getInstance() {
		if (instance == null) {
			instance = new EventManager();
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
	 * Get the current event
	 * 
	 * @return the current event
	 */
	public DataEvent getCurrentEvent() {
		return _currentEvent;
	}


	/**
	 * Open an event file
	 * 
	 * @param file the event file
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
		notifyEventListeners(_currentHipoFile, 0);
		
		_currentEvent = null;
		_eventIndex = 0;

		try {
			getNextEvent();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Do we have a hipo file?
	 * @return <code>true</code> if we have a file
	 */
	public boolean haveOpenFile() {
		return _currentHipoFile != null;
	}
	
	/**
	 * Get the current hipo file
	 * @return the current hipo file
	 */
	public File getCurrentFile() {
		return _currentHipoFile;
	}

	/**
	 * Get the number of events available.
	 * 
	 * @return the number of events available
	 */
	public int getEventCount() {

		int evcount = 0;
		evcount = (_dataSource == null) ? 0 : _dataSource.getSize();
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

		boolean isOK = (getEventCount() > 0) && (getEventNumber() < getEventCount());
		return isOK;
	}

	/**
	 * Obtain the number of remaining events. For a file source it is what you
	 * expect. For an et source, it is arbitrarily set to a large number
	 * 
	 * @return the number of remaining events
	 */
	public int getNumRemainingEvents() {
		int numRemaining = getEventCount() - getEventNumber();
		return numRemaining;
	}

	/**
	 * Determines whether any goto event control should be enabled.
	 * 
	 * @return <code>true</code> if any prev event control should be enabled.
	 */
	public boolean isGotoOK() {
		return (getEventCount() > 0);
	}

	/**
	 * Get the next event from the current compact reader
	 * 
	 * @return the next event, if possible
	 */
	public DataEvent getNextEvent() {

		if (_dataSource.hasEvent()) {
			_currentEvent = _dataSource.getNextEvent();
			_eventIndex++;
			ifPassSetEvent(_currentEvent);
		}
		
		return _currentEvent;
	}

	// set the event only if it passes filtering
	private void ifPassSetEvent(DataEvent event) {
		if (event != null) {
			if (passFilters(event)) {
				setNextEvent(event);
			} else {
				getNextEvent();
			}
		}

	}

	/**
	 * See if another event is available
	 * 
	 * @return <code>true</code> if another event is available
	 */
	public boolean hasEvent() {
		return ((_dataSource != null) && _dataSource.hasEvent());
	}

	// skip over n events
	private void skipEvents(int n) {
		if (n < 1) {
			return;
		}

		int numRemaining = getNumRemainingEvents();
		n = Math.min(numRemaining, n);

		for (int i = 0; i < n; i++) {
			if (_dataSource.hasEvent()) {
				_dataSource.getNextEvent();
				_eventIndex++;
			}
		}
	}

	/**
	 * 
	 * @param eventNumber a 1-based number 1..num events in file
	 * @return the event at the given number (if possible).
	 */
	public DataEvent gotoEvent(int eventNumber) {

		if ((eventNumber < 1) || (eventNumber == _eventIndex) || (eventNumber > getEventCount())) {
			return _currentEvent;
		}

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
	
	//streaming notifications
	
	

	// new event file notification
	private void notifyEventListeners(File file, int opt) {

		for (int index = 0; index < 3; index++) {
			if (_viewListenerList[index] != null) {
				// Guaranteed to return a non-null array
				Object[] listeners = _viewListenerList[index].getListenerList();

				// This weird loop is the bullet proof way of notifying all
				// listeners.
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == IEventListener.class) {
						if (opt == 0) {
							IEventListener listener = (IEventListener) listeners[i + 1];
							listener.openedNewEventFile(file);
						}
						if (opt == 1) {
							((IEventListener) listeners[i + 1]).rewoundFile(file);
						}
					}
				}
			}
		}
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
					IEventListener listener = (IEventListener) listeners[i + 1];
					if (listeners[i] == IEventListener.class) {
						listener.newEvent(_currentEvent, _streaming);
					}
				}
			}

		} // index loop

	}

	/**
	 * Remove a IEventListener. IEventListener listeners listen for new
	 * events.
	 * 
	 * @param listener the IEventListener listener to remove.
	 */
	public void removeEventListener(IEventListener listener) {

		if (listener == null) {
			return;
		}

		for (int i = 0; i < 3; i++) {
			if (_viewListenerList[i] != null) {
				_viewListenerList[i].remove(IEventListener.class, listener);
			}
		}
	}

	/**
	 * Add a IEventListener. IEventListener listeners listen for new
	 * events.
	 * 
	 * @param listener the IEventListener listener to add.
	 * @param index    Determines gross notification order. Those in index 0 are
	 *                 notified first. Then those in index 1. Finally those in index
	 *                 2. The Data containers should be in index 0. The trajectory
	 *                 and noise in index 1, and the regular views in index 2 (they
	 *                 are notified last)
	 */
	public void addEventListener(IEventListener listener, int index) {

		if (listener == null) {
			return;
		}

		if (_viewListenerList[index] == null) {
			_viewListenerList[index] = new EventListenerList();
		}

		_viewListenerList[index].add(IEventListener.class, listener);
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
	 * @param filter the filter to add
	 */
	public void addEventFilter(IEventFilter filter) {
		if (filter != null) {
			if (!_eventFilters.contains(filter)) {
				_eventFilters.add(filter);
			}
		}
	}
	
	/**
	 * Rewind the current file
	 */
	public void rewindFile() {
		gotoEvent(1);
		notifyEventListeners(_currentHipoFile, 1);
	}
	
	
	/**
	 * Send a streaming related notification to the listeners
	 * @param option either START_STREAMING or STOP_STREAMING
	 * @param reason (only used for stop streaming) either STREAMING_COMPLETED or STREAMING_CANCELLED
	 */
	protected void notifyEventListenersStreaming(int option, int reason) {
		
		for (int index = 0; index < 3; index++) {
			if (_viewListenerList[index] != null) {
				// Guaranteed to return a non-null array
				Object[] listeners = _viewListenerList[index].getListenerList();

				// This weird loop is the bullet proof way of notifying all
				// listeners.
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					IEventListener listener = (IEventListener) listeners[i + 1];
					if (listeners[i] == IEventListener.class) {
						
						if (option == START_STREAMING) {
							listener.streamingStarted(_currentHipoFile, getNumRemainingEvents());
						}
						else if (option == STOP_STREAMING) {
							listener.streamingEnded(_currentHipoFile, reason);
						}
					}
				}
			}

		} // index loop
		
	}

	
	/**
	 * Stream to the end of the file
	 */
	public void streamToEndOfFile() {
		
		boolean isAWTThread = SwingUtilities.isEventDispatchThread();
		System.out.println("Is AWT Thread: " + isAWTThread);
		
		_streaming = true;
		
		notifyEventListenersStreaming(START_STREAMING, -1);
		
		int numRemain = getNumRemainingEvents();

		for (int i = 0; i < numRemain; i++) {
			getNextEvent();
			
//			if (!isAWTThread && ((i % 1000) == 0)) {
//				Thread.currentThread().yield();
//			}
		}
		
		_streaming = false;
		notifyEventListenersStreaming(STOP_STREAMING, STREAMING_COMPLETED);
		reloadCurrentEvent();
	}
	
	/**
	 * Set whether we are streaming
	 * @return <code>true</coder> id we are streaming
	 */
	public boolean isStreaming() {
		return _streaming;
	}

}