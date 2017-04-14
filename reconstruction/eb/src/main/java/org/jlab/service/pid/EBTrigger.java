
package org.jlab.service.pid;

import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jlab.detector.base.DetectorType;
import static java.lang.Math.abs;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.clas.detector.*;

/**
 *
 * @author jnewton
 */
public class EBTrigger {

    DetectorEvent event = new DetectorEvent();
    DataEvent de;

    public EBTrigger(){
        
    }
    
    public void setEvent(DetectorEvent e){this.event = e;}
    public void setDataEvent(DataEvent data){this.de = data;}
    
    public void RFInformation() {
      
          if(de.hasBank("RF::info")==true){
            EvioDataBank bank = (EvioDataBank) de.getBank("RF::info");
            //event.getEventTrigger().setRFTime(bank.getDouble("rf",0));//Obtain a TDC value
        }
    }
            
        
    public void Trigger() {
        for(int i = 0 ; i < this.event.getParticles().size() ; i++) {
            TIDResult result = new TIDResult();
            result = EBTrigger.GetParticleScore(event.getParticles().get(i)); //How much does it resemble electron/positron?
            event.getParticles().get(i).setScore(result.getScore());//"score" is recorded for each DetectorParticle
           // System.out.println("score" + result.getScore());
        }
    
        ElectronTriggerList electron = new ElectronTriggerList();
        PositronTriggerList positron = new PositronTriggerList();
        NegativePionTriggerList negativepion = new NegativePionTriggerList();
        /*
        event.getEventTrigger().setElectronCandidates(electron.getCandidates(event));
        event.getEventTrigger().setPositronCandidates(positron.getCandidates(event));
        event.getEventTrigger().setNegativePionCandidates(negativepion.getCandidates(event));

            switch(event.getEventTrigger().TriggerScenario()) { //Case 1 means electrons were found, Case 2 means positrons were found, Case 3 means negative pions were found
                case 0:
                //System.out.println("No Trigger Found");
                break;
                case 1:
                TriggerElectron telectron = new TriggerElectron();
                telectron.CollectBestTriggerInformation(event); 
                break;
                case 2:
                TriggerPositron tpositron = new TriggerPositron();
                tpositron.CollectBestTriggerInformation(event);
                break;
                case 3:
                TriggerNegativePion tnegativepion = new TriggerNegativePion();
                tnegativepion.CollectBestTriggerInformation(event);
                break;
              }*/
        }
       
     
    public void CalcBeta2(DetectorParticle p){ //Maybe you can modify this so that speed of track can be calculated by any detector based off availabilitiy
        if(p.hasHit(DetectorType.FTOF, 2)==true){
            DetectorResponse res = p.getHit(DetectorType.FTOF, 2);
            double path = res.getPath();
            double time = res.getTime();

          double beta = 0.0;// p.getPathLength(DetectorType.FTOF)/(time-event.getEventTrigger().getStartTime())/29.9792;
            p.setBeta(beta);
            double mom = p.vector().mag();
            double mass2 = (mom*mom - beta*beta*mom*mom)/(beta*beta);
            p.setMass(mass2);
        }
    }
    
    public void CalcBetas(){
        for(int i = 0; i < event.getParticles().size() ; i++){
            DetectorParticle p = new DetectorParticle();
            p = event.getParticles().get(i);
            CalcBeta2(p);
        }
    }
       
    


    
    public static TIDResult GetParticleScore(DetectorParticle particle) {  
               
        TIDExamination TID = new TIDExamination(); //This is the "DetectorParticle"s PID properties.
        TID.setCorrectSF(TID.SamplingFractionCheck(particle)); //Is the sampling fraction within +-5 Sigma?
        TID.setHTCC(TID.HTCCSignal(particle)); //Is there a signal in HTCC?
        TID.setFTOF(particle.hasHit(DetectorType.FTOF, 2));//Is there a hit in FTOF1B?
               
        TIDResult Result = new TIDResult();
        Result.setScore(TID.getTriggerScore());//Trigger Score for Electron/Positron
        Result.setTIDExamination(TID);
                
           
        return Result;
      }
    
        
}


