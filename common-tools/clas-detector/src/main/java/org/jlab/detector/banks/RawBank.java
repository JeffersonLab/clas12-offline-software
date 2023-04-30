package org.jlab.detector.banks;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Schema;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 * A FilteredBank specific to raw ADC/TDC banks, filtered on order by decades.
 * This is to leverage hijacking the higher decimal digits of order to encode
 * additional information, and avoid reconstruction engines or others from
 * having to manually interpret order, although they still can if necessary.
 * 
 * @author baltzell
 */
public class RawBank extends FilteredBank {

    public static final class OrderGroups {
        public static final OrderType[] NOMINAL = new OrderType[] {
            OrderType.NOMINAL,
            OrderType.BGADDED_NOMINAL
        };
        public static final OrderType[] NOISE1 = new OrderType[] {
            OrderType.NOMINAL,
            OrderType.NOISE1,
            OrderType.BGADDED_NOMINAL,
            OrderType.BGADDED_NOISE1
        };
        public static final OrderType[] NOISE2 = new OrderType[] {
            OrderType.NOMINAL,
            OrderType.NOISE1,
            OrderType.NOISE2,
            OrderType.BGADDED_NOMINAL,
            OrderType.BGADDED_NOISE1,
            OrderType.BGADDED_NOISE2,
        };
        public static final OrderType[] NOISE3 = new OrderType[] {
            OrderType.NOMINAL,
            OrderType.NOISE1,
            OrderType.NOISE2,
            OrderType.NOISE3,
            OrderType.BGADDED_NOMINAL,
            OrderType.BGADDED_NOISE1,
            OrderType.BGADDED_NOISE2,
            OrderType.BGADDED_NOISE3
        };
        public static final OrderType[] NODENOISE_NOBG = new OrderType[] {
            OrderType.NOMINAL,
            OrderType.NOISE1,
            OrderType.NOISE2,
            OrderType.NOISE3
        };
        public static final OrderType[] NOBG = new OrderType[] {
            OrderType.NOMINAL,
            OrderType.NOISE1,
            OrderType.NOISE2,
            OrderType.NOISE3,
            OrderType.BGREMOVED
        };
        public static final OrderType[] DEFAULT = NOMINAL;
        public static final OrderType[] NODENOISE = NOISE3;
    }

    public static enum OrderType {
        NOMINAL         (  0),  // normal hits retained by denoising level-0
        BGADDED_NOMINAL ( 10),  // hits added by background merging and retained by level-0
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

    public static final String FILTER_VAR_NAME = "order"; 

    public RawBank(Schema sch){
        super(sch, DEFAULT_ALLOC, FILTER_VAR_NAME);
        setFilter(OrderGroups.DEFAULT);
    }

    public RawBank(Schema sch, int allocate){
        super(sch, allocate, FILTER_VAR_NAME);
        setFilter(OrderGroups.DEFAULT);
    }

    public RawBank(Schema sch, int allocate, OrderType... types) {
        super(sch, allocate, FILTER_VAR_NAME);
        setFilter(types);
    }

    public RawBank(Schema sch, OrderType... types) {
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
     * Get one of the standard OrderGroups from its string name.
     * @param group string name of one of the OrderGroups
     * @return the corresponding list of orders 
     * @throws java.lang.NoSuchFieldException 
     * @throws java.lang.IllegalAccessException 
     */
    public static final OrderType[] getFilterGroup(String group)
        throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        return (OrderType[])OrderGroups.class.getField(group).get(null);
    }

    /**
     * Create a custom order group from OrderType's string names 
     * @param type string names of OrderTypes
     * @return the corresponding list of orders
     * @throws java.lang.NoSuchFieldException
     * @throws java.lang.IllegalAccessException
     */
    public static final OrderType[] createFilterGroup(String... type)
        throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        OrderType[] ret = new OrderType[type.length];
        for (int i=0; i<type.length; i++) {
            ret[i] = (OrderType)OrderType.class.getField(type[i]).get(null);
        }
        return ret;
    }

    /**
     * Maybe we should name this something else?
     * @param index filtered index
     * @return raw/true order, the first digit
     */
    public int trueOrder(int index){
        return this.getInt("order", index)%10;
    }

    /**
     * @param index filtered index
     * @return sector value
     */
    public int sector(int index){
        return this.getInt("sector", index);
    }
    
    /**
     * @param index filtered index
     * @return layer value
     */
    public int layer(int index){
        return this.getInt("layer", index);
    }
    
    /**
     * @param index filtered index
     * @return component value
     */
    public int component(int index){
        return this.getInt("component", index);
    }
    
    /**
     * @param index filtered index
     * @return adc value
     */
    public int adc(int index){
        return this.getInt("ADC", index);
    }
    
    /**
     * @param index filtered index
     * @return tdc value
     */
    public int tdc(int index){
        return this.getInt("TDC", index);
    }

    public static void main(String[] args){

        try {
            for (OrderType ot : getFilterGroup("DEFAULT")) {
                System.out.println(ot);
            }
            for (OrderType ot : createFilterGroup("NOISE0","BGREMOVED")) {
                System.out.println(ot);
            }
        } catch (Exception ex) {
            Logger.getLogger(RawBank.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        HipoReader r = new HipoReader();
        r.open("/Users/baltzell/data/decoded/clas_006676.evio.00170-00174.hipo");
        Event e = new Event();
        
        RawBank ftof = new RawBank(r.getSchemaFactory().getSchema("FTOF::adc"),40);
        Bank    fadc = new Bank(r.getSchemaFactory().getSchema("FTOF::adc"));
        
        ftof.setFilter(OrderType.NOMINAL,OrderType.BGREMOVED);
        
        for(int i = 0; i < 5; i++){
            r.nextEvent(e);
            e.read(fadc);
            ftof.read(e);
	        System.out.println("\n============================= event");
            System.out.printf("FTOF ADC size %8d, filtered size = %8d\n",fadc.getRows(),ftof.rows());
	        fadc.show();
	        for(int j = 0; j < ftof.rows(); j++){
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
