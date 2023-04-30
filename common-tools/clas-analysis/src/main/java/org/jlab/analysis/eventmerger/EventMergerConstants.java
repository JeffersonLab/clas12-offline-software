package org.jlab.analysis.eventmerger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.base.DetectorType;
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
    
    private final ConstantsManager                                 manager   = new ConstantsManager();
    private final Map <DetectorType, Map <EventMergerEnum,Double>> constants = new HashMap<>();
    private final Map <DetectorType, Map <EventMergerEnum,String>> tables    = new HashMap<>();
    private final Map <EventMergerEnum, EventMergerEnum>           links     = new HashMap<>();
    private final Map <EventMergerEnum, String>                    items     = new HashMap<>();

    private static final String[] tableNames={
            "/daq/config/dc",
            "/calibration/dc/time_jitter",
            "/calibration/ftof/time_jitter",
            "/calibration/ec/time_jitter",
            "/calibration/cnd/time_jitter",
            "/calibration/ctof/time_jitter",            
            "/calibration/band/time_jitter"            
    };

    public static final List<DetectorType> ADCs = Arrays.asList(DetectorType.BAND,
                                                                DetectorType.BMT,
                                                                DetectorType.BST,
                                                                DetectorType.CND,
                                                                DetectorType.CTOF,
                                                                DetectorType.ECAL,
                                                                DetectorType.FMT,
                                                                DetectorType.FTCAL,
                                                                DetectorType.FTHODO,
                                                                DetectorType.FTOF,
                                                                DetectorType.FTTRK,
                                                                DetectorType.HTCC,
                                                                DetectorType.LTCC,
                                                                DetectorType.URWELL);
                                                
    public static final List<DetectorType> TDCs = Arrays.asList(DetectorType.BAND,
                                                                DetectorType.CND,
                                                                DetectorType.CTOF,
                                                                DetectorType.DC,
                                                                DetectorType.ECAL,
                                                                DetectorType.FTOF);
                                                                                                
    
    public EventMergerConstants() {
        
        // fill table map
        // time jitter
        setTable(DetectorType.DC,   EventMergerEnum.TIME_JITTER, "/calibration/dc/time_jitter");
        setTable(DetectorType.FTOF, EventMergerEnum.TIME_JITTER, "/calibration/ftof/time_jitter");
        setTable(DetectorType.ECAL, EventMergerEnum.TIME_JITTER, "/calibration/ec/time_jitter");
        setTable(DetectorType.CND,  EventMergerEnum.TIME_JITTER, "/calibration/cnd/time_jitter");
        setTable(DetectorType.CTOF, EventMergerEnum.TIME_JITTER, "/calibration/ctof/time_jitter");
        setTable(DetectorType.BAND, EventMergerEnum.TIME_JITTER, "/calibration/band/time_jitter");
        // readout parameters
        setTable(DetectorType.DC,EventMergerEnum.READOUT_PAR,"/daq/config/dc");
        
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
        setDouble(DetectorType.DC,  EventMergerEnum.TDC_CONV,  1.0);
        setDouble(DetectorType.FTOF,EventMergerEnum.TDC_CONV,  0.02345);
        setDouble(DetectorType.ECAL,EventMergerEnum.TDC_CONV,  0.02345);
        setDouble(DetectorType.CTOF,EventMergerEnum.TDC_CONV,  0.02345);
        setDouble(DetectorType.CND, EventMergerEnum.TDC_CONV,  0.02345);
        setDouble(DetectorType.BAND,EventMergerEnum.TDC_CONV,  0.02345);
        setInt(DetectorType.DC,   EventMergerEnum.MERGE_SIZE, 2);
        setInt(DetectorType.FTOF, EventMergerEnum.MERGE_SIZE, 1);
        setInt(DetectorType.ECAL, EventMergerEnum.MERGE_SIZE, 1);
        setInt(DetectorType.CTOF, EventMergerEnum.MERGE_SIZE, 1);
        setInt(DetectorType.CND,  EventMergerEnum.MERGE_SIZE, 1);
        setInt(DetectorType.BAND, EventMergerEnum.MERGE_SIZE, 1);
        
        // initialize manager
        manager.setVariation("default");
        manager.init(Arrays.asList(tableNames));
    }
    
    private void setTable(DetectorType detector, EventMergerEnum key, String path) {
        if (!tables.containsKey(detector)) tables.put(detector,new HashMap<>());
        tables.get(detector).put(key,path);
    }
   
    private void setLinks(EventMergerEnum item, EventMergerEnum value) {
        if (!links.containsKey(item)) links.put(item,value);
    }
   
    private void setItems(EventMergerEnum item, String value) {
        if (!items.containsKey(item)) items.put(item,value);
    }
   
    private void setDouble(DetectorType detector, EventMergerEnum key, double value) {
        if(!constants.containsKey(detector)) constants.put(detector, new HashMap<>());
        constants.get(detector).put(key,value);
    }

    private void setInt(DetectorType detector, EventMergerEnum key, int value) {
        if(!constants.containsKey(detector)) constants.put(detector, new HashMap<>());
        constants.get(detector).put(key,(double)value);
    }
    /**
     * Get double value for selected detector and constant (used for run-independent constants)
     * 
     * @param detector: detector identifier 
     * @param key:      constant
     * @return
     */
    public double getDouble(DetectorType detector, EventMergerEnum key) {
        if (!constants.containsKey(detector))
            throw new RuntimeException("Missing Integer Key:  "+detector);
        if (!constants.get(detector).containsKey(key))
            throw new RuntimeException("Missing Integer Key:  "+key);
        return constants.get(detector).get(key);
    }
    
    /**
     * Get double value for selected detector and constant (used for run-independent constants)
     * 
     * @param detector: detector identifier 
     * @param key:      constant
     * @return
     */
    public int getInt(DetectorType detector, EventMergerEnum key) {
        return (int) this.getDouble(detector, key);
    }
    
    /**
     * Get double value for selected run number, detector and constant
     * 
     * @param run:      run number
     * @param detector: detector identifier 
     * @param item:     constant
     * @return
     */
    public double getDouble(int run, DetectorType detector, EventMergerEnum item) {
        IndexedTable table = getTable(run, detector, links.get(item));
        if(table==null)
            return 0;
        else
            return table.getDoubleValue(items.get(item), 0, 0, 0);
    }
    
    /**
     * Get integer value for selected run number, detector and constant
     * 
     * @param run:      run number
     * @param detector: detector identifier 
     * @param item:     constant
     * @return
     */
    public int getInt(int run, DetectorType detector, EventMergerEnum item) {
        IndexedTable table = getTable(run, detector, links.get(item));
        if(table==null)
            return 0;
        else
            return table.getIntValue(items.get(item), 0, 0, 0);
    }
    
    /**
     * Get integer value for selected run number, detector  component and constant
     *
     * @param run:      run number
     * @param detector: detector identifier 
     * @param item:     constant
     * @param sector
     * @param layer
     * @param component
     * @return
     */
    public int getInt(int run, DetectorType detector, EventMergerEnum item, int sector, int layer, int component) {
        IndexedTable table = getTable(run, detector, links.get(item));
        if(table==null)
            return 0;
        else
            return table.getIntValue(items.get(item), sector, layer, component);
    }
    
    private String getTable(DetectorType detector, EventMergerEnum key) {
        if (!tables.containsKey(detector))
//            throw new RuntimeException("Missing  Key:  "+detector);
            return null;
        if (!tables.get(detector).containsKey(key))
//            throw new RuntimeException("Missing  Key:  "+key);
            return null;
        return tables.get(detector).get(key);
    }
    
    private IndexedTable getTable(int run, DetectorType detector, EventMergerEnum key) {
        String path = getTable(detector, key);
        if(path==null)
            return null;
        else
            return manager.getConstants(run, path);
    }
    
}
