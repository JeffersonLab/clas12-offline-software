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

        this.tables.put(ADC,new IndexedTable(4));
        this.tables.put(TDC,new IndexedTable(4));

        for (int row=0; row<fromTrans.getRowCount(); row++) {

            final int crate = (int)fromTrans.getValueAt(row,0);
            final int slot = (int)fromTrans.getValueAt(row,1);
            final int channel = (int)fromTrans.getValueAt(row,2);
            final int order = (int)fromTrans.getValueAt(row,3);

            final int prevSector = fromTrans.getIntValue("sector",crate,slot,channel,order);
            final int prevLayer = fromTrans.getIntValue("layer",crate,slot,channel,order);
            final int prevComp = fromTrans.getIntValue("component",crate,slot,channel,order);
            final int prevOrder = fromTrans.getIntValue("order",crate,slot,channel,order);

            int adctdc;
            switch (order) {
                case ADCL:
                case ADCR:
                    adctdc = ADC;
                    break;
                case TDCL:
                case TDCR:
                    adctdc = TDC;
                    break;
                default:
                    throw new RuntimeException("Unknown order:  "+order);
            }

            for (String varName : VARNAMES) {
                final int curr = toTrans.getIntValue(varName, crate, slot, channel, order);
                this.tables.get(adctdc).setIntValue(curr, varName, prevSector, prevLayer, prevComp, prevOrder);
            }
        }
    }
}