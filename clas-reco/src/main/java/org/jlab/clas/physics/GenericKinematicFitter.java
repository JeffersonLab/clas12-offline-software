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
            if(event.hasBank("EVENTTB::particle")){
                EvioDataBank evntBank = (EvioDataBank) event.getBank("EVENTTB::particle");
                int nrows = evntBank.rows();
                PhysicsEvent  physEvent = new PhysicsEvent();
                physEvent.setBeam(this.beamEnergy);
                for(int loop = 0; loop < nrows; loop++){
                    
                    int pid    = evntBank.getInt("pid", loop);
                    int status = evntBank.getInt("status", loop);
                    
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
                        
                        if(status>0){
                            physEvent.addParticle(part);
                        }
                    }
                }
                return physEvent;
            }
            
        }
        return new PhysicsEvent(this.beamEnergy);
    }
    
    public PhysicsEvent  getGeneratedEvent(DataEvent event){
        PhysicsEvent physEvent = new PhysicsEvent();
        physEvent.setBeam(this.beamEnergy);
        if(event.hasBank("GenPart::true")){
            EvioDataBank evntBank = (EvioDataBank) event.getBank("GenPart::true");
            int nrows = evntBank.rows();
            for(int loop = 0; loop < nrows; loop++){
                Particle genParticle = new Particle(
                        evntBank.getInt("pid", loop),
                        evntBank.getDouble("px", loop)*0.001,
                        evntBank.getDouble("py", loop)*0.001,
                        evntBank.getDouble("pz", loop)*0.001,
                        evntBank.getDouble("vx", loop),
                        evntBank.getDouble("vy", loop),
                        evntBank.getDouble("vz", loop));
                if(genParticle.p()<10.999&&
                        Math.toDegrees(genParticle.theta())>2.0){
                    physEvent.addParticle(genParticle);    
                }
            }
        }
        return physEvent;
    }
    
    
    public RecEvent getRecEvent(DataEvent event){
        
        PhysicsEvent rev = getPhysicsEvent(event);
        PhysicsEvent gev = getGeneratedEvent(event);
        RecEvent  recEvent = new RecEvent(this.beamEnergy);
        
        for(int i = 0; i < rev.count();i++){
            recEvent.getReconstructed().addParticle(rev.getParticle(i));
        }
        
        for(int i = 0; i < gev.count();i++){
            recEvent.getGenerated().addParticle(gev.getParticle(i));
        }
        
        return recEvent;
    }
}
