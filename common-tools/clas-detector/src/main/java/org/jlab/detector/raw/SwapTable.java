package org.jlab.detector.raw;

import java.util.HashMap;
import java.util.Map;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * Interpret the sector/layer/component/order mapping between two CCDB "/daq/tt"
 * translation tables, based on their shared crate/slot/channel/order, and store
 * it in a new IndexedTable for access.
 *
 * @author baltzell
 */
public class SwapTable {

    public static final String[] VAR_NAMES = {"sector", "layer", "component", "order"};
    private static final Map<String,Integer> VAR_INDEX_MAP = new HashMap<String,Integer>() {{
        put(VAR_NAMES[0],0);
        put(VAR_NAMES[1],1);
        put(VAR_NAMES[2],2);
        put(VAR_NAMES[3],3);
    }};

    private IndexedTable table = null;

    /**
     * @param varName name of new variable to retrieve (sector/layer/component/order)
     * @param slco array of old variables (sector/layer/component/order)
     * @return new value of the requested variable
     */
    public int get(String varName,int... slco) {
        if (this.table.hasEntry(slco)) {
            return this.table.getIntValue(varName,slco);
        }
        else {
            return slco[getVariableIndex(varName)];
        }
    }

    /**
     * @param slco array of old variables (sector/layer/component/order)
     * @return new value of the requested variable
     */
    public int[] get(int... slco) {
        int[] ret = new int[4];
        for (int ivar=0; ivar<VAR_NAMES.length; ivar++) {
            ret[ivar] = this.get(VAR_NAMES[ivar],slco);
        }
        return ret;
    }

    /**
     * @param varName name of variable's index in s/l/c/o to retrieve
     * @return index
     */
    public static int getVariableIndex(String varName) {
        return VAR_INDEX_MAP.get(varName);
    }

    /**
     * @param fromTrans previous translation table
     * @param toTrans new translation table
     */
    public SwapTable(IndexedTable fromTrans,IndexedTable toTrans) {

        for (int ivar=0; ivar<VAR_NAMES.length; ivar++) {
            VAR_INDEX_MAP.put(VAR_NAMES[ivar],ivar);
        }
        
        this.table = new IndexedTable(VAR_NAMES.length,String.join(":",VAR_NAMES));

        for (int row=0; row<fromTrans.getRowCount(); row++) {

            // crate/slot/channel is the base for mapping between the two tables:
            final int crate = Integer.valueOf((String)fromTrans.getValueAt(row,0));
            final int slot = Integer.valueOf((String)fromTrans.getValueAt(row,1));
            final int channel = Integer.valueOf((String)fromTrans.getValueAt(row,2));

            // load the previous and current values of sector/layer/component/order:
            boolean diff = false;
            int[] previous = new int[VAR_NAMES.length];
            int[] current = new int[VAR_NAMES.length];
            for (int ivar=0; ivar<VAR_NAMES.length; ivar++) {
                previous[ivar] = fromTrans.getIntValue(VAR_NAMES[ivar],crate,slot,channel);
                current[ivar] = toTrans.getIntValue(VAR_NAMES[ivar], crate, slot, channel);
                if (previous[ivar] != current[ivar]) {
                    diff = true;
                }
            }
            
            // fill the new table if different:
            if (diff) {
                String[] cvals = new String[VAR_NAMES.length*2];
                for (int ii=0; ii<VAR_NAMES.length; ii++) {
                    cvals[ii] = String.format("%d",previous[ii]);
                }
                for (int ii=0; ii<VAR_NAMES.length; ii++) {
                    cvals[ii+VAR_NAMES.length] = String.format("%d",current[ii]);
                }
                this.table.addEntryFromString(cvals);
            }
        }
    }

    public String toString() {
        String ret = "";
        int[] prev = new int[VAR_NAMES.length];
        int[] curr = new int[VAR_NAMES.length];
        for (int row=0; row<this.table.getRowCount(); row++) {
            for (int ivar=0; ivar<VAR_NAMES.length; ivar++) {
                prev[ivar] = Integer.valueOf((String)this.table.getValueAt(row,ivar));
            }
            for (int ivar=0; ivar<VAR_NAMES.length; ivar++) {
                curr[ivar] = this.table.getIntValue(VAR_NAMES[ivar],prev);
            }
            ret += String.format("%d/%d/%d/%d --> %d/%d/%d/%d\n",
                    prev[0],prev[1],prev[2],prev[3],
                    curr[0],curr[1],curr[2],curr[3]);
        }
        return ret;
    }

}