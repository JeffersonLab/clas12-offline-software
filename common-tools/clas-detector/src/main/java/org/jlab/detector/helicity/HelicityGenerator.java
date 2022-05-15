package org.jlab.detector.helicity;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helicity Pseudo-Random Sequence.
 *
 * This calculates the first helicity state in each pattern (quartet/octet/toggle),
 * according to the specs here:
 * 
 * https://hallaweb.jlab.org/equipment/daq/HelicityUsersGuideFeb4.pdf
 *
 * Note, the convention above, and internally in this class, is HIGH=0=+ and LOW=1=-.
 *
 * Note, this pseudorandom generator operates on raw helicity, and does not produce
 * the same sequence if you just reverse all the states, so the HWP-corrected
 * helicity cannot be used here.
 * 
 * User calls addState() until initialized()==true, then getState().
 *
 * Public interface requires HelicityBit (not integers).
 *
 * @author baltzell
 */
public final class HelicityGenerator implements Comparable<HelicityGenerator>, Comparator<HelicityGenerator> {

    static final Logger LOGGER = Logger.getLogger(HelicityGenerator.class.getName());
    public static final int REGISTER_SIZE=30;
    private final List<Integer> states=new ArrayList<>();
    private int offset=0;
    private int register=0;
    private long timestamp=0;
    private double clock=29.56; // Hz

    public HelicityGenerator(){}

    public void setClock(double clock){this.clock=clock;}

    /**
     * Get the next bit in the sequence.
     * Requires initialized()==true.
     */
    private int getNextBit() {
        final int bit7  = (this.register>>6)  & 0x1;
        final int bit28 = (this.register>>27) & 0x1;
        final int bit29 = (this.register>>28) & 0x1;
        final int bit30 = (this.register>>29) & 0x1;
        final int nextBit = (bit30^bit29^bit28^bit7) & 0x1;
        return nextBit;
    }

    @Override
    public int compareTo(HelicityGenerator other) {
        if (this.getTimestamp() < other.getTimestamp()) return -1;
        if (this.getTimestamp() > other.getTimestamp()) return +1;
        return 0;
    }
    
    @Override
    public int compare(HelicityGenerator o1, HelicityGenerator o2) {
        return o1.compareTo(o2);
    }
    
    /**
     * Get the timestamp of the first state in the generator sequence.
     * @return timestamp (4ns)
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Shift the register with the next state.
     * Requires initialized()==false.
     */
    private void shiftRegister(int state) {
        this.states.add(state);
        this.register = ( state |  (this.register<<1) ) & 0x3FFFFFFF;
    }

    /**
     * Shift the register with the next state.
     * Requires initialized()==true.
     */
    private void shiftRegister() {
        this.shiftRegister(this.getNextBit());
    }

    /**
     * Reset the generator, clearing all added states, in order to
     * reuse this {@link HelicityGenerator} for a new sequence.
     */
    public void reset() {
        this.states.clear();
        this.register=0;
        this.timestamp=-1;
    }

    /**
     * Test whether the generator is sufficiently initialized such that
     * {@link #get(int)} method can be called, based on whether the
     * number of added states is at least {@link REGISTER_SIZE}.
     *
     * @return whether the sequence is initialized
     */
    public boolean initialized() {
        return this.states.size() >= REGISTER_SIZE;
    }

    /**
     * Get the number of states currently in the generator's sequence.
     * 
     * @return size 
     */
    public int size() {
        return this.states.size();
    }
    
    /**
     * Let the user add a state to initialize the sequence.
     * 
     * This must be the first helicity state in the next pattern.  States
     * must be added serially and without skipped patterns.  Requires 
     * {@link initialized} is false and a defined HelicityBit, else throws
     * an exception..
     *
     * @param bit the HelicityBit to add to the sequence.  This must
     * be the raw helicity, e.g. HelicityState.getHelicityRaw(), not the
     * HWP-corrected version.
     */
    public void addState(HelicityBit bit) {
        if (bit==HelicityBit.UDF)
            throw new RuntimeException("Helicity state undefined.");
        if (this.initialized())
            throw new RuntimeException("Already initialized");
        this.shiftRegister(bit==HelicityBit.PLUS?0:1);
    }

    /**
     * Let the user add a state to initialize the sequence.
     * 
     * This just calls {@link #addState(HelicityBit)} with the raw
     * {@link #HelicityBit} from the given {@link #HelicityState}.
     *
     * This must be the first helicity state in the next pattern.  States
     * must be added serially and without skipped patterns.  Requires
     * {@link initialized} is false and a defined {@link #HelicityState}.
     * 
     * @param state the HelicityState to add to the sequence.
     */
    public void addState(HelicityState state) {
        this.addState(state.getHelicityRaw());
    }
   
