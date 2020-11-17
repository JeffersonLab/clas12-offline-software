package org.jlab.detector.helicity;

import java.util.Comparator;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.jnp.hipo4.data.Bank;

import org.jlab.io.base.DataBank;

/**
 *  See:
 *  common-tools/clas-detector/doc
 *  https://logbooks.jlab.org/entry/3531353
 *
 * @author baltzell
 */
public class HelicityState implements Comparable<HelicityState>, Comparator<HelicityState> {

    public static class Mask {
        public static final int HELICITY =0x1;
        public static final int SYNC     =0x2;
        public static final int PATTERN  =0x4;
        public static final int BIGGAP   =0x8;
        public static final int SMALLGAP =0x10;
    }

    // FIXME:  these should go in CCDB
    private static final short HALFADC=2000;
    private static final byte SECTOR=1;
    private static final byte LAYER=1;
    private static final byte HELICITY_COMPONENT=1;
    private static final byte PAIRSYNC_COMPONENT=2;
    private static final byte PATTERNSYNC_COMPONENT=3;

    private HelicityBit helicityRaw = HelicityBit.UDF;
    private HelicityBit helicity    = HelicityBit.UDF;
    private HelicityBit pairSync    = HelicityBit.UDF;
    private HelicityBit patternSync = HelicityBit.UDF;
    private long timestamp   = 0;
    private int  event       = 0;
    private int  run         = 0;
    private byte hwStatus    = 0;
    private byte swStatus    = 0;

    public HelicityState(){}

    /**
     * Compare based on timestamp for sorting and List insertion.
     * @param o1
     * @param o2
     * @return negative/positive if o1 is before/after o2, else zero.
     */
    @Override
    public int compare(HelicityState o1, HelicityState o2) {
        return o1.compareTo(o2);
    }
    
    /**
     * Compare based on timestamp for sorting and List insertion.
     * @param other
     * @return negative/positive if this is before/after other, else zero.
     */
    @Override
    public int compareTo(HelicityState other) {
        if (this.getTimestamp() < other.getTimestamp()) return -1;
        if (this.getTimestamp() > other.getTimestamp()) return +1;
        return 0;
    }
    

    public void addSwStatusMask(int mask) {
        this.swStatus |= mask;
    }

    public int getSwStatus() {
        return this.swStatus;
    }
   
    public int getHwStatus() {
        return this.hwStatus;
    }
    
    private HelicityBit getFadcState(short ped) {
        if      (ped == HALFADC) return HelicityBit.UDF;
        else if (ped > HALFADC)  return HelicityBit.PLUS;
        else                     return HelicityBit.MINUS;
    }

    /**
     * Create a state from a HEL::adc org.jlab.jnp.hipo4.data.Bank
     * 
     * @param adcBank HEL::adc
     * @return state extracted from the bank
     */
    public static HelicityState createFromFadcBank(Bank adcBank) {
        HelicityState state=new HelicityState();
        for (int ii=0; ii<adcBank.getRows(); ii++) {
            if (adcBank.getInt("sector",ii) != SECTOR) continue;
            if (adcBank.getInt("layer",ii) != LAYER) continue;
            switch (adcBank.getInt("component",ii)) {
                case HELICITY_COMPONENT:
                    state.helicityRaw = state.getFadcState(adcBank.getShort("ped",ii));
                    break;
                case PAIRSYNC_COMPONENT:
                    state.pairSync = state.getFadcState(adcBank.getShort("ped",ii));
                    break;
                case PATTERNSYNC_COMPONENT:
                    state.patternSync = state.getFadcState(adcBank.getShort("ped",ii));
                    break;
                default:
                    break;
            }
        }

        state.hwStatus=0;
        if (state.helicityRaw==HelicityBit.UDF) state.hwStatus |= Mask.HELICITY;
        if (state.pairSync==HelicityBit.UDF)    state.hwStatus |= Mask.SYNC;
        if (state.patternSync==HelicityBit.UDF) state.hwStatus |= Mask.PATTERN;

        state.fixMissingReadouts();

        // Fix the overall sign-convention error in the offline helicity:
        state.invert();

        return state;
    }

    /**
     * Create a state from a HEL::flip org.jlab.jnp.hipo4.data.Bank
     * 
     * @param flipBank HEL::flip
     * @return state extracted from the bank
     */
    public static HelicityState createFromFlipBank(Bank flipBank) {
        HelicityState state = new HelicityState();
        state.run         = flipBank.getInt("run",0);
        state.event       = flipBank.getInt("event",0);
        state.timestamp   = flipBank.getLong("timestamp",0);
        state.hwStatus    = flipBank.getByte("status",0);
        state.helicity    = HelicityBit.create(flipBank.getByte("helicity",0));
        state.helicityRaw = HelicityBit.create(flipBank.getByte("helicityRaw",0));
        state.pairSync    = HelicityBit.create(flipBank.getByte("pair",0));
        state.patternSync = HelicityBit.create(flipBank.getByte("pattern",0));
        return state;
    }
   
