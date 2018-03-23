package org.jlab.rec.dc;

import java.util.ArrayList;

import cnuphys.snr.NoiseReductionParameters;

/**
 * Constants used in the reconstruction
 */
public class Constants {

	static boolean ConstantsLoaded = false;
	// RECONSTRUCTION PARAMETERS
	public static final int DC_MIN_NLAYERS = 4;
        public static final double SPEEDLIGHT = 29.97924580;
	// DATABASE VARIATION
	//public static final String DBVAR = "default";
	public static double NSUPERLAYERTRACKING = 5;
        public static double TSTARTEST = 560.;
	// GEOMETRY PARAMETERS

	// other CLAS12 parameters
	public static final  int NSECT  = 6;
	public static final  int NSLAY  = 6;
	public static final  int NSLAYR = 2;
	public static final  int NLAYR  = 6;
	public static final  int NREG   = 3;
	public static final  int NWIRE  = 114; //1 guard + 112 sense + 1 guard

	public static final double z_extrap_to_LowFieldReg = 592.; // z in cm in the region outside of DC-R3 [used for extrapolation of the track to the outer detectors]
	public static final double[] wpdist = {0.386160,0.404220,0.621906,0.658597,0.935140,0.977982};
        
	// CONSTANTS USED IN RECONSTRUCTION
	//---------------------------------
        public static final double TRIGJIT = 20;
        public static final double[] TIMEWINMINEDGE = {-25.0,-25.0,-25.0};
        public static final double[] TIMEWINMAXEDGE = {275.0,350.0,750.0};
        
        
	public static final double LIGHTVEL = 0.00299792458;        // velocity of light (cm/ns) - conversion factor from radius in cm to momentum in GeV/c

	
	/// A region-segment contains two segments if they are in the same sector
	/// and region and satisfy the proximity condition:
	/// |Xwires2-Xwires1| = a*Xwires1 + b
	/// where a and b are DC parameters set by DC_RSEG_a and DC_RSEG_B .
	public static final double DC_RSEG_A = 0.18;
	public static final double DC_RSEG_B = 5; 

	public static final double PASSINGHITRESIDUAL = 2.0;  //  refine later

	public static final double CELLRESOL = 0.0300; //300 microns = 300 * 10^-4 cm

	
	/**
	 * The minimum chi2 prob. for the fit to the hit-based tracking clusters.
	 * This value has been optimized for the local coordinate system used in hit-based tracking.
	 * Only change it if you know what you are doing....
	 */
	public static final double HITBASEDTRKGMINFITHI2PROB = 0.65;
	/**
	 * All clusters below this size are passed at hit-based tracking level; we do not attempt to split clusters with size smaller than this.
	 */
	public static final int HITBASEDTRKGNONSPLITTABLECLSSIZE = 8;

	/**
	 * The number of end cells to keep in a column of hits -- this applies to the DC-hit pruning algorithm
	 */
	public static final int DEFAULTNBENDCELLSTOKEEP = 1;
	/**
	 * The number of end cells to keep in a column of 4 hits but less than 10 hit -- this applies to the DC-hit pruning algorithm
	 */
	public static final int NBENDCELLSTOKEEPMORETHAN4HITSINCOLUMN = 2;


	public static final double TRACKSELECTQFMINCHSQ = 10000; 
	public static final double TCHISQPROBFITXZ = 0.01;

	//------------
	// ----- cut based cand select
	
	public static final double TRACKDIRTOCROSSDIRCOSANGLE =0.85;//= 0.95;
        
        public static double CROSSLISTSELECTQFMINCHSQ=2000;
        
	public static final double SEGMENTPLANESANGLE = 1.5;  // the angle between the normals to the segment fit planes is 12 degrees (6+6 for +/- stereo relative angles) + 1.5 degrees tolerance.  This number (1.5) should be optimized 

	public static final double ARGONRADLEN = 14;  // radiation length in Argon is 14 cm

	public static final  double SWIMSTEPSIZE = 5.00*1.e-4; //n00 microns

	public static final int MAXNBCROSSES = 100; // max num crosses persector

	public static final int MAXNBHITS = 350;

	public static final double MINTRKMOM = 0.050;

	public static final double MAXTRKMOM = 20.0;

	public static final int MAXCLUSSIZE = 14;
	
	public static final double MAXCHI2 = 10000;
        
        public static double HBTCHI2CUT = 10000;
	
	//public static final boolean OUTOFTIMEFLAG = true;

	//private static boolean T2DGRID ;
	//private static boolean CALIB;
	//private static double TORSCALE;
	
	private static boolean runLAYEREFFS = false;
	
