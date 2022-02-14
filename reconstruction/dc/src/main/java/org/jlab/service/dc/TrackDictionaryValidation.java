package org.jlab.service.dc;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.base.DetectorType;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.utils.groups.IndexedList;

import org.jlab.utils.options.OptionParser;

public class TrackDictionaryValidation {

    private Map<ArrayList<Byte>, Particle>   dictionary = null;
    private final IndexedList<DataGroup>     dataGroups = new IndexedList<DataGroup>(1);
    private final EmbeddedCanvasTabbed       canvas     = new EmbeddedCanvasTabbed("Dictionary", "Matched Roads", "Matched Tracks", "Efficiency");
    public static Logger LOGGER = Logger.getLogger(TrackDictionaryValidation.class.getName());
            
    public TrackDictionaryValidation(){

    }

    public void analyzeHistos() {
        // calculate road finding efficiencies
        this.effHisto(this.dataGroups.getItem(2).getH2F("hi_ptheta_pos_found"), 
                      this.dataGroups.getItem(2).getH2F("hi_ptheta_pos_missing"), 
                      this.dataGroups.getItem(3).getH2F("hi_ptheta_pos_eff"));
        this.effHisto(this.dataGroups.getItem(2).getH2F("hi_phitheta_pos_found"), 
                      this.dataGroups.getItem(2).getH2F("hi_phitheta_pos_missing"), 
                      this.dataGroups.getItem(3).getH2F("hi_phitheta_pos_eff"));
        this.effHisto(this.dataGroups.getItem(2).getH2F("hi_ptheta_neg_found"), 
                      this.dataGroups.getItem(2).getH2F("hi_ptheta_neg_missing"), 
                      this.dataGroups.getItem(3).getH2F("hi_ptheta_neg_eff"));
        this.effHisto(this.dataGroups.getItem(2).getH2F("hi_phitheta_neg_found"), 
                      this.dataGroups.getItem(2).getH2F("hi_phitheta_neg_missing"), 
                      this.dataGroups.getItem(3).getH2F("hi_phitheta_neg_eff"));
        LOGGER.log(Level.INFO, "Positive particles found/missed: " + this.dataGroups.getItem(2).getH2F("hi_ptheta_pos_found").integral() + "/" +
                                                               + this.dataGroups.getItem(2).getH2F("hi_ptheta_pos_missing").integral());
        LOGGER.log(Level.INFO, "Negative particles found/missed: " + this.dataGroups.getItem(2).getH2F("hi_ptheta_neg_found").integral() + "/" +
                                                               + this.dataGroups.getItem(2).getH2F("hi_ptheta_neg_missing").integral());
    }
    
