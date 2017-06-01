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
    /*	public static final int[] cableid = 
	    {0,0,1,7, 13,19,25,31,37,0,0,0,0,43,49,55,61,67,73,79,
  		 0,0,2,8, 14,20,26,32,38,0,0,0,0,44,50,56,62,68,74,80,
		 0,0,3,9, 15,21,27,33,39,0,0,0,0,45,51,57,63,69,75,81,
		 0,0,4,10,16,22,28,34,40,0,0,0,0,46,52,58,64,70,76,82,
		 0,0,5,11,17,23,29,35,41,0,0,0,0,47,53,59,65,71,77,83,
		 0,0,6,12,18,24,30,36,42,0,0,0,0,48,54,60,66,72,78,84};*/
 /*
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
	//T0s
	public static final double[][][][] T0			= new double[6][6][7][6]; //nSec*nSL*nSlots*nCables
	public static final double[][][][] T0Err		= new double[6][6][7][6]; //nSec*nSL*nSlots*nCables
     */
    //Map of Cable ID (1, .., 6) in terms of Layer number (1, ..., 6) and localWire# (1, ..., 16)
    public static final int[][] CableID = { //[nLayer][nLocWire] => nLocWire=16, 7 groups of 16 wires in each layer
        {1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6}, //Layer 1
        {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6}, //Layer 2
        {1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6}, //Layer 3
        {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6}, //Layer 4
        {1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6}, //Layer 5
        {1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6}, //Layer 6  
    //===> 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 (Local wire ID: 0 for 1st, 16th, 32th, 48th, 64th, 80th, 96th wires)
    };

    //Calibration parameters from DB    
    static DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(10, "default");
    static DatabaseConstantProvider dbprovider_Test = new DatabaseConstantProvider(10, "dc_test1");

    public static final synchronized void Load(int runNb, String var) {
        // T2D
        double[][] DELTANM = new double[6][6];
        double[][] V0 = new double[6][6];					    // staturated drift velocity in cm/ns
        double[][] DELT_BFIELD_COEFFICIENT = new double[6][6]; //coefficient of the bfield part of the increase in time

        double[][] TMAXSUPERLAYER = new double[6][6];
        double[] DMAXSUPERLAYER = new double[6];

        double DELTATIME_BFIELD_PAR1[][] = new double[6][6];
        double DELTATIME_BFIELD_PAR2[][] = new double[6][6];
        double DELTATIME_BFIELD_PAR3[][] = new double[6][6];
        double DELTATIME_BFIELD_PAR4[][] = new double[6][6];

        double DISTBETA[][] = new double[6][6];

        //RMS
        // Instantiating the constants arrays
        double[][] PAR1 = new double[6][6];
        double[][] PAR2 = new double[6][6];
        double[][] PAR3 = new double[6][6];
        double[][] PAR4 = new double[6][6];
        double[][] SCAL = new double[6][6];

        //T0s
        double[][][][] T0 = new double[6][6][7][6]; //nSec*nSL*nSlots*nCables
        double[][][][] T0ERR = new double[6][6][7][6]; //nSec*nSL*nSlots*nCables

        if (runNb != 10 || !var.equalsIgnoreCase("default")) {
            dbprovider = new DatabaseConstantProvider(runNb, var); // reset using the new variation
        }	    // load table reads entire table and makes an array of variables for each column in the table.
        dbprovider.loadTable("/calibration/dc/signal_generation/dc_resolution");
        dbprovider.loadTable("/calibration/dc/time_to_distance/tvsx_devel_v2");
        dbprovider.loadTable("/calibration/dc/time_corrections/T0Corrections");
        dbprovider.loadTable("/geometry/dc/superlayer");
        //disconncect from database. Important to do this after loading tables.
        dbprovider.disconnect();

        //dbprovider.show();
        // Getting DMAX
        for (int i = 0; i < dbprovider.length("/geometry/dc/superlayer/superlayer"); i++) {
            int iSly = dbprovider.getInteger("/geometry/dc/superlayer/superlayer", i);

            DMAXSUPERLAYER[iSly - 1] = 2 * dbprovider.getDouble("/geometry/dc/superlayer/wpdist", i);
        }
        // Getting the  Constants
        // 1) Time RMS
        for (int i = 0; i < dbprovider.length("/calibration/dc/signal_generation/dc_resolution/Sector"); i++) {

            int iSec = dbprovider.getInteger("/calibration/dc/signal_generation/dc_resolution/Sector", i);
            int iSly = dbprovider.getInteger("/calibration/dc/signal_generation/dc_resolution/Superlayer", i);
            double iPAR1 = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/parameter1", i);
            double iPAR2 = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/parameter2", i);
            double iPAR3 = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/parameter3", i);
            double iPAR4 = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/parameter4", i);
            double iSCAL = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/scale", i);

            PAR1[iSec - 1][iSly - 1] = iPAR1;
            PAR2[iSec - 1][iSly - 1] = iPAR2;
            PAR3[iSec - 1][iSly - 1] = iPAR3;
            PAR4[iSec - 1][iSly - 1] = iPAR4;
            SCAL[iSec - 1][iSly - 1] = iSCAL;

            System.out.println(" iSec " + iSec + " iSly " + iSly + " iPAR1 " + iPAR1 + " iPAR2 " + iPAR2 + " iPAR3 " + iPAR3 + " iPAR4 " + iPAR4 + " iSCAL " + iSCAL);

        }

        // 2) T2D
        for (int i = 0; i < dbprovider.length("/calibration/dc/time_to_distance/tvsx_devel_v2/Sector"); i++) {

            int iSec = dbprovider.getInteger("/calibration/dc/time_to_distance/tvsx_devel_v2/Sector", i);
            int iSly = dbprovider.getInteger("/calibration/dc/time_to_distance/tvsx_devel_v2/Superlayer", i);
            double iv0 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/v0", i);
            double ideltanm = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/deltanm", i);
            double itmax = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/tmax", i);
            double idelta_bfield_coefficient = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/delta_bfield_coefficient", i);
            double ib1 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b1", i);
            double ib2 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b2", i);
            double ib3 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b3", i);
            double ib4 = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b4", i);
            double idistbeta = dbprovider.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/distbeta", i);

            DELTANM[iSec - 1][iSly - 1] = ideltanm;
            V0[iSec - 1][iSly - 1] = iv0;
            DELT_BFIELD_COEFFICIENT[iSec - 1][iSly - 1] = idelta_bfield_coefficient;

            TMAXSUPERLAYER[iSec - 1][iSly - 1] = itmax;

            DELTATIME_BFIELD_PAR1[iSec - 1][iSly - 1] = ib1;
            DELTATIME_BFIELD_PAR2[iSec - 1][iSly - 1] = ib2;
            DELTATIME_BFIELD_PAR3[iSec - 1][iSly - 1] = ib3;
            DELTATIME_BFIELD_PAR4[iSec - 1][iSly - 1] = ib4;

            DISTBETA[iSec - 1][iSly - 1] = idistbeta;

            //System.out.println(" T2D Constants :  deltanm "+deltanm[iSec-1][iSly-1] +"  v0 "+v0[iSec-1][iSly-1]+" delt_bfield_coefficient " +delt_bfield_coefficient[iSec-1][iSly-1]+
            //"  b1 "+deltatime_bfield_par1[iSec-1][iSly-1]+" b2 "+deltatime_bfield_par2[iSec-1][iSly-1]+" b3 "+deltatime_bfield_par3[iSec-1][iSly-1]+" b4 "+deltatime_bfield_par4[iSec-1][iSly-1]);
        }
        // T0-subtraction
        for (int i = 0; i < dbprovider.length("/calibration/dc/time_corrections/T0Corrections/Sector"); i++) {

            int iSec = dbprovider.getInteger("/calibration/dc/time_corrections/T0Corrections/Sector", i);
            int iSly = dbprovider.getInteger("/calibration/dc/time_corrections/T0Corrections/Superlayer", i);
            int iSlot = dbprovider.getInteger("/calibration/dc/time_corrections/T0Corrections/Slot", i);
            int iCab = dbprovider.getInteger("/calibration/dc/time_corrections/T0Corrections/Cable", i);
            double t0 = dbprovider.getDouble("/calibration/dc/time_corrections/T0Corrections/T0Correction", i);
            double t0Error = dbprovider.getDouble("/calibration/dc/time_corrections/T0Corrections/T0Error", i);

            T0[iSec - 1][iSly - 1][iSlot - 1][iCab - 1] = t0;
            T0ERR[iSec - 1][iSly - 1][iSlot - 1][iCab - 1] = t0Error;
        }
        CCDBConstants.setDELTANM(DELTANM);
        CCDBConstants.setV0(V0);
        CCDBConstants.setDELT_BFIELD_COEFFICIENT(DELT_BFIELD_COEFFICIENT);
        CCDBConstants.setTMAXSUPERLAYER(TMAXSUPERLAYER);
        CCDBConstants.setDMAXSUPERLAYER(DMAXSUPERLAYER);
        CCDBConstants.setDELTATIME_BFIELD_PAR1(DELTATIME_BFIELD_PAR1);
        CCDBConstants.setDELTATIME_BFIELD_PAR2(DELTATIME_BFIELD_PAR2);
        CCDBConstants.setDELTATIME_BFIELD_PAR3(DELTATIME_BFIELD_PAR3);
        CCDBConstants.setDELTATIME_BFIELD_PAR4(DELTATIME_BFIELD_PAR4);
        CCDBConstants.setDISTBETA(DISTBETA);
        CCDBConstants.setPAR1(PAR1);
        CCDBConstants.setPAR2(PAR2);
        CCDBConstants.setPAR3(PAR3);
        CCDBConstants.setPAR4(PAR4);
        CCDBConstants.setSCAL(SCAL);
        CCDBConstants.setT0(T0);
        CCDBConstants.setT0ERR(T0ERR);
    }

    public static final synchronized void LoadDevel(int runNb, String var, String var2) {

        double[][] DELTANM = new double[6][6];
        double[][] V0 = new double[6][6];					    // staturated drift velocity in cm/ns
        double[][] DELT_BFIELD_COEFFICIENT = new double[6][6]; //coefficient of the bfield part of the increase in time

        double[][] TMAXSUPERLAYER = new double[6][6];
        double[] DMAXSUPERLAYER = new double[6];

        double DELTATIME_BFIELD_PAR1[][] = new double[6][6];
        double DELTATIME_BFIELD_PAR2[][] = new double[6][6];
        double DELTATIME_BFIELD_PAR3[][] = new double[6][6];
        double DELTATIME_BFIELD_PAR4[][] = new double[6][6];

        double DISTBETA[][] = new double[6][6];

        //RMS
        // Instantiating the constants arrays
        double[][] PAR1 = new double[6][6];
        double[][] PAR2 = new double[6][6];
        double[][] PAR3 = new double[6][6];
        double[][] PAR4 = new double[6][6];
        double[][] SCAL = new double[6][6];

        //T0s
        double[][][][] T0 = new double[6][6][7][6]; //nSec*nSL*nSlots*nCables
        double[][][][] T0ERR = new double[6][6][7][6]; //nSec*nSL*nSlots*nCables

        dbprovider = new DatabaseConstantProvider(runNb, var); // reset using the new variation
        dbprovider_Test = new DatabaseConstantProvider(runNb, var2); // reset using the new variation
        // load table reads entire table and makes an array of variables for each column in the table.
        dbprovider.loadTable("/calibration/dc/signal_generation/dc_resolution");
        dbprovider_Test.loadTable("/calibration/dc/time_to_distance/tvsx_devel_v2");
        dbprovider_Test.loadTable("/calibration/dc/time_corrections/T0Corrections");
        dbprovider.loadTable("/geometry/dc/superlayer");
        //disconncect from database. Important to do this after loading tables.
        dbprovider.disconnect();

        //dbprovider.show();
        // Getting DMAX
        for (int i = 0; i < dbprovider.length("/geometry/dc/superlayer/superlayer"); i++) {
            int iSly = dbprovider.getInteger("/geometry/dc/superlayer/superlayer", i);

            DMAXSUPERLAYER[iSly - 1] = 2 * dbprovider.getDouble("/geometry/dc/superlayer/wpdist", i);
        }
        // Getting the  Constants
        // 1) Time RMS
        for (int i = 0; i < dbprovider.length("/calibration/dc/signal_generation/dc_resolution/Sector"); i++) {

            int iSec = dbprovider.getInteger("/calibration/dc/signal_generation/dc_resolution/Sector", i);
            int iSly = dbprovider.getInteger("/calibration/dc/signal_generation/dc_resolution/Superlayer", i);
            double iPAR1 = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/parameter1", i);
            double iPAR2 = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/parameter2", i);
            double iPAR3 = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/parameter3", i);
            double iPAR4 = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/parameter4", i);
            double iSCAL = dbprovider.getDouble("/calibration/dc/signal_generation/dc_resolution/scale", i);

            PAR1[iSec - 1][iSly - 1] = iPAR1;
            PAR2[iSec - 1][iSly - 1] = iPAR2;
            PAR3[iSec - 1][iSly - 1] = iPAR3;
            PAR4[iSec - 1][iSly - 1] = iPAR4;
            SCAL[iSec - 1][iSly - 1] = iSCAL;

            System.out.println(" iSec " + iSec + " iSly " + iSly + " iPAR1 " + iPAR1 + " iPAR2 " + iPAR2 + " iPAR3 " + iPAR3 + " iPAR4 " + iPAR4 + " iSCAL " + iSCAL);

        }

        // 2) T2D
        for (int i = 0; i < dbprovider_Test.length("/calibration/dc/time_to_distance/tvsx_devel_v2/Sector"); i++) {

            int iSec = dbprovider_Test.getInteger("/calibration/dc/time_to_distance/tvsx_devel_v2/Sector", i);
            int iSly = dbprovider_Test.getInteger("/calibration/dc/time_to_distance/tvsx_devel_v2/Superlayer", i);
            double iv0 = dbprovider_Test.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/v0", i);
            double ideltanm = dbprovider_Test.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/deltanm", i);
            double itmax = dbprovider_Test.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/tmax", i);
            double idelta_bfield_coefficient = dbprovider_Test.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/delta_bfield_coefficient", i);
            double ib1 = dbprovider_Test.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b1", i);
            double ib2 = dbprovider_Test.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b2", i);
            double ib3 = dbprovider_Test.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b3", i);
            double ib4 = dbprovider_Test.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/b4", i);
            double idistbeta = dbprovider_Test.getDouble("/calibration/dc/time_to_distance/tvsx_devel_v2/distbeta", i);

            DELTANM[iSec - 1][iSly - 1] = ideltanm;
            V0[iSec - 1][iSly - 1] = iv0;
            DELT_BFIELD_COEFFICIENT[iSec - 1][iSly - 1] = idelta_bfield_coefficient;

            TMAXSUPERLAYER[iSec - 1][iSly - 1] = itmax;

            DELTATIME_BFIELD_PAR1[iSec - 1][iSly - 1] = ib1;
            DELTATIME_BFIELD_PAR2[iSec - 1][iSly - 1] = ib2;
            DELTATIME_BFIELD_PAR3[iSec - 1][iSly - 1] = ib3;
            DELTATIME_BFIELD_PAR4[iSec - 1][iSly - 1] = ib4;

            DISTBETA[iSec - 1][iSly - 1] = idistbeta;

            System.out.println(" T2D Constants :  "+ " deltanm " + DELTANM[iSec - 1][iSly - 1] + "  v0 " + V0[iSec - 1][iSly - 1] + " delt_bfield_coefficient " + DELT_BFIELD_COEFFICIENT[iSec - 1][iSly - 1]
                    + "  b1 " + DELTATIME_BFIELD_PAR1[iSec - 1][iSly - 1] + " b2 " + DELTATIME_BFIELD_PAR2[iSec - 1][iSly - 1] + " b3 " + DELTATIME_BFIELD_PAR3[iSec - 1][iSly - 1] + " b4 " + DELTATIME_BFIELD_PAR4[iSec - 1][iSly - 1]);
        }
        // T0-subtraction
        for (int i = 0; i < dbprovider_Test.length("/calibration/dc/time_corrections/T0Corrections/Sector"); i++) {

            int iSec = dbprovider_Test.getInteger("/calibration/dc/time_corrections/T0Corrections/Sector", i);
            int iSly = dbprovider_Test.getInteger("/calibration/dc/time_corrections/T0Corrections/Superlayer", i);
            int iSlot = dbprovider_Test.getInteger("/calibration/dc/time_corrections/T0Corrections/Slot", i);
            int iCab = dbprovider_Test.getInteger("/calibration/dc/time_corrections/T0Corrections/Cable", i);
            double t0 = dbprovider_Test.getDouble("/calibration/dc/time_corrections/T0Corrections/T0Correction", i);
            double t0Error = dbprovider_Test.getDouble("/calibration/dc/time_corrections/T0Corrections/T0Error", i);

            T0[iSec - 1][iSly - 1][iSlot - 1][iCab - 1] = t0;
            T0ERR[iSec - 1][iSly - 1][iSlot - 1][iCab - 1] = t0Error;
            System.out.println(" DC CALIBRATION CONSTANTS TO; Cable id = "+iCab+" T0 = "+t0);
        }
        CCDBConstants.setDELTANM(DELTANM);
        CCDBConstants.setV0(V0);
        CCDBConstants.setDELT_BFIELD_COEFFICIENT(DELT_BFIELD_COEFFICIENT);
        CCDBConstants.setTMAXSUPERLAYER(TMAXSUPERLAYER);
        CCDBConstants.setDMAXSUPERLAYER(DMAXSUPERLAYER);
        CCDBConstants.setDELTATIME_BFIELD_PAR1(DELTATIME_BFIELD_PAR1);
        CCDBConstants.setDELTATIME_BFIELD_PAR2(DELTATIME_BFIELD_PAR2);
        CCDBConstants.setDELTATIME_BFIELD_PAR3(DELTATIME_BFIELD_PAR3);
        CCDBConstants.setDELTATIME_BFIELD_PAR4(DELTATIME_BFIELD_PAR4);
        CCDBConstants.setDISTBETA(DISTBETA);
        CCDBConstants.setPAR1(PAR1);
        CCDBConstants.setPAR2(PAR2);
        CCDBConstants.setPAR3(PAR3);
        CCDBConstants.setPAR4(PAR4);
        CCDBConstants.setSCAL(SCAL);
        CCDBConstants.setT0(T0);
        CCDBConstants.setT0ERR(T0ERR);

    }

    public static final void main(String arg[]) {
        CalibrationConstantsLoader.LoadDevel(790, "default", "dc_test1");
    }
}
