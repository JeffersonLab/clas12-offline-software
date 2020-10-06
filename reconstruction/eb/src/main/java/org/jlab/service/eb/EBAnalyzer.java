package org.jlab.service.eb;

import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.detector.base.DetectorType;

import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.pdg.PDGDatabase;

import org.jlab.rec.eb.EBConstants;
import org.jlab.rec.eb.EBCCDBConstants;
import org.jlab.rec.eb.EBCCDBEnum;
import org.jlab.rec.eb.EBUtil;
import org.jlab.rec.eb.SamplingFractions;
import org.jlab.rec.eb.EBRadioFrequency;

/**
 * @author gavalian
 * @author jnewton
 * @author devita
 * @author baltzell
 */
public class EBAnalyzer {

    private final EBCCDBConstants ccdb;
    private final EBRadioFrequency ebrf;

    static final int[]  PID_POSITIVE = new int[]{-11,  211, 321, 2212, 45};
    static final int[]  PID_NEGATIVE = new int[]{ 11, -211,-321,-2212};
    static final int[]  PID_NEUTRAL = new int[]{22,2112};

    Map <DetectorType,List<Integer>> chargedBetaDetectors;
    Map <DetectorType,List<Integer>> neutralBetaDetectors;
    
    public EBAnalyzer(EBCCDBConstants ccdb,EBRadioFrequency ebrf) {
        this.ccdb=ccdb;
        this.ebrf=ebrf;

        // setup prioritization on timing detectors:
        chargedBetaDetectors=new LinkedHashMap<>();
        chargedBetaDetectors.put(DetectorType.FTOF,Arrays.asList(2,1,3));
        chargedBetaDetectors.put(DetectorType.CTOF,Arrays.asList(1));
        chargedBetaDetectors.put(DetectorType.ECAL,Arrays.asList(1,4,7));
        //chargedBetaDetectors.put(DetectorType.HTCC,Arrays.asList(0));
        //chargedBetaDetectors.put(DetectorType.CND,Arrays.asList(0));
        //chargedBetaDetectors.put(DetectorType.FTCAL,Arrays.asList(0));
        neutralBetaDetectors=new LinkedHashMap<>();
        neutralBetaDetectors.put(DetectorType.ECAL,Arrays.asList(1,4,7));
        neutralBetaDetectors.put(DetectorType.CND,Arrays.asList(0));
        neutralBetaDetectors.put(DetectorType.CTOF,Arrays.asList(0));
        neutralBetaDetectors.put(DetectorType.FTCAL,Arrays.asList(0));
        neutralBetaDetectors.put(DetectorType.BAND,Arrays.asList(0));
        //neutralBetaDetectors.put(DetectorType.FTOF,Arrays.asList(2,1,3));
    }

    
    /**
     *
     * Determine event start time from FT electron and reassign timing-based
     * particle identification accordingly.
     * 
     * Choice of which FT electron to use is that with the smallest vertex
     * time difference between FT and any combination of FD charged particle
     * and pid (mass) hypothesis.
     *
     * WARNING:  Here we hijack the event's particles, overwriting their pids,
     * rather than making copies, since particle ordering is critical for
     * shadow banks to work as intended, so they should have been written to
     * REC::Particle bank already.
     *
     * @param event
     */
    public void processEventFT(DetectorEvent event) {

        if (event.getParticles().size() <= 0) return;

        // An FD electron was already used to get start time, abort:
        //if (event.getTriggerParticle()!=null &&
        //    event.getTriggerParticle().getPid()==11 &&
        //    event.getTriggerParticle().getStatus().isForward()) return;

        // Match FT against these hypotheses in FD:
        final int[] hypotheses = new int[]{-11,11,-211,211,-321,321,2212};
    
        // particle candidates for FT-FD time-matching:
        List<DetectorParticle> electronFT = new ArrayList<>();
        List<DetectorParticle> chargedFD = new ArrayList<>();

        // corresponding response candidates, to avoid excessive searches during combinatorics:
        List<DetectorResponse> chargedFTOF = new ArrayList<>();

        // load the candidates:
        for (DetectorParticle p : event.getParticles()) {
            if (p.getStatus().isTagger() && p.getPid()==11) {
                electronFT.add(p);
            }
            else if (p.getStatus().isForward() && p.getCharge()!=0) {
                DetectorResponse ftof1a = p.getHit(DetectorType.FTOF,2);
                if (ftof1a==null) continue;
                chargedFD.add(p);
                chargedFTOF.add(ftof1a);
            }
        }

        // no good candidates, abort:
        if (electronFT.size()>0 && chargedFD.size()>0) {
        
            // the index of the FT particle with the best FT-FD timing-match:
            int iMinTimeDiffFT = -1;

            // anything relevant must be better than half-bucket:
            double minTimeDiff = this.ccdb.getDouble(EBCCDBEnum.RF_BUCKET_LENGTH)/2;

            // loop over FT electron candidates:
            for (int itag=0; itag<electronFT.size(); itag++) {

                // calculate RF-corrected FT start time:
                final double startTimeFT = ebrf.getStartTime(electronFT.get(itag),DetectorType.FTCAL,-1);
            
                // loop over FD charged particles:
                for (int ifd=0; ifd<chargedFD.size(); ifd++) {

                    final DetectorResponse ftof1a = chargedFTOF.get(ifd);
                    final double pFD = chargedFD.get(ifd).vector().mag();
                    final double pathFD = chargedFD.get(ifd).getPathLength(ftof1a.getPosition());
                    
                    for (int pid : hypotheses) {

                        // ignore if measured charge doesn't match hypothesis charge:
                        if (PDGDatabase.getParticleById(pid).charge() !=
                                chargedFD.get(ifd).getCharge()) {
                            continue;
                        }
                    
                        // calculate beta and vertex time based on measured
                        // momentum and this pid hypothesis:
                        final double beta = pFD /
                                Math.sqrt(pFD*pFD + Math.pow(PDGDatabase.getParticleMass(pid),2));
                        final double vtxTimeFD = ftof1a.getTime() -
                                pathFD/PhysicsConstants.speedOfLight()/beta;
                        
                        // check for a new best FT-FD timing match:
                        if (Math.abs(vtxTimeFD-startTimeFT) < Math.abs(minTimeDiff)) {
                            minTimeDiff = vtxTimeFD-startTimeFT;
                            iMinTimeDiffFT = itag;
                        }
                    }
                }
            }
       
            if (iMinTimeDiffFT>=0) {

                // reassign trigger particle:
                for (DetectorParticle p : event.getParticles()) {
                    p.setTriggerParticle(false);
                }
                electronFT.get(iMinTimeDiffFT).setTriggerParticle(true);

                // set start time:
                final double startTime = ebrf.getStartTime(electronFT.get(iMinTimeDiffFT),DetectorType.FTCAL,-1);
                event.getEventHeader().setStartTimeFT(startTime);
                assignParticleStartTimes(event,DetectorType.FTCAL,-1);

                // recalculate betas, pids, etc:
                this.assignBetas(event,true);
                this.assignPids(event,true);
            }
        }
        this.assignNeutralMomenta(event);
    }
    
