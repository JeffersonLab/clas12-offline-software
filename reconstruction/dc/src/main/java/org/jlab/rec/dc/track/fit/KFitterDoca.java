package org.jlab.rec.dc.track.fit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.fit.StateVecsDoca.CovMat;
import org.jlab.rec.dc.track.fit.StateVecsDoca.StateVec;
import org.jlab.jnp.matrix.*;

/**
 * @author ziegler
 */
public class KFitterDoca {

    private static final Logger LOGGER = Logger.getLogger(KFitterDoca.class.getName());
    
    public boolean setFitFailed = false;

    private StateVecsDoca sv;
    private MeasVecsDoca mv = new MeasVecsDoca();

    public StateVec finalStateVec;
    public CovMat finalCovMat;
    private StateVec initialStateVec;
    private CovMat initialCovMat;
    public List<org.jlab.rec.dc.trajectory.StateVec> kfStateVecsAlongTrajectory;
    public int totNumIter = 30;
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
    
    public KFitterDoca(Track trk, DCGeant4Factory DcDetector,
                   boolean TimeBasedUsingHBtrack,
                   Swim swimmer, int c) {
        sv = new StateVecsDoca(swimmer);
        if (TimeBasedUsingHBtrack) {
            this.initFromHB(trk, DcDetector);
        } else {
            this.init(trk, DcDetector, c);
        }
    }
    boolean TBT =false;
    private void initFromHB(Track trk, DCGeant4Factory DcDetector) {
        mv.setMeasVecsFromHB(trk, DcDetector);
        int mSize = mv.measurements.size();
        sv.Z = new double[mSize];

        for (int i = 0; i < mSize; i++) {
            sv.Z[i] = mv.measurements.get(i).z;
        }
        sv.initFromHB(trk, sv.Z[0], this);
        TBT = true;
    }

    public final void init(Track trk, DCGeant4Factory DcDetector, int c) {
        mv.setMeasVecs(trk, DcDetector);
        int mSize = mv.measurements.size();

        sv.Z = new double[mSize];

        for (int i = 0; i < mSize; i++) {
            sv.Z[i] = mv.measurements.get(i).z;
        }
        sv.init(trk, sv.Z[0], this, c);
    }
    public int interNum = 0;
    double initChi2 = Double.POSITIVE_INFINITY;
    public void runFitter(int sector) {
        this.chi2 = Double.POSITIVE_INFINITY;
        double initChi2 = Double.POSITIVE_INFINITY;
        this.NDF = mv.ndf;
        int svzLength = sv.Z.length;

        if(TBT==true) {
            this.chi2kf = 0;
            // Get the input parameters
            for (int k = 0; k < svzLength - 1; k++) {
                    sv.transport(sector, k, k + 1,
                            sv.trackTraj.get(k),
                            sv.trackCov.get(k));
            }
            this.calcFinalChisq(sector);
            this.initialStateVec = sv.trackTraj.get(svzLength - 1);
            this.initialCovMat = sv.trackCov.get(svzLength - 1);
            this.finalStateVec = sv.trackTraj.get(svzLength - 1);
            this.finalCovMat = sv.trackCov.get(svzLength - 1);
            initChi2 = this.chi2; 
            if(Double.isNaN(chi2)) {
                this.setFitFailed = true;
                return;
            }
        }
    //        IntStream.range(1,totNumIter ).parallel().forEach(i -> {
        for (int i = 1; i <= totNumIter; i++) {
            interNum = i;
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
                    //LOGGER.log(Level.FINE, "sector " +sector+"stateVec "+sv.trackTraj.get(k).printInfo());
                    if(k>=2) {
                        sv.transport(sector, k, k - 2,
                            sv.trackTraj.get(k),
                            sv.trackCov.get(k));
                        this.filter(k - 2);
                        sv.transport(sector, k - 2, k - 1,
                            sv.trackTraj.get(k - 2),
                            sv.trackCov.get(k - 2));
                        this.filter(k - 1);
                    } else {
                        sv.transport(sector, 1, 0,
                            sv.trackTraj.get(1),
                            sv.trackCov.get(1));
                        this.filter(0);
                    }
                }
            }
            for (int k = 0; k < svzLength - 1; k++) {
                //if(i==2 && this.totNumIter==30)
                //LOGGER.log(Level.FINE, "stateVec "+sv.trackTraj.get(k).printInfo());
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
        if(Double.isNaN(chi2))
            this.setFitFailed = true;
        if(TBT==true) {
            if(chi2>initChi2) { // fit failed
                this.finalStateVec = this.initialStateVec;
                this.finalCovMat = this.initialCovMat;
                sv.trackTraj.put(svzLength - 1,this.initialStateVec);
                sv.trackCov.put(svzLength - 1, this.initialCovMat);
                this.calcFinalChisq(sector);
            }
        }
    }

