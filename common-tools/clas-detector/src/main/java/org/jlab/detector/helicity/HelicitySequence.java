package org.jlab.detector.helicity;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;
import org.jlab.io.hipo.HipoDataSource;

/**
 * Stores a sequence of helicity states and provides timestamp- or state-count-
 * based lookup of helicity state based on the measured sequence or pseudo-random
 * generator sequence, and provides some integrity checking of the sequence, 
 * including comparing the measured and generator sequences.
 * 
 * - timestamp-based lookup is denoted by "find" in method names.
 * - count-based lookup is denoted by "get" in method names.
 * - generator-based lookup is denoted by "predict" in method names (although it's 
 *   only actually predicting if the timestamp/count is past the measured range).
 *
 * The generator gives HelicityBit, while measured sequence gives HelicityState.
 * The former includes only the helicity signal, while the latter adds pair- and
 * pattern-sync signals, timestamps, and run and event numbers.
 *
 * @author baltzell
 */
public class HelicitySequence implements Comparator<HelicityState> {

    // FIXME:  these should go to CCDB:
    public static final double TIMESTAMP_CLOCK=250.0e6; // Hz
    public static final double HELICITY_CLOCK=29.56; // Hz

    private boolean analyzed=false;
    private final List<HelicityState> states=new ArrayList<>();
    private final HelicityGenerator generator=new HelicityGenerator();
    private int generatorOffset=0;
    private final int debug=0;

    public HelicitySequence(){}

    /**
     * Compare based on timestamp for sorting and List insertion.
     * @param o1
     * @param o2
     * @return negative/positive of o1 is before/after o2, else zero.
     */
    @Override
    public int compare(HelicityState o1, HelicityState o2) {
        if (o1.getTimestamp() < o2.getTimestamp()) return -1;
        if (o1.getTimestamp() > o2.getTimestamp()) return +1;
        return 0;
    }

    /**
     * Get the the number of states in the sequence.
     * @return the number of states
     */
    public final int size() { return this.states.size(); }

    /**
     * Add a state to the sequence, unless the same timestamp already exists
     * or it has undefined bits, and keep them ordered by timestamp.
     * @param state = the state to add
     * @return whether the state was added
     */
    public final boolean addState(HelicityState state) {
        
        if (!state.isValid()) return false;
        
        // mark that we'll need to redo the analysis:
        this.analyzed=false;
        
        // FIXME:  should we be using a SortedSet instead?
        // Looks like SortedList doesn't exist until java 10,
        // and SortedSet doesn't give easy access by index?
        // Meanwhile, we manually enforce the insertion order here.
        if (this.states.isEmpty()) {
            this.states.add(state);
            return true;
        }
        else {
            final int index=Collections.binarySearch(this.states,state,new HelicitySequence());
            if (index==this.states.size()) {
                // its timestamp is later than the existing sequence:
                this.states.add(state);
                return true;
            }
            else if (index<0) {
                // it's a unique timestamp, insert it:
                this.states.add(-index-1,state);
                return true;
            }
            else {
                // it's a duplicate timestamp, ignore it:
                return false;
            }
        }
    }

    /**
     * Given the first helicity state in a quartet, get another bit in that quartet.
     * @param firstBit = first helicity state in the quartet
     * @param bitIndex = index of the state to retrieve (0/1/2/3)
     * @return the requested bit
     */
    public final static HelicityBit getBitInQuartet(HelicityBit firstBit, int bitIndex) {
        final int a = firstBit==HelicityBit.PLUS ? 1 : 0;
        final int b = ((bitIndex+1)/2)%2 ^ a;
        return b==1 ? HelicityBit.PLUS : HelicityBit.MINUS;
    }

    /**
     * Get the state index of a TI timestamp.
     * This returns invalid (-1) if the timestamp is not in the range of measured states.
     * @param timestamp
     * @return index
     */
    protected final int findIndex(long timestamp) {
        if (!this.analyzed) this.analyze();
        if (timestamp < this.getTimestamp(0)) return -1;
        if (timestamp > this.getTimestamp(this.size()-1)) return -1;
        // make a fake state for timestamp search:
        HelicityState state=new HelicityState();
        state.setTimestamp(timestamp);
        final int index=Collections.binarySearch(this.states,state,new HelicitySequence());
        return index<0 ? -index-2 : index;
    }

