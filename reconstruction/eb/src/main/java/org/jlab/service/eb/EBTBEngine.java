package org.jlab.service.eb;
import java.util.HashMap;
import java.util.Map;
import org.jlab.io.base.DataBank;
import org.jlab.rec.eb.EBScalers;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 * @author baltzell
 */
public class EBTBEngine extends EBEngine {

    // static to store across events:
    static EBScalers ebScalers = new EBScalers();

    final static String HB_BANK = "HitBasedTrkg::HBTracks";
    final static String TB_BANK = "TimeBasedTrkg::TBTracks";
    
    public EBTBEngine(){
        super("EBTB");
        setUsePOCA(false);
    }

    @Override
    public boolean processDataEvent(DataEvent de) {
        final boolean ret = super.processDataEvent(de,ebScalers);
        this.linkTracks(de);
        return ret;
    }

    /**
     * Reconstruct the mapping between hit-based and time-based tracks, by going
     * back to the original, non-DST tracking banks and check their "id" variables.
     * This modifies REC::Track in place in the event, assigning its "hbindex" to
     * the corresponding row number in RECHB::Track.
     * @param de
     */
    public void linkTracks(DataEvent de) {

        if (!de.hasBank("REC::Track")) return;

        // fill map of track id -> row in RECHB::Track:
        Map<Short,Short> hbids = new HashMap<>();
        if (de.hasBank(HB_BANK) && de.hasBank("RECHB::Track")) {
            for (short row=0; row<de.getBank("RECHB::Track").rows(); row++) {
                final short hbindex = de.getBank("RECHB::Track").getShort("index", row);
                hbids.put(de.getBank(HB_BANK).getShort("id", hbindex),row);
            }
        }

        // set REC::Track.hbindex:
        DataBank tbbank = de.getBank("REC::Track");
        de.removeBank("REC::Track");
        for (int row=0; row<tbbank.rows(); row++) {
            final short tbindex = tbbank.getShort("index",row);
            short hbindex = -1;
            if (hbids.containsKey(de.getBank(TB_BANK).getShort("id",tbindex))) {
                hbindex = hbids.get(de.getBank(TB_BANK).getShort("id",tbindex));
            }
            tbbank.setShort("hbindex", row, hbindex);
        }
        de.appendBank(tbbank);
    }

    @Override
    public void initBankNames() {
        this.setEventBank("REC::Event");
        this.setParticleBank("REC::Particle");
        this.setEventBankFT("RECFT::Event");
        this.setParticleBankFT("RECFT::Particle");
        this.setCalorimeterBank("REC::Calorimeter");
        this.setCherenkovBank("REC::Cherenkov");
        this.setScintillatorBank("REC::Scintillator");
        this.setScintClusterBank("REC::ScintExtras");
        this.setTrackBank("REC::Track");
        this.setCrossBank("REC::TrackCross");
        this.setCovMatrixBank("REC::CovMat");
        this.setTrajectoryBank("REC::Traj");        
        this.setFTBank("REC::ForwardTagger");
        this.setFTOFHitsType("FTOF::hits");
        this.setTrackType("TimeBasedTrkg::TBTracks");
        this.setTrajectoryType("TimeBasedTrkg::Trajectory");
        this.setCovMatrixType("TimeBasedTrkg::TBCovMat");
    }
    
}
