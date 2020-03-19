package org.jlab.analysis.eventmerger;
import java.util.List;
import org.jlab.detector.decode.DaqScalersSequence;
import org.jlab.jnp.hipo4.data.*;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;
import org.jlab.utils.system.ClasUtilsFile;



public class RandomTriggerFilter {

    FilterTrigger triggerFilter = null;
    FilterFcup fcupFilter = null;
    
    public RandomTriggerFilter(int bit, double current){
        triggerFilter = new FilterTrigger(bit);
        fcupFilter = new FilterFcup(current);
    }

    private FilterTrigger getTriggerFilter() {
        return triggerFilter;
    }

    private FilterFcup getFcupFilter() {
        return fcupFilter;
    }

    private void init(HipoReader reader) {
        triggerFilter.init(reader);
        fcupFilter.init(reader);
    }
    
    public boolean processEvent(Event event) {
        if(triggerFilter.processEvent(event)
           && fcupFilter.processEvent(event)) {
            return true;
        }
        else {
            return false;
        }
    }
    

    public static void main(String[] args){
      
        OptionParser parser = new OptionParser("triggerBitFiilter");
        parser.addRequired("-o"    ,"output file");
        parser.addRequired("-b"    ,"trigger bit");
        parser.setRequiresInputList(false);
        parser.addOption("-c"    ,"-1", "minimum beam current");
        parser.addOption("-n"    ,"-1", "maximum number of events to process");
        parser.parse(args);

        List<String> inputList = parser.getInputList();

        if(parser.hasOption("-o")==true&&parser.hasOption("-b")==true){

            String outputFile = parser.getOption("-o").stringValue();
            int triggerBit    = parser.getOption("-b").intValue();            
            double minCurrent = parser.getOption("-c").doubleValue();
            int maxEvents     = parser.getOption("-n").intValue();

            if(inputList.isEmpty()==true){
                parser.printUsage();
                System.out.println("\n >>>> error : no input file is specified....\n");
                System.exit(0);
            }
            
            int counter = 0;

            DaqScalersSequence chargeSeq = DaqScalersSequence.readSequence(inputList);

            //Writer
            HipoWriterSorted writer = new HipoWriterSorted();
            writer.getSchemaFactory().initFromDirectory(ClasUtilsFile.getResourceDir("COATJAVA", "etc/bankdefs/hipo4"));
            writer.setCompressionType(1);
            writer.open(outputFile);

            RandomTriggerFilter filter = new RandomTriggerFilter(triggerBit, minCurrent);
            filter.getFcupFilter().setScalerSequence(chargeSeq);

            ProgressPrintout  progress = new ProgressPrintout();
            for(String inputFile : inputList){
                // Reader
                HipoReader reader = new HipoReader();
                reader.setTags(0);
                reader.open(inputFile);
                filter.init(reader);

                while (reader.hasNext()) {
                    
                    Event event = new Event();
                    reader.nextEvent(event);
            
                    counter++;
                    
                    if(filter.processEvent(event)) {
                        writer.addEvent(event, event.getEventTag());
                    }
                    progress.updateStatus();
                    if(maxEvents>0){
                        if(counter>maxEvents) break;
                    }
                }
                progress.showStatus();
            }
            writer.close();
        }
    }
}
