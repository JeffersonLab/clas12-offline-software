package cnuphys.eventStation;

import org.jlab.io.base.DataEvent;

/**
 * This is a filter for a data event.
 * @author heddle
 *
 */
public interface IAcceptDataEvent {

	/**
	 * A filter to accept or reject a DataEvent
	 * @param dataEvent the clasio DataEvent object
	 * @return <code>true</code> if this event is to be accepted.
	 */
	public boolean accept(DataEvent dataEvent);
	
	/**
	 * Provide a short description of this filter, such as
	 * "Has TOF hits in sector 1"
	 * @return
	 */
	public String getDescription();
}
