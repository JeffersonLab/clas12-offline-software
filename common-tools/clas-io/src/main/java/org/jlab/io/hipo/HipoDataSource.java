package org.jlab.io.hipo;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventList;
import org.jlab.io.base.DataEventType;
import org.jlab.io.base.DataSource;
import org.jlab.io.evio.EvioDataDictionary;
import org.jlab.io.base.DataSourceType;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.jnp.hipo4.io.HipoReader;


/**
 *
 * @author gavalian
 */
public class HipoDataSource implements DataSource {

    public static Logger LOGGER = Logger.getLogger(HipoDataSource.class.getName());

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
    /**
     * Creates a Writer class with Dictionary from the Reader.
     * This method should be used when filtering the input file
     * to ensure consistency of dictionaries and banks in the output.
     * @return HipoDataSync object for writing an output.
     */
    public HipoDataSync createWriter(){
        SchemaFactory factory = reader.getSchemaFactory();
        HipoDataSync   writer = new HipoDataSync(factory);
        return writer;
    }
    
    @Override
    public void open(String filename) {
        this.reader.open(filename);
        LOGGER.log(Level.INFO,"[DataSourceDump] --> opened file with events # " );
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

    public  HipoReader getReader(){ return reader;}
    @Override
    public int getSize() {
        //return reader.
        //return 1;
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
        Event event = new Event();
        reader.nextEvent(event);
        /*hipoEvent.getDataBuffer();        
        System.out.println(" GET NEXT HIPO EVENT : DICTIONARY SIZE = " + 
                hipoEvent.getSchemaFactory().getSchemaList().size() + "  EVENT LENGTH = "
                + hipoEvent.getDataBuffer().length);
        hipoEvent.showNodes();*/
        HipoDataEvent  hipoEvent = new HipoDataEvent(event,reader.getSchemaFactory());
        if(reader.hasNext()==true){
            hipoEvent.setType(DataEventType.EVENT_ACCUMULATE);
        } else {
            hipoEvent.setType(DataEventType.EVENT_STOP);
        }
        return hipoEvent;
    }

    @Override
    public DataEvent getPreviousEvent() {
       
        /*HipoEvent  hipoEvent = reader.readPreviousEvent();
        hipoEvent.getDataBuffer();
        HipoDataEvent  evioEvent = new HipoDataEvent(hipoEvent.getDataBuffer(),hipoEvent.getSchemaFactory());*/
        //return hipoEvent;
        return null;
    }

    @Override
    public DataEvent gotoEvent(int index) {
        Event event = new Event();
        reader.getEvent(event, index);
        /*hipoEvent.getDataBuffer();        
        System.out.println(" GET NEXT HIPO EVENT : DICTIONARY SIZE = " + 
                hipoEvent.getSchemaFactory().getSchemaList().size() + "  EVENT LENGTH = "
                + hipoEvent.getDataBuffer().length);
        hipoEvent.showNodes();*/
        HipoDataEvent  hipoEvent = new HipoDataEvent(event,reader.getSchemaFactory());
        if(reader.hasNext()==true){
            hipoEvent.setType(DataEventType.EVENT_ACCUMULATE);
        } else {
            hipoEvent.setType(DataEventType.EVENT_STOP);
        }
        return hipoEvent;       
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
