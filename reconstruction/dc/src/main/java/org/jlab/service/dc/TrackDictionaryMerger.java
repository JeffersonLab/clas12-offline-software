package org.jlab.service.dc;

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
import org.jlab.clas.physics.Particle;
import org.jlab.utils.options.OptionParser;

public class TrackDictionaryMerger {

    private Map<ArrayList<Byte>, Particle> dictionary = null;
    private int nlines;
    private int nfull;
    private int ndupli;            
    public static Logger LOGGER = Logger.getLogger(TrackDictionaryMerger.class.getName());
    
    public TrackDictionaryMerger(){

    }

    public Map<ArrayList<Byte>, Particle> getDictionary() {
        return dictionary;
    }
    
    public boolean init() {
        this.dictionary = new HashMap<>();
        this.nlines = 0;
        this.ndupli = 0;
        this.nfull  = 0;
        return true;
    }
    
    
    public void readDictionary(String fileName) {
        
        LOGGER.log(Level.INFO, "\nReading dictionary from file " + fileName);
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
                this.nlines++;
                String[] lineValues;
                lineValues  = line.split("\t");
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
                    Particle road = new Particle(211*charge, px, py, pz, 0, 0, vz);
                    // take wire id of first layer in each superlayer, id>0
                    for(int i=0; i<6; i++) {
                        int wire = Integer.parseInt(lineValues[4+i*6]);
                        if(wire>0) wires.add((byte) wire);
                    }
                    // keep only roads with 6 superlayers
                    if(wires.size()!=6) continue;
                    int paddle1b = Integer.parseInt(lineValues[40]);
                    int paddle2  = Integer.parseInt(lineValues[42]);
                    int pcalu    = Integer.parseInt(lineValues[43]);
                    int pcalv    = Integer.parseInt(lineValues[44]);
                    int pcalw    = Integer.parseInt(lineValues[45]);
                    int htcc     = Integer.parseInt(lineValues[46]);
                    int sector   = Integer.parseInt(lineValues[47]);
                    
                    wires.add((byte) paddle1b);
                    wires.add((byte) paddle2);
                    wires.add((byte) pcalu);
                    wires.add((byte) pcalv);
                    wires.add((byte) pcalw);
                    wires.add((byte) htcc);
                    wires.add((byte) sector);
                    double pcalE   = Double.parseDouble(lineValues[48]);
                    double ecinE   = Double.parseDouble(lineValues[49]);
                    double ecoutE  = Double.parseDouble(lineValues[50]);
                    road.setProperty("pcalE",  pcalE);
                    road.setProperty("ecinE",  ecinE);
                    road.setProperty("ecoutE", ecoutE);                    
                    nFull++;
                    this.nfull++;
                    if(this.dictionary.containsKey(wires)) {
                        nDupli++;
                        this.ndupli++;
                        if(nDupli<10) LOGGER.log(Level.INFO, "WARNING: found duplicate road");
                        else if(nDupli==10) LOGGER.log(Level.INFO, "WARNING: reached maximum number of warnings, switching to silent mode");
                    }
                    else {
                        this.dictionary.put(wires, road);
                    }
                }
                if(nLines % 1000000 == 0) LOGGER.log(Level.INFO, "Number of processed/full/duplicates in current file " + nLines + "/" + nFull + "/" + nDupli + " and in merged dictionary " 
                               + this.nlines + "/" + this.nfull + "/" + this.ndupli + ", current dictionary size: " + this.dictionary.keySet().size());
            }
            LOGGER.log(Level.INFO, "Number of processed/full/duplicates in current file " + nLines + "/" + nFull + "/" + nDupli + " and in merged dictionary " 
                               + this.nlines + "/" + this.nfull + "/" + this.ndupli + ", current dictionary size: " + this.dictionary.keySet().size());
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        catch (IOException e) {
        } 
   }
    
    private void setDictionary(Map<ArrayList<Byte>, Particle> newDictionary) {
        this.dictionary = newDictionary;
    }
    
    private void writeDictionary(String dictName) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(dictName);
            for(Map.Entry<ArrayList<Byte>, Particle> entry : this.dictionary.entrySet()) {
                ArrayList<Byte> road = entry.getKey();
                Particle        part = entry.getValue();
                if(road.size()<12) {
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
                    part.getProperty("pcalE"),part.getProperty("ecinE"),part.getProperty("ecoutE"));
                }
            }
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TrackDictionaryMakerRNG.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
       
    }
    
    
    public static void main(String[] args) {
        
        OptionParser parser = new OptionParser("dict-merger");
        parser.addOption("-o","output.txt", "output dictionary file");
        parser.parse(args);
        
        List<String> inputList = parser.getInputList();
        
        if(parser.hasOption("-o")==true){
            
            if(inputList.isEmpty()==true){
                parser.printUsage();
                LOGGER.log(Level.INFO, "\n >>>> error : no input file is specified....\n");
                System.exit(0);
            }

            String outputFile = parser.getOption("-o").stringValue();

            TrackDictionaryMerger tm = new TrackDictionaryMerger();
            tm.init();

            for(String inputFile : inputList){
                tm.readDictionary(inputFile);
                tm.writeDictionary(outputFile);

            }
        }
        else {
            parser.printUsage();
            LOGGER.log(Level.INFO, "\n >>>> error : no dictionary specified: specify the road dictionary or choose to create it from file\n");
            System.exit(0);       
        }

    }
    
    
}