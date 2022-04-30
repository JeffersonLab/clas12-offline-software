package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.Track;
import org.jlab.utils.groups.IndexedTable;

/**
 * Service to return reconstructed TRACKS
 * format
 *
 * @author ziegler
 *
 */
public class CVTSecondPassEngine extends CVTEngine {
    
    public CVTSecondPassEngine() {
        super("CVTSecondPass");
        this.setOutputBankPrefix("Rec");
    }

    
    @Override
    public boolean processDataEvent(DataEvent event) {

        int run = this.getRun(event);
        Swim swimmer = new Swim();

        IndexedTable beamPos   = this.getConstantsManager().getConstants(run, "/geometry/beam/position");
        
        if(Constants.getInstance().isCosmics) {
            return true;
        } else {
            TracksFromTargetRec trackFinder = new TracksFromTargetRec(swimmer, beamPos);
            List<Seed>  seeds  = trackFinder.getSeedsFromBanks(event);
            List<Track> tracks = null;
            if(seeds!=null) {
                tracks = trackFinder.getTracks(event, this.isInitFromMc(), 
                                                      this.isKfFilterOn(), 
                                                      this.getKfIterations(), 
                                                      false, this.getPid());
            }
            
            List<DataBank> banks = new ArrayList<>();
            if(trackFinder.getSVThits()!=null) banks.add(RecoBankWriter.fillSVTHitBank(event, trackFinder.getSVThits(), this.getSvtHitBank()));
            if(trackFinder.getBMThits()!=null) banks.add(RecoBankWriter.fillBMTHitBank(event, trackFinder.getBMThits(), this.getBmtHitBank()));
            if(trackFinder.getSVTclusters()!=null) banks.add(RecoBankWriter.fillSVTClusterBank(event, trackFinder.getSVTclusters(), this.getSvtClusterBank()));
            if(trackFinder.getBMTclusters()!=null) banks.add(RecoBankWriter.fillBMTClusterBank(event, trackFinder.getBMTclusters(), this.getBmtClusterBank()));
            if(trackFinder.getSVTcrosses()!=null) banks.add(RecoBankWriter.fillSVTCrossBank(event, trackFinder.getSVTcrosses(), this.getSvtCrossBank()));
            if(trackFinder.getBMTcrosses()!=null) banks.add(RecoBankWriter.fillBMTCrossBank(event, trackFinder.getBMTcrosses(), this.getBmtCrossBank()));
            if(seeds!=null) banks.add(RecoBankWriter.fillSeedBank(event, seeds, this.getSeedBank()));
            if(tracks!=null) {
                banks.add(RecoBankWriter.fillTrackBank(event, tracks, this.getTrackBank()));
                banks.add(RecoBankWriter.fillUTrackBank(event, tracks, this.getUTrackBank()));
                banks.add(RecoBankWriter.fillTrackCovMatBank(event, tracks, this.getCovMat()));
                banks.add(RecoBankWriter.fillTrajectoryBank(event, tracks, this.getTrajectoryBank()));
                banks.add(RecoBankWriter.fillKFTrajectoryBank(event, tracks, this.getKFTrajectoryBank()));
            }
            if(!banks.isEmpty()) event.appendBanks(banks.toArray(new DataBank[0]));
        }
        return true;
    }
   
}