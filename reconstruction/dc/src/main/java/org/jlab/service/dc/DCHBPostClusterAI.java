/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.dc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.swimtools.Swimmer;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.HitReader;
import org.jlab.rec.dc.banks.RecoBankWriter;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.cross.CrossList;
import org.jlab.rec.dc.cross.CrossListFinder;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.nn.PatternRec;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;

/**
 *
 * @author ziegler
 */
public class DCHBPostClusterAI extends DCEngine {

    public DCHBPostClusterAI() {
        super("DCHAI");
    }
    
    
    @Override
    public void initBankNames() {
        this.getBankNames().setHitsInputBank("HitBasedTrkg::HBHits");
        this.getBankNames().setClustersInputBank("HitBasedTrkg::HBClusters");
        this.getBankNames().setHitsBank("HitBasedTrkg::AIHits");
        this.getBankNames().setClustersBank("HitBasedTrkg::AIClusters");
        this.getBankNames().setSegmentsBank("HitBasedTrkg::AISegments");
        this.getBankNames().setCrossesBank("HitBasedTrkg::AICrosses");
        this.getBankNames().setTracksBank("HitBasedTrkg::AITracks");
        this.getBankNames().setIdsBank("HitBasedTrkg::AIHitTrkId");
        
        super.registerOutputBank("HitBasedTrkg::AIHits");
        super.registerOutputBank("HitBasedTrkg::AIClusters");
        super.registerOutputBank("HitBasedTrkg::AISegments");
        super.registerOutputBank("HitBasedTrkg::AICrosses");
        super.registerOutputBank("HitBasedTrkg::AITracks");
        super.registerOutputBank("HitBasedTrkg::AIHitTrkId");
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        
        int run = this.getRun(event);
        if(run==0) {
            System.out.println("RUN=0: Skipping event");
            return true;
        }
        
        /* IO */
        HitReader reader      = new HitReader(this.getBankNames());
        RecoBankWriter writer = new RecoBankWriter(this.getBankNames());
        // get Field
        Swim dcSwim = new Swim();
        /* 2 */
        
        /* 5 */
        if(Constants.DEBUG)
            System.out.println("HB AI process event");
        /* 7 */
        /* 8 */
        //AI
        List<Track> trkcands = null;
        List<Cross> crosses = null;
        List<FittedCluster> clusters = null;
        List<Segment> segments = null;
        List<FittedHit> fhits = null;

        reader.read_NNHits(event, Constants.getInstance().dcDetector);

        //I) get the lists
        List<Hit> hits = reader.get_DCHits();
        fhits = new ArrayList<FittedHit>();
        //II) process the hits
        //1) exit if hit list is empty
        if (hits.isEmpty()) {
            return true;
        }
        PatternRec pr = new PatternRec();
        segments = pr.RecomposeSegments(hits, Constants.getInstance().dcDetector);
        Collections.sort(segments);

        if (segments.isEmpty()) {
            return true;
        } 
        //crossList
        CrossList crosslist = pr.RecomposeCrossList(segments, Constants.getInstance().dcDetector);
        crosses = new ArrayList<Cross>();
        if(Constants.DEBUG==true) 
            System.out.println("num cands = "+crosslist.size());
        for (List<Cross> clist : crosslist) {
            crosses.addAll(clist); 
            if(Constants.DEBUG==true) {
                for(Cross c : clist)
                    System.out.println("Pass Cross"+c.printInfo());
            }
        }
        if (crosses.isEmpty()) {
            clusters = new ArrayList<FittedCluster>();
            for(Segment seg : segments) {
                clusters.add(seg.get_fittedCluster());
            }
            event.appendBanks(
                    writer.fillHBHitsBank(event, fhits),    
                    writer.fillHBClustersBank(event, clusters),
                    writer.fillHBSegmentsBank(event, segments));
            return true;
        } 
        // update B field
        CrossListFinder crossLister = new CrossListFinder();
        for(Cross cr : crosses) {
            crossLister.updateBFittedHits(event, cr, null, Constants.getInstance().dcDetector, null, dcSwim);
        }
        //find the list of  track candidates
        TrackCandListFinder trkcandFinder = new TrackCandListFinder(Constants.HITBASE);
        trkcands = trkcandFinder.getTrackCands(crosslist,
            Constants.getInstance().dcDetector,
            Swimmer.getTorScale(),
            dcSwim, true);

        // track found
        clusters = new ArrayList<FittedCluster>();
        int trkId = 1;
        if (trkcands.size() > 0) {
            // remove overlaps
            trkcandFinder.removeOverlappingTracks(trkcands);
            for (Track trk : trkcands) {
                // reset the id
                trk.set_Id(trkId);
                trkcandFinder.matchHits(trk.get_Trajectory(),
                        trk,
                        Constants.getInstance().dcDetector,
                        dcSwim);
                for (Cross c : trk) {
                    c.set_CrossDirIntersSegWires();
                    clusters.add(c.get_Segment1().get_fittedCluster());
                    clusters.add(c.get_Segment2().get_fittedCluster());
                    trkcandFinder.setHitDoubletsInfo(c.get_Segment1());
                    trkcandFinder.setHitDoubletsInfo(c.get_Segment2());
                    for (FittedHit h1 : c.get_Segment1()) {
                        if(h1.get_AssociatedHBTrackID()>0) fhits.add(h1);
                    }
                    for (FittedHit h2 : c.get_Segment2()) {
                        if(h2.get_AssociatedHBTrackID()>0) fhits.add(h2);
                    }
                }
                trkId++;
            }
        }
        
                // no candidate found, stop here and save the hits,
        // the clusters, the segments, the crosses
        if (trkcands.isEmpty()) {
            event.appendBanks(
                    writer.fillHBHitsBank(event, fhits),    
                    writer.fillHBSegmentsBank(event, segments),
                    writer.fillHBCrossesBank(event, crosses));
        }
        else {
            event.appendBanks(
                    writer.fillHBHitsBank(event, fhits),    
                    writer.fillHBClustersBank(event, clusters),
                    writer.fillHBSegmentsBank(event, segments),
                    writer.fillHBCrossesBank(event, crosses),
                    writer.fillHBTracksBank(event, trkcands),
                    writer.fillHBHitsTrkIdBank(event, fhits) );
        } 
        return true;
    }
    
}
