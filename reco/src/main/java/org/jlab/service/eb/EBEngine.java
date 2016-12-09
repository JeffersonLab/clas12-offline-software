/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import java.util.List;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.service.pid


/**
 *
 * @author gavalian
 */
public class EBEngine extends ReconstructionEngine {
    
    public EBEngine(){
        super("EB","gavalian","1.0");
    }
    
    @Override
    public boolean processDataEvent(DataEvent de) {
        
        int eventType = EBio.TRACKS_HB;
        
        if(EBio.isTimeBased(de)==true){
            eventType = EBio.TRACKS_TB;
        }
        
        List<DetectorParticle>  chargedParticles = EBio.readTracks(de, eventType);
        List<DetectorResponse>  ftofResponse     = EBio.readFTOF(de);
        List<DetectorResponse>  ecalResponse     = EBio.readECAL(de);
        List<CherenkovResponse> htccResponse     = EBio.readHTCC(de);
        
        EBProcessor processor = new EBProcessor(chargedParticles);
        processor.addTOF(ftofResponse);
        processor.addECAL(ecalResponse);
        processor.addHTCC(htccResponse);
        
        processor.matchTimeOfFlight();
        processor.matchCalorimeter();
        processor.matchHTCC();
        processor.matchNeutral();
        
        List<DetectorParticle> centralParticles = EBio.readCentralTracks(de);
        
        /*
        int nparticles = processor.getParticles().size();
        System.out.println("--------------------------------- ### NPARTICLES " + nparticles);
        for(int i = 0; i < nparticles; i++){
            if(processor.getParticles().get(i).getCharge()<0){
                System.out.println(processor.getParticles().get(i).toString());
            }                        
        }
        for(int i = 0; i < processor.getResponses().size(); i++){
            System.out.println(processor.getResponses().get(i).toString());
        }*/
        
        processor.getParticles().addAll(centralParticles);
        /*
        if(de.hasBank("RUN::config")==true){
            EvioDataBank bankHeader = (EvioDataBank) de.getBank("RUN::config");
            System.out.println(String.format("***>>> RUN # %6d   EVENT %6d", 
                    bankHeader.getInt("Run",0), bankHeader.getInt("Event",0)));
        }*/
        //processor.show();
        /*System.out.println("CHARGED PARTICLES = " + chargedParticles.size());
        for(DetectorParticle p : chargedParticles){
            System.out.println(p);
        }*/
        
         DetectorEvent  detectorEvent = new DetectorEvent();
        
        for(int i = 0 ; i < chargedParticles.size() ; i++){ 
            detectorEvent.addParticle(chargedParticles.get(i));
        }

      
        EventTrigger trigger = new EventTrigger();  
        detectorEvent.setEventTrigger(trigger);
        
        EBTrigger triggerinfo = new EBTrigger();
        triggerinfo.setEvent(detectorEvent);
        triggerinfo.setDataEvent(de);
        
        triggerinfo.RFInformation(); //Obtain RF Time
        triggerinfo.Trigger();//Use Trigger Particle Vertex Time and RF Time for Start Time
        triggerinfo.CalcBetas(); //Calculate Speeds and Masses of Particles
       
        //System.out.println(detectorEvent.getEventTrigger());
        
        
        EBPID pid = new EBPID();
        if(EBio.isTimeBased(de)==true){
           pid.setEvent(detectorEvent);
           pid.PIDAssignment();//PID Assignment
}
        
        
        
        
        EvioDataBank pBank = (EvioDataBank) EBio.writeTraks(processor.getParticles(), eventType);
        EvioDataBank dBank = (EvioDataBank) EBio.writeResponses(processor.getResponses(), eventType);

        de.appendBanks(pBank,dBank);
        
        return true;
    }

    @Override
    public boolean init() {
        System.out.println("[EB::] --> event builder is ready....");
        return true;
    }
    
}
