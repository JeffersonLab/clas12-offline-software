/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.analysis.eventmerger;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author devita
 */
public class EventMergerConstants {
    
    private final Map <String,EventMergerEnum> constants = new HashMap<>();
    private final Map <String, Map <EventMergerEnum,Double>>   globalConstants = new HashMap<>();
    private final Map <String, Map <EventMergerEnum,double[]>> doubleArrayConstants = new HashMap<>();
    private final Map <String, Map <EventMergerEnum,int[]>>    intArrayConstants = new HashMap<>();

    public EventMergerConstants() {
        load();
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
    

    public void load() {
        // DC constants
        setDouble("DC",EventMergerEnum.JITTER_CYCLES, 2.0);
        setDouble("DC",EventMergerEnum.JITTER_PERIOD, 4.0);
        setDouble("DC",EventMergerEnum.JITTER_PHASE,  1.0);
        setDouble("DC",EventMergerEnum.TDC_CONV,      1.0);
        int[] deadTimes =    { 350,  350,  350,  350,  350,  350,
                               350,  350,  350,  350,  350,  350,
                              1100, 1100, 1100, 1100, 1100, 1100,
                              1000, 1000, 1000, 1000, 1000, 1000,
                              1000, 1000, 1000, 1000, 1000, 1000,
                              1000, 1000, 1000, 1000, 1000, 1000};
        setIntArray("DC",EventMergerEnum.DEAD_TIME, deadTimes);
        int[] readoutWidth =    { 500,  500,  500,  500,  500,  500,
                                  500,  500,  500,  500,  500,  500,
                                 1400, 1400, 1400, 1400, 1400, 1400,
                                 1400, 1400, 1400, 1400, 1400, 1400,
                                 1200, 1200, 1200, 1200, 1200, 1200,
                                 1200, 1200, 1200, 1200, 1200, 1200};
        setIntArray("DC",EventMergerEnum.READOUT_WINDOW_WIDTH, readoutWidth);
                
    }
}
