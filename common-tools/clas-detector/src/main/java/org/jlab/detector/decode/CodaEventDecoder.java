/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.detector.decode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.jevio.ByteDataTransformer;
import org.jlab.coda.jevio.CompositeData;
import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioNode;
import org.jlab.detector.decode.DetectorDataDgtz.ADCData;
import org.jlab.detector.decode.DetectorDataDgtz.SCALERData;
import org.jlab.detector.decode.DetectorDataDgtz.TDCData;
import org.jlab.detector.decode.DetectorDataDgtz.VTPData;
import org.jlab.detector.helicity.HelicityBit;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.evio.EvioTreeBranch;
import org.jlab.utils.data.DataUtils;

import org.jlab.jnp.utils.json.JsonObject;

//import sun.awt.image.IntegerComponentRaster;

/**
 *
 * @author gavalian
 */
public class CodaEventDecoder {

    private int   runNumber = 0;
    private int eventNumber = 0;
    private int    unixTime = 0;
    private long  timeStamp = 0L;
    private int timeStampErrors = 0;
    private long    triggerBits = 0;
    private byte helicityLevel3 = HelicityBit.UDF.value();
    private List<Integer> triggerWords = new ArrayList<>();
    JsonObject  epicsData = new JsonObject();

    private final long timeStampTolerance = 0L;
    private int tiMaster = -1; 
            
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
        
        int event_size = event.getHandler().getStructure().getByteBuffer().array().length;
        if(event_size>600*1024){
            System.out.println("error: >>>> EVENT SIZE EXCEEDS 600 kB");
            return new ArrayList<DetectorDataDgtz>();
        }
        
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
        List<DetectorDataDgtz>  vtpEntries = this.getDataEntries_VTP(event);
        rawEntries.addAll(vtpEntries);
        List<DetectorDataDgtz>  scalerEntries = this.getDataEntries_Scalers(event);
        rawEntries.addAll(scalerEntries);

        this.getDataEntries_EPICS(event);
        this.setTimeStamp(event);

