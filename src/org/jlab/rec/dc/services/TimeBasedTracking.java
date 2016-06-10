package org.jlab.rec.dc.services;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clasrec.main.DetectorReconstruction;
import org.jlab.clasrec.utils.ServiceConfiguration;
import org.jlab.evio.clas12.EvioDataEvent;
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
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.segment.SegmentFinder;
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;
import org.jlab.rec.dc.track.TrackMicroMegasMatching;
import org.jlab.rec.dc.trajectory.DCSwimmer;
//import org.jlab.rec.dc.trajectory.Vertex;
import org.jlab.rec.dc.trajectory.Vertex;

/**
 * Service to return reconstructed DC track candidates from Hit-based tracking.
 *
 * @author ziegler
 */
public class TimeBasedTracking  extends DetectorReconstruction {

	
    public TimeBasedTracking() {

    	super("DCTB", "ziegler", "2.1");
    	
		
    }
	public void configure() {
        
        System.err.println("DCReconstruction::configure is not implemented.");
        
        
    } // end of configure()
	int fidNb = 0;
	int recNb = 0;
	
	ClusterFitter cf = new ClusterFitter();
    ClusterCleanerUtilities ct = new ClusterCleanerUtilities();
    
	@Override
	public void processEvent(EvioDataEvent event) {
				
		List<FittedHit> fhits = new ArrayList<FittedHit>();
		
		List<FittedCluster> clusters = new ArrayList<FittedCluster>();
		List<Segment> segments = new ArrayList<Segment>();
		List<Cross> crosses = new ArrayList<Cross>();
		List<Track> trkcands = new ArrayList<Track>();
		
		//instantiate bank writer
		RecoBankWriter rbc = new RecoBankWriter();
			
		if(Constants.DEBUGPRINTMODE==true)
			System.out.println("*********  TIME-BASED TRACKING  *********");
		
		HitReader hitRead = new HitReader();
		hitRead.read_HBHits(event);

		List<FittedHit> hits = new ArrayList<FittedHit>();
		//I) get the hits
		hits = hitRead.get_HBHits();
		if(Constants.DEBUGPRINTMODE==true)
			System.out.println(" This is TimeBased Tracking. ...\n Getting HB hits from previous service : Nb of hits = "+hits.size());
		//II) process the hits

		//1) exit if hit list is empty
		if(hits.size()==0 ) {
			return;
		}
		
		//2) find the clusters from these hits
		ClusterFinder clusFinder = new ClusterFinder();
		
		clusters = clusFinder.FindTimeBasedClusters(hits, cf, ct);
		
		if(clusters.size()==0) {
			rbc.fillAllTBBanks(event, rbc, hits, null, null, null, null);
			return;
		}
		if(Constants.DEBUGPRINTMODE==true) {
			int NSeg = 0;;
			for(FittedCluster cls : clusters) {
				System.out.println(cls.printInfo());
				for(FittedHit h :cls)
					System.out.println(h.printInfo());
				NSeg+= cls.get_Superlayer();
			}
			if(NSeg == 21)	
				fidNb++;
		}
		
		if(Constants.DEBUGPRINTMODE==true)
		System.out.println("Nb of clusters = "+clusters.size());
		
		
		
		//3) find the segments from the fitted clusters
		SegmentFinder segFinder = new SegmentFinder();
		segments =  segFinder.get_Segments(clusters, event);
		
		
	
		if(Constants.DEBUGPRINTMODE==true)  
			System.out.println("Nb of segments "+segments.size());
		if(segments.size()==0) { // need 6 segments to make a trajectory
			
			for(FittedCluster c : clusters) {					
				for(FittedHit hit : c) {		
					hit.set_AssociatedClusterID(c.get_Id());
					hit.set_AssociatedHBTrackID(c.get(0).get_AssociatedHBTrackID());
					fhits.add(hit);						
				}
			}
			rbc.fillAllTBBanks(event, rbc, fhits, clusters, null, null, null);
			return;
		}
		
		for(Segment seg : segments) {					
			for(FittedHit hit : seg.get_fittedCluster()) {		
				
				fhits.add(hit);						
			}
		}
		
		CrossMaker crossMake = new CrossMaker();
		crosses = crossMake.find_Crosses(segments);
		
		if(Constants.DEBUGPRINTMODE==true)  
			System.out.println("Nb of crosses "+crosses.size());
		if(crosses.size()==0 ) {
			
			rbc.fillAllTBBanks(event, rbc, fhits, clusters, segments, null, null);
			return;
		}
		
		//5) make list of crosses consistent with a track candidate
		CrossListFinder crossLister = new CrossListFinder();
		
		
		
		if(Constants.DEBUGPRINTMODE==true)  
			System.out.println("Nb of crosses "+crosses.size());
		
		CrossList crosslist = crossLister.candCrossLists(crosses);
		
		if(crosslist.size()==0) {
			if(Constants.DEBUGPRINTMODE==true)  
				System.out.println("No cross list found !!!");
			
			rbc.fillAllTBBanks(event, rbc, fhits, clusters, segments, crosses, null);
			return;
		}
			
		
		//6) find the list of  track candidates
		TrackCandListFinder trkcandFinder = new TrackCandListFinder("TimeBased");
		trkcands = trkcandFinder.getTrackCands(crosslist) ;
		
		if(Constants.DEBUGPRINTMODE==true)  
			System.out.println("Nb of tracks "+trkcands.size());
		if(trkcands.size()==0) {
			
			rbc.fillAllTBBanks(event, rbc, fhits, clusters, segments, crosses, null); // no cand found, stop here and save the hits, the clusters, the segments, the crosses
			return;
		}
		
		trkcandFinder.removeOverlappingTracks(trkcands);
		
		if(Constants.turnOnMicroMegas==true) {
			
			TrackMicroMegasMatching mms = new TrackMicroMegasMatching();
			mms.getMicroMegasPoints(event);
			
			for(int k =0; k<trkcands.size(); k++) {
				Track thecand = trkcands.get(k);
				
				mms.matchTrackToMM(thecand);
				mms.reFitTrackWithMicroMegas(thecand, trkcandFinder, 3);
			}
		}
		
		
		// track found
		if(Constants.useRaster==true) {
			
			Vertex vtx = new Vertex();
			for(int k =0; k<trkcands.size(); k++) {
				Track thecand = trkcands.get(k);
				vtx.resetTrackAtRasterRadius(event, thecand);
			}
		}
		
		rbc.fillAllTBBanks(event, rbc, fhits, clusters, segments, crosses, trkcands);
		if(Constants.DEBUGPRINTMODE==true)
			System.out.println("all DCTB banks should be appended !!!");

		
	}
	
