package org.jlab.detector.scalers;
    
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.detector.helicity.HelicityBit;
import org.jlab.detector.helicity.HelicityInterval;

/**
 * The Struck is a multi-channel scaler that can buffer many readings over very
 * short dwell times, advanced by and latched to input control signals.
 * 
 * The CLAS12 DAQ uses this device to readout helicity-latched counts for the
 * purpose of beam-spin asymmetry measurements, and that's what this class is
 * geared towards.
 *
 * At some point over the years, readout patterns changed and this class alone
 * is no longer sufficient.  See StruckScalers instead.
 * 
 * @author baltzell
 */
public class StruckScaler extends DaqScaler {

    private static final boolean GATEINVERTED=false;

    protected HelicityBit helicity = HelicityBit.UDF;
    protected HelicityBit quartet = HelicityBit.UDF;
    public void setHelicity(HelicityBit b) { this.helicity=b; }
    public void setQuartet(HelicityBit b) { this.quartet=b; }
    public HelicityBit getHelicity() { return this.helicity; }
    public HelicityBit getQuartet() { return this.quartet; }

    // These slots corrspond to gated/ungated scalers in RAW::scaler.
    public static final int SLOT_GATED=0;
    public static final int SLOT_UNGATED=1;

    // These channels correspond to the settle/tsettle periods in RAW::scaler,
    // but figuring out which is which requires checking the clock:
    private static final int CHAN_FCUP_A=0;
    private static final int CHAN_SLM_A=1;
    private static final int CHAN_CLOCK_A=2;
    private static final int CHAN_FCUP_B=32;
    private static final int CHAN_SLM_B=33;
    private static final int CHAN_CLOCK_B=34;

    protected Interval interval;

    public void setInterval(Interval intvl) {
        this.interval = intvl;
    }
    public Interval getInterval() {
        return this.interval;
    }

    @Override
    public String toString() {
        return String.format("i=%s h=%d q=%d %s",this.interval,this.helicity.value(),this.quartet.value(),super.toString());
    }

    /**
     * Convenience for mapping channel number in RAW::scaler to input signal.
     */
    public static enum Input {
        FCUP,
        SLM,
        CLOCK,
        UDF;
        public static Input create(int struckChannel) {
            switch (struckChannel) {
                case CHAN_FCUP_A:
                    return FCUP;
                case CHAN_SLM_A:
                    return SLM;
                case CHAN_CLOCK_A:
                    return CLOCK;
                case CHAN_FCUP_B:
                    return FCUP;
                case CHAN_SLM_B:
                    return SLM;
                case CHAN_CLOCK_B:
                    return CLOCK;
                default:
                    return UDF;
            }
        }
        public static boolean equals(Input input, int struckChannel) {
            return create(struckChannel) == input;
        }
    }
   
    /**
     * Convenience for mapping channel number in RAW::scaler to scaler period.
     * Ultimately this period will map to helicity tsettle/tstable.
     */
    public static enum Interval {
        A,
        B,
        UDF;
        public static Interval create(int struckChannel) {
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
        public static boolean equals(Interval interval, int struckChannel) {
            return create(struckChannel) == interval;
        }
    }

    /**
     * Determine whether the clock looks more like tsettle or tstable periods.
     * @param clock the value of this clock readout
     * @param helTable /runcontrol/helicity CCDB table
     * @return the type of helicity period 
     */
    public HelicityInterval getHelicityInterval(long clock, IndexedTable helTable) {
        final double clockSeconds = (double)clock / this.clockFreq;
        // these guys are in microseconds in CCDB, convert them to seconds:
        final double tsettleSeconds = 1E-6 * helTable.getDoubleValue("tsettle",0,0,0);
        final double tstableSeconds = 1E-6 * helTable.getDoubleValue("tstable",0,0,0);
        return HelicityInterval.createLoose(0.1, clockSeconds, tsettleSeconds, tstableSeconds);
    }

    /**
     * Determine whether the clock looks more like tsettle or tstable periods.
     * @param helTable /runcontrol/helicity CCDB table
     * @return the type of helicity period 
     */
    public HelicityInterval getHelicityInterval(IndexedTable helTable) {
        return this.getHelicityInterval(this.clock, helTable);
    }

    /**
     * Look for the first ungated clock readout whose value corresponds to the
     * helicity tsettle period, and return it's Struck period.
     * @param bank a RAW::scaler bank
     * @param helTable /runcontrol/helicity CCDB table
     * @return the type of Struck period 
     */
    public final Interval getStableInterval(Bank bank, IndexedTable helTable) {
        for (int k=0; k<bank.getRows(); k++){
            if (bank.getInt("crate",k)!=CRATE) continue;
            if (bank.getInt("slot",k)!=SLOT_UNGATED) continue;
            final int chan = bank.getInt("channel",k);
            if (chan==CHAN_CLOCK_A || chan==CHAN_CLOCK_B) {
                final long clck = bank.getLong("value",k);
                if (this.getHelicityInterval(clck,helTable) == HelicityInterval.TSTABLE) {
                    return Interval.create(chan);
                }
            }
        }
        return Interval.UDF;
    }

    public StruckScaler() {
        this.clockFreq = 1e6;
    }

    /**
     * This made sense when we only had one tsettle+tstable in each readout. 
     * @param bank a RAW::scaler bank
     * @param fcupTable /runcontrol/fcup CCDB table
     * @param slmTable /runcontrol/slm CCDB table
     * @param helTable /runcontrol/helicity CCDB table
     */
    @Deprecated
    public StruckScaler(Bank bank,IndexedTable fcupTable, IndexedTable slmTable, IndexedTable helTable) {

        // the STRUCK's clock is 1 MHz
        this.clockFreq = 1e6;

        // Here we're going to assume the stable period is the same Struck
        // period throughout a single readout.  Almost always correct ...
        Interval stablePeriod = this.getStableInterval(bank, helTable);

        // Couldn't find an ungated clock in the stable period, so there's
        // nothing useful we can do:
        if (stablePeriod == Interval.UDF) return;
        
        for (int k=0; k<bank.getRows(); k++){

            // If it ain't the right crate, ignore it:
            if (bank.getInt("crate",k)!=CRATE) continue;

            // Determine the tsettle/tstable period for this bank row:
            Interval thisPeriod = Interval.create(bank.getInt("channel",k));

            // If it doesn't correspond to tstable, ignore it:
            if (thisPeriod != stablePeriod) continue;

            // Interpret the scaler values:
            final int chan = bank.getInt("channel",k);
            switch (bank.getInt("slot",k)) {
                case SLOT_GATED:
                    if (Input.equals(Input.FCUP, chan)) {
                        this.helicity = HelicityBit.createFromRawBit(bank.getByte("helicity",k));
                        this.quartet = HelicityBit.createFromRawBit(bank.getByte("quartet",k));
                        this.gatedFcup = bank.getLong("value",k);
                    }
                    else if (Input.equals(Input.SLM, chan)) {
                        this.gatedSlm = bank.getLong("value",k);
                    }
                    else if (Input.equals(Input.CLOCK, chan)) {
                        this.gatedClock = bank.getLong("value",k);
                    }
                    break;
                case SLOT_UNGATED:
                    if (Input.equals(Input.FCUP, chan)) {
                        this.fcup = bank.getLong("value",k);
                    }
                    else if (Input.equals(Input.SLM, chan)) {
                        this.slm = bank.getLong("value",k);
                    }
                    else if (Input.equals(Input.CLOCK, chan)) {
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