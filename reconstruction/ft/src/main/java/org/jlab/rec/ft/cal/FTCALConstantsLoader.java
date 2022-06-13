package org.jlab.rec.ft.cal;

public class FTCALConstantsLoader {
	
	public FTCALConstantsLoader() {
	}
		
	public static int DEBUGMODE = 0;	
 
        // RECONSTRUCTION CONSTANTS
        public static final double TIMECONVFAC = 100./4.;                            // conversion factor from TDC channel to time (ns^-1)
        public static final double VEFF        = 150.;                               // speed of light in the scintillator mm/ns
    
                                                  // energy threshold in GeV	
        // GEOMETRY PARAMETERS
	public static double CRYS_DELTA  = 11.5;
	public static double CRYS_WIDTH  = 15.3;					    // crystal width in mm
	public static double CRYS_LENGTH = 200.;					    // crystal length in mm
	public static double CRYS_ZPOS   = 1898.;                                           // position of the crystal front face
		
}