    /**
     * Create a state from a HEL::flip org.jlab.io.base.DataBank
     * 
     * FIXME:  can we not cast/convert between DataBank and Bank?
     * 
     * @param flipBank HEL::flip
     * @return state extracted from the bank
     */
    public static HelicityState createFromFlipBank(DataBank flipBank) {
        HelicityState state = new HelicityState();
        state.run         = flipBank.getInt("run",0);
        state.event       = flipBank.getInt("event",0);
        state.timestamp   = flipBank.getLong("timestamp",0);
        state.hwStatus    = flipBank.getByte("status",0);
        state.helicity    = HelicityBit.create(flipBank.getByte("helicity",0));
        state.helicityRaw = HelicityBit.create(flipBank.getByte("helicityRaw",0));
        state.pairSync    = HelicityBit.create(flipBank.getByte("pair",0));
        state.patternSync = HelicityBit.create(flipBank.getByte("pattern",0));
        return state;
    }

    /**
     * If we have some valid channels, assume any invalid ones are
     * in the low/minus state.
     *
     * This is also done in the online delayed helicity correction.
     *
     * This is to fix an odd behavior where sometimes individual channels
     * are missing from readout when in the low state.  This seems to only
     * happen very close to helicity state changes.  Maybe due to the
     * FADC readout mode being used for helicity channels, and/or maybe
     * associated with the busy gating on the T-settle signal.
     */
    private void fixMissingReadouts() {
        if (this.helicityRaw!=HelicityBit.UDF ||
            this.pairSync!=HelicityBit.UDF ||
            this.patternSync!=HelicityBit.UDF) {
            if (this.helicityRaw==HelicityBit.UDF) this.helicityRaw=HelicityBit.MINUS;
            if (this.pairSync==HelicityBit.UDF) this.pairSync=HelicityBit.MINUS;
            if (this.patternSync==HelicityBit.UDF) this.patternSync=HelicityBit.MINUS;
        }
    }

    public double getSecondsDelta(HelicityState other) {
        return (this.timestamp-other.timestamp)*4e-9;
    }
    public int getEventDelta(HelicityState other) {
        return this.event-other.event;
    }

    @Override
    public String toString() {
        return String.format("%d %+d/%+d/%+d",
            this.timestamp,this.helicityRaw.value(),this.pairSync.value(),this.patternSync.value());
    }

    public String getInfo(HelicityState other,int counter) {
        return String.format("%s %8.2f %5d %7d",
                this.toString(),
                1000*this.getSecondsDelta(other),
                this.getEventDelta(other),
                counter);
    }

    public Bank getFlipBank(SchemaFactory schemaFactory) {
        Bank bank=new Bank(schemaFactory.getSchema("HEL::flip"),1);
        bank.putInt("run", 0, this.run);
        bank.putInt("event", 0, this.event);
        bank.putLong("timestamp", 0, this.timestamp);
        bank.putByte("status", 0, this.hwStatus);
        bank.putByte("helicity", 0, this.helicity.value());
        bank.putByte("helicityRaw", 0, this.helicityRaw.value());
        bank.putByte("pair", 0, this.pairSync.value());
        bank.putByte("pattern", 0, this.patternSync.value());
        return bank;
    }

    /**
     * @return whether any raw values are undefined 
     */
    public final boolean isValid() {
        return this.helicityRaw!=HelicityBit.UDF &&
               this.pairSync!=HelicityBit.UDF &&
               this.patternSync!=HelicityBit.UDF;
    }

    /**
     * @param other
     * @return whether the other state has equal raw values 
     */
    public boolean equals(HelicityState other) {
        if (this.helicityRaw != other.helicityRaw) return false;
        if (this.pairSync != other.pairSync) return false;
        return this.patternSync == other.patternSync;
    }

    /**
     * Assign the half-wave-plate-corrected helicity
     * @param hwp the HWP status (-1/0/1=IN/UDF/OUT, same as CCDB) 
     */
    public void setHalfWavePlate(byte hwp) {
        this.helicity = HelicityBit.create((byte)(hwp*this.helicityRaw.value()));
    }
    public void setTimestamp(long timestamp) { this.timestamp=timestamp; }
    public void setEvent(int event) { this.event=event; }
    public void setRun(int run) { this.run=run; }

    public int  getRun() { return this.run; }
    public int  getEvent() { return this.event; }
    public long getTimestamp() { return this.timestamp; }
    public HelicityBit getHelicityRaw() { return this.helicityRaw; }
    public HelicityBit getHelicity() { return this.helicity; }
    public HelicityBit getPairSync() { return this.pairSync; }
    public HelicityBit getPatternSync() { return this.patternSync; }

    /**
     * Inverts the helicity/helicityRaw components of this state
     * @return this state after inversion
     */
    public HelicityState invert() {
       this.helicity = HelicityBit.getFlipped(this.helicity);
       this.helicityRaw = HelicityBit.getFlipped(this.helicityRaw);
       return this;
    }

}
