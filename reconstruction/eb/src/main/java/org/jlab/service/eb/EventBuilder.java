package org.jlab.service.eb;

import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.jlab.clas.physics.Vector3;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.eb.EBCCDBConstants;
import org.jlab.rec.eb.EBCCDBEnum;

/**
 *
 * @author gavalian
 */
public class EventBuilder {

    private DetectorEvent              detectorEvent = new DetectorEvent();
    private List<DetectorResponse> detectorResponses = new ArrayList<DetectorResponse>();
    private List<CherenkovResponse> cherenkovResponses = new ArrayList<CherenkovResponse>();
    private List<TaggerResponse> taggerResponses = new ArrayList<TaggerResponse>();
    private List<Map<DetectorType,Integer>> ftIndices = new ArrayList<Map<DetectorType,Integer>>();
    private int[]  TriggerList = new int[]{11,-11,0};
    private HashMap<Integer,Integer> pindex_map = new HashMap<Integer, Integer>();

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
    
    public void addFTIndices(List<Map<DetectorType, Integer>> ftindex) {
        ftIndices.addAll(ftindex);
    }

    /**
     * add tracks to the detector event class. First a particle is initialized from the track
     * and added to the detector event.
     * @param tracks 
     */
    public void addForwardTracks(List<DetectorTrack> tracks) {
        for(int i = 0 ; i < tracks.size(); i++){
            DetectorParticle particle = new DetectorParticle(tracks.get(i));
            //particle.setStatus(1);
            detectorEvent.addParticle(particle);
        }
    }
    
    public void addCentralTracks(List<DetectorTrack> tracks) {
        for(int i = 0 ; i < tracks.size(); i++){
            DetectorParticle particle = new DetectorParticle(tracks.get(i));
            detectorEvent.addParticle(particle);
        }
    }
    
//    public void addTaggerTracks(List<TaggerResponse> taggers) {
//        for(int i = 0 ; i < taggers.size(); i++){
//            //DetectorParticle particle = new DetectorParticle(taggers.get(i));
//            DetectorParticle particle = DetectorParticle.createFTparticle(taggers.get(i));
//            // FIXME:  get rid of hardcoded 100
//            //particle.setStatus(100);
//            detectorEvent.addParticle(particle);
//        }
//    }
    
    public void addForwardTaggerParticles(List<DetectorParticle> particles) {
        //for(DetectorTrack track : tracks){                                                                                         
        for(int i = 0 ; i < particles.size(); i++){
            //DetectorParticle particle = new DetectorParticle(particles.get(i));                                                    
            detectorEvent.addParticle(particles.get(i));

        }
    }
    

    /**
     * processes all particles and associating detector responses with given cuts to each particle.
     * the thresholds are described in the EBConstatns class for each layer of the detector.
     */
    public void processHitMatching(){
        
        int np = detectorEvent.getParticles().size();
        for(int n = 0; n < np; n++){
            DetectorParticle  p = this.detectorEvent.getParticle(n);

            double quality = 0.0;
            
            // Matching tracks to detector responses, adding
            // responses to the particle if reasonable match
            // is found and set associations

            // Matching tracks to FTOF layer 1A detector.
            Double ftof1a_match_cut = EBCCDBConstants.getDouble(EBCCDBEnum.FTOF_MATCHING_1A);
            int index = p.getDetectorHit(this.detectorResponses, DetectorType.FTOF, 1, ftof1a_match_cut);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
            }

            // Matching tracks to FTOF layer 1B detector.
            Double ftof1b_match_cut = EBCCDBConstants.getDouble(EBCCDBEnum.FTOF_MATCHING_1B);
            //System.out.println("FTOF Match Cut " + ftof1b_match_cut);
            index = p.getDetectorHit(this.detectorResponses, DetectorType.FTOF, 2, ftof1b_match_cut);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
            }

            // Matching tracks to FTOF layer 2 detector.
            Double ftof2_match_cut = EBCCDBConstants.getDouble(EBCCDBEnum.FTOF_MATCHING_2);
            index = p.getDetectorHit(this.detectorResponses, DetectorType.FTOF, 3, ftof2_match_cut);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
            }

            // FIXME:  Remove this, CD matching should just be imported.
            // Matching tracks to CTOF detector.
