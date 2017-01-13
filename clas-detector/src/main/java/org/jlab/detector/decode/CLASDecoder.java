/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.decode;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.hipo.schema.SchemaFactory;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataBank;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author gavalian
 */
public class CLASDecoder {
    
    private CodaEventDecoder          codaDecoder = null; 
    private DetectorEventDecoder  detectorDecoder = null;
    private List<DetectorDataDgtz>       dataList = new ArrayList<DetectorDataDgtz>();    
    private HipoDataSync                   writer = null;
    private HipoDataEvent               hipoEvent = null;
    private int                  decoderDebugMode = 0;
    
    private String[]      detectorBanksAdc = new String[]{"FTOF::adc","ECAL::adc",""};
    
    public CLASDecoder(boolean development){
        codaDecoder = new CodaEventDecoder();
        detectorDecoder = new DetectorEventDecoder(development);
        //dictionary.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        writer = new HipoDataSync();
        hipoEvent = (HipoDataEvent) writer.createEvent();
    }
    
    public CLASDecoder(){        
        codaDecoder = new CodaEventDecoder();
        detectorDecoder = new DetectorEventDecoder();
        //dictionary.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        writer = new HipoDataSync();
        hipoEvent = (HipoDataEvent) writer.createEvent();
    }
    
    public static CLASDecoder createDecoder(){
        CLASDecoder decoder = new CLASDecoder();
        return decoder;
    }
    
    public static CLASDecoder createDecoderDevel(){
        CLASDecoder decoder = new CLASDecoder(true);
        return decoder;
    }
    
    public void setDebugMode(int mode){
        this.decoderDebugMode = mode;
    }
    
