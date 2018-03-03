package org.jlab.rec.ft.hodo;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;


public class FTHODOConstantsLoader {
	
	FTHODOConstantsLoader() {
	}
	
	public static int debugMode = 0;
	public static boolean CSTLOADED = false;

	
	/*
 	// BANK TAG and NUMS statics :
	// Reco tag
	public static final int RECOTAG = 811;
	// Gemc tag&nums
    public static final int TAG = 801;
    public static final int EDEP_NUM = 7;
    public static final int E_NUM    = 6;
    public static final int TIME_NUM = 23;
    public static final int PID_NUM = 1;
    public static final int X_NUM = 8;
    public static final int Y_NUM = 9;
    public static final int Z_NUM = 10;
    
    public static final int TAG = 802;
    public static final int TILEID_NUM = 1;
    public static final int LAYERID_NUM = 2;
    public static final int ADC_NUM = 3;
    public static final int TDC_NUM = 4;
    
   */

//	// HIT CONVERSION CONSTANTS
//	public static double[][][] mips_charge    = new double[8][2][20];
//	public static double[][][] mips_energy    = new double[8][2][20];
//	public static double[][][] time_offset    = new double[8][2][20];
//	public static int[][][]    status         = new int[8][2][20];
//	
//	//GEOMETRY
//	public static double[][][] px             = new double[8][2][20];
//	public static double[][][] py             = new double[8][2][20];
//	public static double[][][] pz             = new double[8][2][20];

        // RECONSTRUCTION CONSTANTS
        public static double TIMECONVFAC = 100./4.;                            // conversion factor from TDC channel to time (ns^-1)
	public static double EN_THRES = 0.25;                                                   // energy threshold in MeV	
	public static double FADC_TO_CHARGE = 4*0.4884/50.;

	// CLUSTER RECONSTRUCTION PARAMETERS
	public static double cluster_min_energy = 1.;			       // minimum number of crystals in a cluster in MeV
	public static int    cluster_min_size   = 2;			       // minimum number of crystals in a cluster 
	public static double time_window        = 8;                           // time window of hits forming a cluster
	public static double hit_distance       = 3;                           // max distance of hits forming a cluster in cm

//	static DatabaseConstantProvider dbprovider = null;
//
//        public static synchronized void Load(int runno, String var) {
//
//            System.out.println(" LOADING CONSTANTS ");
////		if (CSTLOADED == true) 
////			return null;
//            dbprovider = new DatabaseConstantProvider(runno, var); // reset using the new variation
//				
//	    // load table reads entire table and makes an array of variables for each column in the table.
//	    dbprovider.loadTable("/calibration/ft/fthodo/charge_to_energy");
//	    dbprovider.loadTable("/calibration/ft/fthodo/time_offsets");
//	    dbprovider.loadTable("/calibration/ft/fthodo/status");
//	    dbprovider.loadTable("/geometry/ft/fthodo");
//	    //disconnect from database. Important to do this after loading tables.
//	    dbprovider.disconnect(); 
//
//	    dbprovider.show();
//		  
//	    // Getting the Timing Constants
//	    // 1) Charge to Energy Conversion: MIPS_CHARGE, MIPS_ENERGY, FADC_TO_CHARGE
//	    for(int i =0; i< dbprovider.length("/calibration/ft/fthodo/charge_to_energy/mips_charge"); i++) {
//	    	
//	    	int iSec = dbprovider.getInteger("/calibration/ft/fthodo/charge_to_energy/sector", i);	    
//	        int iLay = dbprovider.getInteger("/calibration/ft/fthodo/charge_to_energy/layer", i);
//	        int iCom = dbprovider.getInteger("/calibration/ft/fthodo/charge_to_energy/component", i);
//	        double imips_charge = dbprovider.getDouble("/calibration/ft/fthodo/charge_to_energy/mips_charge", i);
//	        double imips_energy = dbprovider.getDouble("/calibration/ft/fthodo/charge_to_energy/mips_energy", i);
//	       
//	        mips_charge[iSec-1][iLay-1][iCom-1]    = imips_charge;
//	        mips_energy[iSec-1][iLay-1][iCom-1]    = imips_energy;
//	        if(debugMode>=1) System.out.println("energy_to_charge table: " + iCom + " " + imips_charge + " " + imips_energy );
//	    }
//	    // 2) Offsets : TIME_OFFSET 
//	    for(int i =0; i< dbprovider.length("/calibration/ft/fthodo/time_offsets/time_offset"); i++) {
//	    	
//	    	int iSec = dbprovider.getInteger("/calibration/ft/fthodo/time_offsets/sector", i);	    
//	        int iLay = dbprovider.getInteger("/calibration/ft/fthodo/time_offsets/layer", i);
//	        int iCom = dbprovider.getInteger("/calibration/ft/fthodo/time_offsets/component", i);
//	        double ioffset = dbprovider.getDouble("/calibration/ft/fthodo/time_offsets/time_offset", i);
//	       
//	        time_offset[iSec-1][iLay-1][iCom-1] = ioffset;
//	        if(debugMode>=1) System.out.println("time_offsets: " + iCom + " " + ioffset );
//	    }
//	    // 3) Status : STATUS 
//	    for(int i =0; i< dbprovider.length("/calibration/ft/fthodo/status/status"); i++) {
//	    	
//	    	int iSec = dbprovider.getInteger("/calibration/ft/fthodo/status/sector", i);	    
//	        int iLay = dbprovider.getInteger("/calibration/ft/fthodo/status/layer", i);
//	        int iCom = dbprovider.getInteger("/calibration/ft/fthodo/status/component", i);
//	        int istatus = dbprovider.getInteger("/calibration/ft/fthodo/status/status", i);
//	       
//	        status[iSec-1][iLay-1][iCom-1] = istatus;
//	        if(debugMode>=1) System.out.println("status: " + iCom + " " + istatus );
//	    }
//	    // 4) Geometry 
//	    for(int i =0; i< dbprovider.length("/geometry/ft/fthodo/x"); i++) {
//	    	
//	    	int iSec = dbprovider.getInteger("/geometry/ft/fthodo/sector", i);	    
//	        int iLay = dbprovider.getInteger("/geometry/ft/fthodo/layer", i);
//	        int iCom = dbprovider.getInteger("/geometry/ft/fthodo/component", i);
//	        double x = dbprovider.getDouble("/geometry/ft/fthodo/x", i);
//	        double y = dbprovider.getDouble("/geometry/ft/fthodo/y", i);
//	        double z = dbprovider.getDouble("/geometry/ft/fthodo/z", i);
//	       
//	        px[iSec-1][iLay-1][iCom-1] = x;
//	        py[iSec-1][iLay-1][iCom-1] = y;
//	        pz[iSec-1][iLay-1][iCom-1] = z;
//	        if(debugMode>=1) System.out.println("geometry: " + iCom + " " + x + " " + y + " " + z);
//	    }
//	CSTLOADED = true;
//    System.out.println("SUCCESSFULLY LOADED FTHODO CALIBRATION CONSTANTS....");
////	return dbprovider;
//	
//	}
	
	
}
