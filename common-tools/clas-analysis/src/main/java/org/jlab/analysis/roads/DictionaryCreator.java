package org.jlab.analysis.roads;

import java.util.ArrayList;
import java.util.List;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

import org.jlab.utils.options.OptionParser;

public class DictionaryCreator {

    private Dictionary   dictionary = new Dictionary();
    
    
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
        System.out.println("\nCreating dictionary from file: " + inputFileName);
        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFileName);
        
        System.out.println("\nDictionary will be saved to: " + dictName); 
        
        int nevent = -1;
        while(reader.hasEvent() == true) {
            DataEvent event = reader.getNextEvent();
            nevent++;
            if(nevent%10000 == 0) System.out.println("Analyzed " + nevent + " events, found " + dictionary.size() + " roads");
            
            ArrayList<Road> roads = Road.getRoads(event, chargeSelect, pidSelect, thrs);
            for(Road road : roads) {
                if(!dictionary.containsKey(road.getRoad()))  {
                    dictionary.put(road.getRoad(), road.getParticle());
                }
            }
        }
        System.out.println("Analyzed " + nevent + " events, found " + dictionary.size() + " roads");
        dictionary.writeDictionary(dictName);
    }


    public static void main(String[] args) {
        
        OptionParser parser = new OptionParser("dict-validation");
        parser.addRequired("-o"      , "dictionary file name");
        parser.addRequired("-i"      , "event file");
        parser.addOption("-pid"      , "0", "select particle PID for new dictonary, 0: no selection,");
        parser.addOption("-charge"   , "0", "select particle charge for new dictionary, 0: no selection");
        parser.addOption("-threshold", "1", "select roads momentum threshold in GeV");
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
        int duplicates = parser.getOption("-dupli").intValue();
        if(duplicates<0 || duplicates>1) {
            System.out.println("\terror: invalid duplicate-removal option, allowed values are 0=false or 1=true");
            System.exit(1);
        }
        double thrs    = parser.getOption("-threshold").doubleValue();
        
        System.out.println("Dictionary file name set to: " + dictionaryFileName);
        System.out.println("Event file for dictionary creation set to:    " + inputFileName);
        System.out.println("PID selection for dictionary creation/validation set to:    " + pid);
        System.out.println("Charge selection for dictionary creation/validation set to: " + charge);
        System.out.println("Momentum threshold set to:                                  " + thrs);
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
        creator.createDictionary(inputFileName, dictionaryFileName, pid, charge, thrs, duplicates);
         
    }
    
    
}