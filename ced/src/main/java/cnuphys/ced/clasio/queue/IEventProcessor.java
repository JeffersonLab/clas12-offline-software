package cnuphys.ced.clasio.queue;

import org.jlab.io.base.DataEvent;

public interface IEventProcessor {

    /**
     * Process an event. 
     * @param event the EvioDataEvent event to process.
     */
    public void processEvent(DataEvent event);
}
