package org.jlab.detector.scalers;
    
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.utils.groups.IndexedTable;

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

    // these are the non-settle periods:
    private static final int CHAN_FCUP=0;
    private static final int CHAN_SLM=1;
    private static final int CHAN_CLOCK=2;

    // these are the settle periods (currently ignored):
    private static final int CHAN_FCUP_SETTLE=32;
    private static final int CHAN_SLM_SETTLE=33;
    private static final int CHAN_CLOCK_SETTLE=34;

    public StruckScaler() {}

    public StruckScaler(Bank bank,IndexedTable fcupTable, IndexedTable slmTable) {

        // the STRUCK's clock is 1 MHz
        this.clockFreq = 1e6;

        // this will get the last entries (most recent) in the bank
        for (int k=0; k<bank.getRows(); k++){
            if (bank.getInt("crate",k)!=CRATE) {
                continue;
            }
            if (bank.getInt("slot",k)==SLOT_GATED) {
                switch (bank.getInt("channel",k)) {
                    case CHAN_FCUP:
                        this.helicity = bank.getByte("helicity",k) > 0 ? POSITIVE : NEGATIVE;
                        this.quartet = bank.getByte("quartet",k)   > 0 ? POSITIVE : NEGATIVE;
                        this.gatedFcup = bank.getLong("value",k);
                        break;
                    case CHAN_SLM:
                        this.gatedSlm = bank.getLong("value",k);
                        break;
                    case CHAN_CLOCK:
                        this.gatedClock = bank.getLong("value",k);
                        break;
                    default:
                        break;
                }
            }
            else if (bank.getInt("slot",k)==SLOT_UNGATED) {
                switch (bank.getInt("channel",k)) {
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
        }
        if (GATEINVERTED) {
            gatedSlm = slm - gatedSlm;
            gatedFcup = fcup - gatedFcup;
            gatedClock = clock - gatedClock;
        }

        this.calibrate(fcupTable,slmTable);
    }
}