    /**
     * Get the nth state in the measured sequence.
     * @param n = the index of the state, where 0 corresponds to the first state
     * @return the helicity state, null if outside the mesaured range
     */
    public HelicityState get(int n) {
        if (!this.analyzed) this.analyze();
        if (n < 0 || n>this.states.size()-1) return null;
        return this.states.get(n);
    }

    /**
     * Find the state corresponding to a given timestamp in the measured sequence.
     * @param timestamp = TI timestamp (units = 4 ns)
     * @return the helicity state, null if timestamp is outside of measured range
     */
    public HelicityState find(long timestamp) {
        final int index = this.findIndex(timestamp);
        if (index < 0) return null;
        return this.get(index);
    }

    /**
     * Predict the nth state in the sequence.
     * 
     * This uses the pseudo-random sequence of the helicity hardware to
     * generate the sequence into the infinite future and requires that enough
     * states were provided to initialize it.  Returns null if generator cannot
     * be initialized or the state is before the measured ones (i.e. negative).
     * 
     * @param n = the index of the state
     * @return the helicity bit
     */
    public HelicityBit getPrediction(int n) {
        if (!this.analyzed) this.analyze();
        if (!this.generator.initialized()) return null;
        if (n-this.generatorOffset<0) return null;

        // Generator only knows about first states in a pattern (e.g. quartets),
        // so get it and then calculate here within that pattern.
        // FIXME:  here we assume the helicity board is in QUARTET configuration.
        final int nQuartet = (n-this.generatorOffset)/4;
        final int nBitInQuartet = (n-this.generatorOffset)%4;
        HelicityBit firstBitInQuartet = this.generator.getState(nQuartet);
        return HelicitySequence.getBitInQuartet(firstBitInQuartet,nBitInQuartet);
    }

    /**
     * Predict the state of a TI timestamp.
     * 
     * This uses the pseudo-random sequence of the helicity hardware to
     * generate the sequence into the infinite future and requires that enough
     * states were provided to initialize it.  Returns null if generator cannot
     * be initialized or timestamp is before the measured ones.
     * 
     * @param timestamp = TI timestamp (units = 4 ns)
     * @return the helicity bit
     */
    public HelicityBit findPrediction(long timestamp) {
        if (!this.analyzed) this.analyze();
        if (timestamp < this.getTimestamp(0)) return null;
        if (timestamp <= this.getTimestamp(this.size()-1)) {
            // it's in the measured range, so lookup index based on timestamp:
            return this.getPrediction(this.findIndex(timestamp));
        }
        else {
            // here we predict past the measured sequence,
            // assuming the helicity clock frequency and the
            // first measured timestamp in the sequence:
            // FIXME:  use the latest available measured timestamp,
            //         or an average
            final int n = (int) ( (timestamp-this.getTimestamp(0)) /
                    TIMESTAMP_CLOCK * HELICITY_CLOCK );
            return this.getPrediction(n);
        }
    }

    public void show() {
        HelicityState prev=this.states.get(0);
        for (int ii=0; ii<this.states.size(); ii++) {
            if (this.states.get(ii).getQuartet()==HelicityBit.PLUS) continue;
            System.out.println(String.format("%4d %6s %6s %6s",
                    ii,
                    this.get(ii).getInfo(prev,ii),
                    this.get(ii).getHelicity(),
                    this.getPrediction(ii)));
            prev=this.states.get(ii);
        }
    }

    /**
     * Get the timestamp of a state in the sequence.
     * @param index = the index of the state
     * @return the timestamp of the state
     */
    private long getTimestamp(int index) {
        return this.states.get(index).getTimestamp();
    }

    /**
     * Reject false flips, e.g. in between files if decoding files singly.
     */
    private void rejectFalseFlips() {
        while (true) {
            boolean rejection=false;
            for (int ii=0; ii<this.states.size()-3; ii++) {
                final double dt01 = (this.getTimestamp(ii+1)-this.getTimestamp(ii+0))/TIMESTAMP_CLOCK;
                final double dt12 = (this.getTimestamp(ii+2)-this.getTimestamp(ii+1))/TIMESTAMP_CLOCK;
                if (Math.abs(dt01+dt12-1./HELICITY_CLOCK) < 0.3/HELICITY_CLOCK) {
                    this.states.remove(ii+1);
                    rejection=true;
                    break;
                }
            }
            if (!rejection) break;
        }
    }

