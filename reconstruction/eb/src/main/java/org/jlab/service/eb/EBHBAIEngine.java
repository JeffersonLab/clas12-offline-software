package org.jlab.service.eb;
import org.jlab.rec.eb.EBScalers;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 * @author baltzell
 */
public class EBHBAIEngine extends EBEngine {
    
    // static to store across events:
    static EBScalers ebScalers = new EBScalers();
    
    public EBHBAIEngine(){
        super("EBHBAI");
        setUsePOCA(true);
    }
   
    @Override
    public boolean processDataEvent(DataEvent de) {
        return super.processDataEvent(de,ebScalers);
    }

    @Override
    public void initBankNames() {
	this.setOutputBankPrefix("RECHBAI");

	this.setTrackType("HitBasedTrkg::AITracks");
         this.setFTOFHitsType("FTOF::hbclusters");
        this.setCvtTrackType("CVT::Tracks");
        this.setCvtTrajType("CVT::Trajectory");
    }
    
}
