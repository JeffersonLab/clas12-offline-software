package org.jlab.rec.fmt.track.fit;

import java.util.List;

import org.jlab.clas.swimtools.Swim;
import org.jlab.rec.fmt.track.fit.StateVecs.CovMat;
import org.jlab.rec.fmt.track.fit.StateVecs.StateVec;
import org.jlab.jnp.matrix.*;
import org.jlab.rec.fmt.cluster.Cluster;
import org.jlab.rec.fmt.track.Track;
import org.jlab.rec.fmt.track.Trajectory;

/**
 * @author ziegler
 */
public class KFitter {

    public boolean setFitFailed = false;

    private StateVecs sv;
    private MeasVecs mv = new MeasVecs();

    public StateVec finalStateVec = null;
    public CovMat finalCovMat;
    public int totNumIter = 10;
    public boolean filterOn = true;
    public double chi2 = 0;
    public int NDF = 0;
    public int ConvStatus = 1;
    public int interNum = 0;

    Matrix first_inverse = new Matrix();
    Matrix addition      = new Matrix();
    Matrix result        = new Matrix();
    Matrix result_inv    = new Matrix();
    Matrix adj           = new Matrix();

    Track track = null;
    private List<Cluster> clusters;

    public KFitter(Track track, Swim swimmer, int c) {
        sv = new StateVecs(swimmer);
        this.track = track;
        this.clusters = track.getClusters();
        this.init(clusters, track.getSector(), track.getX(), track.getY(), track.getZ(), 
                            track.getPx(), track.getPy(), track.getPz(), track.getQ(), c);
    }

    public KFitter(List<Cluster> clusters, int sector, double xVtx, double yVtx, double zVtx,
            double pxVtx, double pyVtx, double pzVtx, int q, Swim swimmer, int c) {
        sv = new StateVecs(swimmer);
        this.track = new Track(0,sector, q, xVtx, yVtx, zVtx, pxVtx, pyVtx, pzVtx, clusters);
        this.clusters = clusters;
        this.init(clusters, sector, xVtx, yVtx, zVtx, pxVtx, pyVtx, pzVtx, q, c);
    }

    private void init(List<Cluster> clusters, int sector, double xVtx, double yVtx, double zVtx,
            double pxVtx, double pyVtx, double pzVtx, int q, int c) {
        // initialize measVecs
        mv.setMeasVecs(clusters);
        int mSize = mv.measurements.size();

        sv.Z = new double[mSize];

        for (int i = 0; i < mSize; i++) sv.Z[i] = mv.measurements.get(i).z;

        // initialize stateVecs
        sv.init(sector, xVtx, yVtx, zVtx, pxVtx, pyVtx, pzVtx, q, sv.Z[0], this, c);
    }

    public void runFitter(int sector) {
        int svzLength = sv.Z.length;

        for (int i = 1; i <= totNumIter; i++) {
            interNum = i;
            this.chi2 = 0;
            if (i > 1) {
                for (int k = svzLength - 1; k > 0; k--) {
                    if (k >= 1) {
                        sv.transport(sector, k, k - 1, sv.trackTraj.get(k), sv.trackCov.get(k));
                        this.filter(k - 1);
                    }
                }
            }
            for (int k = 0; k < svzLength - 1; k++) {
                sv.transport(sector, k, k + 1, sv.trackTraj.get(k), sv.trackCov.get(k));
                this.filter(k + 1);
            }
            if (i > 1) {
                if (this.setFitFailed) i = totNumIter;
                if (!this.setFitFailed) {
                    this.finalStateVec = sv.trackTraj.get(svzLength - 1);
                    this.finalCovMat = sv.trackCov.get(svzLength - 1);
                } else {
                    this.ConvStatus = 1;
                }
            }
        }
        if (totNumIter == 1) {
            this.finalStateVec = sv.trackTraj.get(svzLength - 1);
            this.finalCovMat = sv.trackCov.get(svzLength - 1);
        }

        // Do one final pass to get the final chi^2 and the corresponding centroid residuals.
        this.chi2 = 0;
        for (int k = svzLength - 1; k > 0; --k) {
            if (k >= 1) {
                sv.transport(sector, k, k-1, sv.trackTraj.get(k), sv.trackCov.get(k));
                this.filter(k - 1);
            }
        }
        for (int k = 0; k < svzLength - 1; ++k) {
            sv.transport(sector, k, k+1, sv.trackTraj.get(k), sv.trackCov.get(k));
        }

        
        // save final trajectory points
        if(this.finalStateVec!=null) {
            for (int k = 0; k < svzLength; ++k) {
                Trajectory trj = new Trajectory(mv.measurements.get(k).layer,
                                                sv.trackTraj.get(k).x,
                                                sv.trackTraj.get(k).y,
                                                sv.trackTraj.get(k).z,
                                                sv.trackTraj.get(k).tx,
                                                sv.trackTraj.get(k).ty,
                                                0,
                                                sv.trackTraj.get(k).deltaPath);
                track.setFMTtraj(trj);
            }
        }
        
//        for (int li = 1; li <= 6; ++li) {
//            // Get the state vector closest in z to the FMT layer.
//            // NOTE: A simple optimization would be to do this on another for loop with only the
//            //       state vectors, saving the ones closest to the FMT layers to use them later
//            //       instead of looping through them 6 times.
//            int closestSVID = -1;
//            double closestSVDistance = Double.POSITIVE_INFINITY;
//            for (int si = 0; si < sv.trackTraj.size(); ++si) {
//                double svDistance = Math.abs(sv.trackTraj.get(si).z - Geometry.getLayerZ(li-1));
//                if (svDistance < closestSVDistance) {
//                    closestSVID = si;
//                    closestSVDistance = svDistance;
//                }
//            }
//
//            // Get the state vector's y position in the layer's local coordinates.
//            Point3D Pos = new Point3D(sv.trackTraj.get(closestSVID).x, sv.trackTraj.get(closestSVID).y, 0);
//            Point3D locPos = Geometry.globalToLocal(Pos,li);
//
//            // Store the cluster's residual.
//            for (Cluster cl : this.clusters) {
//                if (cl.get_Layer() == li) {
//                    cl.set_CentroidResidual(cl.get_Centroid() - locPos.y());
//                }
//            }
//        }
    }

