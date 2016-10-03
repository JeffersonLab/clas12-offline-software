package org.jlab.rec.ctof;

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
	public static double[][][] YOFF 			= new double[1][1][48];
	public static double[][][] LAMBDAU 			= new double[1][1][48];
	public static double[][][] LAMBDAD 			= new double[1][1][48];
	public static double[][][] LAMBDAUU 		= new double[1][1][48];
	public static double[][][] LAMBDADU 		= new double[1][1][48];
	public static double[][][] EFFVELU 			= new double[1][1][48];
	public static double[][][] EFFVELD 			= new double[1][1][48];
	public static double[][][] EFFVELUU 		= new double[1][1][48];
	public static double[][][] EFFVELDU 		= new double[1][1][48];
	public static double[][][] TW0U 			= new double[1][1][48];
	public static double[][][] TW1U 			= new double[1][1][48];
	public static double[][][] TW2U 			= new double[1][1][48];
	public static double[][][] TW0D 			= new double[1][1][48];
	public static double[][][] TW1D 			= new double[1][1][48];
	public static double[][][] TW2D 			= new double[1][1][48];
	public static double[][][] UD 				= new double[1][1][48];
	public static double[][][] PADDLE2PADDLE 	= new double[1][1][48];
	public static int[][][] STATUSU 			= new int[1][1][48];
	public static int[][][] STATUSD 			= new int[1][1][48];
	
	 //Calibration parameters from DB    
    static DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(10,"default");
    
    public static boolean areCalibConstantsLoaded = false;
    
    public static synchronized DatabaseConstantProvider Load() {
    	if (CSTLOADED == true) 
			return null;
		
	    // load table reads entire table and makes an array of variables for each column in the table.
	    dbprovider.loadTable("/calibration/ctof/attenuation");
	    dbprovider.loadTable("/calibration/ctof/effective_velocity");
	    dbprovider.loadTable("/calibration/ctof/timing_offset");
	   // dbprovider.loadTable("/calibration/ctof/time_walk");
	    dbprovider.loadTable("/calibration/ctof/status");
	    //disconncect from database. Important to do this after loading tables.
	    dbprovider.disconnect(); 

	    dbprovider.show();
	    
	    // Getting the Timing Constants
	    // 1) Time-walk - at present there is no table for time_walk in the DB so these parameters will all be 0
	    /*
	    for(int i =0; i< dbprovider.length("/calibration/ctof/time_walk/tw0_upstreamt"); i++) {
	    	
	    	int iSec = dbprovider.getInteger("/calibration/ctof/time_walk/sector", i);	    
	        int iPan = dbprovider.getInteger("/calibration/ctof/time_walk/layer", i);
	        int iPad = dbprovider.getInteger("/calibration/ctof/time_walk/component", i);
	        double iTW0L = dbprovider.getDouble("/calibration/ctof/time_walk/tw0_upstream", i);
	        double iTW1L = dbprovider.getDouble("/calibration/ctof/time_walk/tw1_upstreamt", i);
	        double iTW2L = dbprovider.getDouble("/calibration/ctof/time_walk/tw2_upstream", i);
	        double iTW0R = dbprovider.getDouble("/calibration/ctof/time_walk/tw0_downstreamt", i);
	        double iTW1R = dbprovider.getDouble("/calibration/ctof/time_walk/tw1_downstream", i);
	        double iTW2R = dbprovider.getDouble("/calibration/ctof/time_walk/tw2_downstream", i);

	        TW0U[iSec-1][iPan-1][iPad-1] = iTW0L; 
	        TW1U[iSec-1][iPan-1][iPad-1] = iTW1L;
	        TW2U[iSec-1][iPan-1][iPad-1] = iTW2L;
	        TW0D[iSec-1][iPan-1][iPad-1] = iTW0R;
	        TW1D[iSec-1][iPan-1][iPad-1] = iTW1R;
	        TW2D[iSec-1][iPan-1][iPad-1] = iTW2R;
	        
	    }
	    */
	    //2) Offsets : TIME_OFFSET = TDCU-TDCD - lupstream_downstream
	    for(int i =0; i< dbprovider.length("/calibration/ctof/timing_offset/sector"); i++) {
	    	
	    	int iSec = dbprovider.getInteger("/calibration/ctof/timing_offset/sector", i);	    
	        int iPan = dbprovider.getInteger("/calibration/ctof/timing_offset/layer", i);
	        int iPad = dbprovider.getInteger("/calibration/ctof/timing_offset/component", i);
	        double iUD = dbprovider.getDouble("/calibration/ctof/timing_offset/upstream_downstream", i);
	        double iPaddle2Paddle = dbprovider.getDouble("/calibration/ctof/timing_offset/paddle2paddle", i);

	        UD[iSec-1][iPan-1][iPad-1] = iUD;
	        PADDLE2PADDLE[iSec-1][iPan-1][iPad-1] = iPaddle2Paddle;
	    }

	    // Getting the effective velocities constants
	    for(int i =0; i< dbprovider.length("/calibration/ctof/effective_velocity/veff_upstream"); i++) {

	    	int iSec = dbprovider.getInteger("/calibration/ctof/effective_velocity/sector", i);	    
	        int iPan = dbprovider.getInteger("/calibration/ctof/effective_velocity/layer", i);
	        int iPad = dbprovider.getInteger("/calibration/ctof/effective_velocity/component", i);
	        double iEffVelU = dbprovider.getDouble("/calibration/ctof/effective_velocity/veff_upstream", i);
	        double iEffVelD = dbprovider.getDouble("/calibration/ctof/effective_velocity/veff_downstream", i);
	        double iEffVelUU = dbprovider.getDouble("/calibration/ctof/effective_velocity/veff_upstream_err", i);
	        double iEffVelDU = dbprovider.getDouble("/calibration/ctof/effective_velocity/veff_downstream_err", i);

	        EFFVELU[iSec-1][iPan-1][iPad-1]  = iEffVelU;  
	        EFFVELD[iSec-1][iPan-1][iPad-1]  = iEffVelD;
	        EFFVELUU[iSec-1][iPan-1][iPad-1] = iEffVelUU;
	        EFFVELDU[iSec-1][iPan-1][iPad-1] = iEffVelDU;
	    }

	    
	    // Getting the attenuation length
	    for(int i=0; i<dbprovider.length("/calibration/ctof/attenuation/y_offset"); i++) {
	        int iSec = dbprovider.getInteger("/calibration/ctof/attenuation/sector", i);
	        int iPan = dbprovider.getInteger("/calibration/ctof/attenuation/layer", i);
	        int iPad = dbprovider.getInteger("/calibration/ctof/attenuation/component", i);
	        double yoff = dbprovider.getDouble("/calibration/ctof/attenuation/y_offset", i);
	        double atlD = dbprovider.getDouble("/calibration/ctof/attenuation/attlen_downstream", i);
	        double atlU = dbprovider.getDouble("/calibration/ctof/attenuation/attlen_upstream", i);
	        double atlDU = dbprovider.getDouble("/calibration/ctof/attenuation/attlen_downstream_err", i);
	        double atlUU = dbprovider.getDouble("/calibration/ctof/attenuation/attlen_upstream_err", i);
	        
	        YOFF[iSec-1][iPan-1][iPad-1] = yoff;
	        LAMBDAU[iSec-1][iPan-1][iPad-1] = atlU;
	        LAMBDAD[iSec-1][iPan-1][iPad-1] = atlD;
	        LAMBDAUU[iSec-1][iPan-1][iPad-1] = atlUU;
	        LAMBDADU[iSec-1][iPan-1][iPad-1] = atlDU;
	        
	    }
	    
	    // Getting the status
	    for(int i=0; i<dbprovider.length("/calibration/ctof/status/sector"); i++) { 	    	
	        int iSec = dbprovider.getInteger("/calibration/ctof/status/sector", i);
	        int iPan = dbprovider.getInteger("/calibration/ctof/status/layer", i);
	        int iPad = dbprovider.getInteger("/calibration/ctof/status/component", i);
	        int statU = dbprovider.getInteger("/calibration/ctof/status/stat_upstream", i);
	        int statD = dbprovider.getInteger("/calibration/ctof/status/stat_downstream", i);
	         
	        STATUSU[iSec-1][iPan-1][iPad-1] = statU;
	        STATUSD[iSec-1][iPan-1][iPad-1] = statD;
	      
	    }
	    CSTLOADED = true;
		return dbprovider;
    }
   
    
    
    public static void main (String arg[]) {
    	CalibrationConstantsLoader.Load();
    }
}
