package org.jlab.rec.dc;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import cnuphys.snr.NoiseReductionParameters;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.rec.dc.trajectory.TrajectorySurfaces;

/**
 * Constants used in the reconstruction
 */
public class Constants {

    // private constructor for a singleton
    private Constants() {
    }
    
    // singleton
    private static Constants instance = null;
    
    /**
     * public access to the singleton
     * 
     * @return the dc constants singleton
     */
    public static Constants getInstance() {
            if (instance == null) {
                    instance = new Constants();
            }
            return instance;
    }

    
    public static Logger LOGGER = Logger.getLogger(Constants.class.getName());

    private static boolean ConstantsLoaded = false;
    
    public static boolean DEBUG = false;

    // PHYSICS CONSTANTS
    public static final double SPEEDLIGHT = 29.97924580;
    public static final double LIGHTVEL = 0.00299792458;        // velocity of light (cm/ns) - conversion factor from radius in cm to momentum in GeV/c

    // CONFIGURABLE PARAMETERS
    private String  GEOVARIATION = "default";    
    private boolean ENDPLATESBOWING = false;
    private double  WIREDIST = 0.0;
    public  int     SECTORSELECT = 0;
    public  int     NSUPERLAYERTRACKING = 5;
    private boolean USETSTART = true;
    private boolean USETIMETBETA = false;
    private boolean CHECKBETA = false;
    private int     T2D = 0;     
    private boolean USEDOUBLETS = false;
    
    public static final String DOCARES      = "/calibration/dc/signal_generation/doca_resolution";
    public static final String TIME2DIST    = "/calibration/dc/time_to_distance/time2dist";
    public static final String T0CORRECTION = "/calibration/dc/time_corrections/T0Corrections";
    public static final String TDCTCUTS     = "/calibration/dc/time_corrections/tdctimingcuts";
    public static final String WIRESTAT     = "/calibration/dc/tracking/wire_status";
    public static final String TIMEJITTER   = "/calibration/dc/time_jitter";
    public static final String BEAMPOS      = "/geometry/beam/position";

    public static final String HITBASE = "HitBased";
    
    // GEOMETRY PARAMETERS
    // geometry constants not yet read from CCDB of from geometry services
    public DCGeant4Factory    dcDetector   = null;
    public FTOFGeant4Factory  ftofDetector = null;
    public Detector           ecalDetector = null;
    public Detector           fmtDetector  = null;
    public TrajectorySurfaces tSurf        = null;
    
    public static final double HTCCRADIUS=175;
    public static final double LTCCPLANE=653.09;
    
    // other CLAS12 parameters
    public static final  int NSECT  = 6;
    public static final  int NSLAY  = 6;
    public static final  int NSLAYR = 2;
    public static final  int NLAYR  = 6;
    public static final  int NREG   = 3;
    public static final  int NWIRE  = 114; //1 guard + 112 sense + 1 guard

    public static final double LOWFIELDZ = 592.; // z in cm in the region outside of DC-R3 [used for extrapolation of the track to the outer detectors]
    public double[] wpdist = new double[6];//= {0.386160,0.404220,0.621906,0.658597,0.935140,0.977982};

    // CONSTANTS USED IN RECONSTRUCTION
    //---------------------------------
    public static final double TRIGJIT = 20;
    public static final double[] TIMEWINMINEDGE = {-25.0,-25.0,-25.0};
    public static final double[] TIMEWINMAXEDGE = {275.0,350.0,750.0};

    public static double SEEDCUT = 5000;
    public static double MINPATH = 200;
    public static double BETAHIGH = 1.5;
    public static double BETALOW = 0.15;
    //max number of hits allowed in the event to do tracking
    public static double MAXHITS = 2000;
    public static double TSTARTEST = 560.;

    public static double TRANSVTXCUT = 20;
    
    public static double AVEDRIFTVEL = 0.0027; //velocity in cm / ns. [CLAS-Note 96-008]
    public static double DOCASUMMAXFAC = 1.6;


    // RECONSTRUCTION PARAMETERS
    public static final int DC_MIN_NLAYERS = 4;
    // V0 averaged value in t(beta) term denominator
    public static final double V0AVERAGED = 0.005; //was 0.007 - modified 05/28/2019
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

    public static double CROSSLISTSELECTQFMINCHSQ=2000; //was 2000

    public static final double SEGMENTPLANESANGLE = 1.5;  // the angle between the normals to the segment fit planes is 12 degrees (6+6 for +/- stereo relative angles) + 1.5 degrees tolerance.  This number (1.5) should be optimized 

