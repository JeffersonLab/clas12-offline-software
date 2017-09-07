package org.jlab.service.eb;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import java.util.HashMap;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.detector.base.DetectorType;
import org.jlab.rec.eb.EBConstants;
import org.jlab.clas.pdg.PhysicsConstants;

/**
 * @author gavalian
 * @author jnewton
 * @author devita
 * @author baltzell
 */
public class EBAnalyzer {

    private int[]  pidPositive = new int[]{-11,  211, 321, 2212};
    private int[]  pidNegative = new int[]{ 11, -211,-321,-2212};

    public EBAnalyzer(){}

    public void processEvent(DetectorEvent event) {

        // abort, rely on default init of DetectorEvent:
        if (event.getParticles().size() <= 0) return;

        // first particle is designated as the "trigger" particle:
        // TODO:  reference the code that actually does the sorting
        DetectorParticle trigger = event.getParticle(0);

        // priority is to identify a trigger time:
        boolean foundTriggerTime=false;
        double startTime=-1000;

        // electron/positron is trigger particle:
        if (trigger.getPid()==11 || trigger.getPid()==-11) {

            trigger.setBeta(1.0);
            trigger.setMass(PhysicsConstants.massElectron());

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
                double tof = path/PhysicsConstants.speedOfLight();
                double start_time = time - tof;
                double deltatr = - start_time + event.getEventHeader().getRfTime()
                    + (EBConstants.RF_LARGE_INTEGER+0.5)*EBConstants.RF_BUCKET_LENGTH + EBConstants.RF_OFFSET;
                //double deltatr = - start_time + event.getEventHeader().getRfTime() /* - (trigger.vertex().z() 
                //                                                                      - (EBConstants.TARGET_POSITION))/(PhysicsConstants.speedOfLight())*/
                //    + (EBConstants.RF_LARGE_INTEGER+0.5)*EBConstants.RF_BUCKET_LENGTH + EBConstants.RF_OFFSET;
                double rfcorr = deltatr%EBConstants.RF_BUCKET_LENGTH - EBConstants.RF_BUCKET_LENGTH/2;//RF correction term
                startTime = start_time + rfcorr;
            }
        }

        // neutral is trigger particle:
        else if (trigger.getPid()==0 || trigger.getPid()==22) {
            trigger.setBeta(1.0);
            trigger.setMass(0.0);
            // TODO:  implement full neutral trigger start time
            //startTime = EBConstants.GEMC_STARTTIME;
            //foundTriggerTime=true;
        }

        // trigger particle is unkown:
        else {
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
            if(p.hasHit(DetectorType.CTOF, 0)==true){
                beta = p.getBeta(DetectorType.CTOF ,start_time);
                mass = p.getMass2(DetectorType.CTOF,start_time);
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

            double beta = p.getTheoryBeta(pid);
            double vertex_index = optimalVertexTime(p);

            int pidCandidate = pid;
            boolean vertexCheck = (abs(pid)==211 && vertex_index==1 && p.getBeta()>0.0) || 
                (abs(pid)==2212 && vertex_index==0 && p.getBeta()>0.0) || 
                (abs(pid)==321 && vertex_index==2 && p.getBeta()>0.0);
            boolean sfCheck = p.getEnergyFraction(DetectorType.EC)>org.jlab.rec.eb.EBConstants.ECAL_SAMPLINGFRACTION_CUT;
            boolean htccSignalCheck = p.getNphe(DetectorType.HTCC)>org.jlab.rec.eb.EBConstants.HTCC_NPHE_CUT;
            boolean ltccSignalCheck = p.getNphe(DetectorType.LTCC)>org.jlab.rec.eb.EBConstants.LTCC_NPHE_CUT;
            boolean htccPionThreshold = p.vector().mag()>org.jlab.rec.eb.EBConstants.HTCC_PION_THRESHOLD;
            boolean ltccPionThreshold = p.vector().mag()<org.jlab.rec.eb.EBConstants.LTCC_UPPER_PION_THRESHOLD 
                && p.vector().mag()>org.jlab.rec.eb.EBConstants.LTCC_LOWER_PION_THRESHOLD;

            switch(abs(pid)) {
                case 11:
                    if(htccSignalCheck==true && sfCheck==true){
                        this.finalizePID(p, pid);
                        break;
                    }
                    if(htccSignalCheck==true && sfCheck==true){
                        this.finalizePID(p, pid);
                        break;
                    }
                case 211:
                    if(vertexCheck==true && htccSignalCheck==true && sfCheck==false 
                            && htccPionThreshold==true) {
                        this.finalizePID(p, pid);
                        break;
                            }
                    if(vertexCheck==false && htccSignalCheck==true && sfCheck==false 
                            && htccPionThreshold==true) {
                        this.finalizePID(p, pid);
                        break;
                            } 
                    if(vertexCheck==true && ltccSignalCheck==true && sfCheck==false 
                            && ltccPionThreshold==true) {
                        this.finalizePID(p, pid);
                        break;
                            }
                    if(vertexCheck==false && ltccSignalCheck==true && sfCheck==false 
                            && ltccPionThreshold==true) {
                        this.finalizePID(p, pid);
                        break;
                            }  
                case 321:
                    if(vertexCheck==true && sfCheck==false && htccSignalCheck==false){
                        this.finalizePID(p, pid);
                        break;
                    }
                case 2212:
                    if(vertexCheck==true && sfCheck==false && htccSignalCheck==false){
                        this.finalizePID(p, pid);
                        break;
                    }
            }

        }

        public int optimalVertexTime(DetectorParticle p) {
            int vertex_index = 0;
            HashMap<Integer,Double> vertexDiffs = new HashMap<Integer,Double>(); 
            double vertex_time_hypothesis = 0.0;
            double event_start_time = event.getEventHeader().getStartTime();

            if(p.hasHit(DetectorType.FTOF,1)==true) {
                vertexDiffs.put(0,abs(p.getVertexTime(DetectorType.FTOF, 1, 2212)-event_start_time));
                vertexDiffs.put(1,abs(p.getVertexTime(DetectorType.FTOF, 1, 211)-event_start_time));
                vertexDiffs.put(2,abs(p.getVertexTime(DetectorType.FTOF, 1, 321)-event_start_time));
            }

            if(p.hasHit(DetectorType.FTOF,2)==true) {
                vertexDiffs.put(0,abs(p.getVertexTime(DetectorType.FTOF, 2, 2212)-event_start_time));
                vertexDiffs.put(1,abs(p.getVertexTime(DetectorType.FTOF, 2, 211)-event_start_time));
                vertexDiffs.put(2,abs(p.getVertexTime(DetectorType.FTOF, 2, 321)-event_start_time));
            }

            if(vertexDiffs.size()>0) {
                double min = vertexDiffs.get(0);

                for (int i = 0; i <= 2; i++) {
                    if (vertexDiffs.get(i) < min) {
                        min = vertexDiffs.get(i);
                        vertex_index = i;
                    }
                }
            }
            return vertex_index;
        }


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



