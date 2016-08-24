package org.jlab.service.dc;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
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
import org.jlab.rec.dc.trajectory.DCSwimmer;

public class DCTBEngine extends ReconstructionEngine {

	public DCTBEngine() {
		super("DCTB","ziegler","3.0");
	}

	@Override
	public boolean init() {
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
		// Load the fields
		if (DCSwimmer.areFieldsLoaded == false) {
			DCSwimmer.getMagneticFields();
		}
		 // Load the time-to-distance function
		if( TableLoader.T2DLOADED == false) {
			TableLoader.Fill();
		}
		return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
		
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
		hitRead.read_HBHits(event);

		List<FittedHit> hits = new ArrayList<FittedHit>();
		//I) get the hits
		hits = hitRead.get_HBHits();
		
		//II) process the hits

		//1) exit if hit list is empty
		if(hits.size()==0 ) {
			return true;
		}
		
		//2) find the clusters from these hits
		ClusterFinder clusFinder = new ClusterFinder();
		
		clusters = clusFinder.FindTimeBasedClusters(hits, cf, ct);
		
		if(clusters.size()==0) {
			rbc.fillAllTBBanks((EvioDataEvent) event, rbc, hits, null, null, null, null);
			return true;
		}
		
		//3) find the segments from the fitted clusters
		SegmentFinder segFinder = new SegmentFinder();
		segments =  segFinder.get_Segments(clusters, event);
		
		
	
		
		if(segments.size()==0) { // need 6 segments to make a trajectory
			
			for(FittedCluster c : clusters) {					
				for(FittedHit hit : c) {		
					hit.set_AssociatedClusterID(c.get_Id());
					hit.set_AssociatedHBTrackID(c.get(0).get_AssociatedHBTrackID());
					fhits.add(hit);						
				}
			}
			rbc.fillAllTBBanks((EvioDataEvent) event, rbc, fhits, clusters, null, null, null);
			return true;
		}
		
		for(Segment seg : segments) {					
			for(FittedHit hit : seg.get_fittedCluster()) {		
				
				fhits.add(hit);						
			}
		}
		
		CrossMaker crossMake = new CrossMaker();
		crosses = crossMake.find_Crosses(segments);
		
		
		if(crosses.size()==0 ) {
			
			rbc.fillAllTBBanks((EvioDataEvent) event, rbc, fhits, clusters, segments, null, null);
			return true;
		}
		
		//5) make list of crosses consistent with a track candidate
		CrossListFinder crossLister = new CrossListFinder();		
		
		CrossList crosslist = crossLister.candCrossLists(crosses);
		
		if(crosslist.size()==0) {			
			
			rbc.fillAllTBBanks((EvioDataEvent) event, rbc, fhits, clusters, segments, crosses, null);
			return true;
		}
			
		
		//6) find the list of  track candidates
		TrackCandListFinder trkcandFinder = new TrackCandListFinder("TimeBased");
		trkcands = trkcandFinder.getTrackCands(crosslist) ;
		
		
		if(trkcands.size()==0) {
			
			rbc.fillAllTBBanks((EvioDataEvent) event, rbc, fhits, clusters, segments, crosses, null); // no cand found, stop here and save the hits, the clusters, the segments, the crosses
			return true;
		}
		
		trkcandFinder.removeOverlappingTracks(trkcands);		
		
		rbc.fillAllTBBanks((EvioDataEvent) event, rbc, fhits, clusters, segments, crosses, trkcands);

			
		return true;
	}

}
