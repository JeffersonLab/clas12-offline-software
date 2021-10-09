package org.jlab.rec.cvt.svt;

import org.jlab.detector.geant4.v2.SVT.SVTConstants;


public class SVTParameters {

    
    /**
     * Constants used in the reconstruction
     */
    SVTParameters() {
    }
    public static boolean geoDebug = false;
    //CUTS
    public static int MAXSVTHITS = 700;
    public static int MAXSVTCROSSES = 1000;
    public static double RESIMAX = 5;

    // THRESHOLDS
    public static int initThresholds = 30;
    public static int deltaThresholds = 15;

    // RECONSTRUCTION CONSTANTS
//    public static final double RHOVTXCONSTRAINT = 1. / Math.sqrt(12.);//0.1;
//    public static final double ZVTXCONSTRAINT = 50. / Math.sqrt(12);//5cm
    public static double ETOTCUT = 10.0;
    // GEOMETRY PARAMETERS
//    public static final int[] NSECT = new int[8];
//    public static final int NSLAYR = 2;
//    public static final int NLAYR = 6;
//    public static final int NREG = 3;
      //public static final int NREG = 4;
//    public static final int NSTRIP = 256;

//    public static final int MAXNUMSECT = 18;
    
//    public static Point3D[][][] LEP = new Point3D[MAXNUMSECT][NLAYR][NSTRIP]; //left strip end point
//    public static Point3D[][][] REP = new Point3D[MAXNUMSECT][NLAYR][NSTRIP]; //right strip end point
    
//    public static double  FIDCUX = 17.35;
//    public static double  FIDCUZ = 3.75;
//    public static double  FIDPKX = 3.5;
//    public static double  FIDPKZ0 = 402.624;
//    public static double  FIDPKZ1 = 2.50;
//    public static double  OriginZ = 62.13;
    
// d    public static double[] Z0 = new double[NLAYR]; // the z-position of a BST module in the lab-frame
// d    public static double[] PHI0 = new double[NLAYR]; // the angle of the mid plane of the hybrid sensor for the first sector

//    public static double PITCH = 0.156;
//    public static double STEREOANGLE = Math.toRadians(3.); // value of stereo angle

    //----------------
    // the unit is mm		
    //----------------
//    public static final double SILICONTHICK = 0.320;    // Silicon sensor width

    //public static final double ACTIVESENWIDTH = 40.052; 
//    public static final double ACTIVESENWIDTH = 40.032;
//    public static final double STRIPTSTART = 0.048;
    
    //public static double LAYRGAP = 3.262; //<-- GEMC value--> should be 3.236
//    public static double ACTIVESENLEN = 109.955;
//    static double TOTSENLEN = 111.625;
//    static double MICROGAP = 0.112;
    //public static double DEADZNLEN = TOTSENLEN-ACTIVESENLEN; 
//    public static final double DEADZNLEN = 0.835;
    //MODULELEN = NSENSORS*(ACTIVESENLEN + 2*DEADZNLEN) + (NSENSORS - 1)*MICROGAPLEN;
    //STRIPLENMAX = MODULELEN - 2*DEADZNLEN;

    
    // RDV CHECK CHECK
    //public static final double MODULELENGTH = 3*ACTIVESENLEN+2*DEADZNLEN+2*MICROGAP; // active area for 3 sensors including inbetween dead zones
//    public static final double MODULELENGTH = 3 * ACTIVESENLEN + 4 * DEADZNLEN + 2 * MICROGAP; // active area for 3 sensors including inbetween dead zones
//    public static final double LOCZAXISROTATION = -Math.toRadians(90.);

    // CONSTANTS USED IN RECONSTRUCTION
    //---------------------------------
    public static double LIGHTVEL = 0.000299792458;       // velocity of light (mm/ns) - conversion factor from radius in mm to momentum in GeV/c 

    // selection cuts for helical tracks
    public static final double MINRADCURV = 200.00; //in cm

    // cut on Edep min;
    public static double edep_min = 0.020; //20keV=0.020
    // sum of strip numbers for valid intersection:
    //public static int sumStpNumMin = 174;
    //public static int sumStpNumMax = 259;
    public static int sumStpNumMin = 70;
    public static int sumStpNumMax = 350;

    // contants for dEdx 
    //------------------
    //public static double rho = 2.3296; 	// g/cm^3 (Si)
    static double CaThick = 0.5;
    static double RohacellThick = 2.5;
    static double Z_eff_roha = Math.pow((7.84 / 100.) * Math.pow(1, 2.94) + (64.5 / 100.) * Math.pow(6, 2.94) + (8.38 / 100.) * Math.pow(7, 2.94) + (19.12 / 100.) * Math.pow(8, 2.94), (1. / 2.94));

    // empirical scaling factor from MC
    public static double detMatZ_ov_A_timesThickn = (14. * 2 * SVTConstants.SILICONTHK / 28.0855 + (Z_eff_roha * RohacellThick / 12.0588) + 6 * CaThick / 12.0107);
    //...................
//    //Code for identifying BST in making an ID for a bst intersection
//    public static int BSTidCode = 1;

    // ----- cut based cand select
    public static double phi12cut = 35.;
    public static double phi13cut = 35.;
    public static double phi14cut = 35.;
    public static double radcut = 100.;
    public static double dzdrcut = 200.;// used to be 150

    //BST misalignments
//    public static boolean isRadialMisalignmentTest = false;
 //   public static final double RadSpecs = 0.750;

    public static final double CIRCLEFIT_MAXCHI2 = 100;

    public static final int BSTTRKINGNUMBERITERATIONS = 3;

    public static final int MAXNUMCROSSES = 50;

    public static final int MAXNUMCROSSESINMODULE = 4;

    // these are the constants for ADC to energy conversion 
    public static final int NBITSADC = 3; // 3bit adc for BST

    public static int EMAXREADOUT = 1;

    // for cosmics
    public static final double COSMICSMINRESIDUAL = 12;
    public static final double COSMICSMINRESIDUALZ = 120;

    // track list cut-off
    public static int maxNcands = 200;
    public static boolean hasWidthResolution = false;

    public static boolean ignoreErr = false;

    public static boolean areConstantsLoaded = false;

    public static boolean removeClones = true;

//    public static final double MODULEPOSFAC = 0.5; // % wrt top of  module

    public static final double PIDCUTOFF = 2.6;

    public static double MAXDISTTOTRAJXY = 5; //max xy dist to cross in cm

    public static int BSTEXCLUDEDFITREGION = 0;

    public static boolean LAYEREFFS = false;

}
