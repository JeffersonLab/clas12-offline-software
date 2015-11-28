package cnuphys.ced.clasio.queue;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;

public class EventProducer implements IClasIoEventListener {

    // the event manager
    private final ClasIoEventManager _eventManager = ClasIoEventManager
	    .getInstance();

    // the event queue
    private ClasIoEventQueue _queue;

    /**
     * Create an EventProducer. This is basically an IClasIoEventListener
     * that queues any event it receieves.
     * @param queue the event queue
     * @see IClasIoEventListener
     */
    public EventProducer(ClasIoEventQueue queue) {
	_queue = queue;
	_eventManager.addClasIoEventListener(this, 2);
    }

    @Override
    public void newClasIoEvent(EvioDataEvent event) {
	if (_queue != null) {
	    //add event, which will cause consumer threads that
	    //are waiting to be notified.
	    _queue.add(event);
	}
    }

    @Override
    public void openedNewEventFile(String path) {
    }

}
