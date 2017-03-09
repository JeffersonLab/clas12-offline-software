/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorTrack;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.detector.CherenkovResponse;

/**
 *
 * @author gavalian
 */
public class EventBuilder {
    
    private DetectorEvent              detectorEvent = new DetectorEvent();
    private List<DetectorResponse> detectorResponses = new ArrayList<DetectorResponse>();
    private List<CherenkovResponse> cherenkovResponses = new ArrayList<CherenkovResponse>();
    
    public EventBuilder(){
        
    }
    
    public void addDetectorResponses(List<DetectorResponse> responses){
        detectorResponses.addAll(responses);
    }
    
    public void addCherenkovResponses(List<CherenkovResponse> responses){
        cherenkovResponses.addAll(responses);
    }

    /**
     * add tracks to the detector event class. First a particle is initialized from the track
     * and added to the detector event.
     * @param tracks 
     */
    public void addTracks(List<DetectorTrack> tracks){
        detectorEvent.clear();
        for(DetectorTrack track : tracks){
            DetectorParticle particle = new DetectorParticle(track);
            detectorEvent.addParticle(particle);
        }
    }
    /**
     * processes all particles and associating detector responses with given cuts to each particle.
     * the thresholds are described in the EBConstatns class for each layer of the detector.
     */
    public void processHitMatching(){
        
        //List<DetectorResponse>  responseFTOF1A = DetectorResponse.getListByLayer(detectorResponses, DetectorType.FTOF, 1);
        //List<DetectorResponse>  responseFTOF1B = DetectorResponse.getListByLayer(detectorResponses, DetectorType.FTOF, 2); 
        
        //System.out.println("Detector response store size = " + this.detectorResponses.size());
        int np = detectorEvent.getParticles().size();
        
        for(int n = 0; n < np; n++){
            DetectorParticle  p = this.detectorEvent.getParticle(n);
            /**
             * Matching tracks to FTOF layer 1A detector. Added to the particle and association is
             * set with a particle needed.
             */
            int index = p.getDetectorHit(this.detectorResponses, DetectorType.FTOF, 1, EBConstants.FTOF_MATCHING_1A);
            //System.out.println("index FTOF-1A = " + index);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
            }
            /**
             * Matching tracks to FTOF layer 1B detector. Added to the particle and association is
             */
            index = p.getDetectorHit(this.detectorResponses, DetectorType.FTOF, 2, EBConstants.FTOF_MATCHING_1B);
            //System.out.println("index FTOF-1B = " + index);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
            }
            /**
             * Matching tracks to FTOF layer 1B detector. Added to the particle and association is
             */
            index = p.getDetectorHit(this.detectorResponses, DetectorType.FTOF, 3, EBConstants.FTOF_MATCHING_2);
            //System.out.println("index FTOF-1B = " + index);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
            }
            /**
             * Matching tracks to PCAL (first layer of ECAL) and adding to the particle if reasonable match
             * is found, and proper association is set.
             */
            index = p.getDetectorHit(this.detectorResponses, DetectorType.EC, 1, EBConstants.PCAL_MATCHING);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
            }
            
            index = p.getDetectorHit(this.detectorResponses, DetectorType.EC, 4, EBConstants.PCAL_MATCHING);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
            }
            
            index = p.getDetectorHit(this.detectorResponses, DetectorType.EC, 7, EBConstants.PCAL_MATCHING);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
            }
            
            index = p.getCherenkovSignal(this.cherenkovResponses);
            if(index>=0){
                p.addCherenkovResponse(cherenkovResponses.get(index));
                cherenkovResponses.get(index).setAssociation(n);
            } 
        }
    }
    
    public void processNeutralTracks(){
        
        List<DetectorResponse>  responsesPCAL = this.getUnmatchedResponses(detectorResponses, DetectorType.EC, 1);
        
        List<DetectorParticle>  particles = new ArrayList<DetectorParticle>();
        
        for(DetectorResponse r : responsesPCAL){
            DetectorParticle p = DetectorParticle.createNeutral(r);
            particles.add(p);
        }
        
        List<DetectorResponse>   responsesECIN = this.getUnmatchedResponses(detectorResponses, DetectorType.EC, 4);
        List<DetectorResponse>  responsesECOUT = this.getUnmatchedResponses(detectorResponses, DetectorType.EC, 7);
        
        for(int i = 0; i < particles.size(); i++){
            DetectorParticle p = particles.get(i);
            int index = p.getDetectorHit(responsesECIN, DetectorType.EC, 4, EBConstants.ECIN_MATCHING);
            if(index>=0){ p.addResponse(responsesECIN.get(index), true); responsesECIN.get(index).setAssociation(i);}
            index = p.getDetectorHit(responsesECOUT, DetectorType.EC, 7, EBConstants.ECOUT_MATCHING);
            if(index>=0){ p.addResponse(responsesECOUT.get(index), true); responsesECOUT.get(index).setAssociation(i);}
        }
        
        for(DetectorParticle p : particles){
            double energy = p.getEnergy(DetectorType.EC);
            double px = p.vector().x();
            double py = p.vector().y();
            double pz = p.vector().z();
            p.setPid(22);
            p.setCharge(0);
            p.vector().setXYZ(px*energy/EBConstants.ECAL_SAMPLINGFRACTION, 
                    py*energy/EBConstants.ECAL_SAMPLINGFRACTION,
                    pz*energy/EBConstants.ECAL_SAMPLINGFRACTION);
            if(p.getDetectorResponses().size()>1){
                detectorEvent.addParticle(p);
            }
        }
        
        detectorEvent.setAssociation();
        //System.out.println(" PCAL RESPONSES = " + responsesPCAL.size());
    }
    
    public List<DetectorResponse> getUnmatchedResponses(List<DetectorResponse> list, DetectorType type, int layer){
        List<DetectorResponse>  responses = new ArrayList<DetectorResponse>();
        for(DetectorResponse r : list){
            if(r.getDescriptor().getType()==type&&r.getDescriptor().getLayer()==layer&&r.getAssociation()<0){
                responses.add(r);
            }
        }
        return responses;
    }
    /**
     * Assigns PID and sets start time
     */
     public void assignTrigger()  {
        int npart = this.detectorEvent.getParticles().size();
       
        for(int i = 0; i < npart; i++){
            DetectorParticle p = detectorEvent.getParticle(i);
            if((p.hasHit(DetectorType.FTOF, 2)|| p.hasHit(DetectorType.FTOF, 1))||
                    (p.hasHit(DetectorType.EC, 1)&&p.hasHit(DetectorType.EC, 4))){
                double sfraction = p.getEnergyFraction(DetectorType.EC);
                //System.out.println(sfraction);
                if(sfraction>EBConstants.ECAL_SAMPLINGFRACTION_CUT){
                    if(p.getCharge()<0)
                        p.setPid(11);
                }
            }
        }
       
        int    index = -1;
        double best_p = 0.0;
        for(int i = 0; i < npart; i++){
            if(detectorEvent.getParticle(i).getPid()==11){
                if(detectorEvent.getParticle(i).vector().mag()>best_p){
                    best_p = detectorEvent.getParticle(i).vector().mag();
                    index = i;
                }
            }
        }
       
        if(index>0){
            this.detectorEvent.moveUp(index);
        }
       
       
}
    
    
    public DetectorEvent  getEvent(){return this.detectorEvent;}
    
    public void show(){

        int np = this.detectorEvent.getParticles().size();
        System.out.println(">>>>>>>>> DETECTOR EVENT WITH PARTICLE COUNT # " + np);
        System.out.println(this.detectorEvent.toString());
        /*
        for(int n = 0; n < np; n++){
            System.out.println(detectorEvent.getParticle(n));
        }*/
    }
}
