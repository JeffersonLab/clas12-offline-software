package org.jlab.service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.coda.jevio.EvioException;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.geom.detector.dc.DCFactoryUpdated;
import org.jlab.io.base.DataEvent;
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
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;
import org.jlab.rec.dc.trajectory.DCSwimmer;

import cnuphys.magfield.MagneticField;
import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;



public class DCEngine extends ReconstructionEngine {


    Detector dcDetector = null;
    
    public DCEngine(){
        super("DC","ziegler","3.0");
    }
    
   
    
	@Override
    public boolean processDataEvent(DataEvent event) {
	
		
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
		
		
		HitReader hitRead = new HitReader();
		hitRead.fetch_DCHits(event, noiseAnalysis, parameters, results);

		
		List<Hit> hits = new ArrayList<Hit>();
		//I) get the hits
		hits = hitRead.get_DCHits();
		
		
		
		//II) process the hits

		//1) exit if hit list is empty
		if(hits.size()==0 ) {
			return false;
		}

		//fhits = rbc.createRawHitList(hits);
				
		
		//2) find the clusters from these hits
			ClusterFinder clusFinder = new ClusterFinder();
			clusters = clusFinder.FindHitBasedClusters(hits, ct, cf);
		
			
			
			if(clusters.size()==0) {				
				//rbc.fillAllHBBanks(event, rbc, fhits, null, null, null, null,null);
				return false;
			}
		
			
			rbc.updateListsListWithClusterInfo(fhits, clusters);
			
			
			//3) find the segments from the fitted clusters
			SegmentFinder segFinder = new SegmentFinder();
			segments =  segFinder.get_Segments(clusters, event);
			
			
			if(segments.size()==0) { // need 6 segments to make a trajectory
				
				//rbc.fillAllHBBanks(event, rbc, fhits, clusters, null, null, null, effbank);
				return false;
			}
								
			CrossMaker crossMake = new CrossMaker();
			crosses = crossMake.find_Crosses(segments);
			
			

			if(crosses.size()==0 ) {
				
				//rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, null, null, effbank);
				return false;
			}
			
			//5) make list of crosses consistent with a track candidate
			CrossListFinder crossLister = new CrossListFinder();
			
			CrossList crosslist = crossLister.candCrossLists(crosses);
			
			if(crosslist.size()==0) {
				
				//rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null, effbank);
				return false;
			}

			
			
			
			
			//6) find the list of  track candidates
			TrackCandListFinder trkcandFinder = new TrackCandListFinder("HitBased");
			trkcands = trkcandFinder.getTrackCands(crosslist) ;
			
				
			
			if(trkcands.size()==0) {
				
				//rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, null,effbank); // no cand found, stop here and save the hits, the clusters, the segments, the crosses
				return false;
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
		
			//rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, trkcands, effbank);
			
			
		
		///////////////////////////////
    	/*
        List<FittedCluster> clusters = clusFinder.FindHitBasedClusters(dcHits, ct, cf);
		
        
        for(int i =0; i< dcWires.size(); i++) {
            bank.setInt("id",i, dcWires.get(i).get_Id());
			bank.setInt("superlayer",i, dcWires.get(i).get_Superlayer());
			bank.setInt("layer",i, dcWires.get(i).get_Layer());
			bank.setInt("sector",i, dcWires.get(i).get_Sector());
			bank.setInt("wire",i, dcWires.get(i).get_Wire());
			bank.setDouble("time",i, dcWires.get(i).getTime());
			bank.setDouble("doca",i, dcWires.get(i).get_CellSize());
			bank.setDouble("docaError",i, dcWires.get(i).get_CellSize()/Math.sqrt(12));
        }
        */
      

       
        return true;
    }

    @Override
    public boolean init() {
      
    	// init the geometry
        //dcDetector =  GeometryFactory.getDetector(DetectorType.DC);
        ConstantProvider  provider = GeometryFactory.getConstants(DetectorType.DC, 11, "default");
        dcDetector = (new DCFactoryUpdated()).createDetectorTilted(provider);
        // init the calibration constants
       
    	if (GeometryLoader.isGeometryLoaded == false) {
			GeometryLoader.Load();
		}

		// Load the Constants
		if (Constants.areConstantsLoaded == false) {
			Constants.Load();
			System.out.println(" DB VARIATION LOADED AS "+Constants.DBVAR);
		}
		// Load the calibration constants
		if (CalibrationConstantsLoader.CSTLOADED == false) {
			CalibrationConstantsLoader.Load();
		}
		MagneticField.setUseFastMath(true);
		// Load the fields
		if (DCSwimmer.areFieldsLoaded == false) {
			DCSwimmer.getMagneticFields();
		}
		System.out.println(" **************   Magnetic Fields FastMath **************** "+MagneticField.useFastMath()); 
        return true;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    public static void main(String[] args) throws FileNotFoundException, EvioException{
		 
		String inputFile = "/Users/ziegler/Workdir/Files/GEMC/ForwardTracks/ele.run11.rJun7.f1.p0.th1.ph2.evio";
		
		//String inputFile = args[0];
		//String outputFile = args[1];
		
		System.err.println(" \n[PROCESSING FILE] : " + inputFile);

		DCEngine en = new DCEngine();
		en.init();
		org.jlab.io.evio.EvioSource reader = new org.jlab.io.evio.EvioSource();
		
		int counter = 0;
		
		reader.open(inputFile);
		long t1 = System.currentTimeMillis();
		while(reader.hasEvent()){
			
			counter++;
			org.jlab.io.evio.EvioDataEvent event = (org.jlab.io.evio.EvioDataEvent) reader.getNextEvent();
			en.processDataEvent(event);
			if(counter>500) break;
			//if(counter%100==0)
			//	System.out.println("run "+counter+" events");
			
		}
		double t = System.currentTimeMillis()-t1;
		System.out.println("  PROCESSING TIME = "+t);
	 }

}