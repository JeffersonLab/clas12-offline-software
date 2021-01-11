package org.jlab.service.eb;

import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.io.base.DataEvent;

import org.jlab.detector.base.DetectorType;

import org.jlab.clas.detector.DetectorData;
import org.jlab.clas.detector.DetectorHeader;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorParticleTraj;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorTrack;
import org.jlab.clas.detector.TaggerResponse;
import org.jlab.clas.detector.CherenkovResponse;
import org.jlab.clas.physics.Vector3;

import org.jlab.rec.eb.EBCCDBConstants;
import org.jlab.rec.eb.EBCCDBEnum;
import org.jlab.rec.eb.SamplingFractions;

/**
 *
 * @author gavalian
 * @author baltzell
 */
public class EventBuilder {

    public EBCCDBConstants ccdb;
    private final DetectorEvent               detectorEvent = new DetectorEvent();
    private final List<DetectorResponse>  detectorResponses = new ArrayList<>();
    private final List<Map<DetectorType,Integer>> ftIndices = new ArrayList<>();
    private final HashMap<Integer,Integer> pindex_map = new HashMap<>();
    
    private static final int[] TRIGGERLIST = new int[]{11,-11,211,-211,0};

    private boolean usePOCA=false;
    
    public EventBuilder(EBCCDBConstants ccdb){
        this.ccdb=ccdb;
    }

    public void setUsePOCA(boolean val) {
        usePOCA=val;
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
            if (this.usePOCA)
                detectorEvent.addParticle(new DetectorParticle(tracks.get(i)));
            else
                detectorEvent.addParticle(new DetectorParticleTraj(tracks.get(i)));
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
            this.detectorEvent.getParticle(ii).setStatus(
                    ccdb.getDouble(EBCCDBEnum.HTCC_NPHE_CUT),
                    ccdb.getDouble(EBCCDBEnum.LTCC_NPHE_CUT)
                    );
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

// Treat HTCC specially below, leave this here for now.
//                // HTCC:
//                // Find matching cluster for each particle.
//                int index = p.getCherenkovSignal(this.detectorResponses,DetectorType.HTCC);
//                if(index>=0){
//                    p.addResponse(detectorResponses.get(index));
//                    detectorResponses.get(index).setAssociation(n);
//                } 

                // LTCC:
                int index = p.getCherenkovSignal(this.detectorResponses,DetectorType.LTCC);
                if(index>=0){
                    p.addResponse(detectorResponses.get(index));
                    detectorResponses.get(index).setAssociation(n);
                }
            }

