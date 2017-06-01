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
    }
    
}
