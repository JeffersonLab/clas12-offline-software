package org.jlab.detector.scalers;

import java.util.ArrayList;
import org.jlab.detector.helicity.HelicityBit;
import org.jlab.detector.helicity.HelicityInterval;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.detector.scalers.StruckScaler.Interval;
import org.jlab.detector.scalers.StruckScaler.Input;

/**
 *
 * This is an extension of the StruckScaler class to support multiple tstable
 * helicity intervals in a single RAW::scaler bank.
 * 
 * @author baltzell
 */
public class StruckScalers extends ArrayList<StruckScaler> {

    @Override
    public String toString() {
        String ret = new String();
        for (StruckScaler ss : this) {
            ret += ss.toString();
            ret += "\n"+super.toString();
        }
        return ret;
    }

    /**
     * Get all intervals readout in one RAW::scaler bank.
     * @param bank a RAW::scaler bank
     * @param fcupTable /runcontrol/fcup CCDB table
     * @param slmTable /runcontrol/slm CCDB table
     * @param helTable /runcontrol/helicity CCDB table
     * @return all tstable intervals 
     */
    public static StruckScalers readAll(Bank bank, IndexedTable fcupTable, IndexedTable slmTable, IndexedTable helTable) {

        StruckScalers ret = new StruckScalers();
        StruckScaler reading = new StruckScaler();

        for (int k=0; k<bank.getRows(); ++k) {

            if (bank.getInt("crate",k) != StruckScaler.CRATE) continue;

            final int chan = bank.getInt("channel",k);
            final Interval intvl = Interval.create(chan);

            // Found a new interval:
            if (ret.isEmpty() || intvl != ret.get(ret.size()-1).getInterval()) {
                reading = new StruckScaler();
                reading.setInterval(intvl);
                ret.add(reading);
            }

            switch (bank.getInt("slot",k)) {
                case StruckScaler.SLOT_GATED:
                    if (Input.equals(Input.FCUP, chan)) {
                        reading.helicity = HelicityBit.create(bank.getByte("helicity",k));
                        reading.quartet = HelicityBit.create(bank.getByte("quartet",k));
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

        // go back and "calibrate" all of them, e.g. convert to beam charge and livetime:
        for (StruckScaler ss : ret) { 
            ss.calibrate(fcupTable,slmTable);
        }

        return ret;
    }

    /**
     * Get all the "good" helicity intervals readout in one RAW::scaler bank.
     * Here "good" means tstable, which requires an existing clock reading that
     * also looks like the tstable helicity interval.  Note, this should also
     * get rid of any cases where things didn't get fully initialized and resulted
     * in -1 values in HEL::scaler.
     * @param bank a RAW::scaler bank
     * @param fcupTable /runcontrol/fcup CCDB table
     * @param slmTable /runcontrol/slm CCDB table
     * @param helTable /runcontrol/helicity CCDB table
     * @return all tstable intervals 
     */
    public static StruckScalers readAllPruned(Bank bank, IndexedTable fcupTable, IndexedTable slmTable, IndexedTable helTable) {
        StruckScalers ret = StruckScalers.readAll(bank, fcupTable, slmTable, helTable);
        for (StruckScaler ss : ret) {
            if (ss.getHelicityInterval(helTable) != HelicityInterval.TSTABLE) {
                ret.remove(ss);
            }
        }
        return ret;
    }

    public static void main(String[] args) {
        StruckScalers s = new StruckScalers();
        System.out.println(s);
    }
}
