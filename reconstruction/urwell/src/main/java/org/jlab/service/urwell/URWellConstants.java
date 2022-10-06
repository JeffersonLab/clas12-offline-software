package org.jlab.service.urwell;

/**
 *
 * @author bondi, devita
 */
public class URWellConstants {
    
    
    // geometry
    public final static int NSECTOR  = 6;
    public final static int NLAYER   = 2;
    public final static int NCHAMBER = 3;
    public final static int[] NSTRIPS  = { 542,   628, 714}; // number of strips for the three chambers
    public final static int[] STRIPMIN = {   1,  543, 1171}; // lower strip number
    public final static int[] STRIPMAX = { 542, 1170, 1884}; // higher strip number
    public final static double PITCH = 0.1; // mm
    public final static double[] STEREO = { 10.0, 10.0 };

    // strips
    public final static double THRESHOLD = 0;
    public final static double ADCTOENERGY = 25/1E4; // in eV, values from gemc ADC = (uRwellC.gain*1e6*tInfos.eTot/uRwellC.w_i); with gain = 10^4 and w_i = 25 eV
    public final static double TDCTOTIME = 1;

    // cluster
    public final static double COINCTIME = 100;

}
