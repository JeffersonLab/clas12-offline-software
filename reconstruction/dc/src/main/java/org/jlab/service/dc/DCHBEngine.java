package org.jlab.service.dc;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
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
import org.jlab.rec.dc.trajectory.RoadFinder;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;

public class DCHBEngine extends ReconstructionEngine {

	 // init  
    private Clas12NoiseResult results; 
    private Clas12NoiseAnalysis noiseAnalysis;
    private int[] rightShifts;
    private int[] leftShifts;	
    private NoiseReductionParameters parameters;
	
    private ClusterFitter cf;
    private ClusterCleanerUtilities ct;    
    private List<FittedHit> fhits;
    private List<FittedCluster> clusters;
    private List<Segment> segments;
    private List<Cross> crosses;
    private List<Track> trkcands;
    private RecoBankWriter rbc;
    private HitReader hitRead;	
    private List<Hit> hits;
    private ClusterFinder clusFinder;	
    private SegmentFinder segFinder;
    private RoadFinder pcrossLister;
    private CrossMaker crossMake;
    private CrossListFinder crossLister;	
    private List<List<Cross>> CrossesInSector;	
    private CrossList crosslist;	
    private TrackCandListFinder trkcandFinder;
	
	public DCHBEngine() {
		super("DCHB","ziegler","4.0");
		 // init  
	    results = new Clas12NoiseResult(); 
		noiseAnalysis = new Clas12NoiseAnalysis();
		rightShifts = Constants.SNR_RIGHTSHIFTS;
		leftShifts  = Constants.SNR_LEFTSHIFTS;	
		parameters = new NoiseReductionParameters (
				2,leftShifts,
				rightShifts);
		cf = new ClusterFitter();
	    ct = new ClusterCleanerUtilities();    
	    fhits = new ArrayList<FittedHit>();
		clusters = new ArrayList<FittedCluster>();
		segments = new ArrayList<Segment>();
		crosses = new ArrayList<Cross>();
		trkcands = new ArrayList<Track>();
		rbc = new RecoBankWriter();
		hitRead = new HitReader();	
		hits = new ArrayList<Hit>();
		clusFinder = new ClusterFinder();	
		segFinder = new SegmentFinder();
		pcrossLister = new RoadFinder();	
		crossMake = new CrossMaker();
		crossLister = new CrossListFinder();	
		CrossesInSector = new ArrayList<List<Cross>>();	
		crosslist = new CrossList();	
		trkcandFinder = new TrackCandListFinder("HitBased");
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
		
		results.clear();
		noiseAnalysis.clear();
		fhits.clear();
		clusters.clear();
		hits.clear();	
		CrossesInSector.clear();	
		crosslist.clear();	
		segments.clear();
		crosses.clear();
		trkcands.clear();
		
		hitRead.fetch_DCHits(event, noiseAnalysis, parameters, results);

		//I) get the hits
		hits = hitRead.get_DCHits();
		
		//II) process the hits
		//1) exit if hit list is empty
		if(hits.size()==0 ) {
			return true;
		}

		fhits = rbc.createRawHitList(hits);
						
		//2) find the clusters from these hits
		clusters = clusFinder.FindHitBasedClusters(hits, ct, cf);
			
		if(clusters.size()==0) {				
			rbc.fillAllHBBanks(event, rbc, fhits, null, null, null, null);
			return true;
		}
	
		rbc.updateListsListWithClusterInfo(fhits, clusters);
		
		//3) find the segments from the fitted clusters
		segments =  segFinder.get_Segments(clusters, event);
 
		if(segments.size()==0) { // need 6 segments to make a trajectory			
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, null, null, null);
			return true;
		}
		//RoadFinder
		//
		segments.addAll(pcrossLister.findRoads(segments));
		
		//
		crosses = crossMake.find_Crosses(segments);
 
