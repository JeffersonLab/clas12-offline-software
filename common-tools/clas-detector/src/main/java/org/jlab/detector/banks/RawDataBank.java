package org.jlab.detector.banks;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Schema;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 *
 * @author baltzell
 */
public class RawDataBank extends FilteredBank {

    public RawDataBank(Schema sch, int allocate){
        super(sch, allocate, "order");
    }

    public final void setDecadeOrderFilter(int... decades) {
        filterList.clear();
        for (int i = 0; i < decades.length; i++) {
            if (decades[i]%10 != 0 || decades[i]>120) {
                System.err.println("BAD DECADE!!!!!!!!!!!!!!!");
            }
            for (int j = 0; j<10; j++) {
                filterList.add(decades[i] + j);
            }
        }
    }

    public int trueOrder(int index){
        return this.intValue("order", index)/10;
    }
    public int sector(int index){
        return this.intValue("sector", index);
    }
    public int layer(int index){
        return this.intValue("sector", index);
    }
    public int component(int index){
        return this.intValue("sector", index);
    }
    public int adc(int index){
        return this.intValue("ADC", index);
    }
    public int tdc(int index){
        return this.intValue("TDC", index);
    }

    public static void main(String[] args){

        HipoReader r = new HipoReader();
        r.open("/Users/baltzell/data/jpsitcs_005340.hipo");
        Event e = new Event();
        
        RawDataBank ftof = new RawDataBank(r.getSchemaFactory().getSchema("FTOF::adc"),40);
        Bank        fadc = new Bank(r.getSchemaFactory().getSchema("FTOF::adc"));
        
        ftof.setDecadeOrderFilter(0);
        
        for(int i = 0; i < 10; i++){
            r.nextEvent(e);
            e.read(fadc);
            ftof.read(e);
	        System.out.println("\n============================= event");
            System.out.printf("FTOF ADC size %8d, filtered size = %8d\n",fadc.getRows(),ftof.size());
	        fadc.show();
	        for(int j = 0; j < ftof.size(); j++){
		        System.out.printf("%3d %3d %3d %4d, order = %5d, true index = %4d\n",j,
				  ftof.intValue("sector",j),
                  ftof.intValue("layer",j),
                  ftof.intValue("component",j),
                  ftof.intValue("order",j),
                  ftof.trueIndex(j));
	        }
        }
    }
}
