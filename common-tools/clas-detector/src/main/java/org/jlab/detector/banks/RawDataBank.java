package org.jlab.detector.banks;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

/**
 * An extension to support original bank interface.
 * 
 * @author baltzell
 */
public class RawDataBank extends RawBank {

    private DataBank dataBank;
    private String bankName;

    public RawDataBank(String bankName){
        super(null);
        this.bankName = bankName;
    }

    public RawDataBank(String bankName, int allocate){
        super(null, allocate);
        this.bankName = bankName;
    }

    public RawDataBank(String bankName, int allocate, OrderType... types) {
        super(null, allocate, types);
        this.bankName = bankName;
    }

    public RawDataBank(String bankName, OrderType... types) {
        super(null, types);
        this.bankName = bankName;
    }

    /**
     * Read the bank and prepare filtering
     * @param event
     */ 
    public void read(DataEvent event) {
        indexList.clear();
        dataBank = event.getBank(bankName);
        int rows = dataBank.rows();
        for(int i = 0; i < rows; i++){
            int value = dataBank.getInt(filterVar, i);
            if (filterList.contains(value)) indexList.add(i);
        }
    }
    
    /**
     * @param varName name of the bank variable
     * @param index filtered index to retrieve
     * @return value for the filtered index
     */
    @Override
    public int intValue(String varName, int index ){
        return dataBank.getInt(varName, indexList.get(index));
    }

    /**
     * @param varName name of the bank variable
     * @param index filtered index to retrieve
     * @return value for the filtered index
     */
    @Override
    public long longValue(String varName, int index ){
        return dataBank.getLong(varName, indexList.get(index));
    }

    /**
     * @param varName name of the bank variable
     * @param index filtered index to retrieve
     * @return value for the filtered index
     */
    @Override
    public float floatValue(String varName, int index ){
        return dataBank.getFloat(varName, indexList.get(index));
    }

    public static void main(String[] args){

        RawDataBank ftof = new RawDataBank("FTOF::adc");
        
        HipoDataSource r = new HipoDataSource();
        r.open("/Users/baltzell/data/decoded/clas_006676.evio.00170-00174.hipo");

        for(int i = 0; i < 10; i++){

            DataEvent e = r.getNextEvent();

            DataBank fadc = e.getBank("FTOF::adc");
            ftof.read(e);

	        System.out.println("\n============================= event");
            System.out.printf("FTOF ADC size %8d, filtered size = %8d\n",fadc.rows(),ftof.size());
	        //fadc.show();
	        for(int j = 0; j < ftof.size(); j++){
		        System.out.printf("%3d %3d %3d %4d, order = %5d, true index = %4d\n",j,
                        ftof.sector(j),
                  ftof.intValue("layer",j),
                  ftof.intValue("component",j),
                  ftof.intValue("order",j),
                  ftof.trueIndex(j));
	        }

        }
        

    }
} 
