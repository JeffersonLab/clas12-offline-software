package org.jlab.rec.dc.timetodistance;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.jlab.rec.dc.CCDBConstants;
import org.jlab.rec.dc.CalibrationConstantsLoader;

public class TableLoader {

    public TableLoader() {
        
    }
   
    public static final double[][][][][] DISTFROMTIME = new double[6][6][6][6][850]; // sector slyr alpha Bfield time bins
    static boolean T2DLOADED = false;
    static int minBinIdxB = 0;
    static int maxBinIdxB = 5;
    static int minBinIdxAlpha = 0;
    static int maxBinIdxAlpha = 6;
    static int minBinIdxT = 0;
    static int[][][][] maxBinIdxT = new int[6][6][6][6];

    public static double FracDmaxAtMinVel = 0.615;		// fraction of dmax corresponding to the point in the cell where the velocity is minimal

    /*
	 * 
     */
    public static synchronized void Fill() {

        if (T2DLOADED) {
            return;
        }

        double stepSize = 0.0010;

        DecimalFormat df = new DecimalFormat("#");
        df.setRoundingMode(RoundingMode.CEILING);
        for (int s = 0; s < 6; s++) { // loop over sectors

            for (int r = 0; r < 6; r++) { //loop over slys
                double dmax = CCDBConstants.getDMAXSUPERLAYER()[r];
                double tmax = CCDBConstants.getTMAXSUPERLAYER()[s][r];

                for (int ibfield = 0; ibfield < 6; ibfield++) {
                    double bfield = (double) ibfield * 0.5;

                    double maxdist = 0;

                    for (int icosalpha = 0; icosalpha < 6; icosalpha++) {

                        double cos30minusalpha = Math.cos(Math.toRadians(30.)) + (double) (icosalpha) * (1. - Math.cos(Math.toRadians(30.))) / 5.;

                        double alpha = -(Math.toDegrees(Math.acos(cos30minusalpha)) - 30);

                        int nxmax = (int) (dmax / stepSize);

                        for (int idist = 0; idist < nxmax; idist++) {

                            double x = (double) (idist + 1) * stepSize;
                            double timebfield = calc_Time(x, dmax, tmax, alpha, bfield, s, r);

                            if (timebfield <= calc_Time(dmax, dmax, tmax, 30, bfield, s, r)) {
                                maxdist = x;
                            }

                            if (timebfield > calc_Time(dmax, dmax, tmax, 30, bfield, s, r)) {
                                x = maxdist;
                            }
                            
                            int tbin = Integer.parseInt(df.format(timebfield / 2.)) - 1;

                            if (tbin < 0) {
                                tbin = 0;
                            }
                            
                            if (tbin > maxBinIdxT[s][r][ibfield][icosalpha]) {
                                maxBinIdxT[s][r][ibfield][icosalpha] = tbin;
                            } 
                            if(tbin>849) {
                                 continue;
                            }
                            if (DISTFROMTIME[s][r][ibfield][icosalpha][tbin] == 0) {
                                // firstbin = bin;
                                // bincount = 0;				    	 
                                DISTFROMTIME[s][r][ibfield][icosalpha][tbin] = x;
                            } else {
                                // bincount++;
                                DISTFROMTIME[s][r][ibfield][icosalpha][tbin] += stepSize;
                            }
                               
                        }
                    }
                }
            }
        }

        /*		for(int s = 1; s<2; s++ ){ // loop over sectors

				for(int r = 0; r<6; r++ ){ //loop over slys
						
					for(int ibfield =0; ibfield<6; ibfield++) {
					   
						for(int icosalpha =0; icosalpha<6; icosalpha++) {
							
							for (int tb = 0; tb< maxBinIdxT[s][r][ibfield][icosalpha]; tb++)
								System.out.println("s "+(s+1)+" sl "+(r+1)+" ibfield "+ibfield+" icosalpha "+icosalpha+"tbin "+ tb + " val "+ DISTFROMTIME[s][r][ibfield][icosalpha][tb]);
					    	
						}
					}
				}
			}
         */
        T2DLOADED = true;
    }

