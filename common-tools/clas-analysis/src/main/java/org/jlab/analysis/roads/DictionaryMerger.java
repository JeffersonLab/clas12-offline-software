package org.jlab.analysis.roads;

import java.util.List;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author devita, ziegler
 */
public class DictionaryMerger {

    private Dictionary dictionary = null;
    private int nlines;
    private int nfull;
    private int ndupli;            
    
    public DictionaryMerger(){

    }

    public boolean init() {
        this.dictionary = new Dictionary();
        this.nlines = 0;
        this.ndupli = 0;
        this.nfull  = 0;
        return true;
    }
    
    
    public void readDictionary(String filename) {
        this.dictionary.readDictionary(filename, 1, 3, 0);
    }
    
    public void writeDictionary(String filename) {
        this.dictionary.writeDictionary(filename);
    }
    
    
    public static void main(String[] args) {
        
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

            for(String inputFile : inputList){
                merger.readDictionary(inputFile);
                merger.writeDictionary(outputFile);

            }
        }
        else {
            parser.printUsage();
            System.out.println("\n >>>> error : no dictionary specified: specify the road dictionary or choose to create it from file\n");
            System.exit(0);       
        }

    }
    
    
}