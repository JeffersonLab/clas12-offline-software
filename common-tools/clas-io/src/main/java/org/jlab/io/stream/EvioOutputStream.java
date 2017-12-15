/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.stream;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EventBuilder;
import org.jlab.coda.jevio.EventWriter;
import org.jlab.coda.jevio.EvioBank;

import org.jlab.coda.jevio.EvioCompactStructureHandler;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioDataSync;

/**
 *
 * @author gavalian
 */
public class EvioOutputStream {
    private ByteBuffer evioBuffer;
    private EvioCompactStructureHandler structure = null;
    private final ByteOrder writerByteOrder  = ByteOrder.LITTLE_ENDIAN;
    private final Integer   histogramBankTag = 200;
    private final EvioDataSync    writer = new EvioDataSync();
    
    public EvioOutputStream(String filename){
        writer.open(filename);
    }
    
    public EvioOutputStream(){
        this.createEvent();
    }
    /**
     * Writes Array of objects into the file.
     * @param objArray 
     */
    public void write(ArrayList<EvioStreamObject> objArray){
        for(EvioStreamObject obj : objArray){
            this.writeObject(obj);
        }
    }
    /**
     * writes one object with the interface EvioTreeStream into the file.
     * @param obj 
     */
    public void writeObject(EvioStreamObject obj){
        this.writeTree(obj.getStreamData());
    }
    
    public void writeTree(TreeMap<Integer,Object> tree){
        /*
        EventBuilder builder = new EventBuilder(1,DataType.BANK,0);
            EvioEvent event = builder.getEvent();
            EvioBank baseBank = new EvioBank(histogramBankTag, DataType.ALSOBANK, 0);
            baseBank.setByteOrder(writerByteOrder);
          */  
            
        EvioEvent baseBank = new EvioEvent(histogramBankTag, DataType.ALSOBANK, 0);
        baseBank.setByteOrder(writerByteOrder);
        EventBuilder builder = new EventBuilder(baseBank);
        
        for(Map.Entry<Integer,Object> entry : tree.entrySet()){
            Object obj = entry.getValue();
            Integer num = entry.getKey();
            /*
            * writing double values to the base Bank structure.
            */
            if(obj instanceof double[]){
                try {
                    EvioBank treeData = new EvioBank(histogramBankTag, DataType.DOUBLE64, num);
                    treeData.setByteOrder(writerByteOrder);
                    treeData.appendDoubleData( (double[])  obj);
                    builder.addChild(baseBank, treeData);                        
                } catch (EvioException ex) {
                    Logger.getLogger(EvioOutputStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(obj instanceof float[]){
                try {
                    EvioBank treeData = new EvioBank(histogramBankTag, DataType.FLOAT32, num);
                    treeData.setByteOrder(writerByteOrder);
                    treeData.appendFloatData((float[])  obj);
                    builder.addChild(baseBank, treeData);                        
                } catch (EvioException ex) {
                    Logger.getLogger(EvioOutputStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            /**
             * Writing integer values to the bank structure.
             */
            if(obj instanceof int[]){
                try {
                    EvioBank treeData = new EvioBank(histogramBankTag, DataType.INT32, num);
                    treeData.setByteOrder(writerByteOrder);
                    treeData.appendIntData( (int[])  obj);
                    builder.addChild(baseBank, treeData);                        
                } catch (EvioException ex) {
                    Logger.getLogger(EvioOutputStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            /**
             * Writing integer values to the bank structure.
             */
            if(obj instanceof byte[]){
                try {
                    EvioBank treeData = new EvioBank(histogramBankTag, DataType.CHAR8, num);
                    treeData.setByteOrder(writerByteOrder);
                    treeData.appendByteData((byte[])  obj);
                    builder.addChild(baseBank, treeData);                        
                } catch (EvioException ex) {
                    Logger.getLogger(EvioOutputStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        
        EvioEvent event = builder.getEvent();
        int byteSize = event.getTotalBytes();
        //System.out.println("base bank size = " + byteSize);
        ByteBuffer bb = ByteBuffer.allocate(byteSize);
        bb.order(writerByteOrder);
        event.write(bb);
        bb.flip();
        
        EvioDataEvent dataEvent = new EvioDataEvent(bb);
        writer.writeEvent(dataEvent);            
    }
    
    public void close(){
        writer.close();
    }
           
    /**
     * Creates the event structure
     */
    private void createEvent(){
        try {
            //EvioEvent baseBank = new EvioEvent(1, DataType.BANK, 0);
            EventBuilder builder = new EventBuilder(1,DataType.BANK,0);
            EvioEvent event = builder.getEvent();
            EvioBank baseBank = new EvioBank(10, DataType.ALSOBANK, 0);
            
            builder.addChild(event, baseBank);
            
            ByteOrder byteOrder = writerByteOrder;
            
            int byteSize = event.getTotalBytes();
            //System.out.println("base bank size = " + byteSize);
            ByteBuffer bb = ByteBuffer.allocate(byteSize);
            bb.order(byteOrder);
            event.write(bb);
            bb.flip();
            structure = new EvioCompactStructureHandler(bb,DataType.BANK);
        } catch (EvioException ex) {
            Logger.getLogger(EvioDataSync.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void writeToFile(String filename){
        File file = new File(filename);
        try {
            EventWriter evioWriter = new EventWriter(new File(filename), null, true);
            ByteBuffer original = structure.getByteBuffer();
            ByteBuffer clone = ByteBuffer.allocate(original.capacity());
            clone.order(original.order());
            original.rewind();
            clone.put(original);
            original.rewind();
            clone.flip();
            evioWriter.writeEvent(clone); 
            evioWriter.close();
//new EventWriter(file, 1000000, 2,
            //ByteOrder.BIG_ENDIAN, null, null);
        } catch (EvioException ex) {
            Logger.getLogger(EvioDataSync.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EvioOutputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args){
        EvioOutputStream writer = new EvioOutputStream("/Users/gavalian/Work/outputhistogram.evio");
        TreeMap<Integer,Object>  histos = new TreeMap<Integer,Object>();
        String name = "Histogram_13";
        byte[] namedata = name.getBytes();
        histos.put(9, namedata);
        histos.put(10, new int[]{3,4} );
        histos.put(11, new double[]{4.5,6.7} );
        histos.put(12, new double[]{0.5,1.5,2.5,3.5});        
        writer.writeTree(histos);
        writer.close();
    }
}
