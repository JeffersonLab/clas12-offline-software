package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.base.DataEvent;
import org.jlab.detector.base.DetectorType;


/**
 *
 * @author gavalian
 */
public class DetectorEvent {
    
    private final List<DetectorParticle>  particleList = new ArrayList<>();
    private final PhysicsEvent          generatedEvent = new PhysicsEvent();
    private final PhysicsEvent      reconstructedEvent = new PhysicsEvent();
    private DetectorHeader                 eventHeader = new DetectorHeader();
    
    public DetectorEvent(){
        
    }
   
    public void sort() {
        Collections.sort(particleList);
        setAssociation();
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
        particle.getTrack().setAssociation(this.particleList.size());
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
        List<DetectorResponse> responses = new ArrayList<>();
        for(DetectorParticle p : this.particleList){
            for(DetectorResponse r : p.getDetectorResponses()){
                responses.add(r);
            }
        }
        return responses;
    }

   public List<DetectorResponse>  getCherenkovResponseList(){
       return getResponseList(new DetectorType[]{
           DetectorType.HTCC,DetectorType.LTCC,DetectorType.RICH
       });
    }

    public List<DetectorResponse>  getCalorimeterResponseList(){
       return getResponseList(new DetectorType[]{
           DetectorType.ECAL
       });
    }

    public List<DetectorResponse>  getScintillatorResponseList(){
       return getResponseList(new DetectorType[]{
           DetectorType.FTOF,DetectorType.CTOF,DetectorType.CND,DetectorType.BAND
       });
    }

   public List<DetectorResponse>  getTaggerResponseList(){
       return getResponseList(new DetectorType[]{
           DetectorType.FTCAL,DetectorType.FTHODO
       });
    }

   public List<DetectorResponse>  getResponseList(DetectorType type) {
       return getResponseList(new DetectorType[]{type});
   }

   public List<DetectorResponse> getResponseList(DetectorType[] types) {
        this.setAssociation();
        List<DetectorResponse> responses = new ArrayList<>();
        for(DetectorParticle p : this.particleList){
            for(DetectorResponse r : p.getDetectorResponses()) {
                if (responses.contains(r)) {
                    continue;
                }
                for(DetectorType t : types) {
                    if (r.getDescriptor().getType() == t) {
                        responses.add(r);
                        break;
                    }
                }
            }
        }
        return responses;
   }

   public List<DetectorParticle> getCentralParticles() {
       List<DetectorParticle> central = new ArrayList<>();
       for(DetectorParticle p : this.particleList) {
           if(p.getTrackDetector()==DetectorType.CVT.getDetectorId()){
               central.add(p);
           }
       }
       return central;
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
            for(DetectorResponse r : particleList.get(index).getDetectorResponses()) {
                r.clearAssociations();
            }
        }
        for(int index = 0; index < this.particleList.size(); index++){
            for(DetectorResponse r : particleList.get(index).getDetectorResponses()) {
                if (!r.hasAssociation(index)) {
                    r.addAssociation(index);
                }
            }
            particleList.get(index).getTrack().setAssociation(index);
        }
    }

    public void addParticle(double px, double py, double pz,
            double vx, double vy, double vz){
        DetectorParticle particle = new DetectorParticle();
        particle.vector().setXYZ(px, py, pz);
        particle.vertex().setXYZ(vx, vy, vz);
        this.addParticle(particle);
    }
    
    public DetectorParticle getTriggerParticle() {
        for (int ii=0; ii<particleList.size(); ii++) {
            if (particleList.get(ii).isTriggerParticle()) {
                return particleList.get(ii);
            }
        }
        return null;
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
