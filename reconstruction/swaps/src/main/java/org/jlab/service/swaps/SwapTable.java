package org.jlab.service.swaps;

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

    public static final String[] VARNAMES = {"sector", "layer", "component", "order"};

    private IndexedTable table = null;

    /**
     * @param varName name of new variable to retrieve (sector/layer/component/order)
     * @param slco array of old variables (sector/layer/component/order)
     * @return new value of the requested variable
     */
    public int get(String varName,int... slco) {
        return this.table.getIntValue(varName,slco);
    }

    /**
     * @param fromTrans previous translation table
     * @param toTrans new translation table
     */
    public SwapTable(IndexedTable fromTrans,IndexedTable toTrans) {

        this.table = new IndexedTable(VARNAMES.length,String.join(":",VARNAMES));

        for (int row=0; row<fromTrans.getRowCount(); row++) {

            // crate/slot/channel is the base for mapping between the two tables:
            final int crate = Integer.valueOf((String)fromTrans.getValueAt(row,0));
            final int slot = Integer.valueOf((String)fromTrans.getValueAt(row,1));
            final int channel = Integer.valueOf((String)fromTrans.getValueAt(row,2));

            // load the previous and current values of sector/layer/component/order:
            int[] previous = new int[VARNAMES.length];
            int[] current = new int[VARNAMES.length];
            for (int ivar=0; ivar<VARNAMES.length; ivar++) {
                previous[ivar] = fromTrans.getIntValue(VARNAMES[ivar],crate,slot,channel);
                current[ivar] = toTrans.getIntValue(VARNAMES[ivar], crate, slot, channel);
            }
            
            // load the new table:
            String[] cvals = new String[VARNAMES.length*2];
            for (int ii=0; ii<VARNAMES.length; ii++) {
                cvals[ii] = String.format("%d",previous[ii]);
            }
            for (int ii=0; ii<VARNAMES.length; ii++) {
                cvals[ii+VARNAMES.length] = String.format("%d",current[ii]);
            }
            this.table.addEntryFromString(cvals);
        }
    }

    public String toString() {
        String ret = "";
        int[] prev = new int[VARNAMES.length];
        int[] curr = new int[VARNAMES.length];
        for (int row=0; row<this.table.getRowCount(); row++) {
            for (int ivar=0; ivar<VARNAMES.length; ivar++) {
                prev[ivar] = Integer.valueOf((String)this.table.getValueAt(row,ivar));
            }
            for (int ivar=0; ivar<VARNAMES.length; ivar++) {
                curr[ivar] = this.table.getIntValue(VARNAMES[ivar],prev);
            }
            for (int ivar=0; ivar<VARNAMES.length; ivar++) {
                if (curr[ivar] != prev[ivar]) {
                    ret += String.format("%d/%d/%d/%d --> %d/%d/%d/%d\n",
                            prev[0],prev[1],prev[2],prev[3],
                            curr[0],curr[1],curr[2],curr[3]);
                    break;
                }
            }
        }
        return ret;
    }

}