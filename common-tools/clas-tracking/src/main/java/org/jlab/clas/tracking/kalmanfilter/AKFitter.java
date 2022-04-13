package org.jlab.clas.tracking.kalmanfilter;

import java.util.List;

import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec;

import org.jlab.clas.tracking.utilities.MatrixOps.*;

public abstract class AKFitter {
    
    // control variables
    public static int polarity = -1; //RDV should check that everything works reversing this value    
    public boolean    filterOn = true;
    public int        totNumIter = 5;
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
    public int     dir = 1; 

    private Swim swimmer;
    private KFCovMatOps mo = new KFCovMatOps(Libr.EJML);
    
    
    public AKFitter(boolean filter, int iterations, int dir, boolean beamspot, Swim swim, Libr m) {
        this.filterOn           = filter;
        this.totNumIter         = iterations;
        this.dir                = dir;
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
    
    public void runFitterIter(AStateVecs sv, AMeasVecs mv, int dir) {
        this.numIter++;
        
        int k0 = 0;
        int kf = mv.measurements.size() - 1;
        if(dir<0) {
            k0 = mv.measurements.size() - 1;
            kf = 0;
        }
        
        //init for out to in fitting
        if(sv.trackTrajT.get(k0)==null && sv.trackTrajT.get(kf)!=null) {
            if (mv.measurements.get(k0) == null) 
                return;               
            sv.transport(kf, k0, mv, swimmer);
            sv.initSV.copy(sv.trackTrajT.get(k0));
        }
               
        //re-init cov mat
        if(this.numIter>1) {
            sv.trackTrajT.get(k0).copy(sv.trackTrajS.get(k0));
        }
        sv.trackTrajT.get(k0).copyCovMat(sv.initSV.covMat);                

        // filter & transport
        for (int k = k0; (k-kf)*dir <= 0; k += dir) {
            if (sv.trackTrajT.get(k)==null || mv.measurements.get(k) == null) {
                return;
            }
//            System.out.println(k);
//            System.out.println("before filter:" + mv.dh(k+1, sv.trackTrajT.get(k)));sv.printlnStateVec(sv.trackTrajT.get(k));// sv.printMatrix(sv.trackTrajF.get(k).covMat);
            if(k>1 || this.beamSpotConstraint)
                this.filter(k, sv, mv);
            else
                sv.trackTrajF.put(k, sv.new StateVec(sv.trackTrajT.get(k)));
//            System.out.println("after filtering:" + mv.dh(k+dir, sv.trackTrajF.get(k)));sv.printlnStateVec(sv.trackTrajF.get(k)); //sv.printMatrix(sv.trackTrajF.get(k+1).covMat); 
            if(k != kf) {
                sv.transportFiltered(k, k + dir, mv, swimmer);
//                System.out.println("after transport:" + mv.dh(k+dir, sv.trackTrajT.get(k+dir)));sv.printlnStateVec(sv.trackTrajT.get(k+dir)); //sv.printMatrix(sv.trackTrajT.get(k+1).covMat);  
            }
        }
        
        for (int k = kf; (k-k0)*dir > 0; k -= dir) {
            if (k == kf && sv.trackTrajF.get(k) != null) {
                sv.trackTrajS.put(k, sv.new StateVec(sv.trackTrajF.get(k)));
            }
            if (sv.trackTrajS.get(k) == null || sv.trackTrajF.get(k-dir) == null) {
                return;
            }
            this.smooth(k - dir, sv, mv);
        }
    }
    
    public abstract void runFitter(AStateVecs sv, AMeasVecs mv) ;
    
    public abstract void setTrajectory(AStateVecs sv, AMeasVecs mv) ;
    
    
    public double calc_chi2(AStateVecs sv, AMeasVecs mv) {
        double chisq = 0;
        int ndf = this.NDF0;

        int k0 = 0;
        int kf = mv.measurements.size()-1;
        if(dir<0) {
            k0 = mv.measurements.size()-1;
            kf = 0;
        }
        
        if(sv.trackTrajS.get(k0)!=null) {
            // store trajectory in transported state-vector map
            sv.trackTrajT.put(k0, sv.new StateVec(sv.trackTrajS.get(k0)));

            for(int k = k0; (k-kf)*dir<=0; k+=dir) {
                StateVec iVec = sv.trackTrajT.get(k);
                if(!mv.measurements.get(k).skip) {
                    double dh    = mv.dh(k, iVec);
                    double error = mv.measurements.get(k).error;
                    chisq += dh*dh / error/error;
                    ndf++;
//                    System.out.println(k + " " + mv.measurements.get(k).surface.getLayer() + " " + dh + " " + error + " " + chisq + " " + ndf);
                }
                if((k-kf)*dir<0) {
                    sv.transport(k, k+dir, mv, swimmer);
                    if(sv.trackTrajT.get(k+dir)==null) {
                        chisq=Double.NaN;
                        break;
                    }
                }
            }  
        }
        if(chisq==0) chisq=Double.NaN;
        return chisq;
    }
    
    public abstract void filter(int k, AStateVecs sv, AMeasVecs mv) ;
    
    public abstract void smooth(int k, AStateVecs sv, AMeasVecs mv) ;

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
