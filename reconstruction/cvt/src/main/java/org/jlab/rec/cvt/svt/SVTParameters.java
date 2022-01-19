package org.jlab.rec.cvt.svt;

import org.jlab.detector.geant4.v2.SVT.SVTConstants;


public class SVTParameters {

    
    /**
     * Constants used in the reconstruction
     */
    SVTParameters() {
    }
    
    //CUTS
    public static int MAXSVTHITS = 700;
    public static int MAXSVTCROSSES = 1000;
    public static double RESIMAX = 5;

    // THRESHOLDS
    public static int INITTHRESHOLD = 30;
    public static int DELTATHRESHOLD = 15;

    // RECONSTRUCTION CONSTANTS
    public static double ETOTCUT = 10.0;
    // cut on Edep min;
    public static double EDEPMIN = 0.020; //20keV=0.020

    // selection cuts for helical tracks
    public static final double MINRADCURV = 200.00; //in cm

    // sum of strip numbers for valid intersection:
    //public static int MINSTRIPSUM = 174;
    //public static int MAXSTRIPSUM = 259;
    public static int MINSTRIPSUM = 70;
    public static int MAXSTRIPSUM = 350;

    // contants for dEdx 
    //------------------
    //public static double rho = 2.3296; 	// g/cm^3 (Si)
    private static final double CaThick = 0.5;
    private static final double RohacellThick = 2.5;
    private static final double Z_eff_roha = Math.pow((7.84 / 100.) * Math.pow(1, 2.94) + (64.5 / 100.) * Math.pow(6, 2.94) + (8.38 / 100.) * Math.pow(7, 2.94) + (19.12 / 100.) * Math.pow(8, 2.94), (1. / 2.94));

    // empirical scaling factor from MC
    public static double detMatZ_ov_A_timesThickn = (14. * 2 * SVTConstants.SILICONTHK / 28.0855 + (Z_eff_roha * RohacellThick / 12.0588) + 6 * CaThick / 12.0107);
    //...................
//    //Code for identifying BST in making an ID for a bst intersection
//    public static int BSTidCode = 1;

    // ----- cut based cand select
    public static double PHI12CUT = 35.;
    public static double PHI13CUT = 35.;
    public static double PHI14CUT = 35.;
    public static double RADCUT = 100.;
    public static double DZDRCUT = 200.;// used to be 150

    //BST misalignments
//    public static boolean isRadialMisalignmentTest = false;
 //   public static final double RadSpecs = 0.750;

    public static final int BSTTRKINGNUMBERITERATIONS = 3;

    public static final int MAXNUMCROSSES = 50;

    public static final int MAXNUMCROSSESINMODULE = 4;

    // these are the constants for ADC to energy conversion 
    public static final int NBITSADC = 3; // 3bit adc for BST

    public static int EMAXREADOUT = 1;

    // track list cut-off
    public static int MAXNCANDS = 200;

    public static boolean IGNOREERR = false;

    public static boolean REMOVECLONES = true;

    public static final double PIDCUTOFF = 2.6;

    public static double MAXDISTTOTRAJXY = 5; //max xy dist to cross in cm

    public static int BSTEXCLUDEDFITREGION = 0;

    public static boolean LAYEREFFS = false;

}
