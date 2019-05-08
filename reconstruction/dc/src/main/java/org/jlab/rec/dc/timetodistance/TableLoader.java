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
    static int minBinIdxT  = 0;
    static int[][][][] maxBinIdxT  = new int[6][6][8][6];
    public static double[][][][][] DISTFROMTIME = new double[6][6][maxBinIdxB+1][maxBinIdxAlpha+1][nBinsT]; // sector slyr alpha Bfield time bins [s][r][ibfield][icosalpha][tbin]
    public static double FracDmaxAtMinVel = 0.615;		// fraction of dmax corresponding to the point in the cell where the velocity is minimal

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
    
    public static int maxTBin = -1;
    public static synchronized void Fill(IndexedTable tab) {
        //CCDBTables 0 =  "/calibration/dc/signal_generation/doca_resolution";
        //CCDBTables 1 =  "/calibration/dc/time_to_distance/t2d";
        //CCDBTables 2 =  "/calibration/dc/time_corrections/T0_correction";	
        if (T2DLOADED) return;
        System.out.println(" T2D TABLE FILLED.....");
        double stepSize = 0.0010;
        DecimalFormat df = new DecimalFormat("#");
        df.setRoundingMode(RoundingMode.CEILING);
        
        for(int s = 0; s<6; s++ ){ // loop over sectors

                for(int r = 0; r<6; r++ ){ //loop over slys
                    // Fill constants
                    delta_T0[s][r] = tab.getDoubleValue("delta_T0", s+1,r+1,0);
                    deltanm[s][r] = tab.getDoubleValue("deltanm", s+1,r+1,0);
                    v0[s][r] = tab.getDoubleValue("v0", s+1,r+1,0);
                    delta_bfield_coefficient[s][r] = tab.getDoubleValue("delta_bfield_coefficient", s+1,r+1,0); 
                    b1[s][r] = tab.getDoubleValue("b1", s+1,r+1,0);
                    b2[s][r] = tab.getDoubleValue("b2", s+1,r+1,0);
                    b3[s][r] = tab.getDoubleValue("b3", s+1,r+1,0);
                    b4[s][r] = tab.getDoubleValue("b4", s+1,r+1,0);
                    Tmax[s][r] = tab.getDoubleValue("tmax", s+1,r+1,0);
                    // end fill constants
                    
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
                                    double timebfield = calc_Time( x,  alpha, bfield, s, r) ;
                                    
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
     * @param s sector idx 
     * @param r superlayer idx
     * @return returns time (ns) when given inputs of distance x (cm), local angle alpha (degrees) and magnitude of bfield (Tesla).  
     */
    public static synchronized double calc_Time(double x, double alpha, double bfield, int s, int r) {
        double dmax = 2.*Constants.wpdist[r]; 
        double tmax = Tmax[s][r];
        if(x>dmax)
            x=dmax;
        // Assume a functional form (time=x/v0+a*(x/dmax)**n+b*(x/dmax)**m)
        // for time as a function of x for theta = 30 deg.
        // first, calculate n
        double delta_nm = deltanm[s][r];
        double n = ( 1.+ (delta_nm-1.)*Math.pow(FracDmaxAtMinVel, delta_nm) )/( 1.- Math.pow(FracDmaxAtMinVel, delta_nm));
        //now, calculate m
        double m = n + delta_nm;
        // determine b from the requirement that the time = tmax at dist=dmax
        double v_0 = v0[s][r];
        double b = (tmax - dmax/v_0)/(1.- m/n);
        // determine a from the requirement that the derivative at
        // d=dmax equal the derivative at d=0
        double a = -b*m/n;

        double cos30minusalpha=Math.cos(Math.toRadians(30.-alpha));
        double xhat = x/dmax;
        double dmaxalpha = dmax*cos30minusalpha;
        double xhatalpha = x/dmaxalpha;

         //     now calculate the dist to time function for theta = 'alpha' deg.
         //     Assume a functional form with the SAME POWERS N and M and
         //     coefficient a but a new coefficient 'balpha' to replace b.    
         //     Calculate balpha from the constraint that the value
         //     of the function at dmax*cos30minusalpha is equal to tmax

         //     parameter balpha (function of the 30 degree paramters a,n,m)
         double balpha = ( tmax - dmaxalpha/v_0 - a*Math.pow(cos30minusalpha,n))/Math.pow(cos30minusalpha, m);

        //      now calculate function    
         double time = x/v_0 + a*Math.pow(xhat, n) + balpha*Math.pow(xhat, m);

        //     and here's a parameterization of the change in time due to a non-zero
        //     bfield for where xhat=x/dmaxalpha where dmaxalpha is the 'dmax' for 
        //	   a track with local angle alpha (for local angle = alpha)
        // double deltatime_bfield = CCDBConstants.getDELT_BFIELD_COEFFICIENT()[s][r]*Math.pow(bfield,2)*tmax*(CCDBConstants.getDELTATIME_BFIELD_PAR1()[s][r]*xhatalpha+CCDBConstants.getDELTATIME_BFIELD_PAR2()[s][r]*Math.pow(xhatalpha, 2)+
        //		 CCDBConstants.getDELTATIME_BFIELD_PAR3()[s][r]*Math.pow(xhatalpha, 3)+CCDBConstants.getDELTATIME_BFIELD_PAR4()[s][r]*Math.pow(xhatalpha, 4));
        double delBf = delta_bfield_coefficient[s][r]; 
        //delBf = 0.15;
        double deltatime_bfield = delBf*Math.pow(bfield,2)*tmax*(b1[s][r]*xhatalpha+b2[s][r]*Math.pow(xhatalpha, 2)+
                     b3[s][r]*Math.pow(xhatalpha, 3)+b4[s][r]*Math.pow(xhatalpha, 4));
        // System.out.println("dB "+deltatime_bfield+" raw time "+time);
        //calculate the time at alpha deg. and at a non-zero bfield	          
        time += deltatime_bfield;
        //added deta(T0) correction
        time += delta_T0[s][r];

        return time;
    }
    public static double[][] delta_T0 = new double[6][6];
    public static double[][] delta_bfield_coefficient = new double[6][6];
    public static double[][] deltanm = new double[6][6];
    public static double[][] b1 = new double[6][6];
    public static double[][] b2 = new double[6][6];
    public static double[][] b3 = new double[6][6];
    public static double[][] b4 = new double[6][6];
    public static double[][] v0 = new double[6][6];
    public static double[][] Tmax = new double[6][6];

	//public static void main(String args[]) {
	//	CalibrationConstantsLoader.Load(10, "default");
	//	TableLoader tbl = new TableLoader();
	//	TableLoader.Fill();
		//System.out.println(maxBinIdxT[1][0][0]+" "+maxBinIdxT[1][0][5]+" "+DISTFROMTIME[1][0][0][maxBinIdxT[1][0][0]]+ " "+DISTFROMTIME[1][0][5][maxBinIdxT[1][0][5]]);
		//System.out.println(tbl.interpolateOnGrid(2.5, Math.toRadians(0.000000), 1000) );
	  //579: B 2.5 alpha 0 d 1.3419999999999992 alpha 1 1.3474999999999997
	   
	//}
}
