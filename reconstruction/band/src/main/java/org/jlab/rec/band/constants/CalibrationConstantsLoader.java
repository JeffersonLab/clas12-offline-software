package org.jlab.rec.band.constants;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.netlib.util.doubleW;

import java.util.HashMap;
import java.util.Map;
import java.lang.Integer;
import java.awt.List;
import java.lang.Double;

/**
 * 
 * @author Efrain Segarra
 * This class loads constants from CCDB
 */

public class CalibrationConstantsLoader {

	public CalibrationConstantsLoader() {
		// TODO Auto-generated constructor stub
	}
	public static boolean CSTLOADED = false;

	// Maps for constants from database
	public static Map<Integer, Double> TDC_T_OFFSET       = new HashMap<Integer,Double>  ();	// TDC L-R offset [ns]
	public static Map<Integer, Double> FADC_T_OFFSET      = new HashMap<Integer,Double>  ();	// FADC L-R offset [ns]
	public static Map<Integer, Double> TDC_VEFF           = new HashMap<Integer,Double>  ();	// TDC effective velocity [cm/ns]
	public static Map<Integer, Double> FADC_VEFF          = new HashMap<Integer,Double>  ();	// FADC effective velocity [cm/ns]
	public static Map<Integer, Double> FADC_MT_P2P_OFFSET = new HashMap<Integer,Double>  ();	// FADC mean time paddle-to-paddle offset [ns]
	public static Map<Integer, Double> FADC_MT_P2P_RES    = new HashMap<Integer,Double>  ();	// FADC mean time paddle resolution [ns]
	public static Map<Integer, Double> FADC_MT_L2L_OFFSET = new HashMap<Integer,Double>  ();	// FADC mean time layer-by-layer offset [ns]
	public static Map<Integer, Double> FADC_MT_L2L_RES    = new HashMap<Integer,Double>  ();	// FADC mean time layer resolution [ns]
	public static Map<Integer, Double> FADC_ATTEN_LENGTH  = new HashMap<Integer,Double>  ();	// FADC attenuation length [cm]
	public static Map<Integer, double[]> TIMEWALK_L       = new HashMap<Integer,double[]>(); 	// Parameters for time-walk correction for L PMTs
	public static Map<Integer, double[]> TIMEWALK_R       = new HashMap<Integer,double[]>(); 	// Parameters for time-walk correction for R PMTs

	public static double JITTER_PERIOD = 0;
	public static int JITTER_PHASE = 0;
	public static int JITTER_CYCLES = 0;


	static DatabaseConstantProvider dbprovider = null;

