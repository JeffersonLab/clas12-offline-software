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

import org.jlab.jnp.hipo4.io.HipoWriter;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;

import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;
import org.jlab.utils.system.ClasUtilsFile;
/**
 *
 * @author gavalian
 */
public class CLASDecoder4 {
    
    private CodaEventDecoder          codaDecoder = null; 
    private DetectorEventDecoder  detectorDecoder = null;
    private List<DetectorDataDgtz>       dataList = new ArrayList<DetectorDataDgtz>();    
    private HipoDataSync                   writer = null;
    private HipoDataEvent               hipoEvent = null;
    private boolean              isRunNumberFixed = false;
    private int                  decoderDebugMode = 0;
    private SchemaFactory        schemaFactory = new SchemaFactory();
//    private String[]      detectorBanksAdc = new String[]{"FTOF::adc","ECAL::adc",""};
    
    public CLASDecoder4(boolean development){
        codaDecoder = new CodaEventDecoder();
        detectorDecoder = new DetectorEventDecoder(development);
        //dictionary.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        writer = new HipoDataSync();
        hipoEvent = (HipoDataEvent) writer.createEvent();
        String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
        schemaFactory.initFromDirectory(dir);
    }
    
    public CLASDecoder4(){        
        codaDecoder = new CodaEventDecoder();
        detectorDecoder = new DetectorEventDecoder();
        //dictionary.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        writer = new HipoDataSync();
        hipoEvent = (HipoDataEvent) writer.createEvent();
        String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
        schemaFactory.initFromDirectory(dir);
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
    
    public Bank getDataBankADC(String name, DetectorType type){
        
        List<DetectorDataDgtz> adcDGTZ = this.getEntriesADC(type);
        
        if(schemaFactory.hasSchema(name)==false) return null;
        
        Bank adcBANK = new Bank(schemaFactory.getSchema(name), adcDGTZ.size());
        
        for(int i = 0; i < adcDGTZ.size(); i++){
            adcBANK.putByte("sector", i, (byte) adcDGTZ.get(i).getDescriptor().getSector());
            adcBANK.putByte("layer", i, (byte) adcDGTZ.get(i).getDescriptor().getLayer());
            adcBANK.putShort("component", i, (short) adcDGTZ.get(i).getDescriptor().getComponent());
            adcBANK.putByte("order", i, (byte) adcDGTZ.get(i).getDescriptor().getOrder());
            adcBANK.putInt("ADC", i, adcDGTZ.get(i).getADCData(0).getADC());
            adcBANK.putFloat("time", i, (float) adcDGTZ.get(i).getADCData(0).getTime());
            adcBANK.putShort("ped", i, (short) adcDGTZ.get(i).getADCData(0).getPedestal());            
            if(name == "BST::adc") adcBANK.putLong("timestamp", i, adcDGTZ.get(i).getADCData(0).getTimeStamp()); // 1234 = dummy placeholder value
            if(name.equals("BMT::adc")||name.equals("FMT::adc")|| name.equals("FTTRK::adc")){
            	adcBANK.putInt("ADC", i, adcDGTZ.get(i).getADCData(0).getHeight());
            	adcBANK.putInt("integral", i, adcDGTZ.get(i).getADCData(0).getIntegral());
            	adcBANK.putLong("timestamp", i, adcDGTZ.get(i).getADCData(0).getTimeStamp());
            }	
         }
        return adcBANK;
    }
    
    
    public Bank getDataBankTDC(String name, DetectorType type){
        
        List<DetectorDataDgtz> tdcDGTZ = this.getEntriesTDC(type);
        if(schemaFactory.hasSchema(name)==false) return null;
        Bank tdcBANK = new Bank(schemaFactory.getSchema(name), tdcDGTZ.size());
        
        if(tdcBANK==null) return null;
        
        for(int i = 0; i < tdcDGTZ.size(); i++){
            tdcBANK.putByte("sector", i, (byte) tdcDGTZ.get(i).getDescriptor().getSector());
            tdcBANK.putByte("layer", i, (byte) tdcDGTZ.get(i).getDescriptor().getLayer());
            tdcBANK.putShort("component", i, (short) tdcDGTZ.get(i).getDescriptor().getComponent());
            tdcBANK.putByte("order", i, (byte) tdcDGTZ.get(i).getDescriptor().getOrder());
            tdcBANK.putInt("TDC", i, tdcDGTZ.get(i).getTDCData(0).getTime());
        }
        return tdcBANK;
    }
    
    public Bank getDataBankUndecodedADC(String name, DetectorType type){
        List<DetectorDataDgtz> adcDGTZ = this.getEntriesADC(type);
        Bank adcBANK = new Bank(schemaFactory.getSchema(name), adcDGTZ.size());
        
        for(int i = 0; i < adcDGTZ.size(); i++){
            adcBANK.putByte("crate", i, (byte) adcDGTZ.get(i).getDescriptor().getCrate());
            adcBANK.putByte("slot", i, (byte) adcDGTZ.get(i).getDescriptor().getSlot());
            adcBANK.putShort("channel", i, (short) adcDGTZ.get(i).getDescriptor().getChannel());
            adcBANK.putInt("ADC", i, adcDGTZ.get(i).getADCData(0).getADC());
            adcBANK.putFloat("time", i, (float) adcDGTZ.get(i).getADCData(0).getTime());
            adcBANK.putShort("ped", i, (short) adcDGTZ.get(i).getADCData(0).getPedestal());            
        }
        return adcBANK;
    }
    
    public Bank getDataBankUndecodedTDC(String name, DetectorType type){
        
        List<DetectorDataDgtz> tdcDGTZ = this.getEntriesTDC(type);
        
        Bank tdcBANK = new Bank(schemaFactory.getSchema(name), tdcDGTZ.size());
        if(tdcBANK==null) return null;
        
        for(int i = 0; i < tdcDGTZ.size(); i++){
            tdcBANK.putByte("crate", i, (byte) tdcDGTZ.get(i).getDescriptor().getCrate());
            tdcBANK.putByte("slot", i, (byte) tdcDGTZ.get(i).getDescriptor().getSlot());
            tdcBANK.putShort("channel", i, (short) tdcDGTZ.get(i).getDescriptor().getChannel());
            tdcBANK.putInt("TDC", i, tdcDGTZ.get(i).getTDCData(0).getTime());
        }
        return tdcBANK;
    }
    
    public Bank getDataBankUndecodedVTP(String name, DetectorType type){
        
        List<DetectorDataDgtz> vtpDGTZ = this.getEntriesVTP(type);
        
        Bank vtpBANK = new Bank(schemaFactory.getSchema(name), vtpDGTZ.size());
        if(vtpBANK==null) return null;
        
        for(int i = 0; i < vtpDGTZ.size(); i++){
            vtpBANK.putByte("crate", i, (byte) vtpDGTZ.get(i).getDescriptor().getCrate());
//            vtpBANK.setByte("slot", i, (byte) vtpDGTZ.get(i).getDescriptor().getSlot());
//            vtpBANK.setShort("channel", i, (short) vtpDGTZ.get(i).getDescriptor().getChannel());
            vtpBANK.putInt("value", i, vtpDGTZ.get(i).getVTPData(0).getWord());
        }
        return vtpBANK;
    }
    
    public Bank getDataBankUndecodedSCALER(String name, DetectorType type){
        
        List<DetectorDataDgtz> scalerDGTZ = this.getEntriesSCALER(type);
        
        Bank scalerBANK = new Bank(schemaFactory.getSchema(name), scalerDGTZ.size());
        if(scalerBANK==null) return null;
        
        for(int i = 0; i < scalerDGTZ.size(); i++){
            scalerBANK.putByte("crate", i, (byte) scalerDGTZ.get(i).getDescriptor().getCrate());
            scalerBANK.putByte("slot", i, (byte) scalerDGTZ.get(i).getDescriptor().getSlot());
            scalerBANK.putShort("channel", i, (short) scalerDGTZ.get(i).getDescriptor().getChannel());
            scalerBANK.putByte("helicity", i, (byte) scalerDGTZ.get(i).getSCALERData(0).getHelicity());
            scalerBANK.putByte("quartet", i, (byte) scalerDGTZ.get(i).getSCALERData(0).getQuartet());
            scalerBANK.putLong("value", i, scalerDGTZ.get(i).getSCALERData(0).getValue());
        }
//        if(scalerBANK.rows()>0)scalerBANK.show();
        return scalerBANK;
    }
    
    public Event getDataEvent(DataEvent rawEvent){
        this.initEvent(rawEvent);
        return getDataEvent();
    }
    
    public Event getDataEvent(){
                
        Event event = new Event();
        
        String[]        adcBankNames = new String[]{"FTOF::adc","ECAL::adc","FTCAL::adc","FTHODO::adc","FTTRK::adc",
                                                    "HTCC::adc","BST::adc","CTOF::adc","CND::adc","LTCC::adc","BMT::adc",
                                                    "FMT::adc","HEL::adc","RF::adc"};
        DetectorType[]  adcBankTypes = new DetectorType[]{DetectorType.FTOF,DetectorType.ECAL,DetectorType.FTCAL,DetectorType.FTHODO,DetectorType.FTTRK,
                                                          DetectorType.HTCC,DetectorType.BST,DetectorType.CTOF,DetectorType.CND,DetectorType.LTCC,DetectorType.BMT,
                                                          DetectorType.FMT,DetectorType.HEL,DetectorType.RF};
        
        String[]        tdcBankNames = new String[]{"FTOF::tdc","ECAL::tdc","DC::tdc","HTCC::tdc","LTCC::tdc","CTOF::tdc","CND::tdc","RF::tdc","RICH::tdc"};
        DetectorType[]  tdcBankTypes = new DetectorType[]{DetectorType.FTOF,DetectorType.ECAL,
            DetectorType.DC,DetectorType.HTCC,DetectorType.LTCC,DetectorType.CTOF,DetectorType.CND,DetectorType.RF,DetectorType.RICH};
        
        for(int i = 0; i < adcBankTypes.length; i++){
            Bank adcBank = getDataBankADC(adcBankNames[i],adcBankTypes[i]);
            if(adcBank!=null){
                if(adcBank.getRows()>0){
                    event.write(adcBank);
                }
            }
        }
        
        for(int i = 0; i < tdcBankTypes.length; i++){
            Bank tdcBank = getDataBankTDC(tdcBankNames[i],tdcBankTypes[i]);
            if(tdcBank!=null){
                if(tdcBank.getRows()>0){
                    event.write(tdcBank);
                }
            }
        }        
        
        /**
         * Adding un-decoded banks to the event
         */
        
        try {
            Bank adcBankUD = this.getDataBankUndecodedADC("RAW::adc", DetectorType.UNDEFINED);
            if(adcBankUD!=null){
                if(adcBankUD.getRows()>0){
                    event.write(adcBankUD);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        try {
            Bank tdcBankUD = this.getDataBankUndecodedTDC("RAW::tdc", DetectorType.UNDEFINED);
            if(tdcBankUD!=null){
                if(tdcBankUD.getRows()>0){
                    event.write(tdcBankUD);
                }
            } else {
                
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        try {
            Bank vtpBankUD = this.getDataBankUndecodedVTP("RAW::vtp", DetectorType.UNDEFINED);
            if(vtpBankUD!=null){
                if(vtpBankUD.getRows()>0){
                    event.write(vtpBankUD);
                }
            } else {
                
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        try {
            Bank scalerBankUD = this.getDataBankUndecodedSCALER("RAW::scaler", DetectorType.UNDEFINED);
            if(scalerBankUD!=null){
                if(scalerBankUD.getRows()>0){
                    event.write(scalerBankUD);
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

    public Bank createHeaderBank( int nrun, int nevent, float torus, float solenoid){
        
        if(schemaFactory.hasSchema("RUN::config")==false) return null;
        
        Bank bank = new Bank(schemaFactory.getSchema("RUN::config"), 1);
        
        int    localRun = this.codaDecoder.getRunNumber();
        int  localEvent = this.codaDecoder.getEventNumber();
        int   localTime = this.codaDecoder.getUnixTime();
        long  timeStamp = this.codaDecoder.getTimeStamp();
        long triggerBits = this.codaDecoder.getTriggerBits();
        
        if(nrun>0){
            localRun = nrun;
            localEvent = nevent;
        }
        bank.putInt("run",        0, localRun);
        bank.putInt("event",      0, localEvent);
        bank.putInt("unixtime",   0, localTime);
        bank.putLong("trigger",   0, triggerBits);        
        bank.putFloat("torus",    0, torus);
        bank.putFloat("solenoid", 0, solenoid);        
        bank.putLong("timestamp", 0, timeStamp);        
        
        
        return bank;
    }
    
    public Bank createTriggerBank(){
        
        if(schemaFactory.hasSchema("RUN::trigger")==false) return null;
        
        Bank bank = new Bank(schemaFactory.getSchema("RUN::trigger"), this.codaDecoder.getTriggerWords().size());
        
        for(int i=0; i<this.codaDecoder.getTriggerWords().size(); i++) {
            bank.putInt("id",      i, i+1);
            bank.putInt("trigger", i, this.codaDecoder.getTriggerWords().get(i));
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
            
            CLASDecoder4 decoder = new CLASDecoder4(developmentMode);
            
            decoder.setDebugMode(debug);
            
            //HipoDataSync writer = new HipoDataSync();
            System.out.println(" OUTPUT WRITER CHANGED TO JNP HIPO");
            HipoWriter writer = new HipoWriter();
            writer.setCompressionType(compression);
            writer.getSchemaFactory().initFromDirectory(ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4"));
            
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
                    
                    Event  decodedEvent = decoder.getDataEvent(event);
                    
                    Bank   header = decoder.createHeaderBank( nrun, counter, (float) torus, (float) solenoid);
                    if(header!=null) decodedEvent.write(header);
                    Bank   trigger = decoder.createTriggerBank();
                    if(trigger!=null) decodedEvent.write(trigger);
                    //decodedEvent.appendBanks(header);
                    //decodedEvent.appendBanks(trigger);

                    //HipoDataEvent dhe = (HipoDataEvent) decodedEvent;
                    //writer.writeEvent(dhe.getHipoEvent());
                    writer.addEvent(decodedEvent);
                    
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
