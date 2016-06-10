package org.jlab.rec.dc;

import org.jlab.clasrec.utils.DatabaseConstantProvider;

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
	public static double[][] PAR1 			= new double[6][6];
	public static double[][] PAR2 			= new double[6][6];
	public static double[][] PAR3 			= new double[6][6];
	public static double[][] PAR4 			= new double[6][6];
	public static double[][] SCAL 			= new double[6][6];
	
	public static int[][][][] STATUS 			= new int[6][6][6][112];
	
	 //Calibration parameters from DB    
    static DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(10,"default");
    //private Detector ftofDetector;
    public static boolean areCalibConstantsLoaded = false;
    
    public static synchronized void Load() {
    	
		if (CSTLOADED) return;
		
		dbprovider = new DatabaseConstantProvider(10,Constants.DBVAR); // reset using the variation
	    // load table reads entire table and makes an array of variables for each column in the table.
	    dbprovider.loadTable("/calibration/dc/signal_generation/dc_resolution");
	   
	    //disconncect from database. Important to do this after loading tables.
	    dbprovider.disconnect(); 

	    //dbprovider.show();
	    
	    // Getting the  Constants
	    // 1) Time RMS
	    for(int i =0; i< dbprovider.length("/calibration/dc/signal_generation/dc_resolution/Sector"); i++) {
	    	
	    	int iSec = dbprovider.getInteger("/calibration/dc/signal_generation/dc_resolution/Sector", i);	    
	        int iSly = dbprovider.getInteger("/calibration/dc/signal_generation/dc_resolution/Superlayer", i);
	        double iPAR1 = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/parameter1", i);
	        double iPAR2 = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/parameter2", i);
	        double iPAR3 = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/parameter3", i);
	        double iPAR4 = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/parameter4", i);
	        double iSCAL = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/scale", i);

	        PAR1[iSec-1][iSly-1] = iPAR1;
	        PAR2[iSec-1][iSly-1] = iPAR2;
	        PAR3[iSec-1][iSly-1] = iPAR3;
	        PAR4[iSec-1][iSly-1] = iPAR4;
	        SCAL[iSec-1][iSly-1] = iSCAL;
	        
	        System.out.println(" iSec "+iSec+" iSly "+iSly+" iPAR1 "+iPAR1+" iPAR2 "+iPAR2+" iPAR3 "+iPAR3+" iPAR4 "+iPAR4+" iSCAL "+iSCAL);
	        
	    }
	    
	    CSTLOADED = true;
    }
   
    
    
    public static void main (String arg[]) {
    	CalibrationConstantsLoader.Load();
    }
}
