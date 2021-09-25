/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.dc;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.swimtools.Swimmer;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.HitReader;
import org.jlab.rec.dc.banks.RecoBankWriter;
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
import org.jlab.rec.dc.trajectory.Road;
import org.jlab.rec.dc.trajectory.RoadFinder;

/**
 *
 * @author ziegler
 */
public class DCHBPostClusterConv extends DCEngine {
    public DCHBPostClusterConv() {
        super("DCHB");
    }
    
    @Override
    public void initBankNames() {
        this.getBankNames().setHitsInputBank("HitBasedTrkg::HBHits");
        this.getBankNames().setClustersInputBank("HitBasedTrkg::HBClusters");
        this.getBankNames().setHitsBank("HitBasedTrkg::HBHits");
        this.getBankNames().setClustersBank("HitBasedTrkg::HBClusters");
        this.getBankNames().setSegmentsBank("HitBasedTrkg::HBSegments");
        this.getBankNames().setCrossesBank("HitBasedTrkg::HBCrosses");
        this.getBankNames().setTracksBank("HitBasedTrkg::HBTracks");
        this.getBankNames().setIdsBank("HitBasedTrkg::HBHitTrkId");
        
        super.registerOutputBank("HitBasedTrkg::HBSegments");
        super.registerOutputBank("HitBasedTrkg::HBCrosses");
        super.registerOutputBank("HitBasedTrkg::HBTracks");
        super.registerOutputBank("HitBasedTrkg::HBHitTrkId");
    }
        
