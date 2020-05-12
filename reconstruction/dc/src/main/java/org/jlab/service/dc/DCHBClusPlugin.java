package org.jlab.service.dc;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jlab.clas.swimtools.MagFieldsEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.swimtools.Swimmer;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
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
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;
import org.jlab.rec.dc.trajectory.RoadFinder;
import org.jlab.rec.dc.trajectory.Road;
import org.jlab.utils.groups.IndexedTable;

/**
 * @author zigler
 */
public class DCHBClusPlugin extends DCEngine {

    private AtomicInteger Run = new AtomicInteger(0);
    private double triggerPhase;
    private int newRun = 0;

    public DCHBClusPlugin() {
        super("DCHB");
    }

    @Override
    public boolean init() {
        // Load cuts
        super.LoadTables();
        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        if (!event.hasBank("RUN::config")) {
            return true;
        }

        /* 1 */
        // get Field
        Swim dcSwim = new Swim();
        /* 2 */
        
        /* 5 */
        ClusterFitter cf = new ClusterFitter();
        
        /* 7 */
        RecoBankWriter rbc = new RecoBankWriter();
        /* 8 */
        
        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();
        List<FittedCluster> clusters = clusFinder.RecomposeClusters(event, dcDetector, cf);
        if (clusters.isEmpty()) {
            return true;
        }
        
        //3) find the segments from the fitted clusters
        SegmentFinder segFinder = new SegmentFinder();
        List<Segment> segments = segFinder.get_Segments(clusters,
                event,
                dcDetector, false);
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
        
        /* 16 */
        CrossMaker crossMake = new CrossMaker();
        List<Cross> crosses = crossMake.find_Crosses(segments, dcDetector);
        if (crosses.isEmpty()) {
            event.appendBanks(
                    rbc.fillHBSegmentsBank(event, segments),
                    rbc.fillHBCrossesBank(event, crosses));
            return true;
        }
        /* 17 */
        CrossListFinder crossLister = new CrossListFinder();

        CrossList crosslist = crossLister.candCrossLists(event, crosses,
                false,
                super.getConstantsManager().getConstants(newRun, Constants.TIME2DIST),
                dcDetector,
                null,
                dcSwim);
        /* 18 */
        //6) find the list of  track candidates
        TrackCandListFinder trkcandFinder = new TrackCandListFinder(Constants.HITBASE);
        List<Track> trkcands = trkcandFinder.getTrackCands(crosslist,
                dcDetector,
                Swimmer.getTorScale(),
                dcSwim);
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
                        dcDetector,
                        dcSwim);
                for (Cross c : trk) {
                    c.get_Segment1().isOnTrack = true;
                    c.get_Segment2().isOnTrack = true;

                    for (FittedHit h1 : c.get_Segment1()) {
                        h1.set_AssociatedHBTrackID(trk.get_Id());
                    }
                    for (FittedHit h2 : c.get_Segment2()) {
                        h2.set_AssociatedHBTrackID(trk.get_Id());
                    }
                }
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
        List<Road> allRoads = rf.findRoads(segments, dcDetector);
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
                        dcDetector,
                        r.a);
                if (pSegment != null)
                    psegments.add(pSegment);
            }
        }
        segments.addAll(psegments);
        List<Cross> pcrosses = crossMake.find_Crosses(segments, dcDetector);
        CrossList pcrosslist = crossLister.candCrossLists(event, pcrosses,
                false,
                super.getConstantsManager().getConstants(newRun, Constants.TIME2DIST),
                dcDetector,
                null,
                dcSwim);
        List<Track> mistrkcands = trkcandFinder.getTrackCands(pcrosslist,
                dcDetector,
                Swimmer.getTorScale(),
                dcSwim);

        // remove overlaps
        if (mistrkcands.size() > 0) {
            trkcandFinder.removeOverlappingTracks(mistrkcands);
            for (Track trk : mistrkcands) {

                // reset the id
                trk.set_Id(trkId);
                trkcandFinder.matchHits(trk.get_Trajectory(),
                        trk,
                        dcDetector,
                        dcSwim);
                for (Cross c : trk) {
                    for (FittedHit h1 : c.get_Segment1()) {
                        h1.set_AssociatedHBTrackID(trk.get_Id());
                    }
                    for (FittedHit h2 : c.get_Segment2()) {
                        h2.set_AssociatedHBTrackID(trk.get_Id());
                    }
                }
                trkId++;
            }
        }
        trkcands.addAll(mistrkcands);

        // no candidate found, stop here and save the hits,
        // the clusters, the segments, the crosses
        if (trkcands.isEmpty()) {
            event.appendBanks(
                rbc.fillHBSegmentsBank(event, segments),
                rbc.fillHBCrossesBank(event, crosses));
            return true;
        }
        event.appendBanks(
            rbc.fillHBSegmentsBank(event, segments),
            rbc.fillHBCrossesBank(event, crosses),
            rbc.fillHBTracksBank(event, trkcands));
        return true;
    }

}
