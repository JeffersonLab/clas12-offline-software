/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.detector.decode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.jevio.ByteDataTransformer;
import org.jlab.coda.jevio.CompositeData;
import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioNode;
import org.jlab.detector.decode.DetectorDataDgtz.ADCData;
import org.jlab.detector.decode.DetectorDataDgtz.TDCData;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.evio.EvioTreeBranch;
import org.jlab.utils.data.DataUtils;

/**
 *
 * @author gavalian
 */
public class CodaEventDecoder {
    
    private int   runNumber = 0;
    private int eventNumber = 0; 
    private long  timeStamp = 0L;
    private int triggerBits = 0;

    public CodaEventDecoder(){
        
    }
    /**
     * returns detector digitized data entries from the event.
     * all branches are analyzed and different types of digitized data
     * is created for each type of ADC and TDC data.
     * @param event
     * @return 
     */
    public List<DetectorDataDgtz> getDataEntries(EvioDataEvent event){
        List<DetectorDataDgtz>  rawEntries = new ArrayList<DetectorDataDgtz>();
        List<EvioTreeBranch> branches = this.getEventBranches(event);
        for(EvioTreeBranch branch : branches){
            List<DetectorDataDgtz>  list = this.getDataEntries(event,branch.getTag());
            if(list != null){
                rawEntries.addAll(list);
            }
        }        
        List<DetectorDataDgtz>  tdcEntries = this.getDataEntries_TDC(event);
        rawEntries.addAll(tdcEntries);
        this.setTimeStamp(event);
        
        return rawEntries;
    }
    
    public int getRunNumber(){
        return this.runNumber;
    }
    
    public int getEventNumber(){
        return this.eventNumber;
    }
    
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(EvioDataEvent event) {
        List<DetectorDataDgtz> tiEntries = this.getDataEntries_TI(event);
        if(tiEntries.size()>0) {
            long ts = tiEntries.get(0).getTimeStamp();
            for(int i=1; i<tiEntries.size(); i++) {
                if(tiEntries.get(i).getTimeStamp() != ts) System.out.println("WARNING: mismatch in TI time stamps");
            }
            this.timeStamp = ts ;
        }
    }
      
    public int getTriggerBits() {
        return triggerBits;
    }

    public void setTriggerBits(int triggerBits) {
        this.triggerBits = triggerBits;
    }
  