    /**
     *
     * Determine event start time from trigger particle, assign particles'
     * betas and pids and neutrals' momenta
     * 
     * @param event
     */
    public void processEvent(DetectorEvent event) {

        // abort, rely on default init of DetectorEvent:
        if (event.getParticles().size() <= 0) return;

        DetectorParticle trigger = event.getTriggerParticle();
        if (trigger!=null) {

            // priority is to identify a trigger time:
            boolean foundTriggerTime=false;
            double startTime=-1000;

            // electron/positron/pion is trigger particle:
            if (trigger.getPid()==11 || trigger.getPid()==-11 ||
                    trigger.getPid()==211 || trigger.getPid()==-211) {

                trigger.setBeta(trigger.getTheoryBeta(trigger.getPid()));
                trigger.setMass(PDGDatabase.getParticleById(trigger.getPid()).mass());

                // prefer FTOF Panel 1B:
                if (trigger.hasHit(DetectorType.FTOF, 2)==true){
                    startTime = ebrf.getStartTime(trigger,DetectorType.FTOF,2);
                    assignParticleStartTimes(event,DetectorType.FTOF,2);
                    foundTriggerTime = true;
                }

                // else use FTOF Panel 1A:
                else if (trigger.hasHit(DetectorType.FTOF, 1)==true){
                    startTime = ebrf.getStartTime(trigger,DetectorType.FTOF,1);
                    assignParticleStartTimes(event,DetectorType.FTOF,1);
                    foundTriggerTime = true;
                }
            }

            // neutral is trigger particle:
            else if (trigger.getPid()==0 || trigger.getPid()==22) {
                trigger.setBeta(1.0);
                trigger.setMass(0.0);
                // TODO:  implement full neutral trigger start time?
                //foundTriggerTime=true;
            }

            // we found event start time, so set it and do pid:
            if (foundTriggerTime) {
                event.getEventHeader().setStartTime(startTime);
                this.assignBetas(event,false);
                this.assignPids(event,false);
            }
        }
        this.assignNeutralMomenta(event);
    }

