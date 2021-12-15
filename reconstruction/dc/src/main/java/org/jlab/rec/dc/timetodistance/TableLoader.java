package org.jlab.rec.dc.timetodistance;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.rec.dc.Constants;
import org.jlab.utils.groups.IndexedTable;


public class TableLoader {

    public TableLoader() {
    }
    
    public static final Logger LOGGER = Logger.getLogger(TableLoader.class.getName());

    private static boolean T2DLOADED = false;
    
    private static final int NBINST=2000;
    
    public static final double[] BfieldValues = new double[]{0.0000, 1.0000, 1.4142, 1.7321, 2.0000, 2.2361, 2.4495, 2.6458};
    public static int minBinIdxB = 0;
    public static int maxBinIdxB = BfieldValues.length-1;
    public static int minBinIdxAlpha = 0;
    public static int maxBinIdxAlpha = 5;
    private static final double[] AlphaMid = new double[6];
    private static final double[][] AlphaBounds = new double[6][2];
    public static int minBinIdxT  = 0;
    public static final int[][][][] maxBinIdxT  = new int[6][6][8][6];
    public static double[][][][][] DISTFROMTIME = new double[6][6][maxBinIdxB+1][maxBinIdxAlpha+1][NBINST]; // sector slyr alpha Bfield time bins [s][r][ibfield][icosalpha][tbin]    
    public static int maxTBin = -1;
        //public static double[] distbetaValues = new double[]{0.16, 0.16, 0.08, 0.08, 0.08, 0.08};
    
    /*
     * 
     */
    public static void test(){
            TimeToDistanceEstimator tde = new TimeToDistanceEstimator();
            for(int s = 0; s<1; s++ ){ // loop over sectors
                    for(int r = 4; r<5; r++ ){ //loop over slys
                            for(int ibfield =0; ibfield<1; ibfield++) {
                                for (int tb = 250; tb< 300; tb++) {
                                    LOGGER.log(Level.FINE, " NEW TIME BIN ");
                                    for(int icosalpha =0; icosalpha<maxBinIdxAlpha+1; icosalpha++) {
                                            //for (int tb = 0; tb< maxBinIdxT[s][r][ibfield][icosalpha]; tb++) {
                                            double Xalpha = -(Math.toDegrees(Math.acos(Math.cos(Math.toRadians(30.)) + (icosalpha)*(1. - Math.cos(Math.toRadians(30.)))/5.)) - 30.);
                                            double Xtime=(2*tb+1);

                                            //for (int k=0; k<10; k++){
                                            double Bf = (ibfield)*0.5;
                                            int bbin = tde.getBIdx(Bf);
                                            double Xdoca=tde.interpolateOnGrid((double) Bf, Xalpha, Xtime, s, r);
                                                LOGGER.log(Level.FINE, "Bbin "+ibfield+" B "+ (float)Bf+" sl "+(r+1)+" time "+Xtime+" tb "+tb+" timeBin "+tde.getTimeIdx(Xtime, s, r, ibfield, icosalpha)
                                                        +" icosalpha "+icosalpha+" Xalpha "+(float) Xalpha + " dis "+ (float)DISTFROMTIME[s][r][bbin][icosalpha][tde.getTimeIdx(Xtime, s, r, ibfield, icosalpha)] +" time' "+
                                                      (float)  calc_Time( Xdoca,  Xalpha, Bf, s+1, r+1) +" tdix "+tde.getTimeIdx(calc_Time( Xdoca,  Xalpha, Bf, s+1, r+1), s, r, ibfield, icosalpha));
                                            //}
                                            }

                                    }
                            }
                    }
            }
    }
    
    private static int getAlphaBin(double Alpha) {
        int bin = 0;
        for(int b =0; b<6; b++) {
            if(Alpha>=AlphaBounds[b][0] && Alpha<=AlphaBounds[b][1] )
                bin = b;
        }
        return bin;
    }
    
    private static synchronized void FillAlpha() {
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
                    //LOGGER.log(Level.FINE, v0[s][r]+" "+vmid[s][r]+" "+FracDmaxAtMinVel[s][r]);
                    double dmax = 2.*Constants.getInstance().wpdist[r]; 
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
                                    
                                    if(tbin<0 || tbin>NBINST-1) {
                                        //System.err.println("Problem with tbin");
                                        continue;
                                    }
                                    if(tbin>maxTBin)
                                        maxTBin = tbin;
                                    //if(tbin>maxBinIdxT[s][r][ibfield][icosalpha]) {
                                        //maxBinIdxT[s][r][ibfield][icosalpha] = NBINST; 
                                    //} //LOGGER.log(Level.FINE, "tbin "+tbin+" tmax "+tmax+ "s "+s+" sl "+r );
                                    if(DISTFROMTIME[s][r][ibfield][icosalpha][tbin]==0) {
                                        // firstbin = bi
                                        // bincount = 0;				    	 
                                        DISTFROMTIME[s][r][ibfield][icosalpha][tbin]=x;
                                    } else {
                                        // test for getting center of the bin (to be validated):
                                        //double prevTime = calc_Time(x-stepSize,  alpha, bfield, s+1, r+1);
                                        //if(x>DISTFROMTIME[s][r][ibfield][icosalpha][tbin]
                                        //        && Math.abs((double)(2.*tbin+1)-timebfield)<=Math.abs((double)(2.*tbin+1)-prevTime)) {
                                        //    DISTFROMTIME[s][r][ibfield][icosalpha][tbin]=x;
                                        //}
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
        //TableLoader.test();
        T2DLOADED = true;
     }

    private static synchronized void fillMissingTableBins() {
        
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
        double dmax = 2.*Constants.getInstance().wpdist[r]; 
        double tmax = Tmax[s][r];
        double delBf = delta_bfield_coefficient[s][r]; 
        double Bb1 = b1[s][r];
        double Bb2 = b2[s][r];
        double Bb3 = b3[s][r];
        double Bb4 = b4[s][r];
        if(x>dmax)
            x=dmax;
        
        if(Constants.getInstance().getT2D()==0) {
            
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
