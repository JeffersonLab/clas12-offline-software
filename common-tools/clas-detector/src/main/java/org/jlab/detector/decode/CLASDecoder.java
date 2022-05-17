/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.decode;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataBank;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.jnp.hipo.io.HipoWriter;
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
    private boolean              isRunNumberFixed = false;
    private int                  decoderDebugMode = 0;
    
//    private String[]      detectorBanksAdc = new String[]{"FTOF::adc","ECAL::adc",""};
    
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
    
    public void setRunNumber(int run){
        if(this.isRunNumberFixed==false){
            this.detectorDecoder.setRunNumber(run);
        }
    }

    public void setRunNumber(int run, boolean fixed){        
        this.isRunNumberFixed = fixed;
        this.detectorDecoder.setRunNumber(run);
        System.out.println(" SETTING RUN NUMBER TO " + run + " FIXED = " + this.isRunNumberFixed);
    }
    
    public CodaEventDecoder getCodaEventDecoder() {
	return codaDecoder;
    }
    
    public void initEvent(DataEvent event){
        
        if(event instanceof EvioDataEvent){
            try {
                
                dataList = codaDecoder.getDataEntries( (EvioDataEvent) event);
                
                //dataList = new ArrayList<DetectorDataDgtz>();
                //-----------------------------------------------------------------------------
                // This part reads the BITPACKED FADC data from tag=57638 Format (cmcms)
                // Then unpacks into Detector Digigitized data, and appends to existing buffer
                // Modified on 9/5/2018
                //-----------------------------------------------------------------------------
                
                List<FADCData>  fadcPacked = codaDecoder.getADCEntries((EvioDataEvent) event);
                
                /*for(FADCData data : fadcPacked){
                    data.show();
                }*/
                
                if(fadcPacked!=null){
                    List<DetectorDataDgtz> fadcUnpacked = FADCData.convert(fadcPacked);
                    dataList.addAll(fadcUnpacked);
                }
                //  END of Bitpacked section                
                //-----------------------------------------------------------------------------
                //this.decoderDebugMode = 4;
                if(this.decoderDebugMode>0){
                    System.out.println("\n>>>>>>>>> RAW decoded data");
                    for(DetectorDataDgtz data : dataList){
                        System.out.println(data);
                    }
                }
                int runNumberCoda = codaDecoder.getRunNumber();
                this.setRunNumber(runNumberCoda);
                
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
        List<DetectorDataDgtz>  tdc = new ArrayList<DetectorDataDgtz>();
        for(DetectorDataDgtz entry : entries){
            if(entry.getDescriptor().getType()==type){
                if(entry.getTDCSize()>0&&entry.getADCSize()==0){
                    tdc.add(entry);
                }
            }
        }
        //System.out.println("\t>>>>> produced list  TYPE = "  + type + "  size = " + entries.size()
        //+ "  tdc store = " + adc.size());
        return tdc;
    }    
    
    public List<DetectorDataDgtz>  getEntriesVTP(DetectorType type){
        return getEntriesVTP(type,dataList);    
    }
    /**
     * returns VTP entries from decoded data for given detector type
     * @param type detector type
     * @param entries digitized data list
     * @return list of VTP's for detector type
     */
    public List<DetectorDataDgtz>  getEntriesVTP(DetectorType type, 
        List<DetectorDataDgtz> entries){
        List<DetectorDataDgtz>  vtp = new ArrayList<DetectorDataDgtz>();
        for(DetectorDataDgtz entry : entries){
            if(entry.getDescriptor().getType()==type){
                if(entry.getVTPSize()>0){
                    vtp.add(entry);
                }
            }
        }
//        System.out.println("\t>>>>> produced list  TYPE = "  + type + "  size = " + entries.size() + "  vtp store = " + vtp.size());
        return vtp;
    }
    
    public List<DetectorDataDgtz>  getEntriesSCALER(DetectorType type){
        return getEntriesSCALER(type,dataList);    
    }
    /**
     * returns VTP entries from decoded data for given detector type
     * @param type detector type
     * @param entries digitized data list
     * @return list of VTP's for detector type
     */
    public List<DetectorDataDgtz>  getEntriesSCALER(DetectorType type, 
        List<DetectorDataDgtz> entries){
        List<DetectorDataDgtz>  scaler = new ArrayList<DetectorDataDgtz>();
        for(DetectorDataDgtz entry : entries){
            if(entry.getDescriptor().getType()==type){
                if(entry.getSCALERSize()>0){
                    scaler.add(entry);
                }
            }
        }
//        System.out.println("\t>>>>> produced list  TYPE = "  + type + "  size = " + entries.size() + "  vtp store = " + vtp.size());
        return scaler;
    }
    
    public DataBank getDataBankADC(String name, DetectorType type){
        
        List<DetectorDataDgtz> adcDGTZ = this.getEntriesADC(type);
        
        DataBank adcBANK = hipoEvent.createBank(name, adcDGTZ.size());
        
        for(int i = 0; i < adcDGTZ.size(); i++){
            adcBANK.setByte("sector", i, (byte) adcDGTZ.get(i).getDescriptor().getSector());
            adcBANK.setByte("layer", i, (byte) adcDGTZ.get(i).getDescriptor().getLayer());
            adcBANK.setShort("component", i, (short) adcDGTZ.get(i).getDescriptor().getComponent());
            adcBANK.setByte("order", i, (byte) adcDGTZ.get(i).getDescriptor().getOrder());
            adcBANK.setInt("ADC", i, adcDGTZ.get(i).getADCData(0).getADC());
            adcBANK.setFloat("time", i, (float) adcDGTZ.get(i).getADCData(0).getTime());
            adcBANK.setShort("ped", i, (short) adcDGTZ.get(i).getADCData(0).getPedestal());            
            if(name == "BST::adc") adcBANK.setLong("timestamp", i, adcDGTZ.get(i).getADCData(0).getTimeStamp()); // 1234 = dummy placeholder value
            if(name.equals("BMT::adc")||name.equals("FMT::adc")|| name.equals("FTTRK::adc")){
            	adcBANK.setInt("ADC", i, adcDGTZ.get(i).getADCData(0).getHeight());
            	adcBANK.setInt("integral", i, adcDGTZ.get(i).getADCData(0).getIntegral());
            	adcBANK.setLong("timestamp", i, adcDGTZ.get(i).getADCData(0).getTimeStamp());
            }	
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
            tdcBANK.setShort("component", i, (short) tdcDGTZ.get(i).getDescriptor().getComponent());
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
            adcBANK.setShort("channel", i, (short) adcDGTZ.get(i).getDescriptor().getChannel());
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
            tdcBANK.setShort("channel", i, (short) tdcDGTZ.get(i).getDescriptor().getChannel());
            tdcBANK.setInt("TDC", i, tdcDGTZ.get(i).getTDCData(0).getTime());
        }
        return tdcBANK;
    }
    
    public DataBank getDataBankUndecodedVTP(String name, DetectorType type){
        
        List<DetectorDataDgtz> vtpDGTZ = this.getEntriesVTP(type);
        
        DataBank vtpBANK = hipoEvent.createBank(name, vtpDGTZ.size());
        if(vtpBANK==null) return null;
        
        for(int i = 0; i < vtpDGTZ.size(); i++){
            vtpBANK.setByte("crate", i, (byte) vtpDGTZ.get(i).getDescriptor().getCrate());
//            vtpBANK.setByte("slot", i, (byte) vtpDGTZ.get(i).getDescriptor().getSlot());
//            vtpBANK.setShort("channel", i, (short) vtpDGTZ.get(i).getDescriptor().getChannel());
            vtpBANK.setInt("word", i, vtpDGTZ.get(i).getVTPData(0).getWord());
        }
        return vtpBANK;
    }
    
    public DataBank getDataBankUndecodedSCALER(String name, DetectorType type){
        
        List<DetectorDataDgtz> scalerDGTZ = this.getEntriesSCALER(type);
        
        DataBank scalerBANK = hipoEvent.createBank(name, scalerDGTZ.size());
        if(scalerBANK==null) return null;
        
        for(int i = 0; i < scalerDGTZ.size(); i++){
            scalerBANK.setByte("crate", i, (byte) scalerDGTZ.get(i).getDescriptor().getCrate());
            scalerBANK.setByte("slot", i, (byte) scalerDGTZ.get(i).getDescriptor().getSlot());
            scalerBANK.setShort("channel", i, (short) scalerDGTZ.get(i).getDescriptor().getChannel());
            scalerBANK.setByte("helicity", i, (byte) scalerDGTZ.get(i).getSCALERData(0).getHelicity());
            scalerBANK.setByte("quartet", i, (byte) scalerDGTZ.get(i).getSCALERData(0).getQuartet());
            scalerBANK.setLong("value", i, scalerDGTZ.get(i).getSCALERData(0).getValue());
        }
//        if(scalerBANK.rows()>0)scalerBANK.show();
        return scalerBANK;
    }
    
    public DataEvent getDataEvent(DataEvent rawEvent){
        this.initEvent(rawEvent);
        return getDataEvent();
    }
    
    public DataEvent getDataEvent(){
                
        HipoDataEvent event = (HipoDataEvent) writer.createEvent();
        
        String[]        adcBankNames = new String[]{"FTOF::adc","ECAL::adc","FTCAL::adc","FTHODO::adc","FTTRK::adc",
                                                    "HTCC::adc","BST::adc","CTOF::adc","CND::adc","LTCC::adc","BMT::adc",
                                                    "FMT::adc","HEL::adc","RF::adc","BAND::adc","RASTER::adc"};
        DetectorType[]  adcBankTypes = new DetectorType[]{DetectorType.FTOF,DetectorType.ECAL,DetectorType.FTCAL,DetectorType.FTHODO,DetectorType.FTTRK,
                                                          DetectorType.HTCC,DetectorType.BST,DetectorType.CTOF,DetectorType.CND,DetectorType.LTCC,DetectorType.BMT,
                                                          DetectorType.FMT,DetectorType.HEL,DetectorType.RF, DetectorType.BAND, DetectorType.RASTER};
        
        String[]        tdcBankNames = new String[]{"FTOF::tdc","ECAL::tdc","DC::tdc","HTCC::tdc","LTCC::tdc","CTOF::tdc","CND::tdc","RF::tdc","RICH::tdc","BAND::tdc"};
        DetectorType[]  tdcBankTypes = new DetectorType[]{DetectorType.FTOF,DetectorType.ECAL,
            DetectorType.DC,DetectorType.HTCC,DetectorType.LTCC,DetectorType.CTOF,DetectorType.CND,DetectorType.RF,DetectorType.RICH, DetectorType.BAND};
        
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
        
        try {
            DataBank vtpBankUD = this.getDataBankUndecodedVTP("RAW::vtp", DetectorType.UNDEFINED);
            if(vtpBankUD!=null){
                if(vtpBankUD.rows()>0){
                    event.appendBanks(vtpBankUD);
                }
            } else {
                
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        try {
            DataBank scalerBankUD = this.getDataBankUndecodedSCALER("RAW::scaler", DetectorType.UNDEFINED);
            if(scalerBankUD!=null){
                if(scalerBankUD.rows()>0){
                    event.appendBanks(scalerBankUD);
                }
            } else {
                
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    public long getTriggerPhase() {    	
        long timestamp    = this.codaDecoder.getTimeStamp();
        int  phase_offset = 1;
        return ((timestamp%6)+phase_offset)%6; // TI derived phase correction due to TDC and FADC clock differences 
    }

    public HipoDataBank createHeaderBank(DataEvent event, int nrun, int nevent, float torus, float solenoid){
        HipoDataBank bank = (HipoDataBank) event.createBank("RUN::config", 1);
        
        int    localRun = this.codaDecoder.getRunNumber();
        int  localEvent = this.codaDecoder.getEventNumber();
        int   localTime = this.codaDecoder.getUnixTime();
        long  timeStamp = this.codaDecoder.getTimeStamp();
        long triggerBits = this.codaDecoder.getTriggerBits();
        
        if(nrun>0){
            localRun = nrun;
            localEvent = nevent;
        }
        bank.setInt("run",        0, localRun);
        bank.setInt("event",      0, localEvent);
        bank.setInt("unixtime",   0, localTime);
        bank.setLong("trigger",   0, triggerBits);        
        bank.setFloat("torus",    0, torus);
        bank.setFloat("solenoid", 0, solenoid);        
        bank.setLong("timestamp", 0, timeStamp);        
        
        
        return bank;
    }
    
    public HipoDataBank createTriggerBank(DataEvent event){
        HipoDataBank bank = (HipoDataBank) event.createBank("RUN::trigger", this.codaDecoder.getTriggerWords().size());
        
        for(int i=0; i<this.codaDecoder.getTriggerWords().size(); i++) {
            bank.setInt("id",      i, i+1);
            bank.setInt("trigger", i, this.codaDecoder.getTriggerWords().get(i));
        }
        return bank;
    }
    
    public static void main(String[] args){
        
        OptionParser parser = new OptionParser("decoder");
        parser.addOption("-n", "-1", "maximum number of events to process");        
        parser.addOption("-c", "2", "compression type (0-NONE, 1-LZ4 Fast, 2-LZ4 Best, 3-GZIP)");
        parser.addOption("-d", "0","debug mode, set >0 for more verbose output");
        parser.addOption("-m", "run","translation tables source (use -m devel for development tables)");
        parser.addOption("-b", "16","record buffer size in MB");
        parser.addRequired("-o","output.hipo");
        
        
        parser.addOption("-r", "-1","run number in the header bank (-1 means use CODA run)");
        parser.addOption("-t", "-0.5","torus current in the header bank");
        parser.addOption("-s", "0.5","solenoid current in the header bank");
        
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
            int  recordsize = parser.getOption("-b").intValue();
            int debug = parser.getOption("-d").intValue();            
            
            CLASDecoder decoder = new CLASDecoder(developmentMode);
            
            decoder.setDebugMode(debug);
            
            //HipoDataSync writer = new HipoDataSync();
            System.out.println(" OUTPUT WRITER CHANGED TO JNP HIPO");
            HipoWriter writer = new HipoWriter(recordsize*1024*1024);
            writer.setCompressionType(compression);
            writer.appendSchemaFactoryFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
            int nrun = parser.getOption("-r").intValue();
            double torus = parser.getOption("-t").doubleValue();
            double solenoid = parser.getOption("-s").doubleValue();
            
            
            writer.open(outputFile);
            ProgressPrintout progress = new ProgressPrintout();
            System.out.println("INPUT LIST SIZE = " + inputList.size());
            int nevents = parser.getOption("-n").intValue();
            int counter = 0;
            
            if(nrun>0){
                decoder.setRunNumber(nrun,true);
            }
            
            for(String inputFile : inputList){
                EvioSource reader = new EvioSource();
                reader.open(inputFile);
                while(reader.hasEvent()==true){
                    EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
                    DataEvent  decodedEvent = decoder.getDataEvent(event);
                    DataBank   header = decoder.createHeaderBank(decodedEvent, nrun, counter, (float) torus, (float) solenoid);
                    DataBank   trigger = decoder.createTriggerBank(decodedEvent);
                    decodedEvent.appendBanks(header);
                    decodedEvent.appendBanks(trigger);

                    HipoDataEvent dhe = (HipoDataEvent) decodedEvent;
                    //writer.writeEvent(dhe.getHipoEvent());
                    
                    counter++;
                    progress.updateStatus();
                    if(nevents>0){
                        if(counter>=nevents) break;
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
