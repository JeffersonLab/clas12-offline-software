/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.detector.base.DetectorType;

/**
 *
 * @author gavalian
 */
public class EBAnalyzer {
    
    public EBAnalyzer(){
        
    }
    
    public void processEvent(DetectorEvent event){
        if(event.getParticles().size()>0){
            DetectorParticle trigger = event.getParticle(0);
            //System.out.println(" trigger pid = " + trigger.getPid());
            if(trigger.getPid()==11){
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
                this.assignPids(event);
                //System.out.println(event.toString());
                
            }
        }
    }
    
    
    public void assignPids(DetectorEvent event){
        int np = event.getParticles().size();
        for(int i = 1; i < np; i++){
            DetectorParticle p = event.getParticle(i);
            double start_time  = event.getStartTime();
            double beta = 0.0;
            double mass = 0.0;
            if(p.hasHit(DetectorType.FTOF, 1)==true){
                beta = p.getBeta(DetectorType.FTOF, start_time);
                mass = p.getMass2(DetectorType.FTOF, start_time);
            }
            if(p.hasHit(DetectorType.FTOF, 2)==true){
                beta = p.getBeta(DetectorType.FTOF, start_time);
                mass = p.getMass(DetectorType.FTOF, start_time);
            }
            if(p.hasHit(DetectorType.FTOF, 3)==true){
                beta = p.getBeta(DetectorType.FTOF, start_time);
                mass = p.getMass(DetectorType.FTOF, start_time);
            }
            p.setBeta(beta);
            p.setMass(mass);
            //p.getBeta(DetectorType.BST, start_time)
        }
    }
    
}
