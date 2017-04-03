/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.detector.base.DetectorType;
import org.jlab.service.pid.*;

/**
 *
 * @author gavalian
 */
public class EBAnalyzer {
    
    private int[]  pidPositive = new int[]{  -11, 211, 321, 2212};
    private int[]  pidNegative = new int[]{ -211,-321,-2212};
    
    public EBAnalyzer(){
        
    }
    
    public void processEvent(DetectorEvent event){
        if(event.getParticles().size()>0){
            DetectorParticle trigger = event.getParticle(0);
            
            //System.out.println(" trigger pid = " + trigger.getPid());
            if(trigger.getPid()==EBConstants.TRIGGER_ID){
                trigger.setBeta(1.0);
                trigger.setMass(0.0005);
                
                double time = 0.0;
                double path = 0.0;
                if(trigger.hasHit(DetectorType.FTOF, 1)==true){
                   //System.out.println("There is a FTOF1A hit!!!");
                    time = trigger.getTime(DetectorType.FTOF, 1);
                    path = trigger.getPathLength(DetectorType.FTOF, 1);
                }
                if(trigger.hasHit(DetectorType.FTOF, 2)==true){
                    //System.out.println("There is a FTOF1B hit!!!");
                    time = trigger.getTime(DetectorType.FTOF, 2);
                    path = trigger.getPathLength(DetectorType.FTOF, 2);
                }

                double t_of_f = path/EBConstants.SPEED_OF_LIGHT;
                double start_time = time - t_of_f;
                double deltatr = start_time - event.getRfTime() - (trigger.vertex().z() 
                        - (EBConstants.TARGET_POSITION))/(EBConstants.SPEED_OF_LIGHT)
                        +EBConstants.RF_LARGE_INTEGER*EBConstants.RF_BUCKET_LENGTH + EBConstants.RF_OFFSET;
                double t_0corr = deltatr%EBConstants.RF_BUCKET_LENGTH - EBConstants.RF_BUCKET_LENGTH/2;//RF correction term
                event.setStartTime(start_time + t_0corr);
                ///System.out.println(start_time);
                //System.out.println(" TIME = " + t_of_f + "  time from TOF = " + time);
                //System.out.println(" PATH = " + path + " " );
                //System.out.println(" SET START TIME = " + start_time + "  ACTUAL TIME = " + event.getStartTime());
                this.assignMasses(event);
                this.assignPids(event);
                //EBPID pid = new EBPID();
                //pid.setEvent(event);
                //pid.PIDAssignment();
                //System.out.println(event.toString());
                
            }
        }
    }
    
    
    public void assignMasses(DetectorEvent event){
        
        int np = event.getParticles().size();
        //System.out.println("======================= ANALYSIS");
        for(int i = 1; i < np; i++) {
            DetectorParticle p = event.getParticle(i);
            double start_time  = event.getStartTime();
            double beta = 0.0;
            double mass = 0.0;
            
            if(p.hasHit(DetectorType.FTOF, 1)==true){
                //System.out.println("1A");
                beta = p.getBeta(DetectorType.FTOF,1, start_time);
                mass = p.getMass2(DetectorType.FTOF,1, start_time);
                p.setBeta(beta);
                
                //System.out.println(String.format("PARTICLE %3d (Layer 1) p = %8.3f beta = %8.3f mass2 = %8.3f", 
                //        i,p.vector().mag(),beta,mass));
            }
            if(p.hasHit(DetectorType.FTOF, 2)==true){
                //System.out.println("1B");
                beta = p.getBeta(DetectorType.FTOF, 2,start_time);
                mass = p.getMass2(DetectorType.FTOF, 2,start_time);
                p.setBeta(beta);
                //System.out.println(String.format("PARTICLE %3d (Layer 2) p = %8.3f beta = %8.3f mass2 = %8.3f", 
                //        i,p.vector().mag(),beta,mass));
            }
            //System.out.println("----------------");

            
           
            //p.getBeta(DetectorType.BST, start_time)
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
               // System.out.println(pidHyp.get(0).getPid());
                //p.setPid(pidHyp.get(0).getPid());
            }
            //System.out.println("  ");
        }
    }
    

public class PIDHypothesis{
    
        private int theoryPID = -1;
        private double PIDquality = 0.0;
        private DetectorEvent event;
        
        
    
        public PIDHypothesis() {
        
            }
        
        public void setEvent(DetectorEvent e) {event = e;}
    
        public void PIDMatch(DetectorParticle p, int pid) {
            
            double beta = p.getTheoryBeta(pid);
            double beta_index = p.MinBetaAssociation(pid); //Associaton for the PID Candidate
                                                               //closest speed to measured track
            
            int pidCandidate = pid;
            boolean betaCheck = (abs(pid)==211 && beta_index==1 && p.getBeta()>0.0) || 
                        (abs(pid)==2212 && beta_index==0 && p.getBeta()>0.0) || 
                        (abs(pid)==321 && beta_index==2 && p.getBeta()>0.0);
            boolean sfCheck = p.getEnergyFraction(DetectorType.EC)>EBConstants.ECAL_SAMPLINGFRACTION_CUT;
            boolean htccSignalCheck = p.getNphe()>EBConstants.HTCC_NPHE_CUT;
            boolean htccPionThreshold = p.vector().mag()>EBConstants.HTCC_PION_THRESHOLD;
            
            switch(abs(pid)) {
                case 11:
                    if(betaCheck==true && htccSignalCheck==true && sfCheck==true){
                        this.finalizePID(p, pid);
                        break;
                    }
                    if(betaCheck==false && htccSignalCheck==true && sfCheck==true){
                        this.finalizePID(p, pid);
                        break;
                    }
                case 211:
                    if(betaCheck==true && htccSignalCheck==true && sfCheck==false 
                            && htccPionThreshold==true) {
                        this.finalizePID(p, pid);
                        break;
                    }
                    if(betaCheck==false && htccSignalCheck==true && sfCheck==false 
                            && htccPionThreshold==true) {
                        this.finalizePID(p, pid);
                        break;
                    }               
                case 321:
                    if(betaCheck==true && sfCheck==false && htccSignalCheck==false){
                        this.finalizePID(p, pid);
                        break;
                    }
                case 2212:
                    if(betaCheck==true && sfCheck==false && htccSignalCheck==false){
                        this.finalizePID(p, pid);
                        break;
                    }
            }

        }
        
                
        public double PIDQuality(DetectorParticle p, int pid, DetectorEvent event) {
            
            return 0.0;
        }
        
        private void finalizePID(DetectorParticle p, int pid) {
                        p.setPid(pid);
                        theoryPID = pid;
                        PIDquality = this.PIDQuality(p, pid, event);
        }
                



              
    }

}

