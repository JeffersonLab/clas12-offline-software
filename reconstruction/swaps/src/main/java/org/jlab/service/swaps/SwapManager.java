package org.jlab.service.swaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

/**
 * 
 * Implements a cable swapping interpreted from two ConstantsManager instances,
 * allowing to transform a decoded HIPO file's sector/layer/component from the
 * translation tables used at decoding time into a different one.
 * 
 * @author baltzell
 */
public class SwapManager {

    public static final int ADC=0;
    public static final int TDC=1;

    public static final int ADCL=0;
    public static final int ADCR=1;
    public static final int TDCL=2;
    public static final int TDCR=3;

    public static final String[] VARNAMES = {"sector", "layer", "component", "order"};
    
    List<String> tableNames = new ArrayList<>();

    Map<Integer,HashMap<String,Swap>> swaps = new HashMap<>();
    
    ConstantsManager prevConman;
    ConstantsManager currConman;

    /**
     * @param tableNames list of CCDB translation table names for swapping
     * @param previous timestamp/variation used for translation tables during decoding
     * @param current timestamp/variation with correct translation tables
     */
    public SwapManager(List<String> tableNames,ConstantsManager previous,ConstantsManager current) {
        this.tableNames.addAll(tableNames);
        this.prevConman = previous;
        this.currConman = current;
    }

    private void add(int run) {
        this.swaps.put(run,new HashMap<>());
        for (String tableName : tableNames) {
            IndexedTable prev = prevConman.getConstants(run, tableName);
            IndexedTable curr = currConman.getConstants(run, tableName);
            this.swaps.get(run).put(tableName,new Swap(prev,curr));
        }
    }
   
    /**
     * Get the un-swapped value 
     * @param adctdc 0/1 = ADC/TDC
     * @tableName translation table name, e.g. "/daq/tt/ecal"
     * @varName name of new variable to retrieve (sector/layer/component/order)
     * @slco array of old variables (sector/layer/component/order)
     */
    public int get(int adctdc,int run,String tableName,String varName,int... slco) {
        if (!this.swaps.containsKey(run)) {
            this.add(run);
        }
        return this.swaps.get(run).get(tableName).get(adctdc,varName,slco);
    }

    private class Swap {

        Map<Integer,IndexedTable> tables=new HashMap<>();
        
        public int get(int adctdc,String name,int... slco) {
            return this.tables.get(adctdc).getIntValue(name,slco);
        }
        
        public Swap(IndexedTable prevTrans,IndexedTable currTrans) {

            this.tables.put(ADC,new IndexedTable(4));
            this.tables.put(TDC,new IndexedTable(4));

            for (int row=0; row<prevTrans.getRowCount(); row++) {

                int crate = (int)prevTrans.getValueAt(row,0);
                int slot = (int)prevTrans.getValueAt(row,1);
                int channel = (int)prevTrans.getValueAt(row,2);
                int order = (int)prevTrans.getValueAt(row,3);

                int prevSector = prevTrans.getIntValue("sector",crate,slot,channel,order);
                int prevLayer = prevTrans.getIntValue("layer",crate,slot,channel,order);
                int prevComp = prevTrans.getIntValue("component",crate,slot,channel,order);
                int prevOrder = prevTrans.getIntValue("order",crate,slot,channel,order);
                
                int adctdc;
                if      (order==ADCL || order==ADCR) adctdc = ADC;
                else if (order==TDCL || order==TDCR) adctdc = TDC;
                else throw new RuntimeException("Unknown order:  "+order);

                for (int ivar=0; ivar<VARNAMES.length; ivar++) {
                    int curr = currTrans.getIntValue(VARNAMES[ivar],crate,slot,channel,order);
                    this.tables.get(adctdc).setIntValue(curr,VARNAMES[ivar],prevSector,prevLayer,prevComp,prevOrder);
                }
            }
        }
    }
}
