package org.jlab.rec.dc.track.fit;

import Jama.Matrix;
import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.fit.StateVecs.CovMat;
import org.jlab.rec.dc.track.fit.StateVecs.StateVec;

public class KFitter {

    public boolean setFitFailed = false;

    StateVecs sv = new StateVecs();
    MeasVecs mv = new MeasVecs();

    public StateVec finalStateVec;
    public CovMat finalCovMat;
    public List<org.jlab.rec.dc.trajectory.StateVec> kfStateVecsAlongTrajectory;
    public int totNumIter = 30;
    double newChisq = Double.POSITIVE_INFINITY;

    public double chi2 = 0;
    public double chi2kf = 0;
    public int NDF = 0;
    public int ConvStatus = 1;
    
    public KFitter(Track trk, DCGeant4Factory DcDetector, boolean TimeBasedUsingHBtrack) { 
        if(TimeBasedUsingHBtrack==true) {
            this.initFromHB(trk, DcDetector); 
        } else {
            this.init(trk, DcDetector);
        }
    }
    public void initFromHB(Track trk, DCGeant4Factory DcDetector) {
        mv.setMeasVecsFromHB(trk, DcDetector);
        sv.Z = new double[mv.measurements.size()];

        for (int i = 0; i < mv.measurements.size(); i++) {
            sv.Z[i] = mv.measurements.get(i).z; 
        } 
       // if(sv.Z.length<20) { // quit no enough measurements
        //    totNumIter=0;
        //    this.setFitFailed=true;
        //    return;
        //}
        sv.initFromHB(trk, sv.Z[0], this);
    }
    public void init(Track trk, DCGeant4Factory DcDetector) {
        mv.setMeasVecs(trk, DcDetector);
        sv.Z = new double[mv.measurements.size()];

        for (int i = 0; i < mv.measurements.size(); i++) {
            sv.Z[i] = mv.measurements.get(i).z;
        }
        sv.init(trk, sv.Z[0], this);
    }
    public boolean useFilter =true;
    public void runFitter() {
        this.chi2 = 0;
        this.NDF = mv.ndf;

        for (int i = 1; i <= totNumIter; i++) {
            this.chi2kf = 0;
            if (i > 1) {
                sv.transport(sv.Z.length - 1, 0, sv.trackTraj.get(sv.Z.length - 1), sv.trackCov.get(sv.Z.length - 1)); //get new state vec at 1st measurement after propagating back from the last filtered state
            }
            for (int k = 0; k < sv.Z.length - 1; k++) {
                sv.transport(k, k + 1, sv.trackTraj.get(k), sv.trackCov.get(k));
                //sv.trackTraj.add(k+1, sv.StateVec); 
                //sv.trackCov.add(k+1, sv.CovMat);
                // System.out.println((k)+"] trans "+sv.trackTraj.get(k).x+","+sv.trackTraj.get(k).y+","+
                //		sv.trackTraj.get(k).z+","+sv.trackTraj.get(k).tx+","+sv.trackTraj.get(k).ty+" "+1./sv.trackTraj.get(k).Q); 
                if(useFilter)
                    this.filter(k + 1);
            }
            if(i>1) {
                //this.calcFinalChisq();
                //if(this.chi2>1000000) {
                //    i = totNumIter;
                //    this.setFitFailed=true;
                //}
                
                double deltaChi2 = Math.abs(this.chi2kf - newChisq);
                if (this.chi2kf < newChisq) {
                    this.finalStateVec = sv.trackTraj.get(sv.Z.length - 1);
                    this.finalCovMat = sv.trackCov.get(sv.Z.length - 1);
                    //System.out.println("newChisq "+newChisq+" this.chi2 "+this.chi2kf+" iter "+i);
                    
                    //if(deltaChi2<0.5)
                    //    i = totNumIter;
                    //if(deltaChi2<0.01) 
                      //  i = totNumIter;
                    
                     if(deltaChi2<0.01 ) {
                        this.ConvStatus=0;
                       // i = totNumIter;
                     }
                     
                    newChisq = this.chi2kf; 
                } else { 
                    this.ConvStatus=1;
                }
            }
        }
        this.calcFinalChisq();

    }

