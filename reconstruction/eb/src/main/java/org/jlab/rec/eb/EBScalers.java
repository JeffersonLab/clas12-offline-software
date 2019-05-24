package org.jlab.rec.eb;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
/**
 *
 * Read the occaisonal scaler bank and store across events.
 *
 * @deprecated Moved to decoding, see org.jlab.detector.decode.DaqScalers
 *
 * See https://logbooks.jlab.org/comment/14616
 *
 * The EPICS equation for converting fcup scaler S to beam current I:
 *   I [nA] = (S [Hz] - offset ) / slope * attenuation;
 *
 * The (32 bit) 1 MHz DSC2 clock rolls over every ~35 minutes.  We
 * instead try to use the (48 bit) TI 4-ns timestamp, by storing the
 * first timestamp in the run.
 *
 * Probably this will have to be done post-processing, e.g. in trains,
 * where we can process the full run in one shot.
 *
 * FIXME:  Use CCDB for GATEINVERTED, CLOCKFREQ, CRATE/SLOT/CHAN
 *
 * @author baltzell
 */
public class EBScalers {

    private RawReading firstRawReading=null;
    private RawReading prevRawReading=new RawReading();
    private Reading prevReading=new Reading();

    public static class Reading {
        private double beamCharge=0;
        private double liveTime=0;
        public void setBeamCharge(double x) { beamCharge=x; }
        public void setLiveTime(double x)   { liveTime=x; }
        public double getBeamCharge()       { return beamCharge; }
        public double getLiveTime()         { return liveTime; }
        public void show() { System.out.println("BCG=%.3f   LT=%.3f"); }
    }

    private static class RawReading {
        
        private static final String SCALERBANKNAME="RAW::scaler";
        private static final int CRATE=64;
        private static final int SLOT=64;
        private static final int CHAN_GATEDFCUP=0;
        private static final int CHAN_GATEDCLOCK=2;
        private static final int CHAN_FCUP=32;
        private static final int CHAN_CLOCK=34;

        // whether the gating signal was inverted:
        private static final boolean GATEINVERTED=true;

        // clock frequency for conversion from clock counts to time:
        private static final double CLOCKFREQ=1e6; // Hz

        private long fcup=0;         // counts
        private long gatedFcup=0;    // counts
        private long clock=0;        // counts
        private long gatedClock=0;   // counts
        private long tiTimeStamp=0;  // 4-nanoseconds
        private int unixTime=0;      // seconds
       
        public long   getClock()       { return this.clock; }
        public long   getFcup()        { return this.fcup; }
        public long   getGatedClock()  { return this.gatedClock; }
        public long   getGatedFcup()   { return this.gatedFcup; }
        public long   getTiTimeStamp() { return this.tiTimeStamp; }
        public int    getUnixTime()    { return this.unixTime; }
        public double getClockTime()   { return this.clock / CLOCKFREQ; }

        public void subtract(RawReading r) {
            this.fcup -= r.fcup;
            this.clock -= r.clock;
            this.gatedFcup -= r.gatedFcup;
            this.gatedClock -= r.gatedClock;
            this.unixTime -= r.unixTime;
            this.tiTimeStamp -= r.tiTimeStamp;
        }

        public void show() {
            System.out.println("FCUP = "+this.fcup+"/"+this.gatedFcup);
            System.out.println("CLOK = "+this.clock+"/"+this.gatedClock);
            System.out.println("TIME = "+this.unixTime+"/"+this.tiTimeStamp);
        }
        
        public RawReading() {}
        
        public RawReading(RawReading r) {
            this.fcup = r.fcup;
            this.clock = r.clock;
            this.gatedFcup = r.gatedFcup;
            this.gatedClock = r.gatedClock;
            this.unixTime = r.unixTime;
            this.tiTimeStamp = r.tiTimeStamp;
        }
        
        public RawReading(DataEvent event) {
            
            if (!event.hasBank(SCALERBANKNAME)) return;
            if (!event.hasBank("RUN::config")) return;
            
            tiTimeStamp=event.getBank("RUN::config").getLong("timestamp",0);
            unixTime   =event.getBank("RUN::config").getInt("unixtime",0);
            
            DataBank bank = event.getBank(SCALERBANKNAME);
            for(int k=0;k<bank.rows(); k++){
                if (bank.getInt("crate",k)==CRATE &&
                    bank.getInt("slot",k)==SLOT) {
                    switch (bank.getInt("channel",k)) {
                        case CHAN_GATEDFCUP:
                            gatedFcup = bank.getLong("value",k);
                            break;
                        case CHAN_GATEDCLOCK:
                            gatedClock = bank.getLong("value",k);
                            break;
                        case CHAN_CLOCK:
                            clock = bank.getLong("value",k);
                            break;
                        case CHAN_FCUP:
                            fcup = bank.getLong("value",k);
                            break;
                        default:
                            break;
                    }
                }
            }
            if (GATEINVERTED) {
                gatedFcup = fcup - gatedFcup;
                gatedClock = clock - gatedClock;
            }
        }
    }

    /**
     * This returns the most recent available scaler info.  If events are
     * misordered, this is the scaler info with the most recent TI timestamp.
     */
    public synchronized Reading readScalers(DataEvent event,EBCCDBConstants ccdb) {
       
        RawReading raw=new RawReading(event);
       
        if (raw.getGatedFcup()<=0 || raw.getTiTimeStamp()<=0) return prevReading;

        if (firstRawReading==null ||
            firstRawReading.getTiTimeStamp() > raw.getTiTimeStamp()) {
            firstRawReading = raw;
        }
         
        RawReading rawRelDiff = new RawReading(raw);
        rawRelDiff.subtract(prevRawReading);
     
        // ignore earlier readouts:
        if (raw.getFcup() < prevRawReading.getFcup()) return prevReading;
        if (raw.getTiTimeStamp() < prevRawReading.getTiTimeStamp()) return prevReading;

        // retrieve fcup calibrations:
        final double fcup_slope =ccdb.getDouble(EBCCDBEnum.FCUP_slope);
        final double fcup_offset=ccdb.getDouble(EBCCDBEnum.FCUP_offset);
        final double fcup_atten =ccdb.getDouble(EBCCDBEnum.FCUP_atten);

        final double livetime = (double)rawRelDiff.getGatedClock() / rawRelDiff.getClock();
        final double seconds = (raw.getTiTimeStamp() - firstRawReading.getTiTimeStamp()) * 4/1e9;
        final double beamCharge = (raw.getGatedFcup() - fcup_offset * seconds) / fcup_slope * fcup_atten;

        prevRawReading = raw;
        prevReading.setLiveTime(livetime);
        prevReading.setBeamCharge(beamCharge);
        //System.out.println("<<<<<<<<<<<<<<    >>>>>>>>>>>>>");
        //raw.show();
        //prevReading.show();
        return prevReading;
    }
}

