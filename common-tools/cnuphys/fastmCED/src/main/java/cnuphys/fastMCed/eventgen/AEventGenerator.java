package cnuphys.fastMCed.eventgen;

import java.util.Vector;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

public abstract class AEventGenerator {
	
	/**
	 * Return a simple description. This will show up in a menu.
	 * @return a simple description of this generator
	 */
	public abstract String generatorDescription();
	
	/**
	 * Get the next event
	 * @return the FastMC PhysicsEvent object that can be used to generate hits.
	 */
	public abstract PhysicsEvent nextEvent();
	
	/**
	 * The number of the current event
	 * @return the number of the current event
	 */
	public abstract int eventNumber();
	
	/**
	 * Get the event count; i.e. the total number of events that can be accessed.
	 * If this is unknown (essentially infinite) return Integer.MAX_VALUE.
	 * @return the total number of events that can be accessed.
	 */
	public abstract int eventCount();
	
	/**
	 * This generator is being closed (discarded) in place of a new
	 * generator. If the generator needs to clean up, free up resources,
	 * etc. it should override this method. Otherwise it can just keep this
	 * empty implementation.
	 */
	public void close() {
		System.err.println("Closing generator " + generatorDescription());
	}
	
	/**
	 * Get the most recently read or created event
	 * @return the most recently read or created event
	 */
	public abstract PhysicsEvent getCurrentEvent();
	
	/**
	 * Get the number of events available for streaming. If this
	 * is an infinite source then this will return Integer.MAX_VALUE.
	 * @return the number of events available for streaming.
	 */
	public int eventsRemaining() {
		int count = eventCount();
		if ((count < 0) || (count == Integer.MAX_VALUE)) {
			return Integer.MAX_VALUE;
		}
		else {
			return count - eventNumber();
		}
	}
	
	
	/**
	 * Get a collection of unique LundIds in the current event
	 * 
	 * @return a collection of unique LundIds
	 */
	public void uniqueLundIds(Vector<LundId> uniqueIds) {

		PhysicsEvent event = getCurrentEvent();

		if ((event != null) && (event.count() > 0)) {
			for (int index = 0; index < event.count(); index++) {
				Particle particle = event.getParticle(index);
				LundId lid = LundSupport.getInstance().get(particle.pid());
				uniqueIds.remove(lid);
				uniqueIds.add(lid);

			}
		}
	}

}
