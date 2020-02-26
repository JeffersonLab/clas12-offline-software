package org.jlab.rec.dc.track.fit;

import Jama.Matrix;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.fit.StateVecs.CovMat;
import org.jlab.rec.dc.track.fit.StateVecs.StateVec;


/**
 * @author ziegler
 * @since 08.08.2018 modified by gurjyan
 */
public class KFitter {

    public boolean setFitFailed = false;

    private StateVecs sv;
    private MeasVecs mv = new MeasVecs();

    public StateVec finalStateVec;
    public CovMat finalCovMat;
    public List<org.jlab.rec.dc.trajectory.StateVec> kfStateVecsAlongTrajectory;
    public int totNumIter = 30;
    private double newChisq = Double.POSITIVE_INFINITY;

    public double chi2 = 0;
    private double chi2kf = 0;
    public int NDF = 0;
    public int ConvStatus = 1;

    public KFitter(Track trk, DCGeant4Factory DcDetector,
                   boolean TimeBasedUsingHBtrack,
                   Swim swimmer) {
        sv = new StateVecs(swimmer);
        if (TimeBasedUsingHBtrack) {
            this.initFromHB(trk, DcDetector);
        } else {
            this.init(trk, DcDetector);
        }
    }

    private void initFromHB(Track trk, DCGeant4Factory DcDetector) {
        mv.setMeasVecsFromHB(trk, DcDetector);
        int mSize = mv.measurements.size();
        sv.Z = new double[mSize];

        for (int i = 0; i < mSize; i++) {
            sv.Z[i] = mv.measurements.get(i).z;
        }
        sv.initFromHB(trk, sv.Z[0], this);
    }

    public void init(Track trk, DCGeant4Factory DcDetector) {
        mv.setMeasVecs(trk, DcDetector);
        int mSize = mv.measurements.size();

        sv.Z = new double[mSize];

        for (int i = 0; i < mSize; i++) {
            sv.Z[i] = mv.measurements.get(i).z;
        }
        sv.init(trk, sv.Z[0], this);
    }
    
    public void runFitter(int sector) {
        this.chi2 = 0;
        this.NDF = mv.ndf;
        int svzLength = sv.Z.length;

//        IntStream.range(1,totNumIter ).parallel().forEach(i -> {
        for (int i = 1; i <= totNumIter; i++) {
            
            this.chi2kf = 0;
            if (i > 1) {
                //get new state vec at 1st measurement after propagating back from the last filtered state
                /*sv.transport(sector,
                        svzLength - 1,
                        0,
                        sv.trackTraj.get(svzLength - 1),
                        sv.trackCov.get(svzLength- 1)); */
                for (int k = svzLength - 1; k >0; k--) {
                    //if(i==2 && this.totNumIter==30)
                    //System.out.println("sector " +sector+"stateVec "+sv.trackTraj.get(k).printInfo());
                    sv.transport(sector, k, k - 1,
                        sv.trackTraj.get(k),
                        sv.trackCov.get(k));
                    this.filter(k - 1);
                }
            }
            for (int k = 0; k < svzLength - 1; k++) {
                //if(i==2 && this.totNumIter==30)
                //System.out.println("stateVec "+sv.trackTraj.get(k).printInfo());
                sv.transport(sector, k, k + 1,
                        sv.trackTraj.get(k),
                        sv.trackCov.get(k));
                    this.filter(k + 1);
            }
            if (i > 1) {
                if(this.setFitFailed==true)
                    i = totNumIter;
                if (this.chi2kf < newChisq) {
                    if(this.finalStateVec!=null) {
                        if(Math.abs(sv.trackTraj.get(svzLength - 1).Q-this.finalStateVec.Q)<5.e-4 &&
                                Math.abs(sv.trackTraj.get(svzLength - 1).x-this.finalStateVec.x)<1.e-4 &&
                                Math.abs(sv.trackTraj.get(svzLength - 1).y-this.finalStateVec.y)<1.e-4 &&
                                Math.abs(sv.trackTraj.get(svzLength - 1).tx-this.finalStateVec.tx)<1.e-6 &&
                                Math.abs(sv.trackTraj.get(svzLength - 1).ty-this.finalStateVec.ty)<1.e-6) {
                            i = totNumIter;
                        }
                    }
                    this.finalStateVec = sv.trackTraj.get(svzLength - 1);
                    this.finalCovMat = sv.trackCov.get(svzLength - 1);

//                    if (deltaChi2 < 0.001) {
//                        this.ConvStatus = 0;
//                        i = totNumIter;
//                    }

                    newChisq = this.chi2kf;
                } else {
                    this.ConvStatus = 1;
                }
            }
        }
//        });
        if(totNumIter==1) {
            this.finalStateVec = sv.trackTraj.get(svzLength - 1);
            this.finalCovMat = sv.trackCov.get(svzLength - 1);
        }
        this.calcFinalChisq(sector);

    }

