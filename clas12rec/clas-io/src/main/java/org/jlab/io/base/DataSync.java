/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.base;

/**
 *
 * @author gavalian
 */
public interface DataSync {
    
    void open(String file);
    void writeEvent(DataEvent event);
    void close();
    
    DataEvent createEvent();
}
