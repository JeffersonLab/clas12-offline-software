package org.jlab.service.swaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

/**
 * 
 * Cache manager for cable swaps across multiple run numbers and CCDB tables,
 * interpreted from two ConstantsManager instances, implemented to enable
 * transforming a decoded HIPO file's sector/layer/component/order from the
 * translation tables used at decoding time into a different one.
 * 
 * @author baltzell
 */
public class SwapManager {

    HashMap<Integer,HashMap<String,Swap>> swaps = new HashMap<>();
    List<String> tableNames = new ArrayList<>();
    ConstantsManager prevConman;
    ConstantsManager currConman;

    /**
     * @param tableNames list of CCDB translation table names to be available for swapping
     * @param previous timestamp/variation used for translation tables during decoding
     * @param current timestamp/variation with correct translation tables
     */
    public SwapManager(List<String> tableNames,ConstantsManager previous,ConstantsManager current) {
        this.tableNames.addAll(tableNames);
        this.prevConman = previous;
        this.currConman = current;
    }

    /**
     * Get an unswapped value. 
     * @param adctdc 0/1 = ADC/TDC
     * @param run run number
     * @param tableName CCDB translation table name, e.g. "/daq/tt/ecal"
     * @param varName name of new variable to retrieve (sector/layer/component/order)
     * @param slco array of old variables (sector/layer/component/order)
     * @return new value of the requested variable
     */
    public int get(int adctdc,int run,String tableName,String varName,int... slco) {
        if (!this.swaps.containsKey(run)) {
            this.add(run);
        }
        return this.swaps.get(run).get(tableName).get(adctdc,varName,slco);
    }

    /**
     * Initialize the swaps for a given run number.
     * @param run 
     */
    private void add(int run) {
        this.swaps.put(run,new HashMap<>());
        for (String tableName : tableNames) {
            IndexedTable prev = prevConman.getConstants(run, tableName);
            IndexedTable curr = currConman.getConstants(run, tableName);
            this.swaps.get(run).put(tableName,new Swap(prev,curr));
        }
    }
}
