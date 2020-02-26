package cnuphys.fastMCed.consumers;

import java.util.List;

import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.streaming.StreamProcessStatus;
import cnuphys.fastMCed.streaming.StreamReason;

/**
 * This is a test consumer that will flag event number 44 in the stream
 * @author heddle
 *
 */
public class Event44Consumer extends PhysicsEventConsumer {

	@Override
	public String getConsumerName() {
		return "Event 44 Test Consumer";
	}

	@Override
	public void streamingChange(StreamReason reason) {
		//ignore
	}

	@Override
	public StreamProcessStatus streamingPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		if (PhysicsEventManager.getInstance().eventNumber() == 44) {
			return StreamProcessStatus.FLAG;
		}
		else {
			return StreamProcessStatus.CONTINUE;
		}
	}

	@Override
	public void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		System.err.println(getConsumerName() + "  got a new event via next event");

	}

	@Override
	public String flagExplanation() {
		return "Reached event number 44";
	}

}
