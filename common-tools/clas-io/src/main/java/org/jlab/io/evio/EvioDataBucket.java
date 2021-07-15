/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.evio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EventBuilder;
import org.jlab.coda.jevio.EventWriter;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioReader;

/**
 *
 * @author gavalian
 */
public class EvioDataBucket {
    
    private EventWriter writer = null;
    private EvioReader  reader = null;
    
    private ByteBuffer  eventBuffer = null;
    private int         MAX_BUFFER_SIZE = 10*1024*1024;
    
    public EvioDataBucket(){
        
    }
    
    public EvioDataBucket(byte[] array, ByteOrder order){
        
        this.eventBuffer = ByteBuffer.wrap(array);
        this.eventBuffer.order(order);
        
        try {
            
            reader = new EvioReader(this.eventBuffer);
            
            System.out.println("number of events = " + reader.getEventCount() 
                    + " buffer length = " + array.length);
            int counter = 0;
            
            EvioEvent event = reader.parseNextEvent();
            if(event==null) System.out.println(" event is null " );
            for(int loop = 0; loop < array.length; loop++){
                System.out.print(String.format(" %6X ", array[loop]));
                if(loop%10==0) System.out.println();
            }
            while(event!=null){
                event = reader.parseNextEvent();
                counter++;
            }
            System.out.println("event count = " + counter);
        } catch (EvioException ex) {
            Logger.getLogger(EvioDataBucket.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EvioDataBucket.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void openWriter(String filename){
        this.eventBuffer = ByteBuffer.allocate(this.MAX_BUFFER_SIZE);
        this.eventBuffer.order(ByteOrder.LITTLE_ENDIAN);
        try {
            writer = new EventWriter(this.eventBuffer, null);
        } catch (EvioException ex) {
            Logger.getLogger(EvioDataBucket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void writeEvent(EvioEvent ev){
        try {
            this.writer.writeEvent(ev);
        } catch (EvioException ex) {
            Logger.getLogger(EvioDataBucket.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EvioDataBucket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void writeEvent(ByteBuffer buffer){
        try {
            writer.writeEvent(buffer);
            System.out.println("WRITING EVENT BUFFER of LENGTH " + buffer.capacity());
        } catch (EvioException ex) {
            Logger.getLogger(EvioDataBucket.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EvioDataBucket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean hasSpace(EvioDataEvent event){
        return (event.getEventBuffer().capacity()+writer.getBytesWrittenToBuffer()>=this.MAX_BUFFER_SIZE);
    }
    
    
    public byte[] getEventArray(){
        long bytesWritten = this.writer.getBytesWrittenToBuffer();
        int  alloc        = (int) bytesWritten;
        byte[] array = new byte[alloc];
        this.eventBuffer.get(array,0,array.length);
        return array;
    }
        
    public byte[]  getEventArrayGzip(){
        return EvioDataBucket.gzip(this.getEventArray());
    }
    
    public static byte[] gzip(byte[] ungzipped) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bytes);
            gzipOutputStream.write(ungzipped);
            gzipOutputStream.close();
        } catch (IOException e) {
           // LOG.error("Could not gzip " + Arrays.toString(ungzipped));
            System.out.println("[iG5DataCompressor] ERROR: Could not gzip the array....");
        }
        return bytes.toByteArray();
    }
    
    public void writeEvent(EvioDataEvent event){
        this.writeEvent(event.getEventBuffer());
    }
    
    public void show(){
        System.out.println("ALLOCATED SIZE = " + this.eventBuffer.capacity()
        + "   LIMIT " + this.eventBuffer.limit() + " BYTES WRITTEN = " +
                writer.getBytesWrittenToBuffer());
    }
    
    
    
    public static void main(String[] args){
        EvioDataBucket  bucket = new EvioDataBucket();
        bucket.openWriter("test.evio");
        bucket.show();
        for(int loop = 0; loop < 2; loop++){
            System.out.println("WRITING event # " + loop);
            EventBuilder eb = new EventBuilder(1, DataType.INT32, 1);
            EvioEvent ev = eb.getEvent();
            int[] dat = new int[24];
            try {
                ev.appendIntData(dat);
                bucket.writeEvent(ev);
            } catch (EvioException ex) {
                Logger.getLogger(EvioDataBucket.class.getName()).log(Level.SEVERE, null, ex);
            }
            //EvioDataEvent event = EvioFactory.createEvioEvent();
            //bucket.writeEvent(event.getEventBuffer());
        }
        
        bucket.show();
        
        EvioDataBucket  rb = new EvioDataBucket(bucket.getEventArray(), ByteOrder.LITTLE_ENDIAN);
    }
}
