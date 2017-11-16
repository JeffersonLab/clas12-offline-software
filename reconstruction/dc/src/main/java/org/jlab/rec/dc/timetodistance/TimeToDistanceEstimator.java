package org.jlab.rec.dc.timetodistance;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import org.jlab.rec.dc.Constants;


public class TimeToDistanceEstimator {

	public TimeToDistanceEstimator() {
		// TODO Auto-generated constructor stub
	}

	 private double interpolateLinear(double x, double xa, double xb, double ya, double yb) {
		 double y = ya*(xb - x)/(xb - xa) + yb*(x - xa)/(xb - xa);
		 if(xb - xa == 0)
			 y = ya + yb;
		 return y;
	 }
         
	 /**
	  * 
	  * @param B B field in T
	  * @param alpha is the local angle in degrees
	  * @param t time
	  * @param SlyrIdx slyr index (0...5)
	  * @return
	  */
	 public double interpolateOnGrid(double B, double alpha, double t,  int SecIdx, int SlyrIdx) {
		 // for a given value of B find the bin edges in Tesla and the corresponding index:
		// if(alpha>30)
		//	 alpha-=30;
		 
		 int binlowB  = this.getBIdx(B);
		 int binhighB = binlowB + 1; 
		 
		 if(binhighB > TableLoader.maxBinIdxB-1) {
			 binhighB = TableLoader.maxBinIdxB-1;
		 }
			 
		 
		 double B1 = binlowB*0.5;
		 double B2 = binhighB*0.5;
		
		 // for alpha ranges		
		 int binlowAlpha  = this.getAlphaIdx(alpha);
		 int binhighAlpha = binlowAlpha + 1;
		 
		 if(binhighAlpha > TableLoader.maxBinIdxAlpha-1) {
			 binhighAlpha = TableLoader.maxBinIdxAlpha-1;
		 }
		 
		 double alpha1 = this.getAlphaFromAlphaIdx(binlowAlpha);	 
		 double alpha2 = this.getAlphaFromAlphaIdx(binhighAlpha);
		
		 // get the time bin edges:
		 int binlowT = this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binlowAlpha);  
		 int binhighT = binlowT + 1; 
		 
		 if(binhighT>TableLoader.maxBinIdxT[SecIdx][SlyrIdx][binlowB][binlowAlpha]-1)
			 binhighT=TableLoader.maxBinIdxT[SecIdx][SlyrIdx][binlowB][binlowAlpha]-1;
		 
		 double t1 = binlowT*2.;
		 double t2 = binhighT*2.;
		
		 if(t>t2)
			 t=t2;
		 
