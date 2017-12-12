/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.physics;

import java.util.Map;
import org.jlab.clas.pdg.PDGDatabase;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.jnp.hipo.data.HipoEvent;
import org.jlab.jnp.hipo.data.HipoNode;

/**
 *
 * @author gavalian
 */
public class GenericKinematicFitter {
    
    private final   EventFilter filter = new EventFilter();
    protected Double  beamEnergy  = 11.0;
    
    
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
        //if(event instanceof DataEvent){
            //System.out.println("   CHECK FOR  PARTICLE = " + event.hasBank("EVENT::particle"));
        if(event.hasBank("REC::Particle")){
            DataBank evntBank = (DataBank) event.getBank("REC::Particle");
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
                        if(status>0){
                            physEvent.addParticle(part);
                        }
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
            
        //}
        return new PhysicsEvent(this.beamEnergy);
    }
    
    public PhysicsEvent  getGeneratedEvent(DataEvent event){
        PhysicsEvent physEvent = new PhysicsEvent();
        physEvent.setBeam(this.beamEnergy);
        if(event.hasBank("MC::Particle")){
            DataBank evntBank = (DataBank) event.getBank("MC::Particle");
            int nrows = evntBank.rows();
            for(int loop = 0; loop < nrows; loop++){
                Particle genParticle = new Particle(
                        evntBank.getInt("pid", loop),
                        evntBank.getFloat("px", loop)*0.001,
                        evntBank.getFloat("py", loop)*0.001,
                        evntBank.getFloat("pz", loop)*0.001,
                        evntBank.getFloat("vx", loop),
                        evntBank.getFloat("vy", loop),
                        evntBank.getFloat("vz", loop));
                    		physEvent.addParticle(genParticle);    
            }
        }
        return physEvent;
    }
        
    public PhysicsEvent createEvent(DataEvent event){
        PhysicsEvent  recEvent = this.getPhysicsEvent(event);
        PhysicsEvent  genEvent = this.getGeneratedEvent(event);
        for(int i = 0; i < genEvent.count();i++){
            recEvent.mc().add(genEvent.getParticle(i));
        }
        return recEvent;
    }
    
    public PhysicsEvent createEvent(HipoEvent event){
        PhysicsEvent physEvent = new PhysicsEvent();
        physEvent.setBeam(this.beamEnergy);
        if(event.hasGroup(20)==true){
            Map<Integer,HipoNode>  items = event.getGroup(20);
            int nentries = items.get(1).getDataSize();
            for(int i = 0; i < nentries; i++){
                int pid = items.get(1).getInt(i);
                double px = items.get(2).getFloat(i*3+0);
                double py = items.get(2).getFloat(i*3+1);
                double pz = items.get(2).getFloat(i*3+2);
                Particle particle = new Particle(pid,px,py,pz);
                double vx = items.get(3).getShort(i*3+0)*100.0;
                double vy = items.get(3).getShort(i*3+1)*100.0;
                double vz = items.get(3).getShort(i*3+2)*100.0;
                particle.vertex().setXYZ(vx, vy, vz);
                physEvent.mc().add(particle);
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
