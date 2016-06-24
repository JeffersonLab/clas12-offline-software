/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.data.io.hipo;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.jlab.data.io.DataEvent;
import org.jlab.data.io.DataEventList;
import org.jlab.data.io.DataSource;
import org.jlab.evio.clas12.EvioDataDescriptor;
import org.jlab.evio.clas12.EvioDataDictionary;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.hipo.io.HipoReader;
import org.jlab.hipo.io.HipoRecord;

/**
 *
 * @author gavalian
 */
public class HipoDataSource implements DataSource {

    HipoReader  reader = null;
    EvioDataDictionary  dictionary = new EvioDataDictionary();
    int                 numberOfRecords = 0;
    int                 currentEventNumber = 0;
    int                 minEventNumber     = 0;
    int                 numberOfEvent      = 0;
    
    public HipoDataSource(){
        this.reader = new HipoReader();
    }
    
    public boolean hasEvent() {
        return (this.currentEventNumber<this.numberOfEvent);
    }

    public void open(File file) {
        
    }

    public void open(String filename) {
        this.reader.open(filename);
        HipoRecord header = this.reader.readRecord(0);
        int  ncount = header.getEventCount();
        for(int ev = 0; ev < ncount; ev++){
            byte[] descBytes  = this.reader.readEvent(ev);
            String descString = new String(descBytes);
            EvioDataDescriptor  descriptor = new EvioDataDescriptor(descString);
            this.dictionary.addDescriptor(descriptor);
        }
        this.dictionary.show();
        this.minEventNumber = ncount;
        this.currentEventNumber = ncount;
        this.numberOfEvent      = this.reader.getEventCount();
    }

    public void open(ByteBuffer buff) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void close() {
        
    }

    public int getSize() {
        return this.numberOfEvent - this.minEventNumber;
    }

    public DataEventList getEventList(int start, int stop) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public DataEventList getEventList(int nrecords) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public DataEvent getNextEvent() {
        byte[] array             = this.reader.readEvent(this.currentEventNumber);
        this.currentEventNumber++;
        EvioDataEvent  evioEvent = new EvioDataEvent(array,ByteOrder.LITTLE_ENDIAN,this.dictionary);        
        return evioEvent;
    }

    public DataEvent getPreviousEvent() {
        if(this.currentEventNumber>this.minEventNumber){
            this.currentEventNumber--;           
        }
        byte[] array             = this.reader.readEvent(this.currentEventNumber);
        EvioDataEvent  evioEvent = new EvioDataEvent(array,ByteOrder.LITTLE_ENDIAN,this.dictionary);        
        return evioEvent;
    }

    public DataEvent gotoEvent(int index) {
        if(index>=this.minEventNumber&&index<this.numberOfEvent){
            this.currentEventNumber = index;
            byte[] array             = this.reader.readEvent(this.currentEventNumber);
        EvioDataEvent  evioEvent = new EvioDataEvent(array,ByteOrder.LITTLE_ENDIAN,this.dictionary);        
        return evioEvent;
        }
        return null;
    }

    public void reset() {
        
    }

    public int getCurrentIndex() {
        return this.currentEventNumber;
    }
    
}
