package org.jlab.rec.ft.hodo;


public class FTHODOConstantsLoader {
	
	FTHODOConstantsLoader() {
	}
	
	public static int DEBUGMODE = 0;

        // RECONSTRUCTION CONSTANTS
        public static double TIMECONVFAC = 100./4.;                            // conversion factor from TDC channel to time (ns^-1)
	public static double EN_THRES = 0.25;                                                   // energy threshold in MeV	
	public static double FADC_TO_CHARGE = 4*0.4884/50.;

	// CLUSTER RECONSTRUCTION PARAMETERS
	public static double CLUSTER_MIN_ENERGY = 1.;			       // minimum number of crystals in a cluster in MeV
	public static int    CLUSTER_MIN_SIZE   = 2;			       // minimum number of crystals in a cluster 
	public static double TIME_WINDOW        = 8;                           // time window of hits forming a cluster
	public static double HIT_DISTANCE       = 3;                           // max distance of hits forming a cluster in cm

}