	// SNR parameters -- can be optimized
	public static final  int[] SNR_RIGHTSHIFTS = {0,1,2,2,4,4};
	public static final  int[] SNR_LEFTSHIFTS  = {0,1,2,2,4,4};	
	
	
	// Arrays for combinatorial cluster compositions
        static final int[][] CombArray1Layer = new int[][]{{0},{1}};
        static final int[][] CombArray2Layers = new int[][]{{0,0},{1,0},{0,1},{1,1}};
        static final int[][] CombArray3Layers = new int[][]{{0,0,0},{1,0,0},{0,1,0},{1,1,0},{0,0,1},{1,0,1},{0,1,1},{1,1,1}};
        static final int[][] CombArray4Layers = new int[][]{{0,0,0,0},{1,0,0,0},{0,1,0,0},{1,1,0,0},{0,0,1,0},{1,0,1,0},{0,1,1,0},{1,1,1,0},{0,0,0,1},{1,0,0,1},{0,1,0,1},{1,1,0,1},{0,0,1,1},{1,0,1,1},{0,1,1,1},{1,1,1,1}};
        static final int[][] CombArray5Layers = new int[][]{{0,0,0,0,0},{1,0,0,0,0},{0,1,0,0,0},{1,1,0,0,0},{0,0,1,0,0},{1,0,1,0,0},{0,1,1,0,0},{1,1,1,0,0},{0,0,0,1,0},{1,0,0,1,0},{0,1,0,1,0},{1,1,0,1,0},{0,0,1,1,0},{1,0,1,1,0},{0,1,1,1,0},{1,1,1,1,0},{0,0,0,0,1},{1,0,0,0,1},{0,1,0,0,1},{1,1,0,0,1},{0,0,1,0,1},{1,0,1,0,1},{0,1,1,0,1},{1,1,1,0,1},{0,0,0,1,1},{1,0,0,1,1},{0,1,0,1,1},{1,1,0,1,1},{0,0,1,1,1},{1,0,1,1,1},{0,1,1,1,1},{1,1,1,1,1}};
        static final int[][] CombArray6Layers = new int[][]{{0,0,0,0,0,0},{1,0,0,0,0,0},{0,1,0,0,0,0},{1,1,0,0,0,0},{0,0,1,0,0,0},{1,0,1,0,0,0},{0,1,1,0,0,0},{1,1,1,0,0,0},{0,0,0,1,0,0},{1,0,0,1,0,0},{0,1,0,1,0,0},{1,1,0,1,0,0},{0,0,1,1,0,0},{1,0,1,1,0,0},{0,1,1,1,0,0},{1,1,1,1,0,0},{0,0,0,0,1,0},{1,0,0,0,1,0},{0,1,0,0,1,0},{1,1,0,0,1,0},{0,0,1,0,1,0},{1,0,1,0,1,0},{0,1,1,0,1,0},{1,1,1,0,1,0},{0,0,0,1,1,0},{1,0,0,1,1,0},{0,1,0,1,1,0},{1,1,0,1,1,0},{0,0,1,1,1,0},{1,0,1,1,1,0},{0,1,1,1,1,0},{1,1,1,1,1,0},{0,0,0,0,0,1},{1,0,0,0,0,1},{0,1,0,0,0,1},{1,1,0,0,0,1},{0,0,1,0,0,1},{1,0,1,0,0,1},{0,1,1,0,0,1},{1,1,1,0,0,1},{0,0,0,1,0,1},{1,0,0,1,0,1},{0,1,0,1,0,1},{1,1,0,1,0,1},{0,0,1,1,0,1},{1,0,1,1,0,1},{0,1,1,1,0,1},{1,1,1,1,0,1},{0,0,0,0,1,1},{1,0,0,0,1,1},{0,1,0,0,1,1},{1,1,0,0,1,1},{0,0,1,0,1,1},{1,0,1,0,1,1},{0,1,1,0,1,1},{1,1,1,0,1,1},{0,0,0,1,1,1},{1,0,0,1,1,1},{0,1,0,1,1,1},{1,1,0,1,1,1},{0,0,1,1,1,1},{1,0,1,1,1,1},{0,1,1,1,1,1},{1,1,1,1,1,1}};

	public static final ArrayList<int[][]> CombArray = new ArrayList<int[][]>(6);
	
	public static int[][] STBLOC;
    
        
    
	public static final synchronized void Load() {
            if (ConstantsLoaded==true)
                    return;
            CombArray.add(CombArray1Layer);
            CombArray.add(CombArray2Layers);
            CombArray.add(CombArray3Layers);
            CombArray.add(CombArray4Layers);
            CombArray.add(CombArray5Layers);
            CombArray.add(CombArray6Layers);

            STBLOC = new int[6][6];
            for(int s =0; s<6; s++) {
                STBLOC[s][0]=1;
                STBLOC[s][1]=-1;
                STBLOC[s][4]=1;
                STBLOC[s][5]=1;
            }
            
            for(int sl =2; sl<4; sl++) {
                STBLOC[2][sl]=1;
                STBLOC[3][sl]=1;
                STBLOC[4][sl]=1;
                STBLOC[0][sl]=-1;
                STBLOC[1][sl]=-1;
                STBLOC[5][sl]=-1;
            }
		
		NoiseReductionParameters.setLookForTracks(false);
		
		System.out.println("CONSTANTS LOADED!!!");
		
	}


	public static final boolean LAYEREFFS() {
		return runLAYEREFFS;
	}


	public static final void setLAYEREFFS(final boolean le) {
		runLAYEREFFS = le;
	}


	//public static final double getTORSCALE() {
	//	return TORSCALE;
	//}


	//public static final void setTORSCALE(double tORSCALE) {
	//	TORSCALE = tORSCALE;
	//}
        
   

}
