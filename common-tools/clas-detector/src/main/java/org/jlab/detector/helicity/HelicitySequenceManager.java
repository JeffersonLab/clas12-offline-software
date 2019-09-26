package org.jlab.detector.helicity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 *
 * Manage helicity sequences for multiple run numbers simultaneously.
 * 
 * @author baltzell
 */
public final class HelicitySequenceManager {

    private final int delay;
    private int verbosity=1;
    private volatile Map<Integer,HelicitySequenceDelayed> seqMap=new HashMap<>();

    public HelicitySequenceManager(int delay) {
        this.delay=delay;
    }

    public HelicitySequenceManager(int delay,List<String> filenames) {
        this.delay=delay;
        initialize(filenames);
    }
    
    public HelicitySequenceManager(int delay,HipoReader reader) {
        this.delay=delay;
        initialize(reader);
    }


    public void setVerbosity(int verbosity) {
        this.verbosity=verbosity;
        for (HelicitySequence hs : seqMap.values()) {
            hs.setVerbosity(verbosity);
        }
    }

    private boolean addState(int runno,HelicityState state) {
        if (runno <= 0) return false;
        if (!seqMap.containsKey(runno)) {
            seqMap.put(runno, new HelicitySequenceDelayed(delay));
            seqMap.get(runno).setVerbosity(verbosity);
        }
        return seqMap.get(runno).addState(state);
    }

    public HelicitySequence getSequence(int runno) {
        if (seqMap.containsKey(runno)) return seqMap.get(runno);
        return null;
    }

    public HelicityBit search(int runno, long timestamp) {
        if (seqMap.containsKey(runno)) return seqMap.get(runno).search(timestamp);
        return HelicityBit.UDF;
    }

    public HelicityBit predictGenerated(int runno, long timestamp) {
        if (seqMap.containsKey(runno)) return seqMap.get(runno).predictGenerated(timestamp);
        return HelicityBit.UDF;
    }
    
    public HelicityBit searchGenerated(int runno, long timestamp) {
        if (seqMap.containsKey(runno)) return seqMap.get(runno).searchGenerated(timestamp);
        return HelicityBit.UDF;
    }
  
    /**
     * Initialize from a HipoReader object.
     * @param reader
     */ 
    public void initialize(HipoReader reader) {
        SchemaFactory schema = reader.getSchemaFactory();
        while (reader.hasNext()) {
            Event event=new Event();
            Bank flipBank=new Bank(schema.getSchema("HEL::flip"));
            reader.nextEvent(event);
            event.read(flipBank);
            if (flipBank.getRows()<1) continue;
            final int runno=flipBank.getInt("run",0);
            this.addState(runno,HelicityState.createFromFlipBank(flipBank));
        }
    }

    /**
     * Initialize from a list of file names:
     * @param filenames
     */ 
    public void initialize(List<String> filenames) {
        for (String filename : filenames) {
            HipoReader reader = new HipoReader();
            reader.setTags(1);
            reader.open(filename);
            initialize(reader);
            reader.close();
        }
    }

    public boolean analyze() {
        boolean ret=true;
        for (HelicitySequenceDelayed hsd : seqMap.values()) {
            if (!hsd.analyze()) ret=false;
        }
        return ret;
    }

    public void show() {
        for (Entry<Integer, HelicitySequenceDelayed> x : seqMap.entrySet()) {
            System.out.println("Run Number:::::::::::::::::: "+x.getKey());
            x.getValue().show();
        }
    }

}