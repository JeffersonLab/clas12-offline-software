package org.jlab.rec.eb;

import java.util.HashMap;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;

public class EBDatabaseConstantProvider extends DatabaseConstantProvider {

    private HashMap<Integer,Integer[]> integerContainer=new HashMap<Integer,Integer[]>();
    private HashMap<Integer,Double[]> doubleContainer=new HashMap<Integer,Double[]>();

    EBDatabaseConstantProvider() { super(); }
    EBDatabaseConstantProvider(int run, String var) { super(run,var); }

    int createIntegers (String table,int key) {
        final int len=this.length(table);
        Integer vals[]=new Integer[len];
        for (int ii=0; ii<len; ii++)
            vals[ii]=this.getInteger(table,ii);
        integerContainer.put(key,vals);
        return len;
    }
    
    int createDoubles (String table,int key) {
        final int len=this.length(table);
        Double vals[]=new Double[len];
        for (int ii=0; ii<len; ii++)
            vals[ii]=this.getDouble(table,ii);
        doubleContainer.put(key,vals);
        return len;
    }

    Integer[] getIntegers (int key) { return integerContainer.get(key); }
    Double[] getDoubles (int key) { return doubleContainer.get(key); }

}

