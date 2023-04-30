package org.jlab.rec.rich;


import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

import java.io.FileReader;
import java.io.BufferedReader;

/**
 *
 * @author mcontalb
 */
public class RICHParameters{

    // Default flags of RICH reconstruction parameters to be re-loaded from CCDB or TxT

    public int     PROCESS_RAWDATA                        =   1;        // Process raw data starting from RICH::tdc
    public int     PROCESS_DATA                           =   1;        // Process data starting from RICH::Signal (Hit-Cluster) and REC::Trajectory
    public int     USE_SIGNAL_BANK                        =   0;        // Use RICH::Signal Bank (vs RICH::Hit and RICH::Cluster)
    public int     FORCE_DC_MATCH                         =   0;        // if 1 force the hadron track to hit the cluster
    public int     DO_ANALYTIC                            =   0;        // if 1 calculate analytic solution
    public int     TRACE_PHOTONS                          =   1;        // if 1 ray-trace phtons 

    public int     THROW_ELECTRONS                        =   0;        // if 1 throw photons for electron hypothesis
    public int     THROW_PIONS                            =   1;        // if 1 throw photons for pion hypothesis
    public int     THROW_KAONS                            =   0;        // if 1 throw photons for kaon hypothesis
    public int     THROW_PROTONS                          =   0;        // if 1 throw photons for proton hypothesis
    public int     TRACE_NITROGEN                         =   0;        // if 1 ray-trace phtoons out of nitrogen
    public int     SAVE_PHOTONS                           =   0;        // Store reconstructed photons in the photons bank
    public int     SAVE_THROWS                            =   0;        // Store throwed photons in the photons bank

    public int     DO_MIRROR_HADS                         =   1;        // if 1 reconstruct hadrons pointing to mirror
    public int     DO_CURVED_AERO                         =   0;        // if 1 use spherical surface of aerogel

    public int     USE_ELECTRON_ANGLES                    =   1;        // Get Cherenkov angle and rms from electrons control sample
    public int     USE_LIKE_DELTAN                        =   0;        // Use global normalization (expected number of photons) in likelihood
    public int     USE_CALIBRATED_PIXELS                  =   1;        // Get pixel properties from calibration data
    public int     USE_PIXEL_GAIN                         =   0;        // Use pixel status and efficiency in the likelihood
    public int     USE_PIXEL_EFF                          =   1;        // Use pixel status and efficiency in the likelihood
    public int     USE_PIXEL_TIME                         =   1;        // Use pixel status and efficiency in the likelihood
    public int     USE_PIXEL_BACKGR                       =   1;        // Use pixel background in the likelihood

    public int     DO_PASS2_LIKE                          =   1;        // Adopt Likelihood of PASS2 type 
    public int     DO_PASS1_LIKE                          =   0;        // Adopt Likelihood of PASS1 type 
    public int     DO_LHCB_LIKE                           =   0;        // Adopt Likelihood of LHCB  type 

    public int     RING_ONLY_BEST                         =   0;        // Save only ring photons with hypothesis equal to BEST
    public int     RING_ONLY_USED                         =   0;        // Save only ring photons with no bad status flag

    public int     DEBUG_RECO_FLAG                        =   0;        // Number of events with debug of Reconstruction Falgs 
    public int     DEBUG_RECO_PAR                         =   0;        // Number of events with debug of Reconstruction Parameters
    public int     DEBUG_CAL_COST                         =   0;        // Number of events with debug of Calibration Constants
    public int     DEBUG_PROC_TIME                        =   0;        // if 1 activate the sub-process time consumption printout

    // Default values of RICH reconstruction parameters to be re-loaded from CCDB or TxT

    public double  OFFSET_TIME                            =   0;        // Offset for time matching
    public double  GOODHIT_FRAC                           =   80.;      // Maximum duration (in % of local max) to flag xtalk  

    public double  RICH_DCMATCH_CUT                       =   15.;      // RICH cluster matching cut with tracks 
    public double  THROW_ASSOCIATION_CUT                  =   10.;      // Max distance to set initial values for tracing photons (cm)
    public double  RICH_HITMATCH_RMS                      =   0.6;      // RICH - particle matching chi2 reference (cm)

