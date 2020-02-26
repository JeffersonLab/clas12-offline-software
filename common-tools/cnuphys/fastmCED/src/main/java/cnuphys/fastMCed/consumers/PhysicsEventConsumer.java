package cnuphys.fastMCed.consumers;

import java.util.List;

import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.fastMCed.eventgen.AEventGenerator;
import cnuphys.fastMCed.eventio.IPhysicsEventListener;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.streaming.IStreamProcessor;
import cnuphys.fastMCed.streaming.StreamProcessStatus;
import cnuphys.fastMCed.streaming.StreamReason;

public abstract class PhysicsEventConsumer implements IPhysicsEventListener, IStreamProcessor {
	
	/** Flag indicating whether the consumer is active */
	protected boolean _active = false;
	
	/**
	 * The null constructor is the one called during discovery.
	 * So don't overload with a non-null constructor
	 */
	public PhysicsEventConsumer() {}
	
	/**
	 * This returns an identifying name for the consumer.
	 * @return a name, which will appear in a list of consumers.
	 */
	public abstract String getConsumerName();

	/**
	 * A message about a change in the streaming state.
	 * @param reason the reason for the change. It will be
	 * one of the self-explanatory values of the StreamReason
	 * class:<br>
	 * STARTED, STOPPED, PAUSED, FINISHED, RESUMED
	 */
	@Override
	public abstract void streamingChange(StreamReason reason);

	/**
	 * A new event in the stream. This occurs when FastMCed is  not
	 * looking event by event, but when it is quickly streaming through
	 * a large number of events. NOTE: this is NOT on a separate thread.It
	 * will in fact be on the GUI thread. This is by design.
	 * @param event the new event arriving through the FastMCed streaming mechanism.
	 * @return StreamProcessStatus.CONTINUE (success) or StreamingReason.FLAG (problem). 
	 * Any consumer returning StreamingReason.FLAG will halt the process
	 * and cause the GUI to display the event that caused the StreamingReason.FLAG. 
	 * The normal return (nothing interesting) is StreamingReason.CONTINUE
	 */
	@Override
	public abstract StreamProcessStatus streamingPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits);

	/**
	 * A new event generator is active
	 * @param generator the now active generator
	 */
	@Override
	public void newEventGenerator(final AEventGenerator generator) {
	}

	/**
	 * New event has arrived from the FastMC engine via the "next event" mechanism.
	 * Note that in streaming mode, do not get broadcast this way, they
	 * are broadcasted via streamingPhysicsEvent
	 * @param event the generated physics event
	 * @see cnuphys.fastMCed.streaming.IStreamProcessor
	 */
	@Override
	public abstract void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits);
	
	/**
	 * If the consumer flags an event, it will be asked
	 * to return the reason. 
	 * @return the reason an event was flagged.
	 */
	@Override
	public abstract String flagExplanation();
	
	/**
	 * Checks whether this consumer is active. Only active consumers
	 * get new streaming events.
	 * @return <code>true</code> if this consumer is active;
	 */
	public boolean isActive() {
		return _active;
	}
	
	/**
	 * Sets whether this consumer is active. Only active consumers
	 * get new streaming events.
	 * @param active the active flag.
	 */
	public void setActive(boolean active) {
		_active = active;
	}

}
