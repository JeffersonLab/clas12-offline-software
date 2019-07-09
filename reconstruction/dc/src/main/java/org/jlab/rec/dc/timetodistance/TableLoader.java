package org.jlab.rec.dc.timetodistance;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.rec.dc.Constants;
import org.jlab.utils.groups.IndexedTable;


public class TableLoader {

    public TableLoader() {
            // TODO Auto-generated constructor stub
    }
    static final protected int nBinsT=2000;
    //public static double[][][][][] DISTFROMTIME = new double[6][6][6][6][850]; // sector slyr alpha Bfield time bins
    
    static boolean T2DLOADED = false;
    static boolean T0LOADED = false;
    
    public static double[] BfieldValues = new double[]{0.0000, 1.0000, 1.4142, 1.7321, 2.0000, 2.2361, 2.4495, 2.6458};
    static int minBinIdxB = 0;
    static int maxBinIdxB = BfieldValues.length-1;
    static int minBinIdxAlpha = 0;
    static int maxBinIdxAlpha = 5;
    public static double[] AlphaMid = new double[6];
    public static double[][] AlphaBounds = new double[6][2];
    static int minBinIdxT  = 0;
    static int[][][][] maxBinIdxT  = new int[6][6][8][6];
    public static double[][][][][] DISTFROMTIME = new double[6][6][maxBinIdxB+1][maxBinIdxAlpha+1][nBinsT]; // sector slyr alpha Bfield time bins [s][r][ibfield][icosalpha][tbin]
    
    //public static double[] distbetaValues = new double[]{0.16, 0.16, 0.08, 0.08, 0.08, 0.08};
    
    /*
     * 
     */
    public static void test(){
            TimeToDistanceEstimator tde = new TimeToDistanceEstimator();
            for(int s = 0; s<6; s++ ){ // loop over sectors
                    for(int r = 0; r<6; r++ ){ //loop over slys
                            for(int ibfield =0; ibfield<BfieldValues.length; ibfield++) {
                                    for(int icosalpha =0; icosalpha<maxBinIdxAlpha+1; icosalpha++) {
                                            //for (int tb = 0; tb< maxBinIdxT[s][r][ibfield][icosalpha]; tb++) {
                                                for (int tb = 0; tb< nBinsT; tb++) {
                                                    if(DISTFROMTIME[s][r][ibfield][icosalpha][tb]==0 && tb>1)
                                                        DISTFROMTIME[s][r][ibfield][icosalpha][tb]=DISTFROMTIME[s][r][ibfield][icosalpha][tb-1];
                                                    if(DISTFROMTIME[s][r][ibfield][icosalpha][tb]==0)
                                                        System.out.println("Bbin "+ibfield+" cos "+icosalpha+" tb "+tb);
                                                //double Xalpha = -(Math.toDegrees(Math.acos(Math.cos(Math.toRadians(30.)) + (icosalpha)*(1. - Math.cos(Math.toRadians(30.)))/5.)) - 30.);
                                                //double Xtime=(2*tb+1);
                                                
                                                //for (int k=0; k<10; k++){
                                                //double Bf = (ibfield+0.1*k)*0.5;
                                                //int bbin = tde.getBIdx(Bf);
                                                //double Xdoca=tde.interpolateOnGrid((double) Bf, Xalpha, Xtime, s, r);
                                                  //  System.out.println("Bbin "+ibfield+" B "+ (float)Bf+" sl "+(r+1)+" time "+Xtime+" tb "+tb+" timeBin "+tde.getTimeIdx(Xtime, s, r, ibfield, icosalpha)
                                                   //         +" icosalpha "+icosalpha+" Xalpha "+(float) Xalpha + " dis "+ (float)DISTFROMTIME[s][r][bbin][icosalpha][tde.getTimeIdx(Xtime, s, r, ibfield, icosalpha)] +" dis' "+
                                                    //      (float) Xdoca );
                                                //}
                                            }

                                    }
                            }
                    }
            }
    }
    
    
    
