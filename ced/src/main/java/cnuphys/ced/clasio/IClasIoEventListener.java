package cnuphys.ced.clasio;

import java.util.EventListener;

import org.jlab.evio.clas12.EvioDataEvent;

public interface IClasIoEventListener extends EventListener {
	/**
	 * Notifies listeners that a new event has arrived.
	 * 
	 * @param event
	 *            the new event.
	 */
	public void newClasIoEvent(final EvioDataEvent event);

	/**
	 * Opened a new event file
	 * 
	 * @param path
	 *            the path to the new file
	 */
	public void openedNewEventFile(final String path);

}