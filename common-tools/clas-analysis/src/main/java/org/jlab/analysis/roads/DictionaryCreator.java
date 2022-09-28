package org.jlab.analysis.roads;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.logging.DefaultLogger;
import org.jlab.utils.benchmark.ProgressPrintout;

import org.jlab.utils.options.OptionParser;

public class DictionaryCreator {

    private Dictionary   dictionary = new Dictionary();
    
    
    /**
     * create dictionary from event file
     * @param inputFileName: input hipo file name
     * @param dictName: output dictionary file name
     * @param maxEvents: maximum number of events to process
     * @param pidSelect: PID for track selection
     * @param chargeSelect: charge for track selection
     * @param thrs: momentum threshold for track selection
     * @param vzmin: minimum track vz
     * @param vzmax: maximum track vz
     * @param duplicates: remove duplicate roads (0/1)
     */
    public void createDictionary(String inputFileName, String dictName, int maxEvents, int pidSelect, int chargeSelect, double thrs, double vzmin, double vzmax, int duplicates) {
        // create dictionary from event file
        System.out.println("\nCreating dictionary from file: " + inputFileName);
        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFileName);
        
        System.out.println("\nDictionary will be saved to: " + dictName); 
        
        ProgressPrintout progress = new ProgressPrintout();
        
        try {
            FileWriter writer = new FileWriter(dictName, false);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
 
            int nEvents = -1;
            while(reader.hasEvent() == true && nEvents<maxEvents) {
                nEvents++;

                DataEvent event = reader.getNextEvent();

                ArrayList<Road> roads = Road.getRoads(event, chargeSelect, pidSelect, thrs, vzmin, vzmax);
                for(Road road : roads) {
                    if(!dictionary.containsKey(road.getKey()))  {
                        dictionary.put(road.getKey(), road.getParticle());
                        bufferedWriter.write(road.toString());
                        bufferedWriter.newLine();            
                    }
                }
                progress.setAsInteger("roads", dictionary.size());
                progress.updateStatus();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        progress.showStatus();
    }


    public static void main(String[] args) {
        
        DefaultLogger.debug();

        OptionParser parser = new OptionParser("dict-validation");
        parser.addRequired("-o"      , "dictionary file name");
        parser.addRequired("-i"      , "event file");
        parser.addOption("-pid"      , "0", "select particle PID for new dictonary, 0: no selection,");
        parser.addOption("-charge"   , "0", "select particle charge for new dictionary, 0: no selection");
        parser.addOption("-threshold", "1", "select roads momentum threshold in GeV");
        parser.addOption("-vzmin"  , "-10", "minimum vz (cm)");
        parser.addOption("-vzmax"  ,  "10", "maximum vz (cm)");
        parser.addOption("-n"        ,"-1", "maximum number of events to process for validation");
        parser.addOption("-dupli"    , "1", "remove duplicates in dictionary creation, 0=false, 1=true");
        parser.parse(args);
        
        List<String> arguments = new ArrayList<String>();
        for(String item : args){ arguments.add(item); }
        
        String dictionaryFileName = null;
        if(parser.hasOption("-o")==true) dictionaryFileName = parser.getOption("-o").stringValue();
        
        String inputFileName = null;
        if(parser.hasOption("-i")==true) inputFileName = parser.getOption("-i").stringValue();
            
        int pid        = parser.getOption("-pid").intValue();
        int charge     = parser.getOption("-charge").intValue();
        if(Math.abs(charge)>1) {
            System.out.println("\terror: invalid charge selection");
            System.exit(1);
        }
        int maxEvents  = parser.getOption("-n").intValue();
        if(maxEvents<0) maxEvents = Integer.MAX_VALUE; 
        int duplicates = parser.getOption("-dupli").intValue();
        if(duplicates<0 || duplicates>1) {
            System.out.println("\terror: invalid duplicate-removal option, allowed values are 0=false or 1=true");
            System.exit(1);
        }
        double thrs    = parser.getOption("-threshold").doubleValue();
        double vzmin   = parser.getOption("-vzmin").doubleValue();
        double vzmax   = parser.getOption("-vzmax").doubleValue();
        
        System.out.println("Dictionary file name set to: " + dictionaryFileName);
        System.out.println("Event file for dictionary creation set to:    " + inputFileName);
        System.out.println("PID selection for dictionary creation/validation set to:    " + pid);
        System.out.println("Charge selection for dictionary creation/validation set to: " + charge);
        System.out.println("Momentum threshold set to:                                  " + thrs);
        System.out.println("Vertex range set to:                                        " + vzmin + ":" + vzmax);
        System.out.println("Maximum number of events to process set to:                 " + maxEvents);
        System.out.println("Duplicates remove flag set to:                              " + duplicates);
//        dictionaryFileName="/Users/devita/tracks_silvia.txt";
//        inputFileName = "/Users/devita/out_clas_003355.evio.440.hipo";
//        testFileName  = "/Users/devita/out_clas_003355.evio.440.hipo";
//        mode =2;
//        wireSmear=0;
//        maxEvents = 100000;  
        boolean debug=false;
        
        DictionaryCreator creator = new DictionaryCreator();
        creator.createDictionary(inputFileName, dictionaryFileName, maxEvents, pid, charge, thrs, vzmin, vzmax, duplicates);
         
    }
    
    
}