    /**
     * create dictionary from event file
     * @param inputFileName: input hipo file name
     * @param dictName: output dictionary file name
     * @param pidSelect: PID for track selection
     * @param chargeSelect: charge for track selection
     * @param thrs: momentum threshold for track selection
     * @param duplicates: remove duplicate roads (0/1)
     */
    public void createDictionary(String inputFileName, String dictName , int pidSelect, int chargeSelect, double thrs, int duplicates) {
        // create dictionary from event file
        LOGGER.log(Level.INFO, "\nCreating dictionary from file: " + inputFileName);
        Map<ArrayList<Integer>, Particle> newDictionary = new HashMap<>();
        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFileName);
        String[] tokens = inputFileName.split("/");
        LOGGER.log(Level.INFO, "\nDictionary will be saved to: " + dictName); 
        int nevent = -1;
        while(reader.hasEvent() == true) {
            DataEvent event = reader.getNextEvent();
            nevent++;
            if(nevent%10000 == 0) LOGGER.log(Level.INFO, "Analyzed " + nevent + " events, found " + newDictionary.size() + " roads");
            DataBank runConfig       = null;
            DataBank recParticle     = null;
            DataBank recCalorimeter  = null;
            DataBank recScintillator = null;
            DataBank recCherenkov    = null;
            DataBank recTrack        = null;
            DataBank tbtTrack        = null;
            DataBank ecalCluster     = null;
            DataBank tbtHits         = null;
            DataBank htccRec         = null;
            DataBank htccADC         = null;
            if (event.hasBank("RUN::config")) {
                runConfig = event.getBank("RUN::config");
            }            
            if (event.hasBank("REC::Particle")) {
                recParticle = event.getBank("REC::Particle");
            }
            if (event.hasBank("REC::Scintillator")) {
                recScintillator = event.getBank("REC::Scintillator");
            }
            if (event.hasBank("REC::Calorimeter")) {
                recCalorimeter = event.getBank("REC::Calorimeter");
            }
            if (event.hasBank("REC::Cherenkov")) {
                recCherenkov = event.getBank("REC::Cherenkov");
            }
            if (event.hasBank("REC::Track")) {
                recTrack = event.getBank("REC::Track");
            }
            if (event.hasBank("TimeBasedTrkg::TBTracks")) {
                tbtTrack = event.getBank("TimeBasedTrkg::TBTracks");
            }
            if (event.hasBank("TimeBasedTrkg::TBHits")) {
                tbtHits = event.getBank("TimeBasedTrkg::TBHits");
            }
            if (event.hasBank("ECAL::clusters")) {
                ecalCluster = event.getBank("ECAL::clusters");
            }
            if (event.hasBank("HTCC::rec")) {
                htccRec = event.getBank("HTCC::rec");
            }
            if (event.hasBank("HTCC::adc")) {
                htccADC = event.getBank("HTCC::adc");
            }
            // add other banks
            if(recParticle!=null && recTrack != null && tbtTrack != null && tbtHits != null) {
                for (int i = 0; i < recTrack.rows(); i++) {
                    // start from tracks
                    int index    = recTrack.getShort("index", i);
                    int pindex   = recTrack.getShort("pindex", i);
                    int detector = recTrack.getByte("detector", i);
                    // use only forward tracks
                    if(detector == DetectorType.DC.getDetectorId()) {
                        int charge   = recParticle.getByte("charge", pindex);
                        int pid      = recParticle.getInt("pid", pindex);
                        // if charge or pid selectrion are set, then compare to the current track values
                        if(chargeSelect != 0 && chargeSelect!=charge) continue;
                        if(pidSelect != 0    && pidSelect!=pid)       continue;   
                        // save particle information
                        Particle part = new Particle(
                                        -11*charge,
                                        recParticle.getFloat("px", pindex),
                                        recParticle.getFloat("py", pindex),
                                        recParticle.getFloat("pz", pindex),
                                        recParticle.getFloat("vx", pindex),
                                        recParticle.getFloat("vy", pindex),
                                        recParticle.getFloat("vz", pindex)); 
                        if(part.p()<thrs) continue; //use only roads with momentum above the selected value
                        // get the DC wires' IDs
                        int trackSector = 0;
                        int[] wireArray = new int[36];
                        for (int j = 0; j < tbtHits.rows(); j++) {
                            if (tbtHits.getByte("trkID", j) == tbtTrack.getShort("id", index)) {
                                trackSector = tbtHits.getByte("sector", j);
                                int superlayer = tbtHits.getByte("superlayer", j);
                                int layer = tbtHits.getByte("layer", j);
                                int wire = tbtHits.getShort("wire", j);
                                wireArray[(superlayer - 1) * 6 + layer - 1] = wire;
                            }
                        }
                        ArrayList<Integer> wires = new ArrayList<>();
                        for (int k = 0; k < 6; k++) {
                            for (int l=0; l<6; l++) {
                                // use first non zero wire in superlayer
                                if(wireArray[k*6 +l] != 0) {
                                   wires.add(wireArray[k*6+l]);
                                   break;
                                }
                            }
                        }
                        // keep only roads with 6 superlayers
                        if(wires.size()==6) {
                            // now check other detectors
                            int paddle1b = 0;
                            int paddle2  = 0;
                            int pcalU    = 0;
                            int pcalV    = 0;
                            int pcalW    = 0;
                            int htcc     = 0;
                            double pcalE = 0;
                            double ecinE = 0;
                            double ecoutE= 0;                             // check FTOF
                            if(recScintillator!=null) {
                                for(int j=0; j<recScintillator.rows(); j++) {
                                    if(recScintillator.getShort("pindex",j) == pindex) {
                                        int detectorScint  = recScintillator.getByte("detector", j);
                                        int layerScint     = recScintillator.getByte("layer",j);
                                        int componentScint = recScintillator.getShort("component",j);
                                        if(detectorScint==DetectorType.FTOF.getDetectorId() && layerScint==2) paddle1b = componentScint;
                                        if(detectorScint==DetectorType.FTOF.getDetectorId() && layerScint==3) paddle2  = componentScint;
                                    }
                                }
                            }
                            // check ECAL
                            if(recCalorimeter!=null && ecalCluster!=null) {
                                for(int j=0; j<recCalorimeter.rows(); j++) {
                                    if(recCalorimeter.getShort("pindex",j) == pindex) {
                                        int detectorClus = recCalorimeter.getByte("detector", j);
                                        int indexClus    = recCalorimeter.getShort("index",j);
                                        int layerClus    = recCalorimeter.getByte("layer",j);
                                        double energy    = recCalorimeter.getFloat("energy",j);
                                        // use pcal only
                                        if(detectorClus==DetectorType.ECAL.getDetectorId() && layerClus==1) {
                                            pcalU = (ecalCluster.getInt("coordU",indexClus)-4)/8+1;
                                            pcalV = (ecalCluster.getInt("coordV",indexClus)-4)/8+1;
                                            pcalW = (ecalCluster.getInt("coordW",indexClus)-4)/8+1;
                                            pcalE =  energy;
//                                            LOGGER.log(Level.INFO, pcalU + " " + pcalV + " " + pcalW);
                                        }
                                        else if(detectorClus==DetectorType.ECAL.getDetectorId() && layerClus==4) {
                                            ecinE = energy;
                                        }
                                        else if(detectorClus==DetectorType.ECAL.getDetectorId() && layerClus==7) {
                                            ecoutE = energy;
                                        }
                                    }
                                }
                            }
                           // check HTCC
                            if (recCherenkov != null && htccRec != null && htccADC != null && false) {
                                int htcc_event;
//                                recCherenkov.show(); htccADC.show();
                                for (int j = 0; j < recCherenkov.rows(); j++) {
                                    if (recCherenkov.getShort("pindex", j) == pindex) {
                                        int detectorCheren = recCherenkov.getByte("detector", j);
                                        if (detectorCheren == DetectorType.HTCC.getDetectorId()) {
                                            int nhits = htccRec.getShort("nhits",recCherenkov.getShort("index", j));
                                            double x = recCherenkov.getFloat("x", j);
                                            double y = recCherenkov.getFloat("y", j);
                                            double z = recCherenkov.getFloat("z", j);                                            
                                            double thetaCheren = Math.acos(z/Math.sqrt(x*x+y*y+z*z));
                                            double phiCheren   = Math.atan2(y, x);
                                            thetaCheren = Math.toDegrees(thetaCheren);
                                            phiCheren   = Math.toDegrees(phiCheren );
                                            double phiCC   = Math.round(phiCheren); 
                                            if(phiCC<0) phiCC +=360;
                                            double thetaCC = ((double) Math.round(thetaCheren*100))/100.;
                                            ArrayList<int[]> htccPMTs        = htccPMT(thetaCC, phiCC);
                                            ArrayList<int[]> htccPMTsMatched = new ArrayList<>();
//                                            LOGGER.log(Level.INFO, thetaCheren + " " + thetaCC + " " + phiCheren + " " + phiCC + " " + htccPMTs.size());
                                            //The special case of 4 hits, where we need to check if the hits were not in fact only 3
                                            for(int iPMT = 0; iPMT < htccPMTs.size(); iPMT++) {
                                                int htccSector    = htccPMTs.get(iPMT)[0];
                                                int htccLayer     = htccPMTs.get(iPMT)[1];
                                                int htccComponent = htccPMTs.get(iPMT)[2];
                                                boolean found = false;
//                                                LOGGER.log(Level.INFO, iPMT + " " + htccSector + " " + htccLayer + " " + htccComponent);
                                                for (int k = 0; k < htccADC.rows(); k++) {
                                                    int sector    = htccADC.getByte("sector", k);
                                                    int layer     = htccADC.getByte("layer", k);
                                                    int component = htccADC.getShort("component", k);
//                                                    LOGGER.log(Level.INFO, k + " " + sector + " " + layer + " " + component);
                                                    if( htccSector    == sector && 
                                                        htccLayer     == layer && 
                                                        htccComponent == component) {
                                                        found = true;
//                                                        LOGGER.log(Level.INFO, "Found match in adc bank");
                                                    }                                                  
                                                }
                                                if(found) {
                                                    htccPMTsMatched.add(htccPMTs.get(iPMT));
                                                }
//                                                else {
//                                                    LOGGER.log(Level.INFO, "Removing hit " + iPMT + " among " + htccPMTs.size() + " " + thetaCC + " " +  phiCC + " " + htccSector + "/" + htccLayer+ "/"+htccComponent);
//                                                    runConfig.show();recCherenkov.show(); htccRec.show();htccADC.show();
//                                                }
                                                }
                                            if(htccPMTsMatched.size() != nhits) {
                                                LOGGER.log(Level.INFO, "Mismatch in HTCC cluster size " +  runConfig.getInt("event",0) + " " + nhits +"/"+htccPMTsMatched.size()+"/"+htccPMTs.size() + " " + thetaCC + " " +  phiCC + " " +((phiCC+30)%60-30));
//                                                for(int iPMT = 0; iPMT < htccPMTs.size(); iPMT++) {
//                                                    int htccSector    = htccPMTs.get(iPMT)[0];
//                                                    int htccLayer     = htccPMTs.get(iPMT)[1];
//                                                    int htccComponent = htccPMTs.get(iPMT)[2];
//                                                    LOGGER.log(Level.INFO, iPMT + " " + htccSector + " " + htccLayer + " " + htccComponent);
//                                                }
//                                                for (int k = 0; k < htccADC.rows(); k++) {
//                                                    int sector    = htccADC.getByte("sector", k);
//                                                    int layer     = htccADC.getByte("layer", k);
//                                                    int component = htccADC.getShort("component", k);
//                                                    LOGGER.log(Level.INFO, k + " " + sector + " " + layer + " " + component);
//                                                }
                                            }
                                            htcc = this.htccMask(htccPMTsMatched);
                                        }
                                    }
                                }
                            }
                            wires.add(paddle1b);
                            wires.add(paddle2);
                            wires.add(pcalU);
                            wires.add(pcalV);
                            wires.add(pcalW);
                            wires.add(htcc);
                            wires.add(trackSector);
                            part.setProperty("pcalE",  pcalE);
                            part.setProperty("ecinE",  ecinE);
                            part.setProperty("ecoutE", ecoutE);
                            if(duplicates==1) wires.add(nevent);
                            // save roads to map
                            if(!newDictionary.containsKey(wires))  {
                                newDictionary.put(wires, part);
                            }   
                        }
                    }
                }
            }
        }
        LOGGER.log(Level.INFO, "Analyzed " + nevent + " events, found " + newDictionary.size() + " roads");
        this.writeDictionary(newDictionary, dictName);
    }
    
    public void createHistos() {
        // roads in dictionary
        H2F hi_ptheta_neg_road = new H2F("hi_ptheta_neg_road", "hi_ptheta_neg_road", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_neg_road.setTitleX("p (GeV)");
        hi_ptheta_neg_road.setTitleY("#theta (deg)");
        H2F hi_phitheta_neg_road = new H2F("hi_phitheta_neg_road", "hi_phitheta_neg_road", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_neg_road.setTitleX("#phi (deg)");
        hi_phitheta_neg_road.setTitleY("#theta (deg)");
        H2F hi_vztheta_neg_road = new H2F("hi_vztheta_neg_road", "hi_vztheta_neg_road", 100, -15, 15, 100, 0.0, 65.0);     
        hi_vztheta_neg_road.setTitleX("vz (cm)");
        hi_vztheta_neg_road.setTitleY("#theta (deg)");
        H2F hi_ftofdc_neg_road = new H2F("hi_ftofdc_neg_road", "hi_ftofdc_neg_road", 120, 0.0, 120.0, 70, 0.0, 70.0);       
        hi_ftofdc_neg_road.setTitleX("DC-R3 wire");
        hi_ftofdc_neg_road.setTitleY("FTOF paddle");
        H2F hi_pcalftof_neg_road = new H2F("hi_pcalftof_neg_road", "hi_pcalftof_neg_road", 65, 0.0, 65.0, 70, 0.0, 70.0);      
        hi_pcalftof_neg_road.setTitleX("FTOF paddle");
        hi_pcalftof_neg_road.setTitleY("PCAL strip");
        H2F hi_ptheta_pos_road = new H2F("hi_ptheta_pos_road", "hi_ptheta_pos_road", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_pos_road.setTitleX("p (GeV)");
        hi_ptheta_pos_road.setTitleY("#theta (deg)");
        H2F hi_phitheta_pos_road = new H2F("hi_phitheta_pos_road", "hi_phitheta_pos_road", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_pos_road.setTitleX("#phi (deg)");
        hi_phitheta_pos_road.setTitleY("#theta (deg)");
        H2F hi_vztheta_pos_road = new H2F("hi_vztheta_pos_road", "hi_vztheta_pos_road", 100, -15, 15, 100, 0.0, 65.0);     
        hi_vztheta_pos_road.setTitleX("vz (cm)");
        hi_vztheta_pos_road.setTitleY("#theta (deg)");
        H2F hi_ftofdc_pos_road = new H2F("hi_ftofdc_pos_road", "hi_ftofdc_pos_road", 120, 0.0, 120.0, 70, 0.0, 70.0);       
        hi_ftofdc_pos_road.setTitleX("DC-R3 wire");
        hi_ftofdc_pos_road.setTitleY("FTOF paddle");
        H2F hi_pcalftof_pos_road = new H2F("hi_pcalftof_pos_road", "hi_pcalftof_pos_road", 65, 0.0, 65.0, 70, 0.0, 70.0);       
        hi_pcalftof_pos_road.setTitleX("FTOF paddle");
        hi_pcalftof_pos_road.setTitleY("PCAL strip");
        DataGroup dRoads  = new DataGroup(5,2);
        dRoads.addDataSet(hi_ptheta_neg_road,   0);
        dRoads.addDataSet(hi_phitheta_neg_road, 1);
        dRoads.addDataSet(hi_vztheta_neg_road,  2);
        dRoads.addDataSet(hi_ftofdc_neg_road,   3);
        dRoads.addDataSet(hi_pcalftof_neg_road, 4);
        dRoads.addDataSet(hi_ptheta_pos_road,   5);
        dRoads.addDataSet(hi_phitheta_pos_road, 6);
        dRoads.addDataSet(hi_vztheta_pos_road,  7);
        dRoads.addDataSet(hi_ftofdc_pos_road,   8);
        dRoads.addDataSet(hi_pcalftof_pos_road, 9);
        this.dataGroups.add(dRoads, 0);
        // matched roads
        H2F hi_ptheta_neg_matchedroad = new H2F("hi_ptheta_neg_matchedroad", "hi_ptheta_neg_matchedroad", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_neg_matchedroad.setTitleX("p (GeV)");
        hi_ptheta_neg_matchedroad.setTitleY("#theta (deg)");
        H2F hi_phitheta_neg_matchedroad = new H2F("hi_phitheta_neg_matchedroad", "hi_phitheta_neg_matchedroad", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_neg_matchedroad.setTitleX("#phi (deg)");
        hi_phitheta_neg_matchedroad.setTitleY("#theta (deg)");
        H2F hi_vztheta_neg_matchedroad = new H2F("hi_vztheta_neg_matchedroad", "hi_vztheta_neg_matchedroad", 100, -15, 15, 100, 0.0, 65.0);     
        hi_vztheta_neg_matchedroad.setTitleX("vz (cm)");
        hi_vztheta_neg_matchedroad.setTitleY("#theta (deg)");
        H2F hi_ptheta_pos_matchedroad = new H2F("hi_ptheta_pos_matchedroad", "hi_ptheta_pos_matchedroad", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_pos_matchedroad.setTitleX("p (GeV)");
        hi_ptheta_pos_matchedroad.setTitleY("#theta (deg)");
        H2F hi_phitheta_pos_matchedroad = new H2F("hi_phitheta_pos_matchedroad", "hi_phitheta_pos_matchedroad", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_pos_matchedroad.setTitleX("#phi (deg)");
        hi_phitheta_pos_matchedroad.setTitleY("#theta (deg)");
        H2F hi_vztheta_pos_matchedroad = new H2F("hi_vztheta_pos_matchedroad", "hi_vztheta_pos_matchedroad", 100, -15, 15, 100, 0.0, 65.0);     
        hi_vztheta_pos_matchedroad.setTitleX("vz (cm)");
        hi_vztheta_pos_matchedroad.setTitleY("#theta (deg)");
        DataGroup dMatchedRoads  = new DataGroup(3,2);
        dMatchedRoads.addDataSet(hi_ptheta_neg_matchedroad,   0);
        dMatchedRoads.addDataSet(hi_phitheta_neg_matchedroad, 1);
        dMatchedRoads.addDataSet(hi_vztheta_neg_matchedroad,  2);
        dMatchedRoads.addDataSet(hi_ptheta_pos_matchedroad,   3);
        dMatchedRoads.addDataSet(hi_phitheta_pos_matchedroad, 4);
        dMatchedRoads.addDataSet(hi_vztheta_pos_matchedroad,  5);
        this.dataGroups.add(dMatchedRoads, 1);        
        // negative tracks
        H2F hi_ptheta_neg_found = new H2F("hi_ptheta_neg_found", "hi_ptheta_neg_found", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_neg_found.setTitleX("p (GeV)");
        hi_ptheta_neg_found.setTitleY("#theta (deg)");
        H2F hi_ptheta_neg_missing = new H2F("hi_ptheta_neg_missing", "hi_ptheta_neg_missing", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_neg_missing.setTitleX("p (GeV)");
        hi_ptheta_neg_missing.setTitleY("#theta (deg)");
        H2F hi_phitheta_neg_found = new H2F("hi_phitheta_neg_found", "hi_phitheta_neg_found", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_neg_found.setTitleX("#phi (deg)");
        hi_phitheta_neg_found.setTitleY("#theta (deg)");
        H2F hi_phitheta_neg_missing = new H2F("hi_phitheta_neg_missing", "hi_phitheta_neg_missing", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_neg_missing.setTitleX("#phi (deg)");
        hi_phitheta_neg_missing.setTitleY("#theta (deg)");
        // positive tracks
        H2F hi_ptheta_pos_found = new H2F("hi_ptheta_pos_found", "hi_ptheta_pos_found", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_pos_found.setTitleX("p (GeV)");
        hi_ptheta_pos_found.setTitleY("#theta (deg)");
        H2F hi_ptheta_pos_missing = new H2F("hi_ptheta_pos_missing", "hi_ptheta_pos_missing", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_pos_missing.setTitleX("p (GeV)");
        hi_ptheta_pos_missing.setTitleY("#theta (deg)");
        H2F hi_phitheta_pos_found = new H2F("hi_phitheta_pos_found", "hi_phitheta_pos_found", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_pos_found.setTitleX("#phi (deg)");
        hi_phitheta_pos_found.setTitleY("#theta (deg)");
        H2F hi_phitheta_pos_missing = new H2F("hi_phitheta_pos_missing", "hi_phitheta_pos_missing", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_pos_missing.setTitleX("#phi (deg)");
        hi_phitheta_pos_missing.setTitleY("#theta (deg)");
        DataGroup dMatches  = new DataGroup(4,2);
        dMatches.addDataSet(hi_ptheta_neg_found,     0);
        dMatches.addDataSet(hi_ptheta_neg_missing,   1);
        dMatches.addDataSet(hi_phitheta_neg_found,   2);
        dMatches.addDataSet(hi_phitheta_neg_missing, 3);
        dMatches.addDataSet(hi_ptheta_pos_found,     4);
        dMatches.addDataSet(hi_ptheta_pos_missing,   5);
        dMatches.addDataSet(hi_phitheta_pos_found,   6);
        dMatches.addDataSet(hi_phitheta_pos_missing, 7);
        this.dataGroups.add(dMatches, 2);
        // efficiencies
        H2F hi_ptheta_neg_eff = new H2F("hi_ptheta_neg_eff", "hi_ptheta_neg_eff", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_neg_eff.setTitleX("p (GeV)");
        hi_ptheta_neg_eff.setTitleY("#theta (deg)");
        H2F hi_phitheta_neg_eff = new H2F("hi_phitheta_neg_eff", "hi_phitheta_neg_eff", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_neg_eff.setTitleX("#phi (deg)");
        hi_phitheta_neg_eff.setTitleY("#theta (deg)");
        H2F hi_ptheta_pos_eff = new H2F("hi_ptheta_pos_eff", "hi_ptheta_pos_eff", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_pos_eff.setTitleX("p (GeV)");
        hi_ptheta_pos_eff.setTitleY("#theta (deg)");
        H2F hi_phitheta_pos_eff = new H2F("hi_phitheta_pos_eff", "hi_phitheta_pos_eff", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_pos_eff.setTitleX("#phi (deg)");
        hi_phitheta_pos_eff.setTitleY("#theta (deg)");
        DataGroup dEff  = new DataGroup(2,2);
        dEff.addDataSet(hi_ptheta_neg_eff,     0);
        dEff.addDataSet(hi_phitheta_neg_eff,   1);
        dEff.addDataSet(hi_ptheta_pos_eff,     2);
        dEff.addDataSet(hi_phitheta_pos_eff,   3);
        this.dataGroups.add(dEff, 3);
        
    }    

    private void effHisto(H2F found, H2F miss, H2F eff) {
        for(int ix=0; ix< found.getDataSize(0); ix++) {
            for(int iy=0; iy< found.getDataSize(1); iy++) {
                double fEntry = found.getBinContent(ix, iy);
                double mEntry = miss.getBinContent(ix, iy);
                double effValue = 0;
                if(fEntry+mEntry>0) effValue = fEntry/(fEntry+mEntry);
                eff.setBinContent(ix, iy, effValue);
            }   
        }
    }
    
    private Particle findRoad(ArrayList<Byte> wires, int dcSmear, int pcalUSmear, int pcalVWSmear) {
        Particle foundRoad = null;
        if(dcSmear>0 || pcalUSmear>0 || pcalVWSmear>0) {
            for(int k1=-dcSmear; k1<=dcSmear; k1++) {
            for(int k2=-dcSmear; k2<=dcSmear; k2++) {
            for(int k3=-dcSmear; k3<=dcSmear; k3++) {
            for(int k4=-dcSmear; k4<=dcSmear; k4++) {
            for(int k5=-dcSmear; k5<=dcSmear; k5++) {
            for(int k6=-dcSmear; k6<=dcSmear; k6++) {
            for(int k7=-pcalUSmear;   k7<=pcalUSmear;  k7++) {
            for(int k8=-pcalUSmear;   k8<=pcalUSmear;  k8++) {
            for(int k9=-pcalVWSmear;  k9<=pcalVWSmear; k9++) {
            for(int k10=-pcalVWSmear; k10<=pcalVWSmear; k10++) {
                ArrayList<Byte> wiresCopy = new ArrayList(wires);
                wiresCopy.set(0, (byte) (wires.get(0) + k1));
                wiresCopy.set(1, (byte) (wires.get(1) + k2));
                wiresCopy.set(2, (byte) (wires.get(2) + k3));
                wiresCopy.set(3, (byte) (wires.get(3) + k4));
                wiresCopy.set(4, (byte) (wires.get(4) + k5));
                wiresCopy.set(5, (byte) (wires.get(5) + k6));
                wiresCopy.set(6, (byte) (wires.get(6) + k7));
                wiresCopy.set(7, (byte) 0); //panel 2
                wiresCopy.set(8, (byte) (wires.get(8) + k8));
                wiresCopy.set(9, (byte) (wires.get(9) + k9));
                wiresCopy.set(10,(byte) (wires.get(10)+ k10));
                wiresCopy.set(11, (byte) 0); //htcc
                wiresCopy.set(12,(byte) (wires.get(12)));
                if(this.dictionary.containsKey(wiresCopy)) {
                    foundRoad=this.dictionary.get(wiresCopy);
                    break;
                }
            }}}}}}}}}}
        }
        else {
            if(this.dictionary.containsKey(wires)) foundRoad=this.dictionary.get(wires);
        } 
        return foundRoad;
    }
    
    public EmbeddedCanvasTabbed getCanvas() {
        return canvas;
    }

    public Map<ArrayList<Byte>, Particle> getDictionary() {
        return dictionary;
    }

    public int htccMask(ArrayList<int[]> htccPMTS) {
        int[] htccMaskArray = new int[8];
        for(int iPMT = 0; iPMT < htccPMTS.size(); iPMT++) {
            int htccSector    = htccPMTS.get(iPMT)[0];
            int htccLayer     = htccPMTS.get(iPMT)[1];
            int htccComponent = htccPMTS.get(iPMT)[2];
            int ibit = (htccComponent-1) +(htccLayer-1)*4;
            htccMaskArray[ibit]=1;
        }
        int htccMask=0;
//        LOGGER.log(Level.INFO, "Mask " + htccPMTS.size());
        for(int ibit=0; ibit<8; ibit++) {
            if(htccMaskArray[ibit]>0) {
                int imask = 1 << ibit;
                htccMask += imask;
//                LOGGER.log(Level.INFO, ibit + " " + imask + " " + htccMask);
            }
        }
        return htccMask;
    }
    
    public ArrayList<int[]> htccPMT(double th, double ph) {
        // phi is defined between 0 and 360
        double TableTheta1[] = { 8.75, 16.25, 23.75, 31.25};
        double TableTheta2[] = {12.50, 20.00, 27.50};
        double TableTheta3[] = {11.25, 18.75, 26.25};
        double TableTheta4[] = {13.75, 21.25, 28.75};

        ArrayList<int[]> htccPMTS = new ArrayList<>();

        double p1, p2, ph_new;

        int sector=0;
        if (ph > 30.0 && ph <= 90.0) {
            sector = 2;
        } else if (ph > 90.0 && ph <= 150.0) {
            sector = 3;
        } else if (ph > 150.0 && ph <= 210.0) {
            sector = 4;
        } else if (ph > 210.0 && ph <= 270.0) {
            sector = 5;
        } else if (ph > 270.0 && ph <= 330.0) {
            sector = 6;
        } else if (ph <= 30.0 || ph > 330.0) {
            sector = 1;
        }
        double phSec = ((ph+30.0)%60)-30;

//   int sector[];
//   HTCC_Hits cherenkov_road = null;
        //First, find the number of hits, which table to use, and sector/layer/component
        for (int i = 0; i < 4; i++) {
            // 1 hit case
            if (th == TableTheta1[i] && (phSec==-15 || phSec==15)) {
                int htccPMT[] = new int[3];
                htccPMT[0] = sector;
                if (phSec == -15.0) {
                    htccPMT[1] = 1;
                } else if (phSec == 15.0) {
                    htccPMT[1] = 2;
                }              
                htccPMT[2] = i + 1;
                htccPMTS.add(htccPMT);  
            }
            // 2 hits over phi
            else if (th == TableTheta1[i] && phSec==0) {
                for(int k = 0; k < 2; k++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    htccPMT[1] = k + 1;
                    htccPMT[2] = i + 1;
                    htccPMTS.add(htccPMT);  
                }                    
            }
            // 2 hits over sectors
            else if (th == TableTheta1[i] && phSec==-30) {
                for(int k = 0; k < 2; k++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector + k; if(htccPMT[0]==7) htccPMT[0]=1;
                    htccPMT[1] = 2 - k;
                    htccPMT[2] = i + 1;
                    htccPMTS.add(htccPMT);  
                }                    
            }
        }
        for (int i = 0; i < 3; i++) {
            // 2 hits over theta
            if (th == TableTheta2[i] && (phSec==-15 || phSec==15)) {
                for(int k = 0; k < 2; k++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    if (phSec == -15.0) {
                        htccPMT[1] = 1;
                    } else if (phSec == 15.0) {
                        htccPMT[1] = 2;
                    }              
                    htccPMT[2] = i + 1 + k;
                    htccPMTS.add(htccPMT);  
                }                    
            }
            // 4 hits
            if (th == TableTheta2[i] && phSec==0) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    htccPMTS.add(htccPMT);  
                }
                }
            }
            // 4 hits in between sectors
            if (th == TableTheta2[i] && phSec==-30) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector + j; if(htccPMT[0]==7) htccPMT[0]=1;
                    htccPMT[1] = 2 - j;
                    htccPMT[2] = i + 1 + k;
                    htccPMTS.add(htccPMT);  
                }
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            // 3 hit combinations
            if (th == TableTheta3[i] && phSec==-5) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==1 && j==1)) htccPMTS.add(htccPMT);  
                }
                }
            }
            if (th == TableTheta3[i] && phSec==5) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==1 && j==0)) htccPMTS.add(htccPMT);  
                }
                }
            }
            if (th == TableTheta3[i] && phSec==-25) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector - j; if(htccPMT[0]==0) htccPMT[0]=6;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==1 && j==1)) htccPMTS.add(htccPMT);  
                }
                }
            }
            if (th == TableTheta3[i] && phSec==25) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector + j; if(htccPMT[0]==7) htccPMT[0]=1;
                    htccPMT[1] = 2 - j;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==1 && j==1)) htccPMTS.add(htccPMT);  
                }
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            // 3 hit combinations
            if (th == TableTheta4[i] && phSec==-5) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==0 && j==1)) htccPMTS.add(htccPMT);  
                }
                }
            }
            if (th == TableTheta4[i] && phSec==5) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==0 && j==0)) htccPMTS.add(htccPMT);  
                }
                }
            }
            if (th == TableTheta4[i] && phSec==-25) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector - j; if(htccPMT[0]==0) htccPMT[0]=6;
                    htccPMT[1] = j + 1;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==0 && j==1)) htccPMTS.add(htccPMT);  
                }
                }
            }
            if (th == TableTheta4[i] && phSec==25) {
                for(int k = 0; k < 2; k++) {
                for(int j = 0; j < 2; j++) {
                    int htccPMT[] = new int[3];
                    htccPMT[0] = sector + j; if(htccPMT[0]==7) htccPMT[0]=1;
                    htccPMT[1] = 2 - j;
                    htccPMT[2] = i + 1 + k;
                    if(!(k==0 && j==1)) htccPMTS.add(htccPMT);  
                }
                }
            }        }
        return htccPMTS;
    }


    public boolean init() {
        this.createHistos();
        return true;
    }
    
    public void plotHistos() {
        this.analyzeHistos();
        this.canvas.getCanvas("Dictionary").divide(5, 2);
        this.canvas.getCanvas("Dictionary").setGridX(false);
        this.canvas.getCanvas("Dictionary").setGridY(false);
        this.canvas.getCanvas("Dictionary").draw(dataGroups.getItem(0));
        this.canvas.getCanvas("Dictionary").getPad(0).getAxisZ().setLog(true);
        this.canvas.getCanvas("Dictionary").getPad(5).getAxisZ().setLog(true);
        this.canvas.getCanvas("Matched Roads").divide(3, 2);
        this.canvas.getCanvas("Matched Roads").setGridX(false);
        this.canvas.getCanvas("Matched Roads").setGridY(false);
        this.canvas.getCanvas("Matched Roads").draw(dataGroups.getItem(1));
        this.canvas.getCanvas("Matched Roads").getPad(0).getAxisZ().setLog(true);
        this.canvas.getCanvas("Matched Roads").getPad(3).getAxisZ().setLog(true);
        this.canvas.getCanvas("Matched Tracks").divide(4, 2);
        this.canvas.getCanvas("Matched Tracks").setGridX(false);
        this.canvas.getCanvas("Matched Tracks").setGridY(false);
        this.canvas.getCanvas("Matched Tracks").draw(dataGroups.getItem(2));
        this.canvas.getCanvas("Efficiency").divide(2, 2);
        this.canvas.getCanvas("Efficiency").setGridX(false);
        this.canvas.getCanvas("Efficiency").setGridY(false);
        this.canvas.getCanvas("Efficiency").draw(dataGroups.getItem(3));
    }
    
    public void printDictionary() {
        if(this.dictionary !=null) {
            for(Map.Entry<ArrayList<Byte>, Particle> entry : this.dictionary.entrySet()) {
                ArrayList<Byte> wires = entry.getKey();
                Particle road = entry.getValue();
                for(int wire: wires) System.out.print(wire + " ");
                System.out.println(road.charge() + " " + road.p() + " " + Math.toDegrees(road.theta()) + " " + Math.toDegrees(road.phi()) + " " + road.vz());
            }
        }
    }
    
    /**
     * Test selected dictionary on input event file
     * @param fileName: input event hipo file
     * @param wireSmear: dc wire smearing
     * @param pcalSmear: pcal strip smearing
     * @param sectorDependence: sector-dependence mode (0=false, 1=true)
     * @param mode: test mode
     * @param maxEvents: max number of events to process
     * @param pidSelect: pid for track selection
     * @param chargeSelect: charge for track selection
     * @param thrs: momentum threshold for track selection
     */
    public void processFile(String fileName, int wireSmear, int pcalSmear, int sectorDependence, int mode, int maxEvents, int pidSelect, int chargeSelect, double thrs) {
        // testing dictionary on event file
        
        LOGGER.log(Level.INFO, "\nTesting dictionary on file " + fileName);
        
        int pcalUSmear  = pcalSmear;
        int pcalVWSmear = 0;
        if(mode>1) pcalVWSmear = pcalSmear;
        
        HipoDataSource reader = new HipoDataSource();
        reader.open(fileName);
        int nevent = -1;
        while(reader.hasEvent() == true) {
            if(maxEvents>0) {
                if(nevent>= maxEvents) break;
            }
            DataEvent event = reader.getNextEvent();
            nevent++;
            if(nevent%10000 == 0) LOGGER.log(Level.INFO, "Analyzed " + nevent + " events");
            DataBank runConfig       = null;
            DataBank recParticle     = null;
            DataBank recCalorimeter  = null;
            DataBank recScintillator = null;
            DataBank recCherenkov    = null;
            DataBank recTrack        = null;
            DataBank tbtTrack        = null;
            DataBank ecalCluster     = null;
            DataBank tbtHits         = null;
            DataBank htccRec         = null;
            DataBank htccADC         = null;
            DataBank mcPart          = null;          
            if (event.hasBank("RUN::config")) {
                runConfig = event.getBank("RUN::config");
            }            
            if (event.hasBank("REC::Particle")) {
                recParticle = event.getBank("REC::Particle");
            }
            if (event.hasBank("REC::Scintillator")) {
                recScintillator = event.getBank("REC::Scintillator");
            }
            if (event.hasBank("REC::Calorimeter")) {
                recCalorimeter = event.getBank("REC::Calorimeter");
            }
            if (event.hasBank("REC::Cherenkov")) {
                recCherenkov = event.getBank("REC::Cherenkov");
            }
            if (event.hasBank("REC::Track")) {
                recTrack = event.getBank("REC::Track");
            }
            if (event.hasBank("TimeBasedTrkg::TBTracks")) {
                tbtTrack = event.getBank("TimeBasedTrkg::TBTracks");
            }
            if (event.hasBank("TimeBasedTrkg::TBHits")) {
                tbtHits = event.getBank("TimeBasedTrkg::TBHits");
            }
            if (event.hasBank("ECAL::clusters")) {
                ecalCluster = event.getBank("ECAL::clusters");
            }
            if (event.hasBank("HTCC::rec")) {
                htccRec = event.getBank("HTCC::rec");
            }
            if (event.hasBank("HTCC::adc")) {
                htccADC = event.getBank("HTCC::adc");
            }
            if (event.hasBank("MC::Particle")) {
                mcPart = event.getBank("MC::Particle");
            }
            // add other banks
            if(recParticle!=null && recTrack != null && tbtTrack != null && tbtHits != null) {
                for (int i = 0; i < recTrack.rows(); i++) {
                    // start from tracks
                    int index    = recTrack.getShort("index", i);
                    int pindex   = recTrack.getShort("pindex", i);
                    int detector = recTrack.getByte("detector", i);
                    // use only forward tracks
                    if(detector == DetectorType.DC.getDetectorId()) {
                        int charge   = recParticle.getByte("charge", pindex);
                        int pid      = recParticle.getInt("pid", pindex);
                        // if pid or charge are selected keep only matching particles
                        if(chargeSelect != 0 && chargeSelect!=charge) continue;
                        if(pidSelect != 0    && pidSelect!=pid)       continue;   
                        // save particle information
                        Particle part = new Particle(
                                        -11*charge,
                                        recParticle.getFloat("px", pindex),
                                        recParticle.getFloat("py", pindex),
                                        recParticle.getFloat("pz", pindex),
                                        recParticle.getFloat("vx", pindex),
                                        recParticle.getFloat("vy", pindex),
                                        recParticle.getFloat("vz", pindex));                   
                        boolean goodTrack=true;
                        // meglect tracks below 1 GeV
                        if(part.p()<thrs) goodTrack=false;
                        // neglect tracks with bad vertex
                        if(Math.abs(part.vz())>10) goodTrack=false;
                        if (mcPart != null) {
                            for(int loop = 0; loop < mcPart.rows(); loop++) { 
                                Particle genPart = new Particle(
                                            mcPart.getInt("pid",  loop),
                                            mcPart.getFloat("px", loop),
                                            mcPart.getFloat("py", loop),
                                            mcPart.getFloat("pz", loop),
                                            mcPart.getFloat("vx", loop),
                                            mcPart.getFloat("vy", loop),
                                            mcPart.getFloat("vz", loop));
                                if(part.charge()!=genPart.charge() ||
                                   Math.abs(part.p()-genPart.p())>0.1 ||
                                   Math.abs(Math.toDegrees(part.phi()-genPart.phi()))>5 ||    
                                   Math.abs(Math.toDegrees(part.theta()-genPart.theta()))>5) {
                                   goodTrack=false;
                                }
                            }   
                        }
                        if(!goodTrack) continue;
                        
                        // get the DC wires' IDs
                        int[] wireArray = new int[36];
                        int trackSector = 0;
                        int nSL3=0;
                        for (int j = 0; j < tbtHits.rows(); j++) {
                            if (tbtHits.getByte("trkID", j) == tbtTrack.getShort("id", index)) {
                                trackSector    = tbtHits.getByte("sector", j);
                                int superlayer = tbtHits.getByte("superlayer", j);
                                int layer      = tbtHits.getByte("layer", j);
                                int wire       = tbtHits.getShort("wire", j);
                                wireArray[(superlayer - 1) * 6 + layer - 1] = wire;
                                if(superlayer==3) nSL3++;
                            }
                        }

                        if(nSL3<3) continue; //ignore tracks with less than 3 hits in SL3 as in dictionary maker
                        ArrayList<Byte> wires = new ArrayList<>();
                        for (int k = 0; k < 6; k++) {
                            for (int l=0; l<1; l++) {
                                // use first non zero wire in superlayer
                                if(wireArray[k*6 +l] != 0) {
                                   wires.add((byte) wireArray[k*6+l]);
                                   break;
                                }
                            }
                        }
                            
                        // use only tracks with 6 superlayers
                        if(wires.size()==6) {
                            // now check other detectors
                            int paddle1b = 0;
                            int paddle2  = 0;
                            int pcalU    = 0;
                            int pcalV    = 0;
                            int pcalW    = 0;
                            int htcc     = 0;
                            // check FTOF
                            if(recScintillator!=null && mode>0) {
                                for(int j=0; j<recScintillator.rows(); j++) {
                                    if(recScintillator.getShort("pindex",j) == pindex) {
                                        int detectorScint  = recScintillator.getByte("detector", j);
                                        int layerScint     = recScintillator.getByte("layer",j);
                                        int componentScint = recScintillator.getShort("component",j);
                                        if(detectorScint==DetectorType.FTOF.getDetectorId() && layerScint==2) paddle1b = componentScint;
                                        if(detectorScint==DetectorType.FTOF.getDetectorId() && layerScint==3) paddle2  = componentScint;
                                    }
                                }
                            }
                            // check ECAL
                            if(recCalorimeter!=null && ecalCluster!=null && mode>0) {
                                for(int j=0; j<recCalorimeter.rows(); j++) {
                                    if(recCalorimeter.getShort("pindex",j) == pindex) {
                                        int detectorClus = recCalorimeter.getByte("detector", j);
                                        int indexClus    = recCalorimeter.getShort("index",j);
                                        int layerClus    = recCalorimeter.getByte("layer",j);
                                        // use pcal only
                                        if(detectorClus==DetectorType.ECAL.getDetectorId() && layerClus==1) {
                                            pcalU = (ecalCluster.getInt("coordU",indexClus)-4)/8+1;
                                            if(mode>1) {
                                                pcalV = (ecalCluster.getInt("coordV",indexClus)-4)/8+1;
                                                pcalW = (ecalCluster.getInt("coordW",indexClus)-4)/8+1;
                                            }
                                        }
                                    }
                                }
                            }
                           // check HTCC
                            if (recCherenkov != null && htccRec != null && htccADC != null && mode > 2 && false) {
                                int htcc_event;
//                                recCherenkov.show(); htccADC.show();
                                for (int j = 0; j < recCherenkov.rows(); j++) {
                                    if (recCherenkov.getShort("pindex", j) == pindex) {
                                        int detectorCheren = recCherenkov.getByte("detector", j);
                                        if (detectorCheren == DetectorType.HTCC.getDetectorId()) {
                                            int nhits = htccRec.getShort("nhits",recCherenkov.getShort("index", j));
                                            double x = recCherenkov.getFloat("x", j);
                                            double y = recCherenkov.getFloat("y", j);
                                            double z = recCherenkov.getFloat("z", j);                                            
                                            double thetaCheren = Math.acos(z/Math.sqrt(x*x+y*y+z*z));
                                            double phiCheren   = Math.atan2(y, x);
                                            thetaCheren = Math.toDegrees(thetaCheren);
                                            phiCheren   = Math.toDegrees(phiCheren );
                                            double phiCC   = Math.round(phiCheren); 
                                            if(phiCC<0) phiCC +=360;
                                            double thetaCC = ((double) Math.round(thetaCheren*100))/100.;
                                            ArrayList<int[]> htccPMTs        = htccPMT(thetaCC, phiCC);
                                            ArrayList<int[]> htccPMTsMatched = new ArrayList<int[]>();
//                                            LOGGER.log(Level.INFO, thetaCheren + " " + thetaCC + " " + phiCheren + " " + phiCC + " " + htccPMTs.size());
                                            //The special case of 4 hits, where we need to check if the hits were not in fact only 3
                                            for(int iPMT = 0; iPMT < htccPMTs.size(); iPMT++) {
                                                int htccSector    = htccPMTs.get(iPMT)[0];
                                                int htccLayer     = htccPMTs.get(iPMT)[1];
                                                int htccComponent = htccPMTs.get(iPMT)[2];
                                                boolean found = false;
//                                                LOGGER.log(Level.INFO, iPMT + " " + htccSector + " " + htccLayer + " " + htccComponent);
                                                for (int k = 0; k < htccADC.rows(); k++) {
                                                    int sector    = htccADC.getByte("sector", k);
                                                    int layer     = htccADC.getByte("layer", k);
                                                    int component = htccADC.getShort("component", k);
//                                                    LOGGER.log(Level.INFO, k + " " + sector + " " + layer + " " + component);
                                                    if( htccSector    == sector && 
                                                        htccLayer     == layer && 
                                                        htccComponent == component) {
                                                        found = true;
//                                                        LOGGER.log(Level.INFO, "Found match in adc bank");
                                                    }                                                  
                                                }
                                                if(found) {
                                                    htccPMTsMatched.add(htccPMTs.get(iPMT));
                                                }
//                                                else {
//                                                    LOGGER.log(Level.INFO, "Removing hit " + iPMT + " among " + htccPMTs.size() + " " + thetaCC + " " +  phiCC + " " + htccSector + "/" + htccLayer+ "/"+htccComponent);
//                                                    runConfig.show();recCherenkov.show(); htccRec.show();htccADC.show();
//                                                }
                                            }
                                            if(htccPMTsMatched.size() != nhits) LOGGER.log(Level.INFO, "Mismatch in HTCC cluster size " + runConfig.getInt("event",0) + " " + nhits +"/"+htccPMTsMatched.size()+"/"+htccPMTs.size() + " " + thetaCC + " " +  phiCC);
                                            htcc = this.htccMask(htccPMTsMatched);
                                        }
                                    }
                                }
                            }
                            
                            wires.add((byte) paddle1b);
                            wires.add((byte) paddle2);
                            wires.add((byte) pcalU);
                            wires.add((byte) pcalV);
                            wires.add((byte) pcalW);
                            wires.add((byte) htcc);
                            wires.add((byte) (trackSector*sectorDependence));
                            if(mode>0 && (paddle1b==0 || pcalU==0)) continue;
                            if(mode>1 && (paddle1b==0 || pcalU==0 || pcalV==0 || pcalW==0)) continue;
                            if(mode>2 && (paddle1b==0 || pcalU==0 || pcalV==0 || pcalW==0 || htcc==-1)) continue;
                            double phi    = (Math.toDegrees(part.phi())+180+30)%60-30;    
                            Particle road = this.findRoad(wires,wireSmear,pcalUSmear,pcalVWSmear);
                            if(road != null) {
                                double phiRoad = (Math.toDegrees(road.phi())+180+30)%60-30;
                                if(road.charge()<0) {
                                    this.dataGroups.getItem(1).getH2F("hi_ptheta_neg_matchedroad").fill(road.p(), Math.toDegrees(road.theta()));
                                    this.dataGroups.getItem(1).getH2F("hi_phitheta_neg_matchedroad").fill(phiRoad, Math.toDegrees(road.theta()));
                                    this.dataGroups.getItem(1).getH2F("hi_vztheta_neg_matchedroad").fill(road.vz(), Math.toDegrees(road.theta()));
                                }
                                else {
                                    this.dataGroups.getItem(1).getH2F("hi_ptheta_pos_matchedroad").fill(road.p(), Math.toDegrees(road.theta()));
                                    this.dataGroups.getItem(1).getH2F("hi_phitheta_pos_matchedroad").fill(phiRoad, Math.toDegrees(road.theta()));                            
                                    this.dataGroups.getItem(1).getH2F("hi_vztheta_pos_matchedroad").fill(road.vz(), Math.toDegrees(road.theta()));
                                }
                                if(charge==-1) {
                                    this.dataGroups.getItem(2).getH2F("hi_ptheta_neg_found").fill(part.p(), Math.toDegrees(part.theta()));
                                    this.dataGroups.getItem(2).getH2F("hi_phitheta_neg_found").fill(phi, Math.toDegrees(part.theta()));
                                }
                                else {
                                    this.dataGroups.getItem(2).getH2F("hi_ptheta_pos_found").fill(part.p(), Math.toDegrees(part.theta()));
                                    this.dataGroups.getItem(2).getH2F("hi_phitheta_pos_found").fill(phi, Math.toDegrees(part.theta()));
                                }
                            }
                            else {
                                if(charge==-1) {
                                    this.dataGroups.getItem(2).getH2F("hi_ptheta_neg_missing").fill(part.p(), Math.toDegrees(part.theta()));
                                    this.dataGroups.getItem(2).getH2F("hi_phitheta_neg_missing").fill(phi, Math.toDegrees(part.theta()));
                                }
                                else {
                                    this.dataGroups.getItem(2).getH2F("hi_ptheta_pos_missing").fill(part.p(), Math.toDegrees(part.theta()));
                                    this.dataGroups.getItem(2).getH2F("hi_phitheta_pos_missing").fill(phi, Math.toDegrees(part.theta()));
                                }                    
                            }
                        }
                    }
                }
            }
        }

    }
    
    /**
     *
     * @param fileName
     * @param sec
     * @param mode
     * @param thrs
     */
    public void readDictionary(String fileName, int sec, int mode, double thrs) {
        
        this.dictionary = new HashMap<>();
        
        LOGGER.log(Level.INFO, "\nReading dictionary from file " + fileName);
        int nLines = 0;
        int nFull  = 0;
        int nDupli = 0;
        int nfirst = 0;
        
        File fileDict = new File(fileName);
        BufferedReader txtreader = null;
        try {
            txtreader = new BufferedReader(new FileReader(fileDict));
            String line = null;
            while ((line = txtreader.readLine()) != null) {
                nLines++;
                if(nLines % 1000000 == 0) LOGGER.log(Level.INFO, "Read " + nLines + " roads");
                String[] lineValues = line.split("\t");
                ArrayList<Byte> wires = new ArrayList<>();
                if(lineValues.length < 51) {
                    LOGGER.log(Level.INFO, "WARNING: dictionary line " + nLines + " incomplete: skipping");
                }
                else {
//                    LOGGER.log(Level.INFO, line);
                    int charge   = Integer.parseInt(lineValues[0]);
                    double p     = Double.parseDouble(lineValues[1]);
                    double theta = Double.parseDouble(lineValues[2]);
                    double phi   = Double.parseDouble(lineValues[3]);
                    double vz    = Double.parseDouble(lineValues[41]);
                    double px    = p*Math.sin(Math.toRadians(theta))*Math.cos(Math.toRadians(phi));
                    double py    = p*Math.sin(Math.toRadians(theta))*Math.sin(Math.toRadians(phi));
                    double pz    = p*Math.cos(Math.toRadians(theta));
                    double phiSec = (phi+360+30)%60-30;
                    double pcalE  = Double.parseDouble(lineValues[48]);
                    double ecinE  = Double.parseDouble(lineValues[49]);
                    double ecoutE = Double.parseDouble(lineValues[50]);
                    Particle road = new Particle(211*charge, px, py, pz, 0, 0, vz);
                    road.setProperty("pcalE",  pcalE);
                    road.setProperty("ecinE",  ecinE);
                    road.setProperty("ecoutE", ecoutE);
                    // take wire id of first layer in each superlayer, id>0
                    for(int i=0; i<6; i++) {
                        int wire = Integer.parseInt(lineValues[4+i*6]);
                        if(wire>0) wires.add((byte) wire);
                    }
                    int sector   = 0;
                    int paddle1b = 0;
                    int paddle2  = 0;
                    int pcalu    = 0; 
                    int pcalv    = 0;                    
                    int pcalw    = 0;
                    int htcc     = 0;
                    if(lineValues.length >=44 & mode>0) {
                        paddle1b = Integer.parseInt(lineValues[40]);                    
                        paddle2  = Integer.parseInt(lineValues[42]);
                        pcalu    = Integer.parseInt(lineValues[43]);
                    }                    
                    if(lineValues.length >=46 & mode>1) {
                        pcalv    = Integer.parseInt(lineValues[44]);                    
                        pcalw    = Integer.parseInt(lineValues[45]);
                    }                    
                    if(lineValues.length >=47 & mode>2) {
                        htcc     = Integer.parseInt(lineValues[46]);
                    }
                    if(lineValues.length >=48) {
                        sector   = Integer.parseInt(lineValues[47]);
                    }
                    // keep only roads with 6 superlayers
                    if(wires.size()!=6 || road.p()< thrs) continue;
                    // add other detectors
                    wires.add((byte) paddle1b);
                    wires.add((byte) paddle2);
                    wires.add((byte) pcalu);
                    wires.add((byte) pcalv);
                    wires.add((byte) pcalw);
                    wires.add((byte) htcc);
                    wires.add((byte) (sector*sec));
                    nFull++;
                    if(this.dictionary.containsKey(wires)) {
                        nDupli++;
                        if(nDupli<10) LOGGER.log(Level.INFO, "WARNING: found duplicate road");
                        else if(nDupli==10) LOGGER.log(Level.INFO, "WARNING: reached maximum number of warnings, switching to silent mode");
                    }
                    else {
                        this.dictionary.put(wires, road);
                        if(road.charge()<0) {
                            this.dataGroups.getItem(0).getH2F("hi_ptheta_neg_road").fill(road.p(), Math.toDegrees(road.theta()));
                            this.dataGroups.getItem(0).getH2F("hi_phitheta_neg_road").fill(phiSec, Math.toDegrees(road.theta()));
                            this.dataGroups.getItem(0).getH2F("hi_vztheta_neg_road").fill(road.vz(), Math.toDegrees(road.theta()));
                            if(paddle1b>0) this.dataGroups.getItem(0).getH2F("hi_ftofdc_neg_road").fill(wires.get(5),paddle1b);
                            if(paddle1b>0) this.dataGroups.getItem(0).getH2F("hi_pcalftof_neg_road").fill(paddle1b, pcalu);
                        }
                        else {
                            this.dataGroups.getItem(0).getH2F("hi_ptheta_pos_road").fill(road.p(), Math.toDegrees(road.theta()));
                            this.dataGroups.getItem(0).getH2F("hi_phitheta_pos_road").fill(phiSec, Math.toDegrees(road.theta()));                            
                            this.dataGroups.getItem(0).getH2F("hi_vztheta_pos_road").fill(road.vz(), Math.toDegrees(road.theta()));
                            if(paddle1b>0) this.dataGroups.getItem(0).getH2F("hi_ftofdc_pos_road").fill(wires.get(5),paddle1b);
                            if(paddle1b>0) this.dataGroups.getItem(0).getH2F("hi_pcalftof_pos_road").fill(paddle1b, pcalu);
                        }
                    }
                }
            }
            LOGGER.log(Level.INFO, "Found " + nLines + " roads with " + nFull + " full ones, " + nDupli + " duplicates and " + this.dictionary.keySet().size() + " good ones");
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        } 
   }
    
    private void setDictionary(Map<ArrayList<Byte>, Particle> newDictionary) {
        this.dictionary = newDictionary;
    }
    
    private void writeDictionary(Map<ArrayList<Integer>, Particle> dictionary, String dictName) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(dictName);
            for(Map.Entry<ArrayList<Integer>, Particle> entry : dictionary.entrySet()) {
                ArrayList<Integer> road = entry.getKey();
                Particle           part = entry.getValue();
                if(road.size()<13) {
                    continue;
                }
                else {
                    int wl1 = road.get(0);
                    int wl2 = road.get(1);
                    int wl3 = road.get(2);
                    int wl4 = road.get(3);
                    int wl5 = road.get(4);
                    int wl6 = road.get(5);
                    int paddle1b = road.get(6);
                    int paddle2  = road.get(7);
                    int pcalU    = road.get(8);
                    int pcalV    = road.get(9);
                    int pcalW    = road.get(10);
                    int htcc     = road.get(11);
                    int sector   = road.get(12);
                    pw.printf("%d\t%.2f\t%.2f\t%.2f\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%d\t%d\t%d\t%d\t"
                    + "%d\t%.2f\t%d\t%d\t%d\t%d\t"
                    + "%d\t%d\t%.1f\t%.1f\t%.1f\n",
                    //+ "%.1f\t %.1f\t %.1f\t %.1f\t %.1f\t %.1f\t\n", 
                    part.charge(), part.p(), Math.toDegrees(part.theta()), Math.toDegrees(part.phi()),
                    road.get(0), 0, 0, 0, 0, 0, 
                    road.get(1), 0, 0, 0, 0, 0, 
                    road.get(2), 0, 0, 0, 0, 0, 
                    road.get(3), 0, 0, 0, 0, 0, 
                    road.get(4), 0, 0, 0, 0, 0, 
                    road.get(5), 0, 0, 0, 0, 0,  
                    paddle1b, part.vz(), paddle2, pcalU, pcalV, pcalW, htcc, sector, 
                    part.getProperty("pcalE"), part.getProperty("ecinE"), part.getProperty("ecoutE"));
                }
            }
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TrackDictionaryMakerRNG.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
       
    }
    

    public static void main(String[] args) {
        
        OptionParser parser = new OptionParser("dict-validation");
        parser.addOption("-dict"     , "",  "dictionary file name");
        parser.addOption("-create"   ,  "", "select filename for new dictionary created from event file");
        parser.addOption("-i"        ,  "", "event file for dictionary test");
        parser.addOption("-pid"      , "0", "select particle PID for new dictonary, 0: no selection,");
        parser.addOption("-charge"   , "0", "select particle charge for new dictionary, 0: no selection");
        parser.addOption("-wire"     , "0", "dc wire smearing in road finding");
        parser.addOption("-strip"    , "0", "pcal strip smearing in road finding");
        parser.addOption("-sector"   , "0", "sector dependent roads, 0=false, 1=true)");
        parser.addOption("-mode"     , "0", "select test mode, 0: DC only, 1: DC-FTOF-pcalU, 2: DC-FTOF-pcalUVW, 3: DC-FTOF-pcalUVW-HTCC");
        parser.addOption("-threshold", "1", "select roads momentum threshold in GeV");
        parser.addOption("-n"        ,"-1", "maximum number of events to process for validation");
        parser.addOption("-dupli"    , "1", "remove duplicates in dictionary creation, 0=false, 1=true");
        parser.parse(args);
        
        List<String> arguments = new ArrayList<>();
        for(String item : args){ arguments.add(item); }
        
        String dictionaryFileName = null;
        if(parser.hasOption("-dict")==true) dictionaryFileName = parser.getOption("-dict").stringValue();
        
        String inputFileName = null;
        if(parser.hasOption("-create")==true) inputFileName = parser.getOption("-create").stringValue();
            
        String testFileName = null;
        if(parser.hasOption("-i")==true) testFileName = parser.getOption("-i").stringValue();
            
        
        int pid        = parser.getOption("-pid").intValue();
        int charge     = parser.getOption("-charge").intValue();
        if(Math.abs(charge)>1) {
            LOGGER.log(Level.INFO, "\terror: invalid charge selection");
            System.exit(1);
        }
        int wireSmear  = parser.getOption("-wire").intValue();
        if(wireSmear<0) {
            LOGGER.log(Level.INFO, "\terror: invalid dc wire smearing, value should be >0");
            System.exit(1);
        }
        int pcalSmear  = parser.getOption("-strip").intValue();
        if(pcalSmear<0) {
            LOGGER.log(Level.INFO, "\terror: invalid pcal strip smearing, value should be >0");
            System.exit(1);
        }
        int sector     = parser.getOption("-sector").intValue();
        if(sector<0 || sector>1) {
            LOGGER.log(Level.INFO, "\terror: invalid sector-dependence option, allowed values are 0=false or 1=true");
            System.exit(1);
        }
        int mode       = parser.getOption("-mode").intValue();
        if(mode<0 || mode>3) {
            LOGGER.log(Level.INFO, "\terror: invalid test mode, allowed options are 0-DC only, 1-DC-FTOF-pcalU, 2-DC-FTOF-pcalUVW, 3-DC-FTOF-pcalUVW-HTCC");
            System.exit(1);
        }
        int maxEvents  = parser.getOption("-n").intValue();
        int duplicates = parser.getOption("-dupli").intValue();
        if(duplicates<0 || duplicates>1) {
            LOGGER.log(Level.INFO, "\terror: invalid duplicate-removal option, allowed values are 0=false or 1=true");
            System.exit(1);
        }
        double thrs    = parser.getOption("-threshold").doubleValue();
        
        LOGGER.log(Level.INFO, "Dictionary file name set to: " + dictionaryFileName);
        if(parser.containsOptions(arguments, "-c"))   LOGGER.log(Level.INFO, "Event file for dictionary creation set to:    " + inputFileName);
        if(parser.containsOptions(arguments, "-i"))   LOGGER.log(Level.INFO, "Event file for dictionary validation set to:  " + testFileName);
        LOGGER.log(Level.INFO, "PID selection for dictionary creation/validation set to:    " + pid);
        LOGGER.log(Level.INFO, "Charge selection for dictionary creation/validation set to: " + charge);
        LOGGER.log(Level.INFO, "Momentum threshold set to:                                  " + thrs);
        LOGGER.log(Level.INFO, "Wire smearing for dictionary validation set to:             " + wireSmear);
        LOGGER.log(Level.INFO, "Pcal smearing for dictionary validation set to:             " + pcalSmear);
        LOGGER.log(Level.INFO, "Sector dependence for dictionary validation set to:         " + sector);
        LOGGER.log(Level.INFO, "Test mode set to:                                           " + mode);
        LOGGER.log(Level.INFO, "Maximum number of events to process set to:                 " + maxEvents);
        LOGGER.log(Level.INFO, "Duplicates remove flag set to:                              " + duplicates);
//        dictionaryFileName="/Users/devita/tracks_silvia.txt";
//        inputFileName = "/Users/devita/out_clas_003355.evio.440.hipo";
//        testFileName  = "/Users/devita/out_clas_003355.evio.440.hipo";
//        mode =2;
//        wireSmear=0;
//        maxEvents = 100000;  
        boolean debug=false;
        
        TrackDictionaryValidation tm = new TrackDictionaryValidation();
        tm.init();
        if(parser.containsOptions(arguments, "-c") || parser.containsOptions(arguments, "-i") || debug) {
            if(parser.containsOptions(arguments, "-c") || debug) {
                tm.createDictionary(inputFileName, dictionaryFileName, pid, charge, thrs, duplicates);
            }
            else if(parser.containsOptions(arguments, "-i") || debug) {
                tm.readDictionary(dictionaryFileName,sector,mode,thrs);                
    //        tm.printDictionary();
                tm.processFile(testFileName,wireSmear,pcalSmear,sector,mode,maxEvents, pid, charge,thrs);

                JFrame frame = new JFrame("Tracking");
                Dimension screensize = null;
                screensize = Toolkit.getDefaultToolkit().getScreenSize();
                frame.setSize((int) (screensize.getWidth() * 0.8), (int) (screensize.getHeight() * 0.8));
                frame.add(tm.getCanvas());
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                tm.plotHistos();
            }
        }
        else {
            parser.printUsage();
            LOGGER.log(Level.INFO, "\n >>>> error : no dictionary specified: specify the road dictionary or choose to create it from file\n");
            System.exit(0);       
        }

    }
    
    
}