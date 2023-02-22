package org.jlab.detector.banks;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Schema;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 * FilterBank specific to ADC/TDC banks, filtered on order by decades.  This
 * is to leverage hijacking the higher digits of order to encode additional
 * information.  See RawOrderType.
 * 
 * @author baltzell
 */
public class RawDataBank extends FilteredBank {

    public RawDataBank(Schema sch, int allocate){
        super(sch, allocate, "order");
    }

    public final void setFilter(RawOrderType... types) {
        for (int i=0; i<types.length; i++) {
            for (int j = 0; j<10; j++) {
                filterList.add(j + types[i].getTypeId());
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
        
        ftof.setFilter(RawOrderType.BACKGROUND,RawOrderType.DENOISED);
        
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
