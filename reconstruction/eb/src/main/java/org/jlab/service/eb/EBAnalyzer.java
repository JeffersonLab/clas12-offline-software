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

import org.jlab.rec.eb.EBCCDBConstants;
import org.jlab.rec.eb.EBCCDBEnum;

/**
 * @author gavalian
 * @author jnewton
 * @author devita
 * @author baltzell
 */
public class EBAnalyzer {

    private int[]  pidPositive = new int[]{-11,  211, 321, 2212, 45};
    private int[]  pidNegative = new int[]{ 11, -211,-321,-2212};

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

//            trigger.setBeta(1.0);
//            trigger.setMass(PhysicsConstants.massElectron());

            double time = 0.0;
            double path = 0.0;

            // TODO:  get these hardcoded FTOF "layer" 1/2/3 constants out of here.
            // Should be from DetectorType instead, and similarly for for ECAL/PCAL's 1/4/7 (e.g. in EBCCDB)
            
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
                final double start_time = time - tof;
                final double vzCorr = (tgpos - trigger.vertex().z()) / PhysicsConstants.speedOfLight();
                final double deltatr = - start_time + event.getEventHeader().getRfTime() + vzCorr +
                    + (EBConstants.RF_LARGE_INTEGER+0.5)*rfBucketLength + rfOffset;
                
                //double deltatr = - start_time + event.getEventHeader().getRfTime() /* - (trigger.vertex().z() 
                //                                                                      - (EBConstants.TARGET_POSITION))/(PhysicsConstants.speedOfLight())*/
                //    + (EBConstants.RF_LARGE_INTEGER+0.5)*EBConstants.RF_BUCKET_LENGTH + EBConstants.RF_OFFSET;
                
                final double rfcorr = deltatr % rfBucketLength - rfBucketLength/2;//RF correction term
                
