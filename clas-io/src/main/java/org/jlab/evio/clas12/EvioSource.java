package org.jlab.evio.clas12;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.jevio.EvioCompactReader;
import org.jlab.coda.jevio.EvioException;
import org.jlab.data.io.DataEvent;

import org.jlab.data.io.DataSource;
import org.jlab.data.io.DataEventList;

public class EvioSource implements DataSource {
    
    private ByteOrder  storeByteOrder = ByteOrder.BIG_ENDIAN;
    private EvioCompactReader evioReader    = null;
    private EvioDataEvent  evioEvent = null;
    private int        currentEvent;
    private int        currentFileEntries;
    private EvioDataDictionary dictionary = new EvioDataDictionary();
    private String         dictionaryPath = "some";
    
    
    public EvioSource(){
        
        String CLAS12DIR = System.getenv("CLAS12DIR");
        String CLAS12DIRPROP = System.getProperty("CLAS12DIR");
        
        if(CLAS12DIR==null){
            System.out.println("---> Warning the CLAS12DIR environment is not defined.");
            //return;
        } else {
            dictionaryPath = CLAS12DIR + "/etc/bankdefs/clas12";
        }
        
        if(CLAS12DIRPROP==null){
            System.out.println("---> Warning the CLAS12DIR property is not defined.");
        } else {
            dictionaryPath = CLAS12DIRPROP + "/etc/bankdefs/clas12";
        }

        if(CLAS12DIRPROP==null&&CLAS12DIR==null){
            return;
        }
        //dictionary.initWithDir(dictionaryPath);
        //System.err.println("[EvioSource] ---> Loaded bank Descriptors from    : " +
        //        dictionaryPath);
        //System.err.println("[EvioSource] ---> Factory loaded descriptor count : " 
        //+ dictionary.getDescriptorList().length);
        
        EvioFactory.loadDictionary(dictionaryPath);
        dictionary = EvioFactory.getDictionary();
        System.err.println("[EvioSource] ---> Factory loaded descriptor count : " 
                + dictionary.getDescriptorList().length);
        dictionary.show();
    }
    
    public EvioSource(String filename){
                String CLAS12DIR = System.getenv("CLAS12DIR");
        String CLAS12DIRPROP = System.getProperty("CLAS12DIR");
        
        if(CLAS12DIR==null){
            System.out.println("---> Warning the CLAS12DIR environment is not defined.");
            //return;
        } else {
            dictionaryPath = CLAS12DIR + "/etc/bankdefs/clas12";
        }
        
        if(CLAS12DIRPROP==null){
            System.out.println("---> Warning the CLAS12DIR property is not defined.");
        } else {
            dictionaryPath = CLAS12DIRPROP + "/etc/bankdefs/clas12";
        }

        if(CLAS12DIRPROP==null&&CLAS12DIR==null){
            return;
        }
        //dictionary.initWithDir(dictionaryPath);
        //System.err.println("[EvioSource] ---> Loaded bank Descriptors from    : " +
        //        dictionaryPath);
        //System.err.println("[EvioSource] ---> Factory loaded descriptor count : " 
        //+ dictionary.getDescriptorList().length);
        
        EvioFactory.loadDictionary(dictionaryPath);
        dictionary = EvioFactory.getDictionary();
        System.err.println("[EvioSource] ---> Factory loaded descriptor count : " 
                + dictionary.getDescriptorList().length);
        dictionary.show();
        this.open(filename);
    }
    
    @Override
    public void open(File file) {
        this.open(file.getAbsolutePath());        
    }

    @Override
    public void open(String filename) {
        try {
            evioReader = new EvioCompactReader(new File(filename));
            currentEvent = 1;
            currentFileEntries = evioReader.getEventCount();
            storeByteOrder     = evioReader.getFileByteOrder();
            System.out.println("****** opened FILE [] ** NEVENTS = " + 
                    currentFileEntries + " *******");
            // TODO Auto-generated method stub
        } catch (EvioException ex) {
            Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    @Override
    public void open(ByteBuffer buff) {
        // TODO Auto-generated method stub
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getSize() {
        // TODO Auto-generated method stub
        return currentFileEntries;
    }

    @Override
    public DataEventList getEventList(int start, int stop) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataEventList getEventList(int nrecords) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void reset() {
        currentEvent = 1;        
    }
    
    @Override
    public int getCurrentIndex() {
        // TODO Auto-generated method stub
        return currentEvent;
    }
    
    @Override
    public DataEvent getPreviousEvent() {
       if(currentEvent>currentFileEntries||currentEvent==2) return null;
        try {
            currentEvent--;
            currentEvent--;
            ByteBuffer evioBuffer = evioReader.getEventBuffer(currentEvent, true);
            EvioDataEvent event = new EvioDataEvent(evioBuffer.array(),storeByteOrder,dictionary);            
            currentEvent++;
            return event;
        } catch (EvioException ex) {
            Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Override
    public DataEvent gotoEvent(int index) {
        if(index<=1||index>currentFileEntries) return null;
        try {
            ByteBuffer evioBuffer = evioReader.getEventBuffer(index, true);
            EvioDataEvent event = new EvioDataEvent(evioBuffer.array(),storeByteOrder,dictionary);            
            currentEvent = index+1;
            return event;
        } catch (EvioException ex) {
            Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public EvioDataEventHandler  getNextEventHandler(){
        if(currentEvent>currentFileEntries) return null;
        try {
            ByteBuffer evioBuffer = evioReader.getEventBuffer(currentEvent, true);
            EvioDataEventHandler event = new EvioDataEventHandler(evioBuffer.array(),storeByteOrder);
            currentEvent++;
            return event;
        } catch (EvioException ex) {
            Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Override
    public DataEvent getNextEvent() {
        if(currentEvent>currentFileEntries) return null;
        try {
            ByteBuffer evioBuffer = evioReader.getEventBuffer(currentEvent, true);
            EvioDataEvent event = new EvioDataEvent(evioBuffer.array(),storeByteOrder,dictionary);
            currentEvent++;
            return event;
        } catch (EvioException ex) {
            Logger.getLogger(EvioSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    /*
    public Histogram1D scanTree(String path, int maxevents){
        currentEvent = 1;
        int maxestimate = 5000;
        if(maxestimate>=maxevents){
            maxestimate = maxevents-2;
        }
        
        DataRangeEstimator datae = new DataRangeEstimator(path,100,maxestimate);
        
        for(int loop = 0; loop < maxevents; loop++){
            DataEvent event = this.getNextEvent();
            double[] darray = event.getDouble(path);
            if(darray!=null){
                for(int ndata = 0; ndata < darray.length; ndata++){
                    datae.fill(darray[ndata]);
                    //System.out.println("--> " + darray[ndata]);
                }            
            }
        }
        //= new Histogram1D();
        System.out.println("----> bins  = " + datae.getHistogram().getAxis().getNbins());
        return datae.getHistogram();
    }
    */
    @Override
    public boolean hasEvent() {
        if(currentEvent>=currentFileEntries) return false;
        return true;
    }
    
    
    public static void main(String[] args){
        
    }
}
