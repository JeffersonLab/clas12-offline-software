package org.jlab.rec.dc.timetodistance;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class TimeToDistanceEstimator {

	public TimeToDistanceEstimator() {
		// TODO Auto-generated constructor stub
	}

	 private double interpolateLinear(double x, double xa, double xb, double ya, double yb) {
		 double y = ya*(xb - x)/(xb - xa) + yb*(x - xa)/(xb - xa);
		 return y;
	 }
	 
	 public double interpolateOnGrid(double B, double alpha, double t) {
		 // for a given value of B find the bin edges in Tesla and the corresponding index:
		 int binlowB  = this.getBIdx(B);
		 int binhighB = binlowB +1; 
		 double B1 = binlowB*0.5;
		 double B2 = binhighB*0.5;
		 
		 // for alpha there are only 2 bins
		 double alpha1 = 0;
		 double alpha2 = Math.toRadians(30.);
		 if(alpha<alpha1)
			 alpha=alpha1;
		 if(alpha>alpha2)
			 alpha=alpha2;
		 
		 int binlowAlpha  = 0;
		 int binhighAlpha = 1;
		 // get the time bin edges:
		 int binlowT = this.getTimeIdx(t);
		 int binhighT = binlowT +1; 
		 double t1 = binlowT*2.;
		 double t2 = binhighT*2.;
		// System.out.println(" binlowT "+binlowT);
		 // interpolate in B:
		 double f_B_alpha1_t1 = interpolateLinear(B*B, B1*B1, B2*B2, 
				 TableLoader.DISTFROMTIME[binlowB][binlowAlpha][binlowT],
				 TableLoader.DISTFROMTIME[binhighB][binlowAlpha][binlowT]);
		 double f_B_alpha2_t1 = interpolateLinear(B*B, B1*B1, B2*B2, 
				 TableLoader.DISTFROMTIME[binlowB][binhighAlpha][binlowT],
				 TableLoader.DISTFROMTIME[binhighB][binhighAlpha][binlowT]);
		 double f_B_alpha1_t2 = interpolateLinear(B*B, B1*B1, B2*B2, 
				 TableLoader.DISTFROMTIME[binlowB][binlowAlpha][binhighT],
				 TableLoader.DISTFROMTIME[binhighB][binlowAlpha][binhighT]);
		 double f_B_alpha2_t2 = interpolateLinear(B*B, B1*B1, B2*B2, 
				 TableLoader.DISTFROMTIME[binlowB][binhighAlpha][binhighT],
				 TableLoader.DISTFROMTIME[binhighB][binhighAlpha][binhighT]);

		 // interpolate in d for 2 values of alpha:		 
		 double f_B_alpha1_t = interpolateLinear(t, t1, t2, f_B_alpha1_t1, f_B_alpha1_t2);
		 double f_B_alpha2_t = interpolateLinear(t, t1, t2, f_B_alpha2_t1, f_B_alpha2_t2);
		 
		 // interpolate in alpha:
		 double f_B_alpha_t = interpolateLinear(Math.cos(alpha), Math.cos(alpha1), Math.cos(alpha2), f_B_alpha1_t, f_B_alpha2_t);
		
		 return f_B_alpha_t;
	 }
	
	   private int getTimeIdx(double t1) {
		DecimalFormat df = new DecimalFormat("#");
		df.setRoundingMode(RoundingMode.CEILING);
		int binIdx = Integer.parseInt(df.format(t1/2.) ) -1;
		if(binIdx<0)
			binIdx = TableLoader.minBinIdxT;
		if(binIdx>TableLoader.maxBinIdxT)
			binIdx = TableLoader.maxBinIdxT;
		return binIdx;
	}

	private int getBIdx(double b1) {
		// double bfield = (double)ibfield*0.5;
		int binIdx = (int) ((1+b1)*2) -2;
		if(binIdx<0)
			binIdx = TableLoader.minBinIdxB;
		if(binIdx>TableLoader.maxBinIdxB)
			binIdx = TableLoader.maxBinIdxB;
		return binIdx;
	}

	
}