    public static final double ARGONRADLEN = 14;  // radiation length in Argon is 14 cm
    
     public static final double AIRRADLEN = 30400; // radiation length in cm

    public static final  double SWIMSTEPSIZE = 5.00*1.e-4; //n00 microns

    public static final int MAXNBCROSSES = 100; // max num crosses persector

    public static final int MAXNBHITS = 350;

    public static final double MINTRKMOM = 0.050;

    public static final double MAXTRKMOM = 20.0;

    public static final int MAXCLUSSIZE = 14;

    public static final double MAXCHI2 = 10000;

    public static double HBTCHI2CUT = 2000;

    public static double SEGSUMRESIDCUT = 0.9;

    // SNR parameters -- can be optimized
    public static final  int[] SNR_RIGHTSHIFTS = {0,1,2,2,4,4};
    public static final  int[] SNR_LEFTSHIFTS  = {0,1,2,2,4,4};	

    // Z Range for MS
    public double[] Z = new double[13];

    // Arrays for combinatorial cluster compositions
    private static final int[][] CombArray1Layer = new int[][]{
            {0},
            {1}};

    private static final int[][] CombArray2Layers = new int[][]{
            {0,0}, {1,0},
            {0,1}, {1,1}};

    private static final int[][] CombArray3Layers = new int[][]{
            {0,0,0}, {1,0,0}, {0,1,0}, {1,1,0},
            {0,0,1}, {1,0,1}, {0,1,1}, {1,1,1}};

    private static final int[][] CombArray4Layers = new int[][]{
            {0,0,0,0}, {1,0,0,0}, {0,1,0,0}, {1,1,0,0},
            {0,0,1,0}, {1,0,1,0}, {0,1,1,0}, {1,1,1,0},
            {0,0,0,1}, {1,0,0,1}, {0,1,0,1}, {1,1,0,1},
            {0,0,1,1}, {1,0,1,1}, {0,1,1,1}, {1,1,1,1}};

    private static final int[][] CombArray5Layers = new int[][]{
            {0,0,0,0,0}, {1,0,0,0,0}, {0,1,0,0,0}, {1,1,0,0,0},
            {0,0,1,0,0}, {1,0,1,0,0}, {0,1,1,0,0}, {1,1,1,0,0},
            {0,0,0,1,0}, {1,0,0,1,0}, {0,1,0,1,0}, {1,1,0,1,0},
            {0,0,1,1,0}, {1,0,1,1,0}, {0,1,1,1,0}, {1,1,1,1,0},
            {0,0,0,0,1}, {1,0,0,0,1}, {0,1,0,0,1}, {1,1,0,0,1},
            {0,0,1,0,1}, {1,0,1,0,1}, {0,1,1,0,1}, {1,1,1,0,1},
            {0,0,0,1,1}, {1,0,0,1,1}, {0,1,0,1,1}, {1,1,0,1,1},
            {0,0,1,1,1}, {1,0,1,1,1}, {0,1,1,1,1}, {1,1,1,1,1}};

    private static final int[][] CombArray6Layers = new int[][]{
            {0,0,0,0,0,0}, {1,0,0,0,0,0}, {0,1,0,0,0,0}, {1,1,0,0,0,0},
            {0,0,1,0,0,0}, {1,0,1,0,0,0}, {0,1,1,0,0,0}, {1,1,1,0,0,0},
            {0,0,0,1,0,0}, {1,0,0,1,0,0}, {0,1,0,1,0,0}, {1,1,0,1,0,0},
            {0,0,1,1,0,0}, {1,0,1,1,0,0}, {0,1,1,1,0,0}, {1,1,1,1,0,0},
            {0,0,0,0,1,0}, {1,0,0,0,1,0}, {0,1,0,0,1,0}, {1,1,0,0,1,0},
            {0,0,1,0,1,0}, {1,0,1,0,1,0}, {0,1,1,0,1,0}, {1,1,1,0,1,0},
            {0,0,0,1,1,0}, {1,0,0,1,1,0}, {0,1,0,1,1,0}, {1,1,0,1,1,0},
            {0,0,1,1,1,0}, {1,0,1,1,1,0}, {0,1,1,1,1,0}, {1,1,1,1,1,0},
            {0,0,0,0,0,1}, {1,0,0,0,0,1}, {0,1,0,0,0,1}, {1,1,0,0,0,1},
            {0,0,1,0,0,1}, {1,0,1,0,0,1}, {0,1,1,0,0,1}, {1,1,1,0,0,1},
            {0,0,0,1,0,1}, {1,0,0,1,0,1}, {0,1,0,1,0,1}, {1,1,0,1,0,1},
            {0,0,1,1,0,1}, {1,0,1,1,0,1}, {0,1,1,1,0,1}, {1,1,1,1,0,1},
            {0,0,0,0,1,1}, {1,0,0,0,1,1}, {0,1,0,0,1,1}, {1,1,0,0,1,1},
            {0,0,1,0,1,1}, {1,0,1,0,1,1}, {0,1,1,0,1,1}, {1,1,1,0,1,1},
            {0,0,0,1,1,1}, {1,0,0,1,1,1}, {0,1,0,1,1,1}, {1,1,0,1,1,1},
            {0,0,1,1,1,1}, {1,0,1,1,1,1}, {0,1,1,1,1,1}, {1,1,1,1,1,1}};

