package cnuphys.fastMCed.consumers;

import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;

import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.fastMCed.eventgen.random.RandomEventGenerator;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.frame.FastMCed;
import cnuphys.fastMCed.snr.SNRDictionary;
import cnuphys.fastMCed.snr.SNRManager;
import cnuphys.fastMCed.streaming.StreamProcessStatus;
import cnuphys.fastMCed.streaming.StreamReason;
import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.splot.pdata.DataSetException;

public class SNRResolutionConsumer extends ASNRConsumer {
	
	private ResolutionHisto pHisto;
	private ResolutionHisto pabsHisto;
	private ResolutionHisto thetaHisto;
	private ResolutionHisto phiHisto;
	
//	public static final String TITLE = "title";
//	public static final String XLABEL = "xlabel";
//	public static final String YLABEL = "ylabel";
//	public static final String DATAMIN = "datamin";
//	public static final String DATAMAX = "datamax";
//	public static final String NUMBIN = "numbin";
	
	
	public SNRResolutionConsumer() {
		
		JFrame parent = FastMCed.getFrame();
		
		Properties pProps = new Properties();
		pProps.put(ResolutionHisto.TITLE, "Momentum Resolution (percent)");
		pProps.put(ResolutionHisto.XLABEL, UnicodeSupport.CAPITAL_DELTA + "p/p (%)");
		pProps.put(ResolutionHisto.YLABEL, "Counts");
		pProps.put(ResolutionHisto.DATAMIN, "-15.");
		pProps.put(ResolutionHisto.DATAMAX, "15.");
		pProps.put(ResolutionHisto.NUMBIN, "50");	
		pHisto = new ResolutionHisto(parent, pProps);
		
		Properties pabsProps = new Properties();
		pabsProps.put(ResolutionHisto.TITLE, "Momentum Resolution (MeV/c)");
		pabsProps.put(ResolutionHisto.XLABEL, UnicodeSupport.CAPITAL_DELTA + "p (MeV/c)");
		pabsProps.put(ResolutionHisto.YLABEL, "Counts");
		pabsProps.put(ResolutionHisto.DATAMIN, "-500.");
		pabsProps.put(ResolutionHisto.DATAMAX, "500.");
		pabsProps.put(ResolutionHisto.NUMBIN, "50");	
		pabsHisto = new ResolutionHisto(parent, pabsProps);
		
		Properties thetaProps = new Properties();
		thetaProps.put(ResolutionHisto.TITLE, "Theta Resolution (degrees)");
		thetaProps.put(ResolutionHisto.XLABEL, UnicodeSupport.CAPITAL_DELTA + UnicodeSupport.SMALL_THETA + " (degrees)");
		thetaProps.put(ResolutionHisto.YLABEL, "Counts");
		thetaProps.put(ResolutionHisto.DATAMIN, "-1.");
		thetaProps.put(ResolutionHisto.DATAMAX, "1.");
		thetaProps.put(ResolutionHisto.NUMBIN, "50");	
		thetaHisto = new ResolutionHisto(parent, thetaProps);
		
		Properties phiProps = new Properties();
		phiProps.put(ResolutionHisto.TITLE, "Phi Resolution (degrees)");
		phiProps.put(ResolutionHisto.XLABEL, UnicodeSupport.CAPITAL_DELTA + UnicodeSupport.SMALL_PHI + " (degrees)");
		phiProps.put(ResolutionHisto.YLABEL, "Counts");
		phiProps.put(ResolutionHisto.DATAMIN, "-5.");
		phiProps.put(ResolutionHisto.DATAMAX, "5.");
		phiProps.put(ResolutionHisto.NUMBIN, "50");	
		phiHisto = new ResolutionHisto(parent, phiProps);



	}

	@Override
	public String getConsumerName() {
		return "SNR Resolution";
	}

