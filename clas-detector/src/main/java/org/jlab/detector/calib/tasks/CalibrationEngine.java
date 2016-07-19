/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.calib.tasks;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.task.IDataEventListener;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author gavalian
 */
public class CalibrationEngine implements IDataEventListener {
    
    private int       runNumber = 10;
    private String runVariation = "default";
    
    public CalibrationEngine(){
        
    }

    @Override
    public void dataEventAction(DataEvent event) {
        
    }

    @Override
    public void timerUpdate() {
        
    }

    @Override
    public void resetEventListener() {
        
    }
    
    public IndexedList<DataGroup>  getDataGroup(){
        return null;
    }
    
    public List<CalibrationConstants>  getCalibrationConstants(){
        return new ArrayList<CalibrationConstants>();
    }
}