    private void filter(int k) {
        if (sv.trackTraj.get(k) != null &&
                sv.trackCov.get(k).covMat != null &&
                k < sv.Z.length 
                && mv.measurements.get(k).reject==false) {
            double[] K = new double[5];
            double V = Math.abs(mv.measurements.get(k).unc);
            double[] H = mv.H(sv.trackTraj.get(k).y,
                    mv.measurements.get(k).tilt,
                    mv.measurements.get(k).wireMaxSag,
                    mv.measurements.get(k).wireLen);

            double[][] HTGH = new double[][]{
                    {H[0] * H[0] / V, H[0] * H[1] / V, 0, 0, 0},
                    {H[0] * H[1] / V, H[1] * H[1] / V, 0, 0, 0},
                    {0, 0, 0, 0, 0},
                    {0, 0, 0, 0, 0},
                    {0, 0, 0, 0, 0}
            };

            Matrix Ci;

            if (!this.isNonsingular(sv.trackCov.get(k).covMat)) {
                return;
            }
            try {
                Ci = sv.trackCov.get(k).covMat.inverse();
            } catch (Exception e) {
                return;
            }

            Matrix Ca;
            try {
                Ca = Ci.plus(new Matrix(HTGH));
            } catch (Exception e) {
                return;
            }
            if (Ca != null && !this.isNonsingular(Ca)) {
                return;
            }
            if (Ca != null) {
                Matrix CaInv = Ca.inverse();
                if (CaInv != null) {
                    sv.trackCov.get(k).covMat = CaInv;
                } else {
                    return;
                }
            } else {
                return;
            }

            for (int j = 0; j < 5; j++) {
                // the gain matrix
                K[j] = (H[0] * sv.trackCov.get(k).covMat.get(j, 0) +
                        H[1] * sv.trackCov.get(k).covMat.get(j, 1)) / V;
            }

            double h = mv.h(new double[]{sv.trackTraj.get(k).x, sv.trackTraj.get(k).y},
                    mv.measurements.get(k).tilt,
                    mv.measurements.get(k).wireMaxSag,
                    mv.measurements.get(k).wireLen);
            double c2 = ((mv.measurements.get(k).x - h) * (mv.measurements.get(k).x - h) / V);

            double x_filt = sv.trackTraj.get(k).x + K[0] * (mv.measurements.get(k).x - h);
            double y_filt = sv.trackTraj.get(k).y + K[1] * (mv.measurements.get(k).x - h);
            double tx_filt = sv.trackTraj.get(k).tx + K[2] * (mv.measurements.get(k).x - h);
            double ty_filt = sv.trackTraj.get(k).ty + K[3] * (mv.measurements.get(k).x - h);
            double Q_filt = sv.trackTraj.get(k).Q + K[4] * (mv.measurements.get(k).x - h);

            
            chi2kf += c2;
            sv.trackTraj.get(k).x = x_filt;
            sv.trackTraj.get(k).y = y_filt;
            sv.trackTraj.get(k).tx = tx_filt;
            sv.trackTraj.get(k).ty = ty_filt;
            sv.trackTraj.get(k).Q = Q_filt;

        }
    }

