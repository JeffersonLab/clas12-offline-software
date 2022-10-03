package org.jlab.analysis.roads;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import org.jlab.analysis.roads.Dictionary.TestMode;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.logging.DefaultLogger;
import org.jlab.utils.benchmark.ProgressPrintout;

import org.jlab.utils.options.OptionParser;

public class DictionaryValidator {

    private Dictionary             dictionary = null;
    private Map<String, DataGroup> dataGroups = new LinkedHashMap<>();
    private EmbeddedCanvasTabbed   canvas     = null;
            
    private String[] charges = {"pos", "neg"};
    private String fontName = "Arial";
    
    public DictionaryValidator(){
        this.initGraphics();
    }

    public void analyzeHistos() {
        // calculate road finding efficiencies
        this.effHisto(this.dataGroups.get("Matched Tracks").getH2F("hi_ptheta_pos_found"), 
                      this.dataGroups.get("Matched Tracks").getH2F("hi_ptheta_pos_missing"), 
                      this.dataGroups.get("Efficiency").getH2F("hi_ptheta_pos_eff"));
        this.effHisto(this.dataGroups.get("Matched Tracks").getH2F("hi_phitheta_pos_found"), 
                      this.dataGroups.get("Matched Tracks").getH2F("hi_phitheta_pos_missing"), 
                      this.dataGroups.get("Efficiency").getH2F("hi_phitheta_pos_eff"));
        this.effHisto(this.dataGroups.get("Matched Tracks").getH2F("hi_ptheta_neg_found"), 
                      this.dataGroups.get("Matched Tracks").getH2F("hi_ptheta_neg_missing"), 
                      this.dataGroups.get("Efficiency").getH2F("hi_ptheta_neg_eff"));
        this.effHisto(this.dataGroups.get("Matched Tracks").getH2F("hi_phitheta_neg_found"), 
                      this.dataGroups.get("Matched Tracks").getH2F("hi_phitheta_neg_missing"), 
                      this.dataGroups.get("Efficiency").getH2F("hi_phitheta_neg_eff"));
        System.out.println("Positive particles found/missed: " + this.dataGroups.get("Matched Tracks").getH2F("hi_ptheta_pos_found").integral() + "/" +
                                                               + this.dataGroups.get("Matched Tracks").getH2F("hi_ptheta_pos_missing").integral());
        System.out.println("Negative particles found/missed: " + this.dataGroups.get("Matched Tracks").getH2F("hi_ptheta_neg_found").integral() + "/" +
                                                               + this.dataGroups.get("Matched Tracks").getH2F("hi_ptheta_neg_missing").integral());
    }
    