    public Matrix filterCovMat(double[] H, Matrix Ci, double V) {
        
        double det = Matrix5x5.inverse(Ci, first_inverse, adj);
        if(Math.abs(det)<1.e-30)
            return null;
        
        addition.set(
                    H[0] * H[0] / V, H[0] * H[1] / V, 0, 0, 0,
                    H[0] * H[1] / V, H[1] * H[1] / V, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0);
        //LOGGER.log(Level.FINE, "Ci ");
        //Matrix5x5.show(Ci);
        //LOGGER.log(Level.FINE, "Cinv ");
        //Matrix5x5.show(first_inverse);
        //LOGGER.log(Level.FINE, "addition ");
        //Matrix5x5.show(addition);
        
        Matrix5x5.add(first_inverse, addition, result);
        double det2 = Matrix5x5.inverse(result, result_inv, adj);
        //LOGGER.log(Level.FINE, "addition result");
        //Matrix5x5.show(result);
        //LOGGER.log(Level.FINE, "inv result");
        //Matrix5x5.show(result_inv);
        if(Math.abs(det2)<1.e-30)
            return null;
        
        return result_inv;
    }
    private double KFScale = 4;
    private void filter(int k) {
        if(Double.isNaN(sv.trackTraj.get(k).x) || Double.isNaN(sv.trackTraj.get(k).y) 
                || Double.isNaN(sv.trackTraj.get(k).tx) ||Double.isNaN(sv.trackTraj.get(k).ty )
                || Double.isNaN(sv.trackTraj.get(k).Q)) {
                this.setFitFailed = true;
                return;
        }
        if (sv.trackTraj.get(k) != null &&
                sv.trackCov.get(k).covMat != null &&
                k < sv.Z.length 
                && mv.measurements.get(k).reject==false) {
            
            
            double[] K = new double[5];
            double V = mv.measurements.get(k).unc[0]*KFScale;
            double[] H = mv.H(new double[]{sv.trackTraj.get(k).x, sv.trackTraj.get(k).y},
                    mv.measurements.get(k).z,
                    mv.measurements.get(k).wireLine[0]);
            Matrix CaInv = this.filterCovMat(H, sv.trackCov.get(k).covMat, V);
            if (CaInv != null) {
                    sv.trackCov.get(k).covMat = CaInv;
                } else {
                    return;
            }

            for (int j = 0; j < 5; j++) {
                // the gain matrix
                K[j] = (H[0] * sv.trackCov.get(k).covMat.get(j, 0) +
                        H[1] * sv.trackCov.get(k).covMat.get(j, 1)) / V;
            }
            
            double h = mv.h(new double[]{sv.trackTraj.get(k).x, sv.trackTraj.get(k).y},
                    mv.measurements.get(k).z,
                    mv.measurements.get(k).wireLine[0]);
            
            double signMeas = 1;
            double sign = 1;
            if(mv.measurements.get(k).doca[1]!=-99 || 
                    !(Math.abs(mv.measurements.get(k).doca[0])<0.5 && mv.measurements.get(k).doca[1]==-99 ) ) { //use LR only for double hits && large enough docas
                signMeas = Math.signum(mv.measurements.get(k).doca[0]);
                sign = Math.signum(h);
            } else {
                signMeas = Math.signum(h);
                sign = Math.signum(h);
            }
            //if(this.interNum>1)
            //    signMeas = Math.signum(h);
            double c2 = ((signMeas*Math.abs(mv.measurements.get(k).doca[0]) - sign*Math.abs(h)) 
                    * (signMeas*Math.abs(mv.measurements.get(k).doca[0]) - sign*Math.abs(h)) / V);
            //if(signMeas!=Math.signum(h) && this.interNum>1) LOGGER.log(Level.FINE, sv.trackTraj.get(k).printInfo()+" h "+(float)h);
            double x_filt = sv.trackTraj.get(k).x + K[0] * (signMeas*Math.abs(mv.measurements.get(k).doca[0]) - sign*Math.abs(h));
            double y_filt = sv.trackTraj.get(k).y + K[1] * (signMeas*Math.abs(mv.measurements.get(k).doca[0]) - sign*Math.abs(h));
            double tx_filt = sv.trackTraj.get(k).tx + K[2] * (signMeas*Math.abs(mv.measurements.get(k).doca[0]) - sign*Math.abs(h));
            double ty_filt = sv.trackTraj.get(k).ty + K[3] * (signMeas*Math.abs(mv.measurements.get(k).doca[0]) - sign*Math.abs(h));
            double Q_filt = sv.trackTraj.get(k).Q + K[4] * (signMeas*Math.abs(mv.measurements.get(k).doca[0]) - sign*Math.abs(h));
               
            //USE THE DOUBLE HIT
            if(mv.measurements.get(k).doca[1]!=-99) { 
                //now filter using the other Hit
                V = mv.measurements.get(k).unc[1]*KFScale;
                H = mv.H(new double[]{x_filt, y_filt},
                    mv.measurements.get(k).z,
                    mv.measurements.get(k).wireLine[1]);
                CaInv = this.filterCovMat(H, sv.trackCov.get(k).covMat, V);
                if (CaInv != null) {
                        sv.trackCov.get(k).covMat = CaInv;
                    } else {
                        return;
                }
                for (int j = 0; j < 5; j++) {
                // the gain matrix
                K[j] = (H[0] * sv.trackCov.get(k).covMat.get(j, 0) +
                        H[1] * sv.trackCov.get(k).covMat.get(j, 1)) / V;
                }
                h=mv.h(new double[]{x_filt, y_filt},
                    mv.measurements.get(k).z,
                    mv.measurements.get(k).wireLine[1]);   
            
                signMeas = Math.signum(mv.measurements.get(k).doca[1]);
                sign = Math.signum(h);
                //if(this.interNum>1)
                //    signMeas = Math.signum(h);
                
                x_filt += K[0] * (signMeas*Math.abs(mv.measurements.get(k).doca[1]) - sign*Math.abs(h));
                y_filt += K[1] * (signMeas*Math.abs(mv.measurements.get(k).doca[1]) - sign*Math.abs(h));
                tx_filt += K[2] * (signMeas*Math.abs(mv.measurements.get(k).doca[1]) - sign*Math.abs(h));
                ty_filt += K[3] * (signMeas*Math.abs(mv.measurements.get(k).doca[1]) - sign*Math.abs(h));
                Q_filt += K[4] * (signMeas*Math.abs(mv.measurements.get(k).doca[1]) - sign*Math.abs(h));
              
                c2 += ((signMeas*Math.abs(mv.measurements.get(k).doca[1]) - sign*Math.abs(h)) 
                        * (signMeas*Math.abs(mv.measurements.get(k).doca[1]) - sign*Math.abs(h)) / V);
            } 
            
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

    public Matrix propagateToVtx(int sector, double Zf) {
        return sv.transport(sector, 0, Zf, sv.trackTraj.get(0), sv.trackCov.get(0));
        
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
            double V0 = mv.measurements.get(0).unc[0];
            double h0 = mv.h(new double[]{sv.trackTraj.get(0).x, sv.trackTraj.get(0).y},
                    mv.measurements.get(0).z,
                    mv.measurements.get(0).wireLine[0]);
            svc.setProjector(mv.measurements.get(0).wireLine[0].origin().x());
            svc.setProjectorDoca(h0);
            kfStateVecsAlongTrajectory.add(svc); 
            double res = (mv.measurements.get(0).doca[0] - h0);
            chi2 += (mv.measurements.get(0).doca[0] - h0) * (mv.measurements.get(0).doca[0] - h0) / V0;
            nRj[mv.measurements.get(0).region-1]+=res*res/mv.measurements.get(0).error;
            //USE THE DOUBLE HIT
            if(mv.measurements.get(0).doca[1]!=-99) { 
                V0 = mv.measurements.get(0).unc[1];
                h0 = mv.h(new double[]{sv.trackTraj.get(0).x, sv.trackTraj.get(0).y},
                    mv.measurements.get(0).z,
                    mv.measurements.get(0).wireLine[1]);
                res = (mv.measurements.get(0).doca[1] - h0);
                chi2 += (mv.measurements.get(0).doca[1] - h0) * (mv.measurements.get(0).doca[1] - h0) / V0;
                nRj[mv.measurements.get(0).region-1]+=res*res/mv.measurements.get(0).error;
                svc.setProjector(mv.measurements.get(0).wireLine[1].origin().x());
                svc.setProjectorDoca(h0);
                kfStateVecsAlongTrajectory.add(svc); 
            }
            for (int k1 = 0; k1 < k; k1++) {
                sv.transport(sector, k1, k1 + 1, sv.trackTraj.get(k1), sv.trackCov.get(k1));

                double V = mv.measurements.get(k1 + 1).unc[0];
                double h = mv.h(new double[]{sv.trackTraj.get(k1 + 1).x, sv.trackTraj.get(k1 + 1).y},
                    mv.measurements.get(k1 + 1).z,
                    mv.measurements.get(k1 + 1).wireLine[0]);
                svc = new org.jlab.rec.dc.trajectory.StateVec(sv.trackTraj.get(k1 + 1).x,
                        sv.trackTraj.get(k1 + 1).y,
                        sv.trackTraj.get(k1 + 1).tx,
                        sv.trackTraj.get(k1 + 1).ty);
                svc.setZ(sv.trackTraj.get(k1 + 1).z);
                svc.setB(sv.trackTraj.get(k1 + 1).B);
                path += sv.trackTraj.get(k1 + 1).deltaPath;
                svc.setPathLength(path);
                svc.setProjector(mv.measurements.get(k1 + 1).wireLine[0].origin().x());
                svc.setProjectorDoca(h);
                kfStateVecsAlongTrajectory.add(svc); 
                res = (mv.measurements.get(k1 + 1).doca[0]  - h); 
                chi2 += (mv.measurements.get(k1 + 1).doca[0]  - h) * (mv.measurements.get(k1 + 1).doca[0]  - h) / V;
                nRj[mv.measurements.get(k1 + 1).region-1]+=res*res/V;
                //USE THE DOUBLE HIT
                if(mv.measurements.get(k1 + 1).doca[1]!=-99) { 
                    V = mv.measurements.get(k1 + 1).unc[1];
                    h = mv.h(new double[]{sv.trackTraj.get(k1 + 1).x, sv.trackTraj.get(k1 + 1).y},
                    mv.measurements.get(k1 + 1).z,
                    mv.measurements.get(k1 + 1).wireLine[1]);
                    res = (mv.measurements.get(k1 + 1).doca[1]  - h);
                    chi2 += (mv.measurements.get(k1 + 1).doca[1]  - h) * (mv.measurements.get(k1 + 1).doca[1]  - h) / V;
                    nRj[mv.measurements.get(k1 + 1).region-1]+=res*res/V;
                    svc.setProjector(mv.measurements.get(k1 + 1).wireLine[1].origin().x());
                    svc.setProjectorDoca(h);
                    kfStateVecsAlongTrajectory.add(svc); 
                }
            } 
        } 
        
    }

    /*private boolean isNonsingular(Matrix mat) {
        double matDet = mat.det();
        return Math.abs(matDet) >= 1.e-30;
    }*/

}
