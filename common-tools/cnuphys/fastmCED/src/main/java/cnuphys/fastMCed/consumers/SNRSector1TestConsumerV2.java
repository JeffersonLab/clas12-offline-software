package cnuphys.fastMCed.consumers;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;

import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.fastMCed.eventgen.random.RandomEventGenerator;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.snr.SNRDictionary;
import cnuphys.fastMCed.snr.SNRManager;
import cnuphys.fastMCed.streaming.StreamProcessStatus;
import cnuphys.fastMCed.streaming.StreamReason;
import cnuphys.lund.GeneratedParticleRecord;

public class SNRSector1TestConsumerV2 extends ASNRConsumer {

	ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();

	private long totalFoundTime;
	int numFound;
	
	private long totalMissedTime;
	int numMissed;

	int numTrialFound = 100000;
	int numTrialMissed = 100;
	
	int streamNFound;
	int streamNMissed;

	@Override
	public String getConsumerName() {
		return "SNR Speed Test";
	}

	@Override
	public void streamingChange(StreamReason reason) {
		if (reason == StreamReason.STOPPED) {
			double ntot = streamNMissed + streamNFound;
			if (ntot > 1) {
				System.err.println("percentage of missed = " + (100. * ((double) streamNMissed)) / ntot);
			}
		}
	}


	@Override
	public StreamProcessStatus streamingPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		
		if (_inDictionary == null) {
			loadOrCreateDictionary(SNRDictionary.IN_BENDER);
		}

		if (snr.segmentsInAllSuperlayers(0, SNRManager.RIGHT)) {
			String hash = snr.hashKey(0, SNRManager.RIGHT); 

			// see if this key is in the dictionary. If it is we'll get
			//  a hash of a GeneratedParticleRec back
			String gprHash = _inDictionary.get(hash);


			if (gprHash != null) { //match
				streamNFound++;
			}
			else {
				String nearestKey = _inDictionary.nearestKey(hash);
				gprHash = _inDictionary.get(nearestKey);

				streamNMissed++;
			}
		}
		return StreamProcessStatus.CONTINUE;
	}

	@Override
	public void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {

		if (_inDictionary == null) {
			loadOrCreateDictionary(SNRDictionary.IN_BENDER);
		}

		if ((_inDictionary != null) && !_inDictionary.isEmpty()) {
			if (PhysicsEventManager.getInstance().getEventGenerator() instanceof RandomEventGenerator) {

				//test is for sector 1 right leaners only
				if (snr.segmentsInAllSuperlayers(0, SNRManager.RIGHT)) {
					String hash = snr.hashKey(0, SNRManager.RIGHT); 

					// see if this key is in the dictionary. If it is we'll get
					//  a hash of a GeneratedParticleRec back
					String gprHash = _inDictionary.get(hash);

					if (gprHash != null) { //match
						
						System.err.println("Found Time Test");

						long startTime = mxbean.getCurrentThreadUserTime(); //ns
						
						
						
						
						for (int i = 0; i < numTrialFound; i++) {
							hash = snr.hashKey(0, SNRManager.RIGHT); 
							gprHash = _inDictionary.get(hash);
							GeneratedParticleRecord rpr = GeneratedParticleRecord.fromHash(gprHash);
						}
						totalFoundTime += (mxbean.getCurrentThreadUserTime() - startTime);
						numFound += numTrialFound;
						double avgTimeFound = ((double)totalFoundTime)/numFound;
						System.err.println("Done found time test. Average time = " + (avgTimeFound/1000000) + " ms");
						
						
					} else {  //no match
						System.err.println("Missed Time Test");
						long startTime = mxbean.getCurrentThreadUserTime();
						for (int i = 0; i < numTrialMissed; i++) {
							String nearestKey = _inDictionary.nearestKey(hash);
							gprHash = _inDictionary.get(nearestKey);
							GeneratedParticleRecord rpr = GeneratedParticleRecord.fromHash(gprHash);
						}
						totalMissedTime += (mxbean.getCurrentThreadUserTime() - startTime);
						numMissed += numTrialMissed;
						double avgTimeMissed = ((double)totalMissedTime)/numMissed;
						System.err.println("Done missed time test. Average time = " + (avgTimeMissed/1000000) + " ms");


					}
				}

			} // random generator
		}
	}


}
