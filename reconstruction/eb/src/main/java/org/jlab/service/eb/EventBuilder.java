package org.jlab.service.eb;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.detector.base.DetectorType;

import org.jlab.clas.detector.CalorimeterResponse;
import org.jlab.clas.detector.DetectorHeader;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorTrack;
import org.jlab.clas.detector.CherenkovResponse;
import org.jlab.clas.detector.ScintillatorResponse;
import org.jlab.clas.detector.TaggerResponse;

import org.jlab.clas.physics.Vector3;
import org.jlab.geom.prim.Vector3D;

import org.jlab.rec.eb.EBConstants;
import org.jlab.rec.eb.EBCCDBConstants;
import org.jlab.rec.eb.EBCCDBEnum;
import org.jlab.rec.eb.EBUtil;
import org.jlab.rec.eb.SamplingFractions;

/**
 *
 * @author gavalian
 * @author baltzell
 */
public class EventBuilder {

    public EBCCDBConstants ccdb;
    private DetectorEvent               detectorEvent = new DetectorEvent();
    private List<DetectorResponse>  detectorResponses = new ArrayList<DetectorResponse>();
    private List<TaggerResponse>      taggerResponses = new ArrayList<TaggerResponse>();
    private List<Map<DetectorType,Integer>> ftIndices = new ArrayList<Map<DetectorType,Integer>>();
    private int[]  TriggerList = new int[]{11,-11,211,-211,0};
    private HashMap<Integer,Integer> pindex_map = new HashMap<Integer, Integer>();

