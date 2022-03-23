package org.jlab.rec.cvt.splitservices;

import org.jlab.clas.swimtools.Swim;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.RecoBankWriter;
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
    private int Run = -1;
    
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
        this.setRunConditionsParameters(event, Run, false, "");
        
        IndexedTable beamPos   = this.getConstantsManager().getConstants(this.getRun(), "/geometry/beam/position");
        
        if(Constants.ISCOSMICDATA) {
            return true;
        } else {
            double xb = beamPos.getDoubleValue("x_offset", 0, 0, 0)*10;
            double yb = beamPos.getDoubleValue("y_offset", 0, 0, 0)*10;
            trksFromTargetRec.getSeedsFromBanks(event, xb , yb); //write another method to start from recomp seeds
            trksFromTargetRec.getTracks(event, true, 2);
        }
        return true;
    }
     
    
    
    
    private void registerBanks() {
        super.registerOutputBank("CVTRec::ELCTracks");     //eloss corrected tracks
    }
    

}