package org.jlab.service.dc;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.swimtools.Swimmer;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
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
import org.jlab.rec.dc.trajectory.RoadFinder;
import org.jlab.rec.dc.trajectory.Road;

/**
 * @author ziegler
 * @since 08.09.2018 updated by gurjyan
 @deprecated 
 */
public class DCHBEngine extends DCEngine {


    public DCHBEngine() {
        super("DCHB");
        this.getBanks().init("HitBasedTrkg", "", "HB");
    }


    @Override
    public void setDropBanks() {
        super.registerOutputBank(this.getBanks().getHitsBank());
        super.registerOutputBank(this.getBanks().getClustersBank());
        super.registerOutputBank(this.getBanks().getSegmentsBank());
        super.registerOutputBank(this.getBanks().getCrossesBank());
        super.registerOutputBank(this.getBanks().getTracksBank());
        super.registerOutputBank(this.getBanks().getIdsBank());
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {

        int run = this.getRun(event);
        if(run==0) return true;
        
        double triggerPhase = this.getTriggerPhase(event);

       
       if (event.hasBank("MC::Particle") && this.getEngineConfigString("wireDistort")==null) {
           Constants.getInstance().setWIREDIST(0);
       }

        /* 1 */
        // get Field
        Swim dcSwim = new Swim();
        /* 2 */
        // init SNR
        Clas12NoiseResult results = new Clas12NoiseResult();
        /* 3 */
        Clas12NoiseAnalysis noiseAnalysis = new Clas12NoiseAnalysis();
        /* 4 */
        NoiseReductionParameters parameters =
                new NoiseReductionParameters(
                        2,
                        Constants.SNR_LEFTSHIFTS,
                        Constants.SNR_RIGHTSHIFTS);
        /* 5 */
        ClusterFitter cf = new ClusterFitter();
        /* 6 */
        ClusterCleanerUtilities ct = new ClusterCleanerUtilities();
        /* 7 */
        RecoBankWriter rbc = new RecoBankWriter(this.getBanks());
        /* 8 */
        HitReader hitRead = new HitReader(this.getBanks());
        /* 9 */
        hitRead.fetch_DCHits(event,
                noiseAnalysis,
                parameters,
                results,
                super.getConstantsManager().getConstants(run, Constants.TIME2DIST),
                super.getConstantsManager().getConstants(run, Constants.TDCTCUTS),
                super.getConstantsManager().getConstants(run, Constants.WIRESTAT),
                Constants.getInstance().dcDetector,
                triggerPhase);
        /* 10 */
        //I) get the hits
        List<Hit> hits = hitRead.get_DCHits();
        //II) process the hits
        //1) exit if hit list is empty
        if (hits.isEmpty()) {
            return true;
        }
        /* 11 */
        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();
        List<FittedCluster> clusters = clusFinder.FindHitBasedClusters(hits,
                ct,
                cf,
                Constants.getInstance().dcDetector);
        if (clusters.isEmpty()) {
            return true;
        }
        /* 12 */
        List<FittedHit> fhits = rbc.createRawHitList(hits);
        /* 13 : assign cluster IDs to hits: if hit is associated to two clusters, the second survives*/ 
        rbc.updateListsWithClusterInfo(fhits, clusters);
        /* 14 */
        //3) find the segments from the fitted clusters
        SegmentFinder segFinder = new SegmentFinder();
        List<Segment> segments = segFinder.get_Segments(clusters,
                event,
                Constants.getInstance().dcDetector, false);
        /* 15 */
        // need 6 segments to make a trajectory
        if (segments.isEmpty()) {
            rbc.fillAllHBBanks(event,
                    fhits,
                    clusters,
                    null,
                    null,
                    null);
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
        /* 16 */
        CrossMaker crossMake = new CrossMaker();
        List<Cross> crosses = crossMake.find_Crosses(segments, Constants.getInstance().dcDetector);
        if (crosses.isEmpty()) {
            rbc.fillAllHBBanks(event,
                    fhits,
                    clusters,
                    segments,
                    null,
                    null);
            return true;
        }
        /* 17 */
        CrossListFinder crossLister = new CrossListFinder();

        CrossList crosslist = crossLister.candCrossLists(event, crosses,
                false,
                super.getConstantsManager().getConstants(run, Constants.TIME2DIST),
                Constants.getInstance().dcDetector,
                null,
                dcSwim, false);
        /* 18 */
        //6) find the list of  track candidates
        TrackCandListFinder trkcandFinder = new TrackCandListFinder(Constants.HITBASE);
        List<Track> trkcands = trkcandFinder.getTrackCands(crosslist,
                Constants.getInstance().dcDetector,
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
                        Constants.getInstance().dcDetector,
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
        List<Road> allRoads = rf.findRoads(segments, Constants.getInstance().dcDetector);
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
                        Constants.getInstance().dcDetector,
                        r.a);
                if (pSegment != null)
                    psegments.add(pSegment);
            }
        }
        segments.addAll(psegments);
        List<Cross> pcrosses = crossMake.find_Crosses(segments, Constants.getInstance().dcDetector);
        CrossList pcrosslist = crossLister.candCrossLists(event, pcrosses,
                false,
                super.getConstantsManager().getConstants(run, Constants.TIME2DIST),
                Constants.getInstance().dcDetector,
                null,
                dcSwim, true);
        //pcrosslist.removeDuplicates(crosslist);
        List<Track> mistrkcands = trkcandFinder.getTrackCands(pcrosslist,
                Constants.getInstance().dcDetector,
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
                        Constants.getInstance().dcDetector,
                        dcSwim);
                for (Cross c : trk) {
                    c.set_CrossDirIntersSegWires();
                    trkcandFinder.setHitDoubletsInfo(c.get_Segment1());
                    trkcandFinder.setHitDoubletsInfo(c.get_Segment2());
                }
                trkId++;
            }
        }
        trkcands.addAll(mistrkcands);

        // no candidate found, stop here and save the hits,
        // the clusters, the segments, the crosses
        if (trkcands.isEmpty()) {
            rbc.fillAllHBBanks(event,
                    fhits,
                    clusters,
                    segments,
                    crosses,
                    null);
            return true;
        }
        rbc.fillAllHBBanks(event,
                fhits,
                clusters,
                segments,
                crosses,
                trkcands);
        return true;
    }

    
}