//            index = p.getDetectorHit(this.detectorResponses, DetectorType.CTOF, 0, EBConstants.CTOF_Matching);
//            if(index>=0){
//                p.addResponse(detectorResponses.get(index), true);
//                detectorResponses.get(index).setAssociation(n);
//            }

            // FIXME:  Remove this, CD matching should just be imported.
            // Matching tracks to CND detector.
            //index = p.getDetectorHit(this.detectorResponses, DetectorType.CND, 0, EBConstants.CND_Matching);
            //if(index>=0){
            //    p.addResponse(detectorResponses.get(index), true);
            //    detectorResponses.get(index).setAssociation(n);
            //}

            // Matching tracks to PCAL:
            Double pcal_match_cut = EBCCDBConstants.getDouble(EBCCDBEnum.PCAL_MATCHING);
            //System.out.println("PCAL MATCH CUT " + pcal_match_cut);
            index = p.getDetectorHit(this.detectorResponses, DetectorType.ECAL, 1, pcal_match_cut);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
                //quality = p.getDetectorHitQuality(detectorResponses, index, EBConstants.PCAL_hitRes);
                //detectorResponses.get(index).setHitQuality(quality);
            }
           
            // Matching tracks to EC Inner:
            Double ecin_match_cut = EBCCDBConstants.getDouble(EBCCDBEnum.ECIN_MATCHING);
            index = p.getDetectorHit(this.detectorResponses, DetectorType.ECAL, 4, ecin_match_cut);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
                //quality = p.getDetectorHitQuality(detectorResponses, index, EBConstants.ECOUT_hitRes);
                //detectorResponses.get(index).setHitQuality(quality);
            }
            
            // Matching tracks to EC Outer:
            Double ecout_match_cut = EBCCDBConstants.getDouble(EBCCDBEnum.ECOUT_MATCHING);
            index = p.getDetectorHit(this.detectorResponses, DetectorType.ECAL, 7, ecout_match_cut);
            if(index>=0){
                p.addResponse(detectorResponses.get(index), true);
                detectorResponses.get(index).setAssociation(n);
                //quality = p.getDetectorHitQuality(detectorResponses, index, EBConstants.ECOUT_hitRes);
                //detectorResponses.get(index).setHitQuality(quality);
            }
           
            // Matching tracks to HTCC:
            index = p.getCherenkovSignal(this.cherenkovResponses,DetectorType.HTCC);
            //double = p.getCherenkovSignalQuality()
            if(index>=0){
                p.addCherenkovResponse(cherenkovResponses.get(index));
                cherenkovResponses.get(index).setAssociation(n);
            } 

            // Matching tracks to LTCC:
            index = p.getCherenkovSignal(this.cherenkovResponses,DetectorType.LTCC);
            //double = p.getCherenkovSignalQuality()
            if(index>=0){
                p.addCherenkovResponse(cherenkovResponses.get(index));
                cherenkovResponses.get(index).setAssociation(n);
            }             

        }
    }
    
    public void forwardTaggerIDMatching() {
        int np = this.detectorEvent.getParticles().size();
        if(this.ftIndices.size()>0 && this.detectorEvent.getParticles().size()>0) {
            for(int n = 0 ; n < np ; n++){
                int counter = 0;
                DetectorParticle p = this.detectorEvent.getParticles().get(n);
                if(p.getTrackDetector()==DetectorType.FTCAL.getDetectorId()) {
                    int particle_calID = this.ftIndices.get(counter).get(DetectorType.FTCAL);
                    int index = getForwardTaggerMatch(this.taggerResponses, p, DetectorType.FTCAL, particle_calID);
                    if(index>=0){
                        p.addTaggerResponse(this.taggerResponses.get(index));
                        this.taggerResponses.get(index).setAssociation(n);
                    }
      
                    int particle_hodoID = this.ftIndices.get(counter).get(DetectorType.FTHODO);
                    index = getForwardTaggerMatch(this.taggerResponses, p, DetectorType.FTHODO, particle_hodoID);
                    if(index>=0){
                        p.addTaggerResponse(this.taggerResponses.get(index));
                        this.taggerResponses.get(index).setAssociation(n);
                    }
                    counter = counter + 1;
                }
        }
        }
    }
    
    public int getForwardTaggerMatch(List<TaggerResponse> hitList, DetectorParticle part, 
            DetectorType type, int ft_id) {
        int bestIndex = -1;
        for(int loop = 0; loop < hitList.size(); loop++) {
            if(type==DetectorType.FTCAL){
                if(hitList.get(loop).getDescriptor().getType()==type
                    && hitList.get(loop).getID()==ft_id){
                    bestIndex = loop;
                }
            }
            if(type==DetectorType.FTHODO){
               if(hitList.get(loop).getDescriptor().getType()==type
                     &&  hitList.get(loop).getID()==ft_id){
                   bestIndex = loop;
               }
            }
        }
        return bestIndex;
    }

    /*
     * processNeutralTracks
     * - original version, requires PCAL for all neutrals
     *
    public void processNeutralTracks(){

        // get all unmatched calorimeter responses:
        List<DetectorResponse>   responsesPCAL = this.getUnmatchedResponses(detectorResponses, DetectorType.ECAL, 1);
        List<DetectorResponse>   responsesECIN = this.getUnmatchedResponses(detectorResponses, DetectorType.ECAL, 4);
        List<DetectorResponse>  responsesECOUT = this.getUnmatchedResponses(detectorResponses, DetectorType.ECAL, 7);

        // Critical to use DetectorType for this, instead of hardcoded constants everywhere.
        List<DetectorResponse> responsesFTOF1A = this.getUnmatchedResponses(detectorResponses, DetectorType.FTOF, 1);
        List<DetectorResponse> responsesFTOF1B = this.getUnmatchedResponses(detectorResponses, DetectorType.FTOF, 2);
        List<DetectorResponse>  responsesFTOF2 = this.getUnmatchedResponses(detectorResponses, DetectorType.FTOF, 3);
       
        // setup the empty list of neutral particles:
        List<DetectorParticle>  particles = new ArrayList<DetectorParticle>();

        // add a new neutral particle for each unmatched PCAL response:
        for(DetectorResponse r : responsesPCAL){
            DetectorParticle p = DetectorParticle.createNeutral(r);
            particles.add(p);
        }

        for(int i = 0; i < particles.size(); i++){
            DetectorParticle p = particles.get(i);
            int index = p.getDetectorHit(responsesECIN, DetectorType.ECAL, 4, EBConstants.ECIN_MATCHING);
            if(index>=0){ p.addResponse(responsesECIN.get(index), true); responsesECIN.get(index).setAssociation(i);}
            index = p.getDetectorHit(responsesECOUT, DetectorType.ECAL, 7, EBConstants.ECOUT_MATCHING);
            if(index>=0){ p.addResponse(responsesECOUT.get(index), true); responsesECOUT.get(index).setAssociation(i);}
            index = p.getDetectorHit(responsesFTOF1A, DetectorType.FTOF, 1, EBConstants.FTOF_MATCHING_1A);
            if(index>=0){ p.addResponse(responsesFTOF1A.get(index), true); responsesFTOF1A.get(index).setAssociation(i);}
            index = p.getDetectorHit(responsesFTOF1B, DetectorType.FTOF, 2, EBConstants.FTOF_MATCHING_1B);
            if(index>=0){ p.addResponse(responsesFTOF1B.get(index), true); responsesFTOF1B.get(index).setAssociation(i);}
        }
        
        for(DetectorParticle p : particles){
            double energy = p.getEnergy(DetectorType.ECAL);
            double px = p.vector().x();
            double py = p.vector().y();
            double pz = p.vector().z();
            p.setPid(22);
            p.setCharge(0);
            p.vector().setXYZ(px*energy/EBConstants.ECAL_SAMPLINGFRACTION, 
                    py*energy/EBConstants.ECAL_SAMPLINGFRACTION,
                    pz*energy/EBConstants.ECAL_SAMPLINGFRACTION);

            // WRONG:
            //int calorimeter_count = responsesPCAL.size() + responsesECIN.size() + responsesECOUT.size();
            //int scintillator_count = responsesFTOF1A.size() + responsesFTOF1B.size() + responsesFTOF2.size();
            //if (calorimeter_count>0  && scintillator_count==0)
            //    detectorEvent.addParticle(p);

            // RIGHTER:
            int caloCount = p.countResponses(DetectorType.ECAL,1) +
                            p.countResponses(DetectorType.ECAL,4) +
                            p.countResponses(DetectorType.ECAL,7);
            int ftofCount = p.countResponses(DetectorType.FTOF,1) +
                            p.countResponses(DetectorType.FTOF,2) +
                            p.countResponses(DetectorType.FTOF,3);
            if (caloCount>0 && ftofCount==0)
                detectorEvent.addParticle(p);
        }
        
        detectorEvent.setAssociation();
    }
     */

    /*
     * processNeutralTracks
     * - new version, does not require PCAL for all neutrals
     *
     * FIXME:  get layer index-constants from somewhere else
     */
    public void processNeutralTracks() {

        EBCentral ebm=new EBCentral(this);
       
        // define neutrals based on unmatched ECAL clusters:
        
        // these have a PCAL cluster:
        List<DetectorParticle> partsPCAL = ebm.findNeutrals(1);
        
        // these have ECIN but no PCAL (previous line exhausted PCAL):
        List<DetectorParticle> partsECIN = ebm.findNeutrals(4);

        // these have ECOUT but no PCAL nor ECIN (previous lines exhausted PCAL and ICIN):
        List<DetectorParticle> partsECOUT = ebm.findNeutrals(7);
        
        List<DetectorParticle> particles=new ArrayList<DetectorParticle>();
        particles.addAll(partsPCAL);
        particles.addAll(partsECIN);
        particles.addAll(partsECOUT);

        // set particle kinematics:
        for(DetectorParticle p : particles) {
            final double energy = p.getEnergy(DetectorType.ECAL);
            final double px = p.vector().x();
            final double py = p.vector().y();
            final double pz = p.vector().z();
            p.setCharge(0);
            p.vector().setXYZ(px*energy/EBConstants.ECAL_SAMPLINGFRACTION, 
                    py*energy/EBConstants.ECAL_SAMPLINGFRACTION,
                    pz*energy/EBConstants.ECAL_SAMPLINGFRACTION);

            final int pcalCount = p.countResponses(DetectorType.ECAL,1);
            final int caloCount = pcalCount + 
                                  p.countResponses(DetectorType.ECAL,4) +
                                  p.countResponses(DetectorType.ECAL,7);
            final int ftofCount = p.countResponses(DetectorType.FTOF,1) +
                                  p.countResponses(DetectorType.FTOF,2) +
                                  p.countResponses(DetectorType.FTOF,3);

            // FIXME:  use ECAL timing to differentiate neutrons and photons

            // if no PCAL, call it a neutron:
            if (pcalCount == 0) p.setPid(2112);
            
            // if lots of FTOF, call it a neutron:
            // FIXME:  this currently degrades photon efficiency 
            //else if (ftofCount > 1) p.setPid(2112);

            // else it's a photon:
            else p.setPid(22);

            detectorEvent.addParticle(p);
        }
        this.pindex_map.put(2, particles.size());
        detectorEvent.setAssociation();
    }
    
    public HashMap<Integer, Integer> getPindexMap() {
        return this.pindex_map;
    }
    
    public List<DetectorResponse> getUnmatchedResponses(List<DetectorResponse> list, DetectorType type, int layer){
        if (list==null) list=detectorResponses;
        List<DetectorResponse>  responses = new ArrayList<DetectorResponse>();
        for(DetectorResponse r : list){
            if(r.getDescriptor().getType()==type&&r.getDescriptor().getLayer()==layer&&r.getAssociation()<0){
                responses.add(r);
            }
        }
        return responses;
    }
    

        
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
        //for(int n = 0; n < np; n++){
        //    System.out.println(detectorEvent.getParticle(n));
        //}
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
                if(this.charge==p.getCharge()){
                    p.setPid(this.id);
		    flag = true; //Software trigger found
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

        if(index>=0){
            event.moveUp(index);
            if(event.getParticle(0).getPid()==this.id){
                //flag = true;
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