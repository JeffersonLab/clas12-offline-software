package org.jlab.rec.tof.hit;

/**
 * The order variable in raw banks encodes in two bits the information on the
 * readout type (ADC/TDC) and readout side (for example left/right or 
 * upstream/downstream)
 * 
 * @author devita
 */
public enum RawOrder {

    ADC1        ( 0),  // left or upstream ADC
    ADC2        ( 1),  // right or downstream ADC
    TDC1        ( 2),  // left or upstream TDC
    TDC2        ( 3);  // right or downstream TDC

    private final int rawOrderId;

    private RawOrder(int id){
        rawOrderId = id;
    }

    public int getTypeId() {
        return rawOrderId;
    }

}

