package org.jlab.service.eb;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.clas.detector.*;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.clas.detector.CherenkovResponse;
import org.jlab.rec.eb.EBCCDBConstants;
import org.jlab.rec.eb.EBCCDBEnum;
import org.jlab.rec.eb.EBScalers;
import org.jlab.rec.eb.EBRadioFrequency;

/**
 *
 * @author gavalian
 *@author jnewton
 *@author baltzell
 */
public class EBEngine extends ReconstructionEngine {

    public static final Logger LOGGER = Logger.getLogger(EBEngine.class.getName());

    boolean usePOCA = false;

    // output banks:
    String eventBank        = null;
    String eventBankFT      = null;
    String particleBank     = null;
    String particleBankFT   = null;
    String calorimeterBank  = null;
    String caloextrasBank   = null;
    String scintillatorBank = null;
    String scintextrasBank  = null;
    String cherenkovBank    = null;
    String trackBank        = null;
    String utrackBank       = null;
    String crossBank        = null;
    String ftBank           = null;
    String trajectoryBank   = null;
    String covMatrixBank    = null;
    
    // inputs banks:
    String trackType        = null;
    String ftofHitsType     = null;
    String trajectoryType   = null;
    String covMatrixType    = null;
    String cvtTrackType     = null;
    String cvtTrajType      = null;
    
    public EBEngine(String name){
        super(name,"gavalian","1.0");
        initBankNames();
    }

    public void initBankNames() {
        //Initialize bank names
    }

    public void setUsePOCA(boolean val) {
        this.usePOCA=val;
    }

    @Override
    public boolean processDataEvent(DataEvent de) {
        throw new RuntimeException("EBEngine cannot be used directly.  Use EBTBEngine/EBHBEngine instead.");
    }

    public void setOutputBankPrefix(String prefix) {
        this.setEventBank(prefix+"::Event");
        this.setParticleBank(prefix+"::Particle");
        this.setCalorimeterBank(prefix+"::Calorimeter");
        this.setCaloExtrasBank(prefix+"::CaloExtras");
        this.setCherenkovBank(prefix+"::Cherenkov");
        this.setScintillatorBank(prefix+"::Scintillator");
        this.setScintExtrasBank(prefix+"::ScintExtras");
        this.setTrackBank(prefix+"::Track");
        this.setUTrackBank(prefix+"::UTrack");
        this.setCrossBank(prefix+"::TrackCross");
        this.setTrajectoryBank(prefix+"::Traj");
        this.setFTBank(prefix+"::ForwardTagger");
        if (!this.getClass().isAssignableFrom(EBHBEngine.class) &&
            !this.getClass().isAssignableFrom(EBHBAIEngine.class)) {
            this.setEventBankFT(prefix+"FT::Event");
            this.setParticleBankFT(prefix+"FT::Particle");
            this.setCovMatrixBank(prefix+"::CovMat");
        }
    }