    public static synchronized void FillT0Tables(int run, String variation) {
        if (T0LOADED) return;
        System.out.println(" T0 TABLE FILLED..... for Run "+run+" with VARIATION "+variation);
        DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(run, variation);
        dbprovider.loadTable("/calibration/dc/time_corrections/T0Corrections");
        //disconnect from database. Important to do this after loading tables.
        dbprovider.disconnect();
        // T0-subtraction
        double[][][][] T0 ;
        double[][][][] T0ERR ;
        //T0s
        T0 = new double[6][6][7][6]; //nSec*nSL*nSlots*nCables
        T0ERR = new double[6][6][7][6]; //nSec*nSL*nSlots*nCables
        for (int i = 0; i < dbprovider.length("/calibration/dc/time_corrections/T0Corrections/Sector"); i++) {
            int iSec = dbprovider.getInteger("/calibration/dc/time_corrections/T0Corrections/Sector", i);
            int iSly = dbprovider.getInteger("/calibration/dc/time_corrections/T0Corrections/Superlayer", i);
            int iSlot = dbprovider.getInteger("/calibration/dc/time_corrections/T0Corrections/Slot", i);
            int iCab = dbprovider.getInteger("/calibration/dc/time_corrections/T0Corrections/Cable", i);
            double t0 = dbprovider.getDouble("/calibration/dc/time_corrections/T0Corrections/T0Correction", i);
            double t0Error = dbprovider.getDouble("/calibration/dc/time_corrections/T0Corrections/T0Error", i);

            T0[iSec - 1][iSly - 1][iSlot - 1][iCab - 1] = t0; 
            T0ERR[iSec - 1][iSly - 1][iSlot - 1][iCab - 1] = t0Error;
            Constants.setT0(T0);
            Constants.setT0Err(T0ERR);
            //System.out.println("T0 = "+t0);
        }
        T0LOADED = true;
    }
    public static int getAlphaBin(double Alpha) {
        int bin = 0;
        for(int b =0; b<6; b++) {
            if(Alpha>=AlphaBounds[b][0] && Alpha<=AlphaBounds[b][1] )
                bin = b;
        }
        return bin;
    }
    public static int maxTBin = -1;
    public static synchronized void FillAlpha() {
        for(int icosalpha =0; icosalpha<maxBinIdxAlpha+1; icosalpha++) {

            double cos30minusalphaM = Math.cos(Math.toRadians(30.)) + (double) 
                    (icosalpha)*(1. - Math.cos(Math.toRadians(30.)))/5.;
            double alphaM = -(Math.toDegrees(Math.acos(cos30minusalphaM)) - 30);
            AlphaMid[icosalpha]= alphaM;
            double cos30minusalphaU = Math.cos(Math.toRadians(30.)) + (double) 
                    (icosalpha+0.5)*(1. - Math.cos(Math.toRadians(30.)))/5.;
            double alphaU = -(Math.toDegrees(Math.acos(cos30minusalphaU)) - 30);
            AlphaBounds[icosalpha][1] = alphaU;
            double cos30minusalphaL = Math.cos(Math.toRadians(30.)) + (double) 
                    (icosalpha-0.5)*(1. - Math.cos(Math.toRadians(30.)))/5.;
            double alphaL = -(Math.toDegrees(Math.acos(cos30minusalphaL)) - 30);
            AlphaBounds[icosalpha][0] = alphaL;
        }
        AlphaMid[0] = 0;
        AlphaMid[5] = 30;
        AlphaBounds[0][0] = 0;
        AlphaBounds[5][1] = 30;
    }
    public static synchronized void Fill(IndexedTable tab) {
        //CCDBTables 0 =  "/calibration/dc/signal_generation/doca_resolution";
        //CCDBTables 1 =  "/calibration/dc/time_to_distance/t2d";
        //CCDBTables 2 =  "/calibration/dc/time_corrections/T0_correction";	
        if (T2DLOADED) return;
        System.out.println(" T2D TABLE FILLED.....");
        double stepSize = 0.0010;
        DecimalFormat df = new DecimalFormat("#");
        df.setRoundingMode(RoundingMode.CEILING);
        
        FillAlpha();
        for(int s = 0; s<6; s++ ){ // loop over sectors

                for(int r = 0; r<6; r++ ){ //loop over slys
                    // Fill constants
                    delta_T0[s][r] = tab.getDoubleValue("delta_T0", s+1,r+1,0);
                    FracDmaxAtMinVel[s][r] = tab.getDoubleValue("c1", s+1,r+1,0);//use same table. names strings 
                    deltanm[s][r] = tab.getDoubleValue("deltanm", s+1,r+1,0);
                    v0[s][r] = tab.getDoubleValue("v0", s+1,r+1,0);
                    vmid[s][r] = tab.getDoubleValue("c2", s+1,r+1,0);
                    delta_bfield_coefficient[s][r] = tab.getDoubleValue("delta_bfield_coefficient", s+1,r+1,0); 
                    b1[s][r] = tab.getDoubleValue("b1", s+1,r+1,0);
                    b2[s][r] = tab.getDoubleValue("b2", s+1,r+1,0);
                    b3[s][r] = tab.getDoubleValue("b3", s+1,r+1,0);
                    b4[s][r] = tab.getDoubleValue("b4", s+1,r+1,0);
                    Tmax[s][r] = tab.getDoubleValue("tmax", s+1,r+1,0);
                    // end fill constants
                    //System.out.println(v0[s][r]+" "+vmid[s][r]+" "+FracDmaxAtMinVel[s][r]);
                    double dmax = 2.*Constants.wpdist[r]; 
                    //double tmax = CCDBConstants.getTMAXSUPERLAYER()[s][r];
                    for(int ibfield =0; ibfield<maxBinIdxB+1; ibfield++) {
                        double bfield = BfieldValues[ibfield];

                        for(int icosalpha =0; icosalpha<maxBinIdxAlpha+1; icosalpha++) {

                                double cos30minusalpha = Math.cos(Math.toRadians(30.)) + (double) (icosalpha)*(1. - Math.cos(Math.toRadians(30.)))/5.;
                                double alpha = -(Math.toDegrees(Math.acos(cos30minusalpha)) - 30);
                                int nxmax = (int) (dmax*cos30minusalpha/stepSize); 

                                for(int idist =0; idist<nxmax; idist++) {

                                    double x = (double)(idist+1)*stepSize;
                                    double timebfield = calc_Time( x,  alpha, bfield, s+1, r+1) ;
                                    
                                    int tbin = Integer.parseInt(df.format(timebfield/2.) ) -1;
                                    
                                    if(tbin<0 || tbin>nBinsT-1) {
                                        //System.err.println("Problem with tbin");
                                        continue;
                                    }
                                    if(tbin>maxTBin)
                                        maxTBin = tbin;
                                    //if(tbin>maxBinIdxT[s][r][ibfield][icosalpha]) {
                                        //maxBinIdxT[s][r][ibfield][icosalpha] = nBinsT; 
                                    //} //System.out.println("tbin "+tbin+" tmax "+tmax+ "s "+s+" sl "+r );
                                    if(DISTFROMTIME[s][r][ibfield][icosalpha][tbin]==0) {
                                        // firstbin = bi
                                        // bincount = 0;				    	 
                                        DISTFROMTIME[s][r][ibfield][icosalpha][tbin]=x;
                                    } else {
                                        // bincount++;
                                        DISTFROMTIME[s][r][ibfield][icosalpha][tbin]+=stepSize;
                                    }

                                    /* if(timebfield>timebfield_max) {
                                        DISTFROMTIME[s][r][ibfield][icosalpha][tbin]=x-stepSize*0.5;
                                        if(DISTFROMTIME[s][r][ibfield][icosalpha][tbin]>dmax)
                                            DISTFROMTIME[s][r][ibfield][icosalpha][tbin] = dmax;                                               
                                    } */
                                }
                            }
                        }
                }
        }	
        TableLoader.fillMissingTableBins();
        
        T2DLOADED = true;
     }

