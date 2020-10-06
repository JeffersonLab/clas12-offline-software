package org.jlab.analysis.eventmerger;
import java.util.List;
import org.jlab.detector.scalers.DaqScalersSequence;
import org.jlab.jnp.hipo4.data.*;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;
import org.jlab.jnp.utils.json.JsonObject;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;
import org.jlab.utils.system.ClasUtilsFile;


/**
 * Random trigger filtering tool: filters hipo event according to trigger bit 
 * and beam current to create random-trigger files for background merging
 *  
 * Usage: triggerBitFilter -b [trigger bit] -o [output file] 
 * Options: 
 *      -c : minimum beam current (default = -1)
 *      -n : maximum number of events to process (default = -1)
 * 
 *  Event is filtered if selected trigger bit is set and no other bit is
 * 
 * @author devita
 */

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
    
    /**
     * Create Json object with filter settings
     * @param triggerBit
     * @param minCurrent
     * @return
     */
    public JsonObject settingsToJson(int triggerBit, double minCurrent){
        
        JsonObject filterData = new JsonObject();
        filterData.add("trigger-bit", triggerBit);
        filterData.add("current-threshold", minCurrent); 
        JsonObject json = new JsonObject();
        json.add("filter", filterData);
        return json;
    }
    
    /**
     * Create hipo bank with Json string saved as byte array
     * @param writer
     * @param json
     * @return
     */
    public Bank createFilterBank(HipoWriterSorted writer, JsonObject json){
        
        if(writer.getSchemaFactory().hasSchema("RUN::filter")==false) return null;
        String jsonString = json.toString();
        //create bank
        Bank bank = new Bank(writer.getSchemaFactory().getSchema("RUN::filter"), jsonString.length());
        for (int ii=0; ii<jsonString.length(); ii++) {
            bank.putByte("json",ii,(byte)jsonString.charAt(ii));
        }
        return bank;
    }

    public static void main(String[] args){
      
        OptionParser parser = new OptionParser("trigger-filter");
        parser.addRequired("-o"    ,"output file");
        parser.addRequired("-b"    ,"trigger bit (0-63)");
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
            
            if(triggerBit<0 || triggerBit>63) {
                parser.printUsage();
                System.out.println("\n >>>> error : invalid trigger bit....\n");
                System.exit(0);
            }
            
            int counter = 0;

            DaqScalersSequence chargeSeq = DaqScalersSequence.readSequence(inputList);

            //Writer
            HipoWriterSorted writer = new HipoWriterSorted();
            writer.getSchemaFactory().initFromDirectory(ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4"));
            writer.setCompressionType(2);
            writer.open(outputFile);

            RandomTriggerFilter filter = new RandomTriggerFilter(triggerBit, minCurrent);
            filter.getFcupFilter().setScalerSequence(chargeSeq);

            ProgressPrintout  progress = new ProgressPrintout();
            for(String inputFile : inputList){

                // write tag-1 events 
                SortedWriterUtils utils = new SortedWriterUtils();
//                utils.writeTag(writer, utils.SCALERTAG, inputFile);
                
                // Reader
                HipoReader reader = new HipoReader();
                reader.setTags(0);
                reader.open(inputFile);
                filter.init(reader);

                // create tag 1 event with trigger filter information
                JsonObject json = filter.settingsToJson(triggerBit, minCurrent);
                // write tag-1 event
                Event  tagEvent = new Event();
                tagEvent.write(filter.createFilterBank(writer, json));
                tagEvent.setEventTag(utils.CONFIGTAG);
                writer.addEvent(tagEvent,tagEvent.getEventTag());
                System.out.println("\nAdding tag-1 bank with filter settings...");
                System.out.println(json);

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
            
            filter.getFcupFilter().showStats();
        }
    }
}
