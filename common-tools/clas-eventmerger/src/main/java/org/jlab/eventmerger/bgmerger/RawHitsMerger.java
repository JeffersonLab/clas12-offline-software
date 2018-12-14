/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.eventmerger.bgmerger;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;

public class RawHitsMerger extends ReconstructionEngine {

   
    public RawHitsMerger() {
        super("RawHitMerger","ziegler","1.0");
    }

    

    public static void main(String[] args)  {
        
        //String inputFile = args[0];
        //String outputFile = args[1];
        //String inputFile2="/Users/ziegler/Desktop/Work/Files/Data/random_4013.hipo";
        String inputFile2="/Users/ziegler/Desktop/Work/Files/Data/skimrandomnotracks.700_899.hipo";
        String inputFile="/Users/ziegler/Desktop/Work/Files/Data/out_clas_004150.evio.10_19_filt.hipo";
        
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
        String outputFile="/Users/ziegler/Desktop/Work/Files/TestMergedFile.hipo";
        String outputFile0="/Users/ziegler/Desktop/Work/Files/TestDataUnMergedFile.hipo";
        //String outputFile="/Users/ziegler/Desktop/Work/Files/FMTDevel/gemc/pion_recFMTClusNoTrkRefit.hipo";
        
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