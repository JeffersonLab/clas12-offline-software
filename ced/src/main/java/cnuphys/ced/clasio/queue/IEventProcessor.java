package cnuphys.ced.clasio.queue;

import org.jlab.io.evio.EvioDataEvent;

public interface IEventProcessor {

    /**
     * Process an event. 
     * @param event the EvioDataEvent event to process.
     */
    public void processEvent(EvioDataEvent event);
}