    public boolean processDataEvent(DataEvent de,EBScalers ebs) {

        // check run number, get constants from CCDB:
        int run=-1;
        if (de.hasBank("RUN::config")) {
            run=de.getBank("RUN::config").getInt("run",0);
        }

        EBCCDBConstants ccdb = new EBCCDBConstants(run,this.getConstantsManager());

        DetectorHeader head = EBio.readHeader(de,ebs,ccdb);

        EventBuilder eb = new EventBuilder(ccdb);
        eb.setUsePOCA(this.usePOCA);
        eb.initEvent(head); // clear particles

        EBMatching ebm = new EBMatching(eb);
        
        // Process RF:
        EBRadioFrequency rf = new EBRadioFrequency(ccdb);
        eb.getEvent().getEventHeader().setRfTime(rf.getTime(de)+ccdb.getDouble(EBCCDBEnum.RF_OFFSET));
        
        List<DetectorResponse> responseECAL = CalorimeterResponse.readHipoEvent(de, "ECAL::clusters", DetectorType.ECAL,"ECAL::moments", "ECAL::calib");
        List<DetectorResponse> responseFTOF = ScintillatorResponse.readHipoEvent(de, ftofHitsType, DetectorType.FTOF);
        List<DetectorResponse> responseCTOF = ScintillatorResponse.readHipoEvent(de, "CTOF::clusters", DetectorType.CTOF);
        List<DetectorResponse> responseCND  = ScintillatorResponse.readHipoEvent(de, "CND::clusters", DetectorType.CND);
        List<DetectorResponse> responseBAND = ScintillatorResponse.readHipoEvent(de, "BAND::hits", DetectorType.BAND);
        List<DetectorResponse> responseHTCC = CherenkovResponse.readHipoEvent(de,"HTCC::rec",DetectorType.HTCC);
        List<DetectorResponse> responseLTCC = CherenkovResponse.readHipoEvent(de,"LTCC::clusters",DetectorType.LTCC);

        eb.addDetectorResponses(responseFTOF);
        eb.addDetectorResponses(responseCTOF);
        eb.addDetectorResponses(responseCND);
        eb.addDetectorResponses(responseBAND);
        eb.addDetectorResponses(responseECAL);
        eb.addDetectorResponses(responseHTCC);
        eb.addDetectorResponses(responseLTCC);

        // Add tracks
        List<DetectorTrack>  tracks = DetectorData.readDetectorTracks(de, trackType, trajectoryType, covMatrixType);
        eb.addTracks(tracks);      
        
        List<DetectorTrack> ctracks = DetectorData.readCentralDetectorTracks(de, cvtTrackType, cvtTrajType);
        eb.addTracks(ctracks);
        
        List<DetectorTrack> cutracks = DetectorData.readCentralDetectorTracks(de, "CVTRec::UTracks", cvtTrajType);
       
        // FIXME:  remove need for these indexing bookkeepers:
        eb.getPindexMap().put(0, tracks.size());
        eb.getPindexMap().put(1, ctracks.size());
        
        // Process tracks-hit matching:
        eb.processHitMatching();

        // Assign trigger/startTime particle: 
        eb.assignTrigger();
 
        // Make neutrals after assigning trigger particle, to get vertex/momentum right:

        // Create forward neutrals:
        eb.processNeutralTracks();

        // Create central neutrals:
        ebm.addCentralNeutrals(eb.getEvent());

        // Add BAND particles:
        eb.processBAND(responseBAND);

        // Set the statuses before particle identification:
        eb.setParticleStatuses();

        // Do PID etc:
        EBAnalyzer analyzer = new EBAnalyzer(ccdb,rf);
        analyzer.processEvent(eb.getEvent());

        // Add Forward Tagger particles:
        eb.processForwardTagger(de);

        // create REC:detector banks:
        if(!eb.getEvent().getParticles().isEmpty()){
       
            eb.getEvent().sort();
            eb.setParticleStatuses();
            //eb.setEventStatuses();
            
            DataBank bankP = DetectorData.getDetectorParticleBank(eb.getEvent().getParticles(), de, particleBank);
            de.appendBanks(bankP);
          
            DataBank bankEve = DetectorData.getEventBank(eb.getEvent(), de, eventBank);
            de.appendBanks(bankEve);

            List<DetectorResponse> calorimeters = eb.getEvent().getCalorimeterResponseList();
            if(calorimeterBank!=null && !calorimeters.isEmpty()) {
                DataBank bankCal = DetectorData.getCalorimeterResponseBank(calorimeters, de, calorimeterBank);
                de.appendBanks(bankCal);
                DataBank bankCaloExtras = DetectorData.getCaloExtrasResponseBank(calorimeters, de, caloextrasBank);
                de.appendBanks(bankCaloExtras);               
            }
            List<DetectorResponse> scintillators = eb.getEvent().getScintillatorResponseList();
            if(scintillatorBank!=null && !scintillators.isEmpty()) {
                DataBank bankSci = DetectorData.getScintillatorResponseBank(scintillators, de, scintillatorBank);
                de.appendBanks(bankSci);               
                DataBank eaxtbankSci = DetectorData.getScintExtrasResponseBank(scintillators, de, scintextrasBank);
                de.appendBanks(eaxtbankSci);               
            }
            List<DetectorResponse> cherenkovs = eb.getEvent().getCherenkovResponseList();
            if(cherenkovBank!=null && !cherenkovs.isEmpty()) {
                DataBank bankChe = DetectorData.getCherenkovResponseBank(cherenkovs, de, cherenkovBank);
                de.appendBanks(bankChe);
            }
            
            List<DetectorResponse> taggers = eb.getEvent().getTaggerResponseList();
            if (ftBank!=null && !taggers.isEmpty()) {
                DataBank bankForwardTagger = DetectorData.getForwardTaggerBank(taggers, de, ftBank);
                de.appendBanks(bankForwardTagger);
            }

            if (trackBank!=null && (!tracks.isEmpty() || !ctracks.isEmpty()) ) {
                final int ntracks = tracks.size() + ctracks.size();
                DataBank bankTrack = DetectorData.getTracksBank(eb.getEvent().getParticles(), de, trackBank, ntracks);
                de.appendBanks(bankTrack);
                DataBank bankTraj  = DetectorData.getTrajectoriesBank(eb.getEvent().getParticles(), de, trajectoryBank);
                if (bankTraj != null) de.appendBanks(bankTraj);
                DataBank bankCovMat = DetectorData.getCovMatrixBank(eb.getEvent().getParticles(), de, covMatrixBank);
                if (bankCovMat != null) de.appendBanks(bankCovMat);

                if (!cutracks.isEmpty()) {
                    DataBank x = DetectorData.getUTracksBank(cutracks, ctracks, de, utrackBank);
                    de.appendBanks(x);
                }
            }
      
            // update PID for FT-based start time:
            // WARNING:  this modified particles
            analyzer.processEventFT(eb.getEvent());
            if (eb.getEvent().getEventHeader().getStartTimeFT()>0 && eventBankFT!=null) {
                DataBank bankEveFT = DetectorData.getEventShadowBank(eb.getEvent(), de, eventBankFT);
                de.appendBanks(bankEveFT);
                if (particleBankFT!=null) {
                    DataBank bankPFT = DetectorData.getDetectorParticleShadowBank(eb.getEvent().getParticles(), de, particleBankFT);
                    de.appendBanks(bankPFT);
                }
            }
          
        }

        return true;
    }

