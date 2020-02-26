package cnuphys.fastMCed.eventgen;

import org.jlab.clas.physics.PhysicsEvent;

public interface IEventSource {

	/**
	 * By some method, return an event
	 * @return the event
	 */
	public PhysicsEvent getEvent();
}