	@Override
	public void init() {
		// Load the Geometry
		if (GeometryLoader.isGeometryLoaded == false) {
			GeometryLoader.Load();
		}

		// Load the Constants
		if (Constants.areConstantsLoaded == false) {
			Constants.Load();
		}
		
		// Load the calibration constants
			if (CalibrationConstantsLoader.CSTLOADED == false) {
				CalibrationConstantsLoader.Load();
			}
			this.requireCalibration("DC");
	    // Load the fields
			if (DCSwimmer.areFieldsLoaded == false) {
				DCSwimmer.getMagneticFields();		
			}
	    // Load the time-to-distance function
			if( TableLoader.T2DLOADED == false) {
				TableLoader.Fill();
			}
			
	}
	@Override
	public void configure(ServiceConfiguration config) {
		System.out.println(" CONFIGURING SERVICE DCTB ************************************** ");
		if(config.hasItem("DCTB", "kalman")) {
			String KalmanFlag = config.asString("DCTB", "kalman");
			
			boolean kFlag = Boolean.parseBoolean(KalmanFlag);
			Constants.useKalmanFilter = kFlag;			
			System.out.println("\n\n********** KALMAN ON " + kFlag + "  *************");

		}
		if(config.hasItem("DCTB", "useMicroMegas")) {
			String MMFlag = config.asString("DCTB", "useMicroMegas");
			
			boolean mFlag = Boolean.parseBoolean(MMFlag);
			Constants.turnOnMicroMegas = mFlag;			
			System.out.println("\n\n********** MICROMEGAS ON " + mFlag + "  *************");

		}
		if(config.hasItem("DCTB", "useRaster")) {
			String RFlag = config.asString("DCTB", "useRaster");
			
			boolean rFlag = Boolean.parseBoolean(RFlag);
			Constants.useRaster = rFlag;			
			System.out.println("\n\n********** RASTER ? " + rFlag + "  *************");

		}
		if(config.hasItem("DCTB", "debug")) {
			int debug = config.asInteger("DCTB", "debug");
			if(debug>0)
				System.out.println("************************************** VERSION DCREC 2.0 ****************************************");
			}
	}
	
}
