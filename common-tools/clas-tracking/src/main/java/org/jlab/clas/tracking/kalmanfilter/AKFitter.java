package org.jlab.clas.tracking.kalmanfilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    public Map<Integer, HitOnTrack> trajPoints = new HashMap<>();
    
    public AKFitter(boolean filter, int iterations, int dir, Swim swim, Libr m) {
        this.filterOn           = filter;
        this.totNumIter         = iterations;
        this.dir                = dir;
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
     
    public double getResidualsCut() {
        return resiCut;
    }

    public void setResidualsCut(double resiCut) {
        this.resiCut = resiCut;
    }
    
    public void runFitterIter(AStateVecs sv, AMeasVecs mv) {
        this.numIter++;
//        System.out.println("\niter: " + numIter + " dir: " + dir);
        
        int k0 = 0;
        int kf = mv.measurements.size() - 1;
        if(dir<0) {
            k0 = mv.measurements.size() - 1;
            kf = 0;
        }
        
        //init for backward filtering
        if(numIter==1) {
            boolean init = this.initIter(sv, mv);
            if(!init) return;
        }
               
        //re-init state vector and cov mat for forward filtering
        this.initOneWayIter(sv, k0);
        
        // filter & transport in forward direction
        boolean forward = this.runOneWayFitterIter(sv, mv, k0, kf);
        if (!forward) return;
        
        //re-init state vector and cov mat for backward filtering
        this.initOneWayIter(sv, kf);
            
        // filter and transport in backward direction
        boolean backward = this.runOneWayFitterIter(sv, mv, kf, k0);
        if (!backward) return;

        // smoothing
        for (int k = kf; (k-k0)*dir >= 0; k -= dir) {         
//            System.out.println("before smooth:" + mv.dh(k, sv.trackTrajB.get(k)));sv.printlnStateVec(sv.trackTrajB.get(k));// sv.printMatrix(sv.trackTrajF.get(k).covMat);
            if(k==kf) 
                sv.smoothed().put(k, sv.filtered(true).get(k).clone());
            else if(k==k0)
                sv.smoothed().put(k, sv.filtered(false).get(k).clone());
            else {
//                System.out.println("averaging forward:" + mv.dh(k, sv.filtered(true).get(k)));sv.printlnStateVec(sv.filtered(true).get(k)); //mo.printMatrix(sv.filtered(true).get(k).covMat,"AAAA");
//                System.out.println("averaging backward:" + mv.dh(k, sv.transported(false).get(k)));sv.printlnStateVec(sv.transported(false).get(k)); //mo.printMatrix(sv.transported(false).get(k).covMat,"AAAA");
                sv.smoothed().put(k, this.smooth(sv.filtered(true).get(k), sv.transported(false).get(k)));
//                System.out.println("after smooth:" + mv.dh(k, sv.smoothed().get(k)));sv.printlnStateVec(sv.smoothed().get(k));           
                
            }
        }     
    }
    
    public boolean initIter(AStateVecs sv, AMeasVecs mv) {
        
        if(sv.transported(true).get(0)==null) return false;
        
        for (int k=0; k<mv.measurements.size()-1; k++) {
            sv.transported(true).put(k+1, sv.transport(sv.transported(true).get(k), k+1, mv, swimmer));
            if(sv.transported(true).get(k+1) == null) return false;
        }
        
        sv.lastSV = sv.transported(true).get(mv.measurements.size()-1).clone();
        
        return true;
    }
    
    public void initOneWayIter(AStateVecs sv, int i0) {
        boolean forward = (i0==0 && dir>0) || (i0!=0 && dir<0);
        if(sv.filtered(!forward).get(i0)!=null)
            sv.transported(forward).put(i0, sv.filtered(!forward).get(i0).clone());
        if(i0==0) sv.transported(forward).get(i0).copyCovMat(sv.initSV.covMat);
        else      sv.transported(forward).get(i0).copyCovMat(sv.lastSV.covMat);
    }
    
    public boolean runOneWayFitterIter(AStateVecs sv, AMeasVecs mv, int k0, int kf) {
        
        int dk = (int) Math.signum(kf-k0);
        
        boolean forward = dk*dir>0;
        
        for (int k = k0; (k-kf)*dk <= 0; k += dk) {
            if (sv.transported(forward).get(k)==null || mv.measurements.get(k) == null)
                return false;
            
//            System.out.println(k + " " + mv.measurements.get(k).surface.toString());
//            System.out.println("before filter:" + mv.dh(k, sv.transported(forward).get(k)));sv.printlnStateVec(sv.transported(forward).get(k));
            
            sv.filtered(forward).put(k, this.filter(k, sv.transported(forward).get(k), mv));            
            if(sv.filtered(forward).get(k) == null) 
                return false;
            
//            System.out.println("after filtering:" + mv.dh(k, sv.filtered(forward).get(k)));sv.printlnStateVec(sv.filtered(forward).get(k));  
            
            if(k != kf) {
                sv.transported(forward).put(k+dk, sv.transport(sv.filtered(forward).get(k), k+dk, mv, swimmer));
                if(sv.transported(forward).get(k+dk) == null) 
                    return false;
            
//                System.out.println("after transport:" + mv.dh(k+dk, sv.transported(forward).get(k+dk)));sv.printlnStateVec(sv.transported(forward).get(k+dk)); 
            }
        }   
        return true;
    }
    
    
    public abstract void runFitter(AStateVecs sv, AMeasVecs mv) ;
    
    public void setTrajectory(AStateVecs sv, AMeasVecs mv) {
        trajPoints = new HashMap<>();
        for (int k = 0; k < sv.smoothed().size(); k++) {
            int index   = mv.measurements.get(k).layer;
            int layer   = mv.measurements.get(k).surface.getLayer();
            int sector  = mv.measurements.get(k).surface.getSector();
            double tRes = mv.dh(k, sv.transported().get(k));
            double fRes = mv.dh(k, sv.filtered(false).get(k));
            double sRes = mv.dh(k, sv.smoothed().get(k));
            if(!mv.measurements.get(k).surface.passive) {
                trajPoints.put(index, new HitOnTrack(layer, sector, sv.transported().get(k), tRes, fRes, sRes));
                if(mv.measurements.get(k).skip)
                    trajPoints.get(index).isUsed = false;
            } else {
                trajPoints.put(index, new HitOnTrack(layer, sector, sv.transported().get(k), -999, -999, -999));
                trajPoints.get(index).isUsed = false;
            }            
        } 
    }
    
    
    public double calc_chi2(AStateVecs sv, AMeasVecs mv) {
        double chisq = 0;
        this.NDF = this.NDF0;

        int k0 = 0;
        int kf = mv.measurements.size()-1;
        if(dir<0) {
            k0 = mv.measurements.size()-1;
            kf = 0;
        }
         
        if(sv.smoothed().get(k0)!=null && sv.smoothed().get(kf)!=null) {
            // store trajectory in transport state-vector map
            sv.transported().put(0, sv.smoothed().get(0).clone());

            for(int k = 0; k< mv.measurements.size(); k++) {
                if(!mv.measurements.get(k).skip) {
                    double dh    = mv.dh(k, sv.smoothed().get(k));
                    double error = mv.measurements.get(k).error;
                    chisq += dh*dh / error/error;
                    this.NDF++;
//                    System.out.println(k + " " + mv.measurements.get(k).surface.getLayer() + " " + dh + " " + error + " " + chisq + " " + ndf);
                }
                if(k< mv.measurements.size()-1) {
                    sv.transport(k, k+1, mv, swimmer);
                    if(sv.transported().get(k+1)==null) {
                        chisq=Double.NaN;
                        break;
                    }
                }
            }  
        }
        if(chisq==0) chisq=Double.NaN;
        return chisq;
    }
    
    public abstract StateVec filter(int k, StateVec sv, AMeasVecs mv) ;
    
    public abstract StateVec smooth(int k, AStateVecs sv, AMeasVecs mv) ;

    public abstract StateVec smooth(StateVec v1, StateVec v2);
    
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
        public int sector;
        public double x;
        public double y;
        public double z;
        public double px;
        public double py;
        public double pz;
        public double path;
        public double dx;
        public double dE;
        public double residual;
        public double transportedResidual;
        public double filteredResidual;
        public double smoothedResidual;
        public boolean isUsed = true;
        
        public HitOnTrack(int layer, int sector, double x, double y, double z, 
                          double px, double py, double pz, double path, double dx,
                          double de, double tRes, double fRes, double sRes) {
            this.layer = layer;
            this.sector = sector;
            this.x = x;
            this.y = y;
            this.z = z;
            this.px = px;
            this.py = py;
            this.pz = pz;
            this.path = path;
            this.dx = dx;
            this.dE = de;
            this.residual = sRes;
            this.transportedResidual = tRes;
            this.filteredResidual = fRes;
            this.smoothedResidual = sRes;
        }
        
        public HitOnTrack(int layer, int sector, StateVec sv, double tRes, double fRes, double sRes) {
            this.layer = layer;
            this.sector = sector; 
            this.x = sv.x;
            this.y = sv.y;
            this.z = sv.z;
            this.px = sv.px;
            this.py = sv.py;
            this.pz = sv.pz;
            this.path = sv.path;
            this.dx = sv.dx;
            this.dE = sv.energyLoss;
            this.residual = sRes;
            this.transportedResidual = tRes;
            this.filteredResidual = fRes;
            this.smoothedResidual = sRes;
        }
    }
    
    public void printConfig() {
        System.out.println("Kalman Filter configuration:");
        System.out.println("- filter status " + this.filterOn);
        System.out.println("- number of iterations " + this.totNumIter);
        System.out.println("- matrix library " + this.mo.getMatrixLibrary().name());  
    }
}
