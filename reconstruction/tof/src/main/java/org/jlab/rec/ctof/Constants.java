package org.jlab.rec.ctof;

/**
 *
 * @author ziegler
 *
 */
public class Constants {

    public Constants() {
        // TODO Auto-generated constructor stub
    }

    public static boolean CSTLOADED = false;

    public static final int[] NPAD = new int[1]; // the number of paddles per
    // sector

    //public static final double[] SCBARTHICKN = new double[1]; // 3 cm
    //public static final double LSBCONVFAC = 24. / 1000.; // ns/bin
//    public static final double[] ADC_MIP = new double[1]; // From DB 800
    public static final double DEDX_MIP = 1.956; // ~2 MeV/g/cm^2
    public static final double[] PEDU = new double[1]; // L pedestal
    public static final double[] PEDD = new double[1]; // R pedestal
    public static final double[] PEDUUNC = new double[1]; // uncertainty in L
    // pedestal
    public static final double[] PEDDUNC = new double[1]; // uncertainty in R
    // pedestal
    public static final double[] ADC_MIP_UNC = new double[1]; // ADC MIP
    // uncertainty
    public static final double ADCJITTERU = 1; // ADC jitter
    public static final double ADCJITTERD = 1; // ADC jitter
    public static final double TDCJITTERU = 0; // TDC jitter
    public static final double TDCJITTERD = 0; // TDC jitter

    // HPOSBIN correction
    public static final int    HPOSBINS = 100;
    public static final double HPOSBINW = 1.0; // cm
    
    public static final double MINENERGY = 0.1; // 10th of MeV guess --> to be
    // optimized

    public static final int LSBCONVFACERROR = 0;

    public static boolean DEBUGMODE = false;

    public static final double[] TRKMATCHXPAR = new double[1]; // track matching
    // parameter for
    // coord x
    public static final double[] TRKMATCHYPAR = new double[1]; // track matching
    // parameter for
    // coord y
    public static final double[] TRKMATCHZPAR = new double[1]; // track matching
    // parameter for
    // coord z

//    public static final double DYHL = 0.8861; // Shift along beam line between
//    // high and the low pitch angles
//    // counters
//    // public static final double PCO = 19.4160; // Constant put the center of
//    // the CTOF barrel in its design position
//    public static final double PCO = 10.0; // Constant put the center of the
//    // CTOF barrel in its design
//    // position according to GEMC

    public static double CLSMATCHXPAR = 1; // x-matching parameter between
    // panels 1a and 1b
    public static double CLSMATCHYPAR = 1; // y-matching parameter between
    // panels 1a and 1b
    public static double CLSMATCHZPAR = 1; // z-matching parameter between
    // panels 1a and 1b
    public static double CLSMATCHTPAR = 1; // t-matching parameter between
    // panels 1a and 1b
    public static double CLS1ATRKMATCHXPAR = 1; // x-matching parameter between
    // panel 1a cluster and track
    // intersection with it
    public static double CLS1ATRKMATCHYPAR = 1; // y-matching parameter between
    // panel 1a cluster and track
    // intersection with it
    public static double CLS1ATRKMATCHZPAR = 1; // z-matching parameter between
    // panel 1a cluster and track
    // intersection with it
    public static double CLS1BTRKMATCHXPAR = 1; // x-matching parameter between
    // panel 1b cluster and track
    // intersection with it
    public static double CLS1BTRKMATCHYPAR = 1; // y-matching parameter between
    // panel 1b cluster and track
    // intersection with it
    public static double CLS1BTRKMATCHZPAR = 1; // z-matching parameter between
    // panel 1b cluster and track
    // intersection with it

    public static synchronized void Load() {
        if (CSTLOADED) {
            return;
        }

        NPAD[0] = 48;

//        SCBARTHICKN[0] = 3.02;

//        ADC_MIP[0] = 800;

        TRKMATCHXPAR[0] = 1; // some reasonable value needs to be put there
        TRKMATCHYPAR[0] = 1; // some reasonable value needs to be put there
        TRKMATCHZPAR[0] = 1; // some reasonable value needs to be put there

        CSTLOADED = true;
    }
}
