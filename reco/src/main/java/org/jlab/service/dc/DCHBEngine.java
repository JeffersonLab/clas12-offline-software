package org.jlab.service.dc;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.rec.dc.CalibrationConstantsLoader;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.GeometryLoader;
import org.jlab.rec.dc.banks.HitReader;
import org.jlab.rec.dc.banks.RecoBankWriter;
import org.jlab.rec.dc.cluster.ClusterCleanerUtilities;
import org.jlab.rec.dc.cluster.ClusterFinder;
import org.jlab.rec.dc.cluster.ClusterFitter;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.cross.CrossList;
import org.jlab.rec.dc.cross.CrossListFinder;
import org.jlab.rec.dc.cross.CrossMaker;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.segment.SegmentFinder;
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;
import org.jlab.rec.dc.trajectory.DCSwimmer;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;

public class DCHBEngine extends ReconstructionEngine {

	public DCHBEngine() {
		super("DCHB","ziegler","3.0");
	}

	String FieldsConfig="";
	int Run = -1;
	
	@Override
	public boolean init() {
		
		// Load the Fields 
		DCSwimmer.getMagneticFields();
		
		return true;
	}

	
	@Override
	public boolean processDataEvent(DataEvent event) {
		setRunConditionsParameters( event) ;
		 // init SNR 
	    Clas12NoiseResult results = new Clas12NoiseResult(); 
		Clas12NoiseAnalysis noiseAnalysis = new Clas12NoiseAnalysis();

		int[] rightShifts = Constants.SNR_RIGHTSHIFTS;
		int[] leftShifts  = Constants.SNR_LEFTSHIFTS;
		NoiseReductionParameters parameters = new NoiseReductionParameters (
				2,leftShifts,
				rightShifts);
		
	  
		ClusterFitter cf = new ClusterFitter();
	    ClusterCleanerUtilities ct = new ClusterCleanerUtilities();
	    
	    List<FittedHit> fhits = new ArrayList<FittedHit>();
		List<FittedCluster> clusters = new ArrayList<FittedCluster>();
		List<Segment> segments = new ArrayList<Segment>();
		List<Cross> crosses = new ArrayList<Cross>();
		
		List<Track> trkcands = new ArrayList<Track>();
		
		//instantiate bank writer
		RecoBankWriter rbc = new RecoBankWriter();
		
		//if(Constants.DEBUGCROSSES)
		//	event.appendBank(rbc.fillR3CrossfromMCTrack(event));
		
		HitReader hitRead = new HitReader();
		hitRead.fetch_DCHits(event, noiseAnalysis, parameters, results);

		List<Hit> hits = new ArrayList<Hit>();
		//I) get the hits
		hits = hitRead.get_DCHits();
		
		//II) process the hits
		//1) exit if hit list is empty
		if(hits.size()==0 ) {
			return true;
		}

		fhits = rbc.createRawHitList(hits);
				
		
		//2) find the clusters from these hits
		ClusterFinder clusFinder = new ClusterFinder();
		clusters = clusFinder.FindHitBasedClusters(hits, ct, cf);
		
		
		if(clusters.size()==0) {				
			rbc.fillAllHBBanks(event, rbc, fhits, null, null, null, null);
			return true;
		}
	
		rbc.updateListsListWithClusterInfo(fhits, clusters);
				
		//3) find the segments from the fitted clusters
		SegmentFinder segFinder = new SegmentFinder();
		segments =  segFinder.get_Segments(clusters, event);
 
		if(segments.size()==0) { // need 6 segments to make a trajectory			
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, null, null, null);
			return true;
		}
							
		CrossMaker crossMake = new CrossMaker();
		crosses = crossMake.find_Crosses(segments);
 
