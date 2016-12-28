/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.service.pid.EventTrigger;


/**
 *
 * @author gavalian
 */
public class DetectorEvent {
    
    private List<DetectorParticle>  particleList = new ArrayList<DetectorParticle>();
    private PhysicsEvent       generatedEvent = new PhysicsEvent();
    private PhysicsEvent   reconstructedEvent = new PhysicsEvent();
    private EventTrigger trigger = new EventTrigger();
    
    
    public DetectorEvent(){
        
    }
    
    public PhysicsEvent getGeneratedEvent(){
        return this.generatedEvent;
    }
    
    public PhysicsEvent getPhysicsEvent(){
        return this.reconstructedEvent;
    }
    
    public DetectorParticle matchedParticle(int pid, int skip){
        Particle particle = generatedEvent.getParticleByPid(pid, skip);
        if(particle.p()<0.0000001) return new DetectorParticle();
        return matchedParticle(particle);
    }
    
    public DetectorParticle matchedParticle(Particle p){
        double compare = 100.0;
        int index = -1;
        for(int i = 0; i < particleList.size();i++){
            if(p.charge()==particleList.get(i).getCharge()){
            //System.out.println("index = " + i + "  compare = " + particleList.get(i).compare(p.vector().vect()));
                if(particleList.get(i).compare(p.vector().vect())<compare){
                    compare = particleList.get(i).compare(p.vector().vect());
                    index   = i; 
                }
            }
        }
        if(index<0&&compare>0.2) return new DetectorParticle();
        return this.particleList.get(index);
    }
    
    public void clear(){
        this.particleList.clear();
    }
    
    public void addParticle(DetectorParticle particle){        
        this.particleList.add(particle);
    }
    

    
    public List<DetectorParticle> getParticles(){ return this.particleList;}
    
    public void addParticle(double px, double py, double pz,
            double vx, double vy, double vz){
        DetectorParticle particle = new DetectorParticle();
        particle.vector().setXYZ(px, py, pz);
        particle.vertex().setXYZ(vx, vy, vz);
        this.addParticle(particle);
    }
    
    public void setEventTrigger(EventTrigger trig){this.trigger = trig;}
    public EventTrigger getEventTrigger(){return this.trigger;}
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(" === [ DETECTOR EVENT ] === \n");
        for(DetectorParticle particle : this.particleList){
            str.append(particle.toString());
            str.append("\n");
        }
        return str.toString();
    }
}