    @Override
    public boolean processDataEvent(DataEvent event) {
    
        int run = this.getRun(event);
        if(run==0) return true;
        
        /* IO */
        HitReader      reader = new HitReader(this.getBankNames());
        RecoBankWriter writer = new RecoBankWriter(this.getBankNames());
        // get Field
        Swim dcSwim = new Swim();
     
        List<Track> trkcands = null;
        List<Cross> crosses = null;
        List<FittedCluster> clusters = null;
        List<Segment> segments = null;
        List<FittedHit> fhits = null;
        
        //2) find the clusters from these hits
        fhits = new ArrayList<FittedHit>();
        ClusterFinder clusFinder = new ClusterFinder();
        ClusterFitter cf = new ClusterFitter();
        clusters = clusFinder.RecomposeClusters(event, Constants.dcDetector, cf);
        if (clusters ==null || clusters.isEmpty()) {
            return true;
        }

        //3) find the segments from the fitted clusters
        SegmentFinder segFinder = new SegmentFinder();
        segments = segFinder.get_Segments(clusters,
                event,
                Constants.dcDetector, false);

        /* 15 */
        // need 6 segments to make a trajectory
        if (segments.isEmpty()) {
            return true;
        }
        List<Segment> rmSegs = new ArrayList<>();
        // clean up hit-based segments
        double trkDocOverCellSize;
        for (Segment se : segments) {
            trkDocOverCellSize = 0;
            for (FittedHit fh : se.get_fittedCluster()) {
                trkDocOverCellSize += fh.get_ClusFitDoca() / fh.get_CellSize();
            }
            if (trkDocOverCellSize / se.size() > 1.1) {
                rmSegs.add(se);
            }
        }
        segments.removeAll(rmSegs);
        if(segments == null || segments.isEmpty())
            return true;
        /* 16 */
        CrossMaker crossMake = new CrossMaker();
        crosses = crossMake.find_Crosses(segments, Constants.dcDetector);
        if (crosses.isEmpty()) {
            event.appendBanks(
                    writer.fillHBSegmentsBank(event, segments));
            return true;
        }
        /* 17 */
        CrossListFinder crossLister = new CrossListFinder();

        CrossList crosslist = crossLister.candCrossLists(event, crosses,
                false,
                null,
                Constants.dcDetector,
                null,
                dcSwim, false);
        /* 18 */
        //6) find the list of  track candidates
        TrackCandListFinder trkcandFinder = new TrackCandListFinder(Constants.HITBASE);
        trkcands = trkcandFinder.getTrackCands(crosslist,
                Constants.dcDetector,
                Swimmer.getTorScale(),
                dcSwim, false);
        /* 19 */
        // track found
        int trkId = 1;
        if (trkcands.size() > 0) {
            // remove overlaps
            trkcandFinder.removeOverlappingTracks(trkcands);
            for (Track trk : trkcands) {
                // reset the id
                trk.set_Id(trkId);
                trkcandFinder.matchHits(trk.get_Trajectory(),
                        trk,
                        Constants.dcDetector,
                        dcSwim);
                trkId++;
            }
        }
        List<Segment> crossSegsNotOnTrack = new ArrayList<>();
        List<Segment> psegments = new ArrayList<>();

        for (Cross c : crosses) {
            if (!c.get_Segment1().isOnTrack)
                crossSegsNotOnTrack.add(c.get_Segment1());
            if (!c.get_Segment2().isOnTrack)
                crossSegsNotOnTrack.add(c.get_Segment2());
        }

        RoadFinder rf = new RoadFinder();
        List<Road> allRoads = rf.findRoads(segments, Constants.dcDetector);
        List<Segment> Segs2Road = new ArrayList<>();
        for (Road r : allRoads) { 
            Segs2Road.clear();
            int missingSL = -1;
            for (int ri = 0; ri < 3; ri++) {
                if (r.get(ri).associatedCrossId == -1) {
                    if (r.get(ri).get_Superlayer() % 2 == 1) {
                        missingSL = r.get(ri).get_Superlayer() + 1;
                    } else {
                        missingSL = r.get(ri).get_Superlayer() - 1;
                    }
                }
            } 
            if(missingSL==-1) 
                continue;
            for (int ri = 0; ri < 3; ri++) {
                for (Segment s : crossSegsNotOnTrack) {
                    if (s.get_Sector() == r.get(ri).get_Sector() &&
                            s.get_Region() == r.get(ri).get_Region() &&
                            s.associatedCrossId == r.get(ri).associatedCrossId &&
                            r.get(ri).associatedCrossId != -1) {
                        if (s.get_Superlayer() % 2 == missingSL % 2)
                            Segs2Road.add(s); 
                    }
                }
            }
            if (Segs2Road.size() == 2) {
                Segment pSegment = rf.findRoadMissingSegment(Segs2Road,
                        Constants.dcDetector,
                        r.a);
                if (pSegment != null)
                    psegments.add(pSegment);
            }
        }

        segments.addAll(psegments);
        List<Cross> pcrosses = crossMake.find_Crosses(segments, Constants.dcDetector);

        CrossList pcrosslist = crossLister.candCrossLists(event, pcrosses,
                false,
                null,
                Constants.dcDetector,
                null,
                dcSwim, true);
        //pcrosslist.removeDuplicates(crosslist); 

        List<Track> mistrkcands = trkcandFinder.getTrackCands(pcrosslist,
                Constants.dcDetector,
                Swimmer.getTorScale(),
                dcSwim, false);

        // remove overlaps
        if (mistrkcands.size() > 0) {
            trkcandFinder.removeOverlappingTracks(mistrkcands);
            for (Track trk : mistrkcands) {

                // reset the id
                trk.set_Id(trkId);
                trkcandFinder.matchHits(trk.get_Trajectory(),
                        trk,
                        Constants.dcDetector,
                        dcSwim);
                trkId++;
            }
        }

        trkcands.addAll(mistrkcands);
        if(Constants.DEBUG) {
        System.out.println("Found after 5STg "+mistrkcands.size()+" HB seeds ");
            for(int i = 0; i< trkcands.size(); i++) {
                System.out.println("cand "+i);
                for(Cross c : trkcands.get(i)) {
                    System.out.println(c.printInfo());
                }
                System.out.println("------------------------------------------------------------------ ");
            }
        }
        //gather all the hits for pointer bank creation
        for (Track trk : trkcands) {
            for (Cross c : trk) {
                c.set_CrossDirIntersSegWires();
                trkcandFinder.setHitDoubletsInfo(c.get_Segment1());
                trkcandFinder.setHitDoubletsInfo(c.get_Segment2());
                for (FittedHit h1 : c.get_Segment1()) {
//                        h1.setSignalPropagTimeAlongWire(dcDetector); //PASS1, not necessary because hits were already updated in trkcandFinder.matchHits
//                        h1.setSignalTimeOfFlight();                  //PASS1
                    if(h1.get_AssociatedHBTrackID()>0) fhits.add(h1);
                }
                for (FittedHit h2 : c.get_Segment2()) {
//                        h2.setSignalPropagTimeAlongWire(dcDetector); //PASS1
//                        h2.setSignalTimeOfFlight();                  //PASS1
                    if(h2.get_AssociatedHBTrackID()>0) fhits.add(h2);
                }
            }
        }
        
                // no candidate found, stop here and save the hits,
        // the clusters, the segments, the crosses
        if (trkcands.isEmpty()) {
             event.appendBanks(
                    writer.fillHBSegmentsBank(event, segments),
                    writer.fillHBCrossesBank(event, crosses));
        } else {
            event.appendBanks(
                    writer.fillHBSegmentsBank(event, segments),
                    writer.fillHBCrossesBank(event, crosses),
                    writer.fillHBTracksBank(event, trkcands),
                    writer.fillHBHitsTrkIdBank(event, fhits));
        }
        return true;
    }
}