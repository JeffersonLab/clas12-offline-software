package org.jlab.rec.cvt.svt;

import java.util.ArrayList;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Triangle3D;

public class Constants {


    /**
     * Constants used in the reconstruction
     */
    Constants() {
    }

    // THRESHOLDS
    public static int initThresholds = 30;
    public static int deltaThresholds = 15;

    // RECONSTRUCTION CONSTANTS
    public static final double RHOVTXCONSTRAINT = 1. / Math.sqrt(12.);//0.1;
    public static final double ZVTXCONSTRAINT = 50. / Math.sqrt(12);//5cm
    public static double ETOTCUT = 10.0;
    // GEOMETRY PARAMETERS
    public static final int[] NSECT = new int[8];
    public static final int NSLAYR = 2;
    public static final int NLAYR = 6;
    public static final int NREG = 3;
      //public static final int NREG = 4;
    public static final int NSTRIP = 256;

    public static final int MAXNUMSECT = 18;
    // public static final int MAXNUMSECT = 24;
    public static double[][] MODULERADIUS = new double[NLAYR][MAXNUMSECT]; // the radius of a BST module w.r.t. the beam axis
    public static double[][] TX = new double[NREG][MAXNUMSECT]; // shift par
    public static double[][] TY = new double[NREG][MAXNUMSECT]; // shift par
    public static double[][] TZ = new double[NREG][MAXNUMSECT]; // shift par
    public static double[][] RX = new double[NREG][MAXNUMSECT]; // shift par
    public static double[][] RY = new double[NREG][MAXNUMSECT]; // shift par
    public static double[][] RZ = new double[NREG][MAXNUMSECT]; // shift par
    public static double[][] RA = new double[NREG][MAXNUMSECT]; // shift par
    
    public static double  FIDCUX = 17.35;
    public static double  FIDCUZ = 3.75;
    public static double  FIDPKX = 3.5;
    public static double  FIDPKZ0 = 402.624;
    public static double  FIDPKZ1 = 2.50;
    public static double  OriginZ = 62.13;
    
    public static double[] Z0 = new double[NLAYR]; // the z-position of a BST module in the lab-frame
    public static double[] PHI0 = new double[NLAYR]; // the angle of the mid plane of the hybrid sensor for the first sector

    public static double PITCH = 0.156;
    public static double STEREOANGLE = Math.toRadians(3.); // value of stereo angle

    //----------------
    // the unit is mm		
    //----------------
    public static final double SILICONTHICK = 0.320;    // Silicon sensor width

    //public static final double ACTIVESENWIDTH = 40.052; 
    public static final double ACTIVESENWIDTH = 40.032;
    public static final double STRIPTSTART = 0.048;
    public static double LAYRGAP = 3.166; //LAYRGAP = 3.547; DB:3.166
    //public static double LAYRGAP = 3.262; //<-- GEMC value--> should be 3.236
    public static double ACTIVESENLEN = 109.955;
    static double TOTSENLEN = 111.625;
    static double MICROGAP = 0.112;
    //public static double DEADZNLEN = TOTSENLEN-ACTIVESENLEN; 
    public static final double DEADZNLEN = 0.835;
    //MODULELEN = NSENSORS*(ACTIVESENLEN + 2*DEADZNLEN) + (NSENSORS - 1)*MICROGAPLEN;
    //STRIPLENMAX = MODULELEN - 2*DEADZNLEN;

    //public static final double MODULELENGTH = 3*ACTIVESENLEN+2*DEADZNLEN+2*MICROGAP; // active area for 3 sensors including inbetween dead zones
    public static final double MODULELENGTH = 3 * ACTIVESENLEN + 4 * DEADZNLEN + 2 * MICROGAP; // active area for 3 sensors including inbetween dead zones
    public static final double LOCZAXISROTATION = -Math.toRadians(90.);

