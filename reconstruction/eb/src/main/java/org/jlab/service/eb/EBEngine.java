/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;


import java.util.List;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.clas.detector.*;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.clas.detector.CherenkovResponse;

/**
 *
 * @author gavalian
 *@author jnewton
 */
public class EBEngine extends ReconstructionEngine {

    String eventBank        = null;
    String particleBank     = null;
    String calorimeterBank  = null;
    String scintillatorBank = null;
    String cherenkovBank    = null;
    String trackBank        = null;
    String crossBank        = null;
    String matrixBank       = null;
    String trackType        = null;

    public EBEngine(String name){
        super(name,"gavalian","1.0");
        initBankNames();
    }
    
    public void initBankNames() {
        //Initialize bank names
    }
    

    public boolean processDataEvent(DataEvent de) {
        
        DetectorHeader head = head = EBio.readHeader(de);

        EventBuilder eb = new EventBuilder();
        eb.initEvent(head); // clear particles

        List<DetectorResponse>   responseECAL = CalorimeterResponse.readHipoEvent(de, "ECAL::clusters", DetectorType.EC);
        List<DetectorResponse>  responseFTOF = ScintillatorResponse.readHipoEvent(de, "FTOF::hits", DetectorType.FTOF);
        List<DetectorResponse>  responseCTOF = ScintillatorResponse.readHipoEvent(de, "CTOF::hits", DetectorType.CTOF);
        
        List<CherenkovResponse>     responseHTCC = CherenkovResponse.readHipoEvent(de,"HTCC::rec",DetectorType.HTCC);
        List<CherenkovResponse>     responseLTCC = CherenkovResponse.readHipoEvent(de,"LTCC::rec",DetectorType.LTCC);
        
        List<TaggerResponse>             trackFT = TaggerResponse.readHipoEvent(de, "FT::particles");
        
        eb.addDetectorResponses(responseFTOF);
        eb.addDetectorResponses(responseCTOF);
        eb.addDetectorResponses(responseECAL);
        eb.addCherenkovResponses(responseHTCC);
        eb.addCherenkovResponses(responseLTCC);

        

        
        // Add tracks
        List<DetectorTrack>  tracks = DetectorData.readDetectorTracks(de, trackType);
        eb.addTracks(tracks);       
        List<DetectorTrack> ctracks = DetectorData.readCentralDetectorTracks(de, "CVTRec::Tracks");
        eb.addTracks(ctracks);
        


        eb.processHitMatching();
        eb.addTaggerTracks(trackFT);
        eb.processNeutralTracks();        
        eb.assignTrigger();
 
        EBRadioFrequency rf = new EBRadioFrequency();
        eb.getEvent().getEventHeader().setRfTime(rf.getTime(de)+EBConstants.RF_OFFSET);
        //eb.getEvent().setRfTime(rf);
        
        //System.out.println(eb.getEvent().toString());
 
        
//        for(int i = 0; i < eb.getEvent().getParticles().size(); i++) {
//            System.out.println("Particle  " + i);
//            for(int j = 0 ; j < eb.getEvent().getParticles().get(i).getDetectorResponses().size() ; j++){
//                System.out.println("Point  " + eb.getEvent().getParticles().get(i).getDetectorResponses().get(j).getMatchedDistance());
//            }
//        }
        
        
        EBAnalyzer analyzer = new EBAnalyzer();
        //System.out.println("analyzing");
        analyzer.processEvent(eb.getEvent());
        

        
        //System.out.println(eb.getEvent().toString());
        

        
        if(eb.getEvent().getParticles().size()>0){
            DataBank bankP = DetectorData.getDetectorParticleBank(eb.getEvent().getParticles(), de, particleBank);
            de.appendBanks(bankP);
            DataBank bankEve = DetectorData.getEventBank(eb.getEvent(), de, eventBank);
            de.appendBanks(bankEve);
            List<CalorimeterResponse>   calorimeters = eb.getEvent().getCalorimeterResponseList();
            if(calorimeterBank!=null && calorimeters.size()>0) {
                DataBank bankCal = DetectorData.getCalorimeterResponseBank(calorimeters, de, calorimeterBank);
                de.appendBanks(bankCal);
            }
            List<ScintillatorResponse> scintillators = eb.getEvent().getScintillatorResponseList();
            if(scintillatorBank!=null && scintillators.size()>0) {
                DataBank bankSci = DetectorData.getScintillatorResponseBank(scintillators, de, scintillatorBank);
                de.appendBanks(bankSci);               
            }
            List<CherenkovResponse>       cherenkovs = eb.getEvent().getCherenkovResponseList();
            if(cherenkovBank!=null && cherenkovs.size()>0) {
                DataBank bankChe = DetectorData.getCherenkovResponseBank(cherenkovs, de, cherenkovBank);
                de.appendBanks(bankChe);
            }
            if(matrixBank!=null) {
                DataBank bankMat = DetectorData.getTBCovMatBank(eb.getEvent().getParticles(), de, matrixBank);
                de.appendBanks(bankMat);
            }
        
        }

        return true;
    }

    public void setEventBank(String eventBank) {
        this.eventBank = eventBank;
    }

    public void setParticleBank(String particleBank) {
        this.particleBank = particleBank;
    }

    public void setCalorimeterBank(String calorimeterBank) {
        this.calorimeterBank = calorimeterBank;
    }

    public void setScintillatorBank(String scintillatorBank) {
        this.scintillatorBank = scintillatorBank;
    }

    public void setCherenkovBank(String cherenkovBank) {
        this.cherenkovBank = cherenkovBank;
    }

    public void setTrackBank(String trackBank) {
        this.trackBank = trackBank;
    }

    public void setCrossBank(String crossBank) {
        this.crossBank = crossBank;
    }

    public void setTrackType(String trackType) {
        this.trackType = trackType;
    }


    
    @Override
    public boolean init() {
        System.out.println("[EB::] --> event builder is ready....");
        return true;
    }
    
}