    @SuppressWarnings("unused")
    private void smooth(int sector, int k) {
        this.chi2 = 0;
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null) {
            sv.transport(sector, k, 0, sv.trackTraj.get(k), sv.trackCov.get(k));
            for (int k1 = 0; k1 < k; k1++) {
                sv.transport(sector, k1, k1 + 1, sv.trackTraj.get(k1), sv.trackCov.get(k1));
                this.filter(k1 + 1);
            }
        }
    }

    private void calcFinalChisq(int sector) {
        int k = sv.Z.length - 1;
        this.chi2 = 0;
        double path = 0;
        double[] nRj = new double[3];
        
        kfStateVecsAlongTrajectory = new ArrayList<>();
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null) {
            sv.transport(sector, sv.Z.length - 1, 0,
                    sv.trackTraj.get(sv.Z.length - 1),
                    sv.trackCov.get(sv.Z.length - 1));
            org.jlab.rec.dc.trajectory.StateVec svc =
                    new org.jlab.rec.dc.trajectory.StateVec(sv.trackTraj.get(0).x,
                            sv.trackTraj.get(0).y,
                            sv.trackTraj.get(0).tx,
                            sv.trackTraj.get(0).ty);
            svc.setZ(sv.trackTraj.get(0).z);
            svc.setB(sv.trackTraj.get(0).B);
            path += sv.trackTraj.get(0).deltaPath;
            svc.setPathLength(path);
            double h0 = mv.h(new double[]{sv.trackTraj.get(0).x, sv.trackTraj.get(0).y},
                    mv.measurements.get(0).tilt,
                    mv.measurements.get(0).wireMaxSag,
                    mv.measurements.get(0).wireLen);
            svc.setProjector(h0);
            kfStateVecsAlongTrajectory.add(svc); 
            double res = (mv.measurements.get(0).x - h0);
            chi2 += (mv.measurements.get(0).x - h0) * (mv.measurements.get(0).x - h0) / mv.measurements.get(0).error;
            nRj[mv.measurements.get(0).region-1]+=res*res/mv.measurements.get(0).error;
            
            for (int k1 = 0; k1 < k; k1++) {
                sv.transport(sector, k1, k1 + 1, sv.trackTraj.get(k1), sv.trackCov.get(k1));

                double V = mv.measurements.get(k1 + 1).error;
                double h = mv.h(new double[]{sv.trackTraj.get(k1 + 1).x, sv.trackTraj.get(k1 + 1).y},
                        mv.measurements.get(k1 + 1).tilt,
                        mv.measurements.get(k1 + 1).wireMaxSag,
                        mv.measurements.get(k1 + 1).wireLen);
                svc = new org.jlab.rec.dc.trajectory.StateVec(sv.trackTraj.get(k1 + 1).x,
                        sv.trackTraj.get(k1 + 1).y,
                        sv.trackTraj.get(k1 + 1).tx,
                        sv.trackTraj.get(k1 + 1).ty);
                svc.setZ(sv.trackTraj.get(k1 + 1).z);
                svc.setB(sv.trackTraj.get(k1 + 1).B);
                path += sv.trackTraj.get(k1 + 1).deltaPath;
                svc.setPathLength(path);
                svc.setProjector(h);
                kfStateVecsAlongTrajectory.add(svc); 
                res = (mv.measurements.get(k1 + 1).x - h);
                chi2 += (mv.measurements.get(k1 + 1).x - h) * (mv.measurements.get(k1 + 1).x - h) / V;
                
                nRj[mv.measurements.get(k1 + 1).region-1]+=res*res/V;
            
            } 
        } 
        
    }

    private boolean isNonsingular(Matrix mat) {
        double matDet = mat.det();
        return Math.abs(matDet) >= 1.e-30;
    }

}
