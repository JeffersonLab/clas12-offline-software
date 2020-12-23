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
        this.setEventBank("RECAI::Event");
        this.setParticleBank("RECAI::Particle");
        this.setEventBankFT("RECFTAI::Event");
        this.setParticleBankFT("RECFTAI::Particle");
        this.setCalorimeterBank("RECAI::Calorimeter");
        this.setCherenkovBank("RECAI::Cherenkov");
        this.setScintillatorBank("RECAI::Scintillator");
        this.setScintClusterBank("RECAI::ScintExtras");
        this.setTrackBank("RECAI::Track");
        this.setCrossBank("RECAI::TrackCross");
        this.setCovMatrixBank("RECAI::CovMat");
        this.setTrajectoryBank("RECAI::Traj");        
        this.setFTBank("RECAI::ForwardTagger");
        this.setFTOFHitsType("FTOF::hits");
        this.setTrackType("TimeBasedTrkg::AITracks");
        this.setTrajectoryType("TimeBasedTrkg::AITrajectory");
        this.setCovMatrixType("TimeBasedTrkg::AICovMat");
    }
    
}
