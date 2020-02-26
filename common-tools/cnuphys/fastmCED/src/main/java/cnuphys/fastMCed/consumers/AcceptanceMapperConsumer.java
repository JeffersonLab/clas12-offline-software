package cnuphys.fastMCed.consumers;

import java.util.List;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.geom.DetectorId;

import cnuphys.fastMCed.fastmc.HitHolder;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.streaming.StreamProcessStatus;
import cnuphys.fastMCed.streaming.StreamReason;
import cnuphys.lund.GeneratedParticleRecord;

public class AcceptanceMapperConsumer extends PhysicsEventConsumer {

	private int uniqueLayerThreshold = 32;
	private String _errStr = "???";
	
	double thetaMin = Double.POSITIVE_INFINITY;
	double thetaMax = Double.NEGATIVE_INFINITY;
	
	double phiMin = Double.NaN;
	double phiMax = Double.NaN;
	
	@Override
	public String getConsumerName() {
		return "Acceptance Mapper";
	}

	@Override
	public void streamingChange(StreamReason reason) {
		if (reason == StreamReason.STOPPED) {
			System.err.println("ACCEPTMAP thetaMin = " + thetaMin + " at phi = " + phiMin);
			System.err.println("ACCEPTMAP thetaMax = " + thetaMax + " at phi = " + phiMax);
			
			thetaMin = Double.POSITIVE_INFINITY;
			thetaMax = Double.NEGATIVE_INFINITY;
			phiMin = Double.NaN;
		 phiMax = Double.NaN;

		}
	}

	@Override
	public StreamProcessStatus streamingPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		int uniqueCount = uniqueLayCount(particleHits);
		
		if (uniqueCount >= uniqueLayerThreshold) {
			GeneratedParticleRecord gpr = particleHits.get(0).getGeneratedParticleRecord();
			
			double theta = gpr.getTheta();
			double phi = gpr.getPhi();
			
			if (theta < thetaMin) {
				thetaMin = theta;
				phiMin = phi;
			}
			if (theta > thetaMax) {
				thetaMax = theta;
				phiMax = phi;
			}
			
		}
		return StreamProcessStatus.CONTINUE;
	}

	@Override
	public void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
	}

	@Override
	public String flagExplanation() {
		return _errStr;
	}
	
	private int uniqueLayCount(List<ParticleHits> particleHits) {
		HitHolder hitHolder = particleHits.get(0).getHitHolder(DetectorId.DC);
		int count = hitHolder.sectorUniqueLayerCount(0);
		return count;
	}

}
