package cnuphys.bCNU.event;

import java.util.EventListener;

import org.jlab.coda.jevio.EvioEvent;

public interface IPhysicsEventListener extends EventListener {

    /**
     * Notifies listeners that a new event has arrived from jevio. This is the
     * actual event not a copy so it should not be modified.
     * 
     * @param event
     *            the new event.
     */
    public void newPhysicsEvent(final EvioEvent event);

}
