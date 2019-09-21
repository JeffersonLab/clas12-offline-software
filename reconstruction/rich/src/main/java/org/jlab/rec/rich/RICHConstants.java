/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.rich;

/**
 *
 * @author mcontalb
 */
public class RICHConstants {

    // -----------------
    // Static
    // -----------------

    public static final int     EVENT_TIME                = 300;     // Expected Event time

    public static final int     LEADING_EDGE_POLARITY     = 1;       // MAROC polarity of the leading edge
    public static final int     TRAILING_EDGE_POLARITY    = 0;       // MAROC polarity of the trailing edge

    //public static final int     CLUSTER_TIME_WINDOW       = 20;    // Cluster acceptance time window
    public static final int     CLUSTER_TIME_WINDOW       = 500000;  // initial test with data
    public static final int     CLUSTER_MIN_SIZE          = 3;       // Cluster acceptance min size
    public static final int     CLUSTER_MIN_CHARGE        = 0;       // Cluster acceptance min energy

    public static final double  COSMIC_TRACKING_Z         = -120.;    // Tracking station height in cosmic run
    public static final double  COSMIC_AEROGEL_Z          = -100.;    // Aerogel  station height in cosmic run

    public static final double  RICH_AEROGEL_INDEX        =   1.05;    // Aerogel refracting index (da CCDB)
    public static final double  RICH_AERO_THICKNESS       =      2.;   // Aerogel thickness (da CCDB
    public static final double  RICH_AIR_INDEX            =   1.000273; //  AIR n used in Mirazita's code (da CCDB)

    public static final double PHOTON_DISTMIN_TRACING     =   0.0001;   // max distance to set initial values for tracing photons
    public static final double PHOTON_DISTMIN_SPHERE      =   200.;     // max distance to approximate the spherical mirror with triangles
    public static final double RICH_MIN_CHANGLE           =  10.e-3;    // rad

    public static final double  RICH_MATCH_POLYDIST       =   1.e-3;    // Matching dist between poly and point
    public static final double  RICH_BKG_PROBABILITY      =   1.e-3;   // Background probability for likelihood

    public static final double READ_FROM_FILES            =   0.;      // read values from txt files

    // -----------------
    // Read from CCDB/TEXT 
    // -----------------

    
    public int     DO_MISALIGNMENT                        =   1;        // if 1 apply misalignment 
    public int     FORCE_DC_MATCH                         =   0;        // if 1 force the hadron track to hit the cluster
    public int     MISA_RICH_REF                          =   1;        // if 1 use local RICH frame (instead of Lab frame)
    public int     MISA_PMT_PIVOT                         =   1;        // if 1 use MAPMT barycenter for rotations
    public int     APPLY_SURVEY                           =   0;        // if 1 apply the survey data for misalignment

    public int     DO_ANALYTIC                            =   1;        // if 1 calculate analytic solution
    public int     THROW_ELECTRONS                        =   1;        // if 1 throw photons for electron hypothesis
    public int     THROW_PIONS                            =   0;        // if 1 throw photons for pion hypothesis
    public int     THROW_KAONS                            =   0;        // if 1 throw photons for kaon hypothesis
    public int     THROW_PROTONS                          =   0;        // if 1 throw photons for proton hypothesis
    public int     THROW_PHOTON_NUMBER                    =   50;       // number of photon trials for every hypothesis
    public int     TRACE_PHOTONS                          =   1;        // if 1 ray-trace phtoons

    public int     REDO_RICH_RECO                         =   1;        // if 1 rewrite the RICH banks
    public int     DO_MIRROR_HADS                         =   1;        // if 1 reconstruct hadrons pointing to mirror
    public int     DO_CURVED_AERO                         =   1;        // if 1 use spherical surface of aerogel

    public double  GOODHIT_FRAC                           =   80.;      // Maximum duration (in % of local max) to flag xtalk  
    public double  RICH_DCMATCH_CUT                       =   15.;      // RICH cluster matching cut with tracks 
    public double  RICH_HITMATCH_RMS                      =   0.6;      // RICH - particle matching chi2 reference (cm)
    public double  RICH_DIRECT_RMS                        =   4.2e-3;   // Expected single photon angular resolution (rad)

    public double  SHOW_PROGRESS_INTERVAL                 =   10.;      // Time interval between progress dumping (sec)
    public double  THROW_ASSOCIATION_CUT                  =   10.;      // Max distance to set initial values for tracing photons (cm)
    public double  RICH_TIME_RMS                          =   1.5;      // Expected single photon Time resolution (ns)
    public double  RICH_DEBUG                             =   0.0;      // Flag to activate the printout for debug

    public double  MISA_SHIFT_SCALE                       =   1.0;      // Scale factor for misalignment shifts
    public double  MISA_ANGLE_SCALE                       =   1.0;      // Scale factor for misalignment angles

    // -----------------
    // Run Type
    // -----------------

    public static final int     CLAS_RUN                  = 1;       // 1 if clas runs
    public static final int     GEMC_RUN                  = 0;       // 1 if clas runs
    public static final int     COSMIC_RUN                = 0;       // 1 if cosmic runs

    // -----------------
    // Generic
    // -----------------

    public static double MRAD = 1000.;
    public static double RAD = 180./Math.PI;

}
