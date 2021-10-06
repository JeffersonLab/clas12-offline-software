package org.jlab.detector.helicity;

import org.jlab.utils.groups.IndexedTable;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;

/**
 * Just adds a delay to HelicitySequence, where delay is the number
 * of "windows".  For example, a quartet pattern contains 4 windows, and
 * if the helicity clock is the usual 29.56 Hz, a window lasts 33.829 ms.
 *
 * Stores a sequence of helicity states and provides timestamp- or state-count-
 * based search of helicity state based on the measured sequence or pseudo-random
 * generator sequence, and provides some integrity checking of the sequence, 
 * including comparing the measured and generator sequences.
 *
 * ____________________________________________________________________
 * Getter methods naming convention:
 * 
 * Prefixes:
 * "get"     - based on state count
 * "search"  - based on finding timestamp in the measured sequence
 * "predict" - based on generator seed time and expected periodicity
 *
 * Suffixes:
 * "Generated" - use the psuedo-random generator's sequence
 * ____________________________________________________________________
 * 
 * The generator methods are able to look past the measured range, while the
 * non-generator methods cannot.
 * 
 * The inputs to initialize the sequence are {@link HelicityState} objects, one
 * per window at the helicity board clock frequency, which contain important
 * information for constructing and validating the sequence (e.g. helicity and
 * sync bits and timestamps).
 * 
 * The return values from getters are just {@link HelicityBit} objects and
 * represent the HWP-corrected helicity.
 *
 * See {@link HelicityAnalysisSimple} for an example on using this class.
 * 
 * @author baltzell
 */
public class HelicitySequenceDelayed extends HelicitySequence {
  
    private int delay;

    public HelicitySequenceDelayed(int delay) {
        this.delay=delay;
    }

    public boolean setRunNumber(int runNumber) {
        try {
            DatabaseConstantProvider dcp=new DatabaseConstantProvider(runNumber,"default");
            IndexedTable it=dcp.readTable("/runcontrol/helicity");
            this.helicityClock=it.getDoubleValue("frequency",0,0,0);
            this.delay=it.getIntValue("delay",0,0,0);
            this.pattern=HelicityPattern.create((byte)it.getIntValue("pattern",0,0,0));
            this.generator.setClock(this.helicityClock);
            System.out.println(String.format("HelicitySequenceDelayed:  got parameters from CCDB for run %d:",runNumber));
            System.out.println(String.format("HelicitySequenceDelayed:  CCDB clock: %.4f seconds",this.helicityClock));
            System.out.println(String.format("HelicitySequenceDelayed:  CCDB delay: %d windows",this.delay));
            System.out.println(String.format("HelicitySequenceDelayed:  CCDB pattern: %s",this.pattern));
            if (this.pattern != HelicityPattern.QUARTET) {
                System.err.println("HelicitySequenceDelayed:  not ready for non-QUARTET pattern");
                return false;
            }
            return true;
        }
        catch (Exception e) {
            System.err.println(String.format("HelicitySequence:  error retrieving clock from ccdb for run %d",runNumber));
            return false;
        }
    }

    /**
     * Get the delay-corrected nth state in the measured sequence, by walking
     * forward delay states and returning HelicityBit.UDF if any errors
     * along the way.
     * 
     * @param n the index of the state, where 0 corresponds to the first state
     * @return the helicity state
     */
    @Override
    protected HelicityBit get(int n) {
        if (n<0) return HelicityBit.UDF;
        for (int i=n+1; i<=n+delay && i<super.size(); i++) {
            // if any of the intermediate states have integrity issues:
            if (super.getState(i).getSwStatus()!=0) {
                return HelicityBit.UDF;
            }
        }
        return super.get(n+delay);
    }
    
    /**
     * Get the delay-corrected state of a TI timestamp by searching the measured
     * sequence and walking forward, returning HelicityBit.UDF if any errors
     * along the way.
     * 
     * @param timestamp TI-timestamp (i.e. RUN::config.timestamp)
     * @return the helicity bit
     */
    @Override
    public HelicityBit search(long timestamp) {
        return this.search(timestamp,0);
    }
    
    /**
     * Get the delay-corrected state of a TI timestamp by searching the measured
     * sequence and walking forward, returning HelicityBit.UDF if any errors
     * along the way.
     * 
     * @param timestamp TI-timestamp (i.e. RUN::config.timestamp)
     * @param offset number of states offset
     * @return the helicity bit
     */
    public HelicityBit search(long timestamp,int offset) {
        return this.get(super.searchIndex(timestamp)+offset);
    }

    /**
     * Get the delay-corrected nth state in the sequence, based on the generator,
     * returning HelicityBit.UDF if generator cannot be initialized or the state
     * index is before the generator was initialized.
     *
     * @param n the index of the state
     * @return the helicity bit
     */
    @Override
    protected HelicityBit getGenerated(int n) {
        return super.getGenerated(n+delay);
    }

    /**
     * Get the delay-corrected state of a TI timestamp based on the generator,
     * its seed timestamp, and the expected helicity window frequency, returning
     * HelicityBit.UDF if generator cannot be initialized or timestamp is before
     * the generator was initialized.
     * 
     * @param timestamp TI-timestamp (i.e. RUN::config.timestamp)
     * @return the helicity bit
     */
    @Override
    public HelicityBit predictGenerated(long timestamp) {
        final int n=super.predictIndex(timestamp);
        if (n<0) return HelicityBit.UDF;
        return this.getGenerated(n);
    }
    
    /**
     * Get the delay-corrected state of a TI timestamp by searching the measured
     * sequence to count states and using the generator to calculate the helicity,
     * returning HelicityBit.UDF if generator cannot be initialized or timestamp
     * is before the generator was initialized or after the measured sequence.
     * 
     * @param timestamp TI-timestamp (i.e. RUN::config.timestamp)
     * @return the helicity bit
     */
    public HelicityBit searchGenerated(long timestamp) {
        final int n=super.searchIndex(timestamp);
        if (n<0) return HelicityBit.UDF;
        return this.getGenerated(n);
    }
  
    @Override
    public void show() {
        System.out.println("GENERATORTIMESTAMP:  "+this.generator.getTimestamp());
        HelicityState prev=super.getState(0);
        for (int ii=0; ii<this.size(); ii++) {
            if (super.getState(ii).getPatternSync()==HelicityBit.PLUS) continue;
            //System.out.print(String.format("%4d %6s %6s %6s",
            //        ii,
            //        super.getState(ii).getInfo(prev,ii),
            //        super.getState(ii).getHelicity(),
            //        super.getGenerated(ii)));
            final long ts=super.getState(ii).getTimestamp()+1;
            System.out.print(String.format("%4d %6s %6s %6s",
                    ii,
                    this.getState(ii).getInfo(prev,ii),
                    this.search(ts),
                    this.searchGenerated(ts)));
            if (super.getState(ii).getSwStatus()!=0) {
                System.out.print("   "+String.format("0x%04x",super.getState(ii).getSwStatus()));
            }
            System.out.println();
            prev=super.getState(ii);
        }
    }
}
