package org.jlab.rec.fvt.track.fit;

import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.rec.fvt.track.fit.StateVecs.CovMat;
import org.jlab.rec.fvt.track.fit.StateVecs.StateVec;
import org.jlab.jnp.matrix.*;
import org.jlab.rec.fmt.cluster.Cluster;

/**
 * @author ziegler
 */
public class KFitter {

    public boolean setFitFailed = false;

    private StateVecs sv;
    private MeasVecs mv = new MeasVecs();

    public StateVec finalStateVec;
    public CovMat finalCovMat;
    public int totNumIter = 10;
    private double newChisq = Double.POSITIVE_INFINITY;
    public boolean filterOn = true;
    public double chi2 = 0;
    private double chi2kf = 0;
    public int NDF = 0;
    public int ConvStatus = 1;

    Matrix first_inverse = new Matrix();
    Matrix addition      = new Matrix();
    Matrix result        = new Matrix();
    Matrix result_inv    = new Matrix();
    Matrix adj           = new Matrix();
    
    public KFitter(List<Cluster> clusters,
            int sector, double xVtx, double yVtx, double zVtx, 
            double pxVtx, double pyVtx, double pzVtx,
            int q, 
             Swim swimmer, int c) {
        sv = new StateVecs(swimmer);
        this.init(clusters, 
            sector, xVtx, yVtx, zVtx, 
            pxVtx, pyVtx, pzVtx,
            q, c);
    }

    public void init(List<Cluster> clusters,
            int sector, double xVtx, double yVtx, double zVtx, 
            double pxVtx, double pyVtx, double pzVtx,
            int q, int c) {
        //initialize measVecs
        mv.setMeasVecs(clusters);
        int mSize = mv.measurements.size();

        sv.Z = new double[mSize];

        for (int i = 0; i < mSize; i++) {
            sv.Z[i] = mv.measurements.get(i).z;
        }
        //initialize stateVecs
        sv.init(sector, xVtx, yVtx, zVtx, 
                pxVtx, pyVtx, pzVtx, q,
                sv.Z[0], this, c);
    }
    public int interNum = 0;
    public void runFitter(int sector) {
        this.chi2 = 0;
        int svzLength = sv.Z.length;
        
        for (int i = 1; i <= totNumIter; i++) {
            interNum = i;
            this.chi2kf = 0;
            if (i > 1) {
                
                for (int k = svzLength - 1; k >0; k--) {
                    
                    //System.out.println("k " +k+"stateVec "+sv.trackTraj.get(k).printInfo());
                    if(k>=1) {
                        sv.transport(sector, k, k - 1,
                            sv.trackTraj.get(k),
                            sv.trackCov.get(k));
                        
                        this.filter(k - 1);
                    } 
                }
            }
            for (int k = 0; k < svzLength - 1; k++) {
                //System.out.println(k+") stateVec "+sv.trackTraj.get(k).printInfo());
                sv.transport(sector, k, k + 1,
                        sv.trackTraj.get(k),
                        sv.trackCov.get(k));
                //if(i==1 && k==0)
                //    Matrix5x5.show(sv.trackCov.get(k).covMat);
                this.filter(k + 1);
            }
            if (i > 1) {
                if(this.setFitFailed==true)
                    i = totNumIter;
                if (this.setFitFailed==false) {
//                    if(this.finalStateVec!=null) {
//                        if(Math.abs(sv.trackTraj.get(svzLength - 1).Q-this.finalStateVec.Q)<5.e-4 &&
//                                Math.abs(sv.trackTraj.get(svzLength - 1).x-this.finalStateVec.x)<1.e-4 &&
//                                Math.abs(sv.trackTraj.get(svzLength - 1).y-this.finalStateVec.y)<1.e-4 &&
//                                Math.abs(sv.trackTraj.get(svzLength - 1).tx-this.finalStateVec.tx)<1.e-6 &&
//                                Math.abs(sv.trackTraj.get(svzLength - 1).ty-this.finalStateVec.ty)<1.e-6) {
//                            i = totNumIter;
//                        }
//                    }
                    this.finalStateVec = sv.trackTraj.get(svzLength - 1);
                    this.finalCovMat = sv.trackCov.get(svzLength - 1);
                    
                } else {
                    this.ConvStatus = 1;
                }
            }
        }
        if(totNumIter==1) {
            this.finalStateVec = sv.trackTraj.get(svzLength - 1);
            this.finalCovMat = sv.trackCov.get(svzLength - 1);
        }
        
    }
    
