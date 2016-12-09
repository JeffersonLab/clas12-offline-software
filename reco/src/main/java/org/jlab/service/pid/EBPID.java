/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.pid;

import static java.lang.Math.abs;
import java.util.HashMap;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataEvent;








/**
 *
 * @author Joseph Newton
 */
public class EBPID {
    
    DetectorEvent event = new DetectorEvent();

    public EBPID(){
        
    }
    
    public void setEvent(DetectorEvent e){this.event = e;}

    
    

    
    public void PIDAssignment() {

        for(int i = 0; i < event.getParticles().size(); i++) { 
                Particleid(event.getParticles().get(i)); //Assigns PID
                TimingChecks(event.getParticles().get(i)); //Checks timing and removes hits that fail
                if(event.getParticles().get(i).getParticleTimeCheck()!=true) {
                    Particleid(event.getParticles().get(i));
                 }
            }   
        }
    
    public void Particleid(DetectorParticle particle) {
                TBElectron electron = new TBElectron();
                TBPion pion = new TBPion();
                TBKaon kaon = new TBKaon();
                TBProton proton = new TBProton();
                
                int eid, piid, kid, prid;
                
                eid = electron.getPIDResult(particle).getFinalID(); //Is it an electron/positron?
                piid = pion.getPIDResult(particle).getFinalID();//Is it a charged pion?
                kid = kaon.getPIDResult(particle).getFinalID();//Is it a charged kaon?
                prid = proton.getPIDResult(particle).getFinalID();//Is it a proton/anti-proton?
                
                particle.setPid(eid + piid + kid + prid); //Only one value will be non-zero
         }
    
    public void TimingChecks(DetectorParticle particle) {
                ECTiming ectime = new ECTiming();
                ectime.CoincidenceCheck(event, particle, DetectorType.EC, 0);
                ectime.CoincidenceCheck(event,particle,DetectorType.EC,1);
                ectime.CoincidenceCheck(event,particle,DetectorType.EC,2);
                
                
        }
    

    
    

 
}


class TBElectron implements ParticleID {

            public PIDResult getPIDResult(DetectorParticle particle) {  
               
                PIDExamination PID = new PIDExamination(); //This is the "DetectorParticle"s PID properties.
                PID.setClosestBeta(PID.GetBeta(particle, 11));
                PID.setHTCC(PID.HTCCSignal(particle));
                PID.setCorrectSF(PID.SamplingFractionCheck(particle));
                PID.setHTCCThreshold(PID.HTCCThreshold(particle));
                

                
                HashMap<Integer,PIDExamination> ElectronTests= new HashMap<Integer,PIDExamination>();
                ElectronTests = PID.getElectronTests(particle);
                PIDResult Result = new PIDResult();
                for(int i = 0 ; i < ElectronTests.size() ; i++){
                    if(PID.compareExams(ElectronTests.get(i))==true){
                        Result.setFinalID(11*-particle.getCharge());
                        Result.setPIDExamination(PID);
                        break;
                    }
                    if(i == (ElectronTests.size()-1)){
                        Result.setFinalID(0);
                        Result.setPIDExamination(PID);
                    }
                }
                
                return Result;
            }
                        
    }


class TBPion implements ParticleID {

            public PIDResult getPIDResult(DetectorParticle particle) {  
               
                PIDExamination PID = new PIDExamination(); //This is the "DetectorParticle"s PID properties.
                PID.setClosestBeta(PID.GetBeta(particle, 211));
                PID.setHTCC(PID.HTCCSignal(particle));
                PID.setCorrectSF(PID.SamplingFractionCheck(particle));
                PID.setHTCCThreshold(PID.HTCCThreshold(particle));
                
                HashMap<Integer,PIDExamination> PionTests= new HashMap<Integer,PIDExamination>();
                PionTests = PID.getPionTests(particle);
                PIDResult Result = new PIDResult();
                for(int i = 0 ; i < PionTests.size() ; i++){
                
                    if(PID.compareExams(PionTests.get(i))==true){
                        Result.setFinalID(211*particle.getCharge());
                        Result.setPIDExamination(PID);
                        break;
                    }
                    if(i == (PionTests.size()-1)){
                        Result.setFinalID(0);
                        Result.setPIDExamination(PID);
                    }
                }
                
                return Result;
            }
                        
    }

class TBKaon implements ParticleID {

            public PIDResult getPIDResult(DetectorParticle particle) {  
               
                PIDExamination PID = new PIDExamination(); //This is the "DetectorParticle"s PID properties.
                PID.setClosestBeta(PID.GetBeta(particle, 321));
                PID.setHTCC(PID.HTCCSignal(particle));
                //PID.setLTCC(PIDAssignment.LTCCSignal(particle));
                
                HashMap<Integer,PIDExamination> KaonTests= new HashMap<Integer,PIDExamination>();
                KaonTests = PID.getKaonTests(particle);
                PIDResult Result = new PIDResult();
                for(int i = 0 ; i < KaonTests.size() ; i++){
                    if(PID.compareExams(KaonTests.get(i))==true){
                        Result.setFinalID(321*particle.getCharge());
                        Result.setPIDExamination(PID);
                        break;
                    }
                    if(i == (KaonTests.size()-1)){
                        Result.setFinalID(0);
                        Result.setPIDExamination(PID);
                    }
                }
                
                return Result;
            }
                        
    }

class TBProton implements ParticleID {

            public PIDResult getPIDResult(DetectorParticle particle) {  
               
                PIDExamination PID = new PIDExamination(); //This is the "DetectorParticle"s PID properties.
                PID.setClosestBeta(PID.GetBeta(particle, 2212));
                PID.setHTCC(PID.HTCCSignal(particle));
                //PID.setLTCC(PIDAssignment.LTCCSignal(particle));
                
                HashMap<Integer,PIDExamination> ProtonTests= new HashMap<Integer,PIDExamination>();
                ProtonTests = PID.getProtonTests(particle);
                PIDResult Result = new PIDResult();
                for(int i = 0 ; i < ProtonTests.size() ; i++){
                    if(PID.compareExams(ProtonTests.get(i))==true){
                        Result.setFinalID(2212*particle.getCharge());
                        Result.setPIDExamination(PID);
                        break;
                    }
                    if(i == (ProtonTests.size()-1)){
                        Result.setFinalID(0);
                        Result.setPIDExamination(PID);
                    }
                }
                
                return Result;
            }
            
        }

class ECTiming implements ParticleTiming {
    public void CoincidenceCheck(DetectorEvent event, DetectorParticle particle, DetectorType type, int layer){
        if(particle.hasHit(type,layer)==true){
        double delta = particle.getVertexTime(type,layer) - event.getEventTrigger().getStartTime();
        if(abs(delta)>2){
            particle.getHit(type, layer).setEnergy(0.0);
            particle.setParticleTimeCheck(false);
        }
        if(abs(delta)<2){
            particle.setParticleTimeCheck(true);
        }
    }
        if(particle.hasHit(type,layer)==false){
            particle.setParticleTimeCheck(true);
        }
    }
}