    public final ArrayList<int[][]> CombArray = new ArrayList<>(6);

    public final int[][] STBLOC = new int[6][6];

    public final double[][][] MAXENDPLTDEFLEC = new double[3][6][2];
    

    public boolean isUSETSTART() {
        return USETSTART;
    }

    public void setUSETSTART(boolean usetstart) {
        USETSTART = usetstart;
    }
    
    public boolean useUSETIMETBETA() {
        return USETIMETBETA;
    }

    public void setUSETIMETBETA(boolean usetimebeta) {
        USETIMETBETA = usetimebeta;
    }
       
    public double getWIREDIST() {
        return WIREDIST;
    }

    public void setWIREDIST(double aDIST) {
        WIREDIST = aDIST;
    }

    public String getGEOVARIATION() {
        return GEOVARIATION;
    }

    public void setGEOVARIATION(String geovariation) {
        GEOVARIATION = geovariation;
    }

    public boolean ENDPLATESBOWING() {
        return ENDPLATESBOWING;
    }

    public void setENDPLATESBOWING(boolean endplatesbowing) {
        ENDPLATESBOWING = endplatesbowing;
    }

    public boolean USEBETACUT() {
        return CHECKBETA;
    }

    public void setBETACUT(boolean checkbeta) {
        CHECKBETA = checkbeta;
    }

    public void  setT2D(int i) {
        T2D = i;
    }
    public int getT2D() {
        return T2D;
    }

    public boolean USEDOUBLETS() {
        return USEDOUBLETS;
    }

    public void setUSEDOUBLETS(boolean USEDOUBLETS) {
        this.USEDOUBLETS = USEDOUBLETS;
    }
    
    public synchronized void initialize(String engine,
                                        String variation, 
                                        boolean wireDistortion,
                                        boolean useStartTime,
                                        boolean useTimeBeta,
                                        boolean useBetaCut,
                                        int t2d, 
                                        boolean useDoublets,
                                        int nSuperLayer,
                                        int selectedSector) {
        if (ConstantsLoaded) {
            printConfig(engine);
        }
        else {
            GEOVARIATION    = variation;
            ENDPLATESBOWING = wireDistortion;
            USETSTART       = useStartTime;
            USETIMETBETA    = useTimeBeta;
            CHECKBETA       = useBetaCut;
            T2D             = t2d;
            USEDOUBLETS     = useDoublets;
            NSUPERLAYERTRACKING = nSuperLayer;
            SECTORSELECT    = selectedSector;

            LoadConstants();

            LoadGeometry(GEOVARIATION);

            ConstantsLoaded = true;
            printConfig(engine);
        }
    }
    
    public synchronized void initialize(String engine) {
        if (ConstantsLoaded) {
            printConfig(engine);
        }
        else {
            LoadConstants();

            LoadGeometry(GEOVARIATION);

            ConstantsLoaded = true;
            printConfig(engine);
        }
    }
    
    public void printConfig(String engine) {
        LOGGER.log(Level.INFO, "["+engine+"] run with variation = " + GEOVARIATION);
        LOGGER.log(Level.INFO, "["+engine+"] run with sector selection = " + SECTORSELECT);
        LOGGER.log(Level.INFO, "["+engine+"] run with start time option = " + USETSTART);
        LOGGER.log(Level.INFO, "["+engine+"] run with wire distortions = " + ENDPLATESBOWING);
        LOGGER.log(Level.INFO, "["+engine+"] run with with new tBeta configuration = " + USETIMETBETA);
        LOGGER.log(Level.INFO, "["+engine+"] run with with Beta cut = " + CHECKBETA);
        LOGGER.log(Level.INFO, "["+engine+"] run with time to distance function set to exponential/polynomial (0/1) = " + T2D);
        LOGGER.log(Level.INFO, "["+engine+"] run with with hit doublets recovery = " + USEDOUBLETS);
        LOGGER.log(Level.INFO, "["+engine+"] run with with Five-out-of-six-superlayer-trkg = " + NSUPERLAYERTRACKING);        
    }
    
