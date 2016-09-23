package org.jlab.rec.ftof;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;

/**
 * 
 * @author ziegler
 *
 */
public class CalibrationConstantsLoader {

	public CalibrationConstantsLoader() {
		// TODO Auto-generated constructor stub
	}
	public static boolean CSTLOADED = false;
	
	// Instantiating the constants arrays
	public static double[][][] YOFF 			= new double[6][3][62];
	public static double[][][] LAMBDAL 			= new double[6][3][62];
	public static double[][][] LAMBDAR 			= new double[6][3][62];
	public static double[][][] LAMBDALU 		= new double[6][3][62];
	public static double[][][] LAMBDARU 		= new double[6][3][62];
	public static double[][][] EFFVELL 			= new double[6][3][62];
	public static double[][][] EFFVELR 			= new double[6][3][62];
	public static double[][][] EFFVELLU 		= new double[6][3][62];
	public static double[][][] EFFVELRU 		= new double[6][3][62];
	public static double[][][] TW0L 			= new double[6][3][62];
	public static double[][][] TW1L 			= new double[6][3][62];
	public static double[][][] TW2L 			= new double[6][3][62];
	public static double[][][] TW0R 			= new double[6][3][62];
	public static double[][][] TW1R 			= new double[6][3][62];
	public static double[][][] TW2R 			= new double[6][3][62];
	public static double[][][] LR 				= new double[6][3][62];
	public static double[][][] PADDLE2PADDLE 	= new double[6][3][62];
	public static int[][][] STATUSU 			= new int[6][3][62];
	public static int[][][] STATUSD 			= new int[6][3][62];
	
	 //Calibration parameters from DB    
    static DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(10,"default");
    //private Detector ftofDetector;
    public static boolean areCalibConstantsLoaded = false;
    
