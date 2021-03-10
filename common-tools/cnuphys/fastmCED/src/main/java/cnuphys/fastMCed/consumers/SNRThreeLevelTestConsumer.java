package cnuphys.fastMCed.consumers;

import java.util.List;

import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.snr.SNRManager;
import cnuphys.fastMCed.streaming.StreamProcessStatus;
import cnuphys.fastMCed.streaming.StreamReason;

/**
 * This is to test the rewrite of two level SNR <BR>
 * Level 1: Normal SNR That finds isolated noise and segments candidates in each
 * superlayer. The output is an extended word for each superlayer <BR>
 * Level 2
 * 
 * @author heddle
 *
 */
public class SNRThreeLevelTestConsumer extends PhysicsEventConsumer {

	// the SNR Manager singleton
	SNRManager snrManager = SNRManager.getInstance();

	@Override
	public String getConsumerName() {
		return "SNR Three Level Test";
	}

	@Override
	public void streamingChange(StreamReason reason) {
		System.err.println("SNRThreeLevelTestConsumer stream change: " + reason);
	}

	@Override
	public StreamProcessStatus streamingPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
//		if (snrManager.getNoiseCount() > 0) {
//			return StreamProcessStatus.FLAG;
//		}
//		else {
//			return StreamProcessStatus.CONTINUE;
//		}
		return StreamProcessStatus.CONTINUE;
	}

	@Override
	public void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		System.err.println("SNRThreeLevelTestConsumer newPhysicsEvent");
	}

	@Override
	public String flagExplanation() {
		return "SNRThreeLevelTestConsumer flag ";
	}

}
