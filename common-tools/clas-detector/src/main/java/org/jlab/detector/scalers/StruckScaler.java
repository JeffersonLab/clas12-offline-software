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

    /**
     * The input signal mapping.
     */
    public enum Channel {
        FCUP,
        SLM,
        CLOCK,
        UDF;
        public static boolean equals(Channel s, int c) {
            switch (s) {
                case FCUP:
                    return c==CHAN_FCUP_A || c==CHAN_FCUP_B;
                case SLM:
                    return c==CHAN_SLM_A || c==CHAN_SLM_B;
                case CLOCK:
                    return c==CHAN_CLOCK_A || c==CHAN_CLOCK_B;
                default:
                    return false;
            } 
        }
    }
   
    /**
     * The period mapping, which corresponds to helicity period.
     */
    public enum Period {
        A,
        B,
        UDF;
        public static Period create(int struckChannel) {
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

    /**
     * Determine whether the clock looks more like tsettle or tstable periods.
     * @param clock
     * @param helTable /runcontrol/helicity CCDB table
     * @return the type of helicity period 
     */
    public HelicityPeriod getHelicityPeriod(long clock, IndexedTable helTable) {
        final double clockSeconds = (double)clock / this.clockFreq;
        // these guys are in microseconds in CCDB, convert them to seconds:
        final double tsettleSeconds = 1E6 * helTable.getDoubleValue("tsettle",0,0,0);
        final double tstableSeconds = 1E6 * helTable.getDoubleValue("tstable",0,0,0);
        return HelicityPeriod.create(clockSeconds, tstableSeconds, tsettleSeconds);
    }

    /**
     * Look for an ungated clock readout whose value corresponds to the helicity
     * tsettle period, and return it's Struck period.
     * @param bank
     * @param helTable /runcontrol/helicity CCDB table
     * @return 
     */
    public final Period getStablePeriod(Bank bank, IndexedTable helTable) {
        for (int k=0; k<bank.getRows(); k++){
            if (bank.getInt("crate",k)!=CRATE) continue;
            if (bank.getInt("slot",k)!=SLOT_UNGATED) continue;
            final int chan = bank.getInt("channel",k);
            if (chan==CHAN_CLOCK_A || chan==CHAN_CLOCK_B) {
                final long clck = bank.getLong("value",k);
                if (this.getHelicityPeriod(clck,helTable) == HelicityPeriod.TSTABLE) {
                    return Period.create(chan);
                }
            }
        }
        return Period.UDF;
    }

    public StruckScaler(Bank bank,IndexedTable fcupTable, IndexedTable slmTable, IndexedTable helTable) {

        // the STRUCK's clock is 1 MHz
        this.clockFreq = 1e6;

        // Here we're going to assume the stable period is the same Struck
        // period throughout a single readout.  Almost always correct ...
        // FIXME
        Period stablePeriod = this.getStablePeriod(bank, helTable);

        // Couldn't find an ungated clock in the stable period, so there's
        // nothing useful we can do:
        if (stablePeriod == Period.UDF) return;
        
        for (int k=0; k<bank.getRows(); k++){

            // If it ain't the right crate, ignore it:
            if (bank.getInt("crate",k)!=CRATE) continue;

            // Determine the tsettle/tstable period for this bank row:
            Period thisPeriod = Period.create(bank.getInt("channel",k));

            // If it doesn't correspond to tstable, ignore it:
            if (thisPeriod != stablePeriod) continue;

            // Determine the gating for this bank row, and, if undefined,
            // just ignore it:
            Gating gating = Gating.UDF;
            switch (bank.getInt("slot",k)) {
                case SLOT_GATED:
                    gating = Gating.GATED;
                    break;
                case SLOT_UNGATED:
                    gating = Gating.UNGATED;
                    break;
                default:
                    continue;
            }

            // Finally, do somthing useful:
            final int chan = bank.getInt("channel",k);
            switch (gating) {
                case GATED:
                    if (Channel.equals(Channel.FCUP, chan)) {
                        this.helicity = bank.getByte("helicity",k) > 0 ? POSITIVE : NEGATIVE;
                        this.quartet = bank.getByte("quartet",k)   > 0 ? POSITIVE : NEGATIVE;
                        this.gatedFcup = bank.getLong("value",k);
                    }
                    else if (Channel.equals(Channel.SLM, chan)) {
                        this.gatedSlm = bank.getLong("value",k);
                    }
                    else if (Channel.equals(Channel.CLOCK, chan)) {
                        this.gatedClock = bank.getLong("value",k);
                    }
                    break;
                case UNGATED:
                    if (Channel.equals(Channel.FCUP, chan)) {
                        this.fcup = bank.getLong("value",k);
                    }
                    else if (Channel.equals(Channel.SLM, chan)) {
                        this.slm = bank.getLong("value",k);
                    }
                    else if (Channel.equals(Channel.CLOCK, chan)) {
                        this.clock = bank.getLong("value",k);
                    }
                    break;
                default:
                    break;
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