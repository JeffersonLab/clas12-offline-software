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

	// DATABASE VARIATION
	//public static final String DBVAR = "default";
	
	// GEOMETRY PARAMETERS

	// other CLAS12 parameters
	public static final  int NSECT  = 6;
	public static final  int NSLAY  = 6;
	public static final  int NSLAYR = 2;
	public static final  int NLAYR  = 6;
	public static final  int NREG   = 3;
	public static final  int NWIRE  = 114; //1 guard + 112 sense + 1 guard

	public static final double z_extrap_to_LowFieldReg = 592.; // z in cm in the region outside of DC-R3 [used for extrapolation of the track to the outer detectors]
	
	// CONSTANTS USED IN RECONSTRUCTION
	//---------------------------------

	public static final double LIGHTVEL = 0.00299792458;        // velocity of light (cm/ns) - conversion factor from radius in cm to momentum in GeV/c

	/**
	 * Time to distance (from GEMC) in ns
	 */
	public static final double[] TIMETODIST = new double[3];    // Gemc time to distance d = t*TtoD[regionIdx]
	/**
	 * Drift velocity per region
	 */
	public static final double[] DRIFTVEL = new double[3];


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


	public static final double TRACKSELECTQFMINCHSQ = 600; 
	public static final double TCHISQPROBFITXZ = 0.01;

	//------------
	// ----- cut based cand select
	
	public static final double TRACKDIRTOCROSSDIRCOSANGLE =0.85;//= 0.95;

	public static final double SEGMENTPLANESANGLE = 1.5;  // the angle between the normals to the segment fit planes is 12 degrees (6+6 for +/- stereo relative angles) + 1.5 degrees tolerance.  This number (1.5) should be optimized 

	public static final double ARGONRADLEN = 14;  // radiation length in Argon is 14 cm

	public static final  double SWIMSTEPSIZE = 5.00*1.e-4; //n00 microns

	public static final int MAXNBCROSSES = 30; // max num crosses persector

	public static final int MAXNBHITS = 350;

	public static final double MINTRKMOM = 0.050;

	public static final double MAXTRKMOM = 20.0;

	public static final int MAXCLUSSIZE = 14;
	
	public static final double MAXCHI2 = 10;

	public static final boolean LAYEREFFS = false;
	
	public static final boolean OUTOFTIMEFLAG = true;

	private static boolean T2DGRID ;
	private static boolean CALIB;
	private static double TORSCALE;
	
	// SNR parameters -- can be optimized
	public static final  int[] SNR_RIGHTSHIFTS = {0,1,2,2,4,4};
	public static final  int[] SNR_LEFTSHIFTS  = {0,1,2,2,4,4};	
	
	public static final boolean useNoiseAlgo =false;

	public static final boolean turnOnMicroMegas = false;

	public static final boolean useRaster = false;

	public static final double T0 =0;
	
	//public static final boolean isCalibrationRun = false;
	public static final boolean useTimeToDistanceGrid = true;
	//public static final boolean DEBUGCROSSES = false;

	// Arrays for combinatorial cluster compositions
    static final int[][] CombArray1Layer = new int[][]{{0},{1}};
    static final int[][] CombArray2Layers = new int[][]{{0,0},{1,0},{0,1},{1,1}};
    static final int[][] CombArray3Layers = new int[][]{{0,0,0},{1,0,0},{0,1,0},{1,1,0},{0,0,1},{1,0,1},{0,1,1},{1,1,1}};
    static final int[][] CombArray4Layers = new int[][]{{0,0,0,0},{1,0,0,0},{0,1,0,0},{1,1,0,0},{0,0,1,0},{1,0,1,0},{0,1,1,0},{1,1,1,0},{0,0,0,1},{1,0,0,1},{0,1,0,1},{1,1,0,1},{0,0,1,1},{1,0,1,1},{0,1,1,1},{1,1,1,1}};
    static final int[][] CombArray5Layers = new int[][]{{0,0,0,0,0},{1,0,0,0,0},{0,1,0,0,0},{1,1,0,0,0},{0,0,1,0,0},{1,0,1,0,0},{0,1,1,0,0},{1,1,1,0,0},{0,0,0,1,0},{1,0,0,1,0},{0,1,0,1,0},{1,1,0,1,0},{0,0,1,1,0},{1,0,1,1,0},{0,1,1,1,0},{1,1,1,1,0},{0,0,0,0,1},{1,0,0,0,1},{0,1,0,0,1},{1,1,0,0,1},{0,0,1,0,1},{1,0,1,0,1},{0,1,1,0,1},{1,1,1,0,1},{0,0,0,1,1},{1,0,0,1,1},{0,1,0,1,1},{1,1,0,1,1},{0,0,1,1,1},{1,0,1,1,1},{0,1,1,1,1},{1,1,1,1,1}};
    static final int[][] CombArray6Layers = new int[][]{{0,0,0,0,0,0},{1,0,0,0,0,0},{0,1,0,0,0,0},{1,1,0,0,0,0},{0,0,1,0,0,0},{1,0,1,0,0,0},{0,1,1,0,0,0},{1,1,1,0,0,0},{0,0,0,1,0,0},{1,0,0,1,0,0},{0,1,0,1,0,0},{1,1,0,1,0,0},{0,0,1,1,0,0},{1,0,1,1,0,0},{0,1,1,1,0,0},{1,1,1,1,0,0},{0,0,0,0,1,0},{1,0,0,0,1,0},{0,1,0,0,1,0},{1,1,0,0,1,0},{0,0,1,0,1,0},{1,0,1,0,1,0},{0,1,1,0,1,0},{1,1,1,0,1,0},{0,0,0,1,1,0},{1,0,0,1,1,0},{0,1,0,1,1,0},{1,1,0,1,1,0},{0,0,1,1,1,0},{1,0,1,1,1,0},{0,1,1,1,1,0},{1,1,1,1,1,0},{0,0,0,0,0,1},{1,0,0,0,0,1},{0,1,0,0,0,1},{1,1,0,0,0,1},{0,0,1,0,0,1},{1,0,1,0,0,1},{0,1,1,0,0,1},{1,1,1,0,0,1},{0,0,0,1,0,1},{1,0,0,1,0,1},{0,1,0,1,0,1},{1,1,0,1,0,1},{0,0,1,1,0,1},{1,0,1,1,0,1},{0,1,1,1,0,1},{1,1,1,1,0,1},{0,0,0,0,1,1},{1,0,0,0,1,1},{0,1,0,0,1,1},{1,1,0,0,1,1},{0,0,1,0,1,1},{1,0,1,0,1,1},{0,1,1,0,1,1},{1,1,1,0,1,1},{0,0,0,1,1,1},{1,0,0,1,1,1},{0,1,0,1,1,1},{1,1,0,1,1,1},{0,0,1,1,1,1},{1,0,1,1,1,1},{0,1,1,1,1,1},{1,1,1,1,1,1}};

	public static final ArrayList<int[][]> CombArray = new ArrayList<int[][]>(6);
	
	
	public static final synchronized void Load(boolean timeToDistanceGridSetting, boolean calibRun, double torusScale) {
		if (ConstantsLoaded==true)
			return;
		CombArray.add(CombArray1Layer);
		CombArray.add(CombArray2Layers);
		CombArray.add(CombArray3Layers);
		CombArray.add(CombArray4Layers);
		CombArray.add(CombArray5Layers);
		CombArray.add(CombArray6Layers);
		
		setT2DGRID(timeToDistanceGridSetting);
		setCALIB(calibRun);
		setTORSCALE(torusScale);
		
		NoiseReductionParameters.setLookForTracks(false);
		
//		if(Constants.isSimulation == false ) {
//			Constants.useNoiseAlgo = false;
//			Constants.useTimeToDistanceGrid =true;
//		}
		
		
		TIMETODIST[0] = 0.0053;  //in cm per ns
		TIMETODIST[1] = 0.0026;
		TIMETODIST[2] = 0.0036;
		
		System.out.println("CONSTANTS LOADED!!!");
		
	}


	public static final boolean isT2DGRID() {
		return T2DGRID;
	}


	public static final void setT2DGRID(final boolean t2dgrid) {
		T2DGRID = t2dgrid;
	}


	public static final boolean isCALIB() {
		return CALIB;
	}


	public static final void setCALIB(boolean cALIB) {
		CALIB = cALIB;
	}


	public static final double getTORSCALE() {
		return TORSCALE;
	}


	public static final void setTORSCALE(double tORSCALE) {
		TORSCALE = tORSCALE;
	}
}
