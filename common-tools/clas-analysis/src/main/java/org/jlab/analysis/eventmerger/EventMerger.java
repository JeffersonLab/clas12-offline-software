package org.jlab.analysis.eventmerger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.logging.DefaultLogger;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;

/**
 * Tool for merging of signal and background events
 *      
 * Usage : bgMerger -b [background file] -i [input data file] -o [merged file] 
 * Options :
 *      -d : list of detectors, for example "DC,FTOF,HTCC" (default = DC,FTOF)
 *      -n : maximum number of events to process (default = -1)
 * 
 * @author ziegler
 * @author devita
 * 
 * FIXME: event tags are not preserved
 */

public class EventMerger {
   
    private boolean suppressDoubleHits = true;
    private EventMergerConstants constants = new EventMergerConstants();
    
    private List<DetectorType> detectors;
    

    public EventMerger() {
        detectors = Arrays.asList(DetectorType.DC, DetectorType.FTOF);
        printDetectors();
    }

    public EventMerger(String[] dets, boolean dhits) {
        suppressDoubleHits = dhits;
        System.out.println("Double hits suppression flag set to " + suppressDoubleHits);
        detectors = new ArrayList<>();
        for(String d : dets) detectors.add(DetectorType.getType(d));
        printDetectors();
    }

    private void printDetectors() {
        System.out.print("\nMerging activated for detectors: ");
        for(DetectorType det : detectors) System.out.print(det.getName() + " ");
        System.out.println("\n");
    }
    
    /**
     * Append merged banks to hipo event
     * 
     * @param event
     * @param bg1
     * @param bg2
     */
    public void mergeEvents(DataEvent event, DataEvent bg1, DataEvent bg2) {
        
        if(!event.hasBank("RUN::config") || !bg1.hasBank("RUN::config") || !bg2.hasBank("RUN::config")) {
            System.out.println("Missing RUN::config bank");
            return;
        }
        
        if(event.hasBank("DC::doca")) event.removeBank("DC::doca");
        
        ADCTDCMerger merger = new ADCTDCMerger(constants, event, bg1, bg2);
        merger.setSuppressDoubleHits(suppressDoubleHits);
        
        for(DetectorType det : detectors) {
            
            List<DataBank> banks = new ArrayList<>();
            List<String>   names = new ArrayList<>();
            
            if(EventMergerConstants.ADCs.contains(det)) {
                names.add(det.getName()+"::adc");
                banks.add(merger.getADCBank(det)); 
            }
            if(EventMergerConstants.TDCs.contains(det)) {
                names.add(det.getName()+"::tdc");
                banks.add(merger.getTDCBank(det));
            }
            if(banks.isEmpty())
                System.out.println("Unknown detector:" + det);
            else {
                event.removeBanks(names.toArray(String[]::new));
                event.appendBanks(banks.toArray(DataBank[]::new));
            }
        }
    }
    
    public static void main(String[] args)  {

        DefaultLogger.debug();

        OptionParser parser = new OptionParser("bg-merger");
        parser.addRequired("-o"    ,"merged file");
        parser.addRequired("-i"    ,"input data file");
        parser.addRequired("-b"    ,"background file");
        parser.setRequiresInputList(false);
        parser.addOption("-n"    ,"-1", "maximum number of events to process");
        parser.addOption("-d"    ,"DC,FTOF", "list of detectors, for example \"DC,FTOF,HTCC\"");
        parser.addOption("-s"    ,"1", "suppress double TDC hits on the same component, 0-no suppression, 1-suppression");
        parser.parse(args);
        
        if(parser.hasOption("-i")==true&&parser.hasOption("-o")==true&&parser.hasOption("-b")==true){

            String dataFile   = parser.getOption("-i").stringValue();
            String outputFile = parser.getOption("-o").stringValue();
            String bgFile     = parser.getOption("-b").stringValue();
            
            int     maxEvents  = parser.getOption("-n").intValue();
            String  detectors  = parser.getOption("-d").stringValue();
            boolean doubleHits = true;
            if(parser.getOption("-s").intValue()==0) doubleHits = false;
            
            EventMerger merger = new EventMerger(detectors.split(","),doubleHits);

            int counter = 0;

            // Readers for event and background
            HipoDataSource readerData = new HipoDataSource();
            readerData.open(dataFile);
            HipoDataSource readerBg = new HipoDataSource();
            readerBg.open(bgFile);

            //Writer
            HipoDataSync writer = new HipoDataSync();
            writer.setCompressionType(2);
            writer.open(outputFile);
            
            ProgressPrintout  progress = new ProgressPrintout();
            while (readerData.hasEvent()&& readerBg.hasEvent()) {

                counter++;

                //System.out.println("************************************************************* ");
                DataEvent eventData = readerData.getNextEvent();
                DataEvent eventBg1  = readerBg.getNextEvent();
                if(!readerBg.hasEvent()) break;
                DataEvent eventBg2  = readerBg.getNextEvent();
                
                merger.mergeEvents(eventData, eventBg1, eventBg2);
                writer.writeEvent(eventData);
                progress.updateStatus();
                if(maxEvents>0){
                    if(counter>=maxEvents) break;
                }
            }
            progress.showStatus();
            writer.close();
        }

    }

}