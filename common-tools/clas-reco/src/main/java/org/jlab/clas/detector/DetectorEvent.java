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



/**
 *
 * @author gavalian
 */
public class DetectorEvent {
    
    private List<DetectorParticle>  particleList = new ArrayList<DetectorParticle>();
    private PhysicsEvent          generatedEvent = new PhysicsEvent();
    private PhysicsEvent      reconstructedEvent = new PhysicsEvent();
    private DetectorHeader           eventHeader = new DetectorHeader();
    
//    private double            RF_OFFSET = 0.0;
//    private double             RF_BUNCH = 2.004;
//    private int                RF_SHIFT = 800;
    
    
    public DetectorEvent(){
        
    }
    
    
    public static DetectorEvent readDetectorEvent(DataEvent event){
        return DetectorData.readDetectorEvent(event);
    }

    public DetectorHeader getEventHeader() {
        return eventHeader;
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

    public void addEventHeader(DetectorHeader eventHeader) {
        this.eventHeader = eventHeader;
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
    
    public List<CherenkovResponse>  getCherenkovResponseList(){
        this.setAssociation();
        List<CherenkovResponse> responses = new ArrayList<CherenkovResponse>();
        for(DetectorParticle p : this.particleList){
            for(CherenkovResponse r : p.getCherenkovResponses()){
                responses.add(r);
            }
        }
        return responses;
    }
    
    public List<CalorimeterResponse>  getCalorimeterResponseList(){
        this.setAssociation();
        List<CalorimeterResponse> responses = new ArrayList<CalorimeterResponse>();
        for(DetectorParticle p : this.particleList){
            for(CalorimeterResponse r : p.getCalorimeterResponses()){
                responses.add(r);
            }
        }
        return responses;
    }
    
    public List<ScintillatorResponse>  getScintillatorResponseList(){
        this.setAssociation();
        List<ScintillatorResponse> responses = new ArrayList<ScintillatorResponse>();
        for(DetectorParticle p : this.particleList){
            for(ScintillatorResponse r : p.getScintillatorResponses()){
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
    

    
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("DETECTOR EVENT [PARTICLE = %4d]  start time = %8.3f\n", 
                this.particleList.size(),this.getEventHeader().getStartTime()));
        for(DetectorParticle particle : this.particleList){
            str.append(particle.toString());
            str.append("\n");
        }
        return str.toString();
    }
}

