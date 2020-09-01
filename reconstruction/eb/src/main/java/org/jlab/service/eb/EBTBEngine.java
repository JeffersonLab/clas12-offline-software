package org.jlab.service.eb;
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

    public EBTBEngine(){
        super("EBTB");
        setUsePOCA(false);
    }

    @Override
    public boolean processDataEvent(DataEvent de) {
        return super.processDataEvent(de,ebScalers);
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
