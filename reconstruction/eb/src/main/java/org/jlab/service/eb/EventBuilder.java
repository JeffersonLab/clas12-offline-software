/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.detector.CalorimeterResponse;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorTrack;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.detector.CherenkovResponse;
import org.jlab.clas.detector.ScintillatorResponse;
import org.jlab.clas.detector.TaggerResponse;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author gavalian
 */
public class EventBuilder {
    
    private DetectorEvent              detectorEvent = new DetectorEvent();
    private List<DetectorResponse> detectorResponses = new ArrayList<DetectorResponse>();
    private List<CherenkovResponse> cherenkovResponses = new ArrayList<CherenkovResponse>();
    private List<ScintillatorResponse> scintillatorResponses = new ArrayList<ScintillatorResponse>();
    private List<CalorimeterResponse> calorimeterResponses = new ArrayList<CalorimeterResponse>();
    private List<TaggerResponse> taggerResponses = new ArrayList<TaggerResponse>();
    private int[]  NegativeTriggerList = new int[]{11};
    
    public EventBuilder(){
        
    }
    
    public void addDetectorResponses(List<DetectorResponse> responses){
        detectorResponses.addAll(responses);
    }
    
    public void addCherenkovResponses(List<CherenkovResponse> responses){
        cherenkovResponses.addAll(responses);
    }
    
    public void addCalorimeterResponses(List<CalorimeterResponse> responses){
        calorimeterResponses.addAll(responses);
    }
    
    public void addScintillatorResponses(List<ScintillatorResponse> responses){
        scintillatorResponses.addAll(responses);
    }
    
    public void addTaggerResponses(List<TaggerResponse> responses){
        taggerResponses.addAll(responses);
    }

    /**
     * add tracks to the detector event class. First a particle is initialized from the track
     * and added to the detector event.
     * @param tracks 
     */
    //public void addTracks(List<DetectorTrack> tracks){
    
        
    public void addHBTracks(List<DetectorTrack> tracks) {
        detectorEvent.clear();
        //for(DetectorTrack track : tracks){
        for(int i = 0 ; i < tracks.size(); i++){
            DetectorParticle particle = new DetectorParticle(tracks.get(i));
            detectorEvent.addParticle(particle);
        }
    }
    
    
    public void addCentralTracks(List<DetectorTrack> tracks) {
        detectorEvent.clear();
        //for(DetectorTrack track : tracks){
        for(int i = 0 ; i < tracks.size(); i++){
            DetectorParticle particle = new DetectorParticle(tracks.get(i));
            //System.out.println("Central Track CTOF intersection....." + tracks.get(i).getTrackIntersect());
            detectorEvent.addParticle(particle);
        }
    }
    
    public void addTaggerTracks(List<TaggerResponse> taggers) {
        //for(DetectorTrack track : tracks){
        for(int i = 0 ; i < taggers.size(); i++){
            int charge = taggers.get(i).getCharge();
            int id = taggers.get(i).getID();
            Vector3D momentum = taggers.get(i).getMomentum();
            DetectorParticle particle = new DetectorParticle(id,charge,momentum.x(),momentum.y(),momentum.z());
            //System.out.println("Central Track CTOF intersection....." + tracks.get(i).getTrackIntersect());
            detectorEvent.addParticle(particle);
        }
    }
    
    /*
    public void addTBTracks(List<DetectorTrack> tracks, List<double[]> covMatrices) {
        detectorEvent.clear();
        //for(DetectorTrack track : tracks){
        for(int i = 0 ; i < tracks.size(); i++){
            DetectorParticle particle = new DetectorParticle(tracks.get(i), covMatrices.get(i));
            detectorEvent.addParticle(particle);
        }
        
    }*/
    
