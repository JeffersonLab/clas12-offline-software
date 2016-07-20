/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.task;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.base.DataSource;
import org.jlab.io.evio.EvioFactory;

/**
 *
 * @author gavalian
 */
public class DataSourceProcessor {
    
    DataSource dataSource = null;
    List<IDataEventListener>  eventListeners = new ArrayList<IDataEventListener>();
    private int  eventProcessorDelay = 0;
    private int  eventsProcessed     = 0;
    private int  listenerUpdateRate  = 20000;
    private long timeSpendOnReading     = 0L;
    private long timeSpendOnProcessing  = 0L;

    
    public DataSourceProcessor(){
        
    }
    
    public DataSourceProcessor(DataSource ds){
        setSource(ds);
    }
    
    public int getDelay(){
        return this.eventProcessorDelay;
    }
    
    public int getUpdateRate(){
        return this.listenerUpdateRate;
    }
    
    public void setUpdateRate(int nevents){
        this.listenerUpdateRate = nevents;
    }
    
    public void setDelay(int dvalue){
        this.eventProcessorDelay = dvalue;
    }
    
    public void addEventListener(IDataEventListener evL){
        eventListeners.add(evL);
    }
    
    public String getStatusString(){
        double evRate = this.eventsProcessed/(this.timeSpendOnReading/1000.0);
        double prRate = this.eventsProcessed/(this.timeSpendOnProcessing/1000.0);
        if(this.eventListeners.isEmpty()) prRate = 0.0;
        if(this.timeSpendOnProcessing==0L) prRate = 0.0;
        if(this.timeSpendOnReading==0L) evRate = 0.0;
        
        return String.format("    Events %d  : Reading %.2f  evt/sec. Processing %.2f evt/sec",
                this.eventsProcessed,
                evRate,prRate);
    }
    
    public int  getProgress(){
        return this.eventsProcessed;
    }
    
    public List<IDataEventListener>  getEventListeners(){
        return this.eventListeners;
    }
    
    public final void setSource(DataSource  ds){
        dataSource = ds;
        System.out.println("[EventProcessor] --> added a data source...");
        this.eventsProcessed    = 0;
        this.timeSpendOnReading = 0L;
        this.timeSpendOnProcessing = 0L;
    }
           
    
    public void processSource(int delay){
        eventsProcessed = 0;
        while(processNextEvent()==true){
            //eventsProcessed++;
        }
    }
    
    public boolean processNextEvent(){
        return processNextEvent(0,DataEventType.EVENT_SINGLE);
    }
    
    public boolean processNextEvent(int delay, DataEventType type){
        
        if(type==DataEventType.EVENT_STOP){
            DataEvent event = EvioFactory.createEvioEvent();
            event.setType(type);
            for(IDataEventListener processor : eventListeners){
                processor.dataEventAction(event);
            }
            return true;
        }
        
        if(dataSource==null) {
            //System.out.println("[DataSourceProcessor] error ---> data source is not set");
            return false;
        }
        if(dataSource.hasEvent()==false){
            //System.out.println("[DataSourceProcessor] error ---> data source has no events");
            return false;
        }
               
        
        Long st = System.currentTimeMillis();
        DataEvent event = dataSource.getNextEvent();        
        event.setType(type);
        
        if(this.eventsProcessed==0){
            event.setType(DataEventType.EVENT_START);
        }
        
        this.eventsProcessed++;
        Long et = System.currentTimeMillis();
        this.timeSpendOnReading += (et-st);
        //System.out.println(" processing next event ---> " + this.eventsProcessed);
        if(event!=null){
            st = System.currentTimeMillis();
            for(IDataEventListener processor : eventListeners){
                try {
                    processor.dataEventAction(event);
                    if(eventsProcessed%this.listenerUpdateRate==0){
                        processor.timerUpdate();
                    }
                } catch (Exception e){
                    
                }
            }
            et = System.currentTimeMillis();
            this.timeSpendOnProcessing += (et-st);
            if(delay>0){
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DataSourceProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }                
        }
        
        return true;
    }
    
    
}
