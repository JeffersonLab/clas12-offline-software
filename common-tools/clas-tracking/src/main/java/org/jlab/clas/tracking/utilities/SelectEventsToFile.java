/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.tracking.utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;

/**
 *
 * @author ziegler
 */
public class SelectEventsToFile {
    public static void main(String[] args) throws FileNotFoundException {
    String[] splited ;
    BufferedReader reader;
    Map<Integer, Integer> evts = new HashMap<Integer, Integer>();
        try {
            reader = new BufferedReader(new FileReader("/Users/ziegler/Desktop/Base/CodeDevel/Validation/hipo4Validation/selEvts.txt"));
            try {
                String line = reader.readLine(); 
                while(line != null) {
                    splited = line.split("\\s+"); 
                    evts.put(Integer.parseInt(splited[1]), Integer.parseInt(splited[0]));
                    
                    line = reader.readLine();
                }
            } catch (IOException ex) {
                Logger.getLogger(SelectEventsToFile.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SelectEventsToFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String inputFile = "/Users/ziegler/Desktop/Base/CodeDevel/Validation/hipo4Validation/infiles/sidis.hipo";
        
        HipoDataSource hiporeader = new HipoDataSource();
        hiporeader.open(inputFile);

        HipoDataSync writer = new HipoDataSync();
        //Writer

        String outputFile = "/Users/ziegler/Desktop/Base/CodeDevel/Validation/hipo4Validation/infiles/sidisFilt.hipo";

        writer.open(outputFile);
        
        while (hiporeader.hasEvent()) {

            DataEvent event = hiporeader.getNextEvent();
            
            DataBank bank = event.getBank("RUN::config");
            
            int run = bank.getInt("run", 0);
            int evt = bank.getInt("event", 0);
            if(evts.get(evt)!=null && evts.get(evt).intValue()==run) {
                writer.writeEvent(event);
                System.out.println("PROCESSED  EVENT " + event.getBank("RUN::config").getInt("event", 0));
            }
            
        }
        writer.close();
        
    }
}
