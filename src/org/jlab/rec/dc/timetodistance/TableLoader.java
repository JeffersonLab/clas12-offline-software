package org.jlab.rec.dc.timetodistance;

import java.math.RoundingMode;
import java.text.DecimalFormat;


public class TableLoader {

	public TableLoader() {
		// TODO Auto-generated constructor stub
	}

	public static double[][][] DISTFROMTIME = new double[6][2][640]; // alpha Bfield time bins
	public static boolean T2DLOADED = false;
	static int minBinIdxB = 0;
	static int maxBinIdxB = 5;
	static int minBinIdxAlpha = 0;
	static int maxBinIdxAlpha = 1;
	static int minBinIdxT0 = 0;
	static int maxBinIdxT0 = -1;
	static int minBinIdxT  = 0;
	static int maxBinIdxT  = -1;

	public static synchronized void Fill() {
	    	
			if (T2DLOADED) return;
			double dmax = 1.35; // chamber specific --> 3 tables for each region
			double dmax0 = dmax*Math.cos(Math.toRadians(30.));
			double v0 = 0.005; //cm/ns is the saturated drift velocity
			double tmax = 400;
			double delt_bfield_coefficient = 1;
			//int firstbin0=-1;
			//int bincount0 =-1;
			//int firstbin =-1;
			//int bincount  =-1;
			
			int deltanm=2;
			double minVelDriftDist = 0.615;
			double stepSize = 0.0010;
			
			DecimalFormat df = new DecimalFormat("#");
			df.setRoundingMode(RoundingMode.CEILING);
			
			for(int ibfield =0; ibfield<6; ibfield++) {
			
				double bfield = (double)ibfield*0.5;
				int nxmax = (int) (dmax/stepSize); 
				
				for(int idist =0; idist<nxmax; idist++) {
					double x = (double)(idist+1)*stepSize;
					double xhat = x/dmax;
					double xhat0 = x/dmax0;
					double[] nm = calcnm( deltanm, minVelDriftDist);
					double b = (tmax - dmax/v0)/(1.-nm[1]/nm[0]);
					double a = -b*nm[1]/nm[0];
					double time = x/v0+a*Math.pow(xhat,nm[0])+b*Math.pow(xhat, nm[1]);
					double deltatime_bfield=delt_bfield_coefficient*bfield*bfield/6.25*tmax*(0.4*xhat-2.*xhat*xhat+10.*xhat*xhat*xhat-6.5*xhat*xhat*xhat*xhat);
					double deltatime_bfield_0deg=delt_bfield_coefficient*bfield*bfield/6.25*tmax*(0.4*xhat0-2.*xhat0*xhat0+10.*xhat0*xhat0*xhat0-6.5*xhat0*xhat0*xhat0*xhat0);
					double timebfield=time+deltatime_bfield;
					// now calculate the dist to time function for theta = 0 deg.
					// Assume a functional form with the SAME POWERS but different
					// coefficients:
					// time=x/v0+a0*(x/dmax0)**n+b0*(x/dmax0)**m
					// intermediate variables gamma and zeta
			        // gamma=tmax-cos30*dmax/v0-(cos30*dmax)**n/m/v0*(1./cos30-1.)
			        //        zeta=(1.-m/n*(cos30*dmax)**(m-n))
					 double gamma=tmax-Math.cos(Math.toRadians(30.))*dmax/v0-Math.pow(Math.cos(Math.toRadians(30.))*dmax,nm[0])/nm[1]/v0*(1./Math.cos(Math.toRadians(30.))-1.);
				     double zeta=(1.-nm[1]/nm[0]*Math.pow(Math.cos(Math.toRadians(30.))*dmax,(nm[1]-nm[0])));
				     double b0deg=gamma/zeta;					    		 
				     double a0deg=(tmax-Math.cos(Math.toRadians(30.))*dmax/v0)-b0deg;
				    // calculate the time for 0deg. with no B-field					    		 
				     double time0deg=x/v0+a0deg*Math.pow(xhat0,nm[1])+b0deg*Math.pow(xhat0,nm[0]);					    		 
				    // now add in the extra time due to the non-zero bfield					    		 
				     double time0degbfield=time0deg+deltatime_bfield_0deg;
				     int bin0 = Integer.parseInt(df.format(time0degbfield/2.) ) -1;
				     int bin = Integer.parseInt(df.format(timebfield/2.) ) -1;
				    
				     if(bin<0)
				    	 bin=0;
				     if(bin0<0)
				    	 bin0=0;
				     if(bin0>maxBinIdxT0)
				    	 maxBinIdxT0 = bin0;
				     if(bin>maxBinIdxT)
				    	 maxBinIdxT = bin;
				     
				     if(DISTFROMTIME[ibfield][1][bin]==0) {
				    	// firstbin = bin;
				    	// bincount = 0;				    	 
					     DISTFROMTIME[ibfield][1][bin]=x;
				     } else {
				    	// bincount++;
				    	 DISTFROMTIME[ibfield][1][bin]+=stepSize/2;
				     }
				     if(DISTFROMTIME[ibfield][0][bin0]==0) {
				    	// firstbin0 = bin0;
				    	// bincount0 = 0;
				    	 DISTFROMTIME[ibfield][0][bin0]=x;
				     } else {
				    	// bincount0++;
				    	 DISTFROMTIME[ibfield][0][bin0]+=stepSize/2;
				     }
				  /*   if(time0degbfield>1110 && time0degbfield<1113)
				    	 System.out.println("bin 0 "+bin0+" bin "+bin+": B "+bfield+" alpha "+0+" d "+DISTFROMTIME[ibfield][0][bin0] +" alpha "+1+" d "+DISTFROMTIME[ibfield][1][bin]+
				    		 " t0 "+time0degbfield+" t "+timebfield)	; */
				}
			
			}
					
			T2DLOADED = true;
	 }
	 private static double[] calcnm(int deltanm, double minVelDriftDist) {
		
		   double[] nm = new double[2];
		   double v1 = Math.pow((deltanm-1.)*minVelDriftDist, deltanm)  ;
		   double v2 = Math.pow(minVelDriftDist, deltanm)  ;
		   double n = (1+v1)/(1-v2);
		   double m = n + deltanm;
		   nm[0] = n;
		   nm[1] = m;
		   
		   return nm;
	}


	public static void main(String args[]) {
		  /* 
		   float[][][] array = new float[2][6][20];
		   for(int i = 0; i< 2; i++)
			   for(int j = 0; j< 6; j++)
				   for(int k = 0; k< 20; k++) {
					   array[i][j][k] = (k+1)*3*(j+2);
				   }
		  
		   System.out.println(array[1][3][5]); */
		TableLoader tbl = new TableLoader();
		TableLoader.Fill();
	//	System.out.println(tbl.interpolateOnGrid(2.5, Math.toRadians(0.000000), 1000) );
	  //579: B 2.5 alpha 0 d 1.3419999999999992 alpha 1 1.3474999999999997
	   
	}
}