    /**
     * Assign per-particle start times, based on the trigger particle's timing
     * and momentum, but with vz-correction per-particle.
     * 
     * @param event
     * @param type
     * @param layer 
     */
    public void assignParticleStartTimes(DetectorEvent event,DetectorType type,int layer) {
        DetectorParticle trig = event.getTriggerParticle();
        for (int ii=0; ii<event.getParticles().size(); ii++) {
            if (event.getParticles().get(ii).getCharge()!=0) {
                event.getParticles().get(ii).setStartTime(
                        ebrf.getStartTime(trig, type, layer, 
                        event.getParticles().get(ii).vertex().z()));
            }
            
        }
        
    }

    public void assignNeutralMomenta(DetectorEvent de) {
        final int np = de.getParticles().size();
        for (int ii=0; ii<np; ii++) {
            if (de.getParticle(ii).getCharge() != 0) continue;
            DetectorParticle p = de.getParticle(ii);
            switch (abs(p.getPid())) {
                case 2112:
                    // neutron momentum defined by measured beta if valid:
                    final double beta = p.getBeta();
                    if (beta>0 && beta<1) {
                        final double mass = PDGDatabase.getParticleById(p.getPid()).mass();
                        final double psquared = Math.pow(mass*beta,2) / (1-beta*beta);
                        p.vector().setMag( Math.sqrt(psquared) );
                    }
                    else {
                        p.vector().setMag(0.0);
                    }
                    break;
                case 22:
                    if (p.hasHit(DetectorType.ECAL)) {
                        // ECAL photon momentum defined by measured energy:
                        p.vector().setMag(p.getEnergy(DetectorType.ECAL) /
                            SamplingFractions.getMean(22,p,ccdb));
                    }
                    else if (p.hasHit(DetectorType.CND) || p.hasHit(DetectorType.CTOF) || p.hasHit(DetectorType.BAND)) {
                        // Central and BAND have no handle on photon energy, so we set momentum to zero,
                        // and let user get direction from REC::Scintillator.x/y/z.
                        p.vector().setMag(0.0);
                    }
                    break;
                case 0:
                    // neutrals without a good pid get zero momentum:
                    p.vector().setMag(0.0);
                    break;
                default:
                    throw new RuntimeException("assignNeutralMomentum:  not ready for pid="+p.getPid());
            }
        }
    }