    // CONSTANTS USED IN RECONSTRUCTION
    //---------------------------------
    public static double LIGHTVEL = 0.000299792458;       // velocity of light (mm/ns) - conversion factor from radius in mm to momentum in GeV/c 

    // selection cuts for helical tracks
    public static final double MINRADCURV = 200.00; //in cm

    // cut on Edep min;
    public static double edep_min = 0.020; //20keV=0.020
    // cut on strip intersection tolerance
    public static double interTol = 10.0; //10.0 = /1 cm
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
    public static double detMatZ_ov_A_timesThickn = (14. * 2 * SILICONTHICK / 28.0855 + (Z_eff_roha * RohacellThick / 12.0588) + 6 * CaThick / 12.0107);
    //...................
    //Code for identifying BST in making an ID for a bst intersection
    public static int BSTidCode = 1;

    // ----- cut based cand select
    public static double phi12cut = 35.;
    public static double phi13cut = 35.;
    public static double phi14cut = 35.;
    public static double radcut = 100.;
    public static double dzdrcut = 200.;// used to be 150

    //BST misalignments
    public static boolean isRadialMisalignmentTest = false;
    public static final double RadSpecs = 0.750;

    public static final double CIRCLEFIT_MAXCHI2 = 100;

    public static final int BSTTRKINGNUMBERITERATIONS = 3;

    public static final int MAXNUMCROSSES = 50;

    public static final int MAXNUMCROSSESINMODULE = 4;

    // these are the constants for ADC to energy conversion 
    public static final int NBITSADC = 3; // 3bit adc for BST

    public static int EMAXREADOUT = 1;

    // for cosmics
    public static final double COSMICSMINRESIDUAL = 2;
    public static final double COSMICSMINRESIDUALZ = 20;

    public static ArrayList<ArrayList<Shape3D>> MODULEPLANES;

    // track list cut-off
    public static int maxNcands = 200;
    public static boolean hasWidthResolution = false;

    public static boolean ignoreErr = false;

    public static boolean areConstantsLoaded = false;

    public static boolean removeClones = true;

    public static final double SILICONRADLEN = 9.36 * 10; //check this - converted to mm

    public static final double MODULEPOSFAC = 0.5; // % wrt top of  module

    public static final double PIDCUTOFF = 2.6;

    public static final double TOLTOMODULEEDGE = 1.0; // Tolerance for track trajectory point at layer to module fiducial edge (mm)

    public static double MAXDISTTOTRAJXY = 5; //max xy dist to cross in cm

    public static int BSTEXCLUDEDFITREGION = 0;

    public static boolean newGeometry = true;

    public static boolean LAYEREFFS = false;

