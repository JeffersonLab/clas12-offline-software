package org.jlab.service.eb;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import java.util.HashMap;

import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.detector.DetectorResponse;

import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.pdg.PDGDatabase;

import org.jlab.rec.eb.EBConstants;
import org.jlab.rec.eb.EBCCDBConstants;
import org.jlab.rec.eb.EBCCDBEnum;
import org.jlab.rec.eb.EBUtil;

/**
 * @author gavalian
 * @author jnewton
 * @author devita
 * @author baltzell
 */
public class EBAnalyzer {

    private int[]  pidPositive = new int[]{-11,  211, 321, 2212, 45};
    private int[]  pidNegative = new int[]{ 11, -211,-321,-2212};
    private int[]  pidNeutral = new int[]{22,2112};

    public EBAnalyzer(){}

    public void processEvent(DetectorEvent event) {

        // abort, rely on default init of DetectorEvent:
        if (event.getParticles().size() <= 0) return;

        // first particle is designated as the "trigger" particle:
        DetectorParticle trigger = event.getParticle(0);

        // priority is to identify a trigger time:
        boolean foundTriggerTime=false;
        double startTime=-1000;

        // electron/positron/pion is trigger particle:
        if (trigger.getPid()==11 || trigger.getPid()==-11 ||
            trigger.getPid()==211 || trigger.getPid()==-211) {

            trigger.setBeta(trigger.getTheoryBeta(trigger.getPid()));
            trigger.setMass(PDGDatabase.getParticleById(trigger.getPid()).mass());

            double time = 0.0;
            double path = 0.0;

            // prefer FTOF Panel 1B:
            if (trigger.hasHit(DetectorType.FTOF, 2)==true){
                time = trigger.getTime(DetectorType.FTOF, 2);
                path = trigger.getPathLength(DetectorType.FTOF, 2);
                foundTriggerTime = true;
            }

            // else use FTOF Panel 1A:
            else if (trigger.hasHit(DetectorType.FTOF, 1)==true){
                time = trigger.getTime(DetectorType.FTOF, 1);
                path = trigger.getPathLength(DetectorType.FTOF, 1);
                foundTriggerTime = true;
            }
            
            // set startTime based on FTOF:
            if (foundTriggerTime) {
                final double tgpos = EBCCDBConstants.getDouble(EBCCDBEnum.TARGET_POSITION);
                final double rfOffset = EBCCDBConstants.getDouble(EBCCDBEnum.RF_OFFSET);
                final double rfBucketLength = EBCCDBConstants.getDouble(EBCCDBEnum.RF_BUCKET_LENGTH); 

                final double tof = path/PhysicsConstants.speedOfLight()/trigger.getBeta();
                final double vertexTime = time - tof;

                final double vzCorr = (tgpos - trigger.vertex().z()) / PhysicsConstants.speedOfLight();

                final double deltatr = - vertexTime + event.getEventHeader().getRfTime() - vzCorr +
                    + (EBConstants.RF_LARGE_INTEGER+0.5)*rfBucketLength + rfOffset;
                final double rfCorr = deltatr % rfBucketLength - rfBucketLength/2;
                
                startTime = vertexTime + rfCorr;
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
            this.assignBetas(event);
            this.assignPids(event);
        }

    }


    public void assignBetas(DetectorEvent event){

        final double start_time  = event.getEventHeader().getStartTime();
        int np = event.getParticles().size();
        for(int i = 1; i < np; i++) {
            DetectorParticle p = event.getParticle(i);
            double beta = 0.0;
            if(p.hasHit(DetectorType.FTOF, 2)==true){
                beta = p.getBeta(DetectorType.FTOF,2, start_time);
            }
            else if(p.hasHit(DetectorType.FTOF, 1)==true){
                beta = p.getBeta(DetectorType.FTOF, 1,start_time);
            }
            else if(p.hasHit(DetectorType.CTOF)==true){
                beta = p.getBeta(DetectorType.CTOF ,start_time);
            }
            else if(p.hasHit(DetectorType.FTOF, 3)==true){
                beta = p.getBeta(DetectorType.FTOF, 3,start_time);
            }
            p.setBeta(beta);
        }
    }

    public void assignPids(DetectorEvent event) {

        PIDHypothesis pidHyp = new PIDHypothesis();
        pidHyp.setEvent(event);

        // pid for trigger particle is already chosen,
        // just call this to set quality factor:
        pidHyp.finalizePID(event.getParticle(0),event.getParticle(0).getPid());

        // loop skips first (trigger) particle:
        final int np = event.getParticles().size();
        for(int i = 1; i < np; i++){
        
            DetectorParticle p = event.getParticle(i);
            
            if (p.getCharge()>0){
                for(int b = 0; b < this.pidPositive.length; b++) {
                    pidHyp.PIDMatch(p, this.pidPositive[b]); 
                }
            } 
            
            else if (p.getCharge()<0) {
                for(int b = 0; b < this.pidNegative.length; b++) {
                    pidHyp.PIDMatch(p, this.pidNegative[b]);
                }
                //Collections.sort(pidHyp); 
            }
/*            
            else {
                for(int b = 0; b < this.pidNeutral.length; b++) {
                    pidHyp.PIDMatch(p, this.pidNeutral[b]);
                }
            }
*/

        }
    }



    public class PIDHypothesis {

        private int theoryPID = -1;
        private double PIDquality = 0.0;
        private DetectorEvent event;

        public PIDHypothesis() {}

        public void setEvent(DetectorEvent e) {event = e;}

        public void PIDMatch(DetectorParticle p, int pid) {

            final int pidFromTiming = bestPidFromTiming(p);
            
            final boolean pidFromTimingCheck = pid==pidFromTiming && p.getTheoryBeta(pid)>0;
            
            final boolean isElectron = EBUtil.isSimpleElectron(p);
            
            final boolean htccSignalCheck = p.getNphe(DetectorType.HTCC)>EBConstants.HTCC_NPHE_CUT;
            final boolean ltccSignalCheck = p.getNphe(DetectorType.LTCC)>EBConstants.LTCC_NPHE_CUT;
            
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
                    break;
                case 22:
                    break;
            }

        }

        /**
         * Get the hadron hypotheses with the closest vertex time.
         */
        public int bestPidFromTiming(DetectorParticle p) {
            int[] hypotheses;
            if      (p.getCharge()>0) hypotheses=pidPositive;
            else if (p.getCharge()<0) hypotheses=pidNegative;
            else throw new RuntimeException("bestPidFromTiming:  not ready for neutrals");
            final double startTime = event.getEventHeader().getStartTime();
            double minTimeDiff=Double.MAX_VALUE;
            int bestPid=0;
            for (int ii=0; ii<hypotheses.length; ii++) {
                if (abs(hypotheses[ii])==11) continue;
                double dt=Double.MAX_VALUE;
                if (p.hasHit(DetectorType.FTOF,2)==true)
                    dt = p.getVertexTime(DetectorType.FTOF,2,hypotheses[ii]) - startTime;
                else if (p.hasHit(DetectorType.FTOF,1)==true)
                    dt = p.getVertexTime(DetectorType.FTOF,1,hypotheses[ii]) - startTime;
                else if (p.hasHit(DetectorType.CTOF)==true)
                    dt = p.getVertexTime(DetectorType.CTOF,0,hypotheses[ii]) - startTime;
                else if (p.hasHit(DetectorType.FTOF,3)==true)
                    dt = p.getVertexTime(DetectorType.FTOF,3,hypotheses[ii]) - startTime;
                if ( abs(dt) < minTimeDiff ) {
                    minTimeDiff=abs(dt);
                    bestPid=hypotheses[ii];
                }
            }
            return bestPid;
        }


        /**
         * Get a basic pid quality factor.
         */
        public double PIDQuality(DetectorParticle p, int pid, DetectorEvent event) {
            double q=999;

            // electron/positron:
            if (abs(pid)==11) {
                q = pow(EBUtil.getSamplingFractionNSigma(p),2);
            }

            // based on timing:
            else if (p.getCharge()!=0) {
                final double startTime = event.getEventHeader().getStartTime();
                double sigma = -1;
                double delta_t = 99999;
                if (p.hasHit(DetectorType.FTOF,2)==true) {
                    sigma = 0.085; //EBUtil.getTimingResolution(p,DetectorType.FTOF,2);
                    delta_t = abs(p.getVertexTime(DetectorType.FTOF, 2, pid)-startTime);
                }
                else if (p.hasHit(DetectorType.FTOF,1)==true) {
                    sigma = 0.125; //EBUtil.getTimingResolution(p,DetectorType.FTOF,1);
                    delta_t = abs(p.getVertexTime(DetectorType.FTOF, 1, pid)-startTime);
                }
                else if (p.hasHit(DetectorType.CTOF)==true) {
                    sigma = 0.065; //EBUtil.getTimingResolution(p,DetectorType.CTOF,0);
                    delta_t = abs(p.getVertexTime(DetectorType.CTOF, 0, pid)-startTime);
                }
                else if (p.hasHit(DetectorType.FTOF,3)==true) {
                    sigma = 0.152; //EBUtil.getTimingResolution(p,DetectorType.FTOF,3);
                    delta_t = abs(p.getVertexTime(DetectorType.FTOF, 3, pid)-startTime);
                }
                if (sigma>0) q = pow((delta_t/sigma),2);
            }

            // neutrals:
            else {
            }

            return q;
        }


        /**
         * Set particle's pid and quality factor.
         */
        public void finalizePID(DetectorParticle p, int pid) {
            p.setPid(pid);
            theoryPID = pid;
            PIDquality = this.PIDQuality(p, pid, event);
            p.setPidQuality(PIDquality);

        }
        
    }
}