    public void assignBetas(DetectorEvent event,final boolean useStartTimeFromFT){

        final double startTime = useStartTimeFromFT ?
            event.getEventHeader().getStartTimeFT() :
            event.getEventHeader().getStartTime();

        for (DetectorParticle p : event.getParticles()) {
            double beta = -99;
            final double thisStartTime = p.getCharge()==0 ? startTime : p.getStartTime();

            if (p.isTriggerParticle()) {
                final double mass = PDGDatabase.getParticleById(p.getPid()).mass();
                final double mom  = p.vector().mag();
                beta = mom / Math.sqrt(mass*mass+mom*mom);
            }
            else {
                if (p.getCharge()==0) {
                    for (Entry<DetectorType,List<Integer>> bd : neutralBetaDetectors.entrySet()) {
                        if (p.hasHit(bd.getKey())) {
                            beta = p.getNeutralBeta(bd.getKey(),bd.getValue(),startTime);
                            break;
                        }
                    }
                }
                else {
                    boolean found=false;
                    for (Entry<DetectorType,List<Integer>> bd : chargedBetaDetectors.entrySet()) {
                        for (Integer layer : bd.getValue()) {
                            if (p.hasHit(bd.getKey(),layer)) {
                                beta = p.getBeta(bd.getKey(),layer, p.getStartTime());
                                found=true;
                                break;
                            }
                        }
                        if (found) break;
                    }
                }
            }
            p.setBeta(beta);
        }
    }
    public void assignPids(DetectorEvent event,final boolean useStartTimeFromFT) {

        PIDHypothesis pidHyp = new PIDHypothesis();
        pidHyp.setEvent(event);
        pidHyp.setUseStartTimeFromFT(useStartTimeFromFT);

        for (DetectorParticle p : event.getParticles()) {
            if (p.isTriggerParticle()) {
                pidHyp.finalizePID(p,p.getPid());
            }
            else {
                if (p.getCharge()>0){
                    for(int b = 0; b < EBAnalyzer.PID_POSITIVE.length; b++) {
                        pidHyp.PIDMatch(p, EBAnalyzer.PID_POSITIVE[b]); 
                    }
                } 
                else if (p.getCharge()<0) {
                    for(int b = 0; b < EBAnalyzer.PID_NEGATIVE.length; b++) {
                        pidHyp.PIDMatch(p, EBAnalyzer.PID_NEGATIVE[b]);
                    }
                }
                else {
                    for(int b = 0; b < EBAnalyzer.PID_NEUTRAL.length; b++) {
                        pidHyp.PIDMatch(p, EBAnalyzer.PID_NEUTRAL[b]);
                    }
                }
            }
        }
    }



    public class PIDHypothesis {

        private int theoryPID = -1;
        private double PIDquality = 0.0;
        private DetectorEvent event;
        private boolean useStartTimeFromFT = false;

        public PIDHypothesis() {}

        public void setEvent(DetectorEvent e) {event = e;}

        public void setUseStartTimeFromFT(boolean b) {useStartTimeFromFT = b;}

        public double getStartTime() {
            return useStartTimeFromFT ?
                event.getEventHeader().getStartTimeFT() :
                event.getEventHeader().getStartTime();
        }

        public void PIDMatch(DetectorParticle p, int pid) {

            final int pidFromTiming = bestPidFromTiming(p);
            
            final boolean pidFromTimingCheck = pid==pidFromTiming && p.getTheoryBeta(pid)>0;
           
            final boolean isElectron = EBUtil.isSimpleElectron(p,ccdb);
            
            final boolean htccSignalCheck = p.getNphe(DetectorType.HTCC)>ccdb.getDouble(EBCCDBEnum.HTCC_NPHE_CUT);
            final boolean ltccSignalCheck = p.getNphe(DetectorType.LTCC)>ccdb.getDouble(EBCCDBEnum.LTCC_NPHE_CUT);
            
            final boolean htccPionThreshold = p.vector().mag()>EBConstants.HTCC_PION_THRESHOLD;
            final boolean ltccPionThreshold = p.vector().mag()>EBConstants.LTCC_PION_THRESHOLD;
            final boolean ltccKaonThreshold = p.vector().mag()>EBConstants.LTCC_KAON_THRESHOLD;

            switch(abs(pid)) {
                case 11:
                    // require htcc nphe and ecal sampling fraction for electrons: 
                    if(isElectron) {
                        this.finalizePID(p, pid);
                    }
                    break;

                case 211:
                    if (pidFromTimingCheck && !isElectron) {
                        // pion is best timing
                        this.finalizePID(p,pid);
                    }
                    else if (!isElectron && htccSignalCheck && htccPionThreshold) {
                        // pion is not the best timing, but htcc signal and above pion threshold:
                        this.finalizePID(p,pid);
                    }
                    break;
                case 321:
                    if (pidFromTimingCheck && !isElectron) {
                        // kaon is best timing
                        if (ltccSignalCheck && ltccPionThreshold) {
                            // let ltcc veto back to pion:
                            this.finalizePID(p,(pid>0?211:-211));
                        }
                        else {
                            this.finalizePID(p,pid);
                        }
                    }
                    break;
                case 2212:
                    if (pidFromTimingCheck && !isElectron) {
                        // proton is best timing
                        if (ltccSignalCheck && ltccPionThreshold) {
                            // let ltcc veto back to pion:
                            this.finalizePID(p,(pid>0?211:-211));
                        }
                        else {
                            this.finalizePID(p,pid);
                        }
                    }
                    break;
                case 45:
                    if (pidFromTimingCheck && !isElectron) {
                        this.finalizePID(p,pid);
                    }
                    break;

                case 2112:
                    if (pidFromTimingCheck) {
                        this.finalizePID(p,pid);
                    }
                    break;
                case 22:
                    if (pidFromTimingCheck) {
                        this.finalizePID(p,pid);
                    }
                    break;
            }

        }

