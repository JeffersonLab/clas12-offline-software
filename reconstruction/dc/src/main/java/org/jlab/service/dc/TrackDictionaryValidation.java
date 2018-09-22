package org.jlab.service.dc;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import org.jlab.clas.physics.Particle;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.utils.groups.IndexedList;

import org.jlab.utils.options.OptionParser;

public class TrackDictionaryValidation {

    private Map<ArrayList<Integer>, Particle> dictionary = null;
    private IndexedList<DataGroup>           dataGroups = new IndexedList<DataGroup>(1);
    private EmbeddedCanvasTabbed             canvas     = new EmbeddedCanvasTabbed("Dictionary", "Matched Roads", "Matched Tracks", "Efficiency");
            
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
    }
    
    public void createDictionary(String inputFileName) {
        // create dictionary from event file
        System.out.println("\nCreating dictionary from file: " + inputFileName);
        Map<ArrayList<Integer>, Particle> newDictionary = new HashMap<>();
        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFileName);
        int nevent = -1;
        while(reader.hasEvent() == true) {
            DataEvent event = reader.getNextEvent();
            nevent++;
            if(nevent%10000 == 0) System.out.println("Analyzed " + nevent + " events");
            DataBank recTrack = null;
            DataBank recHits = null;
            if (event.hasBank("TimeBasedTrkg::TBTracks")) {
                recTrack = event.getBank("TimeBasedTrkg::TBTracks");
            }
            if (event.hasBank("TimeBasedTrkg::TBHits")) {
                recHits = event.getBank("TimeBasedTrkg::TBHits");
            }
            if (recTrack != null && recHits != null) {
                for (int i = 0; i < recTrack.rows(); i++) {
                    int charge = recTrack.getByte("q",i);
                     Particle part = new Particle(
                                        charge*211,
                                        recTrack.getFloat("p0_x", i),
                                        recTrack.getFloat("p0_y", i),
                                        recTrack.getFloat("p0_z", i),
                                        recTrack.getFloat("Vtx0_x", i),
                                        recTrack.getFloat("Vtx0_y", i),
                                        recTrack.getFloat("Vtx0_z", i));
                    int[] wireArray = new int[36];
                    for (int j = 0; j < recHits.rows(); j++) {
                        if (recHits.getByte("trkID", j) == recTrack.getShort("id", i)) {
                            int sector = recHits.getByte("sector", j);
                            int superlayer = recHits.getByte("superlayer", j);
                            int layer = recHits.getByte("layer", j);
                            int wire = recHits.getShort("wire", j);
                            wireArray[(superlayer - 1) * 6 + layer - 1] = wire;
                        }
                    }
                    ArrayList<Integer> wires = new ArrayList<Integer>();
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
                        if(!newDictionary.containsKey(wires))  {
                            newDictionary.put(wires, part);
                        }   
                    }
                }
            }
        }
        this.setDictionary(newDictionary);
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
        H2F hi_pcalftof_pos_road = new H2F("hi_pcalftof_pos_road", "hi_pcalftof_pos_road", 65, 0.0, 65.0, 70, 0.0, 70.0);       
        hi_pcalftof_pos_road.setTitleX("FTOF paddle");
        hi_pcalftof_pos_road.setTitleY("PCAL strip");
        DataGroup dRoads  = new DataGroup(4,2);
        dRoads.addDataSet(hi_ptheta_neg_road,   0);
        dRoads.addDataSet(hi_phitheta_neg_road, 1);
        dRoads.addDataSet(hi_vztheta_neg_road,  2);
        dRoads.addDataSet(hi_pcalftof_neg_road, 3);
        dRoads.addDataSet(hi_ptheta_pos_road,   4);
        dRoads.addDataSet(hi_phitheta_pos_road, 5);
        dRoads.addDataSet(hi_vztheta_pos_road,  6);
        dRoads.addDataSet(hi_pcalftof_pos_road, 7);
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
    
    private Particle findRoad(ArrayList<Integer> wires, int smear) {
        Particle foundRoad = null;
        if(smear>0) {
            for(int k1=-smear; k1<=smear; k1++) {
            for(int k2=-smear; k2<=smear; k2++) {
            for(int k3=-smear; k3<=smear; k3++) {
            for(int k4=-smear; k4<=smear; k4++) {
            for(int k5=-smear; k5<=smear; k5++) {
            for(int k6=-smear; k6<=smear; k6++) {
                ArrayList<Integer> wiresCopy = new ArrayList(wires);
                wiresCopy.set(0, wires.get(0) + k1);
                wiresCopy.set(1, wires.get(1) + k2);
                wiresCopy.set(2, wires.get(2) + k3);
                wiresCopy.set(3, wires.get(3) + k4);
                wiresCopy.set(4, wires.get(4) + k5);
                wiresCopy.set(5, wires.get(5) + k6);
                if(this.dictionary.containsKey(wiresCopy)) {
                    foundRoad=this.dictionary.get(wiresCopy);
                    break;
                }
            }}}}}}
        }
        else {
            if(this.dictionary.containsKey(wires)) foundRoad=this.dictionary.get(wires);
        } 
        return foundRoad;
    }
    
    public EmbeddedCanvasTabbed getCanvas() {
        return canvas;
    }

    public Map<ArrayList<Integer>, Particle> getDictionary() {
        return dictionary;
    }
    
    public boolean init() {
        this.createHistos();
        return true;
    }
    
    public void plotHistos() {
        this.analyzeHistos();
        this.canvas.getCanvas("Dictionary").divide(4, 2);
        this.canvas.getCanvas("Dictionary").setGridX(false);
        this.canvas.getCanvas("Dictionary").setGridY(false);
        this.canvas.getCanvas("Dictionary").draw(dataGroups.getItem(0));
        this.canvas.getCanvas("Dictionary").getPad(0).getAxisZ().setLog(true);
        this.canvas.getCanvas("Dictionary").getPad(4).getAxisZ().setLog(true);
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
            for(Map.Entry<ArrayList<Integer>, Particle> entry : this.dictionary.entrySet()) {
                ArrayList<Integer> wires = entry.getKey();
                Particle road = entry.getValue();
                for(int wire: wires) System.out.print(wire + " ");
                System.out.println(road.charge() + " " + road.p() + " " + Math.toDegrees(road.theta()) + " " + Math.toDegrees(road.phi()) + " " + road.vz());
            }
        }
    }
    public void processFile(String fileName, int wireSmear, int maxEvents) {
        // testing dictionary on event file
        
        System.out.println("\nTesting dictionary on file " + fileName);

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
            DataBank recTrack = null;
            DataBank recHits = null;
            DataBank recFtof = null;
            DataBank mcPart  = null;
            if (event.hasBank("TimeBasedTrkg::TBTracks")) {
                recTrack = event.getBank("TimeBasedTrkg::TBTracks");
            }
            if (event.hasBank("TimeBasedTrkg::TBHits")) {
                recHits = event.getBank("TimeBasedTrkg::TBHits");
            }
            if (event.hasBank("FTOF::hits")) {
                recHits = event.getBank("TimeBasedTrkg::TBHits");
            }
            if (event.hasBank("MC::Particle")) {
                mcPart = event.getBank("MC::Particle");
            }
            if (recTrack != null && recHits != null) {
                for (int i = 0; i < recTrack.rows(); i++) {
                    int charge = recTrack.getByte("q",i);
                    Particle part = new Particle(
                                        charge*211,
                                        recTrack.getFloat("p0_x", i),
                                        recTrack.getFloat("p0_y", i),
                                        recTrack.getFloat("p0_z", i),
                                        recTrack.getFloat("Vtx0_x", i),
                                        recTrack.getFloat("Vtx0_y", i),
                                        recTrack.getFloat("Vtx0_z", i));
                    boolean goodTrack=true;
                    // neglect tracks with bad vertex
                    if(Math.abs(part.vz())>10) goodTrack=false;
                    // for mc events, use only well reconstructed tracks
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
                    int[] wireArray = new int[36];
                    int nSL3=0;
                    for (int j = 0; j < recHits.rows(); j++) {
                        if (recHits.getByte("trkID", j) == recTrack.getShort("id", i)) {
                            int sector = recHits.getByte("sector", j);
                            int superlayer = recHits.getByte("superlayer", j);
                            int layer = recHits.getByte("layer", j);
                            int wire = recHits.getShort("wire", j);
                            wireArray[(superlayer - 1) * 6 + layer - 1] = wire;
                            if(superlayer==3) nSL3++;
                        }
                    }
                    if(nSL3<3) continue; //ignore tracks with less than 3 hits in SL3 as in dictionary maker
                    ArrayList<Integer> wires = new ArrayList<Integer>();
                    for (int k = 0; k < 6; k++) {
                        for (int l=0; l<1; l++) {
                            // use first non zero wire in superlayer
                            if(wireArray[k*6 +l] != 0) {
                               wires.add(wireArray[k*6+l]);
                               break;
                            }
                        }
                    }
                    // use only tracks with 6 superlayers
                    if(wires.size()==6) {
                        double phi     = (Math.toDegrees(part.phi())+180+30)%60-30;                        
                        Particle road = this.findRoad(wires,wireSmear);
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
    
    
    public void readDictionary(String fileName) {
        
        this.dictionary = new HashMap<>();
        
        System.out.println("\nReading dictionary from file " + fileName);
        int nLines = 0;
        int nFull  = 0;
        int nDupli = 0;
        
        File fileDict = new File(fileName);
        BufferedReader txtreader = null;
        try {
            txtreader = new BufferedReader(new FileReader(fileDict));
            String line = null;
            while ((line = txtreader.readLine()) != null) {
                nLines++;
                if(nLines % 1000000 == 0) System.out.println("Read " + nLines + " roads");
                String[] lineValues;
                lineValues  = line.split("\t");
                ArrayList<Integer> wires = new ArrayList<Integer>();
                if(lineValues.length < 42) {
                    System.out.println("WARNING: dictionary line " + nLines + " incomplete: skipping");
                }
                else {
//                    System.out.println(line);
                    int charge   = Integer.parseInt(lineValues[0]);
                    double p     = Double.parseDouble(lineValues[1]);
                    double theta = Double.parseDouble(lineValues[2]);
                    double phi   = Double.parseDouble(lineValues[3]);
                    double vz    = Double.parseDouble(lineValues[41]);
                    double px    = p*Math.sin(Math.toRadians(theta))*Math.cos(Math.toRadians(phi));
                    double py    = p*Math.sin(Math.toRadians(theta))*Math.sin(Math.toRadians(phi));
                    double pz    = p*Math.cos(Math.toRadians(theta));
                    Particle road = new Particle(211*charge, px, py, pz, 0, 0, vz);
                    // take wire id of first layer in each superlayer, id>0
                    for(int i=0; i<6; i++) {
                        int wire = Integer.parseInt(lineValues[4+i*6]);
                        if(wire>0) wires.add(wire);
                    }
                    int paddle1b = Integer.parseInt(lineValues[40]);
                    int paddle2  = Integer.parseInt(lineValues[42]);
                    int pcalu    = Integer.parseInt(lineValues[43]);                    
                    // keep only roads with 6 superlayers
                    if(wires.size()!=6) continue;
//                    // keep only roads with ftof
//                    if(paddle1b==0) continue;
                    nFull++;
                    if(this.dictionary.containsKey(wires)) {
                        nDupli++;
                        if(nDupli<10) System.out.println("WARNING: found duplicate road");
                        else if(nDupli==10) System.out.println("WARNING: reached maximum number of warnings, switching to silent mode");
                    }
                    else {
                        this.dictionary.put(wires, road);
                        double phiSec = (Math.toDegrees(road.phi())+180+30)%60-30;
                        if(road.charge()<0) {
                            this.dataGroups.getItem(0).getH2F("hi_ptheta_neg_road").fill(road.p(), Math.toDegrees(road.theta()));
                            this.dataGroups.getItem(0).getH2F("hi_phitheta_neg_road").fill(phiSec, Math.toDegrees(road.theta()));
                            this.dataGroups.getItem(0).getH2F("hi_vztheta_neg_road").fill(road.vz(), Math.toDegrees(road.theta()));
                            if(paddle1b>0) this.dataGroups.getItem(0).getH2F("hi_pcalftof_neg_road").fill(paddle1b, pcalu);
                        }
                        else {
                            this.dataGroups.getItem(0).getH2F("hi_ptheta_pos_road").fill(road.p(), Math.toDegrees(road.theta()));
                            this.dataGroups.getItem(0).getH2F("hi_phitheta_pos_road").fill(phiSec, Math.toDegrees(road.theta()));                            
                            this.dataGroups.getItem(0).getH2F("hi_vztheta_pos_road").fill(road.vz(), Math.toDegrees(road.theta()));
                            if(paddle1b>0) this.dataGroups.getItem(0).getH2F("hi_pcalftof_pos_road").fill(paddle1b, pcalu);
                        }
                    }
                }
            }
            System.out.println("Found " + nLines + " roads with " + nFull + " full ones, " + nDupli + " duplicates and " + this.dictionary.keySet().size() + " good ones");
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        } 
   }
    
    private void setDictionary(Map<ArrayList<Integer>, Particle> newDictionary) {
        this.dictionary = newDictionary;
    }
    

    public static void main(String[] args) {
        
        OptionParser parser = new OptionParser("dict-validation");
        parser.addOption("-d","dictionary.txt", "read dictionary from file");
        parser.addOption("-c","input.hipo", "create dictionary from event file");
        parser.addOption("-i","set event file for dictionary validation");
        parser.addOption("-w", "0", "wire smearing in road finding");
        parser.addOption("-n", "-1", "maximum number of events to process");
        parser.parse(args);
        
        List<String> arguments = new ArrayList<String>();
        for(String item : args){ arguments.add(item); }
        
        String dictionaryFileName = null;
        if(parser.hasOption("-d")==true){
            dictionaryFileName = parser.getOption("-d").stringValue();
        }
        String inputFileName = null;
        if(parser.hasOption("-c")==true){
            inputFileName = parser.getOption("-c").stringValue();
        }
        String testFileName = null;
        if(parser.hasOption("-i")==true){
            testFileName = parser.getOption("-i").stringValue();
        }
        int wireSmear = parser.getOption("-w").intValue();
        int maxEvents = parser.getOption("-n").intValue();
            
//        dictionaryFileName="/Users/devita/TracksDic_test.txt";
//        inputFileName = "/Users/devita/out_clas_004013.0.9.hipo";
//        testFileName  = "/Users/devita/out_clas_004013.0.9.hipo";
//        testFileName  = "/Users/devita/clas12_pi.hipo";
//        wireSmear=0;
//        maxEvents = 100000;  
        boolean debug=false;
        
        TrackDictionaryValidation tm = new TrackDictionaryValidation();
        tm.init();
        if(parser.containsOptions(arguments, "-c") || parser.containsOptions(arguments, "-d") || debug) {
            if(parser.containsOptions(arguments, "-c")) {
                tm.createDictionary(inputFileName);
            }
            else if(parser.hasOption("-d")==true || debug) {
                tm.readDictionary(dictionaryFileName);                
            }
    //        tm.printDictionary();
            tm.processFile(testFileName,wireSmear,maxEvents);

            JFrame frame = new JFrame("Tracking");
            Dimension screensize = null;
            screensize = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setSize((int) (screensize.getWidth() * 0.8), (int) (screensize.getHeight() * 0.8));
            frame.add(tm.getCanvas());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            tm.plotHistos();
        }
        else {
            parser.printUsage();
            System.out.println("\n >>>> error : no dictionary specified: specify the road dictionary or choose to create it from file\n");
            System.exit(0);       
        }

    }
    
    
}