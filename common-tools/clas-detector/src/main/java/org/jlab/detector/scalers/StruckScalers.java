package org.jlab.detector.scalers;

import java.util.ArrayList;
import java.util.HashMap;
import org.jlab.detector.helicity.HelicityBit;
import org.jlab.detector.helicity.HelicityInterval;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.detector.scalers.StruckScaler.Interval;
import org.jlab.detector.scalers.StruckScaler.Input;

/**
 *
 * This is an extension of the StruckScaler class to support multiple intervals
 * reported in a single RAW::scaler bank, merge intervals created from false
 * advances, strip out single types of helicity intervals, e.g., tstable, 
 * disentangle odd readout patterns, etc. 
 * 
 * @author baltzell
 */
public class StruckScalers extends ArrayList<StruckScaler> {

    IndexedTable fcupTable;
    IndexedTable slmTable;
    IndexedTable helTable;

    /**
     * @param fcupTable CCDB's /runcontrol/fcup
     * @param slmTable CCDB's /runcontrol/slm
     * @param helTable CCDB's /runcontrol/helicity
     * @param bank a RAW::scaler bank
     * @return
     */
    public static StruckScalers read(Bank bank, IndexedTable fcupTable, IndexedTable slmTable, IndexedTable helTable) {
        StruckScalers ss = new StruckScalers(fcupTable, slmTable, helTable);
        ss.read(bank);
        ss.disentangle();
        ss.strip(HelicityInterval.TSTABLE);
        ss.calibrate();
        return ss;        
    }

    @Override
    public String toString() {
        String ret = new String();
        ret += "------------- STRUCK ------------------------------\n";
        for (StruckScaler ss : this) {
            ret += ss.toString() + "\n";
        }
        return ret;
    }

    /**
     * @param fcupTable CCDB's /runcontrol/fcup
     * @param slmTable CCDB's /runcontrol/slm
     * @param helTable CCDB's /runcontrol/helicity
     */
    public StruckScalers(IndexedTable fcupTable, IndexedTable slmTable, IndexedTable helTable) {
        this.fcupTable = fcupTable;
        this.slmTable = slmTable;
        this.helTable = helTable;
    }

    /**
     * Get all intervals readout in one RAW::scaler bank.
     * @param bank a RAW::scaler bank
     */
    private void read(Bank bank) {

        StruckScaler reading = new StruckScaler();

        for (int k=0; k<bank.getRows(); ++k) {

            // we've got other types of scalers in RAW::scaler, ignore them:
            if (bank.getInt("crate",k) != StruckScaler.CRATE) continue;
            if (bank.getInt("slot",k) != StruckScaler.SLOT_GATED &&
                bank.getInt("slot",k) != StruckScaler.SLOT_UNGATED) continue;

            final int chan = bank.getInt("channel",k);
            final Interval intvl = Interval.create(chan);

            // found a new interval, register it:
            if (this.isEmpty() || intvl != this.get(this.size()-1).getInterval()) {
                reading = new StruckScaler();
                reading.setInterval(intvl);
                this.add(reading);
            }

            switch (bank.getInt("slot",k)) {
                case StruckScaler.SLOT_GATED:
                    if (Input.equals(Input.FCUP, chan)) {
                        reading.helicity = HelicityBit.createFromRawBit(bank.getByte("helicity",k));
                        reading.quartet = HelicityBit.createFromRawBit(bank.getByte("quartet",k));
                        reading.gatedFcup = bank.getLong("value",k);
                    }
                    else if (Input.equals(Input.SLM, chan)) {
                        reading.gatedSlm = bank.getLong("value",k);
                    }
                    else if (Input.equals(Input.CLOCK, chan)) {
                        reading.gatedClock = bank.getLong("value",k);
                    }
                    break;
                case StruckScaler.SLOT_UNGATED:
                    if (Input.equals(Input.FCUP, chan)) {
                        reading.fcup = bank.getLong("value",k);
                    }
                    else if (Input.equals(Input.SLM, chan)) {
                        reading.slm = bank.getLong("value",k);
                    }
                    else if (Input.equals(Input.CLOCK, chan)) {
                        reading.clock = bank.getLong("value",k);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Convert raw scaler counts into beam charge:
     */
    private void calibrate() {
        for (StruckScaler ss : this) {
            ss.calibrate(this.fcupTable, this.slmTable);
        }
    }

    /**
     * Merge two intervals into one by copying the ungated scalers.
     * @param source the interval to remove after copying its ungated scalers
     * @param destination the interval to update and keep
     */
    private void copyGate(StruckScaler source, StruckScaler destination) {
        destination.clock = source.clock;
        destination.fcup = source.fcup;
        destination.slm = source.slm;
        this.remove(source); 
    }

    /**
     * Merge two intervals into one by copying the ungated scalers.
     * @param source the interval to remove after copying its ungated scalers
     * @param destination the interval to update and keep
     */
    private void copyGate(int source, int destination) {
        this.copyGate(this.get(source), this.get(destination));
    }

    /**
     * When there's one interval in a RAW::scaler bank, that interval is 
     * represented by (6) contiguous bank rows.  But when there's multiple
     * intervals, the gated/ungated are interspersed with another interval
     * (and the ungated doesn't contain the helicity bit).  Here we fix.
     */
    private void disentangle() {
        HashMap<StruckScaler,StruckScaler> d = new HashMap<>();
        for (int ii=0; ii<this.size()-2; ii++) {
            if (this.get(ii).interval != this.get(ii+2).interval) continue;
            if (this.get(ii).clock<0 && this.get(ii+2).clock>0) {
                if (this.get(ii).gatedClock>0 && this.get(ii+2).gatedClock<0) {
                    d.put(this.get(ii+2), this.get(ii));
                }
            }
        }
        for (StruckScaler source : d.keySet()) {
            this.copyGate(source, d.get(source));
        }
    }

    /**
     * Merge two intervals into one by adding their contents.
     * @param source the interval to remove after adding it to the other one
     * @param destination the interval to update and keep 
     */
    private void add(int source, int destination) {
        this.get(destination).add(this.get(source));
        this.remove(source);
    }

    /**
     * Identify intervals created by false advances, remove them, and add them
     * to the previous interval.  False intervals must have the same helicity as
     * the previous one and have an odd clock value.  Hmm, on further thought,
     * there's not much point in doing this, and it's better to just strip out
     * things that look like tsettle and keep everything else as is.
     */
    private void add() {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove all intervals that don't look like the given interval.
     * @param interval the type of helicity interval to preserve
     */
    private void strip(HelicityInterval interval) {
        for (int ii=0; ii<this.size(); ii++) {
            if (this.get(ii).getHelicityInterval(this.helTable) != interval) {
                this.remove(ii);
            }
        }
    }

}
