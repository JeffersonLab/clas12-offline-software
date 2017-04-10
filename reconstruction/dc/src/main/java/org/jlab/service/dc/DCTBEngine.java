package org.jlab.service.dc;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
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
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;
import org.jlab.rec.dc.trajectory.RoadFinder;

public class DCTBEngine extends ReconstructionEngine {

	private ClusterFitter cf;
    private ClusterCleanerUtilities ct;    
    private List<FittedHit> hits;
    private List<FittedHit> fhits;
    private List<FittedCluster> clusters;
    private List<Segment> segments;
    private List<Cross> crosses;
    private List<Track> trkcands;
    private RecoBankWriter rbc;
    private HitReader hitRead;	
    private ClusterFinder clusFinder;	
    private SegmentFinder segFinder;
    private RoadFinder pcrossLister;
    private CrossMaker crossMake;
    private CrossListFinder crossLister;	
    private List<List<Cross>> CrossesInSector;	
    private CrossList crosslist;	
    private TrackCandListFinder trkcandFinder;
	
	public DCTBEngine() {
		super("DCTB","ziegler","4.0");
		
		cf = new ClusterFitter();
	    ct = new ClusterCleanerUtilities();    
	    hits = new ArrayList<FittedHit>();
	    fhits = new ArrayList<FittedHit>();
		clusters = new ArrayList<FittedCluster>();
		segments = new ArrayList<Segment>();
		crosses = new ArrayList<Cross>();
		trkcands = new ArrayList<Track>();
		rbc = new RecoBankWriter();
		hitRead = new HitReader();	
		clusFinder = new ClusterFinder();	
		segFinder = new SegmentFinder();
		pcrossLister = new RoadFinder();	
		crossMake = new CrossMaker();
		crossLister = new CrossListFinder();	
		CrossesInSector = new ArrayList<List<Cross>>();	
		crosslist = new CrossList();	
		trkcandFinder = new TrackCandListFinder("TimeBased");
	}

	@Override
	public boolean init() {
		
		return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
		//System.out.println(" RUNNING TIME BASED....................................");
		hits.clear();	
		fhits.clear();	
		clusters.clear();		
		CrossesInSector.clear();	
		crosslist.clear();	
		segments.clear();
		crosses.clear();
		trkcands.clear();
		
		hitRead.read_HBHits(event);

		//I) get the hits
		hits = hitRead.get_HBHits();
		
		//II) process the hits

		//1) exit if hit list is empty
		if(hits.size()==0 ) {
			return true;
		}
		
		//2) find the clusters from these hits
		clusters = clusFinder.FindTimeBasedClusters(hits, cf, ct);
		
		if(clusters.size()==0) {
			rbc.fillAllTBBanks(event, rbc, hits, null, null, null, null);
			return true;
		}
		
		//3) find the segments from the fitted clusters
		segments =  segFinder.get_Segments(segFinder.selectTimeBasedSegments(clusters), event);
		
		if(segments.size()==0) { // need 6 segments to make a trajectory
			
			for(FittedCluster c : clusters) {					
				for(FittedHit hit : c) {		
					hit.set_AssociatedClusterID(c.get_Id());
					hit.set_AssociatedHBTrackID(c.get(0).get_AssociatedHBTrackID());
					fhits.add(hit);						
				}
			}
			rbc.fillAllTBBanks( event, rbc, fhits, clusters, null, null, null);
			return true;
		}
		
		for(Segment seg : segments) {					
			for(FittedHit hit : seg.get_fittedCluster()) {						
				fhits.add(hit);						
			}
		}
		
		//RoadFinder
		//
		segments.addAll(pcrossLister.findRoads(segments));		
		//
		//System.out.println("nb trk segs "+pSegments.size());
		crosses = crossMake.find_Crosses(segments);
				
		if(crosses.size()==0 ) {			
			rbc.fillAllTBBanks(event, rbc, fhits, clusters, segments, null, null);
			return true;
		}
		
		//5) make list of crosses consistent with a track candidate
		crosslist = crossLister.candCrossLists(crosses);
		
		if(crosslist.size()==0) {			
			//System.out.println(" Failed on cross list !");
			rbc.fillAllTBBanks(event, rbc, fhits, clusters, segments, crosses, null);
			return true;
		}
			
		
		//6) find the list of  track candidates
		trkcandFinder = new TrackCandListFinder("TimeBased");
		trkcands = trkcandFinder.getTrackCands(crosslist) ;				
		if(trkcands.size()==0) {			
			rbc.fillAllTBBanks( event, rbc, fhits, clusters, segments, crosses, null); // no cand found, stop here and save the hits, the clusters, the segments, the crosses
			return true;
		}
		
		trkcandFinder.removeOverlappingTracks(trkcands);	
		
		int trkId = 1;
		for(Track trk: trkcands) {
			// reset the id
			trk.set_Id(trkId); 
			for(Cross c : trk) { 
				for(FittedHit h1 : c.get_Segment1())
					h1.set_AssociatedTBTrackID(trk.get_Id());
			  	for(FittedHit h2 : c.get_Segment2())
			  		h2.set_AssociatedTBTrackID(trk.get_Id());	
			}
			trkId++;
		}
		
		
		/*
		for(Track trk : trkcands) {
			for(Cross c : trk) {
				for(Segment s : c) {
					for(FittedHit h : s) {
						h.set_AssociatedTBTrackID(trk.get_Id()); 
					}
				}
			}
		} */
		
		rbc.fillAllTBBanks( event, rbc, fhits, clusters, segments, crosses, trkcands);

			
		return true;
	}

}
