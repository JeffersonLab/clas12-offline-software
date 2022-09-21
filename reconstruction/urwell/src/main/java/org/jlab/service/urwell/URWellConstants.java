package org.jlab.service.urwell;

/**
 *
 * @author bondi, devita
 */
public class URWellConstants {
    
    
    // geometry
    public final static int[] NSTRIPS = { 542, 628, 714};

    // strips
    public final static double THRESHOLD = 0;
    public final static double ADCTOENERGY = 25/1E7; // in keV, values from gemc ADC = (uRwellC.gain*1e6*tInfos.eTot/uRwellC.w_i); with gain = 10^4 and w_i = 25 eV
    public final static double TDCTOTIME = 1;

    // cluster
    public final static double COINCTIME = 100;
    

}
