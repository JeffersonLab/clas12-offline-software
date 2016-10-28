package cnuphys.ced.clasio;

import java.util.EventListener;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.evio.EvioDataEvent;

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
	
	/**
	 * Change the event source type
	 * @param source
	 */
	public void changedEventSource(ClasIoEventManager.EventSourceType source);
	
	/**
	 * New fast mc event
	 * @param event the generated physics event
	 */
	public void newFastMCGenEvent(PhysicsEvent event);

}