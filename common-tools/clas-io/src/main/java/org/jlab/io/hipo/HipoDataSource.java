/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import java.io.File;
import java.nio.ByteBuffer;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventList;
import org.jlab.io.base.DataSource;
import org.jlab.io.evio.EvioDataDictionary;
import org.jlab.io.base.DataSourceType;
import org.jlab.jnp.hipo.data.HipoEvent;
import org.jlab.jnp.hipo.io.HipoReader;

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
    
    @Override
    public boolean hasEvent() {
        return reader.hasNext();
    }

    @Override
    public void open(File file) {
        this.open(file.getAbsolutePath());
    }

    @Override
    public void open(String filename) {
        this.reader.open(filename);
        System.out.println("[DataSourceDump] --> opened file with events # " + this.reader.getEventCount());
        //this.reader.getSchemaFactory().show();
        /*
        HipoRecord header = this.reader.getHeaderRecord();
        int  ncount = header.getEventCount();
        System.out.println("[HipoDataSource] ---> dictionary record opened. # entries = " + ncount);
        for(int ev = 0; ev < ncount; ev++){
            byte[] descBytes  = header.getEvent(ev);
            String descString = new String(descBytes);
            //System.out.println("init dictionary : " + descString);
            EvioDataDescriptor  descriptor = new EvioDataDescriptor(descString);
            this.dictionary.addDescriptor(descriptor);
        }*/
        //this.dictionary.show();
        /*this.minEventNumber = 0;
        this.currentEventNumber = 0;
        this.numberOfEvent      = this.reader.getEventCount();*/
    }

    public void open(ByteBuffer buff) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void close() {
        
    }

    @Override
    public int getSize() {
        return reader.getEventCount();
    }

    @Override
    public DataEventList getEventList(int start, int stop) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataEventList getEventList(int nrecords) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataEvent getNextEvent() {
        HipoEvent  hipoEvent = reader.readNextEvent();
        /*hipoEvent.getDataBuffer();        
        System.out.println(" GET NEXT HIPO EVENT : DICTIONARY SIZE = " + 
                hipoEvent.getSchemaFactory().getSchemaList().size() + "  EVENT LENGTH = "
                + hipoEvent.getDataBuffer().length);
        hipoEvent.showNodes();*/
        HipoDataEvent  evioEvent = new HipoDataEvent(hipoEvent.getDataBuffer(),hipoEvent.getSchemaFactory());
        return evioEvent;
    }

    @Override
    public DataEvent getPreviousEvent() {
        HipoEvent  hipoEvent = reader.readPreviousEvent();
        hipoEvent.getDataBuffer();
        HipoDataEvent  evioEvent = new HipoDataEvent(hipoEvent.getDataBuffer(),hipoEvent.getSchemaFactory());
        return evioEvent;
        
    }

    @Override
    public DataEvent gotoEvent(int index) {
        HipoEvent  hipoEvent = reader.readEvent(index);
        hipoEvent.getDataBuffer();
        HipoDataEvent  evioEvent = new HipoDataEvent(hipoEvent.getDataBuffer(),hipoEvent.getSchemaFactory());
        return evioEvent;
    }
    
    @Override
    public void reset() {
        this.currentEventNumber = 0;
    }

    @Override
    public int getCurrentIndex() {
        return this.currentEventNumber;
    }
        
    public static void main(String[] args){
        HipoDataSource reader = new HipoDataSource();
        reader.open("test_hipoio.hipo");
        int counter = 0;
        while(reader.hasEvent()==true){
            DataEvent  event = reader.getNextEvent();
            System.out.println("EVENT # " + counter);
            event.show();
            counter++;
        }
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.FILE;        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void waitForEvents() {
        
    }
}
