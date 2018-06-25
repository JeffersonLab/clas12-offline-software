package org.jlab.rec.ft.cal;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;

public class FTCALConstantsLoader {
	
	public FTCALConstantsLoader() {
	}
		
	public static int debugMode = 0;
	public static boolean CSTLOADED = false;
	
 /*
 // BANK TAG and NUMS statics :
	// Reco tag
	public static final int HITTAG = 911;
	public static final int CLUSTERTAG = 912;
	
	// Gemc tag&nums
    public static final int TAG = 901;
    public static final int EDEP_NUM = 7;
    public static final int E_NUM    = 6;
    public static final int TIME_NUM = 23;
    public static final int PID_NUM = 1;
    public static final int X_NUM = 8;
    public static final int Y_NUM = 9;
    public static final int Z_NUM = 10;
    
    public static final int TAG = 902;
    public static final int IDX_NUM = 1;
    public static final int IDY_NUM = 2;
    public static final int ADC_NUM = 3;
    public static final int TDC_NUM = 4; 
    
   */
//	// HIT CONVERSION CONSTANTS
//	public static double[][][] mips_charge    = new double[1][1][484];
//	public static double[][][] mips_energy    = new double[1][1][484];
//	public static double[][][] fadc_to_charge = new double[1][1][484];
//	public static double[][][] time_offset    = new double[1][1][484];
//	public static int[][][]    status         = new int[1][1][484];
 
        // RECONSTRUCTION CONSTANTS
        public static final double TIMECONVFAC = 100./4.;                            // conversion factor from TDC channel to time (ns^-1)
        public static final double VEFF        = 150.;                               // speed of light in the scintillator mm/ns
    
//	// CLUSTER RECONSTRUCTION PARAMETERS
//	public static double seed_min_energy;											      // minimum cluster reconstructed energy
//	public static double cluster_min_energy;													  // minimum number of crystals in a cluster 
//	public static int    cluster_min_size;													  // minimum number of crystals in a cluster 
//	public static double time_window;                                                  // time window of hits forming a cluster
//	public static double w0;                                                  // time window of hits forming a cluster
//	public static double depth_z;                                                  // time window of hits forming a cluster
//
//	// CONSTANTS USED IN CORRECTIONS
//	public static double[] energy_corr = new double[5] ;
//	public static double[] theta_corr  = new double[4] ;
//	public static double[] phi_corr    = new double[6] ;
//        public static double[][][] c0      = new double[1][1][484];
//        public static double[][][] c1      = new double[1][1][484];
//        public static double[][][] c2      = new double[1][1][484];
//        public static double[][][] c3      = new double[1][1][484];
//        public static double[][][] c4      = new double[1][1][484];
        
