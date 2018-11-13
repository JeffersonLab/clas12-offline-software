package org.jlab.service.dc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.clas.physics.Particle;
import org.jlab.utils.options.OptionParser;

public class TrackDictionaryMerger {

    private Map<ArrayList<Integer>, String> dictionary = null;
            
    public TrackDictionaryMerger(){

    }

    public Map<ArrayList<Integer>, String> getDictionary() {
        return dictionary;
    }
    
    public boolean init() {
        this.dictionary = new HashMap<>();
        return true;
    }
        
    public void printDictionary(String fileName) throws IOException {
        if(this.dictionary !=null) {
            File fileDict = new File(fileName);
            BufferedWriter txtwriter = null;
            try {
                txtwriter = new BufferedWriter(new FileWriter(fileDict));
                for(Map.Entry<ArrayList<Integer>, String> entry : this.dictionary.entrySet()) {
                    ArrayList<Integer> wires = entry.getKey();
                    String road = entry.getValue();
                    txtwriter.write(road);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            } 
        }
    }
    
    
    public void readDictionary(String fileName) {
        
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
                    // keep only roads with 6 superlayers
                    if(wires.size()!=6) continue;
                    int paddle1b = Integer.parseInt(lineValues[40]);
                    int paddle2  = Integer.parseInt(lineValues[42]);
                    int pcalu    = Integer.parseInt(lineValues[43]);
                    nFull++;
                    if(this.dictionary.containsKey(wires)) {
                        nDupli++;
                        if(nDupli<10) System.out.println("WARNING: found duplicate road");
                        else if(nDupli==10) System.out.println("WARNING: reached maximum number of warnings, switching to silent mode");
                    }
                    else {
                        this.dictionary.put(wires, line);
                    }
                }
                if(nLines % 1000000 == 0) System.out.println("Read " + nLines + " roads with " + nFull + " full ones, " + nDupli + " duplicates and " + this.dictionary.keySet().size() + " good ones");
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
    
    private void setDictionary(Map<ArrayList<Integer>, String> newDictionary) {
        this.dictionary = newDictionary;
    }
    

    public static void main(String[] args) {
        
        OptionParser parser = new OptionParser("dict-validation");
        parser.addOption("-o","output.txt", "output dictionary file");
        parser.parse(args);
        
        List<String> inputList = parser.getInputList();
        
        if(parser.hasOption("-o")==true){
            
            if(inputList.isEmpty()==true){
                parser.printUsage();
                System.out.println("\n >>>> error : no input file is specified....\n");
                System.exit(0);
            }

            String outputFile = parser.getOption("-o").stringValue();

            TrackDictionaryMerger tm = new TrackDictionaryMerger();
            tm.init();

            for(String inputFile : inputList){
                tm.readDictionary(inputFile);
            }
        }
        else {
            parser.printUsage();
            System.out.println("\n >>>> error : no dictionary specified: specify the road dictionary or choose to create it from file\n");
            System.exit(0);       
        }

    }
    
    
}