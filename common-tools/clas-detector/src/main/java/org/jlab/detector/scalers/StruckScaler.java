package org.jlab.detector.scalers;
    
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.detector.helicity.HelicityPeriod;

/**
 * The Struck is a multi-channel scaler that can buffer many readings over very
 * short dwell times, advanced by and latched to input control signals.
 * 
 * The CLAS12 DAQ uses this device to readout helicity-latched counts for the
 * purpose of beam-spin asymmetry measurements, and that's what this class is
 * geared towards.
 * 
 * @author baltzell
 */
public class StruckScaler extends DaqScaler {

    private static final boolean GATEINVERTED=false;

    private final byte UDF=0;
    private final byte POSITIVE=1;
    private final byte NEGATIVE=-1;
    private byte helicity=UDF;
    private byte quartet=UDF;
    public byte getHelicity() { return this.helicity; }
    public byte getQuartet() { return this.quartet; }

    private static final int SLOT_GATED=0;
    private static final int SLOT_UNGATED=1;

    // these channels correspond to the settle/tsettle periods, but figuring
    // out which is which requires the checking the clock:
    private static final int CHAN_FCUP_A=0;
    private static final int CHAN_SLM_A=1;
    private static final int CHAN_CLOCK_A=2;
    private static final int CHAN_FCUP_B=32;
    private static final int CHAN_SLM_B=33;
    private static final int CHAN_CLOCK_B=34;

    public enum StruckPeriod {
        A,
        B,
        UDF;
        public static StruckPeriod create(int struckChannel) {
            switch (struckChannel) {
                case CHAN_FCUP_A:
                    return A;
                case CHAN_SLM_A:
                    return A;
                case CHAN_CLOCK_A:
                    return A;
                case CHAN_FCUP_B:
                    return B;
                case CHAN_SLM_B:
                    return B;
                case CHAN_CLOCK_B:
                    return B;
                default:
                    return UDF;
            }
        }
    }

    //public class StruckHelicityPeriod {
    //    HelicityPeriod helPeriod;
    //    StruckPeriod struckPeriod;
    //    public StruckHelicityPeriod(HelicityPeriod hp, StruckPeriod sp) {
    //        this.helPeriod = hp;
    //        this.struckPeriod = sp;
    //    }
    //}

    public StruckScaler() {}

    /**
     * Look for an ungated clock readout whose value corresponds to the helicity
     * tsettle period, and return it's Struck period.
     * @param bank
     * @return 
     */
    public final StruckPeriod getStablePeriod(Bank bank, IndexedTable helTable) {
        for (int k=0; k<bank.getRows(); k++){
            if (bank.getInt("crate",k)!=CRATE) continue;
            if (bank.getInt("slot",k)!=SLOT_UNGATED) continue;
            final int chan = bank.getInt("channel",k);
            if (chan==CHAN_CLOCK_A || chan==CHAN_CLOCK_B) {
                final long clck = bank.getLong("value",k);
                if (this.getHelicityPeriod(clck,helTable) == HelicityPeriod.TSTABLE) {
                    return StruckPeriod.create(chan);
                }
            }
        }
        return StruckPeriod.UDF;
    }

    public StruckScaler(Bank bank,IndexedTable fcupTable, IndexedTable slmTable, IndexedTable helTable) {

        // the STRUCK's clock is 1 MHz
        this.clockFreq = 1e6;

        // Here we're going to assume the stable period is the same Struck period
        // throughout a single readout.  Almost always correct ...
        // FIXME
        StruckPeriod stablePeriod = this.getStablePeriod(bank, helTable);

        // Couldn't find an ungated clock in the stable period, so there's
        // nothing useful we can do:
        if (stablePeriod == StruckPeriod.UDF) return;
        
        for (int k=0; k<bank.getRows(); k++){

            if (bank.getInt("crate",k)!=CRATE) continue;

            // If the period doesn't correspond to tstable, ignore it:
            StruckPeriod thisPeriod = StruckPeriod.create(bank.getInt("channel",k));
            if (thisPeriod != stablePeriod) continue;

            if (bank.getInt("slot",k)==SLOT_GATED) {
                switch (bank.getInt("channel",k)) {
                    case CHAN_FCUP_A:
                        this.helicity = bank.getByte("helicity",k) > 0 ? POSITIVE : NEGATIVE;
                        this.quartet = bank.getByte("quartet",k)   > 0 ? POSITIVE : NEGATIVE;
                        this.gatedFcup = bank.getLong("value",k);
                        break;
                    case CHAN_SLM_A:
                        this.gatedSlm = bank.getLong("value",k);
                        break;
                    case CHAN_CLOCK_A:
                        this.gatedClock = bank.getLong("value",k);
                        break;
                    default:
                        break;
                }
            }
            else if (bank.getInt("slot",k)==SLOT_UNGATED) {
                switch (bank.getInt("channel",k)) {
                    case CHAN_FCUP_B:
                        this.fcup = bank.getLong("value",k);
                        break;
                    case CHAN_SLM_B:
                        this.slm = bank.getLong("value",k);
                        break;
                    case CHAN_CLOCK_B:
                        this.clock = bank.getLong("value",k);
                        break;
                    default:
                        break;
                }
            }
        }
        if (GATEINVERTED) {
            gatedSlm = slm - gatedSlm;
            gatedFcup = fcup - gatedFcup;
            gatedClock = clock - gatedClock;
        }

        this.calibrate(fcupTable,slmTable);
    }
}