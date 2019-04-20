package org.jlab.detector.helicity;

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
public class HelicityState {

    // FIXME:  these should go in CCDB
    private static final short HALFADC=2000;
    private static final byte SECTOR=1;
    private static final byte LAYER=1;
    private static final byte HELICITY_COMPONENT=1;
    private static final byte SYNC_COMPONENT=2;
    private static final byte QUARTET_COMPONENT=3;

    private HelicityBit helicityRaw = HelicityBit.UDF;
    private HelicityBit helicity    = HelicityBit.UDF;
    private HelicityBit sync        = HelicityBit.UDF;
    private HelicityBit quartet     = HelicityBit.UDF;
    private long timestamp   = 0;
    private int  event       = 0;
    private int  run         = 0;

    public HelicityState(){}

    private HelicityBit getFadcState(short ped) {
        if      (ped == HALFADC) return HelicityBit.UDF;
        else if (ped > HALFADC)  return HelicityBit.PLUS;
        else                     return HelicityBit.MINUS;
    }

    public static HelicityState createFromFadcBank(Bank adcBank) {
        HelicityState state=new HelicityState();
        for (int ii=0; ii<adcBank.getRows(); ii++) {
            if (adcBank.getInt("sector",ii) != SECTOR) continue;
            if (adcBank.getInt("layer",ii) != LAYER) continue;
            switch (adcBank.getInt("component",ii)) {
                case HELICITY_COMPONENT:
                    state.helicityRaw = state.getFadcState(adcBank.getShort("ped",ii));
                    break;
                case SYNC_COMPONENT:
                    state.sync = state.getFadcState(adcBank.getShort("ped",ii));
                    break;
                case QUARTET_COMPONENT:
                    state.quartet = state.getFadcState(adcBank.getShort("ped",ii));
                    break;
                default:
                    break;
            }
        }
        state.fixMissingReadouts();
        return state;
    }

    /**
     * @param flipBank = HEL::flip
     * @return = HelicityState extracted from the bank
     */
    public static HelicityState createFromFlipBank(Bank flipBank) {
        HelicityState state = new HelicityState();
        state.run         = flipBank.getInt("run",0);
        state.event       = flipBank.getInt("event",0);
        state.timestamp   = flipBank.getLong("timestamp",0);
        state.helicity    = HelicityBit.create(flipBank.getByte("helicity",0));
        state.helicityRaw = HelicityBit.create(flipBank.getByte("helicityRaw",0));
        state.sync        = HelicityBit.create(flipBank.getByte("sync",0));
        state.quartet     = HelicityBit.create(flipBank.getByte("quartet",0));
        return state;
    }
   
    /**
     * FIXME:  can we not cast/convert between DataBank and Bank?
     * 
     * @param flipBank = HEL::flip
     * @return HelicityState extracted from the bank
     */
    public static HelicityState createFromFlipBank(DataBank flipBank) {
        HelicityState state = new HelicityState();
        state.run         = flipBank.getInt("run",0);
        state.event       = flipBank.getInt("event",0);
        state.timestamp   = flipBank.getLong("timestamp",0);
        state.helicity    = HelicityBit.create(flipBank.getByte("helicity",0));
        state.helicityRaw = HelicityBit.create(flipBank.getByte("helicityRaw",0));
        state.sync        = HelicityBit.create(flipBank.getByte("sync",0));
        state.quartet     = HelicityBit.create(flipBank.getByte("quartet",0));
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
            this.sync!=HelicityBit.UDF ||
            this.quartet!=HelicityBit.UDF) {
            if (this.helicityRaw==HelicityBit.UDF) this.helicityRaw=HelicityBit.MINUS;
            if (this.sync==HelicityBit.UDF) this.sync=HelicityBit.MINUS;
            if (this.quartet==HelicityBit.UDF) this.quartet=HelicityBit.MINUS;
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
            this.timestamp,this.helicityRaw.value(),this.sync.value(),this.quartet.value());
    }

    public String getInfo(HelicityState other,int counter) {
        return String.format("%s %6.2f %5d %7d",
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
        bank.putByte("helicity", 0, this.helicity.value());
        bank.putByte("helicityRaw", 0, this.helicityRaw.value());
        bank.putByte("sync", 0, this.sync.value());
        bank.putByte("quartet", 0, this.quartet.value());
        return bank;
    }

    /**
     * @return whether any raw values are undefined 
     */
    public final boolean isValid() {
        return this.helicityRaw!=HelicityBit.UDF &&
               this.sync!=HelicityBit.UDF &&
               this.quartet!=HelicityBit.UDF;
    }

    /**
     * @param other
     * @return whether the other state has equal raw values 
     */
    public boolean equals(HelicityState other) {
        if (this.helicityRaw != other.helicityRaw) return false;
        if (this.sync != other.sync) return false;
        return this.quartet == other.quartet;
    }

    /**
     * Assign the half-wave-plate-corrected helicity
     * @param hwp = the HWP status (-1/0/1=IN/UDF/OUT, same as CCDB) 
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
    public HelicityBit getSync() { return this.sync; }
    public HelicityBit getQuartet() { return this.quartet; }

}
