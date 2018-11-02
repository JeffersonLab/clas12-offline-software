package org.jlab.rec.ftof;

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

    public static final int[] NPAD = new int[3]; // the number of paddles per
    // sector for panel 1A, 1B,
    // and 2
    // public static final double[] ADC_MIP = new double[3]; // From DB 800 A1 &
    // 2 2000 for 1B
    public static final double DEDX_MIP = 1.956; // units = MeV/g/cm^2
    public static final double[] PEDL = new double[3]; // L pedestal
    public static final double[] PEDR = new double[3]; // R pedestal
    public static final double[] PEDLUNC = new double[3]; // uncertainty in L
    // pedestal
    public static final double[] PEDRUNC = new double[3]; // uncertainty in R
    // pedestal
    // public static final double[] ADC_MIP_UNC = new double[3]; // ADC MIP
    // uncertainty
    public static final double ADCJITTERL = 1; // ADC jitter
    public static final double ADCJITTERR = 1; // ADC jitter
    public static final double TDCJITTERL = 0; // TDC jitter
    public static final double TDCJITTERR = 0; // TDC jitter

    public static final double[][] DELTA_T = new double[3][2]; // parametrization
    // array for the
    // resolutions
    // of the
    // counters for
    // panels 1,2,3

    public static final double MINENERGY = 0.1; // 10th of MeV guess --> to be
    // optimized

    public static final int LSBCONVFACERROR = 0;

    public static boolean DEBUGMODE = false;

    public static final double[] TRKMATCHXPAR = new double[3]; // track matching
    // parameter for
    // coord x
    public static final double[] TRKMATCHYPAR = new double[3]; // track matching
    // parameter for
    // coord y
    public static final double[] TRKMATCHZPAR = new double[3]; // track matching
    // parameter for
    // coord z

    public static final double TDCMINSCALE = 140; // converted tdc value minimum
    // value
    public static final double TDCMAXSCALE = 250; // converted tdc value maximum
    // value
    public static final double ADCMIN = 100; // adc value minimum value
    public static final double ADCMAX = 10000; // adc value maximum value

    public static double CLSMATCHXPAR = 10; // x-matching parameter between
    // panels 1a and 1b
    public static double CLSMATCHYPAR = 10; // y-matching parameter between
    // panels 1a and 1b
    public static double CLSMATCHZPAR = 10; // z-matching parameter between
    // panels 1a and 1b
    public static double CLSMATCHTPAR = 10; // t-matching parameter between
    // panels 1a and 1b
    public static double CLS1ATRKMATCHXPAR = 15; // x-matching parameter between
    // panel 1a cluster and
    // track intersection with
    // it
    public static double CLS1ATRKMATCHYPAR = 10; // y-matching parameter between
    // panel 1a cluster and
    // track intersection with
    // it
    public static double CLS1ATRKMATCHZPAR = 10; // z-matching parameter between
    // panel 1a cluster and
    // track intersection with
    // it
    public static double CLS1BTRKMATCHXPAR = 15; // x-matching parameter between
    // panel 1b cluster and
    // track intersection with
    // it
    public static double CLS1BTRKMATCHYPAR = 10; // y-matching parameter between
    // panel 1b cluster and
    // track intersection with
    // it
    public static double CLS1BTRKMATCHZPAR = 10; // z-matching parameter between
    // panel 1b cluster and
    // track intersection with
    // it

    public static synchronized void Load() {
        if (CSTLOADED == true) {
            return;
        }

        NPAD[0] = 23;
        NPAD[1] = 62;
        NPAD[2] = 5;

        // ADC_MIP[0] = 800;
        // ADC_MIP[1] = 2000;
        // ADC_MIP[2] = 800;
        DELTA_T[0][0] = 5.45;
        DELTA_T[0][1] = 74.55;
        DELTA_T[1][0] = 0.90;
        DELTA_T[1][1] = 29.10;
        DELTA_T[2][0] = 5.00;
        DELTA_T[2][1] = 145.00;

        TRKMATCHXPAR[0] = 15; // some reasonable value needs to be put there
        TRKMATCHXPAR[1] = 10; // some reasonable value needs to be put there
        TRKMATCHXPAR[2] = 10; // some reasonable value needs to be put there
        TRKMATCHYPAR[0] = 15; // some reasonable value needs to be put there
        TRKMATCHYPAR[1] = 10; // some reasonable value needs to be put there
        TRKMATCHYPAR[2] = 10; // some reasonable value needs to be put there
        TRKMATCHZPAR[0] = 15; // some reasonable value needs to be put there
        TRKMATCHZPAR[1] = 10; // some reasonable value needs to be put there
        TRKMATCHZPAR[2] = 10; // some reasonable value needs to be put there

        CSTLOADED = true;
    }
}