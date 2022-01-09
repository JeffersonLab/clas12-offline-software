package org.jlab.clas.tracking.kalmanfilter;

import java.util.List;

import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec;

import org.jlab.clas.tracking.utilities.MatrixOps.*;

public abstract class AKFitter {
    
    // control variables
    public static int polarity = -1; //RDV should check that everything works reversing this value    
    public boolean    filterOn = true;
    public int        totNumIter = 1;
    public boolean    beamSpotConstraint = false;
    
    // parameters
    private double _Xb; //beam axis pars
    private double _Yb;
    private double resiCut = 100;//residual cut for the measurements

    // return variables
    public boolean setFitFailed = false;
    public double  chi2;
    public int     NDF;
    public int     NDF0;
    public int     numIter;
    
    private Swim swimmer;
    private KFCovMatOps mo = new KFCovMatOps(Libr.EJML);
    
    
    public AKFitter(boolean filter, int iterations, boolean beamspot, Swim swim, Libr m) {
        this.filterOn           = filter;
        this.totNumIter         = iterations;
        this.beamSpotConstraint = beamspot;
        this.swimmer            = swim;
        this.setMatrixLibrary(m);
    }
    
    public final void setMatrixLibrary(Libr ml) {
        mo = new KFCovMatOps(ml);
    }
    
    public KFCovMatOps getMatrixOps() {
        return mo;
    }
    
    public void setMeasurements(List<Surface> measSurfaces, AMeasVecs mv) {
        mv.setMeasVecs(measSurfaces);
    }

    public Swim getSwimmer() {
        return swimmer;
    }

    public void setSwimmer(Swim swimmer) {
        this.swimmer = swimmer;
    }
     
    /**
     * @return the resiCut
     */
    public double getResiCut() {
        return resiCut;
    }

    /**
     * @param resiCut the resiCut to set
     */
    public void setResiCut(double resiCut) {
        this.resiCut = resiCut;
    }
    
    public void runFitterIter(AStateVecs sv, AMeasVecs mv) {
        this.numIter++;
        for (int k = 0; k < mv.measurements.size() - 1; k++) {
//            System.out.println(k);
            if (sv.trackTraj.get(k)==null || mv.measurements.get(k + 1) == null) {
                return;
            }
            if(k==0) sv.trackTraj.get(0).copyCovMat(sv.initSV.covMat);
            
//            System.out.println("before transport");sv.printlnStateVec(sv.trackTraj.get(k));
            sv.transport(k, k + 1, mv, swimmer);
//            System.out.println("after transport:" + mv.dh(k+1, sv.trackTraj.get(k+1)));sv.printlnStateVec(sv.trackTraj.get(k+1));            
            this.filter(k + 1, sv, mv);
//            System.out.println("after filtering:" + mv.dh(k+1, sv.trackTraj.get(k+1)));sv.printlnStateVec(sv.trackTraj.get(k+1));            
        }
                                
        for (int k =  mv.measurements.size() - 1; k>0 ;k--) {
            if (sv.trackTraj.get(k)==null || mv.measurements.get(k - 1) == null) {
                return;
            }
//            System.out.println("before transport");sv.printlnStateVec(sv.trackTraj.get(k));
            sv.transport(k, k - 1, mv, swimmer);
//            System.out.println("after transport:" + mv.dh(k-1, sv.trackTraj.get(k-1)));sv.printlnStateVec(sv.trackTraj.get(k-1));            
            if(k>1 || this.beamSpotConstraint)
               this.filter(k - 1, sv, mv);
//            System.out.println("after filtering:" + mv.dh(k-1, sv.trackTraj.get(k-1)));sv.printlnStateVec(sv.trackTraj.get(k-1));            
        }        
    }
    
    public abstract void runFitter(AStateVecs sv, AMeasVecs mv) ;
    
    public abstract void setTrajectory(AStateVecs sv, AMeasVecs mv) ;
    
    
    public double calc_chi2(AStateVecs sv, AMeasVecs mv) {
        double chisq = 0;
        int ndf = this.NDF0;

        for(int k = 0; k< mv.measurements.size()-1; k++) {
            if(mv.measurements.get(k+1).skip==false && sv.trackTraj.get(k)!=null) {
                sv.transport(k, k+1, mv, swimmer);
                double dh    = mv.dh(k+1, sv.trackTraj.get(k+1));
                double error = mv.measurements.get(k+1).error;
                chisq += dh*dh / error/error;
                ndf++;
            }
        }  
        if(chisq==0) chisq=Double.NaN;
        return chisq;
    }
    
    public abstract void filter(int k, AStateVecs sv, AMeasVecs mv) ;

    /**
     * @return the _Xb
     */
    public double getXb() {
        return _Xb;
    }

    /**
     * @param _Xb the _Xb to set
     */
    public void setXb(double _Xb) {
        this._Xb = _Xb;
    }

    /**
     * @return the _Yb
     */
    public double getYb() {
        return _Yb;
    }

    /**
     * @param _Yb the _Yb to set
     */
    public void setYb(double _Yb) {
        this._Yb = _Yb;
    }
    
    // RDV why not just using state vectors?
    public class HitOnTrack {

        public int layer;
        public double x;
        public double y;
        public double z;
        public double px;
        public double py;
        public double pz;
        public double resi;
        public boolean isMeasUsed = true;
        
        public HitOnTrack(int layer, double x, double y, double z, double px, double py, double pz, double resi) {
            this.layer = layer;
            this.x = x;
            this.y = y;
            this.z = z;
            this.px = px;
            this.py = py;
            this.pz = pz;
            this.resi = resi;
        }
        
        public HitOnTrack(int layer, StateVec sv, double resi) {
            this.layer = layer;
            this.x = sv.x;
            this.y = sv.y;
            this.z = sv.z;
            this.px = sv.px;
            this.py = sv.py;
            this.pz = sv.pz;
            this.resi = resi;
        }
    }
    
    public void printConfig() {
        System.out.println("Kalman Filter configuration:");
        System.out.println("- filter status " + this.filterOn);
        System.out.println("- beam spot constraint " + this.beamSpotConstraint);
        System.out.println("- number of iterations " + this.totNumIter);
        System.out.println("- matrix library " + this.mo.getMatrixLibrary().name());  
    }
}