    public Matrix filterCovMat(double[] H, Matrix Ci, double V) {
        double det = Matrix5x5.inverse(Ci, first_inverse, adj);
        if (Math.abs(det) < 1.e-30) return null;

        addition.set(
                    H[0] * H[0] / V, H[0] * H[1] / V, H[0] * H[2] / V, H[0] * H[3] / V, H[0] * H[4] / V,
                    H[1] * H[0] / V, H[1] * H[1] / V, H[1] * H[2] / V, H[1] * H[3] / V, H[1] * H[4] / V,
                    H[2] * H[0] / V, H[2] * H[1] / V, H[2] * H[2] / V, H[2] * H[3] / V, H[2] * H[4] / V,
                    H[3] * H[0] / V, H[3] * H[1] / V, H[3] * H[2] / V, H[3] * H[3] / V, H[3] * H[4] / V,
                    H[4] * H[0] / V, H[4] * H[1] / V, H[4] * H[2] / V, H[4] * H[3] / V, H[4] * H[4] / V
                    );

        Matrix5x5.add(first_inverse, addition, result);
        double det2 = Matrix5x5.inverse(result, result_inv, adj);

        if (Math.abs(det2) < 1.e-30) return null;

        return result_inv;
    }

    private void filter(int k) {
        if (sv.trackTraj.get(k) != null && sv.trackCov.get(k).covMat != null && k < sv.Z.length ) {
            double[] K = new double[5];
            double V = mv.measurements.get(k).error;

            double[] H = mv.H(sv.trackTraj.get(k),sv);
            Matrix CaInv = this.filterCovMat(H, sv.trackCov.get(k).covMat, V);
            if (CaInv != null) sv.trackCov.get(k).covMat = CaInv;
            else return;

            // Calculate the gain matrix.
            for (int j = 0; j < 5; j++) {
                K[j] = 0;
                for (int i = 0; i < 5; i++) K[j] += H[i] * sv.trackCov.get(k).covMat.get(j, i) / V ;
            }

            // Update Chi^2 and filtered state vector.
            double h = mv.h(sv.trackTraj.get(k));
            double m = mv.measurements.get(k).centroid;
            this.chi2 += ((m-h)*(m-h)/mv.measurements.get(k).error/mv.measurements.get(k).error);

            double x_filt = sv.trackTraj.get(k).x + K[0] * (m-h);
            double y_filt = sv.trackTraj.get(k).y + K[1] * (m-h);
            double tx_filt = sv.trackTraj.get(k).tx + 0*K[2] * (m-h);
            double ty_filt = sv.trackTraj.get(k).ty + 0*K[3] * (m-h);
            double Q_filt = sv.trackTraj.get(k).Q + 0*K[4] * (m-h);

            if (filterOn) {
                sv.trackTraj.get(k).x = x_filt;
                sv.trackTraj.get(k).y = y_filt;
                sv.trackTraj.get(k).tx = tx_filt;
                sv.trackTraj.get(k).ty = ty_filt;
                sv.trackTraj.get(k).Q = Q_filt;
            }
        }
    }

    public Matrix propagateToVtx(int sector, double Zf) {
        return sv.transport(sector, 0, Zf, sv.trackTraj.get(0), sv.trackCov.get(0));
    }
}
