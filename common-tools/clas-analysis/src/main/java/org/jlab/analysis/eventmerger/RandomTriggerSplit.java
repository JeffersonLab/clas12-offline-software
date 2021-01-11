package org.jlab.analysis.eventmerger;
import java.util.List;
import org.jlab.jnp.hipo4.data.*;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;
import org.jlab.utils.system.ClasUtilsFile;

/**
 * Random trigger file splitting tool: splits a list of input files 
 * into output files with a given number of tag-0 events
 * 
 * Usage : fileSplitter -o [output file prefix] 
 * Options :
 *      -n : maximum number of events to process (default = -1)
 *      -s : number of events per output file (default = -1)
 * 
 * @author devita
 */
public class RandomTriggerSplit {

    
    public RandomTriggerSplit(){
    }

    private HipoWriterSorted openOutputFile(String outputfile){
        HipoWriterSorted writer = null;
        writer = new HipoWriterSorted();
        writer.getSchemaFactory().initFromDirectory(ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4"));
        writer.setCompressionType(2);
        writer.open(outputfile);
        System.out.println("Open output file " + outputfile);
        return writer;
    }

    public static void main(String[] args){
      
        OptionParser parser = new OptionParser("trigger-splitter");
        parser.addOption("-o"    ,"",   "output file prefix");
        parser.addOption("-n"    ,"-1", "maximum number of events to process");
        parser.addOption("-s"    ,"-1", "number of events per output file");
        parser.parse(args);

        List<String> inputList = parser.getInputList();

        if(parser.hasOption("-o")==true){

            String outputFile = parser.getOption("-o").stringValue();
            int maxInEvents   = parser.getOption("-n").intValue();
            int maxOutEvents  = parser.getOption("-s").intValue();

            if(inputList.isEmpty()==true){
                parser.printUsage();
                System.out.println("\n >>>> error : no input file is specified....\n");
                System.exit(0);
            }
            
            int eventCounter = 0;
            int fileCounter  = 1;

            //Writer
            HipoWriterSorted writer = null;
            SortedWriterUtils utils = new SortedWriterUtils();
            
            RandomTriggerSplit splitter = new RandomTriggerSplit();

            ProgressPrintout  progress = new ProgressPrintout();
            for(String inputFile : inputList){
                // Reader
                HipoReader reader = new HipoReader();
                reader.setTags(0);
                reader.open(inputFile);
                
                while (reader.hasNext()) {
                    // open/close ouputfiles
                    if(((eventCounter%maxOutEvents)==0 && maxOutEvents!=-1) || writer==null) {
                        if(writer!=null) writer.close();
                        String filename = String.format("%05d", fileCounter) + ".hipo";
                        if(!outputFile.equals("")) filename = outputFile + "_" + filename;
                        writer = splitter.openOutputFile(filename);
                        utils.writeTag(writer, utils.SCALERTAG, inputFile);
                        utils.writeTag(writer, utils.CONFIGTAG, inputFile);
                        fileCounter++;
                    }
                    Event event = new Event();
                    reader.nextEvent(event);
            
                    eventCounter++;
                    writer.addEvent(event, event.getEventTag());
                    
                    progress.updateStatus();
                    if(maxInEvents>0){
                        if(eventCounter>maxInEvents) break;
                    }
                }
                progress.showStatus();
            }
            writer.close();
        }
    }
}
