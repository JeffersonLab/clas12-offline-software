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
import org.jlab.io.base.DataEvent;
//import org.jlab.service.pid.EventTrigger;


/**
 *
 * @author gavalian
 */
public class DetectorEvent {
    
    private List<DetectorParticle>  particleList = new ArrayList<DetectorParticle>();
    private PhysicsEvent          generatedEvent = new PhysicsEvent();
    private PhysicsEvent      reconstructedEvent = new PhysicsEvent();
    private double                rfTime         = -20.0;
    private double                eventStartTime = 0.0;
    
    
    private double            RF_OFFSET = 0.0;
    private double             RF_BUNCH = 2.004;
    private int                RF_SHIFT = 800;
    //private EventTrigger trigger = new EventTrigger();
    
    
    public DetectorEvent(){
        
    }
    
    
    public static DetectorEvent readDetectorEvent(DataEvent event){
        return DetectorData.readDetectorEvent(event);
    }
    
    public PhysicsEvent getGeneratedEvent(){
        return this.generatedEvent;
    }
    
    public PhysicsEvent getPhysicsEvent(){
        return this.reconstructedEvent;
    }
    
    public void setStartTime(double starttime){
        this.eventStartTime = starttime;
        if(this.rfTime>0){
            double   delta = starttime - rfTime + this.RF_SHIFT*this.RF_BUNCH;
            double t0_corr = delta%this.RF_BUNCH - this.RF_BUNCH/2.0;
            this.eventStartTime = starttime + t0_corr;
        }
    }
    
    public void setRfTime(double rf){
        this.rfTime = rf;
    }
    
    public double getRfTime(){
        return rfTime;
    }
    
    public double getStartTime(){
        return this.eventStartTime;
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
    public DetectorParticle  getParticle(int index) { return this.particleList.get(index);}
    /**
     * returns detector response list contained in all the particles. first the association
     * is ran to ensure that all detector responses have proper a
     * @return 
     */
    public List<DetectorResponse>  getDetectorResponseList(){
        this.setAssociation();
        List<DetectorResponse> responses = new ArrayList<DetectorResponse>();
        for(DetectorParticle p : this.particleList){
            for(DetectorResponse r : p.getDetectorResponses()){
                responses.add(r);
            }
        }
        return responses;
    }
    
    public void moveUp(int index){
        if(index>0 && index < this.particleList.size()){
            DetectorParticle p = this.particleList.get(index);
            this.particleList.remove(index);
            this.particleList.add(0, p);
            this.setAssociation();
        }
    }
    
    public void setAssociation(){
        for(int index = 0; index < this.particleList.size(); index++){
            List<DetectorResponse> responses = particleList.get(index).getDetectorResponses();
            for(DetectorResponse r : responses){
                r.setAssociation(index);
            }
        }
    }
    
    public void addParticle(double px, double py, double pz,
            double vx, double vy, double vz){
        DetectorParticle particle = new DetectorParticle();
        particle.vector().setXYZ(px, py, pz);
        particle.vertex().setXYZ(vx, vy, vz);
        this.addParticle(particle);
    }
    
    /*
    public void setEventTrigger(EventTrigger trig){this.trigger = trig;}
    public EventTrigger getEventTrigger(){return this.trigger;}
    */
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("DETECTOR EVENT [PARTICLE = %4d]  start time = %8.3f\n", 
                this.particleList.size(),this.getStartTime()));
        for(DetectorParticle particle : this.particleList){
            str.append(particle.toString());
            str.append("\n");
        }
        return str.toString();
    }
}
