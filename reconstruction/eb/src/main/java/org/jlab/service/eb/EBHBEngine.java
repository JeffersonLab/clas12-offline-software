package org.jlab.service.eb;
import org.jlab.rec.eb.EBScalers;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 * @author baltzell
 */
public class EBHBEngine extends EBEngine {
    
    // static to store across events:
    static EBScalers ebScalers = new EBScalers();
    
    public EBHBEngine(){
        super("EBHB");
        setUsePOCA(false);
    }
   
    @Override
    public boolean processDataEvent(DataEvent de) {
        return super.processDataEvent(de,ebScalers);
    }

    @Override
    public void initBankNames() {
        this.setOutputBankPrefix("RECHB");

        this.setTrackType("HitBasedTrkg::HBTracks");
        this.setFTOFHitsType("FTOF::hbclusters");
        this.setCvtTrackType("CVT::Tracks");
        this.setCvtTrajType("CVT::Trajectory");
        this.setTrajectoryType("HitBasedTrkg::Trajectory");
    }
    
}
