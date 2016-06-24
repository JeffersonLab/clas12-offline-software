/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.task;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioSource;

/**
 *
 * @author gavalian
 */
public class DataSourceService extends Service<Integer> {

    @Override
    protected Task<Integer> createTask() {
        EvioSource dataSource = new EvioSource();
        dataSource.open("/Users/gavalian/Work/Software/Release-8.0/COATJAVA/coatjava/../DVCS_TorusOnly.evio");
        IDataEventProcessor  processor = new IDataEventProcessor(){

            public void processEvent(DataEvent event, IDataEventType type) {
                System.out.println("i'm here");
            }            
        };        
        DataSourceTask  task = new DataSourceTask(dataSource);        
        task.addEventProcessor(processor);
        return task;
    }
    
    public static void main(String[] args){
        DataSourceService service = new DataSourceService();
        service.start();
    }
}
