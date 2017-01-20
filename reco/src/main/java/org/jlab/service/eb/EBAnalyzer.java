/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.detector.base.DetectorType;

/**
 *
 * @author gavalian
 */
public class EBAnalyzer {
    
    private int[]  pidPositive = new int[]{  211, 321, 2212};
    private int[]  pidNegative = new int[]{ -211,-321,-2212};
    
    public EBAnalyzer(){
        
    }
    
    public void processEvent(DetectorEvent event){
        if(event.getParticles().size()>0){
            DetectorParticle trigger = event.getParticle(0);
            
            //System.out.println(" trigger pid = " + trigger.getPid());
            if(trigger.getPid()==11){
                trigger.setBeta(1.0);
                trigger.setMass(0.0005);
                
                double time = 0.0;
                double path = 0.0;
                if(trigger.hasHit(DetectorType.FTOF, 1)==true){
                    time = trigger.getTime(DetectorType.FTOF, 1);
                    path = trigger.getPathLength(DetectorType.FTOF, 1);
                }
                if(trigger.hasHit(DetectorType.FTOF, 2)==true){
                    time = trigger.getTime(DetectorType.FTOF, 2);
                    path = trigger.getPathLength(DetectorType.FTOF, 2);
                }
                
                double t_of_f = path/30.0;
                double start_time = time - t_of_f;
                event.setStartTime(start_time);
                //System.out.println(" TIME = " + t_of_f + "  time from TOF = " + time);
                //System.out.println(" PATH = " + path + " " );
                //System.out.println(" SET START TIME = " + start_time + "  ACTUAL TIME = " + event.getStartTime());
                this.assignMasses(event);
                this.assignPids(event);
                //System.out.println(event.toString());
                
            }
        }
    }
    
    
    public void assignMasses(DetectorEvent event){
        
        int np = event.getParticles().size();
        //System.out.println("======================= ANALYSIS");
        for(int i = 1; i < np; i++){
            DetectorParticle p = event.getParticle(i);
            double start_time  = event.getStartTime();
            double beta = 0.0;
            double mass = 0.0;
            if(p.hasHit(DetectorType.FTOF, 1)==true){
                beta = p.getBeta(DetectorType.FTOF,1, start_time);
                mass = p.getMass2(DetectorType.FTOF,1, start_time);
                //System.out.println(String.format("PARTICLE %3d (Layer 1) p = %8.3f beta = %8.3f mass2 = %8.3f", 
                //        i,p.vector().mag(),beta,mass));
            }
            if(p.hasHit(DetectorType.FTOF, 2)==true){
                beta = p.getBeta(DetectorType.FTOF, 2,start_time);
                mass = p.getMass2(DetectorType.FTOF, 2,start_time);
                //System.out.println(String.format("PARTICLE %3d (Layer 2) p = %8.3f beta = %8.3f mass2 = %8.3f", 
                //        i,p.vector().mag(),beta,mass));
            }
            //System.out.println("----------------");
            /*
            if(p.hasHit(DetectorType.FTOF, 3)==true){
                beta = p.getBeta(DetectorType.FTOF, 3, start_time);
                mass = p.getMass(DetectorType.FTOF, 3, start_time);
            }*/
            p.setBeta(beta);
            p.setMass(mass);
            
            List<PidHypothesis> pidHyp = new ArrayList<PidHypothesis>();
            
           
            //p.getBeta(DetectorType.BST, start_time)
        }
    }
    
    public void assignPids(DetectorEvent event){
        int np = event.getParticles().size();
        List<PidHypothesis> pidHyp = new ArrayList<PidHypothesis>();
        
        for(int i = 1; i < np; i++){
            
            DetectorParticle p = event.getParticle(i);
            pidHyp.clear();
            if(p.getCharge()==0) break;
            if(p.getCharge()>0){
                for(int b = 0; b < this.pidPositive.length; b++){
                    PidHypothesis hyp = new PidHypothesis();
                    hyp.initParticle(p, this.pidPositive[b]);
                    pidHyp.add(hyp);
                }
                /*System.out.println("BEFORE SORTING");
                for(PidHypothesis h : pidHyp){
                    System.out.println(" hyp = " + h.getDiff() + " pid = " + h.getPid());
                }*/
                Collections.sort(pidHyp);
                /*System.out.println("AFTER SORTING");
                for(PidHypothesis h : pidHyp){
                    System.out.println(" hyp = " + h.getDiff() + " pid = " + h.getPid());
                }*/
                p.setPid(pidHyp.get(0).getPid());
            } else {
                for(int b = 0; b < this.pidNegative.length; b++){
                    PidHypothesis hyp = new PidHypothesis();
                    hyp.initParticle(p, this.pidNegative[b]);
                    pidHyp.add(hyp);
                }
                Collections.sort(pidHyp);                
                p.setPid(pidHyp.get(0).getPid());
            }
        }
    }
    
    public static class PidHypothesis implements Comparable{
        private int   pidTheory = 0;
        private double betaDiff = 0.0;
        
        public PidHypothesis(){
            
        }
        
        public void   initParticle(DetectorParticle p, int pid){
            double beta = p.getTheoryBeta(pid);
            this.pidTheory = pid;
            betaDiff = Math.abs(beta-p.getBeta());
        }
        
        public double getDiff(){return betaDiff;}
        public int    getPid(){ return pidTheory;}

        public int compareTo(Object o) {
            PidHypothesis p = (PidHypothesis) o;
            if(p.getDiff()>betaDiff) return -1;
            return 1;
        }
    
}
}
