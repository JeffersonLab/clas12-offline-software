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

    public static final int DISEN = 0b1;    // disentangle non-contiguous bank intervals 
    public static final int STRIP = 0b10;   // remove tsettle intervals
    public static final int MERGE = 0b100;  // merge tstable intervals generated from false advances
    public static final int CALIB = 0b1000; // calibrate raw scaler into beam charge
    public static final int ALL = DISEN | STRIP | MERGE | CALIB;

    private final IndexedTable fcupTable;
    private final IndexedTable slmTable;
    private final IndexedTable helTable;

    /**
     * @param fcupTable CCDB's /runcontrol/fcup
     * @param slmTable CCDB's /runcontrol/slm
     * @param helTable CCDB's /runcontrol/helicity
     * @param bank a RAW::scaler bank
     * @return
     */
    public static StruckScalers read(Bank bank, IndexedTable fcupTable, IndexedTable slmTable, IndexedTable helTable) {
        return read(ALL, bank, fcupTable, slmTable, helTable);
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
     * @param ops a mask of the operations to perform
     * @param fcupTable CCDB's /runcontrol/fcup
     * @param slmTable CCDB's /runcontrol/slm
     * @param helTable CCDB's /runcontrol/helicity
     * @param bank a RAW::scaler bank
     * @return
     */
    public static StruckScalers read(int ops, Bank bank, IndexedTable fcupTable, IndexedTable slmTable, IndexedTable helTable) {
        StruckScalers ss = new StruckScalers(fcupTable, slmTable, helTable);
        ss.read(bank);
        if ((ops & DISEN) != 0) ss.disentangle();
        if ((ops & STRIP) != 0) ss.strip(HelicityInterval.TSTABLE);
        if ((ops & MERGE) != 0) ss.merge();
        if ((ops & CALIB) != 0) ss.calibrate();
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
     * Get all intervals readout in one RAW::scaler bank.
     * @param bank a RAW::scaler bank
     */
    public void read(Bank bank) {

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
    public void calibrate() {
        for (StruckScaler ss : this) {
            ss.calibrate(this.fcupTable, this.slmTable);
        }
    }

    /**
     * Remove all intervals whose clock doesn't look like the given interval.
     * @param interval the type of helicity interval to preserve
     */
    public void strip(HelicityInterval interval) {
        for (int ii=0; ii<this.size(); ii++) {
            if (this.get(ii).getHelicityInterval(this.helTable) != interval) {
                this.remove(ii);
            }
        }
    }

    /**
     * Merge two intervals into one by copying the ungated scalers.
     * @param source the interval to remove after copying its ungated scalers
     * @param destination the interval to update and keep
     */
    public void copyGate(StruckScaler source, StruckScaler destination) {
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
    public void copyGate(int source, int destination) {
        this.copyGate(this.get(source), this.get(destination));
    }

    /**
     * When there's one interval in a RAW::scaler bank, that interval is 
     * represented by (6) contiguous bank rows.  But when there's multiple
     * intervals, the gated/ungated are interspersed with another interval in
     * between.  Here we disentangle that.  Note, this should be called *before*
     * any of the other manipulations.
     */
    public void disentangle() {
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
     * Merge two intervals into one by adding their raw scaler values.
     * @param source the interval to remove after adding it to the other one
     * @param destination the interval to update and keep 
     */
    public void add(int source, int destination) {
        this.get(destination).add(this.get(source));
        this.remove(source);
    }

    /**
     * Identify intervals created by false advances and merge their raw scaler
     * values into one full tstable interval.  False advances must have the same
     * helicity as the previous one and odd clock values.  Note, this requires
     * tsettle intervals were already removed.
     */
    public void merge() {

        // clock must be with in 1% of the expected for tstable:
        final double tolerance = 0.01;

        final double tsettle = 1E-6 * helTable.getDoubleValue("tsettle",0,0,0);
        final double tstable = 1E-6 * helTable.getDoubleValue("tstable",0,0,0);

        // Loop over the starting interval:
        for (int i0=0; i0<this.size()-1; ++i0) {

            // Initialize the sum of the intervals' clocks:
            double clockSum = (double)this.get(i0).clock / this.get(i0).clockFreq;

            // The ending interval's index:
            int i1 = i0;

            // Whether we already found a good, summed interval:
            boolean found = false;

            // loop over subsequent intervals:
            for (int jj=i0+1; jj<this.size(); ++jj) {

                // Stop if it's not the same helicity as the starting interval:
                if (this.get(i0).helicity != this.get(jj).helicity) break;

                // The summed interval:
                clockSum += (double)this.get(jj).clock / this.get(jj).clockFreq;
                HelicityInterval intvl = HelicityInterval.createStrict(tolerance, clockSum, tsettle, tstable);

                // The summed interval looks like a full tstable, so update the
                // ending interval:
                if (intvl == HelicityInterval.TSTABLE) {
                    i1 = jj;
                    found = true;
                }

                // If the summed interval doesn't look like a full tstable, but
                // we already found one that did, then there's no point in
                // continuing with this starting interval:
                else if (found) {
                    break;
                }
            }

            // Finally, add the intervals to the starting interval.
            // Note, we walk backwards here because we'll be removing elements
            // and leaving i0+1 as the next starting interval.
            if (i1 > i0) {
                for (int kk=i1; kk>=i0; --kk) {
                    this.add(kk, i0);
                }
            }
        }
    }

}
