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

    public Dsc2RawReading dsc2=null;
    public StruckRawReading struck=null;

    private long timestamp=0;
    public void setTimestamp(long timestamp) { this.timestamp=timestamp; }
    public long getTimestamp(){ return this.timestamp; }

    @Deprecated public double getBeamChargeGated() { return this.dsc2.getBeamChargeGated(); }
    @Deprecated public double getBeamCharge() { return this.dsc2.getBeamCharge(); }
    @Deprecated public double getLivetime() { return this.struck.getLivetime(); }
    
    /**
    * @param runScalerBank HIPO RUN::scaler bank
    */
    public static DaqScalers create(Bank runScalerBank) {
        DaqScalers ds=new DaqScalers();
        ds.dsc2=new Dsc2RawReading();
        for (int ii=0; ii<runScalerBank.getRows(); ii++) {
            ds.dsc2.setLivetime(runScalerBank.getFloat("livetime", ii));
            ds.dsc2.setBeamCharge(runScalerBank.getFloat("fcup",ii));
            ds.dsc2.setBeamChargeGated(runScalerBank.getFloat("fcupgated",ii));
            break; 
        }
        return ds;
    }

    /**
    * @param rawScalerBank HIPO RAW::scaler bank
    * @param fcupTable /runcontrol/fcup IndexedTable from CCDB
    * @param seconds duration between run start and current event
    */
    public static DaqScalers create(Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable,double seconds) {

        StruckRawReading struck = new StruckRawReading(rawScalerBank);
        Dsc2RawReading dsc2 = new Dsc2RawReading(rawScalerBank);

        // retrieve fcup/slm calibrations:
        final double fcup_slope  = fcupTable.getDoubleValue("slope",0,0,0);  // Hz/nA
        final double fcup_offset = fcupTable.getDoubleValue("offset",0,0,0); // Hz
        final double fcup_atten  = fcupTable.getDoubleValue("atten",0,0,0);  // attenuation
        final double slm_slope   = slmTable.getDoubleValue("slope",0,0,0);   // Hz/nA
        final double slm_offset  = slmTable.getDoubleValue("offset",0,0,0);  // Hz
        final double slm_atten   = slmTable.getDoubleValue("atten",0,0,0);   // attenuation

        if (struck.getClock() > 0 && struck.getGatedClock()>0) {
            double q  = (double)(struck.getSlm()      - slm_offset * struck.getClockSeconds() );
            double qg = (double)(struck.getGatedSlm() - slm_offset * struck.getGatedClockSeconds() );
            struck.setBeamChargeSLM(q * slm_atten / slm_slope);
            struck.setBeamChargeGatedSLM(qg * slm_atten / slm_slope);
            struck.setLivetime((double)struck.getGatedClock() / struck.getClock());
        }

        if (dsc2.getClock() > 0) {
            double live = (double)dsc2.getGatedSlm() / dsc2.getSlm();
            double q  = (double)(dsc2.getFcup()      - fcup_offset * seconds );
            double qg = (double)(dsc2.getGatedFcup() - fcup_offset * seconds * live);
            dsc2.setBeamCharge(q * fcup_atten / fcup_slope);
            dsc2.setBeamChargeGated(qg * fcup_atten / fcup_slope);
            dsc2.setLivetime(struck.getClock()>0?(double)struck.getGatedClock()/struck.getClock():-1);
        }

        if (dsc2.getClock()>0 || struck.getClock()>0) {
            DaqScalers ds=new DaqScalers();
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
    public static DaqScalers create(Bank rawScalerBank,IndexedTable fcupTable,IndexedTable slmTable) {
        Dsc2RawReading dsc2 = new Dsc2RawReading(rawScalerBank);
        return create(rawScalerBank,fcupTable,slmTable,dsc2.getGatedClockSeconds());
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
        public double getClockSeconds()   { return (double)this.clock / this.clockFreq; }
        public double getGatedClockSeconds() { return (double)this.gatedClock / this.clockFreq; }
       
        // not really "raw" anymore:
        private double beamCharge=0;
        private double beamChargeGated=0;
        private double beamChargeSLM=0;
        private double beamChargeGatedSLM=0;
        private double livetime=0;
        protected void setBeamCharge(double q) { this.beamCharge=q; }
        protected void setBeamChargeGated(double q) { this.beamChargeGated=q; }
        protected void setBeamChargeSLM(double q) { this.beamChargeSLM=q; }
        protected void setBeamChargeGatedSLM(double q) { this.beamChargeGatedSLM=q; }
        protected void setLivetime(double l) { this.livetime=l; }
        public double getBeamCharge() { return beamCharge; }
        public double getBeamChargeGated() { return beamChargeGated; }
        public double getLivetime()   { return livetime; }
        public double getBeamChargeSLM() { return beamChargeSLM; }
        public double getBeamChargeGatedSLM() { return beamChargeGatedSLM; }
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

        public StruckRawReading() {}

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

        public Dsc2RawReading() {}

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

