package cnuphys.ced.clasio.queue;

import org.jlab.evio.clas12.EvioDataEvent;

public interface IEventProcessor {

    /**
     * Process an event. 
     * @param event the EvioDataEvent event to process.
     */
    public void processEvent(EvioDataEvent event);
}
