package org.jlab.rec.cvt.splitservices;

import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.utils.groups.IndexedTable;

/**
 * Service to return reconstructed TRACKS
 * format
 *
 * @author ziegler
 *
 */
public class CVTSecondPass extends CVTEngine {

    private TracksFromTargetRec trksFromTargetRec = null;
    
    public CVTSecondPass() {
        super("CVTSecondPass");
        
        trksFromTargetRec = new TracksFromTargetRec();
    }

    
    @Override
    public boolean init() {
        
        this.loadConfiguration();
        this.initConstantsTables();
        this.loadGeometries();
        this.registerBanks();
        return true;
    }
    
    
    @Override
    public boolean processDataEvent(DataEvent event) {

        int run = this.getRun(event);
        Swim swimmer = new Swim();

        IndexedTable beamPos   = this.getConstantsManager().getConstants(run, "/geometry/beam/position");
        
        if(Constants.ISCOSMICDATA) {
            return true;
        } else {
            double xb = beamPos.getDoubleValue("x_offset", 0, 0, 0)*10;
            double yb = beamPos.getDoubleValue("y_offset", 0, 0, 0)*10;
            List<Seed> seeds = trksFromTargetRec.getSeedsFromBanks(event, swimmer, xb , yb); //write another method to start from recomp seeds
            if(seeds == null)
                return false;
            trksFromTargetRec.getTracks(event, seeds, false, 2);
        }
        return true;
    }
     
    
    
    
    @Override
    public void registerBanks() {
        super.registerOutputBank("BMTRec::Hits");
        super.registerOutputBank("BMTRec::Clusters");
        super.registerOutputBank("BSTRec::Crosses");
        super.registerOutputBank("BSTRec::Hits");
        super.registerOutputBank("BSTRec::Clusters");
        super.registerOutputBank("BSTRec::Crosses");
        super.registerOutputBank("CVTRec::Seeds");
        super.registerOutputBank("CVTRec::ELCTracks");     //eloss corrected tracks
    }
    

}