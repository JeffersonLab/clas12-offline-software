package org.jlab.rec.ctof;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;

/**
 *
 * @author ziegler
 *
 */
public class CCDBConstantsLoader {

    public CCDBConstantsLoader() {
        // TODO Auto-generated constructor stub
    }

    public static boolean CSTLOADED = false;

    // Calibration parameters from DB
    //public static boolean areCalibConstantsLoaded = false;
    static DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(
            10, "default");

    public static final synchronized void Load(int runNb) {
        dbprovider = new DatabaseConstantProvider(runNb, "default"); // reset
        // using
        // the
        // new
        // run
        // Instantiating the constants arrays
        double[][][] YOFF = new double[1][1][48];
        double[][][] LAMBDAU = new double[1][1][48];
        double[][][] LAMBDAD = new double[1][1][48];
        double[][][] LAMBDAUU = new double[1][1][48];
        double[][][] LAMBDADU = new double[1][1][48];
        double[][][] EFFVELU = new double[1][1][48];
        double[][][] EFFVELD = new double[1][1][48];
        double[][][] EFFVELUU = new double[1][1][48];
        double[][][] EFFVELDU = new double[1][1][48];
        double[][][] TW0U = new double[1][1][48];
        double[][][] TW1U = new double[1][1][48];
        double[][][] TW2U = new double[1][1][48];
        double[][][] TW0D = new double[1][1][48];
        double[][][] TW1D = new double[1][1][48];
        double[][][] TW2D = new double[1][1][48];
        double[][][] UD = new double[1][1][48];
        double[][][] PADDLE2PADDLE = new double[1][1][48];
        int[][][] STATUSU = new int[1][1][48];
        int[][][] STATUSD = new int[1][1][48];

        // load table reads entire table and makes an array of variables for
        // each column in the table.
        dbprovider.loadTable("/calibration/ctof/attenuation");
        dbprovider.loadTable("/calibration/ctof/effective_velocity");
        dbprovider.loadTable("/calibration/ctof/timing_offset");
        // dbprovider.loadTable("/calibration/ctof/time_walk");
        dbprovider.loadTable("/calibration/ctof/status");
        // disconncect from database. Important to do this after loading tables.
        dbprovider.disconnect();

        dbprovider.show();

        // Getting the Timing Constants
        // 1) Time-walk - at present there is no table for time_walk in the DB
        // so these parameters will all be 0
        /*
		 * for(int i =0; i<
		 * dbprovider.length("/calibration/ctof/time_walk/tw0_upstreamt"); i++)
		 * {
		 * 
		 * int iSec =
		 * dbprovider.getInteger("/calibration/ctof/time_walk/sector", i); int
		 * iPan = dbprovider.getInteger("/calibration/ctof/time_walk/layer", i);
		 * int iPad =
		 * dbprovider.getInteger("/calibration/ctof/time_walk/component", i);
		 * double iTW0L =
		 * dbprovider.getDouble("/calibration/ctof/time_walk/tw0_upstream", i);
		 * double iTW1L =
		 * dbprovider.getDouble("/calibration/ctof/time_walk/tw1_upstreamt", i);
		 * double iTW2L =
		 * dbprovider.getDouble("/calibration/ctof/time_walk/tw2_upstream", i);
		 * double iTW0R =
		 * dbprovider.getDouble("/calibration/ctof/time_walk/tw0_downstreamt",
		 * i); double iTW1R =
		 * dbprovider.getDouble("/calibration/ctof/time_walk/tw1_downstream",
		 * i); double iTW2R =
		 * dbprovider.getDouble("/calibration/ctof/time_walk/tw2_downstream",
		 * i);
		 * 
		 * TW0U[iSec-1][iPan-1][iPad-1] = iTW0L; TW1U[iSec-1][iPan-1][iPad-1] =
		 * iTW1L; TW2U[iSec-1][iPan-1][iPad-1] = iTW2L;
		 * TW0D[iSec-1][iPan-1][iPad-1] = iTW0R; TW1D[iSec-1][iPan-1][iPad-1] =
		 * iTW1R; TW2D[iSec-1][iPan-1][iPad-1] = iTW2R;
		 * 
		 * }
         */
        // 2) Offsets : TIME_OFFSET = TDCU-TDCD - lupstream_downstream
        for (int i = 0; i < dbprovider
                .length("/calibration/ctof/timing_offset/sector"); i++) {

            int iSec = dbprovider.getInteger(
                    "/calibration/ctof/timing_offset/sector", i);
            int iPan = dbprovider.getInteger(
                    "/calibration/ctof/timing_offset/layer", i);
            int iPad = dbprovider.getInteger(
                    "/calibration/ctof/timing_offset/component", i);
            double iUD = dbprovider.getDouble(
                    "/calibration/ctof/timing_offset/upstream_downstream", i);
            double iPaddle2Paddle = dbprovider.getDouble(
                    "/calibration/ctof/timing_offset/paddle2paddle", i);

            UD[iSec - 1][iPan - 1][iPad - 1] = iUD;
            PADDLE2PADDLE[iSec - 1][iPan - 1][iPad - 1] = iPaddle2Paddle;
        }

        // Getting the effective velocities constants
        for (int i = 0; i < dbprovider
                .length("/calibration/ctof/effective_velocity/veff_upstream"); i++) {

            int iSec = dbprovider.getInteger(
                    "/calibration/ctof/effective_velocity/sector", i);
            int iPan = dbprovider.getInteger(
                    "/calibration/ctof/effective_velocity/layer", i);
            int iPad = dbprovider.getInteger(
                    "/calibration/ctof/effective_velocity/component", i);
            double iEffVelU = dbprovider.getDouble(
                    "/calibration/ctof/effective_velocity/veff_upstream", i);
            double iEffVelD = dbprovider.getDouble(
                    "/calibration/ctof/effective_velocity/veff_downstream", i);
            double iEffVelUU = dbprovider
                    .getDouble(
                            "/calibration/ctof/effective_velocity/veff_upstream_err",
                            i);
            double iEffVelDU = dbprovider.getDouble(
                    "/calibration/ctof/effective_velocity/veff_downstream_err",
                    i);

            EFFVELU[iSec - 1][iPan - 1][iPad - 1] = iEffVelU;
            EFFVELD[iSec - 1][iPan - 1][iPad - 1] = iEffVelD;
            EFFVELUU[iSec - 1][iPan - 1][iPad - 1] = iEffVelUU;
            EFFVELDU[iSec - 1][iPan - 1][iPad - 1] = iEffVelDU;
        }

        // Getting the attenuation length
        for (int i = 0; i < dbprovider
                .length("/calibration/ctof/attenuation/y_offset"); i++) {
            int iSec = dbprovider.getInteger(
                    "/calibration/ctof/attenuation/sector", i);
            int iPan = dbprovider.getInteger(
                    "/calibration/ctof/attenuation/layer", i);
            int iPad = dbprovider.getInteger(
                    "/calibration/ctof/attenuation/component", i);
            double yoff = dbprovider.getDouble(
                    "/calibration/ctof/attenuation/y_offset", i);
            double atlD = dbprovider.getDouble(
                    "/calibration/ctof/attenuation/attlen_downstream", i);
            double atlU = dbprovider.getDouble(
                    "/calibration/ctof/attenuation/attlen_upstream", i);
            double atlDU = dbprovider.getDouble(
                    "/calibration/ctof/attenuation/attlen_downstream_err", i);
            double atlUU = dbprovider.getDouble(
                    "/calibration/ctof/attenuation/attlen_upstream_err", i);

            YOFF[iSec - 1][iPan - 1][iPad - 1] = yoff;
            LAMBDAU[iSec - 1][iPan - 1][iPad - 1] = atlU;
            LAMBDAD[iSec - 1][iPan - 1][iPad - 1] = atlD;
            LAMBDAUU[iSec - 1][iPan - 1][iPad - 1] = atlUU;
            LAMBDADU[iSec - 1][iPan - 1][iPad - 1] = atlDU;

        }

        // Getting the status
        for (int i = 0; i < dbprovider
                .length("/calibration/ctof/status/sector"); i++) {
            int iSec = dbprovider.getInteger("/calibration/ctof/status/sector",
                    i);
            int iPan = dbprovider.getInteger("/calibration/ctof/status/layer",
                    i);
            int iPad = dbprovider.getInteger(
                    "/calibration/ctof/status/component", i);
            int statU = dbprovider.getInteger(
                    "/calibration/ctof/status/stat_upstream", i);
            int statD = dbprovider.getInteger(
                    "/calibration/ctof/status/stat_downstream", i);

            STATUSU[iSec - 1][iPan - 1][iPad - 1] = statU;
            STATUSD[iSec - 1][iPan - 1][iPad - 1] = statD;

        }

        CCDBConstants.setYOFF(YOFF);
        CCDBConstants.setLAMBDAU(LAMBDAU);
        CCDBConstants.setLAMBDAD(LAMBDAD);
        CCDBConstants.setLAMBDAUU(LAMBDAUU);
        CCDBConstants.setLAMBDADU(LAMBDADU);
        CCDBConstants.setEFFVELU(EFFVELU);
        CCDBConstants.setEFFVELD(EFFVELD);
        CCDBConstants.setEFFVELUU(EFFVELUU);
        CCDBConstants.setEFFVELDU(EFFVELDU);
        //CCDBConstants.setTW0U(TW0U) ;
        //CCDBConstants.setTW1U(TW1U) ;
        //CCDBConstants.setTW2U(TW2U) ;
        //CCDBConstants.setTW0D(TW0D) ;
        //CCDBConstants.setTW1D(TW1D) ;
        //CCDBConstants.setTW2D(TW2D) ;
        CCDBConstants.setUD(UD);
        CCDBConstants.setPADDLE2PADDLE(PADDLE2PADDLE);
        CCDBConstants.setSTATUSU(STATUSU);
        CCDBConstants.setSTATUSD(STATUSD);

        CSTLOADED = true;
        setDB(dbprovider);
    }

    private static DatabaseConstantProvider DB;

    public static final DatabaseConstantProvider getDB() {
        return DB;
    }

    public static final void setDB(DatabaseConstantProvider dB) {
        DB = dB;
    }

    public static void main(String arg[]) {
        CCDBConstantsLoader.Load(10);
    }
}
