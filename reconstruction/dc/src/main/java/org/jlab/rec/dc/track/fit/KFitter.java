package org.jlab.rec.dc.track.fit;

import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.fit.StateVecs.StateVec;

import Jama.Matrix;
import org.jlab.rec.dc.track.fit.StateVecs;

public class KFitter {

    public boolean setFitFailed = false;

    StateVecs sv = new StateVecs();
    MeasVecs mv = new MeasVecs();

    public StateVec finalStateVec;

    public KFitter(Track trk) {
        this.init(trk);
    }

    public void init(Track trk) {
        mv.setMeasVecs(trk);
        sv.Z = new double[mv.measurements.size()];

        for (int i = 0; i < mv.measurements.size(); i++) {
            sv.Z[i] = mv.measurements.get(i).z;
        }
        sv.init(trk, sv.Z[0], this);
    }

    public int totNumIter = 10;
    double newChisq = Double.POSITIVE_INFINITY;

    public void runFitter() {
        this.chi2 = 0;
        this.NDF = sv.Z.length - 5;

        for (int i = 1; i <= totNumIter; i++) {
            if (i > 1) {
                sv.transport(sv.Z.length - 1, 0, sv.trackTraj.get(sv.Z.length - 1), sv.trackCov.get(sv.Z.length - 1)); //get new state vec at 1st measurement after propagating back from the last filtered state
            }
            for (int k = 0; k < sv.Z.length - 1; k++) {
                sv.transport(k, k + 1, sv.trackTraj.get(k), sv.trackCov.get(k));
                //sv.trackTraj.add(k+1, sv.StateVec); 
                //sv.trackCov.add(k+1, sv.CovMat);
                System.out.println((k+1)+"] trans "+sv.trackTraj.get(k+1).x+","+sv.trackTraj.get(k+1).y+","+
                		sv.trackTraj.get(k+1).z+","+sv.trackTraj.get(k+1).tx+","+sv.trackTraj.get(k+1).ty); 
                this.filter(k + 1);
            }
            this.calcFinalChisq();
            if (this.chi2 < newChisq) {
                this.finalStateVec = sv.trackTraj.get(sv.Z.length - 1);
                newChisq = this.chi2;
            } else {
                i = totNumIter;
            }
        }

    }
    public double chi2 = 0;
    public int NDF = 0;

    private void filter(int k) {
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null && k < sv.Z.length) {
            double[] K = new double[5];
            double V = mv.measurements.get(k).unc;
            double[] H = mv.H(mv.measurements.get(k).tilt);

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

            double h = mv.h(new double[]{sv.trackTraj.get(k).x, sv.trackTraj.get(k).y}, (int) mv.measurements.get(k).tilt);
            //double c2 = ((1 - (H[0]*K[0] + H[1]*K[1]))*(1 - (H[0]*K[0] + H[1]*K[1]))*(mv.measurements.get(k).x - h)*(mv.measurements.get(k).x - h)/V);

            double x_filt = sv.trackTraj.get(k).x + K[0] * (mv.measurements.get(k).x - h);
            double y_filt = sv.trackTraj.get(k).y + K[1] * (mv.measurements.get(k).x - h);
            double tx_filt = sv.trackTraj.get(k).tx + K[2] * (mv.measurements.get(k).x - h);
            double ty_filt = sv.trackTraj.get(k).ty + K[3] * (mv.measurements.get(k).x - h);
            double Q_filt = sv.trackTraj.get(k).Q + K[4] * (mv.measurements.get(k).x - h);

            //chi2 += c2; System.out.println("KFchi2 "+chi2);
            //if(chi2<10) { 
            sv.trackTraj.get(k).x = x_filt;
            sv.trackTraj.get(k).y = y_filt;
            sv.trackTraj.get(k).tx = tx_filt;
            sv.trackTraj.get(k).ty = ty_filt;
            sv.trackTraj.get(k).Q = Q_filt;
            //}

        }
    }

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
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null) {
            sv.rinit(sv.Z[0], k);
            for (int k1 = 0; k1 < k; k1++) {
                sv.transport(k1, k1 + 1, sv.trackTraj.get(k1), sv.trackCov.get(k1));
                double V = mv.measurements.get(k1 + 1).error;
                double h = mv.h(new double[]{sv.trackTraj.get(k1 + 1).x, sv.trackTraj.get(k1 + 1).y}, (int) mv.measurements.get(k1 + 1).tilt);

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
            System.out.println(C.get(k, 0) + "	" + C.get(k, 1) + "	" + C.get(k, 2) + "	" + C.get(k, 3) + "	" + C.get(k, 4));
        }
    }

    private boolean isNonsingular(Matrix mat) {

        for (int j = 0; j < mat.getColumnDimension(); j++) {
            if (mat.get(j, j) < 0.00000000001) {
                return false;
            }
        }
        return true;
    }

}
