package org.jlab.service.eb;


/**
 *
 * @author devita
 */
public class EBHBEngine extends EBEngine {
    
    
    public EBHBEngine(){
        super("EBHB");
    }
    
    @Override
    public void initBankNames() {
        this.setEventBank("RECHB::Event");
        this.setParticleBank("RECHB::Particle");
        this.setCalorimeterBank("RECHB::Calorimeter");
        this.setCherenkovBank("RECHB::Cherenkov");
        this.setScintillatorBank("RECHB::Scintillator");
        this.setTrackBank("RECHB::Track");
        this.setCrossBank("RECHB::TrackCross");
        this.setTrackType("HitBasedTrkg::HBTracks");
        this.setFTOFHitsType("FTOF::hbhits");
        this.setFTBank("RECHB::ForwardTagger");
    }
    
}