    private void filter(int k) {
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null && k < sv.Z.length) {
            double[] K = new double[5];
            double V = Math.abs(mv.measurements.get(k).unc);
            double[] H = mv.H(sv.trackTraj.get(k).y, mv.measurements.get(k).tilt, mv.measurements.get(k).wireMaxSag, mv.measurements.get(k).wireLen);

            double[][] HTGH = new double[][]{
                {H[0] * H[0] / V, H[0] * H[1] / V, 0, 0, 0},
                {H[0] * H[1] / V, H[1] * H[1] / V, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0}
            };

            Matrix Ci = null;

            if (this.isNonsingular(sv.trackCov.get(k).covMat) == false) {
                //System.out.println("Covariance Matrix is non-invertible - quit filter!");
                return;
            }
            try {
                Ci = sv.trackCov.get(k).covMat.inverse();
            } catch (Exception e) {
                return;
            }

            Matrix Ca = null;
            try {
                Ca = Ci.plus(new Matrix(HTGH));
            } catch (Exception e) {
                return;
            }
            if (Ca != null && this.isNonsingular(Ca) == false) {
                //System.out.println("Covariance Matrix is non-invertible - quit filter!");
                return;
            }
            if (Ca != null) {
                if (Ca.inverse() != null) {
                    Matrix CaInv = Ca.inverse();
                    sv.trackCov.get(k).covMat = CaInv;
                    //System.err.println("Error: e");
                } else {
                    return;
                }
            } else {
                return;
            }

            for (int j = 0; j < 5; j++) {
                // the gain matrix
                K[j] = (H[0] * sv.trackCov.get(k).covMat.get(j, 0) + H[1] * sv.trackCov.get(k).covMat.get(j, 1)) / V;
            }

            double h = mv.h(new double[]{sv.trackTraj.get(k).x, sv.trackTraj.get(k).y}, 
                    (int) mv.measurements.get(k).tilt, mv.measurements.get(k).wireMaxSag, mv.measurements.get(k).wireLen);
            //double c2 = ((1 - (H[0]*K[0] + H[1]*K[1]))*(1 - (H[0]*K[0] + H[1]*K[1]))*(mv.measurements.get(k).x - h)*(mv.measurements.get(k).x - h)/V);
            double c2 = ((mv.measurements.get(k).x - h)*(mv.measurements.get(k).x - h)/V);

            double x_filt = sv.trackTraj.get(k).x + K[0] * (mv.measurements.get(k).x - h);
            double y_filt = sv.trackTraj.get(k).y + K[1] * (mv.measurements.get(k).x - h);
            double tx_filt = sv.trackTraj.get(k).tx + K[2] * (mv.measurements.get(k).x - h);
            double ty_filt = sv.trackTraj.get(k).ty + K[3] * (mv.measurements.get(k).x - h);
            double Q_filt = sv.trackTraj.get(k).Q + K[4] * (mv.measurements.get(k).x - h);

            chi2kf += c2; 
            //System.out.println("KFchi2 "+chi2);
            //if(chi2<10) { 
            sv.trackTraj.get(k).x = x_filt;
            sv.trackTraj.get(k).y = y_filt;
            sv.trackTraj.get(k).tx = tx_filt;
            sv.trackTraj.get(k).ty = ty_filt;
            sv.trackTraj.get(k).Q = Q_filt;
            //}

        }
    }

    @SuppressWarnings("unused")
	private void smooth(int k) {
        this.chi2 = 0;
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null) {
            sv.transport(k, 0, sv.trackTraj.get(k), sv.trackCov.get(k));
            for (int k1 = 0; k1 < k; k1++) {
                sv.transport(k1, k1 + 1, sv.trackTraj.get(k1), sv.trackCov.get(k1));
                this.filter(k1 + 1);
            }
        }
    }

    private void calcFinalChisq() {
        int k = sv.Z.length - 1;
        this.chi2 = 0;
        double path =0;
        kfStateVecsAlongTrajectory = new ArrayList<org.jlab.rec.dc.trajectory.StateVec>(); 
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null) {
            sv.rinit(sv.Z[0], k);
            org.jlab.rec.dc.trajectory.StateVec svc = new org.jlab.rec.dc.trajectory.StateVec(sv.trackTraj.get(0).x, sv.trackTraj.get(0).y, sv.trackTraj.get(0).tx, sv.trackTraj.get(0).ty);
            svc.setZ(sv.trackTraj.get(0).z);
            svc.setB(sv.trackTraj.get(0).B);
            path+=sv.trackTraj.get(0).deltaPath;
            svc.setPathLength(path);
            double h0 = mv.h(new double[]{sv.trackTraj.get(0).x, sv.trackTraj.get(0).y}, 
                        (int) mv.measurements.get(0).tilt,  mv.measurements.get(0).wireMaxSag,  mv.measurements.get(0).wireLen);
            svc.setProjector(h0);
            kfStateVecsAlongTrajectory.add(svc);
            chi2 += (mv.measurements.get(0).x - h0) * (mv.measurements.get(0).x - h0) / mv.measurements.get(0).error;
            for (int k1 = 0; k1 < k; k1++) {
                sv.transport(k1, k1 + 1, sv.trackTraj.get(k1), sv.trackCov.get(k1));
                
                double V = mv.measurements.get(k1 + 1).error; 
                double h = mv.h(new double[]{sv.trackTraj.get(k1 + 1).x, sv.trackTraj.get(k1 + 1).y}, 
                        (int) mv.measurements.get(k1 + 1).tilt,  mv.measurements.get(k1 + 1).wireMaxSag,  mv.measurements.get(k1 + 1).wireLen);
//System.out.println("KF "+mv.measurements.get(k1 + 1).z+" meas --> "+mv.measurements.get(k1 + 1).x+" h "+h+" state x "+sv.trackTraj.get(k1 + 1).x+" y "+ sv.trackTraj.get(k1 + 1).y);
                svc = new org.jlab.rec.dc.trajectory.StateVec(sv.trackTraj.get(k1 + 1).x, sv.trackTraj.get(k1 + 1).y, sv.trackTraj.get(k1 + 1).tx, sv.trackTraj.get(k1 + 1).ty);
                svc.setZ(sv.trackTraj.get(k1 + 1).z);
                svc.setB(sv.trackTraj.get(k1 + 1).B);
                path+=sv.trackTraj.get(k1 + 1).deltaPath;
                svc.setPathLength(path);
                svc.setProjector(h);
                kfStateVecsAlongTrajectory.add(svc);
                chi2 += (mv.measurements.get(k1 + 1).x - h) * (mv.measurements.get(k1 + 1).x - h) / V;
            }
        }
    }

    /**
     * prints the matrix -- used for debugging
     *
     * @param C matrix
     */
    public void printMatrix(Matrix C) {
        for (int k = 0; k < 5; k++) {
        }
    }

    private boolean isNonsingular(Matrix mat) {
        double matDet = mat.det();
        return Math.abs(matDet) >= 1.e-30;
    }
    
}
