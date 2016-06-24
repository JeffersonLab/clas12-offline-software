/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.data.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import org.jlab.data.io.DataEvent;
import org.jlab.data.io.DataSource;
import org.jlab.evio.clas12.EvioSource;

/**
 *
 * @author gavalian
 */
public class DataSourceTask extends Task<Integer> {
    
    private DataSource  reader =  null;
    private List<IDataEventProcessor>  processorList = new ArrayList<IDataEventProcessor>();
    private List<IDataSourceTaskListener> processListeners = new ArrayList<IDataSourceTaskListener>();
    
    
    private Integer   processedEventsCount    = 0;
    private Integer   processListenerInterval = 100;
    private Integer   processEventDelay       = 0;
    
    public DataSourceTask(DataSource src){
        this.reader = src;
    }

    public void addEventProcessor(IDataEventProcessor proc){
        this.processorList.add(proc);
    }
    
    public void setListenerInterval(int interval){
        this.processListenerInterval = interval;
    }
    
    public void setEventDelay(int delay){
        this.processEventDelay = delay;
    }
    
    public void addListener(IDataSourceTaskListener lst){
        this.processListeners.add(lst);
    }
    
    @Override
    protected Integer call() throws Exception {
        if(this.reader!=null){
            int  nevents = reader.getSize();
            this.processedEventsCount = 0;
            while(reader.hasEvent()==true){
                DataEvent  event = reader.getNextEvent();
                this.processedEventsCount++;
                for(IDataEventProcessor proc : this.processorList){
                    try {
                        proc.processEvent(event, IDataEventType.EVENT_ACCUMULATE);
                    } catch (Exception e){
                        System.out.println("[DataSourceTask] --->  error while execuing processor ");
                    }
                }
                
                int progress = (int)  (((double) 100.0*this.processedEventsCount)/nevents);
                this.updateProgress(progress, nevents);
            }
            
        }
        return this.processedEventsCount;
    }
    
    
    public static void main(String[] args){
        EvioSource dataSource = new EvioSource();
        dataSource.open("/Users/gavalian/Work/Software/Release-8.0/COATJAVA/coatjava/../DVCS_TorusOnly.evio");
        IDataEventProcessor  processor = new IDataEventProcessor(){

            public void processEvent(DataEvent event, IDataEventType type) {
                System.out.println("i'm here");
            }
            
        };
        
        DataSourceTask  task = new DataSourceTask(dataSource);
        try {
            task.call();
        } catch (Exception ex) {
            Logger.getLogger(DataSourceTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