		if(crosses.size()==0 ) {			
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, null, null);
			return true;
		}
		CrossesInSector = crossLister.get_CrossesInSectors(crosses);
		for(int s =0; s< 6; s++) {
			if(CrossesInSector.get(s).size()>Constants.MAXNBCROSSES) {
				rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null);
				return true;
			}
		}
		
		crosslist = crossLister.candCrossLists(crosses);		
		if(crosslist.size()==0) {			
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null);
			return true;
		}

		//6) find the list of  track candidates
		trkcandFinder = new TrackCandListFinder("HitBased");		
		trkcands = trkcandFinder.getTrackCands(crosslist) ;
		 
		if(trkcands.size()==0) {			
			rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null); // no cand found, stop here and save the hits, the clusters, the segments, the crosses
			return true;
		}
		// track found	
		int trkId = 1;
		for(Track trk: trkcands) {						
			for(Cross c : trk) { 
				for(FittedHit h1 : c.get_Segment1())
					h1.set_AssociatedHBTrackID(trk.get_Id());
			  	for(FittedHit h2 : c.get_Segment2())
			  		h2.set_AssociatedHBTrackID(trk.get_Id());	
			}
			
		}
	  
		trkcandFinder.removeOverlappingTracks(trkcands);		// remove overlaps
		
		for(Track trk: trkcands) {		
			// reset the id
			trk.set_Id(trkId);
			for(Cross c : trk) { 
				for(FittedHit h1 : c.get_Segment1())
					h1.set_AssociatedHBTrackID(trk.get_Id());
			  	for(FittedHit h2 : c.get_Segment2())
			  		h2.set_AssociatedHBTrackID(trk.get_Id());	
			}
			trkId++;
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
		
		
		// Load the constants
		//-------------------
		int newRun = bank.getInt("run", 0);
		boolean T2DCalc = false;
		
		if(Run!=newRun) {
			if(newRun>751 && newRun<912) {
				T2DCalc = true;
				Constants.setT0(true);		
				Constants.setUseMiniStagger(true);
			}
			if(newRun==9)
				T2DCalc = true;
			
			System.out.println("   SETTING RUN-DEPENDENT CONSTANTS, T0 = "+Constants.getT0()+ " use ministagger "+Constants.getUseMiniStagger());
			CalibrationConstantsLoader.Load(newRun, "default");
			
			TableLoader.Fill();
			
			GeometryLoader.Load(newRun, "default");
		}
		Run = newRun;

		
		// Load the fields
		//-----------------
		String newConfig = "SOLENOID"+bank.getFloat("solenoid",0)+"TORUS"+bank.getFloat("torus",0)+"RUN"+bank.getInt("run", 0);		
		//System.out.println(" fields "+newConfig);
		if (FieldsConfig.equals(newConfig)==false) {
			// Load the Constants
			double TorScale = (double)bank.getFloat("torus",0);
			//TorScale = -0.5;
			
			Constants.Load(T2DCalc, isCalib, TorScale); // set the T2D Grid for Cosmics data only so far....
			// Load the Fields
			//DCSwimmer.setMagneticFieldsScales(1.0, bank.getFloat("Torus")[0]); // something changed in the configuration ... 
			DCSwimmer.setMagneticFieldsScales(bank.getFloat("solenoid",0), TorScale); // something changed in the configuration ... 
		}
		FieldsConfig = newConfig;
		
	}
	public static void main(String[] args) throws FileNotFoundException, EvioException{
		
		//String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/clas_000767_000.hipo";
		String inputFile = "/Users/ziegler/Workdir/Distribution/CLARA/CLARA_INSTALL/data/in/clas12_000797_a00000.hipo";
		//String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/e2to6hipo.hipo";
		// String inputFile="/Users/ziegler/Downloads/out.hipo";
		//String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/Run758.hipo";
		//String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/old/RaffaNew.hipo";
		//String inputFile = args[0];
		//String outputFile = args[1];
		
		System.err.println(" \n[PROCESSING FILE] : " + inputFile);
		
		DCHBEngine en = new DCHBEngine();
		en.init();
		DCTBEngine en2 = new DCTBEngine();		
		en2.init();
		
		int counter = 0;
		
		 HipoDataSource reader = new HipoDataSource();
         reader.open(inputFile);
		
         HipoDataSync writer = new HipoDataSync();
		//Writer
		 String outputFile="/Users/ziegler/Workdir/Distribution/DCTest_797D2GC.hipo";
		
		 writer.open(outputFile);
		
		long t1=0;
		while(reader.hasEvent() ){
			
			counter++;
		
			DataEvent event = reader.getNextEvent();
			if(counter>0)
				t1 = System.currentTimeMillis();
			
		
			
			en.processDataEvent(event);
			
			// Processing TB   
			en2.processDataEvent(event);
			//System.out.println("  EVENT "+counter);
			if(counter>9) break;
			//event.show();
			//if(counter%100==0)
			System.out.println("*************************************************************run "+counter+" events");
			//if(event.hasBank("HitBasedTrkg::HBTracks")) {
				writer.writeEvent(event); 
			//}
		}
		writer.close();
		double t = System.currentTimeMillis()-t1;
		System.out.println(t1+" TOTAL  PROCESSING TIME = "+(t/(float)counter));
	 }
	
}
