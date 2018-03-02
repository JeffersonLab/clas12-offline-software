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

    public static final int     EVENT_TIME                = 300;     // Expected Event time

    public static final int     LEADING_EDGE_POLARITY     = 1;       // MAROC polarity of the leading edge
    public static final int     TRAILING_EDGE_POLARITY    = 0;       // MAROC polarity of the trailing edge

    //public static final int     CLUSTER_TIME_WINDOW       = 20;    // Cluster acceptance time window
    public static final int     CLUSTER_TIME_WINDOW       = 500000;  // initial test with data
    public static final int     CLUSTER_MIN_SIZE          = 3;       // Cluster acceptance min size
    public static final int     CLUSTER_MIN_CHARGE        = 0;       // Cluster acceptance min energy

    public static final int     GOODHIT_FRAC              = 80;      // maximum duration (in % of local max) to flag xtalk  

    public static final double  COSMIC_TRACKING_Z         = -120.;    // Tracking station height in cosmic run
    public static final double  COSMIC_AEROGEL_Z          = -100.;    // Aerogel  station height in cosmic run

    // -----------------
    // Cosmic run
    // -----------------

    public static final int     COSMIC_RUN                = 1;       // 1 if cosmic runs

}
