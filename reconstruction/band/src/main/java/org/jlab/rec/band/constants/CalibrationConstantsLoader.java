package org.jlab.rec.band.constants;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;

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

	// Instantiating the constants arrays
	//public static double[][][] FADC_T_OFFSET	= new double[24][3][2];
	//public static double[][][] VEFF_TDC			= new double[24][3][2];	
	//public static double[][] VEFF_FADC 			= new double[6][5];

	public static Map<Integer, Double> TDC_T_OFFSET = new HashMap<Integer, Double>();


	public static boolean arEnergyibConstantsLoaded = false;

	static DatabaseConstantProvider dbprovider = null;

	public static synchronized void Load(int runno, String var) {

		System.out.println("*Loading calibration constants*");

		dbprovider = new DatabaseConstantProvider(runno, var); // reset using the new variation

		// load table reads entire table and makes an array of variables for each column in the table.
		dbprovider.loadTable("/calibration/band/lr_offsets");
		dbprovider.loadTable("/calibration/band/effective_velocity");

		//disconncect from database. Important to do this after loading tables.
		dbprovider.disconnect(); 

		dbprovider.show();


		// Time offsets
		for(int i =0; i< dbprovider.length("/calibration/band/lr_offsets/sector"); i++) {
			// Get sector, layer, component
			int sector = dbprovider.getInteger("/calibration/band/lr_offsets/sector", i);	    
			int layer = dbprovider.getInteger("/calibration/band/lr_offsets/layer", i);
			int component = dbprovider.getInteger("/calibration/band/lr_offsets/component", i);
			// Get the actual offsets
			double tdc_off = dbprovider.getDouble("/calibration/band/lr_offsets/tdc_off", i);
			double fadc_off = dbprovider.getDouble("/calibration/band/lr_offsets/fadc_off", i);

			int key = sector*100+layer*10+component;
			TDC_T_OFFSET.put( Integer.valueOf(key) , Double.valueOf(tdc_off) );

			//System.out.println("time_offset_LR "+iTO);
		}

		for (Integer keys : TDC_T_OFFSET.keySet())  
		{
			System.out.println(keys + ":"+ TDC_T_OFFSET.get(keys));
		}

		// TDC time jitter
		//JITTER_PERIOD = dbprovider.getDouble("/calibration/band/time_jitter/period", 0);
		//JITTER_PHASE  = dbprovider.getInteger("/calibration/band/time_jitter/phase", 0);
		//JITTER_CYCLES = dbprovider.getInteger("/calibration/band/time_jitter/cycles", 0);




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
