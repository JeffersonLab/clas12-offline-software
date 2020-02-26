package cnuphys.ced.clasio;

import java.util.EventListener;

import org.jlab.io.base.DataEvent;

public interface IClasIoEventListener extends EventListener {
	/**
	 * Notifies listeners that a new event has arrived.
	 * 
	 * @param event
	 *            the new event.
	 */
	public void newClasIoEvent(final DataEvent event);
	
	/**
	 * Opened a new event file
	 * 
	 * @param path
	 *            the path to the new file
	 */
	public void openedNewEventFile(final String path);
	
	/**
	 * Change the event source type
	 * @param source
	 */
	public void changedEventSource(ClasIoEventManager.EventSourceType source);
	
	/**
	 * Tests whether this listener is interested in events while accumulating
	 * @return <code>true</code> if this listener is NOT interested in  events while accumulating
	 */
	public boolean ignoreIfAccumulating();
	
}