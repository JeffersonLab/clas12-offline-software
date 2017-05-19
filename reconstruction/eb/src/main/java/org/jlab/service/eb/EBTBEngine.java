/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    }
    
}
