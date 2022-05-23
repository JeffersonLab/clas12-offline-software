package org.jlab.analysis.roads;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.utils.groups.IndexedList;

import org.jlab.utils.options.OptionParser;

public class DictionaryValidator {

    private Dictionary             dictionary = new Dictionary();
    private IndexedList<DataGroup> dataGroups = new IndexedList<DataGroup>(1);
    private EmbeddedCanvasTabbed   canvas     = new EmbeddedCanvasTabbed("Dictionary", "Matched Roads", "Matched Tracks", "Efficiency");
            
    public DictionaryValidator(){

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
        System.out.println("Positive particles found/missed: " + this.dataGroups.getItem(2).getH2F("hi_ptheta_pos_found").integral() + "/" +
                                                               + this.dataGroups.getItem(2).getH2F("hi_ptheta_pos_missing").integral());
        System.out.println("Negative particles found/missed: " + this.dataGroups.getItem(2).getH2F("hi_ptheta_neg_found").integral() + "/" +
                                                               + this.dataGroups.getItem(2).getH2F("hi_ptheta_neg_missing").integral());
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
    
    private Particle findRoad(byte[] wires, int dcSmear, int pcalUSmear, int pcalVWSmear) {
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
                byte[] wiresCopy = new byte[wires.length];
                wiresCopy[0]  = (byte) (wires[0]  + k1);
                wiresCopy[1]  = (byte) (wires[1]  + k2);
                wiresCopy[2]  = (byte) (wires[2]  + k3);
                wiresCopy[3]  = (byte) (wires[3]  + k4);
                wiresCopy[4]  = (byte) (wires[4]  + k5);
                wiresCopy[5]  = (byte) (wires[5]  + k6);
                wiresCopy[6]  = (byte) (wires[6]  + k7);
                wiresCopy[7]  = (byte) 0; //panel 2
                wiresCopy[8]  = (byte) (wires[8]  + k8);
                wiresCopy[9]  = (byte) (wires[9]  + k9);
                wiresCopy[10] = (byte) (wires[10] + k10);
                wiresCopy[11] = (byte) 0; //htcc
                wiresCopy[12] = (byte) (wires[12]);
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

    public void init(String filename, int sec, int mode, double threshold) {
        this.dictionary.readDictionary(filename, sec, mode, threshold);
        this.createHistos();
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
        
        System.out.println("\nTesting dictionary on file " + fileName);
        
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
            if(nevent%10000 == 0) System.out.println("Analyzed " + nevent + " events");
            
            ArrayList<Road> roads = Road.getRoads(event, chargeSelect, pidSelect, thrs);
            
            for(Road road : roads) {
                if(sectorDependence==0) road.setSector((byte) 0);
                if(mode>0 && (road.getPaddle(DetectorLayer.FTOF1B)==0 || 
                              road.getStrip(DetectorLayer.PCAL_U)==0)) continue;
                if(mode>1 && (road.getPaddle(DetectorLayer.FTOF1B)==0 || 
                              road.getStrip(DetectorLayer.PCAL_U)==0  || 
                              road.getStrip(DetectorLayer.PCAL_V)==0  || 
                              road.getStrip(DetectorLayer.PCAL_W)==0)) continue;
                if(mode>2 && (road.getPaddle(DetectorLayer.FTOF1B)==0 || 
                              road.getStrip(DetectorLayer.PCAL_U)==0  || 
                              road.getStrip(DetectorLayer.PCAL_V)==0  || 
                              road.getStrip(DetectorLayer.PCAL_W)==0  ||
                              road.getHtccMask()==-1)) continue;
                double phi    = (Math.toDegrees(road.getParticle().phi())+180+30)%60-30;    
                Particle part = this.findRoad(road.getRoad(),wireSmear,pcalUSmear,pcalVWSmear);
                if(road != null) {
                    double phiRoad = (Math.toDegrees(road.getParticle().phi())+180+30)%60-30;
                    if(part.charge()<0) {
                        this.dataGroups.getItem(1).getH2F("hi_ptheta_neg_matchedroad").fill(part.p(), Math.toDegrees(part.theta()));
                        this.dataGroups.getItem(1).getH2F("hi_phitheta_neg_matchedroad").fill(phiRoad, Math.toDegrees(part.theta()));
                        this.dataGroups.getItem(1).getH2F("hi_vztheta_neg_matchedroad").fill(part.vz(), Math.toDegrees(part.theta()));
                    }
                    else {
                        this.dataGroups.getItem(1).getH2F("hi_ptheta_pos_matchedroad").fill(part.p(), Math.toDegrees(part.theta()));
                        this.dataGroups.getItem(1).getH2F("hi_phitheta_pos_matchedroad").fill(phiRoad, Math.toDegrees(part.theta()));                            
                        this.dataGroups.getItem(1).getH2F("hi_vztheta_pos_matchedroad").fill(part.vz(), Math.toDegrees(part.theta()));
                    }
                    if(road.getParticle().charge()==-1) {
                        this.dataGroups.getItem(2).getH2F("hi_ptheta_neg_found").fill(road.getParticle().p(), Math.toDegrees(road.getParticle().theta()));
                        this.dataGroups.getItem(2).getH2F("hi_phitheta_neg_found").fill(phi, Math.toDegrees(road.getParticle().theta()));
                    }
                    else {
                        this.dataGroups.getItem(2).getH2F("hi_ptheta_pos_found").fill(road.getParticle().p(), Math.toDegrees(road.getParticle().theta()));
                        this.dataGroups.getItem(2).getH2F("hi_phitheta_pos_found").fill(phi, Math.toDegrees(road.getParticle().theta()));
                    }
                }
                else {
                    if(road.getParticle().charge()==-1) {
                        this.dataGroups.getItem(2).getH2F("hi_ptheta_neg_missing").fill(road.getParticle().p(), Math.toDegrees(road.getParticle().theta()));
                        this.dataGroups.getItem(2).getH2F("hi_phitheta_neg_missing").fill(phi, Math.toDegrees(road.getParticle().theta()));
                    }
                    else {
                        this.dataGroups.getItem(2).getH2F("hi_ptheta_pos_missing").fill(road.getParticle().p(), Math.toDegrees(road.getParticle().theta()));
                        this.dataGroups.getItem(2).getH2F("hi_phitheta_pos_missing").fill(phi, Math.toDegrees(road.getParticle().theta()));
                    }                    
                }
            }
        }

    }
    

    

    public static void main(String[] args) {
        
        OptionParser parser = new OptionParser("dict-validation");
        parser.addRequired("-dict"   , "dictionary file name");
        parser.addRequired("-i"      , "event file for dictionary test");
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
        
        List<String> arguments = new ArrayList<String>();
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
        int wireSmear  = parser.getOption("-wire").intValue();
        if(wireSmear<0) {
            System.out.println("\terror: invalid dc wire smearing, value should be >0");
            System.exit(1);
        }
        int pcalSmear  = parser.getOption("-strip").intValue();
        if(pcalSmear<0) {
            System.out.println("\terror: invalid pcal strip smearing, value should be >0");
            System.exit(1);
        }
        int sector     = parser.getOption("-sector").intValue();
        if(sector<0 || sector>1) {
            System.out.println("\terror: invalid sector-dependence option, allowed values are 0=false or 1=true");
            System.exit(1);
        }
        int mode       = parser.getOption("-mode").intValue();
        if(mode<0 || mode>3) {
            System.out.println("\terror: invalid test mode, allowed options are 0-DC only, 1-DC-FTOF-pcalU, 2-DC-FTOF-pcalUVW, 3-DC-FTOF-pcalUVW-HTCC");
            System.exit(1);
        }
        int maxEvents  = parser.getOption("-n").intValue();
        int duplicates = parser.getOption("-dupli").intValue();
        if(duplicates<0 || duplicates>1) {
            System.out.println("\terror: invalid duplicate-removal option, allowed values are 0=false or 1=true");
            System.exit(1);
        }
        double thrs    = parser.getOption("-threshold").doubleValue();
        
        System.out.println("Dictionary file name set to: " + dictionaryFileName);
        System.out.println("Event file for dictionary validation set to:  " + testFileName);
        System.out.println("PID selection for dictionary validation set to:     " + pid);
        System.out.println("Charge selection for dictionary validation set to:  " + charge);
        System.out.println("Momentum threshold set to:                          " + thrs);
        System.out.println("Wire smearing for dictionary validation set to:     " + wireSmear);
        System.out.println("Pcal smearing for dictionary validation set to:     " + pcalSmear);
        System.out.println("Sector dependence for dictionary validation set to: " + sector);
        System.out.println("Test mode set to:                                   " + mode);
        System.out.println("Maximum number of events to process set to:         " + maxEvents);
        System.out.println("Duplicates remove flag set to:                      " + duplicates);
//        dictionaryFileName="/Users/devita/tracks_silvia.txt";
//        inputFileName = "/Users/devita/out_clas_003355.evio.440.hipo";
//        testFileName  = "/Users/devita/out_clas_003355.evio.440.hipo";
//        mode =2;
//        wireSmear=0;
//        maxEvents = 100000;  
        boolean debug=false;
        
        DictionaryValidator validator = new DictionaryValidator();
        validator.init(dictionaryFileName,sector,mode,thrs);                
    //        tm.printDictionary();
        validator.processFile(testFileName,wireSmear,pcalSmear,sector,mode,maxEvents, pid, charge,thrs);

        JFrame frame = new JFrame("Tracking");
        Dimension screensize = null;
        screensize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screensize.getWidth() * 0.8), (int) (screensize.getHeight() * 0.8));
        frame.add(validator.getCanvas());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        validator.plotHistos();
    }
    
    
}