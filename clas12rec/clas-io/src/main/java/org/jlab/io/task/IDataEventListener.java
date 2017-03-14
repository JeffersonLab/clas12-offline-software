/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.task;

import org.jlab.io.base.DataEvent;

/**
 *
 * @author gavalian
 */
public interface IDataEventListener {
    void  dataEventAction(DataEvent event);
    void  timerUpdate();
    void  resetEventListener();
}
