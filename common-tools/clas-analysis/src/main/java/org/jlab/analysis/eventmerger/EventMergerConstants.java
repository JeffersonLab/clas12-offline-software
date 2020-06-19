/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.analysis.eventmerger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 */
public class EventMergerConstants {
    
    private final Map <String,EventMergerEnum> constants = new HashMap<>();
    private final Map <String, Map <EventMergerEnum,Double>>       globalConstants = new HashMap<>();
    private final Map <String, Map <EventMergerEnum,double[]>>     doubleArrayConstants = new HashMap<>();
    private final Map <String, Map <EventMergerEnum,int[]>>        intArrayConstants = new HashMap<>();
    private final Map <String, Map <EventMergerEnum,IndexedTable>> indexedTableConstants = new HashMap<>();

    private static final String[] tableNames={
            "/daq/config/dc",
            "/calibration/dc/time_jitter",
            "/calibration/ftof/time_jitter",
            "/calibration/ec/time_jitter",
            "/calibration/cnd/time_jitter",
            "/calibration/ctof/time_jitter",            
    };

    public EventMergerConstants() {

    }

    public static List<String> getTableNames() {
        return Arrays.asList(tableNames);
    }    
    
    private void setDouble(String detector, EventMergerEnum key, Double value) {
        if(!globalConstants.containsKey(detector)) globalConstants.put(detector, new HashMap<>());
        globalConstants.get(detector).put(key,value);
    }
    
    private void setDoubleArray(String detector, EventMergerEnum key, double[] value) {
        if (!doubleArrayConstants.containsKey(detector)) doubleArrayConstants.put(detector,new HashMap<>());
        doubleArrayConstants.get(detector).put(key,value);
    }
   
    private void setIntArray(String detector, EventMergerEnum key, int[] value) {
        if (!intArrayConstants.containsKey(detector)) intArrayConstants.put(detector,new HashMap<>());
        intArrayConstants.get(detector).put(key,value);
    }
   
    private void setTable(String detector, EventMergerEnum key, IndexedTable table) {
        if (!indexedTableConstants.containsKey(detector)) indexedTableConstants.put(detector,new HashMap<>());
        indexedTableConstants.get(detector).put(key,table);
    }
   
    public double getDouble(String detector, EventMergerEnum key) {
        if (!globalConstants.containsKey(detector))
            throw new RuntimeException("Missing Integer Key:  "+detector);
        if (!globalConstants.get(detector).containsKey(key))
            throw new RuntimeException("Missing Integer Key:  "+key);
        return globalConstants.get(detector).get(key);
    }
    
    public double getDouble(String detector, EventMergerEnum key, int layer) {
        if (!doubleArrayConstants.containsKey(detector))
            throw new RuntimeException("Missing Integer Key:  "+detector);
        if (!doubleArrayConstants.get(detector).containsKey(key))
            throw new RuntimeException("Missing Integer Key:  "+key);
        double[] array = doubleArrayConstants.get(detector).get(key);
        if (layer<=0 || layer>array.length)
            throw new RuntimeException("Missing Integer Layer:  "+layer);
        return array[layer-1];
    }
    
    public int getInt(String detector, EventMergerEnum key, String item, int sector, int layer, int component) {
        if (!indexedTableConstants.containsKey(detector))
            throw new RuntimeException("Missing Integer Key:  "+detector);
        if (!indexedTableConstants.get(detector).containsKey(key))
            throw new RuntimeException("Missing Integer Key:  "+key);
        IndexedTable table = indexedTableConstants.get(detector).get(key);
        if (!table.hasEntry(sector,layer,component))
            throw new RuntimeException("Missing entry Sector/Layer/Component:  "+sector+"/"+layer+"/"+component);
        return table.getIntValue(item, sector, layer, component);
    }
    
    public double getDouble(String detector, EventMergerEnum key, String item, int sector, int layer, int component) {
        if (!indexedTableConstants.containsKey(detector))
            throw new RuntimeException("Missing Integer Key:  "+detector);
        if (!indexedTableConstants.get(detector).containsKey(key))
            throw new RuntimeException("Missing Integer Key:  "+key);
        IndexedTable table = indexedTableConstants.get(detector).get(key);
        if (!table.hasEntry(sector,layer,component))
            throw new RuntimeException("Missing entry Sector/Layer/Component:  "+sector+"/"+layer+"/"+component);
        return table.getDoubleValue(item, sector, layer, component);
    }

    public int getInt(String detector, EventMergerEnum key, int layer) {
        if (!intArrayConstants.containsKey(detector))
            throw new RuntimeException("Missing Integer Key:  "+detector);
        if (!intArrayConstants.get(detector).containsKey(key))
            throw new RuntimeException("Missing Integer Key:  "+key);
        int[] array = intArrayConstants.get(detector).get(key);
        if (layer<=0 || layer>array.length)
            throw new RuntimeException("Missing Integer Layer:  "+layer);
        return array[layer-1];
    }
    
    public double[] getDoubleArray(String detector, EventMergerEnum key) {
        if (!doubleArrayConstants.containsKey(detector))
            throw new RuntimeException("Missing Integer Key:  "+detector);
        if (!doubleArrayConstants.get(detector).containsKey(key))
            throw new RuntimeException("Missing Integer Key:  "+key);
        return doubleArrayConstants.get(detector).get(key);
    }
    
    public int[] getIntArray(String detector, EventMergerEnum key) {
        if (!intArrayConstants.containsKey(detector))
            throw new RuntimeException("Missing Integer Key:  "+detector);
        if (!intArrayConstants.get(detector).containsKey(key))
            throw new RuntimeException("Missing Integer Key:  "+key);
        return intArrayConstants.get(detector).get(key);
    }
    
    public IndexedTable getTable(String detector, EventMergerEnum key) {
        if (!indexedTableConstants.containsKey(detector))
            throw new RuntimeException("Missing Integer Key:  "+detector);
        if (!indexedTableConstants.get(detector).containsKey(key))
            throw new RuntimeException("Missing Integer Key:  "+key);
        return indexedTableConstants.get(detector).get(key);
    }
    
    public void load(int run, ConstantsManager manager) {
        // DC constants
        setDouble("DC",EventMergerEnum.TDC_CONV,  1.0);
        setTable("DC",EventMergerEnum.READOUT_PAR,manager.getConstants(run, "/daq/config/dc"));
        setTable("DC",EventMergerEnum.TIME_JITTER,manager.getConstants(run, "/calibration/dc/time_jitter"));
        // FTOF constants
        setDouble("FTOF",EventMergerEnum.TDC_CONV,  0.02345);
        setTable("FTOF",EventMergerEnum.TIME_JITTER,manager.getConstants(run, "/calibration/ftof/time_jitter"));
        // EC constants
        setDouble("ECAL",EventMergerEnum.TDC_CONV,  0.02345);
        setTable("ECAL",EventMergerEnum.TIME_JITTER,manager.getConstants(run, "/calibration/ec/time_jitter"));
        // CTOF constants
        setDouble("CTOF",EventMergerEnum.TDC_CONV,  0.02345);
        setTable("CTOF",EventMergerEnum.TIME_JITTER,manager.getConstants(run, "/calibration/ctof/time_jitter"));
        // CND constants
        setDouble("CND",EventMergerEnum.TDC_CONV,  0.02345);
        setTable("CND",EventMergerEnum.TIME_JITTER,manager.getConstants(run, "/calibration/cnd/time_jitter"));
    }
}
