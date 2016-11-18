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
import org.jlab.clas.physics.Vector3;
import org.jlab.detector.base.DetectorType;

/**
 *
 * @author gavalian
 */
public class EBProcessor {
    
    List<DetectorParticle>      particles = new ArrayList<DetectorParticle>();
    List<DetectorResponse>  responsesFTOF = new ArrayList<DetectorResponse>();
    List<DetectorResponse>  responsesECAL = new ArrayList<DetectorResponse>();
    
    public EBProcessor(){
        
    }
    
    public EBProcessor(List<DetectorParticle> list){
        particles.addAll(list);
    }
    
    public void addTOF(List<DetectorResponse> det_ftof){
        responsesFTOF.addAll(det_ftof);
    }
    
    public void addECAL(List<DetectorResponse> det_ecal){
        responsesECAL.addAll(det_ecal);
    }
    
    public List<DetectorResponse> getResponses(){
        List<DetectorResponse> responses = new ArrayList<DetectorResponse>();
        responses.addAll(this.responsesFTOF);
        responses.addAll(this.responsesECAL);
        return responses;
    }
    
    public void matchCalorimeter(){
        
        for(DetectorResponse resp : responsesECAL){
            resp.setAssociation(-1);
        }
        
        List<DetectorResponse>  pcal = DetectorResponse.getListByLayer(responsesECAL, DetectorType.EC, 1);
        //System.out.println(" PCAL HITS SIZE = " + pcal.size() + " " + responsesECAL.size());
        int iparticle = 0;
        for(DetectorParticle p : particles){            
            int index = p.getDetectorHit(pcal,DetectorType.EC,1,15.0);
            if(index>=0){
                DetectorResponse response = pcal.get(index);
                response.setAssociation(iparticle);
                p.addResponse(response,true);
            }
            iparticle++;
        }
        
        
        List<DetectorResponse>  ecin = DetectorResponse.getListByLayer(responsesECAL, DetectorType.EC, 4);
        //System.out.println(" ECIN COUNTER = " + ecin.size());
        iparticle = 0;
        for(DetectorParticle p : particles){            
            int index = p.getDetectorHit(ecin,DetectorType.EC,4,15.0);
            if(index>=0){
                DetectorResponse response = ecin.get(index);
                response.setAssociation(iparticle);
                p.addResponse(response,true);
            }
            iparticle++;
        }
        
        List<DetectorResponse>  ecout = DetectorResponse.getListByLayer(responsesECAL, DetectorType.EC, 7);
        //System.out.println(" ECOUT COUNTER = " + ecout.size());
        iparticle = 0;
        for(DetectorParticle p : particles){            
            int index = p.getDetectorHit(ecout,DetectorType.EC,7,15.0);
            if(index>=0){
                DetectorResponse response = ecout.get(index);
                response.setAssociation(iparticle);
                p.addResponse(response,true);
            }
            iparticle++;
        }
    }
    
    
    public List<DetectorParticle>  getParticles(){return this.particles;}
    
    
    public void matchNeutral(){
        List<DetectorResponse>  pcal  = DetectorResponse.getListByLayer(responsesECAL, DetectorType.EC, 1);
        List<DetectorParticle>  gamma = new ArrayList<DetectorParticle>();
        
        for(int i = 0; i < pcal.size(); i++){
            if(pcal.get(i).getAssociation()<0){
                DetectorParticle  p = new DetectorParticle();
                Vector3  u = new Vector3(pcal.get(i).getPosition().x(),
                        pcal.get(i).getPosition().y(),pcal.get(i).getPosition().z());
                u.unit();
                p.setCross(0.0, 0.0, 0.0, u.x(),u.y(),u.z());
                p.setPid(22);
                p.setCharge(0);                
                p.addResponse(pcal.get(i));
                gamma.add(p);
                
            }
        }
        List<DetectorResponse>  ecin  = DetectorResponse.getListByLayer(responsesECAL, DetectorType.EC, 4);
        List<DetectorResponse>  ecout = DetectorResponse.getListByLayer(responsesECAL, DetectorType.EC, 7);
        
        int nparticles = this.particles.size();
        int iparticle  = 0;
        for(DetectorParticle p : gamma){
            int index = p.getDetectorHit(ecin,DetectorType.EC,4,10.0);
            if(index>=0){
                if(ecin.get(index).getAssociation()<0){
                    ecin.get(index).setAssociation(nparticles+iparticle);
                    p.addResponse(ecin.get(index));
                }
            }
            
            index = p.getDetectorHit(ecout,DetectorType.EC,7,10.0);
            if(index>=0){
                if(ecout.get(index).getAssociation()<0){
                    ecout.get(index).setAssociation(nparticles+iparticle);
                    p.addResponse(ecout.get(index));
                }
            }
            iparticle++;
            
            double  energy = p.getEnergy(DetectorType.EC)/0.27;
            Vector3 dir    = p.getCrossDir();
            p.vector().setXYZ(dir.x()*energy, dir.y()*energy, dir.z()*energy);
            p.setBeta(1.0);
        }
        
        for(int i = 0; i < gamma.size(); i++){
            if(gamma.get(i).vector().mag()>0.5){
                particles.add(gamma.get(i));
            }
        }
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
                p.addResponse(response,true);
                p.setBeta(p.getBeta(DetectorType.FTOF));
                p.setMass(p.getMass2(DetectorType.FTOF));
            }
            iparticle++;
        }        
        
    }
    
    
    public void readCentralTracks(){
        
    }
    public void show(){
        System.out.println("------->  SHOW Event BUILDER Results <------");
        System.out.println(" ECAL HITS = " + responsesECAL.size());
        for(DetectorParticle p : particles){
            System.out.println(p);
        }
    }
}