    private static void fillMissingTableBins() {
        
        for(int s = 0; s<6; s++ ){ // loop over sectors

            for(int r = 0; r<6; r++ ){ //loop over slys
                
                for(int ibfield =0; ibfield<maxBinIdxB+1; ibfield++) {
                    
                    for(int icosalpha =0; icosalpha<maxBinIdxAlpha+1; icosalpha++) {
                        
                        for(int tbin = 0; tbin<maxTBin; tbin++) {
                            if(DISTFROMTIME[s][r][ibfield][icosalpha][tbin]!=0 && DISTFROMTIME[s][r][ibfield][icosalpha][tbin+1]==0) {
                                DISTFROMTIME[s][r][ibfield][icosalpha][tbin+1] = DISTFROMTIME[s][r][ibfield][icosalpha][tbin];
                            }
                        }
                        
                    }
                }
            }
        }
    }
    /**
     * 
     * @param x distance to wire in cm
     * @param alpha local angle in deg
     * @param bfield B field value a x in T
     * @param sector sector  
     * @param superlayer superlayer 
     * @return returns time (ns) when given inputs of distance x (cm), local angle alpha (degrees) and magnitude of bfield (Tesla).  
     */
    public static synchronized double calc_Time(double x, double alpha, double bfield, int sector, int superlayer) {
        int s = sector - 1;
        int r = superlayer - 1;
        double dmax = 2.*Constants.wpdist[r]; 
        double tmax = Tmax[s][r];
        double delBf = delta_bfield_coefficient[s][r]; 
        double Bb1 = b1[s][r];
        double Bb2 = b2[s][r];
        double Bb3 = b3[s][r];
        double Bb4 = b4[s][r];
        if(x>dmax)
            x=dmax;
        
        if(Constants.getT2D()==0) {
            
            return T2DFunctions.ExpoFcn(x, alpha, bfield, v0[s][r], deltanm[s][r], 0.615, 
                tmax, dmax, delBf, Bb1, Bb2, Bb3, Bb4, superlayer) + delta_T0[s][r];
        } else {
            return T2DFunctions.polyFcnMac(x, alpha, bfield, v0[s][r], vmid[s][r], FracDmaxAtMinVel[s][r], 
                tmax, dmax, delBf, Bb1, Bb2, Bb3, Bb4, superlayer) ;
        }
    }
    
    public static double[][] delta_T0 = new double[6][6];
    public static double[][] delta_bfield_coefficient = new double[6][6];
    public static double[][] deltanm = new double[6][6];
    public static double[][] vmid = new double[6][6];
    public static double[][] v0 = new double[6][6];
    public static double[][] b1 = new double[6][6];
    public static double[][] b2 = new double[6][6];
    public static double[][] b3 = new double[6][6];
    public static double[][] b4 = new double[6][6];
    public static double[][] Tmax = new double[6][6];
    public static double[][] FracDmaxAtMinVel = new double[6][6];		// fraction of dmax corresponding to the point in the cell where the velocity is minimal

}
