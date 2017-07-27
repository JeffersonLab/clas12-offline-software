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
import org.jlab.clas.detector.DetectorHeader;
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
    private List<TaggerResponse> taggerResponses = new ArrayList<TaggerResponse>();
    private int[]  TriggerList = new int[]{11,-11,0};
    
    public EventBuilder(){
        
    }
    
    public void initEvent() {
        detectorEvent.clear();
    }

    public void initEvent(DetectorHeader head) {
        detectorEvent.clear();
        detectorEvent.addEventHeader(head);
    }
    public void addDetectorResponses(List<DetectorResponse> responses){
        detectorResponses.addAll(responses);
    }
    
    public void addCherenkovResponses(List<CherenkovResponse> responses){
        cherenkovResponses.addAll(responses);
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
    
        
    public void addTracks(List<DetectorTrack> tracks) {
        //for(DetectorTrack track : tracks){
        for(int i = 0 ; i < tracks.size(); i++){
            DetectorParticle particle = new DetectorParticle(tracks.get(i));
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
    
    public void addTaggerTracks(List<TaggerResponse> taggers) {
        //for(DetectorTrack track : tracks){
        for(int i = 0 ; i < taggers.size(); i++){
            int charge = taggers.get(i).getCharge();
            int id = taggers.get(i).getID();
            Vector3D momentum = taggers.get(i).getMomentum();
            DetectorParticle particle = new DetectorParticle(id,charge,momentum.x(),momentum.y(),momentum.z());
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
             * Matching tracks to FTOF layer 2 detector. Added to the particle and association is
             */
            index = p.getDetectorHit(this.detectorResponses, DetectorType.FTOF, 3, EBConstants.FTOF_MATCHING_2);
            //System.out.println("index FTOF-1B = " + index);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
            }
            /**
             * Matching tracks to CTOF detector. Added to the particle and association is
             */
            index = p.getDetectorHit(this.detectorResponses, DetectorType.CTOF, 0, EBConstants.CTOF_Matching);
//            //System.out.println("index CTOF = " + index);
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
                //quality = p.getDetectorHitQuality(detectorResponses, index, EBConstants.PCAL_hitRes);
                //detectorResponses.get(index).setHitQuality(quality);
            }
            
            index = p.getDetectorHit(this.detectorResponses, DetectorType.EC, 4, EBConstants.ECIN_MATCHING);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
                //quality = p.getDetectorHitQuality(detectorResponses, index, EBConstants.ECOUT_hitRes);
                //detectorResponses.get(index).setHitQuality(quality);
            }
            
            index = p.getDetectorHit(this.detectorResponses, DetectorType.EC, 7, EBConstants.ECOUT_MATCHING);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
                //quality = p.getDetectorHitQuality(detectorResponses, index, EBConstants.ECOUT_hitRes);
                //detectorResponses.get(index).setHitQuality(quality);
            }
            
            index = p.getCherenkovSignal(this.cherenkovResponses,DetectorType.HTCC);
            //System.out.println("HTCC Matching Index " + index);
            //double = p.getCherenkovSignalQuality()
            if(index>=0){
                p.addCherenkovResponse(cherenkovResponses.get(index));
                cherenkovResponses.get(index).setAssociation(n);
            } 

            index = p.getCherenkovSignal(this.cherenkovResponses,DetectorType.LTCC);
            //double = p.getCherenkovSignalQuality()
            if(index>=0){
                p.addCherenkovResponse(cherenkovResponses.get(index));
                cherenkovResponses.get(index).setAssociation(n);
            }             

        }
    }
    
    public void processNeutralTracks(){
        
       
        
        List<DetectorResponse>  responsesPCAL = this.getUnmatchedResponses((List<DetectorResponse>) detectorResponses, DetectorType.EC, 1);
        
        List<DetectorParticle>  particles = new ArrayList<DetectorParticle>();
        
        for(DetectorResponse r : responsesPCAL){
            DetectorParticle p = DetectorParticle.createNeutral(r);
            particles.add(p);
        }
        List<DetectorResponse>   responsesECIN = this.getUnmatchedResponses(detectorResponses, DetectorType.EC, 4);
        List<DetectorResponse>  responsesECOUT = this.getUnmatchedResponses(detectorResponses, DetectorType.EC, 7);
        List<DetectorResponse> responsesFTOF1A = this.getUnmatchedResponses(detectorResponses, DetectorType.FTOF, 1);
        List<DetectorResponse> responsesFTOF1B = this.getUnmatchedResponses(detectorResponses, DetectorType.FTOF, 2);
        List<DetectorResponse> responsesFTOF2 = this.getUnmatchedResponses(detectorResponses, DetectorType.FTOF, 3);
        
        for(int i = 0; i < particles.size(); i++){
            DetectorParticle p = particles.get(i);
            int index = p.getDetectorHit(responsesECIN, DetectorType.EC, 4, EBConstants.ECIN_MATCHING);
            if(index>=0){ p.addResponse(responsesECIN.get(index), true); responsesECIN.get(index).setAssociation(i);}
            index = p.getDetectorHit(responsesECOUT, DetectorType.EC, 7, EBConstants.ECOUT_MATCHING);
            if(index>=0){ p.addResponse(responsesECOUT.get(index), true); responsesECOUT.get(index).setAssociation(i);}
            index = p.getDetectorHit(responsesFTOF1A, DetectorType.FTOF, 1, EBConstants.FTOF_MATCHING_1A);
            if(index>=0){ p.addResponse(responsesFTOF1A.get(index), true); responsesFTOF1A.get(index).setAssociation(i);}
            index = p.getDetectorHit(responsesFTOF1B, DetectorType.FTOF, 2, EBConstants.FTOF_MATCHING_1B);
            if(index>=0){ p.addResponse(responsesFTOF1B.get(index), true); responsesFTOF1B.get(index).setAssociation(i);}
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
            int calorimeter_count = responsesPCAL.size() + responsesECIN.size() + responsesECOUT.size();
            int scintillator_count = responsesFTOF1A.size() + responsesFTOF1B.size() + responsesFTOF2.size();
            if(calorimeter_count>1  && scintillator_count==0){
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
    
    /*
    public List<ScintillatorResponse> getUnmatchedFTOFResponses(List<ScintillatorResponse> list, DetectorType type, int layer){
        List<ScintillatorResponse>  responses = new ArrayList<ScintillatorResponse>();
        for(ScintillatorResponse r : list){
            if(r.getDescriptor().getType()==type&&r.getDescriptor().getLayer()==layer&&r.getAssociation()<0){
                responses.add(r);
            }
        }
        return responses;
    }
    */
        
    public void assignTrigger()  {
        int i = 0;
        boolean hasTrigger=false;
        while(hasTrigger==false) {
              
                    if(TriggerList[i]==11){
                    ElectronTriggerOption electron = new ElectronTriggerOption();
                    hasTrigger = electron.assignSoftwareTrigger(detectorEvent);
                    }
                    if(TriggerList[i]==-11){
                    PositronTriggerOption positron = new PositronTriggerOption();
                    hasTrigger= positron.assignSoftwareTrigger(detectorEvent);
                    }
                    if(TriggerList[i]==-211){
                    NegPionTriggerOption negpion = new NegPionTriggerOption();
                    hasTrigger = negpion.assignSoftwareTrigger(detectorEvent);
                    }
                    if(TriggerList[i]==211){
                    PosPionTriggerOption pospion = new PosPionTriggerOption();
                    hasTrigger = pospion.assignSoftwareTrigger(detectorEvent);
                    }
                    if(TriggerList[i]==0){
                    hasTrigger = true;
                    }
                        
               
                i = i + 1;
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

    class TriggerOptions {
        
        public int id;
        public int score_requirement;
        public int charge;
        
        TriggerOptions() {
            initID();
        }
        
        public void initID() {
            //Initialize parameters
        }
        
        public void setID(int pid) {
            this.id = pid;
        }
        
        public void setScoreRequirement(int sc) {
            this.score_requirement = sc;
        }
        
        public void setCharge(int ch) {
            this.charge = ch;
        }
        
        public boolean assignSoftwareTrigger(DetectorEvent event) {
            boolean flag = false;
            int npart = event.getParticles().size();
            for(int i = 0; i < npart; i++){
                DetectorParticle p = event.getParticle(i);
                if(p.getSoftwareTriggerScore()>=this.score_requirement) { //Possible Electron
                    //System.out.println("The requirements is " + this.score_requirement);
                    if(this.charge==p.getCharge()){
                        p.setPid(this.id);
                            }


                    }
                }

       
            int    index = -1;
            double best_p = 0.0;
            for(int i = 0; i < npart; i++){
                if(event.getParticle(i).getPid()==this.id){
                    if(event.getParticle(i).vector().mag()>best_p){ //Sorting the Momentum
                        best_p = event.getParticle(i).vector().mag();
                        index = i;
                    }
                }
            }
       
                if(index>0){
                    event.moveUp(index);
                    if(event.getParticle(0).getPid()==this.id){
                        flag = true;
                    }
                }
            

                return flag;
            }

        }

    class ElectronTriggerOption extends TriggerOptions {

         @Override
         public void initID() {
             this.setID(11);
             this.setScoreRequirement(110);
             this.setCharge(-1);
         }
         
    }    

    class PositronTriggerOption extends TriggerOptions {

         @Override
         public void initID() {
             this.setID(-11);
             this.setScoreRequirement(110);
             this.setCharge(1);
         }
         
    }  
 
    class NegPionTriggerOption extends TriggerOptions {

         @Override
         public void initID() {
             this.setID(-211);
             this.setScoreRequirement(10);
             this.setCharge(-1);
         }
         
    }    

    class PosPionTriggerOption extends TriggerOptions {

         @Override
         public void initID() {
             this.setID(211);
             this.setScoreRequirement(10);
             this.setCharge(1);
         }
         
    }  