    public void createHistos() {
        // roads in dictionary
        H2F hi_ptheta_neg_road = new H2F("hi_ptheta_neg_road", "Negatives", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_neg_road.setTitleX("p (GeV)");
        hi_ptheta_neg_road.setTitleY("#theta (deg)");
        H2F hi_phitheta_neg_road = new H2F("hi_phitheta_neg_road", "Negatives", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_neg_road.setTitleX("#phi (deg)");
        hi_phitheta_neg_road.setTitleY("#theta (deg)");
        H2F hi_vztheta_neg_road = new H2F("hi_vztheta_neg_road", "Negatives", 100, -15, 15, 100, 0.0, 65.0);     
        hi_vztheta_neg_road.setTitleX("vz (cm)");
        hi_vztheta_neg_road.setTitleY("#theta (deg)");
        H2F hi_ftofdc_neg_road = new H2F("hi_ftofdc_neg_road", "Negatives", 120, 0.0, 120.0, 70, 0.0, 70.0);       
        hi_ftofdc_neg_road.setTitleX("DC-R3 wire");
        hi_ftofdc_neg_road.setTitleY("FTOF paddle");
        H2F hi_pcalftof_neg_road = new H2F("hi_pcalftof_neg_road", "Negatives", 65, 0.0, 65.0, 70, 0.0, 70.0);      
        hi_pcalftof_neg_road.setTitleX("FTOF paddle");
        hi_pcalftof_neg_road.setTitleY("PCAL strip");
        H2F hi_ptheta_pos_road = new H2F("hi_ptheta_pos_road", "Positives", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_pos_road.setTitleX("p (GeV)");
        hi_ptheta_pos_road.setTitleY("#theta (deg)");
        H2F hi_phitheta_pos_road = new H2F("hi_phitheta_pos_road", "Positives", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_pos_road.setTitleX("#phi (deg)");
        hi_phitheta_pos_road.setTitleY("#theta (deg)");
        H2F hi_vztheta_pos_road = new H2F("hi_vztheta_pos_road", "Positives", 100, -15, 15, 100, 0.0, 65.0);     
        hi_vztheta_pos_road.setTitleX("vz (cm)");
        hi_vztheta_pos_road.setTitleY("#theta (deg)");
        H2F hi_ftofdc_pos_road = new H2F("hi_ftofdc_pos_road", "Positives", 120, 0.0, 120.0, 70, 0.0, 70.0);       
        hi_ftofdc_pos_road.setTitleX("DC-R3 wire");
        hi_ftofdc_pos_road.setTitleY("FTOF paddle");
        H2F hi_pcalftof_pos_road = new H2F("hi_pcalftof_pos_road", "Positives", 65, 0.0, 65.0, 70, 0.0, 70.0);       
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
        this.dataGroups.put("Dictionary", dRoads);
        // matched roads
        H2F hi_ptheta_neg_matchedroad = new H2F("hi_ptheta_neg_matchedroad", "Negatives", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_neg_matchedroad.setTitleX("p (GeV)");
        hi_ptheta_neg_matchedroad.setTitleY("#theta (deg)");
        H2F hi_phitheta_neg_matchedroad = new H2F("hi_phitheta_neg_matchedroad", "Negatives", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_neg_matchedroad.setTitleX("#phi (deg)");
        hi_phitheta_neg_matchedroad.setTitleY("#theta (deg)");
        H2F hi_vztheta_neg_matchedroad = new H2F("hi_vztheta_neg_matchedroad", "Negatives", 100, -15, 15, 100, 0.0, 65.0);     
        hi_vztheta_neg_matchedroad.setTitleX("vz (cm)");
        hi_vztheta_neg_matchedroad.setTitleY("#theta (deg)");
        H2F hi_ptheta_pos_matchedroad = new H2F("hi_ptheta_pos_matchedroad", "Positives", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_pos_matchedroad.setTitleX("p (GeV)");
        hi_ptheta_pos_matchedroad.setTitleY("#theta (deg)");
        H2F hi_phitheta_pos_matchedroad = new H2F("hi_phitheta_pos_matchedroad", "Positives", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_pos_matchedroad.setTitleX("#phi (deg)");
        hi_phitheta_pos_matchedroad.setTitleY("#theta (deg)");
        H2F hi_vztheta_pos_matchedroad = new H2F("hi_vztheta_pos_matchedroad", "Positives", 100, -15, 15, 100, 0.0, 65.0);     
        hi_vztheta_pos_matchedroad.setTitleX("vz (cm)");
        hi_vztheta_pos_matchedroad.setTitleY("#theta (deg)");
        DataGroup dMatchedRoads  = new DataGroup(3,2);
        dMatchedRoads.addDataSet(hi_ptheta_neg_matchedroad,   0);
        dMatchedRoads.addDataSet(hi_phitheta_neg_matchedroad, 1);
        dMatchedRoads.addDataSet(hi_vztheta_neg_matchedroad,  2);
        dMatchedRoads.addDataSet(hi_ptheta_pos_matchedroad,   3);
        dMatchedRoads.addDataSet(hi_phitheta_pos_matchedroad, 4);
        dMatchedRoads.addDataSet(hi_vztheta_pos_matchedroad,  5);
        this.dataGroups.put("Matched Roads", dMatchedRoads);        
        // negative tracks
        H2F hi_ptheta_neg_found = new H2F("hi_ptheta_neg_found", "Found Negatives", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_neg_found.setTitleX("p (GeV)");
        hi_ptheta_neg_found.setTitleY("#theta (deg)");
        H2F hi_ptheta_neg_missing = new H2F("hi_ptheta_neg_missing", "Missing Negatives", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_neg_missing.setTitleX("p (GeV)");
        hi_ptheta_neg_missing.setTitleY("#theta (deg)");
        H2F hi_phitheta_neg_found = new H2F("hi_phitheta_neg_found", "Found Positives", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_neg_found.setTitleX("#phi (deg)");
        hi_phitheta_neg_found.setTitleY("#theta (deg)");
        H2F hi_phitheta_neg_missing = new H2F("hi_phitheta_neg_missing", "Missing Positives", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_neg_missing.setTitleX("#phi (deg)");
        hi_phitheta_neg_missing.setTitleY("#theta (deg)");
        // positive tracks
        H2F hi_ptheta_pos_found = new H2F("hi_ptheta_pos_found", "Found Negatives", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_pos_found.setTitleX("p (GeV)");
        hi_ptheta_pos_found.setTitleY("#theta (deg)");
        H2F hi_ptheta_pos_missing = new H2F("hi_ptheta_pos_missing", "Missing Negatives", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_pos_missing.setTitleX("p (GeV)");
        hi_ptheta_pos_missing.setTitleY("#theta (deg)");
        H2F hi_phitheta_pos_found = new H2F("hi_phitheta_pos_found", "Found Positives", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_pos_found.setTitleX("#phi (deg)");
        hi_phitheta_pos_found.setTitleY("#theta (deg)");
        H2F hi_phitheta_pos_missing = new H2F("hi_phitheta_pos_missing", "Missing Positives", 100, -30, 30, 100, 0.0, 65.0);     
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
        this.dataGroups.put("Matched Tracks", dMatches);
        // efficiencies
        H2F hi_ptheta_neg_eff = new H2F("hi_ptheta_neg_eff", "Negatives", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_neg_eff.setTitleX("p (GeV)");
        hi_ptheta_neg_eff.setTitleY("#theta (deg)");
        H2F hi_phitheta_neg_eff = new H2F("hi_phitheta_neg_eff", "Negatives", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_neg_eff.setTitleX("#phi (deg)");
        hi_phitheta_neg_eff.setTitleY("#theta (deg)");
        H2F hi_ptheta_pos_eff = new H2F("hi_ptheta_pos_eff", "Positives", 100, 0.0, 10.0, 100, 0.0, 65.0);     
        hi_ptheta_pos_eff.setTitleX("p (GeV)");
        hi_ptheta_pos_eff.setTitleY("#theta (deg)");
        H2F hi_phitheta_pos_eff = new H2F("hi_phitheta_pos_eff", "Positives", 100, -30, 30, 100, 0.0, 65.0);     
        hi_phitheta_pos_eff.setTitleX("#phi (deg)");
        hi_phitheta_pos_eff.setTitleY("#theta (deg)");
        DataGroup dEff  = new DataGroup(2,2);
        dEff.addDataSet(hi_ptheta_neg_eff,     0);
        dEff.addDataSet(hi_phitheta_neg_eff,   1);
        dEff.addDataSet(hi_ptheta_pos_eff,     2);
        dEff.addDataSet(hi_phitheta_pos_eff,   3);
        this.dataGroups.put("Efficiency", dEff);
        
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
                ArrayList<Byte> wiresCopy = new ArrayList<>();
                wiresCopy.add((byte) (wires.get(0)  + k1));
                wiresCopy.add((byte) (wires.get(1)  + k2));
                wiresCopy.add((byte) (wires.get(2)  + k3));
                wiresCopy.add((byte) (wires.get(3)  + k4));
                wiresCopy.add((byte) (wires.get(4)  + k5));
                wiresCopy.add((byte) (wires.get(5)  + k6));
                wiresCopy.add((byte) (wires.get(6)  + k7));
                wiresCopy.add((byte) 0); //panel 2
                wiresCopy.add((byte) (wires.get(8)  + k8));
                wiresCopy.add((byte) (wires.get(9)  + k9));
                wiresCopy.add((byte) (wires.get(10) + k10));
                wiresCopy.add((byte) 0); //htcc
                wiresCopy.add((byte) (wires.get(12)));
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

    public void init(String filename, TestMode mode, int wireBin, int stripBin, int sectorDependence) {
        this.dictionary = new Dictionary();
        this.dictionary.readDictionary(filename, mode, wireBin, stripBin, sectorDependence);
        this.createHistos();
        this.plotRoads();
    }
        
    private void initGraphics() {
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        GStyle.getAxisAttributesZ().setLabelFontSize(14);
        GStyle.getAxisAttributesX().setLabelFontName(this.fontName);
        GStyle.getAxisAttributesY().setLabelFontName(this.fontName);
        GStyle.getAxisAttributesZ().setLabelFontName(this.fontName);
        GStyle.getAxisAttributesX().setTitleFontName(this.fontName);
        GStyle.getAxisAttributesY().setTitleFontName(this.fontName);
        GStyle.getAxisAttributesZ().setTitleFontName(this.fontName);
        GStyle.setGraphicsFrameLineWidth(1);
        GStyle.getH1FAttributes().setLineWidth(2);
    }
        
    public void plotHistos() {
        this.analyzeHistos();
        for(String key : this.dataGroups.keySet()) {
            if( this.canvas == null)
                this.canvas = new EmbeddedCanvasTabbed(key);
            else
                this.canvas.addCanvas(key);
            this.canvas.getCanvas(key).draw(this.dataGroups.get(key));
            this.canvas.getCanvas(key).setGridX(false);
            this.canvas.getCanvas(key).setGridY(false);
            if(key.equals("Dictionary")) {
                this.canvas.getCanvas(key).getPad(0).getAxisZ().setLog(true);
                this.canvas.getCanvas(key).getPad(5).getAxisZ().setLog(true); 
            }
            else if(key.equals("Matched Roads")){
                this.canvas.getCanvas(key).getPad(0).getAxisZ().setLog(true);
                this.canvas.getCanvas(key).getPad(5).getAxisZ().setLog(true);                 
            }
        }
    }
    
    public void plotRoads() {
        for(ArrayList<Byte> key : this.dictionary.keySet()) {
            Road road = new Road(key, this.dictionary.get(key));
            double phiSec = (Math.toDegrees(road.getParticle().phi())+360+30)%60-30;
            int icharge = 0;
            if(road.getParticle().charge()<0) icharge = 1;
                this.dataGroups.get("Dictionary").getH2F("hi_ptheta_" + charges[icharge] + "_road").fill(road.getParticle().p(), Math.toDegrees(road.getParticle().theta()));
                this.dataGroups.get("Dictionary").getH2F("hi_phitheta_" + charges[icharge] + "_road").fill(phiSec, Math.toDegrees(road.getParticle().theta()));
                this.dataGroups.get("Dictionary").getH2F("hi_vztheta_" + charges[icharge] + "_road").fill(road.getParticle().vz(), Math.toDegrees(road.getParticle().theta()));
                this.dataGroups.get("Dictionary").getH2F("hi_ftofdc_" + charges[icharge] + "_road").fill(road.getKey().get(5),road.getPaddle(DetectorLayer.FTOF1B));
                this.dataGroups.get("Dictionary").getH2F("hi_pcalftof_" + charges[icharge] + "_road").fill(road.getPaddle(DetectorLayer.FTOF1B), road.getStrip(DetectorLayer.PCAL_U));
        }
    }
    
    /**
     * Test selected dictionary on input event file
     * @param fileName: input event hipo file
     * @param wireBin: dc wire smearing
     * @param pcalBin: pcal strip smearing
     * @param sectorDependence: sector-dependence mode (0=false, 1=true)
     * @param mode: test mode
     * @param maxEvents: max number of events to process
     * @param pidSelect: pid for track selection
     * @param chargeSelect: charge for track selection
     * @param thrs: momentum threshold for track selection
     * @param vzmin: minimum track vz
     * @param vzmax: maximum track vz
     */
    public void processFile(String fileName, int wireBin, int pcalBin, int sectorDependence, 
                            TestMode mode, int maxEvents, int pidSelect, int chargeSelect, double thrs,
                            double vzmin, double vzmax) {
        // testing dictionary on event file
        
        System.out.println("\nTesting dictionary on file " + fileName);
        
        ProgressPrintout progress = new ProgressPrintout();

        int pcalUSmear  = pcalBin;
        int pcalVWSmear = 0;
        if(mode.contains(TestMode.DCFTOFPCALUVW)) pcalVWSmear = pcalBin;
        
        HipoDataSource reader = new HipoDataSource();
        reader.open(fileName);
        int nevent = -1;
        while(reader.hasEvent() == true) {
            if(maxEvents>0) {
                if(nevent>= maxEvents) break;
            }
            DataEvent event = reader.getNextEvent();
            nevent++;
            
            ArrayList<Road> roads = Road.getRoads(event, chargeSelect, pidSelect, thrs, vzmin, vzmax);
            for(Road road : roads) {
                road.setBinning(wireBin, pcalBin, sectorDependence);
                if(!road.isValid(mode)) continue;
                double phi    = (Math.toDegrees(road.getParticle().phi())+180+30)%60-30;    
                int ichRoad   = (-road.getParticle().charge()+1)/2;
                Particle part = this.dictionary.get(road.getKey(mode));
                if(part != null) {
                    double phiRoad = (Math.toDegrees(road.getParticle().phi())+180+30)%60-30;
                    int ichPart = (-part.charge()+1)/2;
                    this.dataGroups.get("Matched Roads").getH2F("hi_ptheta_"   + charges[ichPart] + "_matchedroad").fill(part.p(), Math.toDegrees(part.theta()));
                    this.dataGroups.get("Matched Roads").getH2F("hi_phitheta_" + charges[ichPart] + "_matchedroad").fill(phiRoad, Math.toDegrees(part.theta()));
                    this.dataGroups.get("Matched Roads").getH2F("hi_vztheta_"  + charges[ichPart] + "_matchedroad").fill(part.vz(), Math.toDegrees(part.theta()));                    
                    this.dataGroups.get("Matched Tracks").getH2F("hi_ptheta_"   + charges[ichRoad] + "_found").fill(road.getParticle().p(), Math.toDegrees(road.getParticle().theta()));
                    this.dataGroups.get("Matched Tracks").getH2F("hi_phitheta_" + charges[ichRoad] + "_found").fill(phi, Math.toDegrees(road.getParticle().theta()));
                }
                else {
                    this.dataGroups.get("Matched Tracks").getH2F("hi_ptheta_"   + charges[ichRoad] + "_missing").fill(road.getParticle().p(), Math.toDegrees(road.getParticle().theta()));
                    this.dataGroups.get("Matched Tracks").getH2F("hi_phitheta_" + charges[ichRoad] + "_missing").fill(phi, Math.toDegrees(road.getParticle().theta()));                  
                }
            }
            progress.updateStatus();
        }
        progress.showStatus();
    }
    

    

    public static void main(String[] args) {
        
        DefaultLogger.debug();

        OptionParser parser = new OptionParser("dict-validation");
        parser.addRequired("-dict"   , "dictionary file name");
        parser.addRequired("-i"      , "event file for dictionary test");
        parser.addOption("-pid"      , "0", "select particle PID for new dictonary, 0: no selection,");
        parser.addOption("-charge"   , "0", "select particle charge for new dictionary, 0: no selection");
        parser.addOption("-wire"     , "2", "dc wire smearing in road finding");
        parser.addOption("-strip"    , "2", "pcal strip smearing in road finding");
        parser.addOption("-sector"   , "0", "sector dependent roads, 0=false, 1=true)");
        parser.addOption("-mode"     , "0", "select test mode, " + TestMode.getOptionsString());
        parser.addOption("-threshold", "1", "select roads momentum threshold in GeV");
        parser.addOption("-vzmin"  , "-10", "minimum vz (cm)");
        parser.addOption("-vzmax"  ,  "10", "maximum vz (cm)");
        parser.addOption("-n"        ,"-1", "maximum number of events to process for validation");
        parser.parse(args);
        
        List<String> arguments = new ArrayList<>();
        for(String item : args){ arguments.add(item); }
        
        String dictionaryFileName = null;
        if(parser.hasOption("-dict")==true) dictionaryFileName = parser.getOption("-dict").stringValue();
        
        String testFileName = null;
        if(parser.hasOption("-i")==true) testFileName = parser.getOption("-i").stringValue();
            
        
        int pid        = parser.getOption("-pid").intValue();
        int charge     = parser.getOption("-charge").intValue();
        if(Math.abs(charge)>1) {
            System.out.println("\terror: invalid charge selection");
            System.exit(1);
        }
        int wireBin  = parser.getOption("-wire").intValue();
        if(wireBin<0) {
            System.out.println("\terror: invalid dc wire smearing, value should be >0");
            System.exit(1);
        }
        int stripBin  = parser.getOption("-strip").intValue();
        if(stripBin<0) {
            System.out.println("\terror: invalid pcal strip smearing, value should be >0");
            System.exit(1);
        }
        int sector     = parser.getOption("-sector").intValue();
        if(sector<0 || sector>1) {
            System.out.println("\terror: invalid sector-dependence option, allowed values are 0=false or 1=true");
            System.exit(1);
        }
        TestMode mode  = TestMode.getTestMode(parser.getOption("-mode").intValue());
        if(mode == TestMode.UDF) {
            System.out.println("\terror: invalid test mode, " + TestMode.getOptionsString());
            System.exit(1);
        }
        int maxEvents  = parser.getOption("-n").intValue();
        
        double thrs    = parser.getOption("-threshold").doubleValue();
        double vzmin   = parser.getOption("-vzmin").doubleValue();
        double vzmax   = parser.getOption("-vzmax").doubleValue();
        
        System.out.println("Dictionary file name set to: " + dictionaryFileName);
        System.out.println("Event file for dictionary validation set to:  " + testFileName);
        System.out.println("PID selection for dictionary validation set to:     " + pid);
        System.out.println("Charge selection for dictionary validation set to:  " + charge);
        System.out.println("Momentum threshold set to:                          " + thrs);
        System.out.println("Vertex range set to:                                " + vzmin + ":" + vzmax);
        System.out.println("Wire binning for dictionary validation set to:      " + wireBin);
        System.out.println("Pcal binning for dictionary validation set to:      " + stripBin);
        System.out.println("Sector dependence for dictionary validation set to: " + sector);
        System.out.println("Test mode set to:                                   " + mode);
        System.out.println("Maximum number of events to process set to:         " + maxEvents);
        
        DictionaryValidator validator = new DictionaryValidator();
        validator.init(dictionaryFileName, mode, wireBin, stripBin, sector);                
    //        tm.printDictionary();
        validator.processFile(testFileName,wireBin,stripBin,sector,mode,maxEvents,pid,charge,thrs, vzmin, vzmax);
        validator.plotHistos();

        JFrame frame = new JFrame("Roads Validation");
        Dimension screensize = null;
        screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screensize.getWidth() * 0.5), (int) (screensize.getHeight() * 0.5));
        frame.add(validator.getCanvas());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    
}