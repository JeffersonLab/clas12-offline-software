/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft;

/**
 *
 * @author devita
 */
public class FTConstants {
    
    public static final int     HODO_MIN_CLUSTER_SIZE = 2;         // minimum size of hodo clusters  for match to calorimeter

    public static final double  CAL_HODO_DISTANCE_MATCHING = 3.0;   // matching distance in cm
    public static final double  CAL_HODO_TIME_MATCHING     = 8;    // matching time in ns (it was 8)
    
//    public static final double  CAL_TRK_DISTANCE_MATCHING = 1.5;   // matching distance in cm
//    public static final double  CAL_TRK_TIME_MATCHING     = 30;   // matching time in ns TOBECORRECTED
      public static final double  CAL_TRK_DISTANCE_MATCHING = 3.;   // matching distance in cm (it was 3)
      public static final double  CAL_TRK_TIME_MATCHING     = 100;   // matching time in ns TOBECORRECTED (it was 300)


    
    public static final double  TRK_MIN_CROSS_NUMBER = 1;           // minimum number of crosses to find a line in the tracker
//    public static final double  TRK0_TRK1_DISTANCE_MATCHING = 0.25;  // matching distance between FTTRK points in cms
//    public static final double  TOLERANCE_ON_CROSSES_TWO_DETECTORS = 0.; // 1. cm radius tolerance, tune it up
//    public static final double  TRK0_TRK1_RADTOL = 2.5;              // max tolerance for TRK0/TRK1 distance (3D)
///    public static final double  TRK0_TRK1_RADTOL = 0.5;             // max tolerance for TRK0/TRK1 radial distance (2D)
///    public static final double  TRK0_TRK1_PHITOL = 0.07;             // max tolerance for TRK0/TRK1 phi angular deviation
///    public static final double  TRK0_TRK1_THETATOL = 0.07;           // max tolerance for TRK0/TRK1 theta angulare deviation
    public static final double  TRK0_TRK1_RADTOL = 10.;             // max tolerance for TRK0/TRK1 radial distance (2D)
    public static final double  TRK0_TRK1_PHITOL = 1.;             // max tolerance for TRK0/TRK1 phi angular deviation
    public static final double  TRK0_TRK1_THETATOL = 1.;           // max tolerance for TRK0/TRK1 theta angulare deviation
    
    public static final double  FTTRKMinAdcThreshold = 18.;             // check min threshold for FTTTRK ADCs (18)
    public static final double  FTTRKMaxAdcThreshold = 1000.;            // check max threshold for FTTTRK ADCs (18)
    public static final double  TRK_MIN_CLUS_ENERGY = 20.;              // minimum cluster energy to accept a signal cluster (20)
    public static final int     TRK_MIN_CLUS_SIZE = 0;                  // minimum cluster size to form crosses (0: no limit on cluster size) // 2 is very strict


}