    public double  RICH_NOMINAL_SANGLE                    =   6.e-3;    // Expected single photon angular resolution (rad)
    public double  NSIGMA_CHERENKOV                       =   5;        // Number of sigmas for Cherenkov angle selection
    public double  NSIGMA_TIME                            =   20;       // Number of sigmas for time selection

    public int     QUADRANT_NUMBER                        =   15;       // Number of quadrants (square root of)

    public int     THROW_PHOTON_NUMBER                    =   50;       // number of photon trials for every hypothesis
    public double  RAYTRACE_RESO_FRAC                     =   0.1;      // Fraction of RICH equivalent resolution
    public int     RAY_NFRONT_REFLE                       =   1;        // Maximum number of reflectionson the front mirrros
    public int     RAYTRACE_MAX_NSTEPS                    =   20;       // Maximum number of steps for raytracing

    public double  PIXEL_NOMINAL_STIME                    =   1.0;      // nominal pixel time resolution
    public double  PIXEL_NOMINAL_DARKRATE                 =   5.e-7;    // nominal pixel background
    public double  RICH_NOMINAL_NELE                      =   16;       // nominal photon yield for electron


    // -----------------
    public RICHParameters() {
    // -----------------
    }


    //------------------------------
    public void load_CCDB(ConstantsManager manager, int run, int ncalls, boolean engineDebug){
    //------------------------------

        int debugMode = 0;

        init_FlagCCDB( manager.getConstants(run, "/calibration/rich/reco_flag"), engineDebug );
        init_ParameterCCDB( manager.getConstants(run, "/calibration/rich/reco_parameter") );

        if((debugMode>=1 || DEBUG_RECO_FLAG>=1) && ncalls<Math.max(1,DEBUG_RECO_FLAG)){
            System.out.format("------------------------------------------------------------- \n");
            System.out.format("RICH: Load RECO Flags from CCDB for run %6d  (ncalls %3d) \n", run,ncalls);
            System.out.format("------------------------------------------------------------- \n");
            System.out.format("Banks \n /calibration/rich/reco_flag \n /calibration/rich/reco_parameter  \n ");
            
            dump_RecoFlags(run);
        }

        if((debugMode>=1 || DEBUG_RECO_PAR>=1) && ncalls<Math.max(1,DEBUG_RECO_PAR)) {
            System.out.format("------------------------------------------------------------- \n");
            System.out.format("RICH: Load RECO Parameters from CCDB for run %6d (ncalls %3d) \n", run,ncalls);
            System.out.format("------------------------------------------------------------- \n");

            dump_RecoParameters(run);
        }

        if(RICHConstants.RECOPAR_FROM_FILE==1){

            if(debugMode>0)System.out.format("RICHFactory: Load calibration parameters from TxT\n");
            init_ParameterTxT();

        }
 
    }


