package cnuphys.ced.clasio.queue;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.base.DataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;

public class EventProducer implements IClasIoEventListener {

	// the event manager
	private final ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// the event queue
	private ClasIoEventQueue _queue;

	/**
	 * Create an EventProducer. This is basically an IClasIoEventListener that
	 * queues any event it receieves.
	 * 
	 * @param queue
	 *            the event queue
	 * @see IClasIoEventListener
	 */
	public EventProducer(ClasIoEventQueue queue) {
		_queue = queue;
		_eventManager.addClasIoEventListener(this, 2);
	}

	/**
	 * New fast mc event
	 * @param event the generated physics event
	 */
	@Override
	public void newFastMCGenEvent(PhysicsEvent event) {
	}
	

	@Override
	public void newClasIoEvent(DataEvent event) {
		if (_queue != null) {
			// add event, which will cause consumer threads that
			// are waiting to be notified.
			_queue.add(event);
		}
	}

	@Override
	public void openedNewEventFile(String path) {
	}

	/**
	 * Change the event source type
	 * 
	 * @param source
	 *            the new source: File, ET, FastMC
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}

}