    private synchronized void LoadConstants() {
        CombArray.clear();
        CombArray.add(CombArray1Layer);
        CombArray.add(CombArray2Layers);
        CombArray.add(CombArray3Layers);
        CombArray.add(CombArray4Layers);
        CombArray.add(CombArray5Layers);
        CombArray.add(CombArray6Layers);

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

        MAXENDPLTDEFLEC[0][0][0] = 0.20;
        MAXENDPLTDEFLEC[0][0][1] = 0.23;
        MAXENDPLTDEFLEC[0][1][0] = 0.24;
        MAXENDPLTDEFLEC[0][1][1] = 0.21;
        MAXENDPLTDEFLEC[0][2][0] = 0.21;
        MAXENDPLTDEFLEC[0][2][1] = 0.24;
        MAXENDPLTDEFLEC[0][3][0] = 0.23;
        MAXENDPLTDEFLEC[0][3][1] = 0.20;
        MAXENDPLTDEFLEC[0][4][0] = 0.20;
        MAXENDPLTDEFLEC[0][4][1] = 0.26;
        MAXENDPLTDEFLEC[0][5][0] = 0.26;
        MAXENDPLTDEFLEC[0][5][1] = 0.20;

        MAXENDPLTDEFLEC[1][0][0] = 0.085;
        MAXENDPLTDEFLEC[1][0][1] = 0.180;
        MAXENDPLTDEFLEC[1][1][0] = 0.121;
        MAXENDPLTDEFLEC[1][1][1] = 0.123;
        MAXENDPLTDEFLEC[1][2][0] = 0.123;
        MAXENDPLTDEFLEC[1][2][1] = 0.121;
        MAXENDPLTDEFLEC[1][3][0] = 0.180;
        MAXENDPLTDEFLEC[1][3][1] = 0.085;
        MAXENDPLTDEFLEC[1][4][0] = 0.190;
        MAXENDPLTDEFLEC[1][4][1] = 0.095;
        MAXENDPLTDEFLEC[1][5][0] = 0.095;
        MAXENDPLTDEFLEC[1][5][1] = 0.190;

        MAXENDPLTDEFLEC[2][0][0] = 0.;
        MAXENDPLTDEFLEC[2][0][1] = 0.;
        MAXENDPLTDEFLEC[2][1][0] = 0.;
        MAXENDPLTDEFLEC[2][1][1] = 0.;
        MAXENDPLTDEFLEC[2][2][0] = 0.;
        MAXENDPLTDEFLEC[2][2][1] = 0.;
        MAXENDPLTDEFLEC[2][3][0] = 0.;
        MAXENDPLTDEFLEC[2][3][1] = 0.;
        MAXENDPLTDEFLEC[2][4][0] = 0.;
        MAXENDPLTDEFLEC[2][4][1] = 0.;
        MAXENDPLTDEFLEC[2][5][0] = 0.;
        MAXENDPLTDEFLEC[2][5][1] = 0.;
            
    }

    private synchronized void LoadGeometry(String geoVariation) {
        // Load the geometry
        ConstantProvider provider = GeometryFactory.getConstants(DetectorType.DC, 11, geoVariation);
        dcDetector = new DCGeant4Factory(provider, DCGeant4Factory.MINISTAGGERON, ENDPLATESBOWING);
        for(int l=0; l<6; l++) {
            wpdist[l] = provider.getDouble("/geometry/dc/superlayer/wpdist", l);
        }
        // Load target
        ConstantProvider providerTG = GeometryFactory.getConstants(DetectorType.TARGET, 11, geoVariation);
        double targetPosition = providerTG.getDouble("/geometry/target/position",0);
        double targetLength   = providerTG.getDouble("/geometry/target/length",0);
        // Load other geometries
        ConstantProvider providerFTOF = GeometryFactory.getConstants(DetectorType.FTOF, 11, geoVariation);
        ftofDetector = new FTOFGeant4Factory(providerFTOF);        
        ecalDetector =  GeometryFactory.getDetector(DetectorType.ECAL, 11, geoVariation);
        fmtDetector =  GeometryFactory.getDetector(DetectorType.FMT, 11, geoVariation);
        // create the surfaces
        tSurf = new TrajectorySurfaces();
        tSurf.LoadSurfaces(targetPosition, targetLength,dcDetector, ftofDetector, ecalDetector, fmtDetector);        
    }
   

}