		if(crosses.size()==0 ) {			
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, null, null);
			return true;
		}

		CrossListFinder crossLister = new CrossListFinder();
		
		List<List<Cross>> CrossesInSector = crossLister.get_CrossesInSectors(crosses);
		for(int s =0; s< 6; s++) {
			if(CrossesInSector.get(s).size()>Constants.MAXNBCROSSES) {
				return true;
			}
		}
		
		CrossList crosslist = crossLister.candCrossLists(crosses);
		
		if(crosslist.size()==0) {
			
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null);
			return true;
		}

		//6) find the list of  track candidates
		TrackCandListFinder trkcandFinder = new TrackCandListFinder("HitBased");
		trkcands = trkcandFinder.getTrackCands(crosslist) ;
		 
		if(trkcands.size()==0) {
			
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null); // no cand found, stop here and save the hits, the clusters, the segments, the crosses
			return true;
		}
		// track found		
		for(Track trk: trkcands) {				
			for(Cross c : trk) { 
				for(FittedHit h1 : c.get_Segment1())
					h1.set_AssociatedHBTrackID(trk.get_Id());
			  	for(FittedHit h2 : c.get_Segment2())
			  		h2.set_AssociatedHBTrackID(trk.get_Id());	
			}
		}
	  
		rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, trkcands);

		return true;
	}

	public void setRunConditionsParameters(DataEvent event) {
		if(event.hasBank("RUN::config")==false) {
			System.err.println("RUN CONDITIONS NOT READ!");
			return;
		}
		boolean isMC = false;
		boolean isCosmics = false;
		
	
        DataBank bank = event.getBank("RUN::config");	
		if(bank.getByte("type", 0)==0)
			isMC = true;
		if(bank.getByte("mode", 0)==1)
			isCosmics = true;
		// force cosmics
		//isCosmics = true;
		//System.out.println(bank.getInt("Event")[0]);
		boolean isCalib = isCosmics;  // all cosmics runs are for calibration right now
		//
		
		// Load the fields
		//-----------------
		String newConfig = "SOLENOID"+bank.getFloat("solenoid",0)+"TORUS"+bank.getFloat("torus",0);		
		//System.out.println(" fields "+newConfig);
		if (FieldsConfig.equals(newConfig)==false) {
			// Load the Constants
			Constants.Load(isCosmics, isCalib, (double)bank.getFloat("torus",0)); // set the T2D Grid for Cosmics data only so far....
			// Load the Fields
			//DCSwimmer.setMagneticFieldsScales(1.0, bank.getFloat("Torus")[0]); // something changed in the configuration ... 
			DCSwimmer.setMagneticFieldsScales(bank.getFloat("solenoid",0), bank.getFloat("torus",0)); // something changed in the configuration ... 
		}
		FieldsConfig = newConfig;
		
		// Load the constants
		//-------------------
		int newRun = bank.getInt("run", 0);
		
		if(Run!=newRun) {
			//CalibrationConstantsLoader.Load(newRun, "default");
			CalibrationConstantsLoader.Load(newRun, "dc_test1");
			TableLoader.Fill();
			
			GeometryLoader.Load(newRun, "default");
		}
		Run = newRun;
		
	}
	public static void main(String[] args) throws FileNotFoundException, EvioException{
		 
		//String inputFile = "/Users/ziegler/Workdir/Files/GEMC/ForwardTracks/ele.run11.rJun7.f1.p0.th1.ph2.evio";
		//String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-3.0.1/gemc_eppippim_A0001_gen.evio";
		//String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/gemc_generated.hipo";
		String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/gemc_GagiksTest.hipo";
		//String inputFile = args[0];
		//String outputFile = args[1];
		
		System.err.println(" \n[PROCESSING FILE] : " + inputFile);
		
		HeaderEngine enh = new HeaderEngine();
		enh.init();
		
		DCHBEngine en = new DCHBEngine();
		en.init();
		DCTBEngine en2 = new DCTBEngine();
		//DCTBRasterEngine en2 = new DCTBRasterEngine();
		en2.init();
		//org.jlab.io.evio.EvioSource reader = new org.jlab.io.evio.EvioSource();
		
		int counter = 0;
		
		//reader.open(inputFile);
		//long t1 = System.currentTimeMillis();
		
		 HipoDataSource reader = new HipoDataSource();
         reader.open(inputFile);
		
		//Writer
		
		//String outputFile="/Users/ziegler/Workdir/Distribution/coatjava-3.0.1/DCRBREC.evio";
		//String outputFile="/Users/ziegler/Workdir/Distribution/coatjava-3.0.4/T2DRec15deg.ev";
		//org.jlab.io.evio.DataSync writer = new org.jlab.io.evio.DataSync();
		//writer.open(outputFile);
		
		long t1=0;
		while(reader.hasEvent() ){
			
			counter++;
		
			DataEvent event = reader.getNextEvent();
			if(counter>0)
				t1 = System.currentTimeMillis();
			
			//enh.processDataEvent(event);
			
			en.processDataEvent(event);
			
			// Processing TB   
			en2.processDataEvent(event);
			//System.out.println("  EVENT "+counter);
			if(counter>1) break;
			//if(counter%100==0)
				System.out.println("run "+counter+" events");
			//writer.writeEvent(event);
		}
		//writer.close();
		double t = System.currentTimeMillis()-t1;
		System.out.println(t1+" TOTAL  PROCESSING TIME = "+(t/(float)counter));
	 }
	
}
