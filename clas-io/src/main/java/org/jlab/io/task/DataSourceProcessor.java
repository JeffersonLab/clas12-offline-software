/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.task;

import java.util.ArrayList;
import java.util.List;
import org.jlab.io.base.DataSource;

/**
 *
 * @author gavalian
 */
public class DataSourceProcessor {
    
    DataSource dataSource = null;
    List<IDataEventListener>  eventListeners = new ArrayList<IDataEventListener>();
    
    public DataSourceProcessor(){
        
    }
    
    public DataSourceProcessor(DataSource ds){
        setSource(ds);
    }
    
    public void addEventListener(IDataEventListener evL){
        eventListeners.add(evL);
    }
    
    public List<IDataEventListener>  getEventListeners(){
        return this.eventListeners;
    }
    
    public final void setSource(DataSource  ds){
        dataSource = ds;
    }
    
    
    
}
