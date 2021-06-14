package org.jlab.analysis.roads;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.base.DetectorLayer;

/**
 *
 * @author devita, ziegler
 */
public class Dictionary extends HashMap<byte[], Particle> {

    public Dictionary() {
    }
    
    
    public void printDictionary() {
        for(Map.Entry<byte[], Particle> entry : this.entrySet()) {
            byte[] road       = entry.getKey();
            Particle particle = entry.getValue();
            Road r = new Road(road,particle);
            System.out.println(r.toString());
        }
    }

    public void readDictionary(String fileName, int sec, int mode, double thrs) {
        
        System.out.println("\nReading dictionary from file " + fileName);
        int nLines = 0;
        int nFull  = 0;
        int nDupli = 0;
        int nfirst = 0;
        
        File fileDict = new File(fileName);
        
        try {
            BufferedReader txtreader = new BufferedReader(new FileReader(fileDict));
            
            String line = null;
            while ((line = txtreader.readLine()) != null) {
                
                nLines++;
                if(nLines % 1000000 == 0) System.out.println("Read " + nLines + " roads");
                
                Road road = new Road(line);
                if(road.getSuperLayers()!=6 || road.getParticle().p()< thrs) continue;
                road.setSector((byte) (road.getSector()*sec));
                if(mode<3) road.setHtccMask((byte) 0);
                if(mode<2) {
                    road.setStrip(DetectorLayer.PCAL_V, (byte) 0);
                    road.setStrip(DetectorLayer.PCAL_W, (byte) 0);
                }
                if(mode<1) {
                    road.setPaddle(DetectorLayer.FTOF1B, (byte) 0);
                    road.setStrip(DetectorLayer.PCAL_U, (byte) 0);
                }
                nFull++;
                if(this.containsKey(road.getRoad())) {
                    nDupli++;
                    if(nDupli<10) System.out.println("WARNING: found duplicate road");
                    else if(nDupli==10) System.out.println("WARNING: reached maximum number of warnings, switching to silent mode");
                }
                else {
                    this.put(road.getRoad(), road.getParticle());
                        
                }
            }
            txtreader.close();
            System.out.println("Found " + nLines + " roads with " + nFull + " full ones, " + nDupli + " duplicates and " + this.keySet().size() + " good ones");
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
            FileWriter writer = new FileWriter(filename, true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
 
            for(Map.Entry<byte[],Particle> entry : this.entrySet()) {
                Road road = new Road(entry.getKey(), entry.getValue());
                bufferedWriter.write(road.toString());
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