    //------------------------------
    public void init_FlagCCDB(IndexedTable flagConstants, boolean engineDebug) {
    //------------------------------

        int debugMode = 0;

	PROCESS_RAWDATA             =  flagConstants.getIntValue("reco_raw", 0, 0, 0);
        PROCESS_DATA                =  flagConstants.getIntValue("reco_data", 0, 0, 0);
        USE_SIGNAL_BANK             =  flagConstants.getIntValue("use_sig_bank", 0, 0, 0);

        FORCE_DC_MATCH              =  flagConstants.getIntValue("force_dc_match", 0, 0, 0);
        DO_ANALYTIC                 =  flagConstants.getIntValue("do_analytic", 0, 0, 0);
        TRACE_PHOTONS               =  flagConstants.getIntValue("do_traced", 0, 0, 0);

        THROW_ELECTRONS             =  flagConstants.getIntValue("throw_e", 0, 0, 0);
        THROW_PIONS                 =  flagConstants.getIntValue("throw_pi", 0, 0, 0);
        THROW_KAONS                 =  flagConstants.getIntValue("throw_k", 0, 0, 0);
        THROW_PROTONS               =  flagConstants.getIntValue("throw_pr", 0, 0, 0);
        TRACE_NITROGEN              =  flagConstants.getIntValue("throw_N2", 0, 0, 0);
        SAVE_PHOTONS                =  flagConstants.getIntValue("save_photons", 0, 0, 0);
        SAVE_THROWS                 =  flagConstants.getIntValue("save_throws", 0, 0, 0);

        DO_MIRROR_HADS              =  flagConstants.getIntValue("use_spher", 0, 0, 0);
        DO_CURVED_AERO              =  flagConstants.getIntValue("do_curved", 0, 0, 0);

        USE_ELECTRON_ANGLES         =  flagConstants.getIntValue("use_ecalib", 0, 0, 0);

        USE_LIKE_DELTAN             =  flagConstants.getIntValue("like_norm", 0, 0, 0);
        USE_CALIBRATED_PIXELS       =  flagConstants.getIntValue("pixel_calib", 0, 0, 0);
        USE_PIXEL_GAIN              =  flagConstants.getIntValue("pixel_gain", 0, 0, 0);
        USE_PIXEL_EFF               =  flagConstants.getIntValue("pixel_eff", 0, 0, 0);
        USE_PIXEL_TIME              =  flagConstants.getIntValue("pixel_time", 0, 0, 0);
        USE_PIXEL_BACKGR            =  flagConstants.getIntValue("pixel_back", 0, 0, 0);

        DO_PASS1_LIKE               =  flagConstants.getIntValue("pass1_like", 0, 0, 0);
        DO_PASS2_LIKE               =  flagConstants.getIntValue("pass2_like", 0, 0, 0);
        DO_LHCB_LIKE                =  flagConstants.getIntValue("lhcb_like", 0, 0, 0);

        DEBUG_RECO_FLAG             =  flagConstants.getIntValue("debug_reco_flag", 0, 0, 0);
        DEBUG_RECO_PAR              =  flagConstants.getIntValue("debug_reco_par", 0, 0, 0);
        DEBUG_CAL_COST              =  flagConstants.getIntValue("debug_cal_const", 0, 0, 0);
        DEBUG_PROC_TIME             =  flagConstants.getIntValue("debug_CPUtime", 0, 0, 0);

        RING_ONLY_BEST              =  flagConstants.getIntValue("ring_only_best", 0, 0, 0);
        RING_ONLY_USED              =  flagConstants.getIntValue("ring_only_used", 0, 0, 0);

        if(!engineDebug){
            if(debugMode>=1)System.out.format("RICH RECO debugging set to OFF \n");
            DEBUG_RECO_FLAG             =  0;
            DEBUG_RECO_PAR              =  0;
            DEBUG_CAL_COST              =  0;
            DEBUG_PROC_TIME             =  0;
        }

    }


    //------------------------------
    public void init_ParameterCCDB(IndexedTable paraConstants) {
    //------------------------------

        int debugMode = 0;

        OFFSET_TIME                 =  paraConstants.getDoubleValue("global_time_off", 0, 0, 0);
        GOODHIT_FRAC                =  paraConstants.getDoubleValue("xtalk_frac", 0, 0, 0);

        RICH_DCMATCH_CUT            =  paraConstants.getDoubleValue("dc_match", 0, 0, 0);
        THROW_ASSOCIATION_CUT       =  paraConstants.getDoubleValue("trial_match", 0, 0, 0);
        RICH_HITMATCH_RMS           =  paraConstants.getDoubleValue("match_rms", 0, 0, 0);

        NSIGMA_CHERENKOV            =  paraConstants.getDoubleValue("nsigma_cher", 0, 0, 0);
        NSIGMA_TIME                 =  paraConstants.getDoubleValue("nsigma_time", 0, 0, 0);

        QUADRANT_NUMBER             =  paraConstants.getIntValue("quad_number", 0, 0, 0);

        THROW_PHOTON_NUMBER         =  paraConstants.getIntValue("N_trials", 0, 0, 0);
        RAYTRACE_RESO_FRAC          =  paraConstants.getDoubleValue("ray_resofrac", 0, 0, 0) / 100.;
        RAY_NFRONT_REFLE            =  paraConstants.getIntValue("ray_Nfront", 0, 0, 0);
        RAYTRACE_MAX_NSTEPS         =  paraConstants.getIntValue("ray_steps", 0, 0, 0);

        RICH_NOMINAL_SANGLE         =  paraConstants.getDoubleValue("ref_sangle", 0, 0, 0) / 1000.;
        PIXEL_NOMINAL_STIME         =  paraConstants.getDoubleValue("ref_stime", 0, 0, 0);
        PIXEL_NOMINAL_DARKRATE      =  paraConstants.getDoubleValue("ref_darkrate", 0, 0, 0) / 1.e9;
        RICH_NOMINAL_NELE           =  paraConstants.getDoubleValue("ref_Nele", 0, 0, 0);

    }