            // only match with CTOF/CND if it's a central track:
            else if (p.getTrackDetectorID()==DetectorType.CVT.getDetectorId()) {
                // NOTE:  Should we do 2-d matching in cylindrical coordinates for CD?
                findMatchingHit(n,p,detectorResponses,DetectorType.CTOF,1, ccdb.getDouble(EBCCDBEnum.CTOF_DZ));
                findMatchingHit(n,p,detectorResponses,DetectorType.CND,-1, ccdb.getDouble(EBCCDBEnum.CND_DZ));
            }

        }

        // Special treatment for HTCC, with coarse resolution.
        // Try all combos of HTCC clusters and particle to find best matches.
        while (true) {
            int bestPart=-1;
            int bestRes=-1;
            CherenkovResponse.TrackResidual bestTR=null;
            for (int ires=0; ires<this.detectorResponses.size(); ires++) {
                if (this.detectorResponses.get(ires).getDescriptor().getType() != DetectorType.HTCC) continue;
                if (this.detectorResponses.get(ires).getAssociation()>=0) continue;
                CherenkovResponse che=(CherenkovResponse)this.detectorResponses.get(ires);
                int ipart = che.findClosestTrack(this.detectorEvent.getParticles());
                if (ipart < 0) continue;
                CherenkovResponse.TrackResidual tr = che.getTrackResidual(this.detectorEvent.getParticle(ipart));
                if (bestTR==null || tr.compareTo(bestTR)<0) {
                    bestPart = ipart;
                    bestRes = ires;
                    bestTR = tr;
                }
            }
            if (bestTR==null) break;
            this.detectorEvent.getParticle(bestPart).addResponse(this.detectorResponses.get(bestRes),true);
            this.detectorResponses.get(bestRes).setAssociation(bestPart);
        }
       /* 
        // Special treatment for HTCC, with coarse resolution.
        // Find matching particle for each cluster.
        for (int ii=0; ii<this.detectorResponses.size();ii++) {
            if (this.detectorResponses.get(ii).getAssociation()>=0) continue;
            if (this.detectorResponses.get(ii) instanceof CherenkovResponse) {
                CherenkovResponse che = (CherenkovResponse)this.detectorResponses.get(ii);
                int index = che.findClosestTrack(this.detectorEvent.getParticles());
                if (index>=0) {
                    this.detectorEvent.getParticle(index).addResponse(che);
                    che.setAssociation(index);
                }
            }
        }
        */
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
            responses.get(index).addAssociation(pindex);
            return true;
        }
        return false;
    }
    
    public void forwardTaggerIDMatching() {
        int np = this.detectorEvent.getParticles().size();
        if(this.ftIndices.size()>0 && this.detectorEvent.getParticles().size()>0) {
            int ftParticleCounter = 0;
            for(int n = 0 ; n < np ; n++){
                DetectorParticle p = this.detectorEvent.getParticles().get(n);
                if (p.getTrackDetector() != DetectorType.FTCAL.getDetectorId()) continue;
                final int particle_calID = this.ftIndices.get(ftParticleCounter).get(DetectorType.FTCAL);
                int index = getForwardTaggerMatch(this.detectorResponses, p, DetectorType.FTCAL, particle_calID);
                if(index>=0){
                    p.addResponse(this.detectorResponses.get(index));
                    this.detectorResponses.get(index).setAssociation(n);

                    // make an artificial cross for FTHODO clusters:
                    final double x=this.detectorResponses.get(index).getPosition().x();
                    final double y=this.detectorResponses.get(index).getPosition().y();
                    final double z=this.detectorResponses.get(index).getPosition().z();
                    final double mag = Math.sqrt(x*x+y*y+z*z);
                    p.getTrack().addCross(x,y,z,x/mag,y/mag,z/mag);
                    p.getTrack().setPath(mag);
                }

                final int particle_hodoID = this.ftIndices.get(ftParticleCounter).get(DetectorType.FTHODO);
                index = getForwardTaggerMatch(this.detectorResponses, p, DetectorType.FTHODO, particle_hodoID);
                if(index>=0){
                    p.addResponse(this.detectorResponses.get(index));
                    this.detectorResponses.get(index).setAssociation(n);
                }
                ftParticleCounter++;
            }
        }
    }
    
    public int getForwardTaggerMatch(List<DetectorResponse> hitList, DetectorParticle part, 
            DetectorType type, int ft_id) {
        int bestIndex = -1;
        for(int loop = 0; loop < hitList.size(); loop++) {
            if (type!=DetectorType.FTCAL && type!=DetectorType.FTHODO) {
                continue;
            }
            if (hitList.get(loop).getDescriptor().getType()==type &&
                ((TaggerResponse)hitList.get(loop)).getID()==ft_id){
                bestIndex = loop;
                break;
            }
        }
        return bestIndex;
    }

    public void processForwardTagger(DataEvent de) {
        List<DetectorParticle> ftparticles = DetectorData.readForwardTaggerParticles(de, "FT::particles");       
        List<Map<DetectorType, Integer>> indices = DetectorData.readForwardTaggerIndex(de,"FT::particles");
        List<DetectorResponse> responseFTCAL = TaggerResponse.readHipoEvent(de,"FTCAL::clusters",DetectorType.FTCAL);
        List<DetectorResponse> responseFTHODO = TaggerResponse.readHipoEvent(de,"FTHODO::clusters",DetectorType.FTHODO);
        addParticles(ftparticles);
        addDetectorResponses(responseFTCAL);
        addDetectorResponses(responseFTHODO);
        addFTIndices(indices);
        forwardTaggerIDMatching();
    }

    public void processBAND(List<DetectorResponse> bandHits) {
        Vector3 vtx=new Vector3(0,0,0);
        DetectorParticle trig=this.detectorEvent.getTriggerParticle();
        if (trig!=null) vtx.copy(trig.vertex());
        for (DetectorResponse r : bandHits) {
            if (r.getDescriptor().getType()==DetectorType.BAND) {
                // Non-zero BAND hits are ignored:
                if (r.getStatus()!=0) continue;
                DetectorParticle p=DetectorParticle.createNeutral(r, vtx);
                r.setAssociation(this.detectorEvent.getParticles().size());
                this.detectorEvent.addParticle(p);
           }
        }
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
        
        List<DetectorParticle> particles=new ArrayList<>();
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
        List<DetectorResponse>  responses = new ArrayList<>();
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

            switch (TRIGGERLIST[i]) {
                case 11:
                    ElectronTriggerOption electron = new ElectronTriggerOption();
                    hasTrigger = electron.assignSoftwareTrigger(detectorEvent,ccdb);
                    break;
                case -11:
                    PositronTriggerOption positron = new PositronTriggerOption();
                    hasTrigger= positron.assignSoftwareTrigger(detectorEvent,ccdb);
                    break;
                case -211:
                    NegPionTriggerOption negpion = new NegPionTriggerOption();
                    hasTrigger = negpion.assignSoftwareTrigger(detectorEvent,ccdb);
                    break;
                case 211:
                    PosPionTriggerOption pospion = new PosPionTriggerOption();
                    hasTrigger = pospion.assignSoftwareTrigger(detectorEvent,ccdb);
                    break;
                case 0:
                    hasTrigger = true;
                    break;
                default:
                    break;
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

        int score = 0;

        final double npheCut = ccdb.getDouble(EBCCDBEnum.HTCC_NPHE_CUT);
        if(p.getNphe(DetectorType.HTCC) > npheCut){
            score += 10;
        }
        
        final int sector = p.getSector(DetectorType.ECAL);
        if (sector > 0) {
            final double nSigmaCut = ccdb.getSectorDouble(EBCCDBEnum.ELEC_SF_nsigma,sector);
            final double sfNSigma = SamplingFractions.getNSigma(11,p,ccdb);
            final double minPcalEnergy = ccdb.getSectorDouble(EBCCDBEnum.ELEC_PCAL_min_energy,sector);
            if(abs(sfNSigma) < nSigmaCut &&
                    p.getEnergy(DetectorType.ECAL,1) > minPcalEnergy) {
                score += 100;
            }
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
            // assign and move the trigger particle to the first row:
            if (index>=0) {
                event.getParticle(index).setTriggerParticle(true);
                event.moveUp(index);
            }
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
