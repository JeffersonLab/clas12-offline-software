package org.jlab.service.eb;
import org.jlab.rec.eb.EBScalers;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 * @author baltzell
 */
public class EBTBAIEngine extends EBEngine {

    // static to store across events:
    static EBScalers ebScalers = new EBScalers();

    public EBTBAIEngine(){
        super("EBTBAI");
        setUsePOCA(false);
    }

    @Override
    public boolean processDataEvent(DataEvent de) {
        return super.processDataEvent(de,ebScalers);
    }

    @Override
    public void initBankNames() {
        this.setOutputBankPrefix("RECAI");

        this.setFTOFHitsType("FTOF::clusters");
        this.setTrackType("TimeBasedTrkg::AITracks");
        this.setTrajectoryType("TimeBasedTrkg::AITrajectory");
        this.setCovMatrixType("TimeBasedTrkg::AICovMat");
        this.setCvtTrackType("CVTRec::Tracks");
        this.setCvtTrajType("CVTRec::Trajectory");
    }
    
}