    public static synchronized void Load() {
        if (areConstantsLoaded) {
            return;
        }

        NSECT[0] = 10;
        NSECT[1] = 10;
        NSECT[2] = 14;
        NSECT[3] = 14;
        NSECT[4] = 18;
        NSECT[5] = 18;
       // NSECT[6] = 24;
       // NSECT[7] = 24;

        // the values of the z0 position of the BST module local coordinate system
        // in the lab frame coordinate system (from gemc geometry file), for each of the regions:
        /*
		Z0[0] = -219.856 + 0.5*DEADZNLEN;
		Z0[1] = -219.856 + 0.5*DEADZNLEN;
		Z0[2] = -180.490 + 0.5*DEADZNLEN;
		Z0[3] = -180.490 + 0.5*DEADZNLEN;
		Z0[4] = -141.530 + 0.5*DEADZNLEN;
		Z0[5] = -141.530 + 0.5*DEADZNLEN;
		Z0[6] =  -83.406 + 0.5*DEADZNLEN;
		Z0[7] =  -83.406 + 0.5*DEADZNLEN;
         */
        Z0[0] = -219.826 + 0. * DEADZNLEN;
        Z0[1] = -219.826 + 0. * DEADZNLEN;
        Z0[2] = -180.380 + 0. * DEADZNLEN;
        Z0[3] = -180.380 + 0. * DEADZNLEN;
        Z0[4] = -141.206 + 0. * DEADZNLEN;
        Z0[5] = -141.206 + 0. * DEADZNLEN;
        //Z0[6] = -83.405 + 0. * DEADZNLEN;
        //Z0[7] = -83.405 + 0. * DEADZNLEN;

        //Z0[0]=-219.826; Z0[1]=Z0[0];
        //Z0[2]=-180.38;  Z0[3]=Z0[2];
        //Z0[4]=-141.206; Z0[5]=Z0[4];
        //Z0[6]=-83.405;  Z0[7]=Z0[6];
        double rotationFlag = 1;// in hardware the tracker is rotated by an  180 degrees in azimuth
        PHI0[0] = Math.toRadians(90. + 180. * rotationFlag);
        PHI0[1] = Math.toRadians(90. + 180. * rotationFlag);
        PHI0[2] = Math.toRadians(90. + 180. * rotationFlag);
        PHI0[3] = Math.toRadians(90. + 180. * rotationFlag);
        PHI0[4] = Math.toRadians(90. + 180. * rotationFlag);
        PHI0[5] = Math.toRadians(90. + 180. * rotationFlag);
       // PHI0[6] = Math.toRadians(90. + 180. * rotationFlag);
        //PHI0[7] = Math.toRadians(90. + 180. * rotationFlag);

        /*
		for(int s = 0; s <NSECT[0]; s++) {
			MODULERADIUS[0][s] = 65.285 - MODULEPOSFAC*SILICONTHICK;
		}
		for(int s = 0; s <NSECT[2]; s++) {
			MODULERADIUS[2][s] = 92.945 - MODULEPOSFAC*SILICONTHICK;
		}
		for(int s = 0; s <NSECT[4]; s++) {
			MODULERADIUS[4][s] = 120.365 - MODULEPOSFAC*SILICONTHICK;
		}
		for(int s = 0; s <NSECT[6]; s++) {
			MODULERADIUS[6][s] = 161.275 - MODULEPOSFAC*SILICONTHICK;
		}
		for(int s = 0; s <NSECT[1]; s++) {
			MODULERADIUS[1][s] = 68.832 + MODULEPOSFAC*SILICONTHICK ;
		}
		for(int s = 0; s <NSECT[3]; s++) {
			MODULERADIUS[3][s] = 96.492 + MODULEPOSFAC*SILICONTHICK;
		}
		for(int s = 0; s <NSECT[5]; s++) {
			MODULERADIUS[5][s] = 123.912 + MODULEPOSFAC*SILICONTHICK;
		}
		for(int s = 0; s <NSECT[7]; s++) {
			MODULERADIUS[7][s] = 164.822 + MODULEPOSFAC*SILICONTHICK;	
		}
         */
        for (int s = 0; s < NSECT[0]; s++) {
            MODULERADIUS[0][s] = 65.447 - MODULEPOSFAC * SILICONTHICK;
        }
        for (int s = 0; s < NSECT[2]; s++) {
            MODULERADIUS[2][s] = 93.047 - MODULEPOSFAC * SILICONTHICK;
        }
        for (int s = 0; s < NSECT[4]; s++) {
            MODULERADIUS[4][s] = 120.482 - MODULEPOSFAC * SILICONTHICK;
        }
        //for (int s = 0; s < NSECT[6]; s++) {
        //    MODULERADIUS[6][s] = 161.362 - MODULEPOSFAC * SILICONTHICK;
        //}

        for (int s = 0; s < NSECT[1]; s++) {
            MODULERADIUS[1][s] = 65.447 + LAYRGAP + MODULEPOSFAC * SILICONTHICK;
        }
        for (int s = 0; s < NSECT[3]; s++) {
            MODULERADIUS[3][s] = 93.047 + LAYRGAP + MODULEPOSFAC * SILICONTHICK;
        }
        for (int s = 0; s < NSECT[5]; s++) {
            MODULERADIUS[5][s] = 120.482 + LAYRGAP + MODULEPOSFAC * SILICONTHICK;
        }
        //for (int s = 0; s < NSECT[7]; s++) {
        //    MODULERADIUS[7][s] = 161.362 + LAYRGAP + MODULEPOSFAC * SILICONTHICK;
        //}
        LAYRGAP = MODULERADIUS[1][0] - MODULERADIUS[0][0];

        // SHIFTS
        
        TX[0][0] = 0.177;	TY[0][0] = 0.184;	TZ[0][0] = 0.183;	RX[0][0] = 0.312;	RY[0][0] = 0;	RZ[0][0] = 0.95;	RA[0][0] = 0.164;
        TX[0][1] = 0.134;	TY[0][1] = 0.1;	TZ[0][1] = 0.19;	RX[0][1] = 0.243;	RY[0][1] = -0.177;	RZ[0][1] = 0.954;	RA[0][1] = 0.102;
        TX[0][2] = 0.14;	TY[0][2] = -0.083;	TZ[0][2] = 0.163;	RX[0][2] = 0.017;	RY[0][2] = -0.052;	RZ[0][2] = 0.999;	RA[0][2] = 0.106;
        TX[0][3] = 0.023;	TY[0][3] = -0.079;	TZ[0][3] = 0.148;	RX[0][3] = 0.103;	RY[0][3] = 0.317;	RZ[0][3] = 0.943;	RA[0][3] = 0.099;
        TX[0][4] = -0.073;	TY[0][4] = -0.04;	TZ[0][4] = 0.143;	RX[0][4] = 0.395;	RY[0][4] = 0.287;	RZ[0][4] = 0.872;	RA[0][4] = 0.13;
        TX[0][5] = -0.097;	TY[0][5] = 0.074;	TZ[0][5] = 0.107;	RX[0][5] = 0.565;	RY[0][5] = 0;	RZ[0][5] = 0.825;	RA[0][5] = 0.148;
        TX[0][6] = -0.073;	TY[0][6] = 0.069;	TZ[0][6] = 0.052;	RX[0][6] = 0.097;	RY[0][6] = -0.071;	RZ[0][6] = -0.993;	RA[0][6] = 0.372;
        TX[0][7] = 0.033;	TY[0][7] = 0.336;	TZ[0][7] = 0.094;	RX[0][7] = 0.05;	RY[0][7] = -0.154;	RZ[0][7] = 0.987;	RA[0][7] = 0.145;
        TX[0][8] = 0.034;	TY[0][8] = 0.325;	TZ[0][8] = 0.111;	RX[0][8] = 0;	RY[0][8] = 0;	RZ[0][8] = 0;	RA[0][8] = 0;
        TX[0][9] = 0.127;	TY[0][9] = 0.289;	TZ[0][9] = 0.164;	RX[0][9] = 0.326;	RY[0][9] = 0.237;	RZ[0][9] = 0.915;	RA[0][9] = 0.117;
        TX[1][0] = 0.13;	TY[1][0] = 0.342;	TZ[1][0] = 0.186;	RX[1][0] = -0.205;	RY[1][0] = 0;	RZ[1][0] = 0.979;	RA[1][0] = 0.106;
        TX[1][1] = 0.105;	TY[1][1] = 0.261;	TZ[1][1] = 0.186;	RX[1][1] = 0;	RY[1][1] = 0;	RZ[1][1] = 0;	RA[1][1] = 0;
        TX[1][2] = 0.027;	TY[1][2] = 0.218;	TZ[1][2] = 0.203;	RX[1][2] = 0;	RY[1][2] = 0;	RZ[1][2] = 0;	RA[1][2] = 0;
        TX[1][3] = -0.045;	TY[1][3] = 0.161;	TZ[1][3] = 0.183;	RX[1][3] = 0;	RY[1][3] = 0;	RZ[1][3] = 0;	RA[1][3] = 0;
        TX[1][4] = -0.005;	TY[1][4] = -0.166;	TZ[1][4] = 0.108;	RX[1][4] = 0;	RY[1][4] = 0;	RZ[1][4] = 0;	RA[1][4] = 0;
        TX[1][5] = -0.045;	TY[1][5] = -0.111;	TZ[1][5] = 0.115;	RX[1][5] = 0;	RY[1][5] = 0;	RZ[1][5] = 0;	RA[1][5] = 0;
        TX[1][6] = -0.14;	TY[1][6] = -0.098;	TZ[1][6] = 0.069;	RX[1][6] = 0.171;	RY[1][6] = 0.083;	RZ[1][6] = 0.982;	RA[1][6] = 0.098;
        TX[1][7] = -0.153;	TY[1][7] = 0.018;	TZ[1][7] = 0.079;	RX[1][7] = 0.227;	RY[1][7] = 0;	RZ[1][7] = 0.974;	RA[1][7] = 0.107;
        TX[1][8] = -0.181;	TY[1][8] = 0.298;	TZ[1][8] = 0.175;	RX[1][8] = -0.274;	RY[1][8] = 0.132;	RZ[1][8] = 0.953;	RA[1][8] = 0.1;
        TX[1][9] = -0.081;	TY[1][9] = 0.201;	TZ[1][9] = 0.085;	RX[1][9] = 0;	RY[1][9] = 0;	RZ[1][9] = 0;	RA[1][9] = 0;
        TX[1][10] = -0.064;	TY[1][10] = 0.295;	TZ[1][10] = 0.072;	RX[1][10] = 0;	RY[1][10] = 0;	RZ[1][10] = 0;	RA[1][10] = 0;
        TX[1][11] = -0.024;	TY[1][11] = 0.379;	TZ[1][11] = 0.09;	RX[1][11] = 0;	RY[1][11] = 0;	RZ[1][11] = 0;	RA[1][11] = 0;
        TX[1][12] = 0.03;	TY[1][12] = 0.37;	TZ[1][12] = 0.116;	RX[1][12] = 0;	RY[1][12] = 0;	RZ[1][12] = 0;	RA[1][12] = 0;
        TX[1][13] = 0.034;	TY[1][13] = 0.357;	TZ[1][13] = 0.162;	RX[1][13] = -0.104;	RY[1][13] = -0.05;	RZ[1][13] = 0.993;	RA[1][13] = 0.082;
        TX[2][0] = 0.066;	TY[2][0] = 0.14;	TZ[2][0] = 0.13;	RX[2][0] = -0.034;	RY[2][0] = 0;	RZ[2][0] = 0.999;	RA[2][0] = 0.139;
        TX[2][1] = 0.082;	TY[2][1] = 0.077;	TZ[2][1] = 0.14;	RX[2][1] = 0;	RY[2][1] = 0;	RZ[2][1] = 0;	RA[2][1] = 0;
        TX[2][2] = 0.068;	TY[2][2] = 0.03;	TZ[2][2] = 0.179;	RX[2][2] = 0;	RY[2][2] = 0;	RZ[2][2] = 0;	RA[2][2] = 0;
        TX[2][3] = 0.17;	TY[2][3] = -0.156;	TZ[2][3] = 0.188;	RX[2][3] = 0;	RY[2][3] = 0;	RZ[2][3] = 0;	RA[2][3] = 0;
        TX[2][4] = 0.11;	TY[2][4] = -0.212;	TZ[2][4] = 0.181;	RX[2][4] = -0.016;	RY[2][4] = 0.092;	RZ[2][4] = 0.996;	RA[2][4] = 0.091;
        TX[2][5] = 0.068;	TY[2][5] = -0.212;	TZ[2][5] = 0.18;	RX[2][5] = 0.038;	RY[2][5] = 0.213;	RZ[2][5] = 0.976;	RA[2][5] = 0.082;
        TX[2][6] = 0.003;	TY[2][6] = -0.193;	TZ[2][6] = 0.197;	RX[2][6] = 0.113;	RY[2][6] = 0.196;	RZ[2][6] = 0.974;	RA[2][6] = 0.084;
        TX[2][7] = -0.073;	TY[2][7] = -0.166;	TZ[2][7] = 0.204;	RX[2][7] = 0.09;	RY[2][7] = 0.075;	RZ[2][7] = 0.993;	RA[2][7] = 0.082;
        TX[2][8] = -0.146;	TY[2][8] = -0.153;	TZ[2][8] = 0.191;	RX[2][8] = 0.157;	RY[2][8] = 0.057;	RZ[2][8] = 0.986;	RA[2][8] = 0.098;
        TX[2][9] = -0.198;	TY[2][9] = -0.09;	TZ[2][9] = 0.203;	RX[2][9] = 0.061;	RY[2][9] = 0;	RZ[2][9] = 0.998;	RA[2][9] = 0.189;
        TX[2][10] = -0.201;	TY[2][10] = 0.029;	TZ[2][10] = 0.147;	RX[2][10] = 0.137;	RY[2][10] = -0.05;	RZ[2][10] = 0.989;	RA[2][10] = 0.089;
        TX[2][11] = -0.15;	TY[2][11] = 0.05;	TZ[2][11] = 0.187;	RX[2][11] = 0.073;	RY[2][11] = -0.061;	RZ[2][11] = 0.995;	RA[2][11] = 0.096;
        TX[2][12] = -0.176;	TY[2][12] = 0.166;	TZ[2][12] = 0.155;	RX[2][12] = -0.017;	RY[2][12] = 0.029;	RZ[2][12] = 0.999;	RA[2][12] = 0.094;
        TX[2][13] = -0.228;	TY[2][13] = 0.285;	TZ[2][13] = 0.156;	RX[2][13] = 0;	RY[2][13] = 0;	RZ[2][13] = 0;	RA[2][13] = 0;
        TX[2][14] = -0.194;	TY[2][14] = 0.275;	TZ[2][14] = 0.143;	RX[2][14] = -0.027;	RY[2][14] = -0.152;	RZ[2][14] = 0.988;	RA[2][14] = 0.09;
        TX[2][15] = -0.145;	TY[2][15] = 0.3;	TZ[2][15] = 0.119;	RX[2][15] = 0;	RY[2][15] = 0;	RZ[2][15] = 0;	RA[2][15] = 0;
        TX[2][16] = -0.069;	TY[2][16] = 0.255;	TZ[2][16] = 0.119;	RX[2][16] = -0.071;	RY[2][16] = -0.059;	RZ[2][16] = 0.996;	RA[2][16] = 0.123;
        TX[2][17] = 0.035;	TY[2][17] = 0.229;	TZ[2][17] = 0.109;	RX[2][17] = -0.059;	RY[2][17] = -0.022;	RZ[2][17] = 0.998;	RA[2][17] = 0.087;
       /* TX[3][0] = -0.191;	TY[3][0] = -0.17;	TZ[3][0] = -0.136;	RX[3][0] = -0.876;	RY[3][0] = 0;	RZ[3][0] = -0.481;	RA[3][0] = 0.089;
        TX[3][1] = -0.196;	TY[3][1] = -0.123;	TZ[3][1] = -0.137;	RX[3][1] = -0.689;	RY[3][1] = 0.185;	RZ[3][1] = -0.701;	RA[3][1] = 0.105;
        TX[3][2] = -0.081;	TY[3][2] = -0.162;	TZ[3][2] = -0.162;	RX[3][2] = -0.374;	RY[3][2] = 0.216;	RZ[3][2] = -0.902;	RA[3][2] = 0.118;
        TX[3][3] = -0.106;	TY[3][3] = -0.103;	TZ[3][3] = -0.151;	RX[3][3] = -0.185;	RY[3][3] = 0.185;	RZ[3][3] = -0.965;	RA[3][3] = 0.09;
        TX[3][4] = -0.03;	TY[3][4] = -0.125;	TZ[3][4] = -0.189;	RX[3][4] = 0.02;	RY[3][4] = -0.035;	RZ[3][4] = -0.999;	RA[3][4] = 0.118;
        TX[3][5] = -0.013;	TY[3][5] = -0.038;	TZ[3][5] = -0.213;	RX[3][5] = 0.067;	RY[3][5] = -0.249;	RZ[3][5] = -0.966;	RA[3][5] = 0.106;
        TX[3][6] = 0.068;	TY[3][6] = -0.03;	TZ[3][6] = -0.207;	RX[3][6] = 0;	RY[3][6] = 0;	RZ[3][6] = 0;	RA[3][6] = 0;
        TX[3][7] = 0.115;	TY[3][7] = -0.053;	TZ[3][7] = -0.258;	RX[3][7] = 0;	RY[3][7] = 0;	RZ[3][7] = 0;	RA[3][7] = 0;
        TX[3][8] = 0.169;	TY[3][8] = -0.028;	TZ[3][8] = -0.268;	RX[3][8] = 0;	RY[3][8] = 0;	RZ[3][8] = 0;	RA[3][8] = 0;
        TX[3][9] = 0.211;	TY[3][9] = -0.099;	TZ[3][9] = -0.291;	RX[3][9] = 0;	RY[3][9] = 0;	RZ[3][9] = 0;	RA[3][9] = 0;
        TX[3][10] = 0.23;	TY[3][10] = -0.162;	TZ[3][10] = -0.323;	RX[3][10] = 0;	RY[3][10] = 0;	RZ[3][10] = 0;	RA[3][10] = 0;
        TX[3][11] = 0.237;	TY[3][11] = -0.169;	TZ[3][11] = -0.398;	RX[3][11] = 0;	RY[3][11] = 0;	RZ[3][11] = 0;	RA[3][11] = 0;
        TX[3][12] = 0.307;	TY[3][12] = -0.19;	TZ[3][12] = -0.374;	RX[3][12] = -0.128;	RY[3][12] = 0;	RZ[3][12] = -0.992;	RA[3][12] = 0.093;
        TX[3][13] = 0.295;	TY[3][13] = -0.221;	TZ[3][13] = -0.409;	RX[3][13] = 0;	RY[3][13] = 0;	RZ[3][13] = 0;	RA[3][13] = 0;
        TX[3][14] = 0.263;	TY[3][14] = -0.208;	TZ[3][14] = -0.334;	RX[3][14] = -0.192;	RY[3][14] = 0.111;	RZ[3][14] = -0.975;	RA[3][14] = 0.114;
        TX[3][15] = 0.199;	TY[3][15] = -0.213;	TZ[3][15] = -0.384;	RX[3][15] = -0.108;	RY[3][15] = 0.108;	RZ[3][15] = -0.988;	RA[3][15] = 0.147;
        TX[3][16] = 0.159;	TY[3][16] = -0.322;	TZ[3][16] = -0.333;	RX[3][16] = -0.096;	RY[3][16] = 0.166;	RZ[3][16] = -0.982;	RA[3][16] = 0.083;
        TX[3][17] = 0.046;	TY[3][17] = -0.292;	TZ[3][17] = -0.286;	RX[3][17] = 0.017;	RY[3][17] = -0.063;	RZ[3][17] = -0.998;	RA[3][17] = 0.099;
        TX[3][18] = -0.028;	TY[3][18] = -0.306;	TZ[3][18] = -0.276;	RX[3][18] = 0;	RY[3][18] = 0;	RZ[3][18] = 0;	RA[3][18] = 0;
        TX[3][19] = -0.086;	TY[3][19] = -0.288;	TZ[3][19] = -0.228;	RX[3][19] = 0;	RY[3][19] = 0;	RZ[3][19] = 0;	RA[3][19] = 0;
        TX[3][20] = -0.12;	TY[3][20] = -0.292;	TZ[3][20] = -0.218;	RX[3][20] = 0;	RY[3][20] = 0;	RZ[3][20] = 0;	RA[3][20] = 0;
        TX[3][21] = -0.128;	TY[3][21] = -0.268;	TZ[3][21] = -0.202;	RX[3][21] = -0.498;	RY[3][21] = -0.498;	RZ[3][21] = -0.71;	RA[3][21] = 0.091;
        TX[3][22] = -0.174;	TY[3][22] = -0.268;	TZ[3][22] = -0.163;	RX[3][22] = 0;	RY[3][22] = 0;	RZ[3][22] = 0;	RA[3][22] = 0;
        TX[3][23] = -0.181;	TY[3][23] = -0.225;	TZ[3][23] = -0.171;	RX[3][23] = 0;	RY[3][23] = 0;	RZ[3][23] = 0;	RA[3][23] = 0;
        */
       
                        
        {
            ArrayList<ArrayList<Shape3D>> modules = new ArrayList<ArrayList<Shape3D>>();
            Geometry geo = new Geometry();

            for (int layer = 1; layer <= 8; layer++) {
                ArrayList<Shape3D> layerModules = new ArrayList<Shape3D>();

                for (int sector = 1; sector <= Constants.NSECT[layer - 1]; sector++) {

                    Shape3D module = new Shape3D();

                    Point3D PlaneModuleOrigin = geo.getPlaneModuleOrigin(sector, layer);
                    double x0 = PlaneModuleOrigin.x();
                    double y0 = PlaneModuleOrigin.y();
                    Point3D PlaneModuleEnd = geo.getPlaneModuleEnd(sector, layer);
                    double x1 = PlaneModuleEnd.x();
                    double y1 = PlaneModuleEnd.y();

                    double[] z = new double[6];
                    z[0] = PlaneModuleOrigin.z();
                    z[1] = PlaneModuleOrigin.z() + Constants.ACTIVESENLEN;
                    z[2] = PlaneModuleOrigin.z() + Constants.ACTIVESENLEN + 1 * Constants.DEADZNLEN;
                    z[3] = PlaneModuleOrigin.z() + 2 * Constants.ACTIVESENLEN + 1 * Constants.DEADZNLEN;
                    z[4] = PlaneModuleOrigin.z() + 2 * Constants.ACTIVESENLEN + 2 * Constants.DEADZNLEN;
                    z[5] = PlaneModuleOrigin.z() + 3 * Constants.ACTIVESENLEN + 2 * Constants.DEADZNLEN;

                    for (int i = 0; i < 5; i++) {
                        Point3D ModulePoint1 = new Point3D(x0, y0, z[i]);
                        Point3D ModulePoint2 = new Point3D(x1, y1, z[i]);
                        Point3D ModulePoint3 = new Point3D(x0, y0, z[i + 1]);
                        Point3D ModulePoint4 = new Point3D(x1, y1, z[i + 1]);

                        Triangle3D Module1 = new Triangle3D(ModulePoint1, ModulePoint2, ModulePoint4);
                        Triangle3D Module2 = new Triangle3D(ModulePoint1, ModulePoint3, ModulePoint4);

                        module.addFace(Module1);
                        module.addFace(Module2);
                    }

                    layerModules.add(module);
                }
                modules.add(layer - 1, layerModules);
            }
            MODULEPLANES = modules;
        }

        areConstantsLoaded = true;
        System.out.println(" SVT geometry constants loaded ? " + areConstantsLoaded);

    }

}
