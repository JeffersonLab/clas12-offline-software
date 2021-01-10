package org.jlab.detector.helicity;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
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
class HelicitySequence {

    public static final double TIMESTAMP_CLOCK=250.0e6; // Hz
    protected double helicityClock=29.56; // Hz
    protected HelicityPattern pattern=HelicityPattern.QUARTET;
    protected boolean halfWavePlate=false;
    protected boolean analyzed=false;
    protected final Map<Long,HelicityGenerator> generators=new HashMap<>();
    protected final HelicityGenerator generator=new HelicityGenerator();
    protected final List<HelicityState> states=new ArrayList<>();
    protected int verbosity=0;

    public HelicitySequence(){}

    public void setVerbosity(int verbosity) {
        this.verbosity=verbosity;
        this.generator.setVerbosity(verbosity);
    }
   
    public boolean getHalfWavePlate() {
        return this.halfWavePlate;
    }

    /**
     * Get the the number of states in the sequence.
     * @return the number of states
     */
    public final int size() { return this.states.size(); }

    /**
     * Add a state to the sequence, unless the same timestamp already exists
     * or it has undefined bits, and keep them ordered by timestamp.  Note,
     * these {@link HelicityState}s do not have to be added in order, as they
     * will be automatically ordered by timestamp upon insertion.
     * @param state the state to add
     * @return whether the state was added
     */
    public final boolean addState(HelicityState state) {
        
        if (!state.isValid()) return false;
        
        if (this.verbosity>3) {
            System.out.println("HelicitySequence:  adding state:  "+state);
        }

        // terminate if trying to add more than one run number:
        for (HelicityState hs : this.states) {
            if (hs.getRun()!=state.getRun()) {
                throw new RuntimeException("Run number mismatch:  "+state.getRun()+"/"+state.getRun());
            }
        }
        
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
            final int index=Collections.binarySearch(this.states,state,new HelicityState());
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
     * @param firstBit first helicity state in the quartet
     * @param bitIndex index of the state to retrieve (0/1/2/3)
     * @return the requested bit
     */
    public final static HelicityBit getBitInQuartet(HelicityBit firstBit, int bitIndex) {
        final int a = firstBit==HelicityBit.PLUS ? 1 : 0;
        final int b = ((bitIndex+1)/2)%2 ^ a;
        return b==1 ? HelicityBit.PLUS : HelicityBit.MINUS;
    }

    /**
     * Get the state index of a TI timestamp, based on binary search
     * within the measured sequence.
     * This returns invalid (-1) if the timestamp is not contained within
     * the range of measured states.
     * @param timestamp TI timestamp (i.e. RUN::config.timestamp)
     * @return index
     */
    protected final int searchIndex(long timestamp) {
        if (!this.analyzed) this.analyze();
        if (timestamp < this.getTimestamp(0)) return -1;
        if (timestamp > this.getTimestamp(this.size()-1)) return -1;
        // make a fake state for timestamp search:
        HelicityState state=new HelicityState();
        state.setTimestamp(timestamp);
        final int index=Collections.binarySearch(this.states,state,new HelicityState());
        final int n = index<0 ? -index-2 : index;
        return n;
    }
   
    /**
     * Get the state index of a TI timestamp, based only on the first measured
     * state's timestamp and the helicity periodicity.
     * This returns invalid (-1) if the timestamp is before the measured states.
     * @param timestamp
     * @return index
     */
    public int predictIndex(long timestamp) {
        if (!this.analyzed) this.analyze();
        if (!this.generator.initialized()) return -1;
        if (timestamp < this.generator.getTimestamp()) return -1;
        final int n = (int) ( (timestamp-this.generator.getTimestamp()) /
                TIMESTAMP_CLOCK * this.helicityClock );
        return n+this.generator.getOffset();
    }
   
    /**
     * Get the nth state in the measured sequence.
     * @param n the index of the state, where 0 corresponds to the first state
     * @return the helicity state, null if outside the mesaured range
     */
    protected HelicityState getState(int n) {
        if (!this.analyzed) this.analyze();
        if (n < 0 || n>this.states.size()-1) return null;
        return this.states.get(n);
    }

    /**
     * Find the state corresponding to a given timestamp in the measured sequence.
     * @param timestamp TI timestamp (i.e. RUN::config.timestamp)
     * @return the helicity state, null if timestamp is outside of measured range
     */
    protected HelicityState searchState(long timestamp) {
        final int index = this.searchIndex(timestamp);
        if (index < 0) return null;
        return this.getState(index);
    }

    /**
     * Get the nth state in the measured sequence.
     * @param n the index of the state, where 0 corresponds to the first state
     * @return the helicity state, HelicityBit.UDF if outside the mesaured range
     */
    protected HelicityBit get(int n) {
        HelicityState state = this.getState(n);
        if (state==null) return HelicityBit.UDF;
        else return state.getHelicity();
    }

    /**
     * Find the state corresponding to a given timestamp in the measured sequence.
     * @param timestamp TI timestamp (i.e. RUN::config.timestamp)
     * @return the helicity state, null if timestamp is outside of measured range
     */
    public HelicityBit search(long timestamp) {
        return this.search(timestamp,0);
    }

    /**
     * Find the state corresponding to a given timestamp in the measured sequence.
     * @param timestamp TI timestamp (i.e. RUN::config.timestamp)
     * @param offset number of states offset
     * @return the helicity state, null if timestamp is outside of measured range
     */
    public HelicityBit search(long timestamp,int offset) {
        final int index = this.searchIndex(timestamp)+offset;
        if (index < 0) return HelicityBit.UDF;
        else return this.getState(index).getHelicity();
    }

    /**
     * Predict the nth state in the sequence.
     * 
     * This uses the pseudo-random sequence of the helicity hardware to
     * generate the sequence into the infinite future and requires that enough
     * states were provided to initialize it.  Returns null if generator cannot
     * be initialized or the state is before the measured ones (i.e. negative).
     * 
     * @param n the index of the state
     * @return the helicity bit
     */
    protected HelicityBit getGenerated(int n) {
        if (!this.analyzed) this.analyze();
        if (!this.generator.initialized()) return HelicityBit.UDF;
        if (n-this.generator.getOffset()<0) return HelicityBit.UDF;

        // Generator only knows about first states in a pattern (e.g. quartets),
        // so get it and then calculate here within that pattern.
        // FIXME:  here we assume the helicity board is in QUARTET configuration.
        final int nQuartet = (n-this.generator.getOffset())/4;
        final int nBitInQuartet = (n-this.generator.getOffset())%4;
        HelicityBit firstBitInQuartet = this.generator.get(nQuartet);
        HelicityBit bit = getBitInQuartet(firstBitInQuartet,nBitInQuartet);
        
        // the generator operates on the raw states, so flip it if the HWP is in:
        // (or should we leave it to the user code to flip it properly?)
        if (this.halfWavePlate) bit=HelicityBit.getFlipped(bit);
        return bit;
    }

    /**
     * Predict the state of a TI timestamp.
     * 
     * This uses the pseudo-random sequence of the helicity hardware to
     * generate the sequence into the infinite future and requires that enough
     * states were provided to initialize it.  Returns null if generator cannot
     * be initialized or timestamp is before the generator timestamp.
     * 
     * @param timestamp TI timestamp (i.e. RUN::config.timestamp)
     * @return the helicity bit
     */
    public HelicityBit predictGenerated(long timestamp) {
        final int n=this.predictIndex(timestamp);
        if (n<0) return HelicityBit.UDF;
        return this.getGenerated(n);
        /*
        if (timestamp < this.getTimestamp(0)) return null;
        if (timestamp <= this.getTimestamp(this.size()-1)) {
            // it's in the measured range, so search index based on timestamp:
            return this.getGenerated(this.findIndex(timestamp));
        }
        else {
            // here we predict past the measured sequence,
            // assuming the helicity clock frequency and the
            // first measured timestamp in the sequence:
            // FIXME:  use the latest available measured timestamp,
            //         or an average
            final int n = (int) ( (timestamp-this.getTimestamp(0)) /
                    TIMESTAMP_CLOCK * this.helicityClock );
            return this.getGenerated(n);
        }
        */
    }

    /**
     * Get whether the pseudo-random generator is initialized.  This must be
     * true before calling the predict methods, because they require a working
     * generator.  The number of valid states required to intialize the generator
     * is {@link HelicityGenerator.REGISTER_SIZE}.
     * 
     * @return whether initialized 
     */
    public boolean initialized() {
        if (!this.analyzed) this.analyze();
        return this.generator.initialized();
    }
    
    public void show() {
        HelicityState prev=this.getState(0);
        for (int ii=0; ii<this.size(); ii++) {
            if (this.getState(ii).getPatternSync()==HelicityBit.PLUS) continue;
            System.out.println(String.format("%4d %6s %6s %6s",
                    ii,
                    this.getState(ii).getInfo(prev,ii),
                    this.getState(ii).getHelicity(),
                    this.getGenerated(ii)));
            prev=this.getState(ii);
        }
    }

    /**
     * Get the TI-timestamp of a state in the sequence.
     * @param index the index of the state
     * @return the timestamp of the state
     */
    public long getTimestamp(int index) {
        return this.states.get(index).getTimestamp();
    }

    /**
     * Reject false flips, e.g. in between files if decoding files singly.
     */
    private int rejectFalseFlips() {

        // always reject the first state in the sequence, since it was
        // triggered by the first available readout and (usually) not
        // on an actual state change, so it's timestamp is invalid:
        if (this.states.size()>0) {
            this.states.remove(0);
        }
        int nRejects=0;
        while (true) {
            boolean rejection=false;
            for (int ii=0; ii<this.states.size()-3; ii++) {
                final double dt01 = (this.getTimestamp(ii+1)-this.getTimestamp(ii+0))/TIMESTAMP_CLOCK;
                final double dt12 = (this.getTimestamp(ii+2)-this.getTimestamp(ii+1))/TIMESTAMP_CLOCK;
                if (Math.abs(dt01+dt12-1./this.helicityClock) < 0.3/this.helicityClock) {
                    this.states.remove(ii+1);
                    rejection=true;
                    nRejects++;
                    break;
                }
            }
            if (!rejection) break;
        }
        return nRejects;
    }
    
    /**
     * Analyze the sequence, prune false states, initialize the generator.
     * @return sequence integrity
     */
    protected final boolean analyze() {

        if (verbosity>0) {
            System.out.println("HelicitySequence:  Analyzing ....");
        }

        final int nRejects=this.rejectFalseFlips();
        if (verbosity>0) {
            System.out.println("HelicitySequence:  Rejected false flips:  "+nRejects);
        }

        if (this.states.size()>0) {
            // just use first state to determine whether HWP is in:
            this.halfWavePlate = this.states.get(0).getHelicity().value() !=
                                 this.states.get(0).getHelicityRaw().value();
            if (verbosity>1) {
                System.out.println("HelicitySequnce:  HWP: "+this.halfWavePlate);
            }
        }

        this.analyzed=true;

        final boolean integrity=this.integrityCheck();

        final boolean geninit=this.generator.initialize(this.states);

        if (geninit) {
            this.generators.put(this.generator.getTimestamp(), this.generator);
        }

        return integrity && geninit; 
    }

    /**
     * Perform integrity checking on the sequence.
     * @return whether the integrity checking succeeded
     */
    public final boolean integrityCheck() {

        int hwpErrors=0;
        int syncErrors=0;
        int quartetErrors=0;
        int bigGapErrors=0;
        int smallGapErrors=0;

        for (int ii=1; ii<this.states.size(); ii++) {

            // check that HWP is consistent across all states:
            if (this.states.get(ii).getHelicity().value()*this.states.get(ii).getHelicityRaw().value() !=
                this.states.get(ii-1).getHelicity().value()*this.states.get(ii-1).getHelicityRaw().value()) {
                hwpErrors++;
                if (verbosity>1) System.err.println("ERROR:  HelicitySequence HWP: "+ii);
            }
            
            // check if neighboring syncs are the same (they shouldn't be):
            if (this.states.get(ii).getPairSync().value() == this.states.get(ii-1).getPairSync().value()) {
                syncErrors++;
                this.states.get(ii).addSwStatusMask(HelicityState.Mask.SYNC);
                if (verbosity>1) System.err.println("ERROR: HelicitySequence SYNC: "+ii);
            }

            // check if quartet sequence is broken (should be 1minus + 3plus):
            if (ii > 2) {
                if (this.states.get(ii-0).getPatternSync().value()+
                    this.states.get(ii-1).getPatternSync().value()+
                    this.states.get(ii-2).getPatternSync().value()+
                    this.states.get(ii-3).getPatternSync().value() != 2) {
                    quartetErrors++;
                    this.states.get(ii).addSwStatusMask(HelicityState.Mask.PATTERN);
                    if (verbosity>1) System.err.println("ERROR:  HelicitySequence QUARTET: "+ii);
                }
            }

            // check timestamp deltas:
            final double seconds = (this.getTimestamp(ii)-this.getTimestamp(ii-1))/TIMESTAMP_CLOCK;
            if (seconds < (1.0-0.5)/this.helicityClock) {
                smallGapErrors++;
                this.states.get(ii).addSwStatusMask(HelicityState.Mask.SMALLGAP);
                if (verbosity>1) System.err.println("ERROR:  HelicitySequence TIMESTAMP: "+ii+" "+
                        this.getTimestamp(ii)+" "+this.getTimestamp(ii-1)+" "+seconds+"s");
            }
            else if (seconds > (1.0+0.5)/this.helicityClock) {
                bigGapErrors++;
                this.states.get(ii).addSwStatusMask(HelicityState.Mask.BIGGAP);
                if (verbosity>1) System.err.println("ERROR:  HelicitySequence TIMESTAMP: "+ii+" "+
                        this.getTimestamp(ii)+" "+this.getTimestamp(ii-1)+" "+seconds+"s");
            }
        }

        // compare with generator:
        int generatorErrors=0;
        if (this.generator.initialized()) {
            for (int ii=0; ii<this.states.size(); ii++) {
                HelicityBit g=this.getGenerated(ii);
                if (g!=null && g!=this.states.get(ii).getHelicity()) {
                    generatorErrors++;
                }
            }
        }

        if (verbosity>0) {
            System.out.println("HelicitySequence:  HWP       ERRORS:  "+hwpErrors);
            System.out.println("HelicitySequence:  SYNC      ERRORS:  "+syncErrors);
            System.out.println("HelicitySequence:  QUARTET   ERRORS:  "+quartetErrors);
            System.out.println("HelicitySequence:  BIGGAP    ERRORS:  "+bigGapErrors);
            System.out.println("HelicitySequence:  SMALLGAP  ERRORS:  "+smallGapErrors);
            System.out.println("HelicitySequence:  GENERATOR ERRORS:  "+generatorErrors);
        }

        return (hwpErrors+syncErrors+quartetErrors+bigGapErrors+smallGapErrors+generatorErrors) == 0;
    }

    public void initialize(HipoReader reader) {
        SchemaFactory schema = reader.getSchemaFactory();
        while (reader.hasNext()) {
            Event event=new Event();
            Bank flipBank=new Bank(schema.getSchema("HEL::flip"));
            reader.nextEvent(event);
            event.read(flipBank);
            if (flipBank.getRows()<1) continue;
            this.addState(HelicityState.createFromFlipBank(flipBank));
        }
    }

    public void initialize(List<String> filenames) {
        for (String filename : filenames) {
            HipoReader reader = new HipoReader();
            reader.setTags(1);
            reader.open(filename);
            initialize(reader);
        }
    }
}
