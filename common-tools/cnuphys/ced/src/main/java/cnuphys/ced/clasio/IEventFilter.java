package cnuphys.ced.clasio;

import org.jlab.io.base.DataEvent;

public interface IEventFilter {

	/**
	 * A filter for an event
	 * @param event the event to test
	 * @return <code>true</code> if the event passes the filter
	 */
	public boolean pass(DataEvent event);
	
	/**
	 * Set the active state of the filter
	 * @param active the state of the filter
	 */
	public void setActive(boolean active);
	
	/**
	 * Check whether the filter is active
	 * @return <code>true</code> if the filter is active
	 */
	public boolean isActive();
	
	/**
	 * Set the name of the filter
	 * @param name the name of the filter
	 */
	public void setName(String name);
	
	/**
	 * Get the name of the filter.
	 * @return the name of the filter.
	 */
	public String getName();
}