    /**
     * Get the nth state in the sequence.
     * Requires initialized()==true.
     *
     * @param n number of states after the first one.
     * @return the nth HelicityBit in the sequence.
     */
    public HelicityBit get(final int n) {
        if (!this.initialized())
            throw new RuntimeException("Not initialized.");
        if (n < 0)
            throw new RuntimeException("Invalid argument.");
        while (this.states.size() <= n)
            this.shiftRegister();
        return this.states.get(n) == 0 ?
            HelicityBit.PLUS : HelicityBit.MINUS;
    }
    
   
    /**
     * Initialize with a list of states.
     * 
     * The states are first time-ordered, and error-checking is done to find the
     * first valid sequence of sufficient length in the list and use it to
     * initialize the generator, otherwise the return value will be false.
     * @param states list of HelicityState objects
     * @return success of initializing the generator 
     */
    public final boolean initialize(List<HelicityState> states) {

        LOGGER.log(Level.INFO, "HelicityGenerator:  Initializing with {0} states ...", states.size());
       
        // make sure they're time-ordered:
        Collections.sort(states);
        
        for (HelicityState state : states) {
            LOGGER.log(Level.FINEST, "initializing with: {0}", state);
        }
        
        // reset this generator:
        this.reset();
      
        // these will be the first valid sequence found in the input states:
        List<Integer> iStates=new ArrayList<>();

        for (int iState=0; iState<states.size(); iState++) {
            
            HelicityState thisState=states.get(iState);

            // any initial state will do:
            if (iStates.isEmpty()) {
                LOGGER.log(Level.FINE, "got first state: {0}", thisState);
                iStates.add(iState);
            }

            else {

                HelicityState prevState = states.get(iStates.get(iStates.size()-1));
            
                // bad pair sync, reset the sequence:
                if (thisState.getPairSync() == prevState.getPairSync()) {
                    LOGGER.log(Level.INFO, "got bad pair, resetting... {0} / {1}", new Object[]{prevState, thisState});
                    iStates.clear();
                }

                // bad pattern sync, reset the sequence:
                // FIXME: we assume quartet here
                else if (iStates.size() > 2 &&
                        thisState.getPatternSync().value()+
                        prevState.getPatternSync().value()+
                        states.get(iStates.get(iStates.size()-2)).getPatternSync().value()+
                        states.get(iStates.get(iStates.size()-3)).getPatternSync().value() != 2 ){
                    LOGGER.log(Level.INFO, "got bad pattern, resetting... {0}", thisState);
                    iStates.clear();
                }
           
                else {

                    // get time difference between states:
                    final double seconds = (thisState.getTimestamp() -
                            prevState.getTimestamp()) / HelicitySequence.TIMESTAMP_CLOCK;
                
                    // bad timestamp delta, reset the sequence:
                    if (seconds < (1.0-0.5)/this.clock ||
                        seconds > (1.0+0.5)/this.clock) {
                        LOGGER.log(Level.INFO,"HelicityGenerator:  got bad timestamp, resetting... ");
                        iStates.clear();
                    }

                    // passed all checks, add the state:
                    else {
                        iStates.add(iState);
                    }
           
                    // we got enough states, stop looking for more:
                    // FIXME: we assume quartet here
                    if (iStates.size() >= REGISTER_SIZE*4+1) {
                        break;
                    }
                }
            }
        }

        // ok, we got enough to initialize the generator:
        // FIXME: we assume quartet here
        if (iStates.size() >= REGISTER_SIZE*4+1) {
            
            // this will be the index of the first state
            this.offset = -1;

            // store all states timestamps to apply a modulo correction
            List <Double> timestamps = new ArrayList<>();
            List <Double> timestampsRaw = new ArrayList<>();
            
            for (int jj=0; jj<iStates.size(); jj++) {

                HelicityState state=states.get(iStates.get(jj));

                // we've got enough valid, consecutive states, so correct
                // the first state's timestamp and stop the investigation:
                if (this.initialized()) {
                    this.timestamp=0;
                    for (int kk=0; kk<timestamps.size(); kk++) {
                        this.timestamp += timestamps.get(kk);
                    }
                    this.timestamp /= timestamps.size();
                    LOGGER.log(Level.INFO, "raw timestamps:  {0}", timestampsRaw);
                    LOGGER.log(Level.INFO, "timestamps:      {0}", timestamps);
                    LOGGER.log(Level.INFO, "modulo-corrected timestamp:  {0}", this.timestamp);
                    break;
                }

                // first state in the pattern, add it to the generator:
                if (state.getPatternSync() == HelicityBit.MINUS) {
                    if (this.size() == 0) {
                        this.offset = jj;
                    }
                    this.addState(state);
                }

                // subtract off the nominal flip period:
                if (this.size() > 0) {
                    long timeStamp=state.getTimestamp();
                    double corr=(jj-this.offset)/this.clock*HelicitySequence.TIMESTAMP_CLOCK;
                    timestamps.add(timeStamp-corr);
                    timestampsRaw.add((double)timeStamp);
                    LOGGER.info(String.format("HelicityGenerator:  timestamp = %d/%.1f/%.2f",
                                timeStamp,corr,timeStamp-corr));
                }
            }
        }

        if (!this.initialized()) {
            System.err.println("HelicityGenerator:  Initialization Error.");
            this.reset();
        }
        LOGGER.fine("HelicityGenerator:  Initialized.");

        return this.initialized();
    }
    
    public int getOffset() {
        return this.offset;
    }
    
}
