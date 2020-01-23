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

    SchemaFactory schema=null;
    private final int delay;
    private int verbosity=1;
    private volatile Map<Integer,HelicitySequenceDelayed> seqMap=new HashMap<>();
    Bank rcfgBank=null;
    
    public HelicitySequenceManager(int delay,List<String> filenames) {
        this.delay=delay;
        initialize(filenames);
    }
    
    private HelicitySequenceManager(int delay,HipoReader reader) {
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
            if (!seqMap.get(runno).setRunNumber(runno)) {
              System.err.println("HelicitySequenceManager:  error retrieving from CCDB, ABORT.");
              System.exit(1);
            }
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
    
    public HelicityBit search(Event event) {
        event.read(this.rcfgBank);
        if (rcfgBank.getRows()<1) return HelicityBit.UDF;
        return this.search(rcfgBank.getInt("run",0),rcfgBank.getLong("timestamp",0));
    }
    public HelicityBit predictGenerated(Event event) {
        event.read(this.rcfgBank);
        if (rcfgBank.getRows()<1) return HelicityBit.UDF;
        return this.predictGenerated(rcfgBank.getInt("run",0),rcfgBank.getLong("timestamp",0));
    }
    public HelicityBit searchGenerated(Event event) {
        event.read(this.rcfgBank);
        if (rcfgBank.getRows()<1) return HelicityBit.UDF;
        return this.searchGenerated(rcfgBank.getInt("run",0),rcfgBank.getLong("timestamp",0));
    }

    public boolean getHalfWavePlate(Event event) {
        event.read(this.rcfgBank);
        if (rcfgBank.getRows()>0) {
            int runno=rcfgBank.getInt("run",0);
            if (seqMap.containsKey(runno)) {
              return seqMap.get(runno).getHalfWavePlate();
            }
        }
        return false;
    }

    /**
     * Initialize from a HipoReader object.
     * This requires an unread HipoReader, since HipoReader doesn't provide a
     * rewind option, nor a way to read the set tags.
     * @param reader
     */ 
    private void initialize(HipoReader reader) {
        if (this.schema==null) {
            this.schema=reader.getSchemaFactory();
            this.rcfgBank=new Bank(this.schema.getSchema("RUN::config"));
        }
        reader.setTags(1);
        while (reader.hasNext()) {
            Event event=new Event();
            Bank flipBank=new Bank(this.schema.getSchema("HEL::flip"));
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
    private void initialize(List<String> filenames) {
        for (String filename : filenames) {
            HipoReader reader = new HipoReader();
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