    public void addTBTracks(List<DetectorTrack> tracks) {
        //for(DetectorTrack track : tracks){
        for(int i = 0 ; i < tracks.size(); i++){
            DetectorParticle particle = new DetectorParticle(tracks.get(i));
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
        //System.out.println("Number of Particles = " + np);
        for(int n = 0; n < np; n++){
            DetectorParticle  p = this.detectorEvent.getParticle(n);

            double quality = 0.0;
            /**
             * Matching tracks to FTOF layer 1A detector. Added to the particle and association is
             * set with a particle needed.
             */
            int index = p.getScintillatorHit(this.scintillatorResponses, DetectorType.FTOF, 1, EBConstants.FTOF_MATCHING_1A);
            //System.out.println("index FTOF-1A = " + index);
            if(index>=0){
                p.addResponse(scintillatorResponses.get(index), true);
                scintillatorResponses.get(index).setAssociation(n);
            }
            /**
             * Matching tracks to FTOF layer 1B detector. Added to the particle and association is
             */
            index = p.getScintillatorHit(this.scintillatorResponses, DetectorType.FTOF, 2, EBConstants.FTOF_MATCHING_1B);
            //System.out.println("index FTOF-1B = " + index);
            if(index>=0){
                p.addResponse(scintillatorResponses.get(index), true);
                scintillatorResponses.get(index).setAssociation(n);
            }
            /**
             * Matching tracks to FTOF layer 2 detector. Added to the particle and association is
             */
            index = p.getScintillatorHit(this.scintillatorResponses, DetectorType.FTOF, 3, EBConstants.FTOF_MATCHING_2);
            //System.out.println("index FTOF-1B = " + index);
            if(index>=0){
                p.addResponse(scintillatorResponses.get(index), true);
                scintillatorResponses.get(index).setAssociation(n);
            }
            /**
             * Matching tracks to FTOF layer 2 detector. Added to the particle and association is
             */
//            index = p.getScintillatorHit(this.scintillatorResponses, DetectorType.CTOF, 0, EBConstants.CTOF_Matching);
//            //System.out.println("index CTOF = " + index);
//            if(index>=0){
//                p.addResponse(scintillatorResponses.get(index), true);
//                scintillatorResponses.get(index).setAssociation(n);
//            }
            /**
             * Matching tracks to PCAL (first layer of ECAL) and adding to the particle if reasonable match
             * is found, and proper association is set.
             */
            index = p.getCalorimeterHit(this.calorimeterResponses, DetectorType.EC, 1, EBConstants.PCAL_MATCHING);
            if(index>=0){
                p.addResponse(calorimeterResponses.get(index), true);
                calorimeterResponses.get(index).setAssociation(n);
                //quality = p.getDetectorHitQuality(detectorResponses, index, EBConstants.PCAL_hitRes);
                //detectorResponses.get(index).setHitQuality(quality);
            }
            
            index = p.getCalorimeterHit(this.calorimeterResponses, DetectorType.EC, 4, EBConstants.ECIN_MATCHING);
            if(index>=0){
                p.addResponse(calorimeterResponses.get(index), true);
                calorimeterResponses.get(index).setAssociation(n);
                //quality = p.getDetectorHitQuality(detectorResponses, index, EBConstants.ECOUT_hitRes);
                //detectorResponses.get(index).setHitQuality(quality);
            }
            
            index = p.getCalorimeterHit(this.calorimeterResponses, DetectorType.EC, 7, EBConstants.PCAL_MATCHING);
            if(index>=0){
                p.addResponse(calorimeterResponses.get(index), true);
                calorimeterResponses.get(index).setAssociation(n);
                //quality = p.getDetectorHitQuality(detectorResponses, index, EBConstants.ECOUT_hitRes);
                //detectorResponses.get(index).setHitQuality(quality);
            }
            
            index = p.getCherenkovSignal(this.cherenkovResponses);
            //double = p.getCherenkovSignalQuality()
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
//     public void assignTrigger()  {
//        int npart = this.detectorEvent.getParticles().size();
//        Trigger trigger = new Trigger();
// 
//        for(int i = 0; i < npart; i++){
//                if(this.detectorEvent.getParticles().get(i).getCharge()<0) {
//                for(int b = 0 ; b < this.NegativeTriggerList.length ; b++){
//                    trigger.TriggerCheck(this.detectorEvent.getParticles().get(i), this.NegativeTriggerList[b]);
//                }
//                }
//        }
//       
//        int best_trigger_index = -1;
//        int best_score = 0;
//        
//        for(int i = 0; i < npart; i++){
//       
//            if(detectorEvent.getParticle(i).getScore()>=110 && detectorEvent.getParticles().get(i).getCharge()<0){ //Find the highest trigger score
//                if(detectorEvent.getParticle(i).getScore()>best_score){
//                    detectorEvent.getParticle(i).setPid(11);
//                    best_score = detectorEvent.getParticle(i).getScore();
//                    best_trigger_index = i;
//                }
//            }
//          
//        }
        
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
       

        
//       
//        int highest_p_index = -1;
//        double highest_p = 0.0;
//        
//        for(int i = 0; i < npart; i++){
//                if(detectorEvent.getParticle(i).vector().mag()>highest_p){  //Find the highest momentum track
//                    highest_p = detectorEvent.getParticle(i).vector().mag();
//                    highest_p_index = i;
//                }
//            }
//        
//        if(detectorEvent.getParticle(highest_p_index).getScore() ==         
//                detectorEvent.getParticle(best_trigger_index).getScore()){  //If two are tied with same trigger
//            this.detectorEvent.moveUp(highest_p_index);                     //score, the track with the higher
//        }                                                                   //momentum is assigned as the trigger

    
    
    public static class Trigger implements Comparable {
            private Boolean htccSignalCheck;
            private Boolean sfCheck;
            private Boolean hasFTOF;
            
            public Trigger(){
            }
                
            public void TriggerCheck(DetectorParticle p, int tid){
                    
            htccSignalCheck = p.getNphe()>EBConstants.HTCC_NPHE_CUT;
            sfCheck = p.getEnergyFraction(DetectorType.EC)>EBConstants.ECAL_SAMPLINGFRACTION_CUT;
            hasFTOF = p.hasHit(DetectorType.FTOF, 2)|| p.hasHit(DetectorType.FTOF, 1);
            
            switch(tid) {
                case 11:
                    int triggerScore = 0;
                    if(htccSignalCheck==true){
                        triggerScore = triggerScore + 1000;
                    }
                    if(sfCheck==true){
                        triggerScore = triggerScore + 100;
                    }
                    if(hasFTOF==true){
                        triggerScore = triggerScore + 10;
                    }
                    p.setScore(triggerScore);
                    //System.out.println("The trigger score is ....  " + triggerScore);
                    }

            }
            public int compareTo(Object o) {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
