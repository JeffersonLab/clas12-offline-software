package cnuphys.eventStation;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSource;

public abstract class EventStation {
	
	/** the version string */
	public static String version = "0.1";
	
	//count of created event stations
	private static int _count = 0;
	
	//the name if this station
	private String _name;
	
	//numerical ID of this station
	private int _id;
	
	// the current event
	private DataEvent _currentEvent;

	// the clas_io source of events
	private DataSource _dataSource;
	
	//the event index for this EventStation
	private int _eventIndex;
	
	//the one and only filter
	private IAcceptDataEvent _filter;
	
	/**
	 * Create an EventStation
	 * @param name the name of the station
	 */
	public EventStation(String name) {
		++_count;
		_id = _count;
		_name = (name != null) ? name : ("Event Station " + _id);
	}
	
	/**
	 * Specifies whether a request for the next event is possible. Generally the return should
	 * be <code>true</code>, although, for example, if the event source is a file we
	 * might be ad the end of the filee.
	 * 
	 * @return <code>true</code> if a request for the next event is possible
	 */
	public abstract boolean isNextOK();

	/**
	 * Determines whether a request for the previous event is possible
	 * 
	 * @return <code>true</code> if a request for the next previous event is possible
	 */
	public abstract boolean isPrevOK();
	
	/**
	 * Determines whether a request to go to a specific event is possible. 
	 * For some sources, such as an ET ring, this will always return false.
	 * 
	 * @return <code>true</code> if a request to go to a specific event is possible
	 */
	public abstract boolean isGotoOK();
	
	/**
	 * Get the number of events available in this EventStation.
	 * @return the event count, or possibly Integer.MAX_VALUE for a streaming source
	 * like ET.
	 */
	public abstract int getEventCount();
	
	/**
	 * Obtain the number of remaining events. For a file source it is what you
	 * expect. For an ET source, it is arbitrarily set to Integer.MAX_VALUE
	 * 
	 * @return the number of remaining events
	 */
	public abstract int getNumRemainingEvents();

	
	/**
	 * Get the event number, which is just the event index for this EventStation.
	 * @return the event number, which is just the event index for this EventStation.
	 */
	public int getEventNumber() {
		return _eventIndex;
	}
	

	/**
	 * Get the name of the station
	 * @return the name of the station
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Get the numerical Id of the station. These are just assigned sequentially.
	 * @return the numerical Id of the station
	 */
	public int getId() {
		return _id;
	}
		
	/**
	 * Get the current event for this EventStation
	 * @return the current event for this EventStation
	 */
	public DataEvent getCurrentEvent() {
		return _currentEvent;
	}
	
	/**
	 * Get the clasio (in coatjava) DataSource for this EventStation. It might
	 * be a evio or hipo file source, an ET source, etc.
	 * @return the clasio (in coatjava) DataSource for this EventStation
	 */
	public DataSource getDataSource() {
		return _dataSource;
	}
	
	/**
	 * Set the optional filter for this station. There can only be zero or one
	 * active filters.
	 * @param filter the new filter. Set to <code>null</code> for no filter (default).
	 */
	public void setFilter(IAcceptDataEvent filter) {
		_filter = filter;
	}
	
	/**
	 * Remove any event filtering. 
	 */
	public void removeFilter() {
		setFilter(null);
	}
	
	/**
	 * main method used for testing
	 * @param arg command line arguments (ignored)
	 */
	public static void main(String[] arg) {
		System.out.println("Testing eventStation version " + version);
	}

	
	/**
	 * Event filters should be closed when done, so that associated threads
	 * can be stopped and other resources freed.
	 */
	public abstract void close();


}