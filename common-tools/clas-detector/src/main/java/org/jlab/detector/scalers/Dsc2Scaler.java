package org.jlab.detector.scalers;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.utils.groups.IndexedTable;

public class Dsc2Scaler extends DaqScaler{

    private static final boolean GATEINVERTED=true;

    // DSC has TRG and TDC thresholds, we use only TDC here:
    private static final int SLOT=64;
    private static final int CHAN_FCUP_GATED=16;
    private static final int CHAN_SLM_GATED=17;
    private static final int CHAN_CLOCK_GATED=18;
    private static final int CHAN_FCUP=48;
    private static final int CHAN_SLM=49;
    private static final int CHAN_CLOCK=50;

    public Dsc2Scaler() {}

    public Dsc2Scaler(Bank bank,IndexedTable fcupTable,IndexedTable slmTable,double seconds) {

        // the DSC2's clock is (currently) 1 MHz
        // FIXME:  use CCDB
        this.clockFreq=1e6;

        // this will get the last entries (most recent) in the bank
        for (int k=0; k<bank.getRows(); k++){

            if (bank.getInt("crate",k)!=CRATE || bank.getInt("slot",k)!=SLOT) {
                continue;
            }
            switch (bank.getInt("channel",k)) {
                case CHAN_FCUP_GATED:
                    this.gatedFcup = bank.getLong("value",k);
                    break;
                case CHAN_SLM_GATED:
                    this.gatedSlm = bank.getLong("value",k);
                    break;
                case CHAN_CLOCK_GATED:
                    this.gatedClock = bank.getLong("value",k);
                    break;
                case CHAN_FCUP:
                    this.fcup = bank.getLong("value",k);
                    break;
                case CHAN_SLM:
                    this.slm = bank.getLong("value",k);
                    break;
                case CHAN_CLOCK:
                    this.clock = bank.getLong("value",k);
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
    
        this.calibrate(fcupTable,slmTable,seconds);
    }

    /**
     * @param bank RAW::scaler bank
     * @param fcupTable /runcontrol/fcup CCDB table
     * @param slmTable  /runcontrol/slm CCDB table
     */
    public Dsc2Scaler(Bank bank,IndexedTable fcupTable,IndexedTable slmTable) {
        this(bank,fcupTable,slmTable,1);
        this.calibrate(fcupTable,slmTable);
    }

    /**
     * During some run periods, the run-integrating DSC2 scaler's clock frequency
     * was too large and rolls over during the run.  So here we can pass in seconds
     * (e.g. based on RCDB run start time) instead.
     * @param fcupTable /runcontrol/fcup CCDB table
     * @param slmTable /runcontrol/slm CCDB table
     * @param seconds 
     */
    protected final void calibrate(IndexedTable fcupTable,IndexedTable slmTable,double seconds) {
        if (this.slm>0) {
            super.calibrate(fcupTable,slmTable,seconds,seconds*((double)this.gatedSlm)/this.slm);
        }
    }
}