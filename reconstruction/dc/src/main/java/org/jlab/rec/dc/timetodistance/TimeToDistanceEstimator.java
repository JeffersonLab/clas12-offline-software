package org.jlab.rec.dc.timetodistance;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TimeToDistanceEstimator {

    public TimeToDistanceEstimator() {
    }
    
    private static final Logger LOGGER = Logger.getLogger(TimeToDistanceEstimator.class.getName());
    /**
     * 
     * @param x value on grid
     * @param xa lower x bound on grid
     * @param xb upper x bound on grid
     * @param ya lower y bound on grid
     * @param yb upper y bound on grid
     * @return y value on grid from linear interpolation between a and b evaluated at x
     */
    private double interpolateLinear(double x0, double xa, double xb, double ya, double yb) {
        double x = x0;
        if(x>xb)
            x=xb;
        if(x<xa)
            x=xa;
        double y = (ya + yb)*0.5;
        if(xb - xa == 0) 
            return y;
        
        y = ya*(xb - x)/(xb - xa) + yb*(x - xa)/(xb - xa);
        
        return y;
    }

    /**
    * 
     * @param Bf
    * @param alpha is the local angle in degrees
    * @param t time in ns
     * @param SecIdx
    * @param SlyrIdx slyr index (0...5)
    * @return the distance to the wire in cm
    */
    public double interpolateOnGrid(double Bf, double alpha, double t,  int SecIdx, int SlyrIdx) {
        
        double B = Math.abs(Bf);
        
        int binlowB  = this.getBIdx(B);
        int binhighB = binlowB + 1; 

        if(binhighB > TableLoader.maxBinIdxB) {
            binhighB = TableLoader.maxBinIdxB;
        }

        double B1 = TableLoader.BfieldValues[binlowB];
        double B2 = TableLoader.BfieldValues[binhighB];

         // for alpha ranges		
        int binlowAlpha  = this.getAlphaIdx(alpha);
        int binhighAlpha = binlowAlpha + 1;

        if(binhighAlpha > TableLoader.maxBinIdxAlpha) {
            binhighAlpha = TableLoader.maxBinIdxAlpha;
        }
        //if(binhighAlpha==binlowAlpha) {
        //    binlowAlpha=binhighAlpha-1;
        //}

        double alpha1 = this.getAlphaFromAlphaIdx(binlowAlpha);	 
        double alpha2 = this.getAlphaFromAlphaIdx(binhighAlpha);
        
        // interpolate in B:
        double f_B_alpha1_t1 = interpolateLinear(B*B, B1*B1, B2*B2, 
                    TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binlowB][binlowAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binlowAlpha)],
                    TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binhighB][binlowAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binhighB, binlowAlpha)]);
        double f_B_alpha2_t1 = interpolateLinear(B*B, B1*B1, B2*B2, 
                    TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binlowB][binhighAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binhighAlpha)],
                    TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binhighB][binhighAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binhighB, binhighAlpha)]);
        double f_B_alpha1_t2 = interpolateLinear(B*B, B1*B1, B2*B2, 
                    TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binlowB][binlowAlpha][this.getTimeNextIdx(t, SecIdx, SlyrIdx, binlowB, binlowAlpha)],
                    TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binhighB][binlowAlpha][this.getTimeNextIdx(t, SecIdx, SlyrIdx, binhighB, binlowAlpha)]);
        double f_B_alpha2_t2 = interpolateLinear(B*B, B1*B1, B2*B2, 
                    TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binlowB][binhighAlpha][this.getTimeNextIdx(t, SecIdx, SlyrIdx, binlowB, binhighAlpha)],
                    TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binhighB][binhighAlpha][this.getTimeNextIdx(t, SecIdx, SlyrIdx, binhighB, binhighAlpha)]);
         // interpolate in d for 2 values of alpha:		 
        double f_B_alpha1_t = interpolateLinear(t, this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binlowAlpha)*2., this.getTimeNextIdx(t, SecIdx, SlyrIdx, binhighB, binlowAlpha)*2., f_B_alpha1_t1, f_B_alpha1_t2);
        double f_B_alpha2_t = interpolateLinear(t, this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binhighAlpha)*2., this.getTimeNextIdx(t, SecIdx, SlyrIdx, binhighB, binhighAlpha)*2., f_B_alpha2_t1, f_B_alpha2_t2);
        //LOGGER.log(Level.FINE,  TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binlowB][binlowAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binlowAlpha)]);
        //LOGGER.log(Level.FINE, SlyrIdx+" binlowB "+binlowB+" binlowAlpha "+binlowAlpha+" t "+this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binlowAlpha)+" time "+t);
        //LOGGER.log(Level.FINE, TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binlowB][binhighAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binhighAlpha)]);
        //LOGGER.log(Level.FINE, SlyrIdx+" binlowB "+binlowB+" binhighAlpha "+binhighAlpha+" t "+this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binhighAlpha)+" time "+t);
        //LOGGER.log(Level.FINE, TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binhighB][binlowAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binhighB, binlowAlpha)]);
        //LOGGER.log(Level.FINE, SlyrIdx+" binhighB "+binhighB+" binlowAlpha "+binlowAlpha+" t "+this.getTimeIdx(t, SecIdx, SlyrIdx, binhighB, binlowAlpha)+" time "+t);
        //LOGGER.log(Level.FINE, TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binhighB][binhighAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binhighB, binhighAlpha)]);
        //LOGGER.log(Level.FINE, SlyrIdx+" binhighB "+binhighB+" binhighAlpha "+binhighAlpha+" t "+this.getTimeIdx(t, SecIdx, SlyrIdx, binhighB, binhighAlpha)+" time "+t);
        //LOGGER.log(Level.FINE, " f_B_alpha1_t1 "+f_B_alpha1_t1+" f_B_alpha2_t1 "+f_B_alpha2_t1
        //            +" f_B_alpha1_t2 "+f_B_alpha1_t2+" f_B_alpha2_t2 "+f_B_alpha2_t2
        //            +" f_B_alpha1_t "+f_B_alpha1_t+" f_B_alpha2_t "+f_B_alpha2_t);
        
        // interpolate in alpha: (cos30-cosA)
        double f_B_alpha_t = interpolateLinear(Math.cos(Math.toRadians(30.))-Math.cos(Math.toRadians(alpha)), 
                    Math.cos(Math.toRadians(30.))-Math.cos(Math.toRadians(alpha1)), 
                    Math.cos(Math.toRadians(30.))-Math.cos(Math.toRadians(alpha2)), f_B_alpha1_t, f_B_alpha2_t);
        
        return f_B_alpha_t;

    /*
        // interpolate in B:
        double f_B_alpha1_t = interpolateLinear(B*B, B1*B1, B2*B2, 
                    TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binlowB][binlowAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binlowAlpha)],
                    TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binhighB][binlowAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binhighB, binlowAlpha)]);
        double f_B_alpha2_t = interpolateLinear(B*B, B1*B1, B2*B2, 
                    TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binlowB][binhighAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binhighAlpha)],
                    TableLoader.DISTFROMTIME[SecIdx][SlyrIdx][binhighB][binhighAlpha][this.getTimeIdx(t, SecIdx, SlyrIdx, binhighB, binhighAlpha)]);
        
        // interpolate in alpha: (cos30-cosA)
        double f_B_alpha_t = interpolateLinear(Math.cos(Math.toRadians(30.))-Math.cos(Math.toRadians(alpha)), 
                    Math.cos(Math.toRadians(30.))-Math.cos(Math.toRadians(alpha1)), 
                    Math.cos(Math.toRadians(30.))-Math.cos(Math.toRadians(alpha2)), f_B_alpha1_t, f_B_alpha2_t);
        return f_B_alpha_t;
    */
    }

    /**
     * 
     * @param binAlpha alpha parameter bin
     * @return value of alpha from alpha bin
     */
    private double getAlphaFromAlphaIdx(int binAlpha) {
        double cos30minusalpha = Math.cos(Math.toRadians(30.)) + (double) (binAlpha)*(1. - Math.cos(Math.toRadians(30.)))/5.;
        double alpha =  -(Math.toDegrees(Math.acos(cos30minusalpha)) - 30);
        double alpha1 = 0;
        double alpha2 = 30.;
        if(alpha<alpha1) {
            alpha=alpha1;
        }
         if(alpha>alpha2) {
             alpha=alpha2;
        }	
        return alpha;
    }
    /**
     * 
     * @param t1 time value in ns
     * @param is sector index (0...5)
     * @param ir superlayer index (0...5)
     * @param ibfield bfield bin (0...7)
     * @param icosalpha cosalpha bin (0...5)
     * @return time bin
     */
    public int getTimeIdx(double t1, int is, int ir, int ibfield, int icosalpha) {
        
        DecimalFormat df = new DecimalFormat("#");
        df.setRoundingMode(RoundingMode.CEILING);
       
        int binIdx =0;
        try{
            binIdx = Integer.parseInt(df.format(t1/2.) ) -1; 
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, " time bin error "+t1+" ");
        }
        if(binIdx<0) {
            binIdx = TableLoader.minBinIdxT;
        }
        if(binIdx>TableLoader.maxTBin) {
            binIdx = TableLoader.maxTBin ;
        }

        return binIdx;
    }
    /**
     * 
     * @param b1 bfield value in T
     * @return B field bin
     */
    public int getBIdx(double b1) {
        
//        int binIdx = (int) ((1+b1)*2) -2;
//        if(binIdx<0) {
//            binIdx = TableLoader.minBinIdxB;
//        }
//        if(binIdx>TableLoader.maxBinIdxB) {
//            binIdx = TableLoader.maxBinIdxB;
//        }
        int maxBinIdxB = TableLoader.BfieldValues.length-1;
        DecimalFormat df = new DecimalFormat("#");
        df.setRoundingMode(RoundingMode.CEILING);
       
        int binIdx =0;
        try{
            binIdx = Integer.parseInt(df.format(b1*b1) ) -1; 
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, " field bin error "+b1+" ");
        }
        if(binIdx<0) {
            binIdx = 0;
        }
        if(binIdx>maxBinIdxB)
            binIdx = maxBinIdxB;
        return binIdx;
    }
    /**
     * 
     * @param alpha alpha parameter in deg
     * @return alpha bin
     */
    private int getAlphaIdx(double alpha) {
        double Ccos30minusalpha = Math.cos(Math.toRadians(30.-alpha) ) ; 
        double Cicosalpha = (Ccos30minusalpha - Math.cos(Math.toRadians(30.)))/((1. - Math.cos(Math.toRadians(30.)))/5.);
        int binIdx = (int)  Cicosalpha; 
        if(binIdx<0) {
            binIdx = TableLoader.minBinIdxAlpha;
        }
        if(binIdx>TableLoader.maxBinIdxAlpha) {
            binIdx = TableLoader.maxBinIdxAlpha;
        } 
        return binIdx;
    }

    private int getTimeNextIdx(double t, int SecIdx, int SlyrIdx, int binlowB, int binlowAlpha) {
        int binlowT = this.getTimeIdx(t, SecIdx, SlyrIdx, binlowB, binlowAlpha);  
        int binhighT = binlowT + 1; 

        if(binhighT>TableLoader.maxBinIdxT[SecIdx][SlyrIdx][binlowB][binlowAlpha]) {
            binhighT=TableLoader.maxBinIdxT[SecIdx][SlyrIdx][binlowB][binlowAlpha];
        }
        return binhighT;
    }
    
    /**
     * @param slyIdx superlayer index
     * @param time
     * @return test doca corr
     */
    public double addDOCACorr(double time, int slyIdx) {
        double dDoca = 0;
        if(slyIdx+1 == 5 || slyIdx+1 ==6) {
            if(time>600) {
                dDoca = 0.15;
            } else {
                dDoca = (7.6e-3 - 2.4e-4*time +9.8e-3*time*time - 3.8e-6*time*time*time)*5.5410595e-05;
            }
            //LOGGER.log(Level.FINE, "time "+time +" added doca "+(float)dDoca);
        }
        return dDoca;
    }
}
        

