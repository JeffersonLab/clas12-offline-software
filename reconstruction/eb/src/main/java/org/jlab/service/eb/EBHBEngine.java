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
        setUsePOCA(true);
    }
   
    @Override
    public boolean processDataEvent(DataEvent de) {
        return super.processDataEvent(de,ebScalers);
    }

    @Override
    public void initBankNames() {
        this.setEventBank("RECHB::Event");
        this.setParticleBank("RECHB::Particle");
        this.setCalorimeterBank("RECHB::Calorimeter");
        this.setCherenkovBank("RECHB::Cherenkov");
        this.setScintillatorBank("RECHB::Scintillator");
        this.setScintClusterBank("RECHB::ScintExtras");
        this.setTrackBank("RECHB::Track");
        this.setCrossBank("RECHB::TrackCross");
        this.setTrackType("HitBasedTrkg::HBTracks");
        this.setFTOFHitsType("FTOF::hbhits");
        this.setFTBank("RECHB::ForwardTagger");
    }
    
}