    public EventBuilder(EBCCDBConstants ccdb){
        this.ccdb=ccdb;
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
    public void addTracks(List<DetectorTrack> tracks) {
        for(int i = 0 ; i < tracks.size(); i++){
            DetectorParticle particle = new DetectorParticle(tracks.get(i));
            detectorEvent.addParticle(particle);
        }
    }
    
    public void addParticles(List<DetectorParticle> particles) {
        for(int i = 0 ; i < particles.size(); i++){
            detectorEvent.addParticle(particles.get(i));
        }
    }
   
    /**
     * set every particle's status
     */
    public void setParticleStatuses() {
        for (int ii=0; ii<this.detectorEvent.getParticles().size(); ii++) {
            EBUtil.setParticleStatus(this.detectorEvent.getParticles().get(ii),ccdb);
        }
    }

    /**
     * processes all particles and associating detector responses with given cuts to each particle.
     */
    public void processHitMatching(){
        
        int np = detectorEvent.getParticles().size();
        for(int n = 0; n < np; n++){
            DetectorParticle  p = this.detectorEvent.getParticle(n);

            double quality = 0.0;
            
            // Matching tracks to detector responses, adding
            // responses to the particle if reasonable match
            // is found and set associations

            // only match with FTOF/ECAL/HTCC/LTCC if it's a DC track:
            if (p.getTrackDetectorID()==DetectorType.DC.getDetectorId()) {

                // FTOF:
                findMatchingHit(n,p,detectorResponses,DetectorType.FTOF, 1, ccdb.getDouble(EBCCDBEnum.FTOF_MATCHING_1A));
                findMatchingHit(n,p,detectorResponses,DetectorType.FTOF, 2, ccdb.getDouble(EBCCDBEnum.FTOF_MATCHING_1B));
                findMatchingHit(n,p,detectorResponses,DetectorType.FTOF, 3, ccdb.getDouble(EBCCDBEnum.FTOF_MATCHING_2));
                
                // ECAL:
                findMatchingHit(n,p,detectorResponses,DetectorType.ECAL, 1, ccdb.getDouble(EBCCDBEnum.PCAL_MATCHING));
                findMatchingHit(n,p,detectorResponses,DetectorType.ECAL, 4, ccdb.getDouble(EBCCDBEnum.ECIN_MATCHING));
                findMatchingHit(n,p,detectorResponses,DetectorType.ECAL, 7, ccdb.getDouble(EBCCDBEnum.ECOUT_MATCHING));

                // HTCC:
                int index = p.getCherenkovSignal(this.detectorResponses,DetectorType.HTCC);
                if(index>=0){
                    p.addResponse(detectorResponses.get(index));
                    detectorResponses.get(index).setAssociation(n);
                } 

                // LTCC:
                index = p.getCherenkovSignal(this.detectorResponses,DetectorType.LTCC);
                if(index>=0){
                    p.addResponse(detectorResponses.get(index));
                    detectorResponses.get(index).setAssociation(n);
                }
            }

            // only match with CTOF/CND if it's a central track:
            else if (p.getTrackDetectorID()==DetectorType.CVT.getDetectorId()) {
                // NOTE:  Should we do 2-d matching in cylindrical coordinates for CD?
                findMatchingHit(n,p,detectorResponses,DetectorType.CTOF,0, ccdb.getDouble(EBCCDBEnum.CTOF_DZ));
                findMatchingHit(n,p,detectorResponses,DetectorType.CND, 0, ccdb.getDouble(EBCCDBEnum.CND_DZ));
            }

        }
    }

    /**
     * Find closest matching response of given detector type and layer within given distance.
     * If found, associate it with the particle.
     *
     * @param pindex the particle's index
     * @param particle the particle
     * @param responses all responses
     * @param type detector type to find
     * @param layer detector layer to find
     * @param distance maximum distance between trajectory and hit
     *
     * @return whether match was found
     */
    public boolean findMatchingHit(
            final int pindex, DetectorParticle particle, List<DetectorResponse> responses,
            DetectorType type, final int layer, final double distance) {
        final int index = particle.getDetectorHit(responses,type,layer,distance);
        if (index>=0) {
            particle.addResponse(responses.get(index),true);
            responses.get(index).setAssociation(pindex);
            return true;
        }
        return false;
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
     */
    public void processNeutralTracks() {

        EBMatching ebm=new EBMatching(this);
       
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
            
            final double visEnergy = p.getEnergy(DetectorType.ECAL);
            final double sampFract = SamplingFractions.getMean(22,p,ccdb);
            final double corEnergy = visEnergy / sampFract;

            // direction cosines:
            final double cx = p.vector().x();
            final double cy = p.vector().y();
            final double cz = p.vector().z();
            
            p.setCharge(0);
            p.vector().setXYZ(cx*corEnergy,cy*corEnergy,cz*corEnergy);
                    
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
            if(r.getDescriptor().getType()==type &&
               (r.getDescriptor().getLayer()==layer || layer<=0) &&
               r.getAssociation()<0){
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
                hasTrigger = electron.assignSoftwareTrigger(detectorEvent,ccdb);
            }
            else if(TriggerList[i]==-11){
                PositronTriggerOption positron = new PositronTriggerOption();
                hasTrigger= positron.assignSoftwareTrigger(detectorEvent,ccdb);
            }
            else if(TriggerList[i]==-211){
                NegPionTriggerOption negpion = new NegPionTriggerOption();
                hasTrigger = negpion.assignSoftwareTrigger(detectorEvent,ccdb);
            }
            else if(TriggerList[i]==211){
                PosPionTriggerOption pospion = new PosPionTriggerOption();
                hasTrigger = pospion.assignSoftwareTrigger(detectorEvent,ccdb);
            }
            else if(TriggerList[i]==0){
                hasTrigger = true;
            }
            i++;
        }
    }


    public DetectorEvent  getEvent(){return this.detectorEvent;}

    public void show(){

        int np = this.detectorEvent.getParticles().size();
        System.out.println(">>>>>>>>> DETECTOR EVENT WITH PARTICLE COUNT # " + np);
        System.out.println(this.detectorEvent.toString());
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
    
    public int getSoftwareTriggerScore(DetectorParticle p,EBCCDBConstants ccdb) {

        final double npheCut = ccdb.getDouble(EBCCDBEnum.HTCC_NPHE_CUT);
        final double sfNSigma = SamplingFractions.getNSigma(11,p,ccdb);

        int score = 0;
        if(p.getNphe(DetectorType.HTCC) > npheCut){
            score += 10;
        }
        if(abs(sfNSigma) < EBConstants.ECAL_SF_NSIGMA &&
            p.getEnergy(DetectorType.ECAL,1) > EBConstants.PCAL_ELEC_MINENERGY) {
            score += 100;
        }
        if(p.hasHit(DetectorType.FTOF,1)==true || p.hasHit(DetectorType.FTOF,2)==true){
            score += 1000;
        }

        return score;
    }

    public boolean assignSoftwareTrigger(DetectorEvent event,EBCCDBConstants ccdb) {

        final int npart = event.getParticles().size();
        boolean foundTriggerParticle = false;
        
        // search for candidates:
        int maxScore = 0;
        for (int i=0; i<npart; i++) {
            DetectorParticle p = event.getParticle(i);
            final int score = getSoftwareTriggerScore(p,ccdb);
            if(score >= this.score_requirement) {
                if (this.charge==p.getCharge()) {
                    p.setPid(this.id);
                    p.setScore(score);
                    foundTriggerParticle = true;
                    if (score > maxScore) maxScore=score;
                }
            }
        }

        // of those candidates with max score, choose the one with max momentum:
        if (foundTriggerParticle) {
            int    index = -1;
            double maxMom = 0.0;
            for(int i = 0; i < npart; i++){
                if(event.getParticle(i).getPid()==this.id ){
                    if(event.getParticle(i).getScore() >= maxScore &&
                       event.getParticle(i).vector().mag() > maxMom){
                        maxMom = event.getParticle(i).vector().mag();
                        index = i;
                            }
                }
            }
            // move the trigger particle to the first row:
            if (index>=0) event.moveUp(index);
        }

        return foundTriggerParticle;
    }

}

class ElectronTriggerOption extends TriggerOptions {

    @Override
    public void initID() {
        this.setID(11);
        this.setScoreRequirement(1110);
        this.setCharge(-1);
    }

}    

class PositronTriggerOption extends TriggerOptions {

    @Override
    public void initID() {
        this.setID(-11);
        this.setScoreRequirement(1110);
        this.setCharge(1);
    }

}  

class NegPionTriggerOption extends TriggerOptions {

    @Override
    public void initID() {
        this.setID(-211);
        this.setScoreRequirement(1000);
        this.setCharge(-1);
    }

}    

class PosPionTriggerOption extends TriggerOptions {

    @Override
    public void initID() {
        this.setID(211);
        this.setScoreRequirement(1000);
        this.setCharge(1);
    }

}  
