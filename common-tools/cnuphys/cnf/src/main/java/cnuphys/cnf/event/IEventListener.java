package cnuphys.cnf.event;

import java.util.EventListener;

import org.jlab.io.base.DataEvent;

public interface IEventListener extends EventListener {
	/**
	 * Notifies listeners that a new event has arrived.
	 * 
	 * @param event the new event.
	 */
	public void newClasIoEvent(final DataEvent event);

	/**
	 * Opened a new event file
	 * 
	 * @param path the path to the new file
	 */
	public void openedNewEventFile(final String path);


}