	public static synchronized void Load(int runno, String var) {

		//System.out.println("*Loading calibration constants*");

		dbprovider = new DatabaseConstantProvider(runno, var); // reset using the new variation

		// load table reads entire table and makes an array of variables for each column in the table.
		dbprovider.loadTable("/calibration/band/time_jitter"	    );
		dbprovider.loadTable("/calibration/band/lr_offsets"         );
		dbprovider.loadTable("/calibration/band/effective_velocity" );
		dbprovider.loadTable("/calibration/band/paddle_offsets"     );
		dbprovider.loadTable("/calibration/band/layer_offsets"      );
		dbprovider.loadTable("/calibration/band/attenuation_lengths");
		dbprovider.loadTable("/calibration/band/time_walk_corr_left"     );
		dbprovider.loadTable("/calibration/band/time_walk_corr_right"     );

		//disconncect from database. Important to do this after loading tables.
		dbprovider.disconnect(); 
		dbprovider.show();

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Time offsets
		for(int i =0; i< dbprovider.length("/calibration/band/lr_offsets/sector"); i++) {
			// Get sector, layer, component
			int sector 		= dbprovider.getInteger("/calibration/band/lr_offsets/sector", 		i);	    
			int layer 		= dbprovider.getInteger("/calibration/band/lr_offsets/layer", 		i);
			int component 	= dbprovider.getInteger("/calibration/band/lr_offsets/component", 	i);
			// Get the actual offsets
			double tdc_off 	= dbprovider.getDouble("/calibration/band/lr_offsets/tdc_off", 		i);
			double fadc_off = dbprovider.getDouble("/calibration/band/lr_offsets/fadc_off", 	i);
			// Put in the maps
			int key = sector*100+layer*10+component;
			TDC_T_OFFSET.put( 	Integer.valueOf(key), 	Double.valueOf(tdc_off ) );
			FADC_T_OFFSET.put( 	Integer.valueOf(key), 	Double.valueOf(fadc_off) );
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Time-walk correction parameters for left PMTs
		for(int i =0; i< dbprovider.length("/calibration/band/time_walk_corr_left/sector"); i++) {
			// Get sector, layer, component
			int sector 		= dbprovider.getInteger("/calibration/band/time_walk_corr_left/sector"   , 	i);	    
			int layer 		= dbprovider.getInteger("/calibration/band/time_walk_corr_left/layer"    , 	i);
			int component 	= dbprovider.getInteger("/calibration/band/time_walk_corr_left/component", 	i);
			// Get parameters
			double parA = dbprovider.getDouble("/calibration/band/time_walk_corr_left/par_a",i);
			double parB = dbprovider.getDouble("/calibration/band/time_walk_corr_left/par_b",i);
			// Get errors
			double errA = dbprovider.getDouble("/calibration/band/time_walk_corr_left/err_a",i);
			double errB = dbprovider.getDouble("/calibration/band/time_walk_corr_left/err_b",i);
			// Put in the maps
			int key = sector*100+layer*10+component;
			double time_walk_params[] = {parA,parB,errA,errB};
			TIMEWALK_L.put(Integer.valueOf(key), time_walk_params);
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Time-walk correction parameters for right PMTs
		for(int i =0; i< dbprovider.length("/calibration/band/time_walk_corr_right/sector"); i++) {
			// Get sector, layer, component
			int sector 		= dbprovider.getInteger("/calibration/band/time_walk_corr_right/sector"   , 	i);	    
			int layer 		= dbprovider.getInteger("/calibration/band/time_walk_corr_right/layer"    , 	i);
			int component 	= dbprovider.getInteger("/calibration/band/time_walk_corr_right/component", 	i);
			// Get parameters
			double parA = dbprovider.getDouble("/calibration/band/time_walk_corr_right/par_a",i);
			double parB = dbprovider.getDouble("/calibration/band/time_walk_corr_right/par_b",i);
			// Get errors
			double errA = dbprovider.getDouble("/calibration/band/time_walk_corr_right/err_a",i);
			double errB = dbprovider.getDouble("/calibration/band/time_walk_corr_right/err_b",i);
			// Put in the maps
			int key = sector*100+layer*10+component;
			double time_walk_params[] = {parA,parB,errA,errB};
			TIMEWALK_R.put(Integer.valueOf(key), time_walk_params);
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Speed of lights
		for(int i =0; i< dbprovider.length("/calibration/band/effective_velocity/sector"); i++) {
			// Get sector, layer, component
			int sector 		= dbprovider.getInteger("/calibration/band/effective_velocity/sector",		i);	    
			int layer 		= dbprovider.getInteger("/calibration/band/effective_velocity/layer",		i);
			int component 	= dbprovider.getInteger("/calibration/band/effective_velocity/component",	i);
			// Get the velocities
			double veff_tdc		= dbprovider.getDouble("/calibration/band/effective_velocity/veff_tdc", 	i);
			double veff_fadc	= dbprovider.getDouble("/calibration/band/effective_velocity/veff_fadc",	i);
			// Put in the maps
			int key = sector*100+layer*10+component;
			TDC_VEFF.put(	Integer.valueOf(key),		Double.valueOf(veff_tdc) );
			FADC_VEFF.put(	Integer.valueOf(key), 		Double.valueOf(veff_fadc) );
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// TDC time jitter
		for( int i = 0; i < dbprovider.length("/calibration/band/time_jitter/sector"); i++){
			int sector 		= dbprovider.getInteger("/calibration/band/time_jitter/sector",          i);
			int layer 		= dbprovider.getInteger("/calibration/band/time_jitter/layer",          i);
			int component 		= dbprovider.getInteger("/calibration/band/time_jitter/component",          i);
			double period 		= dbprovider.getDouble("/calibration/band/time_jitter/period", i);
			double phase 		= dbprovider.getDouble("/calibration/band/time_jitter/phase", i);
			double cycles 		= dbprovider.getDouble("/calibration/band/time_jitter/cycles", i);
			JITTER_PERIOD = dbprovider.getDouble("/calibration/band/time_jitter/period", 0);
			JITTER_PHASE  = dbprovider.getInteger("/calibration/band/time_jitter/phase", 0);
			JITTER_CYCLES = dbprovider.getInteger("/calibration/band/time_jitter/cycles", 0);
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Paddle-to-paddle offsets
		for(int i =0; i< dbprovider.length("/calibration/band/paddle_offsets/sector"); i++) {
			int sector 		= dbprovider.getInteger("/calibration/band/paddle_offsets/sector",		i);	    
			int layer 		= dbprovider.getInteger("/calibration/band/paddle_offsets/layer",		i);
			int component 	= dbprovider.getInteger("/calibration/band/paddle_offsets/component",	i);

			// Get offset and resolution for FADC
			double p2p_off_fadc	= dbprovider.getDouble("/calibration/band/paddle_offsets/offset_fadc", 	i);
			double p2p_res_fadc	= dbprovider.getDouble("/calibration/band/paddle_offsets/resolution_fadc",	i);

			// Put in maps
			int key = sector*100+layer*10+component;
			FADC_MT_P2P_OFFSET.put(	Integer.valueOf(key),		Double.valueOf(p2p_off_fadc) );
			FADC_MT_P2P_RES.put(	Integer.valueOf(key),		Double.valueOf(p2p_res_fadc) );

		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Layer-to-layer offsets
		for(int i =0; i< dbprovider.length("/calibration/band/layer_offsets/sector"); i++) {
			int sector 		= dbprovider.getInteger("/calibration/band/layer_offsets/sector",		i);	    
			int layer 		= dbprovider.getInteger("/calibration/band/layer_offsets/layer",		i);
			int component 	= dbprovider.getInteger("/calibration/band/layer_offsets/component",	i);

			// Get offset and resolution for FADC
			double l2l_off_fadc	= dbprovider.getDouble("/calibration/band/layer_offsets/offset_fadc", 	i);
			double l2l_res_fadc	= dbprovider.getDouble("/calibration/band/layer_offsets/resolution_fadc",	i);

			// Put in maps
			int key = sector*100+layer*10+component;
			FADC_MT_L2L_OFFSET.put(	Integer.valueOf(key),		Double.valueOf(l2l_off_fadc) );
			FADC_MT_L2L_RES.put(	Integer.valueOf(key),		Double.valueOf(l2l_res_fadc) );
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Attenuation constants
		for( int i=0; i < dbprovider.length("/calibration/band/attenuation_lengths/sector"); i++){
			int sector 	 	= dbprovider.getInteger("/calibration/band/attenuation_lengths/sector", 	i);
			int layer		= dbprovider.getInteger("/calibration/band/attenuation_lengths/layer",		i);
			int component		= dbprovider.getInteger("/calibration/band/attenuation_lengths/component",	i);

			// Grab attenuation length
			double atten		= dbprovider.getDouble("/calibration/band/attenuation_lengths/atten_len",	i);
			double atten_err	= dbprovider.getDouble("/calibration/band/attenuation_lengths/atten_len_err",	i);

			// Put in map
			int key = sector*100+layer*10+component;
			FADC_ATTEN_LENGTH.put(	Integer.valueOf(key),		Double.valueOf(atten) 	);
		}

		CSTLOADED = true;
		//System.out.println("SUCCESSFULLY LOADED band CALIBRATION CONSTANTS....");

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
