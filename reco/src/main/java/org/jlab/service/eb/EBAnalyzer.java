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
    private int[]  pidNegative = new int[]{ 11, -211,-321,-2212};
    
    public EBAnalyzer(){
        
    }
    
    public void processEvent(DetectorEvent event){
        if(event.getParticles().size()>0){
            DetectorParticle trigger = event.getParticle(0);
            
            //System.out.println(" trigger pid = " + trigger.getPid());
            if(abs(trigger.getPid())==11){
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
                //System.out.println("FTOF time" + time);
                double t_of_f = path/29.9792;
                double start_time = time - t_of_f;
                double deltatr = start_time - event.getRfTime() - (trigger.vertex().z() - (-4.5))/(29.9792)+800*2.004 + EBConstants.RF_OFFSET;
                double t_0corr = deltatr%2.004 - 2.004/2;//RF correction term
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
        for(int i = 0; i < np; i++) {
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
            
            if(p.hasHit(DetectorType.FTOF, 3)==true){
                //System.out.println("2");
                beta = p.getBeta(DetectorType.FTOF, 3, start_time);
                mass = p.getMass(DetectorType.FTOF, 3, start_time);
                p.setBeta(beta);
            }
            
            
            
            List<PidHypothesis> pidHyp = new ArrayList<PidHypothesis>();
            
           
            //p.getBeta(DetectorType.BST, start_time)
        }
    }
    
    public void assignPids(DetectorEvent event) {
        int np = event.getParticles().size();
        List<PidHypothesis> pidHyp = new ArrayList<PidHypothesis>();
        
        for(int i = 0; i < np; i++){
            DetectorParticle p = event.getParticle(i);
            //System.out.println("New Particle with Charge = " + p.getCharge());
            //System.out.println("This particle has a speed (v/c) of  = " + p.getBeta());
            pidHyp.clear();
            if(p.getCharge()==0) break;
            if(p.getCharge()>0){
                for(int b = 0; b < this.pidPositive.length; b++){
                    PidHypothesis hyp = new PidHypothesis();
                    //System.out.println("ID Analysis..... " + this.pidPositive[b]);
                    hyp.initParticle(p, this.pidPositive[b]);
                    //System.out.println(p.getPid());
                    pidHyp.add(hyp);
                }
                //System.out.println("BEFORE SORTING");
                for(PidHypothesis h : pidHyp){
                    //System.out.println(" hyp = " + h.getchi2()+ " pid = " + h.getPid());
                }
                //Collections.sort(pidHyp);
                //System.out.println("AFTER SORTING");
                for(PidHypothesis h : pidHyp){
                    //System.out.println(" hyp = " + h.getchi2() + " pid = " + h.getPid());
                }
                //p.setPid(pidHyp.get(0).getPid());
            } else {
                for(int b = 0; b < this.pidNegative.length; b++){
                    PidHypothesis hyp = new PidHypothesis();
                    //System.out.println("ID Analysis..... " + this.pidNegative[b]);
                    hyp.initParticle(p, this.pidNegative[b]);
                    //System.out.println(p.getPid());
                    pidHyp.add(hyp);
                }
                //Collections.sort(pidHyp); 
               // System.out.println(pidHyp.get(0).getPid());
                //p.setPid(pidHyp.get(0).getPid());
            }
            //System.out.println("  ");
        }
    }
    
    
    
    public static class PidHypothesis {
        private int   pidTheory = 0;
        private double betaSigma = 0.0;
        private double sfSigma = 0.0;
        private Boolean betaCheck = null;
        private Boolean sfCheck = null;
        private Boolean htccSignalCheck = null;
        private Boolean htccPionThreshold = null;
        private double chi2 = 0.0;

        
        
        public PidHypothesis(){
            
        }
        
        public void   initParticle(DetectorParticle p, int pid) {
            
            HashMap<Integer,Double> betaDiffs = new HashMap<Integer,Double>(); //Beta Differences
            betaDiffs.put(0,abs(p.getTheoryBeta(11) - p.getBeta())); //How close is track beta to an electron's?
            betaDiffs.put(1,abs(p.getTheoryBeta(2212) - p.getBeta()));//How close is track beta to a proton's's?
            betaDiffs.put(2,abs(p.getTheoryBeta(211) - p.getBeta()));//How close is track beta to a pion's?
            betaDiffs.put(3,abs(p.getTheoryBeta(321) - p.getBeta()));//How close is track beta to a kaon's?
            double min = betaDiffs.get(0);
            int beta_index = 0;
            for (int i = 0; i <= 3; i++) {
                if (betaDiffs.get(i) < min) {
                min = betaDiffs.get(i);
                beta_index = i;
                }
            }
            
            double beta = p.getTheoryBeta(pid);
            this.pidTheory = pid;
            betaSigma = Math.abs(beta-p.getBeta());
            betaCheck = (abs(pid)==11 && beta_index==0 && p.getBeta()>0.0) || (abs(pid)==211 && beta_index==2 && p.getBeta()>0.0) 
                        || (abs(pid)==2212 && beta_index==1 && p.getBeta()>0.0) || (abs(pid)==321 && beta_index==3 && p.getBeta()>0.0);
            sfCheck = p.getEnergyFraction(DetectorType.EC)>EBConstants.ECAL_SAMPLINGFRACTION_CUT;
            htccSignalCheck = p.getNphe()>1;
            htccPionThreshold = p.vector().mag()>4.9;
            //System.out.println("betaCheck  sfCheck  htccSignalCheck  htccPionThreshold");
            //System.out.println(betaCheck + "  " + sfCheck + "  " + "  " + htccSignalCheck + "  " + htccPionThreshold);
            this.PID(p, pid);

        }

        
        public double getSigma(){return betaSigma;}
        public int    getPid(){ return pidTheory;}
        public double getchi2(){ return chi2; }
        

        public void PID(DetectorParticle p, int pid) {
                //System.out.println("Pre-PID " + pid);
            switch(abs(pid)) {
                case 11:
                    boolean ElectronCondition1 = betaCheck==true && sfCheck==true && htccSignalCheck==true;
                    boolean ElectronCondition2 = betaCheck==true && sfCheck==false && htccSignalCheck==true && htccPionThreshold==false;
                    boolean ElectronCondition3 = betaCheck==false && sfCheck==true && htccSignalCheck==true;
                    boolean ElectronCondition4 = betaCheck==false && sfCheck==false && htccSignalCheck==true && htccPionThreshold==false;
                    if(ElectronCondition1==true || ElectronCondition2==true || ElectronCondition3==true || ElectronCondition4==true){
                        p.setPid(pid);
                        this.pidTheory = pid;
                        break;
                    }
                case 211:
                    boolean PionCondition1 = betaCheck==true && sfCheck==false && htccSignalCheck==false;
                    boolean PionCondition2 = betaCheck==false && sfCheck==false && htccSignalCheck==true && htccPionThreshold==true;
                    boolean PionCondition3 = betaCheck==true && sfCheck==false && htccSignalCheck==true && htccPionThreshold==true;
                    if(PionCondition1==true || PionCondition2==true || PionCondition3==true){
                        p.setPid(pid);
                        this.pidTheory = pid;
                        break;
                    }
                case 321:
                    boolean KaonCondition1 = betaCheck==true && sfCheck==false && htccSignalCheck==false;
                    if(KaonCondition1==true){
                       // System.out.println("Negative Kaon?");
                    p.setPid(pid);
                    this.pidTheory = pid;
                    break;
                    }
                case 2212:
                    boolean ProtonCondition1 = betaCheck==true && sfCheck==false && htccSignalCheck==false;
                    if(ProtonCondition1==true){
                    p.setPid(pid);
                    this.pidTheory = pid;
                    break;
                    }
            }

        }

        public int compareTo(Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    
}
}

