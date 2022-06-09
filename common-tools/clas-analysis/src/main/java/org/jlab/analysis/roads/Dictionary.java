package org.jlab.analysis.roads;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jlab.clas.physics.Particle;
import org.jlab.utils.benchmark.ProgressPrintout;

/**
 *
 * @author devita, ziegler
 */
public class Dictionary extends HashMap<ArrayList<Byte>, Particle> {
                
    public Dictionary() {
    }
    
        
    public void printDictionary() {
        for(Map.Entry<ArrayList<Byte>, Particle> entry : this.entrySet()) {
            ArrayList<Byte> road = entry.getKey();
            Particle particle = entry.getValue();
            Road r = new Road(road,particle);
            System.out.println(r.toString());
        }
    }

    public void readDictionary(String fileName, TestMode mode, int wireBinning, int stripBinning, int sectorDependence) {
        
        System.out.println("\nReading dictionary from file " + fileName);
        int nFull  = 0;
        int nDupli = 0;
        
        File fileDict = new File(fileName);
        
        try {
            BufferedReader txtreader = new BufferedReader(new FileReader(fileDict));
            
            ProgressPrintout progress = new ProgressPrintout();
            
            String line = null;
            while ((line = txtreader.readLine()) != null) {
                
                Road road = new Road(line);
                road.setBinning(wireBinning, stripBinning, sectorDependence);
                nFull++;
                if(this.containsKey(road.getKey(mode))) {
                    nDupli++;
                    if(nDupli<10) System.out.println("WARNING: found duplicate road");
                    else if(nDupli==10) System.out.println("WARNING: reached maximum number of warnings, switching to silent mode");
                }
                else {
                    this.put(road.getKey(mode), road.getParticle());
                }
                progress.setAsInteger("duplicates", nDupli);
                progress.setAsInteger("good", this.size());
                progress.setAsInteger("roads", nFull);
                progress.updateStatus();
            }
            txtreader.close();
            progress.showStatus();
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        } 
    }

    public void writeDictionary(String filename) {
        
        try {
            FileWriter writer = new FileWriter(filename, false);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
 
            for(Map.Entry<ArrayList<Byte>,Particle> entry : this.entrySet()) {
                Road road = new Road(entry.getKey(), entry.getValue());
                bufferedWriter.write(road.toString());
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public static enum TestMode {
            
        UDF(-1,                      "Undefined"),
        DC(0,                               "DC"),
        DCFTOFPCALU(1,             "DCFTOFPCALU"),
        DCFTOFPCALUVW(2,         "DCFTOFPCALUVW"),
        DCFTOFPCALUVWHTCC(3, "DCFTOFPCALUVWHTCC");

        private int mode;
        private String name;
        

        TestMode() {
            mode = -1;
            name = "UNDEFINED";
        }

        TestMode(int mode, String name) {
            this.mode = mode;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getMode() {
            return mode;
        }

        public boolean contains(TestMode mode) {
            return this.getMode() >= mode.getMode();
        }
        
        public static TestMode getTestMode(int mode) {
            switch (mode) {
                case 0:
                    return TestMode.DC;
                case 1:
                    return TestMode.DCFTOFPCALU;
                case 2:
                    return TestMode.DCFTOFPCALUVW;
                case 3:
                    return TestMode.DCFTOFPCALUVWHTCC;
                default:
                    return TestMode.UDF;
            }
        }
        
        public static String getOptionsString() {
            String s = "available options are ";
            for(int i=0; i<3; i++)
                s += i + "-" + TestMode.getTestMode(i).getName() + " " ;
            return s;
        }
    }
}
