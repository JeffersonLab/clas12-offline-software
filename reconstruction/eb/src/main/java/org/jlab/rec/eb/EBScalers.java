package org.jlab.rec.eb;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import org.jlab.rec.eb.EBCCDBConstants;
import org.jlab.rec.eb.EBCCDBEnum;

/**
 *
 * Read the occaisonal scaler bank and store across events.
 *
 * See https://logbooks.jlab.org/comment/14616
 *
 * The EPICS fcup_slope/offset is:
 *   I [nA] = (R [Hz] - offset ) / slope
 * So, offset has units Hz, and slope has units 1/nC
 *
 * FIXME:  Use CCDB for translation table and fcup slope/offset.
 * Things to go in CCDB:
 * FCUP_OFFSET/SLOPE/ATTEN
 * GATEINVERTED
 * CLOCKFREQ
 * CRATE/SLOT/CHAN
 *
 * @author baltzell
 */
public class EBScalers {

    // integrated beam charge:
    private static double BEAMCHARGE=0;      // nC

    // these are "instantaneous":
    private static double INST_BEAMCHARGE=0; // nC
    private static double INST_LIVETIME=0;

    // previous scaler readings:
    private static int PREV_FCUP=-1;         // Counts
    private static int PREV_CLOCK=-1;        // Counts
    private static int PREV_GATEDFCUP=-1;    // Counts
    private static int PREV_GATEDCLOCK=-1;   // Counts

    // the relevant crate/slot/channel numbers for the integrating scalers:
    private static final String BANKNAME="RAW::scaler";
    private static final int CRATE=64;
    private static final int SLOT=64;
    private static final int CHAN_GATEDFCUP=0;
    private static final int CHAN_GATEDCLOCK=2;
    private static final int CHAN_FCUP=32;
    private static final int CHAN_CLOCK=34;

    // calibration constants for fcup              // units (EPICS name)
    private static final double FCUP_SLOPE=906.2;  // 1/nC  (fcup_slope)
    private static final double FCUP_OFFSET=250.0; // Hz    (fcup_offset)
    private static final double FCUP_ATTEN=1.0;    // None  (beam_stop_atten)

    // whether the gating signal was inverted:
    private static final boolean GATEINVERTED=true;

    private static final double CLOCKFREQ=1e6;// 125e6; // Hz

    private static final boolean DEBUG=false;

    /**
     * Get most recent integrated beam charge (units=nC)
     */
    public synchronized double getIntegratedBeamCharge() {
        return BEAMCHARGE;
    }
   
    /**
     * Get most recent "instantaneous" clock livetime.
     */
    public synchronized double getLiveTime() {
        return INST_LIVETIME;
    }

    /**
     * Get most recent "instantaneous" beam charge (units=nC)
     */
    public synchronized double getBeamCharge() {
        return INST_BEAMCHARGE;
    }

    public synchronized boolean readScalers(DataEvent event,EBCCDBConstants ccdb) {
        
        if (!event.hasBank(BANKNAME)) return false;

        int fcup=-1;
        int clock=-1;
        int gatedFcup=-1;
        int gatedClock=-1;

        DataBank bank = event.getBank(BANKNAME);

        for(int k=0;k<bank.rows(); k++){

            final int crate = bank.getInt("crate",k);
            final int slot  = bank.getInt("slot",k);
            final int chan  = bank.getInt("channel",k);

            // Integrating scalers:
            if (crate==CRATE && slot==SLOT) {
                switch (chan) {
                    case CHAN_GATEDFCUP:
                        gatedFcup = bank.getInt("value",k);
                        break;
                    case CHAN_GATEDCLOCK:
                        gatedClock = bank.getInt("value",k);
                        break;
                    case CHAN_CLOCK:
                        clock = bank.getInt("value",k);
                        break;
                    case CHAN_FCUP:
                        fcup = bank.getInt("value",k);
                        break;
                    default:
                        break;
                }
            }
        }

        // sanity check on all readings:
        if (gatedFcup<0 || fcup<0 || gatedClock<0 || clock<=0) return false;

        // invert the gating:
        if (GATEINVERTED) {
            gatedFcup = fcup - gatedFcup;
            gatedClock = clock - gatedClock;
        }
        
        // calculate deltas relative to previous reading:
        final int delFcup = fcup - PREV_FCUP;
        final int delClock = clock - PREV_CLOCK;
        final int delGatedFcup = gatedFcup - PREV_GATEDFCUP;
        final int delGatedClock = gatedClock - PREV_GATEDCLOCK;
        
        // convert clock counts to seconds:
        final double time = (double)clock / CLOCKFREQ; // seconds
        final double delTime = (double)delClock / CLOCKFREQ; // seconds

        // update the latest calculations:
        INST_LIVETIME = (double)delGatedClock / delClock;
        INST_BEAMCHARGE = ((double)delGatedFcup/delTime-FCUP_OFFSET) / FCUP_SLOPE;
        BEAMCHARGE = ((double)gatedFcup/time-FCUP_OFFSET) / FCUP_SLOPE;
       
        // convert from average-nA to integrated-nC:
        INST_BEAMCHARGE *= delTime;
        BEAMCHARGE *= time;

        // correct for beam stopper attenuation:
        INST_BEAMCHARGE *= FCUP_ATTEN;
        BEAMCHARGE *= FCUP_ATTEN;

        // update the previous scaler readings:
        PREV_FCUP = fcup;
        PREV_CLOCK = clock;
        PREV_GATEDFCUP = gatedFcup;
        PREV_GATEDCLOCK = gatedClock;
       
        if (DEBUG) {
            System.err.println("--------------------------------------------------------------------");
            System.err.println("FCUP = "+fcup+"/"+gatedFcup+"/"+delFcup+"/"+delGatedFcup);
            System.err.println("CLOCK= "+clock+"/"+gatedClock+"/"+delClock+"/"+delGatedClock);
            System.err.println("TIME = "+delTime+"/"+time);
            System.err.println(String.format("Q    = %.3f/%.3f ",INST_BEAMCHARGE,BEAMCHARGE));
            System.err.println(String.format("LT   = %.3f ",INST_LIVETIME));
            System.err.println("--------------------------------------------------------------------");
        }

        return true;
    }

/*
    public class EBScaler {
        public double Q=0;
        public double INSTQ=0;
        public double LT=0;
        public void EBScaler(double q,double instq,double lt) {
            Q=q;
            INSTQ=instq;
            LT=lt;
        }
    }
    //EBScaler ebs=new EBScaler(BEAMCHARGE,INST_BEAMCHARGE,INST_LIVETIME);
*/

/*
    public static final enum ScalerSignal {
        FCUP  (0, "FCUP"),
        SLM   (1, "SLM"),
        CLOCK (2, "CLOCK")
    }

    public static final enum ScalerVersion {
        GATEDTRG  (0, "GATEDTRG"),
        GATEDTDC  (1, "GATEDTDC"),
        TRG       (2, "TRG"),
        TDC       (3, "TDC")
    }
    
    public static final int NCHAN=16;
    public static final int getChannel(ScalerSignal sig, ScalerVersion ver) {
        return sig + NCHAN * ver;
    }
*/

}