    //------------------------------
    public void init_ParameterTxT() {
    //------------------------------

        /*int debugMode = 0;

        IndexedTable prova = new IndexedTable(3, "DO_ALIGNMENT/I:FORCE_DC_MATCHD");

        try {

            BufferedReader bf = new BufferedReader(new FileReader(par_filename));
            String currentLine = null;

            while ( (currentLine = bf.readLine()) != null) {

                String[] array = currentLine.split(" ");
                int    isec    = Integer.parseInt(array[0]);
                int    ila     = Integer.parseInt(array[1]);
                int    ico     = Integer.parseInt(array[2]);
                String name    = array[4];

                if(name.equals("DO_ALIGNMENT") DO_ALIGNMENT = Integer.parseInt(array[2]);
                String type    = array[3];
                if(type.equals("I") ival = 
                double val     = Double.parseDouble(array[5]);

                prova.addEntry(isec, ila, ico);
                prova.setDoubleValue(val, "value", isec, ila, ico);
                int id = parlist.valueOf(name).id();
                prova.setIntValue(id, "name", isec, ila, ico);

                 double eval  = prova.getDoubleValue("value", isec, ila, ico);
                 int etype = prova.getIntValue("name", isec, ila, ico);

                 System.out.format(" PROVA Value %s %7.2f -->  %4d %7.2f \n",name, val, etype, eval);
            }

        } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", par_filename);
                e.printStackTrace();
        }*/

    }

 
    //------------------------------
    public void dump_RecoFlags(int run) {
    //------------------------------

        System.out.format("RICH reconstruction flags \n");

        System.out.format("CCDB RICH PARA    PROCESS_RAWDATA              %9d \n", PROCESS_RAWDATA);
        System.out.format("CCDB RICH PARA    PROCESS_DATA                 %9d \n", PROCESS_DATA);
        System.out.format("CCDB RICH PARA    USE_SIGNAL_BANK              %9d \n", USE_SIGNAL_BANK);
        System.out.format("CCDB RICH PARA    FORCE_DC_MATCH               %9d \n", FORCE_DC_MATCH);
        System.out.format("CCDB RICH PARA    DO_ANALYTIC                  %9d \n\n", DO_ANALYTIC);

        System.out.format("CCDB RICH PARA    TRACE_PHOTONS                %9d \n", TRACE_PHOTONS);
        System.out.format("CCDB RICH PARA    THROW_ELECTRONS              %9d \n", THROW_ELECTRONS);
        System.out.format("CCDB RICH PARA    THROW_PIONS                  %9d \n", THROW_PIONS);
        System.out.format("CCDB RICH PARA    THROW_KAONS                  %9d \n", THROW_KAONS);
        System.out.format("CCDB RICH PARA    THROW_PROTONS                %9d \n", THROW_PROTONS);
        System.out.format("CCDB RICH PARA    TRACE_NITROGEN               %9d \n", TRACE_NITROGEN);
        System.out.format("CCDB RICH PARA    SAVE_PHOTONS                 %9d \n", SAVE_PHOTONS);
        System.out.format("CCDB RICH PARA    SAVE_THROWS                  %9d \n\n", SAVE_THROWS);

        System.out.format("CCDB RICH PARA    DO_MIRROR_HADS               %9d \n", DO_MIRROR_HADS);
        System.out.format("CCDB RICH PARA    DO_CURVED_AERO               %9d \n\n", DO_CURVED_AERO);

        System.out.format("CCDB RICH PARA    USE_ELECTRON_ANGLES          %9d \n", USE_ELECTRON_ANGLES);
        System.out.format("CCDB RICH PARA    USE_LIKE_DELTAN              %9d \n", USE_LIKE_DELTAN);
        System.out.format("CCDB RICH PARA    USE_CALIBRATED_PIXELS        %9d \n", USE_CALIBRATED_PIXELS);
        System.out.format("CCDB RICH PARA    USE_PIXEL_GAIN               %9d \n", USE_PIXEL_GAIN);
        System.out.format("CCDB RICH PARA    USE_PIXEL_EFF                %9d \n", USE_PIXEL_EFF);
        System.out.format("CCDB RICH PARA    USE_PIXEL_TIME               %9d \n", USE_PIXEL_TIME);
        System.out.format("CCDB RICH PARA    USE_PIXEL_BACKGR             %9d \n\n", USE_PIXEL_BACKGR);

        System.out.format("CCDB RICH PARA    DO_PASS2_LIKE                %9d \n", DO_PASS2_LIKE);
        System.out.format("CCDB RICH PARA    DO_PASS1_LIKE                %9d \n", DO_PASS1_LIKE);
        System.out.format("CCDB RICH PARA    DO_LHCB_LIKE                 %9d \n\n", DO_LHCB_LIKE);

        System.out.format("CCDB RICH PARA    DEBUG_RECO_FLAG              %9d \n", DEBUG_RECO_FLAG);
        System.out.format("CCDB RICH PARA    DEBUG_RECO_PAR               %9d \n", DEBUG_RECO_PAR);
        System.out.format("CCDB RICH PARA    DEBUG_CAL_COST               %9d \n", DEBUG_CAL_COST);
        System.out.format("CCDB RICH PARA    DEBUG_PROC_TIME              %9d \n\n", DEBUG_PROC_TIME);

        System.out.format("CCDB RICH PARA    RING_ONLY_BEST               %9d \n", RING_ONLY_BEST);
        System.out.format("CCDB RICH PARA    RING_ONLY_USED               %9d \n", RING_ONLY_USED);


        System.out.format(" \n");
    }