	@Override
	public void streamingChange(StreamReason reason) {
		if (reason == StreamReason.STOPPED) {
		}
	}

	
	int NP = 0;
	@Override
	public StreamProcessStatus streamingPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {

		if (snr.segmentsInAllSuperlayers(0, SNRManager.RIGHT)) {
			GeneratedParticleRecord gpr = particleHits.get(0).getGeneratedParticleRecord();

			if (_inDictionary == null) {
				loadOrCreateDictionary(SNRDictionary.IN_BENDER);
				pHisto.setVisible(true);
				pabsHisto.setVisible(true);
				thetaHisto.setVisible(true);
				phiHisto.setVisible(true);
			}

			if ((_inDictionary != null) && !_inDictionary.isEmpty()) {
				if (PhysicsEventManager.getInstance().getEventGenerator() instanceof RandomEventGenerator) {

					GeneratedParticleRecord rpr;

					// test is for sector 1 right leaners only
					String hash = snr.hashKey(0, SNRManager.RIGHT);
					String gprHash = _inDictionary.get(hash);

					// if not there, add
					if (gprHash == null) {
						// System.err.println("Added to dictionary");

						gprHash = gpr.hashKey();
						_inDictionary.put(hash, gprHash);

					}
					
					rpr = GeneratedParticleRecord.fromHash(gprHash);
					TrajectoryRowData trajData = getTruth(rpr.getCharge());
					double dP = trajData.getMomentum() - 1000*rpr.getMomentum();
					double dTheta = trajData.getTheta() - rpr.getTheta();
					double dPhi = trajData.getPhi() - rpr.getPhi();
					double fracdP = dP/trajData.getMomentum();

					try {
						thetaHisto.getDataSet().add(dTheta);
						phiHisto.getDataSet().add(dPhi);
						pabsHisto.getDataSet().add(dP);
						pHisto.getDataSet().add(fracdP * 100);
					} catch (DataSetException e) {
						e.printStackTrace();
					}

					NP = ((NP + 1) % 100);

					if (NP == 0) {
						pHisto.getPlotCanvas().needsRedraw(true);
					}
					
				} //random generator
			} //not empty
		}
		return StreamProcessStatus.CONTINUE;
	}

	@Override
	public void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {

		if (_inDictionary == null) {
			loadOrCreateDictionary(SNRDictionary.IN_BENDER);
			pHisto.setVisible(true);
		}

		if ((_inDictionary != null) && !_inDictionary.isEmpty()) {
			if (PhysicsEventManager.getInstance().getEventGenerator() instanceof RandomEventGenerator) {

				// test is for sector 1 right leaners only
				if (snr.segmentsInAllSuperlayers(0, SNRManager.RIGHT)) {
					String hash = snr.hashKey(0, SNRManager.RIGHT);

					// see if this key is in the dictionary. If it is we'll get
					// a hash of a GeneratedParticleRec back
					String gprHash = _inDictionary.get(hash);

					GeneratedParticleRecord rpr;
					if (gprHash != null) {
						System.err.println("*** Dictionary Match ***");
					} else {
						System.err.println("No dictionary match. Looking for closest");

						String nearestKey = _inDictionary.nearestKey(hash);
						System.err.println("COMMON BITS " + SNRDictionary.commonBits(hash, nearestKey));
						gprHash = _inDictionary.get(nearestKey);
						System.err.println("Closest Match");
						// add to plot
					}

					rpr = GeneratedParticleRecord.fromHash(gprHash);
					System.err.println(rpr.toString());
					TrajectoryRowData trajData = getTruth(rpr.getCharge());
					double dP = Math.abs(trajData.getMomentum() - 1000 * rpr.getMomentum());
					double dTheta = Math.abs(trajData.getTheta() - rpr.getTheta());
					double dPhi = Math.abs(trajData.getPhi() - rpr.getPhi());
					double fracdP = dP / trajData.getMomentum();
					System.err.println(String.format(
							"Error dP = %-6.3f MeV dP/P = %-6.3f%% dTheta = %-6.3f deg  dPhi = %-6.3f deg", dP,
							100 * fracdP, dTheta, dPhi));
				}
			} // random generator
		}
	}
}
