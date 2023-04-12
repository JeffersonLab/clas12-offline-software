package org.jlab.detector.banks;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Schema;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 * A FilteredBank specific to raw ADC/TDC banks, filtered on order by decades.
 * This is to leverage hijacking the higher decimal digits of order to encode
 * additional information, and avoid reconstruction engines or others from
 * manually interpreting order.
 * 
 * @author baltzell
 */
public class RawDataBank extends FilteredBank {

    public static final String FILTER_VAR_NAME = "order"; 

    public static final OrderType[] DEFAULT_ORDERS = new OrderType[] {
        OrderType.NOISE0, OrderType.BGADDED_NOISE0
    };

    public enum OrderType {
        NOISE0          (  0),  // normal hits retained by denoising level-0
        BGADDED_NOISE0  ( 10),  // hits added by background merging and retained by level-0
        BGREMOVED       ( 20),  // hits removed during background merging 
        RESERVED        ( 30),  // reserved for later use
        NOISE1          ( 40),  // normal hits retained by level-1 denoising
        NOISE2          ( 50),  // normal hits retained by level-2 denoising
        NOISE3          ( 60),  // normal hits retained by level-3 denoising
        BGADDED_NOISE1  ( 70),  // background hits retained by level-1 denoising
        BGADDED_NOISE2  ( 80),  // background hits retained by level-2 denoising
        BGADDED_NOISE3  ( 90),  // background hits retained by level-3 denoising
        USER1           (100),
        USER2           (110),
        USER3           (120);
        private final int rawOrderId;
        private OrderType(int id){ rawOrderId = id; }
        public int getTypeId() { return rawOrderId; }
    }

    public RawDataBank(Schema sch){
        super(sch, DEFAULT_ALLOC, FILTER_VAR_NAME);
        setFilter(DEFAULT_ORDERS);
    }

    public RawDataBank(Schema sch, int allocate){
        super(sch, allocate, FILTER_VAR_NAME);
        setFilter(DEFAULT_ORDERS);
    }

    public RawDataBank(Schema sch, int allocate, OrderType... types) {
        super(sch, allocate, FILTER_VAR_NAME);
        setFilter(types);
    }

    public RawDataBank(Schema sch, OrderType... types) {
        super(sch, DEFAULT_ALLOC, FILTER_VAR_NAME);
        setFilter(types);
    }

    public final void setFilter(OrderType... types) {
        filterList.clear();
        for (OrderType type : types) {
            for (int j = 0; j<10; j++) {
                filterList.add(j + type.getTypeId());
            }
        }
    }

    /**
     * Maybe we should name this something else?
     * @param index filtered index
     * @return raw/true order, the first digit
     */
    public int trueOrder(int index){
        return this.intValue("order", index)%10;
    }

    /**
     * @param index filtered index
     * @return sector value
     */
    public int sector(int index){
        return this.intValue("sector", index);
    }
    
    /**
     * @param index filtered index
     * @return layer value
     */
    public int layer(int index){
        return this.intValue("layer", index);
    }
    
    /**
     * @param index filtered index
     * @return component value
     */
    public int component(int index){
        return this.intValue("component", index);
    }
    
    /**
     * @param index filtered index
     * @return adc value
     */
    public int adc(int index){
        return this.intValue("ADC", index);
    }
    
    /**
     * @param index filtered index
     * @return tdc value
     */
    public int tdc(int index){
        return this.intValue("TDC", index);
    }

    public static void main(String[] args){

        HipoReader r = new HipoReader();
        r.open("/Users/baltzell/data/jpsitcs_005340.hipo");
        Event e = new Event();
        
        RawDataBank ftof = new RawDataBank(r.getSchemaFactory().getSchema("FTOF::adc"),40);
        Bank        fadc = new Bank(r.getSchemaFactory().getSchema("FTOF::adc"));
        
        ftof.setFilter(OrderType.NOISE0,OrderType.BGREMOVED);
        
        for(int i = 0; i < 10; i++){
            r.nextEvent(e);
            e.read(fadc);
            ftof.read(e);
	        System.out.println("\n============================= event");
            System.out.printf("FTOF ADC size %8d, filtered size = %8d\n",fadc.getRows(),ftof.size());
	        fadc.show();
	        for(int j = 0; j < ftof.size(); j++){
		        System.out.printf("%3d %3d %3d %4d, order = %5d, true index = %4d\n",j,
				  ftof.sector(j),
                  ftof.layer(j),
                  ftof.component(j),
                  ftof.trueOrder(j),
                  ftof.trueIndex(j));
	        }
        }
    }
}
