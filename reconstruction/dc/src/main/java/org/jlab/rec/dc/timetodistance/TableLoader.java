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
    public static double[][][][][] DISTFROMTIME = new double[6][6][8][6][nBinsT]; // sector slyr alpha Bfield time bins
    static boolean T2DLOADED = false;
    static boolean T0LOADED = false;
    static int minBinIdxB = 0;
    static int maxBinIdxB = 7;
    static int minBinIdxAlpha = 0;
    static int maxBinIdxAlpha = 6;
    static int minBinIdxT  = 0;
    static int[][][][] maxBinIdxT  = new int[6][6][8][6];

    public static double FracDmaxAtMinVel = 0.615;		// fraction of dmax corresponding to the point in the cell where the velocity is minimal

    /*
     * 
     */
    public void test(){
            TimeToDistanceEstimator tde = new TimeToDistanceEstimator();
            for(int s = 0; s<1; s++ ){ // loop over sectors
                    for(int r = 2; r<3; r++ ){ //loop over slys
                            for(int ibfield =0; ibfield<8; ibfield++) {
                                    for(int icosalpha =0; icosalpha<6; icosalpha++) {
                                            for (int tb = 0; tb< maxBinIdxT[s][r][ibfield][icosalpha]; tb++) {
                                                double Xalpha = -(Math.toDegrees(Math.acos(Math.cos(Math.toRadians(30.)) + (icosalpha)*(1. - Math.cos(Math.toRadians(30.)))/5.)) - 30.);
                                                double Xtime=(2*tb+1);
                                                double Xdoca=tde.interpolateOnGrid((double) ibfield*0.5, Xalpha, Xtime, s, r);
                                                    System.out.println("s "+(s+1)+" sl "+(r+1)+" time "+(2*tb+1)
                                                            +" icosalpha "+icosalpha+" Xalpha "+Xalpha+" B "+ ibfield*0.5 + " dis "+ (float)DISTFROMTIME[s][r][ibfield][icosalpha][tb] +" "+
                                                          (float) Xdoca );
                                            }

                                    }
                            }
                    }
            }
    }

    public static synchronized void FillT0Tables(int run) {
        if (T0LOADED) return;
        System.out.println(" T0 TABLE FILLED.....");
        DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(run, "default");
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
            
        }
        T0LOADED = true;
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
        for(int s = 0; s<6; s++ ){ // loop over sectors

                for(int r = 0; r<6; r++ ){ //loop over slys
                        double dmax = 2.*Constants.wpdist[r]; 
                        //double tmax = CCDBConstants.getTMAXSUPERLAYER()[s][r];
                        double tmax = tab.getDoubleValue("tmax", s+1,r+1,0);

                        for(int ibfield =0; ibfield<8; ibfield++) {
                            double bfield = (double)ibfield*0.5;

                            double maxdist =0;

                                for(int icosalpha =0; icosalpha<6; icosalpha++) {

                                        double cos30minusalpha = Math.cos(Math.toRadians(30.)) + (double) (icosalpha)*(1. - Math.cos(Math.toRadians(30.)))/5.;

                                        double alpha = -(Math.toDegrees(Math.acos(cos30minusalpha)) - 30);

                                        int nxmax = (int) (dmax/stepSize); 

                                        for(int idist =0; idist<nxmax; idist++) {

                                                double x = (double)(idist+1)*stepSize;
                                                double timebfield = calc_Time( x,  dmax,  tmax,  alpha, bfield, s, r, tab) ;

                                                if(timebfield<=calc_Time( dmax,  dmax,  tmax,  30, bfield, s, r, tab))
                                                        maxdist=x;

                                                if(timebfield>calc_Time( dmax,  dmax,  tmax,  30, bfield, s, r, tab))
                                                        x=maxdist;

                                            int tbin = Integer.parseInt(df.format(timebfield/2.) ) -1;


                                            if(tbin<0)
                                                tbin=0;
                                            if(tbin>=nBinsT)
                                                tbin = nBinsT-1;
                                            if(tbin>maxBinIdxT[s][r][ibfield][icosalpha]) {
                                                maxBinIdxT[s][r][ibfield][icosalpha] = tbin; 
                                            } //System.out.println("tbin "+tbin+" tmax "+tmax+ "s "+s+" sl "+r );
                                            if(DISTFROMTIME[s][r][ibfield][icosalpha][tbin]==0) {
                                                // firstbin = bin;
                                                // bincount = 0;				    	 
                                                DISTFROMTIME[s][r][ibfield][icosalpha][tbin]=x;
                                            } else {
                                                // bincount++;
                                                DISTFROMTIME[s][r][ibfield][icosalpha][tbin]+=stepSize;
                                            }

                                           // System.out.println(r+"  "+ibfield+"  "+icosalpha+"  "+tbin +"  "+timebfield+ "  "+x+"  "+alpha); 
                                        }
                                }
                        }
                }
        }	
        T2DLOADED = true;
     }

    /**
     * 
     * @param x distance to wire in cm
     * @param dmax max distance to wire in cm
     * @param tmax max drift time in ns
     * @param alpha local angle in deg
     * @param bfield B field value a x in T
     * @param s sector idx 
     * @param r superlayer idx
     * @return returns time (ns) when given inputs of distance x (cm), local angle alpha (degrees) and magnitude of bfield (Tesla).  
     */
    public static synchronized double calc_Time(double x, double dmax, double tmax, double alpha, double bfield, int s, int r, IndexedTable tab) {

        // Assume a functional form (time=x/v0+a*(x/dmax)**n+b*(x/dmax)**m)
        // for time as a function of x for theta = 30 deg.
        // first, calculate n
        double deltanm = tab.getDoubleValue("deltanm", s+1,r+1,0);
        double n = ( 1.+ (deltanm-1.)*Math.pow(FracDmaxAtMinVel, deltanm) )/( 1.- Math.pow(FracDmaxAtMinVel, deltanm));
        //now, calculate m
        double m = n + deltanm;
        // determine b from the requirement that the time = tmax at dist=dmax
        double v0 = tab.getDoubleValue("v0", s+1,r+1,0);
        double b = (tmax - dmax/v0)/(1.- m/n);
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
         double balpha = ( tmax - dmaxalpha/v0 - a*Math.pow(cos30minusalpha,n))/Math.pow(cos30minusalpha, m);

        //      now calculate function    
         double time = x/v0 + a*Math.pow(xhat, n) + balpha*Math.pow(xhat, m);

        //     and here's a parameterization of the change in time due to a non-zero
        //     bfield for where xhat=x/dmaxalpha where dmaxalpha is the 'dmax' for 
        //	   a track with local angle alpha (for local angle = alpha)
        // double deltatime_bfield = CCDBConstants.getDELT_BFIELD_COEFFICIENT()[s][r]*Math.pow(bfield,2)*tmax*(CCDBConstants.getDELTATIME_BFIELD_PAR1()[s][r]*xhatalpha+CCDBConstants.getDELTATIME_BFIELD_PAR2()[s][r]*Math.pow(xhatalpha, 2)+
        //		 CCDBConstants.getDELTATIME_BFIELD_PAR3()[s][r]*Math.pow(xhatalpha, 3)+CCDBConstants.getDELTATIME_BFIELD_PAR4()[s][r]*Math.pow(xhatalpha, 4));
        double delBf = tab.getDoubleValue("delta_bfield_coefficient", s+1,r+1,0); 
        //delBf = 0.15;
        double deltatime_bfield = delBf*Math.pow(bfield,2)*tmax*(tab.getDoubleValue("b1", s+1,r+1,0)*xhatalpha+tab.getDoubleValue("b2", s+1,r+1,0)*Math.pow(xhatalpha, 2)+
                     tab.getDoubleValue("b3", s+1,r+1,0)*Math.pow(xhatalpha, 3)+tab.getDoubleValue("b4", s+1,r+1,0)*Math.pow(xhatalpha, 4));
        // System.out.println("dB "+deltatime_bfield+" raw time "+time);
        //calculate the time at alpha deg. and at a non-zero bfield	          
        time += deltatime_bfield;
        //added deta(T0) correction
        time+= tab.getDoubleValue("delta_T0", s+1,r+1,0);

        return time;
    }

	//public static void main(String args[]) {
	//	CalibrationConstantsLoader.Load(10, "default");
	//	TableLoader tbl = new TableLoader();
	//	TableLoader.Fill();
		//System.out.println(maxBinIdxT[1][0][0]+" "+maxBinIdxT[1][0][5]+" "+DISTFROMTIME[1][0][0][maxBinIdxT[1][0][0]]+ " "+DISTFROMTIME[1][0][5][maxBinIdxT[1][0][5]]);
		//System.out.println(tbl.interpolateOnGrid(2.5, Math.toRadians(0.000000), 1000) );
	  //579: B 2.5 alpha 0 d 1.3419999999999992 alpha 1 1.3474999999999997
	   
	//}
}
