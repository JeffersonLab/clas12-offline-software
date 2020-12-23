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
        this.setEventBank("RECHBAI::Event");
        this.setParticleBank("RECHBAI::Particle");
        this.setCalorimeterBank("RECHBAI::Calorimeter");
        this.setCherenkovBank("RECHBAI::Cherenkov");
        this.setScintillatorBank("RECHBAI::Scintillator");
        this.setScintClusterBank("RECHBAI::ScintExtras");
        this.setTrackBank("RECHBAI::Track");
        this.setCrossBank("RECHBAI::TrackCross");
        this.setTrackType("HitBasedTrkg::AITracks");
        this.setFTOFHitsType("FTOF::hbhits");
        this.setFTBank("RECHBAI::ForwardTagger");
    }
    
}
