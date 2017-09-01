package org.jlab.service.eb;


/**
 *
 * @author devita
 */
public class EBTBEngine extends EBEngine {
    
    
    public EBTBEngine(){
        super("EBTB");
    }
    
    @Override
    public void initBankNames() {
        this.setEventBank("REC::Event");
        this.setParticleBank("REC::Particle");
        this.setCalorimeterBank("REC::Calorimeter");
        this.setCherenkovBank("REC::Cherenkov");
        this.setScintillatorBank("REC::Scintillator");
        this.setTrackBank("REC::Track");
        this.setCrossBank("REC::TrackCross");
        this.setTrackType("TimeBasedTrkg::TBTracks");
        this.setFTBank("REC::ForwardTagger");
    }
    
}
