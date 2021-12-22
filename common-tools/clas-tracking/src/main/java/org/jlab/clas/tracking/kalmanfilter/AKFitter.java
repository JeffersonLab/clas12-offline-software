package org.jlab.clas.tracking.kalmanfilter;

import java.util.List;

import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec;

import org.jlab.clas.tracking.utilities.MatrixOps.*;

public abstract class AKFitter {
    
    public static int polarity = -1;
    public boolean setFitFailed = true;

    // AStateVecs sv 
    // AMeasVecs mv 
    public boolean filterOn = true;
    private double _tarShift; //targetshift
    private double _Xb; //beam axis pars
    private double _Yb;
    private double resiCut = 100;//residual cut for the measurements

    public KFCovMatOps mo = new KFCovMatOps(Libr.EJML);
    
    public void setMatrixLibrary(Libr ml) {
        mo = new KFCovMatOps(ml);
    }
    
    public Libr getMatrixLibrary() {
        return mo.getMatrixLibrary();
    }
    
    public void setMeasurements(List<Surface> measSurfaces, AMeasVecs mv) {
        mv.setMeasVecs(measSurfaces);
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

    public int totNumIter = 5;
                    
    public void runFitterIter(Swim swimmer, int it, AStateVecs sv, AMeasVecs mv) {
        for (int k = 0; k < mv.measurements.size() - 1; k++) {
                if (sv.trackCov.get(k) == null || mv.measurements.get(k + 1) == null) {
                    return;
                } 
                sv.transport(k, k + 1, sv.trackTraj.get(k), sv.trackCov.get(k), mv.measurements.get(k+1), 
                        swimmer); 
                this.filter(k + 1, swimmer, 1, sv, mv); 
            }
            
        for (int k =  mv.measurements.size() - 1; k>0 ;k--) {
            if (sv.trackCov.get(k) == null || mv.measurements.get(k - 1) == null) {
                return;
            }
            sv.transport(k, k - 1, sv.trackTraj.get(k), sv.trackCov.get(k), mv.measurements.get(k-1), 
                    swimmer);
            if(k>1)
               this.filter(k - 1, swimmer, -1, sv, mv);
        }
    }
    public abstract void runFitter(Swim swimmer, AStateVecs sv, AMeasVecs mv) ;
    
    public abstract void setTrajectory(AStateVecs sv, AMeasVecs mv) ;
    
    
    public double chi2 = 0;
    public int NDF = 0;

    public double calc_chi2(Swim swimmer, AStateVecs sv, AMeasVecs mv) {
        //get the measurement
        double m = 0;
        //get the projector state
        double h = 0;
        double chi2 =0;
        m=0;
        h=0;
        int ndf = -5;
        StateVec stv = sv.transported(0, 1, sv.trackTraj.get(0), mv.measurements.get(1), swimmer);
        double dh = mv.dh(1, stv);
        if(mv.measurements.get(1).skip==false) { 
            chi2 = dh*dh / mv.measurements.get(1).error;
            ndf++;
        }
        for(int k = 1; k< sv.Layer.size()-1; k++) {
            if(mv.measurements.get(k+1).skip==false) {
                stv = sv.transported(k, k+1, stv, mv.measurements.get(k+1), swimmer);
                dh = mv.dh(k+1, stv);
                chi2 += dh*dh / mv.measurements.get(k+1).error;
                ndf++;
            }
        }  
        return chi2;
    }
    
    public abstract void filter(int k, Swim swimmer, int dir, AStateVecs sv, AMeasVecs mv) ;

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

    /**
     * @return the _tarShift
     */
    public double getTarShift() {
        return _tarShift;
    }

    /**
     * @param _TarShift the _tarShift to set
     */
    public void setTarShift(double _TarShift) {
        this._tarShift = _TarShift;
    }

    /**
     * prints the matrix -- used for debugging
     *
     * @param C matrix
     */
    public void printMatrix(double[][] C, String s) {
        System.out.println("    "+s);
        for (int k = 0; k < 5; k++) {
            System.out.println(C[k][0]+"	"+C[k][1]+"	"+C[k][2]+"	"+C[k][3]+"	"+C[k][4]);
        }
        System.out.println("    ");
    }

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
    }

}