                startTime = start_time + rfcorr;
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
            this.assignMasses(event);
            this.assignPids(event);
        }

    }


    public void assignMasses(DetectorEvent event){

        int np = event.getParticles().size();
        for(int i = 1; i < np; i++) {
            DetectorParticle p = event.getParticle(i);
            double start_time  = event.getEventHeader().getStartTime();
            double beta = 0.0;
            double mass = 0.0;

            if(p.hasHit(DetectorType.FTOF, 1)==true){
                beta = p.getBeta(DetectorType.FTOF,1, start_time);
                mass = p.getMass2(DetectorType.FTOF,1, start_time);
                p.setBeta(beta);
            }
            if(p.hasHit(DetectorType.FTOF, 2)==true){
                beta = p.getBeta(DetectorType.FTOF, 2,start_time);
                mass = p.getMass2(DetectorType.FTOF, 2,start_time);
                p.setBeta(beta);
            }
            if(p.hasHit(DetectorType.CTOF)==true){
                beta = p.getBeta(DetectorType.CTOF ,start_time);
                mass = p.getMass2(DetectorType.CTOF,start_time);
                //System.out.println("CTOF Beta" + beta);
                p.setBeta(beta);
            }
        }
    }

    public void assignPids(DetectorEvent event) {
        int np = event.getParticles().size();
        PIDHypothesis pidHyp = new PIDHypothesis();
        for(int i = 1; i < np; i++){
            DetectorParticle p = event.getParticle(i);
            if(p.getCharge()==0) break;
            if(p.getCharge()>0){
                for(int b = 0; b < this.pidPositive.length; b++){
                    pidHyp.setEvent(event);
                    pidHyp.PIDMatch(p, this.pidPositive[b]); 
                    //pidHyp.PIDQuality(p,this.pidPositive[b],event);
                }
                //p.setPid(pidHyp.get(0).getPid());
            } else {
                for(int b = 0; b < this.pidNegative.length; b++){
                    pidHyp.setEvent(event);
                    pidHyp.PIDMatch(p, this.pidNegative[b]);
                    //pidHyp.PIDQuality(p, this.pidNegative[b],event);
                }
                //Collections.sort(pidHyp); 
                //p.setPid(pidHyp.get(0).getPid());
            }
        }
    }



    public class PIDHypothesis {

        private int theoryPID = -1;
        private double PIDquality = 0.0;
        private DetectorEvent event;

        public PIDHypothesis() {

        }

        public void setEvent(DetectorEvent e) {event = e;}

        public void PIDMatch(DetectorParticle p, int pid) {

            final int pidFromTiming = bestPidFromTiming(p);
            final boolean pidCheck = pid==pidFromTiming && p.getTheoryBeta(pid)>0;

            Double ener = p.getEnergy(DetectorType.ECAL);
            Double[] t = EBCCDBConstants.getArray(EBCCDBEnum.ELEC_SF);
            Double[] s = EBCCDBConstants.getArray(EBCCDBEnum.ELEC_SFS);
            double sfMean = t[0]*(t[1] + t[2]/ener + t[3]*pow(ener,-2));
            double sfSigma = s[0];
            double sf = p.getEnergyFraction(DetectorType.ECAL);
            double sf_upper_limit = sfMean + 5*sfSigma;
            double sf_lower_limit = sfMean - 5*sfSigma;
            
            boolean sfCheck = sf > sf_lower_limit;
            
            boolean htccSignalCheck = p.getNphe(DetectorType.HTCC)>EBConstants.HTCC_NPHE_CUT;
            
            boolean ltccSignalCheck = p.getNphe(DetectorType.LTCC)>EBConstants.LTCC_NPHE_CUT;
            
            boolean htccPionThreshold = p.vector().mag()>EBConstants.HTCC_PION_THRESHOLD;
            
            boolean ltccPionThreshold = p.vector().mag()>EBConstants.LTCC_LOWER_PION_THRESHOLD;

            boolean ltccKaonThreshold = p.vector().mag()>EBConstants.LTCC_UPPER_PION_THRESHOLD;

            switch(abs(pid)) {
                case 11:
                    // require htcc nphe and ecal sampling fraction for electrons: 
                    if(htccSignalCheck && sfCheck) {
                        this.finalizePID(p, pid);
                    }
                    break;

                case 211:
                    if (pidCheck && (!htccSignalCheck || !sfCheck)) {
                        // pion is best timing
                        this.finalizePID(p,pid);
                    }
                    else if (!sfCheck && htccSignalCheck && htccPionThreshold) {
                        // pion is not the best timing, but htcc signal and above pion threshold:
                        this.finalizePID(p,pid);
                    }
                    break;
                case 321:
                    if (pidCheck && (!htccSignalCheck || !sfCheck)) {
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
                    if (pidCheck && (!htccSignalCheck || !sfCheck)) {
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
                    if (pidCheck && (!htccSignalCheck || !sfCheck)) {
                        this.finalizePID(p,pid);
                    }
                    break;
            }

        }

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
                if ( abs(dt) < minTimeDiff ) {
                    minTimeDiff=abs(dt);
                    bestPid=hypotheses[ii];
                }
            }
            return bestPid;
        }
/*
        public int optimalVertexTime(DetectorParticle p) {

            int vertex_index = 0;
            HashMap<Integer,Double> vertexDiffs = new HashMap<Integer,Double>(); 
            double vertex_time_hypothesis = 0.0;
            double event_start_time = event.getEventHeader().getStartTime();

            // prefer FTOF 1B:
            if(p.hasHit(DetectorType.FTOF,2)==true) {
                vertexDiffs.put(0,abs(p.getVertexTime(DetectorType.FTOF, 2, 2212)-event_start_time));
                vertexDiffs.put(1,abs(p.getVertexTime(DetectorType.FTOF, 2, 211)-event_start_time));
                vertexDiffs.put(2,abs(p.getVertexTime(DetectorType.FTOF, 2, 321)-event_start_time));
            }
            // else use FTOF 1A:
            else if(p.hasHit(DetectorType.FTOF,1)==true) {
                vertexDiffs.put(0,abs(p.getVertexTime(DetectorType.FTOF, 1, 2212)-event_start_time));
                vertexDiffs.put(1,abs(p.getVertexTime(DetectorType.FTOF, 1, 211)-event_start_time));
                vertexDiffs.put(2,abs(p.getVertexTime(DetectorType.FTOF, 1, 321)-event_start_time));
            }
            // else use CTOF:
            else if(p.hasHit(DetectorType.CTOF)==true) {
                vertexDiffs.put(0,abs(p.getVertexTime(DetectorType.CTOF, 0, 2212)-event_start_time));
                vertexDiffs.put(1,abs(p.getVertexTime(DetectorType.CTOF, 0, 211)-event_start_time));
                vertexDiffs.put(2,abs(p.getVertexTime(DetectorType.CTOF, 0, 321)-event_start_time));               

            }

            if(vertexDiffs.size()>0) {
                double min = vertexDiffs.get(0);

                for (int i = 0; i < vertexDiffs.size(); i++) {
                    if (vertexDiffs.get(i) < min) {
                        min = vertexDiffs.get(i); 
                        vertex_index = i;
                    }
                }
            }
            return vertex_index;
        }
*/


        public double PIDQuality(DetectorParticle p, int pid, DetectorEvent event) {
            double delta_t = abs(p.getVertexTime(DetectorType.FTOF, 2, pid)-event.getEventHeader().getStartTime());
            double sigma = 0.08;
            double q = pow((delta_t/sigma),2);
            return q;
        }


        public void finalizePID(DetectorParticle p, int pid) {
            p.setPid(pid);
            theoryPID = pid;
            PIDquality = this.PIDQuality(p, pid, event);
            p.setPidQuality(PIDquality);

        }

    }
}



