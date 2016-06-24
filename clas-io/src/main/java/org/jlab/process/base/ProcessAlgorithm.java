/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.process.base;

import org.jlab.data.io.DataEvent;

/**
 *
 * @author gavalian
 */
public interface ProcessAlgorithm {
    void begin(String options);
    void processEvent(DataEvent event);
    void end(String options);
}
