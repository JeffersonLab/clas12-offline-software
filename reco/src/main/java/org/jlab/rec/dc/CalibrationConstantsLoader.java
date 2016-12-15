package org.jlab.rec.dc;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;

 /* 
 * @author ziegler
 *
 */
public class CalibrationConstantsLoader {

	public CalibrationConstantsLoader() {
		// TODO Auto-generated constructor stub
	}
	
	// T2D
	public static final double[][] deltanm = new double[6][6];
	public static final double[][] v0 = new double[6][6];					    // staturated drift velocity in cm/ns
	public static final double[][] delt_bfield_coefficient = new double[6][6]; //coefficient of the bfield part of the increase in time
	
	public static final double[] dmaxsuperlayer = {0.77665,0.81285,1.25065,1.32446,1.72947,1.80991};
	public static final double[][] tmaxsuperlayer = new double[6][6];

	public static final double deltatime_bfield_par1[][] = new double[6][6];
	public static final double deltatime_bfield_par2[][] = new double[6][6];
	public static final double deltatime_bfield_par3[][] = new double[6][6];
	public static final double deltatime_bfield_par4[][] = new double[6][6];
	
	public static final double distbeta[][] = new double[6][6];
	
	//RMS
	// Instantiating the constants arrays
	public static final double[][] PAR1 			= new double[6][6];
	public static final double[][] PAR2 			= new double[6][6];
	public static final double[][] PAR3 			= new double[6][6];
	public static final double[][] PAR4 			= new double[6][6];
	public static final double[][] SCAL 			= new double[6][6];
	
	public static final int[][][][] STATUS 			= new int[6][6][6][112];
	
	 //Calibration parameters from DB    
    static DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(10,"default");
    
    public static final synchronized void Load(int runNb, String var) {
    	if(runNb!=10 || !var.equalsIgnoreCase("default"))
			dbprovider = new DatabaseConstantProvider(runNb, var); // reset using the new variation
	    // load table reads entire table and makes an array of variables for each column in the table.
	    dbprovider.loadTable("/calibration/dc/signal_generation/dc_resolution");
	    dbprovider.loadTable("/calibration/dc/time_to_distance/tvsx_devel_v2");
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
	    
	 // 2) T2D
	    for(int i =0; i< dbprovider.length("/calibration/dc/time_to_distance/tvsx_devel_v2/Sector"); i++) {
	    	
	    	int iSec = dbprovider.getInteger("/calibration/dc/time_to_distance/tvsx_devel_v2/Sector", i);	    
	        int iSly = dbprovider.getInteger("/calibration/dc/time_to_distance/tvsx_devel_v2/Superlayer", i);
	        double iv0 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/v0", i);
	        double ideltanm = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/deltanm", i);
	        double itmax = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/tmax", i);
	        double idelta_bfield_coefficient = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v1/delta_bfield_coefficient", i);
	        double ib1 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b1", i);
	        double ib2 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b2", i);
	        double ib3 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b3", i);
	        double ib4 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b4", i);
	        double idistbeta = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/distbeta", i);
	        
	        deltanm[iSec-1][iSly-1] = ideltanm;
	    	v0[iSec-1][iSly-1] = iv0;					    
	    	delt_bfield_coefficient[iSec-1][iSly-1] = idelta_bfield_coefficient; 
	    	
	    	tmaxsuperlayer[iSec-1][iSly-1] = itmax;

	    	deltatime_bfield_par1[iSec-1][iSly-1] = ib1;
	    	deltatime_bfield_par2[iSec-1][iSly-1] = ib2;
	    	deltatime_bfield_par3[iSec-1][iSly-1] = ib3;
	    	deltatime_bfield_par4[iSec-1][iSly-1] = ib4;
	    	
	    	distbeta[iSec-1][iSly-1] = idistbeta;
	    	
	    	System.out.println(" T2D Constants :  deltanm "+deltanm[iSec-1][iSly-1] +"  v0 "+v0[iSec-1][iSly-1]+" delt_bfield_coefficient " +delt_bfield_coefficient[iSec-1][iSly-1]+
	    	"  b1 "+deltatime_bfield_par1[iSec-1][iSly-1]+" b2 "+deltatime_bfield_par2[iSec-1][iSly-1]+" b3 "+deltatime_bfield_par3[iSec-1][iSly-1]+" b4 "+deltatime_bfield_par4[iSec-1][iSly-1]);
	    }
	   
    }
   
    
    
    public static final void main (String arg[]) {
    	CalibrationConstantsLoader.Load(11, "default");
    }
}