    /**
     *
     * @param x
     * @param dmax
     * @param tmax
     * @param alpha
     * @param bfield
     * @param s sector idx
     * @param r superlayer idx
     * @return returns time (ns) when given inputs of distance x (cm), local
     * angle alpha (degrees) and magnitude of bfield (Tesla).
     */
    public static synchronized double calc_Time(double x, double dmax, double tmax, double alpha, double bfield, int s, int r) {

        // Assume a functional form (time=x/v0+a*(x/dmax)**n+b*(x/dmax)**m)
        // for time as a function of x for theta = 30 deg.
        // first, calculate n
        double n = (1. + (CCDBConstants.getDELTANM()[s][r] - 1.) * Math.pow(FracDmaxAtMinVel, CCDBConstants.getDELTANM()[s][r])) / (1. - Math.pow(FracDmaxAtMinVel, CCDBConstants.getDELTANM()[s][r]));
        //now, calculate m
        double m = n + CCDBConstants.getDELTANM()[s][r];
        // determine b from the requirement that the time = tmax at dist=dmax
        double b = (tmax - dmax / CCDBConstants.getV0()[s][r]) / (1. - m / n);
        // determine a from the requirement that the derivative at
        // d=dmax equal the derivative at d=0
        double a = -b * m / n;

        double cos30minusalpha = Math.cos(Math.toRadians(30. - alpha));
        double xhat = x / dmax;
        double dmaxalpha = dmax * cos30minusalpha;
        double xhatalpha = x / dmaxalpha;

        //     now calculate the dist to time function for theta = 'alpha' deg.
        //     Assume a functional form with the SAME POWERS N and M and
        //     coefficient a but a new coefficient 'balpha' to replace b.    
        //     Calculate balpha from the constraint that the value
        //     of the function at dmax*cos30minusalpha is equal to tmax
        //     parameter balpha (function of the 30 degree paramters a,n,m)
        double balpha = (tmax - dmaxalpha / CCDBConstants.getV0()[s][r] - a * Math.pow(cos30minusalpha, n)) / Math.pow(cos30minusalpha, m);

        //      now calculate function    
        double time = x / CCDBConstants.getV0()[s][r] + a * Math.pow(xhat, n) + balpha * Math.pow(xhat, m);

        //     and here's a parameterization of the change in time due to a non-zero
        //     bfield for where xhat=x/dmaxalpha where dmaxalpha is the 'dmax' for 
        //	   a track with local angle alpha (for local angle = alpha)
        double deltatime_bfield = CCDBConstants.getDELT_BFIELD_COEFFICIENT()[s][r] * Math.pow(bfield, 2) * tmax * (CCDBConstants.getDELTATIME_BFIELD_PAR1()[s][r] * xhatalpha + CCDBConstants.getDELTATIME_BFIELD_PAR2()[s][r] * Math.pow(xhatalpha, 2)
                + CCDBConstants.getDELTATIME_BFIELD_PAR3()[s][r] * Math.pow(xhatalpha, 3) + CCDBConstants.getDELTATIME_BFIELD_PAR4()[s][r] * Math.pow(xhatalpha, 4));

        //calculate the time at alpha deg. and at a non-zero bfield	          
        time += deltatime_bfield;

        return time;
    }

    public static void main(String args[]) {
        CalibrationConstantsLoader.Load(10, "default");
        TableLoader tbl = new TableLoader();
        TableLoader.Fill();
        //System.out.println(maxBinIdxT[1][0][0]+" "+maxBinIdxT[1][0][5]+" "+DISTFROMTIME[1][0][0][maxBinIdxT[1][0][0]]+ " "+DISTFROMTIME[1][0][5][maxBinIdxT[1][0][5]]);
        //System.out.println(tbl.interpolateOnGrid(2.5, Math.toRadians(0.000000), 1000) );
        //579: B 2.5 alpha 0 d 1.3419999999999992 alpha 1 1.3474999999999997

    }
}