    //------------------------------
    public void dump_RecoParameters(int run) {
    //------------------------------

        System.out.format("RICH reconstruction parameters \n");

        System.out.format("CCDB RICH PARA    OFFSET_TIME                  %9.3f (ns) \n", OFFSET_TIME);
        System.out.format("CCDB RICH PARA    GOODHIT_FRAC                 %9.3f (e-2) \n\n", GOODHIT_FRAC);

        System.out.format("CCDB RICH PARA    RICH_DCMATCH_CUT             %9.3f (cm) \n", RICH_DCMATCH_CUT);
        System.out.format("CCDB RICH PARA    THROW_ASSOCIATION_CUT        %9.3f (cm) \n", THROW_ASSOCIATION_CUT);
        System.out.format("CCDB RICH PARA    RICH_HITMATCH_RMS            %9.3f (cm) \n\n", RICH_HITMATCH_RMS);

        System.out.format("CCDB RICH PARA    NSIGMA_CHERENKOV             %9.3f \n", NSIGMA_CHERENKOV);
        System.out.format("CCDB RICH PARA    NSIGMA_TIME                  %9.3f \n", NSIGMA_TIME);
        System.out.format("CCDB RICH PARA    QUADRANT_NUMBER              %9d \n\n", QUADRANT_NUMBER);

        System.out.format("CCDB RICH PARA    THROW_PHOTON_NUMBER          %9d \n", THROW_PHOTON_NUMBER);
        System.out.format("CCDB RICH PARA    RAYTRACE_RESO_FRAC           %9.3f (e-2) \n", RAYTRACE_RESO_FRAC*100.);
        System.out.format("CCDB RICH PARA    RAY_NFRONT_REFLE             %9d \n", RAY_NFRONT_REFLE);
        System.out.format("CCDB RICH PARA    RAYTRACE_MAX_NSTEPS          %9d \n\n", RAYTRACE_MAX_NSTEPS);

        System.out.format("CCDB RICH PARA    RICH_NOMINAL_SANGLE          %9.3f (mrad) \n", RICH_NOMINAL_SANGLE*1e3);
        System.out.format("CCDB RICH PARA    PIXEL_NOMINAL_STIME          %9.3f (ns) \n", PIXEL_NOMINAL_STIME);
        System.out.format("CCDB RICH PARA    PIXEL_NOMINAL_DARKRATE       %9.3f (Hz) \n", PIXEL_NOMINAL_DARKRATE*1e9);
        System.out.format("CCDB RICH PARA    RICH_NOMINAL_NELE            %9.3f \n", RICH_NOMINAL_NELE);

        System.out.format(" \n");

    }

}