		 // interpolate in B:
		 double f_B_alpha1_t1 = interpolateLinear(B*B, B1*B1, B2*B2, 
				 TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binlowB][binlowAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binlowAlpha)],
				 TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binhighB][binlowAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binhighB, binlowAlpha)]);
		 double f_B_alpha2_t1 = interpolateLinear(B*B, B1*B1, B2*B2, 
				 TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binlowB][binhighAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binhighAlpha)],
				 TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binhighB][binhighAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binhighB, binhighAlpha)]);
		 double f_B_alpha1_t2 = interpolateLinear(B*B, B1*B1, B2*B2, 
				 TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binlowB][binlowAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binlowAlpha)+1],
				 TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binhighB][binlowAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binhighB, binlowAlpha)+1]);
		 double f_B_alpha2_t2 = interpolateLinear(B*B, B1*B1, B2*B2, 
				 TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binlowB][binhighAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binhighAlpha)+1],
				 TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binhighB][binhighAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binhighB, binhighAlpha)+1]);
		 // interpolate in d for 2 values of alpha:		 
		 double f_B_alpha1_t = interpolateLinear(t, t1, t2, f_B_alpha1_t1, f_B_alpha1_t2);
		 double f_B_alpha2_t = interpolateLinear(t, t1, t2, f_B_alpha2_t1, f_B_alpha2_t2);
		 
		 // interpolate in alpha: (cos30-cosA)
		 double f_B_alpha_t = interpolateLinear(Math.cos(Math.toRadians(30.))-Math.cos(Math.toRadians(alpha)), 
				 Math.cos(Math.toRadians(30.))-Math.cos(Math.toRadians(alpha1)), 
				 Math.cos(Math.toRadians(30.))-Math.cos(Math.toRadians(alpha2)), f_B_alpha1_t, f_B_alpha2_t);
		 
		 /*if(t>120)
				System.out.println(binlowAlpha+") "+ TableLoader.DISTFROMTIME[RegIdx][binlowB][binlowAlpha][this.getTimeIdx(t, RegIdx, binlowB, binlowAlpha)]+" "+
						TableLoader.DISTFROMTIME[RegIdx][binhighB][binlowAlpha][this.getTimeIdx(t, RegIdx, binhighB, binlowAlpha)]+" "+
						TableLoader.DISTFROMTIME[RegIdx][binlowB][binhighAlpha][this.getTimeIdx(t, RegIdx, binlowB, binhighAlpha)]+" "+
						TableLoader.DISTFROMTIME[RegIdx][binhighB][binhighAlpha][this.getTimeIdx(t, RegIdx, binhighB, binhighAlpha)]+" "+
						TableLoader.DISTFROMTIME[RegIdx][binlowB][binlowAlpha][this.getTimeIdx(t, RegIdx, binlowB, binlowAlpha)+1]+" "+
						TableLoader.DISTFROMTIME[RegIdx][binhighB][binlowAlpha][this.getTimeIdx(t, RegIdx, binhighB, binlowAlpha)+1]+" "+
						TableLoader.DISTFROMTIME[RegIdx][binlowB][binhighAlpha][this.getTimeIdx(t, RegIdx, binlowB, binhighAlpha)+1]+" "+
						TableLoader.DISTFROMTIME[RegIdx][binhighB][binhighAlpha][this.getTimeIdx(t, RegIdx, binhighB, binhighAlpha)+1]
								+"  --  "+f_B_alpha1_t1+"  :  "+f_B_alpha2_t1+"  :  "+f_B_alpha1_t2+"  :  "+f_B_alpha2_t2
								+"  ---  "+f_B_alpha1_t+"  :  "+f_B_alpha2_t+" === "+f_B_alpha_t); */
		 if(f_B_alpha_t>2*Constants.wpdist[SlyrIdx])
			 f_B_alpha_t = 2*Constants.wpdist[SlyrIdx];                                                      
			// System.out.println(SlyrIdx+" t "+t+" "+f_B_alpha_t+" tmax "+CalibrationConstantsLoader.dmaxsuperlayer[SlyrIdx]);
		return f_B_alpha_t;
	 }
	
	   private double getAlphaFromAlphaIdx(int binAlpha) {
			
		 double cos30minusalpha = Math.cos(Math.toRadians(30.)) + (double) (binAlpha)*(1. - Math.cos(Math.toRadians(30.)))/5.;
		 double alpha =  -(Math.toDegrees(Math.acos(cos30minusalpha)) - 30);
		 double alpha1 = 0;
		 double alpha2 = 30.;
		 if(alpha<alpha1)
			 alpha=alpha1;
		 if(alpha>alpha2)
			 alpha=alpha2;	
		return alpha;
	}

	private int getTimeIdx(double t1, int is, int ir, int ibfield, int icosalpha) {
		DecimalFormat df = new DecimalFormat("#");
		df.setRoundingMode(RoundingMode.CEILING);
		int binIdx = Integer.parseInt(df.format(t1/2.) ) -1; 
		if(binIdx<0)
			binIdx = TableLoader.minBinIdxT;
		if(binIdx>TableLoader.maxBinIdxT[is][ir][ibfield][icosalpha])
			binIdx = TableLoader.maxBinIdxT[is][ir][ibfield][icosalpha]-1;
		
		return binIdx;
	}

	private int getBIdx(double b1) {
		// double bfield = (double)ibfield*0.5;
		int binIdx = (int) ((1+b1)*2) -2;
		if(binIdx<0)
			binIdx = TableLoader.minBinIdxB;
		if(binIdx>TableLoader.maxBinIdxB)
			binIdx = TableLoader.maxBinIdxB-1;
		return binIdx;
	}

	private int getAlphaIdx(double alpha) {
		double Ccos30minusalpha = Math.cos(Math.toRadians(30-alpha) ) ; 
		double Cicosalpha = (Ccos30minusalpha - Math.cos(Math.toRadians(30.)))/((1. - Math.cos(Math.toRadians(30.)))/5.);
		int binIdx = (int)  Cicosalpha; 
		if(binIdx<0)
			binIdx = TableLoader.minBinIdxAlpha;
		if(binIdx>TableLoader.maxBinIdxAlpha)
			binIdx = TableLoader.maxBinIdxAlpha-1; 
		
		
		return binIdx;
	}
	
		
		
		
}
