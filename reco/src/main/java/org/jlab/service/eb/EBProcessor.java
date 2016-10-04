/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.detector.base.DetectorType;

/**
 *
 * @author gavalian
 */
public class EBProcessor {
    
    List<DetectorParticle>      particles = new ArrayList<DetectorParticle>();
    List<DetectorResponse>  responsesFTOF = new ArrayList<DetectorResponse>();
    List<DetectorResponse>  responsesCALO = new ArrayList<DetectorResponse>();
    
    public EBProcessor(){
        
    }
    
    public EBProcessor(List<DetectorParticle> list){
        particles.addAll(list);
    }
    
    public void addTOF(List<DetectorResponse> det_ftof){
        responsesFTOF.addAll(det_ftof);
    }
    
    
    
    public void matchTimeOfFlight(){
        
        for(DetectorResponse resp : responsesFTOF){
            resp.setAssociation(-1);
        }
        
        int iparticle = 0;
        for(DetectorParticle p : particles){            
            int index = p.getDetectorHit(responsesFTOF,DetectorType.FTOF,2,15.0);
            if(index>=0){
                DetectorResponse response = responsesFTOF.get(index);
                response.setAssociation(iparticle);
                p.addResponse(response);
                p.setBeta(p.getBeta(DetectorType.FTOF));
                p.setMass(p.getMass2(DetectorType.FTOF));
            }
            iparticle++;
        }        
        
    }
    
    public void show(){
        System.out.println("------->  SHOW Event BUILDER Results <------");
        for(DetectorParticle p : particles){
            System.out.println(p);
        }
    }
}