	public static int    MAX_CLUS_RAD =3 ;                                                   // maximum radius of the cluster in # of crystals
	public static double EN_THRES = 0.01;                                                   // energy threshold in GeV	
    // GEOMETRY PARAMETERS
	public static double CRYS_DELTA  = 11.5;
	public static double CRYS_WIDTH  = 15.3;					    // crystal width in mm
	public static double CRYS_LENGTH = 200.;					    // crystal length in mm
	public static double CRYS_ZPOS   = 1898.;                                           // position of the crystal front face
	
//	static DatabaseConstantProvider dbprovider = null;
//	
//        public static synchronized void Load(int runno, String var) {
//
//            System.out.println(" LOADING CONSTANTS ");
////		if (CSTLOADED == true) 
////			return null;
//            dbprovider = new DatabaseConstantProvider(runno, var); // reset using the new variation
//	    
//            	
//	    // load table reads entire table and makes an array of variables for each column in the table.
//	    dbprovider.loadTable("/calibration/ft/ftcal/charge_to_energy");
//	    dbprovider.loadTable("/calibration/ft/ftcal/time_offsets");
//	    dbprovider.loadTable("/calibration/ft/ftcal/status");
//	    dbprovider.loadTable("/calibration/ft/ftcal/cluster");
//	    dbprovider.loadTable("/calibration/ft/ftcal/phicorr");
//	    dbprovider.loadTable("/calibration/ft/ftcal/thetacorr");
//	    dbprovider.loadTable("/calibration/ft/ftcal/energycorr");
//	    //disconnect from database. Important to do this after loading tables.
//	    dbprovider.disconnect(); 
//
//	    dbprovider.show();
//		  
//	    // Getting the Timing Constants
//	    // 1) Charge to Energy Conversion: MIPS_CHARGE, MIPS_ENERGY, FADC_TO_CHARGE
//	    for(int i =0; i< dbprovider.length("/calibration/ft/ftcal/charge_to_energy/mips_charge"); i++) {
//	    	
//	    	int iSec = dbprovider.getInteger("/calibration/ft/ftcal/charge_to_energy/sector", i);	    
//	        int iLay = dbprovider.getInteger("/calibration/ft/ftcal/charge_to_energy/layer", i);
//	        int iCom = dbprovider.getInteger("/calibration/ft/ftcal/charge_to_energy/component", i);
//	        double imips_charge = dbprovider.getDouble("/calibration/ft/ftcal/charge_to_energy/mips_charge", i);
//	        double imips_energy = dbprovider.getDouble("/calibration/ft/ftcal/charge_to_energy/mips_energy", i);
//	        double ifadc2charge = dbprovider.getDouble("/calibration/ft/ftcal/charge_to_energy/fadc_to_charge", i);
//	       
//	        mips_charge[iSec-1][iLay-1][iCom-1]    = imips_charge;
//	        mips_energy[iSec-1][iLay-1][iCom-1]    = imips_energy;
//	        fadc_to_charge[iSec-1][iLay-1][iCom-1] = ifadc2charge;
//	        if(debugMode>=1) System.out.println("energy_to_charge table: " + iCom + " " + imips_charge + " " + imips_energy + " " + ifadc2charge);
//	    }
//	    // 2) Offsets : TIME_OFFSET 
//	    for(int i =0; i< dbprovider.length("/calibration/ft/ftcal/time_offsets/time_offset"); i++) {
//	    	
//	    	int iSec = dbprovider.getInteger("/calibration/ft/ftcal/time_offsets/sector", i);	    
//	        int iLay = dbprovider.getInteger("/calibration/ft/ftcal/time_offsets/layer", i);
//	        int iCom = dbprovider.getInteger("/calibration/ft/ftcal/time_offsets/component", i);
//	        double ioffset = dbprovider.getDouble("/calibration/ft/ftcal/time_offsets/time_offset", i);
//	       
//	        time_offset[iSec-1][iLay-1][iCom-1] = ioffset;
//	        if(debugMode>=1) System.out.println("time_offset: " + iCom + " " + ioffset );
//	    }
//	    // 3) Status : STATUS 
//	    for(int i =0; i< dbprovider.length("/calibration/ft/ftcal/status/status"); i++) {
//	    	
//	    	int iSec = dbprovider.getInteger("/calibration/ft/ftcal/status/sector", i);	    
//	        int iLay = dbprovider.getInteger("/calibration/ft/ftcal/status/layer", i);
//	        int iCom = dbprovider.getInteger("/calibration/ft/ftcal/status/component", i);
//	        int istatus = dbprovider.getInteger("/calibration/ft/ftcal/status/status", i);
//	       
//	        status[iSec-1][iLay-1][iCom-1] = istatus;
//	        if(debugMode>=1) System.out.println("status: " + iCom + " " + istatus );
//	    }
//	    // 4) Cluster Reconstruction Parameters : CLUSTER 
//	    for(int i =0; i< dbprovider.length("/calibration/ft/ftcal/cluster/seed_min_energy"); i++) {
//	    	
//	    	int iSec = dbprovider.getInteger("/calibration/ft/ftcal/cluster/sector", i);	    
//	        int iLay = dbprovider.getInteger("/calibration/ft/ftcal/cluster/layer", i);
//	        int iCom = dbprovider.getInteger("/calibration/ft/ftcal/cluster/component", i);
//	        double iseed    = dbprovider.getDouble("/calibration/ft/ftcal/cluster/seed_min_energy", i);
//	        double icluster = dbprovider.getDouble("/calibration/ft/ftcal/cluster/cluster_min_energy", i);
//	        int    isize    = dbprovider.getInteger("/calibration/ft/ftcal/cluster/cluster_min_size", i);
//	        double itime    = dbprovider.getDouble("/calibration/ft/ftcal/cluster/time_window", i);
//	        double iw0      = dbprovider.getDouble("/calibration/ft/ftcal/cluster/w0", i);
//	        double idepth   = dbprovider.getDouble("/calibration/ft/ftcal/cluster/depth_z", i);
//	       
//	        seed_min_energy    = iseed/1000.;
//	        cluster_min_energy = icluster/1000.;
//	        cluster_min_size   = isize;
//	        time_window        = itime;
//	        w0                 = iw0;
//	        depth_z            = idepth;
//	        if(debugMode>=1) System.out.println("cluster table: " + time_window + " " + cluster_min_energy + " " + cluster_min_size + " " + depth_z);
//	    }
//	    // 5) OLD Energy Corrections : ECORR 
//	        energy_corr[0]    =  0.0587502;
//	        energy_corr[1]    =  0.0881192;
//	        energy_corr[2]    = -0.0120113;
//                energy_corr[3]    =  0.00131961;
//	        energy_corr[4]    = -0.0000550674;
//
//                // 6) Theta Corrections : THETACORR 
//	    for(int i =0; i< dbprovider.length("/calibration/ft/ftcal/thetacorr/thetacorr0"); i++) {
//	    	
//	    	int iSec = dbprovider.getInteger("/calibration/ft/ftcal/thetacorr/sector", i);	    
//	        int iLay = dbprovider.getInteger("/calibration/ft/ftcal/thetacorr/layer", i);
//	        int iCom = dbprovider.getInteger("/calibration/ft/ftcal/thetacorr/component", i);
//	        double thetacorr0    = dbprovider.getDouble("/calibration/ft/ftcal/thetacorr/thetacorr0", i);
//	        double thetacorr1    = dbprovider.getDouble("/calibration/ft/ftcal/thetacorr/thetacorr1", i);
//	        double thetacorr2    = dbprovider.getDouble("/calibration/ft/ftcal/thetacorr/thetacorr2", i);
//	        double thetacorr3    = dbprovider.getDouble("/calibration/ft/ftcal/thetacorr/thetacorr3", i);
//	       
//	        theta_corr[0]    = thetacorr0;
//	        theta_corr[1]    = thetacorr1;
//	        theta_corr[2]    = thetacorr2;
//	        theta_corr[3]    = thetacorr3;
//	        if(debugMode>=1) System.out.println("theta correction: " + thetacorr0 + " " + thetacorr1 + " " + thetacorr2 + " " + thetacorr3 );
//	    }
//	    // 7) Phi Corrections : PHICORR 
//	    for(int i =0; i< dbprovider.length("/calibration/ft/ftcal/phicorr/phicorr0"); i++) {
//	    	
//	    	int iSec = dbprovider.getInteger("/calibration/ft/ftcal/phicorr/sector", i);	    
//	        int iLay = dbprovider.getInteger("/calibration/ft/ftcal/phicorr/layer", i);
//	        int iCom = dbprovider.getInteger("/calibration/ft/ftcal/phicorr/component", i);
//	        double phicorr0    = dbprovider.getDouble("/calibration/ft/ftcal/phicorr/phicorr0", i);
//	        double phicorr1    = dbprovider.getDouble("/calibration/ft/ftcal/phicorr/phicorr1", i);
//	        double phicorr2    = dbprovider.getDouble("/calibration/ft/ftcal/phicorr/phicorr2", i);
//	        double phicorr3    = dbprovider.getDouble("/calibration/ft/ftcal/phicorr/phicorr3", i);
//	        double phicorr4    = dbprovider.getDouble("/calibration/ft/ftcal/phicorr/phicorr4", i);
//	        double phicorr5    = dbprovider.getDouble("/calibration/ft/ftcal/phicorr/phicorr5", i);
//	       
//	        phi_corr[0]    = phicorr0;
//	        phi_corr[1]    = phicorr1;
//	        phi_corr[2]    = phicorr2;
//	        phi_corr[3]    = phicorr3;
//	        phi_corr[4]    = phicorr4;
//	        phi_corr[5]    = phicorr5;
//	        if(debugMode>=1) System.out.println("theta correction: " + phicorr0 + " " + phicorr1 + " " + phicorr2 + " " 	        														 + phicorr3 + " " + phicorr4 + " " + phicorr5);	        
//	    }
//	    // 5) NEW Energy Corrections : ENERGYCORR 
//	    for(int i =0; i< dbprovider.length("/calibration/ft/ftcal/energycorr/c0"); i++) {
//	    	
//	    	int iSec = dbprovider.getInteger("/calibration/ft/ftcal/energycorr/sector", i);	    
//	        int iLay = dbprovider.getInteger("/calibration/ft/ftcal/energycorr/layer", i);
//	        int iCom = dbprovider.getInteger("/calibration/ft/ftcal/energycorr/component", i);
//	        double ecorr0    = dbprovider.getDouble("/calibration/ft/ftcal/energycorr/c0", i);
//	        double ecorr1    = dbprovider.getDouble("/calibration/ft/ftcal/energycorr/c1", i);
//	        double ecorr2    = dbprovider.getDouble("/calibration/ft/ftcal/energycorr/c2", i);
//	        double ecorr3    = dbprovider.getDouble("/calibration/ft/ftcal/energycorr/c3", i);
//	        double ecorr4    = dbprovider.getDouble("/calibration/ft/ftcal/energycorr/c4", i);
//	       
//                c0[iSec-1][iLay-1][iCom-1] = ecorr0;
//                c1[iSec-1][iLay-1][iCom-1] = ecorr1;
//                c2[iSec-1][iLay-1][iCom-1] = ecorr2;
//                c3[iSec-1][iLay-1][iCom-1] = ecorr3;
//                c4[iSec-1][iLay-1][iCom-1] = ecorr4;
//                
//	        if(debugMode>=1) System.out.println("energy correction: " + ecorr0 + " " + ecorr1 + " " + ecorr2 + " " + ecorr3 + " " + ecorr4);
//	    }
//
//	
//	CSTLOADED = true;
//        System.out.println("SUCCESSFULLY LOADED FTCAL CALIBRATION CONSTANTS....");
////	return dbprovider;
//	
//	}
		
}
