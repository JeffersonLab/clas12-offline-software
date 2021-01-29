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
    public static final double  CAL_HODO_TIME_MATCHING     = 8;    // matching time in ns
    
    public static final double  CAL_TRK_DISTANCE_MATCHING = 1.5;   // matching distance in cm
    public static final double  CAL_TRK_TIME_MATCHING     = 30;   // matching time in ns TOBECORRECTED
    
    public static final double  TRK_MIN_CROSS_NUMBER = 2;           // minimum number of crosses to find a line in the tracker
    public static final double  TRK0_TRK1_DISTANCE_MATCHING = 0.01;  // matching distance between FTTRK points in cms
    public static final double  TOLERANCE_ON_CROSSES_TWO_DETECTORS = 1.; // 1. cm radius tolerance, tune it up
    public static final double  FTTRKAdcThreshold = 5.;             // check minimum threshold for FTTTRK ADCs (18)
    public static final double  TRK_MIN_CLUS_ENERGY = 0.;           // minimum cluster energy to accept a signal cluster (15)
    public static final int     TRK_MIN_CLUS_SIZE = 0;              // minimum cluster size to form crosses (0: no limit on cluster size)
}
