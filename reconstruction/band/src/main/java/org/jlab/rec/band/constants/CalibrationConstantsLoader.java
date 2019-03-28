package org.jlab.rec.band.constants;

import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.utils.groups.IndexedTable;

import java.util.HashMap;
import java.util.Map;
import java.lang.Integer;
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
	public static Map<Integer, Double> TDC_MT_P2P_OFFSET  = new HashMap<Integer,Double>  ();	// TDC mean time paddle-to-paddle offset [ns]
	public static Map<Integer, Double> TDC_MT_P2P_RES     = new HashMap<Integer,Double>  ();	// TDC mean time paddle resolution [ns]
	public static Map<Integer, Double> TDC_MT_L2L_OFFSET  = new HashMap<Integer,Double>  ();	// TDC mean time layer-by-layer offset [ns]
	public static Map<Integer, Double> TDC_MT_L2L_RES     = new HashMap<Integer,Double>  ();	// TDC mean time layer resolution [ns]
	public static Map<Integer, Double> FADC_ATTEN_LENGTH  = new HashMap<Integer,Double>  ();	// FADC attenuation length [cm]
	public static Map<Integer, double[]> TIMEWALK_L       = new HashMap<Integer,double[]>(); 	// Parameters for time-walk correction for L PMTs
	public static Map<Integer, double[]> TIMEWALK_R       = new HashMap<Integer,double[]>(); 	// Parameters for time-walk correction for R PMTs

	public static double JITTER_PERIOD = 0;
	public static int JITTER_PHASE = 0;
	public static int JITTER_CYCLES = 0;

	public static synchronized void Load(int runno, String var, ConstantsManager manager) {

		//System.out.println("*Loading calibration constants*");
		manager.setVariation(var);
		
	    IndexedTable  lroffsets  = manager.getConstants(runno, "/calibration/band/lr_offsets");
	    IndexedTable  timewalkL  = manager.getConstants(runno, "/calibration/band/time_walk_corr_left");
	    IndexedTable  timewalkR  = manager.getConstants(runno, "/calibration/band/time_walk_corr_right");
	    IndexedTable  effvel     = manager.getConstants(runno, "/calibration/band/effective_velocity");
	    IndexedTable  timejitter = manager.getConstants(runno, "/calibration/band/time_jitter");
	    IndexedTable  attenuation= manager.getConstants(runno, "/calibration/band/attenuation_lengths");
	    IndexedTable  paddleoffs_fadc = manager.getConstants(runno, "/calibration/band/paddle_offsets");
	    IndexedTable  layeroffs_fadc  = manager.getConstants(runno, "/calibration/band/layer_offsets");
	    IndexedTable  paddleoffs_tdc  = manager.getConstants(runno, "/calibration/band/paddle_offsets_tdc");
	    IndexedTable  layeroffs_tdc   = manager.getConstants(runno, "/calibration/band/layer_offsets_tdc");	    
	    
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Time offsets
		for(int i = 0; i < lroffsets.getRowCount(); i++) {
			// Get sector, layer, component
			int sector    = Integer.parseInt((String)lroffsets.getValueAt(i, 0));
       	    int layer     = Integer.parseInt((String)lroffsets.getValueAt(i, 1));
       	    int component = Integer.parseInt((String)lroffsets.getValueAt(i, 2));
			// Get the actual offsets
			double tdc_off 	= lroffsets.getDoubleValue("tdc_off",  sector, layer, component);
			double fadc_off = lroffsets.getDoubleValue("fadc_off", sector, layer, component);
			// Put in the maps
			int key = sector*100+layer*10+component;
			TDC_T_OFFSET.put( 	Integer.valueOf(key), 	Double.valueOf(tdc_off ) );
			FADC_T_OFFSET.put( 	Integer.valueOf(key), 	Double.valueOf(fadc_off) );		
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Time-walk correction parameters for left PMTs
		for(int i = 0; i < timewalkL.getRowCount(); i++) {
			// Get sector, layer, component
			int sector    = Integer.parseInt((String)timewalkL.getValueAt(i, 0));
       	    int layer     = Integer.parseInt((String)timewalkL.getValueAt(i, 1));
       	    int component = Integer.parseInt((String)timewalkL.getValueAt(i, 2));
			// Get parameters
			double parA = timewalkL.getDoubleValue("par_a", sector, layer, component); 
			double parB = timewalkL.getDoubleValue("par_b", sector, layer, component); 
			//Get errors
			double errA = timewalkL.getDoubleValue("err_a", sector, layer, component); 
			double errB = timewalkL.getDoubleValue("err_b", sector, layer, component); 
			// Put in the maps
			int key = sector*100+layer*10+component;
			double time_walk_params[] = {parA,parB,errA,errB};
			TIMEWALK_L.put(Integer.valueOf(key), time_walk_params);
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Time-walk correction parameters for right PMTs
		for(int i = 0; i < timewalkR.getRowCount(); i++) {
			// Get sector, layer, component
			int sector    = Integer.parseInt((String)timewalkR.getValueAt(i, 0));
       	    int layer     = Integer.parseInt((String)timewalkR.getValueAt(i, 1));
       	    int component = Integer.parseInt((String)timewalkR.getValueAt(i, 2));
       	    // Get parameters
			double parA = timewalkR.getDoubleValue("par_a", sector, layer, component); 
			double parB = timewalkR.getDoubleValue("par_b", sector, layer, component); 
			// Get errors
			double errA = timewalkR.getDoubleValue("err_a", sector, layer, component); 
			double errB = timewalkR.getDoubleValue("err_b", sector, layer, component);
			// Put in the maps
			int key = sector*100+layer*10+component;
			double time_walk_params[] = {parA,parB,errA,errB};
			TIMEWALK_R.put(Integer.valueOf(key), time_walk_params);
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Speed of lights
		for(int i = 0; i < effvel.getRowCount(); i++) {
			// Get sector, layer, component
			int sector 	   = Integer.parseInt((String)effvel.getValueAt(i, 0));    
			int layer 	   = Integer.parseInt((String)effvel.getValueAt(i, 1));    
			int component  = Integer.parseInt((String)effvel.getValueAt(i, 2));    
			// Get the velocities
			double veff_tdc		= effvel.getDoubleValue("veff_tdc",  sector, layer, component);
			double veff_fadc	= effvel.getDoubleValue("veff_fadc", sector, layer, component);
			// Put in the maps
			int key = sector*100+layer*10+component;
			TDC_VEFF.put(	Integer.valueOf(key),		Double.valueOf(veff_tdc) );
			FADC_VEFF.put(	Integer.valueOf(key), 		Double.valueOf(veff_fadc) );
			
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// TDC time jitter
		for( int i = 0; i < timejitter.getRowCount(); i++){
			JITTER_PERIOD = timejitter.getDoubleValue("period", 0,0,0);
			JITTER_PHASE  = timejitter.getIntValue("phase", 0,0,0);
			JITTER_CYCLES = timejitter.getIntValue("cycles", 0,0,0);	
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Paddle-to-paddle offsets FADC
		for(int i =0; i < paddleoffs_fadc.getRowCount(); i++) {
			// Get sector, layer, component
			int sector 	   = Integer.parseInt((String)paddleoffs_fadc.getValueAt(i, 0));    
			int layer 	   = Integer.parseInt((String)paddleoffs_fadc.getValueAt(i, 1));    
			int component  = Integer.parseInt((String)paddleoffs_fadc.getValueAt(i, 2)); 
			// Get offset and resolution for FADC
			double p2p_off_fadc	= paddleoffs_fadc.getDoubleValue("offset_fadc",  sector, layer, component);
			double p2p_res_fadc	= paddleoffs_fadc.getDoubleValue("resolution_fadc",  sector, layer, component);
			// Put in maps 
			int key = sector*100+layer*10+component;
			FADC_MT_P2P_OFFSET.put(	Integer.valueOf(key),		Double.valueOf(p2p_off_fadc) );
			FADC_MT_P2P_RES.put(	Integer.valueOf(key),		Double.valueOf(p2p_res_fadc) );

		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Layer-to-layer offsets FADC
		for(int i =0; i < layeroffs_fadc.getRowCount(); i++) {
			// Get sector, layer, component
			int sector 	   = Integer.parseInt((String)layeroffs_fadc.getValueAt(i, 0));    
			int layer 	   = Integer.parseInt((String)layeroffs_fadc.getValueAt(i, 1));    
			int component  = Integer.parseInt((String)layeroffs_fadc.getValueAt(i, 2)); 
			// Get offset and resolution for FADC
			double l2l_off_fadc	= layeroffs_fadc.getDoubleValue("offset_fadc",  sector, layer, component);
			double l2l_res_fadc	= layeroffs_fadc.getDoubleValue("resolution_fadc",  sector, layer, component);
			// Put in maps
			int key = sector*100+layer*10+component;
			FADC_MT_L2L_OFFSET.put(	Integer.valueOf(key),		Double.valueOf(l2l_off_fadc) );
			FADC_MT_L2L_RES.put(	Integer.valueOf(key),		Double.valueOf(l2l_res_fadc) );
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Paddle-to-paddle offsets
		for(int i =0; i < paddleoffs_tdc.getRowCount(); i++) {
			// Get sector, layer, component
			int sector 	   = Integer.parseInt((String)paddleoffs_tdc.getValueAt(i, 0));    
			int layer 	   = Integer.parseInt((String)paddleoffs_tdc.getValueAt(i, 1));    
			int component  = Integer.parseInt((String)paddleoffs_tdc.getValueAt(i, 2)); 
			// Get offset and resolution for FADC
			double p2p_off_tdc	= paddleoffs_tdc.getDoubleValue("offset_tdc",  sector, layer, component);
			double p2p_res_tdc	= paddleoffs_tdc.getDoubleValue("resolution_tdc",  sector, layer, component);
			// Put in maps 
			int key = sector*100+layer*10+component;
			TDC_MT_P2P_OFFSET.put(	Integer.valueOf(key),		Double.valueOf(p2p_off_tdc) );
			TDC_MT_P2P_RES.put(	 	Integer.valueOf(key),		Double.valueOf(p2p_res_tdc) );

		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Layer-to-layer offsets
		for(int i =0; i < layeroffs_tdc.getRowCount(); i++) {
			// Get sector, layer, component
			int sector 	   = Integer.parseInt((String)layeroffs_tdc.getValueAt(i, 0));    
			int layer 	   = Integer.parseInt((String)layeroffs_tdc.getValueAt(i, 1));    
			int component  = Integer.parseInt((String)layeroffs_tdc.getValueAt(i, 2)); 
			// Get offset and resolution for FADC
			double l2l_off_tdc	= layeroffs_tdc.getDoubleValue("offset_tdc",  sector, layer, component);
			double l2l_res_tdc	= layeroffs_tdc.getDoubleValue("resolution_tdc",  sector, layer, component);
			// Put in maps
			int key = sector*100+layer*10+component;
			TDC_MT_L2L_OFFSET.put(	Integer.valueOf(key),		Double.valueOf(l2l_off_tdc) );
			TDC_MT_L2L_RES.put(	Integer.valueOf(key),			Double.valueOf(l2l_res_tdc) );
}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Attenuation constants
		for( int i=0; i < attenuation.getRowCount(); i++){
			// Get sector, layer, component
			int sector 	   = Integer.parseInt((String)attenuation.getValueAt(i, 0));    
			int layer 	   = Integer.parseInt((String)attenuation.getValueAt(i, 1));    
			int component  = Integer.parseInt((String)attenuation.getValueAt(i, 2)); 
			// Grab attenuation length
			double atten		= attenuation.getDoubleValue("atten_len",  sector, layer, component);
			double atten_err	= attenuation.getDoubleValue("atten_len_err",  sector, layer, component);
			// Put in map
			int key = sector*100+layer*10+component;
			FADC_ATTEN_LENGTH.put(	Integer.valueOf(key),		Double.valueOf(atten) 	);
		}

		CSTLOADED = true;
		//System.out.println("SUCCESSFULLY LOADED band CALIBRATION CONSTANTS....");

		

	} 


	private static DatabaseConstantProvider DB;

	public static final DatabaseConstantProvider getDB() {
		return DB;
	}


	public static final void setDB(DatabaseConstantProvider dB) {
		DB = dB;
	}

	public static void main (String arg[]) {
	//	CalibrationConstantsLoader.Load(10,"default");
	}
}
