package org.jlab.rec.ft;

/**
 *
 * @author devita
 * @author filippi
 */
public class FTConstants {
    
    public static final int     HODO_MIN_CLUSTER_SIZE = 2;         // minimum size of hodo clusters  for match to calorimeter

    public static final double  CAL_HODO_DISTANCE_MATCHING = 3.0;  // matching distance in cm
    public static final double  CAL_HODO_TIME_MATCHING     = 8;    // matching time in ns (it was 8)
    
    public static final double  CAL_TRK_DISTANCE_MATCHING = 3.;    // matching distance in cm (it was 1.5)
    public static final double  CAL_TRK_TIME_MATCHING     = 200;   // matching time in ns TOBECORRECTED (it was 30 - 300 - 100 - 150)

    public static final double  TRK_STRIP_MIN_TIME = 50;                 // minimum time value associated to one strip
    public static final double  TRK_STRIP_MAX_TIME = 350;                 // minimum time value associated to one strip
    
    public static final double  TRK_MIN_CROSS_NUMBER = 1;            // minimum number of crosses to find a line in the tracker
    public static final double  TRK1_TRK2_RADTOL = 0.05;             // max tolerance for TRK0/TRK1 distance (3D) (was 1.-0.5)
    public static final double  TRK1_TRK2_PHITOL = 0.07;             // max tolerance for TRK0/TRK1 phi angular deviation (was 1.-0.7)
    public static final double  TRK1_TRK2_THETATOL = 0.07;           // max tolerance for TRK0/TRK1 theta angulare deviation (was 1.-0.7)
    
    public static final double  TRK_ADC_MINIMUM = 18.;                  // check min threshold for FTTTRK ADCs (18)
    public static final double  TRK_ADC_MAXIMUM = 2000.;                  // check max threshold for FTTTRK ADCs (18) was 1000 170122
    public static final int     TRK_MAX_STRIPS_FOR_TRUNCATED_MEAN = 20;   // threshold for maxnumber of strips included in the truncated mean algo.
    public static final int     TRK_MIN_CLUSTER_SIZE_CENTROID = 3;        // minimum size of the cluster to provide a reliable location by its centroid
    public static final double  TRK_MIN_CLUS_ENERGY = 20.;                // minimum cluster energy to accept a signal cluster (20)
    public static final int     TRK_MIN_CLUS_SIZE = 0;                    // minimum cluster size to form crosses (0: no limit on cluster size) // 2 is very strict
    public static final int     TRK_MAXNUMBEROFHITS = 100;                // maximum number of accepted hits in the tracker banks
}
