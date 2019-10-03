package org.jlab.service.dc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.swimtools.Swimmer;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.HitReader;
import org.jlab.rec.dc.banks.RecoBankWriter;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.cross.CrossList;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.nn.PatternRec;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;
import org.jlab.utils.groups.IndexedTable;

/**
 * @author zigler
 */
public class DCNNEngine extends DCEngine {

    private AtomicInteger Run = new AtomicInteger(0);
    private double triggerPhase;
    private int newRun = 0;

    public DCNNEngine() {
        super("DCNN");
    }

    @Override
    public boolean init() {
        // Load cuts
        Constants.Load();
        super.setOptions();
        super.LoadTables();
        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
//        long startTime = 0;
        //setRunConditionsParameters( event) ;
        if (!event.hasBank("RUN::config")) {
            return true;
        }

       DataBank bank = event.getBank("RUN::config");
       long timeStamp = bank.getLong("timestamp", 0);
       double triggerPhase = 0;

        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);
       if (newRun == 0)
           return true;

       if (Run.get() == 0 || (Run.get() != 0 && Run.get() != newRun)) {
           if (timeStamp == -1)
               return true;
 //          if (debug.get()) startTime = System.currentTimeMillis();
           IndexedTable tabJ = super.getConstantsManager().getConstants(newRun, Constants.TIMEJITTER);
           double period = tabJ.getDoubleValue("period", 0, 0, 0);
           int phase = tabJ.getIntValue("phase", 0, 0, 0);
           int cycles = tabJ.getIntValue("cycles", 0, 0, 0);

           if (cycles > 0) triggerPhase = period * ((timeStamp + phase) % cycles);

           TableLoader.FillT0Tables(newRun, super.variationName);
           TableLoader.Fill(super.getConstantsManager().getConstants(newRun, Constants.TIME2DIST));

           Run.set(newRun);
           if (event.hasBank("MC::Particle") && this.getEngineConfigString("wireDistort")==null) {
               Constants.setWIREDIST(0);
           }

 //          if (debug.get()) System.out.println("NEW RUN INIT = " + (System.currentTimeMillis() - startTime));
       }

        // get Field
        Swim dcSwim = new Swim();
        
        RecoBankWriter rbc = new RecoBankWriter();
       
        HitReader hitRead = new HitReader();
       
        hitRead.read_NNHits(event, dcDetector, triggerPhase);
       
        //I) get the lists
        List<Hit> hits = hitRead.get_DCHits();
        List<FittedHit> fhits = new ArrayList<FittedHit>();
        List<FittedCluster> clusters = new ArrayList<FittedCluster>();
        //II) process the hits
        //1) exit if hit list is empty
        if (hits.isEmpty()) {
            return true;
        }
        PatternRec pr = new PatternRec();
        List<Segment> segments = pr.RecomposeSegments(hits, dcDetector);
        
        if (segments.isEmpty()) {
            return true;
        }
        
        //crossList
        CrossList crosslist = pr.RecomposeCrossList(segments, dcDetector);
        List<Cross> crosses = new ArrayList<Cross>();

        for (List<Cross> clist : crosslist) {
            crosses.addAll(clist);
        }
        //find the list of  track candidates
        TrackCandListFinder trkcandFinder = new TrackCandListFinder(Constants.HITBASE);
        List<Track> trkcands = trkcandFinder.getTrackCands(crosslist,
                dcDetector,
                Swimmer.getTorScale(),
                dcSwim);
        
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
                    clusters.add(c.get_Segment1().get_fittedCluster());
                    clusters.add(c.get_Segment2().get_fittedCluster());
                    for (FittedHit h1 : c.get_Segment1()) {
                        h1.set_AssociatedHBTrackID(trk.get_Id());
                        fhits.add(h1);
                    }
                    for (FittedHit h2 : c.get_Segment2()) {
                        h2.set_AssociatedHBTrackID(trk.get_Id());
                        fhits.add(h2);
                    }
                }
                trkId++;
            }
        }
        
        // no candidate found, stop here 
        if (trkcands.isEmpty()) {
            return true;
        }
        rbc.fillAllHBBanks(event,
                rbc,
                fhits,
                clusters,
                segments,
                crosses,
                trkcands);
        return true;
    }

}
