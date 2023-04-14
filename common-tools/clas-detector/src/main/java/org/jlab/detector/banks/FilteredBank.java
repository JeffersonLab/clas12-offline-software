package org.jlab.detector.banks;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Schema;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 * Utility to provide access to a bank filtered on a certain variable while
 * maintaining mapping to the true row index in the bank.
 *
 * @author gavalian
 * @author baltzell
 */
public class FilteredBank {

    public static final int DEFAULT_ALLOC = 40;

    private final String filterVar;
    private Bank dataBank = null;
    private final List<Integer> indexList = new ArrayList<>();
    protected final Set<Integer> filterList = new HashSet<>();

    /**
     * @param schema schema for the bank to filter
     * @param allocate number of rows to allocate
     * @param variableName name of the variable to filter on 
     */
    public FilteredBank(Schema schema, int allocate, String variableName){
        dataBank = new Bank(schema, allocate);
        filterVar = variableName;
    }


    /**
     * @param schema schema for the bank to filter
     * @param variableName name of the variable to filter on 
     */
    public FilteredBank(Schema schema, String variableName){
        dataBank = new Bank(schema, DEFAULT_ALLOC);
        filterVar = variableName;
    }

    /**
     * Set the filtering criteria
     * @param values values to filter for
     */
    public final void setFilter(int... values){
        filterList.clear();
        for(int i = 0; i < values.length; i++)
            filterList.add(values[i]);
    }

    /**
     * Read the bank and prepare filtering
     */ 
    public void read(Event evt){
        evt.read(dataBank);        
        this.notifyRead();
    }

    /**
     * Prepare filtering for the current bank's values
     */
    protected void notifyRead(){
        indexList.clear();
        int rows = dataBank.getRows();
        for(int i = 0; i < rows; i++){
            int value = dataBank.getInt(filterVar, i);
            if (filterList.contains(value)) indexList.add(i);
        }
    }

    /**
     * @return number of bank rows that satisfy filtering criteria
     */
    public int size(){ 
        return this.indexList.size();
    }

    /**
     * @param varName name of the bank variable
     * @param index filtered index to retrieve
     * @return value for the filtered index
     */
    public int intValue(String varName, int index ){
        return dataBank.getInt(varName, indexList.get(index));
    }

    /**
     * @param varName name of the bank variable
     * @param index filtered index to retrieve
     * @return value for the filtered index
     */
    public long longValue(String varName, int index ){
        return dataBank.getLong(varName, indexList.get(index));
    }

    /**
     * @param varName name of the bank variable
     * @param index filtered index to retrieve
     * @return value for the filtered index
     */
    public float floatValue(String varName, int index ){
        return dataBank.getFloat(varName, indexList.get(index));
    }

    /**
     * @param index filtered index to retrieve
     * @return true bank row index corresponding to the filtered index
     */
    public int trueIndex(int index){
        return this.indexList.get(index);
    }
    
    public static void main(String[] args){

        String file = "/Users/gavalian/Work/DataSpace/rga/rec_005988.00005.00009.hipo";
        HipoReader r = new HipoReader();
        r.open(file);
        Event e = new Event();
        
        FilteredBank ftof = new FilteredBank(r.getSchemaFactory().getSchema("FTOF::adc"),40,"order");
        Bank        fadc = new Bank(r.getSchemaFactory().getSchema("FTOF::adc"));
        
        ftof.setFilter(1);
        
        for(int i = 0; i < 120; i++){
            r.nextEvent(e);
            e.read(fadc);
            // bank has to read initialize
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
