/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.data.tasks;

import org.jlab.data.io.DataEvent;

/**
 *
 * @author gavalian
 */
public interface IDataEventProcessor {    
    public void processEvent(DataEvent event, IDataEventType type);    
}