    public void setEventBank(String name) {
        this.eventBank = name;
    }

    public void setParticleBank(String name) {
        this.particleBank = name;
    }

    public void setEventBankFT(String name) {
        this.eventBankFT = name;
    }

    public void setParticleBankFT(String name) {
        this.particleBankFT = name;
    }
    
    public void setCalorimeterBank(String name) {
        this.calorimeterBank = name;
    }

    public void setScintillatorBank(String name) {
        this.scintillatorBank = name;
    }

    public void setScintExtrasBank(String name) {
        this.scintextrasBank = name;
    }

    public void setCaloExtrasBank(String name) {
        this.caloextrasBank = name;
    }

    public void setCherenkovBank(String name) {
        this.cherenkovBank = name;
    }

    public void setTrackBank(String name) {
        this.trackBank = name;
    }
    
    public void setUTrackBank(String name) {
        this.utrackBank = name;
    }
    
    public void setFTBank(String name) {
        this.ftBank = name;
    }

    public void setCrossBank(String name) {
        this.crossBank = name;
    }

    public void setTrajectoryBank(String name) {
        this.trajectoryBank = name;
    }
    
    public void setCovMatrixBank(String name) {
        this.covMatrixBank = name;
    }
    
    public void setTrackType(String name) {
        this.trackType = name;
    }
    
    public void setFTOFHitsType(String name) {
        this.ftofHitsType = name;
    }

    public void setCovMatrixType(String name) {
        this.covMatrixType = name;
    }

    public void setTrajectoryType(String name) {
        this.trajectoryType = name;
    }

    public void setCvtTrackType(String name) {
        this.cvtTrackType = name;
    }
    
    public void setCvtTrajType(String name) {
        this.cvtTrajType = name;
    }
    
    @Override
    public boolean init() {

        this.registerOutputBank(eventBank);
        this.registerOutputBank(particleBank);
        this.registerOutputBank(eventBankFT);
        this.registerOutputBank(particleBankFT);
        this.registerOutputBank(calorimeterBank);
        this.registerOutputBank(caloextrasBank);
        this.registerOutputBank(scintillatorBank);
        this.registerOutputBank(scintextrasBank);
        this.registerOutputBank(cherenkovBank);
        this.registerOutputBank(trackBank);
        this.registerOutputBank(crossBank);
        this.registerOutputBank(ftBank);
        this.registerOutputBank(trajectoryBank);
        this.registerOutputBank(covMatrixBank);

	    if (this.getEngineConfigString("outputBankPrefix")!=null) {
	        this.setOutputBankPrefix(this.getEngineConfigString("outputBankPrefix"));
        }

        requireConstants(EBCCDBConstants.getAllTableNames());

        this.getConstantsManager().setVariation("default");

        return true;
    }
    
}
