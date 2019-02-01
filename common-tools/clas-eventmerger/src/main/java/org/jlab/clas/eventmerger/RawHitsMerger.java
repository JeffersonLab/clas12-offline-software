/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.eventmerger;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.utils.options.OptionParser;

public class RawHitsMerger extends ReconstructionEngine {

   
    public RawHitsMerger() {
        super("RawHitsMerger","ziegler","1.0");
    }

    

    public static void main(String[] args)  {
        
        String inputFile2= null;//="/Users/ziegler/Desktop/Work/Files/Data/skimrandomnotracks.700_899.hipo";
        String inputFile = null;//="/Users/ziegler/Desktop/Work/Files/Data/out_clas_004150.evio.10_19_filt.hipo";
        String outputFile= null;//"/Users/ziegler/Desktop/Work/Files/TestMergedFile.hipo";
        String outputFile0= null; //"/Users/ziegler/Desktop/Work/Files/TestDataUnMergedFile.hipo";
         
        OptionParser parser = new OptionParser("event-merger");
        parser.addOption("-isignal","");
        parser.addOption("-ibkg","");
        parser.addOption("-osignal","");
        parser.addOption("-omerged","");
        parser.parse(args);
        
        if(parser.hasOption("-isignal")==true && 
                parser.hasOption("-ibkg")==true && 
                parser.hasOption("-osignal")==true && 
                parser.hasOption("-omerged")==true){
            
            inputFile   = parser.getOption("-isignal").stringValue();
            inputFile2  = parser.getOption("-ibkg").stringValue();
            outputFile0 = parser.getOption("-osignal").stringValue();
            outputFile  = parser.getOption("-omerged").stringValue();
        } else {
            System.err.println("Error parsing file names...correct syntax is:");
            System.err.println("./bin/event-merger -isignal myNoBackgoundDataFile -ibkg myBackgroundFile -osignal myOutputNoBackgroundAnalysisSample -omerged myMergedDataSample");
        }
        
        //System.err.println(" \n[PROCESSING FILE] : " + inputFile);
        
        RawHitsMerger en = new RawHitsMerger();
        en.init();
        ADCTDCMerger aDCTDCMerge = new ADCTDCMerger();
       
        int counter = 0;
        
        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);
        HipoDataSource reader2 = new HipoDataSource();
        reader2.open(inputFile2);
        
        //Writer
        HipoDataSync writer0 = new HipoDataSync();
        HipoDataSync writer = new HipoDataSync();
        
        
        writer0.open(outputFile0);
        writer.open(outputFile);
        long t1 = 0;
        while (reader.hasEvent() && reader2.hasEvent()) {
            
            //System.out.println("************************************************************* ");
            DataEvent event = reader.getNextEvent();
            DataEvent event2 = reader2.getNextEvent();
            if(event2.hasBank("TimeBasedTrkg::TBTracks"))
                continue;
            //if(event.hasBank("DC::tdc")==false || event.hasBank("FTOF::tdc")==false || event2.hasBank("DC::tdc")==false || event2.hasBank("FTOF::tdc")==false) {
            //    continue;
            //}
            
            if(event.hasBank("DC::tdc")==true ) {
                if(event.getBank("DC::tdc").rows()>0 ){
                //event.getBank("BST::adc").show();
                //event2.getBank("BST::adc").show();
                //event.getBank("BMT::adc").show();
                //event2.getBank("BMT::adc").show();
                counter++;
                //event.show();
                //System.out.println("********************** ");
                writer0.writeEvent(event);
                aDCTDCMerge.updateEventWithMergedBanks(event, event2);
                writer.writeEvent(event);
                if (counter > 0) {
                    t1 = System.currentTimeMillis();
                }
                //if(counter>11)
                //    break;
                //event.show();
               
                }
        }
        }
        writer0.close();
        writer.close();
        double t = System.currentTimeMillis() - t1;
        System.out.println(t1 + " TOTAL  PROCESSING TIME = " + (t / (float) counter));
    }

    @Override
    public boolean processDataEvent(DataEvent de) {
        return true;
    }

    @Override
    public boolean init() {
        return true;
    }

}