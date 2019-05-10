package org.jlab.detector.helicity;

import java.util.List;
import java.util.ArrayList;

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
public final class HelicityGenerator {

    public static final int REGISTER_SIZE=30;
    private final List<Integer> states=new ArrayList<>();
    private int register=0;

    public HelicityGenerator(){}

    public void reset() {
        this.states.clear();
        this.register=0;
    }

    public int size() {
        return this.states.size();
    }

    public boolean initialized() {
        return this.states.size() >= REGISTER_SIZE;
    }

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
        final int nextBit = this.getNextBit();
        this.shiftRegister(nextBit);
    }

    /**
     * Let the user add a state to initialize the sequence.
     * 
     * This must be the first helicity state in the next pattern.
     * 
     * Requires initialized()==false and a defined HelicityBit.
     *
     * @param bit = the HelicityBit to add to the sequence.  This must
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
     * This must be the first helicity state in the next pattern.
     * 
     * Requires initialized()==false and a defined HelicityState.
     *
     * @param state = the HelicityState to add to the sequence.
     */
    public void addState(HelicityState state) {
        this.addState(state.getHelicityRaw());
    }
    
    /**
     * Get the nth state in the sequence.
     * Requires initialized()==true.
     *
     * @param n = number of states after the first one.
     * @return the nth HelicityBit in the sequence.
     */
    public HelicityBit getState(final int n) {
        if (!this.initialized())
            throw new RuntimeException("Not initialized.");
        if (n < 0)
            throw new RuntimeException("Invalid argument.");
        while (this.states.size() <= n)
            this.shiftRegister();
        return this.states.get(n) == 0 ?
            HelicityBit.PLUS : HelicityBit.MINUS;
    }

    public static void main(String[] args) {
        HelicityGenerator hg=new HelicityGenerator();
        // invent and load some pattern:
        while (!hg.initialized()) {
            hg.addState(HelicityBit.MINUS);
            hg.addState(HelicityBit.PLUS);
            if (hg.initialized()) break;
            hg.addState(HelicityBit.PLUS);
            hg.addState(HelicityBit.MINUS);
            if (hg.initialized()) break;
            hg.addState(HelicityBit.PLUS);
            hg.addState(HelicityBit.MINUS);
            if (hg.initialized()) break;
            hg.addState(HelicityBit.MINUS);
            hg.addState(HelicityBit.PLUS);
        }
        for (int ii=0; ii<50; ii++) {
            System.out.println(ii+" "+hg.getState(ii));
        }
    }

}

