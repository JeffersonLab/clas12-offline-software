package org.jlab.service.swaps;

import java.util.HashMap;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * Interpret the sector/layer/component/order mapping between two CCDB "/daq/tt"
 * translation tables, based on their shared crate/slot/channel/order, and store
 * it in a new IndexedTable for access.
 *
 * @author baltzell
 */
public class Swap {

    public static final int ADC = 0;
    public static final int TDC = 1;

    // CCDB convention for "order" in crate/slot/channel/order:
    public static final int ADCL=0;
    public static final int ADCR=1;
    public static final int TDCL=2;
    public static final int TDCR=3;

    public static final String[] VARNAMES = {"sector", "layer", "component", "order"};

    private HashMap<Integer,IndexedTable> tables=new HashMap<>();

    /**
     * @param adctdc 0/1 = ADC/TDC
     * @param varName name of new variable to retrieve (sector/layer/component/order)
     * @param slco array of old variables (sector/layer/component/order)
     * @return new value of the requested variable
     */
    public int get(int adctdc,String varName,int... slco) {
        return this.tables.get(adctdc).getIntValue(varName,slco);
    }

    /**
     * @param fromTrans previous translation table
     * @param toTrans new translation table
     */
    public Swap(IndexedTable fromTrans,IndexedTable toTrans) {

        this.tables.put(ADC,new IndexedTable(VARNAMES.length,String.join(":",VARNAMES)));
        this.tables.put(TDC,new IndexedTable(VARNAMES.length,String.join(":",VARNAMES)));

        for (int row=0; row<fromTrans.getRowCount(); row++) {

            // crate/slot/channel is the base for mapping between the two tables:
            final int crate = Integer.valueOf((String)fromTrans.getValueAt(row,0));
            final int slot = Integer.valueOf((String)fromTrans.getValueAt(row,1));
            final int channel = Integer.valueOf((String)fromTrans.getValueAt(row,2));

            // get the corresponding previous sector/layer/component/order:
            final int prevSector = fromTrans.getIntValue("sector",crate,slot,channel);
            final int prevLayer = fromTrans.getIntValue("layer",crate,slot,channel);
            final int prevComp = fromTrans.getIntValue("component",crate,slot,channel);
            final int prevOrder = fromTrans.getIntValue("order",crate,slot,channel);

            // determine whether it's an ADC or TDC:
            int adctdc;
            switch (prevOrder) {
                case ADCL:
                case ADCR:
                    adctdc = ADC;
                    break;
                case TDCL:
                case TDCR:
                    adctdc = TDC;
                    break;
                default:
                    throw new RuntimeException("Unknown order:  "+prevOrder);
            }
                
            //System.out.print(String.format("%d/%d/%d   -   ",crate,slot,channel));
            //System.out.print(String.format("%d/%d/%d/%d   -   ",prevSector,prevLayer,prevComp,prevOrder));

            // get the corresponding current sector/layer/component/order and fill the table:
            //String sep="";
            for (String varName : VARNAMES) {
                final int curr = toTrans.getIntValue(varName, crate, slot, channel);
                this.tables.get(adctdc).addEntry(prevSector,prevLayer,prevComp,prevOrder);
                this.tables.get(adctdc).setIntValue(curr, varName, prevSector, prevLayer, prevComp, prevOrder);
                //System.out.print((String.format("%s%d",sep,curr)));
                //sep="/";
            }
            //System.out.println();
        }
    }

    public String toString(int adctdc) {
        String ret = "";
        final String prefix = adctdc==ADC ? "ADC:  " : "TDC:  ";
        for (int row=0; row<tables.get(adctdc).getRowCount(); row++) {
            int[] prev = new int[VARNAMES.length];
            for (int ivar=0; ivar<VARNAMES.length; ivar++) {
                prev[ivar] = Integer.parseInt((String)tables.get(adctdc).getValueAt(row,ivar));
            }
            int[] curr = new int[VARNAMES.length];
            for (int ivar=0; ivar<VARNAMES.length; ivar++) {
                curr[ivar] = tables.get(adctdc).getIntValue(VARNAMES[ivar],prev);
            }
            for (int ivar=0; ivar<VARNAMES.length; ivar++) {
                if (curr[ivar] != prev[ivar]) {
                    ret += prefix;
                    ret += String.format("%d/%d/%d/%d --> %d/%d/%d/%d\n",
                            prev[0],prev[1],prev[2],prev[3],
                            curr[0],curr[1],curr[2],curr[3]);
                    break;
                }
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        return toString(ADC)+toString(TDC);
    }
}