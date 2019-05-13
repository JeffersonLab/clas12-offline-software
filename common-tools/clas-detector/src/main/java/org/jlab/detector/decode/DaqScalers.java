package org.jlab.detector.decode;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * Read the occasional scaler bank, extract beam charge, livetime, etc.
 *
 * We have at least two relevant scaler hardware boards, STRUCK and DSC2, both
 * readout on helicity flips and with DAQ-busy gating, both decoded into RAW::scaler.
 * This class reads RAW::scaler and converts to more user-friendly information.
 *
 * STRUCK.  Latching on helicity states, zeroed upon readout, with both helicity
 * settle (normally 500 us) and non-settle counts, useful for "instantaneous"
 * livetime, beam charge asymmetry, beam trip studies, ...
 *
 * DSC2.  Integrating since beginning of run, useful for beam charge normalization.
 *
 * @see <a href="https://logbooks.jlab.org/comment/14616">logbook entry</a>
 * and common-tools/clas-detector/doc
 *
 * The EPICS equation for converting Faraday Cup raw scaler S to beam current I:
 *   I [nA] = (S [Hz] - offset ) / slope * attenuation;
 *
 * offset/slope/attenuation are read from CCDB
 *
 * Accounting for the offset in accumulated beam charge requires knowledge of
 * time duration.  Currently, the (32 bit) DSC2 clock is zeroed at run start
 * but at 1 Mhz rolls over every 35 seconds, and the (48 bit) 250 MHz TI timestamp
 * can also rollover within a run since only zeroed upon reboot.  Instead we allow
 * run duration to be passed in, e.g. using run start time from RCDB and event
 * unix time from RUN::config.
 *
 * FIXME:  Use CCDB for GATEINVERTED, CLOCK_FREQ, CRATE/SLOT/CHAN
 *
 * @author baltzell
 */
public class DaqScalers {

    private float beamCharge=0;
    private float beamChargeGated=0;
    private float livetime=0;
    public Dsc2RawReading dsc2=null;
    public StruckRawReading struck=null;
    private void setBeamCharge(float q) { this.beamCharge=q; }
    private void setBeamChargeGated(float q) { this.beamChargeGated=q; }
    private void setLivetime(float l) { this.livetime=l; }
    public float getBeamCharge() { return beamCharge; }
    public float getBeamChargeGated() { return beamChargeGated; }
    public float getLivetime()   { return livetime; }
    public void show() { System.out.println("BCG=%.3f   LT=%.3f"); }

    /**
    * @param rawScalerBank HIPO RAW::scaler bank
    * @param fcupTable /runcontrol/fcup IndexedTable from CCDB
    * @param seconds duration between run start and current event
    */
    public static DaqScalers create(Bank rawScalerBank,IndexedTable fcupTable,double seconds) {

        StruckRawReading struck = new StruckRawReading(rawScalerBank);
        Dsc2RawReading dsc2 = new Dsc2RawReading(rawScalerBank);

        // retrieve fcup calibrations:
        final double fcup_slope  = fcupTable.getDoubleValue("slope",0,0,0);
        final double fcup_offset = fcupTable.getDoubleValue("offset",0,0,0);
        final double fcup_atten  = fcupTable.getDoubleValue("atten",0,0,0);

        if (dsc2.getClock() > 0) {

            float live = dsc2.getGatedSlm() / dsc2.getSlm();
            float q  = (float)(dsc2.getFcup()      - fcup_offset * seconds );
            float qg = (float)(dsc2.getGatedFcup() - fcup_offset * seconds * live);
            q  *= fcup_atten / fcup_slope;
            qg *= fcup_atten / fcup_slope;
            float l = -1;
            if (struck.getClock()>0) {
                l = (float)struck.getGatedClock() / struck.getClock();
            }
            DaqScalers ds=new DaqScalers();
            ds.setBeamCharge(q);
            ds.setBeamChargeGated(qg);
            ds.setLivetime(l);
            ds.dsc2=dsc2;
            ds.struck=struck;
            return ds;
        }
        return null;
    }

    /**
    * Same as create(Bank,IndexedTable,double), except relies on DSC2's clock.
    *
    * @param rawScalerBank HIPO RAW::scaler bank
    * @param fcupTable /runcontrol/fcup IndexedTable from CCDB
    */
    public static DaqScalers create(Bank rawScalerBank,IndexedTable fcupTable) {
        Dsc2RawReading dsc2 = new Dsc2RawReading(rawScalerBank);
        return create(rawScalerBank,fcupTable,dsc2.getGatedClockTime());
    }

    private static class RawReading {

        protected static final String RAWBANKNAME="RAW::scaler";
        protected static final int CRATE=64;

        protected double clockFreq=1;   // Hz
        protected long fcup=-1;         // counts
        protected long clock=-1;        // counts
        protected long slm=-1;          // counts
        protected long gatedFcup=-1;    // counts
        protected long gatedClock=-1;   // counts
        protected long gatedSlm=-1;     // counts

        public long   getClock()       { return this.clock; }
        public long   getFcup()        { return this.fcup; }
        public long   getSlm()         { return this.slm; }
        public long   getGatedClock()  { return this.gatedClock; }
        public long   getGatedFcup()   { return this.gatedFcup; }
        public long   getGatedSlm()    { return this.gatedSlm; }
        public double getClockTime()   { return this.clock / this.clockFreq; }
        public double getGatedClockTime() { return this.gatedClock / this.clockFreq; }
    }

    public static class StruckRawReading extends RawReading {

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

        public StruckRawReading(Bank bank) {

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
        }
    }

    public static class Dsc2RawReading extends RawReading{

        private static final boolean GATEINVERTED=true;

        // DSC has TRG and TDC thresholds, we use only TDC here:
        private static final int SLOT=64;
        private static final int CHAN_FCUP_GATED=16;
        private static final int CHAN_SLM_GATED=17;
        private static final int CHAN_CLOCK_GATED=18;
        private static final int CHAN_FCUP=48;
        private static final int CHAN_SLM=49;
        private static final int CHAN_CLOCK=50;

        public Dsc2RawReading(Bank bank) {

            // the DSC2's clock is (currently) 1 MHz
            // FIXME:  use CCDB
            this.clockFreq=1e6;

            // this will get the last entries (most recent) in the bank
            for (int k=0; k<bank.getRows(); k++){

                //String csc = String.format("%02d %02d %02d ",bank.getInt("crate",k),bank.getInt("slot",k),bank.getInt("channel",k));
                //System.out.println(csc+bank.getLong("value",k));

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
        }

    }
}

