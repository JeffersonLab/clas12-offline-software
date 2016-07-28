/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.physics;

import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;

/**
 *
 * @author gavalian
 */
public class GenericKinematicFitter {
    
    private final   EventFilter filter = new EventFilter();
    private Double  beamEnergy  = 11.0;
    
    
    public GenericKinematicFitter(double beam){
        this.beamEnergy = beam;
        this.filter.setFilter("X+:X-:Xn");
    }
    
    /**
     * Returns PhysicsEvent object with reconstructed particles.
     * @param event - DataEvent object
     * @return PhysicsEvent : event containing particles.
     */
    public PhysicsEvent  getPhysicsEvent(DataEvent  event){
        if(event instanceof EvioDataEvent){
            //System.out.println("   CHECK FOR  PARTICLE = " + event.hasBank("EVENT::particle"));
            if(event.hasBank("EVENTHB::particle")){
                EvioDataBank evntBank = (EvioDataBank) event.getBank("EVENTHB::particle");
                int nrows = evntBank.rows();
                PhysicsEvent  physEvent = new PhysicsEvent();
                physEvent.setBeam(this.beamEnergy);
                for(int loop = 0; loop < nrows; loop++){
                    
                    int pid    = evntBank.getInt("pid", loop);
                    if(PDGDatabase.isValidPid(pid)==true){
                        Particle part = new Particle(
                                evntBank.getInt("pid", loop),
                                evntBank.getFloat("px", loop),
                                evntBank.getFloat("py", loop),
                                evntBank.getFloat("pz", loop),
                                evntBank.getFloat("vx", loop),
                                evntBank.getFloat("vy", loop),
                                evntBank.getFloat("vz", loop));
                        physEvent.addParticle(part);
                    } else {
                        Particle part = new Particle();
                        int charge = evntBank.getInt("charge", loop);
                        part.setParticleWithMass(evntBank.getFloat("mass", loop),
                                (byte) charge,
                                evntBank.getFloat("px", loop),
                                evntBank.getFloat("py", loop),
                                evntBank.getFloat("pz", loop),
                                evntBank.getFloat("vx", loop),
                                evntBank.getFloat("vy", loop),
                                evntBank.getFloat("vz", loop)
                        );
                        physEvent.addParticle(part);
                    }
                }
                return physEvent;
            }
            
        }
        return new PhysicsEvent(this.beamEnergy);
    }    
}
