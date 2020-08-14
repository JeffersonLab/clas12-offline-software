/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.analysis.eventmerger;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
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

   
    public EventMerger() {
    }

    

    public static void main(String[] args)  {
        
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
            
            EventMerger en = new EventMerger();
            ADCTDCMerger adctdcMerger = new ADCTDCMerger(detectors.split(","),doubleHits);

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
                
                adctdcMerger.updateEventWithMergedBanks(eventData, eventBg1, eventBg2);
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