    /**
     * returns list of decoded data in the event for given crate.
     * @param event
     * @return 
     */
    public List<DetectorDataDgtz> getDataEntries(EvioDataEvent event, int crate){
        
        List<EvioTreeBranch> branches = this.getEventBranches(event);
        List<DetectorDataDgtz>   bankEntries = new ArrayList<DetectorDataDgtz>();
        
        EvioTreeBranch cbranch = this.getEventBranch(branches, crate);
        if(cbranch == null ) return null;
        
        for(EvioNode node : cbranch.getNodes()){ 
            //System.out.println(" analyzing tag = " + node.getTag());      
            
            if(node.getTag()==57615){
                //  This is regular integrated pulse mode, used for FTOF
                // FTCAL and EC/PCAL
                //return this.getDataEntries_57602(crate, node, event);
                this.readHeaderBank(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
            
            if(node.getTag()==57617){
                //  This is regular integrated pulse mode, used for FTOF
                // FTCAL and EC/PCAL
                //return this.getDataEntries_57602(crate, node, event);
                return this.getDataEntries_57617(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
            
            
            if(node.getTag()==57602){
                //  This is regular integrated pulse mode, used for FTOF
                // FTCAL and EC/PCAL
                //return this.getDataEntries_57602(crate, node, event);
                return this.getDataEntries_57602(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
            
            if(node.getTag()==57601){
                //  This is regular integrated pulse mode, used for FTOF
                // FTCAL and EC/PCAL
                return this.getDataEntries_57601(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
	    if(node.getTag()==57627){
                //  This is regular integrated pulse mode, used for FTOF
                // FTCAL and EC/PCAL
                return this.getDataEntries_57627(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
            if(node.getTag()==57622){
                //  This is regular integrated pulse mode, used for FTOF
                // FTCAL and EC/PCAL
                return this.getDataEntries_57622(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
        }
        return bankEntries;
    }
    
     /**
     * Returns an array of the branches in the event.
     * @param event
     * @return 
     */
    public List<EvioTreeBranch>  getEventBranches(EvioDataEvent event){
        ArrayList<EvioTreeBranch>  branches = new ArrayList<EvioTreeBranch>();
        try {

            //EvioNode mainNODE = event.getStructureHandler().getScannedStructure();
            //List<EvioNode>  eventNodes = mainNODE.getChildNodes();
            //List<EvioNode>  eventNodes = mainNODE.getAllNodes();
            List<EvioNode>  eventNodes = event.getStructureHandler().getNodes();
            if(eventNodes==null){
                return branches;
            }
            
            //System.out.println(" ************** BRANCHES ARRAY SIZE = " + eventNodes.size());
            for(EvioNode node : eventNodes){
                
                EvioTreeBranch eBranch = new EvioTreeBranch(node.getTag(),node.getNum());
                //branches.add(eBranch);
                //System.out.println("  FOR DROP : " + node.getTag() + "  " + node.getNum());
                List<EvioNode>  childNodes = node.getChildNodes();
                if(childNodes!=null){
                    for(EvioNode child : childNodes){
                        eBranch.addNode(child);
                    }
                    branches.add(eBranch);
                }
            }
            
        } catch (EvioException ex) {
            Logger.getLogger(CodaEventDecoder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return branches;
    }
    /**
     * returns branch with with given tag
     * @param branches
     * @param tag
     * @return 
     */
    public EvioTreeBranch  getEventBranch(List<EvioTreeBranch> branches, int tag){
        for(EvioTreeBranch branch : branches){
            if(branch.getTag()==tag) return branch;
        }
        return null;
    }
    
    public void readHeaderBank(Integer crate, EvioNode node, EvioDataEvent event){
        
        if(node.getDataTypeObj()==DataType.INT32||node.getDataTypeObj()==DataType.UINT32){
            try {
                int[] intData = ByteDataTransformer.toIntArray(node.getStructureBuffer(false));
                this.runNumber = intData[3];
                this.eventNumber = intData[4];
                /*System.out.println(" set run number and event nubmber = " 
                + this.runNumber + "  " + this.eventNumber                    
                );
                System.out.println(" EVENT BUFFER LENGTH = " + intData.length);
                for(int i = 0; i < intData.length; i++){
                System.out.println( i + " " + intData[i]);
                }*/
            } catch (Exception e) {
                this.runNumber = 10;
                this.eventNumber = 1;
            }
        } else {
            System.out.println("[error] can not read header bank");
        }
    }
    /**
     * SVT decoding
     * @param crate
     * @param node
     * @param event
     * @return 
     */
    public ArrayList<DetectorDataDgtz>  getDataEntries_57617(Integer crate, EvioNode node, EvioDataEvent event){
        ArrayList<DetectorDataDgtz>  rawdata = new ArrayList<DetectorDataDgtz>();
        
        if(node.getTag()==57617){
            try {
                ByteBuffer     compBuffer = node.getByteData(true);
                CompositeData  compData = new CompositeData(compBuffer.array(),event.getByteOrder());                
                List<DataType> cdatatypes = compData.getTypes();
                List<Object>   cdataitems = compData.getItems();
                int  totalSize = cdataitems.size();
                //ArrayList<EvioRawDataBank> bankArray = new ArrayList<EvioRawDataBank>();
                int  position  = 0;
                while( (position + 4) < totalSize){
                    Byte    slot = (Byte)     cdataitems.get(position);
                    Integer trig = (Integer)  cdataitems.get(position+1);
                    Long    time = (Long)     cdataitems.get(position+2);
                    //EvioRawDataBank  dataBank = new EvioRawDataBank(crate, slot.intValue(),trig,time);
                    Integer nchannels = (Integer) cdataitems.get(position+3);
                    int counter  = 0;
                    position = position + 4;
                    while(counter<nchannels){
                        //System.err.println("Position = " + position + " type =  "
                        //+ cdatatypes.get(position));
                        Byte   half    = (Byte) cdataitems.get(position);
                        Byte   channel = (Byte) cdataitems.get(position+1);
                        Byte   tdcbyte = (Byte) cdataitems.get(position+2);
                        Short  tdc     = DataUtils.getShortFromByte(tdcbyte);
                        //Byte   tdc     = (Byte) cdataitems.get(position+2);
                        //Short   adc     = (Short)  cdataitems.get(position+3);
                        Byte   adc     = (Byte)  cdataitems.get(position+3);
                        
                        int halfWord = DataUtils.getIntFromByte(half);
                        int   chipID = DataUtils.getInteger(halfWord, 0, 2);
                        int   halfID = DataUtils.getInteger(halfWord, 3, 3);
                        
                        Integer channelKey = (half<<8)|channel;
                        
                        //System.err.println( "CHIP = " + chipID + " HALF = " + halfID + "  CHANNEL = " + channel + " KEY = " + channelKey  );
                        //dataBank.addChannel(channelKey);
                        //dataBank.addData(channelKey, new RawData(channelKey,tdc,adc));
                        //int channelID = chipID*10000 + halfID*1000 + channel;
                        int channelID = halfID*10000 + chipID*1000 + channel;
                        position += 4;
                        counter++;
                        DetectorDataDgtz entry = new DetectorDataDgtz(crate,slot,channelID);
                        ADCData adcData = new ADCData();
                        adcData.setIntegral(adc);
                        adcData.setPedestal( (short) 0);
                        adcData.setADC(0,0);
                        adcData.setTime(tdc);
                        adcData.setTimeStamp(time);
                        entry.addADC(adcData);
                        //RawDataEntry  entry = new RawDataEntry(crate,slot,channelKey);
                        //entry.setSVT(half, channel, tdc, adc);
//                        System.out.println(crate + " " + slot+ " " + channelID+ " " + adcData.getIntegral()+ " " + adcData.getADC()+ " " + adcData.getTime());
                        rawdata.add(entry);
                    }
                    //bankArray.add(dataBank);
                }
                
            } catch (EvioException ex) {
                //Logger.getLogger(EvioRawDataSource.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IndexOutOfBoundsException ex){
                //System.out.println("[ERROR] ----> ERROR DECODING COMPOSITE DATA FOR ONE EVENT");
            }
        }
        return rawdata;
    }
    /**
     * decoding bank in Mode 1 - full ADC pulse.
     * @param crate
     * @param node
     * @param event
     * @return 
     */
    public List<DetectorDataDgtz>  getDataEntries_57601(Integer crate, EvioNode node, EvioDataEvent event){
        
        ArrayList<DetectorDataDgtz>  entries = new ArrayList<DetectorDataDgtz>();
        
        if(node.getTag()==57601){
            try {
                
                ByteBuffer     compBuffer = node.getByteData(true);
                CompositeData  compData = new CompositeData(compBuffer.array(),event.getByteOrder());
                
                List<DataType> cdatatypes = compData.getTypes();
                List<Object>   cdataitems = compData.getItems();

                if(cdatatypes.get(3) != DataType.NVALUE){
                    System.err.println("[EvioRawDataSource] ** error ** corrupted "
                    + " bank. tag = " + node.getTag() + " num = " + node.getNum());
                    return null;
                }
                
                int position = 0;
                
                while(position<cdatatypes.size()-4){                     
                    Byte    slot = (Byte)     cdataitems.get(position+0);
                    Integer trig = (Integer)  cdataitems.get(position+1);
                    Long    time = (Long)     cdataitems.get(position+2);
                    //EvioRawDataBank  dataBank = new EvioRawDataBank(crate, slot.intValue(),trig,time);
                    
                    Integer nchannels = (Integer) cdataitems.get(position+3);
                    //System.out.println("Retrieving the data size = " + cdataitems.size()
                    //+ "  " + cdatatypes.get(3) + " number of channels = " + nchannels);
                    position += 4;
                    int counter  = 0;
                    while(counter<nchannels){
                        //System.err.println("Position = " + position + " type =  "
                        //+ cdatatypes.get(position));
                        Byte channel   = (Byte) cdataitems.get(position);
                        Integer length = (Integer) cdataitems.get(position+1);
                        DetectorDataDgtz bank = new DetectorDataDgtz(crate,slot.intValue(),channel.intValue());
                        
                        //dataBank.addChannel(channel.intValue());
                        short[] shortbuffer = new short[length];
                        for(int loop = 0; loop < length; loop++){
                            Short sample    = (Short) cdataitems.get(position+2+loop);
                            shortbuffer[loop] = sample;
                            //dataBank.addData(channel.intValue(), 
                            //        new RawData(tdc,adc,pmin,pmax));
                        }
                        
                        bank.addPulse(shortbuffer);
                        bank.setTimeStamp(time);
                        //dataBank.addData(channel.intValue(), 
                        //            new RawData(shortbuffer));
                        entries.add(bank);
                        position += 2+length;
                        counter++;
                    }
                }
                return entries;
                
            } catch (EvioException ex) {
                Logger.getLogger(CodaEventDecoder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return entries;
    }

    public List<DetectorDataDgtz>  getDataEntries_57627(Integer crate, EvioNode node, EvioDataEvent event){
        
        ArrayList<DetectorDataDgtz>  entries = new ArrayList<DetectorDataDgtz>();
        
        if(node.getTag()==57627){
            try {
                
                ByteBuffer     compBuffer = node.getByteData(true);
                CompositeData  compData = new CompositeData(compBuffer.array(),event.getByteOrder());
                
                List<DataType> cdatatypes = compData.getTypes();
                List<Object>   cdataitems = compData.getItems();

                if(cdatatypes.get(3) != DataType.NVALUE){
                    System.err.println("[EvioRawDataSource] ** error ** corrupted "
                    + " bank. tag = " + node.getTag() + " num = " + node.getNum());
                    return null;
                }
               
                int position = 0;
                
                while(position<cdatatypes.size()-4){                     
                    Byte    slot = (Byte)     cdataitems.get(position+0);
                    Integer trig = (Integer)  cdataitems.get(position+1);
                    Long    time = (Long)     cdataitems.get(position+2);
                    //EvioRawDataBank  dataBank = new EvioRawDataBank(crate, slot.intValue(),trig,time);
                    
                    Integer nchannels = (Integer) cdataitems.get(position+3);
                    //System.out.println("Retrieving the data size = " + cdataitems.size()
                    //+ "  " + cdatatypes.get(3) + " number of channels = " + nchannels);
                    position += 4;
                    int counter  = 0;
                    while(counter<nchannels){
                        
                        Short channel   = (Short) cdataitems.get(position);
			Integer length = (Integer) cdataitems.get(position+1);
                        DetectorDataDgtz bank = new DetectorDataDgtz(crate,slot.intValue(),channel.intValue());
                       
                        short[] shortbuffer = new short[length];
                        for(int loop = 0; loop < length; loop++){
                            Short sample    = (Short) cdataitems.get(position+2+loop);
			    shortbuffer[loop] = sample;
                          
                        }
                        
                        bank.addPulse(shortbuffer);
                        bank.setTimeStamp(time);
                        //dataBank.addData(channel.intValue(), 
                        //            new RawData(shortbuffer));
                        entries.add(bank);
                        position += 2+length;
                        counter++;
                    }
                }
                return entries;
                
            } catch (EvioException ex) {
                Logger.getLogger(CodaEventDecoder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return entries;
    }
    /**
     * Decoding MODE 7 data. for given crate.
     * @param crate
     * @param node
     * @param event
     * @return 
     */
    public List<DetectorDataDgtz>  getDataEntries_57602(Integer crate, EvioNode node, EvioDataEvent event){
        List<DetectorDataDgtz>  entries = new ArrayList<DetectorDataDgtz>();        
        if(node.getTag()==57602){
            try {
                ByteBuffer     compBuffer = node.getByteData(true);
                CompositeData  compData = new CompositeData(compBuffer.array(),event.getByteOrder());
                
                List<DataType> cdatatypes = compData.getTypes();
                List<Object>   cdataitems = compData.getItems();
                
                if(cdatatypes.get(3) != DataType.NVALUE){
                    System.err.println("[EvioRawDataSource] ** error ** corrupted "
                    + " bank. tag = " + node.getTag() + " num = " + node.getNum());
                    return null;
                }

                int position = 0;
                //System.out.println(">>>>>> decoding 57602 with data size = " + cdatatypes.size());
                //System.out.println("N-VALUE = " + cdataitems.get(3).toString());
                while((position+4)<cdatatypes.size()){                    
                
                    Byte    slot = (Byte)     cdataitems.get(position+0);
                    Integer trig = (Integer)  cdataitems.get(position+1);
                    Long    time = (Long)     cdataitems.get(position+2);
                    
                    //EvioRawDataBank  dataBank = new EvioRawDataBank(crate,slot.intValue(),trig,time);
                    Integer nchannels = (Integer) cdataitems.get(position+3);
                    //System.out.println(" N - CHANNELS = " + nchannels + "  position = " + position);
                    //System.out.println("Retrieving the data size = " + cdataitems.size()
                    //+ "  " + cdatatypes.get(3) + " number of channels = " + nchannels);
                    position += 4;
                    int counter  = 0;
                    while(counter<nchannels){
                        //System.err.println("Position = " + position + " type =  "
                        //+ cdatatypes.get(position));
                        //Byte    slot = (Byte)     cdataitems.get(0);
                        //Integer trig = (Integer)  cdataitems.get(1);
                        //Long    time = (Long)     cdataitems.get(2);
                        Byte channel   = (Byte) cdataitems.get(position);
                        Integer length = (Integer) cdataitems.get(position+1);
                        //dataBank.addChannel(channel.intValue());
                        
                        //System.out.println(" LENGTH = " + length);
                        position += 2;
                        for(int loop = 0; loop < length; loop++){
                            Short tdc    = (Short) cdataitems.get(position);
                            Integer adc  = (Integer) cdataitems.get(position+1);
                            Short pmin   = (Short) cdataitems.get(position+2);
                            Short pmax   = (Short) cdataitems.get(position+3);
                            DetectorDataDgtz  entry = new DetectorDataDgtz(crate,slot,channel);
                            //entry.setData(BankType.ADCFPGA, new int[]{tdc, adc, pmin, pmax});
                            ADCData   adcData = new ADCData();
                            adcData.setIntegral(adc).setTimeWord(tdc).setPedestal(pmin).setHeight(pmax);
                            entry.addADC(adcData);
                            entry.setTimeStamp(time);
                            entries.add(entry);
                            position+=4;
                            //dataBank.addData(channel.intValue(), 
                            //       new RawData(tdc,adc,pmin,pmax));
                        }
                        //position += 6;
                        counter++;
                    }
                }
                //System.out.println(">>>>>> decoding 57602 final position = " + position);
                //for(DetectorDataDgtz data : entries){
                //    System.out.println(data);
                //}
                return entries;
            } catch (EvioException ex) {
                Logger.getLogger(CodaEventDecoder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return entries;
    }
    
    /**
     * Bank TAG=57622 used for DC (Drift Chambers) TDC values.
     * @param crate
     * @param node
     * @param event
     * @return 
     */
    public List<DetectorDataDgtz>  getDataEntries_57622(Integer crate, EvioNode node, EvioDataEvent event){
        List<DetectorDataDgtz>  entries = new ArrayList<DetectorDataDgtz>();
        if(node.getTag()==57622){
            try {            
                ByteBuffer     compBuffer = node.getByteData(true);
                CompositeData  compData = new CompositeData(compBuffer.array(),event.getByteOrder());
                List<DataType> cdatatypes = compData.getTypes();
                List<Object>   cdataitems = compData.getItems();
                
                int  totalSize = cdataitems.size();
                //System.out.println(" BANK 622 TOTAL SIZE = " + totalSize);                                                                                                       
                int  position  = 0;
                while( (position + 4) < totalSize){
                    Byte    slot = (Byte)     cdataitems.get(position);
                    Integer trig = (Integer)  cdataitems.get(position+1);
                    Long    time = (Long)     cdataitems.get(position+2);
                    //EvioRawDataBank  dataBank = new EvioRawDataBank(crate, slot.intValue(),trig,time);                                                                           
                    Integer nchannels = (Integer) cdataitems.get(position+3);
                    int counter  = 0;
                    position = position + 4;
                    while(counter<nchannels){
                        //System.err.println("Position = " + position + " type =  "                                                                                                
                        //+ cdatatypes.get(position));                                                                                                                             
                        Byte   channel    = (Byte) cdataitems.get(position);
                        Short  tdc     = (Short) cdataitems.get(position+1);
                        
                        position += 2;
                        counter++;
                        DetectorDataDgtz   entry = new DetectorDataDgtz(crate,slot,channel);
                        entry.addTDC(new TDCData(tdc));
                        entries.add(entry);

                    }
                }
            } catch (EvioException ex) {
                //Logger.getLogger(EvioRawDataSource.class.getName()).log(Level.SEVERE, null, ex);                                                                                 
            } catch (IndexOutOfBoundsException ex){
                //System.out.println("[ERROR] ----> ERROR DECODING COMPOSITE DATA FOR ONE EVENT");                                                                                 
            }         
            
        }
        return entries;
    }
    /**
     * reads the TDC values from the bank with tag = 57607, decodes
     * them and returns a list of digitized detector object.
     * @param event
     * @return 
     */
    public List<DetectorDataDgtz>  getDataEntries_TDC(EvioDataEvent event){
        
        List<DetectorDataDgtz> tdcEntries = new ArrayList<DetectorDataDgtz>();        
        List<EvioTreeBranch> branches = this.getEventBranches(event);
        
        for(EvioTreeBranch branch : branches){
            int  crate = branch.getTag();
            EvioTreeBranch cbranch = this.getEventBranch(branches, branch.getTag());
            for(EvioNode node : cbranch.getNodes()){ 
                if(node.getTag()==57607){
                    int[] intData = ByteDataTransformer.toIntArray(node.getStructureBuffer(false));
                    for(int loop = 0; loop < intData.length; loop++){
                        int  dataEntry = intData[loop];
                        int  slot      = DataUtils.getInteger(dataEntry, 27, 31 );
                        int  chan      = DataUtils.getInteger(dataEntry, 19, 25);
                        int  value     = DataUtils.getInteger(dataEntry,  0, 18);
                        DetectorDataDgtz   entry = new DetectorDataDgtz(crate,slot,chan);
                        entry.addTDC(new TDCData(value));
                        tdcEntries.add(entry);
                    }
                }
            }
        }
        return tdcEntries;
    }
    
    
    /**
     * decoding bank that contains TI time stamp.
     * @param crate
     * @param node
     * @param event
     * @return
     */
    public List<DetectorDataDgtz>  getDataEntries_TI(EvioDataEvent event){

        List<DetectorDataDgtz> tiEntries = new ArrayList<>();
        List<EvioTreeBranch> branches = this.getEventBranches(event);

        for(EvioTreeBranch branch : branches){
            int  crate = branch.getTag();
            EvioTreeBranch cbranch = this.getEventBranch(branches, branch.getTag());
            for(EvioNode node : cbranch.getNodes()){
                if(node.getTag()==57610){
                    long[] longData = ByteDataTransformer.toLongArray(node.getStructureBuffer(false));
                    int[]  intData  = ByteDataTransformer.toIntArray(node.getStructureBuffer(false));
                    DetectorDataDgtz entry = new DetectorDataDgtz(crate,0,0);
                    long tStamp = longData[2]&0x00000000ffffffff;
                    entry.setTimeStamp(tStamp);
                    if(node.getDataLength()==4) tiEntries.add(entry);
                    else if(node.getDataLength()==5) { // trigger supervisor crate
                        this.setTriggerBits(intData[6]);
                    }
                }
            }
        }
        return tiEntries;
    }
    
    public static void main(String[] args){
        EvioSource reader = new EvioSource();
        reader.open("/Users/gavalian/Work/Software/Release-8.0/COATJAVA/sector2_000257_mode7.evio");
        CodaEventDecoder decoder = new CodaEventDecoder();
        DetectorEventDecoder detectorDecoder = new DetectorEventDecoder();
        
        int maxEvents = 1;
        int icounter  = 0;
        
        while(reader.hasEvent()==true&&icounter<maxEvents){
            
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            List<DetectorDataDgtz>  dataSet = decoder.getDataEntries(event);
            detectorDecoder.translate(dataSet);
            detectorDecoder.fitPulses(dataSet);
            
            System.out.println("---> printout EVENT # " + icounter);
            for(DetectorDataDgtz data : dataSet){
                System.out.println(data);
            }
            icounter++;
        }
        System.out.println("Done...");
    }
}