    public Matrix filterCovMat(double[] H, Matrix Ci, double V) {
        
        double det = Matrix5x5.inverse(Ci, first_inverse, adj);
        if(Math.abs(det)<1.e-30)
            return null;
        
        addition.set(
                    H[0] * H[0] / V, H[0] * H[1] / V, H[0] * H[2] / V, H[0] * H[3] / V, H[0] * H[4] / V,
                    H[1] * H[0] / V, H[1] * H[1] / V, H[1] * H[2] / V, H[1] * H[3] / V, H[1] * H[4] / V,
                    H[2] * H[0] / V, H[2] * H[1] / V, H[2] * H[2] / V, H[2] * H[3] / V, H[2] * H[4] / V,
                    H[3] * H[0] / V, H[3] * H[1] / V, H[3] * H[2] / V, H[3] * H[3] / V, H[3] * H[4] / V,
                    H[4] * H[0] / V, H[4] * H[1] / V, H[4] * H[2] / V, H[4] * H[3] / V, H[4] * H[4] / V
                    );
        //System.out.println("Ci ");
        //Matrix5x5.show(Ci);
        //System.out.println("Cinv ");
        //Matrix5x5.show(first_inverse);
        //System.out.println("addition ");
        //Matrix5x5.show(addition);
        
        Matrix5x5.add(first_inverse, addition, result);
        double det2 = Matrix5x5.inverse(result, result_inv, adj);
        
        if(Math.abs(det2)<1.e-30)
            return null;
        
        return result_inv;
    }
    private void filter(int k) {
        if (sv.trackTraj.get(k) != null &&
                sv.trackCov.get(k).covMat != null &&
                k < sv.Z.length ) {
            
            
            double[] K = new double[5];
            double V = mv.measurements.get(k).error;
            
            double[] H = mv.H(sv.trackTraj.get(k),sv);
            Matrix CaInv = this.filterCovMat(H, sv.trackCov.get(k).covMat, V);
            if (CaInv != null) {
                    sv.trackCov.get(k).covMat = CaInv;
                } else {
                    return;
            }

            for (int j = 0; j < 5; j++) {
                // the gain matrix
                K[j] = 0;
                for (int i = 0; i < 5; i++) {
                    K[j] += H[i] * sv.trackCov.get(k).covMat.get(j, i) / V ;

                 //System.out.println((float)sv.trackTraj.get(k).z+" H["+i+" ] = "+H[i]+" sv.trackCov.get(k).covMat.get("+j+","+i+") = "+(sv.trackCov.get(k).covMat.get(j, i) ) +")-->K ["+j+" ] = "+K[j]);
                }
            }
            double h = mv.h(sv.trackTraj.get(k));
            double m = mv.measurements.get(k).centroid;
            
            
            double c2 = (m-h)*(m-h);
            //if(signMeas!=Math.signum(h) && this.interNum>1) System.out.println(sv.trackTraj.get(k).printInfo()+" h "+(float)h);
            double x_filt = sv.trackTraj.get(k).x + K[0] * (m-h);
            double y_filt = sv.trackTraj.get(k).y + K[1] * (m-h);
            double tx_filt = sv.trackTraj.get(k).tx + 0*K[2] * (m-h);
            double ty_filt = sv.trackTraj.get(k).ty + 0*K[3] * (m-h);
            double Q_filt = sv.trackTraj.get(k).Q + 0*K[4] * (m-h);
               
        
            
            chi2kf += c2;
            if(filterOn) {
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
