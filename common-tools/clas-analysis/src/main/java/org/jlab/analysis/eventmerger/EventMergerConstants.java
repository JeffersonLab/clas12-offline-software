/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.analysis.eventmerger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

/**
 * Class organizing CCDB constants for background-merging purposes
 * Includes TDC conversion factor, time jitter constants and DC 
 * readout parameters
 * 
 * @author devita
 */
public class EventMergerConstants {
    
    private ConstantsManager                                 manager   = new ConstantsManager();
    private final Map <String, Map <EventMergerEnum,Double>> constants = new HashMap<>();
    private final Map <String, Map <EventMergerEnum,String>> tables    = new HashMap<>();
    private final Map <EventMergerEnum, EventMergerEnum>     links     = new HashMap<>();
    private final Map <EventMergerEnum, String>              items     = new HashMap<>();

    private static final String[] tableNames={
            "/daq/config/dc",
            "/calibration/dc/time_jitter",
            "/calibration/ftof/time_jitter",
            "/calibration/ec/time_jitter",
            "/calibration/cnd/time_jitter",
            "/calibration/ctof/time_jitter",            
    };

    public EventMergerConstants() {
        
        // fill table map
        // time jitter
        setTable("DC",   EventMergerEnum.TIME_JITTER, "/calibration/dc/time_jitter");
        setTable("FTOF", EventMergerEnum.TIME_JITTER, "/calibration/ftof/time_jitter");
        setTable("ECAL", EventMergerEnum.TIME_JITTER, "/calibration/ec/time_jitter");
        setTable("CND",  EventMergerEnum.TIME_JITTER, "/calibration/cnd/time_jitter");
        setTable("CTOF", EventMergerEnum.TIME_JITTER, "/calibration/ctof/time_jitter");
        // readout parameters
        setTable("DC",EventMergerEnum.READOUT_PAR,"/daq/config/dc");
        
        // fill table items map
        setLinks(EventMergerEnum.JITTER_CYCLES,   EventMergerEnum.TIME_JITTER);
        setLinks(EventMergerEnum.JITTER_PERIOD,   EventMergerEnum.TIME_JITTER);
        setLinks(EventMergerEnum.JITTER_PHASE,    EventMergerEnum.TIME_JITTER);
        setLinks(EventMergerEnum.READOUT_WINDOW,  EventMergerEnum.READOUT_PAR);
        setLinks(EventMergerEnum.READOUT_HOLDOFF, EventMergerEnum.READOUT_PAR);
        setItems(EventMergerEnum.JITTER_CYCLES,   "cycles");
        setItems(EventMergerEnum.JITTER_PERIOD,   "period");
        setItems(EventMergerEnum.JITTER_PHASE,    "phase");
        setItems(EventMergerEnum.READOUT_WINDOW,  "window");
        setItems(EventMergerEnum.READOUT_HOLDOFF, "holdoff");
        
        // define additional constants
        setDouble("DC",  EventMergerEnum.TDC_CONV,  1.0);
        setDouble("FTOF",EventMergerEnum.TDC_CONV,  0.02345);
        setDouble("ECAL",EventMergerEnum.TDC_CONV,  0.02345);
        setDouble("CTOF",EventMergerEnum.TDC_CONV,  0.02345);
        setDouble("CND", EventMergerEnum.TDC_CONV,  0.02345);
        
        // initialize manager
        manager.setVariation("default");
        manager.init(Arrays.asList(tableNames));
    }
    
    private void setTable(String detector, EventMergerEnum key, String path) {
        if (!tables.containsKey(detector)) tables.put(detector,new HashMap<>());
        tables.get(detector).put(key,path);
    }
   
    private void setLinks(EventMergerEnum item, EventMergerEnum value) {
        if (!links.containsKey(item)) links.put(item,value);
    }
   
    private void setItems(EventMergerEnum item, String value) {
        if (!items.containsKey(item)) items.put(item,value);
    }
   
    private void setDouble(String detector, EventMergerEnum key, Double value) {
        if(!constants.containsKey(detector)) constants.put(detector, new HashMap<>());
        constants.get(detector).put(key,value);
    }
    
    /**
     * Get double value for selected detector and constant (used for run-independent constants)
     * 
     * @param detector: detector identifier string
     * @param key:      constant
     * @return
     */
    public double getDouble(String detector, EventMergerEnum key) {
        if (!constants.containsKey(detector))
            throw new RuntimeException("Missing Integer Key:  "+detector);
        if (!constants.get(detector).containsKey(key))
            throw new RuntimeException("Missing Integer Key:  "+key);
        return constants.get(detector).get(key);
    }
    
    /**
     * Get double value for selected run number, detector and constant
     * 
     * @param run:      run number
     * @param detector: detector identifier string
     * @param item:     constant
     * @return
     */
    public double getDouble(int run, String detector, EventMergerEnum item) {
        IndexedTable table = getTable(run, detector, links.get(item));
        return table.getDoubleValue(items.get(item), 0, 0, 0);
    }
    
    /**
     * Get integer value for selected run number, detector and constant
     * 
     * @param run:      run number
     * @param detector: detector identifier string
     * @param item:     constant
     * @return
     */
    public int getInt(int run, String detector, EventMergerEnum item) {
        IndexedTable table = getTable(run, detector, links.get(item));
        return table.getIntValue(items.get(item), 0, 0, 0);
    }
    
    /**
     * Get integer value for selected run number, detector  component and constant
     *
     * @param run:      run number
     * @param detector: detector identifier string
     * @param item:     constant
     * @param sector
     * @param layer
     * @param component
     * @return
     */
    public int getInt(int run, String detector, EventMergerEnum item, int sector, int layer, int component) {
        IndexedTable table = getTable(run, detector, links.get(item));
        return table.getIntValue(items.get(item), sector, layer, component);
    }
    
    private String getTable(String detector, EventMergerEnum key) {
        if (!tables.containsKey(detector))
            throw new RuntimeException("Missing  Key:  "+detector);
        if (!tables.get(detector).containsKey(key))
            throw new RuntimeException("Missing  Key:  "+key);
        return tables.get(detector).get(key);
    }
    
    private IndexedTable getTable(int run, String detector, EventMergerEnum key) {
        String path = getTable(detector, key);
        return manager.getConstants(run, path);
    }
    
}
