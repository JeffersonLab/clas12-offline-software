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
 * The EPICS equation for converting fcup scaler S to beam current I:
 *   I [nA] = (S [Hz] - offset ) / slope * attenuation;
 *
 * FIXME:  Use CCDB for:
 * FCUP_OFFSET/SLOPE/ATTEN
 * GATEINVERTED
 * CLOCKFREQ
 * CRATE/SLOT/CHAN
 *
 * @author baltzell
 */
public class EBScalers {

    public class Reading {
        public double beamCharge=0;
        public double instantBeamCharge=0;
        public double liveTime=0;
        Reading(double q,double instq,double lt) {
            beamCharge = q;
            instantBeamCharge = instq;
            liveTime = lt;
        }
    }

    // integrated beam charge:
    private static double BEAMCHARGE=0;      // nC

    // these are "instantaneous":
    private static double INST_BEAMCHARGE=0; // nC
    private static double INST_LIVETIME=0;

    // previous scaler readings:
    private static int PREV_FCUP=-1;           // counts
    private static int PREV_CLOCK=-1;          // counts
    private static int PREV_GATEDFCUP=-1;      // counts
    private static int PREV_GATEDCLOCK=-1;     // counts
    //private static double PREV_UNIXTIME=-1;    // seconds
    //private static double PREV_TITIMESTAMP=-1; // seconds

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
    private static final double FCUP_ATTEN=9.8088; // None  (beam_stop_atten)

    // whether the gating signal was inverted:
    private static final boolean GATEINVERTED=true;

    private static final double CLOCKFREQ=1e6; // Hz

    private static final boolean DEBUG=false;

    public synchronized Reading readScalers(DataEvent event,EBCCDBConstants ccdb) {
       
        // load the previous reading in case we don't find a new one:
        Reading reading=new Reading(BEAMCHARGE,INST_BEAMCHARGE,INST_LIVETIME);
        
        if (!event.hasBank(BANKNAME)) return reading;

        //double tiTimeStamp=0;
        //double unixTime=0;
        //if (event.hasBank("RUN::config")) {
        //    tiTimeStamp = (double)event.getBank("RUN::config").getLong("timestamp",0);
        //    unixTime  = (double)event.getBank("RUN::config").getInt("unixtime",0);
        //    tiTimeStamp *= 4/1e9; // 4-ns cycles, convert to seconds
        //}

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
        if (gatedFcup<0 || fcup<0 || gatedClock<0 || clock<=0) return reading;

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
        //final double delTimeStamp = tiTimeStamp - PREV_TITIMESTAMP;
        
        // convert clock counts to seconds:
        final double clockTime = (double)clock / CLOCKFREQ; // seconds
        final double delClockTime = (double)delClock / CLOCKFREQ; // seconds

        // update the latest calculations:
        INST_LIVETIME = (double)delGatedClock / delClock;
        INST_BEAMCHARGE = ((double)delGatedFcup/delClockTime-FCUP_OFFSET) / FCUP_SLOPE;
        BEAMCHARGE = ((double)gatedFcup/clockTime-FCUP_OFFSET) / FCUP_SLOPE;
       
        // convert from average-nA to integrated-nC:
        INST_BEAMCHARGE *= delClockTime;
        BEAMCHARGE *= clockTime;

        // correct for beam stopper attenuation:
        INST_BEAMCHARGE *= FCUP_ATTEN;
        BEAMCHARGE *= FCUP_ATTEN;

        if (DEBUG) {
            System.err.println("--------------------------------------------------------------------");
            System.err.println("FCUP = "+fcup+"/"+gatedFcup+"/"+delFcup+"/"+delGatedFcup);
            System.err.println("CLOCK= "+clock+"/"+gatedClock+"/"+delClock+"/"+delGatedClock);
            System.err.println("TIME = "+delClockTime+"/"+clockTime);
            System.err.println(String.format("Q    = %.3f/%.3f ",INST_BEAMCHARGE,BEAMCHARGE));
            System.err.println(String.format("LT   = %.3f ",INST_LIVETIME));
            System.err.println("--------------------------------------------------------------------");
            System.err.println(INST_BEAMCHARGE / delClockTime);
        }
        
        if (clock<PREV_CLOCK) {
            System.out.println("EBScalers:  *** WARNING ***  Misordered RAW::Scaler");
        }
        
        // update the previous scaler readings:
        PREV_FCUP = fcup;
        PREV_CLOCK = clock;
        PREV_GATEDFCUP = gatedFcup;
        PREV_GATEDCLOCK = gatedClock;
        //PREV_TITIMESTAMP = tiTimeStamp;
        //PREV_UNIXTIME = unixTime;
      
        // return the new readings:
        reading.beamCharge=BEAMCHARGE;
        reading.instantBeamCharge=INST_BEAMCHARGE;
        reading.liveTime=INST_LIVETIME;
        return reading;
    }


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