        return rawEntries;
    }

    public JsonObject getEpicsData(){
        return this.epicsData;
    }

    public List<Integer> getTriggerWords(){
        return this.triggerWords;
    }

    private void printByteBuffer(ByteBuffer buffer, int max, int columns){
        int n = max;
        if(buffer.capacity()<max) n = buffer.capacity();
        StringBuilder str = new StringBuilder();
        for(int i = 0 ; i < n; i++){
            str.append(String.format("%02X ", buffer.get(i)));
            if( (i+1)%columns==0 ) str.append("\n");
        }
        System.out.println(str.toString());
    }

    public int getRunNumber(){
        return this.runNumber;
    }

    public int getEventNumber(){
        return this.eventNumber;
    }

    public int getUnixTime(){
        return this.unixTime;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public byte getHelicityLevel3() {
        return this.helicityLevel3;
    }

    public void setTimeStamp(EvioDataEvent event) {

        long ts = -1;

        List<DetectorDataDgtz> tiEntries = this.getDataEntries_TI(event);
                
        if(tiEntries.size()==1) {
            ts = tiEntries.get(0).getTimeStamp();
        }
        else if(tiEntries.size()>1) {
            // check sychronization
            boolean tiSync=true;
            int  i0 = -1;
            // set reference timestamp from first entry which is not the tiMaster
            for(int i=0; i<tiEntries.size(); i++) {
                if(tiEntries.get(i).getDescriptor().getCrate()!=this.tiMaster) {
                    i0 = i;
                    break;
                }   
            }
            for(int i=0; i<tiEntries.size(); i++) {
                long deltaTS = this.timeStampTolerance;       
                if(tiEntries.get(i).getDescriptor().getCrate()==this.tiMaster) deltaTS = deltaTS + 1;  // add 1 click tolerance for tiMaster
                if(Math.abs(tiEntries.get(i).getTimeStamp()-tiEntries.get(i0).getTimeStamp())>deltaTS) {
                    tiSync=false;
                    if(this.timeStampErrors<100) {
                        System.err.println("WARNING: mismatch in TI time stamps: crate " 
                                        + tiEntries.get(i).getDescriptor().getCrate() + " reports " 
                                        + tiEntries.get(i).getTimeStamp() + " instead of the " + ts
                                        + " from crate " + tiEntries.get(i0).getDescriptor().getCrate());
                    }
                    else if(this.timeStampErrors==100) {
                        System.err.println("WARNING: reached the maximum number of timeStamp errors (100), supressing future warnings.");
                    }
                    this.timeStampErrors++;
                }
            }
            if(tiSync) ts = tiEntries.get(i0).getTimeStamp();
        }
        this.timeStamp = ts ;
    }

    public long getTriggerBits() {
        return triggerBits;
    }

    public void setTriggerBits(long triggerBits) {
        this.triggerBits = triggerBits;
    }


    public List<FADCData> getADCEntries(EvioDataEvent event){
        List<FADCData>  entries = new ArrayList<FADCData>();
        List<EvioTreeBranch> branches = this.getEventBranches(event);
        for(EvioTreeBranch branch : branches){
            List<FADCData>  list = this.getADCEntries(event,branch.getTag());
            if(list != null){
                entries.addAll(list);
            }
        }
        return entries;
    }

    public List<FADCData> getADCEntries(EvioDataEvent event, int crate){
        List<FADCData>  entries = new ArrayList<FADCData>();

        List<EvioTreeBranch> branches = this.getEventBranches(event);
        EvioTreeBranch cbranch = this.getEventBranch(branches, crate);

        if(cbranch == null ) return null;

        for(EvioNode node : cbranch.getNodes()){

            if(node.getTag()==57638){
                 //System.out.println(" NODE = " + node.getTag() + " , " + node.getNum() +
                 //        " , " + node.getTypeObj().name());
                return this.getDataEntries_57638(crate, node, event);
            }
        }

        return entries;
    }

    public List<FADCData> getADCEntries(EvioDataEvent event, int crate, int tagid){

        List<FADCData>  adc = new ArrayList<FADCData>();
        List<EvioTreeBranch> branches = this.getEventBranches(event);


        EvioTreeBranch cbranch = this.getEventBranch(branches, crate);
        if(cbranch == null ) return null;

        for(EvioNode node : cbranch.getNodes()){
//
             //if(node.getTag()==57638){
           if(node.getTag()==tagid){
                //  This is regular integrated pulse mode, used for FTOF
                // FTCAL and EC/PCAL
                return this.getADCEntries_Tag(crate, node, event,tagid);
                //return this.getDataEntriesMode_7(crate,node, event);
            }

        }
        return adc;
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

        for (EvioNode node : cbranch.getNodes()) {
//            System.out.println(" analyzing tag = " + node.getTag());
            if (node.getTag() == 57615) {
                //  This is regular integrated pulse mode, used for FTOF
                // FTCAL and EC/PCAL
                //return this.getDataEntries_57602(crate, node, event);
                this.tiMaster = crate;
                this.readHeaderBank(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
        }
        for(EvioNode node : cbranch.getNodes()){
//            System.out.println(" analyzing tag = " + node.getTag());

//            if(node.getTag()==57615){
//                //  This is regular integrated pulse mode, used for FTOF
//                // FTCAL and EC/PCAL
//                //return this.getDataEntries_57602(crate, node, event);
//                this.readHeaderBank(crate, node, event);
//                //return this.getDataEntriesMode_7(crate,node, event);
//            }
            if(node.getTag()==57617){
                //  This is regular integrated pulse mode, used for FTOF
                // FTCAL and EC/PCAL
                //return this.getDataEntries_57602(crate, node, event);

                return this.getDataEntries_57617(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
            else if(node.getTag()==57602){
                //  This is regular integrated pulse mode, used for FTOF
                // FTCAL and EC/PCAL
                //return this.getDataEntries_57602(crate, node, event);

                return this.getDataEntries_57602(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
            else if(node.getTag()==57601){
                //  This is regular integrated pulse mode, used for FTOF
                // FTCAL and EC/PCAL

                return this.getDataEntries_57601(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
            else if(node.getTag()==57627){
                //  This is regular integrated pulse mode, used for MM

                return this.getDataEntries_57627(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
            else if(node.getTag()==57640){
                //  This is bit-packed pulse mode, used for MM

                return this.getDataEntries_57640(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
            else if(node.getTag()==57622){
                //  This is regular integrated pulse mode, used for FTOF
                // FTCAL and EC/PCAL

                return this.getDataEntries_57622(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            }
            else if(node.getTag()==57636){
                //  RICH TDC data
                return this.getDataEntries_57636(crate, node, event);
                //return this.getDataEntriesMode_7(crate,node, event);
            } else if(node.getTag()==57641){
                //  RTPC  data decoding
                return this.getDataEntries_57641(crate, node, event);
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
                int[] intData = ByteDataTransformer.toIntArray(node.getStructureBuffer(true));
                this.runNumber = intData[3];
                this.eventNumber = intData[4];
                if(intData[5]!=0) this.unixTime  = intData[5];
                this.helicityLevel3=HelicityBit.DNE.value();
                if(intData.length>7) {
                    if ( (intData[7] & 0x1) == 0) {
                        this.helicityLevel3=HelicityBit.UDF.value();
                    }
                    else if ((intData[7]>>1 & 0x1) == 0) {
                        this.helicityLevel3=HelicityBit.MINUS.value();
                    }
                    else {
                        this.helicityLevel3=HelicityBit.PLUS.value();
                    }
                }                /*System.out.println(" set run number and event nubmber = "
                + this.runNumber + "  " + this.eventNumber + "  " + this.unixTime + "  " + intData[5]
                );
                System.out.println(" EVENT BUFFER LENGTH = " + intData.length);
                for(int i = 0; i < intData.length; i++){
                System.out.println( i + " " + String.format("%08X", intData[i]));
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
//                System.out.println("Found SVT bank");
                ByteBuffer     compBuffer = node.getByteData(true);
                //System.out.println(" COMPOSITE TYPE   = " + node.getTypeObj().name() + " "
                //+ node.getDataTypeObj().name());
                //System.out.println(" COMPOSITE BUFFER = " + compBuffer.array().length);
                /*
                for(int i = 0; i < compBuffer.array().length; i++){
                    short value = (short) (0x00FF&(compBuffer.array()[i]));
                    System.out.println(String.format("%4d ",value));
                }
                System.out.println();
                */
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
                        Byte   adcbyte = (Byte)  cdataitems.get(position+3);

                        // regular FSSR data entry
                        int halfWord = DataUtils.getIntFromByte(half);
                        int   chipID = DataUtils.getInteger(halfWord, 0, 2);
                        int   halfID = DataUtils.getInteger(halfWord, 3, 3);
                        int   adc    = adcbyte;
                        Integer channelKey = ((half<<8) | (channel & 0xff));

//                        System.err.println( "Half/chip = " + half + " CHIP = " + chipID + " HALF = " + halfID + "  CHANNEL = " + channel + " KEY = " + channelKey  );

                        // TDC data entry
                        if(half == -128) {
                            halfWord   = DataUtils.getIntFromByte(channel);
                            halfID     = DataUtils.getInteger(halfWord, 2, 2);
                            chipID     = DataUtils.getInteger(halfWord, 0, 1) + 1;
                            channel    = 0;
                            channelKey = 0;
                            tdc = (short) ((adcbyte<<8) | (tdcbyte & 0xff));
//                            System.err.println( "Half/chip = " + half + " CHIP = " + chipID + " HALF = " + halfID + " TDC = " + tdcbyte + "  ADC = " + adc + " Time = " + tdc  );
                            adc = -1;
                        }

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

    public List<FADCData>  getADCEntries_Tag(Integer crate, EvioNode node, EvioDataEvent event, int tagid){
        List<FADCData>  entries = new ArrayList<FADCData>();
        if(node.getTag()==tagid){
          //if(node.getTag()==57638){
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
                        //DetectorDataDgtz bank = new DetectorDataDgtz(crate,slot.intValue(),channel.intValue());
                        FADCData   bank = new FADCData(crate,slot.intValue(),channel.intValue());
                        //dataBank.addChannel(channel.intValue());
                        short[] shortbuffer = new short[length];
                        for(int loop = 0; loop < length; loop++){
                            Short sample    = (Short) cdataitems.get(position+2+loop);
                            shortbuffer[loop] = sample;
                            //dataBank.addData(channel.intValue(),
                            //        new RawData(tdc,adc,pmin,pmax));
                        }
                        bank.setBuffer(shortbuffer);
                        //bank.addPulse(shortbuffer);
                        //bank.setTimeStamp(time);
                        //dataBank.addData(channel.intValue(),
                        //            new RawData(shortbuffer));
                        entries.add(bank);
                        position += 2+length;
                        counter++;
                    }
                }
                return entries;

            } catch (EvioException ex) {
                ByteBuffer     compBuffer = node.getByteData(true);
                System.out.println("Exception in CRATE = " + crate + "  RUN = " + this.runNumber
                + "  EVENT = " + this.eventNumber + " LENGTH = " + compBuffer.array().length);
                this.printByteBuffer(compBuffer, 120, 20);
//                Logger.getLogger(CodaEventDecoder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return entries;
        //return entries;
    }
    /*
    * 	<dictEntry name="FADC250 Window Raw Data (mode 1 packed)" tag="0xe126" num="0" type="composite">
    * <description format="c,m(c,ms)">
    *  c 	"slot number"
    * m	"number of channels fired"
    * c	"channel number"
    * m	"number of shorts in packed array"
    * s	"packed fadc data"
    * </description>
    * </dictEntry>
    */
    public void decodeComposite(ByteBuffer buffer, int offset, List<DataType> ctypes, List<Object> citems){
        int position = offset;
        int length   = buffer.capacity();
        try {
            while(position<(length-3)){
                Short slot = (short) (0x00FF&(buffer.get(position)));
                position++;
                citems.add(slot);
                ctypes.add(DataType.SHORT16);
                Short counter =  (short) (0x00FF&(buffer.get(position)));
                citems.add(counter);
                ctypes.add(DataType.NVALUE);
                position++;

                for(int i = 0; i < counter; i++){
                    Short channel = (short) (0x00FF&(buffer.get(position)));
                    position++;
                    citems.add(channel);
                    ctypes.add(DataType.SHORT16);
                    Short ndata = (short) (0x00FF&(buffer.get(position)));
                    position++;
                    citems.add(ndata);
                    ctypes.add(DataType.NVALUE);
                    for(int b = 0; b < ndata; b++){
                        Short data = buffer.getShort(position);
                        position+=2;
                        citems.add(data);
                        ctypes.add(DataType.SHORT16);
                    }
                }
            }
        } catch (Exception e){
            System.out.println("Exception : Length = " + length + "  position = " + position);
        }
    }

    public List<FADCData>  getDataEntries_57638(Integer crate, EvioNode node, EvioDataEvent event){
        List<FADCData>  entries = new ArrayList<FADCData>();
        if(node.getTag()==57638){
            //try {
                ByteBuffer     compBuffer = node.getByteData(true);
                //System.out.println(" COMPOSITE TYPE   = " + node.getTypeObj().name() + " " + node.getDataTypeObj().name());
                //System.out.println(" COMPOSITE BUFFER = " + compBuffer.array().length);

                /*for(int i = 0; i < compBuffer.array().length; i++){
                    short value = (short) (0x00FF&(compBuffer.array()[i]));
                    System.out.println(String.format("%4d %4d ",i,value));
                }*/
                //System.out.println();
                List<DataType> cdatatypes = new ArrayList<DataType>();
                List<Object>   cdataitems = new ArrayList<Object>();
                this.decodeComposite(compBuffer, 24, cdatatypes, cdataitems);

            /*try {
                CompositeData  compData = new CompositeData(compBuffer.array(),event.getByteOrder());
                List<DataType> ccdatatypes = compData.getTypes();
                List<Object>   ccdataitems = compData.getItems();
                System.out.println();
                System.out.println(" CDATA/ CTYPES = " + ccdatatypes.size() + " / " + ccdataitems.size());
                for(int i = 0; i < ccdatatypes.size(); i++){
                System.out.println("  TYPE = " + ccdatatypes.get(i) + "     ->  " + ccdataitems.get(i));
                }
            } catch (EvioException ex) {
                Logger.getLogger(CodaEventDecoder.class.getName()).log(Level.SEVERE, null, ex);
            }*/



                /*
                CompositeData  compData = new CompositeData(compBuffer.array(),event.getByteOrder());

                List<DataType> cdatatypes = compData.getTypes();
                List<Object>   cdataitems = compData.getItems();

                */
                /*if(cdatatypes.get(3) != DataType.NVALUE){
                    System.err.println("[EvioRawDataSource] ** error ** corrupted "
                    + " bank. tag = " + node.getTag() + " num = " + node.getNum());
                    return null;
                }*/

                int position = 0;

                while(position<cdatatypes.size()-3){
                    Short       slot = (Short)       cdataitems.get(position+0);
                    Short  nchannels =  (Short) cdataitems.get(position+1);

                    position += 2;
                    //System.out.println("position = " + position + "  /  size = " + cdataitems.size());
                    int     counter = 0;
                    while(counter<nchannels){
                       // System.out.println("N CHANNELS position = " + position + "  /  size = " + cdataitems.size());
                       Short   channel = (Short) cdataitems.get(position);
                       Short   length  = (Short) cdataitems.get(position+1);
                       position +=2;
                       short[] shortbuffer = new short[length];
                       for(int loop = 0; loop < length; loop++){
                           Short sample    = (Short) cdataitems.get(position+loop);
                           shortbuffer[loop] = sample;
                       }
                       position+=length;
                       counter++;
                       FADCData data = new FADCData(crate,slot,channel);
                       data.setBuffer(shortbuffer);
                       if(length>18) entries.add(data);
                    }
                }
                //System.out.println(" Data Types = " + cdatatypes.size() + " data items = " + cdataitems.size());

            /*} catch (EvioException ex) {
                ByteBuffer     compBuffer = node.getByteData(true);
                System.out.println("Exception in CRATE = " + crate + "  RUN = " + this.runNumber
                + "  EVENT = " + this.eventNumber + " LENGTH = " + compBuffer.array().length);
                this.printByteBuffer(compBuffer, 120, 20);
                ex.printStackTrace();
                //Logger.getLogger(CodaEventDecoder.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        }
        return entries;
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
                ByteBuffer     compBuffer = node.getByteData(true);
                System.out.println("Exception in CRATE = " + crate + "  RUN = " + this.runNumber
                + "  EVENT = " + this.eventNumber + " LENGTH = " + compBuffer.array().length);
                this.printByteBuffer(compBuffer, 120, 20);
//                Logger.getLogger(CodaEventDecoder.class.getName()).log(Level.SEVERE, null, ex);
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
                        //Added pulse fitting for MMs
                        ADCData adcData = new ADCData();
			//adcData.setTimeStamp(timeStamp); // bug fixed
                        adcData.setTimeStamp(time);
			adcData.setPulse(shortbuffer);
                        bank.addADC(adcData);
                        //bank.addPulse(shortbuffer);
                        //bank.setTimeStamp(time);
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
     * Decoding MicroMegas Packed Data
     * @param crate
     * @param node
     * @param event
     * @return
     */
    public List<DetectorDataDgtz>  getDataEntries_57640(Integer crate, EvioNode node, EvioDataEvent event){
        // Micromegas packed data
        // ----------------------

        ArrayList<DetectorDataDgtz>  entries = new ArrayList<DetectorDataDgtz>();
        if(node.getTag()==57640){
            try {
                ByteBuffer     compBuffer = node.getByteData(true);
                CompositeData  compData = new CompositeData(compBuffer.array(),event.getByteOrder());

                List<DataType> cdatatypes = compData.getTypes();
                List<Object>   cdataitems = compData.getItems();

                int jdata = 0;  // item counter
                for( int i = 0 ; i < cdatatypes.size();  ) { // loop over data types
                	Byte CRATE     =  (Byte)cdataitems.get( jdata++ ); i++;

                	Integer EV_ID  = (Integer)cdataitems.get( jdata++ ); i++;

                	Long TIMESTAMP =  (Long)cdataitems.get( jdata++ ); i++;

                	Short nChannels =  (Short)cdataitems.get( jdata++ ); i++;

                	for( int ch=0; ch<nChannels; ch++ ) {
                    	Short CHANNEL = (Short)cdataitems.get( jdata++ ); i++;

                    	int nBytes = (Byte)cdataitems.get( jdata++ ); i++;

                    	DetectorDataDgtz bank = new DetectorDataDgtz(crate,CRATE.intValue(),CHANNEL.intValue());

                    	int nSamples = nBytes*8/12;
                    	short[] samples = new short[ nSamples ];
                    	for( short t : samples ) { t = 0x00; }

                    	int s = 0;
                    	for( int b=0;b<nBytes;b++ ) {
                    		short data = (short)((byte)cdataitems.get( jdata++ )&0xFF);

                    		s = (int)Math.floor( b * 8./12. );
                    		if( b%3 != 1) {
                    			samples[s] += (short)data;
                    		}
                    		else {
                    			samples[s] += (data&0x000F)<<8;
                    			if( s+1 < nSamples ) samples[s+1] += ((data&0x00F0)>>4)<<8;
                    		}

                    	}
                    	i++;

                      ADCData adcData = new ADCData();
                      adcData.setTimeStamp(TIMESTAMP);
                      adcData.setPulse(samples);
                      bank.addADC(adcData);
                      entries.add(bank);
                	} // end loop on channels
                } // end loop on data types
                return entries;

            } catch (EvioException ex) {
                Logger.getLogger(CodaEventDecoder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return entries;
    }

        /**
     * Decoding MicroMegas Packed Data
     * @param crate
     * @param node
     * @param event
     * @return
     */
    public List<DetectorDataDgtz>  getDataEntries_57641(Integer crate, EvioNode node, EvioDataEvent event){
        // Micromegas packed data
        // ----------------------

        ArrayList<DetectorDataDgtz>  entries = new ArrayList<DetectorDataDgtz>();
        if(node.getTag()==57641){
            try {
                ByteBuffer     compBuffer = node.getByteData(true);
                CompositeData  compData = new CompositeData(compBuffer.array(),event.getByteOrder());

                List<DataType> cdatatypes = compData.getTypes();
                List<Object>   cdataitems = compData.getItems();

                //System.out.println("composite data size = " + cdatatypes.size());
                int jdata = 0;  // item counter
                for( int i = 0 ; i < cdatatypes.size();  ) { // loop over data types
                	Byte SLOT     =  (Byte)cdataitems.get( jdata++ ); i++;

                	Integer EV_ID  = (Integer)cdataitems.get( jdata++ ); i++;

                	Long TIMESTAMP =  (Long)cdataitems.get( jdata++ ); i++;

                	Short nChannels =  (Short)cdataitems.get( jdata++ ); i++;

                	for( int ch=0; ch<nChannels; ch++ ) {
                    	Short CHANNEL = (Short)cdataitems.get( jdata++ ); i++;


                        int nPulses = (Byte)cdataitems.get( jdata++ ); i++;
                        //System.out.println(" CHANNEL = " + CHANNEL
                        //        + " n pulses = " + nPulses);
                        for(int np = 0; np < nPulses; np++){

                            int firstChannel = (Byte) cdataitems.get( jdata++ ); i++;

                            int nBytes = (Byte)cdataitems.get( jdata++ ); i++;

                            //System.out.println("CREATING CRATE : " + crate +
                            //        "  SLOT : " + SLOT + " CHANNEL : " + CHANNEL);
                            DetectorDataDgtz bank = new DetectorDataDgtz(crate,SLOT.intValue(),CHANNEL.intValue());

                            int nSamples = nBytes*8/12;
                            //System.out.println("CHANNEL = " + CHANNEL + " NSAMPLES = " + nSamples + "  CHANNEL = " + firstChannel);
                            short[] samples = new short[ nSamples ];
                            for( short t : samples ) { t = 0x00; }

                            int s = 0;
                            for( int b=0;b<nBytes;b++ ) {
                                short data = (short)((byte)cdataitems.get( jdata++ )&0xFF);

                                s = (int)Math.floor( b * 8./12. );
                                if( b%3 != 1) {
                                    samples[s] += (short)data;
                                }
                    		else {
                    			samples[s] += (data&0x000F)<<8;
                    			if( s+1 < nSamples ) samples[s+1] += ((data&0x00F0)>>4)<<8;
                    		}
                    	/*ADCData adcData = new ADCData();
                        adcData.setTimeStamp(TIMESTAMP);
                        adcData.setPulse(samples);
                        bank.addADC(adcData);
                        entries.add(bank);*/
                            }
                    	i++;

                        ADCData adcData = new ADCData();
                        adcData.setTimeStamp(TIMESTAMP);
                        adcData.setPulse(samples);
                        adcData.setTime(firstChannel);
                        bank.addADC(adcData);

                        entries.add(bank);
                        }
                        } // end loop on channels
                } // end loop on data types
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
     * Bank TAG=57636 used for RICH TDC values
     * @param crate
     * @param node
     * @param event
     * @return
     */
    public List<DetectorDataDgtz>  getDataEntries_57636(Integer crate, EvioNode node, EvioDataEvent event){

        ArrayList<DetectorDataDgtz>  entries = new ArrayList<DetectorDataDgtz>();

        if(node.getTag()==57636){
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

                    Integer nchannels = (Integer) cdataitems.get(position+3);
                    //System.out.println("Retrieving the data size = " + cdataitems.size()
                    //+ "  " + cdatatypes.get(3) + " number of channels = " + nchannels);
                    position += 4;
                    int counter  = 0;

                    while(counter<nchannels){
                        //System.err.println("Position = " + position + " type =  "
                        //+ cdatatypes.get(position));
                        Integer fiber = ((Byte) cdataitems.get(position))&0xFF;
                        Integer channel = ((Byte) cdataitems.get(position+1))&0xFF;
                        Short rawtdc = (Short) cdataitems.get(position+2);
                        int edge = (rawtdc>>15)&0x1;
                        int tdc = rawtdc&0x7FFF;

                        DetectorDataDgtz bank = new DetectorDataDgtz(crate,slot.intValue(),2*(fiber*192+channel)+edge);
                        bank.addTDC(new TDCData(tdc));

                        //Integer tdc = rawtdc&0x7FFF;
                        //Integer edge = (rawtdc>>15)&0x1;

                        entries.add(bank);
                        position += 3;
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

    public void getDataEntries_EPICS(EvioDataEvent event){
        epicsData = new JsonObject();
        List<EvioTreeBranch> branches = this.getEventBranches(event);
        for(EvioTreeBranch branch : branches){
            for(EvioNode node : branch.getNodes()){
                if(node.getTag()==57620) {
                    byte[] stringData =  ByteDataTransformer.toByteArray(node.getStructureBuffer(true));
                    //System.out.println("Found epics bank " + stringData.length);
                    String cdata = new String(stringData);
                    String[] vars = cdata.trim().split("\n");
                    for (String var : vars) {
                        String[] fields=var.trim().replaceAll("  "," ").split(" ");
                        if (fields.length != 2) continue;
                        String key = fields[1].trim();
                        String sval = fields[0].trim();
                        try {
                            float fval = Float.parseFloat(sval);
                            epicsData.add(key,fval);
                        }
                        catch (NumberFormatException e) {
                            System.err.println("WARNING:  Ignoring EPICS Bank row:  "+var);
                        }
                    }
                    //System.out.println(epicsData);
                }
            }
        }
    }

    public List<DetectorDataDgtz> getDataEntries_Scalers(EvioDataEvent event){

        List<DetectorDataDgtz> scalerEntries = new ArrayList<DetectorDataDgtz>();
//        this.triggerBank = null;
//        System.out.println(" READING SCALER BANK");
        List<EvioTreeBranch> branches = this.getEventBranches(event);
        for(EvioTreeBranch branch : branches){
            int  crate = branch.getTag();
//            EvioTreeBranch cbranch = this.getEventBranch(branches, branch.getTag());
            for(EvioNode node : branch.getNodes()){
                if(node.getTag()==57637 || node.getTag()==57621){
//                    System.out.println("TRIGGER BANK FOUND ");
                    int num = node.getNum();
                    int[] intData =  ByteDataTransformer.toIntArray(node.getStructureBuffer(true));
//                    if(intData.length!=0) System.out.println(" TRIGGER BANK LENGTH = " + intData.length);
                    for(int loop = 2; loop < intData.length; loop++){
                        int  dataEntry = intData[loop];
                        if(node.getTag()==57637) {
                            int helicity = DataUtils.getInteger(dataEntry, 31, 31);
                            int quartet  = DataUtils.getInteger(dataEntry, 30, 30);
                            int interval = DataUtils.getInteger(dataEntry, 29, 29);
                            int id       = DataUtils.getInteger(dataEntry, 24, 28);
                            long value   = DataUtils.getLongFromInt(DataUtils.getInteger(dataEntry,  0, 23));
                            if(id < 3) {
                                DetectorDataDgtz entry = new DetectorDataDgtz(crate,num,id+32*interval);
                                SCALERData scaler = new SCALERData();
                                scaler.setHelicity((byte) helicity);
                                scaler.setQuartet((byte) quartet);
                                scaler.setValue(value);
                                entry.addSCALER(scaler);
                                scalerEntries.add(entry);
                            }
//                            System.out.println(entry.toString());
                        }
                        else if(node.getTag()==57621 && loop>=5) {
                            int id   = (loop-5)%16;
                            int slot = (loop-5)/16;
                            if(id<3 && slot<4) {
                                DetectorDataDgtz entry = new DetectorDataDgtz(crate,num,loop-5);
                                SCALERData scaler = new SCALERData();
                                scaler.setValue(DataUtils.getLongFromInt(dataEntry));
                                entry.addSCALER(scaler);
                                scalerEntries.add(entry);
//                                long long_data = 0;
//                                long  value = (long) ((long_data|dataEntry)&0x00000000FFFFFFFFL);
//                                System.out.println(loop + " " + crate + " " + slot + " " + id + " " + dataEntry + " " + value + " " + DataUtils.getLongFromInt(dataEntry) + " " + String.format("0x%08X", dataEntry) + " " + String.format("0x%16X", value));
                            }
                        }
                    }
                }
            }
        }
        return scalerEntries;
    }

    public List<DetectorDataDgtz> getDataEntries_VTP(EvioDataEvent event){

        List<DetectorDataDgtz> vtpEntries = new ArrayList<DetectorDataDgtz>();
//        this.triggerBank = null;
        //System.out.println(" READING TRIGGER BANK");
        List<EvioTreeBranch> branches = this.getEventBranches(event);
        for(EvioTreeBranch branch : branches){
            int  crate = branch.getTag();
//            EvioTreeBranch cbranch = this.getEventBranch(branches, branch.getTag());
            for(EvioNode node : branch.getNodes()){
                if(node.getTag()==57634/*&&crate==125*/){
//                    System.out.println("TRIGGER BANK FOUND ");
                    int[] intData =  ByteDataTransformer.toIntArray(node.getStructureBuffer(true));
//                    if(intData.length!=0) System.out.println(" TRIGGER BANK LENGTH = " + intData.length);
                    for(int loop = 0; loop < intData.length; loop++){
                        int  dataEntry = intData[loop];
                        DetectorDataDgtz   entry = new DetectorDataDgtz(crate,0,0);
                        entry.addVTP(new VTPData(dataEntry));
//                        System.out.println(crate + " " + dataEntry + " " + entry.toString());
                        vtpEntries.add(entry);
//                        System.out.println(entry.toString());
                    }
                }
            }
        }
//        System.out.println(vtpEntries.size());
        return vtpEntries;
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
                    int[] intData = ByteDataTransformer.toIntArray(node.getStructureBuffer(true));
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
                    long[] longData = ByteDataTransformer.toLongArray(node.getStructureBuffer(true));
                    int[]  intData  = ByteDataTransformer.toIntArray(node.getStructureBuffer(true));
                    long     tStamp = longData[2]&0x0000ffffffffffffL;

		    // Below is endian swap if needed
		    //long    ntStamp = (((long)(intData[5]&0x0000ffffL))<<32) | (intData[4]&0xffffffffL);
		    //System.out.println(longData[2]+" "+tStamp+" "+crate+" "+node.getDataLength());

                    DetectorDataDgtz entry = new DetectorDataDgtz(crate,0,0);
                    entry.setTimeStamp(tStamp);
                    if(node.getDataLength()==4) tiEntries.add(entry);
                    else if(node.getDataLength()==5) { // trigger supervisor crate
                        this.setTriggerBits(intData[6]);
                    }
		    else if(node.getDataLength()==6) { // New format Dec 1 2017 (run 1701)
			this.setTriggerBits(intData[6]<<16|intData[7]);
		    }
		    else if(node.getDataLength()==7) { // New format Dec 1 2017 (run 1701)
			long word = (( (long) intData[7])<<32) | (intData[6]&0xffffffffL);
			this.setTriggerBits(word);
                        this.triggerWords.clear();
                        for(int i=6; i<=8; i++) {
                            this.triggerWords.add(intData[i]);
//                            System.out.println(this.triggerWords.get(this.triggerWords.size()-1));
                        }
		    }
                }
            }
        }

        return tiEntries;
    }


    public static void main(String[] args){
        EvioSource reader = new EvioSource();
        reader.open("/Users/devita/clas_004013.evio.1000");
        CodaEventDecoder decoder = new CodaEventDecoder();
        DetectorEventDecoder detectorDecoder = new DetectorEventDecoder();

        int maxEvents = 5000;
        int icounter  = 0;

        while(reader.hasEvent()==true&&icounter<maxEvents){

            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            List<DetectorDataDgtz>  dataSet = decoder.getDataEntries(event);
            detectorDecoder.translate(dataSet);
            detectorDecoder.fitPulses(dataSet);
            if(decoder.getDataEntries_VTP(event).size()!=0) {
//                for(DetectorDataDgtz entry : decoder.getDataEntries_VTP(event))
//                System.out.println(entry.toString());
            }
//            System.out.println("---> printout EVENT # " + icounter);
//            for(DetectorDataDgtz data : dataSet){
//                System.out.println(data);
//            }
            icounter++;
        }
        System.out.println("Done...");
    }
}