    public void initEvent(DataEvent event){
        
        if(event instanceof EvioDataEvent){
            try {
                dataList = codaDecoder.getDataEntries( (EvioDataEvent) event);
                if(this.decoderDebugMode>0){
                    System.out.println("\n>>>>>>>>> RAW decoded data");
                    for(DetectorDataDgtz data : dataList){
                        System.out.println(data);
                    }
                }
                detectorDecoder.translate(dataList);
                detectorDecoder.fitPulses(dataList);
                if(this.decoderDebugMode>0){
                    System.out.println("\n>>>>>>>>> TRANSLATED data");
                    for(DetectorDataDgtz data : dataList){
                        System.out.println(data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        /*for(DetectorDataDgtz data : dataList){
            System.out.println(data);
        }*/
        //System.out.println("\t >>>>> digitized data : size = " + dataList.size());
    }
    /**
     * return list of digitized ADC values from internal list
     * @param type detector type
     * @return 
     */
    public List<DetectorDataDgtz>  getEntriesADC(DetectorType type){
        return this.getEntriesADC(type, dataList);        
    }
    /**
     * returns ADC entries from decoded data for given detector TYPE
     * @param type detector type
     * @param entries digitized data list
     * @return list of ADC's for detector type
     */
    public List<DetectorDataDgtz>  getEntriesADC(DetectorType type, 
            List<DetectorDataDgtz> entries){
        List<DetectorDataDgtz>  adc = new ArrayList<DetectorDataDgtz>();
        for(DetectorDataDgtz entry : entries){
            if(entry.getDescriptor().getType()==type){
                if(entry.getADCSize()>0&&entry.getTDCSize()==0){
                    adc.add(entry);
                }
            }
        }
        
        //System.out.println("\t>>>>> produced list = " + entries.size()
        //+ "  adc store = " + adc.size());
        return adc;
    }
    
    public List<DetectorDataDgtz>  getEntriesTDC(DetectorType type){
        return getEntriesTDC(type,dataList);    
    }
    /**
     * returns TDC entries from decoded data for given detector type
     * @param type detector type
     * @param entries digitized data list
     * @return list of ADC's for detector type
     */
    public List<DetectorDataDgtz>  getEntriesTDC(DetectorType type, 
            List<DetectorDataDgtz> entries){
        List<DetectorDataDgtz>  adc = new ArrayList<DetectorDataDgtz>();
        for(DetectorDataDgtz entry : entries){
            if(entry.getDescriptor().getType()==type){
                if(entry.getTDCSize()>0&&entry.getADCSize()==0){
                    adc.add(entry);
                }
            }
        }
        //System.out.println("\t>>>>> produced list  TYPE = "  + type + "  size = " + entries.size()
        //+ "  tdc store = " + adc.size());
        return adc;
    }
    
    
    public DataBank getDataBankADC(String name, DetectorType type){
        
        List<DetectorDataDgtz> adcDGTZ = this.getEntriesADC(type);
        
        DataBank adcBANK = hipoEvent.createBank(name, adcDGTZ.size());
        
        for(int i = 0; i < adcDGTZ.size(); i++){
            adcBANK.setByte("sector", i, (byte) adcDGTZ.get(i).getDescriptor().getSector());
            adcBANK.setByte("layer", i, (byte) adcDGTZ.get(i).getDescriptor().getLayer());
            adcBANK.setShort("component", i, (byte) adcDGTZ.get(i).getDescriptor().getComponent());
            adcBANK.setByte("order", i, (byte) adcDGTZ.get(i).getDescriptor().getOrder());
            adcBANK.setInt("ADC", i, adcDGTZ.get(i).getADCData(0).getADC());
            adcBANK.setFloat("time", i, (float) adcDGTZ.get(i).getADCData(0).getTime());
            adcBANK.setShort("ped", i, (short) adcDGTZ.get(i).getADCData(0).getPedestal());            
        }
        return adcBANK;
    }
    
    
    public DataBank getDataBankTDC(String name, DetectorType type){
        
        List<DetectorDataDgtz> tdcDGTZ = this.getEntriesTDC(type);
        
        DataBank tdcBANK = hipoEvent.createBank(name, tdcDGTZ.size());
        if(tdcBANK==null) return null;
        
        for(int i = 0; i < tdcDGTZ.size(); i++){
            tdcBANK.setByte("sector", i, (byte) tdcDGTZ.get(i).getDescriptor().getSector());
            tdcBANK.setByte("layer", i, (byte) tdcDGTZ.get(i).getDescriptor().getLayer());
            tdcBANK.setShort("component", i, (byte) tdcDGTZ.get(i).getDescriptor().getComponent());
            tdcBANK.setByte("order", i, (byte) tdcDGTZ.get(i).getDescriptor().getOrder());
            tdcBANK.setInt("TDC", i, tdcDGTZ.get(i).getTDCData(0).getTime());
        }
        return tdcBANK;
    }
    
    public DataBank getDataBankUndecodedADC(String name, DetectorType type){
        List<DetectorDataDgtz> adcDGTZ = this.getEntriesADC(type);
        DataBank adcBANK = hipoEvent.createBank(name, adcDGTZ.size());
        
        for(int i = 0; i < adcDGTZ.size(); i++){
            adcBANK.setByte("crate", i, (byte) adcDGTZ.get(i).getDescriptor().getCrate());
            adcBANK.setByte("slot", i, (byte) adcDGTZ.get(i).getDescriptor().getSlot());
            adcBANK.setShort("channel", i, (byte) adcDGTZ.get(i).getDescriptor().getChannel());
            adcBANK.setInt("ADC", i, adcDGTZ.get(i).getADCData(0).getADC());
            adcBANK.setFloat("time", i, (float) adcDGTZ.get(i).getADCData(0).getTime());
            adcBANK.setShort("ped", i, (short) adcDGTZ.get(i).getADCData(0).getPedestal());            
        }
        return adcBANK;
    }
    
    public DataBank getDataBankUndecodedTDC(String name, DetectorType type){
        
        List<DetectorDataDgtz> tdcDGTZ = this.getEntriesTDC(type);
        
        DataBank tdcBANK = hipoEvent.createBank(name, tdcDGTZ.size());
        if(tdcBANK==null) return null;
        
        for(int i = 0; i < tdcDGTZ.size(); i++){
            tdcBANK.setByte("crate", i, (byte) tdcDGTZ.get(i).getDescriptor().getCrate());
            tdcBANK.setByte("slot", i, (byte) tdcDGTZ.get(i).getDescriptor().getSlot());
            tdcBANK.setShort("channel", i, (byte) tdcDGTZ.get(i).getDescriptor().getChannel());
            tdcBANK.setInt("TDC", i, tdcDGTZ.get(i).getTDCData(0).getTime());
        }
        return tdcBANK;
    }
    
    public DataEvent getDataEvent(DataEvent rawEvent){
        this.initEvent(rawEvent);
        return getDataEvent();
    }
    
    public DataEvent getDataEvent(){
                
        HipoDataEvent event = (HipoDataEvent) writer.createEvent();
        
        String[]        adcBankNames = new String[]{"FTOF::adc","ECAL::adc","FTCAL::adc","HTCC::adc"};
        DetectorType[]  adcBankTypes = new DetectorType[]{DetectorType.FTOF,DetectorType.EC,DetectorType.FTCAL,DetectorType.HTCC};
        
        String[]        tdcBankNames = new String[]{"FTOF::tdc","ECAL::tdc","DC::tdc"};
        DetectorType[]  tdcBankTypes = new DetectorType[]{DetectorType.FTOF,DetectorType.EC,DetectorType.DC};
        
        for(int i = 0; i < adcBankTypes.length; i++){
            DataBank adcBank = getDataBankADC(adcBankNames[i],adcBankTypes[i]);
            if(adcBank!=null){
                if(adcBank.rows()>0){
                    event.appendBanks(adcBank);
                }
            }
        }
        
        for(int i = 0; i < tdcBankTypes.length; i++){
            DataBank tdcBank = getDataBankTDC(tdcBankNames[i],tdcBankTypes[i]);
            if(tdcBank!=null){
                if(tdcBank.rows()>0){
                    event.appendBanks(tdcBank);
                }
            }
        }        
        /**
         * Adding un-decoded banks to the event
         */
        try {
            DataBank adcBankUD = this.getDataBankUndecodedADC("RAW::adc", DetectorType.UNDEFINED);
            if(adcBankUD!=null){
                if(adcBankUD.rows()>0){
                    event.appendBanks(adcBankUD);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        try {
            DataBank tdcBankUD = this.getDataBankUndecodedTDC("RAW::tdc", DetectorType.UNDEFINED);
            if(tdcBankUD!=null){
                if(tdcBankUD.rows()>0){
                    event.appendBanks(tdcBankUD);
                }
            } else {
                
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return event;
    }
    
    public static void main(String[] args){
        
        OptionParser parser = new OptionParser("decoder");
        parser.addOption("-n", "-1", "maximum number of events to process");        
        parser.addOption("-c", "0", "compression type (0-NONE, 1-GZIP, 2-LZ4)");
        parser.addOption("-d", "0","debug mode, set >0 for more verbose output");
        parser.addOption("-m", "run","translation tables source (use -m devel for development tables)");
        parser.addRequired("-o","output.hipo");
        
        parser.parse(args);
        
        List<String> inputList = parser.getInputList();
        
        if(parser.hasOption("-o")==true){
            
            if(inputList.isEmpty()==true){
                parser.printUsage();
                System.out.println("\n >>>> error : no input file is specified....\n");
                System.exit(0);
            }
            
            String modeDevel = parser.getOption("-m").stringValue();
            boolean developmentMode = false;
            
            if(modeDevel.compareTo("run")!=0&&modeDevel.compareTo("devel")!=0){
                parser.printUsage();
                System.out.println("\n >>>> error : mode has to be set to \"run\" or \"devel\" ");
                System.exit(0);
            }
            
            
            if(modeDevel.compareTo("devel")==0){
                developmentMode = true;
            }
            
            String outputFile = parser.getOption("-o").stringValue();
            int compression = parser.getOption("-c").intValue();
            int debug = parser.getOption("-d").intValue();            
            
            CLASDecoder decoder = new CLASDecoder(developmentMode);
            
            decoder.setDebugMode(debug);
            
            HipoDataSync writer = new HipoDataSync();
            writer.setCompressionType(compression);
            
            writer.open(outputFile);
            ProgressPrintout progress = new ProgressPrintout();
            System.out.println("INPUT LIST SIZE = " + inputList.size());
            int nevents = parser.getOption("-n").intValue();
            int counter = 0;
            
            for(String inputFile : inputList){
                EvioSource reader = new EvioSource();
                reader.open(inputFile);
                while(reader.hasEvent()==true){
                    EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
                    DataEvent  decodedEvent = decoder.getDataEvent(event);
                    writer.writeEvent(decodedEvent);
                    counter++;
                    progress.updateStatus();
                    if(nevents>0){
                        if(counter>nevents) break;
                    }
                }
            }
            writer.close();
        }
        /*
        CLASDecoder decoder = new CLASDecoder();
        EvioSource reader = new EvioSource();
        reader.open("/Users/gavalian/Work/Software/Release-4a.0/DataSet/raw/sector2_000233_mode7.evio.0");
        int icounter = 0;
        while(reader.hasEvent()==true){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            decoder.initEvent(event);
            decoder.getEntriesADC(DetectorType.FTOF);
            decoder.getEntriesTDC(DetectorType.FTOF);
            System.out.println("----");
          
            DataBank  bankADC = decoder.getDataBankADC("FTOF::adc", DetectorType.FTOF);
            DataBank  bankTDC = decoder.getDataBankTDC("FTOF::tdc", DetectorType.FTOF);            
            bankADC.show();
            bankTDC.show();
            DataEvent  decodedEvent = decoder.getDataEvent();
            decodedEvent.show();
            icounter++;
        }
        System.out.println("done... processed events " + icounter);
        */
    }
}