class TriggerElectron implements BestTrigger {
    
    public void CollectBestTriggerInformation(DetectorEvent event){
        /*
                   EventTrigger Trigger = new EventTrigger();
                   Trigger = event.getEventTrigger();
                   DetectorParticle BestTrigger = Trigger.GetBestTriggerParticle(Trigger.getElectronCandidates());
                   Trigger.setTriggerParticle(BestTrigger);
                   Trigger.setzt(BestTrigger.vertex().z());
                   Trigger.setVertexTime(Trigger.VertexTime(BestTrigger, 11));
                   Trigger.setStartTime(Trigger.StartTime(BestTrigger,11)); //calculate start time using speed of an electron
          */         
    }
}

class TriggerPositron implements BestTrigger {
    
    public void CollectBestTriggerInformation(DetectorEvent event){
                /*   EventTrigger Trigger = new EventTrigger();
                   Trigger = event.getEventTrigger();
                   DetectorParticle BestTrigger = Trigger.GetBestTriggerParticle(Trigger.getPositronCandidates());
                   Trigger.setTriggerParticle(BestTrigger);
                   Trigger.setVertexTime(Trigger.VertexTime(BestTrigger, 11));
                   Trigger.setStartTime(Trigger.StartTime(BestTrigger,11));
                   Trigger.setzt(BestTrigger.vertex().z());*/
                   
    }
}

class TriggerNegativePion implements BestTrigger {
    
    public void CollectBestTriggerInformation(DetectorEvent event){
                  /* EventTrigger Trigger = new EventTrigger();
                   Trigger = event.getEventTrigger();
                   DetectorParticle BestTrigger = Trigger.GetBestTriggerParticle(Trigger.getNegativePionCandidates());
                   Trigger.setTriggerParticle(BestTrigger);
                   Trigger.setVertexTime(Trigger.VertexTime(BestTrigger, 211));
                   Trigger.setStartTime(Trigger.StartTime(BestTrigger,211));
                   Trigger.setzt(BestTrigger.vertex().z());
                   */
    }
}



class ElectronTriggerList implements TriggerCandidateList {
    public HashMap<Integer,DetectorParticle>  getCandidates(DetectorEvent event) {
        HashMap<Integer,DetectorParticle> map = new HashMap<Integer,DetectorParticle>();
            int mapiteration = 0;
            for(int i = 0 ; i < event.getParticles().size() ; i++){
                if(event.getParticles().get(i).getCharge()==-1 && event.getParticles().get(i).getNphe()>0
                        && event.getParticles().get(i).hasHit(DetectorType.FTOF, 2)){
                    map.put(mapiteration,event.getParticles().get(i));
                    mapiteration = mapiteration + 1;
                }
            }
        return map;
    }
}

class PositronTriggerList implements TriggerCandidateList {
    public HashMap<Integer,DetectorParticle>  getCandidates(DetectorEvent event) {
        HashMap<Integer,DetectorParticle> map = new HashMap<Integer,DetectorParticle>();
            int mapiteration = 0;
            for(int i = 0 ; i < event.getParticles().size() ; i++){
                if(event.getParticles().get(i).getCharge()==1 && event.getParticles().get(i).getNphe()>0
                        && event.getParticles().get(i).hasHit(DetectorType.FTOF, 2)){
                    map.put(mapiteration,event.getParticles().get(i));
                    mapiteration = mapiteration + 1;
                }
            }
        return map;
    }
}

class NegativePionTriggerList implements TriggerCandidateList {
    public HashMap<Integer,DetectorParticle>  getCandidates(DetectorEvent event) {
        HashMap<Integer,DetectorParticle> map = new HashMap<Integer,DetectorParticle>();
            int mapiteration = 0;
            for(int i = 0 ; i < event.getParticles().size() ; i++){
                if(event.getParticles().get(i).getCharge()==-1 && event.getParticles().get(i).getScore()>=10 &&
                        event.getParticles().get(i).getScore()<=11 && event.getParticles().get(i).vector().mag()>3){
                    map.put(mapiteration,event.getParticles().get(i));
                    mapiteration = mapiteration + 1;
                }
            }
        return map;
    }
}