        /**
         * Get the hadron hypotheses with the closest vertex time.
         * @param p the particle for which to find best pid
         * @return the best pid value
         */
        public int bestPidFromTiming(DetectorParticle p) {
            int bestPid=0;
            if (p.getCharge() == 0) {
                if (p.hasHit(DetectorType.ECAL)) {
                    bestPid = p.getBeta()<ccdb.getDouble(EBCCDBEnum.NEUTRON_maxBeta) ? 2112 : 22;
                }
                else if (p.hasHit(DetectorType.CND) || p.hasHit(DetectorType.CTOF)) {
                    if (!EBUtil.centralNeutralVeto(p)) {
                        bestPid = p.getBeta()<ccdb.getDouble(EBCCDBEnum.CND_NEUTRON_maxBeta) ? 2112 : 22;
                    }
                }
                else if (p.hasHit(DetectorType.BAND)) {
                    bestPid = p.getBeta() < 0.9 ? 2112 : 0;
                }
            }
            else {
                int[] hypotheses;
                if      (p.getCharge()>0) hypotheses=PID_POSITIVE;
                else                      hypotheses=PID_NEGATIVE;
                double minTimeDiff=Double.MAX_VALUE;
                for (int ii=0; ii<hypotheses.length; ii++) {
                    if (abs(hypotheses[ii])==11) continue;
                    double dt=Double.MAX_VALUE;
                    boolean found=false;
                    for (Entry<DetectorType,List<Integer>> bd : chargedBetaDetectors.entrySet()) {
                        for (Integer layer : bd.getValue()) {
                            if (p.hasHit(bd.getKey(),layer)==true) {
                                dt = p.getVertexTime(bd.getKey(),layer,hypotheses[ii])-p.getStartTime();
                                found=true;
                                break;
                            }
                        } 
                        if (found) break;
                    }
                    if ( abs(dt) < minTimeDiff ) {
                        minTimeDiff=abs(dt);
                        bestPid=hypotheses[ii];
                    }
                }
            }
            return bestPid;
        }

        /**
         * Get a basic pid quality factor.
         * @param p the particle for which to calculate a pid quality factor
         * @param pid the pid hypothesis
         * @return the pid quality factor
         */
        public double PIDQuality(DetectorParticle p, int pid) {
            double q=DetectorParticle.DEFAULTQUALITY;

            // FT can't really to a pid quality:
            if (p.getStatus().isTagger()) {
                q = 0;
            }

            // electron/positron:
            else if (abs(pid)==11) {
                q = SamplingFractions.getNSigma(pid,p,ccdb);
            }

            // based on timing:
            else if (p.getCharge()!=0) {
                double sigma = -1;
                double delta_t = 99999;

                boolean found=false;
                for (Entry<DetectorType,List<Integer>> bd : chargedBetaDetectors.entrySet()) {
                    for (Integer layer : bd.getValue()) {
                        if (p.hasHit(bd.getKey(),layer)==true) {
                            sigma = EBUtil.getDetTimingResolution(p.getHit(bd.getKey(),layer),ccdb);
                            if (sigma>0) delta_t = p.getVertexTime(bd.getKey(),layer, pid)-p.getStartTime();
                            found=true;
                            break;
                        }
                        if (found) break;
                    }
                    if (found) break;
                }
                if (sigma>0) q = delta_t / sigma;
            }

            // neutrals:
            else {
            }

            return q;
        }


        /**
         * Set particle's pid and quality factor.
         * @param p the particle for which to calculate a pid quality factor
         * @param pid the pid hypothesis to finalize
         */
        public void finalizePID(DetectorParticle p, int pid) {
            p.setPid(pid);
            theoryPID = pid;
            PIDquality = this.PIDQuality(p, pid);
            p.setPidQuality(PIDquality);

        }
        
    }
}