    /**
     * Analyze the sequence, prune false states, initialize the generator.
     * @return sequence integrity
     */
    protected final boolean analyze() {

        if (debug>1) System.out.println("ANALYZING ....");

        this.rejectFalseFlips();

        // initialize the generator:
        this.generator.reset();
        for (int ii=0; ii<this.states.size(); ii++) {
            if (!this.generator.initialized() &&
                 this.states.get(ii).getQuartet()==HelicityBit.MINUS) {
                if (this.generator.size()==0) {
                    this.generatorOffset=ii;
                }
                this.generator.addState(this.states.get(ii).getHelicity());
            }
        }

        this.analyzed=true;

        return this.integrityCheck();
    }

    /**
     * Perform integrity checking on the sequence.
     * @return whether the integrity checking succeeded
     */
    public final boolean integrityCheck() {

        int syncErrors=0;
        int quartetErrors=0;
        int timestampErrors=0;

        for (int ii=1; ii<this.states.size(); ii++) {

            // check if neighboring syncs are the same (they shouldn't be):
            if (this.states.get(ii).getSync().value() == this.states.get(ii-1).getSync().value()) {
                syncErrors++;
                if (debug>2) System.err.println("ERROR: HelicitySequence SYNC:"+ii);
            }

            // check if quartet sequence is broken (should be 1minus + 3plus):
            if (ii > 2) {
                if (this.states.get(ii-0).getQuartet().value()+
                    this.states.get(ii-1).getQuartet().value()+
                    this.states.get(ii-2).getQuartet().value()+
                    this.states.get(ii-3).getQuartet().value() != 2) {
                    quartetErrors++;
                    if (debug>2) System.err.println("ERROR:  HelicitySequence QUARTET: "+ii);
                }
            }

            // check timestamp deltas:
            final double seconds = (this.getTimestamp(ii)-this.getTimestamp(ii-1))/TIMESTAMP_CLOCK;
            if (seconds < (1.0-0.5)/HELICITY_CLOCK || seconds > (1.0+0.5)/HELICITY_CLOCK) {
                timestampErrors++;
                if (debug>2) System.err.println("ERROR:  HelicitySequence TIMESTAMP: "+ii+" "+
                        this.getTimestamp(ii)+" "+this.getTimestamp(ii-1));
            }
        }

        // compare with generator:
        int generatorErrors=0;
        if (this.generator.initialized()) {
            for (int ii=0; ii<this.states.size(); ii++) {
                HelicityBit g=this.getPrediction(ii);
                if (g!=null && g!=this.states.get(ii).getHelicity()) {
                    generatorErrors++;
                }
            }
        }

        if (debug>1) {
            System.out.println("SYNC      ERRORS:  "+syncErrors);
            System.out.println("QUARTET   ERRORS:  "+quartetErrors);
            System.out.println("TIMESTAMP ERRORS:  "+timestampErrors);
            System.out.println("GENERATOR ERRORS:  "+generatorErrors);
        }

        return (syncErrors + quartetErrors + timestampErrors + generatorErrors) == 0;
    }

    public static void main(String[] args) {

        HelicitySequence seq=new HelicitySequence();

        for (int ii=0; ii<4; ii++)
            System.out.println(HelicityBit.PLUS+" "+getBitInQuartet(HelicityBit.PLUS,ii));
        for (int ii=0; ii<4; ii++)
            System.out.println(HelicityBit.MINUS+" "+getBitInQuartet(HelicityBit.MINUS,ii));

        final String dir="/Users/baltzell/data/CLAS12/rg-b/decoded/";
        final String file="clas_006432.evio.00041-00042.hipo";

        HipoDataSource reader=new HipoDataSource();
        reader.open(dir+file);

        while (reader.hasEvent()) {
            DataEvent event = reader.getNextEvent();
            if (!event.hasBank("HEL::flip")) continue;
            DataBank bank=event.getBank("HEL::flip");
            HelicityState state=HelicityState.createFromFlipBank(bank);
            seq.addState(state);
        }

        seq.show();
    }
}
