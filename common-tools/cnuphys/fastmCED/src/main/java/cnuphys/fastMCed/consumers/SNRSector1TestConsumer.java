package cnuphys.fastMCed.consumers;

import java.io.File;
import java.util.List;

import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.bCNU.util.Environment;
import cnuphys.fastMCed.eventgen.random.RandomEventGenerator;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.snr.Dictionary3DPlot;
import cnuphys.fastMCed.snr.SNRDictionary;
import cnuphys.fastMCed.snr.SNRManager;
import cnuphys.fastMCed.streaming.StreamProcessStatus;
import cnuphys.fastMCed.streaming.StreamReason;
import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.lund.TrajectoryRowData;

public class SNRSector1TestConsumer extends ASNRConsumer {

	private Dictionary3DPlot _plot3DLeft;
	private Dictionary3DPlot _plot3DRight;

	@Override
	public String getConsumerName() {
		return "SNR Sector 1 Test";
	}

	@Override
	public void streamingChange(StreamReason reason) {
		if (reason == StreamReason.STOPPED) {
			writeDictionary(_inDictionary);
			writeDictionary(_outDictionary);
		}
	}

	//write the dictionary
	private void writeDictionary(SNRDictionary dictionary) {
		File file = dictionary.write(Environment.getInstance().getHomeDirectory() + "/dictionaries");
		System.err.println("File: [" + file.getPath() + "]  size: " + file.length());
		System.err.println("Num keys " + dictionary.size());
	}
	
	private void initDicts() {
		if (_inDictionary == null) {
			loadOrCreateDictionary(SNRDictionary.IN_BENDER);
			_plot3DRight = Dictionary3DPlot.plotDictionary(_inDictionary);
		}
		if (_outDictionary == null) {
			loadOrCreateDictionary(SNRDictionary.OUT_BENDER);
			_plot3DLeft = Dictionary3DPlot.plotDictionary(_outDictionary);
		}
	}

	@Override
	public StreamProcessStatus streamingPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		
		initDicts();

		update(_inDictionary,  _plot3DRight, particleHits, SNRManager.RIGHT);
		update(_outDictionary, _plot3DLeft,  particleHits, SNRManager.LEFT);

		return StreamProcessStatus.CONTINUE;
	}
	
	public void update(SNRDictionary dictionary, Dictionary3DPlot plot, List<ParticleHits> particleHits, int direction) {
		if (snr.segmentsInAllSuperlayers(0, direction)) {
			GeneratedParticleRecord gpr = particleHits.get(0).getGeneratedParticleRecord();

			//test is for sector 1 right leaners only
			String hash = snr.hashKey(0, direction); 
			String gprHash = dictionary.get(hash);

			// if not there, add
			if (gprHash == null) {
				
				gprHash = gpr.hashKey();
				dictionary.put(hash, gprHash);
				
				// add to plot
				plot.append(gprHash);

			}

		}
		
	}

	@Override
	public void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {

		initDicts();
		
		if (PhysicsEventManager.getInstance().getEventGenerator() instanceof RandomEventGenerator) {
			lookForTrack(_inDictionary, _plot3DRight, SNRManager.RIGHT);
			lookForTrack(_outDictionary, _plot3DLeft, SNRManager.LEFT);
		}
	} //newPhysicsEvent

	private void lookForTrack(SNRDictionary dictionary, Dictionary3DPlot plot, int direction) {

		System.err.println("Looking in direction: " + direction);
		//test is for sector 1 only
		if (snr.segmentsInAllSuperlayers(0, direction)) {
			String hash = snr.hashKey(0, direction); 

			// see if this key is in the dictionary. If it is we'll get
			//  a hash of a GeneratedParticleRec back
			String gprHash = dictionary.get(hash);

			GeneratedParticleRecord rpr;
			if (gprHash != null) {
				System.err.println("*** Dictionary Match DIRECTION: " + direction);
			} else {
				System.err.println("No dictionary match. Looking for closest.  DIRECTION: " + direction);

				String nearestKey = dictionary.nearestKey(hash);
				System.err.println("COMMON BITS " + SNRDictionary.commonBits(hash, nearestKey));
				gprHash = dictionary.get(nearestKey);
				System.err.println("Closest Match");
				// add to plot
				plot.append(gprHash);
			}
			
			rpr = GeneratedParticleRecord.fromHash(gprHash);
			System.err.println(rpr.toString());
			TrajectoryRowData trajData = getTruth(rpr.getCharge());
			double dP = Math.abs(trajData.getMomentum() - 1000*rpr.getMomentum());
			double dTheta = Math.abs(trajData.getTheta() - rpr.getTheta());
			double dPhi = Math.abs(trajData.getPhi() - rpr.getPhi());
			double fracdP = dP/trajData.getMomentum();
			System.err.println(String.format("Error dP = %-6.3f MeV dP/P = %-6.3f%% dTheta = %-6.3f deg  dPhi = %-6.3f deg", dP, 100*fracdP, dTheta, dPhi));
		}
		
	}
}
