package org.jlab.service.eb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
 *@author baltzell
 */
public class EBEngine extends ReconstructionEngine {

    boolean dropBanks = false;
    boolean alreadyDroppedBanks = false;

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

        if (this.dropBanks==true) this.dropBanks(de);

        // check run number, get constants from CCDB:
        int run=-1;
        if (de.hasBank("RUN::config")) {
            run=de.getBank("RUN::config").getInt("run",0);
        }
        if (run>0 && run!=EBCCDBConstants.getRunNumber()) {
            EBCCDBConstants.load(run,this.getConstantsManager());
        }
        if (!EBCCDBConstants.isLoaded()) {
            System.out.println("EBEngine:  found no run number, CCDB constants not loaded, skipping event.");
            return false;
        }

        DetectorHeader head = EBio.readHeader(de);

        EventBuilder eb = new EventBuilder();
        eb.initEvent(head); // clear particles

        EBMatching ebm = new EBMatching(eb);
        
        // Process RF:
        EBRadioFrequency rf = new EBRadioFrequency();
        eb.getEvent().getEventHeader().setRfTime(rf.getTime(de)+EBCCDBConstants.getDouble(EBCCDBEnum.RF_OFFSET));
        
        List<DetectorResponse>  responseECAL = CalorimeterResponse.readHipoEvent(de, "ECAL::clusters", DetectorType.ECAL,"ECAL::moments");
        List<DetectorResponse>  responseFTOF = ScintillatorResponse.readHipoEvent(de, "FTOF::hits", DetectorType.FTOF);
        List<DetectorResponse>  responseCTOF = ScintillatorResponse.readHipoEvent(de, "CTOF::hits", DetectorType.CTOF);
        List<DetectorResponse>  responseCND  = ScintillatorResponse.readHipoEvent(de, "CND::hits", DetectorType.CND);
        
        List<CherenkovResponse> responseHTCC = CherenkovResponse.readHipoEvent(de,"HTCC::rec",DetectorType.HTCC);
        List<CherenkovResponse> responseLTCC = CherenkovResponse.readHipoEvent(de,"LTCC::clusters",DetectorType.LTCC);
        
        eb.addDetectorResponses(responseFTOF);
        eb.addDetectorResponses(responseCTOF);
        eb.addDetectorResponses(responseCND);
        eb.addDetectorResponses(responseECAL);
        eb.addCherenkovResponses(responseHTCC);
        eb.addCherenkovResponses(responseLTCC);

        // Add tracks
        List<DetectorTrack>  tracks = DetectorData.readDetectorTracks(de, trackType);
        eb.addForwardTracks(tracks);      
        
        List<DetectorTrack> ctracks = DetectorData.readCentralDetectorTracks(de, "CVTRec::Tracks");
        eb.addCentralTracks(ctracks);
        
        eb.getPindexMap().put(0, tracks.size());
        eb.getPindexMap().put(1, ctracks.size());
        
        // Process tracks-hit matching:
        eb.processHitMatching();

        // Assign trigger/startTime particle: 
        eb.assignTrigger();
 
        // Create neutrals:
        // (after assigning trigger particle, to get vertex/momentum right):
        eb.processNeutralTracks();
        
        List<DetectorParticle> centralParticles = eb.getEvent().getCentralParticles();
        
        ebm.processCentralParticles(de,"CVTRec::Tracks","CTOF::hits","CND::hits",
                                    centralParticles, responseCTOF, responseCND);
        
        // Do PID etc:
        EBAnalyzer analyzer = new EBAnalyzer();
        analyzer.processEvent(eb.getEvent());

        // Add Forward Tagger particles:
        List<DetectorParticle> ftparticles = DetectorData.readForwardTaggerParticles(de, "FT::particles");       
        List<Map<DetectorType, Integer>> ftIndices = DetectorData.readForwardTaggerIndex(de,"FT::particles");
        List<TaggerResponse>        responseFTCAL = TaggerResponse.readHipoEvent(de,"FTCAL::clusters",DetectorType.FTCAL);
        List<TaggerResponse>        responseFTHODO = TaggerResponse.readHipoEvent(de,"FTHODO::clusters",DetectorType.FTHODO);
        eb.addForwardTaggerParticles(ftparticles);
        eb.addTaggerResponses(responseFTCAL);
        eb.addTaggerResponses(responseFTHODO);
        eb.addFTIndices(ftIndices);
        eb.forwardTaggerIDMatching();


        // create REC:detector banks:
        if(eb.getEvent().getParticles().size()>0){
        
            eb.setParticleStatuses();
            //eb.setEventStatuses();
            
            DataBank bankP = DetectorData.getDetectorParticleBank(eb.getEvent().getParticles(), de, particleBank);
            de.appendBanks(bankP);
          
            DataBank bankEve = DetectorData.getEventBank(eb.getEvent(), de, eventBank);
            de.appendBanks(bankEve);

            List<DetectorResponse> calorimeters = eb.getEvent().getCalorimeterResponseList();
            if(calorimeterBank!=null && calorimeters.size()>0) {
                DataBank bankCal = DetectorData.getCalorimeterResponseBank(calorimeters, de, calorimeterBank);
                de.appendBanks(bankCal);
            }
            List<DetectorResponse> scintillators = eb.getEvent().getScintillatorResponseList();
            if(scintillatorBank!=null && scintillators.size()>0) {
                DataBank bankSci = DetectorData.getScintillatorResponseBank(scintillators, de, scintillatorBank);
                de.appendBanks(bankSci);               
            }
            List<CherenkovResponse> cherenkovs = eb.getEvent().getCherenkovResponseList();
            if(cherenkovBank!=null && cherenkovs.size()>0) {
                DataBank bankChe = DetectorData.getCherenkovResponseBank(cherenkovs, de, cherenkovBank);
                de.appendBanks(bankChe);
            }
            
            List<TaggerResponse> taggers = eb.getEvent().getTaggerResponseList();
            if (ftBank!=null && taggers.size()>0) {
                DataBank bankForwardTagger = DetectorData.getForwardTaggerBank(eb.getEvent().getTaggerResponseList(), de, ftBank);
                de.appendBanks(bankForwardTagger);
            }

            if (trackBank!=null && (tracks.size()>0 || ctracks.size()>0) ) {
                final int ntracks = tracks.size() + ctracks.size();
                DataBank bankTrack = DetectorData.getTracksBank(eb.getEvent().getParticles(), de, trackBank, ntracks);
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

    public void dropBanks(DataEvent de) {
        if (this.alreadyDroppedBanks==false) {
            System.out.println("\nEBEngine:  dropping REC banks!\n");
            this.alreadyDroppedBanks=true;
        }
        de.removeBank(eventBank);
        de.removeBank(particleBank);
        de.removeBank(calorimeterBank);
        de.removeBank(scintillatorBank);
        de.removeBank(cherenkovBank);
        de.removeBank(trackBank);
        de.removeBank(crossBank);
        de.removeBank(ftBank);
    }

    @Override
    public boolean init() {
        requireConstants(EBCCDBConstants.getAllTableNames());
        this.getConstantsManager().setVariation("default");
        System.out.println("[EB::] --> event builder is ready....");
        return true;
    }

    public boolean init(int run) {
        this.init();
        EBCCDBConstants.load(run,this.getConstantsManager());
        return true;
    }
    
}