    public static synchronized void Load() {
		if (CSTLOADED) return;
	    // load table reads entire table and makes an array of variables for each column in the table.
	    dbprovider.loadTable("/calibration/ftof/attenuation");
	    dbprovider.loadTable("/calibration/ftof/effective_velocity");
	    dbprovider.loadTable("/calibration/ftof/timing_offset");
	    dbprovider.loadTable("/calibration/ftof/time_walk");
	    dbprovider.loadTable("/calibration/ftof/status");
	    //disconncect from database. Important to do this after loading tables.
	    dbprovider.disconnect(); 

	   // dbprovider.show();
	    
	    // Getting the Timing Constants
	    // 1) Time-walk
	    for(int i =0; i< dbprovider.length("/calibration/ftof/time_walk/tw0_left"); i++) {
	    	
	    	int iSec = dbprovider.getInteger("/calibration/ftof/time_walk/sector", i);	    
	        int iPan = dbprovider.getInteger("/calibration/ftof/time_walk/layer", i);
	        int iPad = dbprovider.getInteger("/calibration/ftof/time_walk/component", i);
	        double iTW0L = dbprovider.getDouble("/calibration/ftof/time_walk/tw0_left", i);
	        double iTW1L = dbprovider.getDouble("/calibration/ftof/time_walk/tw1_left", i);
	        double iTW2L = dbprovider.getDouble("/calibration/ftof/time_walk/tw2_left", i);
	        double iTW0R = dbprovider.getDouble("/calibration/ftof/time_walk/tw0_right", i);
	        double iTW1R = dbprovider.getDouble("/calibration/ftof/time_walk/tw1_right", i);
	        double iTW2R = dbprovider.getDouble("/calibration/ftof/time_walk/tw2_right", i);

	        TW0L[iSec-1][iPan-1][iPad-1] = iTW0L; 
	        TW1L[iSec-1][iPan-1][iPad-1] = iTW1L;
	        TW2L[iSec-1][iPan-1][iPad-1] = iTW2L;
	        TW0R[iSec-1][iPan-1][iPad-1] = iTW0R;
	        TW1R[iSec-1][iPan-1][iPad-1] = iTW1R;
	        TW2R[iSec-1][iPan-1][iPad-1] = iTW2R; 
	        
	    }
	    //2) Offsets : TIME_OFFSET = TDCL-TDCR - left_right
	    for(int i =0; i< dbprovider.length("/calibration/ftof/timing_offset/left_right"); i++) {
	    	
	    	int iSec = dbprovider.getInteger("/calibration/ftof/timing_offset/sector", i);	    
	        int iPan = dbprovider.getInteger("/calibration/ftof/timing_offset/layer", i);
	        int iPad = dbprovider.getInteger("/calibration/ftof/timing_offset/component", i);
	        double iLR = dbprovider.getDouble("/calibration/ftof/timing_offset/left_right", i);
	        double iPaddle2Paddle = dbprovider.getDouble("/calibration/ftof/timing_offset/paddle2paddle", i);
	       
	        LR[iSec-1][iPan-1][iPad-1] = iLR;
	        PADDLE2PADDLE[iSec-1][iPan-1][iPad-1] = iPaddle2Paddle; 
	    }

	    // Getting the effective velocities constants
	    for(int i =0; i< dbprovider.length("/calibration/ftof/effective_velocity/veff_left"); i++) {

	    	int iSec = dbprovider.getInteger("/calibration/ftof/effective_velocity/sector", i);	    
	        int iPan = dbprovider.getInteger("/calibration/ftof/effective_velocity/layer", i);
	        int iPad = dbprovider.getInteger("/calibration/ftof/effective_velocity/component", i);
	        double iEffVelL = dbprovider.getDouble("/calibration/ftof/effective_velocity/veff_left", i);
	        double iEffVelR = dbprovider.getDouble("/calibration/ftof/effective_velocity/veff_right", i);
	        double iEffVelLU = dbprovider.getDouble("/calibration/ftof/effective_velocity/veff_left_err", i);
	        double iEffVelRU = dbprovider.getDouble("/calibration/ftof/effective_velocity/veff_right_err", i);

	        EFFVELL[iSec-1][iPan-1][iPad-1] = iEffVelL;  
	        EFFVELR[iSec-1][iPan-1][iPad-1] = iEffVelR;
	        EFFVELLU[iSec-1][iPan-1][iPad-1] = iEffVelLU;
	        EFFVELRU[iSec-1][iPan-1][iPad-1] = iEffVelRU;
	    }

	    
	    // Getting the attenuation length
	    for(int i=0; i<dbprovider.length("/calibration/ftof/attenuation/y_offset"); i++) {
	        int iSec = dbprovider.getInteger("/calibration/ftof/attenuation/sector", i);
	        int iPan = dbprovider.getInteger("/calibration/ftof/attenuation/layer", i);
	        int iPad = dbprovider.getInteger("/calibration/ftof/attenuation/component", i);
	        double yoff = dbprovider.getDouble("/calibration/ftof/attenuation/y_offset", i);
	        double atlR = dbprovider.getDouble("/calibration/ftof/attenuation/attlen_right", i);
	        double atlL = dbprovider.getDouble("/calibration/ftof/attenuation/attlen_left", i);
	        double atlRU = dbprovider.getDouble("/calibration/ftof/attenuation/attlen_right_err", i);
	        double atlLU = dbprovider.getDouble("/calibration/ftof/attenuation/attlen_left_err", i);
	        
	        YOFF[iSec-1][iPan-1][iPad-1] = yoff;
	        LAMBDAL[iSec-1][iPan-1][iPad-1] = atlL;
	        LAMBDAR[iSec-1][iPan-1][iPad-1] = atlR;
	        LAMBDALU[iSec-1][iPan-1][iPad-1] = atlLU;
	        LAMBDARU[iSec-1][iPan-1][iPad-1] = atlRU;
	       
	    }
	    
	    // Getting the status
	    for(int i=0; i<dbprovider.length("/calibration/ftof/status/sector"); i++) {
	        int iSec = dbprovider.getInteger("/calibration/ftof/status/sector", i);
	        int iPan = dbprovider.getInteger("/calibration/ftof/status/layer", i);
	        int iPad = dbprovider.getInteger("/calibration/ftof/status/component", i);
	        int statL = dbprovider.getInteger("/calibration/ftof/status/stat_left", i);
	        int statR = dbprovider.getInteger("/calibration/ftof/status/stat_right", i);
	         
	        STATUSU[iSec-1][iPan-1][iPad-1] = statL;
	        STATUSD[iSec-1][iPan-1][iPad-1] = statR;
	       
	    }
	    CSTLOADED = true;
    }
   
    
    
    public static void main (String arg[]) {
    	CalibrationConstantsLoader.Load();
    }
}
