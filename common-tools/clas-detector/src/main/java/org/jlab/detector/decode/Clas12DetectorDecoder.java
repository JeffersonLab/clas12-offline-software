/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.decode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataDictionary;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author gavalian
 */
public class Clas12DetectorDecoder {
    
    private ConstantsManager  translationManager = new ConstantsManager();
    private Map<String,AbsDetectorDataDecoder>  detectorDecoders = 
            new LinkedHashMap<String,AbsDetectorDataDecoder>();
            
    
    private CodaEventDecoder          codaDecoder = null;    
    private HipoDataSync                 dataSync = null;
            
    public Clas12DetectorDecoder(){
        codaDecoder = new CodaEventDecoder();
        dataSync    = new HipoDataSync();
    }
    
    public void addDecoder(AbsDetectorDataDecoder decoder){
        if(detectorDecoders.containsKey(decoder.getName())==true){
            System.out.println("DetectorDetector : entry with [" +
                    decoder.getName() + "] already exists.");
        } else {
            detectorDecoders.put(decoder.getName(), decoder);
        }
    }
    
    public void init(){
        List<String>   keys = new ArrayList<String>();
        List<String> tables = new ArrayList<String>();
        
        for(Map.Entry<String,AbsDetectorDataDecoder> decoder : detectorDecoders.entrySet()){
            System.out.println(" aa = " + decoder.getKey());
            if(decoder.getValue().getTable()!=null){
                keys.add(decoder.getValue().getName());
                tables.add(decoder.getValue().getTable());
            } else {
                System.out.println("[Clas12DetectorDecoder] skipping module : " + decoder.getKey());
            }
        }
        translationManager.init(keys, tables);
    }
    
    /**
     * create specialty banks such as header, trigger and RF banks
     * @return list of banks to be appended to the event
     */
    public List<DataBank>  createHeaderBanks(){
        
        return new ArrayList<DataBank>();
    }
    /**
     * decode the event by using all abstract modules for detector event decoding.
     * @param event EvioEvent object containing raw CODA data
     * @return HipoEvent object with translated banks
     */
    public DataEvent decodeEvent(DataEvent event){
        DataEvent outputEvent = dataSync.createEvent();
        if(event instanceof EvioDataEvent){
            try {
                List<DetectorDataDgtz> dataList = codaDecoder.getDataEntries( (EvioDataEvent) event);
                
                for(Map.Entry<String,AbsDetectorDataDecoder> entry : detectorDecoders.entrySet()){
                    try {
                        int run = codaDecoder.getRunNumber();
                        IndexedTable   table = translationManager.getConstants(run,entry.getValue().getName());
                        if(table!=null){
                            entry.getValue().setConstantsTable(table);
                            List<DataBank> banks = entry.getValue().createBanks(dataList,outputEvent);
                            for(DataBank bank : banks){
                                if(bank.rows()>0){
                                    outputEvent.appendBank(bank);
                                }
                            }
                        } else {
                            System.out.println("*** error *** unable to get translation table for run = "
                                    + run);
                        }
                    } catch(Exception e){
                        System.out.println("***** decoder error **** exception in module : " + entry.getKey());
                        e.printStackTrace();
                    }
                }
                
            } catch (Exception e){
                
            }
        }
        /**
         * This part creates the specialty banks, i.e. header banks
         * trigger banks and anything in between
         */
        try {
            List<DataBank> banks = this.createHeaderBanks();
             for(DataBank bank : banks){
                 if(bank.rows()>0){
                     outputEvent.appendBank(bank);
                 }
             }
        } catch (Exception e) {
            System.out.println("*** error *** something went wrong when creating header banks");
        }
        
        return outputEvent;
    }
    
    public static Clas12DetectorDecoder getInstance(){
        Clas12DetectorDecoder decoder = new Clas12DetectorDecoder();
        decoder.addDecoder(new AbsDetectorDataDecoder("ECAL","/daq/tt/ec"));
        decoder.addDecoder(new AbsDetectorDataDecoder("FTOF","/daq/tt/ftof"));
        decoder.addDecoder(new AbsDetectorDataDecoder("CTOF","/daq/tt/ctof"));
        decoder.addDecoder(new AbsDetectorDataDecoder("FTHODO","/daq/tt/fthodo"));
        decoder.addDecoder(new AbsDetectorDataDecoder("DC","/daq/tt/dc"));
        decoder.addDecoder(new AbsDetectorDataDecoder("FTCAL","/daq/tt/ftcal"));
        decoder.addDecoder(new AbsDetectorDataDecoder("LTCC","/daq/tt/ltcc"));
        decoder.addDecoder(new AbsDetectorDataDecoder("HTCC","/daq/tt/htcc"));        
        decoder.init();
        return decoder;
    }
    
    public static void main(String[] args){
        
        
        
        
        //System.setProperty("CLAS12DIR","/Users/gavalian/Work/Software/project-3a.0.0/Distribution/clas12-offline-software/coatjava" );
        
        OptionParser parser = new OptionParser();
        
        parser.addOption("-n", "-1", "maximum number of events to process");        
        parser.addOption("-c", "2", "compression type (0-NONE, 1-LZ4 Fast, 2-LZ4 Best, 3-GZIP)");
        parser.addOption("-d", "0","debug mode, set >0 for more verbose output");
        parser.addOption("-m", "run","translation tables source (use -m devel for development tables)");
        parser.addRequired("-o","output.hipo");
        
        parser.parse(args);
        
        List<String> inputFiles = parser.getInputList();
        
        Clas12DetectorDecoder decoder =  Clas12DetectorDecoder.getInstance();
        HipoDataSync writer = new HipoDataSync();
        
        String outputFile = parser.getOption("-o").stringValue();
        int       nevents = parser.getOption("-n").intValue();
        int   compression = parser.getOption("-c").intValue();
        
        
        writer.setCompressionType(compression);
        writer.open(outputFile);
        
        int processedEvents = 0;
        ProgressPrintout progress = new ProgressPrintout();
        
        for(String inputFile : inputFiles){
            EvioSource reader = new EvioSource();
            reader.open(inputFile);
            while(reader.hasEvent()==true){
                EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
                DataEvent decodedEvent = decoder.decodeEvent(event);
                //decodedEvent.show();
                writer.writeEvent(decodedEvent);
                progress.updateStatus();
                if(nevents>0&&processedEvents>=nevents){
                    writer.close();
                    System.exit(0);
                }
            }
        }
        writer.close();
        
        //reader.open("/Users/gavalian/Work/Software/project-2a.0.0/clas_000810.evio.324");
        
        
        /*
        int maxEvents = 400;
        int icounter  = 0;
        
        while(reader.hasEvent()==true&&icounter<maxEvents){
            
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            System.out.println("---> printout EVENT # " + icounter);
            DataEvent decodedEvent = decoder.decodeEvent(event);
            //decodedEvent.show();
            icounter++;
        }
        System.out.println("Done...");*/
    }
}
