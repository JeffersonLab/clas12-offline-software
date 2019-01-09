package org.jlab.rec.band.constants;

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
	public static double[][] UTURNELOSS 		= new double[24][3];
	public static double[][] UTURNTLOSS 		= new double[24][3];
	public static double[][] TIMEOFFSETSLR 		= new double[24][3];	
	public static double[][][] TDCTOTIMESLOPE	= new double[24][3][2];
	public static double[][][] TDCTOTIMEOFFSET	= new double[24][3][2];	
	public static double[][] TIMEOFFSETSECT 	= new double[24][3];
	public static double[][][] EFFVEL 		= new double[24][3][2];
	public static double[][][] ATNLEN               = new double[24][3][2];
	public static double[][][] MIPDIRECT 		= new double[24][3][2];
	public static double[][][] MIPINDIRECT		= new double[24][3][2];
	public static int[][][] Status_LR 		= new int[24][3][2];
	public static double JITTER_PERIOD              = 0;
	public static int JITTER_PHASE                  = 0;
	public static int JITTER_CYCLES                 = 0;
	public static double[] LENGTH                   = new double[3];
	public static double[] ZOFFSET                  = new double[3];
	public static double[] THICKNESS                = new double[1];
	public static double[] INNERRADIUS              = new double[1];
	//Calibration and geometry parameters from DB    

	public static boolean arEnergyibConstantsLoaded = false;

	static DatabaseConstantProvider dbprovider = null;

	public static synchronized void Load(int runno, String var) {

		System.out.println(" LOADING CONSTANTS ");
		dbprovider = new DatabaseConstantProvider(runno, var); // reset using the new variation

		// load table reads entire table and makes an array of variables for each column in the table.
		dbprovider.loadTable("/calibration/band/UturnEloss");
		dbprovider.loadTable("/calibration/band/UturnTloss");
		dbprovider.loadTable("/calibration/band/TimeOffsets_LR");
		dbprovider.loadTable("/calibration/band/TDC_conv");
		dbprovider.loadTable("/calibration/band/TimeOffsets_layer");
		dbprovider.loadTable("/calibration/band/EffV");
		dbprovider.loadTable("/calibration/band/Attenuation");
		dbprovider.loadTable("/calibration/band/Status_LR");
		dbprovider.loadTable("/calibration/band/Energy");
		dbprovider.loadTable("/calibration/band/time_jitter");
		dbprovider.loadTable("/geometry/band/layer");
		dbprovider.loadTable("/geometry/band/band");

		//disconncect from database. Important to do this after loading tables.
		dbprovider.disconnect(); 

		dbprovider.show();


		//E-loss
		for(int i =0; i< dbprovider.length("/calibration/band/UturnEloss/sector"); i++) {

			int iSec = dbprovider.getInteger("/calibration/band/UturnEloss/sector", i);	    
			int iLay = dbprovider.getInteger("/calibration/band/UturnEloss/layer", i);	       
			double iEL = dbprovider.getDouble("/calibration/band/UturnEloss/uturn_eloss", i);

			UTURNELOSS[iSec-1][iLay-1] = iEL;
			//System.out.println("UturnEloss "+iEL);
		}
		//T-loss
		for(int i =0; i< dbprovider.length("/calibration/band/UturnTloss/sector"); i++) {

			int iSec = dbprovider.getInteger("/calibration/band/UturnTloss/sector", i);	    
			int iLay = dbprovider.getInteger("/calibration/band/UturnTloss/layer", i);
			double iTL = dbprovider.getDouble("/calibration/band/UturnTloss/uturn_tloss", i);

			UTURNTLOSS[iSec-1][iLay-1] = iTL;
			//System.out.println("UturnTloss "+iTL);
		}
		// Time offsets
		for(int i =0; i< dbprovider.length("/calibration/band/TimeOffsets_LR/sector"); i++) {

			int iSec = dbprovider.getInteger("/calibration/band/TimeOffsets_LR/sector", i);	    
			int iLay = dbprovider.getInteger("/calibration/band/TimeOffsets_LR/layer", i);
			double iTO = dbprovider.getDouble("/calibration/band/TimeOffsets_LR/time_offset_LR", i);

			TIMEOFFSETSLR[iSec-1][iLay-1] = iTO;
			//System.out.println("time_offset_LR "+iTO);
		}
		//TDC to time conversion
		for(int i =0; i< dbprovider.length("/calibration/band/TDC_conv/sector"); i++) {

			int iSec = dbprovider.getInteger("/calibration/band/TDC_conv/sector", i);	    
			int iLay = dbprovider.getInteger("/calibration/band/TDC_conv/layer", i);
			double iSlL = dbprovider.getDouble("/calibration/band/TDC_conv/slope_L", i);
			double iSlR = dbprovider.getDouble("/calibration/band/TDC_conv/slope_R", i);
			double iOfL = dbprovider.getDouble("/calibration/band/TDC_conv/offset_L", i);
			double iOfR = dbprovider.getDouble("/calibration/band/TDC_conv/offset_R", i);

			TDCTOTIMESLOPE[iSec-1][iLay-1][0] = iSlL;
			TDCTOTIMEOFFSET[iSec-1][iLay-1][0] = iOfL;
			TDCTOTIMESLOPE[iSec-1][iLay-1][1] = iSlR;
			TDCTOTIMEOFFSET[iSec-1][iLay-1][1] = iOfR;
			//System.out.println("TDCTOTIMESLOPE "+iSl);
			//System.out.println("TDCTOTIMEOFFSET "+iOf);
		}
		// ?Time offsets _layer ... DB entry says time_offset_sector
		for(int i =0; i< dbprovider.length("/calibration/band/TimeOffsets_layer/sector"); i++) {

			int iSec = dbprovider.getInteger("/calibration/band/TimeOffsets_layer/sector", i);	    
			int iLay = dbprovider.getInteger("/calibration/band/TimeOffsets_layer/layer", i);
			double iTO = dbprovider.getDouble("/calibration/band/TimeOffsets_layer/time_offset_layer", i);

			TIMEOFFSETSECT[iSec-1][iLay-1] = iTO;
		}
		// Attenuation length
		for(int i =0; i< dbprovider.length("/calibration/band/Attenuation/sector"); i++) {

			int iSec = dbprovider.getInteger("/calibration/band/Attenuation/sector", i);	    
			int iLay = dbprovider.getInteger("/calibration/band/Attenuation/layer", i);
			double iALL = dbprovider.getDouble("/calibration/band/Attenuation/attlen_L", i);
			double iALR = dbprovider.getDouble("/calibration/band/Attenuation/attlen_R", i);

			ATNLEN[iSec-1][iLay-1][0] = iALL;
			ATNLEN[iSec-1][iLay-1][1] = iALR;
		}
		// Effective velocity
		for(int i =0; i< dbprovider.length("/calibration/band/EffV/sector"); i++) {

			int iSec = dbprovider.getInteger("/calibration/band/EffV/sector", i);	    
			int iLay = dbprovider.getInteger("/calibration/band/EffV/layer", i);
			double iVEL = dbprovider.getDouble("/calibration/band/EffV/veff_L", i);
			double iVER = dbprovider.getDouble("/calibration/band/EffV/veff_R", i);

			EFFVEL[iSec-1][iLay-1][0] = iVEL;
			EFFVEL[iSec-1][iLay-1][1] = iVER;
		}
		// Energy
		for(int i =0; i< dbprovider.length("/calibration/band/Energy/sector"); i++) {

			int iSec = dbprovider.getInteger("/calibration/band/Energy/sector", i);	    
			int iLay = dbprovider.getInteger("/calibration/band/Energy/layer", i);
			double iMIPDL = dbprovider.getDouble("/calibration/band/Energy/mip_dir_L", i);
			double iMIPIL = dbprovider.getDouble("/calibration/band/Energy/mip_indir_L", i);
			double iMIPDR = dbprovider.getDouble("/calibration/band/Energy/mip_dir_R", i);
			double iMIPIR = dbprovider.getDouble("/calibration/band/Energy/mip_indir_R", i);

			MIPDIRECT[iSec-1][iLay-1][0] = iMIPDL;
			MIPINDIRECT[iSec-1][iLay-1][0] = iMIPIL;
			MIPDIRECT[iSec-1][iLay-1][1] = iMIPDR;
			MIPINDIRECT[iSec-1][iLay-1][1] = iMIPIR;
		}
		// Status_LR
		for(int i =0; i< dbprovider.length("/calibration/band/Status_LR/sector"); i++) {

			int iSec = dbprovider.getInteger("/calibration/band/Status_LR/sector", i);	    
			int iLay = dbprovider.getInteger("/calibration/band/Status_LR/layer", i);
			int iStatL= dbprovider.getInteger("/calibration/band/Status_LR/status_L", i);
			int iStatR= dbprovider.getInteger("/calibration/band/Status_LR/status_R", i);

			Status_LR[iSec-1][iLay-1][0] = iStatL;
			Status_LR[iSec-1][iLay-1][1] = iStatR;
			//System.out.println("Status_LR "+iStat);
		}
		// TDC time jitter
		JITTER_PERIOD = dbprovider.getDouble("/calibration/band/time_jitter/period", 0);
		JITTER_PHASE  = dbprovider.getInteger("/calibration/band/time_jitter/phase", 0);
		JITTER_CYCLES = dbprovider.getInteger("/calibration/band/time_jitter/cycles", 0);

		// Geometry
		for(int i =0; i< dbprovider.length("/geometry/band/layer/layer"); i++) {
			int iLay = dbprovider.getInteger("/geometry/band/layer/layer", i);
			double iLowerBase = dbprovider.getDouble("/geometry/band/layer/LowerBase", i);
			double iHigherBase = dbprovider.getDouble("/geometry/band/layer/HigherBase", i);
			double iLength = dbprovider.getDouble("/geometry/band/layer/Length", i);
			double iUpstreamZOffset = dbprovider.getDouble("/geometry/band/layer/UpstreamZOffset", i);

			LENGTH[iLay-1] = iLength;
			ZOFFSET[iLay-1] = iUpstreamZOffset;               
		}
		
		// +1 added
		for(int i =0; i< dbprovider.length("/geometry/band/band")+1; i++) { 
			int iband = dbprovider.getInteger("/geometry/band/band", i);
			double iInnerRadius = dbprovider.getDouble("/geometry/band/band/InnerRadius", i);            
			double iOpenAngle = dbprovider.getDouble("/geometry/band/band/OpenAngle", i);            
			double iThickness = dbprovider.getDouble("/geometry/band/band/Thickness", i);      
			double iAzimuthalGap = dbprovider.getDouble("/geometry/band/band/AzimuthalGap", i);  
			double iLateralGap = dbprovider.getDouble("/geometry/band/band/LateralGap", i);

			INNERRADIUS[iband] = iInnerRadius;               
			THICKNESS[iband] = iThickness;

		}       
		CSTLOADED = true;
		System.out.println("SUCCESSFULLY LOADED band CALIBRATION CONSTANTS....");

		setDB(dbprovider);
	} 


	private static DatabaseConstantProvider DB;

	public static final DatabaseConstantProvider getDB() {
		return DB;
	}



	public static final void setDB(DatabaseConstantProvider dB) {
		DB = dB;
	}

	public static void main (String arg[]) {
		CalibrationConstantsLoader.Load(10,"default");
	}
}
