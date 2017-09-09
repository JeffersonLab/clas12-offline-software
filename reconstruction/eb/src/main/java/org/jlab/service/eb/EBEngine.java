package org.jlab.service.eb;

import java.util.Arrays;
import java.util.List;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.clas.detector.*;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.clas.detector.CherenkovResponse;
import org.jlab.rec.eb.EBCCDBConstants;
import org.jlab.rec.eb.EBCCDBEnum;

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
    String ftBank           = null;

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

        List<DetectorResponse>   responseECAL = CalorimeterResponse.readHipoEvent(de, "ECAL::clusters", DetectorType.ECAL);
        List<DetectorResponse>  responseFTOF = ScintillatorResponse.readHipoEvent(de, "FTOF::hits", DetectorType.FTOF);
        List<DetectorResponse>  responseCTOF = ScintillatorResponse.readHipoEvent(de, "CTOF::hits", DetectorType.CTOF);
        //List<DetectorResponse> responseCND = ScintillatorResponse.readHipoEvent(de, "CND::hits", DetectorType.CND);
        
        List<CherenkovResponse>     responseHTCC = CherenkovResponse.readHipoEvent(de,"HTCC::rec",DetectorType.HTCC);
        List<CherenkovResponse>     responseLTCC = CherenkovResponse.readHipoEvent(de,"LTCC::clusters",DetectorType.LTCC);

        
        List<TaggerResponse>        responseFTCAL = TaggerResponse.readHipoEvent(de, "FTCAL::clusters", DetectorType.FTCAL);
        List<TaggerResponse>        responseFTHODO = TaggerResponse.readHipoEvent(de, "FTHODO::clusters",DetectorType.FTHODO);

        System.out.println("# of FTCAL clusters " + responseFTCAL.size());
        System.out.println("# of FTHODO clusters " + responseFTHODO.size());
       
        // FIXME We should be starting with FT::particle, not clusters
        //List<TaggerResponse>             trackFT = TaggerResponse.readHipoEvent(de, "FTCAL::clusters", DetectorType.FTCAL);

        
        eb.addDetectorResponses(responseFTOF);
        eb.addDetectorResponses(responseCTOF);
        //eb.addDetectorResponses(responseCND);
        eb.addDetectorResponses(responseECAL);
        eb.addCherenkovResponses(responseHTCC);
        eb.addCherenkovResponses(responseLTCC);
        eb.addTaggerResponses(responseFTCAL);
        eb.addTaggerResponses(responseFTHODO);

        // Add tracks
        List<DetectorTrack>  tracks = DetectorData.readDetectorTracks(de, trackType);
        eb.addForwardTracks(tracks);       
        List<DetectorTrack> ctracks = DetectorData.readCentralDetectorTracks(de, "CVTRec::Tracks");
        eb.addCentralTracks(ctracks);

        // Process tracks:
        eb.processHitMatching();
        
        eb.processForwardNeutralTracks();
        eb.assignTrigger();
 
        // Process RF:
        EBRadioFrequency rf = new EBRadioFrequency();
        eb.getEvent().getEventHeader().setRfTime(rf.getTime(de)+EBConstants.RF_OFFSET);
        
        // Do PID etc:
        EBAnalyzer analyzer = new EBAnalyzer();
        analyzer.processEvent(eb.getEvent());
        
        List<DetectorParticle> ftparticles = DetectorData.readForwardTaggerParticles(de, "FT::particles");
        eb.addForwardTaggerParticles(ftparticles);
        eb.forwardTaggerIDMatching();
        
        for(int i = 0 ; i < eb.getEvent().getParticles().size() ; i++){
            DetectorParticle p = eb.getEvent().getParticles().get(i);
            System.out.println("Particle Index " + i + " Particle ID " + p.getPid());
            //System.out.println("Particle CAL ID " + p.getFTCALID());
            //System.out.println("Particle HODO ID " + p.getFTHODOID());
            for(int j = 0 ; j < p.getTaggerResponses().size() ; j++){
                System.out.println("Tagger Pindex " + p.getTaggerResponses().get(j).getAssociation());
            }
        }
        
        // create REC:detector banks:
        if(eb.getEvent().getParticles().size()>0){
            
            DataBank bankP = DetectorData.getDetectorParticleBank(eb.getEvent().getParticles(), de, particleBank);
            de.appendBanks(bankP);
            
            DataBank bankEve = DetectorData.getEventBank(eb.getEvent(), de, eventBank);
            de.appendBanks(bankEve);

            List<DetectorResponse>   calorimeters = eb.getEvent().getCalorimeterResponseList();
            if(calorimeterBank!=null && calorimeters.size()>0) {
                DataBank bankCal = DetectorData.getCalorimeterResponseBank(calorimeters, de, calorimeterBank);
                de.appendBanks(bankCal);
            }
            List<DetectorResponse> scintillators = eb.getEvent().getScintillatorResponseList();
            if(scintillatorBank!=null && scintillators.size()>0) {
                DataBank bankSci = DetectorData.getScintillatorResponseBank(scintillators, de, scintillatorBank);
                de.appendBanks(bankSci);               
            }
            List<CherenkovResponse>       cherenkovs = eb.getEvent().getCherenkovResponseList();
            if(cherenkovBank!=null && cherenkovs.size()>0) {
                DataBank bankChe = DetectorData.getCherenkovResponseBank(cherenkovs, de, cherenkovBank);
                de.appendBanks(bankChe);
            }
            
            List<TaggerResponse>       taggers = eb.getEvent().getTaggerResponseList();
            if (ftBank!=null && taggers.size()>0) {
                DataBank bankForwardTagger = DetectorData.getForwardTaggerBank(eb.getEvent().getTaggerResponseList(), de, trackBank);
                de.appendBanks(bankForwardTagger);
            }
                
//            if (ftBank!=null && trackFT.size()>0) {
//                DataBank bankForwardTagger = DetectorData.getForwardTaggerBank(eb.getEvent().getParticles(), de, "REC::ForwardTagger", trackFT.size());
//
//                de.appendBanks(bankForwardTagger);
//            }
            
            if (trackBank!=null && tracks.size()>0) {
                DataBank bankTrack = DetectorData.getTracksBank(eb.getEvent().getParticles(), de, trackBank, tracks.size());
                de.appendBanks(bankTrack);
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
    
    public void setFTBank(String ftBank) {
        this.ftBank = ftBank;
    }

    public void setCrossBank(String crossBank) {
        this.crossBank = crossBank;
    }

    public void setTrackType(String trackType) {
        this.trackType = trackType;
    }


    
    @Override
    public boolean init() {
      
        // load EB constants from CCDB:
        requireConstants(EBCCDBConstants.getAllTableNames());
        this.getConstantsManager().setVariation("default");
        // FIXME: check run number in processDataEvent, reload from CCDB if changed.
        // For now we just use hard-coded run number:
        EBCCDBConstants.load(10,this.getConstantsManager());

        // Example of retrieveing values from EBCCDBConstants: 
        //Double[] t=EBCCDBConstants.getArray(EBCCDBEnum.ELEC_SF);

        System.out.println("[EB::] --> event builder is ready....");
        return true;
    }
    
}