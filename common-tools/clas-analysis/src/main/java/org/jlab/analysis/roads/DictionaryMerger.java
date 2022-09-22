package org.jlab.analysis.roads;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.jlab.logging.DefaultLogger;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author devita, ziegler
 */
public class DictionaryMerger {

    private Dictionary dictionary = null;            
    
    public DictionaryMerger(){

    }

    public boolean init() {
        this.dictionary = new Dictionary();
        return true;
    }
    
    
    public static void main(String[] args) {
        
        DefaultLogger.debug();

        OptionParser parser = new OptionParser("dict-merger");
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

            DictionaryMerger merger = new DictionaryMerger();
            merger.init();

            ProgressPrintout progress = new ProgressPrintout();
            int nDupli = 0;
            int nRoads = 0;
            try {
                FileWriter writer = new FileWriter(outputFile, false);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                
                for(String inputFile : inputList) {
        
                    BufferedReader txtreader = new BufferedReader(new FileReader(inputFile));

                    String line = null;
                    while ((line = txtreader.readLine()) != null) {
                        nRoads++;
                        Road road = new Road(line);
                        
                        if(merger.dictionary.containsKey(road.getKey())) {
                            nDupli++;
                            if(nDupli<10) System.out.println("WARNING: found duplicate road");
                            else if(nDupli==10) System.out.println("WARNING: reached maximum number of warnings, switching to silent mode");
                        }
                        else {
                            merger.dictionary.put(road.getKey(), road.getParticle());
                            bufferedWriter.write(road.toString());
                            bufferedWriter.newLine();
                        }
                        progress.setAsInteger("duplicates", nDupli);
                        progress.setAsInteger("good", merger.dictionary.size());
                        progress.setAsInteger("roads", nRoads);
                        progress.updateStatus();
                    }
                    txtreader.close();
                }
                progress.showStatus();
            } 
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } 
            catch (IOException e) {
                e.printStackTrace();
            } 
        }
        else {
            parser.printUsage();
            System.out.println("\n >>>> error : no dictionary specified: specify the road dictionary or choose to create it from file\n");
            System.exit(0);       
        }

    }
    
    
}